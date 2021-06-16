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

package de.symeda.sormas.backend.sormastosormas.entities.labmessage;

import static de.symeda.sormas.api.sormastosormas.SormasToSormasApiConstants.LAB_MESSAGE_ENDPOINT;
import static de.symeda.sormas.api.sormastosormas.SormasToSormasApiConstants.RESOURCE_PATH;
import static de.symeda.sormas.backend.sormastosormas.processed.ValidationHelper.buildLabMessageValidationGroupName;
import static de.symeda.sormas.backend.sormastosormas.processed.ValidationHelper.handleValidationError;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;

import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Validations;
import de.symeda.sormas.api.labmessage.LabMessageDto;
import de.symeda.sormas.api.labmessage.LabMessageStatus;
import de.symeda.sormas.api.sormastosormas.SormasToSormasEncryptedDataDto;
import de.symeda.sormas.api.sormastosormas.SormasToSormasException;
import de.symeda.sormas.api.sormastosormas.SormasToSormasLabMessageFacade;
import de.symeda.sormas.api.sormastosormas.SormasToSormasOptionsDto;
import de.symeda.sormas.api.sormastosormas.SormasToSormasValidationException;
import de.symeda.sormas.api.sormastosormas.ValidationErrors;
import de.symeda.sormas.api.utils.ValidationRuntimeException;
import de.symeda.sormas.backend.labmessage.LabMessage;
import de.symeda.sormas.backend.labmessage.LabMessageFacadeEjb.LabMessageFacadeEjbLocal;
import de.symeda.sormas.backend.labmessage.LabMessageService;
import de.symeda.sormas.backend.sormastosormas.crypto.SormasToSormasEncryptionService;
import de.symeda.sormas.backend.sormastosormas.rest.SormasToSormasRestClient;

@Stateless(name = "SormasToSormasLabMessageFacade")
public class SormasToSormasLabMessageFacadeEjb implements SormasToSormasLabMessageFacade {

	public static final String SAVE_SHARED_LAB_MESSAGE_ENDPOINT = RESOURCE_PATH + LAB_MESSAGE_ENDPOINT;

	@EJB
	private LabMessageService labMessageService;
	@EJB
	private LabMessageFacadeEjbLocal labMessageFacade;
	@Inject
	private SormasToSormasRestClient sormasToSormasRestClient;

	@EJB
	private SormasToSormasEncryptionService encryptionService;

	@Override
	public void sendLabMessages(List<String> uuids, SormasToSormasOptionsDto options) throws SormasToSormasException {
		List<LabMessage> labMessages = labMessageService.getByUuids(uuids);
		List<LabMessageDto> dtos = labMessages.stream().map(labMessageFacade::toDto).collect(Collectors.toList());
		sormasToSormasRestClient.post(options.getOrganization().getUuid(), SAVE_SHARED_LAB_MESSAGE_ENDPOINT, dtos, null);

		labMessages.forEach(labMessage -> {
			labMessage.setStatus(LabMessageStatus.FORWARDED);
			labMessageService.ensurePersisted(labMessage);
		});
	}

	@Override
	public void saveLabMessages(SormasToSormasEncryptedDataDto encryptedData) throws SormasToSormasException, SormasToSormasValidationException {
		LabMessageDto[] sharedLabMessages = encryptionService.decryptAndVerify(encryptedData, LabMessageDto[].class);

		Map<String, ValidationErrors> validationErrors = new HashMap<>();
		List<LabMessageDto> labMessagesToSave = new ArrayList<>(sharedLabMessages.length);

		for (LabMessageDto labMessage : sharedLabMessages) {
			ValidationErrors errors = validateSharedLabMessage(labMessage);
			if (errors.hasError()) {
				validationErrors.put(buildLabMessageValidationGroupName(labMessage), errors);
			}

			labMessagesToSave.add(labMessage);
		}

		if (validationErrors.size() > 0) {
			throw new SormasToSormasValidationException(validationErrors);
		}

		for (LabMessageDto labMessage : labMessagesToSave) {
			handleValidationError(() -> labMessageFacade.save(labMessage), Captions.LabMessage, buildLabMessageValidationGroupName(labMessage));
		}
	}

	private ValidationErrors validateSharedLabMessage(LabMessageDto labMessage) throws ValidationRuntimeException {
		ValidationErrors errors = new ValidationErrors();

		if (labMessageFacade.exists(labMessage.getUuid())) {
			errors.add(I18nProperties.getCaption(Captions.LabMessage), I18nProperties.getValidationError(Validations.sormasToSormasLabMessageExists));
		}

		return errors;
	}

	@LocalBean
	@Stateless
	public static class SormasToSormasLabMessageFacadeEjbLocal extends SormasToSormasLabMessageFacadeEjb {

	}
}
