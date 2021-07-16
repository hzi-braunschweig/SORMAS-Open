package de.symeda.sormas.backend.caze;

import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import de.symeda.sormas.api.Disease;
import de.symeda.sormas.api.caze.CaseClassification;
import de.symeda.sormas.api.caze.CaseDataDto;
import de.symeda.sormas.api.caze.CaseOrigin;
import de.symeda.sormas.api.caze.InvestigationStatus;
import de.symeda.sormas.api.epidata.EpiDataDto;
import de.symeda.sormas.api.exposure.ExposureDto;
import de.symeda.sormas.api.exposure.ExposureType;
import de.symeda.sormas.api.person.PersonDto;
import de.symeda.sormas.api.symptoms.SymptomState;
import de.symeda.sormas.api.symptoms.SymptomsDto;
import de.symeda.sormas.api.user.UserDto;
import de.symeda.sormas.api.user.UserRole;
import de.symeda.sormas.backend.AbstractBeanTest;
import de.symeda.sormas.backend.TestDataCreator;

public class CasePartialUpdateTest extends AbstractBeanTest {

	@Rule
	public final ExpectedException exception = ExpectedException.none();

	@Test
	public void testDiseaseChangePartialUpdates() {

		TestDataCreator.RDCFEntities rdcf = creator.createRDCFEntities("Region", "District", "Community", "Facility");
		UserDto user = creator
			.createUser(rdcf.region.getUuid(), rdcf.district.getUuid(), rdcf.facility.getUuid(), "Surv", "Sup", UserRole.SURVEILLANCE_SUPERVISOR);
		PersonDto cazePerson = creator.createPerson("Case", "Person");
		CaseDataDto caze = creator.createCase(
			user.toReference(),
			cazePerson.toReference(),
			Disease.EVD,
			CaseClassification.PROBABLE,
			InvestigationStatus.PENDING,
			new Date(),
			rdcf);

		ObjectMapper mapper = new ObjectMapper();
		JsonNode caseJson = mapper.convertValue(caze, JsonNode.class);
		//check if the disease is the same in the converted json
		Disease actualDisease = Enum.valueOf(Disease.class, caseJson.get("disease").textValue());
		assertEquals(Disease.EVD, actualDisease);

		//change the disease in the caze and convert to json 
		//check if the disease in json reflects the modification
		caze.setDisease(Disease.MEASLES);
		caseJson = mapper.convertValue(caze, JsonNode.class);
		actualDisease = Enum.valueOf(Disease.class, caseJson.get("disease").textValue());
		assertEquals(Disease.MEASLES, actualDisease);

		//remove from json some nodes such as person, region, etc to simulate a partial json
		((ObjectNode) caseJson).remove("person");
		((ObjectNode) caseJson).remove("region");
		((ObjectNode) caseJson).remove("district");
		((ObjectNode) caseJson).remove("community");
		((ObjectNode) caseJson).remove("healthFacility");
		((ObjectNode) caseJson).remove("epiData");
		((ObjectNode) caseJson).remove("surveillanceOfficer");
		((ObjectNode) caseJson).remove("symptoms");

		//call the partial update 
		CaseDataDto casePostUpdated = getCaseFacade().postUpdate(caze.getUuid(), caseJson);
		//check if the disease was changed 
		assertEquals(Disease.MEASLES, casePostUpdated.getDisease());
		//check if the fields that were not in the json file has not be deleted
		assertEquals(cazePerson.toReference(), casePostUpdated.getPerson());
	}

	@Test
	public void testPersonReferenceChange() {

		TestDataCreator.RDCFEntities rdcf = creator.createRDCFEntities("Region", "District", "Community", "Facility");
		UserDto user = creator
			.createUser(rdcf.region.getUuid(), rdcf.district.getUuid(), rdcf.facility.getUuid(), "Surv", "Sup", UserRole.SURVEILLANCE_SUPERVISOR);
		PersonDto cazePerson = creator.createPerson("Case", "Person");
		CaseDataDto caze = creator.createCase(
			user.toReference(),
			cazePerson.toReference(),
			Disease.EVD,
			CaseClassification.PROBABLE,
			InvestigationStatus.PENDING,
			new Date(),
			rdcf);

		ObjectMapper mapper = new ObjectMapper();
		JsonNode caseJson = mapper.convertValue(caze, JsonNode.class);
		//check if the reference person uuid is the same in the converted json
		String personReferenceUUuid = caseJson.get("person").get("uuid").textValue();
		assertEquals(cazePerson.getUuid(), personReferenceUUuid);

		//change the reference person in the caze and convert to json 
		//check if the reference person in json reflects the modification
		PersonDto cazePersonToBeUpdated = creator.createPerson("Case", "PersonToBeUpdated");
		caze.setPerson(cazePersonToBeUpdated.toReference());
		caseJson = mapper.convertValue(caze, JsonNode.class);
		personReferenceUUuid = caseJson.get("person").get("uuid").textValue();
		assertEquals(cazePersonToBeUpdated.getUuid(), personReferenceUUuid);

		//remove from json some nodes such as person, region, etc to simulate a partial json

		((ObjectNode) caseJson).remove("caseClassification");
		((ObjectNode) caseJson).remove("district");
		((ObjectNode) caseJson).remove("community");
		((ObjectNode) caseJson).remove("healthFacility");
		((ObjectNode) caseJson).remove("epiData");
		((ObjectNode) caseJson).remove("surveillanceOfficer");
		((ObjectNode) caseJson).remove("symptoms");

		assertEquals(null, caseJson.get("caseClassification"));

		//call the partial update 
		CaseDataDto casePostUpdated = getCaseFacade().postUpdate(caze.getUuid(), caseJson);
		//check if the reference person has been updated  
		assertEquals(cazePersonToBeUpdated.getUuid(), casePostUpdated.getPerson().getUuid());
		//check if the fields that were not in the json file has not be deleted
		assertEquals(CaseClassification.PROBABLE, caze.getCaseClassification());
	}

