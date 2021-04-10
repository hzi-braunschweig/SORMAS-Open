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
package de.symeda.sormas.api.sample;

import java.util.Date;
import java.util.Set;

import de.symeda.sormas.api.ImportIgnore;
import de.symeda.sormas.api.caze.CaseReferenceDto;
import de.symeda.sormas.api.contact.ContactReferenceDto;
import de.symeda.sormas.api.event.EventParticipantReferenceDto;
import de.symeda.sormas.api.facility.FacilityReferenceDto;
import de.symeda.sormas.api.sormastosormas.SormasToSormasOriginInfoDto;
import de.symeda.sormas.api.user.UserReferenceDto;
import de.symeda.sormas.api.utils.DataHelper;
import javax.validation.constraints.NotNull;
import de.symeda.sormas.api.utils.SensitiveData;
import de.symeda.sormas.api.utils.SormasToSormasEntityDto;
import de.symeda.sormas.api.utils.pseudonymization.PseudonymizableDto;

public class SampleDto extends PseudonymizableDto implements SormasToSormasEntityDto {

	private static final long serialVersionUID = -6975445672442728938L;

	public static final String I18N_PREFIX = "Sample";

	public static final String ASSOCIATED_CASE = "associatedCase";
	public static final String LAB_SAMPLE_ID = "labSampleID";
	public static final String FIELD_SAMPLE_ID = "fieldSampleID";
	public static final String SAMPLE_DATE_TIME = "sampleDateTime";
	public static final String REPORT_DATE_TIME = "reportDateTime";
	public static final String REPORTING_USER = "reportingUser";
	public static final String SAMPLE_MATERIAL = "sampleMaterial";
	public static final String SAMPLE_MATERIAL_TEXT = "sampleMaterialText";
	public static final String LAB = "lab";
	public static final String LAB_DETAILS = "labDetails";
	public static final String SAMPLE_PURPOSE = "samplePurpose";
	public static final String SHIPMENT_DATE = "shipmentDate";
	public static final String SHIPMENT_DETAILS = "shipmentDetails";
	public static final String RECEIVED_DATE = "receivedDate";
	public static final String SPECIMEN_CONDITION = "specimenCondition";
	public static final String NO_TEST_POSSIBLE_REASON = "noTestPossibleReason";
	public static final String COMMENT = "comment";
	public static final String SAMPLE_SOURCE = "sampleSource";
	public static final String REFERRED_TO = "referredTo";
	public static final String SHIPPED = "shipped";
	public static final String RECEIVED = "received";
	public static final String PATHOGEN_TESTING_REQUESTED = "pathogenTestingRequested";
	public static final String ADDITIONAL_TESTING_REQUESTED = "additionalTestingRequested";
	public static final String REQUESTED_PATHOGEN_TESTS = "requestedPathogenTests";
	public static final String REQUESTED_ADDITIONAL_TESTS = "requestedAdditionalTests";
	public static final String PATHOGEN_TEST_RESULT = "pathogenTestResult";
	public static final String REQUESTED_OTHER_PATHOGEN_TESTS = "requestedOtherPathogenTests";
	public static final String REQUESTED_OTHER_ADDITIONAL_TESTS = "requestedOtherAdditionalTests";
	public static final String SAMPLING_REASON = "samplingReason";
	public static final String SAMPLING_REASON_DETAILS = "samplingReasonDetails";

	private CaseReferenceDto associatedCase;
	private ContactReferenceDto associatedContact;
	private EventParticipantReferenceDto associatedEventParticipant;
	private String labSampleID;
	private String fieldSampleID;
	@NotNull
	private Date sampleDateTime;

	@NotNull
	private Date reportDateTime;
	@NotNull
	private UserReferenceDto reportingUser;
	@SensitiveData
	private Double reportLat;
	@SensitiveData
	private Double reportLon;

	private Float reportLatLonAccuracy;

	@NotNull
	private SampleMaterial sampleMaterial;
	@SensitiveData
	private String sampleMaterialText;
	@NotNull
	private SamplePurpose samplePurpose;

	private FacilityReferenceDto lab;
	@SensitiveData
	private String labDetails;
	private Date shipmentDate;
	@SensitiveData
	private String shipmentDetails;
	private Date receivedDate;
	private SpecimenCondition specimenCondition;
	@SensitiveData
	private String noTestPossibleReason;
	@SensitiveData
	private String comment;
	private SampleSource sampleSource;
	private SampleReferenceDto referredTo;
	private boolean shipped;
	private boolean received;
	private PathogenTestResultType pathogenTestResult;

	private Boolean pathogenTestingRequested;
	private Boolean additionalTestingRequested;
	private Set<PathogenTestType> requestedPathogenTests;
	private Set<AdditionalTestType> requestedAdditionalTests;
	private String requestedOtherPathogenTests;
	private String requestedOtherAdditionalTests;

