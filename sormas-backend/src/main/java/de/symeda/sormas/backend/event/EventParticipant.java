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
package de.symeda.sormas.backend.event;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import de.symeda.auditlog.api.Audited;
import de.symeda.auditlog.api.AuditedIgnore;
import de.symeda.sormas.backend.caze.Case;
import de.symeda.sormas.backend.common.CoreAdo;
import de.symeda.sormas.backend.person.Person;
import de.symeda.sormas.backend.region.District;
import de.symeda.sormas.backend.region.Region;
import de.symeda.sormas.backend.sample.Sample;
import de.symeda.sormas.backend.sormastosormas.entities.SormasToSormasEntity;
import de.symeda.sormas.backend.sormastosormas.origin.SormasToSormasOriginInfo;
import de.symeda.sormas.backend.sormastosormas.share.shareinfo.ShareInfoEventParticipant;
import de.symeda.sormas.backend.user.User;
import de.symeda.sormas.backend.vaccinationinfo.VaccinationInfo;

@Entity
@Audited
public class EventParticipant extends CoreAdo implements SormasToSormasEntity {

	private static final long serialVersionUID = -9006001699517297107L;

	public static final String TABLE_NAME = "eventparticipant";

	public static final String REPORTING_USER = "reportingUser";
	public static final String EVENT = "event";
	public static final String PERSON = "person";
	public static final String INVOLVEMENT_DESCRIPTION = "involvementDescription";
	public static final String RESULTING_CASE = "resultingCase";
	public static final String VACCINATION_INFO = "vaccinationInfo";
	public static final String SAMPLES = "samples";
	public static final String REGION = "region";
	public static final String DISTRICT = "district";
	public static final String SHARE_INFO_EVENT_PARTICIPANTS = "shareInfoEventParticipants";

	private User reportingUser;
	private Event event;
	private Person person;
	private String involvementDescription;
	private Case resultingCase;
	private Set<Sample> samples;
	private Region region;
	private District district;
	private VaccinationInfo vaccinationInfo;
	private SormasToSormasOriginInfo sormasToSormasOriginInfo;
	private List<ShareInfoEventParticipant> shareInfoEventParticipants = new ArrayList<>(0);

	@ManyToOne(cascade = {})
	public User getReportingUser() {
		return reportingUser;
	}

	public void setReportingUser(User reportingUser) {
		this.reportingUser = reportingUser;
	}

	@ManyToOne(cascade = {})
	public Event getEvent() {
		return event;
	}

	public void setEvent(Event event) {
		this.event = event;
	}

	@ManyToOne(cascade = {})
	@JoinColumn(nullable = false)
	public Person getPerson() {
		return person;
	}

	public void setPerson(Person person) {
		this.person = person;
	}

	@Column(length = 512)
	public String getInvolvementDescription() {
		return involvementDescription;
	}

	public void setInvolvementDescription(String involvementDescription) {
		this.involvementDescription = involvementDescription;
	}

	@Override
	public String toString() {
		return getPerson().toString();
	}

	@ManyToOne(cascade = {})
	@JoinColumn
	public Case getResultingCase() {
		return resultingCase;
	}

	public void setResultingCase(Case resultingCase) {
		this.resultingCase = resultingCase;
	}

	@OneToMany(mappedBy = Sample.ASSOCIATED_EVENT_PARTICIPANT, fetch = FetchType.LAZY)
	public Set<Sample> getSamples() {
		return samples;
	}

	public void setSamples(Set<Sample> samples) {
		this.samples = samples;
	}

	@ManyToOne(cascade = {})
	public Region getRegion() {
		return region;
	}

	public void setRegion(Region region) {
		this.region = region;
	}

	@ManyToOne(cascade = {})
	public District getDistrict() {
		return district;
	}

	public void setDistrict(District district) {
		this.district = district;
	}

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@AuditedIgnore
	public VaccinationInfo getVaccinationInfo() {
		return vaccinationInfo;
	}

	public void setVaccinationInfo(VaccinationInfo vaccinationInfo) {
		this.vaccinationInfo = vaccinationInfo;
	}

	@Override
	@ManyToOne(cascade = CascadeType.ALL)
	@AuditedIgnore
	public SormasToSormasOriginInfo getSormasToSormasOriginInfo() {
		return sormasToSormasOriginInfo;
	}

	@Override
	public void setSormasToSormasOriginInfo(SormasToSormasOriginInfo sormasToSormasOriginInfo) {
		this.sormasToSormasOriginInfo = sormasToSormasOriginInfo;
	}

	@OneToMany(mappedBy = ShareInfoEventParticipant.EVENT_PARTICIPANT, fetch = FetchType.LAZY)
	public List<ShareInfoEventParticipant> getShareInfoEventParticipants() {
		return shareInfoEventParticipants;
	}

	public void setShareInfoEventParticipants(List<ShareInfoEventParticipant> shareInfoEventParticipants) {
		this.shareInfoEventParticipants = shareInfoEventParticipants;
	}
}
