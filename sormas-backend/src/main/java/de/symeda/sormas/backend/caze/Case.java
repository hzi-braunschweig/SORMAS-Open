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
package de.symeda.sormas.backend.caze;

import static de.symeda.sormas.api.EntityDto.COLUMN_LENGTH_BIG;
import static de.symeda.sormas.api.EntityDto.COLUMN_LENGTH_DEFAULT;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import de.symeda.auditlog.api.Audited;
import de.symeda.auditlog.api.AuditedIgnore;
import de.symeda.sormas.api.Disease;
import de.symeda.sormas.api.caze.CaseClassification;
import de.symeda.sormas.api.caze.CaseIdentificationSource;
import de.symeda.sormas.api.caze.CaseOrigin;
import de.symeda.sormas.api.caze.CaseOutcome;
import de.symeda.sormas.api.caze.CaseReferenceDto;
import de.symeda.sormas.api.caze.ContactTracingContactType;
import de.symeda.sormas.api.caze.DengueFeverType;
import de.symeda.sormas.api.caze.EndOfIsolationReason;
import de.symeda.sormas.api.caze.HospitalWardType;
import de.symeda.sormas.api.caze.InfectionSetting;
import de.symeda.sormas.api.caze.InvestigationStatus;
import de.symeda.sormas.api.caze.PlagueType;
import de.symeda.sormas.api.caze.QuarantineReason;
import de.symeda.sormas.api.caze.RabiesType;
import de.symeda.sormas.api.caze.ScreeningType;
import de.symeda.sormas.api.caze.Trimester;
import de.symeda.sormas.api.caze.Vaccination;
import de.symeda.sormas.api.caze.VaccinationInfoSource;
import de.symeda.sormas.api.caze.Vaccine;
import de.symeda.sormas.api.caze.VaccineManufacturer;
import de.symeda.sormas.api.contact.FollowUpStatus;
import de.symeda.sormas.api.contact.QuarantineType;
import de.symeda.sormas.api.disease.DiseaseVariant;
import de.symeda.sormas.api.externaldata.HasExternalData;
import de.symeda.sormas.api.facility.FacilityType;
import de.symeda.sormas.api.utils.PersonalData;
import de.symeda.sormas.api.utils.YesNoUnknown;
import de.symeda.sormas.backend.caze.maternalhistory.MaternalHistory;
import de.symeda.sormas.backend.caze.porthealthinfo.PortHealthInfo;
import de.symeda.sormas.backend.clinicalcourse.ClinicalCourse;
import de.symeda.sormas.backend.common.CoreAdo;
import de.symeda.sormas.backend.contact.Contact;
import de.symeda.sormas.backend.disease.DiseaseVariantConverter;
import de.symeda.sormas.backend.epidata.EpiData;
import de.symeda.sormas.backend.event.EventParticipant;
import de.symeda.sormas.backend.facility.Facility;
import de.symeda.sormas.backend.hospitalization.Hospitalization;
import de.symeda.sormas.backend.infrastructure.PointOfEntry;
import de.symeda.sormas.backend.person.Person;
import de.symeda.sormas.backend.region.Community;
import de.symeda.sormas.backend.region.District;
import de.symeda.sormas.backend.region.Region;
import de.symeda.sormas.backend.sample.Sample;
import de.symeda.sormas.backend.share.ExternalShareInfo;
import de.symeda.sormas.backend.sormastosormas.entities.SormasToSormasEntity;
import de.symeda.sormas.backend.sormastosormas.origin.SormasToSormasOriginInfo;
import de.symeda.sormas.backend.sormastosormas.share.shareinfo.ShareInfoCase;
import de.symeda.sormas.backend.symptoms.Symptoms;
import de.symeda.sormas.backend.task.Task;
import de.symeda.sormas.backend.therapy.Therapy;
import de.symeda.sormas.backend.user.User;
import de.symeda.sormas.backend.visit.Visit;

@Entity(name = "cases")
@Audited
public class Case extends CoreAdo implements SormasToSormasEntity, HasExternalData {

	private static final long serialVersionUID = -2697795184663562129L;

	public static final String TABLE_NAME = "cases";

