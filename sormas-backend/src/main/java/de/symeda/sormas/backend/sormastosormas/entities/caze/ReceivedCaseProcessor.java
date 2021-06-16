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

import static de.symeda.sormas.backend.sormastosormas.processed.ValidationHelper.buildCaseValidationGroupName;
import static de.symeda.sormas.backend.sormastosormas.processed.ValidationHelper.buildContactValidationGroupName;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

import de.symeda.sormas.api.EntityDto;
import de.symeda.sormas.api.caze.CaseDataDto;
import de.symeda.sormas.api.caze.maternalhistory.MaternalHistoryDto;
import de.symeda.sormas.api.contact.ContactDto;
import de.symeda.sormas.api.facility.FacilityType;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.person.PersonDto;
import de.symeda.sormas.api.sormastosormas.SormasToSormasOriginInfoDto;
import de.symeda.sormas.api.sormastosormas.SormasToSormasSampleDto;
import de.symeda.sormas.api.sormastosormas.SormasToSormasValidationException;
import de.symeda.sormas.api.sormastosormas.ValidationErrors;
import de.symeda.sormas.api.sormastosormas.caze.SormasToSormasCaseDto;
import de.symeda.sormas.api.sormastosormas.sharerequest.SormasToSormasCasePreview;
import de.symeda.sormas.api.sormastosormas.sharerequest.SormasToSormasContactPreview;
import de.symeda.sormas.api.utils.DataHelper;
import de.symeda.sormas.backend.contact.ContactFacadeEjb.ContactFacadeEjbLocal;
import de.symeda.sormas.backend.sormastosormas.received.ReceivedDataProcessor;
import de.symeda.sormas.backend.sormastosormas.received.ReceivedDataProcessorHelper;
import de.symeda.sormas.backend.sormastosormas.received.ReceivedDataProcessorHelper.InfrastructureData;

