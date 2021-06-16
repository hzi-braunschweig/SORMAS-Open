/*
 * SORMAS® - Surveillance Outbreak Response Management & Analysis System
 * Copyright © 2016-2020 Helmholtz-Zentrum für Infektionsforschung GmbH (HZI)
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

package de.symeda.sormas.backend.sormastosormas.origin;

import static de.symeda.sormas.api.EntityDto.COLUMN_LENGTH_BIG;
import static de.symeda.sormas.api.EntityDto.COLUMN_LENGTH_DEFAULT;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import de.symeda.auditlog.api.AuditedIgnore;
import de.symeda.sormas.backend.caze.Case;
import de.symeda.sormas.backend.common.AbstractDomainObject;
import de.symeda.sormas.backend.contact.Contact;
import de.symeda.sormas.backend.event.EventParticipant;
import de.symeda.sormas.backend.sormastosormas.share.sharerequest.SormasToSormasShareRequest;

@Entity(name = "sormastosormasorigininfo")
public class SormasToSormasOriginInfo extends AbstractDomainObject {

	private static final long serialVersionUID = -842917698322793413L;

	public static final String ORGANIZATION_ID = "organizationId";
	public static final String SENDER_NAME = "senderName";
	public static final String SENDER_EMAIL = "senderEmail";
	public static final String SENDER_PHONE_NUMBER = "senderPhoneNumber";
	public static final String OWNERSHIP_HANDED_OVER = "ownershipHandedOver";
	public static final String COMMENT = "comment";
	public static final String CASES = "cases";
	public static final String CONTACTS = "contacts";
	public static final String EVENT_PARTICIPANTS = "eventParticipants";

	private String organizationId;

	private String senderName;

	private String senderEmail;

	private String senderPhoneNumber;

	private boolean ownershipHandedOver;

	private String comment;

	private SormasToSormasShareRequest request;

	private List<Case> cases;

	private List<Contact> contacts;

	private List<EventParticipant> eventParticipants;

	@Column(length = COLUMN_LENGTH_DEFAULT, nullable = false)
	public String getOrganizationId() {
		return organizationId;
	}

	public void setOrganizationId(String organizationId) {
		this.organizationId = organizationId;
	}

	@Column(length = COLUMN_LENGTH_DEFAULT, nullable = false)
	public String getSenderName() {
		return senderName;
	}

	public void setSenderName(String senderName) {
		this.senderName = senderName;
	}

	@Column(length = COLUMN_LENGTH_DEFAULT)
	public String getSenderEmail() {
		return senderEmail;
	}

	public void setSenderEmail(String senderEmail) {
		this.senderEmail = senderEmail;
	}

	@Column(length = COLUMN_LENGTH_DEFAULT)
	public String getSenderPhoneNumber() {
		return senderPhoneNumber;
	}

	public void setSenderPhoneNumber(String senderPhoneNumber) {
		this.senderPhoneNumber = senderPhoneNumber;
	}

	@Column
	public boolean isOwnershipHandedOver() {
		return ownershipHandedOver;
	}

	public void setOwnershipHandedOver(boolean ownershipHandedOver) {
		this.ownershipHandedOver = ownershipHandedOver;
	}

	@OneToOne(mappedBy = "originInfo")
	public SormasToSormasShareRequest getRequest() {
		return request;
	}

	public void setRequest(SormasToSormasShareRequest request) {
		this.request = request;
	}

	@Column(length = COLUMN_LENGTH_BIG)
	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "sormasToSormasOriginInfo")
	@AuditedIgnore
	public List<Case> getCases() {
		return cases;
	}

	public void setCases(List<Case> cases) {
		this.cases = cases;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "sormasToSormasOriginInfo")
	@AuditedIgnore
	public List<Contact> getContacts() {
		return contacts;
	}

	public void setContacts(List<Contact> contacts) {
		this.contacts = contacts;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "sormasToSormasOriginInfo")
	@AuditedIgnore
	public List<EventParticipant> getEventParticipants() {
		return eventParticipants;
	}

	public void setEventParticipants(List<EventParticipant> eventParticipants) {
		this.eventParticipants = eventParticipants;
	}
}
