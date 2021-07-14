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

package de.symeda.sormas.backend.sormastosormas;

import java.io.Serializable;

class ShareTreeCriteria implements Serializable {

	private static final long serialVersionUID = 131837357088474316L;

	private String entityUuid;
	private String exceptedOrganizationId;
	private boolean forwardOnly;

	public ShareTreeCriteria() {
	}

	public ShareTreeCriteria(String entityUuid, String exceptedOrganizationId, boolean forwardOnly) {
		this.entityUuid = entityUuid;
		this.exceptedOrganizationId = exceptedOrganizationId;
		this.forwardOnly = forwardOnly;
	}

	public String getEntityUuid() {
		return entityUuid;
	}

	public void setEntityUuid(String entityUuid) {
		this.entityUuid = entityUuid;
	}

	public String getExceptedOrganizationId() {
		return exceptedOrganizationId;
	}

	public void setExceptedOrganizationId(String exceptedOrganizationId) {
		this.exceptedOrganizationId = exceptedOrganizationId;
	}

	public boolean isForwardOnly() {
		return forwardOnly;
	}

	public void setForwardOnly(boolean forwardOnly) {
		this.forwardOnly = forwardOnly;
	}
}
