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
import java.util.stream.Collectors;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

import de.symeda.sormas.api.contact.ContactDto;
import de.symeda.sormas.api.person.PersonDto;
import de.symeda.sormas.api.sample.SampleCriteria;
import de.symeda.sormas.api.sormastosormas.SormasToSormasException;
import de.symeda.sormas.api.sormastosormas.SormasToSormasOptionsDto;
import de.symeda.sormas.api.sormastosormas.SormasToSormasOriginInfoDto;
import de.symeda.sormas.api.sormastosormas.contact.SormasToSormasContactDto;
import de.symeda.sormas.api.sormastosormas.sharerequest.SormasToSormasContactPreview;
import de.symeda.sormas.backend.contact.Contact;
import de.symeda.sormas.backend.sample.Sample;
import de.symeda.sormas.backend.sample.SampleService;
import de.symeda.sormas.backend.sormastosormas.entities.AssociatedEntityWrapper;
import de.symeda.sormas.backend.sormastosormas.share.ShareData;
import de.symeda.sormas.backend.sormastosormas.share.ShareDataBuilder;
import de.symeda.sormas.backend.sormastosormas.share.ShareDataBuilderHelper;
import de.symeda.sormas.backend.sormastosormas.share.shareinfo.SormasToSormasShareInfo;
import de.symeda.sormas.backend.user.User;
import de.symeda.sormas.backend.util.Pseudonymizer;

@Stateless
@LocalBean
public class ContactShareDataBuilder implements ShareDataBuilder<Contact, SormasToSormasContactDto, SormasToSormasContactPreview> {

	@EJB
	private SampleService sampleService;
	@EJB
	private ShareDataBuilderHelper dataBuilderHelper;

	public ShareData<Contact, SormasToSormasContactDto> buildShareData(Contact contact, User user, SormasToSormasOptionsDto options)
		throws SormasToSormasException {
		SormasToSormasOriginInfoDto originInfo =
			dataBuilderHelper.createSormasToSormasOriginInfo(user, options.isHandOverOwnership(), options.getComment());

		return createShareData(
			contact,
			originInfo,
			user,
			options.isWithSamples(),
			options.isPseudonymizePersonalData(),
			options.isPseudonymizeSensitiveData());
	}

	@Override
	public ShareData<Contact, SormasToSormasContactPreview> buildShareDataPreview(Contact contact, User user, SormasToSormasOptionsDto options)
		throws SormasToSormasException {
		SormasToSormasContactPreview contactPreview = dataBuilderHelper.getContactPreview(contact);

		return new ShareData<>(contact, contactPreview);
	}

	@Override
	public List<ShareData<Contact, SormasToSormasContactDto>> buildShareData(SormasToSormasShareInfo shareInfo, User user)
		throws SormasToSormasException {
		SormasToSormasOriginInfoDto originInfo =
			dataBuilderHelper.createSormasToSormasOriginInfo(user, shareInfo.isOwnershipHandedOver(), shareInfo.getComment());

		return shareInfo.getContacts().stream().map(shareInfoContact -> {
			Contact contact = shareInfoContact.getContact();

			return createShareData(
				contact,
				originInfo,
				user,
				shareInfo.isWithSamples(),
				shareInfo.isPseudonymizedPersonalData(),
				shareInfo.isPseudonymizedSensitiveData());
		}).collect(Collectors.toList());
	}

	private ShareData<Contact, SormasToSormasContactDto> createShareData(
		Contact contact,
		SormasToSormasOriginInfoDto originInfo,
		User user,
		boolean withSamples,
		boolean pseudonymizePersonalData,
		boolean pseudonymizeSensitiveData) {
		Pseudonymizer pseudonymizer = dataBuilderHelper.createPseudonymizer(pseudonymizePersonalData, pseudonymizeSensitiveData);

		PersonDto personDto = dataBuilderHelper.getPersonDto(contact.getPerson(), pseudonymizer, pseudonymizePersonalData, pseudonymizeSensitiveData);
		ContactDto contactDto = dataBuilderHelper.getContactDto(contact, pseudonymizer);

		SormasToSormasContactDto contactData = new SormasToSormasContactDto(personDto, contactDto, originInfo);
		ShareData<Contact, SormasToSormasContactDto> shareData = new ShareData<>(contact, contactData);

		if (withSamples) {
			List<Sample> samples = sampleService.findBy(new SampleCriteria().contact(contact.toReference()), user);

			contactData.setSamples(dataBuilderHelper.getSampleDtos(samples, pseudonymizer));
			shareData.addAssociatedEntities(AssociatedEntityWrapper.forSamples(samples));
		}

		return shareData;
	}
}
