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
package de.symeda.sormas.backend.contact;

import static de.symeda.sormas.api.EntityDto.COLUMN_LENGTH_BIG;
import static de.symeda.sormas.api.EntityDto.COLUMN_LENGTH_DEFAULT;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import de.symeda.auditlog.api.Audited;
import de.symeda.auditlog.api.AuditedIgnore;
import de.symeda.sormas.api.Disease;
import de.symeda.sormas.api.contact.ContactCategory;
import de.symeda.sormas.api.contact.ContactClassification;
import de.symeda.sormas.api.contact.ContactIdentificationSource;
import de.symeda.sormas.api.contact.ContactProximity;
import de.symeda.sormas.api.contact.ContactReferenceDto;
import de.symeda.sormas.api.contact.ContactRelation;
import de.symeda.sormas.api.contact.ContactStatus;
import de.symeda.sormas.api.contact.EndOfQuarantineReason;
import de.symeda.sormas.api.contact.FollowUpStatus;
import de.symeda.sormas.api.contact.QuarantineType;
import de.symeda.sormas.api.contact.TracingApp;
import de.symeda.sormas.api.externaldata.HasExternalData;
import de.symeda.sormas.api.utils.YesNoUnknown;
import de.symeda.sormas.backend.caze.Case;
import de.symeda.sormas.backend.clinicalcourse.HealthConditions;
import de.symeda.sormas.backend.common.CoreAdo;
import de.symeda.sormas.backend.epidata.EpiData;
import de.symeda.sormas.backend.person.Person;
import de.symeda.sormas.backend.region.Community;
import de.symeda.sormas.backend.region.District;
import de.symeda.sormas.backend.region.Region;
import de.symeda.sormas.backend.sample.Sample;
import de.symeda.sormas.backend.sormastosormas.entities.SormasToSormasEntity;
import de.symeda.sormas.backend.sormastosormas.origin.SormasToSormasOriginInfo;
import de.symeda.sormas.backend.sormastosormas.share.shareinfo.ShareInfoContact;
import de.symeda.sormas.backend.task.Task;
import de.symeda.sormas.backend.user.User;
import de.symeda.sormas.backend.vaccinationinfo.VaccinationInfo;
import de.symeda.sormas.backend.visit.Visit;

@Entity
@Audited
public class Contact extends CoreAdo implements SormasToSormasEntity, HasExternalData {

	private static final long serialVersionUID = -7764607075875188799L;

	public static final String TABLE_NAME = "contact";

