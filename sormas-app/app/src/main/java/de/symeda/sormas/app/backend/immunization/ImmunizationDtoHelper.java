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

import java.util.ArrayList;
import java.util.List;

import de.symeda.sormas.api.PushResult;
import de.symeda.sormas.api.immunization.ImmunizationDto;
import de.symeda.sormas.api.immunization.ImmunizationReferenceDto;
import de.symeda.sormas.api.vaccination.VaccinationEntityDto;
import de.symeda.sormas.app.backend.caze.Case;
import de.symeda.sormas.app.backend.caze.CaseDtoHelper;
import de.symeda.sormas.app.backend.common.AdoDtoHelper;
import de.symeda.sormas.app.backend.common.DatabaseHelper;
import de.symeda.sormas.app.backend.person.Person;
import de.symeda.sormas.app.backend.person.PersonDtoHelper;
import de.symeda.sormas.app.backend.region.Community;
import de.symeda.sormas.app.backend.region.CommunityDtoHelper;
import de.symeda.sormas.app.backend.region.Country;
import de.symeda.sormas.app.backend.region.CountryDtoHelper;
import de.symeda.sormas.app.backend.region.District;
import de.symeda.sormas.app.backend.region.DistrictDtoHelper;
import de.symeda.sormas.app.backend.region.Region;
import de.symeda.sormas.app.backend.region.RegionDtoHelper;
import de.symeda.sormas.app.backend.user.User;
import de.symeda.sormas.app.backend.user.UserDtoHelper;
import de.symeda.sormas.app.backend.vaccination.VaccinationEntity;
import de.symeda.sormas.app.backend.vaccination.VaccinationEntityDtoHelper;
import de.symeda.sormas.app.rest.NoConnectionException;
import de.symeda.sormas.app.rest.RetroProvider;
import retrofit2.Call;

public class ImmunizationDtoHelper extends AdoDtoHelper<Immunization, ImmunizationDto> {

	private VaccinationEntityDtoHelper vaccinationEntityDtoHelper = new VaccinationEntityDtoHelper();

	@Override
	protected Class<Immunization> getAdoClass() {
		return Immunization.class;
	}

	@Override
	protected Class<ImmunizationDto> getDtoClass() {
		return ImmunizationDto.class;
	}

	@Override
	protected Call<List<ImmunizationDto>> pullAllSince(long since) throws NoConnectionException {
		return RetroProvider.getImmunizationFacade().pullAllSince(since);
	}

	@Override
	protected Call<List<ImmunizationDto>> pullByUuids(List<String> uuids) throws NoConnectionException {
		return RetroProvider.getImmunizationFacade().pullByUuids(uuids);
	}

	@Override
	protected Call<List<PushResult>> pushAll(List<ImmunizationDto> immunizationDtos) throws NoConnectionException {
		return RetroProvider.getImmunizationFacade().pushAll(immunizationDtos);
	}

	@Override
	protected void fillInnerFromDto(Immunization target, ImmunizationDto source) {
		target.setDisease(source.getDisease());
		target.setPerson(DatabaseHelper.getPersonDao().getByReferenceDto(source.getPerson()));
		target.setReportDate(source.getReportDate());
		target.setReportingUser(DatabaseHelper.getUserDao().getByReferenceDto(source.getReportingUser()));
		target.setArchived(source.isArchived());
		target.setImmunizationStatus(source.getImmunizationStatus());
		target.setMeansOfImmunization(source.getMeansOfImmunization());
		target.setMeansOfImmunizationDetails(source.getMeansOfImmunizationDetails());
		target.setImmunizationManagementStatus(source.getImmunizationManagementStatus());
		target.setExternalId(source.getExternalId());
		target.setResponsibleRegion(DatabaseHelper.getRegionDao().getByReferenceDto(source.getResponsibleRegion()));
		target.setResponsibleDistrict(DatabaseHelper.getDistrictDao().getByReferenceDto(source.getResponsibleDistrict()));
		target.setResponsibleCommunity(DatabaseHelper.getCommunityDao().getByReferenceDto(source.getResponsibleCommunity()));
		target.setCountry(DatabaseHelper.getCountryDao().getByReferenceDto(source.getCountry()));
		target.setStartDate(source.getStartDate());
		target.setEndDate(source.getEndDate());
		target.setNumberOfDoses(source.getNumberOfDoses());
		target.setPreviousInfection(source.getPreviousInfection());
		target.setLastInfectionDate(source.getLastInfectionDate());
		target.setAdditionalDetails(source.getAdditionalDetails());
		target.setPositiveTestResultDate(source.getPositiveTestResultDate());
		target.setRecoveryDate(source.getRecoveryDate());
		target.setRelatedCase(DatabaseHelper.getCaseDao().getByReferenceDto(source.getRelatedCase()));

		List<VaccinationEntity> vaccinations = new ArrayList<>();
		if (!source.getVaccinations().isEmpty()) {
			for (VaccinationEntityDto vaccinationDto : source.getVaccinations()) {
				VaccinationEntity vaccination = vaccinationEntityDtoHelper.fillOrCreateFromDto(null, vaccinationDto);
				vaccination.setImmunization(target);
				vaccinations.add(vaccination);
			}
		}
		target.setVaccinations(vaccinations);
	}

