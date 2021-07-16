/*
 * SORMAS® - Surveillance Outbreak Response Management & Analysis System
 * Copyright © 2016-2021 Helmholtz-Zentrum für Infektionsforschung GmbH (HZI)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package de.symeda.sormas.ui.contact;

import static de.symeda.sormas.ui.docgeneration.DocGenerationHelper.isDocGenerationAllowed;
import static de.symeda.sormas.ui.utils.FollowUpUtils.createFollowUpLegend;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.vaadin.hene.popupbutton.PopupButton;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.Page;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.DateField;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.Notification;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;
import com.vaadin.v7.ui.ComboBox;
import com.vaadin.v7.ui.OptionGroup;

import de.symeda.sormas.api.CountryHelper;
import de.symeda.sormas.api.EntityRelevanceStatus;
import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.ReferenceDto;
import de.symeda.sormas.api.bagexport.BAGExportContactDto;
import de.symeda.sormas.api.contact.ContactCriteria;
import de.symeda.sormas.api.contact.ContactIndexDto;
import de.symeda.sormas.api.contact.ContactStatus;
import de.symeda.sormas.api.docgeneneration.DocumentWorkflow;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.Descriptions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Strings;
import de.symeda.sormas.api.i18n.Validations;
import de.symeda.sormas.api.user.UserRight;
import de.symeda.sormas.api.utils.DateHelper;
import de.symeda.sormas.ui.ControllerProvider;
import de.symeda.sormas.ui.SormasUI;
import de.symeda.sormas.ui.UserProvider;
import de.symeda.sormas.ui.ViewModelProviders;
import de.symeda.sormas.ui.caze.CaseController;
import de.symeda.sormas.ui.contact.importer.ContactsImportLayout;
import de.symeda.sormas.ui.utils.AbstractView;
import de.symeda.sormas.ui.utils.ButtonHelper;
import de.symeda.sormas.ui.utils.ComboBoxHelper;
import de.symeda.sormas.ui.utils.ContactDownloadUtil;
import de.symeda.sormas.ui.utils.CssStyles;
import de.symeda.sormas.ui.utils.DateHelper8;
import de.symeda.sormas.ui.utils.DownloadUtil;
import de.symeda.sormas.ui.utils.ExportEntityName;
import de.symeda.sormas.ui.utils.FilteredGrid;
import de.symeda.sormas.ui.utils.GridExportStreamResource;
import de.symeda.sormas.ui.utils.LayoutUtil;
import de.symeda.sormas.ui.utils.MenuBarHelper;
import de.symeda.sormas.ui.utils.VaadinUiUtil;
import de.symeda.sormas.ui.utils.components.expandablebutton.ExpandableButton;

/**
 * A view for performing create-read-update-delete operations on products.
 * <p>
 * See also {@link CaseController} for fetching the data, the actual CRUD
 * operations and controlling the view based on events from outside.
 */
public class ContactsView extends AbstractView {

	private static final long serialVersionUID = -3533557348144005469L;

	private static final int MAX_FOLLOW_UP_VIEW_DAYS = 90;

	public static final String VIEW_NAME = "contacts";

	private final ContactCriteria criteria;
	private final ContactsViewConfiguration viewConfiguration;

	private final FilteredGrid<?, ContactCriteria> grid;

	private MenuBar bulkOperationsDropdown;
	private HashMap<Button, String> statusButtons;
	private Button activeStatusButton;

	// Filters
	private ContactsFilterForm filterForm;
	private ComboBox relevanceStatusFilter;

	private int followUpRangeInterval = 14;
	private boolean buttonPreviousOrNextClick = false;
	private Date followUpToDate;

	private Set<String> getSelectedRows() {
		AbstractContactGrid<?> contactGrid = (AbstractContactGrid<?>) this.grid;
		return this.viewConfiguration.isInEagerMode()
			? contactGrid.asMultiSelect().getSelectedItems().stream().map(ContactIndexDto::getUuid).collect(Collectors.toSet())
			: Collections.emptySet();
	}

