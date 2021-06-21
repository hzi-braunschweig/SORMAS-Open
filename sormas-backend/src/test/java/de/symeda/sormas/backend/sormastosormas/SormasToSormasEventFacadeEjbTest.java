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

package de.symeda.sormas.backend.sormastosormas;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;

import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.ws.rs.core.Response;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.fasterxml.jackson.core.JsonProcessingException;

import de.symeda.sormas.api.Disease;
import de.symeda.sormas.api.caze.Vaccination;
import de.symeda.sormas.api.event.EventDto;
import de.symeda.sormas.api.event.EventInvestigationStatus;
import de.symeda.sormas.api.event.EventParticipantDto;
import de.symeda.sormas.api.event.EventParticipantReferenceDto;
import de.symeda.sormas.api.event.EventReferenceDto;
import de.symeda.sormas.api.event.EventSourceType;
import de.symeda.sormas.api.event.EventStatus;
import de.symeda.sormas.api.event.RiskLevel;
import de.symeda.sormas.api.facility.FacilityDto;
import de.symeda.sormas.api.facility.FacilityReferenceDto;
import de.symeda.sormas.api.facility.FacilityType;
import de.symeda.sormas.api.person.PersonDto;
import de.symeda.sormas.api.sample.AdditionalTestDto;
import de.symeda.sormas.api.sample.PathogenTestDto;
import de.symeda.sormas.api.sample.PathogenTestResultType;
import de.symeda.sormas.api.sample.PathogenTestType;
import de.symeda.sormas.api.sample.SampleDto;
import de.symeda.sormas.api.sample.SampleMaterial;
import de.symeda.sormas.api.sample.SamplePurpose;
import de.symeda.sormas.api.sormastosormas.ServerAccessDataReferenceDto;
import de.symeda.sormas.api.sormastosormas.SormasToSormasEncryptedDataDto;
import de.symeda.sormas.api.sormastosormas.SormasToSormasException;
import de.symeda.sormas.api.sormastosormas.SormasToSormasOptionsDto;
import de.symeda.sormas.api.sormastosormas.SormasToSormasOriginInfoDto;
import de.symeda.sormas.api.sormastosormas.SormasToSormasSampleDto;
import de.symeda.sormas.api.sormastosormas.SormasToSormasShareInfoCriteria;
import de.symeda.sormas.api.sormastosormas.SormasToSormasShareInfoDto;
import de.symeda.sormas.api.sormastosormas.SormasToSormasValidationException;
import de.symeda.sormas.api.sormastosormas.event.SormasToSormasEventDto;
import de.symeda.sormas.api.sormastosormas.sharerequest.SormasToSormasShareRequestDto;
import de.symeda.sormas.api.user.UserDto;
import de.symeda.sormas.api.user.UserReferenceDto;
import de.symeda.sormas.api.user.UserRole;
import de.symeda.sormas.api.utils.DataHelper;
import de.symeda.sormas.backend.MockProducer;
import de.symeda.sormas.backend.TestDataCreator;
import de.symeda.sormas.backend.sormastosormas.shareinfo.ShareInfoEvent;
import de.symeda.sormas.backend.sormastosormas.shareinfo.ShareInfoEventParticipant;
import de.symeda.sormas.backend.user.User;

@RunWith(MockitoJUnitRunner.class)
public class SormasToSormasEventFacadeEjbTest extends SormasToSormasFacadeTest {

