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
package de.symeda.sormas.api.document;

import de.symeda.sormas.api.user.UserReferenceDto;
import de.symeda.sormas.api.utils.DataHelper;
import javax.validation.constraints.NotNull;
import de.symeda.sormas.api.utils.pseudonymization.PseudonymizableDto;

public class DocumentDto extends PseudonymizableDto {

	public static final String UPLOADING_USER = "uploadingUser";
	public static final String NAME = "name";
	public static final String CONTENT_TYPE = "contentType";
	public static final String SIZE = "size";
	public static final String RELATED_ENTITY_UUID = "relatedEntityUuid";
	public static final String RELATED_ENTITY_CLASS = "relatedEntityClass";

	@NotNull
	private UserReferenceDto uploadingUser;
	@NotNull
	private String name;
	@NotNull
	private String mimeType;
	@NotNull
	private long size;
	@NotNull
	private String relatedEntityUuid;
	@NotNull
	private DocumentRelatedEntityType relatedEntityType;

	public static DocumentDto build() {
		DocumentDto document = new DocumentDto();
		document.setUuid(DataHelper.createUuid());

		return document;
	}

	public UserReferenceDto getUploadingUser() {
		return uploadingUser;
	}

	public void setUploadingUser(UserReferenceDto uploadingUser) {
		this.uploadingUser = uploadingUser;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public String getRelatedEntityUuid() {
		return relatedEntityUuid;
	}

	public void setRelatedEntityUuid(String relatedEntityUuid) {
		this.relatedEntityUuid = relatedEntityUuid;
	}

	public DocumentRelatedEntityType getRelatedEntityType() {
		return relatedEntityType;
	}

	public void setRelatedEntityType(DocumentRelatedEntityType relatedEntityType) {
		this.relatedEntityType = relatedEntityType;
	}
}