	@Test
	public void testSetValueWhenNullBefore() throws JsonProcessingException {

		TestDataCreator.RDCFEntities rdcf = creator.createRDCFEntities("Region", "District", "Community", "Facility");
		UserDto user = creator
			.createUser(rdcf.region.getUuid(), rdcf.district.getUuid(), rdcf.facility.getUuid(), "Surv", "Sup", UserRole.SURVEILLANCE_SUPERVISOR);

		PersonDto cazePerson = creator.createPerson("Case", "Person");
		CaseDataDto caze = creator.createCase(
			user.toReference(),
			cazePerson.toReference(),
			Disease.EVD,
			CaseClassification.PROBABLE,
			InvestigationStatus.PENDING,
			new Date(),
			rdcf);

		assertNull(caze.getSymptoms().getAbdominalPain());
		caze.getSymptoms().setAbdominalPain(SymptomState.YES);

		ObjectMapper mapper = new ObjectMapper();
		JsonNode caseJson = mapper.convertValue(caze, JsonNode.class);
		//check if the symptoms are the same in the converted json
		SymptomsDto symptomsDto = mapper.treeToValue(caseJson.get("symptoms"), SymptomsDto.class);
		assertEquals(symptomsDto, caze.getSymptoms());

		((ObjectNode) caseJson).remove("caseClassification");
		((ObjectNode) caseJson).remove("district");
		((ObjectNode) caseJson).remove("community");
		((ObjectNode) caseJson).remove("healthFacility");
		((ObjectNode) caseJson).remove("epiData");
		((ObjectNode) caseJson).remove("surveillanceOfficer");

		//check if the symptoms was updated 
		CaseDataDto casePostUpdated = getCaseFacade().postUpdate(caze.getUuid(), caseJson);
		SymptomsDto symptomsDtoPostUpdate = mapper.treeToValue(caseJson.get("symptoms"), SymptomsDto.class);
		assertEquals(symptomsDtoPostUpdate, casePostUpdated.getSymptoms());

		casePostUpdated.getSymptoms().setVomiting(SymptomState.YES);
		JsonNode caseUpdatedJson = mapper.convertValue(casePostUpdated, JsonNode.class);
		assertFalse(caseJson.get("symptoms").get("abdominalPain").isNull());
		((ObjectNode) caseJson.get("symptoms")).remove("abdominalPain");
		assertFalse(caseJson.get("symptoms").isNull());
		assertNull(caseJson.get("symptoms").get("abdominalPain"));

		CaseDataDto casePostUpdatedSymptoms = getCaseFacade().postUpdate(caze.getUuid(), caseUpdatedJson);
		assertEquals(casePostUpdated.getSymptoms(), casePostUpdatedSymptoms.getSymptoms());

	}

	@Test
	public void testResetValue() {

		TestDataCreator.RDCFEntities rdcf = creator.createRDCFEntities("Region", "District", "Community", "Facility");
		UserDto user = creator
			.createUser(rdcf.region.getUuid(), rdcf.district.getUuid(), rdcf.facility.getUuid(), "Surv", "Sup", UserRole.SURVEILLANCE_SUPERVISOR);
		PersonDto cazePerson = creator.createPerson("Case", "Person");
		CaseDataDto caze = creator.createCase(
			user.toReference(),
			cazePerson.toReference(),
			Disease.EVD,
			CaseClassification.PROBABLE,
			InvestigationStatus.PENDING,
			new Date(),
			rdcf);
		caze.setCaseOrigin(CaseOrigin.IN_COUNTRY);
		caze.setAdditionalDetails("additional details");
		caze = getCaseFacade().saveCase(caze);

		ObjectMapper mapper = new ObjectMapper();
		JsonNode caseJson = mapper.convertValue(caze, JsonNode.class);
		//check if the reference person uuid is the same in the converted json
		CaseOrigin actualCaseOrigin = Enum.valueOf(CaseOrigin.class, caseJson.get("caseOrigin").textValue());
		String actualAdditionalDetails = caseJson.get("additionalDetails").textValue();
		assertEquals(CaseOrigin.IN_COUNTRY, actualCaseOrigin);
		assertEquals(caze.getAdditionalDetails(), actualAdditionalDetails);
		((ObjectNode) caseJson).putNull("caseOrigin");
		((ObjectNode) caseJson).putNull("additionalDetails");

		assertTrue(caseJson.get("caseOrigin").isNull());
		assertTrue(caseJson.get("additionalDetails").isNull());

		//remove from json some nodes 

		((ObjectNode) caseJson).remove("caseClassification");
		((ObjectNode) caseJson).remove("district");
		((ObjectNode) caseJson).remove("community");
		((ObjectNode) caseJson).remove("healthFacility");
		((ObjectNode) caseJson).remove("epiData");
		((ObjectNode) caseJson).remove("surveillanceOfficer");
		((ObjectNode) caseJson).remove("symptoms");

		assertNull(caseJson.get("caseClassification"));

		//call the partial update 
		CaseDataDto casePostUpdated = getCaseFacade().postUpdate(caze.getUuid(), caseJson);
		//check if the fields has been set to null  
		assertNull(casePostUpdated.getAdditionalDetails());
		assertNull(casePostUpdated.getCaseOrigin());
		//check if the fields that were not in the json file has not be deleted
		assertEquals(CaseClassification.PROBABLE, caze.getCaseClassification());
	}