	public static final String CASE_CLASSIFICATION = "caseClassification";
	public static final String CASE_IDENTIFICATION_SOURCE = "caseIdentificationSource";
	public static final String SCREENING_TYPE = "screeningType";
	public static final String CLINICAL_CONFIRMATION = "clinicalConfirmation";
	public static final String EPIDEMIOLOGICAL_CONFIRMATION = "epidemiologicalConfirmation";
	public static final String LABORATORY_DIAGNOSTIC_CONFIRMATION = "laboratoryDiagnosticConfirmation";
	public static final String SYSTEM_CASE_CLASSIFICATION = "systemCaseClassification";
	public static final String INVESTIGATION_STATUS = "investigationStatus";
	public static final String PERSON = "person";
	public static final String DISEASE = "disease";
	public static final String DISEASE_VARIANT = "diseaseVariant";
	public static final String DISEASE_DETAILS = "diseaseDetails";
	public static final String PLAGUE_TYPE = "plagueType";
	public static final String RABIES_TYPE = "rabiesType";
	public static final String HEALTH_FACILITY = "healthFacility";
	public static final String HEALTH_FACILITY_DETAILS = "healthFacilityDetails";
	public static final String REPORTING_USER = "reportingUser";
	public static final String REPORT_DATE = "reportDate";
	public static final String INVESTIGATED_DATE = "investigatedDate";
	public static final String DISTRICT_LEVEL_DATE = "districtLevelDate";
	public static final String SURVEILLANCE_OFFICER = "surveillanceOfficer";
	public static final String CASE_OFFICER = "caseOfficer";
	public static final String SYMPTOMS = "symptoms";
	public static final String TASKS = "tasks";
	public static final String RESPONSIBLE_REGION = "responsibleRegion";
	public static final String RESPONSIBLE_DISTRICT = "responsibleDistrict";
	public static final String RESPONSIBLE_COMMUNITY = "responsibleCommunity";
	public static final String REGION = "region";
	public static final String DISTRICT = "district";
	public static final String COMMUNITY = "community";
	public static final String HOSPITALIZATION = "hospitalization";
	public static final String EPI_DATA = "epiData";
	public static final String CLINICAL_COURSE = "clinicalCourse";
	public static final String MATERNAL_HISTORY = "maternalHistory";
	public static final String PORT_HEALTH_INFO = "portHealthInfo";
	public static final String PREGNANT = "pregnant";
	public static final String VACCINATION = "vaccination";
	public static final String VACCINATION_DOSES = "vaccinationDoses";
	public static final String VACCINATION_INFO_SOURCE = "vaccinationInfoSource";
	public static final String FIRST_VACCINATION_DATE = "firstVaccinationDate";
	public static final String LAST_VACCINATION_DATE = "lastVaccinationDate";
	public static final String VACCINE_NAME = "vaccineName";
	public static final String OTHER_VACCINE_NAME = "otherVaccineName";
	public static final String VACCINE_MANUFACTURER = "vaccineManufacturer";
	public static final String OTHER_VACCINE_MANUFACTURER = "otherVaccineManufacturer";
	public static final String VACCINE_INN = "vaccineInn";
	public static final String VACCINE_BATCH_NUMBER = "vaccineBatchNumber";
	public static final String VACCINE_UNII_CODE = "vaccineUniiCode";
	public static final String VACCINE_ATC_CODE = "vaccineAtcCode";
	public static final String WHICH_VACCINE = "whichVaccine";
	public static final String SMALLPOX_VACCINATION_SCAR = "smallpoxVaccinationScar";
	public static final String EPID_NUMBER = "epidNumber";
	public static final String REPORT_LAT = "reportLat";
	public static final String REPORT_LON = "reportLon";
	public static final String OUTCOME = "outcome";
	public static final String OUTCOME_DATE = "outcomeDate";
	public static final String SEQUELAE = "sequelae";
	public static final String SEQUELAE_DETAILS = "sequelaeDetails";
	public static final String CASE_AGE = "caseAge";
	public static final String ARCHIVED = "archived";
	public static final String THERAPY = "therapy";
	public static final String CLINICIAN_DETAILS = "clinicianDetails";
	public static final String CASE_ORIGIN = "caseOrigin";
	public static final String POINT_OF_ENTRY = "pointOfEntry";
	public static final String POINT_OF_ENTRY_DETAILS = "pointOfEntryDetails";
	public static final String COMPLETENESS = "completeness";
	public static final String ADDITIONAL_DETAILS = "additionalDetails";
	public static final String EXTERNAL_ID = "externalID";
	public static final String EXTERNAL_TOKEN = "externalToken";
	public static final String INTERNAL_TOKEN = "internalToken";
	public static final String SHARED_TO_COUNTRY = "sharedToCountry";
	public static final String NOSOCOMIAL_OUTBREAK = "nosocomialOutbreak";
	public static final String INFECTION_SETTING = "infectionSetting";
	public static final String QUARANTINE = "quarantine";
	public static final String QUARANTINE_TYPE_DETAILS = "quarantineTypeDetails";
	public static final String QUARANTINE_FROM = "quarantineFrom";
	public static final String QUARANTINE_TO = "quarantineTo";
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
	public static final String POSTPARTUM = "postpartum";
	public static final String TRIMESTER = "trimester";
	public static final String SAMPLES = "samples";
	public static final String FOLLOW_UP_STATUS = "followUpStatus";
	public static final String FOLLOW_UP_COMMENT = "followUpComment";
	public static final String FOLLOW_UP_UNTIL = "followUpUntil";
	public static final String OVERWRITE_FOLLOW_UP_UNTIL = "overwriteFollowUpUntil";
	public static final String VISITS = "visits";
	public static final String FACILITY_TYPE = "facilityType";
	public static final String CONVERTED_FROM_CONTACT = "convertedContact";
	public static final String EVENT_PARTICIPANTS = "eventParticipants";
	public static final String SHARE_INFO_CASES = "shareInfoCases";
	public static final String SORMAS_TO_SORMAS_ORIGIN_INFO = "sormasToSormasOriginInfo";
	public static final String EXTERNAL_SHARES = "externalShares";

	public static final String CASE_ID_ISM = "caseIdIsm";
	public static final String CONTACT_TRACING_FIRST_CONTACT_DATE = "contactTracingFirstContactDate";
	public static final String WAS_IN_QUARANTINE_BEFORE_ISOLATION = "wasInQuarantineBeforeIsolation";
	public static final String QUARANTINE_REASON_BEFORE_ISOLATION = "quarantineReasonBeforeIsolation";
	public static final String QUARANTINE_REASON_BEFORE_ISOLATION_DETAILS = "quarantineReasonBeforeIsolationDetails";
	public static final String END_OF_ISOLATION_REASON = "endOfIsolationReason";
	public static final String END_OF_ISOLATION_REASON_DETAILS = "endOfIsolationReasonDetails";

	public static final String RE_INFECTION = "reInfection";
	public static final String PREVIOUS_INFECTION_DATE = "previousInfectionDate";

	public static final String REPORTING_DISTRICT = "reportingDistrict";
	public static final String BLOOD_ORGAN_OR_TISSUE_DONATED = "bloodOrganOrTissueDonated";
	public static final String NOT_A_CASE_REASON_NEGATIVE_TEST = "notACaseReasonNegativeTest";
	public static final String NOT_A_CASE_REASON_PHYSICIAN_INFORMATION = "notACaseReasonPhysicianInformation";
	public static final String NOT_A_CASE_REASON_DIFFERENT_PATHOGEN = "notACaseReasonDifferentPathogen";
	public static final String NOT_A_CASE_REASON_OTHER = "notACaseReasonOther";
	public static final String NOT_A_CASE_REASON_DETAILS = "notACaseReasonDetails";
	public static final String FOLLOW_UP_STATUS_CHANGE_DATE = "followUpStatusChangeDate";
	public static final String FOLLOW_UP_STATUS_CHANGE_USER = "followUpStatusChangeUser";
	public static final String DONT_SHARE_WITH_REPORTING_TOOL = "dontShareWithReportingTool";