	public static final String PERSON = "person";
	public static final String CAZE = "caze";
	public static final String REPORT_DATE_TIME = "reportDateTime";
	public static final String REPORTING_USER = "reportingUser";
	public static final String MULTI_DAY_CONTACT = "multiDayContact";
	public static final String FIRST_CONTACT_DATE = "firstContactDate";
	public static final String LAST_CONTACT_DATE = "lastContactDate";
	public static final String CONTACT_PROXIMITY = "contactProximity";
	public static final String CONTACT_CLASSIFICATION = "contactClassification";
	public static final String CONTACT_STATUS = "contactStatus";
	public static final String COMPLETENESS = "completeness";
	public static final String FOLLOW_UP_STATUS = "followUpStatus";
	public static final String FOLLOW_UP_COMMENT = "followUpComment";
	public static final String FOLLOW_UP_UNTIL = "followUpUntil";
	public static final String CONTACT_OFFICER = "contactOfficer";
	public static final String DESCRIPTION = "description";
	public static final String TASKS = "tasks";
	public static final String RELATION_TO_CASE = "relationToCase";
	public static final String RELATION_DESCRIPTION = "relationDescription";
	public static final String CONTACT_IDENTIFICATION_SOURCE = "contactIdentificationSource";
	public static final String CONTACT_IDENTIFICATION_SOURCE_DETAILS = "contactIdentificationSourceDetails";
	public static final String TRACING_APP = "tracingApp";
	public static final String TRACING_APP_DETAILS = "tracingAppDetails";
	public static final String RESULTING_CASE = "resultingCase";
	public static final String REPORT_LAT = "reportLat";
	public static final String REPORT_LON = "reportLon";
	public static final String REPORT_LAT_LON_ACCURACY = "reportLatLonAccuracy";
	public static final String EXTERNAL_ID = "externalID";
	public static final String EXTERNAL_TOKEN = "externalToken";
	public static final String INTERNAL_TOKEN = "internalToken";
	public static final String REGION = "region";
	public static final String DISTRICT = "district";
	public static final String COMMUNITY = "community";
	public static final String HIGH_PRIORITY = "highPriority";
	public static final String IMMUNOSUPPRESSIVE_THERAPY_BASIC_DISEASE = "immunosuppressiveTherapyBasicDisease";
	public static final String IMMUNOSUPPRESSIVE_THERAPY_BASIC_DISEASE_DETAILS = "immunosuppressiveTherapyBasicDiseaseDetails";
	public static final String CARE_FOR_PEOPLE_OVER_60 = "careForPeopleOver60";
	public static final String GENERAL_PRACTITIONER_DETAILS = "generalPracticionerDetails";
	public static final String QUARANTINE = "quarantine";
	public static final String QUARANTINE_TYPE_DETAILS = "quarantineTypeDetails";
	public static final String QUARANTINE_FROM = "quarantineFrom";
	public static final String QUARANTINE_TO = "quarantineTo";
	public static final String DISEASE = "disease";
	public static final String DISEASE_DETAILS = "diseaseDetails";
	public static final String CASE_ID_EXTERNAL_SYSTEM = "caseIdExternalSystem";
	public static final String CASE_OR_EVENT_INFORMATION = "caseOrEventInformation";
	public static final String CONTACT_PROXIMITY_DETAILS = "contactProximityDetails";
	public static final String CONTACT_CATEGORY = "contactCategory";
	public static final String OVERWRITE_FOLLOW_UP_UNTIL = "overwriteFollowUpUntil";
	public static final String QUARANTINE_HELP_NEEDED = "quarantineHelpNeeded";
	public static final String QUARANTINE_ORDERED_VERBALLY = "quarantineOrderedVerbally";
	public static final String QUARANTINE_ORDERED_OFFICIAL_DOCUMENT = "quarantineOrderedOfficialDocument";
	public static final String QUARANTINE_ORDERED_VERBALLY_DATE = "quarantineOrderedVerballyDate";
	public static final String QUARANTINE_ORDERED_OFFICIAL_DOCUMENT_DATE = "quarantineOrderedOfficialDocumentDate";
	public static final String QUARANTINE_HOME_POSSIBLE = "quarantineHomePossible";
	public static final String QUARANTINE_HOME_POSSIBLE_COMMENT = "quarantineHomePossibleComment";
	public static final String QUARANTINE_HOME_SUPPLY_ENSURED = "quarantineHomeSupplyEnsured";
	public static final String QUARANTINE_HOME_SUPPLY_ENSURED_COMMENT = "quarantineHomeSupplyEnsuredComment";
	public static final String QUARANTINE_EXTENDED = "quarantineExtended";
	public static final String QUARANTINE_REDUCED = "quarantineReduced";
	public static final String QUARANTINE_OFFICIAL_ORDER_SENT = "quarantineOfficialOrderSent";
	public static final String QUARANTINE_OFFICIAL_ORDER_SENT_DATE = "quarantineOfficialOrderSentDate";
	public static final String VISITS = "visits";
	public static final String ADDITIONAL_DETAILS = "additionalDetails";
	public static final String EPI_DATA = "epiData";
	public static final String HEALTH_CONDITIONS = "healthConditions";
	public static final String SHARE_INFO_CONTACTS = "shareInfoContacts";
	public static final String SORMAS_TO_SORMAS_ORIGIN_INFO = "sormasToSormasOriginInfo";
	public static final String RETURNING_TRAVELER = "returningTraveler";
	public static final String END_OF_QUARANTINE_REASON = "endOfQuarantineReason";
	public static final String END_OF_QUARANTINE_REASON_DETAILS = "endOfQuarantineReasonDetails";
	public static final String REPORTING_DISTRICT = "reportingDistrict";
	public static final String VACCINATION_INFO = "vaccinationInfo";
	public static final String FOLLOW_UP_STATUS_CHANGE_DATE = "followUpStatusChangeDate";
	public static final String FOLLOW_UP_STATUS_CHANGE_USER = "followUpStatusChangeUser";

