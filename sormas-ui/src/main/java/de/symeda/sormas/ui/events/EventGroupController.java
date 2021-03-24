/*******************************************************************************
 * SORMAS® - Surveillance Outbreak Response Management & Analysis System
 * Copyright © 2016-2018 Helmholtz-Zentrum für Infektionsforschung GmbH (HZI)
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
 *******************************************************************************/
package de.symeda.sormas.ui.events;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.vaadin.server.Page;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.event.EventGroupDto;
import de.symeda.sormas.api.event.EventGroupIndexDto;
import de.symeda.sormas.api.event.EventGroupReferenceDto;
import de.symeda.sormas.api.event.EventIndexDto;
import de.symeda.sormas.api.event.EventReferenceDto;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Strings;
import de.symeda.sormas.api.user.UserRight;
import de.symeda.sormas.api.utils.DataHelper;
import de.symeda.sormas.ui.SormasUI;
import de.symeda.sormas.ui.UserProvider;
import de.symeda.sormas.ui.events.groups.EventGroupSelectionField;
import de.symeda.sormas.ui.utils.ButtonHelper;
import de.symeda.sormas.ui.utils.CommitDiscardWrapperComponent;
import de.symeda.sormas.ui.utils.CssStyles;
import de.symeda.sormas.ui.utils.VaadinUiUtil;

public class EventGroupController {

	public EventGroupDto create(EventReferenceDto event) {
		return create(Collections.singletonList(event), null);
	}

	public EventGroupDto create(List<EventReferenceDto> events, Runnable callback) {
		CommitDiscardWrapperComponent<EventGroupDataForm> eventCreateComponent = getEventGroupCreateComponent(events, callback);
		EventGroupDto eventGroupDto = eventCreateComponent.getWrappedComponent().getValue();
		VaadinUiUtil.showModalPopupWindow(eventCreateComponent, I18nProperties.getString(Strings.headingCreateNewEventGroup));
		return eventGroupDto;
	}

	public void selectOrCreate(EventReferenceDto eventReference) {
		selectOrCreate(Collections.singletonList(eventReference), null);
	}

	public void selectOrCreate(List<EventReferenceDto> eventReferences, Runnable callback) {
		Set<String> excludedEventGroupUuids = FacadeProvider.getEventGroupFacade()
			.getCommonEventGroupsByEvents(eventReferences)
			.stream()
			.map(EventGroupReferenceDto::getUuid)
			.collect(Collectors.toSet());
		EventGroupSelectionField selectionField = new EventGroupSelectionField(excludedEventGroupUuids);
		selectionField.setWidth(1024, Sizeable.Unit.PIXELS);

		final CommitDiscardWrapperComponent<EventGroupSelectionField> component = new CommitDiscardWrapperComponent<>(selectionField);
		component.addCommitListener(() -> {
			EventGroupIndexDto selectedEventGroup = selectionField.getValue();
			if (selectedEventGroup != null) {
				FacadeProvider.getEventGroupFacade().linkEventsToGroup(eventReferences, selectedEventGroup.toReference());

				Notification.show(I18nProperties.getString(Strings.messageEventLinkedToGroup), Type.TRAY_NOTIFICATION);

				if (callback != null) {
					callback.run();
				} else {
					SormasUI.refreshView();
				}
			} else {
				create(eventReferences, callback);
			}
		});

		selectionField.setSelectionChangeCallback((commitAllowed) -> component.getCommitButton().setEnabled(commitAllowed));
		VaadinUiUtil.showModalPopupWindow(component, I18nProperties.getString(Strings.headingPickOrCreateEventGroup));
	}

	public EventGroupDto createNewEventGroup() {
		return EventGroupDto.build();
	}

	public CommitDiscardWrapperComponent<EventGroupDataForm> getEventGroupCreateComponent(List<EventReferenceDto> eventReferences, Runnable callback) {
		EventGroupDataForm createForm = new EventGroupDataForm(true);
		createForm.setValue(createNewEventGroup());

		final CommitDiscardWrapperComponent<EventGroupDataForm> editView = new CommitDiscardWrapperComponent<>(
			createForm,
			UserProvider.getCurrent().hasUserRight(UserRight.EVENT_CREATE),
			createForm.getFieldGroup());

		editView.addCommitListener(() -> {
			if (!createForm.getFieldGroup().isModified()) {
				EventGroupDto dto = createForm.getValue();
				dto = FacadeProvider.getEventGroupFacade().saveEventGroup(dto);
				FacadeProvider.getEventGroupFacade().linkEventsToGroup(eventReferences, dto.toReference());
				Notification.show(I18nProperties.getString(Strings.messageEventGroupCreated), Type.WARNING_MESSAGE);

				if (callback != null) {
					callback.run();
				} else {
					SormasUI.refreshView();
				}
			}
		});

		return editView;
	}

	public void unlinkEventGroup(EventReferenceDto eventReference, EventGroupReferenceDto eventGroupReference) {
		FacadeProvider.getEventGroupFacade().unlinkEventGroup(eventReference, eventGroupReference);

		Notification.show(I18nProperties.getString(Strings.messageEventToEventGroupUnlinked), Type.TRAY_NOTIFICATION);
	}

