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

package de.symeda.sormas.rest;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.sormastosormas.SormasToSormasApiConstants;
import de.symeda.sormas.api.sormastosormas.SormasToSormasEncryptedDataDto;
import de.symeda.sormas.api.sormastosormas.SormasToSormasEncryptionFacade;
import de.symeda.sormas.api.sormastosormas.SormasToSormasErrorResponse;
import de.symeda.sormas.api.sormastosormas.SormasToSormasException;
import de.symeda.sormas.api.sormastosormas.SormasToSormasValidationException;
import de.symeda.sormas.api.user.UserRole;
import de.symeda.sormas.rest.security.oidc.ClientCredentials;

@Path(SormasToSormasApiConstants.RESOURCE_PATH)
@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
@Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
@ClientCredentials
@RolesAllowed(UserRole._SORMAS_TO_SORMAS_CLIENT)
public class SormasToSormasResource {

	@POST
	@Path(SormasToSormasApiConstants.CASE_REQUEST_ENDPOINT)
	public Response saveSharedCaseRequest(SormasToSormasEncryptedDataDto sharedCases) {
		return handleVoidRequest(() -> FacadeProvider.getSormasToSormasCaseFacade().saveShareRequest(sharedCases));
	}

	@POST
	@Path(SormasToSormasApiConstants.CASE_REQUEST_REJECT_ENDPOINT)
	public Response rejectSharedCaseRequest(SormasToSormasEncryptedDataDto encryptedRequestUuid) {
		return handleVoidRequest(() -> FacadeProvider.getSormasToSormasCaseFacade().rejectShareRequest(encryptedRequestUuid));
	}

	@POST
	@Path(SormasToSormasApiConstants.CASE_REQUEST_ACCEPT_ENDPOINT)
	public Response acceptSharedCaseRequest(SormasToSormasEncryptedDataDto encryptedRequestUuid) {
		return handleRequestWithReturnData(() -> FacadeProvider.getSormasToSormasCaseFacade().getDataForShareRequest(encryptedRequestUuid));
	}

	@POST
	@Path(SormasToSormasApiConstants.CASE_ENDPOINT)
	public Response saveSharedCase(SormasToSormasEncryptedDataDto sharedCases) {
		return handleVoidRequest(() -> FacadeProvider.getSormasToSormasCaseFacade().saveSharedEntities(sharedCases, null));
	}

	@PUT
	@Path(SormasToSormasApiConstants.CASE_ENDPOINT)
	public Response saveReturnedCase(SormasToSormasEncryptedDataDto sharedCases) {
		return handleVoidRequest(() -> FacadeProvider.getSormasToSormasCaseFacade().saveReturnedEntity(sharedCases));
	}

	@POST
	@Path(SormasToSormasApiConstants.CASE_SYNC_ENDPOINT)
	public Response syncSharedCases(SormasToSormasEncryptedDataDto sharedCases) {
		return handleVoidRequest(() -> FacadeProvider.getSormasToSormasCaseFacade().saveSyncedEntity(sharedCases));
	}

	@POST
	@Path(SormasToSormasApiConstants.CONTACT_REQUEST_ENDPOINT)
	public Response saveSharedContactRequest(SormasToSormasEncryptedDataDto sharedContacts) {
		return handleVoidRequest(() -> FacadeProvider.getSormasToSormasContactFacade().saveShareRequest(sharedContacts));
	}

	@POST
	@Path(SormasToSormasApiConstants.CONTACT_REQUEST_REJECT_ENDPOINT)
	public Response rejectSharedContactRequest(SormasToSormasEncryptedDataDto encryptedRequestUuid) {
		return handleVoidRequest(() -> FacadeProvider.getSormasToSormasContactFacade().rejectShareRequest(encryptedRequestUuid));
	}

	@POST
	@Path(SormasToSormasApiConstants.CONTACT_REQUEST_ACCEPT_ENDPOINT)
	public Response acceptSharedContactRequest(SormasToSormasEncryptedDataDto encryptedRequestUuid) {
		return handleRequestWithReturnData(() -> FacadeProvider.getSormasToSormasContactFacade().getDataForShareRequest(encryptedRequestUuid));
	}

	@POST
	@Path(SormasToSormasApiConstants.CONTACT_ENDPOINT)
	public Response saveSharedContact(SormasToSormasEncryptedDataDto sharedContacts) {
		return handleVoidRequest(() -> FacadeProvider.getSormasToSormasContactFacade().saveSharedEntities(sharedContacts, null));
	}

	@PUT
	@Path(SormasToSormasApiConstants.CONTACT_ENDPOINT)
	public Response saveReturnedContact(SormasToSormasEncryptedDataDto sharedContacts) {
		return handleVoidRequest(() -> FacadeProvider.getSormasToSormasContactFacade().saveReturnedEntity(sharedContacts));
	}

