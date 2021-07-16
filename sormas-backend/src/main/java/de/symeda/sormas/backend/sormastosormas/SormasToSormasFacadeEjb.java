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

import java.util.Arrays;
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
import javax.transaction.Transactional;

import de.symeda.sormas.api.feature.FeatureType;
import de.symeda.sormas.backend.feature.FeatureConfigurationFacadeEjb.FeatureConfigurationFacadeEjbLocal;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.symeda.sormas.api.SormasToSormasConfig;
import de.symeda.sormas.api.externalsurveillancetool.ExternalSurveillanceToolException;
import de.symeda.sormas.api.i18n.Strings;
import de.symeda.sormas.api.sormastosormas.ServerAccessDataReferenceDto;
import de.symeda.sormas.api.sormastosormas.SormasToSormasApiConstants;
import de.symeda.sormas.api.sormastosormas.SormasToSormasEncryptedDataDto;
import de.symeda.sormas.api.sormastosormas.SormasToSormasEntityInterface;
import de.symeda.sormas.api.sormastosormas.SormasToSormasException;
import de.symeda.sormas.api.sormastosormas.SormasToSormasFacade;
import de.symeda.sormas.api.sormastosormas.SormasToSormasShareInfoCriteria;
import de.symeda.sormas.api.sormastosormas.SormasToSormasShareInfoDto;
import de.symeda.sormas.api.sormastosormas.validation.SormasToSormasValidationException;
import de.symeda.sormas.api.sormastosormas.sharerequest.ShareRequestDataType;
import de.symeda.sormas.api.sormastosormas.sharerequest.ShareRequestStatus;
import de.symeda.sormas.api.sormastosormas.sharerequest.SormasToSormasShareRequestDto;
import de.symeda.sormas.api.user.UserRight;
import de.symeda.sormas.backend.sormastosormas.caze.SormasToSormasCaseFacadeEjb.SormasToSormasCaseFacadeEjbLocal;
import de.symeda.sormas.backend.sormastosormas.contact.SormasToSormasContactFacadeEjb.SormasToSormasContactFacadeEjbLocal;
import de.symeda.sormas.backend.sormastosormas.event.SormasToSormasEventFacadeEjb.SormasToSormasEventFacadeEjbLocal;
import de.symeda.sormas.backend.sormastosormas.shareinfo.SormasToSormasShareInfo;
import de.symeda.sormas.backend.sormastosormas.shareinfo.SormasToSormasShareInfoService;
import de.symeda.sormas.backend.sormastosormas.sharerequest.SormasToSormasShareRequestFacadeEJB.SormasToSormasShareRequestFacadeEJBLocal;
import de.symeda.sormas.backend.user.UserService;
import de.symeda.sormas.backend.util.DtoHelper;
import de.symeda.sormas.backend.util.ModelConstants;
import de.symeda.sormas.backend.util.QueryHelper;

@Stateless(name = "SormasToSormasFacade")
public class SormasToSormasFacadeEjb implements SormasToSormasFacade {

	private static final Logger LOGGER = LoggerFactory.getLogger(SormasToSormasFacadeEjb.class);

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
	@Inject
	private SormasToSormasConfig sormasToSormasConfig;
	@EJB
	private FeatureConfigurationFacadeEjbLocal featureConfigurationFacade;

	@Override
	public List<ServerAccessDataReferenceDto> getAvailableOrganizations() {
		return serverAccessDataService.getOrganizationList().stream().map(OrganizationServerAccessData::toReference).collect(Collectors.toList());
	}

	@Override
	public ServerAccessDataReferenceDto getOrganizationRef(String id) {
		return serverAccessDataService.getServerListItemById(id).map(OrganizationServerAccessData::toReference).orElse(null);
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

		return QueryHelper.getResultList(em, cq, first, max, this::toSormasToSormasShareInfoDto);
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

	/**
	 * Called from the REST api when the sender revokes share requests
	 *
	 * @param encryptedRequestUuids
	 *            the uuids of requests
	 * @throws SormasToSormasException
	 *             in case of failure
	 */
	@Override
	public void requestsRevoked(SormasToSormasEncryptedDataDto encryptedRequestUuids) throws SormasToSormasException {
		String[] requestUuids = encryptionService.decryptAndVerify(encryptedRequestUuids, String[].class);
		List<SormasToSormasShareRequestDto> shareRequests = shareRequestFacade.getShareRequestsByUuids(Arrays.asList(requestUuids));

		shareRequests.forEach(shareRequest -> {
			shareRequest.setChangeDate(new Date());
			shareRequest.setStatus(ShareRequestStatus.REVOKED);
			shareRequestFacade.saveShareRequest(shareRequest);
		});
	}

	/**
	 * Called from REST api when the receiver accepts a share request
	 *
	 * @param encryptedRequestUuid
	 *            the uuid of the request
	 * @throws SormasToSormasException
	 *             in case of failure
	 */
	@Override
	@Transactional(rollbackOn = {
		Exception.class })
	public void requestAccepted(SormasToSormasEncryptedDataDto encryptedRequestUuid) throws SormasToSormasException {
		String requestUuid = encryptionService.decryptAndVerify(encryptedRequestUuid, String.class);

		SormasToSormasShareInfo shareInfo = shareInfoService.getByRequestUuid(requestUuid);

		shareInfo.setRequestStatus(ShareRequestStatus.ACCEPTED);
		shareInfoService.ensurePersisted(shareInfo);

		try {
			shareInfoService.handleOwnershipChangeInExternalSurvTool(shareInfo);
		} catch (ExternalSurveillanceToolException e) {
			LOGGER.error("Failed to delete shared entities in external surveillance tool", e);

			throw SormasToSormasException.fromStringProperty(Strings.errorSormasToSormasAccept);
		}
	}

	@Override
	public boolean isFeatureEnabledForUser() {
		return userService.hasRight(UserRight.SORMAS_TO_SORMAS_SHARE) && isFeatureConfigured();
	}

	@Override
	public boolean isFeatureConfigured() {
		return !StringUtils.isEmpty(sormasToSormasConfig.getPath());
	}

	@Override
	public boolean isSharingCasesContactsAndSamplesEnabledForUser() {
		return isFeatureEnabledForUser() && featureConfigurationFacade.isFeatureEnabled(FeatureType.SORMAS_TO_SORMAS_SHARE_CASES_WITH_CONTACTS_AND_SAMPLES);
	}

	@Override
	public boolean isSharingEventsEnabledForUser() {
		return isFeatureEnabledForUser() && featureConfigurationFacade.isFeatureEnabled(FeatureType.SORMAS_TO_SORMAS_SHARE_EVENTS);
	}

	@Override
	public boolean isSharingLabMessagesEnabledForUser() {
		return isFeatureEnabledForUser() && featureConfigurationFacade.isFeatureEnabled(FeatureType.SORMAS_TO_SORMAS_SHARE_LAB_MESSAGES);
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