	@Test
	public void testShareEvent() throws SormasToSormasException, JsonProcessingException, NoSuchAlgorithmException, KeyManagementException {
		TestDataCreator.RDCF rdcf = creator.createRDCF();
		UserDto user = creator.createUser(rdcf, UserRole.NATIONAL_USER);

		useSurveillanceOfficerLogin(rdcf);

		Date dateNow = new Date();

		EventDto event = creator
			.createEvent(EventStatus.SCREENING, EventInvestigationStatus.ONGOING, "Test event title", "Test description", user.toReference(), (e) -> {
				e.setRiskLevel(RiskLevel.MODERATE);
				e.setMultiDayEvent(true);
				e.setStartDate(dateNow);
				e.setEndDate(dateNow);
				e.setSrcType(EventSourceType.MEDIA_NEWS);
				e.setSrcMediaWebsite("Test media name");
				e.setSrcMediaName("Test media website");
				e.setSrcMediaDetails("Test media details");

				e.getEventLocation().setRegion(rdcf.region);
				e.getEventLocation().setDistrict(rdcf.district);
			});

		SormasToSormasOptionsDto options = new SormasToSormasOptionsDto();
		options.setOrganization(new ServerAccessDataReferenceDto(SECOND_SERVER_ACCESS_CN));
		options.setComment("Test comment");

		Mockito.when(MockProducer.getSormasToSormasClient().post(Matchers.anyString(), Matchers.anyString(), Matchers.anyString(), Matchers.any()))
			.thenAnswer(invocation -> {
				assertThat(invocation.getArgument(0, String.class), is(SECOND_SERVER_REST_URL));
				assertThat(invocation.getArgument(1, String.class), is("/sormasToSormas/events"));

				String authToken = invocation.getArgument(2, String.class);
				assertThat(authToken, startsWith("Basic "));
				String credentials = new String(Base64.getDecoder().decode(authToken.replace("Basic ", "")), StandardCharsets.UTF_8);
				// uses password from server-list.csv from `serveraccessdefault` package
				//assertThat(credentials, is(StartupShutdownService.SORMAS_TO_SORMAS_USER_NAME + ":" + SECOND_SERVER_REST_PASSWORD));

				SormasToSormasEncryptedDataDto encryptedData = invocation.getArgument(3, SormasToSormasEncryptedDataDto.class);
				SormasToSormasEventDto[] sharedEvents = decryptSharesData(encryptedData.getData(), SormasToSormasEventDto[].class);
				SormasToSormasEventDto sharedEventData = sharedEvents[0];

				EventDto sharedEvent = sharedEventData.getEntity();
				assertThat(sharedEvent.getEventTitle(), is("Test event title"));
				assertThat(sharedEvent.getEventDesc(), is("Test description"));
				assertThat(sharedEvent.getEventStatus(), is(EventStatus.SCREENING));
				assertThat(sharedEvent.getEventInvestigationStatus(), is(EventInvestigationStatus.ONGOING));
				assertThat(sharedEvent.getRiskLevel(), is(RiskLevel.MODERATE));
				assertThat(sharedEvent.isMultiDayEvent(), is(true));
				assertThat(sharedEvent.getStartDate().getTime(), is(dateNow.getTime()));
				assertThat(sharedEvent.getEndDate().getTime(), is(dateNow.getTime()));
				assertThat(sharedEvent.getSrcType(), is(EventSourceType.MEDIA_NEWS));
				assertThat(sharedEvent.getSrcMediaWebsite(), is("Test media name"));
				assertThat(sharedEvent.getSrcMediaName(), is("Test media website"));
				assertThat(sharedEvent.getSrcMediaDetails(), is("Test media details"));

				// share information
				assertThat(sharedEventData.getOriginInfo().getOrganizationId(), is(DEFAULT_SERVER_ACCESS_CN));
				assertThat(sharedEventData.getOriginInfo().getSenderName(), is("Surv Off"));
				assertThat(sharedEventData.getOriginInfo().getComment(), is("Test comment"));

				return Response.noContent().build();
			});

		getSormasToSormasEventFacade().shareEntities(Collections.singletonList(event.getUuid()), options);

		List<SormasToSormasShareInfoDto> shareInfoList =
			getSormasToSormasFacade().getShareInfoIndexList(new SormasToSormasShareInfoCriteria().event(event.toReference()), 0, 100);
		assertThat(shareInfoList.size(), is(1));
		assertThat(shareInfoList.get(0).getTarget().getUuid(), is(SECOND_SERVER_ACCESS_CN));
		assertThat(shareInfoList.get(0).getSender().getCaption(), is("Surv OFF - Surveillance Officer"));
		assertThat(shareInfoList.get(0).getComment(), is("Test comment"));
	}

