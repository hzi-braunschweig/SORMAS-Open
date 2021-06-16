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

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

import de.symeda.sormas.api.event.EventDto;
import de.symeda.sormas.api.event.EventParticipantDto;
import de.symeda.sormas.api.sormastosormas.SormasToSormasException;
import de.symeda.sormas.api.sormastosormas.SormasToSormasOptionsDto;
import de.symeda.sormas.api.sormastosormas.SormasToSormasOriginInfoDto;
import de.symeda.sormas.api.sormastosormas.event.SormasToSormasEventDto;
import de.symeda.sormas.api.sormastosormas.sharerequest.SormasToSormasEventParticipantPreview;
import de.symeda.sormas.api.sormastosormas.sharerequest.SormasToSormasEventPreview;
import de.symeda.sormas.backend.event.Event;
import de.symeda.sormas.backend.event.EventFacadeEjb.EventFacadeEjbLocal;
import de.symeda.sormas.backend.event.EventParticipant;
import de.symeda.sormas.backend.event.EventParticipantFacadeEjb.EventParticipantFacadeEjbLocal;
import de.symeda.sormas.backend.event.EventParticipantService;
import de.symeda.sormas.backend.location.LocationFacadeEjb;
import de.symeda.sormas.backend.sample.Sample;
import de.symeda.sormas.backend.sample.SampleService;
import de.symeda.sormas.backend.sormastosormas.entities.AssociatedEntityWrapper;
import de.symeda.sormas.backend.sormastosormas.share.ShareData;
import de.symeda.sormas.backend.sormastosormas.share.ShareDataBuilder;
import de.symeda.sormas.backend.sormastosormas.share.ShareDataBuilderHelper;
import de.symeda.sormas.backend.sormastosormas.share.shareinfo.ShareInfoEventParticipant;
import de.symeda.sormas.backend.sormastosormas.share.shareinfo.SormasToSormasShareInfo;
import de.symeda.sormas.backend.user.User;
import de.symeda.sormas.backend.util.Pseudonymizer;

@Stateless
@LocalBean
public class EventShareDataBuilder implements ShareDataBuilder<Event, SormasToSormasEventDto, SormasToSormasEventPreview> {

	@EJB
	private ShareDataBuilderHelper dataBuilderHelper;
	@EJB
	private EventFacadeEjbLocal eventFacade;
	@EJB
	private EventParticipantFacadeEjbLocal eventParticipantFacade;
	@EJB
	private EventParticipantService eventParticipantService;
	@EJB
	private SampleService sampleService;

	@Override
	public ShareData<Event, SormasToSormasEventDto> buildShareData(Event event, User user, SormasToSormasOptionsDto options)
		throws SormasToSormasException {
		SormasToSormasOriginInfoDto originInfo =
			dataBuilderHelper.createSormasToSormasOriginInfo(user, options.isHandOverOwnership(), options.getComment());

		List<EventParticipant> eventParticipants = Collections.emptyList();
		if (options.isWithEventParticipants()) {
			eventParticipants = eventParticipantService.getByEventUuids(Collections.singletonList(event.getUuid()));
		}

		return createShareData(
			event,
			originInfo,
			eventParticipants,
			options.isWithSamples(),
			options.isPseudonymizePersonalData(),
			options.isPseudonymizeSensitiveData());
	}

	@Override
	public ShareData<Event, SormasToSormasEventPreview> buildShareDataPreview(Event event, User user, SormasToSormasOptionsDto options)
		throws SormasToSormasException {
		SormasToSormasEventPreview eventPreview = getEventPreview(event);

		List<EventParticipant> eventParticipants = Collections.emptyList();
		if (options.isWithEventParticipants()) {
			eventParticipants = eventParticipantService.getAllActiveByEvent(event);
			eventPreview.setEventParticipants(getEventParticipantPreviews(eventParticipants));
		}

		ShareData<Event, SormasToSormasEventPreview> shareData = new ShareData<>(event, eventPreview);
		shareData.addAssociatedEntities(AssociatedEntityWrapper.forEventParticipants(eventParticipants));

		return shareData;
	}

