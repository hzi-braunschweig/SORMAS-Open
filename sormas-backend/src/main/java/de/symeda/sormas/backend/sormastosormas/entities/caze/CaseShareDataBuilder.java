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

package de.symeda.sormas.backend.sormastosormas.entities.caze;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;

import de.symeda.sormas.api.SormasToSormasConfig;
import de.symeda.sormas.api.caze.CaseDataDto;
import de.symeda.sormas.api.contact.ContactCriteria;
import de.symeda.sormas.api.contact.ContactDto;
import de.symeda.sormas.api.person.PersonDto;
import de.symeda.sormas.api.sample.SampleCriteria;
import de.symeda.sormas.api.sormastosormas.SormasToSormasException;
import de.symeda.sormas.api.sormastosormas.SormasToSormasOptionsDto;
import de.symeda.sormas.api.sormastosormas.SormasToSormasOriginInfoDto;
import de.symeda.sormas.api.sormastosormas.caze.SormasToSormasCaseDto;
import de.symeda.sormas.api.sormastosormas.sharerequest.SormasToSormasCasePreview;
import de.symeda.sormas.api.sormastosormas.sharerequest.SormasToSormasContactPreview;
import de.symeda.sormas.api.utils.DataHelper;
import de.symeda.sormas.backend.caze.Case;
import de.symeda.sormas.backend.caze.CaseFacadeEjb;
import de.symeda.sormas.backend.contact.Contact;
import de.symeda.sormas.backend.contact.ContactService;
import de.symeda.sormas.backend.facility.FacilityFacadeEjb;
import de.symeda.sormas.backend.infrastructure.PointOfEntryFacadeEjb;
import de.symeda.sormas.backend.region.CommunityFacadeEjb;
import de.symeda.sormas.backend.region.DistrictFacadeEjb;
import de.symeda.sormas.backend.region.RegionFacadeEjb;
import de.symeda.sormas.backend.sample.Sample;
import de.symeda.sormas.backend.sample.SampleService;
import de.symeda.sormas.backend.sormastosormas.entities.AssociatedEntityWrapper;
import de.symeda.sormas.backend.sormastosormas.share.ShareData;
import de.symeda.sormas.backend.sormastosormas.share.ShareDataBuilder;
import de.symeda.sormas.backend.sormastosormas.share.ShareDataBuilderHelper;
import de.symeda.sormas.backend.sormastosormas.share.shareinfo.ShareInfoContact;
import de.symeda.sormas.backend.sormastosormas.share.shareinfo.ShareInfoSample;
import de.symeda.sormas.backend.sormastosormas.share.shareinfo.SormasToSormasShareInfo;
import de.symeda.sormas.backend.user.User;
import de.symeda.sormas.backend.util.Pseudonymizer;

@Stateless
@LocalBean
public class CaseShareDataBuilder implements ShareDataBuilder<Case, SormasToSormasCaseDto, SormasToSormasCasePreview> {

	@EJB
	private CaseFacadeEjb.CaseFacadeEjbLocal caseFacade;
	@EJB
	private ContactService contactService;
	@EJB
	private SampleService sampleService;
	@EJB
	private ShareDataBuilderHelper dataBuilderHelper;
	@Inject
	private SormasToSormasConfig sormasToSormasConfig;

	public ShareData<Case, SormasToSormasCaseDto> buildShareData(Case caze, User user, SormasToSormasOptionsDto options)
		throws SormasToSormasException {

		SormasToSormasOriginInfoDto originInfo =
			dataBuilderHelper.createSormasToSormasOriginInfo(user, options.isHandOverOwnership(), options.getComment());

		List<Contact> associatedContacts = Collections.emptyList();
		if (options.isWithAssociatedContacts()) {
			associatedContacts = contactService.findBy(new ContactCriteria().caze(caze.toReference()), user);
		}

		List<Sample> samples = Collections.emptyList();
		if (options.isWithSamples()) {
			final List<Sample> caseSamples = sampleService.findBy(new SampleCriteria().caze(caze.toReference()), user);
			samples = new ArrayList<>(caseSamples);

			for (Contact associatedContact : associatedContacts) {
				List<Sample> contactSamples = sampleService.findBy(new SampleCriteria().contact(associatedContact.toReference()), user)
					.stream()
					.filter(contactSample -> caseSamples.stream().noneMatch(caseSample -> DataHelper.isSame(caseSample, contactSample)))
					.collect(Collectors.toList());

				samples.addAll(contactSamples);
			}
		}

		return createShareData(
			caze,
			originInfo,
			associatedContacts,
			samples,
			options.isPseudonymizePersonalData(),
			options.isPseudonymizeSensitiveData());
	}

	@Override
	public ShareData<Case, SormasToSormasCasePreview> buildShareDataPreview(Case caze, User user, SormasToSormasOptionsDto options)
		throws SormasToSormasException {

		SormasToSormasCasePreview cazePreview = getCasePreview(caze);

		List<Contact> associatedContacts = Collections.emptyList();
		if (options.isWithAssociatedContacts()) {
			associatedContacts = contactService.findBy(new ContactCriteria().caze(caze.toReference()), user);
			cazePreview.setContacts(getContactPreviews(associatedContacts));
		}

		ShareData<Case, SormasToSormasCasePreview> shareData = new ShareData<>(caze, cazePreview);
		shareData.addAssociatedEntities(AssociatedEntityWrapper.forContacts(associatedContacts));

		return shareData;
	}

