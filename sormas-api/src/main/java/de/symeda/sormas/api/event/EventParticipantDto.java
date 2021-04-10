/*******************************************************************************
 * SORMAS® - Surveillance Outbreak Response Management & Analysis System
 * Copyright © 2016-2018 Helmholtz-Zentrum für Infektionsforschung GmbH (HZI)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 *******************************************************************************/
package de.symeda.sormas.api.event;

import de.symeda.sormas.api.caze.CaseReferenceDto;
import de.symeda.sormas.api.person.PersonDto;
import de.symeda.sormas.api.region.DistrictReferenceDto;
import de.symeda.sormas.api.region.RegionReferenceDto;
import de.symeda.sormas.api.sormastosormas.SormasToSormasOriginInfoDto;
import de.symeda.sormas.api.user.UserReferenceDto;
import de.symeda.sormas.api.utils.DataHelper;
import de.symeda.sormas.api.utils.EmbeddedPersonalData;
import javax.validation.constraints.NotNull;
import de.symeda.sormas.api.utils.SensitiveData;
import de.symeda.sormas.api.utils.SormasToSormasEntityDto;
import de.symeda.sormas.api.utils.pseudonymization.PseudonymizableDto;
import de.symeda.sormas.api.vaccinationinfo.VaccinationInfoDto;

public class EventParticipantDto extends PseudonymizableDto implements SormasToSormasEntityDto {

	private static final long serialVersionUID = -8725734604520880084L;

	public static final String I18N_PREFIX = "EventParticipant";

	public static final String EVENT = "event";
	public static final String PERSON = "person";
	public static final String INVOLVEMENT_DESCRIPTION = "involvementDescription";
	public static final String RESULTING_CASE = "resultingCase";
	public static final String REPORTING_USER = "reportingUser";
	public static final String REGION = "region";
	public static final String DISTRICT = "district";
	public static final String VACCINATION_INFO = "vaccinationInfo";

	private UserReferenceDto reportingUser;
	@NotNull
	private EventReferenceDto event;
	@NotNull
	@EmbeddedPersonalData
	private PersonDto person;
	@SensitiveData
	private String involvementDescription;
	private CaseReferenceDto resultingCase; // read-only
	private RegionReferenceDto region;
	private DistrictReferenceDto district;

	private VaccinationInfoDto vaccinationInfo;

	private SormasToSormasOriginInfoDto sormasToSormasOriginInfo;
	private boolean ownershipHandedOver;

	public static EventParticipantDto build(EventReferenceDto event, UserReferenceDto reportingUser) {
		EventParticipantDto eventParticipant = new EventParticipantDto();
		eventParticipant.setUuid(DataHelper.createUuid());
		eventParticipant.setEvent(event);
		eventParticipant.setReportingUser(reportingUser);
		eventParticipant.setVaccinationInfo(VaccinationInfoDto.build());

		return eventParticipant;
	}

	public static EventParticipantDto buildFromCase(
		CaseReferenceDto caseReferenceDto,
		PersonDto person,
		EventReferenceDto event,
		UserReferenceDto reportingUser) {
		EventParticipantDto eventParticipantDto = build(event, reportingUser);

		eventParticipantDto.setPerson(person);
		eventParticipantDto.setResultingCase(caseReferenceDto);

		return eventParticipantDto;
	}

	public static EventParticipantDto buildFromPerson(PersonDto person, EventReferenceDto event, UserReferenceDto reportingUser) {
		EventParticipantDto eventParticipantDto = build(event, reportingUser);

		eventParticipantDto.setPerson(person);

		return eventParticipantDto;
	}

	public UserReferenceDto getReportingUser() {
		return reportingUser;
	}

	public void setReportingUser(UserReferenceDto reportingUser) {
		this.reportingUser = reportingUser;
	}

	public EventReferenceDto getEvent() {
		return event;
	}

	public void setEvent(EventReferenceDto event) {
		this.event = event;
	}

	public PersonDto getPerson() {
		return person;
	}

	public void setPerson(PersonDto person) {
		this.person = person;
	}

	public String getInvolvementDescription() {
		return involvementDescription;
	}

	public void setInvolvementDescription(String involvementDescription) {
		this.involvementDescription = involvementDescription;
	}

	public EventParticipantReferenceDto toReference() {
		return new EventParticipantReferenceDto(getUuid());
	}

	public CaseReferenceDto getResultingCase() {
		return resultingCase;
	}

	/**
	 * This should only be called when filling the DTO in the server backend!
	 */
	public void setResultingCase(CaseReferenceDto resultingCase) {
		this.resultingCase = resultingCase;
	}

	public RegionReferenceDto getRegion() {
		return region;
	}

	public void setRegion(RegionReferenceDto region) {
		this.region = region;
	}

	public DistrictReferenceDto getDistrict() {
		return district;
	}

	public void setDistrict(DistrictReferenceDto district) {
		this.district = district;
	}

	public VaccinationInfoDto getVaccinationInfo() {
		return vaccinationInfo;
	}

	public void setVaccinationInfo(VaccinationInfoDto vaccinationInfo) {
		this.vaccinationInfo = vaccinationInfo;
	}

	@Override
	public SormasToSormasOriginInfoDto getSormasToSormasOriginInfo() {
		return sormasToSormasOriginInfo;
	}

	@Override
	public void setSormasToSormasOriginInfo(SormasToSormasOriginInfoDto sormasToSormasOriginInfo) {
		this.sormasToSormasOriginInfo = sormasToSormasOriginInfo;
	}

	@Override
	public boolean isOwnershipHandedOver() {
		return ownershipHandedOver;
	}

	public void setOwnershipHandedOver(boolean ownershipHandedOver) {
		this.ownershipHandedOver = ownershipHandedOver;
	}
}