	public CommitDiscardWrapperComponent<?> getEventGroupEditComponent(String uuid) {

		EventGroupDto eventGroup = FacadeProvider.getEventGroupFacade().getEventGroupByUuid(uuid);
		EventGroupDataForm eventGroupEditForm = new EventGroupDataForm(false);
		eventGroupEditForm.setValue(eventGroup);
		final CommitDiscardWrapperComponent<EventGroupDataForm> editView = new CommitDiscardWrapperComponent<>(
			eventGroupEditForm,
			UserProvider.getCurrent().hasUserRight(UserRight.EVENT_EDIT),
			eventGroupEditForm.getFieldGroup());

		editView.addCommitListener(() -> {
			if (!eventGroupEditForm.getFieldGroup().isModified()) {
				EventGroupDto updatedEventGroup = eventGroupEditForm.getValue();
				FacadeProvider.getEventGroupFacade().saveEventGroup(updatedEventGroup);
				Notification.show(I18nProperties.getString(Strings.messageEventGroupSaved), Notification.Type.WARNING_MESSAGE);
				SormasUI.refreshView();
			}
		});

		if (UserProvider.getCurrent().hasUserRight(UserRight.EVENT_DELETE)) {
			editView.addDeleteListener(() -> {
				deleteEventGroup(eventGroup);
				UI.getCurrent().getNavigator().navigateTo(EventsView.VIEW_NAME);
			}, I18nProperties.getString(Strings.entityEventGroup));
		}

		// Initialize 'Archive' button
		if (UserProvider.getCurrent().hasUserRight(UserRight.EVENT_ARCHIVE)) {
			boolean archived = FacadeProvider.getEventGroupFacade().isArchived(uuid);
			Button archiveEventButton = ButtonHelper.createButton(archived ? Captions.actionDearchive : Captions.actionArchive, e -> {
				archiveOrDearchiveEventGroup(uuid, !archived);
			}, ValoTheme.BUTTON_LINK);

			editView.getButtonsPanel().addComponentAsFirst(archiveEventButton);
			editView.getButtonsPanel().setComponentAlignment(archiveEventButton, Alignment.BOTTOM_LEFT);
		}

		editView.addDiscardListener(SormasUI::refreshView);

		return editView;

	}

	public VerticalLayout getEventGroupViewTitleLayout(String uuid) {
		EventGroupDto eventGroup = FacadeProvider.getEventGroupFacade().getEventGroupByUuid(uuid);

		VerticalLayout titleLayout = new VerticalLayout();
		titleLayout.addStyleNames(CssStyles.LAYOUT_MINIMAL, CssStyles.VSPACE_4, CssStyles.VSPACE_TOP_4);
		titleLayout.setSpacing(false);

		String shortUuid = DataHelper.getShortUuid(eventGroup.getUuid());
		Label eventLabel = new Label( eventGroup.getName() + " (" + shortUuid + ")");
		eventLabel.addStyleNames(CssStyles.H2, CssStyles.VSPACE_NONE, CssStyles.VSPACE_TOP_NONE, CssStyles.LABEL_PRIMARY);
		titleLayout.addComponent(eventLabel);

		return titleLayout;
	}

    public void navigateToData(String uuid) {
		String navigationState = EventGroupDataView.VIEW_NAME + "/" + uuid;
		SormasUI.get().getNavigator().navigateTo(navigationState);
    }

	private void deleteEventGroup(EventGroupDto eventGroup) {
		FacadeProvider.getEventGroupFacade().deleteEventGroup(eventGroup.getUuid());
	}

	private void archiveOrDearchiveEventGroup(String uuid, boolean archive) {

		if (archive) {
			Label contentLabel = new Label(
				String.format(
					I18nProperties.getString(Strings.confirmationArchiveEventGroup),
					I18nProperties.getString(Strings.entityEventGroup).toLowerCase(),
					I18nProperties.getString(Strings.entityEventGroup).toLowerCase()));
			VaadinUiUtil.showConfirmationPopup(
				I18nProperties.getString(Strings.headingArchiveEventGroup),
				contentLabel,
				I18nProperties.getString(Strings.yes),
				I18nProperties.getString(Strings.no),
				640,
				confirmed -> {
					if (confirmed) {
						FacadeProvider.getEventGroupFacade().archiveOrDearchiveEventGroup(uuid, true);
						Notification.show(
							String.format(I18nProperties.getString(Strings.messageEventGroupArchived), I18nProperties.getString(Strings.entityEventGroup)),
							Type.ASSISTIVE_NOTIFICATION);
						navigateToData(uuid);
					}
				});
		} else {
			Label contentLabel = new Label(
				String.format(
					I18nProperties.getString(Strings.confirmationDearchiveEventGroup),
					I18nProperties.getString(Strings.entityEventGroup).toLowerCase(),
					I18nProperties.getString(Strings.entityEventGroup).toLowerCase()));
			VaadinUiUtil.showConfirmationPopup(
				I18nProperties.getString(Strings.headingDearchiveEventGroup),
				contentLabel,
				I18nProperties.getString(Strings.yes),
				I18nProperties.getString(Strings.no),
				640,
				confirmed -> {
					if (confirmed) {
						FacadeProvider.getEventGroupFacade().archiveOrDearchiveEventGroup(uuid, false);
						Notification.show(
							String.format(I18nProperties.getString(Strings.messageEventGroupDearchived), I18nProperties.getString(Strings.entityEventGroup)),
							Type.ASSISTIVE_NOTIFICATION);
						navigateToData(uuid);
					}
				});
		}
	}

	public void linkAllToGroup(Set<EventIndexDto> selectedItems, Runnable callback) {
		if (selectedItems.size() == 0) {
			new Notification(
				I18nProperties.getString(Strings.headingNoEventsSelected),
				I18nProperties.getString(Strings.messageNoEventsSelected),
				Type.WARNING_MESSAGE,
				false).show(Page.getCurrent());
			return;
		}

		List<EventReferenceDto> eventReferences = selectedItems.stream().map(EventIndexDto::toReference).collect(Collectors.toList());
		selectOrCreate(eventReferences, callback);
	}
}
