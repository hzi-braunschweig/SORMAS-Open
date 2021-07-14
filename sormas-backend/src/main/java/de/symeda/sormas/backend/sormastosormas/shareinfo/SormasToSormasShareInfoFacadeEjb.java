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

package de.symeda.sormas.backend.sormastosormas.shareinfo;

import java.util.List;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import de.symeda.sormas.api.sormastosormas.shareinfo.SormasToSormasShareInfoCriteria;
import de.symeda.sormas.api.sormastosormas.shareinfo.SormasToSormasShareInfoDto;
import de.symeda.sormas.api.sormastosormas.shareinfo.SormasToSormasShareInfoFacade;
import de.symeda.sormas.backend.sormastosormas.OrganizationServerAccessData;
import de.symeda.sormas.backend.sormastosormas.ServerAccessDataService;
import de.symeda.sormas.backend.util.DtoHelper;
import de.symeda.sormas.backend.util.ModelConstants;
import de.symeda.sormas.backend.util.QueryHelper;

@Stateless(name = "SormasToSormasShareInfoFacade")
public class SormasToSormasShareInfoFacadeEjb implements SormasToSormasShareInfoFacade {

	@PersistenceContext(unitName = ModelConstants.PERSISTENCE_UNIT_NAME)
	private EntityManager em;

	@EJB
	private SormasToSormasShareInfoService shareInfoService;

	@EJB
	private ServerAccessDataService serverAccessDataService;

	@Override
	public List<SormasToSormasShareInfoDto> getIndexList(SormasToSormasShareInfoCriteria criteria, Integer first, Integer max) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<SormasToSormasShareInfo> cq = cb.createQuery(SormasToSormasShareInfo.class);
		Root<SormasToSormasShareInfo> root = cq.from(SormasToSormasShareInfo.class);

		Predicate filter = shareInfoService.buildCriteriaFilter(criteria, cb, root);
		if (filter != null) {
			cq.where(filter);
		}

		cq.distinct(true);

		return QueryHelper.getResultList(em, cq, first, max, this::toDto);
	}

	public SormasToSormasShareInfoDto getShareInfoByUuid(String uuid) {
		return toDto(shareInfoService.getByUuid(uuid));
	}

	public SormasToSormasShareInfoDto toDto(SormasToSormasShareInfo source) {
		SormasToSormasShareInfoDto target = new SormasToSormasShareInfoDto();

		DtoHelper.fillDto(target, source);

		OrganizationServerAccessData serverAccessData = serverAccessDataService.getServerListItemById(source.getOrganizationId())
			.orElseGet(() -> new OrganizationServerAccessData(source.getOrganizationId(), source.getOrganizationId()));
		target.setTarget(serverAccessData.toReference());

		target.setRequestStatus(source.getRequestStatus());
		target.setSender(source.getSender().toReference());
		target.setOwnershipHandedOver(source.isOwnershipHandedOver());
		target.setWithAssociatedContacts(source.isWithAssociatedContacts());
		target.setWithSamples(source.isWithSamples());
		target.setWithEvenParticipants(source.isWithEventParticipants());
		target.setPseudonymizedPersonalData(source.isPseudonymizedPersonalData());
		target.setPseudonymizedSensitiveData(source.isPseudonymizedSensitiveData());
		target.setComment(source.getComment());

		return target;
	}

	@LocalBean
	@Stateless
	public static class SormasToSormasShareInfoFacadeEjbLocal extends SormasToSormasShareInfoFacadeEjb {

	}
}
