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

package de.symeda.sormas.backend.sormastosormas.share;

import java.util.ArrayList;
import java.util.List;

import de.symeda.sormas.backend.sormastosormas.entities.AssociatedEntityWrapper;

public class ShareData<T, DTO> {

	private final DTO dto;

	private final T entity;
	private final List<AssociatedEntityWrapper<?>> associatedEntities;

	public ShareData(T entity, DTO dto) {
		this.dto = dto;
		this.entity = entity;
		this.associatedEntities = new ArrayList<>();
	}

	public DTO getDto() {
		return dto;
	}

	public T getEntity() {
		return entity;
	}

	public void addAssociatedEntities(List<AssociatedEntityWrapper<?>> entities) {
		this.associatedEntities.addAll(entities);
	}

	public List<AssociatedEntityWrapper<?>> getAssociatedEntities() {
		return associatedEntities;
	}
}
