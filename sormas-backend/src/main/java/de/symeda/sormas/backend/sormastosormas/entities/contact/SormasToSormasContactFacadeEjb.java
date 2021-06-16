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

package de.symeda.sormas.backend.sormastosormas.entities.contact;

import static de.symeda.sormas.api.sormastosormas.SormasToSormasApiConstants.CONTACT_ENDPOINT;
import static de.symeda.sormas.api.sormastosormas.SormasToSormasApiConstants.CONTACT_SYNC_ENDPOINT;
import static de.symeda.sormas.api.sormastosormas.SormasToSormasApiConstants.RESOURCE_PATH;
import static de.symeda.sormas.backend.sormastosormas.processed.ValidationHelper.buildContactValidationGroupName;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

import de.symeda.sormas.api.caze.CaseReferenceDto;
import de.symeda.sormas.api.contact.ContactDto;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Strings;
import de.symeda.sormas.api.i18n.Validations;
import de.symeda.sormas.api.sormastosormas.SormasToSormasApiConstants;
import de.symeda.sormas.api.sormastosormas.SormasToSormasException;
import de.symeda.sormas.api.sormastosormas.ValidationErrors;
import de.symeda.sormas.api.sormastosormas.contact.SormasToSormasContactDto;
import de.symeda.sormas.api.sormastosormas.contact.SormasToSormasContactFacade;
import de.symeda.sormas.api.sormastosormas.sharerequest.ShareRequestDataType;
import de.symeda.sormas.api.sormastosormas.sharerequest.SormasToSormasContactPreview;
import de.symeda.sormas.api.sormastosormas.sharerequest.SormasToSormasShareRequestDto;
import de.symeda.sormas.backend.caze.CaseFacadeEjb.CaseFacadeEjbLocal;
import de.symeda.sormas.backend.common.BaseAdoService;
import de.symeda.sormas.backend.contact.Contact;
import de.symeda.sormas.backend.contact.ContactFacadeEjb.ContactFacadeEjbLocal;
import de.symeda.sormas.backend.contact.ContactService;
import de.symeda.sormas.backend.sormastosormas.AbstractSormasToSormasInterface;
import de.symeda.sormas.backend.sormastosormas.processed.ProcessedDataPersister;
import de.symeda.sormas.backend.sormastosormas.received.ReceivedDataProcessor;
import de.symeda.sormas.backend.sormastosormas.share.ShareDataBuilder;
import de.symeda.sormas.backend.sormastosormas.share.shareinfo.ShareInfoContact;
import de.symeda.sormas.backend.sormastosormas.share.shareinfo.SormasToSormasShareInfo;
import de.symeda.sormas.backend.sormastosormas.share.shareinfo.SormasToSormasShareInfoService;

