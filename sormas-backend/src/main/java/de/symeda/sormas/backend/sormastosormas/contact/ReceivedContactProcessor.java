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
import de.symeda.sormas.backend.sormastosormas.ReceivedDataProcessor;
import de.symeda.sormas.backend.sormastosormas.SharedDataProcessorHelper;

@Stateless
@LocalBean
public class ReceivedContactProcessor implements ReceivedDataProcessor<ContactDto, SormasToSormasContactDto, ProcessedContactData> {

	@EJB
	private SharedDataProcessorHelper dataProcessorHelper;

	@Override
	public ProcessedContactData processReceivedData(SormasToSormasContactDto receivedData, ContactDto existingContact)
		throws SormasToSormasValidationException {
		Map<String, ValidationErrors> validationErrors = new HashMap<>();

		PersonDto person = receivedData.getPerson();
		ContactDto contact = receivedData.getEntity();
		List<SormasToSormasSampleDto> samples = receivedData.getSamples();
		SormasToSormasOriginInfoDto originInfo = receivedData.getOriginInfo();

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
}