	@Override
	public List<ShareData<Event, SormasToSormasEventDto>> buildShareData(SormasToSormasShareInfo shareInfo, User user)
		throws SormasToSormasException {
		SormasToSormasOriginInfoDto originInfo =
			dataBuilderHelper.createSormasToSormasOriginInfo(user, shareInfo.isOwnershipHandedOver(), shareInfo.getComment());

		return shareInfo.getEvents().stream().map(shareInfoEvent -> {
			Event event = shareInfoEvent.getEvent();

			return createShareData(
				event,
				originInfo,
				shareInfo.getEventParticipants().stream().map(ShareInfoEventParticipant::getEventParticipant).collect(Collectors.toList()),
				shareInfo.isWithSamples(),
				shareInfo.isPseudonymizedPersonalData(),
				shareInfo.isPseudonymizedSensitiveData());
		}).collect(Collectors.toList());
	}

	private ShareData<Event, SormasToSormasEventDto> createShareData(
		Event event,
		SormasToSormasOriginInfoDto originInfo,
		List<EventParticipant> eventParticipants,
		boolean withSamples,
		boolean pseudonymizedPersonalData,
		boolean pseudonymizedSensitiveData) {
		Pseudonymizer pseudonymizer = dataBuilderHelper.createPseudonymizer(pseudonymizedPersonalData, pseudonymizedSensitiveData);

		EventDto eventDto = getEventDto(event, pseudonymizer);

		SormasToSormasEventDto eventData = new SormasToSormasEventDto(eventDto, originInfo);
		ShareData<Event, SormasToSormasEventDto> eventShareData = new ShareData<>(event, eventData);

		List<Sample> samples = Collections.emptyList();
		if (eventParticipants.size() > 0 && withSamples) {
			samples =
				sampleService.getByEventParticipantUuids(eventParticipants.stream().map(EventParticipant::getUuid).collect(Collectors.toList()));
		}

		eventData
			.setEventParticipants(getEventParticipantDtos(eventParticipants, pseudonymizer, pseudonymizedPersonalData, pseudonymizedSensitiveData));
		eventData.setSamples(dataBuilderHelper.getSampleDtos(samples, pseudonymizer));
		eventShareData.addAssociatedEntities(AssociatedEntityWrapper.forEventParticipants(eventParticipants));
		eventShareData.addAssociatedEntities(AssociatedEntityWrapper.forSamples(samples));

		return eventShareData;
	}

	private EventDto getEventDto(Event event, Pseudonymizer pseudonymizer) {
		EventDto eventDto = eventFacade.convertToDto(event, pseudonymizer);

		eventDto.setReportingUser(null);
		eventDto.setSormasToSormasOriginInfo(null);

		return eventDto;
	}

	private List<EventParticipantDto> getEventParticipantDtos(
		List<EventParticipant> eventParticipants,
		Pseudonymizer pseudonymizer,
		boolean pseudonymizedPersonalData,
		boolean pseudonymizedSensitiveData) {
		return eventParticipants.stream().map(eventParticipant -> {
			EventParticipantDto dto = eventParticipantFacade.convertToDto(eventParticipant, pseudonymizer);

			dto.setReportingUser(null);
			dto.setSormasToSormasOriginInfo(null);

			dataBuilderHelper.pseudonymiePerson(dto.getPerson(), pseudonymizedPersonalData, pseudonymizedSensitiveData);

			return dto;
		}).collect(Collectors.toList());
	}

	private SormasToSormasEventPreview getEventPreview(Event event) {
		SormasToSormasEventPreview preview = new SormasToSormasEventPreview();

		preview.setUuid(event.getUuid());
		preview.setReportDateTime(event.getReportDateTime());
		preview.setEventTitle(event.getEventTitle());
		preview.setEventDesc(event.getEventDesc());
		preview.setDisease(event.getDisease());
		preview.setDiseaseDetails(event.getDiseaseDetails());
		preview.setEventLocation(LocationFacadeEjb.toDto(event.getEventLocation()));

		return preview;
	}

	private List<SormasToSormasEventParticipantPreview> getEventParticipantPreviews(List<EventParticipant> eventParticipants) {
		return eventParticipants.stream().map(eventParticipant -> {
			SormasToSormasEventParticipantPreview preview = new SormasToSormasEventParticipantPreview();

			preview.setUuid(eventParticipant.getUuid());
			preview.setPerson(dataBuilderHelper.getPersonPreview(eventParticipant.getPerson()));

			return preview;
		}).collect(Collectors.toList());
	}
}