	private Person person;
	private String description;
	private Disease disease;
	private DiseaseVariant diseaseVariant;
	private String diseaseDetails;
	private PlagueType plagueType;
	private DengueFeverType dengueFeverType;
	private RabiesType rabiesType;

	private CaseClassification caseClassification;
	private CaseClassification systemCaseClassification;
	private CaseIdentificationSource caseIdentificationSource;
	private ScreeningType screeningType;
	private User classificationUser;
	private Date classificationDate;
	private String classificationComment;

	private YesNoUnknown clinicalConfirmation;
	private YesNoUnknown epidemiologicalConfirmation;
	private YesNoUnknown laboratoryDiagnosticConfirmation;

	private InvestigationStatus investigationStatus;
	private Hospitalization hospitalization;
	private EpiData epiData;
	private Therapy therapy;
	private ClinicalCourse clinicalCourse;
	private MaternalHistory maternalHistory;
	private PortHealthInfo portHealthInfo;

	private Region responsibleRegion;
	private District responsibleDistrict;
	private Community responsibleCommunity;

	private Region region;
	private District district;
	private Community community;
	private FacilityType facilityType;
	private Facility healthFacility;
	private String healthFacilityDetails;

	private User reportingUser;
	private Date reportDate;
	private Double reportLat;
	private Double reportLon;
	private Float reportLatLonAccuracy;

	private Date investigatedDate;
	private Date regionLevelDate;
	private Date nationalLevelDate;
	private Date districtLevelDate;

	private User surveillanceOfficer;
	private String clinicianName;
	private String clinicianPhone;
	private String clinicianEmail;
	private User caseOfficer;

	private HospitalWardType notifyingClinic;
	private String notifyingClinicDetails;

	private Symptoms symptoms;

	private YesNoUnknown pregnant;

	private Vaccination vaccination;
	private String vaccinationDoses;
	private VaccinationInfoSource vaccinationInfoSource;
	private Date firstVaccinationDate;
	private Date lastVaccinationDate;
	private Vaccine vaccineName;
	private String otherVaccineName;
	private VaccineManufacturer vaccineManufacturer;
	private String otherVaccineManufacturer;
	private String vaccineInn;
	private String vaccineBatchNumber;
	private String vaccineUniiCode;
	private String vaccineAtcCode;
	private String vaccine;
	private YesNoUnknown smallpoxVaccinationScar;
	private YesNoUnknown smallpoxVaccinationReceived;

	private String epidNumber;

	private CaseOutcome outcome;
	private Date outcomeDate;
	private YesNoUnknown sequelae;
	private String sequelaeDetails;

	private Integer caseAge;

	private boolean archived;
	private String creationVersion;
	private Case duplicateOf;

	private CaseOrigin caseOrigin;
	@PersonalData
	private PointOfEntry pointOfEntry;
	private String pointOfEntryDetails;

	private Float completeness;
	private String additionalDetails;
	private String externalID;
	private String externalToken;
	private String internalToken;
	private boolean sharedToCountry;

	private QuarantineType quarantine;
	private String quarantineTypeDetails;
	private Date quarantineFrom;
	private Date quarantineTo;
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

	private FollowUpStatus followUpStatus;
	private String followUpComment;
	private Date followUpUntil;
	private boolean overwriteFollowUpUntil;

	private YesNoUnknown postpartum;
	private Trimester trimester;

	private List<Task> tasks;
	private Set<Sample> samples = new HashSet<>();
	private Set<Visit> visits = new HashSet<>();
	private Set<EventParticipant> eventParticipants;
	private List<Contact> convertedContact;

	private Integer caseIdIsm;
	private ContactTracingContactType contactTracingFirstContactType;
	private Date contactTracingFirstContactDate;
	private YesNoUnknown wasInQuarantineBeforeIsolation;
	private QuarantineReason quarantineReasonBeforeIsolation;
	private String quarantineReasonBeforeIsolationDetails;
	private EndOfIsolationReason endOfIsolationReason;
	private String endOfIsolationReasonDetails;

	private boolean nosocomialOutbreak;
	private InfectionSetting infectionSetting;

	private YesNoUnknown prohibitionToWork;
	private Date prohibitionToWorkFrom;
	private Date prohibitionToWorkUntil;

	private YesNoUnknown reInfection;
	private Date previousInfectionDate;

	private District reportingDistrict;

	private boolean notACaseReasonNegativeTest;
	private boolean notACaseReasonPhysicianInformation;
	private boolean notACaseReasonDifferentPathogen;
	private boolean notACaseReasonOther;
	private String notACaseReasonDetails;
	/**
	 * Blood/organ/tissue donation in the last 6 months
	 */
	private YesNoUnknown bloodOrganOrTissueDonated;
	private Date followUpStatusChangeDate;
	private User followUpStatusChangeUser;

	private boolean dontShareWithReportingTool;

	private SormasToSormasOriginInfo sormasToSormasOriginInfo;
	private List<ShareInfoCase> shareInfoCases = new ArrayList<>(0);
	private List<ExternalShareInfo> externalShares = new ArrayList<>(0);

	@ManyToOne(cascade = {})
	@JoinColumn(nullable = false)
	public Person getPerson() {
		return person;
	}

	public void setPerson(Person person) {
		this.person = person;
	}

