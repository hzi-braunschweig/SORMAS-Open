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
package de.symeda.sormas.backend;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.symeda.sormas.api.Disease;
import de.symeda.sormas.api.ReferenceDto;
import de.symeda.sormas.api.VisitOrigin;
import de.symeda.sormas.api.action.ActionContext;
import de.symeda.sormas.api.action.ActionDto;
import de.symeda.sormas.api.campaign.CampaignDto;
import de.symeda.sormas.api.campaign.CampaignReferenceDto;
import de.symeda.sormas.api.campaign.data.CampaignFormDataDto;
import de.symeda.sormas.api.campaign.data.CampaignFormDataEntry;
import de.symeda.sormas.api.campaign.diagram.CampaignDiagramDefinitionDto;
import de.symeda.sormas.api.campaign.diagram.DiagramType;
import de.symeda.sormas.api.campaign.form.CampaignFormMetaDto;
import de.symeda.sormas.api.campaign.form.CampaignFormMetaReferenceDto;
import de.symeda.sormas.api.caze.CaseClassification;
import de.symeda.sormas.api.caze.CaseDataDto;
import de.symeda.sormas.api.caze.CaseReferenceDto;
import de.symeda.sormas.api.caze.InvestigationStatus;
import de.symeda.sormas.api.caze.surveillancereport.ReportingType;
import de.symeda.sormas.api.caze.surveillancereport.SurveillanceReportDto;
import de.symeda.sormas.api.clinicalcourse.ClinicalVisitDto;
import de.symeda.sormas.api.clinicalcourse.HealthConditionsDto;
import de.symeda.sormas.api.contact.ContactDto;
import de.symeda.sormas.api.contact.ContactReferenceDto;
import de.symeda.sormas.api.disease.DiseaseConfigurationDto;
import de.symeda.sormas.api.document.DocumentDto;
import de.symeda.sormas.api.document.DocumentRelatedEntityType;
import de.symeda.sormas.api.epidata.EpiDataDto;
import de.symeda.sormas.api.event.EventDto;
import de.symeda.sormas.api.event.EventInvestigationStatus;
import de.symeda.sormas.api.event.EventParticipantDto;
import de.symeda.sormas.api.event.EventParticipantReferenceDto;
import de.symeda.sormas.api.event.EventReferenceDto;
import de.symeda.sormas.api.event.EventStatus;
import de.symeda.sormas.api.event.TypeOfPlace;
import de.symeda.sormas.api.exposure.ExposureDto;
import de.symeda.sormas.api.exposure.ExposureType;
import de.symeda.sormas.api.exposure.TypeOfAnimal;
import de.symeda.sormas.api.facility.FacilityDto;
import de.symeda.sormas.api.facility.FacilityReferenceDto;
import de.symeda.sormas.api.facility.FacilityType;
import de.symeda.sormas.api.immunization.ImmunizationDto;
import de.symeda.sormas.api.immunization.ImmunizationManagementStatus;
import de.symeda.sormas.api.immunization.ImmunizationReferenceDto;
import de.symeda.sormas.api.immunization.ImmunizationStatus;
import de.symeda.sormas.api.immunization.MeansOfImmunization;
import de.symeda.sormas.api.importexport.ExportConfigurationDto;
import de.symeda.sormas.api.importexport.ExportType;
import de.symeda.sormas.api.infrastructure.PointOfEntryDto;
import de.symeda.sormas.api.infrastructure.PointOfEntryReferenceDto;
import de.symeda.sormas.api.infrastructure.PointOfEntryType;
import de.symeda.sormas.api.infrastructure.PopulationDataDto;
import de.symeda.sormas.api.labmessage.LabMessageDto;
import de.symeda.sormas.api.location.LocationDto;
import de.symeda.sormas.api.person.PersonDto;
import de.symeda.sormas.api.person.PersonReferenceDto;
import de.symeda.sormas.api.person.Sex;
import de.symeda.sormas.api.region.CommunityDto;
import de.symeda.sormas.api.region.CommunityReferenceDto;
import de.symeda.sormas.api.region.DistrictReferenceDto;
import de.symeda.sormas.api.region.RegionReferenceDto;
import de.symeda.sormas.api.sample.AdditionalTestDto;
import de.symeda.sormas.api.sample.PathogenTestDto;
import de.symeda.sormas.api.sample.PathogenTestResultType;
import de.symeda.sormas.api.sample.PathogenTestType;
import de.symeda.sormas.api.sample.SampleDto;
import de.symeda.sormas.api.sample.SampleMaterial;
import de.symeda.sormas.api.sample.SamplePurpose;
import de.symeda.sormas.api.sample.SampleReferenceDto;
import de.symeda.sormas.api.share.ExternalShareStatus;
import de.symeda.sormas.api.systemevents.SystemEventDto;
import de.symeda.sormas.api.systemevents.SystemEventStatus;
import de.symeda.sormas.api.systemevents.SystemEventType;
import de.symeda.sormas.api.task.TaskContext;
import de.symeda.sormas.api.task.TaskDto;
import de.symeda.sormas.api.task.TaskStatus;
import de.symeda.sormas.api.task.TaskType;
import de.symeda.sormas.api.therapy.PrescriptionDto;
import de.symeda.sormas.api.therapy.TreatmentDto;
import de.symeda.sormas.api.therapy.TreatmentType;
import de.symeda.sormas.api.travelentry.TravelEntryDto;
import de.symeda.sormas.api.user.UserDto;
import de.symeda.sormas.api.user.UserReferenceDto;
import de.symeda.sormas.api.user.UserRole;
import de.symeda.sormas.api.utils.DataHelper;
import de.symeda.sormas.api.vaccination.VaccinationEntityDto;
import de.symeda.sormas.api.visit.VisitDto;
import de.symeda.sormas.api.visit.VisitStatus;
import de.symeda.sormas.backend.disease.DiseaseConfigurationFacadeEjb.DiseaseConfigurationFacadeEjbLocal;
import de.symeda.sormas.backend.facility.Facility;
import de.symeda.sormas.backend.infrastructure.PointOfEntry;
import de.symeda.sormas.backend.region.Community;
import de.symeda.sormas.backend.region.Continent;
import de.symeda.sormas.backend.region.Country;
import de.symeda.sormas.backend.region.District;
import de.symeda.sormas.backend.region.Region;
import de.symeda.sormas.backend.share.ExternalShareInfo;

public class TestDataCreator {

	private final AbstractBeanTest beanTest;

	public TestDataCreator(AbstractBeanTest beanTest) {
		this.beanTest = beanTest;
	}

	public UserDto createUser(RDCFEntities rdcf, UserRole... roles) {
		return createUser(rdcf.region.getUuid(), rdcf.district.getUuid(), rdcf.facility.getUuid(), "First", "Name", roles);
	}

	public UserDto createUser(RDCF rdcf, UserRole... roles) {
		return createUser(rdcf.region.getUuid(), rdcf.district.getUuid(), rdcf.facility.getUuid(), "First", "Name", roles);
	}

	public UserDto createUser(RDCFEntities rdcf, String firstName, String lastName, UserRole... roles) {
		return createUser(rdcf.region.getUuid(), rdcf.district.getUuid(), rdcf.facility.getUuid(), firstName, lastName, roles);
	}

	public UserDto createUser(String regionUuid, String districtUuid, String facilityUuid, String firstName, String lastName, UserRole... roles) {
		return createUser(regionUuid, districtUuid, null, facilityUuid, firstName, lastName, roles);
	}

