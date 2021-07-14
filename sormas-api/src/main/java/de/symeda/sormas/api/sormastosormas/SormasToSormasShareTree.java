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

package de.symeda.sormas.api.sormastosormas;

import java.io.Serializable;
import java.util.List;

import de.symeda.sormas.api.sormastosormas.shareinfo.SormasToSormasShareInfoDto;

public class SormasToSormasShareTree implements Serializable {

	private static final long serialVersionUID = -6310301421124024550L;

	private SormasToSormasOriginInfoDto origin;

	private SormasToSormasShareInfoDto share;

	private List<SormasToSormasShareTree> reShares;

	public SormasToSormasShareTree() {
	}

	public SormasToSormasShareTree(SormasToSormasOriginInfoDto origin, SormasToSormasShareInfoDto share, List<SormasToSormasShareTree> reShares) {
		this.origin = origin;
		this.share = share;
		this.reShares = reShares;
	}

	public SormasToSormasOriginInfoDto getOrigin() {
		return origin;
	}

	public SormasToSormasShareInfoDto getShare() {
		return share;
	}

	public List<SormasToSormasShareTree> getReShares() {
		return reShares;
	}

	public void setReShares(List<SormasToSormasShareTree> reShares) {
		this.reShares = reShares;
	}
}
