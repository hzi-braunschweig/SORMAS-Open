/*
 * SORMAS® - Surveillance Outbreak Response Management & Analysis System
 * Copyright © 2016-2021 Helmholtz-Zentrum für Infektionsforschung GmbH (HZI)
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package de.symeda.sormas.backend.sormastosormas.entities.event;

import static de.symeda.sormas.backend.sormastosormas.processed.ValidationHelper.buildCaseValidationGroupName;
import static de.symeda.sormas.backend.sormastosormas.processed.ValidationHelper.buildEventValidationGroupName;
import static de.symeda.sormas.backend.sormastosormas.processed.ValidationHelper.handleValidationError;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.transaction.Transactional;

import de.symeda.sormas.api.event.EventDto;
import de.symeda.sormas.api.event.EventParticipantDto;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.sample.SampleDto;
import de.symeda.sormas.api.sormastosormas.SormasToSormasOriginInfoDto;
import de.symeda.sormas.api.sormastosormas.SormasToSormasValidationException;
import de.symeda.sormas.backend.event.EventFacadeEjb.EventFacadeEjbLocal;
import de.symeda.sormas.backend.event.EventParticipantFacadeEjb.EventParticipantFacadeEjbLocal;
import de.symeda.sormas.backend.person.PersonFacadeEjb.PersonFacadeEjbLocal;
import de.symeda.sormas.backend.sormastosormas.origin.SormasToSormasOriginInfoFacadeEjb;
import de.symeda.sormas.backend.sormastosormas.processed.ProcessedDataPersister;
import de.symeda.sormas.backend.sormastosormas.processed.ProcessedDataPersisterHelper;
import de.symeda.sormas.backend.sormastosormas.processed.ProcessedDataPersisterHelper.ReturnedAssociatedEntityCallback;
import de.symeda.sormas.backend.sormastosormas.share.shareinfo.SormasToSormasShareInfo;
import de.symeda.sormas.backend.sormastosormas.share.shareinfo.SormasToSormasShareInfoService;

@Stateless
@LocalBean
public class ProcessedEventDataPersister implements ProcessedDataPersister<ProcessedEventData> {

	@EJB
	private ProcessedDataPersisterHelper processedDataPersisterHelper;
	@EJB
	private EventFacadeEjbLocal eventFacade;

	@EJB
	private EventParticipantFacadeEjbLocal eventParticipantFacade;

	@EJB
	private PersonFacadeEjbLocal personFacade;

	@EJB
	private SormasToSormasShareInfoService shareInfoService;

	@EJB
	private SormasToSormasOriginInfoFacadeEjb.SormasToSormasOriginInfoFacadeEjbLocal oriInfoFacade;

	@Override
	@Transactional(rollbackOn = {
		Exception.class })
	public void persistSharedData(ProcessedEventData processedData) throws SormasToSormasValidationException {
		persistProcessedData(
			processedData,
			null,
			processedDataPersisterHelper::sharedAssociatedEntityCallback,
			processedDataPersisterHelper::sharedAssociatedEntityCallback);
	}

	@Override
	@Transactional(rollbackOn = {
		Exception.class })
	public void persistReturnedData(ProcessedEventData processedData, SormasToSormasOriginInfoDto originInfo)
		throws SormasToSormasValidationException {

		ReturnedAssociatedEntityCallback callback = processedDataPersisterHelper.createReturnedAssociatedEntityCallback(originInfo);

		persistProcessedData(processedData, event -> {
			SormasToSormasShareInfo eventShareInfo = shareInfoService.getByEventAndOrganization(event.getUuid(), originInfo.getOrganizationId());
			eventShareInfo.setOwnershipHandedOver(false);
			shareInfoService.persist(eventShareInfo);
		}, (event, eventParticipant) -> {
			callback.apply(eventParticipant, shareInfoService::getByEventParticipantAndOrganization);
		}, (event, sample) -> {
			callback.apply(sample, shareInfoService::getBySampleAndOrganization);
		});
	}

	@Override
	@Transactional(rollbackOn = {
		Exception.class })
	public void persistSyncData(ProcessedEventData processedData) throws SormasToSormasValidationException {
		SormasToSormasOriginInfoDto originInfo = processedData.getOriginInfo();

		persistProcessedData(processedData, (event) -> {
			SormasToSormasOriginInfoDto eventOriginInfo = event.getSormasToSormasOriginInfo();
			eventOriginInfo.setOwnershipHandedOver(originInfo.isOwnershipHandedOver());
			eventOriginInfo.setComment(originInfo.getComment());

			oriInfoFacade.saveOriginInfo(eventOriginInfo);
		}, processedDataPersisterHelper::syncedAssociatedEntityCallback, processedDataPersisterHelper::syncedAssociatedEntityCallback);
	}

	private void persistProcessedData(
		ProcessedEventData eventData,
		Consumer<EventDto> afterSaveEvent,
		BiConsumer<EventDto, EventParticipantDto> beforeSaveEventParticipant,
		BiConsumer<EventDto, SampleDto> beforeSaveSample)
		throws SormasToSormasValidationException {
		EventDto event = eventData.getEntity();

		final EventDto savedEvent;
		savedEvent = handleValidationError(() -> eventFacade.saveEvent(event, false), Captions.CaseData, buildCaseValidationGroupName(event));
		if (afterSaveEvent != null) {
			afterSaveEvent.accept(savedEvent);
		}

		if (eventData.getEventParticipants() != null) {
			persistEventParticipants(
				eventData.getEventParticipants(),
				beforeSaveEventParticipant != null ? (ep) -> beforeSaveEventParticipant.accept(savedEvent, ep) : null);
		}

		if (eventData.getSamples() != null) {
			processedDataPersisterHelper
				.persistSamples(eventData.getSamples(), beforeSaveSample != null ? (s) -> beforeSaveSample.accept(savedEvent, s) : null);
		}
	}

	private void persistEventParticipants(List<EventParticipantDto> eventParticipants, Consumer<EventParticipantDto> beforeSave)
		throws SormasToSormasValidationException {
		for (EventParticipantDto ep : eventParticipants) {
			handleValidationError(
				() -> personFacade.savePerson(ep.getPerson(), false),
				Captions.EventParticipant,
				buildEventValidationGroupName(ep.getEvent()));

			if (beforeSave != null) {
				beforeSave.accept(ep);
			}

			handleValidationError(
				() -> eventParticipantFacade.saveEventParticipant(ep, false),
				Captions.EventParticipant,
				buildEventValidationGroupName(ep.getEvent()));
		}

	}
}
