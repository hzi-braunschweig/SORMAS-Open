/*
 * * SORMAS® - Surveillance Outbreak Response Management & Analysis System
 * * Copyright © 2016-2021 Helmholtz-Zentrum für Infektionsforschung GmbH (HZI)
 * * This program is free software: you can redistribute it and/or modify
 * * it under the terms of the GNU General Public License as published by
 * * the Free Software Foundation, either version 3 of the License, or
 * * (at your option) any later version.
 * * This program is distributed in the hope that it will be useful,
 * * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * * GNU General Public License for more details.
 * * You should have received a copy of the GNU General Public License
 * * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package de.symeda.sormas.backend.event;

import static de.symeda.sormas.api.EntityDto.COLUMN_LENGTH_BIG;
import static de.symeda.sormas.api.EntityDto.COLUMN_LENGTH_DEFAULT;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Type;

import de.symeda.auditlog.api.Audited;
import de.symeda.auditlog.api.AuditedIgnore;
import de.symeda.sormas.api.Disease;
import de.symeda.sormas.api.event.DiseaseTransmissionMode;
import de.symeda.sormas.api.event.EpidemiologicalEvidenceDetail;
import de.symeda.sormas.api.event.EventInvestigationStatus;
import de.symeda.sormas.api.event.EventManagementStatus;
import de.symeda.sormas.api.event.EventReferenceDto;
import de.symeda.sormas.api.event.EventSourceType;
import de.symeda.sormas.api.event.EventStatus;
import de.symeda.sormas.api.event.HumanTransmissionMode;
import de.symeda.sormas.api.event.InfectionPathCertainty;
import de.symeda.sormas.api.event.InstitutionalPartnerType;
import de.symeda.sormas.api.event.LaboratoryDiagnosticEvidenceDetail;
import de.symeda.sormas.api.event.MeansOfTransport;
import de.symeda.sormas.api.event.MedicallyAssociatedTransmissionMode;
import de.symeda.sormas.api.event.ParenteralTransmissionMode;
import de.symeda.sormas.api.event.RiskLevel;
import de.symeda.sormas.api.event.TypeOfPlace;
import de.symeda.sormas.api.exposure.WorkEnvironment;
import de.symeda.sormas.api.externaldata.HasExternalData;
import de.symeda.sormas.api.utils.YesNoUnknown;
import de.symeda.sormas.backend.common.CoreAdo;
import de.symeda.sormas.backend.location.Location;
import de.symeda.sormas.backend.sormastosormas.entities.SormasToSormasEntity;
import de.symeda.sormas.backend.sormastosormas.origin.SormasToSormasOriginInfo;
import de.symeda.sormas.backend.sormastosormas.share.shareinfo.ShareInfoEvent;
import de.symeda.sormas.backend.task.Task;
import de.symeda.sormas.backend.user.User;
import de.symeda.sormas.backend.util.ModelConstants;

@Entity(name = "events")
@Audited
public class Event extends CoreAdo implements SormasToSormasEntity, HasExternalData {

	private static final long serialVersionUID = 4964495716032049582L;

	public static final String TABLE_NAME = "events";

	public static final String EVENTS_EVENT_GROUPS_TABLE_NAME = "events_eventgroups";

	public static final String EXTERNAL_ID = "externalId";
	public static final String EXTERNAL_TOKEN = "externalToken";
	public static final String INTERNAL_TOKEN = "internalToken";
	public static final String EVENT_STATUS = "eventStatus";
	public static final String RISK_LEVEL = "riskLevel";
	public static final String EVENT_INVESTIGATION_STATUS = "eventInvestigationStatus";
	public static final String EVENT_INVESTIGATION_START_DATE = "eventInvestigationStartDate";
	public static final String EVENT_INVESTIGATION_END_DATE = "eventInvestigationEndDate";
	public static final String EVENT_MANAGEMENT_STATUS = "eventManagementStatus";
	public static final String EVENT_PERSONS = "eventPersons";
	public static final String EVENT_TITLE = "eventTitle";
	public static final String EVENT_DESC = "eventDesc";
	public static final String NOSOCOMIAL = "nosocomial";
	public static final String START_DATE = "startDate";
	public static final String END_DATE = "endDate";
	public static final String REPORT_DATE_TIME = "reportDateTime";
	public static final String REPORTING_USER = "reportingUser";
	public static final String EVOLUTION_DATE = "evolutionDate";
	public static final String EVOLUTION_COMMENT = "evolutionComment";
	public static final String EVENT_LOCATION = "eventLocation";
	public static final String EVENT_GROUPS = "eventGroups";
	public static final String TYPE_OF_PLACE = "typeOfPlace";
	public static final String MEANS_OF_TRANSPORT = "meansOfTransport";
	public static final String MEANS_OF_TRANSPORT_DETAILS = "meansOfTransportDetails";
	public static final String CONNECTION_NUMBER = "connectionNumber";
	public static final String TRAVEL_DATE = "travelDate";
	public static final String COMMERCE = "commerce";
	public static final String WORK_ENVIRONMENT = "workEnvironment";
	public static final String SRC_TYPE = "srcType";
	public static final String SRC_INSTITUTIONAL_PARTNER_TYPE = "srcInstitutionalPartnerType";
	public static final String SRC_INSTITUTIONAL_PARTNER_TYPE_DETAILS = "srcInstitutionalPartnerTypeDetails";
	public static final String SRC_FIRST_NAME = "srcFirstName";
	public static final String SRC_LAST_NAME = "srcLastName";
	public static final String SRC_TEL_NO = "srcTelNo";
	public static final String SRC_EMAIL = "srcEmail";
	public static final String SRC_MEDIA_WEBSITE = "srcMediaWebsite";
	public static final String SRC_MEDIA_NAME = "srcMediaName";
	public static final String SRC_MEDIA_DETAILS = "srcMediaDetails";
	public static final String DISEASE = "disease";
	public static final String DISEASE_DETAILS = "diseaseDetails";
	public static final String RESPONSIBLE_USER = "responsibleUser";
	public static final String TYPE_OF_PLACE_TEXT = "typeOfPlaceText";
	public static final String TASKS = "tasks";
	public static final String REPORT_LAT = "reportLat";
	public static final String REPORT_LON = "reportLon";
	public static final String ARCHIVED = "archived";
	public static final String DISEASE_TRANSMISSION_MODE = "diseaseTransmissionMode";
	public static final String TRANSREGIONAL_OUTBREAK = "transregionalOutbreak";
	public static final String SUPERORDINATE_EVENT = "superordinateEvent";
	public static final String SUBORDINATE_EVENTS = "subordinateEvents";

	public static final String INFECTION_PATH_CERTAINTY = "infectionPathCertainty";
	public static final String HUMAN_TRANSMISSION_MODE = "humanTransmissionMode";
	public static final String PARENTERAL_TRANSMISSION_MODE = "parenteralTransmissionMode";
	public static final String MEDICALLY_ASSOCIATED_TRANSMISSION_MODE = "medicallyAssociatedTransmissionMode";

	public static final String SHARE_INFO_EVENTS = "shareInfoEvents";

	private Event superordinateEvent;
	private List<Event> subordinateEvents;

	private EventStatus eventStatus;
	private RiskLevel riskLevel;
	private EventInvestigationStatus eventInvestigationStatus;
	private Date eventInvestigationStartDate;
	private Date eventInvestigationEndDate;
	private List<EventParticipant> eventPersons;
	private String externalId;
	private String externalToken;
	private String eventTitle;
	private String eventDesc;
	private YesNoUnknown nosocomial;
	private Date startDate;
	private Date endDate;
	private Date reportDateTime;
	private User reportingUser;
	private Date evolutionDate;
	private String evolutionComment;
	private Location eventLocation;
	private TypeOfPlace typeOfPlace;
	private MeansOfTransport meansOfTransport;
	private String meansOfTransportDetails;
	private String connectionNumber;
	private Date travelDate;
	private WorkEnvironment workEnvironment;
	private EventSourceType srcType;
	private InstitutionalPartnerType srcInstitutionalPartnerType;
	private String srcInstitutionalPartnerTypeDetails;
	private String srcFirstName;
	private String srcLastName;
	private String srcTelNo;
	private String srcEmail;
	private String srcMediaWebsite;
	private String srcMediaName;
	private String srcMediaDetails;
	private Disease disease;
	private String diseaseDetails;
	private User responsibleUser;
	private String typeOfPlaceText;
	private Double reportLat;
	private Double reportLon;
	private Float reportLatLonAccuracy;
	private YesNoUnknown transregionalOutbreak;
	private DiseaseTransmissionMode diseaseTransmissionMode;
	private SormasToSormasOriginInfo sormasToSormasOriginInfo;
	private List<ShareInfoEvent> shareInfoEvents = new ArrayList<>(0);
	private EventManagementStatus eventManagementStatus;

	private boolean archived;

	private InfectionPathCertainty infectionPathCertainty;
	private HumanTransmissionMode humanTransmissionMode;
	private ParenteralTransmissionMode parenteralTransmissionMode;
	private MedicallyAssociatedTransmissionMode medicallyAssociatedTransmissionMode;

	private YesNoUnknown epidemiologicalEvidence;
	private Map<EpidemiologicalEvidenceDetail, Boolean> epidemiologicalEvidenceDetails;
	private YesNoUnknown laboratoryDiagnosticEvidence;
	private Map<LaboratoryDiagnosticEvidenceDetail, Boolean> laboratoryDiagnosticEvidenceDetails;

	private List<Task> tasks;
	private List<EventGroup> eventGroups;

	private String internalToken;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	public EventStatus getEventStatus() {
		return eventStatus;
	}

	public void setEventStatus(EventStatus eventStatus) {
		this.eventStatus = eventStatus;
	}

	@Enumerated(EnumType.STRING)
	public RiskLevel getRiskLevel() {
		return riskLevel;
	}

	public void setRiskLevel(RiskLevel riskLevel) {
		this.riskLevel = riskLevel;
	}

	@Enumerated(EnumType.STRING)
	public EventInvestigationStatus getEventInvestigationStatus() {
		return eventInvestigationStatus;
	}

	public void setEventInvestigationStatus(EventInvestigationStatus eventInvestigationStatus) {
		this.eventInvestigationStatus = eventInvestigationStatus;
	}

	@Temporal(TemporalType.TIMESTAMP)
	public Date getEventInvestigationStartDate() {
		return eventInvestigationStartDate;
	}

	public void setEventInvestigationStartDate(Date eventInvestigationStartDate) {
		this.eventInvestigationStartDate = eventInvestigationStartDate;
	}

	@Temporal(TemporalType.TIMESTAMP)
	public Date getEventInvestigationEndDate() {
		return eventInvestigationEndDate;
	}

	public void setEventInvestigationEndDate(Date eventInvestigationEndDate) {
		this.eventInvestigationEndDate = eventInvestigationEndDate;
	}

	@OneToMany(cascade = {}, mappedBy = EventParticipant.EVENT)
	public List<EventParticipant> getEventPersons() {
		return eventPersons;
	}

	public void setEventPersons(List<EventParticipant> eventPersons) {
		this.eventPersons = eventPersons;
	}

	@Column(length = COLUMN_LENGTH_DEFAULT)
	public String getExternalId() {
		return externalId;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	@Column(length = COLUMN_LENGTH_DEFAULT)
	public String getExternalToken() {
		return externalToken;
	}

	public void setExternalToken(String externalToken) {
		this.externalToken = externalToken;
	}

	@Column(length = COLUMN_LENGTH_DEFAULT, nullable = false)
	public String getEventTitle() {
		return eventTitle;
	}

	public void setEventTitle(String eventTitle) {
		this.eventTitle = eventTitle;
	}

	@Column(length = COLUMN_LENGTH_BIG)
	public String getEventDesc() {
		return eventDesc;
	}

	public void setEventDesc(String eventDesc) {
		this.eventDesc = eventDesc;
	}

	@Enumerated(EnumType.STRING)
	public YesNoUnknown getNosocomial() {
		return nosocomial;
	}

	public void setNosocomial(YesNoUnknown nosocomial) {
		this.nosocomial = nosocomial;
	}

	@Temporal(TemporalType.TIMESTAMP)
	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	@Temporal(TemporalType.TIMESTAMP)
	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
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

	@Temporal(TemporalType.TIMESTAMP)
	public Date getEvolutionDate() {
		return evolutionDate;
	}

	public void setEvolutionDate(Date evolutionDate) {
		this.evolutionDate = evolutionDate;
	}

	public String getEvolutionComment() {
		return evolutionComment;
	}

	public void setEvolutionComment(String evolutionComment) {
		this.evolutionComment = evolutionComment;
	}

	@OneToOne(cascade = CascadeType.ALL)
	public Location getEventLocation() {
		if (eventLocation == null) {
			eventLocation = new Location();
		}
		return eventLocation;
	}

	public void setEventLocation(Location eventLocation) {
		this.eventLocation = eventLocation;
	}

	@Enumerated(EnumType.STRING)
	public TypeOfPlace getTypeOfPlace() {
		return typeOfPlace;
	}

	public void setTypeOfPlace(TypeOfPlace typeOfPlace) {
		this.typeOfPlace = typeOfPlace;
	}

	@Enumerated(EnumType.STRING)
	public MeansOfTransport getMeansOfTransport() {
		return meansOfTransport;
	}

	public void setMeansOfTransport(MeansOfTransport meansOfTransport) {
		this.meansOfTransport = meansOfTransport;
	}

	@Column(columnDefinition = "text")
	public String getMeansOfTransportDetails() {
		return meansOfTransportDetails;
	}

	public void setMeansOfTransportDetails(String meansOfTransportDetails) {
		this.meansOfTransportDetails = meansOfTransportDetails;
	}

	@Column(length = COLUMN_LENGTH_DEFAULT)
	public String getConnectionNumber() {
		return connectionNumber;
	}

	public void setConnectionNumber(String connectionNumber) {
		this.connectionNumber = connectionNumber;
	}

	public Date getTravelDate() {
		return travelDate;
	}

	public void setTravelDate(Date travelDate) {
		this.travelDate = travelDate;
	}

	@Enumerated(EnumType.STRING)
	public WorkEnvironment getWorkEnvironment() {
		return workEnvironment;
	}

	public void setWorkEnvironment(WorkEnvironment workEnvironment) {
		this.workEnvironment = workEnvironment;
	}

	@Enumerated(EnumType.STRING)
	public EventSourceType getSrcType() {
		return srcType;
	}

	public void setSrcType(EventSourceType srcType) {
		this.srcType = srcType;
	}

	@Enumerated(EnumType.STRING)
	public InstitutionalPartnerType getSrcInstitutionalPartnerType() {
		return srcInstitutionalPartnerType;
	}

	public void setSrcInstitutionalPartnerType(InstitutionalPartnerType srcInstitutionalPartnerType) {
		this.srcInstitutionalPartnerType = srcInstitutionalPartnerType;
	}

	@Column(length = COLUMN_LENGTH_DEFAULT)
	public String getSrcInstitutionalPartnerTypeDetails() {
		return srcInstitutionalPartnerTypeDetails;
	}

	public void setSrcInstitutionalPartnerTypeDetails(String srcInstitutionalPartnerTypeDetails) {
		this.srcInstitutionalPartnerTypeDetails = srcInstitutionalPartnerTypeDetails;
	}

	@Column(length = COLUMN_LENGTH_DEFAULT)
	public String getSrcFirstName() {
		return srcFirstName;
	}

	public void setSrcFirstName(String srcFirstName) {
		this.srcFirstName = srcFirstName;
	}

	@Column(length = COLUMN_LENGTH_DEFAULT)
	public String getSrcLastName() {
		return srcLastName;
	}

	public void setSrcLastName(String srcLastName) {
		this.srcLastName = srcLastName;
	}

	@Column(length = COLUMN_LENGTH_DEFAULT)
	public String getSrcTelNo() {
		return srcTelNo;
	}

	public void setSrcTelNo(String srcTelNo) {
		this.srcTelNo = srcTelNo;
	}

	@Column(length = COLUMN_LENGTH_DEFAULT)
	public String getSrcEmail() {
		return srcEmail;
	}

	public void setSrcEmail(String srcEmail) {
		this.srcEmail = srcEmail;
	}

	@Column(length = COLUMN_LENGTH_DEFAULT)
	public String getSrcMediaWebsite() {
		return srcMediaWebsite;
	}

	public void setSrcMediaWebsite(String srcMediaWebsite) {
		this.srcMediaWebsite = srcMediaWebsite;
	}

	@Column(length = COLUMN_LENGTH_DEFAULT)
	public String getSrcMediaName() {
		return srcMediaName;
	}

	public void setSrcMediaName(String srcMediaName) {
		this.srcMediaName = srcMediaName;
	}

	@Column(length = COLUMN_LENGTH_BIG)
	public String getSrcMediaDetails() {
		return srcMediaDetails;
	}

	public void setSrcMediaDetails(String srcMediaDetails) {
		this.srcMediaDetails = srcMediaDetails;
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

	@ManyToOne
	public User getResponsibleUser() {
		return responsibleUser;
	}

	public void setResponsibleUser(User responsibleUser) {
		this.responsibleUser = responsibleUser;
	}

	@Column(length = COLUMN_LENGTH_DEFAULT)
	public String getTypeOfPlaceText() {
		return typeOfPlaceText;
	}

	public void setTypeOfPlaceText(String typeOfPlaceText) {
		this.typeOfPlaceText = typeOfPlaceText;
	}

	@Enumerated(EnumType.STRING)
	public YesNoUnknown getEpidemiologicalEvidence() {
		return epidemiologicalEvidence;
	}

	public void setEpidemiologicalEvidence(YesNoUnknown epidemiologicalEvidence) {
		this.epidemiologicalEvidence = epidemiologicalEvidence;
	}

	@Type(type = ModelConstants.HIBERNATE_TYPE_JSON)
	@Column(columnDefinition = ModelConstants.COLUMN_DEFINITION_JSON)
	public Map<EpidemiologicalEvidenceDetail, Boolean> getEpidemiologicalEvidenceDetails() {
		return epidemiologicalEvidenceDetails;
	}

	public void setEpidemiologicalEvidenceDetails(Map<EpidemiologicalEvidenceDetail, Boolean> epidemiologicalEvidenceDetails) {
		this.epidemiologicalEvidenceDetails = epidemiologicalEvidenceDetails;
	}

	@Enumerated(EnumType.STRING)
	public YesNoUnknown getLaboratoryDiagnosticEvidence() {
		return laboratoryDiagnosticEvidence;
	}

	public void setLaboratoryDiagnosticEvidence(YesNoUnknown laboratoryDiagnosticEvidence) {
		this.laboratoryDiagnosticEvidence = laboratoryDiagnosticEvidence;
	}

	@Type(type = ModelConstants.HIBERNATE_TYPE_JSON)
	@Column(columnDefinition = ModelConstants.COLUMN_DEFINITION_JSON)
	public Map<LaboratoryDiagnosticEvidenceDetail, Boolean> getLaboratoryDiagnosticEvidenceDetails() {
		return laboratoryDiagnosticEvidenceDetails;
	}

	public void setLaboratoryDiagnosticEvidenceDetails(Map<LaboratoryDiagnosticEvidenceDetail, Boolean> laboratoryDiagnosticEvidenceDetails) {
		this.laboratoryDiagnosticEvidenceDetails = laboratoryDiagnosticEvidenceDetails;
	}

	@OneToMany(mappedBy = Task.EVENT, fetch = FetchType.LAZY)
	public List<Task> getTasks() {
		return tasks;
	}

	public void setTasks(List<Task> tasks) {
		this.tasks = tasks;
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

	@Enumerated(EnumType.STRING)
	public YesNoUnknown getTransregionalOutbreak() {
		return transregionalOutbreak;
	}

	public void setTransregionalOutbreak(YesNoUnknown transregionalOutbreak) {
		this.transregionalOutbreak = transregionalOutbreak;
	}

	@Enumerated(EnumType.STRING)
	public DiseaseTransmissionMode getDiseaseTransmissionMode() {
		return diseaseTransmissionMode;
	}

	public void setDiseaseTransmissionMode(DiseaseTransmissionMode diseaseTransmissionMode) {
		this.diseaseTransmissionMode = diseaseTransmissionMode;
	}

	@Column
	public boolean isArchived() {
		return archived;
	}

	public void setArchived(boolean archived) {
		this.archived = archived;
	}

	@Override
	public String toString() {
		return EventReferenceDto.buildCaption(getDisease(), getDiseaseDetails(), getEventStatus(), getEventInvestigationStatus(), getStartDate());
	}

	public Float getReportLatLonAccuracy() {
		return reportLatLonAccuracy;
	}

	public void setReportLatLonAccuracy(Float reportLatLonAccuracy) {
		this.reportLatLonAccuracy = reportLatLonAccuracy;
	}

	@ManyToOne
	public Event getSuperordinateEvent() {
		return superordinateEvent;
	}

	public void setSuperordinateEvent(Event superordinateEvent) {
		this.superordinateEvent = superordinateEvent;
	}

	@OneToMany(mappedBy = Event.SUPERORDINATE_EVENT, fetch = FetchType.LAZY)
	public List<Event> getSubordinateEvents() {
		return subordinateEvents;
	}

	@Enumerated(EnumType.STRING)
	public EventManagementStatus getEventManagementStatus() {
		return eventManagementStatus;
	}

	public void setEventManagementStatus(EventManagementStatus eventManagementStatus) {
		this.eventManagementStatus = eventManagementStatus;
	}

	public void setSubordinateEvents(List<Event> subordinateEvents) {
		this.subordinateEvents = subordinateEvents;
	}

	@Enumerated(EnumType.STRING)
	public InfectionPathCertainty getInfectionPathCertainty() {
		return infectionPathCertainty;
	}

	public void setInfectionPathCertainty(InfectionPathCertainty infectionPathCertainty) {
		this.infectionPathCertainty = infectionPathCertainty;
	}

	@Enumerated(EnumType.STRING)
	public HumanTransmissionMode getHumanTransmissionMode() {
		return humanTransmissionMode;
	}

	public void setHumanTransmissionMode(HumanTransmissionMode humanTransmissionMode) {
		this.humanTransmissionMode = humanTransmissionMode;
	}

	@Enumerated(EnumType.STRING)
	public ParenteralTransmissionMode getParenteralTransmissionMode() {
		return parenteralTransmissionMode;
	}

	public void setParenteralTransmissionMode(ParenteralTransmissionMode parenteralTransmissionMode) {
		this.parenteralTransmissionMode = parenteralTransmissionMode;
	}

	@Enumerated(EnumType.STRING)
	public MedicallyAssociatedTransmissionMode getMedicallyAssociatedTransmissionMode() {
		return medicallyAssociatedTransmissionMode;
	}

	public void setMedicallyAssociatedTransmissionMode(MedicallyAssociatedTransmissionMode medicallyAssociatedTransmissionMode) {
		this.medicallyAssociatedTransmissionMode = medicallyAssociatedTransmissionMode;
	}

	@ManyToOne(cascade = CascadeType.ALL)
	@AuditedIgnore
	public SormasToSormasOriginInfo getSormasToSormasOriginInfo() {
		return sormasToSormasOriginInfo;
	}

	public void setSormasToSormasOriginInfo(SormasToSormasOriginInfo originInfo) {
		this.sormasToSormasOriginInfo = originInfo;
	}

	@OneToMany(mappedBy = ShareInfoEvent.EVENT, fetch = FetchType.LAZY)
	public List<ShareInfoEvent> getShareInfoEvents() {
		return shareInfoEvents;
	}

	public void setShareInfoEvents(List<ShareInfoEvent> shareInfoEvents) {
		this.shareInfoEvents = shareInfoEvents;
	}

	@Column(columnDefinition = "text")
	public String getInternalToken() {
		return internalToken;
	}

	public void setInternalToken(String internalToken) {
		this.internalToken = internalToken;
	}

	@AuditedIgnore
	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = EVENTS_EVENT_GROUPS_TABLE_NAME,
		joinColumns = @JoinColumn(name = "event_id"),
		inverseJoinColumns = @JoinColumn(name = "eventgroup_id"))
	public List<EventGroup> getEventGroups() {
		return eventGroups;
	}

	public void setEventGroups(List<EventGroup> eventGroups) {
		this.eventGroups = eventGroups;
	}
}