	public UserDto createUser(
		String regionUuid,
		String districtUuid,
		String communityUuid,
		String facilityUuid,
		String firstName,
		String lastName,
		UserRole... roles) {
		UserDto user1 = UserDto.build();
		user1.setFirstName(firstName);
		user1.setLastName(lastName);
		user1.setUserName(firstName + lastName);
		user1.setUserRoles(new HashSet<UserRole>(Arrays.asList(roles)));
		UserDto user = user1;
		user.setRegion(beanTest.getRegionFacade().getRegionReferenceByUuid(regionUuid));
		user.setDistrict(beanTest.getDistrictFacade().getDistrictReferenceByUuid(districtUuid));
		user.setCommunity(beanTest.getCommunityFacade().getCommunityReferenceByUuid(communityUuid));
		user.setHealthFacility(beanTest.getFacilityFacade().getFacilityReferenceByUuid(facilityUuid));
		user = beanTest.getUserFacade().saveUser(user);

		return user;
	}

	public UserReferenceDto createUserRef(
		String regionUuid,
		String districtUuid,
		String communityUuid,
		String facilityUuid,
		String firstName,
		String lastName,
		UserRole... roles) {
		return createUser(regionUuid, districtUuid, communityUuid, facilityUuid, firstName, lastName, roles).toReference();
	}

	public PersonDto createPerson() {
		return createPerson("FirstName", "LastName");
	}

	public PersonDto createPerson(String firstName, String lastName) {
		return createPerson(firstName, lastName, null);
	}

	public PersonDto createPerson(String firstName, String lastName, Consumer<PersonDto> customConfig) {

		PersonDto person = PersonDto.build();
		person.setFirstName(firstName);
		person.setLastName(lastName);

		if (customConfig != null) {
			customConfig.accept(person);
		}

		person = beanTest.getPersonFacade().savePerson(person);

		return person;
	}

	public PersonDto createPerson(String firstName, String lastName, Sex sex, Integer birthdateYYYY, Integer birthdateMM, Integer birthdateDD) {
		return createPerson(firstName, lastName, sex, birthdateYYYY, birthdateMM, birthdateDD, null, null, null);
	}

	public PersonDto createPerson(
		String firstName,
		String lastName,
		Sex sex,
		Integer birthdateYYYY,
		Integer birthdateMM,
		Integer birthdateDD,
		String passportNr,
		String nationalHealthId) {
		return createPerson(firstName, lastName, sex, birthdateYYYY, birthdateMM, birthdateDD, null, passportNr, nationalHealthId);
	}

	private PersonDto createPerson(
		String firstName,
		String lastName,
		Sex sex,
		Integer birthdateYYYY,
		Integer birthdateMM,
		Integer birthdateDD,
		LocationDto address,
		String passportNr,
		String nationalHealthId) {

		PersonDto person = PersonDto.build();
		person.setFirstName(firstName);
		person.setLastName(lastName);
		person.setSex(sex);
		person.setBirthdateYYYY(birthdateYYYY);
		person.setBirthdateMM(birthdateMM);
		person.setBirthdateDD(birthdateDD);
		person.setPassportNumber(passportNr);
		person.setNationalHealthId(nationalHealthId);

		if (address != null) {
			person.setAddress(address);
		}

		person = beanTest.getPersonFacade().savePerson(person);

		return person;
	}

	public PersonDto createPerson(
		String firstName,
		String lastName,
		Sex sex,
		Integer birthdateYYYY,
		Integer birthdateMM,
		Integer birthdateDD,
		Consumer<PersonDto> customConfig) {

		PersonDto person = PersonDto.build();
		person.setFirstName(firstName);
		person.setLastName(lastName);
		person.setSex(sex);
		person.setBirthdateYYYY(birthdateYYYY);
		person.setBirthdateMM(birthdateMM);
		person.setBirthdateDD(birthdateDD);

		if (customConfig != null) {
			customConfig.accept(person);
		}

		person = beanTest.getPersonFacade().savePerson(person);

		return person;
	}

	public CaseDataDto createUnclassifiedCase(Disease disease) {

		RDCFEntities rdcf = createRDCFEntities("Region", "District", "Community", "Facility");
		UserDto user =
			createUser(rdcf.region.getUuid(), rdcf.district.getUuid(), rdcf.facility.getUuid(), "Surv", "Sup", UserRole.SURVEILLANCE_SUPERVISOR);
		PersonDto cazePerson = createPerson("Case", "Person");
		return createCase(
			user.toReference(),
			cazePerson.toReference(),
			disease,
			CaseClassification.NOT_CLASSIFIED,
			InvestigationStatus.PENDING,
			new Date(),
			rdcf);
	}

	public CaseDataDto createCase(UserReferenceDto user, PersonReferenceDto person, RDCFEntities rdcf) {
		return createCase(user, person, Disease.EVD, CaseClassification.SUSPECT, InvestigationStatus.PENDING, new Date(), rdcf);
	}

	public CaseDataDto createCase(UserReferenceDto user, RDCF rdcf, Consumer<CaseDataDto> setCustomFields) {
		return createCase(
			user,
			createPerson().toReference(),
			Disease.EVD,
			CaseClassification.SUSPECT,
			InvestigationStatus.PENDING,
			new Date(),
			rdcf,
			setCustomFields);
	}

	public CaseDataDto createCase(UserReferenceDto user, PersonReferenceDto person, RDCF rdcf) {
		return createCase(user, person, Disease.EVD, CaseClassification.SUSPECT, InvestigationStatus.PENDING, new Date(), rdcf);
	}

	public CaseDataDto createCase(UserReferenceDto user, PersonReferenceDto person, RDCF rdcf, Consumer<CaseDataDto> setCustomFields) {
		return createCase(user, person, Disease.EVD, CaseClassification.SUSPECT, InvestigationStatus.PENDING, new Date(), rdcf, setCustomFields);
	}

	public CaseDataDto createCase(
		UserReferenceDto user,
		PersonReferenceDto cazePerson,
		Disease disease,
		CaseClassification caseClassification,
		InvestigationStatus investigationStatus,
		Date reportAndOnsetDate,
		RDCFEntities rdcf) {

		return createCase(user, cazePerson, disease, caseClassification, investigationStatus, reportAndOnsetDate, new RDCF(rdcf));
	}

	public CaseDataDto createCase(
		UserReferenceDto user,
		PersonReferenceDto cazePerson,
		Disease disease,
		CaseClassification caseClassification,
		InvestigationStatus investigationStatus,
		Date reportAndOnsetDate,
		RDCFEntities rdcf,
		String healthFacilityDetails) {

		final CaseDataDto aCase = createCase(user, cazePerson, disease, caseClassification, investigationStatus, reportAndOnsetDate, new RDCF(rdcf));
		aCase.setHealthFacilityDetails(healthFacilityDetails);
		return beanTest.getCaseFacade().saveCase(aCase);
	}

	public CaseDataDto createCase(
		UserReferenceDto user,
		PersonReferenceDto cazePerson,
		Disease disease,
		CaseClassification caseClassification,
		InvestigationStatus investigationStatus,
		Date reportAndOnsetDate,
		RDCF rdcf) {

		return createCase(user, cazePerson, disease, caseClassification, investigationStatus, reportAndOnsetDate, rdcf, null);
	}

	public CaseDataDto createCase(
		UserReferenceDto user,
		PersonReferenceDto cazePerson,
		Disease disease,
		CaseClassification caseClassification,
		InvestigationStatus investigationStatus,
		Date reportAndOnsetDate,
		RDCF rdcf,
		Consumer<CaseDataDto> setCustomFields) {

		CaseDataDto caze = CaseDataDto.build(cazePerson, disease);
		caze.setReportDate(reportAndOnsetDate);
		caze.setReportingUser(user);
		caze.getSymptoms().setOnsetDate(reportAndOnsetDate);
		caze.setCaseClassification(caseClassification);
		caze.setInvestigationStatus(investigationStatus);
		caze.setResponsibleRegion(rdcf.region);
		caze.setResponsibleDistrict(rdcf.district);
		caze.setResponsibleCommunity(rdcf.community);
		caze.setFacilityType(beanTest.getFacilityFacade().getByUuid(rdcf.facility.getUuid()).getType());
		caze.setHealthFacility(rdcf.facility);
		caze.setPointOfEntry(rdcf.pointOfEntry);

		if (setCustomFields != null) {
			setCustomFields.accept(caze);
		}

		caze = beanTest.getCaseFacade().saveCase(caze);

		return caze;
	}

