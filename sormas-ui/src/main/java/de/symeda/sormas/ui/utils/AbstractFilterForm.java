package de.symeda.sormas.ui.utils;

import static de.symeda.sormas.ui.utils.LayoutUtil.div;
import static de.symeda.sormas.ui.utils.LayoutUtil.filterLocs;
import static de.symeda.sormas.ui.utils.LayoutUtil.loc;

import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;

import com.vaadin.ui.Button;
import com.vaadin.ui.CustomLayout;
import com.vaadin.v7.data.Property;
import com.vaadin.v7.data.fieldgroup.FieldGroup;
import com.vaadin.v7.data.util.converter.Converter;
import com.vaadin.v7.ui.AbstractTextField;
import com.vaadin.v7.ui.ComboBox;
import com.vaadin.v7.ui.Field;
import com.vaadin.v7.ui.PopupDateField;

import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.region.DistrictReferenceDto;
import de.symeda.sormas.api.region.RegionReferenceDto;
import de.symeda.sormas.api.user.UserDto;
import de.symeda.sormas.api.utils.fieldvisibility.FieldVisibilityCheckers;
import de.symeda.sormas.ui.UserProvider;
import de.symeda.sormas.ui.utils.components.FormActionButtonsComponent;

public abstract class AbstractFilterForm<T> extends AbstractForm<T> {

	private static final long serialVersionUID = -692949260096914243L;

	public static final String FILTER_ITEM_STYLE = "filter-item";

	private static final String ACTION_BUTTONS_ID = "actionButtons";
	private static final String MORE_FILTERS_ID = "moreFilters";

	private CustomLayout moreFiltersLayout;
	private boolean skipChangeEvents;
	private boolean hasFilter;

	protected FormActionButtonsComponent formActionButtonsComponent;

	protected AbstractFilterForm(Class<T> type, String propertyI18nPrefix) {
		this(type, propertyI18nPrefix, null);
	}

	protected AbstractFilterForm(Class<T> type, String propertyI18nPrefix, FieldVisibilityCheckers fieldVisibilityCheckers) {
		this(type, propertyI18nPrefix, fieldVisibilityCheckers, Captions.actionApplyFilters, Captions.actionResetFilters);
	}

	protected AbstractFilterForm(
		Class<T> type,
		String propertyI18nPrefix,
		FieldVisibilityCheckers fieldVisibilityCheckers,
		String applyCaptionTag,
		String resetCaptionTag) {

		super(type, propertyI18nPrefix, new SormasFieldGroupFieldFactory(fieldVisibilityCheckers, null), true);

		String moreFiltersHtmlLayout = createMoreFiltersHtmlLayout();
		boolean hasMoreFilters = moreFiltersHtmlLayout != null && moreFiltersHtmlLayout.length() > 0;

		if (hasMoreFilters) {
			moreFiltersLayout = new CustomLayout();
			moreFiltersLayout.setTemplateContents(moreFiltersHtmlLayout);
			moreFiltersLayout.setVisible(false);
			getContent().addComponent(moreFiltersLayout, MORE_FILTERS_ID);
		}

		formActionButtonsComponent = new FormActionButtonsComponent(applyCaptionTag, resetCaptionTag, moreFiltersLayout);
		getContent().addComponent(formActionButtonsComponent, ACTION_BUTTONS_ID);

		if (hasMoreFilters) {
			addMoreFilters(moreFiltersLayout);
		}

		this.addValueChangeListener(e -> onChange());

		addStyleName(CssStyles.FILTER_FORM);
	}

	public void onChange() {
		hasFilter = streamFieldsForEmptyCheck(getContent()).anyMatch(f -> !f.isEmpty());
	}

	@Override
	protected String createHtmlLayout() {
		return div(filterLocs(ArrayUtils.addAll(getMainFilterLocators(), ACTION_BUTTONS_ID)) + loc(MORE_FILTERS_ID));
	}

	protected abstract String[] getMainFilterLocators();

	protected UserDto currentUserDto() {
		UserProvider currentUser = UserProvider.getCurrent();
		if (currentUser != null) {
			return UserProvider.getCurrent().getUser();
		}
		return null;
	}

	protected String createMoreFiltersHtmlLayout() {
		return "";
	}

	public void addMoreFilters(CustomLayout moreFiltersContainer) {

	}

	protected CustomLayout getMoreFiltersContainer() {
		return moreFiltersLayout;
	}

	@Override
	@SuppressWarnings({
		"rawtypes",
		"unchecked" })
	protected <T1 extends Field> void formatField(T1 field, String propertyId) {

		super.formatField(field, propertyId);

		field.addStyleName(FILTER_ITEM_STYLE);

		String caption = I18nProperties.getPrefixCaption(propertyI18nPrefix, propertyId, field.getCaption());
		setFieldCaption(field, caption);

		field.addValueChangeListener(e -> onFieldValueChange(propertyId, e));
	}

	public void addResetHandler(Button.ClickListener resetHandler) {
		formActionButtonsComponent.addResetHandler(resetHandler);
	}

	public void addApplyHandler(Button.ClickListener applyHandler) {
		formActionButtonsComponent.addApplyHandler(applyHandler);
	}

