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

package de.symeda.sormas.app.backend.vaccination;

import static de.symeda.sormas.api.EntityDto.COLUMN_LENGTH_BIG;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import de.symeda.sormas.api.caze.Trimester;
import de.symeda.sormas.api.caze.VaccinationInfoSource;
import de.symeda.sormas.api.caze.Vaccine;
import de.symeda.sormas.api.caze.VaccineManufacturer;
import de.symeda.sormas.api.utils.YesNoUnknown;
import de.symeda.sormas.app.backend.clinicalcourse.HealthConditions;
import de.symeda.sormas.app.backend.common.EmbeddedAdo;
import de.symeda.sormas.app.backend.common.PseudonymizableAdo;
import de.symeda.sormas.app.backend.immunization.Immunization;
import de.symeda.sormas.app.backend.user.User;

@Entity(name = VaccinationEntity.TABLE_NAME)
@DatabaseTable(tableName = VaccinationEntity.TABLE_NAME)
@EmbeddedAdo
public class VaccinationEntity extends PseudonymizableAdo {

	public static final String TABLE_NAME = "vaccination";
	public static final String I18N_PREFIX = "vaccination";

	@DatabaseField(foreign = true, foreignAutoRefresh = true, canBeNull = false)
	private Immunization immunization;
	@DatabaseField(foreign = true, foreignAutoRefresh = true, canBeNull = false)
	private HealthConditions healthConditions;
	@DatabaseField(dataType = DataType.DATE_LONG, canBeNull = false)
	private Date reportDate;
	@DatabaseField(foreign = true, foreignAutoRefresh = true)
	private User reportingUser;
	@DatabaseField(dataType = DataType.DATE_LONG)
	private Date vaccinationDate;
	@Enumerated(EnumType.STRING)
	private Vaccine vaccineName;
	@Column(length = COLUMN_LENGTH_BIG)
	private String otherVaccineName;
	@Column(length = COLUMN_LENGTH_BIG)
	private String vaccineNameDetails;
	@Enumerated(EnumType.STRING)
	private VaccineManufacturer vaccineManufacturer;
	@Column(length = COLUMN_LENGTH_BIG)
	private String otherVaccineManufacturer;
	@Column(length = COLUMN_LENGTH_BIG)
	private String VaccineManufacturerDetails;
	@Column(length = COLUMN_LENGTH_BIG)
	private String vaccineType;
	@Column(length = COLUMN_LENGTH_BIG)
	private String vaccineDose;
	@Column(length = COLUMN_LENGTH_BIG)
	private String vaccineInn;
	@Column(length = COLUMN_LENGTH_BIG)
	private String vaccineBatchNumber;
	@Column(length = COLUMN_LENGTH_BIG)
	private String vaccineUniiCode;
	@Column(length = COLUMN_LENGTH_BIG)
	private String vaccineAtcCode;
	@Enumerated(EnumType.STRING)
	private VaccinationInfoSource vaccinationInfoSource;
	@Enumerated(EnumType.STRING)
	private YesNoUnknown pregnant;
	@Enumerated(EnumType.STRING)
	private Trimester trimester;

	public Immunization getImmunization() {
		return immunization;
	}

	public void setImmunization(Immunization immunization) {
		this.immunization = immunization;
	}

	public HealthConditions getHealthConditions() {
		return healthConditions;
	}

	public void setHealthConditions(HealthConditions healthConditions) {
		this.healthConditions = healthConditions;
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

	public Date getVaccinationDate() {
		return vaccinationDate;
	}

	public void setVaccinationDate(Date vaccinationDate) {
		this.vaccinationDate = vaccinationDate;
	}

	public Vaccine getVaccineName() {
		return vaccineName;
	}

	public void setVaccineName(Vaccine vaccineName) {
		this.vaccineName = vaccineName;
	}

	public String getOtherVaccineName() {
		return otherVaccineName;
	}

	public void setOtherVaccineName(String otherVaccineName) {
		this.otherVaccineName = otherVaccineName;
	}

	public String getVaccineNameDetails() {
		return vaccineNameDetails;
	}

	public void setVaccineNameDetails(String vaccineNameDetails) {
		this.vaccineNameDetails = vaccineNameDetails;
	}

	public VaccineManufacturer getVaccineManufacturer() {
		return vaccineManufacturer;
	}

	public void setVaccineManufacturer(VaccineManufacturer vaccineManufacturer) {
		this.vaccineManufacturer = vaccineManufacturer;
	}

	public String getOtherVaccineManufacturer() {
		return otherVaccineManufacturer;
	}

	public void setOtherVaccineManufacturer(String otherVaccineManufacturer) {
		this.otherVaccineManufacturer = otherVaccineManufacturer;
	}

	public String getVaccineManufacturerDetails() {
		return VaccineManufacturerDetails;
	}

	public void setVaccineManufacturerDetails(String vaccineManufacturerDetails) {
		VaccineManufacturerDetails = vaccineManufacturerDetails;
	}

	public String getVaccineType() {
		return vaccineType;
	}

	public void setVaccineType(String vaccineType) {
		this.vaccineType = vaccineType;
	}

	public String getVaccineDose() {
		return vaccineDose;
	}

	public void setVaccineDose(String vaccineDose) {
		this.vaccineDose = vaccineDose;
	}

	public String getVaccineInn() {
		return vaccineInn;
	}

	public void setVaccineInn(String vaccineInn) {
		this.vaccineInn = vaccineInn;
	}

	public String getVaccineBatchNumber() {
		return vaccineBatchNumber;
	}

	public void setVaccineBatchNumber(String vaccineBatchNumber) {
		this.vaccineBatchNumber = vaccineBatchNumber;
	}

	public String getVaccineUniiCode() {
		return vaccineUniiCode;
	}

	public void setVaccineUniiCode(String vaccineUniiCode) {
		this.vaccineUniiCode = vaccineUniiCode;
	}

	public String getVaccineAtcCode() {
		return vaccineAtcCode;
	}

	public void setVaccineAtcCode(String vaccineAtcCode) {
		this.vaccineAtcCode = vaccineAtcCode;
	}

	public VaccinationInfoSource getVaccinationInfoSource() {
		return vaccinationInfoSource;
	}

	public void setVaccinationInfoSource(VaccinationInfoSource vaccinationInfoSource) {
		this.vaccinationInfoSource = vaccinationInfoSource;
	}

	public YesNoUnknown getPregnant() {
		return pregnant;
	}

	public void setPregnant(YesNoUnknown pregnant) {
		this.pregnant = pregnant;
	}

	public Trimester getTrimester() {
		return trimester;
	}

	public void setTrimester(Trimester trimester) {
		this.trimester = trimester;
	}

	@Override
	public String getI18nPrefix() {
		return I18N_PREFIX;
	}

}