	@Test
	public void testShareEventWithSamples()
		throws SormasToSormasException, JsonProcessingException, NoSuchAlgorithmException, KeyManagementException {
		TestDataCreator.RDCF rdcf = creator.createRDCF();
		UserDto user = creator.createUser(rdcf, UserRole.NATIONAL_USER);

		useSurveillanceOfficerLogin(rdcf);

		Date dateNow = new Date();

		EventDto event = creator
			.createEvent(EventStatus.SCREENING, EventInvestigationStatus.ONGOING, "Test event title", "Test description", user.toReference(), (e) -> {
				e.getEventLocation().setRegion(rdcf.region);
				e.getEventLocation().setDistrict(rdcf.district);
			});

		EventParticipantDto eventParticipant = creator.createEventParticipant(event.toReference(), creator.createPerson("John", "Doe", p -> {
			p.setBirthName("Test birth name");
		}), "Involved", user.toReference(), (ep) -> {
			ep.setRegion(rdcf.region);
			ep.setDistrict(rdcf.district);
			ep.getVaccinationInfo().setVaccination(Vaccination.VACCINATED);
		});

		SampleDto sample =
			creator.createSample(eventParticipant.toReference(), new Date(), new Date(), user.toReference(), SampleMaterial.BLOOD, rdcf.facility);
		PathogenTestDto pathogenTest = creator.createPathogenTest(
			sample.toReference(),
			PathogenTestType.CULTURE,
			Disease.CORONAVIRUS,
			new Date(),
			rdcf.facility,
			user.toReference(),
			PathogenTestResultType.INDETERMINATE,
			"Test result",
			true,
			null);
		AdditionalTestDto additionalTest = creator.createAdditionalTest(sample.toReference());

		SormasToSormasOptionsDto options = new SormasToSormasOptionsDto();
		options.setOrganization(new ServerAccessDataReferenceDto(SECOND_SERVER_ACCESS_CN));
		options.setWithEventParticipants(true);
		options.setWithSamples(true);
		options.setComment("Test comment");

		Mockito.when(MockProducer.getSormasToSormasClient().post(Matchers.anyString(), Matchers.anyString(), Matchers.anyString(), Matchers.any()))
			.thenAnswer(invocation -> {
				assertThat(invocation.getArgument(0, String.class), is(SECOND_SERVER_REST_URL));
				assertThat(invocation.getArgument(1, String.class), is("/sormasToSormas/events"));

				SormasToSormasEncryptedDataDto encryptedData = invocation.getArgument(3, SormasToSormasEncryptedDataDto.class);
				SormasToSormasEventDto[] sharedEvents = decryptSharesData(encryptedData.getData(), SormasToSormasEventDto[].class);
				SormasToSormasEventDto sharedEventData = sharedEvents[0];

				List<EventParticipantDto> eventParticipants = sharedEventData.getEventParticipants();
				assertThat(eventParticipants, hasSize(1));

				List<SormasToSormasSampleDto> samples = sharedEventData.getSamples();
				assertThat(samples, hasSize(1));

				assertThat(samples.get(0).getPathogenTests(), hasSize(1));
				assertThat(samples.get(0).getAdditionalTests(), hasSize(1));

				return Response.noContent().build();
			});

		getSormasToSormasEventFacade().shareEntities(Collections.singletonList(event.getUuid()), options);

		List<SormasToSormasShareInfoDto> shareInfoList =
			getSormasToSormasFacade().getShareInfoIndexList(new SormasToSormasShareInfoCriteria().event(event.toReference()), 0, 100);
		assertThat(shareInfoList.size(), is(1));
		assertThat(shareInfoList.get(0).getTarget().getUuid(), is(SECOND_SERVER_ACCESS_CN));
		assertThat(shareInfoList.get(0).getSender().getCaption(), is("Surv OFF - Surveillance Officer"));
		assertThat(shareInfoList.get(0).getComment(), is("Test comment"));
	}