	public ImmunizationDto createImmunization(
		Disease disease,
		PersonReferenceDto person,
		UserReferenceDto reportingUser,
		ImmunizationStatus immunizationStatus,
		MeansOfImmunization meansOfImmunization,
		ImmunizationManagementStatus immunizationManagementStatus,
		RDCF rdcf) {
		ImmunizationDto immunization = new ImmunizationDto();
		immunization.setUuid(DataHelper.createUuid());
		immunization.setDisease(disease);
		immunization.setPerson(person);
		immunization.setReportingUser(reportingUser);
		immunization.setImmunizationStatus(immunizationStatus);
		immunization.setMeansOfImmunization(meansOfImmunization);
		immunization.setImmunizationManagementStatus(immunizationManagementStatus);
		immunization.setResponsibleRegion(rdcf.region);
		immunization.setResponsibleDistrict(rdcf.district);
		immunization.setResponsibleCommunity(rdcf.community);

		immunization.setReportDate(new Date());

		return beanTest.getImmunizationFacade().save(immunization);
	}

	public VaccinationEntityDto createVaccinationEntity(
		UserReferenceDto reportingUser,
		ImmunizationReferenceDto immunization,
		HealthConditionsDto healthConditions) {
		VaccinationEntityDto vaccination = new VaccinationEntityDto();
		vaccination.setUuid(DataHelper.createUuid());
		vaccination.setReportingUser(reportingUser);
		vaccination.setReportDate(new Date());

		vaccination.setImmunization(immunization);
		vaccination.setHealthConditions(healthConditions);

		return beanTest.getVaccinationEntityFacade().save(vaccination);
	}

	public TravelEntryDto createTravelEntry(
		PersonReferenceDto person,
		UserReferenceDto reportingUser,
		Disease disease,
		RegionReferenceDto responsibleRegion,
		DistrictReferenceDto responsibleDistrict,
		PointOfEntryReferenceDto pointOfEntry) {

		TravelEntryDto travelEntry = TravelEntryDto.build(person);
		travelEntry.setDisease(disease);
		travelEntry.setReportingUser(reportingUser);
		travelEntry.setResponsibleRegion(responsibleRegion);
		travelEntry.setResponsibleDistrict(responsibleDistrict);
		travelEntry.setPointOfEntry(pointOfEntry);

		return beanTest.getTravelEntryFacade().save(travelEntry);
	}

	public ClinicalVisitDto createClinicalVisit(CaseDataDto caze) {
		return createClinicalVisit(caze, null);
	}

	public ClinicalVisitDto createClinicalVisit(CaseDataDto caze, Consumer<ClinicalVisitDto> extraConfig) {

		ClinicalVisitDto clinicalVisit = ClinicalVisitDto.build(caze.getClinicalCourse().toReference(), caze.getDisease());

		if (extraConfig != null) {
			extraConfig.accept(clinicalVisit);
		}

		clinicalVisit = beanTest.getClinicalVisitFacade().saveClinicalVisit(clinicalVisit, caze.getUuid());
		return clinicalVisit;
	}

	public PrescriptionDto createPrescription(CaseDataDto caze) {
		return createPrescription(caze, null);
	}

	public PrescriptionDto createPrescription(CaseDataDto caze, Consumer<PrescriptionDto> customConfig) {

		PrescriptionDto prescription = PrescriptionDto.buildPrescription(caze.getTherapy().toReference());
		prescription.setPrescriptionType(TreatmentType.BLOOD_TRANSFUSION);

		if (customConfig != null) {
			customConfig.accept(prescription);
		}

		prescription = beanTest.getPrescriptionFacade().savePrescription(prescription);

		return prescription;
	}

	public TreatmentDto createTreatment(CaseDataDto caze) {
		return createTreatment(caze, null);
	}

	public TreatmentDto createTreatment(CaseDataDto caze, Consumer<TreatmentDto> customConfig) {

		TreatmentDto treatment = TreatmentDto.build(caze.getTherapy().toReference());
		treatment.setTreatmentType(TreatmentType.BLOOD_TRANSFUSION);

		if (customConfig != null) {
			customConfig.accept(treatment);
		}

		treatment = beanTest.getTreatmentFacade().saveTreatment(treatment);

		return treatment;
	}

	public ContactDto createContact(UserReferenceDto reportingUser, PersonReferenceDto contactPerson) {
		return createContact(reportingUser, null, contactPerson, null, new Date(), null, null, null);
	}

	public ContactDto createContact(UserReferenceDto reportingUser, PersonReferenceDto contactPerson, Disease disease) {
		return createContact(reportingUser, null, contactPerson, null, new Date(), null, disease, null);
	}

	public ContactDto createContact(UserReferenceDto reportingUser, PersonReferenceDto contactPerson, Date reportDateTime) {
		return createContact(reportingUser, null, contactPerson, null, reportDateTime, null, null, null);
	}

	public ContactDto createContact(UserReferenceDto reportingUser, PersonReferenceDto contactPerson, CaseDataDto caze) {
		return createContact(reportingUser, null, contactPerson, caze, new Date(), null, null, null);
	}

	public ContactDto createContact(
		UserReferenceDto reportingUser,
		UserReferenceDto contactOfficer,
		PersonReferenceDto contactPerson,
		CaseDataDto caze,
		Date reportDateTime,
		Date lastContactDate,
		Disease disease) {

		return createContact(reportingUser, contactOfficer, contactPerson, caze, reportDateTime, lastContactDate, disease, null);
	}

	public ContactDto createContact(
		UserReferenceDto reportingUser,
		UserReferenceDto contactOfficer,
		PersonReferenceDto contactPerson,
		CaseDataDto caze,
		Date reportDateTime,
		Date lastContactDate,
		Disease disease,
		RDCF rdcf) {
		return createContact(reportingUser, contactOfficer, contactPerson, caze, reportDateTime, lastContactDate, disease, rdcf, null);
	}

	public ContactDto createContact(
		UserReferenceDto reportingUser,
		UserReferenceDto contactOfficer,
		PersonReferenceDto contactPerson,
		CaseDataDto caze,
		Date reportDateTime,
		Date lastContactDate,
		Disease disease,
		RDCF rdcf,
		Consumer<ContactDto> customConfig) {
		ContactDto contact;

		if (caze != null) {
			contact = ContactDto.build(caze);
		} else {
			contact = ContactDto.build(null, disease != null ? disease : Disease.EVD, null, null);
			if (rdcf == null) {
				rdcf = createRDCF();
			}
			contact.setRegion(rdcf.region);
			contact.setDistrict(rdcf.district);
		}
		contact.setReportingUser(reportingUser);
		contact.setContactOfficer(contactOfficer);
		contact.setPerson(contactPerson);
		contact.setReportDateTime(reportDateTime);
		contact.setLastContactDate(lastContactDate);
		contact.setEpiData(EpiDataDto.build());

		if (customConfig != null) {
			customConfig.accept(contact);
		}

		contact = beanTest.getContactFacade().saveContact(contact);

		return contact;
	}

	public TaskDto createTask(UserReferenceDto assigneeUser) {
		return createTask(TaskContext.GENERAL, TaskType.OTHER, TaskStatus.PENDING, null, null, null, new Date(), assigneeUser);
	}

