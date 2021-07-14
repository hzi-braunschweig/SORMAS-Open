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
import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.eq;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
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
import de.symeda.sormas.api.caze.CaseClassification;
import de.symeda.sormas.api.caze.CaseDataDto;
import de.symeda.sormas.api.caze.CaseOrigin;
import de.symeda.sormas.api.caze.CaseOutcome;
import de.symeda.sormas.api.caze.porthealthinfo.PortHealthInfoDto;
import de.symeda.sormas.api.contact.ContactDto;
import de.symeda.sormas.api.contact.QuarantineType;
import de.symeda.sormas.api.exposure.AnimalContactType;
import de.symeda.sormas.api.exposure.ExposureDto;
import de.symeda.sormas.api.exposure.ExposureType;
import de.symeda.sormas.api.facility.FacilityDto;
import de.symeda.sormas.api.facility.FacilityReferenceDto;
import de.symeda.sormas.api.facility.FacilityType;
import de.symeda.sormas.api.infrastructure.PointOfEntryDto;
import de.symeda.sormas.api.infrastructure.PointOfEntryReferenceDto;
import de.symeda.sormas.api.infrastructure.PointOfEntryType;
import de.symeda.sormas.api.person.PersonDto;
import de.symeda.sormas.api.person.Sex;
import de.symeda.sormas.api.sample.SampleDto;
import de.symeda.sormas.api.sormastosormas.ServerAccessDataReferenceDto;
import de.symeda.sormas.api.sormastosormas.SormasToSormasEncryptedDataDto;
import de.symeda.sormas.api.sormastosormas.SormasToSormasException;
import de.symeda.sormas.api.sormastosormas.SormasToSormasOptionsDto;
import de.symeda.sormas.api.sormastosormas.SormasToSormasOriginInfoDto;
import de.symeda.sormas.api.sormastosormas.SormasToSormasSampleDto;
import de.symeda.sormas.api.sormastosormas.SormasToSormasShareTree;
import de.symeda.sormas.api.sormastosormas.SormasToSormasValidationException;
import de.symeda.sormas.api.sormastosormas.caze.SormasToSormasCaseDto;
import de.symeda.sormas.api.sormastosormas.shareinfo.SormasToSormasShareInfoCriteria;
import de.symeda.sormas.api.sormastosormas.shareinfo.SormasToSormasShareInfoDto;
import de.symeda.sormas.api.sormastosormas.sharerequest.ShareRequestStatus;
import de.symeda.sormas.api.sormastosormas.sharerequest.SormasToSormasCasePreview;
import de.symeda.sormas.api.sormastosormas.sharerequest.SormasToSormasShareRequestDto;
import de.symeda.sormas.api.symptoms.SymptomState;
import de.symeda.sormas.api.user.UserDto;
import de.symeda.sormas.api.user.UserReferenceDto;
import de.symeda.sormas.api.user.UserRole;
import de.symeda.sormas.api.utils.DataHelper;
import de.symeda.sormas.api.utils.YesNoUnknown;
import de.symeda.sormas.backend.MockProducer;
import de.symeda.sormas.backend.TestDataCreator;
import de.symeda.sormas.backend.sormastosormas.caze.CaseShareRequestData;
import de.symeda.sormas.backend.sormastosormas.shareinfo.ShareInfoCase;
import de.symeda.sormas.backend.sormastosormas.shareinfo.ShareInfoContact;
import de.symeda.sormas.backend.sormastosormas.shareinfo.ShareInfoSample;
import de.symeda.sormas.backend.sormastosormas.shareinfo.SormasToSormasShareInfo;
import de.symeda.sormas.backend.user.User;

@RunWith(MockitoJUnitRunner.class)
public class SormasToSormasCaseFacadeEjbTest extends SormasToSormasFacadeTest {

	@Override
	public void init() {
		super.init();

		getFacilityService().createConstantFacilities();
		getPointOfEntryService().createConstantPointsOfEntry();
	}

	@Test
	public void testShareCase() throws SormasToSormasException {
		TestDataCreator.RDCF rdcf = creator.createRDCF();

		useSurveillanceOfficerLogin(rdcf);

		PersonDto person = creator.createPerson();
		UserReferenceDto officer = creator.createUser(rdcf, UserRole.SURVEILLANCE_OFFICER).toReference();
		CaseDataDto caze = creator.createCase(officer, rdcf, dto -> {
			dto.setPerson(person.toReference());
			dto.setSurveillanceOfficer(officer);
			dto.setClassificationUser(officer);
		});

		SormasToSormasOptionsDto options = new SormasToSormasOptionsDto();
		options.setOrganization(new ServerAccessDataReferenceDto(SECOND_SERVER_ACCESS_ID));
		options.setComment("Test comment");

		Mockito.when(MockProducer.getSormasToSormasClient().post(Matchers.anyString(), Matchers.anyString(), Matchers.any(), Matchers.any()))
			.thenAnswer(invocation -> {
				assertThat(invocation.getArgument(0, String.class), is(SECOND_SERVER_ACCESS_ID));
				assertThat(invocation.getArgument(1, String.class), is("/sormasToSormas/cases"));

				List<SormasToSormasCaseDto> postBody = invocation.getArgument(2, List.class);
				assertThat(postBody.size(), is(1));
				SormasToSormasCaseDto sharedCase = postBody.get(0);

				assertThat(sharedCase.getPerson().getFirstName(), is(person.getFirstName()));
				assertThat(sharedCase.getPerson().getLastName(), is(person.getLastName()));

				assertThat(sharedCase.getEntity().getUuid(), is(caze.getUuid()));
				// users should be cleaned up
				assertThat(sharedCase.getEntity().getReportingUser(), is(nullValue()));
				assertThat(sharedCase.getEntity().getSurveillanceOfficer(), is(nullValue()));
				assertThat(sharedCase.getEntity().getClassificationUser(), is(nullValue()));

				// share information
				assertThat(sharedCase.getOriginInfo().getOrganizationId(), is(DEFAULT_SERVER_ACCESS_ID));
				assertThat(sharedCase.getOriginInfo().getSenderName(), is("Surv Off"));
				assertThat(sharedCase.getOriginInfo().getComment(), is("Test comment"));

				return Response.noContent().build();
			});

		getSormasToSormasCaseFacade().shareEntities(Collections.singletonList(caze.getUuid()), options);

		List<SormasToSormasShareInfoDto> shareInfoList =
			getSormasToSormasShareInfoFacade().getIndexList(new SormasToSormasShareInfoCriteria().caze(caze.toReference()), 0, 100);

		assertThat(shareInfoList.size(), is(1));
		assertThat(shareInfoList.get(0).getTarget().getUuid(), is(SECOND_SERVER_ACCESS_ID));
		assertThat(shareInfoList.get(0).getSender().getCaption(), is("Surv OFF - Surveillance Officer"));
		assertThat(shareInfoList.get(0).getComment(), is("Test comment"));
	}

