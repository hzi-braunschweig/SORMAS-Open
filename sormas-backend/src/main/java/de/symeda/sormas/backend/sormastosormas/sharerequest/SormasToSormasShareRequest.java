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

package de.symeda.sormas.backend.sormastosormas.sharerequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Type;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.symeda.auditlog.api.AuditedIgnore;
import de.symeda.sormas.api.sormastosormas.sharerequest.ShareRequestDataType;
import de.symeda.sormas.api.sormastosormas.sharerequest.ShareRequestStatus;
import de.symeda.sormas.api.sormastosormas.sharerequest.SormasToSormasCasePreview;
import de.symeda.sormas.api.sormastosormas.sharerequest.SormasToSormasContactPreview;
import de.symeda.sormas.api.sormastosormas.sharerequest.SormasToSormasEventPreview;
import de.symeda.sormas.api.utils.ValidationRuntimeException;
import de.symeda.sormas.backend.common.AbstractDomainObject;
import de.symeda.sormas.backend.sormastosormas.SormasToSormasOriginInfo;

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

	private String cases;
	private String contacts;
	private String events;

	private List<SormasToSormasCasePreview> casesList;
	private List<SormasToSormasContactPreview> contactsList;
	private List<SormasToSormasEventPreview> eventsList;

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
	public String getCases() {
		return cases;
	}

	public void setCases(String cases) {
		this.cases = cases;
		casesList = null;
	}

	@AuditedIgnore
	@Type(type = "json")
	@Column(columnDefinition = "json")
	public String getContacts() {
		return contacts;
	}

	public void setContacts(String contacts) {
		this.contacts = contacts;
		contactsList = null;
	}

	@AuditedIgnore
	@Type(type = "json")
	@Column(columnDefinition = "json")
	public String getEvents() {
		return events;
	}

	public void setEvents(String events) {
		this.events = events;
		eventsList = null;
	}

	@Transient
	public List<SormasToSormasCasePreview> getCasesList() {
		if (casesList == null) {
			if (StringUtils.isBlank(cases)) {
				casesList = new ArrayList<>();
			} else {
				try {
					ObjectMapper mapper = new ObjectMapper();
					casesList = Arrays.asList(mapper.readValue(cases, SormasToSormasCasePreview[].class));
				} catch (IOException e) {
					throw new ValidationRuntimeException("Content of cases could not be parsed to List<SormasToSormasCasePreview> - ID: " + getId());
				}
			}
		}
		return casesList;
	}

	public void setCasesList(List<SormasToSormasCasePreview> casesList) {
		this.casesList = casesList;

		if (this.casesList == null) {
			cases = null;
			return;
		}

		try {
			ObjectMapper mapper = new ObjectMapper();
			cases = mapper.writeValueAsString(casesList);
		} catch (JsonProcessingException e) {
			throw new RuntimeException("Content of casesList could not be parsed to JSON String - ID: " + getId());
		}
	}

	@Transient
	public List<SormasToSormasContactPreview> getContactsList() {
		if (contactsList == null) {
			if (StringUtils.isBlank(contacts)) {
				contactsList = new ArrayList<>();
			} else {
				try {
					ObjectMapper mapper = new ObjectMapper();
					contactsList = Arrays.asList(mapper.readValue(contacts, SormasToSormasContactPreview[].class));
				} catch (IOException e) {
					throw new ValidationRuntimeException(
						"Content of contacts could not be parsed to List<SormasToSormasContactPreview> - ID: " + getId());
				}
			}
		}
		return contactsList;
	}

	public void setContactsList(List<SormasToSormasContactPreview> contactsList) {
		this.contactsList = contactsList;

		if (this.contactsList == null) {
			contacts = null;
			return;
		}

		try {
			ObjectMapper mapper = new ObjectMapper();
			contacts = mapper.writeValueAsString(contactsList);
		} catch (JsonProcessingException e) {
			throw new RuntimeException("Content of contactsList could not be parsed to JSON String - ID: " + getId());
		}
	}

	@Transient
	public List<SormasToSormasEventPreview> getEventsList() {
		if (eventsList == null) {
			if (StringUtils.isBlank(events)) {
				eventsList = new ArrayList<>();
			} else {
				try {
					ObjectMapper mapper = new ObjectMapper();
					eventsList = Arrays.asList(mapper.readValue(events, SormasToSormasEventPreview[].class));
				} catch (IOException e) {
					throw new ValidationRuntimeException(
						"Content of events could not be parsed to List<SormasToSormasEventPreview> - ID: " + getId());
				}
			}
		}
		return eventsList;
	}

	public void setEventsList(List<SormasToSormasEventPreview> eventsList) {
		this.eventsList = eventsList;

		if (this.eventsList == null) {
			events = null;
			return;
		}

		try {
			ObjectMapper mapper = new ObjectMapper();
			events = mapper.writeValueAsString(eventsList);
		} catch (JsonProcessingException e) {
			throw new RuntimeException("Content of eventsList could not be parsed to JSON String - ID: " + getId());
		}
	}
}
