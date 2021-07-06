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

package de.symeda.sormas.api.immunization;

import java.util.Date;

import de.symeda.sormas.api.Disease;
import de.symeda.sormas.api.caze.CaseReferenceDto;
import de.symeda.sormas.api.person.PersonReferenceDto;
import de.symeda.sormas.api.region.CommunityReferenceDto;
import de.symeda.sormas.api.region.CountryReferenceDto;
import de.symeda.sormas.api.region.DistrictReferenceDto;
import de.symeda.sormas.api.region.RegionReferenceDto;
import de.symeda.sormas.api.user.UserReferenceDto;
import de.symeda.sormas.api.utils.EmbeddedPersonalData;
import de.symeda.sormas.api.utils.Outbreaks;
import de.symeda.sormas.api.utils.Required;
import de.symeda.sormas.api.utils.YesNoUnknown;
import de.symeda.sormas.api.utils.pseudonymization.PseudonymizableDto;

public class ImmunizationDto extends PseudonymizableDto {

	public static final String I18N_PREFIX = "ImmunizationData";

	public static final String DISEASE = "disease";

	@Outbreaks
	@Required
	private Disease disease;
	@Required
	@EmbeddedPersonalData
	private PersonReferenceDto person;
	private Date reportDate;
	private UserReferenceDto reportingUser;
	private boolean archived;
	private ImmunizationStatus immunizationStatus;
	private MeansOfImmunization meansOfImmunization;
	private String meansOfImmunizationDetails;
	private ImmunizationManagementStatus immunizationManagementStatus;
	private String externalId;

	private RegionReferenceDto responsibleRegion;
	private DistrictReferenceDto responsibleDistrict;
	private CommunityReferenceDto responsibleCommunity;
	private CountryReferenceDto country;

	private Date startDate;
	private Date endDate;
	private Integer numberOfDoses;
	private YesNoUnknown previousInfection;

	private Date lastInfectionDate;
	private String additionalDetails;

	private Date positiveTestResultDate;
	private Date recoveryDate;

	private CaseReferenceDto relatedCase;

	public Disease getDisease() {
		return disease;
	}

	public void setDisease(Disease disease) {
		this.disease = disease;
	}

	public PersonReferenceDto getPerson() {
		return person;
	}

	public void setPerson(PersonReferenceDto person) {
		this.person = person;
	}

	public Date getReportDate() {
		return reportDate;
	}

	public void setReportDate(Date reportDate) {
		this.reportDate = reportDate;
	}

	public UserReferenceDto getReportingUser() {
		return reportingUser;
	}

	public void setReportingUser(UserReferenceDto reportingUser) {
		this.reportingUser = reportingUser;
	}

	public boolean isArchived() {
		return archived;
	}

	public void setArchived(boolean archived) {
		this.archived = archived;
	}

	public ImmunizationStatus getImmunizationStatus() {
		return immunizationStatus;
	}

	public void setImmunizationStatus(ImmunizationStatus immunizationStatus) {
		this.immunizationStatus = immunizationStatus;
	}

	public String getExternalId() {
		return externalId;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	public RegionReferenceDto getResponsibleRegion() {
		return responsibleRegion;
	}

	public void setResponsibleRegion(RegionReferenceDto responsibleRegion) {
		this.responsibleRegion = responsibleRegion;
	}

	public DistrictReferenceDto getResponsibleDistrict() {
		return responsibleDistrict;
	}

	public void setResponsibleDistrict(DistrictReferenceDto responsibleDistrict) {
		this.responsibleDistrict = responsibleDistrict;
	}

	public CommunityReferenceDto getResponsibleCommunity() {
		return responsibleCommunity;
	}

	public void setResponsibleCommunity(CommunityReferenceDto responsibleCommunity) {
		this.responsibleCommunity = responsibleCommunity;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public Integer getNumberOfDoses() {
		return numberOfDoses;
	}

	public void setNumberOfDoses(Integer numberOfDoses) {
		this.numberOfDoses = numberOfDoses;
	}

	public YesNoUnknown getPreviousInfection() {
		return previousInfection;
	}

	public void setPreviousInfection(YesNoUnknown previousInfection) {
		this.previousInfection = previousInfection;
	}

	public Date getLastInfectionDate() {
		return lastInfectionDate;
	}

	public void setLastInfectionDate(Date lastInfectionDate) {
		this.lastInfectionDate = lastInfectionDate;
	}

	public String getAdditionalDetails() {
		return additionalDetails;
	}

	public void setAdditionalDetails(String additionalDetails) {
		this.additionalDetails = additionalDetails;
	}

	public Date getPositiveTestResultDate() {
		return positiveTestResultDate;
	}

	public void setPositiveTestResultDate(Date positiveTestResultDate) {
		this.positiveTestResultDate = positiveTestResultDate;
	}

	public Date getRecoveryDate() {
		return recoveryDate;
	}

	public void setRecoveryDate(Date recoveryDate) {
		this.recoveryDate = recoveryDate;
	}

	public CaseReferenceDto getRelatedCase() {
		return relatedCase;
	}

	public void setRelatedCase(CaseReferenceDto relatedCase) {
		this.relatedCase = relatedCase;
	}

	public MeansOfImmunization getMeansOfImmunization() {
		return meansOfImmunization;
	}

	public void setMeansOfImmunization(MeansOfImmunization meansOfImmunization) {
		this.meansOfImmunization = meansOfImmunization;
	}

	public ImmunizationManagementStatus getImmunizationManagementStatus() {
		return immunizationManagementStatus;
	}

	public void setImmunizationManagementStatus(ImmunizationManagementStatus immunizationManagementStatus) {
		this.immunizationManagementStatus = immunizationManagementStatus;
	}

	public String getMeansOfImmunizationDetails() {
		return meansOfImmunizationDetails;
	}

	public void setMeansOfImmunizationDetails(String meansOfImmunizationDetails) {
		this.meansOfImmunizationDetails = meansOfImmunizationDetails;
	}

	public CountryReferenceDto getCountry() {
		return country;
	}

	public void setCountry(CountryReferenceDto country) {
		this.country = country;
	}
}