	@Test
	public void testShareCaseWithContacts() throws SormasToSormasException {
		TestDataCreator.RDCF rdcf = creator.createRDCF();

		useSurveillanceOfficerLogin(rdcf);

		PersonDto person = creator.createPerson();
		UserReferenceDto officer = creator.createUser(rdcf, UserRole.SURVEILLANCE_OFFICER).toReference();
		CaseDataDto caze = creator.createCase(officer, rdcf, dto -> {
			dto.setPerson(person.toReference());
			dto.setSurveillanceOfficer(officer);
			dto.setClassificationUser(officer);
		});

		ContactDto contact = creator.createContact(officer, creator.createPerson().toReference(), caze);

		SormasToSormasOptionsDto options = new SormasToSormasOptionsDto();
		options.setOrganization(new ServerAccessDataReferenceDto(SECOND_SERVER_ACCESS_ID));
		options.setComment("Test comment");
		options.setWithAssociatedContacts(true);

		Mockito.when(MockProducer.getSormasToSormasClient().post(Matchers.anyString(), Matchers.anyString(), Matchers.any(), Matchers.any()))
			.thenAnswer(invocation -> {
				assertThat(invocation.getArgument(0, String.class), is(SECOND_SERVER_ACCESS_ID));
				assertThat(invocation.getArgument(1, String.class), is("/sormasToSormas/cases"));

				List<SormasToSormasCaseDto> postBody = invocation.getArgument(2, List.class);
				assertThat(postBody.size(), is(1));
				SormasToSormasCaseDto sharedCase = postBody.get(0);
				assertThat(sharedCase.getAssociatedContacts().size(), is(1));

				return Response.noContent().build();
			});

		getSormasToSormasCaseFacade().shareEntities(Collections.singletonList(caze.getUuid()), options);

		List<SormasToSormasShareInfoDto> shareInfoList =
			getSormasToSormasShareInfoFacade().getIndexList(new SormasToSormasShareInfoCriteria().contact(contact.toReference()), 0, 100);

		SormasToSormasShareInfoDto contactShareInfo = shareInfoList.get(0);
		assertThat(contactShareInfo.getTarget().getUuid(), is(SECOND_SERVER_ACCESS_ID));
		assertThat(contactShareInfo.getSender().getCaption(), is("Surv OFF - Surveillance Officer"));
		assertThat(contactShareInfo.getComment(), is("Test comment"));
	}

	@Test
	public void testShareCaseWithSamples() throws SormasToSormasException, JsonProcessingException {
		TestDataCreator.RDCF rdcf = creator.createRDCF();

		useSurveillanceOfficerLogin(rdcf);

		PersonDto person = creator.createPerson();
		UserDto user = creator.createUser(rdcf, UserRole.SURVEILLANCE_OFFICER);
		UserReferenceDto officer = user.toReference();
		CaseDataDto caze = creator.createCase(officer, rdcf, dto -> {
			dto.setPerson(person.toReference());
			dto.setSurveillanceOfficer(officer);
			dto.setClassificationUser(officer);
		});

		Date sampleDateTime = new Date();
		SampleDto caseSample = creator.createSample(caze.toReference(), user.toReference(), rdcf.facility, s -> {
			s.setSampleDateTime(sampleDateTime);
			s.setComment("Test case sample");
		});

		creator.createPathogenTest(caseSample.toReference(), caze);
		creator.createAdditionalTest(caseSample.toReference());

		ContactDto contact = creator.createContact(officer, creator.createPerson().toReference(), caze);
		SampleDto contactSampleSample = creator.createSample(contact.toReference(), officer, rdcf.facility, s -> {
			s.setSampleDateTime(sampleDateTime);
			s.setComment("Test contact sample");
		});
		creator.createPathogenTest(contactSampleSample.toReference(), caze);
		creator.createAdditionalTest(contactSampleSample.toReference());

		SormasToSormasOptionsDto options = new SormasToSormasOptionsDto();
		options.setOrganization(new ServerAccessDataReferenceDto(SECOND_SERVER_ACCESS_ID));
		options.setComment("Test comment");
		options.setWithSamples(true);
		options.setWithAssociatedContacts(true);

		Mockito.when(MockProducer.getSormasToSormasClient().post(Matchers.anyString(), Matchers.anyString(), Matchers.any(), Matchers.any()))
			.thenAnswer(invocation -> {

				assertThat(invocation.getArgument(0, String.class), is(SECOND_SERVER_ACCESS_ID));
				assertThat(invocation.getArgument(1, String.class), is("/sormasToSormas/cases"));

				List<SormasToSormasCaseDto> postBody = invocation.getArgument(2, List.class);
				assertThat(postBody.size(), is(1));
				SormasToSormasCaseDto sharedCase = postBody.get(0);

				assertThat(sharedCase.getSamples().size(), is(2));
				SormasToSormasSampleDto sharedCaseSample = sharedCase.getSamples().get(0);
				assertThat(sharedCaseSample.getSample().getSampleDateTime().compareTo(sampleDateTime), is(0)); // use compareTo because getSampleDateTime() returns Timestamp object due to Sample.java using TemporalType.Timestamp
				assertThat(sharedCaseSample.getSample().getComment(), is("Test case sample"));
				assertThat(sharedCaseSample.getPathogenTests(), hasSize(1));
				assertThat(sharedCaseSample.getAdditionalTests(), hasSize(1));

				SormasToSormasSampleDto sharedContactSample = sharedCase.getSamples().get(1);
				assertThat(sharedContactSample.getSample().getSampleDateTime().compareTo(sampleDateTime), is(0));
				assertThat(sharedContactSample.getSample().getComment(), is("Test contact sample"));
				assertThat(sharedContactSample.getPathogenTests(), hasSize(1));
				assertThat(sharedContactSample.getAdditionalTests(), hasSize(1));

				return Response.noContent().build();
			});

		getSormasToSormasCaseFacade().shareEntities(Collections.singletonList(caze.getUuid()), options);

		List<SormasToSormasShareInfoDto> shareInfoList =
			getSormasToSormasShareInfoFacade().getIndexList(new SormasToSormasShareInfoCriteria().sample(caseSample.toReference()), 0, 100);

		SormasToSormasShareInfoDto contactShareInfo = shareInfoList.get(0);
		assertThat(contactShareInfo.getTarget().getUuid(), is(SECOND_SERVER_ACCESS_ID));
		assertThat(contactShareInfo.getSender().getCaption(), is("Surv OFF - Surveillance Officer"));
		assertThat(contactShareInfo.getComment(), is("Test comment"));
	}

	@Test
	public void testSaveSharedCaseWithInfrastructureName()
		throws JsonProcessingException, SormasToSormasException, SormasToSormasValidationException {
		MappableRdcf rdcf = createRDCF(false);

		PersonDto person = createPersonDto(rdcf);
		person.setFirstName("James");
		person.setLastName("Smith");

		CaseDataDto caze = createRemoteCaseDto(rdcf.remoteRdcf, person);
		caze.getHospitalization().setAdmittedToHealthFacility(YesNoUnknown.YES);
		caze.getSymptoms().setAgitation(SymptomState.YES);
		ExposureDto exposure = ExposureDto.build(ExposureType.ANIMAL_CONTACT);
		exposure.setAnimalContactType(AnimalContactType.TOUCH);
		caze.getEpiData().getExposures().add(exposure);
		caze.getClinicalCourse().getHealthConditions().setAsplenia(YesNoUnknown.YES);
		caze.getMaternalHistory().setChildrenNumber(2);

		SormasToSormasEncryptedDataDto encryptedData =
			encryptShareDataAsArray(new SormasToSormasCaseDto(person, caze, createSormasToSormasOriginInfo(DEFAULT_SERVER_ACCESS_ID, false)));

		getSormasToSormasCaseFacade().saveSharedEntities(encryptedData, null);

		CaseDataDto savedCase = getCaseFacade().getCaseDataByUuid(caze.getUuid());

		assertThat(savedCase, is(notNullValue()));
		assertThat(savedCase.getResponsibleRegion(), is(rdcf.localRdcf.region));
		assertThat(savedCase.getResponsibleDistrict(), is(rdcf.localRdcf.district));
		assertThat(savedCase.getResponsibleCommunity(), is(rdcf.localRdcf.community));
		assertThat(savedCase.getHealthFacility(), is(rdcf.localRdcf.facility));
		assertThat(savedCase.getHospitalization().getAdmittedToHealthFacility(), is(YesNoUnknown.YES));
		assertThat(savedCase.getSymptoms().getAgitation(), is(SymptomState.YES));
		assertThat(savedCase.getEpiData().getExposures().get(0).getAnimalContactType(), is(AnimalContactType.TOUCH));
		assertThat(savedCase.getClinicalCourse().getHealthConditions().getAsplenia(), is(YesNoUnknown.YES));
		assertThat(savedCase.getMaternalHistory().getChildrenNumber(), is(2));

		assertThat(savedCase.getSormasToSormasOriginInfo().getOrganizationId(), is(DEFAULT_SERVER_ACCESS_ID));
		assertThat(savedCase.getSormasToSormasOriginInfo().getSenderName(), is("John doe"));

		PersonDto savedPerson = getPersonFacade().getPersonByUuid(savedCase.getPerson().getUuid());
		assertThat(savedPerson, is(notNullValue()));
		assertThat(savedPerson.getAddress().getRegion(), is(rdcf.localRdcf.region));
		assertThat(savedPerson.getAddress().getDistrict(), is(rdcf.localRdcf.district));
		assertThat(savedPerson.getAddress().getCommunity(), is(rdcf.localRdcf.community));
		assertThat(savedPerson.getFirstName(), is("James"));
		assertThat(savedPerson.getLastName(), is("Smith"));
	}