	@Test
	public void testSaveSharedEvents() throws JsonProcessingException, SormasToSormasException, SormasToSormasValidationException {
		MappableRdcf rdcf = createRDCF(false);

		EventDto event = createEventDto(rdcf.remoteRdcf);
		event.setEventDesc("Test description");
		event.setEventStatus(EventStatus.SCREENING);
		event.setEventInvestigationStatus(EventInvestigationStatus.ONGOING);
		event.setRiskLevel(RiskLevel.MODERATE);
		event.setMultiDayEvent(true);
		Date dateNow = new Date();
		event.setStartDate(dateNow);
		event.setEndDate(dateNow);
		event.setSrcType(EventSourceType.MEDIA_NEWS);
		event.setSrcMediaWebsite("Test media name");
		event.setSrcMediaName("Test media website");
		event.setSrcMediaDetails("Test media details");

		SormasToSormasEncryptedDataDto encryptedData =
			encryptShareDataAsArray(new SormasToSormasEventDto(event, createSormasToSormasOriginInfo(DEFAULT_SERVER_ACCESS_CN, false)));

		getSormasToSormasEventFacade().saveSharedEntities(encryptedData, null);

		EventDto savedEvent = getEventFacade().getEventByUuid(event.getUuid());

		assertThat(savedEvent, is(notNullValue()));

		assertThat(savedEvent.getEventTitle(), is("Test event title"));
		assertThat(savedEvent.getEventDesc(), is("Test description"));
		assertThat(savedEvent.getEventStatus(), is(EventStatus.SCREENING));
		assertThat(savedEvent.getEventInvestigationStatus(), is(EventInvestigationStatus.ONGOING));
		assertThat(savedEvent.getRiskLevel(), is(RiskLevel.MODERATE));
		assertThat(savedEvent.isMultiDayEvent(), is(true));
		assertThat(savedEvent.getStartDate().getTime(), is(dateNow.getTime()));
		assertThat(savedEvent.getEndDate().getTime(), is(dateNow.getTime()));
		assertThat(savedEvent.getSrcType(), is(EventSourceType.MEDIA_NEWS));
		assertThat(savedEvent.getSrcMediaWebsite(), is("Test media name"));
		assertThat(savedEvent.getSrcMediaName(), is("Test media website"));
		assertThat(savedEvent.getSrcMediaDetails(), is("Test media details"));

		assertThat(savedEvent.getEventLocation().getRegion(), is(rdcf.localRdcf.region));
		assertThat(savedEvent.getEventLocation().getDistrict(), is(rdcf.localRdcf.district));

		assertThat(savedEvent.getSormasToSormasOriginInfo().getOrganizationId(), is(DEFAULT_SERVER_ACCESS_CN));
		assertThat(savedEvent.getSormasToSormasOriginInfo().getSenderName(), is("John doe"));
	}

	@Test
	public void testSaveSharedEventsWithParticipants() throws JsonProcessingException, SormasToSormasException, SormasToSormasValidationException {
		MappableRdcf rdcf = createRDCF(false);

		EventDto event = createEventDto(rdcf.remoteRdcf);
		EventParticipantDto eventParticipant = createEventParticipantDto(event.toReference(), UserDto.build().toReference(), rdcf);

		SormasToSormasEventDto shareData = new SormasToSormasEventDto(event, createSormasToSormasOriginInfo(DEFAULT_SERVER_ACCESS_CN, false));
		shareData.setEventParticipants(Collections.singletonList(eventParticipant));

		SormasToSormasEncryptedDataDto encryptedData = encryptShareDataAsArray(shareData);

		getSormasToSormasEventFacade().saveSharedEntities(encryptedData, null);

		EventParticipantDto savedParticipant = getEventParticipantFacade().getEventParticipantByUuid(eventParticipant.getUuid());

		assertThat(savedParticipant, is(notNullValue()));

		assertThat(savedParticipant.getPerson(), is(notNullValue()));
		assertThat(savedParticipant.getPerson().getFirstName(), is("John"));
		assertThat(savedParticipant.getPerson().getLastName(), is("Smith"));

		assertThat(savedParticipant.getPerson().getAddress().getRegion(), is(rdcf.localRdcf.region));
		assertThat(savedParticipant.getPerson().getAddress().getDistrict(), is(rdcf.localRdcf.district));

		assertThat(savedParticipant.getSormasToSormasOriginInfo().getOrganizationId(), is(DEFAULT_SERVER_ACCESS_CN));
		assertThat(savedParticipant.getSormasToSormasOriginInfo().getSenderName(), is("John doe"));
	}

