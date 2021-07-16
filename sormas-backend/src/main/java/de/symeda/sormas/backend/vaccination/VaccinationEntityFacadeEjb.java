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

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.validation.constraints.NotNull;

import de.symeda.sormas.api.utils.ValidationRuntimeException;
import de.symeda.sormas.api.vaccination.VaccinationEntityDto;
import de.symeda.sormas.api.vaccination.VaccinationEntityFacade;
import de.symeda.sormas.backend.clinicalcourse.ClinicalCourseFacadeEjb;
import de.symeda.sormas.backend.immunization.ImmunizationFacadeEjb;
import de.symeda.sormas.backend.immunization.ImmunizationService;
import de.symeda.sormas.backend.user.User;
import de.symeda.sormas.backend.user.UserService;
import de.symeda.sormas.backend.util.DtoHelper;
import de.symeda.sormas.backend.util.Pseudonymizer;

@Stateless(name = "VaccinationEntityFacade")
public class VaccinationEntityFacadeEjb implements VaccinationEntityFacade {

	@EJB
	private UserService userService;
	@EJB
	private ImmunizationService immunizationService;
	@EJB
	private ClinicalCourseFacadeEjb.ClinicalCourseFacadeEjbLocal clinicalCourseFacade;
	@EJB
	private VaccinationEntityService vaccinationService;

	public VaccinationEntityDto save(VaccinationEntityDto dto) {

		VaccinationEntity existingVaccination = dto.getUuid() != null ? vaccinationService.getByUuid(dto.getUuid()) : null;
		VaccinationEntityDto existingDto = toDto(existingVaccination);

		Pseudonymizer pseudonymizer = Pseudonymizer.getDefault(userService::hasRight);
		restorePseudonymizedDto(dto, existingDto, existingVaccination, pseudonymizer);

		validate(dto);

		existingVaccination = fillOrBuildEntity(dto, existingVaccination, true);
		vaccinationService.ensurePersisted(existingVaccination);

		return convertToDto(existingVaccination, pseudonymizer);
	}

	public VaccinationEntityDto convertToDto(VaccinationEntity source, Pseudonymizer pseudonymizer) {

		VaccinationEntityDto dto = toDto(source);

		pseudonymizeDto(source, dto, pseudonymizer);

		return dto;
	}

	private void pseudonymizeDto(VaccinationEntity source, VaccinationEntityDto dto, Pseudonymizer pseudonymizer) {

		if (dto != null) {
			boolean inJurisdiction = immunizationService.inJurisdictionOrOwned(source.getImmunization());
			pseudonymizer.pseudonymizeDto(VaccinationEntityDto.class, dto, inJurisdiction, c -> {

				User currentUser = userService.getCurrentUser();
				pseudonymizer.pseudonymizeUser(source.getReportingUser(), currentUser, dto::setReportingUser);
			});
		}
	}

	private void restorePseudonymizedDto(
		VaccinationEntityDto dto,
		VaccinationEntityDto existingDto,
		VaccinationEntity vaccination,
		Pseudonymizer pseudonymizer) {

		if (existingDto != null) {
			final boolean inJurisdiction = immunizationService.inJurisdictionOrOwned(vaccination.getImmunization());
			final User currentUser = userService.getCurrentUser();
			pseudonymizer.restoreUser(vaccination.getReportingUser(), currentUser, dto, dto::setReportingUser);
			pseudonymizer.restorePseudonymizedValues(VaccinationEntityDto.class, dto, existingDto, inJurisdiction);
		}
	}

	public void validate(VaccinationEntityDto vaccinationDto) {

		if (vaccinationDto.getImmunization() == null) {
			throw new ValidationRuntimeException("i18n TBD");
		}
		if (vaccinationDto.getHealthConditions() == null) {
			throw new ValidationRuntimeException("i18n TBD");
		}
		if (vaccinationDto.getReportDate() == null) {
			throw new ValidationRuntimeException("i18n TBD");
		}
	}

	private VaccinationEntity fillOrBuildEntity(@NotNull VaccinationEntityDto source, VaccinationEntity target, boolean checkChangeDate) {
		target = DtoHelper.fillOrBuildEntity(source, target, VaccinationEntity::new, checkChangeDate);

		target.setImmunization(immunizationService.getByReferenceDto(source.getImmunization()));
		target.setHealthConditions(clinicalCourseFacade.fromHealthConditionsDto(source.getHealthConditions(), checkChangeDate));
		target.setReportDate(source.getReportDate());
		target.setReportingUser(userService.getByReferenceDto(source.getReportingUser()));
		target.setVaccinationDate(source.getVaccinationDate());
		target.setVaccineName(source.getVaccineName());
		target.setOtherVaccineName(source.getOtherVaccineName());
		target.setVaccineNameDetails(source.getVaccineNameDetails());
		target.setVaccineManufacturer(source.getVaccineManufacturer());
		target.setOtherVaccineManufacturer(source.getOtherVaccineManufacturer());
		target.setVaccineManufacturerDetails(source.getVaccineManufacturerDetails());
		target.setVaccineType(source.getVaccineType());
		target.setVaccineDose(source.getVaccineDose());
		target.setVaccineInn(source.getVaccineInn());
		target.setVaccineUniiCode(source.getVaccineUniiCode());
		target.setVaccineAtcCode(source.getVaccineAtcCode());
		target.setVaccinationInfoSource(source.getVaccinationInfoSource());
		target.setPregnant(source.getPregnant());
		target.setTrimester(source.getTrimester());

		return target;
	}

	public VaccinationEntityDto toDto(VaccinationEntity entity) {
		if (entity == null) {
			return null;
		}
		VaccinationEntityDto dto = new VaccinationEntityDto();
		DtoHelper.fillDto(dto, entity);

		dto.setImmunization(ImmunizationFacadeEjb.toReferenceDto(entity.getImmunization()));
		dto.setHealthConditions(ClinicalCourseFacadeEjb.toHealthConditionsDto(entity.getHealthConditions()));
		dto.setReportDate(entity.getReportDate());
		dto.setReportingUser(entity.getReportingUser().toReference());
		dto.setVaccinationDate(entity.getVaccinationDate());
		dto.setVaccineName(entity.getVaccineName());
		dto.setOtherVaccineName(entity.getOtherVaccineName());
		dto.setVaccineNameDetails(entity.getVaccineNameDetails());
		dto.setVaccineManufacturer(entity.getVaccineManufacturer());
		dto.setOtherVaccineManufacturer(entity.getOtherVaccineManufacturer());
		dto.setVaccineManufacturerDetails(entity.getVaccineManufacturerDetails());
		dto.setVaccineType(entity.getVaccineType());
		dto.setVaccineDose(entity.getVaccineDose());
		dto.setVaccineInn(entity.getVaccineInn());
		dto.setVaccineUniiCode(entity.getVaccineUniiCode());
		dto.setVaccineAtcCode(entity.getVaccineAtcCode());
		dto.setVaccinationInfoSource(entity.getVaccinationInfoSource());
		dto.setPregnant(entity.getPregnant());
		dto.setTrimester(entity.getTrimester());

		return dto;
	}

	public VaccinationEntity fromDto(@NotNull VaccinationEntityDto source, boolean checkChangeDate) {
		return fillOrBuildEntity(source, vaccinationService.getByUuid(source.getUuid()), checkChangeDate);
	}

	@LocalBean
	@Stateless
	public static class VaccinationEntityFacadeEjbLocal extends VaccinationEntityFacadeEjb {

	}
}