	@Test
	public void testSaveSharedCaseWithInfrastructureExternalId()
		throws JsonProcessingException, SormasToSormasException, SormasToSormasValidationException {
		MappableRdcf rdcf = createRDCF(true);

		PersonDto person = createPersonDto(rdcf);
		person.setFirstName("James");
		person.setLastName("Smith");

		CaseDataDto caze = createRemoteCaseDto(rdcf.remoteRdcf, person);
		caze.getHospitalization().setAdmittedToHealthFacility(YesNoUnknown.YES);
		caze.getSymptoms().setAgitation(SymptomState.YES);
		ExposureDto exposure = ExposureDto.build(ExposureType.ANIMAL_CONTACT);
		exposure.setAnimalContactType(AnimalContactType.TOUCH);
		caze.getEpiData().getExposures().add(exposure);
		caze.getClinicalCourse().getHealthConditions().setAsplenia(YesNoUnknown.YES);
		caze.getMaternalHistory().setChildrenNumber(2);

		SormasToSormasEncryptedDataDto encryptedData =
			encryptShareDataAsArray(new SormasToSormasCaseDto(person, caze, createSormasToSormasOriginInfo(DEFAULT_SERVER_ACCESS_ID, false)));

		getSormasToSormasCaseFacade().saveSharedEntities(encryptedData, null);

		CaseDataDto savedCase = getCaseFacade().getCaseDataByUuid(caze.getUuid());

		assertThat(savedCase, is(notNullValue()));
		assertThat(savedCase.getResponsibleRegion(), is(rdcf.localRdcf.region));
		assertThat(savedCase.getResponsibleDistrict(), is(rdcf.localRdcf.district));
		assertThat(savedCase.getResponsibleCommunity(), is(rdcf.localRdcf.community));
		assertThat(savedCase.getHealthFacility(), is(rdcf.localRdcf.facility));
		assertThat(savedCase.getHospitalization().getAdmittedToHealthFacility(), is(YesNoUnknown.YES));
		assertThat(savedCase.getSymptoms().getAgitation(), is(SymptomState.YES));
		assertThat(savedCase.getEpiData().getExposures().get(0).getAnimalContactType(), is(AnimalContactType.TOUCH));
		assertThat(savedCase.getClinicalCourse().getHealthConditions().getAsplenia(), is(YesNoUnknown.YES));
		assertThat(savedCase.getMaternalHistory().getChildrenNumber(), is(2));

		assertThat(savedCase.getSormasToSormasOriginInfo().getOrganizationId(), is(DEFAULT_SERVER_ACCESS_ID));
		assertThat(savedCase.getSormasToSormasOriginInfo().getSenderName(), is("John doe"));

		PersonDto savedPerson = getPersonFacade().getPersonByUuid(savedCase.getPerson().getUuid());
		assertThat(savedPerson, is(notNullValue()));
		assertThat(savedPerson.getAddress().getRegion(), is(rdcf.localRdcf.region));
		assertThat(savedPerson.getAddress().getDistrict(), is(rdcf.localRdcf.district));
		assertThat(savedPerson.getAddress().getCommunity(), is(rdcf.localRdcf.community));
		assertThat(savedPerson.getFirstName(), is("James"));
		assertThat(savedPerson.getLastName(), is("Smith"));
	}

	@Test
	public void testSaveSharedPointOfEntryCaseWithInfrastructureName()
		throws JsonProcessingException, SormasToSormasException, SormasToSormasValidationException {
		MappableRdcf rdcf = createRDCF(false);

		PersonDto person = createPersonDto(rdcf);

		CaseDataDto caze = CaseDataDto.build(person.toReference(), Disease.CORONAVIRUS);
		caze.setCaseOrigin(CaseOrigin.POINT_OF_ENTRY);
		caze.setResponsibleRegion(rdcf.remoteRdcf.region);
		caze.setResponsibleDistrict(rdcf.remoteRdcf.district);
		caze.setResponsibleCommunity(rdcf.remoteRdcf.community);
		caze.setPointOfEntry(rdcf.remoteRdcf.pointOfEntry);
		PortHealthInfoDto portHealthInfo = PortHealthInfoDto.build();
		portHealthInfo.setAirlineName("Test Airline");
		caze.setPortHealthInfo(portHealthInfo);

		SormasToSormasEncryptedDataDto encryptedData =
			encryptShareDataAsArray(new SormasToSormasCaseDto(person, caze, createSormasToSormasOriginInfo(DEFAULT_SERVER_ACCESS_ID, false)));
		getSormasToSormasCaseFacade().saveSharedEntities(encryptedData, null);

		CaseDataDto savedCase = getCaseFacade().getCaseDataByUuid(caze.getUuid());

		assertThat(savedCase.getResponsibleRegion(), is(rdcf.localRdcf.region));
		assertThat(savedCase.getResponsibleDistrict(), is(rdcf.localRdcf.district));
		assertThat(savedCase.getResponsibleCommunity(), is(rdcf.localRdcf.community));
		assertThat(savedCase.getPointOfEntry(), is(rdcf.localRdcf.pointOfEntry));
		assertThat(savedCase.getPortHealthInfo().getAirlineName(), is("Test Airline"));
	}

	@Test
	public void testSaveSharedPointOfEntryCaseWithInfrastructureExternalId()
		throws JsonProcessingException, SormasToSormasException, SormasToSormasValidationException {
		MappableRdcf rdcf = createRDCF(true);

		PersonDto person = createPersonDto(rdcf);

		CaseDataDto caze = CaseDataDto.build(person.toReference(), Disease.CORONAVIRUS);
		caze.setCaseOrigin(CaseOrigin.POINT_OF_ENTRY);
		caze.setResponsibleRegion(rdcf.remoteRdcf.region);
		caze.setResponsibleDistrict(rdcf.remoteRdcf.district);
		caze.setResponsibleCommunity(rdcf.remoteRdcf.community);
		caze.setPointOfEntry(rdcf.remoteRdcf.pointOfEntry);
		PortHealthInfoDto portHealthInfo = PortHealthInfoDto.build();
		portHealthInfo.setAirlineName("Test Airline");
		caze.setPortHealthInfo(portHealthInfo);

		SormasToSormasEncryptedDataDto encryptedData =
			encryptShareDataAsArray(new SormasToSormasCaseDto(person, caze, createSormasToSormasOriginInfo(DEFAULT_SERVER_ACCESS_ID, false)));
		getSormasToSormasCaseFacade().saveSharedEntities(encryptedData, null);

		CaseDataDto savedCase = getCaseFacade().getCaseDataByUuid(caze.getUuid());

		assertThat(savedCase.getResponsibleRegion(), is(rdcf.localRdcf.region));
		assertThat(savedCase.getResponsibleDistrict(), is(rdcf.localRdcf.district));
		assertThat(savedCase.getResponsibleCommunity(), is(rdcf.localRdcf.community));
		assertThat(savedCase.getPointOfEntry(), is(rdcf.localRdcf.pointOfEntry));
		assertThat(savedCase.getPortHealthInfo().getAirlineName(), is("Test Airline"));
	}