	@Test
	public void testSaveSharedEventsWithSamples() throws JsonProcessingException, SormasToSormasException, SormasToSormasValidationException {
		MappableRdcf rdcf = createRDCF(false);
		FacilityDto remoteLab = FacilityDto.build();
		remoteLab.setName("Test Lab");
		FacilityDto localLab = creator.createFacility("Test Lab", rdcf.localRdcf.region, rdcf.localRdcf.district, null, FacilityType.LABORATORY);

		EventDto event = createEventDto(rdcf.remoteRdcf);
		UserDto sampleUser = UserDto.build();
		EventParticipantDto eventParticipant = createEventParticipantDto(event.toReference(), sampleUser.toReference(), rdcf);

		SampleDto sample = createSample(eventParticipant.toReference(), sampleUser.toReference(), remoteLab.toReference());
		sample.setLabSampleID("Test lab sample id");

		PathogenTestDto pathogenTest = PathogenTestDto.build(sample, sampleUser);
		pathogenTest.setTestDateTime(new Date());
		pathogenTest.setLab(remoteLab.toReference());
		pathogenTest.setTestType(PathogenTestType.RAPID_TEST);
		pathogenTest.setTestResult(PathogenTestResultType.PENDING);

		AdditionalTestDto additionalTest = AdditionalTestDto.build(sample.toReference());
		additionalTest.setTestDateTime(new Date());
		additionalTest.setHaemoglobin(0.2F);
		additionalTest.setConjBilirubin(0.3F);

		SormasToSormasEventDto shareData = new SormasToSormasEventDto(event, createSormasToSormasOriginInfo(DEFAULT_SERVER_ACCESS_CN, false));
		shareData.setEventParticipants(Collections.singletonList(eventParticipant));
		shareData.setSamples(
			Collections.singletonList(
				new SormasToSormasSampleDto(sample, Collections.singletonList(pathogenTest), Collections.singletonList(additionalTest))));
		SormasToSormasEncryptedDataDto encryptedData = encryptShareDataAsArray(shareData);

		getSormasToSormasEventFacade().saveSharedEntities(encryptedData, null);

		SampleDto savedSample = getSampleFacade().getSampleByUuid(sample.getUuid());
		assertThat(savedSample, is(notNullValue()));
		assertThat(savedSample.getAssociatedEventParticipant(), is(eventParticipant.toReference()));
		assertThat(savedSample.getSampleMaterial(), is(SampleMaterial.BLOOD));
		assertThat(savedSample.getLab(), is(localLab.toReference()));
		assertThat(savedSample.getLabSampleID(), is("Test lab sample id"));

		assertThat(savedSample.getSormasToSormasOriginInfo().getOrganizationId(), is(DEFAULT_SERVER_ACCESS_CN));
		assertThat(savedSample.getSormasToSormasOriginInfo().getSenderName(), is("John doe"));

		PathogenTestDto savedPathogenTest = getPathogenTestFacade().getByUuid(pathogenTest.getUuid());
		assertThat(savedPathogenTest, is(notNullValue()));
		assertThat(savedPathogenTest.getLab(), is(localLab.toReference()));
		assertThat(savedPathogenTest.getTestType(), is(PathogenTestType.RAPID_TEST));
		assertThat(savedPathogenTest.getTestResult(), is(PathogenTestResultType.PENDING));

		AdditionalTestDto savedAdditionalTest = getAdditionalTestFacade().getByUuid(additionalTest.getUuid());
		assertThat(savedAdditionalTest, is(notNullValue()));
		assertThat(savedAdditionalTest.getHaemoglobin(), is(0.2F));
		assertThat(savedAdditionalTest.getConjBilirubin(), is(0.3F));
	}

