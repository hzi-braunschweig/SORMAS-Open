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

import static de.symeda.sormas.api.sormastosormas.SormasToSormasApiConstants.CONTACT_ENDPOINT;
import static de.symeda.sormas.api.sormastosormas.SormasToSormasApiConstants.CONTACT_SYNC_ENDPOINT;
import static de.symeda.sormas.api.sormastosormas.SormasToSormasApiConstants.RESOURCE_PATH;
import static de.symeda.sormas.backend.sormastosormas.ValidationHelper.buildContactValidationGroupName;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

import de.symeda.sormas.api.caze.CaseReferenceDto;
import de.symeda.sormas.api.contact.ContactDto;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Strings;
import de.symeda.sormas.api.i18n.Validations;
import de.symeda.sormas.api.sormastosormas.SormasToSormasException;
import de.symeda.sormas.api.sormastosormas.ValidationErrors;
import de.symeda.sormas.api.sormastosormas.contact.SormasToSormasContactDto;
import de.symeda.sormas.api.sormastosormas.contact.SormasToSormasContactFacade;
import de.symeda.sormas.backend.caze.CaseFacadeEjb.CaseFacadeEjbLocal;
import de.symeda.sormas.backend.common.BaseAdoService;
import de.symeda.sormas.backend.contact.Contact;
import de.symeda.sormas.backend.contact.ContactFacadeEjb.ContactFacadeEjbLocal;
import de.symeda.sormas.backend.contact.ContactService;
import de.symeda.sormas.backend.sormastosormas.AbstractSormasToSormasInterface;
import de.symeda.sormas.backend.sormastosormas.ProcessedDataPersister;
import de.symeda.sormas.backend.sormastosormas.ReceivedDataProcessor;
import de.symeda.sormas.backend.sormastosormas.ShareDataBuilder;
import de.symeda.sormas.backend.sormastosormas.SormasToSormasShareInfo;
import de.symeda.sormas.backend.sormastosormas.SormasToSormasShareInfoService;

@Stateless(name = "SormasToSormasContactFacade")
public class SormasToSormasContactFacadeEjb
	extends AbstractSormasToSormasInterface<Contact, ContactDto, SormasToSormasContactDto, ProcessedContactData>
	implements SormasToSormasContactFacade {

	private static final String SAVE_SHARED_CONTACT_ENDPOINT = RESOURCE_PATH + CONTACT_ENDPOINT;
	public static final String SYNC_SHARED_CONTACT_ENDPOINT = RESOURCE_PATH + CONTACT_SYNC_ENDPOINT;

	@EJB
	private ContactService contactService;
	@EJB
	private ContactShareDataBuilder contactShareDataBuilder;
	@EJB
	private ReceivedContactProcessor sharedContactProcessor;
	@EJB
	private ProcessedContactDataPersister processedContactDataPersister;
	@EJB
	private ContactFacadeEjbLocal contactFacade;
	@EJB
	private CaseFacadeEjbLocal caseFacade;
	@EJB
	private SormasToSormasShareInfoService shareInfoService;

	public SormasToSormasContactFacadeEjb() {
		super(SAVE_SHARED_CONTACT_ENDPOINT, SYNC_SHARED_CONTACT_ENDPOINT, Captions.Contact);
	}

	@Override
	protected BaseAdoService<Contact> getEntityService() {
		return contactService;
	}

	@Override
	protected ShareDataBuilder<Contact, SormasToSormasContactDto> getShareDataBuilder() {
		return contactShareDataBuilder;
	}

	protected ReceivedDataProcessor<ContactDto, SormasToSormasContactDto, ProcessedContactData> getReceivedDataProcessor() {
		return sharedContactProcessor;
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
	protected void validateEntitiesBeforeSend(List<Contact> entities) throws SormasToSormasException {
		Map<String, ValidationErrors> validationErrors = new HashMap<>();
		for (Contact contact : entities) {
			if (!contactService.isContactEditAllowed(contact)) {
				validationErrors.put(
					buildContactValidationGroupName(contact),
					ValidationErrors
						.create(I18nProperties.getCaption(Captions.Contact), I18nProperties.getString(Strings.errorSormasToSormasNotEditable)));
			}
		}

		if (validationErrors.size() > 0) {
			throw new SormasToSormasException(I18nProperties.getString(Strings.errorSormasToSormasShare), validationErrors);
		}
	}

	@Override
	protected ValidationErrors validateSharedEntity(ContactDto entity) {
		ValidationErrors errors = new ValidationErrors();

		if (contactFacade.exists(entity.getUuid())) {
			errors.add(I18nProperties.getCaption(Captions.Contact), I18nProperties.getValidationError(Validations.sormasToSormasContactExists));
		}

		// In case we want to store a new contact, the associated case must already be present 
		CaseReferenceDto caze = entity.getCaze();
		if (caze != null && !caseFacade.exists(caze.getUuid())) {
			errors
				.add(I18nProperties.getCaption(Captions.Contact), I18nProperties.getValidationError(Validations.sormasToSormasContactCaseNotExists));
		}

		return errors;
	}

	@Override
	protected void setEntityShareInfoAssociatedObject(SormasToSormasShareInfo sormasToSormasShareInfo, Contact entity) {
		sormasToSormasShareInfo.setContact(entity);
	}

	@Override
	protected SormasToSormasShareInfo getShareInfoByEntityAndOrganization(String entityUuid, String organizationId) {
		return shareInfoService.getByContactAndOrganization(entityUuid, organizationId);
	}

	@Override
	protected List<ContactDto> loadExistingEntities(List<String> uuids) {
		return contactFacade.getByUuids(uuids);
	}

	@LocalBean
	@Stateless
	public static class SormasToSormasContactFacadeEjbLocal extends SormasToSormasContactFacadeEjb {

	}
}
