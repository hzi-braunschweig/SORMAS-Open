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
package de.symeda.sormas.backend.sample;

import static de.symeda.sormas.api.EntityDto.COLUMN_LENGTH_BIG;
import static de.symeda.sormas.api.EntityDto.COLUMN_LENGTH_DEFAULT;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.Column;
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

import org.apache.commons.lang3.StringUtils;

import de.symeda.auditlog.api.Audited;
import de.symeda.auditlog.api.AuditedIgnore;
import de.symeda.sormas.api.sample.AdditionalTestType;
import de.symeda.sormas.api.sample.PathogenTestResultType;
import de.symeda.sormas.api.sample.PathogenTestType;
import de.symeda.sormas.api.sample.SampleMaterial;
import de.symeda.sormas.api.sample.SamplePurpose;
import de.symeda.sormas.api.sample.SampleReferenceDto;
import de.symeda.sormas.api.sample.SampleSource;
import de.symeda.sormas.api.sample.SamplingReason;
import de.symeda.sormas.api.sample.SpecimenCondition;
import de.symeda.sormas.backend.caze.Case;
import de.symeda.sormas.backend.common.CoreAdo;
import de.symeda.sormas.backend.contact.Contact;
import de.symeda.sormas.backend.event.EventParticipant;
import de.symeda.sormas.backend.facility.Facility;
import de.symeda.sormas.backend.sormastosormas.entities.SormasToSormasEntity;
import de.symeda.sormas.backend.sormastosormas.origin.SormasToSormasOriginInfo;
import de.symeda.sormas.backend.sormastosormas.share.shareinfo.ShareInfoSample;
import de.symeda.sormas.backend.user.User;

@Entity(name = "samples")
@Audited
public class Sample extends CoreAdo implements SormasToSormasEntity {

	private static final long serialVersionUID = -7196712070188634978L;

	public static final String TABLE_NAME = "samples";

	public static final String ASSOCIATED_CASE = "associatedCase";
	public static final String ASSOCIATED_CONTACT = "associatedContact";
	public static final String ASSOCIATED_EVENT_PARTICIPANT = "associatedEventParticipant";
	public static final String LAB_SAMPLE_ID = "labSampleID";
	public static final String FIELD_SAMPLE_ID = "fieldSampleID";
	public static final String SAMPLE_DATE_TIME = "sampleDateTime";
	public static final String REPORT_DATE_TIME = "reportDateTime";
	public static final String REPORTING_USER = "reportingUser";
	public static final String SAMPLE_MATERIAL = "sampleMaterial";
	public static final String SAMPLE_PURPOSE = "samplePurpose";
	public static final String SAMPLE_MATERIAL_TEXT = "sampleMaterialText";
	public static final String LAB = "lab";
	public static final String LAB_DETAILS = "labDetails";
	public static final String SHIPMENT_DATE = "shipmentDate";
	public static final String SHIPMENT_DETAILS = "shipmentDetails";
	public static final String RECEIVED_DATE = "receivedDate";
	public static final String NO_TEST_POSSIBLE_REASON = "noTestPossibleReason";
	public static final String COMMENT = "comment";
	public static final String SAMPLE_SOURCE = "sampleSource";
	public static final String REFERRED_TO = "referredTo";
	public static final String SHIPPED = "shipped";
	public static final String RECEIVED = "received";
	public static final String SPECIMEN_CONDITION = "specimenCondition";
	public static final String PATHOGEN_TESTING_REQUESTED = "pathogenTestingRequested";
	public static final String ADDITIONAL_TESTING_REQUESTED = "additionalTestingRequested";
	public static final String ADDITIONAL_TESTS = "additionalTests";
	public static final String PATHOGEN_TEST_RESULT = "pathogenTestResult";
	public static final String PATHOGEN_TEST_RESULT_CHANGE_DATE = "pathogenTestResultChangeDate";
	public static final String REQUESTED_PATHOGEN_TESTS_STRING = "requestedPathogenTestsString";
	public static final String REQUESTED_ADDITIONAL_TESTS_STRING = "requestedAdditionalTestsString";
	public static final String REQUESTED_OTHER_PATHOGEN_TESTS = "requestedOtherPathogenTests";
	public static final String REQUESTED_OTHER_ADDITIONAL_TESTS = "requestedOtherAdditionalTests";
	public static final String PATHOGENTESTS = "pathogenTests";
	public static final String SAMPLING_REASON = "samplingReason";
	public static final String SAMPLING_REASON_DETAILS = "samplingReasonDetails";

