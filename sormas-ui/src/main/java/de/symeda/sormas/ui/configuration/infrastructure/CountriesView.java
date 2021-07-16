package de.symeda.sormas.ui.configuration.infrastructure;

import java.util.Collections;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;
import com.vaadin.v7.ui.ComboBox;

import de.symeda.sormas.api.EntityRelevanceStatus;
import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.Descriptions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Strings;
import de.symeda.sormas.api.infrastructure.InfrastructureType;
import de.symeda.sormas.api.region.CountryCriteria;
import de.symeda.sormas.api.region.CountryDto;
import de.symeda.sormas.api.region.CountryIndexDto;
import de.symeda.sormas.api.region.SubcontinentReferenceDto;
import de.symeda.sormas.api.user.UserRight;
import de.symeda.sormas.ui.ControllerProvider;
import de.symeda.sormas.ui.UserProvider;
import de.symeda.sormas.ui.ViewModelProviders;
import de.symeda.sormas.ui.configuration.AbstractConfigurationView;
import de.symeda.sormas.ui.configuration.infrastructure.components.SearchField;
import de.symeda.sormas.ui.utils.ButtonHelper;
import de.symeda.sormas.ui.utils.ComboBoxHelper;
import de.symeda.sormas.ui.utils.CssStyles;
import de.symeda.sormas.ui.utils.ExportEntityName;
import de.symeda.sormas.ui.utils.GridExportStreamResource;
import de.symeda.sormas.ui.utils.MenuBarHelper;
import de.symeda.sormas.ui.utils.RowCount;
import de.symeda.sormas.ui.utils.VaadinUiUtil;
import de.symeda.sormas.ui.utils.ViewConfiguration;

public class CountriesView extends AbstractConfigurationView {

	private static final long serialVersionUID = -3900506227084862411L;

	public static final String VIEW_NAME = ROOT_VIEW_NAME + "/countries";

	private CountryCriteria criteria;
	private ViewConfiguration viewConfiguration;

	// Filter
	private SearchField searchField;
	private ComboBox subcontinentFilter;
	private ComboBox relevanceStatusFilter;
	private Button resetButton;

	private HorizontalLayout filterLayout;
	private VerticalLayout gridLayout;
	private CountriesGrid grid;
	private RowCount rowCount;
	protected Button createButton;
	protected Button importButton;
	protected Button importDefaultCountriesButton;
	private MenuBar bulkOperationsDropdown;