	@POST
	@Path(SormasToSormasApiConstants.CONTACT_SYNC_ENDPOINT)
	public Response syncSharedContacts(SormasToSormasEncryptedDataDto sharedContacts) {
		return handleVoidRequest(() -> FacadeProvider.getSormasToSormasContactFacade().saveSyncedEntity(sharedContacts));
	}

	@POST
	@Path(SormasToSormasApiConstants.EVENT_REQUEST_ENDPOINT)
	public Response saveSharedEventRequest(SormasToSormasEncryptedDataDto sharedEvents) {
		return handleVoidRequest(() -> FacadeProvider.getSormasToSormasEventFacade().saveShareRequest(sharedEvents));
	}

	@POST
	@Path(SormasToSormasApiConstants.EVENT_REQUEST_REJECT_ENDPOINT)
	public Response rejectSharedEventRequest(SormasToSormasEncryptedDataDto encryptedRequestUuid) {
		return handleVoidRequest(() -> FacadeProvider.getSormasToSormasEventFacade().rejectShareRequest(encryptedRequestUuid));
	}

	@POST
	@Path(SormasToSormasApiConstants.EVENT_REQUEST_ACCEPT_ENDPOINT)
	public Response acceptSharedEventRequest(SormasToSormasEncryptedDataDto encryptedRequestUuid) {
		return handleRequestWithReturnData(() -> FacadeProvider.getSormasToSormasEventFacade().getDataForShareRequest(encryptedRequestUuid));
	}

	@POST
	@Path(SormasToSormasApiConstants.EVENT_ENDPOINT)
	public Response saveSharedEvents(SormasToSormasEncryptedDataDto sharedEvents) {
		return handleVoidRequest(() -> FacadeProvider.getSormasToSormasEventFacade().saveSharedEntities(sharedEvents, null));
	}

	@PUT
	@Path(SormasToSormasApiConstants.EVENT_ENDPOINT)
	public Response saveReturnedEvent(SormasToSormasEncryptedDataDto sharedEvent) {
		return handleVoidRequest(() -> FacadeProvider.getSormasToSormasEventFacade().saveReturnedEntity(sharedEvent));
	}

	@POST
	@Path(SormasToSormasApiConstants.EVENT_SYNC_ENDPOINT)
	public Response syncSharedEvents(SormasToSormasEncryptedDataDto sharedEvent) {
		return handleVoidRequest(() -> FacadeProvider.getSormasToSormasEventFacade().saveSyncedEntity(sharedEvent));
	}

	@POST
	@Path(SormasToSormasApiConstants.LAB_MESSAGE_ENDPOINT)
	public Response syncSharedLAbMessages(SormasToSormasEncryptedDataDto labMessages) {
		return handleVoidRequest(() -> FacadeProvider.getSormasToSormasLabMessageFacade().saveLabMessages(labMessages));
	}

	@POST
	@Path(SormasToSormasApiConstants.REVOKE_REQUESTS_ENDPOINT)
	public Response revokeShareRequests(SormasToSormasEncryptedDataDto requestUuids) {
		return handleVoidRequest(() -> FacadeProvider.getSormasToSormasFacade().revokeRequests(requestUuids));
	}

	@GET
	@Path(SormasToSormasApiConstants.CERT_ENDPOINT)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response getCertificate() {
		SormasToSormasEncryptionFacade encryptionFacade = FacadeProvider.getSormasToSormasEncryptionFacade();
		try {
			X509Certificate ownCert = encryptionFacade.getOwnCertificate();
			return Response.ok(ownCert.getEncoded(), MediaType.APPLICATION_OCTET_STREAM).build();
		} catch (CertificateException | KeyStoreException | IOException | NoSuchAlgorithmException | SormasToSormasException e) {
			return Response.serverError().build();
		}
	}

	private Response handleVoidRequest(VoidFacadeCall facadeCall) {
		try {
			facadeCall.call();
		} catch (SormasToSormasValidationException e) {
			return Response.status(Response.Status.BAD_REQUEST).entity(new SormasToSormasErrorResponse(e.getErrors())).build();
		} catch (SormasToSormasException e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		}

		return Response.noContent().build();
	}

	private Response handleRequestWithReturnData(FacadeCall facadeCall) {
		Object response;
		try {
			response = facadeCall.call();
		} catch (SormasToSormasValidationException e) {
			return Response.status(Response.Status.BAD_REQUEST).entity(new SormasToSormasErrorResponse(e.getErrors())).build();
		} catch (SormasToSormasException e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		}

		return Response.ok().entity(response).build();
	}

	private interface VoidFacadeCall {

		void call() throws SormasToSormasValidationException, SormasToSormasException;
	}

	private interface FacadeCall {

		Object call() throws SormasToSormasValidationException, SormasToSormasException;
	}
}