	@Override
	protected void fillInnerFromAdo(ImmunizationDto target, Immunization source) {
		target.setDisease(source.getDisease());
		if (source.getPerson() != null) {
			Person person = DatabaseHelper.getPersonDao().queryForId(source.getPerson().getId());
			target.setPerson(PersonDtoHelper.toReferenceDto(person));
		}
		target.setReportDate(source.getReportDate());

		if (source.getReportingUser() != null) {
			User user = DatabaseHelper.getUserDao().queryForId(source.getReportingUser().getId());
			target.setReportingUser(UserDtoHelper.toReferenceDto(user));
		}

		target.setArchived(source.isArchived());
		target.setImmunizationStatus(source.getImmunizationStatus());
		target.setMeansOfImmunization(source.getMeansOfImmunization());
		target.setMeansOfImmunizationDetails(source.getMeansOfImmunizationDetails());
		target.setImmunizationManagementStatus(source.getImmunizationManagementStatus());
		target.setExternalId(source.getExternalId());
		if (source.getResponsibleRegion() != null) {
			Region region = DatabaseHelper.getRegionDao().queryForId(source.getResponsibleRegion().getId());
			target.setResponsibleRegion(RegionDtoHelper.toReferenceDto(region));
		}
		if (source.getResponsibleDistrict() != null) {
			District district = DatabaseHelper.getDistrictDao().queryForId(source.getResponsibleDistrict().getId());
			target.setResponsibleDistrict(DistrictDtoHelper.toReferenceDto(district));
		}
		if (source.getResponsibleCommunity() != null) {
			Community community = DatabaseHelper.getCommunityDao().queryForId(source.getResponsibleCommunity().getId());
			target.setResponsibleCommunity(CommunityDtoHelper.toReferenceDto(community));
		}
		if (source.getCountry() != null) {
			Country country = DatabaseHelper.getCountryDao().queryForId(source.getCountry().getId());
			target.setCountry(CountryDtoHelper.toReferenceDto(country));
		}
		target.setStartDate(source.getStartDate());
		target.setEndDate(source.getEndDate());
		target.setNumberOfDoses(source.getNumberOfDoses());
		target.setPreviousInfection(source.getPreviousInfection());
		target.setLastInfectionDate(source.getLastInfectionDate());
		target.setAdditionalDetails(source.getAdditionalDetails());
		target.setPositiveTestResultDate(source.getPositiveTestResultDate());
		target.setRecoveryDate(source.getRecoveryDate());

		if (source.getRelatedCase() != null) {
			Case caze = DatabaseHelper.getCaseDao().queryForId(source.getRelatedCase().getId());
			target.setRelatedCase(CaseDtoHelper.toReferenceDto(caze));
		}

		List<VaccinationEntityDto> vaccinationEntityDtos = new ArrayList<>();
		if (!source.getVaccinations().isEmpty()) {
			for (VaccinationEntity vaccinationEntity : source.getVaccinations()) {
				VaccinationEntityDto vaccinationEntityDto = vaccinationEntityDtoHelper.adoToDto(vaccinationEntity);
				vaccinationEntityDtos.add(vaccinationEntityDto);
			}
		}
		target.setVaccinations(vaccinationEntityDtos);
	}

	public static ImmunizationReferenceDto toReferenceDto(Immunization ado) {
		if (ado == null) {
			return null;
		}
		ImmunizationReferenceDto dto = new ImmunizationReferenceDto(ado.getUuid(), ado.toString(), ado.getExternalId());

		return dto;
	}
}
