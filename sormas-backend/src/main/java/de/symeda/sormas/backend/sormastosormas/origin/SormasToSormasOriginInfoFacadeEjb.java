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

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

import de.symeda.sormas.api.sormastosormas.SormasToSormasOriginInfoDto;
import de.symeda.sormas.api.sormastosormas.SormasToSormasOriginInfoFacade;
import de.symeda.sormas.backend.sormastosormas.share.sharerequest.SormasToSormasShareRequestService;
import de.symeda.sormas.backend.util.DtoHelper;

@Stateless(name = "SormasToSormasOriginInfoFacade")
public class SormasToSormasOriginInfoFacadeEjb implements SormasToSormasOriginInfoFacade {

	@EJB
	private SormasToSormasOriginInfoService originInfoService;

	@EJB
	private SormasToSormasOriginInfoService sormasToSormasOriginInfoService;

	@EJB
	private SormasToSormasShareRequestService sormasToSormasShareRequestService;

	public SormasToSormasOriginInfoDto saveOriginInfo(SormasToSormasOriginInfoDto originInfoDto) {

		SormasToSormasOriginInfo originInfo = fromDto(originInfoDto, true);

		originInfoService.ensurePersisted(originInfo);

		return toDto(originInfo);
	}

	public SormasToSormasOriginInfo fromDto(SormasToSormasOriginInfoDto source, boolean checkChangeDate) {
		if (source == null) {
			return null;
		}

		SormasToSormasOriginInfo target = DtoHelper
			.fillOrBuildEntity(source, sormasToSormasOriginInfoService.getByUuid(source.getUuid()), SormasToSormasOriginInfo::new, checkChangeDate);

		target.setOrganizationId(source.getOrganizationId());
		target.setSenderName(source.getSenderName());
		target.setSenderEmail(source.getSenderEmail());
		target.setSenderPhoneNumber(source.getSenderPhoneNumber());
		target.setOwnershipHandedOver(source.isOwnershipHandedOver());
		target.setComment(source.getComment());

		return target;
	}

	public static SormasToSormasOriginInfoDto toDto(SormasToSormasOriginInfo source) {
		if (source == null) {
			return null;
		}

		SormasToSormasOriginInfoDto target = new SormasToSormasOriginInfoDto();

		DtoHelper.fillDto(target, source);

		target.setOrganizationId(source.getOrganizationId());
		target.setSenderName(source.getSenderName());
		target.setSenderEmail(source.getSenderEmail());
		target.setSenderPhoneNumber(source.getSenderPhoneNumber());
		target.setOwnershipHandedOver(source.isOwnershipHandedOver());
		target.setComment(source.getComment());

		return target;
	}

	@LocalBean
	@Stateless
	public static class SormasToSormasOriginInfoFacadeEjbLocal extends SormasToSormasOriginInfoFacadeEjb {

	}
}