	public TaskDto createTask(
		TaskContext context,
		TaskType type,
		TaskStatus status,
		CaseReferenceDto caze,
		ContactReferenceDto contact,
		EventReferenceDto event,
		Date dueDate,
		UserReferenceDto assigneeUser) {

		ReferenceDto entityRef;
		switch (context) {
		case CASE:
			entityRef = caze;
			break;
		case CONTACT:
			entityRef = contact;
			break;
		case EVENT:
			entityRef = event;
			break;
		case GENERAL:
			entityRef = null;
			break;
		default:
			throw new IllegalArgumentException(context.toString());
		}

		TaskDto task = TaskDto.build(context, entityRef);
		task.setTaskType(type);
		task.setTaskStatus(status);
		task.setDueDate(dueDate);
		task.setAssigneeUser(assigneeUser);

		task = beanTest.getTaskFacade().saveTask(task);

		return task;
	}

	public VisitDto createVisit(PersonReferenceDto person) {
		return createVisit(Disease.EVD, person);
	}

	public VisitDto createVisit(Disease disease, PersonReferenceDto person) {
		return createVisit(disease, person, new Date(), VisitStatus.COOPERATIVE, VisitOrigin.USER);
	}

	public VisitDto createVisit(Disease disease, PersonReferenceDto person, Date visitDateTime) {
		return createVisit(disease, person, visitDateTime, VisitStatus.COOPERATIVE, VisitOrigin.USER);
	}

	public VisitDto createVisit(Disease disease, PersonReferenceDto person, Date visitDateTime, VisitStatus visitStatus, VisitOrigin visitOrigin) {
		return createVisit(disease, person, visitDateTime, visitStatus, visitOrigin, null);
	}

	public VisitDto createVisit(
		Disease disease,
		PersonReferenceDto person,
		Date visitDateTime,
		VisitStatus visitStatus,
		VisitOrigin visitOrigin,
		Consumer<VisitDto> customConfig) {
		VisitDto visit = VisitDto.build(person, disease, visitOrigin);
		visit.setVisitDateTime(visitDateTime);
		visit.setVisitStatus(visitStatus);

		if (customConfig != null) {
			customConfig.accept(visit);
		}

		visit = beanTest.getVisitFacade().saveVisit(visit);

		return visit;
	}

	public EventDto createEvent(UserReferenceDto reportingUser) {

		return createEvent(reportingUser, new Date());
	}

	public EventDto createEvent(UserReferenceDto reportingUser, Date eventDate) {

		return createEvent(
			EventStatus.SIGNAL,
			EventInvestigationStatus.PENDING,
			"title",
			"Description",
			"FirstName",
			"LastName",
			null,
			null,
			eventDate,
			new Date(),
			reportingUser,
			null,
			null,
			null);
	}

	public EventDto createEvent(
		EventStatus eventStatus,
		EventInvestigationStatus eventInvestigationStatus,
		String eventTitle,
		String eventDesc,
		String srcFirstName,
		String srcLastName,
		String srcTelNo,
		TypeOfPlace typeOfPlace,
		Date eventDate,
		Date reportDateTime,
		UserReferenceDto reportingUser,
		UserReferenceDto responsibleUser,
		Disease disease,
		DistrictReferenceDto district) {

		return createEvent(eventStatus, eventInvestigationStatus, eventTitle, eventDesc, reportingUser, (event) -> {
			event.setSrcFirstName(srcFirstName);
			event.setSrcLastName(srcLastName);
			event.setSrcTelNo(srcTelNo);
			event.setTypeOfPlace(typeOfPlace);
			event.setStartDate(eventDate);
			event.setReportDateTime(reportDateTime);
			event.setReportingUser(reportingUser);
			event.setResponsibleUser(responsibleUser);
			event.setDisease(disease);
			event.getEventLocation().setDistrict(district);
		});
	}

	public EventDto createEvent(
		EventStatus eventStatus,
		EventInvestigationStatus eventInvestigationStatus,
		String eventTitle,
		String eventDesc,
		UserReferenceDto reportingUser,
		Consumer<EventDto> customSettings) {

		EventDto event = EventDto.build();
		event.setEventStatus(eventStatus);
		event.setEventInvestigationStatus(eventInvestigationStatus);
		event.setEventTitle(eventTitle);
		event.setEventDesc(eventDesc);
		event.setReportingUser(reportingUser);

		if (customSettings != null) {
			customSettings.accept(event);
		}

		event = beanTest.getEventFacade().saveEvent(event);

		return event;
	}

	public EventParticipantDto createEventParticipant(EventReferenceDto event, PersonDto eventPerson, UserReferenceDto reportingUser) {
		return createEventParticipant(event, eventPerson, "Description", reportingUser, null);
	}

	public EventParticipantDto createEventParticipant(
		EventReferenceDto event,
		PersonDto eventPerson,
		String involvementDescription,
		UserReferenceDto reportingUser) {
		return createEventParticipant(event, eventPerson, involvementDescription, reportingUser, null);
	}

	public EventParticipantDto createEventParticipant(
		EventReferenceDto event,
		PersonDto eventPerson,
		String involvementDescription,
		UserReferenceDto reportingUser,
		Consumer<EventParticipantDto> customSettings) {

		EventParticipantDto eventParticipant = EventParticipantDto.build(event, reportingUser);
		eventParticipant.setPerson(eventPerson);
		eventParticipant.setInvolvementDescription(involvementDescription);

		if (customSettings != null) {
			customSettings.accept(eventParticipant);
		}

		eventParticipant = beanTest.getEventParticipantFacade().saveEventParticipant(eventParticipant);
		return eventParticipant;
	}

	public SurveillanceReportDto createSurveillanceReport(UserReferenceDto reportingUser, CaseReferenceDto caseReference) {
		return createSurveillanceReport(reportingUser, ReportingType.DOCTOR, caseReference);
	}

	public SurveillanceReportDto createSurveillanceReport(
		UserReferenceDto reportingUser,
		ReportingType reportingType,
		CaseReferenceDto caseReference) {
		SurveillanceReportDto surveillanceReport = SurveillanceReportDto.build(caseReference, reportingUser);
		surveillanceReport.setReportingType(reportingType);
		surveillanceReport.setReportDate(new Date());

		surveillanceReport = beanTest.getSurveillanceReportFacade().saveSurveillanceReport(surveillanceReport);

		return surveillanceReport;
	}

	public ActionDto createAction(EventReferenceDto event) {

		ActionDto action = ActionDto.build(ActionContext.EVENT, event);

		action = beanTest.getActionFacade().saveAction(action);
		return action;
	}

	public SampleDto createSample(CaseReferenceDto associatedCase, UserReferenceDto reportingUser, Facility lab) {
		return createSample(associatedCase, reportingUser, lab, null);
	}

	public SampleDto createSample(CaseReferenceDto associatedCase, UserReferenceDto reportingUser, Facility lab, Consumer<SampleDto> customConfig) {
		return createSample(associatedCase, new Date(), new Date(), reportingUser, SampleMaterial.BLOOD, lab, customConfig);
	}

	public SampleDto createSample(CaseReferenceDto associatedCase, UserReferenceDto reportingUser, FacilityReferenceDto lab) {
		return createSample(associatedCase, reportingUser, lab, null);
	}

	public SampleDto createSample(
		CaseReferenceDto associatedCase,
		UserReferenceDto reportingUser,
		FacilityReferenceDto lab,
		Consumer<SampleDto> customSettings) {

		SampleDto sample = SampleDto.build(reportingUser, associatedCase);
		sample.setSampleDateTime(new Date());
		sample.setReportDateTime(new Date());
		sample.setSampleMaterial(SampleMaterial.BLOOD);
		sample.setSamplePurpose(SamplePurpose.EXTERNAL);
		sample.setLab(beanTest.getFacilityFacade().getFacilityReferenceByUuid(lab.getUuid()));

		if (customSettings != null) {
			customSettings.accept(sample);
		}

		sample = beanTest.getSampleFacade().saveSample(sample);

		return sample;
	}

