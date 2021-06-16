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

package de.symeda.sormas.backend.contact;

import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;

import de.symeda.sormas.backend.caze.Case;
import de.symeda.sormas.backend.clinicalcourse.HealthConditions;
import de.symeda.sormas.backend.epidata.EpiData;
import de.symeda.sormas.backend.event.Event;
import de.symeda.sormas.backend.event.EventParticipant;
import de.symeda.sormas.backend.facility.Facility;
import de.symeda.sormas.backend.infrastructure.PointOfEntry;
import de.symeda.sormas.backend.location.Location;
import de.symeda.sormas.backend.person.Person;
import de.symeda.sormas.backend.region.Community;
import de.symeda.sormas.backend.region.Country;
import de.symeda.sormas.backend.region.District;
import de.symeda.sormas.backend.region.Region;
import de.symeda.sormas.backend.sormastosormas.share.shareinfo.SormasToSormasShareInfo;
import de.symeda.sormas.backend.symptoms.Symptoms;
import de.symeda.sormas.backend.user.User;
import de.symeda.sormas.backend.util.AbstractDomainObjectJoins;
import de.symeda.sormas.backend.vaccinationinfo.VaccinationInfo;
import de.symeda.sormas.backend.visit.Visit;

public class ContactJoins<T> extends AbstractDomainObjectJoins<T, Contact> {

	private Join<Contact, Person> person;
//	private CaseJoins<Contact> caseJoins;
	private Join<Contact, Case> caze;
	private Join<Contact, Case> resultingCase;
	private Join<Case, Person> casePerson;
	private Join<Case, User> caseReportingUser;
	private Join<Case, Region> caseResponsibleRegion;
	private Join<Case, District> caseResponsibleDistrict;
	private Join<Case, Community> caseResponsibleCommunity;
	private Join<Case, Region> caseRegion;
	private Join<Case, District> caseDistrict;
	private Join<Case, Community> caseCommunity;
	private Join<Case, Facility> caseHealthFacility;
	private Join<Case, PointOfEntry> caseasePointOfEntry;
	private Join<Contact, User> contactOfficer;
	private Join<Person, Location> address;
	private Join<Contact, Region> region;
	private Join<Contact, District> district;
	private Join<Contact, Community> community;
	private Join<Contact, User> reportingUser;
	private Join<Location, Region> addressRegion;
	private Join<Location, District> addressDistrict;
	private Join<Location, Community> addressCommunity;
	private Join<Location, Facility> addressFacility;
	private Join<Person, Facility> occupationFacility;
	private Join<Contact, EpiData> epiData;
	private Join<Person, EventParticipant> eventParticipants;
	private Join<Case, EventParticipant> caseEventParticipants;
	private Join<EventParticipant, Event> event;
	private Join<EventParticipant, Event> caseEvent;

	private Join<Contact, Visit> visits;
	private Join<Visit, Symptoms> visitSymptoms;
	private Join<Contact, HealthConditions> healthConditions;
	private Join<Person, Location> personAddress;

	private Join<Person, Country> personBirthCountry;
	private Join<Person, Country> personCitizenship;

	private Join<Contact, District> reportingDistrict;

	private Join<Contact, VaccinationInfo> vaccinationInfo;

	private Join<Contact, SormasToSormasShareInfo> shareInfoContacts;

	public ContactJoins(From<T, Contact> contact) {
		super(contact);

//		this.caseJoins = new CaseJoins<>(contact.join(Contact.CAZE));
	}

	public Join<Contact, Person> getPerson() {
		return getOrCreate(person, Contact.PERSON, JoinType.LEFT, this::setPerson);
	}

	private void setPerson(Join<Contact, Person> person) {
		this.person = person;
	}

	public Join<Contact, Case> getCaze() {
		return getOrCreate(caze, Contact.CAZE, JoinType.LEFT, this::setCaze);
	}

	private void setCaze(Join<Contact, Case> caze) {
		this.caze = caze;
	}

	public Join<Contact, Case> getResultingCase() {
		return getOrCreate(resultingCase, Contact.RESULTING_CASE, JoinType.LEFT, this::setResultingCase);
	}

	private void setResultingCase(Join<Contact, Case> resultingCase) {
		this.resultingCase = resultingCase;
	}

	public Join<Case, Person> getCasePerson() {
		return getOrCreate(casePerson, Case.PERSON, JoinType.LEFT, getCaze(), this::setCasePerson);
	}

	private void setCasePerson(Join<Case, Person> casePerson) {
		this.casePerson = casePerson;
	}

	public Join<Case, User> getCaseReportingUser() {
		return getOrCreate(caseReportingUser, Case.REPORTING_USER, JoinType.LEFT, getCaze(), this::setCaseReportingUser);
	}

	private void setCaseReportingUser(Join<Case, User> caseReportingUser) {
		this.caseReportingUser = caseReportingUser;
	}