	@Test
	public void testSaveSharedCaseWithContacts() throws JsonProcessingException, SormasToSormasException, SormasToSormasValidationException {
		MappableRdcf rdcf = createRDCF(false);
		PersonDto person = createPersonDto(rdcf);

		CaseDataDto caze = createRemoteCaseDto(rdcf.remoteRdcf, person);

		ContactDto contact = createRemoteContactDto(rdcf.remoteRdcf, caze);

		PersonDto contactPerson = createPersonDto(rdcf);
		contact.setPerson(contactPerson.toReference());

		SormasToSormasCaseDto shareData = new SormasToSormasCaseDto(person, caze, createSormasToSormasOriginInfo(DEFAULT_SERVER_ACCESS_ID, false));
		shareData.setAssociatedContacts(Collections.singletonList(new SormasToSormasCaseDto.AssociatedContactDto(contactPerson, contact)));

		SormasToSormasEncryptedDataDto encryptedData = encryptShareDataAsArray(shareData);
		getSormasToSormasCaseFacade().saveSharedEntities(encryptedData, null);

		CaseDataDto savedCase = getCaseFacade().getCaseDataByUuid(caze.getUuid());
		ContactDto savedContact = getContactFacade().getContactByUuid(contact.getUuid());

		assertThat(savedContact, is(notNullValue()));
		assertThat(savedContact.getRegion(), is(rdcf.localRdcf.region));
		assertThat(savedContact.getDistrict(), is(rdcf.localRdcf.district));
		assertThat(savedContact.getCommunity(), is(rdcf.localRdcf.community));

		assertThat(savedCase.getSormasToSormasOriginInfo().getUuid(), is(savedContact.getSormasToSormasOriginInfo().getUuid()));
	}

	@Test
	public void testSaveSharedCaseWithSamples() throws JsonProcessingException, SormasToSormasException, SormasToSormasValidationException {
		MappableRdcf rdcf = createRDCF(false);
		PersonDto person = createPersonDto(rdcf);

		CaseDataDto caze = createRemoteCaseDto(rdcf.remoteRdcf, person);
		SormasToSormasSampleDto sample = createRemoteSampleDtoWithTests(rdcf.remoteRdcf, caze.toReference(), null);

		SormasToSormasCaseDto shareData = new SormasToSormasCaseDto(person, caze, createSormasToSormasOriginInfo(DEFAULT_SERVER_ACCESS_ID, false));
		shareData.setSamples(Collections.singletonList(sample));

		SormasToSormasEncryptedDataDto encryptedData = encryptShareDataAsArray(shareData);
		getSormasToSormasCaseFacade().saveSharedEntities(encryptedData, null);

		CaseDataDto savedCase = getCaseFacade().getCaseDataByUuid(caze.getUuid());
		SampleDto savedSample = getSampleFacade().getSampleByUuid(sample.getSample().getUuid());

		assertThat(savedSample, is(notNullValue()));
		assertThat(savedSample.getLab(), is(rdcf.localRdcf.facility));

		assertThat(getPathogenTestFacade().getAllBySample(savedSample.toReference()), hasSize(1));
		assertThat(getAdditionalTestFacade().getAllBySample(savedSample.getUuid()), hasSize(1));

		assertThat(savedCase.getSormasToSormasOriginInfo().getUuid(), is(savedSample.getSormasToSormasOriginInfo().getUuid()));
	}

	@Test
	public void testShareCaseWithPseudonymizePersonalData() throws SormasToSormasException {
		TestDataCreator.RDCF rdcf = creator.createRDCF();

		useSurveillanceOfficerLogin(rdcf);

		PersonDto person = creator.createPerson();
		UserReferenceDto officer = creator.createUser(rdcf, UserRole.SURVEILLANCE_OFFICER).toReference();
		CaseDataDto caze = creator.createCase(officer, rdcf, dto -> {
			dto.setPerson(person.toReference());
			dto.setSurveillanceOfficer(officer);
			dto.setClassificationUser(officer);
			dto.setAdditionalDetails("Test additional details");
		});

		SormasToSormasOptionsDto options = new SormasToSormasOptionsDto();
		options.setOrganization(new ServerAccessDataReferenceDto(SECOND_SERVER_ACCESS_ID));
		options.setPseudonymizePersonalData(true);

		Mockito.when(MockProducer.getSormasToSormasClient().post(Matchers.anyString(), Matchers.anyString(), Matchers.any(), Matchers.any()))
			.thenAnswer(invocation -> {
				assertThat(invocation.getArgument(0, String.class), is(SECOND_SERVER_ACCESS_ID));
				assertThat(invocation.getArgument(1, String.class), is("/sormasToSormas/cases"));

				List<SormasToSormasCaseDto> postBody = invocation.getArgument(2, List.class);
				assertThat(postBody.size(), is(1));
				SormasToSormasCaseDto sharedCase = postBody.get(0);

				assertThat(sharedCase.getPerson().getFirstName(), is("Confidential"));
				assertThat(sharedCase.getPerson().getLastName(), is("Confidential"));
				assertThat(sharedCase.getEntity().getAdditionalDetails(), is("Test additional details"));

				return Response.noContent().build();
			});

		getSormasToSormasCaseFacade().shareEntities(Collections.singletonList(caze.getUuid()), options);
	}

	@Test
	public void testShareCaseWithPseudonymizeSensitiveData() throws SormasToSormasException {
		TestDataCreator.RDCF rdcf = creator.createRDCF();

		useSurveillanceOfficerLogin(rdcf);

		PersonDto person = creator.createPerson();
		UserReferenceDto officer = creator.createUser(rdcf, UserRole.SURVEILLANCE_OFFICER).toReference();
		CaseDataDto caze = creator.createCase(officer, rdcf, dto -> {
			dto.setPerson(person.toReference());
			dto.setSurveillanceOfficer(officer);
			dto.setClassificationUser(officer);

			dto.setAdditionalDetails("Test additional details");
		});

		SormasToSormasOptionsDto options = new SormasToSormasOptionsDto();
		options.setOrganization(new ServerAccessDataReferenceDto(SECOND_SERVER_ACCESS_ID));
		options.setPseudonymizeSensitiveData(true);

		Mockito.when(MockProducer.getSormasToSormasClient().post(Matchers.anyString(), Matchers.anyString(), Matchers.any(), Matchers.any()))
			.thenAnswer(invocation -> {
				assertThat(invocation.getArgument(0, String.class), is(SECOND_SERVER_ACCESS_ID));
				assertThat(invocation.getArgument(1, String.class), is("/sormasToSormas/cases"));

				List<SormasToSormasCaseDto> postBody = invocation.getArgument(2, List.class);
				assertThat(postBody.size(), is(1));
				SormasToSormasCaseDto sharedCase = postBody.get(0);

				assertThat(sharedCase.getPerson().getFirstName(), is("Confidential"));
				assertThat(sharedCase.getPerson().getLastName(), is("Confidential"));
				assertThat(sharedCase.getEntity().getAdditionalDetails(), isEmptyString());

				return Response.noContent().build();
			});

		getSormasToSormasCaseFacade().shareEntities(Collections.singletonList(caze.getUuid()), options);
	}

