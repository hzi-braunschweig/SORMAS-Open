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

package de.symeda.sormas.backend.sormastosormas.share.sharerequest;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.OneToOne;

import org.hibernate.annotations.Type;

import de.symeda.auditlog.api.AuditedIgnore;
import de.symeda.sormas.api.sormastosormas.sharerequest.ShareRequestDataType;
import de.symeda.sormas.api.sormastosormas.sharerequest.ShareRequestStatus;
import de.symeda.sormas.api.sormastosormas.sharerequest.SormasToSormasCasePreview;
import de.symeda.sormas.api.sormastosormas.sharerequest.SormasToSormasContactPreview;
import de.symeda.sormas.api.sormastosormas.sharerequest.SormasToSormasEventPreview;
import de.symeda.sormas.backend.common.AbstractDomainObject;
import de.symeda.sormas.backend.sormastosormas.origin.SormasToSormasOriginInfo;

@Entity(name = "sormastosormassharerequest")
public class SormasToSormasShareRequest extends AbstractDomainObject {

	private static final long serialVersionUID = 1116921896060439299L;

	public static final String DATA_TYPE = "dataType";
	public static final String STATUS = "status";
	public static final String ORIGIN_INFO = "originInfo";
	public static final String CASES = "cases";
	public static final String CONTACTS = "contacts";
	public static final String EVENTS = "events";

	private ShareRequestDataType dataType;

	private ShareRequestStatus status;

	private SormasToSormasOriginInfo originInfo;

	private List<SormasToSormasCasePreview> cases;
	private List<SormasToSormasContactPreview> contacts;
	private List<SormasToSormasEventPreview> events;

	public SormasToSormasShareRequest() {
	}

	@Enumerated(EnumType.STRING)
	public ShareRequestDataType getDataType() {
		return dataType;
	}

	public void setDataType(ShareRequestDataType dataType) {
		this.dataType = dataType;
	}

	@Enumerated(EnumType.STRING)
	public ShareRequestStatus getStatus() {
		return status;
	}

	public void setStatus(ShareRequestStatus status) {
		this.status = status;
	}

	@OneToOne(cascade = CascadeType.ALL, optional = false)
	public SormasToSormasOriginInfo getOriginInfo() {
		return originInfo;
	}

	public void setOriginInfo(SormasToSormasOriginInfo originInfo) {
		this.originInfo = originInfo;
	}

	@AuditedIgnore
	@Type(type = "json")
	@Column(columnDefinition = "json")
	public List<SormasToSormasCasePreview> getCases() {
		return cases;
	}

	public void setCases(List<SormasToSormasCasePreview> cases) {
		this.cases = cases;
	}

	@AuditedIgnore
	@Type(type = "json")
	@Column(columnDefinition = "json")
	public List<SormasToSormasContactPreview> getContacts() {
		return contacts;
	}

	public void setContacts(List<SormasToSormasContactPreview> contacts) {
		this.contacts = contacts;
	}

	@AuditedIgnore
	@Type(type = "json")
	@Column(columnDefinition = "json")
	public List<SormasToSormasEventPreview> getEvents() {
		return events;
	}

	public void setEvents(List<SormasToSormasEventPreview> events) {
		this.events = events;
	}
}