	public Join<Case, Region> getCaseResponsibleRegion() {
		return getOrCreate(caseResponsibleRegion, Case.RESPONSIBLE_REGION, JoinType.LEFT, getCaze(), this::setCaseResponsibleRegion);
	}

	private void setCaseResponsibleRegion(Join<Case, Region> caseResponsibleRegion) {
		this.caseResponsibleRegion = caseResponsibleRegion;
	}

	public Join<Case, District> getCaseResponsibleDistrict() {
		return getOrCreate(caseResponsibleDistrict, Case.RESPONSIBLE_DISTRICT, JoinType.LEFT, getCaze(), this::setCaseResponsibleDistrict);
	}

	private void setCaseResponsibleDistrict(Join<Case, District> caseResponsibleDistrict) {
		this.caseResponsibleDistrict = caseResponsibleDistrict;
	}

	public Join<Case, Community> getCaseResponsibleCommunity() {
		return getOrCreate(caseResponsibleCommunity, Case.RESPONSIBLE_COMMUNITY, JoinType.LEFT, getCaze(), this::setCaseResponsibleCommunity);
	}

	private void setCaseResponsibleCommunity(Join<Case, Community> caseResponsibleCommunity) {
		this.caseResponsibleCommunity = caseResponsibleCommunity;
	}

	public Join<Case, Region> getCaseRegion() {
		return getOrCreate(caseRegion, Case.REGION, JoinType.LEFT, getCaze(), this::setCaseRegion);
	}

	private void setCaseRegion(Join<Case, Region> caseRegion) {
		this.caseRegion = caseRegion;
	}

	public Join<Case, District> getCaseDistrict() {
		return getOrCreate(caseDistrict, Case.DISTRICT, JoinType.LEFT, getCaze(), this::setCaseDistrict);
	}

	private void setCaseDistrict(Join<Case, District> caseDistrict) {
		this.caseDistrict = caseDistrict;
	}

	public Join<Case, Community> getCaseCommunity() {
		return getOrCreate(caseCommunity, Case.COMMUNITY, JoinType.LEFT, getCaze(), this::setCaseCommunity);
	}

	private void setCaseCommunity(Join<Case, Community> caseCommunity) {
		this.caseCommunity = caseCommunity;
	}

	public Join<Case, Facility> getCaseHealthFacility() {
		return getOrCreate(caseHealthFacility, Case.HEALTH_FACILITY, JoinType.LEFT, getCaze(), this::setCaseHealthFacility);
	}

	private void setCaseHealthFacility(Join<Case, Facility> caseHealthFacility) {
		this.caseHealthFacility = caseHealthFacility;
	}

	public Join<Case, PointOfEntry> getCaseasePointOfEntry() {
		return getOrCreate(caseasePointOfEntry, Case.POINT_OF_ENTRY, JoinType.LEFT, getCaze(), this::setCaseasePointOfEntry);
	}

	private void setCaseasePointOfEntry(Join<Case, PointOfEntry> caseasePointOfEntry) {
		this.caseasePointOfEntry = caseasePointOfEntry;
	}

	public Join<Contact, User> getContactOfficer() {
		return getOrCreate(contactOfficer, Contact.CONTACT_OFFICER, JoinType.LEFT, this::setContactOfficer);
	}

	private void setContactOfficer(Join<Contact, User> contactOfficer) {
		this.contactOfficer = contactOfficer;
	}

	public Join<Person, Location> getAddress() {
		return getOrCreate(address, Person.ADDRESS, JoinType.LEFT, getPerson(), this::setAddress);
	}

	private void setAddress(Join<Person, Location> address) {
		this.address = address;
	}

	public Join<Contact, Region> getRegion() {
		return getOrCreate(region, Contact.REGION, JoinType.LEFT, this::setRegion);
	}

	private void setRegion(Join<Contact, Region> region) {
		this.region = region;
	}

	public Join<Contact, District> getDistrict() {
		return getOrCreate(district, Contact.DISTRICT, JoinType.LEFT, this::setDistrict);
	}

	private void setDistrict(Join<Contact, District> district) {
		this.district = district;
	}

	public Join<Contact, Community> getCommunity() {
		return getOrCreate(community, Contact.COMMUNITY, JoinType.LEFT, this::setCommunity);
	}

	private void setCommunity(Join<Contact, Community> community) {
		this.community = community;
	}

	public Join<Contact, User> getReportingUser() {
		return getOrCreate(reportingUser, Contact.REPORTING_USER, JoinType.LEFT, this::setReportingUser);
	}

	private void setReportingUser(Join<Contact, User> reportingUser) {
		this.reportingUser = reportingUser;
	}

	public Join<Location, Region> getAddressRegion() {
		return getOrCreate(addressRegion, Location.REGION, JoinType.LEFT, getAddress(), this::setAddressRegion);
	}

	private void setAddressRegion(Join<Location, Region> addressRegion) {
		this.addressRegion = addressRegion;
	}