	@Test
	public void testPatchingListElements() throws JsonProcessingException {

		TestDataCreator.RDCFEntities rdcf = creator.createRDCFEntities("Region", "District", "Community", "Facility");
		UserDto user = creator
			.createUser(rdcf.region.getUuid(), rdcf.district.getUuid(), rdcf.facility.getUuid(), "Surv", "Sup", UserRole.SURVEILLANCE_SUPERVISOR);

		PersonDto cazePerson = creator.createPerson("Case", "Person");
		CaseDataDto caze = creator.createCase(
			user.toReference(),
			cazePerson.toReference(),
			Disease.EVD,
			CaseClassification.PROBABLE,
			InvestigationStatus.PENDING,
			new Date(),
			rdcf);

		assertTrue(caze.getEpiData().getExposures().isEmpty());
		ExposureDto firstExposure = ExposureDto.build(ExposureType.TRAVEL);
		firstExposure.getLocation().setDetails("Ghana");
		firstExposure.setStartDate(new Date());
		firstExposure.setEndDate(new Date());
		caze.getEpiData().getExposures().add(firstExposure);

		ExposureDto secondExposure = ExposureDto.build(ExposureType.ANIMAL_CONTACT);
		secondExposure.getLocation().setDetails("Bat");
		secondExposure.setStartDate(new Date());
		secondExposure.setEndDate(new Date());
		caze.getEpiData().getExposures().add(secondExposure);

		ObjectMapper mapper = new ObjectMapper();
		JsonNode caseJson = mapper.convertValue(caze, JsonNode.class);
		EpiDataDto epiDataDto = mapper.treeToValue(caseJson.get("epiData"), EpiDataDto.class);
		assertEquals(epiDataDto, caze.getEpiData());

		CaseDataDto casePostUpdated = getCaseFacade().postUpdate(caze.getUuid(), caseJson);
		assertEquals(casePostUpdated.getEpiData(), caze.getEpiData());
		assertEquals(casePostUpdated.getEpiData().getExposures().size(), 2);
		assertEquals(casePostUpdated.getEpiData().getExposures(), casePostUpdated.getEpiData().getExposures());
		assertEquals(casePostUpdated.getEpiData().getExposures().get(0).getLocation().getDetails(), "Ghana");
		assertEquals(casePostUpdated.getEpiData().getExposures().get(1).getLocation().getDetails(), "Bat");

		//remove the second exposure 
		casePostUpdated.getEpiData().getExposures().remove(1);
		//modify the first exposure 
		casePostUpdated.getEpiData().getExposures().get(0).getLocation().setDetails("China");
		//add a third exposure 
		ExposureDto thirdExposure = ExposureDto.build(ExposureType.GATHERING);
		thirdExposure.getLocation().setDetails("Football match");
		thirdExposure.setStartDate(new Date());
		thirdExposure.setEndDate(new Date());
		casePostUpdated.getEpiData().getExposures().add(thirdExposure);

		JsonNode updatedCaseJson = mapper.convertValue(casePostUpdated, JsonNode.class);

		// assert the json reflects the changes 
		EpiDataDto updatedEpiDataDto = mapper.treeToValue(updatedCaseJson.get("epiData"), EpiDataDto.class);
		assertEquals(updatedEpiDataDto, casePostUpdated.getEpiData());

		CaseDataDto casePostUpdatedSecond = getCaseFacade().postUpdate(caze.getUuid(), updatedCaseJson);

		assertEquals(casePostUpdatedSecond.getEpiData(), casePostUpdated.getEpiData());
		assertEquals(casePostUpdatedSecond.getEpiData().getExposures().size(), 2);
		assertEquals(casePostUpdatedSecond.getEpiData().getExposures(), casePostUpdated.getEpiData().getExposures());
		assertEquals(casePostUpdatedSecond.getEpiData().getExposures().get(0).getLocation().getDetails(), "China");
		assertEquals(casePostUpdatedSecond.getEpiData().getExposures().get(1).getLocation().getDetails(), "Football match");
	}

}