	@Test
	public void testReturnCase() throws SormasToSormasException {
		TestDataCreator.RDCF rdcf = creator.createRDCF();

		useSurveillanceOfficerLogin(rdcf);

		PersonDto person = creator.createPerson();
		UserReferenceDto officer = creator.createUser(rdcf, UserRole.SURVEILLANCE_OFFICER).toReference();
		CaseDataDto caze = creator.createCase(officer, rdcf, dto -> {
			dto.setPerson(person.toReference());
			dto.setSurveillanceOfficer(officer);
			dto.setClassificationUser(officer);

			SormasToSormasOriginInfoDto originInfo = new SormasToSormasOriginInfoDto();
			originInfo.setSenderName("Test Name");
			originInfo.setSenderEmail("test@email.com");
			originInfo.setOrganizationId(DEFAULT_SERVER_ACCESS_ID);
			originInfo.setOwnershipHandedOver(true);
			dto.setSormasToSormasOriginInfo(originInfo);
		});

		SormasToSormasShareRequestDto shareRequest = new SormasToSormasShareRequestDto();
		shareRequest.setUuid(DataHelper.createUuid());
		shareRequest.setOriginInfo(caze.getSormasToSormasOriginInfo());
		getSormasToSormasShareRequestFacade().saveShareRequest(shareRequest);

		ContactDto sharedContact = creator
			.createContact(officer, officer, creator.createPerson().toReference(), caze, new Date(), new Date(), Disease.CORONAVIRUS, rdcf, c -> {
				c.setSormasToSormasOriginInfo(caze.getSormasToSormasOriginInfo());
			});
		ContactDto newContact = creator.createContact(officer, creator.createPerson().toReference(), caze);
		SampleDto sharedSample = creator.createSample(caze.toReference(), officer, rdcf.facility, s -> {
			s.setSormasToSormasOriginInfo(caze.getSormasToSormasOriginInfo());
		});

		SampleDto newSample = creator.createSample(caze.toReference(), officer, rdcf.facility);

		SormasToSormasOptionsDto options = new SormasToSormasOptionsDto();
		options.setOrganization(new ServerAccessDataReferenceDto(SECOND_SERVER_ACCESS_ID));
		options.setHandOverOwnership(true);
		options.setWithAssociatedContacts(true);
		options.setWithSamples(true);
		options.setComment("Test comment");

		Mockito.when(MockProducer.getSormasToSormasClient().put(Matchers.anyString(), Matchers.anyString(), Matchers.any(), Matchers.any()))
			.thenAnswer(invocation -> Response.noContent().build());

		getSormasToSormasCaseFacade().returnEntity(caze.getUuid(), options);

		// case ownership should be lost
		CaseDataDto sharedCase = getCaseFacade().getCaseDataByUuid(caze.getUuid());
		assertThat(sharedCase.getSormasToSormasOriginInfo().isOwnershipHandedOver(), is(false));

		// contact ownership should be lost
		sharedContact = getContactFacade().getContactByUuid(sharedContact.getUuid());
		assertThat(sharedContact.getSormasToSormasOriginInfo().isOwnershipHandedOver(), is(false));

		// new contacts should have share info with ownership handed over
		List<SormasToSormasShareInfoDto> newContactShareInfos =
			getSormasToSormasShareInfoFacade().getIndexList(new SormasToSormasShareInfoCriteria().contact(newContact.toReference()), 0, 100);
		assertThat(newContactShareInfos, hasSize(1));
		assertThat(newContactShareInfos.get(0).isOwnershipHandedOver(), is(true));

		// sample ownership should be lost
		sharedSample = getSampleFacade().getSampleByUuid(sharedSample.getUuid());
		assertThat(sharedSample.getSormasToSormasOriginInfo().isOwnershipHandedOver(), is(false));

		// new samples should have share info with ownership handed over
		List<SormasToSormasShareInfoDto> newSampleShareInfos =
			getSormasToSormasShareInfoFacade().getIndexList(new SormasToSormasShareInfoCriteria().sample(newSample.toReference()), 0, 100);
		assertThat(newSampleShareInfos, hasSize(1));
		assertThat(newSampleShareInfos.get(0).isOwnershipHandedOver(), is(true));
	}

	@Test
	public void testSaveReturnedCase() throws JsonProcessingException, SormasToSormasException {
		MappableRdcf rdcf = createRDCF(false);

		UserReferenceDto officer = creator.createUser(rdcf.localRdcf, UserRole.SURVEILLANCE_OFFICER).toReference();

		PersonDto person = creator.createPerson();
		CaseDataDto caze = creator.createCase(officer, person.toReference(), rdcf.localRdcf);

		PersonDto sharedContactPerson = creator.createPerson();
		ContactDto sharedContact = creator.createContact(officer, sharedContactPerson.toReference(), caze);
		PersonDto newContactPerson = creator.createPerson();
		ContactDto newContact = creator.createContact(officer, newContactPerson.toReference(), caze);
		ContactDto newContact2 = creator.createContact(officer, newContactPerson.toReference(), caze);
		SampleDto sharedSample = creator.createSample(caze.toReference(), officer, rdcf.localRdcf.facility);
		SampleDto newSample = creator.createSample(caze.toReference(), officer, rdcf.localRdcf.facility);
		SampleDto newSample2 = creator.createSample(caze.toReference(), officer, rdcf.localRdcf.facility);

		User officerUser = getUserService().getByReferenceDto(officer);
		getSormasToSormasShareInfoService().persist(
			createShareInfo(
				officerUser,
				DEFAULT_SERVER_ACCESS_ID,
				true,
				i -> i.getCases().add(new ShareInfoCase(i, getCaseService().getByReferenceDto(caze.toReference())))));
		getSormasToSormasShareInfoService().persist(
			createShareInfo(
				officerUser,
				DEFAULT_SERVER_ACCESS_ID,
				true,
				i -> i.getContacts().add(new ShareInfoContact(i, getContactService().getByReferenceDto(sharedContact.toReference())))));
		getSormasToSormasShareInfoService().persist(
			createShareInfo(
				officerUser,
				DEFAULT_SERVER_ACCESS_ID,
				true,
				i -> i.getSamples().add(new ShareInfoSample(i, getSampleService().getByReferenceDto(sharedSample.toReference())))));

		caze.setQuarantine(QuarantineType.HOTEL);

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(caze.getChangeDate());
		calendar.add(Calendar.DAY_OF_MONTH, 1);
		caze.setChangeDate(calendar.getTime());

		SormasToSormasOriginInfoDto originInfo = createSormasToSormasOriginInfo(DEFAULT_SERVER_ACCESS_ID, true);

		SormasToSormasCaseDto shareData = new SormasToSormasCaseDto(person, caze, originInfo);
		shareData.setAssociatedContacts(
			Arrays.asList(
				new SormasToSormasCaseDto.AssociatedContactDto(sharedContactPerson, sharedContact),
				new SormasToSormasCaseDto.AssociatedContactDto(newContactPerson, newContact),
				new SormasToSormasCaseDto.AssociatedContactDto(newContactPerson, newContact2)));
		shareData.setSamples(
			Arrays.asList(
				new SormasToSormasSampleDto(sharedSample, Collections.emptyList(), Collections.emptyList()),
				new SormasToSormasSampleDto(newSample, Collections.emptyList(), Collections.emptyList()),
				new SormasToSormasSampleDto(newSample2, Collections.emptyList(), Collections.emptyList())));

		SormasToSormasEncryptedDataDto encryptedData = encryptShareDataAsArray(shareData);

		try {
			getSormasToSormasCaseFacade().saveReturnedEntity(encryptedData);
		} catch (Exception e) {
			e.printStackTrace();
		}

		CaseDataDto returnedCase = getCaseFacade().getCaseDataByUuid(caze.getUuid());
		assertThat(returnedCase.getQuarantine(), is(QuarantineType.HOTEL));
		assertThat(returnedCase.getReportingUser(), is(officer));

		List<SormasToSormasShareInfoDto> caseShares =
			getSormasToSormasShareInfoFacade().getIndexList(new SormasToSormasShareInfoCriteria().caze(caze.toReference()), 0, 100);
		assertThat(caseShares.get(0).isOwnershipHandedOver(), is(false));

		List<SormasToSormasShareInfoDto> contactShares =
			getSormasToSormasShareInfoFacade().getIndexList(new SormasToSormasShareInfoCriteria().contact(sharedContact.toReference()), 0, 100);
		assertThat(contactShares.get(0).isOwnershipHandedOver(), is(false));

		ContactDto returnedNewContact = getContactFacade().getContactByUuid(newContact.getUuid());
		assertThat(returnedNewContact.getSormasToSormasOriginInfo().isOwnershipHandedOver(), is(true));

		ContactDto returnedNewContact2 = getContactFacade().getContactByUuid(newContact.getUuid());
		assertThat(returnedNewContact2.getSormasToSormasOriginInfo().isOwnershipHandedOver(), is(true));

		List<SormasToSormasShareInfoDto> sampleShares =
			getSormasToSormasShareInfoFacade().getIndexList(new SormasToSormasShareInfoCriteria().sample(sharedSample.toReference()), 0, 100);
		assertThat(sampleShares.get(0).isOwnershipHandedOver(), is(false));

		SampleDto returnedNewSample = getSampleFacade().getSampleByUuid(newSample.getUuid());
		assertThat(returnedNewSample.getSormasToSormasOriginInfo().isOwnershipHandedOver(), is(true));

		SampleDto returnedNewSample2 = getSampleFacade().getSampleByUuid(newSample.getUuid());
		assertThat(returnedNewSample2.getSormasToSormasOriginInfo().isOwnershipHandedOver(), is(true));
	}