	private SamplingReason samplingReason;
	@SensitiveData
	private String samplingReasonDetails;

	private SormasToSormasOriginInfoDto sormasToSormasOriginInfo;
	private boolean ownershipHandedOver;

	@ImportIgnore
	public CaseReferenceDto getAssociatedCase() {
		return associatedCase;
	}

	public void setAssociatedCase(CaseReferenceDto associatedCase) {
		this.associatedCase = associatedCase;
	}

	@ImportIgnore
	public ContactReferenceDto getAssociatedContact() {
		return associatedContact;
	}

	public void setAssociatedContact(ContactReferenceDto associatedContact) {
		this.associatedContact = associatedContact;
	}

	@ImportIgnore
	public EventParticipantReferenceDto getAssociatedEventParticipant() {
		return associatedEventParticipant;
	}

	public void setAssociatedEventParticipant(EventParticipantReferenceDto associatedEventParticipant) {
		this.associatedEventParticipant = associatedEventParticipant;
	}

	public String getLabSampleID() {
		return labSampleID;
	}

	public void setLabSampleID(String labSampleID) {
		this.labSampleID = labSampleID;
	}

	public String getFieldSampleID() {
		return fieldSampleID;
	}

	public void setFieldSampleID(String fieldSampleID) {
		this.fieldSampleID = fieldSampleID;
	}

	public Date getSampleDateTime() {
		return sampleDateTime;
	}

