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

package de.symeda.sormas.api.vaccination;

import java.util.Date;

import de.symeda.sormas.api.caze.Trimester;
import de.symeda.sormas.api.caze.VaccinationInfoSource;
import de.symeda.sormas.api.caze.Vaccine;
import de.symeda.sormas.api.caze.VaccineManufacturer;
import de.symeda.sormas.api.clinicalcourse.HealthConditionsDto;
import de.symeda.sormas.api.immunization.ImmunizationReferenceDto;
import de.symeda.sormas.api.user.UserReferenceDto;
import de.symeda.sormas.api.utils.Required;
import de.symeda.sormas.api.utils.YesNoUnknown;
import de.symeda.sormas.api.utils.pseudonymization.PseudonymizableDto;

public class VaccinationEntityDto extends PseudonymizableDto {

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

	@Required
	private ImmunizationReferenceDto immunization;
	@Required
	private HealthConditionsDto healthConditions;
	@Required
	private Date reportDate;
	private UserReferenceDto reportingUser;
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

	public ImmunizationReferenceDto getImmunization() {
		return immunization;
	}

	public void setImmunization(ImmunizationReferenceDto immunization) {
		this.immunization = immunization;
	}

	public HealthConditionsDto getHealthConditions() {
		return healthConditions;
	}

	public void setHealthConditions(HealthConditionsDto healthConditions) {
		this.healthConditions = healthConditions;
	}

	public Date getReportDate() {
		return reportDate;
	}

	public void setReportDate(Date reportDate) {
		this.reportDate = reportDate;
	}

	public UserReferenceDto getReportingUser() {
		return reportingUser;
	}

	public void setReportingUser(UserReferenceDto reportingUser) {
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
}
