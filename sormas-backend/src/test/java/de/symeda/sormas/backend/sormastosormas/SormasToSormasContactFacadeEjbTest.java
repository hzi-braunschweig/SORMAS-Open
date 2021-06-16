/*
 * SORMAS® - Surveillance Outbreak Response Management & Analysis System
 * Copyright © 2016-2020 Helmholtz-Zentrum für Infektionsforschung GmbH (HZI)
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
import static org.hamcrest.Matchers.nullValue;
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
import de.symeda.sormas.api.contact.ContactDto;
import de.symeda.sormas.api.contact.QuarantineType;
import de.symeda.sormas.api.epidata.AnimalCondition;
import de.symeda.sormas.api.exposure.ExposureDto;
import de.symeda.sormas.api.exposure.ExposureType;
import de.symeda.sormas.api.person.PersonDto;
import de.symeda.sormas.api.sample.PathogenTestResultType;
import de.symeda.sormas.api.sample.PathogenTestType;
import de.symeda.sormas.api.sample.SampleDto;
import de.symeda.sormas.api.sormastosormas.ServerAccessDataReferenceDto;
import de.symeda.sormas.api.sormastosormas.SormasToSormasEncryptedDataDto;
import de.symeda.sormas.api.sormastosormas.SormasToSormasException;
import de.symeda.sormas.api.sormastosormas.SormasToSormasOptionsDto;
import de.symeda.sormas.api.sormastosormas.SormasToSormasOriginInfoDto;
import de.symeda.sormas.api.sormastosormas.SormasToSormasSampleDto;
import de.symeda.sormas.api.sormastosormas.SormasToSormasShareInfoCriteria;
import de.symeda.sormas.api.sormastosormas.SormasToSormasShareInfoDto;
import de.symeda.sormas.api.sormastosormas.SormasToSormasValidationException;
import de.symeda.sormas.api.sormastosormas.contact.SormasToSormasContactDto;
import de.symeda.sormas.api.sormastosormas.sharerequest.SormasToSormasShareRequestDto;
import de.symeda.sormas.api.user.UserReferenceDto;
import de.symeda.sormas.api.user.UserRole;
import de.symeda.sormas.api.utils.DataHelper;
import de.symeda.sormas.backend.MockProducer;
import de.symeda.sormas.backend.TestDataCreator;
import de.symeda.sormas.backend.TestDataCreator.RDCF;
import de.symeda.sormas.backend.common.StartupShutdownService;
import de.symeda.sormas.backend.sormastosormas.share.shareinfo.ShareInfoContact;
import de.symeda.sormas.backend.sormastosormas.share.shareinfo.ShareInfoSample;
import de.symeda.sormas.backend.user.User;

@RunWith(MockitoJUnitRunner.class)
public class SormasToSormasContactFacadeEjbTest extends SormasToSormasFacadeTest {

	@Test
	public void testShareContact() throws SormasToSormasException, JsonProcessingException, NoSuchAlgorithmException, KeyManagementException {
		RDCF rdcf = creator.createRDCF();

		useSurveillanceOfficerLogin(rdcf);

		PersonDto person = creator.createPerson();
		UserReferenceDto officer = creator.createUser(rdcf, UserRole.SURVEILLANCE_OFFICER).toReference();
		ContactDto contact = creator.createContact(officer, officer, person.toReference(), null, new Date(), null, null, rdcf, dto -> {
			dto.setResultingCaseUser(officer);
		});

		SormasToSormasOptionsDto options = new SormasToSormasOptionsDto();
		options.setOrganization(new ServerAccessDataReferenceDto(SECOND_SERVER_ACCESS_CN));
		options.setComment("Test comment");

		Mockito.when(MockProducer.getSormasToSormasClient().post(Matchers.anyString(), Matchers.anyString(), Matchers.anyString(), Matchers.any()))
			.thenAnswer(invocation -> {
				assertThat(invocation.getArgument(0, String.class), is(SECOND_SERVER_REST_URL));
				assertThat(invocation.getArgument(1, String.class), is("/sormasToSormas/contacts"));

				String authToken = invocation.getArgument(2, String.class);
				assertThat(authToken, startsWith("Basic "));
				String credentials = new String(Base64.getDecoder().decode(authToken.replace("Basic ", "")), StandardCharsets.UTF_8);
				// uses password from server-list.csv from `serveraccessdefault` package
				assertThat(credentials, is(StartupShutdownService.SORMAS_TO_SORMAS_USER_NAME + ":" + SECOND_SERVER_REST_PASSWORD));

				SormasToSormasEncryptedDataDto encryptedData = invocation.getArgument(3, SormasToSormasEncryptedDataDto.class);
				SormasToSormasContactDto[] sharedContacts = decryptSharesData(encryptedData.getData(), SormasToSormasContactDto[].class);
				SormasToSormasContactDto sharedContact = sharedContacts[0];

				assertThat(sharedContact.getPerson().getFirstName(), is(person.getFirstName()));
				assertThat(sharedContact.getPerson().getLastName(), is(person.getLastName()));

				assertThat(sharedContact.getEntity().getUuid(), is(contact.getUuid()));
				// users should be cleaned up
				assertThat(sharedContact.getEntity().getReportingUser(), is(nullValue()));
				assertThat(sharedContact.getEntity().getContactOfficer(), is(nullValue()));
				assertThat(sharedContact.getEntity().getResultingCaseUser(), is(nullValue()));

				// share information
				assertThat(sharedContact.getOriginInfo().getOrganizationId(), is(DEFAULT_SERVER_ACCESS_CN));
				assertThat(sharedContact.getOriginInfo().getSenderName(), is("Surv Off"));
				assertThat(sharedContact.getOriginInfo().getComment(), is("Test comment"));

				return Response.noContent().build();
			});

		getSormasToSormasContactFacade().shareEntities(Collections.singletonList(contact.getUuid()), options);

		List<SormasToSormasShareInfoDto> shareInfoList =
			getSormasToSormasFacade().getShareInfoIndexList(new SormasToSormasShareInfoCriteria().contact(contact.toReference()), 0, 100);
		assertThat(shareInfoList.size(), is(1));
		assertThat(shareInfoList.get(0).getTarget().getUuid(), is(SECOND_SERVER_ACCESS_CN));
		assertThat(shareInfoList.get(0).getSender().getCaption(), is("Surv OFF - Surveillance Officer"));
		assertThat(shareInfoList.get(0).getComment(), is("Test comment"));
	}

	@Test
	public void testShareContactWithSamples()
		throws SormasToSormasException, JsonProcessingException, NoSuchAlgorithmException, KeyManagementException {
		RDCF rdcf = creator.createRDCF();

		useSurveillanceOfficerLogin(rdcf);

		PersonDto person = creator.createPerson();
		UserReferenceDto officer = creator.createUser(rdcf, UserRole.SURVEILLANCE_OFFICER).toReference();
		ContactDto contact = creator.createContact(officer, officer, person.toReference(), null, new Date(), null, null, rdcf);

		Date sampleDateTime = new Date();
		SampleDto sample = creator.createSample(contact.toReference(), officer, rdcf.facility, s -> {
			s.setSampleDateTime(sampleDateTime);
			s.setComment("Test sample");
		});
		creator.createPathogenTest(
			sample.toReference(),
			PathogenTestType.RAPID_TEST,
			Disease.CORONAVIRUS,
			sampleDateTime,
			rdcf.facility,
			officer,
			PathogenTestResultType.INDETERMINATE,
			"result",
			true,
			null);
		creator.createAdditionalTest(sample.toReference());

		SormasToSormasOptionsDto options = new SormasToSormasOptionsDto();
		options.setOrganization(new ServerAccessDataReferenceDto(SECOND_SERVER_ACCESS_CN));
		options.setComment("Test comment");
		options.setWithSamples(true);

		Mockito.when(MockProducer.getSormasToSormasClient().post(Matchers.anyString(), Matchers.anyString(), Matchers.anyString(), Matchers.any()))
			.thenAnswer(invocation -> {
				SormasToSormasEncryptedDataDto encryptedData = invocation.getArgument(3, SormasToSormasEncryptedDataDto.class);
				SormasToSormasContactDto[] sharedContacts = decryptSharesData(encryptedData.getData(), SormasToSormasContactDto[].class);

				assertThat(sharedContacts[0].getSamples().size(), is(1));

				SormasToSormasSampleDto sharedSample = sharedContacts[0].getSamples().get(0);
				assertThat(sharedSample.getSample().getSampleDateTime(), is(sampleDateTime));
				assertThat(sharedSample.getSample().getComment(), is("Test sample"));
				assertThat(sharedSample.getPathogenTests(), hasSize(1));
				assertThat(sharedSample.getAdditionalTests(), hasSize(1));

				return Response.noContent().build();
			});

		getSormasToSormasContactFacade().shareEntities(Collections.singletonList(contact.getUuid()), options);

		List<SormasToSormasShareInfoDto> shareInfoList =
			getSormasToSormasFacade().getShareInfoIndexList(new SormasToSormasShareInfoCriteria().sample(sample.toReference()), 0, 100);

		SormasToSormasShareInfoDto sampleShareInfoList = shareInfoList.get(0);
		assertThat(sampleShareInfoList.getTarget().getUuid(), is(SECOND_SERVER_ACCESS_CN));
		assertThat(sampleShareInfoList.getSender().getCaption(), is("Surv OFF - Surveillance Officer"));
		assertThat(sampleShareInfoList.getComment(), is("Test comment"));
	}

	@Test
	public void testSaveSharedContact() throws JsonProcessingException, SormasToSormasException, SormasToSormasValidationException {
		MappableRdcf rdcf = createRDCF(false);

		PersonDto person = createPersonDto(rdcf);
		person.setFirstName("James");
		person.setLastName("Smith");

		ContactDto contact = createRemoteContactDto(rdcf.remoteRdcf, person);

		SormasToSormasEncryptedDataDto encryptedData =
			encryptShareDataAsArray(new SormasToSormasContactDto(person, contact, createSormasToSormasOriginInfo(DEFAULT_SERVER_ACCESS_CN, false)));
		getSormasToSormasContactFacade().saveSharedEntities(encryptedData, null);

		ContactDto savedContact = getContactFacade().getContactByUuid(contact.getUuid());

		assertThat(savedContact, is(notNullValue()));
		assertThat(savedContact.getRegion(), is(rdcf.localRdcf.region));
		assertThat(savedContact.getDistrict(), is(rdcf.localRdcf.district));
		assertThat(savedContact.getCommunity(), is(rdcf.localRdcf.community));
		assertThat(savedContact.getEpiData().getExposures().get(0).getAnimalCondition(), is(AnimalCondition.PROCESSED));

		assertThat(savedContact.getSormasToSormasOriginInfo().getOrganizationId(), is(DEFAULT_SERVER_ACCESS_CN));
		assertThat(savedContact.getSormasToSormasOriginInfo().getSenderName(), is("John doe"));

		PersonDto savedPerson = getPersonFacade().getPersonByUuid(savedContact.getPerson().getUuid());
		assertThat(savedPerson, is(notNullValue()));
		assertThat(savedPerson.getAddress().getRegion(), is(rdcf.localRdcf.region));
		assertThat(savedPerson.getAddress().getDistrict(), is(rdcf.localRdcf.district));
		assertThat(savedPerson.getAddress().getCommunity(), is(rdcf.localRdcf.community));
		assertThat(savedPerson.getFirstName(), is("James"));
		assertThat(savedPerson.getLastName(), is("Smith"));

	}

	@Test
	public void testSaveSharedContactWithSamples() throws JsonProcessingException, SormasToSormasException, SormasToSormasValidationException {
		MappableRdcf rdcf = createRDCF(false);
		PersonDto person = createPersonDto(rdcf);

		ContactDto contact = createRemoteContactDto(rdcf.remoteRdcf, person);
		SormasToSormasSampleDto sample = createRemoteSampleDtoWithTests(rdcf.remoteRdcf, null, contact.toReference());

		SormasToSormasContactDto shareData =
			new SormasToSormasContactDto(person, contact, createSormasToSormasOriginInfo(DEFAULT_SERVER_ACCESS_CN, false));
		shareData.setSamples(Collections.singletonList(sample));

		SormasToSormasEncryptedDataDto encryptedData = encryptShareDataAsArray(shareData);
		getSormasToSormasContactFacade().saveSharedEntities(encryptedData, null);

		ContactDto savedContact = getContactFacade().getContactByUuid(contact.getUuid());
		SampleDto savedSample = getSampleFacade().getSampleByUuid(sample.getSample().getUuid());

		assertThat(savedSample, is(notNullValue()));
		assertThat(savedSample.getLab(), is(rdcf.localRdcf.facility));

		assertThat(getPathogenTestFacade().getAllBySample(savedSample.toReference()), hasSize(1));
		assertThat(getAdditionalTestFacade().getAllBySample(savedSample.getUuid()), hasSize(1));

		assertThat(savedContact.getSormasToSormasOriginInfo().getUuid(), is(savedSample.getSormasToSormasOriginInfo().getUuid()));
	}

	@Test
	public void testReturnContact() throws JsonProcessingException, SormasToSormasException {
		RDCF rdcf = creator.createRDCF();

		useSurveillanceOfficerLogin(rdcf);

		PersonDto person = creator.createPerson();
		UserReferenceDto officer = creator.createUser(rdcf, UserRole.SURVEILLANCE_OFFICER).toReference();

		ContactDto contact =
			creator.createContact(officer, officer, person.toReference(), null, new Date(), new Date(), Disease.CORONAVIRUS, rdcf, c -> {
				SormasToSormasOriginInfoDto originInfo = new SormasToSormasOriginInfoDto();
				originInfo.setSenderName("Test Name");
				originInfo.setSenderEmail("test@email.com");
				originInfo.setOrganizationId(DEFAULT_SERVER_ACCESS_CN);
				originInfo.setOwnershipHandedOver(true);

				c.setSormasToSormasOriginInfo(originInfo);
			});

		SormasToSormasShareRequestDto shareRequest = new SormasToSormasShareRequestDto();
		shareRequest.setUuid(DataHelper.createUuid());
		shareRequest.setOriginInfo(contact.getSormasToSormasOriginInfo());
		getSormasToSormasShareRequestFacade().saveShareRequest(shareRequest);

		SampleDto sharedSample = creator.createSample(contact.toReference(), officer, rdcf.facility, s -> {
			s.setSormasToSormasOriginInfo(contact.getSormasToSormasOriginInfo());
		});

		SampleDto newSample = creator.createSample(contact.toReference(), officer, rdcf.facility, null);

		SormasToSormasOptionsDto options = new SormasToSormasOptionsDto();
		options.setOrganization(new ServerAccessDataReferenceDto(SECOND_SERVER_ACCESS_CN));
		options.setHandOverOwnership(true);
		options.setWithSamples(true);
		options.setComment("Test comment");

		Mockito.when(MockProducer.getSormasToSormasClient().put(Matchers.anyString(), Matchers.anyString(), Matchers.anyString(), Matchers.any()))
			.thenAnswer(invocation -> Response.noContent().build());

		getSormasToSormasContactFacade().returnEntity(contact.getUuid(), options);

		// contact ownership should be lost
		ContactDto sharedContact = getContactFacade().getContactByUuid(contact.getUuid());
		assertThat(sharedContact.getSormasToSormasOriginInfo().isOwnershipHandedOver(), is(false));

		// sample ownership should be lost
		sharedSample = getSampleFacade().getSampleByUuid(sharedSample.getUuid());
		assertThat(sharedSample.getSormasToSormasOriginInfo().isOwnershipHandedOver(), is(false));

		// new samples should have share info with ownership handed over
		List<SormasToSormasShareInfoDto> newSampleShareInfos =
			getSormasToSormasFacade().getShareInfoIndexList(new SormasToSormasShareInfoCriteria().sample(newSample.toReference()), 0, 100);
		assertThat(newSampleShareInfos, hasSize(1));
		assertThat(newSampleShareInfos.get(0).isOwnershipHandedOver(), is(true));
	}

	@Test
	public void testSaveReturnedContact() throws JsonProcessingException, SormasToSormasException, SormasToSormasValidationException {
		MappableRdcf rdcf = createRDCF(false);

		UserReferenceDto officer = creator.createUser(rdcf.localRdcf, UserRole.SURVEILLANCE_OFFICER).toReference();

		PersonDto contactPerson = creator.createPerson();
		ContactDto contact = creator.createContact(officer, contactPerson.toReference());
		SampleDto sharedSample = creator.createSample(contact.toReference(), officer, rdcf.localRdcf.facility, null);
		SampleDto newSample = creator.createSample(contact.toReference(), officer, rdcf.localRdcf.facility, null);

		User officerUser = getUserService().getByReferenceDto(officer);
		getSormasToSormasShareInfoService().persist(
			createShareInfo(
				officerUser,
				DEFAULT_SERVER_ACCESS_CN,
				true,
				i -> i.getContacts().add(new ShareInfoContact(i, getContactService().getByReferenceDto(contact.toReference())))));
		getSormasToSormasShareInfoService().persist(
			createShareInfo(
				officerUser,
				DEFAULT_SERVER_ACCESS_CN,
				true,
				i -> i.getSamples().add(new ShareInfoSample(i, getSampleService().getByReferenceDto(sharedSample.toReference())))));

		contact.setQuarantine(QuarantineType.HOTEL);

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(contact.getChangeDate());
		calendar.add(Calendar.DAY_OF_MONTH, 1);
		contact.setChangeDate(calendar.getTime());

		SormasToSormasContactDto shareData =
			new SormasToSormasContactDto(contactPerson, contact, createSormasToSormasOriginInfo(DEFAULT_SERVER_ACCESS_CN, true));
		shareData.setSamples(
			Arrays.asList(
				new SormasToSormasSampleDto(sharedSample, Collections.emptyList(), Collections.emptyList()),
				new SormasToSormasSampleDto(newSample, Collections.emptyList(), Collections.emptyList())));

		SormasToSormasEncryptedDataDto encryptedData = encryptShareDataAsArray(shareData);

		getSormasToSormasContactFacade().saveReturnedEntity(encryptedData);

		ContactDto returnedContact = getContactFacade().getContactByUuid(contact.getUuid());
		assertThat(returnedContact.getQuarantine(), is(QuarantineType.HOTEL));
		assertThat(returnedContact.getReportingUser(), is(officer));

		List<SormasToSormasShareInfoDto> contactShares =
			getSormasToSormasFacade().getShareInfoIndexList(new SormasToSormasShareInfoCriteria().contact(contact.toReference()), 0, 100);
		assertThat(contactShares.get(0).isOwnershipHandedOver(), is(false));

		List<SormasToSormasShareInfoDto> sampleShares =
			getSormasToSormasFacade().getShareInfoIndexList(new SormasToSormasShareInfoCriteria().sample(sharedSample.toReference()), 0, 100);
		assertThat(sampleShares.get(0).isOwnershipHandedOver(), is(false));

		SampleDto returnedNewSample = getSampleFacade().getSampleByUuid(newSample.getUuid());
		assertThat(returnedNewSample.getSormasToSormasOriginInfo().isOwnershipHandedOver(), is(true));
	}

	protected ContactDto createRemoteContactDto(TestDataCreator.RDCF remoteRdcf, PersonDto person) {
		ContactDto contact = ContactDto.build(null, Disease.CORONAVIRUS, null, null);
		contact.setPerson(person.toReference());
		contact.setRegion(remoteRdcf.region);
		contact.setDistrict(remoteRdcf.district);
		contact.setCommunity(remoteRdcf.community);
		ExposureDto exposure = ExposureDto.build(ExposureType.ANIMAL_CONTACT);
		exposure.setAnimalCondition(AnimalCondition.PROCESSED);
		contact.getEpiData().getExposures().add(exposure);
		return contact;
	}
}
