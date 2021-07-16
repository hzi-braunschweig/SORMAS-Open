package de.symeda.sormas.ui.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import com.vaadin.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.data.provider.DataProvider;
import com.vaadin.data.provider.Query;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.SerializableSupplier;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Label;
import com.vaadin.ui.renderers.HtmlRenderer;

import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Strings;
import de.symeda.sormas.api.user.UserRole;
import de.symeda.sormas.api.utils.criteria.BaseCriteria;
import de.symeda.sormas.api.utils.pseudonymization.PseudonymizableIndexDto;
import de.symeda.sormas.ui.UserProvider;

public class FilteredGrid<T, C extends BaseCriteria> extends Grid<T> {

	public static final String EDIT_BTN_ID = "edit";

	private static final long serialVersionUID = 8116377533153377424L;

	/**
	 * For lazy loading: Defines how many entries are loaded into the grid when new data needs to be loaded for the visible range.
	 */
	private static final int LAZY_BATCH_SIZE = 100;

	private C criteria;
	private boolean inEagerMode;

	public FilteredGrid(Class<T> beanType) {
		super(beanType);
		getDataCommunicator().setMinPushSize(LAZY_BATCH_SIZE);
	}

	public C getCriteria() {
		return criteria;
	}

	public void setCriteria(C criteria) {
		setCriteria(criteria, false);
	}

	public void setCriteria(C criteria, boolean ignoreDataProvider) {
		this.criteria = criteria;
		if (!ignoreDataProvider && !inEagerMode) {
			getFilteredDataProvider().setFilter(criteria);
		}
	}

	public boolean isInEagerMode() {
		return inEagerMode;
	}

	public void setInEagerMode(boolean inEagerMode) {
		this.inEagerMode = inEagerMode;
	}

	@SuppressWarnings("unchecked")
	public ConfigurableFilterDataProvider<T, Void, C> getFilteredDataProvider() {
		return (ConfigurableFilterDataProvider<T, Void, C>) super.getDataProvider();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setDataProvider(DataProvider<T, ?> dataProvider) {
		if (!inEagerMode && !(dataProvider instanceof ConfigurableFilterDataProvider)) {
			dataProvider = (ConfigurableFilterDataProvider<T, Void, C>) dataProvider.withConfigurableFilter();
		}
		super.setDataProvider(dataProvider);
	}

	@Override
	public void setDataProvider(FetchItemsCallback<T> fetchItems, SerializableSupplier<Integer> sizeCallback) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Use this method before calling any bulkaction, to prevent illegal access to pseudonymized entries
	 */
	public void bulkActionHandler(Consumer<Set> callback) {
		if (PseudonymizableIndexDto.class.isAssignableFrom(getBeanType())
			&& getSelectedItems().stream().anyMatch(item -> ((PseudonymizableIndexDto) item).isPseudonymized())) {

			VaadinUiUtil.showConfirmationPopup(
				I18nProperties.getCaption(Captions.bulkActions),
				new Label(I18nProperties.getString(Strings.pseudonymizedEntitiesSelectedWarning)),
				I18nProperties.getCaption(Captions.actionDeselectAndContinue),
				I18nProperties.getCaption(Captions.actionCancel),
				640,
				proceed -> {
					if (proceed) {
						getSelectedItems().stream().filter(item -> ((PseudonymizableIndexDto) item).isPseudonymized()).forEach(this::deselect);
						callback.accept(getSelectedItems());
					}
				});
		} else {
			callback.accept(getSelectedItems());
		}
	}

	/**
	 * Use this method before calling any bulkaction, to prevent illegal access to pseudonymized entries
	 *
	 * @param allowAdminOverride
	 *            allow admins to perform this action even on pseudonymized entries
	 */
	public void bulkActionHandler(Consumer<Set> callback, boolean allowAdminOverride) {
		if (allowAdminOverride && UserProvider.getCurrent().hasUserRole(UserRole.ADMIN)) {
			callback.accept(getSelectedItems());
		} else {
			bulkActionHandler(callback);
		}

	}

	public int getItemCount() {
		return getDataProvider().size(new Query<>());
	}

	/**
	 * Add's a column to the left hand side of the grid complete with an edit-logo
	 *
	 * @param handler
	 *            ItemClickListener
	 */
	protected void addEditColumn(Consumer<T> handler) {

		List<Column<T, ?>> columnsList = new ArrayList<>(getColumns());

		Column<T, String> editColumn = addColumn(entry -> VaadinIcons.EDIT.getHtml(), new HtmlRenderer());
		editColumn.setId(EDIT_BTN_ID);
		editColumn.setCaption(I18nProperties.getCaption(EDIT_BTN_ID));
		editColumn.setSortable(false);
		editColumn.setWidth(20);

		// the edit column should always be on the left for consistency and to prevent sidescrolling
		columnsList.add(0, editColumn);
		setColumnOrder(columnsList.toArray(new Column[columnsList.size()]));

		addItemClickListener(new ShowDetailsListener<>(EDIT_BTN_ID, e -> handler.accept(e)));
	}

	protected void removeColumnIfExists(String columnId) {
		if (getColumn(columnId) != null) {
			removeColumn(columnId);
		}
	}
}