	public CountriesView() {
		super(VIEW_NAME);

		viewConfiguration = ViewModelProviders.of(CountriesView.class).get(ViewConfiguration.class);
		criteria = ViewModelProviders.of(CountriesView.class).get(CountryCriteria.class);
		if (criteria.getRelevanceStatus() == null) {
			criteria.relevanceStatus(EntityRelevanceStatus.ACTIVE);
		}
		grid = new CountriesGrid(criteria);
		gridLayout = new VerticalLayout();
		gridLayout.addComponent(createFilterBar());
		rowCount = new RowCount(Strings.labelNumberOfCountries, 0);
		gridLayout.addComponent(rowCount);
		gridLayout.addComponent(grid);
		gridLayout.setMargin(true);
		gridLayout.setSpacing(false);
		gridLayout.setExpandRatio(grid, 1);
		gridLayout.setSizeFull();
		gridLayout.setStyleName("crud-main-layout");

		if (UserProvider.getCurrent().hasUserRight(UserRight.INFRASTRUCTURE_IMPORT)) {
			importButton = ButtonHelper.createIconButton(Captions.actionImport, VaadinIcons.UPLOAD, e -> {
				Window window = VaadinUiUtil.showPopupWindow(new InfrastructureImportLayout(InfrastructureType.COUNTRY));
				window.setCaption(I18nProperties.getString(Strings.headingImportCountries));
				window.addCloseListener(c -> grid.reload());
			}, ValoTheme.BUTTON_PRIMARY);
			addHeaderComponent(importButton);

			importDefaultCountriesButton = ButtonHelper.createIconButton(Captions.actionImportAllCountries, VaadinIcons.UPLOAD, e -> {
				Window window = VaadinUiUtil.showPopupWindow(new ImportDefaultCountriesLayout());
				window.setCaption(I18nProperties.getString(Strings.headingImportAllCountries));
				window.addCloseListener(c -> grid.reload());
			}, ValoTheme.BUTTON_PRIMARY);
			addHeaderComponent(importDefaultCountriesButton);
		}

		if (UserProvider.getCurrent().hasUserRight(UserRight.INFRASTRUCTURE_EXPORT)) {
			Button exportButton = ButtonHelper.createIconButton(Captions.export, VaadinIcons.TABLE, null, ValoTheme.BUTTON_PRIMARY);
			exportButton.setDescription(I18nProperties.getDescription(Descriptions.descExportButton));
			addHeaderComponent(exportButton);

			StreamResource streamResource = GridExportStreamResource.createStreamResource(
				grid,
				ExportEntityName.COUNTRIES,
				Collections.singletonList(CountriesGrid.EDIT_BTN_ID),
				Collections.singletonList(CountryIndexDto.DEFAULT_NAME));
			FileDownloader fileDownloader = new FileDownloader(streamResource);
			fileDownloader.extend(exportButton);
		}

		if (UserProvider.getCurrent().hasUserRight(UserRight.INFRASTRUCTURE_CREATE)) {
			createButton = ButtonHelper.createIconButton(
				Captions.actionNewEntry,
				VaadinIcons.PLUS_CIRCLE,
				e -> ControllerProvider.getInfrastructureController().createCountry(),
				ValoTheme.BUTTON_PRIMARY);

			addHeaderComponent(createButton);
		}

		if (UserProvider.getCurrent().hasUserRight(UserRight.PERFORM_BULK_OPERATIONS)) {
			Button btnEnterBulkEditMode = ButtonHelper.createIconButton(Captions.actionEnterBulkEditMode, VaadinIcons.CHECK_SQUARE_O, null);
			btnEnterBulkEditMode.setVisible(!viewConfiguration.isInEagerMode());
			addHeaderComponent(btnEnterBulkEditMode);

			Button btnLeaveBulkEditMode =
				ButtonHelper.createIconButton(Captions.actionLeaveBulkEditMode, VaadinIcons.CLOSE, null, ValoTheme.BUTTON_PRIMARY);
			btnLeaveBulkEditMode.setVisible(viewConfiguration.isInEagerMode());
			addHeaderComponent(btnLeaveBulkEditMode);

			btnEnterBulkEditMode.addClickListener(e -> {
				bulkOperationsDropdown.setVisible(true);
				viewConfiguration.setInEagerMode(true);
				btnEnterBulkEditMode.setVisible(false);
				btnLeaveBulkEditMode.setVisible(true);
				searchField.setEnabled(false);
				grid.setInEagerMode(true);
				grid.reload();
			});
			btnLeaveBulkEditMode.addClickListener(e -> {
				bulkOperationsDropdown.setVisible(false);
				viewConfiguration.setInEagerMode(false);
				btnLeaveBulkEditMode.setVisible(false);
				btnEnterBulkEditMode.setVisible(true);
				searchField.setEnabled(true);
				navigateTo(criteria);
			});
		}

		addComponent(gridLayout);
	}

