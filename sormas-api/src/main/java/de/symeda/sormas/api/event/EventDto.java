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

package de.symeda.sormas.api.event;

import java.util.Date;

import de.symeda.sormas.api.Disease;
import de.symeda.sormas.api.exposure.WorkEnvironment;
import de.symeda.sormas.api.location.LocationDto;
import de.symeda.sormas.api.sormastosormas.SormasToSormasOriginInfoDto;
import de.symeda.sormas.api.user.UserReferenceDto;
import de.symeda.sormas.api.utils.DataHelper;
import de.symeda.sormas.api.utils.HideForCountriesExcept;
import javax.validation.constraints.NotNull;
import de.symeda.sormas.api.utils.SensitiveData;
import de.symeda.sormas.api.utils.SormasToSormasEntityDto;
import de.symeda.sormas.api.utils.YesNoUnknown;
import de.symeda.sormas.api.utils.pseudonymization.PseudonymizableDto;

public class EventDto extends PseudonymizableDto implements SormasToSormasEntityDto {

	private static final long serialVersionUID = 2430932452606853497L;

	public static final String I18N_PREFIX = "Event";

	public static final String EVENT_STATUS = "eventStatus";
	public static final String EVENT_INVESTIGATION_STATUS = "eventInvestigationStatus";
	public static final String RISK_LEVEL = "riskLevel";
	public static final String EVENT_INVESTIGATION_START_DATE = "eventInvestigationStartDate";
	public static final String EVENT_INVESTIGATION_END_DATE = "eventInvestigationEndDate";
	public static final String EVENT_PERSONS = "eventPersons";
	public static final String PARTICIPANTS_COUNT = "participantCount";
	public static final String EVENT_ACTIONS = "eventActions";
	public static final String EVENT_MANAGEMENT_STATUS = "eventManagementStatus";
	public static final String EXTERNAL_ID = "externalId";
	public static final String EXTERNAL_TOKEN = "externalToken";
	public static final String EVENT_TITLE = "eventTitle";
	public static final String EVENT_DESC = "eventDesc";
	public static final String NOSOCOMIAL = "nosocomial";
	public static final String START_DATE = "startDate";
	public static final String END_DATE = "endDate";
	public static final String MULTI_DAY_EVENT = "multiDayEvent";
	public static final String REPORT_DATE_TIME = "reportDateTime";
	public static final String REPORTING_USER = "reportingUser";
	public static final String EVOLUTION_DATE = "evolutionDate";
	public static final String EVOLUTION_COMMENT = "evolutionComment";
	public static final String EVENT_LOCATION = "eventLocation";
	public static final String TYPE_OF_PLACE = "typeOfPlace";
	public static final String MEANS_OF_TRANSPORT = "meansOfTransport";
	public static final String MEANS_OF_TRANSPORT_DETAILS = "meansOfTransportDetails";
	public static final String CONNECTION_NUMBER = "connectionNumber";
	public static final String TRAVEL_DATE = "travelDate";
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
	public static final String REPORT_LAT = "reportLat";
	public static final String REPORT_LON = "reportLon";
	public static final String TRANSREGIONAL_OUTBREAK = "transregionalOutbreak";
	public static final String DISEASE_TRANSMISSION_MODE = "diseaseTransmissionMode";
	public static final String SUPERORDINATE_EVENT = "superordinateEvent";
	public static final String SORMAS_TO_SORMAS_ORIGIN_INFO = "sormasToSormasOriginInfo";
	public static final String OWNERSHIP_HANDED_OVER = "ownershipHandedOver";
	public static final String INFECTION_PATH_CERTAINTY = "infectionPathCertainty";
	public static final String HUMAN_TRANSMISSION_MODE = "humanTransmissionMode";
	public static final String PARENTERAL_TRANSMISSION_MODE = "parenteralTransmissionMode";
	public static final String MEDICALLY_ASSOCIATED_TRANSMISSION_MODE = "medicallyAssociatedTransmissionMode";
	public static final String INTERNALID = "internalId";

