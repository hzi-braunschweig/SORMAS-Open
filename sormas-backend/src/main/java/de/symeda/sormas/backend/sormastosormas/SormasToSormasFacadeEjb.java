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

package de.symeda.sormas.backend.sormastosormas;

import static de.symeda.sormas.api.sormastosormas.SormasToSormasApiConstants.RESOURCE_PATH;
import static de.symeda.sormas.backend.sormastosormas.entities.contact.SormasToSormasContactFacadeEjb.SormasToSormasContactFacadeEjbLocal;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import de.symeda.sormas.api.sormastosormas.ServerAccessDataReferenceDto;
import de.symeda.sormas.api.sormastosormas.SormasToSormasApiConstants;
import de.symeda.sormas.api.sormastosormas.SormasToSormasEncryptedDataDto;
import de.symeda.sormas.api.sormastosormas.SormasToSormasEntityInterface;
import de.symeda.sormas.api.sormastosormas.SormasToSormasException;
import de.symeda.sormas.api.sormastosormas.SormasToSormasFacade;
import de.symeda.sormas.api.sormastosormas.SormasToSormasShareInfoCriteria;
import de.symeda.sormas.api.sormastosormas.SormasToSormasShareInfoDto;
import de.symeda.sormas.api.sormastosormas.SormasToSormasValidationException;
import de.symeda.sormas.api.sormastosormas.sharerequest.ShareRequestDataType;
import de.symeda.sormas.api.sormastosormas.sharerequest.ShareRequestStatus;
import de.symeda.sormas.api.sormastosormas.sharerequest.SormasToSormasShareRequestDto;
import de.symeda.sormas.api.user.UserRight;
import de.symeda.sormas.backend.sormastosormas.access.OrganizationServerAccessData;
import de.symeda.sormas.backend.sormastosormas.access.ServerAccessDataService;
import de.symeda.sormas.backend.sormastosormas.crypto.SormasToSormasEncryptionService;
import de.symeda.sormas.backend.sormastosormas.entities.caze.SormasToSormasCaseFacadeEjb.SormasToSormasCaseFacadeEjbLocal;
import de.symeda.sormas.backend.sormastosormas.entities.event.SormasToSormasEventFacadeEjb.SormasToSormasEventFacadeEjbLocal;
import de.symeda.sormas.backend.sormastosormas.rest.SormasToSormasRestClient;
import de.symeda.sormas.backend.sormastosormas.share.shareinfo.SormasToSormasShareInfo;
import de.symeda.sormas.backend.sormastosormas.share.shareinfo.SormasToSormasShareInfoService;
import de.symeda.sormas.backend.sormastosormas.share.sharerequest.SormasToSormasShareRequestFacadeEJB.SormasToSormasShareRequestFacadeEJBLocal;
import de.symeda.sormas.backend.user.UserService;
import de.symeda.sormas.backend.util.DtoHelper;
import de.symeda.sormas.backend.util.ModelConstants;

@Stateless(name = "SormasToSormasFacade")
public class SormasToSormasFacadeEjb implements SormasToSormasFacade {

	private static final String REVOKE_REQUEST_ENDPOINT = RESOURCE_PATH + SormasToSormasApiConstants.REVOKE_REQUESTS_ENDPOINT;

	@PersistenceContext(unitName = ModelConstants.PERSISTENCE_UNIT_NAME)
	private EntityManager em;
	@EJB
	private SormasToSormasShareInfoService shareInfoService;
	@EJB
	private UserService userService;
	@EJB
	private ServerAccessDataService serverAccessDataService;
	@Inject
	private SormasToSormasRestClient sormasToSormasRestClient;
	@EJB
	private SormasToSormasShareRequestFacadeEJBLocal shareRequestFacade;
	@EJB
	private SormasToSormasCaseFacadeEjbLocal sormasToSormasCaseFacade;
	@EJB
	private SormasToSormasContactFacadeEjbLocal sormasToSormasContactFacade;
	@EJB
	private SormasToSormasEventFacadeEjbLocal sormasToSormasEventFacade;
	@EJB
	private SormasToSormasEncryptionService encryptionService;