	@Column(length = COLUMN_LENGTH_BIG)
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Enumerated(EnumType.STRING)
	public Disease getDisease() {
		return disease;
	}

	public void setDisease(Disease disease) {
		this.disease = disease;
	}

	@Column
	@Convert(converter = DiseaseVariantConverter.class)
	public DiseaseVariant getDiseaseVariant() {
		return diseaseVariant;
	}

	public void setDiseaseVariant(DiseaseVariant diseaseVariant) {
		this.diseaseVariant = diseaseVariant;
	}

	@Column(length = COLUMN_LENGTH_DEFAULT)
	public String getDiseaseDetails() {
		return diseaseDetails;
	}

	public void setDiseaseDetails(String diseaseDetails) {
		this.diseaseDetails = diseaseDetails;
	}

	@Enumerated(EnumType.STRING)
	public PlagueType getPlagueType() {
		return plagueType;
	}

	public void setPlagueType(PlagueType plagueType) {
		this.plagueType = plagueType;
	}

	@Enumerated(EnumType.STRING)
	public DengueFeverType getDengueFeverType() {
		return dengueFeverType;
	}

	public void setDengueFeverType(DengueFeverType dengueFeverType) {
		this.dengueFeverType = dengueFeverType;
	}

	@Enumerated(EnumType.STRING)
	public RabiesType getRabiesType() {
		return rabiesType;
	}

	public void setRabiesType(RabiesType rabiesType) {
		this.rabiesType = rabiesType;
	}

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	public CaseClassification getCaseClassification() {
		return caseClassification;
	}

	public void setCaseClassification(CaseClassification caseStatus) {
		this.caseClassification = caseStatus;
	}

	@Enumerated(EnumType.STRING)
	public CaseIdentificationSource getCaseIdentificationSource() {
		return caseIdentificationSource;
	}

	public void setCaseIdentificationSource(CaseIdentificationSource caseIdentificationSource) {
		this.caseIdentificationSource = caseIdentificationSource;
	}

	@Enumerated(EnumType.STRING)
	public ScreeningType getScreeningType() {
		return screeningType;
	}

	public void setScreeningType(ScreeningType screeningType) {
		this.screeningType = screeningType;
	}

	@ManyToOne(cascade = {})
	@JoinColumn(nullable = true)
	public User getClassificationUser() {
		return classificationUser;
	}

	public void setClassificationUser(User classificationUser) {
		this.classificationUser = classificationUser;
	}

	@Temporal(TemporalType.TIMESTAMP)
	public Date getClassificationDate() {
		return classificationDate;
	}

	public void setClassificationDate(Date classificationDate) {
		this.classificationDate = classificationDate;
	}

	@Column(length = COLUMN_LENGTH_DEFAULT)
	public String getClassificationComment() {
		return classificationComment;
	}

	public void setClassificationComment(String classificationComment) {
		this.classificationComment = classificationComment;
	}

	@Enumerated(EnumType.STRING)
	public YesNoUnknown getClinicalConfirmation() {
		return clinicalConfirmation;
	}

	public void setClinicalConfirmation(YesNoUnknown clinicalConfirmation) {
		this.clinicalConfirmation = clinicalConfirmation;
	}

	@Enumerated(EnumType.STRING)
	public YesNoUnknown getEpidemiologicalConfirmation() {
		return epidemiologicalConfirmation;
	}

	public void setEpidemiologicalConfirmation(YesNoUnknown epidemiologicalConfirmation) {
		this.epidemiologicalConfirmation = epidemiologicalConfirmation;
	}

	@Enumerated(EnumType.STRING)
	public YesNoUnknown getLaboratoryDiagnosticConfirmation() {
		return laboratoryDiagnosticConfirmation;
	}

	public void setLaboratoryDiagnosticConfirmation(YesNoUnknown laboratoryDiagnosticConfirmation) {
		this.laboratoryDiagnosticConfirmation = laboratoryDiagnosticConfirmation;
	}

	@ManyToOne(cascade = {})
	@JoinColumn(nullable = false)
	public User getReportingUser() {
		return reportingUser;
	}

