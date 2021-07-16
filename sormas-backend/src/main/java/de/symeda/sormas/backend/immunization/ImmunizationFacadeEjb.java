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

package de.symeda.sormas.backend.immunization;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.validation.constraints.NotNull;

import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Validations;
import de.symeda.sormas.api.immunization.ImmunizationCriteria;
import de.symeda.sormas.api.immunization.ImmunizationDto;
import de.symeda.sormas.api.immunization.ImmunizationFacade;
import de.symeda.sormas.api.immunization.ImmunizationIndexDto;
import de.symeda.sormas.api.immunization.ImmunizationReferenceDto;
import de.symeda.sormas.api.person.PersonReferenceDto;
import de.symeda.sormas.api.utils.SortProperty;
import de.symeda.sormas.api.utils.ValidationRuntimeException;
import de.symeda.sormas.api.vaccination.VaccinationEntityDto;
import de.symeda.sormas.backend.caze.CaseFacadeEjb;
import de.symeda.sormas.backend.caze.CaseService;
import de.symeda.sormas.backend.person.PersonFacadeEjb;
import de.symeda.sormas.backend.person.PersonService;
import de.symeda.sormas.backend.region.CommunityFacadeEjb;
import de.symeda.sormas.backend.region.CommunityService;
import de.symeda.sormas.backend.region.CountryFacadeEjb;
import de.symeda.sormas.backend.region.CountryService;
import de.symeda.sormas.backend.region.DistrictFacadeEjb;
import de.symeda.sormas.backend.region.DistrictService;
import de.symeda.sormas.backend.region.RegionFacadeEjb;
import de.symeda.sormas.backend.region.RegionService;
import de.symeda.sormas.backend.user.User;
import de.symeda.sormas.backend.user.UserService;
import de.symeda.sormas.backend.util.DtoHelper;
import de.symeda.sormas.backend.util.Pseudonymizer;
import de.symeda.sormas.backend.vaccination.VaccinationEntity;
import de.symeda.sormas.backend.vaccination.VaccinationEntityFacadeEjb;

@Stateless(name = "ImmunizationFacade")
public class ImmunizationFacadeEjb implements ImmunizationFacade {

	@EJB
	private ImmunizationService immunizationService;
	@EJB
	private PersonService personService;
	@EJB
	private UserService userService;
	@EJB
	private RegionService regionService;
	@EJB
	private DistrictService districtService;
	@EJB
	private CommunityService communityService;
	@EJB
	private CaseService caseService;
	@EJB
	private CountryService countryService;
	@EJB
	private VaccinationEntityFacadeEjb.VaccinationEntityFacadeEjbLocal vaccinationEntityFacade;

	public static ImmunizationReferenceDto toReferenceDto(Immunization entity) {
		if (entity == null) {
			return null;
		}
		return new ImmunizationReferenceDto(entity.getUuid(), entity.toString(), entity.getExternalId());
	}

	public static ImmunizationReferenceDto toReferenceDto(ImmunizationDto dto) {
		if (dto == null) {
			return null;
		}
		return new ImmunizationReferenceDto(dto.getUuid(), dto.toString(), dto.getExternalId());
	}

	@Override
	public ImmunizationDto getByUuid(String uuid) {
		Pseudonymizer pseudonymizer = Pseudonymizer.getDefaultWithInaccessibleValuePlaceHolder(userService::hasRight);
		return convertToDto(immunizationService.getByUuid(uuid), pseudonymizer);
	}

	@Override
	public void archive(String uuid) {
		Immunization immunization = immunizationService.getByUuid(uuid);
		if (immunization != null) {
			immunization.setArchived(true);
			immunizationService.ensurePersisted(immunization);
		}
	}

	@Override
	public void dearchive(String uuid) {
		Immunization immunization = immunizationService.getByUuid(uuid);
		if (immunization != null) {
			immunization.setArchived(false);
			immunizationService.ensurePersisted(immunization);
		}
	}

	@Override
	public List<ImmunizationDto> getAllAfter(Date date) {
		Pseudonymizer pseudonymizer = Pseudonymizer.getDefaultWithInaccessibleValuePlaceHolder(userService::hasRight);
		return immunizationService.getAllActiveAfter(date).stream().map(c -> convertToDto(c, pseudonymizer)).collect(Collectors.toList());
	}