	@Test
	public void testSaveSharedCaseWithUnknownFacility() throws JsonProcessingException, SormasToSormasException, SormasToSormasValidationException {
		MappableRdcf rdcf = createRDCF(false);

		PersonDto person = createPersonDto(rdcf);
		person.setFirstName("James");
		person.setLastName("Smith");

		CaseDataDto caze = createRemoteCaseDto(rdcf.remoteRdcf, person);
		caze.setHealthFacility(new FacilityReferenceDto("unknown", "Unknown facility", "unknown"));

		SormasToSormasEncryptedDataDto encryptedData =
			encryptShareDataAsArray(new SormasToSormasCaseDto(person, caze, createSormasToSormasOriginInfo(DEFAULT_SERVER_ACCESS_ID, false)));

		getSormasToSormasCaseFacade().saveSharedEntities(encryptedData, null);

		CaseDataDto savedCase = getCaseFacade().getCaseDataByUuid(caze.getUuid());

		assertThat(savedCase, is(notNullValue()));
		assertThat(savedCase.getHealthFacility().getUuid(), is(FacilityDto.OTHER_FACILITY_UUID));
		assertThat(savedCase.getHealthFacilityDetails(), is("Unknown facility"));
	}

	@Test
	public void testSaveSharedCaseWithUnknownPoint() throws JsonProcessingException, SormasToSormasException, SormasToSormasValidationException {
		MappableRdcf rdcf = createRDCF(false);

		PersonDto person = createPersonDto(rdcf);

		CaseDataDto caze = CaseDataDto.build(person.toReference(), Disease.CORONAVIRUS);
		caze.setCaseOrigin(CaseOrigin.POINT_OF_ENTRY);
		caze.setResponsibleRegion(rdcf.remoteRdcf.region);
		caze.setResponsibleDistrict(rdcf.remoteRdcf.district);
		caze.setResponsibleCommunity(rdcf.remoteRdcf.community);
		caze.setPointOfEntry(new PointOfEntryReferenceDto("unknown", "Unknown POE", PointOfEntryType.AIRPORT, null));
		PortHealthInfoDto portHealthInfo = PortHealthInfoDto.build();
		portHealthInfo.setAirlineName("Test Airline");
		caze.setPortHealthInfo(portHealthInfo);

		SormasToSormasEncryptedDataDto encryptedData =
			encryptShareDataAsArray(new SormasToSormasCaseDto(person, caze, createSormasToSormasOriginInfo(DEFAULT_SERVER_ACCESS_ID, false)));
		getSormasToSormasCaseFacade().saveSharedEntities(encryptedData, null);

		CaseDataDto savedCase = getCaseFacade().getCaseDataByUuid(caze.getUuid());

		assertThat(savedCase.getPointOfEntry().getUuid(), is(PointOfEntryDto.OTHER_AIRPORT_UUID));
		assertThat(savedCase.getPointOfEntryDetails(), is("Unknown POE"));
		assertThat(savedCase.getPortHealthInfo().getAirlineName(), is("Test Airline"));
	}

	@Test
	public void testSaveReturnedCaseWithKnownOtherFacility() throws JsonProcessingException, SormasToSormasException {
		MappableRdcf rdcf = createRDCF(false);

		UserReferenceDto officer = creator.createUser(rdcf.localRdcf, UserRole.SURVEILLANCE_OFFICER).toReference();

		PersonDto person = creator.createPerson();
		CaseDataDto caze = creator.createCase(officer, rdcf.localRdcf, c -> {
			c.setPerson(person.toReference());
			c.setHealthFacility(getFacilityFacade().getByUuid(FacilityDto.OTHER_FACILITY_UUID).toReference());
			c.setHealthFacilityDetails("Test HF details");
		});

		User officerUser = getUserService().getByReferenceDto(officer);
		getSormasToSormasShareInfoService().persist(
			createShareInfo(
				officerUser,
				DEFAULT_SERVER_ACCESS_ID,
				true,
				i -> i.getCases().add(new ShareInfoCase(i, getCaseService().getByReferenceDto(caze.toReference())))));

		caze.setHealthFacilityDetails(rdcf.localRdcf.facility.getCaption());

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(caze.getChangeDate());
		calendar.add(Calendar.DAY_OF_MONTH, 1);
		caze.setChangeDate(calendar.getTime());

		SormasToSormasOriginInfoDto originInfo = createSormasToSormasOriginInfo(DEFAULT_SERVER_ACCESS_ID, true);

		SormasToSormasCaseDto shareData = new SormasToSormasCaseDto(person, caze, originInfo);

		SormasToSormasEncryptedDataDto encryptedData = encryptShareDataAsArray(shareData);

		try {
			getSormasToSormasCaseFacade().saveReturnedEntity(encryptedData);
		} catch (Exception e) {
			e.printStackTrace();
		}

		CaseDataDto returnedCase = getCaseFacade().getCaseDataByUuid(caze.getUuid());
		assertThat(returnedCase.getHealthFacility(), is(rdcf.localRdcf.facility));
		assertThat(returnedCase.getHealthFacilityDetails(), is(nullValue()));
	}