	private Date reportDateTime;
	private User reportingUser;
	private Double reportLat;
	private Double reportLon;
	private Float reportLatLonAccuracy;

	private Region region;
	private District district;
	private Community community;

	private Person person;
	private Case caze;
	private Disease disease;
	private String diseaseDetails;
	private ContactRelation relationToCase;
	private String relationDescription;
	private boolean multiDayContact;
	private Date firstContactDate;
	private Date lastContactDate;
	private ContactIdentificationSource contactIdentificationSource;
	private String contactIdentificationSourceDetails;
	private TracingApp tracingApp;
	private String tracingAppDetails;
	private ContactProximity contactProximity;
	private ContactClassification contactClassification;
	private ContactStatus contactStatus;
	private FollowUpStatus followUpStatus;
	private String followUpComment;
	private Date followUpUntil;
	private boolean overwriteFollowUpUntil;
	private User contactOfficer;
	private String description;
	private String externalID;
	private String externalToken;
	private String internalToken;

	private Case resultingCase;
	private User resultingCaseUser;

	private boolean highPriority;
	private YesNoUnknown immunosuppressiveTherapyBasicDisease;
	private String immunosuppressiveTherapyBasicDiseaseDetails;
	private YesNoUnknown careForPeopleOver60;

	private QuarantineType quarantine;
	private String quarantineTypeDetails;
	private Date quarantineFrom;
	private Date quarantineTo;

	private String caseIdExternalSystem;
	private String caseOrEventInformation;

	private String contactProximityDetails;
	private ContactCategory contactCategory;

	private String quarantineHelpNeeded;
	private boolean quarantineOrderedVerbally;
	private boolean quarantineOrderedOfficialDocument;
	private Date quarantineOrderedVerballyDate;
	private Date quarantineOrderedOfficialDocumentDate;
	private YesNoUnknown quarantineHomePossible;
	private String quarantineHomePossibleComment;
	private YesNoUnknown quarantineHomeSupplyEnsured;
	private String quarantineHomeSupplyEnsuredComment;
	private boolean quarantineExtended;
	private boolean quarantineReduced;
	private boolean quarantineOfficialOrderSent;
	private Date quarantineOfficialOrderSentDate;

	private Float completeness;
	private String additionalDetails;
	private EpiData epiData;

	private List<Task> tasks;
	private Set<Sample> samples = new HashSet<>();
	private Set<Visit> visits = new HashSet<>();
	private HealthConditions healthConditions;
	private YesNoUnknown returningTraveler;
	private EndOfQuarantineReason endOfQuarantineReason;
	private String endOfQuarantineReasonDetails;

	private YesNoUnknown prohibitionToWork;
	private Date prohibitionToWorkFrom;
	private Date prohibitionToWorkUntil;

	private District reportingDistrict;

	private VaccinationInfo vaccinationInfo;
	private Date followUpStatusChangeDate;
	private User followUpStatusChangeUser;

	private SormasToSormasOriginInfo sormasToSormasOriginInfo;
	private List<ShareInfoContact> shareInfoContacts = new ArrayList<>(0);

	private Contact duplicateOf;

	@ManyToOne(cascade = {})
	@JoinColumn(nullable = false)
	public Person getPerson() {
		return person;
	}

	public void setPerson(Person person) {
		this.person = person;
	}

	@ManyToOne(cascade = {})
	@JoinColumn
	public Case getCaze() {
		return caze;
	}

	public void setCaze(Case caze) {
		this.caze = caze;
	}

	@Enumerated(EnumType.STRING)
	public Disease getDisease() {
		return disease;
	}

	public void setDisease(Disease disease) {
		this.disease = disease;
	}

	@Column(length = COLUMN_LENGTH_DEFAULT)
	public String getDiseaseDetails() {
		return diseaseDetails;
	}