	public ContactsView() {
		super(VIEW_NAME);

		viewConfiguration = ViewModelProviders.of(getClass()).get(ContactsViewConfiguration.class);
		if (viewConfiguration.getViewType() == null) {
			viewConfiguration.setViewType(ContactsViewType.CONTACTS_OVERVIEW);
		}
		criteria = ViewModelProviders.of(ContactsView.class).get(ContactCriteria.class);
		if (criteria.getRelevanceStatus() == null) {
			criteria.relevanceStatus(EntityRelevanceStatus.ACTIVE);
		}

		if (ContactsViewType.FOLLOW_UP_VISITS_OVERVIEW.equals(viewConfiguration.getViewType())) {
			criteria.reportDateTo(DateHelper.getEndOfDay(new Date()));
			criteria.followUpUntilFrom(DateHelper.getStartOfDay(DateHelper.subtractDays(new Date(), 4)));
			grid = new ContactFollowUpGrid(criteria, new Date(), followUpRangeInterval, getClass());
		} else {
			criteria.followUpUntilFrom(null);
			grid = ContactsViewType.DETAILED_OVERVIEW.equals(viewConfiguration.getViewType())
				? new ContactGridDetailed(criteria, getClass(), ContactsViewConfiguration.class)
				: new ContactGrid(criteria, getClass(), ContactsViewConfiguration.class);
			((AbstractContactGrid) grid).setDataProviderListener(e -> updateStatusButtons());
		}
		final VerticalLayout gridLayout = new VerticalLayout();
		gridLayout.addComponent(createFilterBar());
		gridLayout.addComponent(createStatusFilterBar());
		gridLayout.addComponent(grid);
		if (ContactsViewType.FOLLOW_UP_VISITS_OVERVIEW.equals(viewConfiguration.getViewType())) {
			gridLayout.addComponent(createFollowUpLegend());
		}
		gridLayout.setMargin(true);
		gridLayout.setSpacing(false);
		gridLayout.setSizeFull();
		gridLayout.setExpandRatio(grid, 1);
		gridLayout.setStyleName("crud-main-layout");
		grid.getDataProvider().addDataProviderListener(e -> updateStatusButtons());

		OptionGroup contactsViewSwitcher = new OptionGroup();
		contactsViewSwitcher.setId("contactsViewSwitcher");
		CssStyles.style(contactsViewSwitcher, CssStyles.FORCE_CAPTION, ValoTheme.OPTIONGROUP_HORIZONTAL, CssStyles.OPTIONGROUP_HORIZONTAL_PRIMARY);
		contactsViewSwitcher.addItem(ContactsViewType.CONTACTS_OVERVIEW);
		contactsViewSwitcher.setItemCaption(ContactsViewType.CONTACTS_OVERVIEW, I18nProperties.getCaption(Captions.contactContactsOverview));

		contactsViewSwitcher.addItem(ContactsViewType.DETAILED_OVERVIEW);
		contactsViewSwitcher.setItemCaption(ContactsViewType.DETAILED_OVERVIEW, I18nProperties.getCaption(Captions.contactDetailedOverview));

		contactsViewSwitcher.addItem(ContactsViewType.FOLLOW_UP_VISITS_OVERVIEW);
		contactsViewSwitcher
			.setItemCaption(ContactsViewType.FOLLOW_UP_VISITS_OVERVIEW, I18nProperties.getCaption(Captions.contactFollowUpVisitsOverview));

		contactsViewSwitcher.setValue(viewConfiguration.getViewType());
		contactsViewSwitcher.addValueChangeListener(e -> {
			ContactsViewType viewType = (ContactsViewType) e.getProperty().getValue();

			viewConfiguration.setViewType(viewType);
			SormasUI.get().getNavigator().navigateTo(ContactsView.VIEW_NAME);
		});
		addHeaderComponent(contactsViewSwitcher);

		final PopupButton moreButton = new PopupButton(I18nProperties.getCaption(Captions.moreActions));
		moreButton.setId("more");
		moreButton.setIcon(VaadinIcons.ELLIPSIS_DOTS_V);
		final VerticalLayout moreLayout = new VerticalLayout();
		moreLayout.setSpacing(true);
		moreLayout.setMargin(true);
		moreLayout.addStyleName(CssStyles.LAYOUT_MINIMAL);
		moreLayout.setWidth(250, Unit.PIXELS);
		moreButton.setContent(moreLayout);

		if (viewConfiguration.getViewType().isContactOverview() && UserProvider.getCurrent().hasUserRight(UserRight.CONTACT_IMPORT)) {
			Button importButton = ButtonHelper.createIconButton(Captions.actionImport, VaadinIcons.UPLOAD, e -> {
				Window popupWindow = VaadinUiUtil.showPopupWindow(new ContactsImportLayout());
				popupWindow.setCaption(I18nProperties.getString(Strings.headingImportContacts));
				popupWindow.addCloseListener(c -> {
					AbstractContactGrid<?> grid = (AbstractContactGrid<?>) this.grid;
					grid.reload();
				});
			}, ValoTheme.BUTTON_PRIMARY);
			importButton.setWidth(100, Unit.PERCENTAGE);

			moreLayout.addComponent(importButton);
		}

		if (viewConfiguration.getViewType().isContactOverview() && UserProvider.getCurrent().hasUserRight(UserRight.CONTACT_EXPORT)) {
			VerticalLayout exportLayout = new VerticalLayout();
			{
				exportLayout.setSpacing(true);
				exportLayout.setMargin(true);
				exportLayout.addStyleName(CssStyles.LAYOUT_MINIMAL);
				exportLayout.setWidth(200, Unit.PIXELS);
			}

			PopupButton exportButton = ButtonHelper.createIconPopupButton(Captions.export, VaadinIcons.DOWNLOAD, exportLayout);
			addHeaderComponent(exportButton);

			{
				StreamResource streamResource = GridExportStreamResource.createStreamResourceWithSelectedItems(
					grid,
					() -> this.viewConfiguration.isInEagerMode() ? this.grid.asMultiSelect().getSelectedItems() : Collections.emptySet(),
					ExportEntityName.CONTACTS);
				addExportButton(streamResource, exportButton, exportLayout, VaadinIcons.TABLE, Captions.exportBasic, Descriptions.descExportButton);
			}
			{
				StreamResource extendedExportStreamResource =
					ContactDownloadUtil.createContactExportResource(grid.getCriteria(), this::getSelectedRows, null);

				addExportButton(
					extendedExportStreamResource,
					exportButton,
					exportLayout,
					VaadinIcons.FILE_TEXT,
					Captions.exportDetailed,
					Descriptions.descDetailedExportButton);
			}

			if (UserProvider.getCurrent().hasUserRight(UserRight.VISIT_EXPORT)) {
				StreamResource followUpVisitsExportStreamResource =
					DownloadUtil.createVisitsExportStreamResource(grid.getCriteria(), this::getSelectedRows, ExportEntityName.CONTACT_FOLLOW_UPS);

				addExportButton(
					followUpVisitsExportStreamResource,
					exportButton,
					exportLayout,
					VaadinIcons.FILE_TEXT,
					Captions.exportFollowUp,
					Descriptions.descFollowUpExportButton);
			}

			{
				Button btnCustomExport = ButtonHelper.createIconButton(Captions.exportCustom, VaadinIcons.FILE_TEXT, e -> {
					ControllerProvider.getCustomExportController().openContactExportWindow(grid.getCriteria(), this::getSelectedRows);
				}, ValoTheme.BUTTON_PRIMARY);
				btnCustomExport.setDescription(I18nProperties.getString(Strings.infoCustomExport));
				btnCustomExport.setWidth(100, Unit.PERCENTAGE);
				exportLayout.addComponent(btnCustomExport);
			}

			if (FacadeProvider.getConfigFacade().isConfiguredCountry(CountryHelper.COUNTRY_CODE_SWITZERLAND)
				&& UserProvider.getCurrent().hasUserRight(UserRight.BAG_EXPORT)) {
				StreamResource bagExportResource = DownloadUtil.createCsvExportStreamResource(
					BAGExportContactDto.class,
					null,
					(Integer start, Integer max) -> FacadeProvider.getBAGExportFacade().getContactExportList(this.getSelectedRows(), start, max),
					(propertyId, type) -> propertyId,
					ExportEntityName.BAG_CONTACTS,
					null);

				addExportButton(bagExportResource, exportButton, exportLayout, VaadinIcons.FILE_TEXT, Captions.BAGExport, Strings.infoBAGExport);
			}

			// Warning if no filters have been selected
			{
				Label warningLabel = new Label(I18nProperties.getString(Strings.infoExportNoFilters));
				warningLabel.setId("contactWarningLabel");
				warningLabel.setWidth(100, Unit.PERCENTAGE);
				exportLayout.addComponent(warningLabel);
				warningLabel.setVisible(false);

				exportButton.addClickListener(e -> warningLabel.setVisible(!criteria.hasAnyFilterActive()));
			}
		}

		if (isBulkEditAllowed()) {
			Button btnEnterBulkEditMode = ButtonHelper.createIconButton(Captions.actionEnterBulkEditMode, VaadinIcons.CHECK_SQUARE_O, null);
			{
				btnEnterBulkEditMode.setVisible(!viewConfiguration.isInEagerMode());
				btnEnterBulkEditMode.addStyleName(ValoTheme.BUTTON_PRIMARY);

				btnEnterBulkEditMode.setWidth(100, Unit.PERCENTAGE);
				moreLayout.addComponent(btnEnterBulkEditMode);
			}

			Button btnLeaveBulkEditMode =
				ButtonHelper.createIconButton(Captions.actionLeaveBulkEditMode, VaadinIcons.CLOSE, null, ValoTheme.BUTTON_PRIMARY);
			{
				btnLeaveBulkEditMode.setVisible(viewConfiguration.isInEagerMode());
				btnLeaveBulkEditMode.setWidth(100, Unit.PERCENTAGE);

				moreLayout.addComponent(btnLeaveBulkEditMode);
			}

			btnEnterBulkEditMode.addClickListener(e -> {
				bulkOperationsDropdown.setVisible(true);
				ViewModelProviders.of(getClass()).get(ContactsViewConfiguration.class).setInEagerMode(true);
				btnEnterBulkEditMode.setVisible(false);
				btnLeaveBulkEditMode.setVisible(true);
				filterForm.setSearchFieldEnabled(false);
				((AbstractContactGrid<?>) grid).reload();
			});
			btnLeaveBulkEditMode.addClickListener(e -> {
				bulkOperationsDropdown.setVisible(false);
				ViewModelProviders.of(getClass()).get(ContactsViewConfiguration.class).setInEagerMode(false);
				btnLeaveBulkEditMode.setVisible(false);
				btnEnterBulkEditMode.setVisible(true);
				filterForm.setSearchFieldEnabled(true);
				navigateTo(criteria);
			});
		}

		if (UserProvider.getCurrent().hasUserRight(UserRight.CONTACT_MERGE)) {
			Button mergeDuplicatesButton = ButtonHelper.createIconButton(
				Captions.contactMergeDuplicates,
				VaadinIcons.COMPRESS_SQUARE,
				e -> ControllerProvider.getContactController().navigateToMergeContactsView(),
				ValoTheme.BUTTON_PRIMARY);
			mergeDuplicatesButton.setWidth(100, Unit.PERCENTAGE);
			moreLayout.addComponent(mergeDuplicatesButton);
		}

		if (viewConfiguration.getViewType().isContactOverview() && UserProvider.getCurrent().hasUserRight(UserRight.CONTACT_CREATE)) {
			final ExpandableButton lineListingButton =
				new ExpandableButton(Captions.lineListing).expand(e -> ControllerProvider.getContactController().openLineListingWindow());
			addHeaderComponent(lineListingButton);

			final Button btnNewContact = ButtonHelper.createIconButton(
				Captions.contactNewContact,
				VaadinIcons.PLUS_CIRCLE,
				e -> ControllerProvider.getContactController().create(),
				ValoTheme.BUTTON_PRIMARY);
			addHeaderComponent(btnNewContact);
		}

		if (moreLayout.getComponentCount() > 0) {
			addHeaderComponent(moreButton);
		}

		addComponent(gridLayout);
	}