	public void setSampleDateTime(Date sampleDateTime) {
		this.sampleDateTime = sampleDateTime;
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

	public SampleMaterial getSampleMaterial() {
		return sampleMaterial;
	}

	public void setSampleMaterial(SampleMaterial sampleMaterial) {
		this.sampleMaterial = sampleMaterial;
	}

	public String getSampleMaterialText() {
		return sampleMaterialText;
	}

	public void setSampleMaterialText(String sampleMaterialText) {
		this.sampleMaterialText = sampleMaterialText;
	}

	public SamplePurpose getSamplePurpose() {
		return samplePurpose;
	}

	public void setSamplePurpose(SamplePurpose samplePurpose) {
		this.samplePurpose = samplePurpose;
	}

	public FacilityReferenceDto getLab() {
		return lab;
	}

	public void setLab(FacilityReferenceDto lab) {
		this.lab = lab;
	}

	public String getLabDetails() {
		return labDetails;
	}

	public void setLabDetails(String labDetails) {
		this.labDetails = labDetails;
	}

	public Date getShipmentDate() {
		return shipmentDate;
	}

	public void setShipmentDate(Date shipmentDate) {
		this.shipmentDate = shipmentDate;
	}

	public String getShipmentDetails() {
		return shipmentDetails;
	}

	public void setShipmentDetails(String shipmentDetails) {
		this.shipmentDetails = shipmentDetails;
	}

	public Date getReceivedDate() {
		return receivedDate;
	}

	public void setReceivedDate(Date receivedDate) {
		this.receivedDate = receivedDate;
	}

	public SpecimenCondition getSpecimenCondition() {
		return specimenCondition;
	}

	public void setSpecimenCondition(SpecimenCondition specimenCondition) {
		this.specimenCondition = specimenCondition;
	}

	public String getNoTestPossibleReason() {
		return noTestPossibleReason;
	}

	public void setNoTestPossibleReason(String noTestPossibleReason) {
		this.noTestPossibleReason = noTestPossibleReason;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public SampleSource getSampleSource() {
		return sampleSource;
	}

	public void setSampleSource(SampleSource sampleSource) {
		this.sampleSource = sampleSource;
	}

	@ImportIgnore
	public SampleReferenceDto getReferredTo() {
		return referredTo;
	}

	public void setReferredTo(SampleReferenceDto referredTo) {
		this.referredTo = referredTo;
	}

	public boolean isShipped() {
		return shipped;
	}

	public void setShipped(boolean shipped) {
		this.shipped = shipped;
	}

	public boolean isReceived() {
		return received;
	}

	public void setReceived(boolean received) {
		this.received = received;
	}

	public PathogenTestResultType getPathogenTestResult() {
		return pathogenTestResult;
	}

	public void setPathogenTestResult(PathogenTestResultType pathogenTestResult) {
		this.pathogenTestResult = pathogenTestResult;
	}

	@ImportIgnore
	public Boolean getPathogenTestingRequested() {
		return pathogenTestingRequested;
	}

	public void setPathogenTestingRequested(Boolean pathogenTestingRequested) {
		this.pathogenTestingRequested = pathogenTestingRequested;
	}

	@ImportIgnore
	public Boolean getAdditionalTestingRequested() {
		return additionalTestingRequested;
	}

	public void setAdditionalTestingRequested(Boolean additionalTestingRequested) {
		this.additionalTestingRequested = additionalTestingRequested;
	}

	@ImportIgnore
	public Set<PathogenTestType> getRequestedPathogenTests() {
		return requestedPathogenTests;
	}

	public void setRequestedPathogenTests(Set<PathogenTestType> requestedPathogenTests) {
		this.requestedPathogenTests = requestedPathogenTests;
	}

	@ImportIgnore
	public Set<AdditionalTestType> getRequestedAdditionalTests() {
		return requestedAdditionalTests;
	}

	public void setRequestedAdditionalTests(Set<AdditionalTestType> requestedAdditionalTests) {
		this.requestedAdditionalTests = requestedAdditionalTests;
	}

	@ImportIgnore
	public String getRequestedOtherPathogenTests() {
		return requestedOtherPathogenTests;
	}

	public void setRequestedOtherPathogenTests(String requestedOtherPathogenTests) {
		this.requestedOtherPathogenTests = requestedOtherPathogenTests;
	}

	@ImportIgnore
	public String getRequestedOtherAdditionalTests() {
		return requestedOtherAdditionalTests;
	}

	public void setRequestedOtherAdditionalTests(String requestedOtherAdditionalTests) {
		this.requestedOtherAdditionalTests = requestedOtherAdditionalTests;
	}

	public SamplingReason getSamplingReason() {
		return samplingReason;
	}

	public void setSamplingReason(SamplingReason samplingReason) {
		this.samplingReason = samplingReason;
	}

	public String getSamplingReasonDetails() {
		return samplingReasonDetails;
	}

	public void setSamplingReasonDetails(String samplingReasonDetails) {
		this.samplingReasonDetails = samplingReasonDetails;
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

	public static SampleDto build(UserReferenceDto userRef, CaseReferenceDto caseRef) {

		final SampleDto sampleDto = getSampleDto(userRef);
		sampleDto.setAssociatedCase(caseRef);
		return sampleDto;
	}

	public static SampleDto build(UserReferenceDto userRef, EventParticipantReferenceDto eventParticipantRef) {

		final SampleDto sampleDto = getSampleDto(userRef);
		sampleDto.setAssociatedEventParticipant(eventParticipantRef);
		return sampleDto;
	}

	public static SampleDto build(UserReferenceDto userRef, ContactReferenceDto contactRef) {

		final SampleDto sampleDto = getSampleDto(userRef);
		sampleDto.setAssociatedContact(contactRef);
		return sampleDto;
	}

	private static SampleDto getSampleDto(UserReferenceDto userRef) {

		SampleDto sample = new SampleDto();
		sample.setUuid(DataHelper.createUuid());

		sample.setReportingUser(userRef);
		sample.setReportDateTime(new Date());
		sample.setPathogenTestResult(PathogenTestResultType.PENDING);

		return sample;
	}

	public static SampleDto buildReferral(UserReferenceDto userRef, SampleDto referredSample) {

		final SampleDto sample;
		final CaseReferenceDto associatedCase = referredSample.getAssociatedCase();
		final ContactReferenceDto associatedContact = referredSample.getAssociatedContact();
		final EventParticipantReferenceDto associatedEventParticipant = referredSample.getAssociatedEventParticipant();
		if (associatedCase != null) {
			sample = build(userRef, associatedCase);
		} else if (associatedContact != null) {
			sample = build(userRef, associatedContact);
		} else {
			sample = build(userRef, associatedEventParticipant);
		}
		sample.setSampleDateTime(referredSample.getSampleDateTime());
		sample.setSampleMaterial(referredSample.getSampleMaterial());
		sample.setSampleMaterialText(referredSample.getSampleMaterialText());
		sample.setSampleSource(referredSample.getSampleSource());
		sample.setPathogenTestingRequested(referredSample.getPathogenTestingRequested());
		sample.setAdditionalTestingRequested(referredSample.getAdditionalTestingRequested());
		sample.setRequestedPathogenTests(referredSample.getRequestedPathogenTests());
		sample.setRequestedAdditionalTests(referredSample.getRequestedAdditionalTests());
		sample.setPathogenTestResult(PathogenTestResultType.PENDING);

		return sample;
	}

	@ImportIgnore
	public Double getReportLat() {
		return reportLat;
	}

	public void setReportLat(Double reportLat) {
		this.reportLat = reportLat;
	}

	@ImportIgnore
	public Double getReportLon() {
		return reportLon;
	}

	public void setReportLon(Double reportLon) {
		this.reportLon = reportLon;
	}

	@ImportIgnore
	public Float getReportLatLonAccuracy() {
		return reportLatLonAccuracy;
	}

	public void setReportLatLonAccuracy(Float reportLatLonAccuracy) {
		this.reportLatLonAccuracy = reportLatLonAccuracy;
	}

	public SampleReferenceDto toReference() {
		return new SampleReferenceDto(getUuid());
	}
}
