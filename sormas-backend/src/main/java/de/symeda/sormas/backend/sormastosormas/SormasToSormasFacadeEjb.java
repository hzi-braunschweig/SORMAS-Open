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

import org.apache.commons.lang3.StringUtils;

import de.symeda.sormas.api.SormasToSormasConfig;
import de.symeda.sormas.api.sormastosormas.ServerAccessDataReferenceDto;
import de.symeda.sormas.api.sormastosormas.SormasToSormasApiConstants;
import de.symeda.sormas.api.sormastosormas.SormasToSormasEncryptedDataDto;
import de.symeda.sormas.api.sormastosormas.SormasToSormasEntityInterface;
import de.symeda.sormas.api.sormastosormas.SormasToSormasException;
import de.symeda.sormas.api.sormastosormas.SormasToSormasFacade;
import de.symeda.sormas.api.sormastosormas.SormasToSormasValidationException;
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

@Stateless(name = "SormasToSormasFacade")
public class SormasToSormasFacadeEjb implements SormasToSormasFacade {

	private static final String REVOKE_REQUEST_ENDPOINT = RESOURCE_PATH + SormasToSormasApiConstants.REVOKE_REQUESTS_ENDPOINT;

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

	@Override
	public String getOrganizationId() {
		return serverAccessDataService.getServerAccessData().getId();
	}

	@Override
	public List<ServerAccessDataReferenceDto> getAvailableOrganizations() {
		return serverAccessDataService.getOrganizationList().stream().map(OrganizationServerAccessData::toReference).collect(Collectors.toList());
	}

	@Override
	public ServerAccessDataReferenceDto getOrganizationRef(String id) {
		return serverAccessDataService.getServerListItemById(id).map(OrganizationServerAccessData::toReference).orElse(null);
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
	public void revokeRequests(SormasToSormasEncryptedDataDto encryptedRequestUuids) throws SormasToSormasException {
		String[] requestUuids = encryptionService.decryptAndVerify(encryptedRequestUuids, String[].class);
		List<SormasToSormasShareRequestDto> shareRequests = shareRequestFacade.getShareRequestsByUuids(Arrays.asList(requestUuids));

		shareRequests.forEach(shareRequest -> {
			shareRequest.setChangeDate(new Date());
			shareRequest.setStatus(ShareRequestStatus.REVOKED);
			shareRequestFacade.saveShareRequest(shareRequest);
		});
	}

	@Override
	public boolean isFeatureEnabledForUser() {
		return userService.hasRight(UserRight.SORMAS_TO_SORMAS_SHARE) && isFeatureConfigured();
	}

	@Override
	public boolean isFeatureConfigured() {
		return !StringUtils.isEmpty(sormasToSormasConfig.getPath());
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