	@Test
	public void testReturnEvent() throws JsonProcessingException, SormasToSormasException {
		TestDataCreator.RDCF rdcf = creator.createRDCF();

		useSurveillanceOfficerLogin(rdcf);

		UserReferenceDto officer = creator.createUser(rdcf, UserRole.SURVEILLANCE_OFFICER).toReference();

		EventDto event =
			creator.createEvent(EventStatus.SCREENING, EventInvestigationStatus.ONGOING, "Test event title", "Test description", officer, (e) -> {
				SormasToSormasOriginInfoDto originInfo = new SormasToSormasOriginInfoDto();
				originInfo.setSenderName("Test Name");
				originInfo.setSenderEmail("test@email.com");
				originInfo.setOrganizationId(DEFAULT_SERVER_ACCESS_CN);
				originInfo.setOwnershipHandedOver(true);

				e.setSormasToSormasOriginInfo(originInfo);
			});

		SormasToSormasShareRequestDto shareRequest = new SormasToSormasShareRequestDto();
		shareRequest.setUuid(DataHelper.createUuid());
		shareRequest.setOriginInfo(event.getSormasToSormasOriginInfo());
		getSormasToSormasShareRequestFacade().saveShareRequest(shareRequest);

		PersonDto person = creator.createPerson();
		EventParticipantDto eventParticipant = creator.createEventParticipant(event.toReference(), person, "Involved", officer, (p) -> {
			p.setSormasToSormasOriginInfo(event.getSormasToSormasOriginInfo());
		});
		EventParticipantDto newEventParticipant = creator.createEventParticipant(event.toReference(), person, "Involved", officer);

		SormasToSormasOptionsDto options = new SormasToSormasOptionsDto();
		options.setOrganization(new ServerAccessDataReferenceDto(SECOND_SERVER_ACCESS_CN));
		options.setHandOverOwnership(true);
		options.setWithEventParticipants(true);
		options.setComment("Test comment");

		Mockito.when(MockProducer.getSormasToSormasClient().put(Matchers.anyString(), Matchers.anyString(), Matchers.anyString(), Matchers.any()))
			.thenAnswer(invocation -> Response.noContent().build());

		getSormasToSormasEventFacade().returnEntity(event.getUuid(), options);

		// contact ownership should be lost
		EventDto sharedEvent = getEventFacade().getEventByUuid(event.getUuid());
		assertThat(sharedEvent.getSormasToSormasOriginInfo().isOwnershipHandedOver(), is(false));

		// sample ownership should be lost
		EventParticipantDto sharedEventParticipant = getEventParticipantFacade().getEventParticipantByUuid(eventParticipant.getUuid());
		assertThat(sharedEventParticipant.getSormasToSormasOriginInfo().isOwnershipHandedOver(), is(false));

		// new samples should have share info with ownership handed over
		List<SormasToSormasShareInfoDto> newEventParticipantShareInfos = getSormasToSormasFacade()
			.getShareInfoIndexList(new SormasToSormasShareInfoCriteria().eventParticipant(newEventParticipant.toReference()), 0, 100);
		assertThat(newEventParticipantShareInfos, hasSize(1));
		assertThat(newEventParticipantShareInfos.get(0).isOwnershipHandedOver(), is(true));
	}

	@Test
	public void testSaveReturnedEvent() throws JsonProcessingException, SormasToSormasException, SormasToSormasValidationException {
		MappableRdcf rdcf = createRDCF(false);

		UserReferenceDto officer = creator.createUser(rdcf.localRdcf, UserRole.SURVEILLANCE_OFFICER).toReference();

		EventDto event = creator.createEvent(officer);
		EventParticipantDto eventParticipant = creator.createEventParticipant(event.toReference(), creator.createPerson(), officer);
		EventParticipantDto newEventParticipant = createEventParticipantDto(event.toReference(), UserDto.build().toReference(), rdcf);

		FacilityDto lab = creator.createFacility("Test Lab", rdcf.localRdcf.region, rdcf.localRdcf.district, null, FacilityType.LABORATORY);
		SampleDto newSample = createSample(newEventParticipant.toReference(), officer, lab.toReference());

		User officerUser = getUserService().getByReferenceDto(officer);
		getSormasToSormasShareInfoService().persist(
			createShareInfo(
				officerUser,
				DEFAULT_SERVER_ACCESS_CN,
				true,
				i -> i.getEvents().add(new ShareInfoEvent(i, getEventService().getByReferenceDto(event.toReference())))));
		getSormasToSormasShareInfoService().persist(
			createShareInfo(
				officerUser,
				DEFAULT_SERVER_ACCESS_CN,
				true,
				i -> i.getEventParticipants()
					.add(new ShareInfoEventParticipant(i, getEventParticipantService().getByReferenceDto(eventParticipant.toReference())))));

		event.setEventDesc("Test updated description");
		eventParticipant.getPerson().setBirthName("Test birth name");

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(event.getChangeDate());
		calendar.add(Calendar.DAY_OF_MONTH, 1);
		event.setChangeDate(calendar.getTime());

		SormasToSormasEventDto shareData = new SormasToSormasEventDto(event, createSormasToSormasOriginInfo(DEFAULT_SERVER_ACCESS_CN, true));
		shareData.setEventParticipants(Arrays.asList(eventParticipant, newEventParticipant));
		shareData.setSamples(Collections.singletonList(new SormasToSormasSampleDto(newSample, Collections.emptyList(), Collections.emptyList())));

		SormasToSormasEncryptedDataDto encryptedData = encryptShareDataAsArray(shareData);

		getSormasToSormasEventFacade().saveReturnedEntity(encryptedData);

		EventDto returnedEvent = getEventFacade().getEventByUuid(event.getUuid());
		assertThat(returnedEvent.getEventDesc(), is("Test updated description"));
		assertThat(returnedEvent.getReportingUser(), is(officer));

		List<SormasToSormasShareInfoDto> eventShares =
			getSormasToSormasFacade().getShareInfoIndexList(new SormasToSormasShareInfoCriteria().event(event.toReference()), 0, 100);
		assertThat(eventShares.get(0).isOwnershipHandedOver(), is(false));

		assertThat(
			getEventParticipantFacade().getEventParticipantByUuid(eventParticipant.getUuid()).getPerson().getBirthName(),
			is("Test birth name"));
		List<SormasToSormasShareInfoDto> eventParticipantShares = getSormasToSormasFacade()
			.getShareInfoIndexList(new SormasToSormasShareInfoCriteria().eventParticipant(eventParticipant.toReference()), 0, 100);
		assertThat(eventParticipantShares.get(0).isOwnershipHandedOver(), is(false));

		EventParticipantDto returnedNewEventParticipant = getEventParticipantFacade().getEventParticipantByUuid(newEventParticipant.getUuid());
		assertThat(returnedNewEventParticipant.getSormasToSormasOriginInfo().isOwnershipHandedOver(), is(true));

		SampleDto returnedNewSample = getSampleFacade().getSampleByUuid(newSample.getUuid());
		assertThat(returnedNewSample.getSormasToSormasOriginInfo().isOwnershipHandedOver(), is(true));
	}