	public SampleDto createSample(
		CaseReferenceDto associatedCase,
		Date sampleDateTime,
		Date reportDateTime,
		UserReferenceDto reportingUser,
		SampleMaterial sampleMaterial,
		Facility lab) {
		return createSample(associatedCase, sampleDateTime, reportDateTime, reportingUser, sampleMaterial, lab, null);
	}

	public SampleDto createSample(
		CaseReferenceDto associatedCase,
		Date sampleDateTime,
		Date reportDateTime,
		UserReferenceDto reportingUser,
		SampleMaterial sampleMaterial,
		Facility lab,
		Consumer<SampleDto> customConfig) {
		SampleDto sample = SampleDto.build(reportingUser, associatedCase);
		sample.setSampleDateTime(sampleDateTime);
		sample.setReportDateTime(reportDateTime);
		sample.setSampleMaterial(sampleMaterial);
		sample.setSamplePurpose(SamplePurpose.EXTERNAL);
		sample.setLab(beanTest.getFacilityFacade().getFacilityReferenceByUuid(lab.getUuid()));

		if (customConfig != null) {
			customConfig.accept(sample);
		}

		sample = beanTest.getSampleFacade().saveSample(sample);

		return sample;
	}

	public SampleDto createSample(
		CaseReferenceDto associatedCase,
		Date sampleDateTime,
		Date reportDateTime,
		UserReferenceDto reportingUser,
		SampleMaterial sampleMaterial,
		FacilityReferenceDto lab) {

		SampleDto sample = SampleDto.build(reportingUser, associatedCase);
		sample.setSampleDateTime(sampleDateTime);
		sample.setReportDateTime(reportDateTime);
		sample.setSampleMaterial(sampleMaterial);
		sample.setSamplePurpose(SamplePurpose.EXTERNAL);
		sample.setLab(lab);

		sample = beanTest.getSampleFacade().saveSample(sample);
		return sample;
	}

	@Deprecated
	public SampleDto createSample(
		ContactReferenceDto associatedContact,
		Date sampleDateTime,
		Date reportDateTime,
		UserReferenceDto reportingUser,
		SampleMaterial sampleMaterial,
		Facility lab) {

		SampleDto sample = SampleDto.build(reportingUser, associatedContact);
		sample.setSampleDateTime(sampleDateTime);
		sample.setReportDateTime(reportDateTime);
		sample.setSampleMaterial(sampleMaterial);
		sample.setSamplePurpose(SamplePurpose.EXTERNAL);
		sample.setLab(beanTest.getFacilityFacade().getFacilityReferenceByUuid(lab.getUuid()));

		sample = beanTest.getSampleFacade().saveSample(sample);
		return sample;
	}

	public SampleDto createSample(
		ContactReferenceDto associatedContact,
		Date sampleDateTime,
		Date reportDateTime,
		UserReferenceDto reportingUser,
		SampleMaterial sampleMaterial,
		FacilityReferenceDto lab) {

		SampleDto sample = SampleDto.build(reportingUser, associatedContact);
		sample.setSampleDateTime(sampleDateTime);
		sample.setReportDateTime(reportDateTime);
		sample.setSampleMaterial(sampleMaterial);
		sample.setSamplePurpose(SamplePurpose.EXTERNAL);
		sample.setLab(lab);

		sample = beanTest.getSampleFacade().saveSample(sample);
		return sample;
	}

	public SampleDto createSample(
		ContactReferenceDto associatedContact,
		UserReferenceDto reportingUser,
		FacilityReferenceDto lab,
		Consumer<SampleDto> customSettings) {

		SampleDto sample = SampleDto.build(reportingUser, associatedContact);
		sample.setSampleDateTime(new Date());
		sample.setReportDateTime(new Date());
		sample.setSampleMaterial(SampleMaterial.BLOOD);
		sample.setSamplePurpose(SamplePurpose.EXTERNAL);
		sample.setLab(beanTest.getFacilityFacade().getFacilityReferenceByUuid(lab.getUuid()));

		if (customSettings != null) {
			customSettings.accept(sample);
		}

		sample = beanTest.getSampleFacade().saveSample(sample);

		return sample;
	}

	public SampleDto createSample(
		EventParticipantReferenceDto associatedEventParticipant,
		Date sampleDateTime,
		Date reportDateTime,
		UserReferenceDto reportingUser,
		SampleMaterial sampleMaterial,
		FacilityReferenceDto lab) {

		SampleDto sample = SampleDto.build(reportingUser, associatedEventParticipant);
		sample.setSampleDateTime(sampleDateTime);
		sample.setReportDateTime(reportDateTime);
		sample.setSampleMaterial(sampleMaterial);
		sample.setSamplePurpose(SamplePurpose.EXTERNAL);
		sample.setLab(lab);

		sample = beanTest.getSampleFacade().saveSample(sample);
		return sample;
	}

	public PathogenTestDto createPathogenTest(
		SampleReferenceDto sample,
		PathogenTestType testType,
		Disease testedDisease,
		Date testDateTime,
		Facility lab,
		UserReferenceDto labUser,
		PathogenTestResultType testResult,
		String testResultText,
		boolean verified) {
		return createPathogenTest(sample, testType, testedDisease, testDateTime, lab, labUser, testResult, testResultText, verified, null);
	}

	public PathogenTestDto createPathogenTest(
		SampleReferenceDto sample,
		PathogenTestType testType,
		Disease testedDisease,
		Date testDateTime,
		Facility lab,
		UserReferenceDto labUser,
		PathogenTestResultType testResult,
		String testResultText,
		boolean verified,
		Consumer<PathogenTestDto> extraConfig) {

		PathogenTestDto sampleTest = PathogenTestDto.build(sample, labUser);
		sampleTest.setTestedDisease(testedDisease);
		sampleTest.setTestType(testType);
		sampleTest.setTestDateTime(testDateTime);
		sampleTest.setLab(lab != null ? beanTest.getFacilityFacade().getFacilityReferenceByUuid(lab.getUuid()) : null);
		sampleTest.setTestResult(testResult);
		sampleTest.setTestResultText(testResultText);
		sampleTest.setTestResultVerified(verified);

		if (extraConfig != null) {
			extraConfig.accept(sampleTest);
		}

		sampleTest = beanTest.getSampleTestFacade().savePathogenTest(sampleTest);
		return sampleTest;
	}

	public PathogenTestDto createPathogenTest(
		SampleReferenceDto sample,
		PathogenTestType testType,
		Disease testedDisease,
		Date testDateTime,
		FacilityReferenceDto lab,
		UserReferenceDto labUser,
		PathogenTestResultType testResult,
		String testResultText,
		boolean verified,
		Consumer<PathogenTestDto> extraConfig) {

		PathogenTestDto sampleTest = PathogenTestDto.build(sample, labUser);
		sampleTest.setTestedDisease(testedDisease);
		sampleTest.setTestType(testType);
		sampleTest.setTestDateTime(testDateTime);
		sampleTest.setLab(lab);
		sampleTest.setTestResult(testResult);
		sampleTest.setTestResultText(testResultText);
		sampleTest.setTestResultVerified(verified);

		if (extraConfig != null) {
			extraConfig.accept(sampleTest);
		}

		sampleTest = beanTest.getSampleTestFacade().savePathogenTest(sampleTest);
		return sampleTest;
	}

	public PathogenTestDto createPathogenTest(CaseDataDto associatedCase, PathogenTestType testType, PathogenTestResultType resultType) {
		return createPathogenTest(associatedCase, null, testType, resultType);
	}

