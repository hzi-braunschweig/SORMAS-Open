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

import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import de.symeda.sormas.backend.common.AbstractDomainObject;

@Entity
@Table(name = "sormastosormasshareinfo_entities")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type", discriminatorType = DiscriminatorType.STRING)
public abstract class ShareInfoEntity extends AbstractDomainObject {

	private static final long serialVersionUID = 7901638429986796896L;

	public static final String SHARE_INFO = "shareInfo";

	private SormasToSormasShareInfo shareInfo;

	public ShareInfoEntity() {
	}

	protected ShareInfoEntity(SormasToSormasShareInfo shareInfo) {
		this.shareInfo = shareInfo;
	}

	@ManyToOne
	public SormasToSormasShareInfo getShareInfo() {
		return shareInfo;
	}

	public void setShareInfo(SormasToSormasShareInfo shareInfo) {
		this.shareInfo = shareInfo;
	}
}