	@Override
	public List<ImmunizationDto> getByUuids(List<String> uuids) {
		Pseudonymizer pseudonymizer = Pseudonymizer.getDefaultWithInaccessibleValuePlaceHolder(userService::hasRight);
		return immunizationService.getByUuids(uuids).stream().map(c -> convertToDto(c, pseudonymizer)).collect(Collectors.toList());
	}

	@Override
	public List<String> getAllUuids() {
		return immunizationService.getAllUuids();
	}

	@Override
	public List<String> getArchivedUuidsSince(Date since) {

		if (userService.getCurrentUser() == null) {
			return Collections.emptyList();
		}

		return immunizationService.getArchivedUuidsSince(since);
	}

	@Override
	public List<String> getDeletedUuidsSince(Date since) {

		if (userService.getCurrentUser() == null) {
			return Collections.emptyList();
		}
		return immunizationService.getDeletedUuidsSince(since);
	}

	@Override
	public ImmunizationDto save(ImmunizationDto dto) {
		Immunization existingImmunization = dto.getUuid() != null ? immunizationService.getByUuid(dto.getUuid()) : null;
		ImmunizationDto existingDto = toDto(existingImmunization);

		Pseudonymizer pseudonymizer = Pseudonymizer.getDefault(userService::hasRight);
		restorePseudonymizedDto(dto, existingDto, existingImmunization, pseudonymizer);

		validate(dto);

		existingImmunization = fillOrBuildEntity(dto, existingImmunization, true);
		immunizationService.ensurePersisted(existingImmunization);

		return convertToDto(existingImmunization, pseudonymizer);
	}

	public ImmunizationDto convertToDto(Immunization source, Pseudonymizer pseudonymizer) {

		ImmunizationDto dto = toDto(source);

		pseudonymizeDto(source, dto, pseudonymizer);

		return dto;
	}

	private void pseudonymizeDto(Immunization source, ImmunizationDto dto, Pseudonymizer pseudonymizer) {
		if (dto != null) {
			boolean inJurisdiction = immunizationService.inJurisdictionOrOwned(source);
			pseudonymizer.pseudonymizeDto(ImmunizationDto.class, dto, inJurisdiction, c -> {
				User currentUser = userService.getCurrentUser();
				pseudonymizer.pseudonymizeUser(source.getReportingUser(), currentUser, dto::setReportingUser);
				pseudonymizer.pseudonymizeDto(PersonReferenceDto.class, c.getPerson(), inJurisdiction, null);
			});
		}
	}

	private void restorePseudonymizedDto(ImmunizationDto dto, ImmunizationDto existingDto, Immunization immunization, Pseudonymizer pseudonymizer) {
		if (existingDto != null) {
			final boolean inJurisdiction = immunizationService.inJurisdictionOrOwned(immunization);
			final User currentUser = userService.getCurrentUser();
			pseudonymizer.restoreUser(immunization.getReportingUser(), currentUser, dto, dto::setReportingUser);
			pseudonymizer.restorePseudonymizedValues(ImmunizationDto.class, dto, existingDto, inJurisdiction);
		}
	}

	@Override
	public void validate(ImmunizationDto immunizationDto) throws ValidationRuntimeException {

		// Check whether any required field that does not have a not null constraint in the database is empty
		if (immunizationDto.getPerson() == null) {
			throw new ValidationRuntimeException(I18nProperties.getValidationError(Validations.validPerson));
		}
	}

	@Override
	public long count(ImmunizationCriteria criteria) {
		return immunizationService.count((cb, root) -> immunizationService.buildCriteriaFilter(criteria, cb, root));
	}

	@Override
	public List<ImmunizationIndexDto> getIndexList(ImmunizationCriteria criteria, Integer first, Integer max, List<SortProperty> sortProperties) {
		return null;
	}