	private EventReferenceDto superordinateEvent;

	@NotNull
	private EventStatus eventStatus;
	private RiskLevel riskLevel;
	private EventInvestigationStatus eventInvestigationStatus;
	private Date eventInvestigationStartDate;
	private Date eventInvestigationEndDate;
	private EventManagementStatus eventManagementStatus;
	private String externalId;
	private String externalToken;
	private String eventTitle;
	@NotNull
	private String eventDesc;
	private YesNoUnknown nosocomial;
	private Date startDate;
	private Date endDate;
	@NotNull
	private Date reportDateTime;
	@NotNull
	private UserReferenceDto reportingUser;
	private Date evolutionDate;
	private String evolutionComment;
	private LocationDto eventLocation;
	private TypeOfPlace typeOfPlace;
	private MeansOfTransport meansOfTransport;
	private String meansOfTransportDetails;
	@SensitiveData
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
	@SensitiveData
	private UserReferenceDto responsibleUser;
	private String typeOfPlaceText;
	private Double reportLat;
	private Double reportLon;
	private Float reportLatLonAccuracy;
	private YesNoUnknown transregionalOutbreak;
	private DiseaseTransmissionMode diseaseTransmissionMode;

	@HideForCountriesExcept
	private InfectionPathCertainty infectionPathCertainty;
	@HideForCountriesExcept
	private HumanTransmissionMode humanTransmissionMode;
	@HideForCountriesExcept
	private ParenteralTransmissionMode parenteralTransmissionMode;
	@HideForCountriesExcept
	private MedicallyAssociatedTransmissionMode medicallyAssociatedTransmissionMode;

	private SormasToSormasOriginInfoDto sormasToSormasOriginInfo;
	private boolean ownershipHandedOver;

	@HideForCountriesExcept
	private String internalId;

	public static EventDto build() {
		EventDto event = new EventDto();
		event.setUuid(DataHelper.createUuid());

		event.setEventStatus(EventStatus.SIGNAL);
		event.setEventInvestigationStatus(EventInvestigationStatus.PENDING);
		event.setEventLocation(LocationDto.build());
		event.setReportDateTime(new Date());

		return event;
	}

	public EventStatus getEventStatus() {
		return eventStatus;
	}

	public void setEventStatus(EventStatus eventStatus) {
		this.eventStatus = eventStatus;
	}

	public RiskLevel getRiskLevel() {
		return riskLevel;
	}

	public void setRiskLevel(RiskLevel riskLevel) {
		this.riskLevel = riskLevel;
	}

	public EventInvestigationStatus getEventInvestigationStatus() {
		return eventInvestigationStatus;
	}

	public void setEventInvestigationStatus(EventInvestigationStatus eventInvestigationStatus) {
		this.eventInvestigationStatus = eventInvestigationStatus;
	}

	public Date getEventInvestigationStartDate() {
		return eventInvestigationStartDate;
	}

	public void setEventInvestigationStartDate(Date eventInvestigationStartDate) {
		this.eventInvestigationStartDate = eventInvestigationStartDate;
	}

	public Date getEventInvestigationEndDate() {
		return eventInvestigationEndDate;
	}

	public void setEventInvestigationEndDate(Date eventInvestigationEndDate) {
		this.eventInvestigationEndDate = eventInvestigationEndDate;
	}