	private Case associatedCase;
	private Contact associatedContact;
	private EventParticipant associatedEventParticipant;
	private String labSampleID;
	private String fieldSampleID;
	private Date sampleDateTime;

	private Date reportDateTime;
	private User reportingUser;
	private Double reportLat;
	private Double reportLon;
	private Float reportLatLonAccuracy;

	private SampleMaterial sampleMaterial;
	private SamplePurpose samplePurpose;
	private String sampleMaterialText;
	private Facility lab;
	private String labDetails;
	private Date shipmentDate;
	private String shipmentDetails;
	private Date receivedDate;
	private SpecimenCondition specimenCondition;
	private String noTestPossibleReason;
	private String comment;
	private SampleSource sampleSource;
	private Sample referredTo;
	private boolean shipped;
	private boolean received;
	private PathogenTestResultType pathogenTestResult;
	private Date pathogenTestResultChangeDate;

	private Boolean pathogenTestingRequested;
	private Boolean additionalTestingRequested;
	private Set<PathogenTestType> requestedPathogenTests;
	private Set<AdditionalTestType> requestedAdditionalTests;
	private String requestedOtherPathogenTests;
	private String requestedOtherAdditionalTests;
	private String requestedPathogenTestsString;
	private String requestedAdditionalTestsString;
	private SamplingReason samplingReason;
	private String samplingReasonDetails;

	private List<PathogenTest> pathogenTests;
	private List<AdditionalTest> additionalTests;

	private SormasToSormasOriginInfo sormasToSormasOriginInfo;
	private List<ShareInfoSample> shareInfoSamples = new ArrayList<>(0);

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn
	public Case getAssociatedCase() {
		return associatedCase;
	}