	public ImmunizationDto toDto(Immunization entity) {
		if (entity == null) {
			return null;
		}
		ImmunizationDto dto = new ImmunizationDto();
		DtoHelper.fillDto(dto, entity);

		dto.setDisease(entity.getDisease());
		dto.setPerson(PersonFacadeEjb.toReferenceDto(entity.getPerson()));
		dto.setReportDate(entity.getReportDate());
		dto.setReportingUser(entity.getReportingUser().toReference());
		dto.setArchived(entity.isArchived());
		dto.setImmunizationStatus(entity.getImmunizationStatus());
		dto.setMeansOfImmunization(entity.getMeansOfImmunization());
		dto.setMeansOfImmunizationDetails(entity.getMeansOfImmunizationDetails());
		dto.setImmunizationManagementStatus(entity.getImmunizationManagementStatus());
		dto.setExternalId(entity.getExternalId());
		dto.setResponsibleRegion(RegionFacadeEjb.toReferenceDto(entity.getResponsibleRegion()));
		dto.setResponsibleDistrict(DistrictFacadeEjb.toReferenceDto(entity.getResponsibleDistrict()));
		dto.setResponsibleCommunity(CommunityFacadeEjb.toReferenceDto(entity.getResponsibleCommunity()));
		dto.setCountry(CountryFacadeEjb.toReferenceDto(entity.getCountry()));
		dto.setStartDate(entity.getStartDate());
		dto.setEndDate(entity.getEndDate());
		dto.setNumberOfDoses(entity.getNumberOfDoses());
		dto.setPreviousInfection(entity.getPreviousInfection());
		dto.setLastInfectionDate(entity.getLastInfectionDate());
		dto.setAdditionalDetails(entity.getAdditionalDetails());
		dto.setPositiveTestResultDate(entity.getPositiveTestResultDate());
		dto.setRecoveryDate(entity.getRecoveryDate());
		dto.setRelatedCase(CaseFacadeEjb.toReferenceDto(entity.getRelatedCase()));

		List<VaccinationEntityDto> vaccinationEntityDtos = new ArrayList<>();
		for (VaccinationEntity vaccinationEntity : entity.getVaccinations()) {
			VaccinationEntityDto vaccinationEntityDto = vaccinationEntityFacade.toDto(vaccinationEntity);
			vaccinationEntityDtos.add(vaccinationEntityDto);
		}
		dto.setVaccinations(vaccinationEntityDtos);

		return dto;
	}

	private Immunization fillOrBuildEntity(@NotNull ImmunizationDto source, Immunization target, boolean checkChangeDate) {
		target = DtoHelper.fillOrBuildEntity(source, target, Immunization::new, checkChangeDate);

		target.setDisease(source.getDisease());
		target.setPerson(personService.getByReferenceDto(source.getPerson()));
		target.setReportDate(source.getReportDate());
		target.setReportingUser(userService.getByReferenceDto(source.getReportingUser()));
		target.setArchived(source.isArchived());
		target.setImmunizationStatus(source.getImmunizationStatus());
		target.setMeansOfImmunization(source.getMeansOfImmunization());
		target.setMeansOfImmunizationDetails(source.getMeansOfImmunizationDetails());
		target.setImmunizationManagementStatus(source.getImmunizationManagementStatus());
		target.setExternalId(source.getExternalId());
		target.setResponsibleRegion(regionService.getByReferenceDto(source.getResponsibleRegion()));
		target.setResponsibleDistrict(districtService.getByReferenceDto(source.getResponsibleDistrict()));
		target.setResponsibleCommunity(communityService.getByReferenceDto(source.getResponsibleCommunity()));
		target.setCountry(countryService.getByReferenceDto(source.getCountry()));
		target.setStartDate(source.getStartDate());
		target.setEndDate(source.getEndDate());
		target.setNumberOfDoses(source.getNumberOfDoses());
		target.setPreviousInfection(source.getPreviousInfection());
		target.setLastInfectionDate(source.getLastInfectionDate());
		target.setAdditionalDetails(source.getAdditionalDetails());
		target.setPositiveTestResultDate(source.getPositiveTestResultDate());
		target.setRecoveryDate(source.getRecoveryDate());
		target.setRelatedCase(caseService.getByReferenceDto(source.getRelatedCase()));

		List<VaccinationEntity> vaccinationEntities = new ArrayList<>();
		for (VaccinationEntityDto vaccinationEntityDto : source.getVaccinations()) {
			VaccinationEntity vaccinationEntity = vaccinationEntityFacade.fromDto(vaccinationEntityDto, checkChangeDate);
			vaccinationEntity.setImmunization(target);
			vaccinationEntities.add(vaccinationEntity);
		}
		target.getVaccinations().clear();
		target.getVaccinations().addAll(vaccinationEntities);

		return target;
	}

	@LocalBean
	@Stateless
	public static class ImmunizationFacadeEjbLocal extends ImmunizationFacadeEjb {

	}
}
