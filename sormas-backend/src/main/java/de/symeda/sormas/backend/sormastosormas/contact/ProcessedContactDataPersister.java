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

package de.symeda.sormas.backend.sormastosormas.contact;

import static de.symeda.sormas.backend.sormastosormas.ValidationHelper.buildContactValidationGroupName;
import static de.symeda.sormas.backend.sormastosormas.ValidationHelper.handleValidationError;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.transaction.Transactional;

import de.symeda.sormas.api.contact.ContactDto;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.sample.SampleDto;
import de.symeda.sormas.api.sormastosormas.SormasToSormasOriginInfoDto;
import de.symeda.sormas.api.sormastosormas.SormasToSormasValidationException;
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
public class ProcessedContactDataPersister implements ProcessedDataPersister<ProcessedContactData> {

	@EJB
	private PersonFacadeEjb.PersonFacadeEjbLocal personFacade;
	@EJB
	private ContactFacadeEjb.ContactFacadeEjbLocal contactFacade;
	@EJB
	private ProcessedDataPersisterHelper dataPersisterHelper;
	@EJB
	private SormasToSormasShareInfoService shareInfoService;
	@EJB
	private SormasToSormasOriginInfoFacadeEjbLocal oriInfoFacade;

	@Transactional(rollbackOn = {
		Exception.class })
	public void persistSharedData(ProcessedContactData processedData) throws SormasToSormasValidationException {
		persistProcessedData(processedData, null, dataPersisterHelper::sharedAssociatedEntityCallback, true);
	}

	@Override
	@Transactional(rollbackOn = {
		Exception.class })
	public void persistReturnedData(ProcessedContactData processedData, SormasToSormasOriginInfoDto originInfo)
		throws SormasToSormasValidationException {

		ReturnedAssociatedEntityCallback callback = dataPersisterHelper.createReturnedAssociatedEntityCallback(originInfo);

		persistProcessedData(processedData, contact -> {
			SormasToSormasShareInfo contactShareInfo =
				shareInfoService.getByContactAndOrganization(contact.getUuid(), originInfo.getOrganizationId());
			contactShareInfo.setOwnershipHandedOver(false);
			shareInfoService.persist(contactShareInfo);
		}, (contact, sample) -> {
			callback.apply(sample, shareInfoService::getBySampleAndOrganization);
		}, false);
	}

	@Override
	public void persistSyncData(ProcessedContactData processedData) throws SormasToSormasValidationException {
		SormasToSormasOriginInfoDto originInfo = processedData.getOriginInfo();

		persistProcessedData(processedData, (contact) -> {
			SormasToSormasOriginInfoDto contactOriginInfo = contact.getSormasToSormasOriginInfo();
			contactOriginInfo.setOwnershipHandedOver(originInfo.isOwnershipHandedOver());
			contactOriginInfo.setComment(originInfo.getComment());

			oriInfoFacade.saveOriginInfo(contactOriginInfo);
		}, dataPersisterHelper::syncedAssociatedEntityCallback, false);
	}

	private void persistProcessedData(
		ProcessedContactData processedData,
		Consumer<ContactDto> afterSaveContact,
		BiConsumer<ContactDto, SampleDto> beforeSaveContact,
		boolean isCreate)
		throws SormasToSormasValidationException {

		String contactValidationGroupName = buildContactValidationGroupName(processedData.getEntity());

		final ContactDto savedContact;
		if (isCreate) {
			// save person first during creation
			handleValidationError(() -> personFacade.savePerson(processedData.getPerson(), false), Captions.Person, contactValidationGroupName);
			savedContact = handleValidationError(
				() -> contactFacade.saveContact(processedData.getEntity(), true, true, false),
				Captions.Contact,
				contactValidationGroupName);
		} else {
			//save contact first during update
			savedContact = handleValidationError(
				() -> contactFacade.saveContact(processedData.getEntity(), true, true, false),
				Captions.Contact,
				contactValidationGroupName);
			handleValidationError(() -> personFacade.savePerson(processedData.getPerson(), false), Captions.Person, contactValidationGroupName);
		}

		if (afterSaveContact != null) {
			afterSaveContact.accept(savedContact);
		}

		if (processedData.getSamples() != null) {
			dataPersisterHelper.persistSamples(
				processedData.getSamples(),
				beforeSaveContact != null ? (sample) -> beforeSaveContact.accept(savedContact, sample) : null);
		}
	}
}
