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

package de.symeda.sormas.backend.sormastosormas.processed;

import static de.symeda.sormas.backend.sormastosormas.processed.ValidationHelper.buildPathogenTestValidationGroupName;
import static de.symeda.sormas.backend.sormastosormas.processed.ValidationHelper.buildSampleValidationGroupName;
import static de.symeda.sormas.backend.sormastosormas.processed.ValidationHelper.buildValidationGroupName;
import static de.symeda.sormas.backend.sormastosormas.processed.ValidationHelper.handleValidationError;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

import de.symeda.sormas.api.HasUuid;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.sample.AdditionalTestDto;
import de.symeda.sormas.api.sample.PathogenTestDto;
import de.symeda.sormas.api.sample.SampleDto;
import de.symeda.sormas.api.sormastosormas.SormasToSormasOriginInfoDto;
import de.symeda.sormas.api.sormastosormas.SormasToSormasOriginInfoFacade;
import de.symeda.sormas.api.sormastosormas.SormasToSormasSampleDto;
import de.symeda.sormas.api.sormastosormas.SormasToSormasValidationException;
import de.symeda.sormas.api.utils.SormasToSormasEntityDto;
import de.symeda.sormas.backend.sample.AdditionalTestFacadeEjb;
import de.symeda.sormas.backend.sample.PathogenTestFacadeEjb;
import de.symeda.sormas.backend.sample.SampleFacadeEjb;
import de.symeda.sormas.backend.sormastosormas.origin.SormasToSormasOriginInfoFacadeEjb.SormasToSormasOriginInfoFacadeEjbLocal;
import de.symeda.sormas.backend.sormastosormas.share.shareinfo.SormasToSormasShareInfo;
import de.symeda.sormas.backend.sormastosormas.share.shareinfo.SormasToSormasShareInfoService;

@Stateless
@LocalBean
public class ProcessedDataPersisterHelper {

	@EJB
	private SampleFacadeEjb.SampleFacadeEjbLocal sampleFacade;
	@EJB
	private PathogenTestFacadeEjb.PathogenTestFacadeEjbLocal pathogenTestFacade;
	@EJB
	private AdditionalTestFacadeEjb.AdditionalTestFacadeEjbLocal additionalTestFacade;

	@EJB
	private SormasToSormasOriginInfoFacadeEjbLocal oriInfoFacade;

	@EJB
	private SormasToSormasShareInfoService shareInfoService;

	public void persistSamples(List<SormasToSormasSampleDto> samples, Consumer<SampleDto> beforeSaveSample) throws SormasToSormasValidationException {

		for (SormasToSormasSampleDto sormasToSormasSample : samples) {
			SampleDto sample = sormasToSormasSample.getSample();

			if (beforeSaveSample != null) {
				beforeSaveSample.accept(sample);
			}

			handleValidationError(() -> sampleFacade.saveSample(sample, true, false), Captions.Sample, buildSampleValidationGroupName(sample));

			for (PathogenTestDto pathogenTest : sormasToSormasSample.getPathogenTests()) {
				handleValidationError(
					() -> pathogenTestFacade.savePathogenTest(pathogenTest, false),
					Captions.PathogenTest,
					buildPathogenTestValidationGroupName(pathogenTest));
			}

			for (AdditionalTestDto additionalTest : sormasToSormasSample.getAdditionalTests()) {
				handleValidationError(
					() -> additionalTestFacade.saveAdditionalTest(additionalTest, false),
					Captions.AdditionalTest,
					buildValidationGroupName(Captions.AdditionalTest, additionalTest));
			}
		}
	}

	public <T extends SormasToSormasEntityDto, U extends SormasToSormasEntityDto> void sharedAssociatedEntityCallback(U parent, T entity) {
		entity.setSormasToSormasOriginInfo(parent.getSormasToSormasOriginInfo());
	}

	public <T extends SormasToSormasEntityDto, U extends SormasToSormasEntityDto> void syncedAssociatedEntityCallback(U parent, T entity) {
		if (entity.getSormasToSormasOriginInfo() == null) {
			entity.setSormasToSormasOriginInfo(parent.getSormasToSormasOriginInfo());
		}
	}

	public ReturnedAssociatedEntityCallback createReturnedAssociatedEntityCallback(SormasToSormasOriginInfoDto originInfo) {
		return new ReturnedAssociatedEntityCallback(originInfo, oriInfoFacade, shareInfoService);
	}

	public static class ReturnedAssociatedEntityCallback {

		private boolean originInfoSaved;

		private final SormasToSormasOriginInfoDto originInfo;

		private final SormasToSormasOriginInfoFacade oriInfoFacade;
		private final SormasToSormasShareInfoService shareInfoService;

		public ReturnedAssociatedEntityCallback(
			SormasToSormasOriginInfoDto originInfo,
			SormasToSormasOriginInfoFacade oriInfoFacade,
			SormasToSormasShareInfoService shareInfoService) {
			this.originInfo = originInfo;
			this.oriInfoFacade = oriInfoFacade;
			this.shareInfoService = shareInfoService;
		}

		public <T extends HasUuid & SormasToSormasEntityDto> void apply(T entity, BiFunction<String, String, SormasToSormasShareInfo> findShareInfo) {
			SormasToSormasShareInfo shareInfo = findShareInfo.apply(entity.getUuid(), originInfo.getOrganizationId());
			if (shareInfo == null) {
				if (!originInfoSaved) {
					oriInfoFacade.saveOriginInfo(originInfo);
					originInfoSaved = true;
				}

				entity.setSormasToSormasOriginInfo(originInfo);
			} else {
				shareInfo.setOwnershipHandedOver(false);
				shareInfoService.persist(shareInfo);
			}
		}
	}
}