	@Test
	public void testSaveReturnedCaseWithKnownOtherPointOfEntry() throws JsonProcessingException, SormasToSormasException {
		MappableRdcf rdcf = createRDCF(false);

		UserReferenceDto officer = creator.createUser(rdcf.localRdcf, UserRole.SURVEILLANCE_OFFICER).toReference();

		PersonDto person = creator.createPerson();
		CaseDataDto caze = creator.createCase(officer, rdcf.localRdcf, c -> {
			c.setPerson(person.toReference());
			c.setCaseOrigin(CaseOrigin.POINT_OF_ENTRY);
			c.setPointOfEntry(new PointOfEntryReferenceDto(PointOfEntryDto.OTHER_SEAPORT_UUID, null, null, null));
			c.setPointOfEntryDetails("Test Seaport");
		});

		User officerUser = getUserService().getByReferenceDto(officer);
		getSormasToSormasShareInfoService().persist(
			createShareInfo(
				officerUser,
				DEFAULT_SERVER_ACCESS_ID,
				true,
				i -> i.getCases().add(new ShareInfoCase(i, getCaseService().getByReferenceDto(caze.toReference())))));

		caze.setPointOfEntryDetails(rdcf.localRdcf.pointOfEntry.getCaption());

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(caze.getChangeDate());
		calendar.add(Calendar.DAY_OF_MONTH, 1);
		caze.setChangeDate(calendar.getTime());

		SormasToSormasOriginInfoDto originInfo = createSormasToSormasOriginInfo(DEFAULT_SERVER_ACCESS_ID, true);

		SormasToSormasCaseDto shareData = new SormasToSormasCaseDto(person, caze, originInfo);

		SormasToSormasEncryptedDataDto encryptedData = encryptShareDataAsArray(shareData);

		try {
			getSormasToSormasCaseFacade().saveReturnedEntity(encryptedData);
		} catch (Exception e) {
			e.printStackTrace();
		}

		CaseDataDto returnedCase = getCaseFacade().getCaseDataByUuid(caze.getUuid());
		assertThat(returnedCase.getPointOfEntry(), is(rdcf.localRdcf.pointOfEntry));
		assertThat(returnedCase.getPointOfEntryDetails(), is(nullValue()));
	}

	@Test
	public void testSendCaseShareRequest() throws SormasToSormasException, JsonProcessingException, NoSuchAlgorithmException, KeyManagementException {
		TestDataCreator.RDCF rdcf = creator.createRDCF();

		useSurveillanceOfficerLogin(rdcf);

		PersonDto person = creator.createPerson("John", "Doe", Sex.MALE, 1964, 4, 12);
		UserReferenceDto officer = creator.createUser(rdcf, UserRole.SURVEILLANCE_OFFICER).toReference();
		CaseDataDto caze = creator.createCase(officer, rdcf, dto -> {
			dto.setPerson(person.toReference());
			dto.setDisease(Disease.CORONAVIRUS);
			dto.setCaseClassification(CaseClassification.SUSPECT);
			dto.setOutcome(CaseOutcome.NO_OUTCOME);
		});

		SormasToSormasOptionsDto options = new SormasToSormasOptionsDto();
		options.setOrganization(new ServerAccessDataReferenceDto(SECOND_SERVER_ACCESS_ID));
		options.setComment("Test comment");

		Mockito.when(MockProducer.getSormasToSormasClient().post(Matchers.anyString(), Matchers.anyString(), Matchers.any(), Matchers.any()))
			.thenAnswer(invocation -> {
				assertThat(invocation.getArgument(0, String.class), is(SECOND_SERVER_ACCESS_ID));
				assertThat(invocation.getArgument(1, String.class), is("/sormasToSormas/cases/request"));

				ShareRequestData<CaseShareRequestData> postBody = invocation.getArgument(2, ShareRequestData.class);
				assertThat(postBody.getPreviews().size(), is(1));

				SormasToSormasCasePreview casePreview = (SormasToSormasCasePreview) ((ShareRequestData) postBody).getPreviews().get(0);

				assertThat(casePreview.getPerson().getFirstName(), is(person.getFirstName()));
				assertThat(casePreview.getPerson().getLastName(), is(person.getLastName()));
				assertThat(casePreview.getPerson().getSex(), is(person.getSex()));
				assertThat(casePreview.getPerson().getBirthdateDD(), is(person.getBirthdateDD()));
				assertThat(casePreview.getPerson().getBirthdateMM(), is(person.getBirthdateMM()));
				assertThat(casePreview.getPerson().getBirthdateYYYY(), is(person.getBirthdateYYYY()));

				assertThat(casePreview.getUuid(), is(caze.getUuid()));
				assertThat(casePreview.getDisease(), is(caze.getDisease()));
				assertThat(casePreview.getCaseClassification(), is(caze.getCaseClassification()));
				assertThat(casePreview.getOutcome(), is(caze.getOutcome()));

				// share information
				assertThat(postBody.getOriginInfo().getOrganizationId(), is(DEFAULT_SERVER_ACCESS_ID));
				assertThat(postBody.getOriginInfo().getSenderName(), is("Surv Off"));
				assertThat(postBody.getOriginInfo().getComment(), is("Test comment"));
				assertThat(postBody.getOriginInfo().getComment(), is("Test comment"));

				return Response.noContent().build();
			});

		getSormasToSormasCaseFacade().sendShareRequest(Collections.singletonList(caze.getUuid()), options);

		List<SormasToSormasShareInfoDto> shareInfoList =
			getSormasToSormasShareInfoFacade().getIndexList(new SormasToSormasShareInfoCriteria().caze(caze.toReference()), 0, 100);

		assertThat(shareInfoList.size(), is(1));
		assertThat(shareInfoList.get(0).getTarget().getUuid(), is(SECOND_SERVER_ACCESS_ID));
		assertThat(shareInfoList.get(0).getSender().getCaption(), is("Surv OFF - Surveillance Officer"));
		assertThat(shareInfoList.get(0).getComment(), is("Test comment"));
		assertThat(shareInfoList.get(0).getRequestStatus(), is(ShareRequestStatus.PENDING));
	}

	@Test
	public void testGetAllShares() throws SormasToSormasException {
		TestDataCreator.RDCF rdcf = creator.createRDCF();

		useSurveillanceOfficerLogin(rdcf);

		UserReferenceDto officer = creator.createUser(rdcf, UserRole.SURVEILLANCE_OFFICER).toReference();
		CaseDataDto caze = creator.createCase(officer, creator.createPerson().toReference(), rdcf);

		User officerUser = getUserService().getByReferenceDto(officer);
		getSormasToSormasShareInfoService().persist(
			createShareInfo(
				officerUser,
				SECOND_SERVER_ACCESS_ID,
				true,
				i -> i.getCases().add(new ShareInfoCase(i, getCaseService().getByReferenceDto(caze.toReference())))));

		Mockito
			.when(
				MockProducer.getSormasToSormasClient()
					.post(eq(SECOND_SERVER_ACCESS_ID), eq("/sormasToSormas/cases/shares"), Matchers.any(), Matchers.any()))
			.thenAnswer(invocation -> {
				SormasToSormasShareInfoDto shareInfo = new SormasToSormasShareInfoDto();
				shareInfo.setTarget(new ServerAccessDataReferenceDto("dummy SORMAS"));
				shareInfo.setOwnershipHandedOver(true);
				shareInfo.setComment("re-shared");

				return encryptShareDataAsArray(new SormasToSormasShareTree(null, shareInfo, Collections.emptyList()));
			});

		List<SormasToSormasShareTree> shares = getSormasToSormasCaseFacade().getAllShares(caze.getUuid());

		assertThat(shares, hasSize(1));
		SormasToSormasShareInfoDto fistShare = shares.get(0).getShare();
		assertThat(fistShare.getTarget().getUuid(), is(SECOND_SERVER_ACCESS_ID));
		assertThat(fistShare.isOwnershipHandedOver(), is(true));

		List<SormasToSormasShareTree> reShares = shares.get(0).getReShares();
		assertThat(reShares, hasSize(1));
		SormasToSormasShareInfoDto reShare = reShares.get(0).getShare();
		assertThat(reShare.getTarget().getUuid(), is("dummy SORMAS"));
		assertThat(reShare.isOwnershipHandedOver(), is(true));
		assertThat(reShare.getComment(), is("re-shared"));
		assertThat(reShares.get(0).getReShares(), hasSize(0));
	}