	public Join<Location, District> getAddressDistrict() {
		return getOrCreate(addressDistrict, Location.DISTRICT, JoinType.LEFT, getAddress(), this::setAddressDistrict);
	}

	private void setAddressDistrict(Join<Location, District> addressDistrict) {
		this.addressDistrict = addressDistrict;
	}

	public Join<Location, Community> getAddressCommunity() {
		return getOrCreate(addressCommunity, Location.COMMUNITY, JoinType.LEFT, getAddress(), this::setAddressCommunity);
	}

	private void setAddressCommunity(Join<Location, Community> addressCommunity) {
		this.addressCommunity = addressCommunity;
	}

	public Join<Location, Facility> getAddressFacility() {
		return getOrCreate(addressFacility, Location.FACILITY, JoinType.LEFT, getAddress(), this::setAddressFacility);
	}

	private void setAddressFacility(Join<Location, Facility> addressFacility) {
		this.addressFacility = addressFacility;
	}

	public Join<Contact, EpiData> getEpiData() {
		return getOrCreate(epiData, Contact.EPI_DATA, JoinType.LEFT, this::setEpiData);
	}

	private void setEpiData(Join<Contact, EpiData> epiData) {
		this.epiData = epiData;
	}

	public Join<Contact, Visit> getVisits() {
		return getOrCreate(visits, Contact.VISITS, JoinType.LEFT, this::setVisits);
	}

	private void setVisits(Join<Contact, Visit> visits) {
		this.visits = visits;
	}

	public Join<Visit, Symptoms> getVisitSymptoms() {
		return getOrCreate(visitSymptoms, Visit.SYMPTOMS, JoinType.LEFT, getVisits(), this::setVisitSymptoms);
	}

	private void setVisitSymptoms(Join<Visit, Symptoms> visitSymptoms) {
		this.visitSymptoms = visitSymptoms;
	}

	public Join<Contact, HealthConditions> getHealthConditions() {
		return healthConditions;
	}

	public void setHealthConditions(Join<Contact, HealthConditions> healthConditions) {
		this.healthConditions = healthConditions;
	}

	public Join<Person, Location> getPersonAddress() {
		return getOrCreate(personAddress, Person.ADDRESS, JoinType.LEFT, getPerson(), this::setPersonAddress);
	}

	private void setPersonAddress(Join<Person, Location> personAddress) {
		this.personAddress = personAddress;
	}

	private void setEventParticipants(Join<Person, EventParticipant> eventParticipants) {
		this.eventParticipants = eventParticipants;
	}

	public Join<Person, EventParticipant> getEventParticipants() {
		return getOrCreate(eventParticipants, Person.EVENT_PARTICIPANTS, JoinType.LEFT, getPerson(), this::setEventParticipants);
	}

	private void setCaseEventParticipants(Join<Case, EventParticipant> caseEventParticipants) {
		this.caseEventParticipants = caseEventParticipants;
	}

	public Join<Case, EventParticipant> getCaseEventParticipants() {
		return getOrCreate(caseEventParticipants, Case.EVENT_PARTICIPANTS, JoinType.LEFT, getCaze(), this::setCaseEventParticipants);
	}

	private void setEvent(Join<EventParticipant, Event> event) {
		this.event = event;
	}

	public Join<EventParticipant, Event> getEvent() {
		return getOrCreate(event, EventParticipant.EVENT, JoinType.LEFT, getEventParticipants(), this::setEvent);
	}

	private void setCaseEvent(Join<EventParticipant, Event> caseEvent) {
		this.caseEvent = caseEvent;
	}

	public Join<EventParticipant, Event> getCaseEvent() {
		return getOrCreate(caseEvent, EventParticipant.EVENT, JoinType.LEFT, getCaseEventParticipants(), this::setCaseEvent);
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

	public Join<Contact, District> getReportingDistrict() {
		return getOrCreate(reportingDistrict, Contact.REPORTING_DISTRICT, JoinType.LEFT, this::setReportingDistrict);
	}

	private void setReportingDistrict(Join<Contact, District> reportingDistrict) {
		this.reportingDistrict = reportingDistrict;
	}

	public Join<Contact, VaccinationInfo> getVaccinationInfo() {
		return getOrCreate(vaccinationInfo, Contact.VACCINATION_INFO, JoinType.LEFT, this::setVaccinationInfo);
	}

	private void setVaccinationInfo(Join<Contact, VaccinationInfo> vaccinationInfo) {
		this.vaccinationInfo = vaccinationInfo;
	}

	public Join<Contact, SormasToSormasShareInfo> getShareInfoContacts() {
		return getOrCreate(shareInfoContacts, Contact.SHARE_INFO_CONTACTS, JoinType.LEFT, this::setShareInfoContacts);
	}

	public void setShareInfoContacts(Join<Contact, SormasToSormasShareInfo> shareInfoContacts) {
		this.shareInfoContacts = shareInfoContacts;
	}
}