	@Test
	public void testSyncEvent() throws JsonProcessingException, SormasToSormasException {
		TestDataCreator.RDCF rdcf = creator.createRDCF();

		useSurveillanceOfficerLogin(rdcf);

		UserReferenceDto officer = creator.createUser(rdcf, UserRole.SURVEILLANCE_OFFICER).toReference();

		EventDto event =
			creator.createEvent(EventStatus.SCREENING, EventInvestigationStatus.ONGOING, "Test event title", "Test description", officer, e -> {
				e.getEventLocation().setRegion(rdcf.region);
				e.getEventLocation().setDistrict(rdcf.district);
			});

		PersonDto person = creator.createPerson();
		EventParticipantDto eventParticipant = creator.createEventParticipant(event.toReference(), person, "Involved", officer);

		getSormasToSormasShareInfoService()
			.persist(createShareInfo(getUserService().getByUuid(officer.getUuid()), SECOND_SERVER_ACCESS_CN, false, i -> {
				i.getEvents().add(new ShareInfoEvent(i, getEventService().getByUuid(event.getUuid())));
				i.getEventParticipants().add(new ShareInfoEventParticipant(i, getEventParticipantService().getByUuid(eventParticipant.getUuid())));
			}));

		EventParticipantDto newEventParticipant = creator.createEventParticipant(event.toReference(), person, "Involved", officer);

		SormasToSormasOptionsDto options = new SormasToSormasOptionsDto();
		options.setOrganization(new ServerAccessDataReferenceDto(SECOND_SERVER_ACCESS_CN));
		options.setHandOverOwnership(true);
		options.setWithEventParticipants(true);
		options.setComment("Test comment");

		Mockito.when(MockProducer.getSormasToSormasClient().post(Matchers.anyString(), Matchers.anyString(), Matchers.anyString(), Matchers.any()))
			.thenAnswer(invocation -> Response.noContent().build());

		getSormasToSormasEventFacade().syncEntity(event.getUuid(), options);

		// contact ownership should be lost
		List<SormasToSormasShareInfoDto> eventShareInfoList =
			getSormasToSormasFacade().getShareInfoIndexList(new SormasToSormasShareInfoCriteria().event(event.toReference()), 0, 100);
		assertThat(eventShareInfoList, hasSize(1));
		assertThat(eventShareInfoList.get(0).isOwnershipHandedOver(), is(true));

		// event participant ownership should be lost
		List<SormasToSormasShareInfoDto> eventParticipantShareInfoList = getSormasToSormasFacade()
			.getShareInfoIndexList(new SormasToSormasShareInfoCriteria().eventParticipant(eventParticipant.toReference()), 0, 100);
		assertThat(eventParticipantShareInfoList, hasSize(1));
		assertThat(eventParticipantShareInfoList.get(0).isOwnershipHandedOver(), is(true));

		// new event participant should have share info with ownership handed over
		List<SormasToSormasShareInfoDto> newEventParticipantShareInfoList = getSormasToSormasFacade()
			.getShareInfoIndexList(new SormasToSormasShareInfoCriteria().eventParticipant(newEventParticipant.toReference()), 0, 100);
		assertThat(newEventParticipantShareInfoList, hasSize(1));
		assertThat(newEventParticipantShareInfoList.get(0).isOwnershipHandedOver(), is(true));

	}