	public void setAssociatedCase(Case associatedCase) {
		this.associatedCase = associatedCase;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn
	public Contact getAssociatedContact() {
		return associatedContact;
	}

	public void setAssociatedContact(Contact associatedContact) {
		this.associatedContact = associatedContact;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn
	public EventParticipant getAssociatedEventParticipant() {
		return associatedEventParticipant;
	}

	public void setAssociatedEventParticipant(EventParticipant associatedEventParticipant) {
		this.associatedEventParticipant = associatedEventParticipant;
	}

	@Column(length = COLUMN_LENGTH_DEFAULT)
	public String getLabSampleID() {
		return labSampleID;
	}

	public void setLabSampleID(String labSampleID) {
		this.labSampleID = labSampleID;
	}

	@Column(length = COLUMN_LENGTH_DEFAULT)
	public String getFieldSampleID() {
		return fieldSampleID;
	}

	public void setFieldSampleID(String fieldSampleID) {
		this.fieldSampleID = fieldSampleID;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(nullable = false)
	public Date getSampleDateTime() {
		return sampleDateTime;
	}

	public void setSampleDateTime(Date sampleDateTime) {
		this.sampleDateTime = sampleDateTime;
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

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	public SampleMaterial getSampleMaterial() {
		return sampleMaterial;
	}

	public void setSampleMaterial(SampleMaterial sampleMaterial) {
		this.sampleMaterial = sampleMaterial;
	}

	@Column(length = COLUMN_LENGTH_DEFAULT)
	public String getSampleMaterialText() {
		return sampleMaterialText;
	}

	public void setSampleMaterialText(String sampleMaterialText) {
		this.sampleMaterialText = sampleMaterialText;
	}

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	public SamplePurpose getSamplePurpose() {
		return samplePurpose;
	}

	public void setSamplePurpose(SamplePurpose samplePurpose) {
		this.samplePurpose = samplePurpose;
	}

	@ManyToOne(cascade = {})
	@JoinColumn
	public Facility getLab() {
		return lab;
	}

	public void setLab(Facility lab) {
		this.lab = lab;
	}

	@Column(length = COLUMN_LENGTH_DEFAULT)
	public String getLabDetails() {
		return labDetails;
	}

	public void setLabDetails(String labDetails) {
		this.labDetails = labDetails;
	}

	@Temporal(TemporalType.TIMESTAMP)
	public Date getShipmentDate() {
		return shipmentDate;
	}

	public void setShipmentDate(Date shipmentDate) {
		this.shipmentDate = shipmentDate;
	}

	@Column(length = COLUMN_LENGTH_DEFAULT)
	public String getShipmentDetails() {
		return shipmentDetails;
	}

	public void setShipmentDetails(String shipmentDetails) {
		this.shipmentDetails = shipmentDetails;
	}

	@Temporal(TemporalType.TIMESTAMP)
	public Date getReceivedDate() {
		return receivedDate;
	}

	public void setReceivedDate(Date receivedDate) {
		this.receivedDate = receivedDate;
	}

	@Enumerated(EnumType.STRING)
	public SpecimenCondition getSpecimenCondition() {
		return specimenCondition;
	}

	public void setSpecimenCondition(SpecimenCondition specimenCondition) {
		this.specimenCondition = specimenCondition;
	}

	@Column(length = COLUMN_LENGTH_DEFAULT)
	public String getNoTestPossibleReason() {
		return noTestPossibleReason;
	}

	public void setNoTestPossibleReason(String noTestPossibleReason) {
		this.noTestPossibleReason = noTestPossibleReason;
	}

	@OneToMany(mappedBy = PathogenTest.SAMPLE, fetch = FetchType.LAZY)
	public List<PathogenTest> getPathogenTests() {
		return pathogenTests;
	}

	public void setPathogenTests(List<PathogenTest> pathogenTests) {
		this.pathogenTests = pathogenTests;
	}

	@OneToMany(mappedBy = AdditionalTest.SAMPLE, fetch = FetchType.LAZY)
	public List<AdditionalTest> getAdditionalTests() {
		return additionalTests;
	}

	public void setAdditionalTests(List<AdditionalTest> additionalTests) {
		this.additionalTests = additionalTests;
	}

	@Column(length = COLUMN_LENGTH_BIG)
	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	@Enumerated(EnumType.STRING)
	public SampleSource getSampleSource() {
		return sampleSource;
	}

	public void setSampleSource(SampleSource sampleSource) {
		this.sampleSource = sampleSource;
	}

	@OneToOne(cascade = {})
	@JoinColumn(nullable = true)
	public Sample getReferredTo() {
		return referredTo;
	}

	public void setReferredTo(Sample referredTo) {
		this.referredTo = referredTo;
	}

	@Column
	public boolean isShipped() {
		return shipped;
	}

	public void setShipped(boolean shipped) {
		this.shipped = shipped;
	}

	@Column
	public boolean isReceived() {
		return received;
	}

	public void setReceived(boolean received) {
		this.received = received;
	}

	@Enumerated(EnumType.STRING)
	public PathogenTestResultType getPathogenTestResult() {
		return pathogenTestResult;
	}

	public void setPathogenTestResult(PathogenTestResultType pathogenTestResult) {
		this.pathogenTestResult = pathogenTestResult;
	}

	@Temporal(TemporalType.TIMESTAMP)
	public Date getPathogenTestResultChangeDate() {
		return pathogenTestResultChangeDate;
	}

	public void setPathogenTestResultChangeDate(Date pathogenTestResultChangeDate) {
		this.pathogenTestResultChangeDate = pathogenTestResultChangeDate;
	}

	@Column
	public Boolean getPathogenTestingRequested() {
		return pathogenTestingRequested;
	}

	public void setPathogenTestingRequested(Boolean pathogenTestingRequested) {
		this.pathogenTestingRequested = pathogenTestingRequested;
	}

	@Column
	public Boolean getAdditionalTestingRequested() {
		return additionalTestingRequested;
	}

	public void setAdditionalTestingRequested(Boolean additionalTestingRequested) {
		this.additionalTestingRequested = additionalTestingRequested;
	}

	@Transient
	public Set<PathogenTestType> getRequestedPathogenTests() {
		if (requestedPathogenTests == null) {
			if (StringUtils.isEmpty(requestedPathogenTestsString)) {
				requestedPathogenTests = new HashSet<>();
			} else {
				requestedPathogenTests =
					Arrays.stream(requestedPathogenTestsString.split(",")).map(PathogenTestType::valueOf).collect(Collectors.toSet());
			}
		}
		return requestedPathogenTests;
	}

	public void setRequestedPathogenTests(Set<PathogenTestType> requestedPathogenTests) {
		this.requestedPathogenTests = requestedPathogenTests;

		if (this.requestedPathogenTests == null) {
			return;
		}

		StringBuilder sb = new StringBuilder();
		requestedPathogenTests.stream().forEach(t -> {
			sb.append(t.name());
			sb.append(",");
		});
		if (sb.length() > 0) {
			sb.substring(0, sb.lastIndexOf(","));
		}
		requestedPathogenTestsString = sb.toString();
	}

	@Transient
	public Set<AdditionalTestType> getRequestedAdditionalTests() {
		if (requestedAdditionalTests == null) {
			if (StringUtils.isEmpty(requestedAdditionalTestsString)) {
				requestedAdditionalTests = new HashSet<>();
			} else {
				requestedAdditionalTests =
					Arrays.stream(requestedAdditionalTestsString.split(",")).map(AdditionalTestType::valueOf).collect(Collectors.toSet());
			}
		}
		return requestedAdditionalTests;
	}

	public void setRequestedAdditionalTests(Set<AdditionalTestType> requestedAdditionalTests) {
		this.requestedAdditionalTests = requestedAdditionalTests;

		if (this.requestedAdditionalTests == null) {
			return;
		}

		StringBuilder sb = new StringBuilder();
		requestedAdditionalTests.stream().forEach(t -> {
			sb.append(t.name());
			sb.append(",");
		});
		if (sb.length() > 0) {
			sb.substring(0, sb.lastIndexOf(","));
		}
		requestedAdditionalTestsString = sb.toString();
	}

	public String getRequestedPathogenTestsString() {
		return requestedPathogenTestsString;
	}

	public void setRequestedPathogenTestsString(String requestedPathogenTestsString) {
		this.requestedPathogenTestsString = requestedPathogenTestsString;
		requestedPathogenTests = null;
	}

	public String getRequestedAdditionalTestsString() {
		return requestedAdditionalTestsString;
	}

	public void setRequestedAdditionalTestsString(String requestedAdditionalTestsString) {
		this.requestedAdditionalTestsString = requestedAdditionalTestsString;
		requestedAdditionalTests = null;
	}

	@Column(length = COLUMN_LENGTH_DEFAULT)
	public String getRequestedOtherPathogenTests() {
		return requestedOtherPathogenTests;
	}

	public void setRequestedOtherPathogenTests(String requestedOtherPathogenTests) {
		this.requestedOtherPathogenTests = requestedOtherPathogenTests;
	}

	@Column(length = COLUMN_LENGTH_DEFAULT)
	public String getRequestedOtherAdditionalTests() {
		return requestedOtherAdditionalTests;
	}

	public void setRequestedOtherAdditionalTests(String requestedOtherAdditionalTests) {
		this.requestedOtherAdditionalTests = requestedOtherAdditionalTests;
	}

	@Enumerated(EnumType.STRING)
	public SamplingReason getSamplingReason() {
		return samplingReason;
	}

	public void setSamplingReason(SamplingReason samplingReason) {
		this.samplingReason = samplingReason;
	}

	@Column(columnDefinition = "text")
	public String getSamplingReasonDetails() {
		return samplingReasonDetails;
	}

	public void setSamplingReasonDetails(String samplingReasonDetails) {
		this.samplingReasonDetails = samplingReasonDetails;
	}

	@Override
	public String toString() {
		return SampleReferenceDto.buildCaption(
			getSampleMaterial(),
			getAssociatedCase() != null ? getAssociatedCase().getUuid() : null,
			getAssociatedContact() != null ? getAssociatedContact().getUuid() : null,
			getAssociatedEventParticipant() != null ? getAssociatedEventParticipant().getUuid() : null);
	}

	public SampleReferenceDto toReference() {
		return new SampleReferenceDto(
			getUuid(),
			getSampleMaterial(),
			getAssociatedCase() != null ? getAssociatedCase().getUuid() : null,
			getAssociatedContact() != null ? getAssociatedContact().getUuid() : null,
			getAssociatedEventParticipant() != null ? getAssociatedEventParticipant().getUuid() : null);
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

	@Override
	@ManyToOne(cascade = CascadeType.ALL)
	@AuditedIgnore
	public SormasToSormasOriginInfo getSormasToSormasOriginInfo() {
		return sormasToSormasOriginInfo;
	}

	@Override
	public void setSormasToSormasOriginInfo(SormasToSormasOriginInfo sormasToSormasOriginInfo) {
		this.sormasToSormasOriginInfo = sormasToSormasOriginInfo;
	}

	@OneToMany(mappedBy = ShareInfoSample.SAMPLE, fetch = FetchType.LAZY)
	public List<ShareInfoSample> getShareInfoSamples() {
		return shareInfoSamples;
	}

	public void setShareInfoSamples(List<ShareInfoSample> shareInfoSamples) {
		this.shareInfoSamples = shareInfoSamples;
	}
}