	@Override
	@SuppressWarnings("rawtypes")
	protected void applyFieldConfiguration(FieldConfiguration configuration, Field field) {
		super.applyFieldConfiguration(configuration, field);

		if (configuration.getCaption() != null) {
			setFieldCaption(field, configuration.getCaption());
		}
	}

	@Override
	public void setValue(T newFieldValue) throws ReadOnlyException, Converter.ConversionException {

		doWithoutChangeHandler(() -> {
			super.setValue(newFieldValue);

			applyDependenciesOnNewValue(newFieldValue);

			if (moreFiltersLayout != null) {
				boolean hasExpandedFilter = streamFieldsForEmptyCheck(moreFiltersLayout).anyMatch(f -> !f.isEmpty());
				formActionButtonsComponent.toggleMoreFilters(hasExpandedFilter);
			}
		});
	}

	@SuppressWarnings("rawtypes")
	protected Stream<Field> streamFieldsForEmptyCheck(CustomLayout layout) {
		return FieldHelper.streamFields(layout);
	}

	protected void applyDependenciesOnNewValue(T newValue) {

	}

	protected void applyRegionFilterDependency(RegionReferenceDto region, String districtFieldId) {
		final UserDto user = currentUserDto();
		final ComboBox districtField = getField(districtFieldId);
		if (user.getRegion() != null && user.getDistrict() == null) {
			FieldHelper.updateItems(districtField, FacadeProvider.getDistrictFacade().getAllActiveByRegion(user.getRegion().getUuid()));
			districtField.setEnabled(true);
		} else {
			if (region != null) {
				FieldHelper.updateItems(districtField, FacadeProvider.getDistrictFacade().getAllActiveByRegion(region.getUuid()));
				districtField.setEnabled(true);
			} else {
				districtField.setEnabled(false);
			}
		}
	}

	protected void applyRegionAndDistrictFilterDependency(
		RegionReferenceDto region,
		String districtFieldId,
		DistrictReferenceDto district,
		String communityFieldId) {
		applyRegionFilterDependency(region, districtFieldId);
		applyDistrictDependency(district, communityFieldId);
	}

	protected void applyDistrictDependency(DistrictReferenceDto district, String communityFieldId) {
		final UserDto user = currentUserDto();
		final ComboBox communityField = getField(communityFieldId);
		if (user.getDistrict() != null && user.getCommunity() == null) {
			FieldHelper.updateItems(communityField, FacadeProvider.getCommunityFacade().getAllActiveByDistrict(user.getDistrict().getUuid()));
			communityField.setEnabled(true);
		} else {
			if (district != null) {
				FieldHelper.updateItems(communityField, FacadeProvider.getCommunityFacade().getAllActiveByDistrict(district.getUuid()));
				communityField.setEnabled(true);
			} else {
				communityField.setEnabled(false);
			}
		}
	}

	private void onFieldValueChange(String propertyId, Property.ValueChangeEvent event) {
		if (!skipChangeEvents) {
			try {
				doWithoutChangeHandler(() -> applyDependenciesOnFieldChange(propertyId, event));

				this.getFieldGroup().commit();
				this.fireValueChange(true);
			} catch (FieldGroup.CommitException ex) {
				ex.printStackTrace();
			}
		}
	}

	protected void applyDependenciesOnFieldChange(String propertyId, Property.ValueChangeEvent event) {
	}

	private void doWithoutChangeHandler(Callable callback) {
		this.skipChangeEvents = true;
		try {
			callback.call();
		} finally {
			this.skipChangeEvents = false;
		}
	}

	@SuppressWarnings("rawtypes")
	private <T1 extends Field> void setFieldCaption(T1 field, String caption) {
		field.setCaption(null);
		if (field instanceof ComboBox) {
			((ComboBox) field).setInputPrompt(caption);
		} else if (field instanceof AbstractTextField) {
			((AbstractTextField) field).setInputPrompt(caption);
		} else if (field instanceof PopupDateField) {
			((PopupDateField) field).setInputPrompt(caption);
		} else {
			field.setCaption(caption);
		}
	}

	public boolean hasFilter() {
		return hasFilter;
	}

	protected void clearAndDisableFields(Field... fields) {
		for (Field field : fields) {
			if (field != null) {
				field.setValue(null);
				field.setEnabled(false);
			}
		}
	}

	protected void clearAndDisableFields(String... propertyIds) {
		for (String propertyId : propertyIds) {
			Field<?> field = getField(propertyId);

			field.setValue(null);
			field.setEnabled(false);
		}
	}

	protected void enableFields(String... propertyIds) {
		updateFieldsEnabling(propertyIds, true);
	}

	protected void disableFields(String... propertyIds) {
		updateFieldsEnabling(propertyIds, false);
	}

	private void updateFieldsEnabling(String[] propertyIds, boolean enabled) {
		for (String propertyId : propertyIds) {
			getField(propertyId).setEnabled(enabled);
		}
	}

	protected void enableFields(Field... fields) {
		updateFieldsEnabling(fields, true);
	}

	protected void disableFields(Field... fields) {
		updateFieldsEnabling(fields, false);
	}

	private void updateFieldsEnabling(Field[] fields, boolean enabled) {
		for (Field field : fields) {
			if (field != null) {
				field.setEnabled(enabled);
			}
		}
	}

	interface Callable {

		void call();
	}
}