	@Test
	public void testSaveSyncedEvent() throws JsonProcessingException, SormasToSormasException, SormasToSormasValidationException {
		MappableRdcf rdcf = createRDCF(false);

		UserReferenceDto officer = creator.createUser(rdcf.localRdcf, UserRole.SURVEILLANCE_OFFICER).toReference();

		EventDto event =
			creator.createEvent(EventStatus.SCREENING, EventInvestigationStatus.ONGOING, "Test event title", "Test description", officer, e -> {
				SormasToSormasOriginInfoDto originInfo = new SormasToSormasOriginInfoDto();
				originInfo.setSenderName("Test Name");
				originInfo.setSenderEmail("test@email.com");
				originInfo.setOrganizationId(DEFAULT_SERVER_ACCESS_CN);
				originInfo.setOwnershipHandedOver(false);

				e.setSormasToSormasOriginInfo(originInfo);
			});

		EventParticipantDto eventParticipant =
			creator.createEventParticipant(event.toReference(), creator.createPerson(), "Incolved", officer, (ep) -> {
				ep.setSormasToSormasOriginInfo(event.getSormasToSormasOriginInfo());
			});

		EventParticipantDto newEventParticipant = createEventParticipantDto(event.toReference(), UserDto.build().toReference(), rdcf);

		event.setEventDesc("Test updated description");
		eventParticipant.getPerson().setBirthName("Test birth name");

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(event.getChangeDate());
		calendar.add(Calendar.DAY_OF_MONTH, 1);
		event.setChangeDate(calendar.getTime());

		SormasToSormasEventDto shareData = new SormasToSormasEventDto(event, createSormasToSormasOriginInfo(DEFAULT_SERVER_ACCESS_CN, true));
		shareData.setEventParticipants(Arrays.asList(eventParticipant, newEventParticipant));

		SormasToSormasEncryptedDataDto encryptedData = encryptShareDataAsArray(shareData);

		getSormasToSormasEventFacade().saveSyncedEntity(encryptedData);

		EventDto syncedEvent = getEventFacade().getEventByUuid(event.getUuid());
		assertThat(syncedEvent.getEventDesc(), is("Test updated description"));
		assertThat(syncedEvent.getSormasToSormasOriginInfo().isOwnershipHandedOver(), is(true));

		EventParticipantDto syncedEventParticipant = getEventParticipantFacade().getEventParticipantByUuid(eventParticipant.getUuid());
		assertThat(syncedEventParticipant.getPerson().getBirthName(), is("Test birth name"));
		assertThat(syncedEventParticipant.getSormasToSormasOriginInfo().isOwnershipHandedOver(), is(true));

		EventParticipantDto returnedNewEventParticipant = getEventParticipantFacade().getEventParticipantByUuid(newEventParticipant.getUuid());
		assertThat(returnedNewEventParticipant.getSormasToSormasOriginInfo().isOwnershipHandedOver(), is(true));
	}

	private EventDto createEventDto(TestDataCreator.RDCF rdcf) {
		EventDto eventDto = EventDto.build();

		eventDto.setEventTitle("Test event title");
		eventDto.getEventLocation().setRegion(rdcf.region);
		eventDto.getEventLocation().setDistrict(rdcf.district);

		return eventDto;
	}

	private EventParticipantDto createEventParticipantDto(EventReferenceDto event, UserReferenceDto user, MappableRdcf rdcf) {
		EventParticipantDto participantDto = EventParticipantDto.build(event, user);
		participantDto.setPerson(createPersonDto(rdcf));

		participantDto.setReportingUser(user);
		participantDto.setRegion(rdcf.remoteRdcf.region);
		participantDto.setDistrict(rdcf.remoteRdcf.district);

		return participantDto;
	}

	private SampleDto createSample(EventParticipantReferenceDto eventParticipant, UserReferenceDto reportingUser, FacilityReferenceDto lab) {
		SampleDto sample = SampleDto.build(reportingUser, eventParticipant);
		sample.setSampleMaterial(SampleMaterial.BLOOD);
		sample.setSamplePurpose(SamplePurpose.INTERNAL);
		sample.setLab(lab);
		sample.setSampleDateTime(new Date());

		return sample;
	}

}