	public PathogenTestDto createPathogenTest(SampleReferenceDto sample, CaseDataDto associatedCase) {
		RDCFEntities rdcf = createRDCFEntities("LabRegion", "LabDistrict", "LabCommunity", "LabFacilty");

		return createPathogenTest(
			sample,
			PathogenTestType.ANTIGEN_DETECTION,
			associatedCase.getDisease(),
			new Date(),
			rdcf.facility,
			associatedCase.getReportingUser(),
			PathogenTestResultType.PENDING,
			"",
			false);
	}

	public PathogenTestDto createPathogenTest(
		CaseDataDto associatedCase,
		Disease testedDisease,
		PathogenTestType testType,
		PathogenTestResultType resultType) {

		RDCFEntities rdcf = createRDCFEntities("Region", "District", "Community", "Facility");
		SampleDto sample = createSample(
			new CaseReferenceDto(associatedCase.getUuid()),
			new Date(),
			new Date(),
			associatedCase.getReportingUser(),
			SampleMaterial.BLOOD,
			rdcf.facility);
		return createPathogenTest(
			new SampleReferenceDto(sample.getUuid()),
			testType,
			testedDisease,
			new Date(),
			rdcf.facility,
			associatedCase.getReportingUser(),
			resultType,
			"",
			true);
	}

	public PathogenTestDto buildPathogenTestDto(RDCFEntities rdcf, UserDto user, SampleDto sample, Disease disease, Date testDateTime) {

		final PathogenTestDto newPathogenTest = new PathogenTestDto();

		newPathogenTest.setSample(sample.toReference());
		newPathogenTest.setTestedDisease(disease);
		newPathogenTest.setTestType(PathogenTestType.ISOLATION);

		newPathogenTest.setTestDateTime(testDateTime);
		newPathogenTest.setLab(new FacilityReferenceDto(rdcf.facility.getUuid(), null, null));
		newPathogenTest.setLabUser(user.toReference());
		newPathogenTest.setTestResult(PathogenTestResultType.PENDING);
		newPathogenTest.setTestResultText("all bad!");
		newPathogenTest.setTestResultVerified(false);
		return newPathogenTest;
	}

	public AdditionalTestDto createAdditionalTest(SampleReferenceDto sample) {

		AdditionalTestDto test = AdditionalTestDto.build(sample);
		test.setTestDateTime(new Date());

		test = beanTest.getAdditionalTestFacade().saveAdditionalTest(test);
		return test;
	}

	public ExposureDto buildAnimalContactExposure(TypeOfAnimal typeOfAnimal) {

		ExposureDto exposure = ExposureDto.build(ExposureType.ANIMAL_CONTACT);
		exposure.setTypeOfAnimal(typeOfAnimal);
		return exposure;
	}

	public CampaignDto createCampaign(UserDto user) {

		CampaignDto campaign = CampaignDto.build();
		campaign.setCreatingUser(user.toReference());
		campaign.setName("CampaignName");
		campaign.setDescription("Campaign description");

		campaign = beanTest.getCampaignFacade().saveCampaign(campaign);

		return campaign;
	}

	public CampaignDiagramDefinitionDto createCampaignDiagramDefinition(String diagramId, String diagramCaption) {
		CampaignDiagramDefinitionDto campaignDiagramDefinition = CampaignDiagramDefinitionDto.build();
		campaignDiagramDefinition.setDiagramType(DiagramType.COLUMN);
		campaignDiagramDefinition.setDiagramId(diagramId);
		campaignDiagramDefinition.setDiagramCaption(diagramCaption);

		return campaignDiagramDefinition;
	}

	public CampaignFormMetaDto createCampaignForm(CampaignDto campaign) throws IOException {

		CampaignFormMetaDto campaignForm;

		String schema =
			"[{\"type\": \"text\",\"id\": \"teamNumber\",\"caption\": \"Team number\",\"styles\": [\"first\"]},{\"type\": \"text\",\"id\": "
				+ "\"namesOfTeamMembers\",\"caption\": \"Names of team members\",\"styles\": [\"col-8\"]},{\"type\": \"text\",\"id\": "
				+ "\"monitorName\",\"caption\": \"Name of monitor\",\"styles\": [\"first\"]},{\"type\": \"text\",\"id\": \"agencyName\",\"caption\": "
				+ "\"Agency\"},{\"type\": \"section\",\"id\": \"questionsSection\"},{\"type\": \"label\",\"id\": \"questionsLabel\",\"caption\": \"<h2>Questions</h2>\"}"
				+ ",{\"type\": \"yes-no\",\"id\": \"oneMemberResident\",\"caption\": \"1) At least one team member is resident of same area (villages)?\"},{\"type\": "
				+ "\"yes-no\",\"id\": \"vaccinatorsTrained\",\"caption\": \"2) Both vaccinators trained before this campaign?\"},{\"type\": \"section\","
				+ " \"id\": \"questionsSection2\"},{\"type\": \"label\",\"id\": \"q8To12Label\",\"caption\": \"Q 8-12: Based on observation of team only.\"},"
				+ "{\"type\": \"yes-no\",\"id\": \"askingAboutMonthOlds\",\"caption\": \"8) Is team specially asking about 0-11 months children?\"},"
				+ "{\"type\": \"section\", \"id\": \"questionsSection3\"},{\"type\": \"yes-no\",\"id\": \"atLeastOneMemberChw\","
				+ "\"caption\": \"13) Is at least one member of the team CHW?\"},{\"type\": \"number\",\"id\": "
				+ "\"numberOfChw\",\"caption\": \"No. of CHW\",\"styles\": [\"row\"],\"dependingOn\": \"atLeastOneMemberChw\",\"dependingOnValues\": [\"YES\"]},"
				+ "{\"type\": \"yes-no\",\"id\": \"anyMemberFemale\",\"caption\": \"14) Is any member of the team female?\"},{\"type\": \"yes-no\","
				+ "\"id\": \"accompaniedBySocialMobilizer\",\"caption\": \"15) Does social mobilizer accompany the vaccination team in the field?\"},"
				+ "{\"type\": \"text\",\"id\": \"comments\",\"caption\": \"Comments\",\"styles\": [\"col-12\"]}]";
		String translations =
			"[{\"languageCode\": \"de-DE\", \"translations\": [{\"elementId\": \"teamNumber\", \"caption\": \"Teamnummer\"}, {\"elementId\": \"namesOfTeamMembers\","
				+ " \"caption\": \"Namen der Teammitglieder\"}]}, {\"languageCode\": \"fr-FR\", \"translations\": [{\"elementId\": \"teamNumber\", "
				+ "\"caption\": \"Numéro de l'équipe\"}]}]";

		campaignForm = beanTest.getCampaignFormFacade().buildCampaignFormMetaFromJson("testForm", null, schema, translations);

		campaignForm = beanTest.getCampaignFormFacade().saveCampaignFormMeta(campaignForm);

		return campaignForm;
	}

	public String getCampaignFormData() {
		return "[{\"id\": \"teamNumber\",\"value\": \"12\"},{\"id\": \"namesOfTeamMembers\", \"value\": \"Waldemar Stricker\"},"
			+ "{\"id\": \"monitorName\", \"value\": \"Josef Saks\"},{\"id\": \"agencyName\",\"value\": \"HZI Institut\"},"
			+ "{\"id\": \"oneMemberResident\", \"value\": \"yes\"},{\"id\": \"vaccinatorsTrained\",\"value\": \"no\"},"
			+ "{\"id\": \"askingAboutMonthOlds\",\"value\": \"yes\"},{\"id\": \"atLeastOneMemberChw\",\"value\": \"yes\"},"
			+ "{\"id\": \"numberOfChw\",\"value\": \"7\"},{\"id\": \"anyMemberFemale\",\"value\": \"yes\"},{\"id\": \"accompaniedBySocialMobilizer\",\"value\": \"no\"},"
			+ "{\"id\": \"comments\",\"value\": \"other comments\"}]";
	}

