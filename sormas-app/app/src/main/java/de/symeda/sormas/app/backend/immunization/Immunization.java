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

package de.symeda.sormas.app.backend.immunization;

import static de.symeda.sormas.api.EntityDto.COLUMN_LENGTH_BIG;
import static de.symeda.sormas.api.EntityDto.COLUMN_LENGTH_DEFAULT;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import de.symeda.sormas.api.Disease;
import de.symeda.sormas.api.immunization.ImmunizationManagementStatus;
import de.symeda.sormas.api.immunization.ImmunizationStatus;
import de.symeda.sormas.api.immunization.MeansOfImmunization;
import de.symeda.sormas.api.utils.YesNoUnknown;
import de.symeda.sormas.app.backend.caze.Case;
import de.symeda.sormas.app.backend.common.PseudonymizableAdo;
import de.symeda.sormas.app.backend.person.Person;
import de.symeda.sormas.app.backend.region.Community;
import de.symeda.sormas.app.backend.region.Country;
import de.symeda.sormas.app.backend.region.District;
import de.symeda.sormas.app.backend.region.Region;
import de.symeda.sormas.app.backend.user.User;
import de.symeda.sormas.app.backend.vaccination.VaccinationEntity;

@Entity(name = Immunization.TABLE_NAME)
@DatabaseTable(tableName = Immunization.TABLE_NAME)
public class Immunization extends PseudonymizableAdo {

	public static final String TABLE_NAME = "immunization";
	public static final String I18N_PREFIX = "ImmunizationData";

	@Enumerated(EnumType.STRING)
	private Disease disease;
	@DatabaseField(foreign = true, foreignAutoRefresh = true, canBeNull = false, maxForeignAutoRefreshLevel = 3)
	private Person person;
	@DatabaseField(dataType = DataType.DATE_LONG)
	private Date reportDate;
	@DatabaseField(foreign = true, foreignAutoRefresh = true)
	private User reportingUser;
	@DatabaseField
	private boolean archived;
	@Enumerated(EnumType.STRING)
	private ImmunizationStatus immunizationStatus;
	@Enumerated(EnumType.STRING)
	private MeansOfImmunization meansOfImmunization;
	@Column(length = COLUMN_LENGTH_BIG)
	private String meansOfImmunizationDetails;
	@Enumerated(EnumType.STRING)
	private ImmunizationManagementStatus immunizationManagementStatus;
	@Column(length = COLUMN_LENGTH_DEFAULT)
	private String externalId;

	@DatabaseField(foreign = true, foreignAutoRefresh = true)
	private Region responsibleRegion;
	@DatabaseField(foreign = true, foreignAutoRefresh = true)
	private District responsibleDistrict;
	@DatabaseField(foreign = true, foreignAutoRefresh = true)
	private Community responsibleCommunity;
	@DatabaseField(foreign = true, foreignAutoRefresh = true, maxForeignAutoRefreshLevel = 3)
	private Country country;

	@DatabaseField(dataType = DataType.DATE_LONG)
	private Date startDate;
	@DatabaseField(dataType = DataType.DATE_LONG)
	private Date endDate;
	@DatabaseField
	private Integer numberOfDoses;
	@Enumerated(EnumType.STRING)
	private YesNoUnknown previousInfection;

	@DatabaseField(dataType = DataType.DATE_LONG)
	private Date lastInfectionDate;
	@Column(length = COLUMN_LENGTH_BIG)
	private String additionalDetails;

	@DatabaseField(dataType = DataType.DATE_LONG)
	private Date positiveTestResultDate;
	@DatabaseField(dataType = DataType.DATE_LONG)
	private Date recoveryDate;

	@DatabaseField(foreign = true, foreignAutoRefresh = true)
	private Case relatedCase;

	private List<VaccinationEntity> vaccinations = new ArrayList<>();

	public Disease getDisease() {
		return disease;
	}

	public void setDisease(Disease disease) {
		this.disease = disease;
	}

	public Person getPerson() {
		return person;
	}