	@Override
	public List<ServerAccessDataReferenceDto> getAvailableOrganizations() {
		return serverAccessDataService.getOrganizationList().stream().map(OrganizationServerAccessData::toReference).collect(Collectors.toList());
	}

	@Override
	public ServerAccessDataReferenceDto getOrganizationRef(String id) {
		return serverAccessDataService.getServerListItemById(id).map(OrganizationServerAccessData::toReference).orElseGet(null);
	}

	@Override
	public List<SormasToSormasShareInfoDto> getShareInfoIndexList(SormasToSormasShareInfoCriteria criteria, Integer first, Integer max) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<SormasToSormasShareInfo> cq = cb.createQuery(SormasToSormasShareInfo.class);
		Root<SormasToSormasShareInfo> root = cq.from(SormasToSormasShareInfo.class);

		Predicate filter = shareInfoService.buildCriteriaFilter(criteria, cb, root);
		if (filter != null) {
			cq.where(filter);
		}

		cq.distinct(true);

		List<SormasToSormasShareInfo> resultList;
		if (first != null && max != null) {
			resultList = em.createQuery(cq).setFirstResult(first).setMaxResults(max).getResultList();
		} else {
			resultList = em.createQuery(cq).getResultList();
		}

		return resultList.stream().map(this::toSormasToSormasShareInfoDto).collect(Collectors.toList());
	}

	@Override
	public void rejectShareRequest(ShareRequestDataType dataType, String uuid) throws SormasToSormasException {
		getEntityInterface(dataType).sendRejectShareRequest(uuid);
	}

	@Override
	public void acceptShareRequest(ShareRequestDataType dataType, String uuid) throws SormasToSormasException, SormasToSormasValidationException {
		getEntityInterface(dataType).acceptShareRequest(uuid);
	}

	@Override
	public void revokeShare(String shareInfoUuid) throws SormasToSormasException {
		SormasToSormasShareInfo shareInfo = shareInfoService.getByUuid(shareInfoUuid);

		sormasToSormasRestClient
			.post(shareInfo.getOrganizationId(), REVOKE_REQUEST_ENDPOINT, Collections.singletonList(shareInfo.getRequestUuid()), null);

		shareInfo.setRequestStatus(ShareRequestStatus.REVOKED);
		shareInfoService.ensurePersisted(shareInfo);
	}

	@Override
	public void revokeRequests(SormasToSormasEncryptedDataDto encryptedRequestUuid) throws SormasToSormasException {
		String requestUuid = encryptionService.decryptAndVerify(encryptedRequestUuid, String.class);
		SormasToSormasShareRequestDto shareRequest = shareRequestFacade.getShareRequestByUuid(requestUuid);

		shareRequest.setChangeDate(new Date());
		shareRequest.setStatus(ShareRequestStatus.REVOKED);
		shareRequestFacade.saveShareRequest(shareRequest);
	}

	@Override
	public boolean isFeatureEnabled() {
		return userService.hasRight(UserRight.SORMAS_TO_SORMAS_SHARE) && !serverAccessDataService.getOrganizationList().isEmpty();
	}

	public SormasToSormasShareInfoDto toSormasToSormasShareInfoDto(SormasToSormasShareInfo source) {
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

	private SormasToSormasEntityInterface getEntityInterface(ShareRequestDataType dataType) {
		switch (dataType) {
		case CASE:
			return sormasToSormasCaseFacade;
		case CONTACT:
			return sormasToSormasContactFacade;
		case EVENT:
			return sormasToSormasEventFacade;
		default:
			throw new RuntimeException("Unknown request [" + dataType + "]");
		}
	}

	@LocalBean
	@Stateless
	public static class SormasToSormasFacadeEjbLocal extends SormasToSormasFacadeEjb {

	}
}
