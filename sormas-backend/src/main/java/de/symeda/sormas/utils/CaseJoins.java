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

package de.symeda.sormas.utils;

import java.util.List;

import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;

import de.symeda.sormas.api.disease.DiseaseVariant;
import de.symeda.sormas.backend.caze.Case;
import de.symeda.sormas.backend.clinicalcourse.ClinicalCourse;
import de.symeda.sormas.backend.clinicalcourse.HealthConditions;
import de.symeda.sormas.backend.epidata.EpiData;
import de.symeda.sormas.backend.event.EventParticipant;
import de.symeda.sormas.backend.facility.Facility;
import de.symeda.sormas.backend.hospitalization.Hospitalization;
import de.symeda.sormas.backend.infrastructure.PointOfEntry;
import de.symeda.sormas.backend.location.Location;
import de.symeda.sormas.backend.person.Person;
import de.symeda.sormas.backend.region.Community;
import de.symeda.sormas.backend.region.Country;
import de.symeda.sormas.backend.region.District;
import de.symeda.sormas.backend.region.Region;
import de.symeda.sormas.backend.sample.Sample;
import de.symeda.sormas.backend.share.ExternalShareInfo;
import de.symeda.sormas.backend.sormastosormas.share.shareinfo.SormasToSormasShareInfo;
import de.symeda.sormas.backend.symptoms.Symptoms;
import de.symeda.sormas.backend.user.User;
import de.symeda.sormas.backend.util.AbstractDomainObjectJoins;

public class CaseJoins<T> extends AbstractDomainObjectJoins<T, Case> {

	private Join<Case, Person> person;
	private Join<Case, Region> responsibleRegion;
	private Join<Case, District> responsibleDistrict;
	private Join<Case, Community> responsibleCommunity;
	private Join<Case, Region> region;
	private Join<Case, District> district;
	private Join<Case, Community> community;
	private Join<Case, Facility> facility;
	private Join<Case, PointOfEntry> pointOfEntry;
	private Join<Case, User> surveillanceOfficer;
	private Join<Person, Location> address;
	private Join<Case, User> reportingUser;
	private Join<Person, Location> personAddress;
	private Join<Location, Region> personAddressRegion;
	private Join<Location, District> personAddressDistrict;
	private Join<Location, Community> personAddressCommunity;
	private Join<Location, Facility> personAddressFacility;
	private Join<Person, Facility> occupationFacility;
	private Join<Case, Hospitalization> hospitalization;
	private Join<Case, EpiData> epiData;
	private Join<Case, Symptoms> symptoms;
	private Join<Case, ClinicalCourse> clinicalCourse;
	private Join<ClinicalCourse, HealthConditions> healthConditions;
	private Join<Case, EventParticipant> eventParticipants;
	private Join<Person, List<Location>> personAddresses;
	private Join<Case, Sample> samples;
	private Join<Person, Country> personBirthCountry;
	private Join<Person, Country> personCitizenship;
	private Join<Case, District> reportingDistrict;
	private Join<Case, DiseaseVariant> diseaseVariant;
	private Join<Case, SormasToSormasShareInfo> shareInfoCases;
	private Join<Case, ExternalShareInfo> externalShareInfo;

	public CaseJoins(From<T, Case> caze) {
		super(caze);
	}

	public Join<Case, Person> getPerson() {
		return getOrCreate(person, Case.PERSON, JoinType.LEFT, this::setPerson);
	}

	private void setPerson(Join<Case, Person> person) {
		this.person = person;
	}

	public Join<Case, Region> getResponsibleRegion() {
		return getOrCreate(responsibleRegion, Case.RESPONSIBLE_REGION, JoinType.LEFT, this::setResponsibleRegion);
	}

	private void setResponsibleRegion(Join<Case, Region> responsibleRegion) {
		this.responsibleRegion = responsibleRegion;
	}

	public Join<Case, District> getResponsibleDistrict() {
		return getOrCreate(responsibleDistrict, Case.RESPONSIBLE_DISTRICT, JoinType.LEFT, this::setResponsibleDistrict);
	}

	private void setResponsibleDistrict(Join<Case, District> responsibleDistrict) {
		this.responsibleDistrict = responsibleDistrict;
	}

	public Join<Case, Community> getResponsibleCommunity() {
		return getOrCreate(responsibleCommunity, Case.RESPONSIBLE_COMMUNITY, JoinType.LEFT, this::setResponsibleCommunity);
	}