	public void setDiseaseDetails(String diseaseDetails) {
		this.diseaseDetails = diseaseDetails;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(nullable = false)
	public Date getReportDateTime() {
		return reportDateTime;
	}

	public void setReportDateTime(Date reportDateTime) {
		this.reportDateTime = reportDateTime;
	}

	@ManyToOne(cascade = {})
	@JoinColumn(nullable = false)
	public User getReportingUser() {
		return reportingUser;
	}

	public void setReportingUser(User reportingUser) {
		this.reportingUser = reportingUser;
	}

	@Column(nullable = false)
	public boolean isMultiDayContact() {
		return multiDayContact;
	}

	public void setMultiDayContact(boolean multiDayContact) {
		this.multiDayContact = multiDayContact;
	}

	@Temporal(TemporalType.TIMESTAMP)
	public Date getFirstContactDate() {
		return firstContactDate;
	}

	public void setFirstContactDate(Date firstContactDate) {
		this.firstContactDate = firstContactDate;
	}

	@Temporal(TemporalType.TIMESTAMP)
	public Date getLastContactDate() {
		return lastContactDate;
	}

	public void setLastContactDate(Date lastContactDate) {
		this.lastContactDate = lastContactDate;
	}

	@Enumerated(EnumType.STRING)
	public ContactIdentificationSource getContactIdentificationSource() {
		return contactIdentificationSource;
	}

	public void setContactIdentificationSource(ContactIdentificationSource contactIdentificationSource) {
		this.contactIdentificationSource = contactIdentificationSource;
	}

	@Column(length = COLUMN_LENGTH_DEFAULT)
	public String getContactIdentificationSourceDetails() {
		return contactIdentificationSourceDetails;
	}

	public void setContactIdentificationSourceDetails(String contactIdentificationSourceDetails) {
		this.contactIdentificationSourceDetails = contactIdentificationSourceDetails;
	}

	@Enumerated(EnumType.STRING)
	public TracingApp getTracingApp() {
		return tracingApp;
	}

	public void setTracingApp(TracingApp tracingApp) {
		this.tracingApp = tracingApp;
	}

	@Column(length = COLUMN_LENGTH_DEFAULT)
	public String getTracingAppDetails() {
		return tracingAppDetails;
	}

	public void setTracingAppDetails(String tracingAppDetails) {
		this.tracingAppDetails = tracingAppDetails;
	}

	@Enumerated(EnumType.STRING)
	public ContactProximity getContactProximity() {
		return contactProximity;
	}

	public void setContactProximity(ContactProximity contactProximity) {
		this.contactProximity = contactProximity;
	}

	@Column(length = COLUMN_LENGTH_DEFAULT)
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@ManyToOne(cascade = {})
	@JoinColumn
	public User getContactOfficer() {
		return contactOfficer;
	}

	public void setContactOfficer(User contactOfficer) {
		this.contactOfficer = contactOfficer;
	}

	@Enumerated(EnumType.STRING)
	public FollowUpStatus getFollowUpStatus() {
		return followUpStatus;
	}

	public void setFollowUpStatus(FollowUpStatus followUpStatus) {
		this.followUpStatus = followUpStatus;
	}

	@Temporal(TemporalType.TIMESTAMP)
	public Date getFollowUpUntil() {
		return followUpUntil;
	}

	public void setFollowUpUntil(Date followUpUntil) {
		this.followUpUntil = followUpUntil;
	}

	@Enumerated(EnumType.STRING)
	public ContactClassification getContactClassification() {
		return contactClassification;
	}

	public void setContactClassification(ContactClassification contactClassification) {
		this.contactClassification = contactClassification;
	}

	@Enumerated(EnumType.STRING)
	public ContactStatus getContactStatus() {
		return contactStatus;
	}

	public void setContactStatus(ContactStatus contactStatus) {
		this.contactStatus = contactStatus;
	}

	@Enumerated(EnumType.STRING)
	public ContactRelation getRelationToCase() {
		return relationToCase;
	}

	public void setRelationToCase(ContactRelation relationToCase) {
		this.relationToCase = relationToCase;
	}

	@Column(length = COLUMN_LENGTH_DEFAULT)
	public String getRelationDescription() {
		return relationDescription;
	}

	public void setRelationDescription(String relationDescription) {
		this.relationDescription = relationDescription;
	}

	@ManyToOne(cascade = {})
	@JoinColumn
	public Case getResultingCase() {
		return resultingCase;
	}

	public void setResultingCase(Case resultingCase) {
		this.resultingCase = resultingCase;
	}

	@Override
	public String toString() {
		Person contactPerson = getPerson();
		return ContactReferenceDto.buildCaption(
			contactPerson.getFirstName(),
			contactPerson.getLastName(),
			getCaze() != null ? getCaze().getPerson().getFirstName() : null,
			getCaze() != null ? getCaze().getPerson().getLastName() : null,
			getUuid());
	}

	public ContactReferenceDto toReference() {
		Person contactPerson = getPerson();
		return new ContactReferenceDto(
			getUuid(),
			contactPerson.getFirstName(),
			contactPerson.getLastName(),
			getCaze() != null ? getCaze().getPerson().getFirstName() : null,
			getCaze() != null ? getCaze().getPerson().getLastName() : null);
	}

	@OneToMany(cascade = {}, mappedBy = Task.CONTACT)
	public List<Task> getTasks() {
		return tasks;
	}

	public void setTasks(List<Task> tasks) {
		this.tasks = tasks;
	}

	@AuditedIgnore
	@ManyToMany(mappedBy = Visit.CONTACTS, fetch = FetchType.LAZY)
	public Set<Visit> getVisits() {
		return visits;
	}

	public void setVisits(Set<Visit> visits) {
		this.visits = visits;
	}

	@OneToMany(mappedBy = Sample.ASSOCIATED_CONTACT, fetch = FetchType.LAZY)
	public Set<Sample> getSamples() {
		return samples;
	}

	public void setSamples(Set<Sample> samples) {
		this.samples = samples;
	}

	public Double getReportLat() {
		return reportLat;
	}

	public void setReportLat(Double reportLat) {
		this.reportLat = reportLat;
	}

	public Double getReportLon() {
		return reportLon;
	}

	public void setReportLon(Double reportLon) {
		this.reportLon = reportLon;
	}

	@Column(length = COLUMN_LENGTH_BIG)
	public String getFollowUpComment() {
		return followUpComment;
	}

	public void setFollowUpComment(String followUpComment) {
		this.followUpComment = followUpComment;
	}

	public Float getReportLatLonAccuracy() {
		return reportLatLonAccuracy;
	}

	public void setReportLatLonAccuracy(Float reportLatLonAccuracy) {
		this.reportLatLonAccuracy = reportLatLonAccuracy;
	}

	@ManyToOne(cascade = {})
	@JoinColumn(nullable = true)
	public User getResultingCaseUser() {
		return resultingCaseUser;
	}

	public void setResultingCaseUser(User resultingCaseUser) {
		this.resultingCaseUser = resultingCaseUser;
	}

	@Column(length = COLUMN_LENGTH_DEFAULT)
	public String getExternalID() {
		return externalID;
	}

	public void setExternalID(String externalID) {
		this.externalID = externalID;
	}

	/**
	 * Extra getter for externalID needed to comply with the HasExternalData interface
	 *
	 * @return the externalID
	 */
	@Transient
	public String getExternalId() {
		return externalID;
	}

	/**
	 * Extra setter for externalID needed to comply with the HasExternalData interface
	 * 
	 * @param externalId
	 *            the value to be set for externalID
	 */
	public void setExternalId(String externalId) {
		this.externalID = externalId;
	}

	@Column(length = COLUMN_LENGTH_DEFAULT)
	public String getExternalToken() {
		return externalToken;
	}

	public void setExternalToken(String externalToken) {
		this.externalToken = externalToken;
	}

	@Column(columnDefinition = "text")
	public String getInternalToken() {
		return internalToken;
	}

	public void setInternalToken(String internalToken) {
		this.internalToken = internalToken;
	}

	@ManyToOne(cascade = {})
	public Region getRegion() {
		return region;
	}

	public void setRegion(Region region) {
		this.region = region;
	}

	@ManyToOne(cascade = {})
	public District getDistrict() {
		return district;
	}

	public void setDistrict(District district) {
		this.district = district;
	}

	@ManyToOne(cascade = {})
	public Community getCommunity() {
		return community;
	}

	public void setCommunity(Community community) {
		this.community = community;
	}

	@Column
	public boolean isHighPriority() {
		return highPriority;
	}

	public void setHighPriority(boolean highPriority) {
		this.highPriority = highPriority;
	}

	@Enumerated(EnumType.STRING)
	public YesNoUnknown getImmunosuppressiveTherapyBasicDisease() {
		return immunosuppressiveTherapyBasicDisease;
	}

	public void setImmunosuppressiveTherapyBasicDisease(YesNoUnknown immunosuppressiveTherapyBasicDisease) {
		this.immunosuppressiveTherapyBasicDisease = immunosuppressiveTherapyBasicDisease;
	}

	@Column(length = COLUMN_LENGTH_DEFAULT)
	public String getImmunosuppressiveTherapyBasicDiseaseDetails() {
		return immunosuppressiveTherapyBasicDiseaseDetails;
	}

	public void setImmunosuppressiveTherapyBasicDiseaseDetails(String immunosuppressiveTherapyBasicDiseaseDetails) {
		this.immunosuppressiveTherapyBasicDiseaseDetails = immunosuppressiveTherapyBasicDiseaseDetails;
	}

	@Enumerated(EnumType.STRING)
	public YesNoUnknown getCareForPeopleOver60() {
		return careForPeopleOver60;
	}

	public void setCareForPeopleOver60(YesNoUnknown careForPeopleOver60) {
		this.careForPeopleOver60 = careForPeopleOver60;
	}

	@Enumerated(EnumType.STRING)
	public QuarantineType getQuarantine() {
		return quarantine;
	}

	public void setQuarantine(QuarantineType quarantine) {
		this.quarantine = quarantine;
	}

	@Column(length = COLUMN_LENGTH_DEFAULT)
	public String getquarantineTypeDetails() {
		return quarantineTypeDetails;
	}

	public void setQuarantineTypeDetails(String quarantineTypeDetails) {
		this.quarantineTypeDetails = quarantineTypeDetails;
	}

	@Temporal(TemporalType.TIMESTAMP)
	public Date getQuarantineFrom() {
		return quarantineFrom;
	}

	public void setQuarantineFrom(Date quarantineFrom) {
		this.quarantineFrom = quarantineFrom;
	}

	@Temporal(TemporalType.TIMESTAMP)
	public Date getQuarantineTo() {
		return quarantineTo;
	}

	public void setQuarantineTo(Date quarantineTo) {
		this.quarantineTo = quarantineTo;
	}

	@Column(length = COLUMN_LENGTH_DEFAULT)
	public String getCaseIdExternalSystem() {
		return caseIdExternalSystem;
	}

	public void setCaseIdExternalSystem(String caseIdExternalSystem) {
		this.caseIdExternalSystem = caseIdExternalSystem;
	}

	@Column(length = COLUMN_LENGTH_BIG)
	public String getCaseOrEventInformation() {
		return caseOrEventInformation;
	}

	public void setCaseOrEventInformation(String caseOrEventInformation) {
		this.caseOrEventInformation = caseOrEventInformation;
	}

	@Column
	public boolean isOverwriteFollowUpUntil() {
		return overwriteFollowUpUntil;
	}

	public void setOverwriteFollowUpUntil(boolean overwriteFollowUpUntil) {
		this.overwriteFollowUpUntil = overwriteFollowUpUntil;
	}

	@Column(length = COLUMN_LENGTH_DEFAULT)
	public String getContactProximityDetails() {
		return contactProximityDetails;
	}

	public void setContactProximityDetails(String contactProximityDetails) {
		this.contactProximityDetails = contactProximityDetails;
	}

	@Enumerated(EnumType.STRING)
	public ContactCategory getContactCategory() {
		return contactCategory;
	}

	public void setContactCategory(ContactCategory contactCategory) {
		this.contactCategory = contactCategory;
	}

	@Column(length = COLUMN_LENGTH_DEFAULT)
	public String getQuarantineHelpNeeded() {
		return quarantineHelpNeeded;
	}

	public void setQuarantineHelpNeeded(String quarantineHelpNeeded) {
		this.quarantineHelpNeeded = quarantineHelpNeeded;
	}

	@Column
	public boolean isQuarantineOrderedVerbally() {
		return quarantineOrderedVerbally;
	}

	public void setQuarantineOrderedVerbally(boolean quarantineOrderedVerbally) {
		this.quarantineOrderedVerbally = quarantineOrderedVerbally;
	}

	@Column
	public boolean isQuarantineOrderedOfficialDocument() {
		return quarantineOrderedOfficialDocument;
	}

	public void setQuarantineOrderedOfficialDocument(boolean quarantineOrderedOfficialDocument) {
		this.quarantineOrderedOfficialDocument = quarantineOrderedOfficialDocument;
	}

	@Temporal(TemporalType.TIMESTAMP)
	public Date getQuarantineOrderedVerballyDate() {
		return quarantineOrderedVerballyDate;
	}

	public void setQuarantineOrderedVerballyDate(Date quarantineOrderedVerballyDate) {
		this.quarantineOrderedVerballyDate = quarantineOrderedVerballyDate;
	}

	@Temporal(TemporalType.TIMESTAMP)
	public Date getQuarantineOrderedOfficialDocumentDate() {
		return quarantineOrderedOfficialDocumentDate;
	}

	public void setQuarantineOrderedOfficialDocumentDate(Date quarantineOrderedOfficialDocumentDate) {
		this.quarantineOrderedOfficialDocumentDate = quarantineOrderedOfficialDocumentDate;
	}

	@Enumerated(EnumType.STRING)
	public YesNoUnknown getQuarantineHomePossible() {
		return quarantineHomePossible;
	}

	public void setQuarantineHomePossible(YesNoUnknown quarantineHomePossible) {
		this.quarantineHomePossible = quarantineHomePossible;
	}

	@Column(length = COLUMN_LENGTH_DEFAULT)
	public String getQuarantineHomePossibleComment() {
		return quarantineHomePossibleComment;
	}

	public void setQuarantineHomePossibleComment(String quarantineHomePossibleComment) {
		this.quarantineHomePossibleComment = quarantineHomePossibleComment;
	}

	@Enumerated(EnumType.STRING)
	public YesNoUnknown getQuarantineHomeSupplyEnsured() {
		return quarantineHomeSupplyEnsured;
	}

	public void setQuarantineHomeSupplyEnsured(YesNoUnknown quarantineHomeSupplyEnsured) {
		this.quarantineHomeSupplyEnsured = quarantineHomeSupplyEnsured;
	}

	@Column(length = COLUMN_LENGTH_DEFAULT)
	public String getQuarantineHomeSupplyEnsuredComment() {
		return quarantineHomeSupplyEnsuredComment;
	}

	public void setQuarantineHomeSupplyEnsuredComment(String quarantineHomeSupplyEnsuredComment) {
		this.quarantineHomeSupplyEnsuredComment = quarantineHomeSupplyEnsuredComment;
	}

	@Column
	public boolean isQuarantineExtended() {
		return quarantineExtended;
	}

	public void setQuarantineExtended(boolean quarantineExtended) {
		this.quarantineExtended = quarantineExtended;
	}

	@Column
	public boolean isQuarantineReduced() {
		return quarantineReduced;
	}

	public void setQuarantineReduced(boolean quarantineReduced) {
		this.quarantineReduced = quarantineReduced;
	}

	@Column
	public boolean isQuarantineOfficialOrderSent() {
		return quarantineOfficialOrderSent;
	}

	public void setQuarantineOfficialOrderSent(boolean quarantineOfficialOrderSent) {
		this.quarantineOfficialOrderSent = quarantineOfficialOrderSent;
	}

	@Temporal(TemporalType.TIMESTAMP)
	public Date getQuarantineOfficialOrderSentDate() {
		return quarantineOfficialOrderSentDate;
	}

	public void setQuarantineOfficialOrderSentDate(Date quarantineOfficialOrderSentDate) {
		this.quarantineOfficialOrderSentDate = quarantineOfficialOrderSentDate;
	}

	public Float getCompleteness() {
		return completeness;
	}

	public void setCompleteness(Float completeness) {
		this.completeness = completeness;
	}

	@Column(length = COLUMN_LENGTH_BIG)
	public String getAdditionalDetails() {
		return additionalDetails;
	}

	public void setAdditionalDetails(String additionalDetails) {
		this.additionalDetails = additionalDetails;
	}

	// It's necessary to do a lazy fetch here because having three eager fetching
	// one to one relations
	// produces an error where two non-xa connections are opened
	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@AuditedIgnore
	public EpiData getEpiData() {
		if (epiData == null) {
			epiData = new EpiData();
		}
		return epiData;
	}

	public void setEpiData(EpiData epiData) {
		this.epiData = epiData;
	}

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@AuditedIgnore
	public HealthConditions getHealthConditions() {
		return healthConditions;
	}

	public void setHealthConditions(HealthConditions healthConditions) {
		this.healthConditions = healthConditions;
	}

	@Override
	@ManyToOne(cascade = CascadeType.ALL)
	@AuditedIgnore
	public SormasToSormasOriginInfo getSormasToSormasOriginInfo() {
		return sormasToSormasOriginInfo;
	}

	@Override
	public void setSormasToSormasOriginInfo(SormasToSormasOriginInfo originInfo) {
		this.sormasToSormasOriginInfo = originInfo;
	}

	@OneToMany(mappedBy = ShareInfoContact.CONTACT, fetch = FetchType.LAZY)
	public List<ShareInfoContact> getShareInfoContacts() {
		return shareInfoContacts;
	}

	public void setShareInfoContacts(List<ShareInfoContact> shareInfoContacts) {
		this.shareInfoContacts = shareInfoContacts;
	}

	@Enumerated(EnumType.STRING)
	public EndOfQuarantineReason getEndOfQuarantineReason() {
		return endOfQuarantineReason;
	}

	public void setEndOfQuarantineReason(EndOfQuarantineReason endOfQuarantineReason) {
		this.endOfQuarantineReason = endOfQuarantineReason;
	}

	@Column(length = COLUMN_LENGTH_DEFAULT)
	public String getEndOfQuarantineReasonDetails() {
		return endOfQuarantineReasonDetails;
	}

	public void setEndOfQuarantineReasonDetails(String endOfQuarantineReasonDetails) {
		this.endOfQuarantineReasonDetails = endOfQuarantineReasonDetails;
	}

	@Enumerated(EnumType.STRING)
	public YesNoUnknown getProhibitionToWork() {
		return prohibitionToWork;
	}

	public void setProhibitionToWork(YesNoUnknown prohibitionToWork) {
		this.prohibitionToWork = prohibitionToWork;
	}

	@Temporal(TemporalType.DATE)
	public Date getProhibitionToWorkFrom() {
		return prohibitionToWorkFrom;
	}

	public void setProhibitionToWorkFrom(Date prohibitionToWorkFrom) {
		this.prohibitionToWorkFrom = prohibitionToWorkFrom;
	}

	@Temporal(TemporalType.DATE)
	public Date getProhibitionToWorkUntil() {
		return prohibitionToWorkUntil;
	}

	public void setProhibitionToWorkUntil(Date prohibitionToWorkUntil) {
		this.prohibitionToWorkUntil = prohibitionToWorkUntil;
	}

	@ManyToOne
	public District getReportingDistrict() {
		return reportingDistrict;
	}

	public void setReportingDistrict(District reportingDistrict) {
		this.reportingDistrict = reportingDistrict;
	}

	@Enumerated(EnumType.STRING)
	public YesNoUnknown getReturningTraveler() {
		return returningTraveler;
	}

	public void setReturningTraveler(YesNoUnknown returningTraveler) {
		this.returningTraveler = returningTraveler;
	}

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@AuditedIgnore
	public VaccinationInfo getVaccinationInfo() {
		return vaccinationInfo;
	}

	public void setVaccinationInfo(VaccinationInfo vaccinationInfo) {
		this.vaccinationInfo = vaccinationInfo;
	}

	@Temporal(TemporalType.DATE)
	public Date getFollowUpStatusChangeDate() {
		return followUpStatusChangeDate;
	}

	public void setFollowUpStatusChangeDate(Date followUpStatusChangeDate) {
		this.followUpStatusChangeDate = followUpStatusChangeDate;
	}

	@ManyToOne(cascade = {})
	public User getFollowUpStatusChangeUser() {
		return followUpStatusChangeUser;
	}

	public void setFollowUpStatusChangeUser(User followUpStatusChangeUser) {
		this.followUpStatusChangeUser = followUpStatusChangeUser;
	}

	@OneToOne(cascade = {}, fetch = FetchType.LAZY)
	@AuditedIgnore
	public Contact getDuplicateOf() {
		return duplicateOf;
	}

	public void setDuplicateOf(Contact duplicateOf) {
		this.duplicateOf = duplicateOf;
	}
}
