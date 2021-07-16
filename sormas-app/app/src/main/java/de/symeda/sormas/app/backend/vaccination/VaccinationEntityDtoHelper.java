package de.symeda.sormas.app.backend.vaccination;

import java.util.List;

import de.symeda.sormas.api.PushResult;
import de.symeda.sormas.api.vaccination.VaccinationEntityDto;
import de.symeda.sormas.app.backend.clinicalcourse.HealthConditions;
import de.symeda.sormas.app.backend.clinicalcourse.HealthConditionsDtoHelper;
import de.symeda.sormas.app.backend.common.AdoDtoHelper;
import de.symeda.sormas.app.backend.common.DatabaseHelper;
import de.symeda.sormas.app.backend.immunization.Immunization;
import de.symeda.sormas.app.backend.immunization.ImmunizationDtoHelper;
import de.symeda.sormas.app.backend.user.User;
import de.symeda.sormas.app.backend.user.UserDtoHelper;
import de.symeda.sormas.app.rest.NoConnectionException;
import retrofit2.Call;

public class VaccinationEntityDtoHelper extends AdoDtoHelper<VaccinationEntity, VaccinationEntityDto> {

	private HealthConditionsDtoHelper healthConditionsDtoHelper = new HealthConditionsDtoHelper();

	@Override
	protected Class<VaccinationEntity> getAdoClass() {
		return VaccinationEntity.class;
	}

	@Override
	protected Class<VaccinationEntityDto> getDtoClass() {
		return VaccinationEntityDto.class;
	}

	@Override
	protected Call<List<VaccinationEntityDto>> pullAllSince(long since) throws NoConnectionException {
		throw new UnsupportedOperationException("Entity is embedded");
	}

	@Override
	protected Call<List<VaccinationEntityDto>> pullByUuids(List<String> uuids) throws NoConnectionException {
		throw new UnsupportedOperationException("Entity is embedded");
	}

	@Override
	protected Call<List<PushResult>> pushAll(List<VaccinationEntityDto> vaccinationEntityDtos) throws NoConnectionException {
		throw new UnsupportedOperationException("Entity is embedded");
	}

	@Override
	protected void fillInnerFromDto(VaccinationEntity target, VaccinationEntityDto source) {

		target.setImmunization(DatabaseHelper.getImmunizationDao().getByReferenceDto(source.getImmunization()));
		target.setHealthConditions(healthConditionsDtoHelper.fillOrCreateFromDto(target.getHealthConditions(), source.getHealthConditions()));
		target.setReportDate(source.getReportDate());
		target.setReportingUser(DatabaseHelper.getUserDao().getByReferenceDto(source.getReportingUser()));
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

		target.setPseudonymized(source.isPseudonymized());
	}

	@Override
	protected void fillInnerFromAdo(VaccinationEntityDto target, VaccinationEntity source) {

		if (source.getImmunization() != null) {
			Immunization immunization = DatabaseHelper.getImmunizationDao().queryForId(source.getImmunization().getId());
			target.setImmunization(ImmunizationDtoHelper.toReferenceDto(immunization));
		}

		if (source.getHealthConditions() != null) {
			HealthConditions healthConditions = DatabaseHelper.getHealthConditionsDao().queryForId(source.getHealthConditions().getId());
			target.setHealthConditions(healthConditionsDtoHelper.adoToDto(healthConditions));
		} else {
			target.setHealthConditions(null);
		}

		target.setReportDate(source.getReportDate());

		if (source.getReportingUser() != null) {
			User user = DatabaseHelper.getUserDao().queryForId(source.getReportingUser().getId());
			target.setReportingUser(UserDtoHelper.toReferenceDto(user));
		} else {
			target.setReportingUser(null);
		}

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

		target.setPseudonymized(source.isPseudonymized());
	}
}
