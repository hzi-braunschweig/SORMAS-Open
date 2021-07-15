/*
 *  SORMAS® - Surveillance Outbreak Response Management & Analysis System
 *  Copyright © 2016-2021 Helmholtz-Zentrum für Infektionsforschung GmbH (HZI)
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 */

package de.symeda.sormas.backend.immunization;

import static de.symeda.sormas.api.EntityDto.COLUMN_LENGTH_DEFAULT;

import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import de.symeda.auditlog.api.Audited;
import de.symeda.sormas.api.Disease;
import de.symeda.sormas.api.immunization.ImmunizationManagementStatus;
import de.symeda.sormas.api.immunization.ImmunizationStatus;
import de.symeda.sormas.api.immunization.MeansOfImmunization;
import de.symeda.sormas.api.utils.YesNoUnknown;
import de.symeda.sormas.backend.caze.Case;
import de.symeda.sormas.backend.common.CoreAdo;
import de.symeda.sormas.backend.person.Person;
import de.symeda.sormas.backend.region.Community;
import de.symeda.sormas.backend.region.Country;
import de.symeda.sormas.backend.region.District;
import de.symeda.sormas.backend.region.Region;
import de.symeda.sormas.backend.user.User;

@Entity(name = "immunization")
@Audited
public class Immunization extends CoreAdo {

	public static final String DISEASE = "disease";
	public static final String PERSON = "person";
	public static final String REPORT_DATE = "reportDate";
	public static final String REPORTING_USER = "reportingUser";
	public static final String ARCHIVED = "archived";
	public static final String IMMUNIZATION_STATUS = "immunizationStatus";
	public static final String MEANS_OF_IMMUNIZATION = "meansOfImmunization";
	public static final String IMMUNIZATION_MANAGEMENT_STATUS = "immunizationManagementStatus";
	public static final String EXTERNAL_ID = "externalId";
	public static final String RESPONSIBLE_REGION = "responsibleRegion";
	public static final String RESPONSIBLE_DISTRICT = "responsibleDistrict";
	public static final String RESPONSIBLE_COMMUNITY = "responsibleCommunity";
	public static final String START_DATE = "startDate";
	public static final String END_DATE = "endDate";
	public static final String NUMBER_OF_DOSES = "numberOfDoses";
	public static final String PREVIOUS_INFECTION = "previousInfection";
	public static final String LAST_INFECTION_DATE = "lastInfectionDate";
	public static final String ADDITIONAL_DETAILS = "additionalDetails";
	public static final String POSITIVE_TEST_RESULT_DATE = "positiveTestResultDate";
	public static final String RECOVERY_DATE = "recoveryDate";
	public static final String RELATED_CASE = "relatedCase";
	
	private Disease disease;
	private Person person;
	private Date reportDate;
	private User reportingUser;
	private boolean archived;
	private ImmunizationStatus immunizationStatus;
	private MeansOfImmunization meansOfImmunization;
	private String meansOfImmunizationDetails;
	private ImmunizationManagementStatus immunizationManagementStatus;
	private String externalId;

	private Region responsibleRegion;
	private District responsibleDistrict;
	private Community responsibleCommunity;

	private Date startDate;
	private Date endDate;
	private Integer numberOfDoses;
	private YesNoUnknown previousInfection;

	private Date lastInfectionDate;
	private String additionalDetails;

	private Date positiveTestResultDate;
	private Date recoveryDate;

	private Case relatedCase;

	private Country country;

	@Enumerated(EnumType.STRING)
	public Disease getDisease() {
		return disease;
	}

	public void setDisease(Disease disease) {
		this.disease = disease;
	}

	@ManyToOne(cascade = {}, fetch = FetchType.EAGER)
	@JoinColumn(nullable = false)
	public Person getPerson() {
		return person;
	}

	public void setPerson(Person person) {
		this.person = person;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(nullable = false)
	public Date getReportDate() {
		return reportDate;
	}

	public void setReportDate(Date reportDate) {
		this.reportDate = reportDate;
	}

	@ManyToOne(cascade = {})
	@JoinColumn(nullable = false)
	public User getReportingUser() {
		return reportingUser;
	}

	public void setReportingUser(User reportingUser) {
		this.reportingUser = reportingUser;
	}

	@Column
	public boolean isArchived() {
		return archived;
	}

	public void setArchived(boolean archived) {
		this.archived = archived;
	}

	@Enumerated(EnumType.STRING)
	public ImmunizationStatus getImmunizationStatus() {
		return immunizationStatus;
	}

	public void setImmunizationStatus(ImmunizationStatus immunizationStatus) {
		this.immunizationStatus = immunizationStatus;
	}

	@Column(length = COLUMN_LENGTH_DEFAULT)
	public String getExternalId() {
		return externalId;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
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

	@Column
	public Integer getNumberOfDoses() {
		return numberOfDoses;
	}

	public void setNumberOfDoses(Integer numberOfDoses) {
		this.numberOfDoses = numberOfDoses;
	}

	@Enumerated(EnumType.STRING)
	public YesNoUnknown getPreviousInfection() {
		return previousInfection;
	}

	public void setPreviousInfection(YesNoUnknown previousInfection) {
		this.previousInfection = previousInfection;
	}

	@Temporal(TemporalType.TIMESTAMP)
	public Date getLastInfectionDate() {
		return lastInfectionDate;
	}

	public void setLastInfectionDate(Date lastInfectionDate) {
		this.lastInfectionDate = lastInfectionDate;
	}

	@Column(columnDefinition = "text")
	public String getAdditionalDetails() {
		return additionalDetails;
	}

	public void setAdditionalDetails(String additionalDetails) {
		this.additionalDetails = additionalDetails;
	}

	@Temporal(TemporalType.TIMESTAMP)
	public Date getPositiveTestResultDate() {
		return positiveTestResultDate;
	}

	public void setPositiveTestResultDate(Date positiveTestResultDate) {
		this.positiveTestResultDate = positiveTestResultDate;
	}

	@Temporal(TemporalType.TIMESTAMP)
	public Date getRecoveryDate() {
		return recoveryDate;
	}

	public void setRecoveryDate(Date recoveryDate) {
		this.recoveryDate = recoveryDate;
	}

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public Case getRelatedCase() {
		return relatedCase;
	}

	public void setRelatedCase(Case relatedCase) {
		this.relatedCase = relatedCase;
	}

	@Enumerated(EnumType.STRING)
	public MeansOfImmunization getMeansOfImmunization() {
		return meansOfImmunization;
	}

	public void setMeansOfImmunization(MeansOfImmunization meansOfImmunization) {
		this.meansOfImmunization = meansOfImmunization;
	}

	@Enumerated(EnumType.STRING)
	public ImmunizationManagementStatus getImmunizationManagementStatus() {
		return immunizationManagementStatus;
	}

	public void setImmunizationManagementStatus(ImmunizationManagementStatus immunizationManagementStatus) {
		this.immunizationManagementStatus = immunizationManagementStatus;
	}

	@Column(columnDefinition = "text")
	public String getMeansOfImmunizationDetails() {
		return meansOfImmunizationDetails;
	}

	public void setMeansOfImmunizationDetails(String meansOfImmunizationDetails) {
		this.meansOfImmunizationDetails = meansOfImmunizationDetails;
	}

	@ManyToOne(cascade = {})
	public Country getCountry() {
		return country;
	}

	public void setCountry(Country country) {
		this.country = country;
	}
}
