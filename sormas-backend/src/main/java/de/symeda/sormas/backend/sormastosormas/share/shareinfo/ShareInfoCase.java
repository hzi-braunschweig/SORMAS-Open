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

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import de.symeda.sormas.backend.caze.Case;

@Entity
@DiscriminatorValue("CASE")
public class ShareInfoCase extends ShareInfoEntity {

	private static final long serialVersionUID = 7389931026555830033L;

	public static final String CAZE = "caze";

	private Case caze;

	public ShareInfoCase() {
	}

	public ShareInfoCase(SormasToSormasShareInfo shareInfo, Case caze) {
		super(shareInfo);
		this.caze = caze;
	}

	@ManyToOne
	public Case getCaze() {
		return caze;
	}

	public void setCaze(Case caze) {
		this.caze = caze;
	}
}