	public VerticalLayout createFilterBar() {
		VerticalLayout filterLayout = new VerticalLayout();
		filterLayout.setSpacing(false);
		filterLayout.setMargin(false);
		filterLayout.setWidth(100, Unit.PERCENTAGE);

		filterForm = new ContactsFilterForm();
		filterForm.addValueChangeListener(e -> {
			if (!filterForm.hasFilter()) {
				navigateTo(null);
			}
		});
		filterForm.addResetHandler(e -> {
			ViewModelProviders.of(ContactsView.class).remove(ContactCriteria.class);
			navigateTo(null, true);
		});
		filterForm.addApplyHandler(e -> {
			if (ContactsViewType.FOLLOW_UP_VISITS_OVERVIEW.equals(viewConfiguration.getViewType())) {
				((ContactFollowUpGrid) grid).reload();
			} else {
				((AbstractContactGrid<?>) grid).reload();
			}
		});
		filterLayout.addComponent(filterForm);

		return filterLayout;
	}

	public HorizontalLayout createStatusFilterBar() {
		HorizontalLayout statusFilterLayout = new HorizontalLayout();
		statusFilterLayout.setMargin(false);
		statusFilterLayout.setSpacing(true);
		statusFilterLayout.setWidth(100, Unit.PERCENTAGE);
		statusFilterLayout.addStyleName(CssStyles.VSPACE_3);

		statusButtons = new HashMap<>();

		Button statusAll = ButtonHelper.createButton(I18nProperties.getCaption(Captions.all), e -> {
			criteria.contactStatus(null);
			navigateTo(criteria);
		}, ValoTheme.BUTTON_BORDERLESS, CssStyles.BUTTON_FILTER);

		statusAll.setCaptionAsHtml(true);
		if (ContactsViewType.FOLLOW_UP_VISITS_OVERVIEW.equals(viewConfiguration.getViewType())) {
			CssStyles.style(statusAll, CssStyles.FORCE_CAPTION);
		}

		statusFilterLayout.addComponent(statusAll);

		statusButtons.put(statusAll, I18nProperties.getCaption(Captions.all));
		activeStatusButton = statusAll;

		for (ContactStatus status : ContactStatus.values()) {
			Button statusButton = ButtonHelper.createButton("status-" + status.toString(), status.toString(), e -> {
				criteria.contactStatus(status);
				navigateTo(criteria);
			}, ValoTheme.BUTTON_BORDERLESS, CssStyles.BUTTON_FILTER, CssStyles.BUTTON_FILTER_LIGHT);

			statusButton.setCaptionAsHtml(true);
			statusButton.setData(status);
			if (ContactsViewType.FOLLOW_UP_VISITS_OVERVIEW.equals(viewConfiguration.getViewType())) {
				CssStyles.style(statusButton, CssStyles.FORCE_CAPTION);
			}

			statusFilterLayout.addComponent(statusButton);

			statusButtons.put(statusButton, status.toString());
		}

		HorizontalLayout actionButtonsLayout = new HorizontalLayout();
		actionButtonsLayout.setSpacing(true);
		{
			// Show active/archived/all dropdown
			if (viewConfiguration.getViewType().isContactOverview() && UserProvider.getCurrent().hasUserRight(UserRight.CONTACT_VIEW)) {
				relevanceStatusFilter = ComboBoxHelper.createComboBoxV7();
				relevanceStatusFilter.setId("relevanceStatus");
				relevanceStatusFilter.setWidth(140, Unit.PERCENTAGE);
				relevanceStatusFilter.setNullSelectionAllowed(false);
				relevanceStatusFilter.addItems((Object[]) EntityRelevanceStatus.values());
				relevanceStatusFilter.setItemCaption(EntityRelevanceStatus.ACTIVE, I18nProperties.getCaption(Captions.contactActiveContacts));
				relevanceStatusFilter.setItemCaption(EntityRelevanceStatus.ARCHIVED, I18nProperties.getCaption(Captions.contactArchivedContacts));
				relevanceStatusFilter.setItemCaption(EntityRelevanceStatus.ALL, I18nProperties.getCaption(Captions.contactAllContacts));
				relevanceStatusFilter.addValueChangeListener(e -> {
					criteria.relevanceStatus((EntityRelevanceStatus) e.getProperty().getValue());
					navigateTo(criteria);
				});
				actionButtonsLayout.addComponent(relevanceStatusFilter);
			}

			// Bulk operation dropdown
			if (isBulkEditAllowed()) {
				statusFilterLayout.setWidth(100, Unit.PERCENTAGE);

				boolean hasBulkOperationsRight = UserProvider.getCurrent().hasUserRight(UserRight.PERFORM_BULK_OPERATIONS);

				List<MenuBarHelper.MenuBarItem> bulkActions = new ArrayList<>(
					Arrays.asList(
						new MenuBarHelper.MenuBarItem(
							I18nProperties.getCaption(Captions.bulkEdit),
							VaadinIcons.ELLIPSIS_H,
							mi -> grid
								.bulkActionHandler(items -> ControllerProvider.getContactController().showBulkContactDataEditComponent(items, null)),
							hasBulkOperationsRight),
						new MenuBarHelper.MenuBarItem(
							I18nProperties.getCaption(Captions.bulkCancelFollowUp),
							VaadinIcons.CLOSE,
							mi -> grid.bulkActionHandler(
								items -> ControllerProvider.getContactController()
									.cancelFollowUpOfAllSelectedItems(items, () -> navigateTo(criteria))),
							hasBulkOperationsRight),
						new MenuBarHelper.MenuBarItem(
							I18nProperties.getCaption(Captions.bulkLostToFollowUp),
							VaadinIcons.UNLINK,
							mi -> grid.bulkActionHandler(
								items -> ControllerProvider.getContactController()
									.setAllSelectedItemsToLostToFollowUp(items, () -> navigateTo(criteria))),
							hasBulkOperationsRight),
						new MenuBarHelper.MenuBarItem(
							I18nProperties.getCaption(Captions.bulkDelete),
							VaadinIcons.TRASH,
							mi -> grid.bulkActionHandler(
								items -> ControllerProvider.getContactController().deleteAllSelectedItems(items, () -> navigateTo(criteria)),
								true),
							hasBulkOperationsRight),
						new MenuBarHelper.MenuBarItem(
							I18nProperties.getCaption(Captions.sormasToSormasShare),
							VaadinIcons.SHARE,
							mi -> grid.bulkActionHandler(
								items -> ControllerProvider.getSormasToSormasController().shareSelectedContacts(items, () -> navigateTo(criteria))),
							FacadeProvider.getSormasToSormasFacade().isSharingCasesContactsAndSamplesEnabledForUser())));

				if (isDocGenerationAllowed() && grid instanceof AbstractContactGrid) {
					bulkActions.add(
						new MenuBarHelper.MenuBarItem(
							I18nProperties.getCaption(Captions.bulkActionCreatDocuments),
							VaadinIcons.FILE_TEXT,
							mi -> grid.bulkActionHandler(items -> {
								List<ReferenceDto> references = ((AbstractContactGrid<?>) grid).asMultiSelect()
									.getSelectedItems()
									.stream()
									.map(ContactIndexDto::toReference)
									.collect(Collectors.toList());

								if (references.size() == 0) {
									new Notification(
										I18nProperties.getString(Strings.headingNoContactsSelected),
										I18nProperties.getString(Strings.messageNoContactsSelected),
										Notification.Type.WARNING_MESSAGE,
										false).show(Page.getCurrent());

									return;
								}

								ControllerProvider.getDocGenerationController()
									.showQuarantineOrderDocumentDialog(references, DocumentWorkflow.QUARANTINE_ORDER_CONTACT);
							})));
				}

				bulkOperationsDropdown = MenuBarHelper.createDropDown(Captions.bulkActions, bulkActions);

				bulkOperationsDropdown.setVisible(viewConfiguration.isInEagerMode());
				actionButtonsLayout.addComponent(bulkOperationsDropdown);
			}

			// Follow-up overview scrolling
			if (ContactsViewType.FOLLOW_UP_VISITS_OVERVIEW.equals(viewConfiguration.getViewType())) {
				statusFilterLayout.setWidth(100, Unit.PERCENTAGE);
				HorizontalLayout scrollLayout = buildScrollLayout();
				actionButtonsLayout.addComponent(scrollLayout);
			}
		}
		statusFilterLayout.addComponent(actionButtonsLayout);
		statusFilterLayout.setComponentAlignment(actionButtonsLayout, Alignment.TOP_RIGHT);
		statusFilterLayout.setExpandRatio(actionButtonsLayout, 1);

		return statusFilterLayout;
	}

