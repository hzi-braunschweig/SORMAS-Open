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

package de.symeda.sormas.backend.vaccination;

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
import de.symeda.auditlog.api.AuditedIgnore;
import de.symeda.sormas.api.caze.Trimester;
import de.symeda.sormas.api.caze.VaccinationInfoSource;
import de.symeda.sormas.api.caze.Vaccine;
import de.symeda.sormas.api.caze.VaccineManufacturer;
import de.symeda.sormas.api.utils.YesNoUnknown;
import de.symeda.sormas.backend.clinicalcourse.HealthConditions;
import de.symeda.sormas.backend.common.AbstractDomainObject;
import de.symeda.sormas.backend.immunization.Immunization;
import de.symeda.sormas.backend.user.User;

@Entity(name = "vaccination")
@Audited
public class VaccinationEntity extends AbstractDomainObject {

	public static final String IMMUNIZATION = "immunization";
	public static final String HEALTH_CONDITIONS = "healthConditions";
	public static final String REPORT_DATE = "reportDate";
	public static final String REPORTING_USER = "reportingUser";
	public static final String VACCINATION_DATE = "vaccinationDate";
	public static final String VACCINE_NAME = "vaccineName";
	public static final String OTHER_VACCINE_NAME = "otherVaccineName";
	public static final String VACCINE_NAME_DETAILS = "vaccineNameDetails";
	public static final String VACCINE_MANUFACTURER = "vaccineManufacturer";
	public static final String VACCINE_MANUFACTURER_DETAILS = "vaccineManufacturerDetails";
	public static final String VACCINE_TYPE = "vaccineType";
	public static final String VACCINE_DOSE = "vaccineDose";
	public static final String VACCINE_INN = "vaccineInn";
	public static final String VACCINE_BATCH_NUMBER = "vaccineBatchNumber";
	public static final String VACCINE_UNII_CODE = "vaccineUniiCode";
	public static final String VACCINE_ATC_CODE = "vaccineAtcCode";
	public static final String VACCINATION_INFO_SOURCE = "vaccinationInfoSource";
	public static final String PREGNANT = "pregnant";
	public static final String TRIMESTER = "trimester";

	private Immunization immunization;
	private HealthConditions healthConditions;
	private Date reportDate;
	private User reportingUser;
	private Date vaccinationDate;
	private Vaccine vaccineName;
	private String otherVaccineName;
	private String vaccineNameDetails;
	private VaccineManufacturer vaccineManufacturer;
	private String otherVaccineManufacturer;
	private String VaccineManufacturerDetails;
	private String vaccineType;
	private String vaccineDose;
	private String vaccineInn;
	private String vaccineBatchNumber;
	private String vaccineUniiCode;
	private String vaccineAtcCode;
	private VaccinationInfoSource vaccinationInfoSource;
	private YesNoUnknown pregnant;
	private Trimester trimester;

	@ManyToOne
	@JoinColumn(nullable = false)
	public Immunization getImmunization() {
		return immunization;
	}

	public void setImmunization(Immunization immunization) {
		this.immunization = immunization;
	}

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(nullable = false)
	@AuditedIgnore
	public HealthConditions getHealthConditions() {
		return healthConditions;
	}

	public void setHealthConditions(HealthConditions healthConditions) {
		this.healthConditions = healthConditions;
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
	public User getReportingUser() {
		return reportingUser;
	}

	public void setReportingUser(User reportingUser) {
		this.reportingUser = reportingUser;
	}

	@Temporal(TemporalType.TIMESTAMP)
	public Date getVaccinationDate() {
		return vaccinationDate;
	}

	public void setVaccinationDate(Date vaccinationDate) {
		this.vaccinationDate = vaccinationDate;
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

	@Column(columnDefinition = "text")
	public String getVaccineNameDetails() {
		return vaccineNameDetails;
	}

	public void setVaccineNameDetails(String vaccineNameDetails) {
		this.vaccineNameDetails = vaccineNameDetails;
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
	public String getVaccineManufacturerDetails() {
		return VaccineManufacturerDetails;
	}

	public void setVaccineManufacturerDetails(String vaccineManufacturerDetails) {
		VaccineManufacturerDetails = vaccineManufacturerDetails;
	}

	@Column(columnDefinition = "text")
	public String getVaccineType() {
		return vaccineType;
	}

	public void setVaccineType(String vaccineType) {
		this.vaccineType = vaccineType;
	}

	@Column(columnDefinition = "text")
	public String getVaccineDose() {
		return vaccineDose;
	}

	public void setVaccineDose(String vaccineDose) {
		this.vaccineDose = vaccineDose;
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

	@Enumerated(EnumType.STRING)
	public VaccinationInfoSource getVaccinationInfoSource() {
		return vaccinationInfoSource;
	}

	public void setVaccinationInfoSource(VaccinationInfoSource vaccinationInfoSource) {
		this.vaccinationInfoSource = vaccinationInfoSource;
	}

	@Enumerated(EnumType.STRING)
	public YesNoUnknown getPregnant() {
		return pregnant;
	}

	public void setPregnant(YesNoUnknown pregnant) {
		this.pregnant = pregnant;
	}

	@Enumerated(EnumType.STRING)
	public Trimester getTrimester() {
		return trimester;
	}

	public void setTrimester(Trimester trimester) {
		this.trimester = trimester;
	}

}