@Stateless(name = "SormasToSormasContactFacade")
public class SormasToSormasContactFacadeEjb
	extends AbstractSormasToSormasInterface<Contact, ContactDto, SormasToSormasContactDto, SormasToSormasContactPreview, ProcessedContactData>
	implements SormasToSormasContactFacade {

	private static final String CONTACT_REQUEST_ENDPOINT = RESOURCE_PATH + SormasToSormasApiConstants.CONTACT_REQUEST_ENDPOINT;
	private static final String CONTACT_REQUEST_REJECT_ENDPOINT = RESOURCE_PATH + SormasToSormasApiConstants.CONTACT_REQUEST_REJECT_ENDPOINT;
	private static final String CONTACT_REQUEST_ACCEPT_ENDPOINT = RESOURCE_PATH + SormasToSormasApiConstants.CONTACT_REQUEST_ACCEPT_ENDPOINT;
	private static final String SAVE_SHARED_CONTACT_ENDPOINT = RESOURCE_PATH + CONTACT_ENDPOINT;
	public static final String SYNC_SHARED_CONTACT_ENDPOINT = RESOURCE_PATH + CONTACT_SYNC_ENDPOINT;

	@EJB
	private ContactService contactService;
	@EJB
	private ContactShareDataBuilder contactShareDataBuilder;
	@EJB
	private ReceivedContactProcessor receivedContactProcessor;
	@EJB
	private ProcessedContactDataPersister processedContactDataPersister;
	@EJB
	private ContactFacadeEjbLocal contactFacade;
	@EJB
	private CaseFacadeEjbLocal caseFacade;
	@EJB
	private SormasToSormasShareInfoService shareInfoService;

	public SormasToSormasContactFacadeEjb() {
		super(
			CONTACT_REQUEST_ENDPOINT,
			CONTACT_REQUEST_REJECT_ENDPOINT,
			CONTACT_REQUEST_ACCEPT_ENDPOINT,
			SAVE_SHARED_CONTACT_ENDPOINT,
			SYNC_SHARED_CONTACT_ENDPOINT,
			Captions.Contact,
			ShareRequestDataType.CONTACT,
			ContactShareRequestData.class);
	}

	@Override
	protected BaseAdoService<Contact> getEntityService() {
		return contactService;
	}

	@Override
	protected ShareDataBuilder<Contact, SormasToSormasContactDto, SormasToSormasContactPreview> getShareDataBuilder() {
		return contactShareDataBuilder;
	}

	@Override
	protected ReceivedDataProcessor<ContactDto, SormasToSormasContactDto, ProcessedContactData, SormasToSormasContactPreview> getReceivedDataProcessor() {
		return receivedContactProcessor;
	}

	@Override
	protected ProcessedDataPersister<ProcessedContactData> getProcessedDataPersister() {
		return processedContactDataPersister;
	}

	@Override
	protected Class<SormasToSormasContactDto[]> getShareDataClass() {
		return SormasToSormasContactDto[].class;
	}

	@Override
	protected void validateEntitiesBeforeShare(List<Contact> entities, boolean handOverOwnership) throws SormasToSormasException {
		Map<String, ValidationErrors> validationErrors = new HashMap<>();
		for (Contact contact : entities) {
			if (!contactService.isContactEditAllowed(contact)) {
				validationErrors.put(
					buildContactValidationGroupName(contact),
					ValidationErrors
						.create(I18nProperties.getCaption(Captions.Contact), I18nProperties.getString(Strings.errorSormasToSormasNotEditable)));
			}
			if (handOverOwnership && contact.getPerson().isEnrolledInExternalJournal()) {
				validationErrors.put(
					buildContactValidationGroupName(contact),
					ValidationErrors
						.create(I18nProperties.getCaption(Captions.Contact), I18nProperties.getString(Strings.errorSormasToSormasPersonEnrolled)));
			}
		}

		if (validationErrors.size() > 0) {
			throw new SormasToSormasException(I18nProperties.getString(Strings.errorSormasToSormasShare), validationErrors);
		}
	}

	@Override
	protected ValidationErrors validateSharedEntity(ContactDto entity) {
		return validateSharedUuids(entity.getUuid(), entity.getCaze());
	}

	@Override
	protected ValidationErrors validateSharedPreview(SormasToSormasContactPreview preview) {
		return validateSharedUuids(preview.getUuid(), preview.getCaze());
	}

	@Override
	protected void addEntityToShareInfo(SormasToSormasShareInfo shareInfo, List<Contact> contacts) {
		shareInfo.getContacts().addAll(contacts.stream().map(c -> new ShareInfoContact(shareInfo, c)).collect(Collectors.toList()));
	}

	@Override
	protected SormasToSormasShareInfo getShareInfoByEntityAndOrganization(String entityUuid, String organizationId) {
		return shareInfoService.getByContactAndOrganization(entityUuid, organizationId);
	}

	@Override
	protected List<ContactDto> loadExistingEntities(List<String> uuids) {
		return contactFacade.getByUuids(uuids);
	}

	@Override
	protected void setShareRequestPreviewData(SormasToSormasShareRequestDto request, List<SormasToSormasContactPreview> previews) {
		request.setContacts(previews);
	}

	private ValidationErrors validateSharedUuids(String uuid, CaseReferenceDto caze) {
		ValidationErrors errors = new ValidationErrors();

		if (contactFacade.exists(uuid)) {
			errors.add(I18nProperties.getCaption(Captions.Contact), I18nProperties.getValidationError(Validations.sormasToSormasContactExists));
		}

		if (caze != null && !caseFacade.exists(caze.getUuid())) {
			errors
				.add(I18nProperties.getCaption(Captions.CaseData), I18nProperties.getValidationError(Validations.sormasToSormasContactCaseNotExists));
		}

		return errors;
	}

	@Override
	protected List<String> getUuidsWithPendingOwnershipHandedOver(List<Contact> entities) {
		return shareInfoService.getContactUuidsWithPendingOwnershipHandOver(entities);
	}

	@LocalBean
	@Stateless
	public static class SormasToSormasContactFacadeEjbLocal extends SormasToSormasContactFacadeEjb {

	}
}