	private void reloadGrid() {
		((ContactFollowUpGrid) grid).setVisitColumns(followUpToDate, followUpRangeInterval, criteria);
		((ContactFollowUpGrid) grid).reload();
		updateStatusButtons();
		buttonPreviousOrNextClick = false;
	}

	public void enter(ViewChangeEvent event) {
		String params = event.getParameters().trim();
		if (params.startsWith("?")) {
			params = params.substring(1);
			criteria.fromUrlParams(params);

			if (criteria.getEventUuid() != null) {
				criteria.eventLike(criteria.getEventUuid());
				criteria.eventUuid(null);
			}
		}
		updateFilterComponents();

		if (ContactsViewType.FOLLOW_UP_VISITS_OVERVIEW.equals(viewConfiguration.getViewType())) {
			((ContactFollowUpGrid) grid).reload();
		} else {
			if (viewConfiguration.isInEagerMode()) {
				((AbstractContactGrid<?>) grid).setEagerDataProvider();
			}
			((AbstractContactGrid<?>) grid).reload();
		}
	}

	public void updateFilterComponents() {
		// TODO replace with Vaadin 8 databinding
		applyingCriteria = true;

		updateStatusButtons();
		if (relevanceStatusFilter != null) {
			relevanceStatusFilter.setValue(criteria.getRelevanceStatus());
		}

		filterForm.setValue(criteria);

		applyingCriteria = false;
	}

