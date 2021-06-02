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

import static de.symeda.sormas.backend.sormastosormas.ValidationHelper.buildCaseValidationGroupName;
import static de.symeda.sormas.backend.sormastosormas.ValidationHelper.buildContactValidationGroupName;
import static de.symeda.sormas.backend.sormastosormas.ValidationHelper.handleValidationError;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.transaction.Transactional;

import de.symeda.sormas.api.caze.CaseDataDto;
import de.symeda.sormas.api.contact.ContactDto;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.sample.SampleDto;
import de.symeda.sormas.api.sormastosormas.SormasToSormasOriginInfoDto;
import de.symeda.sormas.api.sormastosormas.SormasToSormasValidationException;
import de.symeda.sormas.api.sormastosormas.caze.SormasToSormasCaseDto;
import de.symeda.sormas.backend.caze.CaseFacadeEjb;
import de.symeda.sormas.backend.contact.ContactFacadeEjb;
import de.symeda.sormas.backend.person.PersonFacadeEjb;
import de.symeda.sormas.backend.sormastosormas.ProcessedDataPersister;
import de.symeda.sormas.backend.sormastosormas.ProcessedDataPersisterHelper;
import de.symeda.sormas.backend.sormastosormas.ProcessedDataPersisterHelper.ReturnedAssociatedEntityCallback;
import de.symeda.sormas.backend.sormastosormas.SormasToSormasOriginInfoFacadeEjb.SormasToSormasOriginInfoFacadeEjbLocal;
import de.symeda.sormas.backend.sormastosormas.SormasToSormasShareInfo;
import de.symeda.sormas.backend.sormastosormas.SormasToSormasShareInfoService;

@Stateless
@LocalBean
public class ProcessedCaseDataPersister implements ProcessedDataPersister<ProcessedCaseData> {

	@EJB
	private PersonFacadeEjb.PersonFacadeEjbLocal personFacade;
	@EJB
	private CaseFacadeEjb.CaseFacadeEjbLocal caseFacade;
	@EJB
	private ContactFacadeEjb.ContactFacadeEjbLocal contactFacade;
	@EJB
	private ProcessedDataPersisterHelper dataPersisterHelper;
	@EJB
	private SormasToSormasShareInfoService shareInfoService;
	@EJB
	private SormasToSormasOriginInfoFacadeEjbLocal originInfoFacade;

	@Transactional(rollbackOn = {
		Exception.class })
	public void persistSharedData(ProcessedCaseData processedData) throws SormasToSormasValidationException {
		persistProcessedData(
			processedData,
			null,
			dataPersisterHelper::sharedAssociatedEntityCallback,
			dataPersisterHelper::sharedAssociatedEntityCallback,
			true);
	}

	@Transactional(rollbackOn = {
		Exception.class })
	public void persistReturnedData(ProcessedCaseData processedData, SormasToSormasOriginInfoDto originInfo)
		throws SormasToSormasValidationException {

		ReturnedAssociatedEntityCallback callback = dataPersisterHelper.createReturnedAssociatedEntityCallback(originInfo);

		persistProcessedData(processedData, (caze) -> {
			SormasToSormasShareInfo shareInfo = shareInfoService.getByCaseAndOrganization(caze.getUuid(), originInfo.getOrganizationId());
			shareInfo.setOwnershipHandedOver(false);
			shareInfoService.persist(shareInfo);
		}, (caze, contact) -> {
			callback.apply(contact, shareInfoService::getByContactAndOrganization);
		}, (caze, sample) -> {
			callback.apply(sample, shareInfoService::getBySampleAndOrganization);
		}, false);
	}

	@Transactional(rollbackOn = {
		Exception.class })
	public void persistSyncData(ProcessedCaseData processedData) throws SormasToSormasValidationException {
		SormasToSormasOriginInfoDto originInfo = processedData.getOriginInfo();

		persistProcessedData(processedData, (caze) -> {
			SormasToSormasOriginInfoDto caseOriginInfo = caze.getSormasToSormasOriginInfo();
			caseOriginInfo.setOwnershipHandedOver(originInfo.isOwnershipHandedOver());
			caseOriginInfo.setComment(originInfo.getComment());

			originInfoFacade.saveOriginInfo(caseOriginInfo);
		}, dataPersisterHelper::syncedAssociatedEntityCallback, dataPersisterHelper::syncedAssociatedEntityCallback, false);
	}

	private void persistProcessedData(
		ProcessedCaseData caseData,
		Consumer<CaseDataDto> afterSaveCase,
		BiConsumer<CaseDataDto, ContactDto> beforeSaveContact,
		BiConsumer<CaseDataDto, SampleDto> beforeSaveSample,
		boolean isCreate)
		throws SormasToSormasValidationException {
		CaseDataDto caze = caseData.getEntity();

		final CaseDataDto savedCase;
		if (isCreate) {
			// save person first during creation
			handleValidationError(() -> personFacade.savePerson(caseData.getPerson(), false), Captions.Person, buildCaseValidationGroupName(caze));
			savedCase = handleValidationError(() -> caseFacade.saveCase(caze, true, false), Captions.CaseData, buildCaseValidationGroupName(caze));
		} else {
			//save case first during update
			savedCase = handleValidationError(() -> caseFacade.saveCase(caze, true, false), Captions.CaseData, buildCaseValidationGroupName(caze));
			handleValidationError(() -> personFacade.savePerson(caseData.getPerson(), false), Captions.Person, buildCaseValidationGroupName(caze));
		}

		if (afterSaveCase != null) {
			afterSaveCase.accept(savedCase);
		}

		if (caseData.getAssociatedContacts() != null) {
			for (SormasToSormasCaseDto.AssociatedContactDto associatedContact : caseData.getAssociatedContacts()) {
				ContactDto contact = associatedContact.getContact();

				if (beforeSaveContact != null) {
					beforeSaveContact.accept(savedCase, contact);
				}

				if (isCreate || !contactFacade.exists(contact.getUuid())) {
					// save person first during creation
					handleValidationError(
						() -> personFacade.savePerson(associatedContact.getPerson(), false),
						Captions.Person,
						buildContactValidationGroupName(contact));
					handleValidationError(
						() -> contactFacade.saveContact(contact, true, true, false),
						Captions.Contact,
						buildContactValidationGroupName(contact));
				} else {
					//save contact first during update
					handleValidationError(
						() -> contactFacade.saveContact(contact, true, true, false),
						Captions.Contact,
						buildContactValidationGroupName(contact));
					handleValidationError(
						() -> personFacade.savePerson(associatedContact.getPerson(), false),
						Captions.Person,
						buildContactValidationGroupName(contact));

				}
			}
		}

		if (caseData.getSamples() != null) {
			dataPersisterHelper.persistSamples(caseData.getSamples(), beforeSaveSample != null ? (s) -> beforeSaveSample.accept(savedCase, s) : null);
		}
	}
}