	@Override
	public List<ShareData<Case, SormasToSormasCaseDto>> buildShareData(SormasToSormasShareInfo shareInfo, User user) throws SormasToSormasException {
		SormasToSormasOriginInfoDto originInfo =
			dataBuilderHelper.createSormasToSormasOriginInfo(user, shareInfo.isOwnershipHandedOver(), shareInfo.getComment());

		return shareInfo.getCases().stream().map(shareInfoCase -> {
			Case caze = shareInfoCase.getCaze();

			return createShareData(
				caze,
				originInfo,
				shareInfo.getContacts().stream().map(ShareInfoContact::getContact).collect(Collectors.toList()),
				shareInfo.getSamples().stream().map(ShareInfoSample::getSample).collect(Collectors.toList()),
				shareInfo.isPseudonymizedPersonalData(),
				shareInfo.isPseudonymizedSensitiveData());
		}).collect(Collectors.toList());
	}

	private ShareData<Case, SormasToSormasCaseDto> createShareData(
		Case caze,
		SormasToSormasOriginInfoDto originInfo,
		List<Contact> contacts,
		List<Sample> samples,
		boolean pseudonymizePersonalData,
		boolean pseudonymizeSensitiveData) {

		Pseudonymizer pseudonymizer = dataBuilderHelper.createPseudonymizer(pseudonymizePersonalData, pseudonymizeSensitiveData);

		PersonDto personDto = dataBuilderHelper.getPersonDto(caze.getPerson(), pseudonymizer, pseudonymizePersonalData, pseudonymizeSensitiveData);
		CaseDataDto cazeDto = getCazeDto(caze, pseudonymizer);

		// external tokens ("Aktenzeichen") are not globally unique in Germany due to SurvNet, therefore, do not
		// transmit the token to other GAs, but let them generate their own token based on their local, configurable
		// format
		if (!sormasToSormasConfig.getRetainCaseExternalToken()) {
			cazeDto.setExternalToken(null);
		}

		SormasToSormasCaseDto caseData = new SormasToSormasCaseDto(personDto, cazeDto, originInfo);
		ShareData<Case, SormasToSormasCaseDto> shareData = new ShareData<>(caze, caseData);

		caseData.setAssociatedContacts(getAssociatedContactDtos(contacts, pseudonymizer, pseudonymizePersonalData, pseudonymizeSensitiveData));
		shareData.addAssociatedEntities(AssociatedEntityWrapper.forContacts(contacts));

		caseData.setSamples(dataBuilderHelper.getSampleDtos(samples, pseudonymizer));
		shareData.addAssociatedEntities(AssociatedEntityWrapper.forSamples(samples));

		return shareData;
	}

	private CaseDataDto getCazeDto(Case caze, Pseudonymizer pseudonymizer) {
		CaseDataDto cazeDto = caseFacade.convertToDto(caze, pseudonymizer);

		cazeDto.setReportingUser(null);
		cazeDto.setClassificationUser(null);
		cazeDto.setSurveillanceOfficer(null);
		cazeDto.setCaseOfficer(null);
		cazeDto.setSormasToSormasOriginInfo(null);
		cazeDto.setDontShareWithReportingTool(false);

		return cazeDto;
	}

	private List<SormasToSormasCaseDto.AssociatedContactDto> getAssociatedContactDtos(
		List<Contact> associatedContacts,
		Pseudonymizer pseudonymizer,
		boolean pseudonymizedPersonalData,
		boolean pseudonymizedSensitiveData) {
		return associatedContacts.stream().map(contact -> {
			PersonDto personDto =
				dataBuilderHelper.getPersonDto(contact.getPerson(), pseudonymizer, pseudonymizedPersonalData, pseudonymizedSensitiveData);
			ContactDto contactDto = dataBuilderHelper.getContactDto(contact, pseudonymizer);

			return new SormasToSormasCaseDto.AssociatedContactDto(personDto, contactDto);
		}).collect(Collectors.toList());
	}

	private SormasToSormasCasePreview getCasePreview(Case caze) {
		SormasToSormasCasePreview casePreview = new SormasToSormasCasePreview();

		casePreview.setUuid(caze.getUuid());
		casePreview.setReportDate(caze.getReportDate());
		casePreview.setDisease(caze.getDisease());
		casePreview.setDiseaseDetails(caze.getDiseaseDetails());
		casePreview.setDiseaseVariant(caze.getDiseaseVariant());
		casePreview.setCaseClassification(caze.getCaseClassification());
		casePreview.setOutcome(caze.getOutcome());
		casePreview.setInvestigationStatus(caze.getInvestigationStatus());
		casePreview.setOnsetDate(caze.getSymptoms().getOnsetDate());
		casePreview.setRegion(RegionFacadeEjb.toReferenceDto(caze.getRegion()));
		casePreview.setDistrict(DistrictFacadeEjb.toReferenceDto(caze.getDistrict()));
		casePreview.setCommunity(CommunityFacadeEjb.toReferenceDto(caze.getCommunity()));
		casePreview.setFacilityType(caze.getFacilityType());
		casePreview.setHealthFacility(FacilityFacadeEjb.toReferenceDto(caze.getHealthFacility()));
		casePreview.setHealthFacilityDetails(caze.getHealthFacilityDetails());
		casePreview.setPointOfEntry(PointOfEntryFacadeEjb.toReferenceDto(caze.getPointOfEntry()));
		casePreview.setPointOfEntryDetails(caze.getPointOfEntryDetails());

		casePreview.setPerson(dataBuilderHelper.getPersonPreview(caze.getPerson()));

		return casePreview;
	}

	private List<SormasToSormasContactPreview> getContactPreviews(List<Contact> contacts) {
		return contacts.stream().map(c -> dataBuilderHelper.getContactPreview(c)).collect(Collectors.toList());
	}
}