	private void setResponsibleCommunity(Join<Case, Community> responsibleCommunity) {
		this.responsibleCommunity = responsibleCommunity;
	}

	public Join<Case, Region> getRegion() {
		return getOrCreate(region, Case.REGION, JoinType.LEFT, this::setRegion);
	}

	private void setRegion(Join<Case, Region> region) {
		this.region = region;
	}

	public Join<Case, District> getDistrict() {
		return getOrCreate(district, Case.DISTRICT, JoinType.LEFT, this::setDistrict);
	}

	public Join<Case, Community> getCommunity() {
		return getOrCreate(community, Case.COMMUNITY, JoinType.LEFT, this::setCommunity);
	}

	private void setCommunity(Join<Case, Community> community) {
		this.community = community;
	}

	private void setDistrict(Join<Case, District> district) {
		this.district = district;
	}

	public Join<Case, Facility> getFacility() {
		return getOrCreate(facility, Case.HEALTH_FACILITY, JoinType.LEFT, this::setFacility);
	}

	private void setFacility(Join<Case, Facility> facility) {
		this.facility = facility;
	}

	public Join<Case, PointOfEntry> getPointOfEntry() {
		return getOrCreate(pointOfEntry, Case.POINT_OF_ENTRY, JoinType.LEFT, this::setPointOfEntry);
	}

	private void setPointOfEntry(Join<Case, PointOfEntry> pointOfEntry) {
		this.pointOfEntry = pointOfEntry;
	}

	public Join<Case, User> getSurveillanceOfficer() {
		return getOrCreate(surveillanceOfficer, Case.SURVEILLANCE_OFFICER, JoinType.LEFT, this::setSurveillanceOfficer);
	}

	private void setSurveillanceOfficer(Join<Case, User> surveillanceOfficer) {
		this.surveillanceOfficer = surveillanceOfficer;
	}

	public Join<Person, Location> getAddress() {
		return getOrCreate(address, Person.ADDRESS, JoinType.LEFT, getPerson(), this::setAddress);
	}

	private void setAddress(Join<Person, Location> address) {
		this.address = address;
	}

	public Join<Case, User> getReportingUser() {
		return getOrCreate(reportingUser, Case.REPORTING_USER, JoinType.LEFT, this::setReportingUser);
	}

	private void setReportingUser(Join<Case, User> reportingUser) {
		this.reportingUser = reportingUser;
	}

	public Join<Person, Location> getPersonAddress() {
		return getOrCreate(personAddress, Person.ADDRESS, JoinType.LEFT, getPerson(), this::setPersonAddress);
	}

	private void setPersonAddress(Join<Person, Location> personAddress) {
		this.personAddress = personAddress;
	}

	public Join<Location, Region> getPersonAddressRegion() {
		return getOrCreate(personAddressRegion, Location.REGION, JoinType.LEFT, getPersonAddress(), this::setPersonAddressRegion);
	}

	private void setPersonAddressRegion(Join<Location, Region> personAddressRegion) {
		this.personAddressRegion = personAddressRegion;
	}

	public Join<Location, District> getPersonAddressDistrict() {
		return getOrCreate(personAddressDistrict, Location.DISTRICT, JoinType.LEFT, getPersonAddress(), this::setPersonAddressDistrict);
	}

	private void setPersonAddressDistrict(Join<Location, District> personAddressDistrict) {
		this.personAddressDistrict = personAddressDistrict;
	}

	public Join<Location, Community> getPersonAddressCommunity() {
		return getOrCreate(personAddressCommunity, Location.COMMUNITY, JoinType.LEFT, getPersonAddress(), this::setPersonAddressCommunity);
	}

	private void setPersonAddressCommunity(Join<Location, Community> personAddressCommunity) {
		this.personAddressCommunity = personAddressCommunity;
	}

	public Join<Location, Facility> getPersonAddressFacility() {
		return getOrCreate(personAddressFacility, Location.FACILITY, JoinType.LEFT, getAddress(), this::setPersonAddressFacility);
	}

	private void setPersonAddressFacility(Join<Location, Facility> personAddressFacility) {
		this.personAddressFacility = personAddressFacility;
	}