	@Test
	public void testGetAllSharesOnMiddleLevel() throws SormasToSormasException {
		TestDataCreator.RDCF rdcf = creator.createRDCF();

		useSurveillanceOfficerLogin(rdcf);

		UserReferenceDto officer = creator.createUser(rdcf, UserRole.SURVEILLANCE_OFFICER).toReference();
		CaseDataDto caze = creator.createCase(officer, creator.createPerson().toReference(), rdcf, c -> {
			SormasToSormasOriginInfoDto originInfo = new SormasToSormasOriginInfoDto();
			originInfo.setSenderName("Test Name");
			originInfo.setOrganizationId(DEFAULT_SERVER_ACCESS_ID);
			originInfo.setOwnershipHandedOver(true);
			originInfo.setComment("first share");

			c.setSormasToSormasOriginInfo(originInfo);
		});

		// initial share by the creator of case
		SormasToSormasShareInfoDto shareToSecond = createShareInfoDto(officer, SECOND_SERVER_ACCESS_ID, true);
		// another share by the creator
		SormasToSormasShareInfoDto anotherShareFromDefault = createShareInfoDto(officer, "anotherShareFromDefault", false);

		// forwarded by the second system
		User officerUser = getUserService().getByReferenceDto(officer);
		SormasToSormasShareInfo shareFromSecond = createShareInfo(
			officerUser,
			"shareFromSecond",
			false,
			i -> i.getCases().add(new ShareInfoCase(i, getCaseService().getByReferenceDto(caze.toReference()))));
		getSormasToSormasShareInfoService().persist(shareFromSecond);

		// The share tree for the above code is:
		// |- default
		//   |- second
		//     |- shareFromSecond
		//   |- another from default

		mockSecondServerAccess();

		// mock share tree on the case creator system
		Mockito
			.when(
				MockProducer.getSormasToSormasClient()
					.post(eq(DEFAULT_SERVER_ACCESS_ID), eq("/sormasToSormas/cases/shares"), Matchers.any(), Matchers.any()))
			.thenAnswer(invocation -> {

				List<SormasToSormasShareTree> shareTrees = new ArrayList<>();
				shareTrees.add(new SormasToSormasShareTree(null, shareToSecond, Collections.emptyList()));
				shareTrees.add(new SormasToSormasShareTree(null, anotherShareFromDefault, Collections.emptyList()));

				return encryptShareData(shareTrees);
			});

		// Mock shares from "anotherShareFromDefault" -> no more shares
		Mockito
			.when(
				MockProducer.getSormasToSormasClient()
					.post(eq("anotherShareFromDefault"), eq("/sormasToSormas/cases/shares"), Matchers.any(), Matchers.any()))
			.thenAnswer(invocation -> encryptShareData(Collections.emptyList()));

		// Mock shares from "shareFromSecond" server -> no more shares
		Mockito.when(
			MockProducer.getSormasToSormasClient().post(eq("shareFromSecond"), eq("/sormasToSormas/cases/shares"), Matchers.any(), Matchers.any()))
			.thenAnswer(invocation -> encryptShareData(Collections.emptyList()));

		List<SormasToSormasShareTree> shares = getSormasToSormasCaseFacade().getAllShares(caze.getUuid());

		assertThat(shares, hasSize(2));
		SormasToSormasShareInfoDto fistShare = shares.get(0).getShare();

		assertThat(fistShare.getTarget().getUuid(), is(SECOND_SERVER_ACCESS_ID));
		assertThat(fistShare.isOwnershipHandedOver(), is(true));

		List<SormasToSormasShareTree> reShares = shares.get(0).getReShares();
		assertThat(reShares, hasSize(1));

		SormasToSormasShareInfoDto reShare = reShares.get(0).getShare();
		assertThat(reShare.getTarget().getUuid(), is("shareFromSecond"));
		assertThat(reShare.isOwnershipHandedOver(), is(false));

		assertThat(reShares.get(0).getReShares(), hasSize(0));

		SormasToSormasShareTree secondReShareFromDefault = shares.get(1);
		assertThat(secondReShareFromDefault.getShare().getTarget().getUuid(), is("anotherShareFromDefault"));
		assertThat(secondReShareFromDefault.getShare().isOwnershipHandedOver(), is(false));

		assertThat(secondReShareFromDefault.getReShares(), hasSize(0));
	}

	@Test
	public void testGetReShares() throws SormasToSormasException, IOException {
		TestDataCreator.RDCF rdcf = creator.createRDCF();

		useSurveillanceOfficerLogin(rdcf);

		UserReferenceDto officer = creator.createUser(rdcf, UserRole.SURVEILLANCE_OFFICER).toReference();
		CaseDataDto caze = creator.createCase(officer, creator.createPerson().toReference(), rdcf);

		User officerUser = getUserService().getByReferenceDto(officer);
		getSormasToSormasShareInfoService().persist(
			createShareInfo(
				officerUser,
				SECOND_SERVER_ACCESS_ID,
				true,
				i -> i.getCases().add(new ShareInfoCase(i, getCaseService().getByReferenceDto(caze.toReference())))));

		Mockito
			.when(
				MockProducer.getSormasToSormasClient()
					.post(eq(SECOND_SERVER_ACCESS_ID), eq("/sormasToSormas/cases/shares"), Matchers.any(), Matchers.any()))
			.thenAnswer(invocation -> {
				SormasToSormasShareInfoDto shareInfo = new SormasToSormasShareInfoDto();
				shareInfo.setTarget(new ServerAccessDataReferenceDto("dummy SORMAS"));
				shareInfo.setOwnershipHandedOver(false);
				shareInfo.setComment("re-shared");

				return encryptShareDataAsArray(new SormasToSormasShareTree(null, shareInfo, Collections.emptyList()));
			});

		Mockito
			.when(MockProducer.getSormasToSormasClient().post(eq("dummy SORMAS"), eq("/sormasToSormas/cases/shares"), Matchers.any(), Matchers.any()))
			.thenAnswer(invocation -> encryptShareData(Collections.emptyList()));

		SormasToSormasEncryptedDataDto encryptedCriteria = encryptShareData(new ShareTreeCriteria(caze.getUuid(), null, false));

		SormasToSormasEncryptedDataDto encryptedShares = getSormasToSormasCaseFacade().getShareTrees(encryptedCriteria);

		mockDefaultServerAccess();

		SormasToSormasShareTree[] shares = getSormasToSormasEncryptionService().decryptAndVerify(
			new SormasToSormasEncryptedDataDto(SECOND_SERVER_ACCESS_ID, encryptedShares.getData()),
			SormasToSormasShareTree[].class);

		assertThat(shares, arrayWithSize(1));
		assertThat(shares[0].getShare().getTarget().getUuid(), is(SECOND_SERVER_ACCESS_ID));
		assertThat(shares[0].getShare().isOwnershipHandedOver(), is(true));
		assertThat(shares[0].getReShares().get(0).getShare().getTarget().getUuid(), is("dummy SORMAS"));
		assertThat(shares[0].getReShares().get(0).getShare().isOwnershipHandedOver(), is(false));
		assertThat(shares[0].getReShares().get(0).getReShares(), hasSize(0));
	}

	private CaseDataDto createRemoteCaseDto(TestDataCreator.RDCF remoteRdcf, PersonDto person) {
		CaseDataDto caze = CaseDataDto.build(person.toReference(), Disease.CORONAVIRUS);
		caze.setResponsibleRegion(remoteRdcf.region);
		caze.setResponsibleDistrict(remoteRdcf.district);
		caze.setResponsibleCommunity(remoteRdcf.community);
		caze.setHealthFacility(remoteRdcf.facility);
		caze.setFacilityType(FacilityType.HOSPITAL);
		return caze;
	}

	private ContactDto createRemoteContactDto(TestDataCreator.RDCF remoteRdcf, CaseDataDto caze) {
		ContactDto contact = ContactDto.build(caze);
		contact.setRegion(remoteRdcf.region);
		contact.setDistrict(remoteRdcf.district);
		contact.setCommunity(remoteRdcf.community);
		return contact;
	}
}