	public void setPerson(Person person) {
		this.person = person;
	}

	public Date getReportDate() {
		return reportDate;
	}

	public void setReportDate(Date reportDate) {
		this.reportDate = reportDate;
	}

	public User getReportingUser() {
		return reportingUser;
	}

	public void setReportingUser(User reportingUser) {
		this.reportingUser = reportingUser;
	}

	public boolean isArchived() {
		return archived;
	}

	public void setArchived(boolean archived) {
		this.archived = archived;
	}

	public ImmunizationStatus getImmunizationStatus() {
		return immunizationStatus;
	}

	public void setImmunizationStatus(ImmunizationStatus immunizationStatus) {
		this.immunizationStatus = immunizationStatus;
	}

	public MeansOfImmunization getMeansOfImmunization() {
		return meansOfImmunization;
	}

	public void setMeansOfImmunization(MeansOfImmunization meansOfImmunization) {
		this.meansOfImmunization = meansOfImmunization;
	}

	public ImmunizationManagementStatus getImmunizationManagementStatus() {
		return immunizationManagementStatus;
	}

	public void setImmunizationManagementStatus(ImmunizationManagementStatus immunizationManagementStatus) {
		this.immunizationManagementStatus = immunizationManagementStatus;
	}

	public String getExternalId() {
		return externalId;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	public Region getResponsibleRegion() {
		return responsibleRegion;
	}

	public void setResponsibleRegion(Region responsibleRegion) {
		this.responsibleRegion = responsibleRegion;
	}

	public District getResponsibleDistrict() {
		return responsibleDistrict;
	}

	public void setResponsibleDistrict(District responsibleDistrict) {
		this.responsibleDistrict = responsibleDistrict;
	}

	public Community getResponsibleCommunity() {
		return responsibleCommunity;
	}

	public void setResponsibleCommunity(Community responsibleCommunity) {
		this.responsibleCommunity = responsibleCommunity;
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

	public Integer getNumberOfDoses() {
		return numberOfDoses;
	}

	public void setNumberOfDoses(Integer numberOfDoses) {
		this.numberOfDoses = numberOfDoses;
	}

	public YesNoUnknown getPreviousInfection() {
		return previousInfection;
	}

	public void setPreviousInfection(YesNoUnknown previousInfection) {
		this.previousInfection = previousInfection;
	}

	public Date getLastInfectionDate() {
		return lastInfectionDate;
	}

	public void setLastInfectionDate(Date lastInfectionDate) {
		this.lastInfectionDate = lastInfectionDate;
	}

	public String getAdditionalDetails() {
		return additionalDetails;
	}

	public void setAdditionalDetails(String additionalDetails) {
		this.additionalDetails = additionalDetails;
	}

	public Date getPositiveTestResultDate() {
		return positiveTestResultDate;
	}

	public void setPositiveTestResultDate(Date positiveTestResultDate) {
		this.positiveTestResultDate = positiveTestResultDate;
	}

	public Date getRecoveryDate() {
		return recoveryDate;
	}

	public void setRecoveryDate(Date recoveryDate) {
		this.recoveryDate = recoveryDate;
	}

	public Case getRelatedCase() {
		return relatedCase;
	}

	public void setRelatedCase(Case relatedCase) {
		this.relatedCase = relatedCase;
	}

	public String getMeansOfImmunizationDetails() {
		return meansOfImmunizationDetails;
	}

	public void setMeansOfImmunizationDetails(String meansOfImmunizationDetails) {
		this.meansOfImmunizationDetails = meansOfImmunizationDetails;
	}

	public Country getCountry() {
		return country;
	}

	public void setCountry(Country country) {
		this.country = country;
	}

	public List<VaccinationEntity> getVaccinations() {
		return vaccinations;
	}

	public void setVaccinations(List<VaccinationEntity> vaccinations) {
		this.vaccinations = vaccinations;
	}

	@Override
	public String getI18nPrefix() {
		return I18N_PREFIX;
	}
}