	public Join<Case, Hospitalization> getHospitalization() {
		return getOrCreate(hospitalization, Case.HOSPITALIZATION, JoinType.LEFT, this::setHospitalization);
	}

	private void setHospitalization(Join<Case, Hospitalization> hospitalization) {
		this.hospitalization = hospitalization;
	}

	public Join<Case, EpiData> getEpiData() {
		return getOrCreate(epiData, Case.EPI_DATA, JoinType.LEFT, this::setEpiData);
	}

	private void setEpiData(Join<Case, EpiData> epiData) {
		this.epiData = epiData;
	}

	public Join<Case, Symptoms> getSymptoms() {
		return getOrCreate(symptoms, Case.SYMPTOMS, JoinType.LEFT, this::setSymptoms);
	}

	private void setSymptoms(Join<Case, Symptoms> symptoms) {
		this.symptoms = symptoms;
	}

	public Join<Case, ClinicalCourse> getClinicalCourse() {
		return getOrCreate(clinicalCourse, Case.CLINICAL_COURSE, JoinType.LEFT, this::setClinicalCourse);
	}

	private void setClinicalCourse(Join<Case, ClinicalCourse> clinicalCourse) {
		this.clinicalCourse = clinicalCourse;
	}

	public Join<ClinicalCourse, HealthConditions> getHealthConditions() {
		return getOrCreate(healthConditions, ClinicalCourse.HEALTH_CONDITIONS, JoinType.LEFT, getClinicalCourse(), this::setHealthConditions);
	}

	private void setHealthConditions(Join<ClinicalCourse, HealthConditions> healthConditions) {
		this.healthConditions = healthConditions;
	}

	private void setEventParticipants(Join<Case, EventParticipant> eventParticipants) {
		this.eventParticipants = eventParticipants;
	}

	public Join<Case, EventParticipant> getEventParticipants() {
		return getOrCreate(eventParticipants, Case.EVENT_PARTICIPANTS, JoinType.LEFT, this::setEventParticipants);
	}

	public Join<Person, List<Location>> getPersonAddresses() {
		return getOrCreate(personAddresses, Person.ADDRESSES, JoinType.LEFT, getPerson(), this::setPersonAddresses);
	}

	private void setPersonAddresses(Join<Person, List<Location>> personAddresses) {
		this.personAddresses = personAddresses;
	}

	public Join<Case, Sample> getSamples() {
		return getOrCreate(samples, Case.SAMPLES, JoinType.LEFT, this::setSamples);
	}

	private void setSamples(Join<Case, Sample> samples) {
		this.samples = samples;
	}

	public Join<Person, Country> getPersonBirthCountry() {
		return getOrCreate(personBirthCountry, Person.BIRTH_COUNTRY, JoinType.LEFT, getPerson(), this::setPersonBirthCountry);
	}

	private void setPersonBirthCountry(Join<Person, Country> personBirthCountry) {
		this.personBirthCountry = personBirthCountry;
	}

	public Join<Person, Country> getPersonCitizenship() {
		return getOrCreate(personCitizenship, Person.CITIZENSHIP, JoinType.LEFT, getPerson(), this::setPersonCitizenship);
	}

	public void setPersonCitizenship(Join<Person, Country> personCitizenship) {
		this.personCitizenship = personCitizenship;
	}

	public Join<Case, District> getReportingDistrict() {
		return getOrCreate(reportingDistrict, Case.REPORTING_DISTRICT, JoinType.LEFT, this::setReportingDistrict);
	}

	private void setReportingDistrict(Join<Case, District> reportingDistrict) {
		this.reportingDistrict = reportingDistrict;
	}

	public Join<Case, SormasToSormasShareInfo> getShareInfoCases() {
		return getOrCreate(shareInfoCases, Case.SHARE_INFO_CASES, JoinType.LEFT, this::setShareInfoCases);
	}

	private void setShareInfoCases(Join<Case, SormasToSormasShareInfo> shareInfoCases) {
		this.shareInfoCases = shareInfoCases;
	}

	public Join<Case, ExternalShareInfo> getExternalShareInfo() {
		return getOrCreate(externalShareInfo, Case.EXTERNAL_SHARES, JoinType.LEFT, this::setExternalShareInfo);
	}

	private void setExternalShareInfo(Join<Case, ExternalShareInfo> externalShareInfo) {
		this.externalShareInfo = externalShareInfo;
	}
}