	public void setReportingUser(User reportingUser) {
		this.reportingUser = reportingUser;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(nullable = false)
	public Date getReportDate() {
		return reportDate;
	}

	public void setReportDate(Date reportDate) {
		this.reportDate = reportDate;
	}

	@Temporal(TemporalType.TIMESTAMP)
	public Date getInvestigatedDate() {
		return investigatedDate;
	}

	public void setInvestigatedDate(Date investigatedDate) {
		this.investigatedDate = investigatedDate;
	}

	@Temporal(TemporalType.TIMESTAMP)
	public Date getRegionLevelDate() {
		return regionLevelDate;
	}

	public void setRegionLevelDate(Date regionLevelDate) {
		this.regionLevelDate = regionLevelDate;
	}

	@Temporal(TemporalType.TIMESTAMP)
	public Date getNationalLevelDate() {
		return nationalLevelDate;
	}

	public void setNationalLevelDate(Date nationalLevelDate) {
		this.nationalLevelDate = nationalLevelDate;
	}

	@Temporal(TemporalType.TIMESTAMP)
	public Date getDistrictLevelDate() {
		return districtLevelDate;
	}

	public void setDistrictLevelDate(Date districtLevelDate) {
		this.districtLevelDate = districtLevelDate;
	}

	@ManyToOne(cascade = {})
	public Facility getHealthFacility() {
		return healthFacility;
	}

	public void setHealthFacility(Facility healthFacility) {
		this.healthFacility = healthFacility;
	}

	@Column(length = COLUMN_LENGTH_DEFAULT)
	public String getHealthFacilityDetails() {
		return healthFacilityDetails;
	}

	public void setHealthFacilityDetails(String healthFacilityDetails) {
		this.healthFacilityDetails = healthFacilityDetails;
	}

	@ManyToOne(cascade = {})
	public User getSurveillanceOfficer() {
		return surveillanceOfficer;
	}

	public void setSurveillanceOfficer(User surveillanceOfficer) {
		this.surveillanceOfficer = surveillanceOfficer;
	}

	@Column(length = COLUMN_LENGTH_DEFAULT, name = "cliniciandetails")
	public String getClinicianName() {
		return clinicianName;
	}

	public void setClinicianName(String clinicianName) {
		this.clinicianName = clinicianName;
	}

	@Column(length = COLUMN_LENGTH_DEFAULT)
	public String getClinicianPhone() {
		return clinicianPhone;
	}

	public void setClinicianPhone(String clinicianPhone) {
		this.clinicianPhone = clinicianPhone;
	}

	@Column(length = COLUMN_LENGTH_DEFAULT)
	public String getClinicianEmail() {
		return clinicianEmail;
	}

	public void setClinicianEmail(String clinicianEmail) {
		this.clinicianEmail = clinicianEmail;
	}

	@ManyToOne(cascade = {})
	public User getCaseOfficer() {
		return caseOfficer;
	}

	public void setCaseOfficer(User caseOfficer) {
		this.caseOfficer = caseOfficer;
	}

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@AuditedIgnore
	public Symptoms getSymptoms() {
		if (symptoms == null) {
			symptoms = new Symptoms();
		}
		return symptoms;
	}

	public void setSymptoms(Symptoms symptoms) {
		this.symptoms = symptoms;
	}

	@ManyToOne(cascade = {})
	public Region getResponsibleRegion() {
		return responsibleRegion;
	}

	public void setResponsibleRegion(Region responsibleRegion) {
		this.responsibleRegion = responsibleRegion;
	}

	@ManyToOne(cascade = {})
	public District getResponsibleDistrict() {
		return responsibleDistrict;
	}

	public void setResponsibleDistrict(District responsibleDistrict) {
		this.responsibleDistrict = responsibleDistrict;
	}

	@ManyToOne(cascade = {})
	public Community getResponsibleCommunity() {
		return responsibleCommunity;
	}

	public void setResponsibleCommunity(Community responsibleCommunity) {
		this.responsibleCommunity = responsibleCommunity;
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

	// It's necessary to do a lazy fetch here because having three eager fetching
	// one to one relations
	// produces an error where two non-xa connections are opened
	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@AuditedIgnore
	public Hospitalization getHospitalization() {
		if (hospitalization == null) {
			hospitalization = new Hospitalization();
		}
		return hospitalization;
	}

	public void setHospitalization(Hospitalization hospitalization) {
		this.hospitalization = hospitalization;
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

	// It's necessary to do a lazy fetch here because having three eager fetching
	// one to one relations
	// produces an error where two non-xa connections are opened
	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@AuditedIgnore
	public Therapy getTherapy() {
		return therapy;
	}

	public void setTherapy(Therapy therapy) {
		this.therapy = therapy;
	}

	// It's necessary to do a lazy fetch here because having three eager fetching
	// one to one relations
	// produces an error where two non-xa connections are opened
	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@AuditedIgnore
	public ClinicalCourse getClinicalCourse() {
		return clinicalCourse;
	}

	public void setClinicalCourse(ClinicalCourse clinicalCourse) {
		this.clinicalCourse = clinicalCourse;
	}

	// It's necessary to do a lazy fetch here because having three eager fetching
	// one to one relations
	// produces an error where two non-xa connections are opened
	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@AuditedIgnore
	public MaternalHistory getMaternalHistory() {
		return maternalHistory;
	}

	public void setMaternalHistory(MaternalHistory maternalHistory) {
		this.maternalHistory = maternalHistory;
	}

	// It's necessary to do a lazy fetch here because having three eager fetching
	// one to one relations produces an error where two non-xa connections are opened
	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@AuditedIgnore
	public PortHealthInfo getPortHealthInfo() {
		return portHealthInfo;
	}

	public void setPortHealthInfo(PortHealthInfo portHealthInfo) {
		this.portHealthInfo = portHealthInfo;
	}

	@OneToMany(mappedBy = Contact.RESULTING_CASE, fetch = FetchType.LAZY)
	public List<Contact> getConvertedContact() {
		return convertedContact;
	}

	public void setConvertedContact(List<Contact> convertedContact) {
		this.convertedContact = convertedContact;
	}

	@Enumerated(EnumType.STRING)
	public YesNoUnknown getPregnant() {
		return pregnant;
	}

	public void setPregnant(YesNoUnknown pregnant) {
		this.pregnant = pregnant;
	}

	@Enumerated(EnumType.STRING)
	public Vaccination getVaccination() {
		return vaccination;
	}

	public void setVaccination(Vaccination vaccination) {
		this.vaccination = vaccination;
	}

	@Column(length = COLUMN_LENGTH_DEFAULT)
	public String getVaccinationDoses() {
		return vaccinationDoses;
	}

	public void setVaccinationDoses(String vaccinationDoses) {
		this.vaccinationDoses = vaccinationDoses;
	}

	@Enumerated(EnumType.STRING)
	public VaccinationInfoSource getVaccinationInfoSource() {
		return vaccinationInfoSource;
	}

	public void setVaccinationInfoSource(VaccinationInfoSource vaccinationInfoSource) {
		this.vaccinationInfoSource = vaccinationInfoSource;
	}

	@Column(length = COLUMN_LENGTH_DEFAULT)
	public String getVaccine() {
		return vaccine;
	}

	public void setVaccine(String vaccine) {
		this.vaccine = vaccine;
	}

	@Enumerated(EnumType.STRING)
	public YesNoUnknown getSmallpoxVaccinationScar() {
		return smallpoxVaccinationScar;
	}

	public void setSmallpoxVaccinationScar(YesNoUnknown smallpoxVaccinationScar) {
		this.smallpoxVaccinationScar = smallpoxVaccinationScar;
	}

	@Enumerated(EnumType.STRING)
	public YesNoUnknown getSmallpoxVaccinationReceived() {
		return smallpoxVaccinationReceived;
	}

	public void setSmallpoxVaccinationReceived(YesNoUnknown smallpoxVaccinationReceived) {
		this.smallpoxVaccinationReceived = smallpoxVaccinationReceived;
	}

	@Temporal(TemporalType.TIMESTAMP)
	public Date getFirstVaccinationDate() {
		return firstVaccinationDate;
	}

	public void setFirstVaccinationDate(Date firstVaccinationDate) {
		this.firstVaccinationDate = firstVaccinationDate;
	}

	@Temporal(TemporalType.TIMESTAMP)
	public Date getLastVaccinationDate() {
		return lastVaccinationDate;
	}

	public void setLastVaccinationDate(Date vaccinationDate) {
		this.lastVaccinationDate = vaccinationDate;
	}

	@Enumerated(EnumType.STRING)
	public Vaccine getVaccineName() {
		return vaccineName;
	}

	public void setVaccineName(Vaccine vaccineName) {
		this.vaccineName = vaccineName;
	}

	@Column(columnDefinition = "text")
	public String getOtherVaccineName() {
		return otherVaccineName;
	}

	public void setOtherVaccineName(String otherVaccineName) {
		this.otherVaccineName = otherVaccineName;
	}

	@Enumerated(EnumType.STRING)
	public VaccineManufacturer getVaccineManufacturer() {
		return vaccineManufacturer;
	}

	public void setVaccineManufacturer(VaccineManufacturer vaccineManufacturer) {
		this.vaccineManufacturer = vaccineManufacturer;
	}

	@Column(columnDefinition = "text")
	public String getOtherVaccineManufacturer() {
		return otherVaccineManufacturer;
	}

	public void setOtherVaccineManufacturer(String otherVaccineManufacturer) {
		this.otherVaccineManufacturer = otherVaccineManufacturer;
	}

	@Column(columnDefinition = "text")
	public String getVaccineInn() {
		return vaccineInn;
	}

	public void setVaccineInn(String vaccineInn) {
		this.vaccineInn = vaccineInn;
	}

	@Column(columnDefinition = "text")
	public String getVaccineBatchNumber() {
		return vaccineBatchNumber;
	}

	public void setVaccineBatchNumber(String vaccineBatchNumber) {
		this.vaccineBatchNumber = vaccineBatchNumber;
	}

	@Column(columnDefinition = "text")
	public String getVaccineUniiCode() {
		return vaccineUniiCode;
	}

	public void setVaccineUniiCode(String vaccineUniiCode) {
		this.vaccineUniiCode = vaccineUniiCode;
	}

	@Column(columnDefinition = "text")
	public String getVaccineAtcCode() {
		return vaccineAtcCode;
	}

	public void setVaccineAtcCode(String vaccineAtcCode) {
		this.vaccineAtcCode = vaccineAtcCode;
	}

	@Column(length = COLUMN_LENGTH_DEFAULT)
	public String getEpidNumber() {
		return epidNumber;
	}

	public void setEpidNumber(String epidNumber) {
		this.epidNumber = epidNumber;
	}

	@Override
	public String toString() {
		//TODO lga how to pseudonymize
		return CaseReferenceDto.buildCaption(getUuid(), person.getFirstName(), person.getLastName());
	}

	public CaseReferenceDto toReference() {
		return new CaseReferenceDto(getUuid(), person.getFirstName(), person.getLastName());
	}

	@OneToMany(cascade = {}, mappedBy = Task.CAZE)
	public List<Task> getTasks() {
		return tasks;
	}

	public void setTasks(List<Task> tasks) {
		this.tasks = tasks;
	}

	@AuditedIgnore
	@OneToMany(mappedBy = Visit.CAZE, fetch = FetchType.LAZY)
	public Set<Visit> getVisits() {
		return visits;
	}

	public void setVisits(Set<Visit> visits) {
		this.visits = visits;
	}

	@OneToMany(mappedBy = Sample.ASSOCIATED_CASE, fetch = FetchType.LAZY)
	public Set<Sample> getSamples() {
		return samples;
	}

	public void setSamples(Set<Sample> samples) {
		this.samples = samples;
	}

	@OneToMany(cascade = {}, mappedBy = EventParticipant.RESULTING_CASE)
	public Set<EventParticipant> getEventParticipants() {
		return eventParticipants;
	}

	public void setEventParticipants(Set<EventParticipant> eventParticipants) {
		this.eventParticipants = eventParticipants;
	}

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	public InvestigationStatus getInvestigationStatus() {
		return investigationStatus;
	}

	public void setInvestigationStatus(InvestigationStatus investigationStatus) {
		this.investigationStatus = investigationStatus;
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

	public Float getReportLatLonAccuracy() {
		return reportLatLonAccuracy;
	}

	public void setReportLatLonAccuracy(Float reportLatLonAccuracy) {
		this.reportLatLonAccuracy = reportLatLonAccuracy;
	}

	@Enumerated(EnumType.STRING)
	public CaseOutcome getOutcome() {
		return outcome;
	}

	public void setOutcome(CaseOutcome outcome) {
		this.outcome = outcome;
	}

	@Temporal(TemporalType.TIMESTAMP)
	public Date getOutcomeDate() {
		return outcomeDate;
	}

	public void setOutcomeDate(Date outcomeDate) {
		this.outcomeDate = outcomeDate;
	}

	@Enumerated(EnumType.STRING)
	public YesNoUnknown getSequelae() {
		return sequelae;
	}

	public void setSequelae(YesNoUnknown sequelae) {
		this.sequelae = sequelae;
	}

	@Column(length = COLUMN_LENGTH_DEFAULT)
	public String getSequelaeDetails() {
		return sequelaeDetails;
	}

	public void setSequelaeDetails(String sequelaeDetails) {
		this.sequelaeDetails = sequelaeDetails;
	}

	@Enumerated(EnumType.STRING)
	public HospitalWardType getNotifyingClinic() {
		return notifyingClinic;
	}

	public void setNotifyingClinic(HospitalWardType notifyingClinic) {
		this.notifyingClinic = notifyingClinic;
	}

	@Column(length = COLUMN_LENGTH_DEFAULT)
	public String getNotifyingClinicDetails() {
		return notifyingClinicDetails;
	}

	public void setNotifyingClinicDetails(String notifyingClinicDetails) {
		this.notifyingClinicDetails = notifyingClinicDetails;
	}

	public Integer getCaseAge() {
		return caseAge;
	}

	public void setCaseAge(Integer caseAge) {
		this.caseAge = caseAge;
	}

	@Column
	public boolean isArchived() {
		return archived;
	}

	public void setArchived(boolean archived) {
		this.archived = archived;
	}

	@Column(length = 32)
	public String getCreationVersion() {
		return creationVersion;
	}

	public void setCreationVersion(String creationVersion) {
		this.creationVersion = creationVersion;
	}

	@OneToOne(cascade = {}, fetch = FetchType.LAZY)
	@AuditedIgnore
	public Case getDuplicateOf() {
		return duplicateOf;
	}

	public void setDuplicateOf(Case duplicateOf) {
		this.duplicateOf = duplicateOf;
	}

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	public CaseClassification getSystemCaseClassification() {
		return systemCaseClassification;
	}

	public void setSystemCaseClassification(CaseClassification systemCaseClassification) {
		this.systemCaseClassification = systemCaseClassification;
	}

	@Enumerated(EnumType.STRING)
	public CaseOrigin getCaseOrigin() {
		return caseOrigin;
	}

	public void setCaseOrigin(CaseOrigin caseOrigin) {
		this.caseOrigin = caseOrigin;
	}

	@ManyToOne(cascade = {})
	public PointOfEntry getPointOfEntry() {
		return pointOfEntry;
	}

	public void setPointOfEntry(PointOfEntry pointOfEntry) {
		this.pointOfEntry = pointOfEntry;
	}

	@Column(length = COLUMN_LENGTH_DEFAULT)
	public String getPointOfEntryDetails() {
		return pointOfEntryDetails;
	}

	public void setPointOfEntryDetails(String pointOfEntryDetails) {
		this.pointOfEntryDetails = pointOfEntryDetails;
	}

	public Float getCompleteness() {
		return completeness;
	}

	public void setCompleteness(Float completeness) {
		this.completeness = completeness;
	}

	@Column(columnDefinition = "text")
	public String getAdditionalDetails() {
		return additionalDetails;
	}

	public void setAdditionalDetails(String additionalDetails) {
		this.additionalDetails = additionalDetails;
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

	@Column
	public boolean isSharedToCountry() {
		return sharedToCountry;
	}

	public void setSharedToCountry(boolean sharedToCountry) {
		this.sharedToCountry = sharedToCountry;
	}

	@Enumerated(EnumType.STRING)
	public QuarantineType getQuarantine() {
		return quarantine;
	}

	public void setQuarantine(QuarantineType quarantine) {
		this.quarantine = quarantine;
	}

	@Column(length = COLUMN_LENGTH_DEFAULT)
	public String getQuarantineTypeDetails() {
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

	@Enumerated(EnumType.STRING)
	public YesNoUnknown getPostpartum() {
		return postpartum;
	}

	public void setPostpartum(YesNoUnknown postpartum) {
		this.postpartum = postpartum;
	}

	@Enumerated(EnumType.STRING)
	public Trimester getTrimester() {
		return trimester;
	}

	public void setTrimester(Trimester trimester) {
		this.trimester = trimester;
	}

	@Enumerated(EnumType.STRING)
	public FollowUpStatus getFollowUpStatus() {
		return followUpStatus;
	}

	public void setFollowUpStatus(FollowUpStatus followUpStatus) {
		this.followUpStatus = followUpStatus;
	}

	@Column(length = COLUMN_LENGTH_BIG)
	public String getFollowUpComment() {
		return followUpComment;
	}

	public void setFollowUpComment(String followUpComment) {
		this.followUpComment = followUpComment;
	}

	@Temporal(TemporalType.TIMESTAMP)
	public Date getFollowUpUntil() {
		return followUpUntil;
	}

	public void setFollowUpUntil(Date followUpUntil) {
		this.followUpUntil = followUpUntil;
	}

	@Column
	public boolean isOverwriteFollowUpUntil() {
		return overwriteFollowUpUntil;
	}

	public void setOverwriteFollowUpUntil(boolean overwriteFollowUpUntil) {
		this.overwriteFollowUpUntil = overwriteFollowUpUntil;
	}

	@Enumerated(EnumType.STRING)
	public FacilityType getFacilityType() {
		return facilityType;
	}

	public void setFacilityType(FacilityType facilityType) {
		this.facilityType = facilityType;
	}

	@Column
	public Integer getCaseIdIsm() {
		return caseIdIsm;
	}

	public void setCaseIdIsm(Integer caseIdIsm) {
		this.caseIdIsm = caseIdIsm;
	}

	@Enumerated(EnumType.STRING)
	public ContactTracingContactType getContactTracingFirstContactType() {
		return contactTracingFirstContactType;
	}

	public void setContactTracingFirstContactType(ContactTracingContactType contactTracingContactType) {
		this.contactTracingFirstContactType = contactTracingContactType;
	}

	@Column
	public Date getContactTracingFirstContactDate() {
		return contactTracingFirstContactDate;
	}

	public void setContactTracingFirstContactDate(Date contactTracingContactDate) {
		this.contactTracingFirstContactDate = contactTracingContactDate;
	}

	@Enumerated(EnumType.STRING)
	public YesNoUnknown getWasInQuarantineBeforeIsolation() {
		return wasInQuarantineBeforeIsolation;
	}

	public void setWasInQuarantineBeforeIsolation(YesNoUnknown wasInQuarantineBeforeIsolation) {
		this.wasInQuarantineBeforeIsolation = wasInQuarantineBeforeIsolation;
	}

	@Enumerated(EnumType.STRING)
	public QuarantineReason getQuarantineReasonBeforeIsolation() {
		return quarantineReasonBeforeIsolation;
	}

	public void setQuarantineReasonBeforeIsolation(QuarantineReason quarantineReasonBeforeIsolation) {
		this.quarantineReasonBeforeIsolation = quarantineReasonBeforeIsolation;
	}

	@Column(length = COLUMN_LENGTH_DEFAULT)
	public String getQuarantineReasonBeforeIsolationDetails() {
		return quarantineReasonBeforeIsolationDetails;
	}

	public void setQuarantineReasonBeforeIsolationDetails(String quarantineReasonBeforeIsolationDetails) {
		this.quarantineReasonBeforeIsolationDetails = quarantineReasonBeforeIsolationDetails;
	}

	@Enumerated(EnumType.STRING)
	public EndOfIsolationReason getEndOfIsolationReason() {
		return endOfIsolationReason;
	}

	public void setEndOfIsolationReason(EndOfIsolationReason endOfIsolationReason) {
		this.endOfIsolationReason = endOfIsolationReason;
	}

	@Column(length = COLUMN_LENGTH_DEFAULT)
	public String getEndOfIsolationReasonDetails() {
		return endOfIsolationReasonDetails;
	}

	public void setEndOfIsolationReasonDetails(String endOfIsolationReasonDetails) {
		this.endOfIsolationReasonDetails = endOfIsolationReasonDetails;
	}

	@Column
	public boolean isNosocomialOutbreak() {
		return nosocomialOutbreak;
	}

	public void setNosocomialOutbreak(boolean nosocomialOutbreak) {
		this.nosocomialOutbreak = nosocomialOutbreak;
	}

	@Enumerated(EnumType.STRING)
	public InfectionSetting getInfectionSetting() {
		return infectionSetting;
	}

	public void setInfectionSetting(InfectionSetting infectionSetting) {
		this.infectionSetting = infectionSetting;
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

	@Enumerated(EnumType.STRING)
	public YesNoUnknown getReInfection() {
		return reInfection;
	}

	public void setReInfection(YesNoUnknown reInfection) {
		this.reInfection = reInfection;
	}

	@Temporal(TemporalType.DATE)
	public Date getPreviousInfectionDate() {
		return previousInfectionDate;
	}

	public void setPreviousInfectionDate(Date previousInfectionDate) {
		this.previousInfectionDate = previousInfectionDate;
	}

	@ManyToOne
	public District getReportingDistrict() {
		return reportingDistrict;
	}

	public void setReportingDistrict(District reportingDistrict) {
		this.reportingDistrict = reportingDistrict;
	}

	@Column
	public boolean isNotACaseReasonNegativeTest() {
		return notACaseReasonNegativeTest;
	}

	public void setNotACaseReasonNegativeTest(boolean notACaseReasonNegativeTest) {
		this.notACaseReasonNegativeTest = notACaseReasonNegativeTest;
	}

	@Column
	public boolean isNotACaseReasonPhysicianInformation() {
		return notACaseReasonPhysicianInformation;
	}

	public void setNotACaseReasonPhysicianInformation(boolean notACaseReasonPhysicianInformation) {
		this.notACaseReasonPhysicianInformation = notACaseReasonPhysicianInformation;
	}

	@Column
	public boolean isNotACaseReasonDifferentPathogen() {
		return notACaseReasonDifferentPathogen;
	}

	public void setNotACaseReasonDifferentPathogen(boolean notACaseReasonDifferentPathogen) {
		this.notACaseReasonDifferentPathogen = notACaseReasonDifferentPathogen;
	}

	@Column
	public boolean isNotACaseReasonOther() {
		return notACaseReasonOther;
	}

	public void setNotACaseReasonOther(boolean notACaseReasonOther) {
		this.notACaseReasonOther = notACaseReasonOther;
	}

	@Column(length = COLUMN_LENGTH_DEFAULT)
	public String getNotACaseReasonDetails() {
		return notACaseReasonDetails;
	}

	public void setNotACaseReasonDetails(String notACaseReasonDetails) {
		this.notACaseReasonDetails = notACaseReasonDetails;
	}

	@Enumerated(EnumType.STRING)
	public YesNoUnknown getBloodOrganOrTissueDonated() {
		return bloodOrganOrTissueDonated;
	}

	public void setBloodOrganOrTissueDonated(YesNoUnknown bloodOrganOrTissueDonated) {
		this.bloodOrganOrTissueDonated = bloodOrganOrTissueDonated;
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

	@OneToMany(mappedBy = ShareInfoCase.CAZE, fetch = FetchType.LAZY)
	@AuditedIgnore
	public List<ShareInfoCase> getShareInfoCases() {
		return shareInfoCases;
	}

	public void setShareInfoCases(List<ShareInfoCase> shareInfoCases) {
		this.shareInfoCases = shareInfoCases;
	}

	@OneToMany(mappedBy = ExternalShareInfo.CAZE, fetch = FetchType.LAZY)
	public List<ExternalShareInfo> getExternalShares() {
		return externalShares;
	}

	public void setExternalShares(List<ExternalShareInfo> externalShares) {
		this.externalShares = externalShares;
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

	public boolean isDontShareWithReportingTool() {
		return dontShareWithReportingTool;
	}

	public void setDontShareWithReportingTool(boolean dontShareWithReportingTool) {
		this.dontShareWithReportingTool = dontShareWithReportingTool;
	}
}