@Stateless
@LocalBean
public class ReceivedCaseProcessor
	implements ReceivedDataProcessor<CaseDataDto, SormasToSormasCaseDto, ProcessedCaseData, SormasToSormasCasePreview> {

	@EJB
	private ReceivedDataProcessorHelper dataProcessorHelper;
	@EJB
	private ContactFacadeEjbLocal contactFacade;

	@Override
	public ProcessedCaseData processReceivedData(SormasToSormasCaseDto receivedCase, CaseDataDto existingCaseData)
		throws SormasToSormasValidationException {
		Map<String, ValidationErrors> validationErrors = new HashMap<>();

		PersonDto person = receivedCase.getPerson();
		CaseDataDto caze = receivedCase.getEntity();
		List<SormasToSormasCaseDto.AssociatedContactDto> associatedContacts = receivedCase.getAssociatedContacts();
		List<SormasToSormasSampleDto> samples = receivedCase.getSamples();
		SormasToSormasOriginInfoDto originInfo = receivedCase.getOriginInfo();

		ValidationErrors caseValidationErrors = new ValidationErrors();

		ValidationErrors originInfoErrorsErrors = dataProcessorHelper.processOriginInfo(originInfo, Captions.CaseData);
		caseValidationErrors.addAll(originInfoErrorsErrors);

		ValidationErrors caseDataErrors = processCaseData(caze, person, existingCaseData);
		caseValidationErrors.addAll(caseDataErrors);

		if (caseValidationErrors.hasError()) {
			validationErrors.put(buildCaseValidationGroupName(caze), caseValidationErrors);
		}

		if (associatedContacts != null && associatedContacts.size() > 0) {
			Map<String, ValidationErrors> contactValidationErrors = processAssociatedContacts(associatedContacts);
			validationErrors.putAll(contactValidationErrors);
		}

		if (samples != null && samples.size() > 0) {
			Map<String, ValidationErrors> sampleErrors = dataProcessorHelper.processSamples(samples);
			validationErrors.putAll(sampleErrors);
		}

		if (validationErrors.size() > 0) {
			throw new SormasToSormasValidationException(validationErrors);
		}

		return new ProcessedCaseData(person, caze, associatedContacts, samples, originInfo);
	}

	@Override
	public SormasToSormasCasePreview processReceivedPreview(SormasToSormasCasePreview preview) throws SormasToSormasValidationException {
		Map<String, ValidationErrors> validationErrors = new HashMap<>();

		ValidationErrors caseValidationErrors = new ValidationErrors();

		DataHelper.Pair<InfrastructureData, List<String>> infrastructureAndErrors = dataProcessorHelper.loadLocalInfrastructure(
			preview.getRegion(),
			preview.getDistrict(),
			preview.getCommunity(),
			preview.getFacilityType(),
			preview.getHealthFacility(),
			preview.getHealthFacilityDetails(),
			preview.getPointOfEntry(),
			preview.getPointOfEntryDetails());

		dataProcessorHelper.handleInfraStructure(infrastructureAndErrors, Captions.CaseData, caseValidationErrors, infrastructureData -> {
			preview.setRegion(infrastructureData.getRegion());
			preview.setDistrict(infrastructureData.getDistrict());
			preview.setCommunity(infrastructureData.getCommunity());
			preview.setHealthFacility(infrastructureData.getFacility());
			preview.setHealthFacilityDetails(infrastructureData.getFacilityDetails());
			preview.setPointOfEntry(infrastructureData.getPointOfEntry());
			preview.setPointOfEntryDetails(infrastructureData.getPointOfEntryDetails());
		});

		if (caseValidationErrors.hasError()) {
			validationErrors.put(buildCaseValidationGroupName(preview), caseValidationErrors);
		}

		ValidationErrors personValidationErrors = dataProcessorHelper.processPersonPreview(preview.getPerson());
		caseValidationErrors.addAll(personValidationErrors);

		List<SormasToSormasContactPreview> contacts = preview.getContacts();
		if (contacts != null && contacts.size() > 0) {
			Map<String, ValidationErrors> contactValidationErrors = processContactPreviews(contacts);
			validationErrors.putAll(contactValidationErrors);
		}

		if (validationErrors.size() > 0) {
			throw new SormasToSormasValidationException(validationErrors);
		}

		return preview;
	}

	private ValidationErrors processCaseData(CaseDataDto caze, PersonDto person, CaseDataDto existingCaseData) {
		ValidationErrors caseValidationErrors = new ValidationErrors();

		ValidationErrors personValidationErrors = dataProcessorHelper.processPerson(person);
		caseValidationErrors.addAll(personValidationErrors);

		caze.setPerson(person.toReference());
		dataProcessorHelper.updateReportingUser(caze, existingCaseData);

		DataHelper.Pair<InfrastructureData, List<String>> infrastructureAndErrors = dataProcessorHelper.loadLocalInfrastructure(
			caze.getRegion(),
			caze.getDistrict(),
			caze.getCommunity(),
			caze.getFacilityType(),
			caze.getHealthFacility(),
			caze.getHealthFacilityDetails(),
			caze.getPointOfEntry(),
			caze.getPointOfEntryDetails());

		dataProcessorHelper.handleInfraStructure(infrastructureAndErrors, Captions.CaseData, caseValidationErrors, infrastructureData -> {
			caze.setRegion(infrastructureData.getRegion());
			caze.setDistrict(infrastructureData.getDistrict());
			caze.setCommunity(infrastructureData.getCommunity());
			caze.setHealthFacility(infrastructureData.getFacility());
			caze.setHealthFacilityDetails(infrastructureData.getFacilityDetails());
			caze.setPointOfEntry(infrastructureData.getPointOfEntry());
			caze.setPointOfEntryDetails(infrastructureData.getPointOfEntryDetails());
		});

		ValidationErrors embeddedObjectErrors = processEmbeddedObjects(caze);
		caseValidationErrors.addAll(embeddedObjectErrors);

		return caseValidationErrors;
	}

	private ValidationErrors processEmbeddedObjects(CaseDataDto caze) {
		ValidationErrors validationErrors = new ValidationErrors();

		if (caze.getHospitalization() != null) {

			caze.getHospitalization().getPreviousHospitalizations().forEach(ph -> {

				DataHelper.Pair<InfrastructureData, List<String>> phInfrastructureAndErrors = dataProcessorHelper.loadLocalInfrastructure(
					ph.getRegion(),
					ph.getDistrict(),
					ph.getCommunity(),
					FacilityType.HOSPITAL,
					ph.getHealthFacility(),
					ph.getHealthFacilityDetails(),
					null,
					null);

				dataProcessorHelper.handleInfraStructure(
					phInfrastructureAndErrors,
					Captions.CaseHospitalization_previousHospitalizations,
					validationErrors,
					(phInfrastructure) -> {
						ph.setRegion(phInfrastructure.getRegion());
						ph.setDistrict(phInfrastructure.getDistrict());
						ph.setCommunity(phInfrastructure.getCommunity());
						ph.setHealthFacility(phInfrastructure.getFacility());
						ph.setHealthFacilityDetails(phInfrastructure.getFacilityDetails());
					});
			});
		}

		MaternalHistoryDto maternalHistory = caze.getMaternalHistory();
		if (maternalHistory != null) {

			DataHelper.Pair<InfrastructureData, List<String>> rashExposureInfrastructureAndErrors = dataProcessorHelper.loadLocalInfrastructure(
				maternalHistory.getRashExposureRegion(),
				maternalHistory.getRashExposureDistrict(),
				maternalHistory.getRashExposureCommunity());

			dataProcessorHelper.handleInfraStructure(
				rashExposureInfrastructureAndErrors,
				Captions.MaternalHistory_rashExposure,
				validationErrors,
				(rashExposureInfrastructure) -> {
					maternalHistory.setRashExposureRegion(rashExposureInfrastructure.getRegion());
					maternalHistory.setRashExposureDistrict(rashExposureInfrastructure.getDistrict());
					maternalHistory.setRashExposureCommunity(rashExposureInfrastructure.getCommunity());
				});
		}

		dataProcessorHelper.processEpiData(caze.getEpiData(), validationErrors);

		return validationErrors;
	}

	private Map<String, ValidationErrors> processAssociatedContacts(List<SormasToSormasCaseDto.AssociatedContactDto> associatedContacts) {
		Map<String, ValidationErrors> validationErrors = new HashMap<>();

		Map<String, ContactDto> existingContactsMap =
			contactFacade.getByUuids(associatedContacts.stream().map(c -> c.getContact().getUuid()).collect(Collectors.toList()))
				.stream()
				.collect(Collectors.toMap(EntityDto::getUuid, Function.identity()));

		for (SormasToSormasCaseDto.AssociatedContactDto associatedContact : associatedContacts) {
			ContactDto contact = associatedContact.getContact();
			ValidationErrors contactErrors =
				dataProcessorHelper.processContactData(contact, associatedContact.getPerson(), existingContactsMap.get(contact.getUuid()));

			if (contactErrors.hasError()) {
				validationErrors.put(buildContactValidationGroupName(contact), contactErrors);
			}
		}

		return validationErrors;
	}

	private Map<String, ValidationErrors> processContactPreviews(List<SormasToSormasContactPreview> contacts) {
		Map<String, ValidationErrors> validationErrors = new HashMap<>();

		for (SormasToSormasContactPreview contact : contacts) {
			ValidationErrors contactErrors = dataProcessorHelper.processContactPreview(contact);

			if (contactErrors.hasError()) {
				validationErrors.put(buildContactValidationGroupName(contact), contactErrors);
			}
		}

		return validationErrors;
	}
}