	public String getExternalId() {
		return externalId;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	public String getExternalToken() {
		return externalToken;
	}

	public void setExternalToken(String externalToken) {
		this.externalToken = externalToken;
	}

	public String getEventTitle() {
		return eventTitle;
	}

	public void setEventTitle(String eventTitle) {
		this.eventTitle = eventTitle;
	}

	public String getEventDesc() {
		return eventDesc;
	}

	public void setEventDesc(String eventDesc) {
		this.eventDesc = eventDesc;
	}

	public YesNoUnknown getNosocomial() {
		return nosocomial;
	}

	public void setNosocomial(YesNoUnknown nosocomial) {
		this.nosocomial = nosocomial;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public boolean isMultiDayEvent() {
		return endDate != null;
	}

	public void setMultiDayEvent(boolean ignored) {
		// do nothing
	}

	public Date getReportDateTime() {
		return reportDateTime;
	}

	public void setReportDateTime(Date reportDateTime) {
		this.reportDateTime = reportDateTime;
	}

	public UserReferenceDto getReportingUser() {
		return reportingUser;
	}

	public void setReportingUser(UserReferenceDto reportingUser) {
		this.reportingUser = reportingUser;
	}

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

	public TypeOfPlace getTypeOfPlace() {
		return typeOfPlace;
	}

	public void setTypeOfPlace(TypeOfPlace typeOfPlace) {
		this.typeOfPlace = typeOfPlace;
	}

	public MeansOfTransport getMeansOfTransport() {
		return meansOfTransport;
	}

	public void setMeansOfTransport(MeansOfTransport meansOfTransport) {
		this.meansOfTransport = meansOfTransport;
	}

	public String getMeansOfTransportDetails() {
		return meansOfTransportDetails;
	}

	public void setMeansOfTransportDetails(String meansOfTransportDetails) {
		this.meansOfTransportDetails = meansOfTransportDetails;
	}

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

	public WorkEnvironment getWorkEnvironment() {
		return workEnvironment;
	}

	public void setWorkEnvironment(WorkEnvironment workEnvironment) {
		this.workEnvironment = workEnvironment;
	}

	public EventSourceType getSrcType() {
		return srcType;
	}

	public void setSrcType(EventSourceType srcType) {
		this.srcType = srcType;
	}

	public InstitutionalPartnerType getSrcInstitutionalPartnerType() {
		return srcInstitutionalPartnerType;
	}

	public void setSrcInstitutionalPartnerType(InstitutionalPartnerType srcInstitutionalPartnerType) {
		this.srcInstitutionalPartnerType = srcInstitutionalPartnerType;
	}

	public String getSrcInstitutionalPartnerTypeDetails() {
		return srcInstitutionalPartnerTypeDetails;
	}

	public void setSrcInstitutionalPartnerTypeDetails(String srcInstitutionalPartnerTypeDetails) {
		this.srcInstitutionalPartnerTypeDetails = srcInstitutionalPartnerTypeDetails;
	}

	public String getSrcFirstName() {
		return srcFirstName;
	}

	public void setSrcFirstName(String srcFirstName) {
		this.srcFirstName = srcFirstName;
	}

	public String getSrcLastName() {
		return srcLastName;
	}

	public void setSrcLastName(String srcLastName) {
		this.srcLastName = srcLastName;
	}

	public String getSrcTelNo() {
		return srcTelNo;
	}

	public void setSrcTelNo(String srcTelNo) {
		this.srcTelNo = srcTelNo;
	}

	public String getSrcEmail() {
		return srcEmail;
	}

	public void setSrcEmail(String srcEmail) {
		this.srcEmail = srcEmail;
	}

	public String getSrcMediaWebsite() {
		return srcMediaWebsite;
	}

	public void setSrcMediaWebsite(String srcMediaWebsite) {
		this.srcMediaWebsite = srcMediaWebsite;
	}

	public String getSrcMediaName() {
		return srcMediaName;
	}

	public void setSrcMediaName(String srcMediaName) {
		this.srcMediaName = srcMediaName;
	}

	public String getSrcMediaDetails() {
		return srcMediaDetails;
	}

	public void setSrcMediaDetails(String srcMediaDetails) {
		this.srcMediaDetails = srcMediaDetails;
	}

	public LocationDto getEventLocation() {
		return eventLocation;
	}

	public void setEventLocation(LocationDto eventLocation) {
		this.eventLocation = eventLocation;
	}

	public Disease getDisease() {
		return disease;
	}

	public void setDisease(Disease disease) {
		this.disease = disease;
	}

	public String getDiseaseDetails() {
		return diseaseDetails;
	}

	public void setDiseaseDetails(String diseaseDetails) {
		this.diseaseDetails = diseaseDetails;
	}

	public UserReferenceDto getResponsibleUser() {
		return responsibleUser;
	}

	public void setResponsibleUser(UserReferenceDto responsibleUser) {
		this.responsibleUser = responsibleUser;
	}

	public String getTypeOfPlaceText() {
		return typeOfPlaceText;
	}

	public void setTypeOfPlaceText(String typeOfPlaceText) {
		this.typeOfPlaceText = typeOfPlaceText;
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

	public YesNoUnknown getTransregionalOutbreak() {
		return transregionalOutbreak;
	}

	public void setTransregionalOutbreak(YesNoUnknown transregionalOutbreak) {
		this.transregionalOutbreak = transregionalOutbreak;
	}

	public DiseaseTransmissionMode getDiseaseTransmissionMode() {
		return diseaseTransmissionMode;
	}

	public void setDiseaseTransmissionMode(DiseaseTransmissionMode diseaseTransmissionMode) {
		this.diseaseTransmissionMode = diseaseTransmissionMode;
	}

	public EventReferenceDto getSuperordinateEvent() {
		return superordinateEvent;
	}

	public void setSuperordinateEvent(EventReferenceDto superordinateEvent) {
		this.superordinateEvent = superordinateEvent;
	}

	public InfectionPathCertainty getInfectionPathCertainty() {
		return infectionPathCertainty;
	}

	public void setInfectionPathCertainty(InfectionPathCertainty infectionPathCertainty) {
		this.infectionPathCertainty = infectionPathCertainty;
	}

	public HumanTransmissionMode getHumanTransmissionMode() {
		return humanTransmissionMode;
	}

	public void setHumanTransmissionMode(HumanTransmissionMode humanTransmissionMode) {
		this.humanTransmissionMode = humanTransmissionMode;
	}

	public ParenteralTransmissionMode getParenteralTransmissionMode() {
		return parenteralTransmissionMode;
	}

	public void setParenteralTransmissionMode(ParenteralTransmissionMode parenteralTransmissionMode) {
		this.parenteralTransmissionMode = parenteralTransmissionMode;
	}

	public MedicallyAssociatedTransmissionMode getMedicallyAssociatedTransmissionMode() {
		return medicallyAssociatedTransmissionMode;
	}

	public void setMedicallyAssociatedTransmissionMode(MedicallyAssociatedTransmissionMode medicallyAssociatedTransmissionMode) {
		this.medicallyAssociatedTransmissionMode = medicallyAssociatedTransmissionMode;
	}

	@Override
	public SormasToSormasOriginInfoDto getSormasToSormasOriginInfo() {
		return sormasToSormasOriginInfo;
	}

	@Override
	public void setSormasToSormasOriginInfo(SormasToSormasOriginInfoDto sormasToSormasOriginInfo) {
		this.sormasToSormasOriginInfo = sormasToSormasOriginInfo;
	}

	@Override
	public boolean isOwnershipHandedOver() {
		return ownershipHandedOver;
	}

	public void setOwnershipHandedOver(boolean ownershipHandedOver) {
		this.ownershipHandedOver = ownershipHandedOver;
	}

	public EventManagementStatus getEventManagementStatus() {
		return eventManagementStatus;
	}

	public void setEventManagementStatus(EventManagementStatus eventManagementStatus) {
		this.eventManagementStatus = eventManagementStatus;
	}

	public String getInternalId() {
		return internalId;
	}

	public void setInternalId(String internalId) {
		this.internalId = internalId;
	}

	public EventReferenceDto toReference() {
		return new EventReferenceDto(getUuid());
	}
}