	private void updateStatusButtons() {

		statusButtons.keySet().forEach(b -> {
			CssStyles.style(b, CssStyles.BUTTON_FILTER_LIGHT);
			b.setCaption(statusButtons.get(b));
			if (b.getData() == criteria.getContactStatus()) {
				activeStatusButton = b;
			}
		});
		CssStyles.removeStyles(activeStatusButton, CssStyles.BUTTON_FILTER_LIGHT);
		if (activeStatusButton != null) {
			activeStatusButton
				.setCaption(statusButtons.get(activeStatusButton) + LayoutUtil.spanCss(CssStyles.BADGE, String.valueOf(grid.getItemCount())));
		}
	}

	private boolean isBulkEditAllowed() {
		return viewConfiguration.getViewType().isContactOverview()
			&& (UserProvider.getCurrent().hasUserRight(UserRight.PERFORM_BULK_OPERATIONS)
				|| FacadeProvider.getSormasToSormasFacade().isSharingCasesContactsAndSamplesEnabledForUser());
	}

	private HorizontalLayout buildScrollLayout() {
		HorizontalLayout scrollLayout = new HorizontalLayout();
		scrollLayout.setMargin(false);

		DateField toReferenceDate = new DateField(I18nProperties.getCaption(Captions.to), LocalDate.now());
		toReferenceDate.setId("toReferenceDateField");
		LocalDate fromReferenceLocal =
			DateHelper8.toLocalDate(DateHelper.subtractDays(DateHelper8.toDate(LocalDate.now()), followUpRangeInterval - 1));
		DateField fromReferenceDate = new DateField(I18nProperties.getCaption(Captions.from), fromReferenceLocal);
		fromReferenceDate.setId("fromReferenceDateField");

		Button minusDaysButton = ButtonHelper.createButton(I18nProperties.getCaption(Captions.contactMinusDays), e -> {
			final LocalDate fromReferenceDateValue = fromReferenceDate.getValue();
			final LocalDate toReferenceDateValue = toReferenceDate.getValue();
			if (fromReferenceDateValue == null || toReferenceDateValue == null) {
				Notification.show(I18nProperties.getValidationError(Validations.validDateRange), Notification.Type.ERROR_MESSAGE);
				return;
			}
			int newFollowUpRangeInterval =
				DateHelper.getDaysBetween(DateHelper8.toDate(fromReferenceDateValue), DateHelper8.toDate(toReferenceDateValue));
			if (newFollowUpRangeInterval <= MAX_FOLLOW_UP_VIEW_DAYS) {
				followUpRangeInterval = newFollowUpRangeInterval;
				buttonPreviousOrNextClick = true;
				toReferenceDate.setValue(toReferenceDateValue.minusDays(followUpRangeInterval));
				fromReferenceDate.setValue(fromReferenceDateValue.minusDays(followUpRangeInterval));
			} else {
				Notification.show(
					String.format(I18nProperties.getString(Strings.messageSelectedPeriodTooLong), MAX_FOLLOW_UP_VIEW_DAYS),
					Notification.Type.WARNING_MESSAGE);
			}
		}, ValoTheme.BUTTON_PRIMARY, CssStyles.FORCE_CAPTION);
		scrollLayout.addComponent(minusDaysButton);

		fromReferenceDate.addValueChangeListener(e -> {
			Date newFromDate = e.getValue() != null ? DateHelper8.toDate(e.getValue()) : new Date();
			if (newFromDate.equals(criteria.getFollowUpUntilFrom())) {
				return;
			}

			final LocalDate toReferenceDateValue = toReferenceDate.getValue();
			if (toReferenceDateValue == null) {
				Notification.show(I18nProperties.getValidationError(Validations.validDateRange), Notification.Type.ERROR_MESSAGE);
				return;
			}

			int newFollowUpRangeInterval = DateHelper.getDaysBetween(newFromDate, DateHelper8.toDate(toReferenceDateValue));

			if (newFollowUpRangeInterval <= MAX_FOLLOW_UP_VIEW_DAYS) {
				applyingCriteria = true;
				criteria.followUpUntilFrom(DateHelper.getStartOfDay(newFromDate));
				applyingCriteria = false;
				followUpRangeInterval = newFollowUpRangeInterval;
				reloadGrid();
			} else {
				Notification.show(
					String.format(I18nProperties.getString(Strings.messageSelectedPeriodTooLong), MAX_FOLLOW_UP_VIEW_DAYS),
					Notification.Type.WARNING_MESSAGE);
				fromReferenceDate.setValue(DateHelper8.toLocalDate(criteria.getFollowUpUntilFrom()));
			}
		});
		scrollLayout.addComponent(fromReferenceDate);

		toReferenceDate.addValueChangeListener(e -> {
			Date newFollowUpToDate = e.getValue() != null ? DateHelper8.toDate(e.getValue()) : new Date();
			if (newFollowUpToDate.equals(criteria.getFollowUpUntilTo())) {
				return;
			}

			final LocalDate fromReferenceDateValue = fromReferenceDate.getValue();
			if (fromReferenceDateValue == null) {
				Notification.show(I18nProperties.getValidationError(Validations.validDateRange), Notification.Type.ERROR_MESSAGE);
				return;
			}

			int newFollowUpRangeInterval = DateHelper.getDaysBetween(DateHelper8.toDate(fromReferenceDateValue), newFollowUpToDate);

			if (newFollowUpRangeInterval <= MAX_FOLLOW_UP_VIEW_DAYS) {
				followUpToDate = newFollowUpToDate;
				applyingCriteria = true;
				criteria.reportDateTo(DateHelper.getEndOfDay(followUpToDate));
				applyingCriteria = false;
				if (!buttonPreviousOrNextClick) {
					followUpRangeInterval = newFollowUpRangeInterval;
					reloadGrid();
				}
			} else {
				Notification.show(
					String.format(I18nProperties.getString(Strings.messageSelectedPeriodTooLong), MAX_FOLLOW_UP_VIEW_DAYS),
					Notification.Type.WARNING_MESSAGE);
				toReferenceDate.setValue(DateHelper8.toLocalDate(followUpToDate));
			}
		});
		scrollLayout.addComponent(toReferenceDate);

		Button plusDaysButton = ButtonHelper.createButton(I18nProperties.getCaption(Captions.contactPlusDays), e -> {
			final LocalDate fromReferenceDateValue = fromReferenceDate.getValue();
			final LocalDate toReferenceDateValue = toReferenceDate.getValue();
			if (fromReferenceDateValue == null || toReferenceDateValue == null) {
				Notification.show(I18nProperties.getValidationError(Validations.validDateRange), Notification.Type.ERROR_MESSAGE);
				return;
			}
			int newFollowUpRangeInterval =
				DateHelper.getDaysBetween(DateHelper8.toDate(fromReferenceDateValue), DateHelper8.toDate(toReferenceDateValue));

			if (newFollowUpRangeInterval <= MAX_FOLLOW_UP_VIEW_DAYS) {
				followUpRangeInterval = newFollowUpRangeInterval;
				buttonPreviousOrNextClick = true;
				toReferenceDate.setValue(toReferenceDateValue.plusDays(followUpRangeInterval));
				fromReferenceDate.setValue(fromReferenceDateValue.plusDays(followUpRangeInterval));
			} else {
				Notification.show(
					String.format(I18nProperties.getString(Strings.messageSelectedPeriodTooLong), MAX_FOLLOW_UP_VIEW_DAYS),
					Notification.Type.WARNING_MESSAGE);
			}
		}, ValoTheme.BUTTON_PRIMARY, CssStyles.FORCE_CAPTION);
		scrollLayout.addComponent(plusDaysButton);
		return scrollLayout;
	}
}
