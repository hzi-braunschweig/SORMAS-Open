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

package de.symeda.sormas.backend.sormastosormas.caze;

import static de.symeda.sormas.api.sormastosormas.SormasToSormasApiConstants.CASE_ENDPOINT;
import static de.symeda.sormas.api.sormastosormas.SormasToSormasApiConstants.CASE_REQUEST_ENDPOINT;
import static de.symeda.sormas.api.sormastosormas.SormasToSormasApiConstants.CASE_SYNC_ENDPOINT;
import static de.symeda.sormas.api.sormastosormas.SormasToSormasApiConstants.RESOURCE_PATH;
import static de.symeda.sormas.backend.sormastosormas.ValidationHelper.buildCaseValidationGroupName;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

import de.symeda.sormas.api.caze.CaseDataDto;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Strings;
import de.symeda.sormas.api.i18n.Validations;
import de.symeda.sormas.api.sormastosormas.SormasToSormasException;
import de.symeda.sormas.api.sormastosormas.ValidationErrors;
import de.symeda.sormas.api.sormastosormas.caze.SormasToSormasCaseDto;
import de.symeda.sormas.api.sormastosormas.caze.SormasToSormasCaseFacade;
import de.symeda.sormas.api.sormastosormas.sharerequest.ShareRequestDataType;
import de.symeda.sormas.api.sormastosormas.sharerequest.SormasToSormasCasePreview;
import de.symeda.sormas.api.sormastosormas.sharerequest.SormasToSormasShareRequestDto;
import de.symeda.sormas.backend.caze.Case;
import de.symeda.sormas.backend.caze.CaseFacadeEjb.CaseFacadeEjbLocal;
import de.symeda.sormas.backend.caze.CaseService;
import de.symeda.sormas.backend.common.BaseAdoService;
import de.symeda.sormas.backend.sormastosormas.AbstractSormasToSormasInterface;
import de.symeda.sormas.backend.sormastosormas.ProcessedDataPersister;
import de.symeda.sormas.backend.sormastosormas.ShareDataBuilder;
import de.symeda.sormas.backend.sormastosormas.SharedDataProcessor;
import de.symeda.sormas.backend.sormastosormas.SormasToSormasShareInfo;
import de.symeda.sormas.backend.sormastosormas.SormasToSormasShareInfoService;

@Stateless(name = "SormasToSormasCaseFacade")
public class SormasToSormasCaseFacadeEjb
	extends AbstractSormasToSormasInterface<Case, CaseDataDto, SormasToSormasCaseDto, SormasToSormasCasePreview, ProcessedCaseData>
	implements SormasToSormasCaseFacade {

	public static final String SHARED_CASE_REQUEST_ENDPOINT = RESOURCE_PATH + CASE_REQUEST_ENDPOINT;
	public static final String SAVE_SHARED_CASE_ENDPOINT = RESOURCE_PATH + CASE_ENDPOINT;
	public static final String SYNC_CASE_ENDPOINT = RESOURCE_PATH + CASE_SYNC_ENDPOINT;

	@EJB
	private CaseService caseService;
	@EJB
	private CaseShareDataBuilder caseShareDataBuilder;
	@EJB
	private SharedCaseProcessor sharedCaseProcessor;
	@EJB
	private ProcessedCaseDataPersister processedCaseDataPersister;
	@EJB
	private CaseFacadeEjbLocal caseFacade;
	@EJB
	private SormasToSormasShareInfoService shareInfoService;

	public SormasToSormasCaseFacadeEjb() {
		super(
			SHARED_CASE_REQUEST_ENDPOINT,
			SAVE_SHARED_CASE_ENDPOINT,
			SYNC_CASE_ENDPOINT,
			Captions.CaseData,
			ShareRequestDataType.CASE,
			CaseShareDataPreview.class);
	}

	@Override
	protected BaseAdoService<Case> getEntityService() {
		return caseService;
	}

	@Override
	protected ShareDataBuilder<Case, SormasToSormasCaseDto, SormasToSormasCasePreview> getShareDataBuilder() {
		return caseShareDataBuilder;
	}

	@Override
	protected SharedDataProcessor<CaseDataDto, SormasToSormasCaseDto, ProcessedCaseData, SormasToSormasCasePreview> getSharedDataProcessor() {
		return sharedCaseProcessor;
	}

	@Override
	protected ProcessedDataPersister<ProcessedCaseData> getProcessedDataPersister() {
		return processedCaseDataPersister;
	}

	@Override
	protected Class<SormasToSormasCaseDto[]> getShareDataClass() {
		return SormasToSormasCaseDto[].class;
	}

	@Override
	protected void validateEntitiesBeforeSend(List<Case> entities) throws SormasToSormasException {
		Map<String, ValidationErrors> validationErrors = new HashMap<>();
		for (Case caze : entities) {
			if (!caseService.isCaseEditAllowed(caze)) {
				validationErrors.put(
					buildCaseValidationGroupName(caze),
					ValidationErrors
						.create(I18nProperties.getCaption(Captions.CaseData), I18nProperties.getString(Strings.errorSormasToSormasNotEditable)));
			}
		}

		if (validationErrors.size() > 0) {
			throw new SormasToSormasException(I18nProperties.getString(Strings.errorSormasToSormasShare), validationErrors);
		}
	}

	@Override
	protected ValidationErrors validateSharedEntity(CaseDataDto entity) {
		return validateSharedUuid(entity.getUuid());
	}

	@Override
	protected ValidationErrors validateSharedPreview(SormasToSormasCasePreview preview) {
		return validateSharedUuid(preview.getUuid());
	}

	@Override
	protected void setEntityShareInfoAssociatedObject(SormasToSormasShareInfo sormasToSormasShareInfo, Case entity) {
		sormasToSormasShareInfo.setCaze(entity);
	}

	@Override
	protected SormasToSormasShareInfo getShareInfoByEntityAndOrganization(String entityUuid, String organizationId) {
		return shareInfoService.getByCaseAndOrganization(entityUuid, organizationId);
	}

	@Override
	protected List<CaseDataDto> loadExistingEntities(List<String> uuids) {
		return caseFacade.getByUuids(uuids);
	}

	@Override
	protected void setShareRequestPreviewData(SormasToSormasShareRequestDto request, List<SormasToSormasCasePreview> previews) {
		request.setCases(previews);
	}

	private ValidationErrors validateSharedUuid(String uuid) {
		ValidationErrors errors = new ValidationErrors();
		if (caseFacade.exists(uuid)) {
			errors.add(I18nProperties.getCaption(Captions.CaseData), I18nProperties.getValidationError(Validations.sormasToSormasCaseExists));
		}

		return errors;
	}

	@LocalBean
	@Stateless
	public static class SormasToSormasCaseFacadeEjbLocal extends SormasToSormasCaseFacadeEjb {

	}
}
