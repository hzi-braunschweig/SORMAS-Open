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

package de.symeda.sormas.backend.sormastosormas.share.shareinfo;

import static de.symeda.sormas.api.EntityDto.COLUMN_LENGTH_BIG;
import static de.symeda.sormas.api.EntityDto.COLUMN_LENGTH_DEFAULT;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.hibernate.annotations.Where;

import de.symeda.sormas.api.sormastosormas.sharerequest.ShareRequestStatus;
import de.symeda.sormas.backend.common.AbstractDomainObject;
import de.symeda.sormas.backend.user.User;

@Entity(name = "sormastosormasshareinfo")
public class SormasToSormasShareInfo extends AbstractDomainObject {

	private static final long serialVersionUID = -8368155805122562791L;

	public static final String CASES = "cases";
	public static final String CONTACTS = "contacts";
	public static final String SAMPLES = "samples";
	public static final String EVENTS = "events";
	public static final String EVENT_PARTICIPANTS = "eventParticipants";
	public static final String OWNERSHIP_HANDED_OVER = "ownershipHandedOver";
	public static final String ORGANIZATION_ID = "organizationId";
	public static final String REQUEST_UUID = "requestUuid";
	public static final String REQUEST_STATUS = "requestStatus";

	private List<ShareInfoCase> cases;

	private List<ShareInfoContact> contacts;

	private List<ShareInfoSample> samples;

	private List<ShareInfoEvent> events;

	private List<ShareInfoEventParticipant> eventParticipants;

	private String organizationId;

	private User sender;

	private boolean ownershipHandedOver;

	private boolean withAssociatedContacts;

	private boolean withSamples;

	private boolean withEventParticipants;

	private boolean pseudonymizedPersonalData;

	private boolean pseudonymizedSensitiveData;

	private String comment;

	private String requestUuid;
	private ShareRequestStatus requestStatus;

	public SormasToSormasShareInfo() {
		cases = new ArrayList<>();
		contacts = new ArrayList<>();
		samples = new ArrayList<>();
		events = new ArrayList<>();
		eventParticipants = new ArrayList<>();
	}

	@OneToMany(mappedBy = ShareInfoCase.SHARE_INFO, cascade = CascadeType.ALL, targetEntity = ShareInfoCase.class)
	@Where(clause = "type='CASE'")
	public List<ShareInfoCase> getCases() {
		return cases;
	}

	public void setCases(List<ShareInfoCase> cases) {
		this.cases = cases;
	}

	@OneToMany(mappedBy = ShareInfoContact.SHARE_INFO, cascade = CascadeType.ALL, targetEntity = ShareInfoContact.class)
	@Where(clause = "type='CONTACT'")
	public List<ShareInfoContact> getContacts() {
		return contacts;
	}

	public void setContacts(List<ShareInfoContact> contacts) {
		this.contacts = contacts;
	}

	@OneToMany(mappedBy = ShareInfoSample.SHARE_INFO, cascade = CascadeType.ALL, targetEntity = ShareInfoSample.class)
	@Where(clause = "type='SAMPLE'")
	public List<ShareInfoSample> getSamples() {
		return samples;
	}

	public void setSamples(List<ShareInfoSample> samples) {
		this.samples = samples;
	}

	@OneToMany(mappedBy = ShareInfoEvent.SHARE_INFO, cascade = CascadeType.ALL, targetEntity = ShareInfoEvent.class)
	@Where(clause = "type='EVENT'")
	public List<ShareInfoEvent> getEvents() {
		return events;
	}

	public void setEvents(List<ShareInfoEvent> events) {
		this.events = events;
	}

	@OneToMany(mappedBy = ShareInfoEventParticipant.SHARE_INFO, cascade = CascadeType.ALL, targetEntity = ShareInfoEventParticipant.class)
	@Where(clause = "type='EVENT_PARTICIPANT'")
	public List<ShareInfoEventParticipant> getEventParticipants() {
		return eventParticipants;
	}

	public void setEventParticipants(List<ShareInfoEventParticipant> eventParticipants) {
		this.eventParticipants = eventParticipants;
	}

	@Column(length = COLUMN_LENGTH_DEFAULT, nullable = false)
	public String getOrganizationId() {
		return organizationId;
	}

	public void setOrganizationId(String organizationId) {
		this.organizationId = organizationId;
	}

	@ManyToOne
	@JoinColumn(nullable = false)
	public User getSender() {
		return sender;
	}

	public void setSender(User sender) {
		this.sender = sender;
	}

	@Column
	public boolean isOwnershipHandedOver() {
		return ownershipHandedOver;
	}

	public void setOwnershipHandedOver(boolean ownershipHandedOver) {
		this.ownershipHandedOver = ownershipHandedOver;
	}

	@Column
	public boolean isWithAssociatedContacts() {
		return withAssociatedContacts;
	}

	public void setWithAssociatedContacts(boolean withAssociatedContacts) {
		this.withAssociatedContacts = withAssociatedContacts;
	}

	@Column
	public boolean isWithSamples() {
		return withSamples;
	}

	public void setWithSamples(boolean withSamples) {
		this.withSamples = withSamples;
	}

	@Column
	public boolean isWithEventParticipants() {
		return withEventParticipants;
	}

	public void setWithEventParticipants(boolean withEventParticipants) {
		this.withEventParticipants = withEventParticipants;
	}

	@Column
	public boolean isPseudonymizedPersonalData() {
		return pseudonymizedPersonalData;
	}

	public void setPseudonymizedPersonalData(boolean pseudonymizedPersonalData) {
		this.pseudonymizedPersonalData = pseudonymizedPersonalData;
	}

	@Column
	public boolean isPseudonymizedSensitiveData() {
		return pseudonymizedSensitiveData;
	}

	public void setPseudonymizedSensitiveData(boolean pseudonymizedSensitiveData) {
		this.pseudonymizedSensitiveData = pseudonymizedSensitiveData;
	}

	@Column(length = COLUMN_LENGTH_BIG)
	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	@Column(unique = true, length = 36)
	public String getRequestUuid() {
		return requestUuid;
	}

	public void setRequestUuid(String requestUuid) {
		this.requestUuid = requestUuid;
	}

	@Enumerated(EnumType.STRING)
	public ShareRequestStatus getRequestStatus() {
		return requestStatus;
	}

	public void setRequestStatus(ShareRequestStatus requestStatus) {
		this.requestStatus = requestStatus;
	}
}