	private HorizontalLayout createFilterBar() {
		filterLayout = new HorizontalLayout();
		filterLayout.setMargin(false);
		filterLayout.setSpacing(true);
		filterLayout.setWidth(100, Unit.PERCENTAGE);

		searchField = new SearchField();
		searchField.addTextChangeListener(e -> {
			criteria.nameCodeLike(e.getText());
			grid.reload();
		});
		filterLayout.addComponent(searchField);

		subcontinentFilter = ComboBoxHelper.createComboBoxV7();
		subcontinentFilter.setId(CountryDto.SUBCONTINENT);
		subcontinentFilter.setWidth(140, Unit.PIXELS);
		subcontinentFilter.setCaption(I18nProperties.getPrefixCaption(CountryDto.I18N_PREFIX, CountryDto.SUBCONTINENT));
		subcontinentFilter.addItems(FacadeProvider.getSubcontinentFacade().getAllActiveAsReference());
		subcontinentFilter.addValueChangeListener(e -> {
			criteria.subcontinent((SubcontinentReferenceDto) e.getProperty().getValue());
			navigateTo(criteria);
		});
		filterLayout.addComponent(subcontinentFilter);

		resetButton = ButtonHelper.createButton(Captions.actionResetFilters, event -> {
			ViewModelProviders.of(CountriesView.class).remove(CountryCriteria.class);
			navigateTo(null);
		}, CssStyles.FORCE_CAPTION);
		resetButton.setVisible(false);

		filterLayout.addComponent(resetButton);

		HorizontalLayout actionButtonsLayout = new HorizontalLayout();
		actionButtonsLayout.setSpacing(true);
		{
			// Show active/archived/all dropdown
			if (UserProvider.getCurrent().hasUserRight(UserRight.INFRASTRUCTURE_VIEW)) {
				relevanceStatusFilter = ComboBoxHelper.createComboBoxV7();
				relevanceStatusFilter.setId("relevanceStatus");
				relevanceStatusFilter.setWidth(220, Unit.PERCENTAGE);
				relevanceStatusFilter.setNullSelectionAllowed(false);
				relevanceStatusFilter.addItems((Object[]) EntityRelevanceStatus.values());
				relevanceStatusFilter.setItemCaption(EntityRelevanceStatus.ACTIVE, I18nProperties.getCaption(Captions.countryActiveCountries));
				relevanceStatusFilter.setItemCaption(EntityRelevanceStatus.ARCHIVED, I18nProperties.getCaption(Captions.countryArchivedCountries));
				relevanceStatusFilter.setItemCaption(EntityRelevanceStatus.ALL, I18nProperties.getCaption(Captions.countryAllCountries));
				relevanceStatusFilter.addValueChangeListener(e -> {
					criteria.relevanceStatus((EntityRelevanceStatus) e.getProperty().getValue());
					navigateTo(criteria);
				});
				actionButtonsLayout.addComponent(relevanceStatusFilter);

				// Bulk operation dropdown
				if (UserProvider.getCurrent().hasUserRight(UserRight.PERFORM_BULK_OPERATIONS)) {
					bulkOperationsDropdown = MenuBarHelper.createDropDown(
						Captions.bulkActions,
						new MenuBarHelper.MenuBarItem(I18nProperties.getCaption(Captions.actionArchive), VaadinIcons.ARCHIVE, selectedItem -> {
							ControllerProvider.getInfrastructureController()
								.archiveOrDearchiveAllSelectedItems(
									true,
									grid.asMultiSelect().getSelectedItems(),
									InfrastructureType.COUNTRY,
									() -> navigateTo(criteria));
						}, EntityRelevanceStatus.ACTIVE.equals(criteria.getRelevanceStatus())),
						new MenuBarHelper.MenuBarItem(I18nProperties.getCaption(Captions.actionDearchive), VaadinIcons.ARCHIVE, selectedItem -> {
							ControllerProvider.getInfrastructureController()
								.archiveOrDearchiveAllSelectedItems(
									false,
									grid.asMultiSelect().getSelectedItems(),
									InfrastructureType.COUNTRY,
									() -> navigateTo(criteria));
						}, EntityRelevanceStatus.ARCHIVED.equals(criteria.getRelevanceStatus())));

					bulkOperationsDropdown
						.setVisible(viewConfiguration.isInEagerMode() && !EntityRelevanceStatus.ALL.equals(criteria.getRelevanceStatus()));
					actionButtonsLayout.addComponent(bulkOperationsDropdown);
				}
			}
		}
		filterLayout.addComponent(actionButtonsLayout);
		filterLayout.setComponentAlignment(actionButtonsLayout, Alignment.BOTTOM_RIGHT);
		filterLayout.setExpandRatio(actionButtonsLayout, 1);

		return filterLayout;
	}

	@Override
	public void enter(ViewChangeListener.ViewChangeEvent event) {

		super.enter(event);
		String params = event.getParameters().trim();
		if (params.startsWith("?")) {
			params = params.substring(1);
			criteria.fromUrlParams(params);
		}
		updateFilterComponents();
		grid.reload();
		rowCount.update(grid.getItemCount());
	}

	public void updateFilterComponents() {

		// TODO replace with Vaadin 8 databinding
		applyingCriteria = true;

		resetButton.setVisible(criteria.hasAnyFilterActive());

		if (relevanceStatusFilter != null) {
			relevanceStatusFilter.setValue(criteria.getRelevanceStatus());
		}
		searchField.setValue(criteria.getNameCodeLike());
		subcontinentFilter.setValue(criteria.getSubcontinent());

		applyingCriteria = false;
	}
}
