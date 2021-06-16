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

import static de.symeda.sormas.backend.sormastosormas.processed.ValidationHelper.buildContactValidationGroupName;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

import de.symeda.sormas.api.contact.ContactDto;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.person.PersonDto;
import de.symeda.sormas.api.sormastosormas.SormasToSormasOriginInfoDto;
import de.symeda.sormas.api.sormastosormas.SormasToSormasSampleDto;
import de.symeda.sormas.api.sormastosormas.SormasToSormasValidationException;
import de.symeda.sormas.api.sormastosormas.ValidationErrors;
import de.symeda.sormas.api.sormastosormas.contact.SormasToSormasContactDto;
import de.symeda.sormas.api.sormastosormas.sharerequest.SormasToSormasContactPreview;
import de.symeda.sormas.backend.sormastosormas.received.ReceivedDataProcessor;
import de.symeda.sormas.backend.sormastosormas.received.ReceivedDataProcessorHelper;

@Stateless
@LocalBean
public class ReceivedContactProcessor
	implements ReceivedDataProcessor<ContactDto, SormasToSormasContactDto, ProcessedContactData, SormasToSormasContactPreview> {

	@EJB
	private ReceivedDataProcessorHelper dataProcessorHelper;

	@Override
	public ProcessedContactData processReceivedData(SormasToSormasContactDto receivedContact, ContactDto existingContact)
		throws SormasToSormasValidationException {
		Map<String, ValidationErrors> validationErrors = new HashMap<>();

		PersonDto person = receivedContact.getPerson();
		ContactDto contact = receivedContact.getEntity();
		List<SormasToSormasSampleDto> samples = receivedContact.getSamples();
		SormasToSormasOriginInfoDto originInfo = receivedContact.getOriginInfo();

		ValidationErrors contactValidationErrors = new ValidationErrors();

		ValidationErrors originInfoErrors = dataProcessorHelper.processOriginInfo(originInfo, Captions.Contact);
		contactValidationErrors.addAll(originInfoErrors);

		ValidationErrors contactDataErrors = dataProcessorHelper.processContactData(contact, person, existingContact);
		contactValidationErrors.addAll(contactDataErrors);

		if (contactValidationErrors.hasError()) {
			validationErrors.put(buildContactValidationGroupName(contact), contactValidationErrors);
		}

		if (samples != null && samples.size() > 0) {
			Map<String, ValidationErrors> sampleErrors = dataProcessorHelper.processSamples(samples);
			validationErrors.putAll(sampleErrors);
		}

		if (validationErrors.size() > 0) {
			throw new SormasToSormasValidationException(validationErrors);
		}

		return new ProcessedContactData(person, contact, samples, originInfo);
	}

	@Override
	public SormasToSormasContactPreview processReceivedPreview(SormasToSormasContactPreview preview) throws SormasToSormasValidationException {
		Map<String, ValidationErrors> validationErrors = new HashMap<>();

		ValidationErrors contactErrors = dataProcessorHelper.processContactPreview(preview);

		if (contactErrors.hasError()) {
			validationErrors.put(buildContactValidationGroupName(preview), contactErrors);
		}

		if (validationErrors.size() > 0) {
			throw new SormasToSormasValidationException(validationErrors);
		}

		return preview;
	}
}
