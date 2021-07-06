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

package de.symeda.sormas.api.immunization;

import java.io.Serializable;
import java.util.Date;

import de.symeda.sormas.api.caze.AgeAndBirthDateDto;
import de.symeda.sormas.api.person.ApproximateAgeType;
import de.symeda.sormas.api.person.Sex;
import de.symeda.sormas.api.utils.PersonalData;
import de.symeda.sormas.api.utils.SensitiveData;
import de.symeda.sormas.api.utils.pseudonymization.PseudonymizableIndexDto;

public class ImmunizationIndexDto extends PseudonymizableIndexDto implements Serializable, Cloneable {

	public static final String I18N_PREFIX = "Immunization";

	public static final String UUID = "uuid";
	public static final String PERSON_UUID = "personUuid";
	public static final String FIRST_NAME = "firstName";
	public static final String LAST_NAME = "lastName";
	public static final String AGE_AND_BIRTH_DATE = "ageAndBirthDate";
	public static final String SEX = "sex";
	public static final String DISTRICT = "district";
	public static String MEANS_OF_IMMUNIZATION = "meansOfImmunization";
	public static String IMMUNIZATION_MANAGEMENT_STATUS = "immunizationManagementStatus";
	public static String IMMUNIZATION_STATUS = "immunizationStatus";
	public static String START_DATE = "startDate";
	public static String END_DATE = "endDate";
	public static String RECOVERY_DATE = "recoveryDate";

	private String uuid;
	private String personUuid;
	@PersonalData
	@SensitiveData
	private String firstName;
	@PersonalData
	@SensitiveData
	private String lastName;
	private AgeAndBirthDateDto ageAndBirthDate;
	private Sex sex;
	private String district;
	private MeansOfImmunization meansOfImmunization;
	private ImmunizationManagementStatus immunizationManagementStatus;
	private ImmunizationStatus immunizationStatus;
	private Date startDate;
	private Date endDate;
	private Date recoveryDate;

	public ImmunizationIndexDto(
		String uuid,
		String personUuid,
		String firstName,
		String lastName,
		Integer age,
		ApproximateAgeType ageType,
		Integer birthdateDD,
		Integer birthdateMM,
		Integer birthdateYYYY,
		Sex sex,
		String district,
		MeansOfImmunization meansOfImmunization,
		ImmunizationManagementStatus immunizationManagementStatus,
		ImmunizationStatus immunizationStatus,
		Date startDate,
		Date endDate,
		Date recoveryDate) {
		this.uuid = uuid;
		this.personUuid = personUuid;
		this.firstName = firstName;
		this.lastName = lastName;
		this.sex = sex;
		this.district = district;
		this.meansOfImmunization = meansOfImmunization;
		this.immunizationManagementStatus = immunizationManagementStatus;
		this.immunizationStatus = immunizationStatus;
		this.startDate = startDate;
		this.endDate = endDate;
		this.recoveryDate = recoveryDate;
		this.ageAndBirthDate = new AgeAndBirthDateDto(age, ageType, birthdateDD, birthdateMM, birthdateYYYY);
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getPersonUuid() {
		return personUuid;
	}

	public void setPersonUuid(String personUuid) {
		this.personUuid = personUuid;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public AgeAndBirthDateDto getAgeAndBirthDate() {
		return ageAndBirthDate;
	}

	public void setAgeAndBirthDate(AgeAndBirthDateDto ageAndBirthDate) {
		this.ageAndBirthDate = ageAndBirthDate;
	}

	public Sex getSex() {
		return sex;
	}

	public void setSex(Sex sex) {
		this.sex = sex;
	}

	public String getDistrict() {
		return district;
	}

	public void setDistrict(String district) {
		this.district = district;
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

	public ImmunizationStatus getImmunizationStatus() {
		return immunizationStatus;
	}

	public void setImmunizationStatus(ImmunizationStatus immunizationStatus) {
		this.immunizationStatus = immunizationStatus;
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

	public Date getRecoveryDate() {
		return recoveryDate;
	}

	public void setRecoveryDate(Date recoveryDate) {
		this.recoveryDate = recoveryDate;
	}
}
