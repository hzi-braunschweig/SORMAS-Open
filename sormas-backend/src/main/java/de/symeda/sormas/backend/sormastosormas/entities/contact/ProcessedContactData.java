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

import java.util.List;

import de.symeda.sormas.api.contact.ContactDto;
import de.symeda.sormas.api.person.PersonDto;
import de.symeda.sormas.api.sormastosormas.SormasToSormasOriginInfoDto;
import de.symeda.sormas.api.sormastosormas.SormasToSormasSampleDto;
import de.symeda.sormas.backend.sormastosormas.processed.ProcessedData;

public class ProcessedContactData extends ProcessedData<ContactDto> {

	private static final long serialVersionUID = 2582825878321003476L;

	private final PersonDto person;
	private final List<SormasToSormasSampleDto> samples;

	public ProcessedContactData(PersonDto person, ContactDto contact, List<SormasToSormasSampleDto> samples, SormasToSormasOriginInfoDto originInfo) {
		super(contact, originInfo);

		this.person = person;
		this.samples = samples;
	}

	public PersonDto getPerson() {
		return person;
	}

	public List<SormasToSormasSampleDto> getSamples() {
		return samples;
	}
}