	public CampaignFormDataDto buildCampaignFormDataDto(CampaignDto campaign, CampaignFormMetaDto campaignForm, RDCF rdcf, String formData) {
		CampaignReferenceDto campaignReferenceDto = new CampaignReferenceDto(campaign.getUuid());
		CampaignFormMetaReferenceDto campaignFormMetaReferenceDto = new CampaignFormMetaReferenceDto(campaignForm.getUuid());

		CampaignFormDataDto campaignFormData =
			CampaignFormDataDto.build(campaignReferenceDto, campaignFormMetaReferenceDto, rdcf.region, rdcf.district, rdcf.community);

		try {
			ObjectMapper mapper = new ObjectMapper();
			campaignFormData.setFormValues(Arrays.asList(mapper.readValue(formData, CampaignFormDataEntry[].class)));
			return campaignFormData;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public CampaignFormDataDto createCampaignFormData(CampaignDto campaign, CampaignFormMetaDto campaignForm, RDCF rdcf, String formData) {

		CampaignFormDataDto campaignFormData = buildCampaignFormDataDto(campaign, campaignForm, rdcf, formData);

		campaignFormData = beanTest.getCampaignFormDataFacade().saveCampaignFormData(campaignFormData);

		return campaignFormData;
	}

	public RDCF createRDCF() {
		return createRDCF("Region", "District", "Community", "Facility");
	}

	public RDCF createRDCF(String regionName, String districtName, String communityName, String facilityName) {
		return createRDCF(regionName, districtName, communityName, facilityName, null);
	}

	public RDCF createRDCF(String regionName, String districtName, String communityName, String facilityName, String pointOfEntryName) {

		Region region = createRegion(regionName);
		District district = createDistrict(districtName, region);
		Community community = createCommunity(communityName, district);
		Facility facility = createFacility(facilityName, region, district, community);

		PointOfEntry pointOfEntry = null;
		if (pointOfEntryName != null) {
			pointOfEntry = createPointOfEntry(pointOfEntryName, region, district);
		}

		return new RDCF(
			new RegionReferenceDto(region.getUuid(), region.getName(), region.getExternalID()),
			new DistrictReferenceDto(district.getUuid(), district.getName(), district.getExternalID()),
			new CommunityReferenceDto(community.getUuid(), community.getName(), community.getExternalID()),
			new FacilityReferenceDto(facility.getUuid(), facility.getName(), facility.getExternalID()),
			pointOfEntry != null
				? new PointOfEntryReferenceDto(
					pointOfEntry.getUuid(),
					pointOfEntry.getName(),
					pointOfEntry.getPointOfEntryType(),
					pointOfEntry.getExternalID())
				: null);
	}

	public RDCFEntities createRDCFEntities() {
		return createRDCFEntities("Region", "District", "Community", "Facility");
	}

	public RDCFEntities createRDCFEntities(String regionName, String districtName, String communityName, String facilityName) {

		Region region = createRegion(regionName);
		District district = createDistrict(districtName, region);
		Community community = createCommunity(communityName, district);
		Facility facility = createFacility(facilityName, region, district, community);

		return new RDCFEntities(region, district, community, facility);
	}

	public Continent createContinent(String name) {
		Continent continent = new Continent();
		continent.setUuid(DataHelper.createUuid());
		continent.setDefaultName(name);
		beanTest.getContinentService().persist(continent);

		return continent;
	}

	public Country createCountry(String countryName, String isoCode, String unoCode) {
		Country country = new Country();
		country.setUuid(DataHelper.createUuid());
		country.setDefaultName(countryName);
		country.setIsoCode(isoCode);
		country.setUnoCode(unoCode);
		beanTest.getCountryService().persist(country);

		return country;
	}

	public Region createRegion(String regionName) {
		return createRegion(regionName, null);
	}

	public Region createRegion(String regionName, String externalId) {
		Region region = new Region();
		region.setUuid(DataHelper.createUuid());
		region.setName(regionName);
		region.setEpidCode("COU-REG");
		region.setExternalID(externalId);

		beanTest.getRegionService().persist(region);

		return region;
	}

	public District createDistrict(String districtName, Region region) {
		return createDistrict(districtName, region, null);
	}

	public District createDistrict(String districtName, Region region, String externalId) {

		District district = new District();
		district.setUuid(DataHelper.createUuid());
		district.setName(districtName);
		district.setRegion(region);
		district.setEpidCode("DIS");
		district.setExternalID(externalId);
		beanTest.getDistrictService().persist(district);

		return district;
	}

	public Community createCommunity(String communityName, District district) {
		return createCommunity(communityName, district, null);
	}

	public Community createCommunity(String communityName, District district, String externalId) {

		Community community = new Community();
		community.setUuid(DataHelper.createUuid());
		community.setName(communityName);
		community.setDistrict(district);
		community.setExternalID(externalId);
		beanTest.getCommunityService().persist(community);

		return community;
	}

	public CommunityDto createCommunity(String communityName, DistrictReferenceDto district) {

		CommunityDto community = CommunityDto.build();
		community.setName(communityName);
		community.setDistrict(district);
		beanTest.getCommunityFacade().saveCommunity(community);
		return community;
	}

	public Facility createFacility(String facilityName, Region region, District district, Community community) {
		return createFacility(facilityName, FacilityType.HOSPITAL, region, district, community);
	}

	public Facility createFacility(String facilityName, FacilityType type, Region region, District district, Community community) {
		return createFacility(facilityName, type, region, district, community, null);
	}

	public Facility createFacility(String facilityName, FacilityType type, Region region, District district, Community community, String externalId) {

		Facility facility = new Facility();
		facility.setUuid(DataHelper.createUuid());
		facility.setName(facilityName);
		facility.setCommunity(community);
		facility.setDistrict(district);
		facility.setRegion(region);
		facility.setType(type);
		facility.setExternalID(externalId);

		beanTest.getFacilityService().persist(facility);

		return facility;
	}

	public FacilityDto createFacility(
		String facilityName,
		RegionReferenceDto region,
		DistrictReferenceDto district,
		CommunityReferenceDto community) {
		return createFacility(facilityName, region, district, community, null);
	}

	public FacilityDto createFacility(
		String facilityName,
		RegionReferenceDto region,
		DistrictReferenceDto district,
		CommunityReferenceDto community,
		FacilityType type) {

		FacilityDto facility = FacilityDto.build();
		facility.setName(facilityName);
		facility.setType(type == null ? FacilityType.HOSPITAL : type);
		facility.setCommunity(community);
		facility.setDistrict(district);
		facility.setRegion(region);
		beanTest.getFacilityFacade().saveFacility(facility);
		return facility;
	}

	public PointOfEntry createPointOfEntry(String pointOfEntryName, Region region, District district) {
		return createPointOfEntry(pointOfEntryName, region, district, null);
	}

	public PointOfEntry createPointOfEntry(String pointOfEntryName, Region region, District district, String externalId) {

		PointOfEntry pointOfEntry = new PointOfEntry();
		pointOfEntry.setUuid(DataHelper.createUuid());
		pointOfEntry.setPointOfEntryType(PointOfEntryType.AIRPORT);
		pointOfEntry.setName(pointOfEntryName);
		pointOfEntry.setDistrict(district);
		pointOfEntry.setRegion(region);
		pointOfEntry.setExternalID(externalId);

		beanTest.getPointOfEntryService().persist(pointOfEntry);

		return pointOfEntry;
	}

	public PointOfEntryDto createPointOfEntry(String pointOfEntryName, RegionReferenceDto region, DistrictReferenceDto district) {

		PointOfEntryDto pointOfEntry = PointOfEntryDto.build();
		pointOfEntry.setUuid(DataHelper.createUuid());
		pointOfEntry.setPointOfEntryType(PointOfEntryType.AIRPORT);
		pointOfEntry.setName(pointOfEntryName);
		pointOfEntry.setDistrict(district);
		pointOfEntry.setRegion(region);
		beanTest.getPointOfEntryFacade().save(pointOfEntry);

		return pointOfEntry;
	}

	public PopulationDataDto createPopulationData(RegionReferenceDto region, DistrictReferenceDto district, Integer population, Date collectionDate) {

		PopulationDataDto populationData = PopulationDataDto.build(collectionDate);
		populationData.setRegion(region);
		populationData.setDistrict(district);
		populationData.setPopulation(population);
		beanTest.getPopulationDataFacade().savePopulationData(Arrays.asList(populationData));
		return populationData;
	}

	public void updateDiseaseConfiguration(Disease disease, Boolean active, Boolean primary, Boolean caseBased) {

		DiseaseConfigurationDto config =
			DiseaseConfigurationFacadeEjbLocal.toDto(beanTest.getDiseaseConfigurationService().getDiseaseConfiguration(disease));
		config.setActive(active);
		config.setPrimaryDisease(primary);
		config.setCaseBased(caseBased);
		beanTest.getDiseaseConfigurationFacade().saveDiseaseConfiguration(config);
	}

	public DocumentDto createDocument(
		UserReferenceDto uploadingUser,
		String name,
		String contentType,
		long size,
		EventReferenceDto event,
		byte[] content)
		throws IOException {
		return createDocument(uploadingUser, name, contentType, size, DocumentRelatedEntityType.EVENT, event.getUuid(), content);
	}

	public DocumentDto createDocument(
		UserReferenceDto uploadingUser,
		String name,
		String contentType,
		long size,
		DocumentRelatedEntityType relatedEntityType,
		String relatedEntityUuid,
		byte[] content)
		throws IOException {
		DocumentDto document = DocumentDto.build();
		document.setUploadingUser(uploadingUser);
		document.setName(name);
		document.setMimeType(contentType);
		document.setSize(size);
		document.setRelatedEntityType(relatedEntityType);
		document.setRelatedEntityUuid(relatedEntityUuid);

		return beanTest.getDocumentFacade().saveDocument(document, content);
	}

	public ExportConfigurationDto createExportConfiguration(String name, ExportType exportType, Set<String> properites, UserReferenceDto user) {
		ExportConfigurationDto exportConfiguration = ExportConfigurationDto.build(user, exportType);
		exportConfiguration.setName(name);
		exportConfiguration.setProperties(properites);

		beanTest.getExportFacade().saveExportConfiguration(exportConfiguration);

		return exportConfiguration;
	}

	public SystemEventDto createSystemEvent(SystemEventType type, Date startDate, SystemEventStatus status) {
		return createSystemEvent(type, startDate, new Date(startDate.getTime() + 1000), status, "Generated for test purposes");
	}

	public SystemEventDto createSystemEvent(SystemEventType type, Date startDate, Date endDate, SystemEventStatus status, String additionalInfo) {
		SystemEventDto systemEvent = SystemEventDto.build();
		systemEvent.setType(type);
		systemEvent.setStartDate(startDate);
		systemEvent.setEndDate(endDate);
		systemEvent.setStatus(status);
		systemEvent.setAdditionalInfo(additionalInfo);
		return systemEvent;
	}

	public LabMessageDto createLabMessage(Consumer<LabMessageDto> customSettings) {
		LabMessageDto labMessage = LabMessageDto.build();

		if (customSettings != null) {
			customSettings.accept(labMessage);
		}

		beanTest.getLabMessageFacade().save(labMessage);

		return labMessage;
	}

//	public DiseaseVariant createDiseaseVariant(String name, Disease disease) {
//
//		DiseaseVariant diseaseVariant = new DiseaseVariant();
//		diseaseVariant.setUuid(DataHelper.createUuid());
//		diseaseVariant.setName(name);
//		diseaseVariant.setDisease(disease);
//
//		beanTest.getDiseaseVariantService().persist(diseaseVariant);
//
//		return diseaseVariant;
//	}

	public ExternalShareInfo createExternalShareInfo(
		CaseReferenceDto caze,
		UserReferenceDto sender,
		ExternalShareStatus status,
		Consumer<ExternalShareInfo> customConfig) {
		ExternalShareInfo shareInfo = new ExternalShareInfo();
		shareInfo.setUuid(DataHelper.createUuid());
		shareInfo.setCaze(beanTest.getCaseService().getByUuid(caze.getUuid()));
		shareInfo.setSender(beanTest.getUserService().getByUuid(sender.getUuid()));
		shareInfo.setStatus(status);

		if (customConfig != null) {
			customConfig.accept(shareInfo);
		}

		beanTest.getExternalShareInfoService().ensurePersisted(shareInfo);

		return shareInfo;
	}

	public ExternalShareInfo createExternalShareInfo(EventReferenceDto event, UserReferenceDto sender, ExternalShareStatus status) {
		ExternalShareInfo shareInfo = new ExternalShareInfo();
		shareInfo.setUuid(DataHelper.createUuid());
		shareInfo.setEvent(beanTest.getEventService().getByUuid(event.getUuid()));
		shareInfo.setSender(beanTest.getUserService().getByUuid(sender.getUuid()));
		shareInfo.setStatus(status);

		beanTest.getExternalShareInfoService().ensurePersisted(shareInfo);

		return shareInfo;
	}

	/**
	 * Creates a list with {@code count} values of type {@code T}.
	 * The list index is given to the {@code valueSupplier} for each value to create.
	 */
	public static <T> List<T> createValuesList(int count, Function<Integer, T> valueSupplier) {

		List<T> values = new ArrayList<>(count);
		for (int i = 0; i < count; i++) {
			values.add(valueSupplier.apply(i));
		}
		return values;
	}

	/**
	 * @author MartinWahnschaffe
	 * @deprecated Use RDCF instead
	 */
	@Deprecated
	public static class RDCFEntities {

		public Region region;
		public District district;
		public Community community;
		public Facility facility;

		public RDCFEntities(Region region, District district, Community community, Facility facility) {
			this.region = region;
			this.district = district;
			this.community = community;
			this.facility = facility;
		}
	}

	public static class RDCF {

		public RegionReferenceDto region;
		public DistrictReferenceDto district;
		public CommunityReferenceDto community;
		public FacilityReferenceDto facility;
		public PointOfEntryReferenceDto pointOfEntry;

		public RDCF(RegionReferenceDto region, DistrictReferenceDto district, CommunityReferenceDto community, FacilityReferenceDto facility) {
			this(region, district, community, facility, null);
		}

		public RDCF(
			RegionReferenceDto region,
			DistrictReferenceDto district,
			CommunityReferenceDto community,
			FacilityReferenceDto facility,
			PointOfEntryReferenceDto pointOfEntry) {
			this.region = region;
			this.district = district;
			this.community = community;
			this.facility = facility;
			this.pointOfEntry = pointOfEntry;
		}

		public RDCF(RDCFEntities rdcfEntities) {
			this.region = new RegionReferenceDto(rdcfEntities.region.getUuid(), rdcfEntities.region.getName(), rdcfEntities.region.getExternalID());
			this.district =
				new DistrictReferenceDto(rdcfEntities.district.getUuid(), rdcfEntities.district.getName(), rdcfEntities.district.getExternalID());
			this.community =
				new CommunityReferenceDto(rdcfEntities.community.getUuid(), rdcfEntities.community.getName(), rdcfEntities.community.getExternalID());
			this.facility =
				new FacilityReferenceDto(rdcfEntities.facility.getUuid(), rdcfEntities.facility.getName(), rdcfEntities.facility.getExternalID());
		}
	}
}
