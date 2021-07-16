/*******************************************************************************
 * SORMAS® - Surveillance Outbreak Response Management & Analysis System
 * Copyright © 2016-2018 Helmholtz-Zentrum für Infektionsforschung GmbH (HZI)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 *******************************************************************************/
package de.symeda.sormas.rest;

import java.util.Date;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.JsonNode;

import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.PushResult;
import de.symeda.sormas.api.caze.CaseCriteria;
import de.symeda.sormas.api.caze.CaseDataDto;
import de.symeda.sormas.api.caze.CaseIndexDetailedDto;
import de.symeda.sormas.api.caze.CaseIndexDto;
import de.symeda.sormas.api.caze.CasePersonDto;
import de.symeda.sormas.api.caze.CriteriaWithSorting;
import de.symeda.sormas.api.common.Page;
import de.symeda.sormas.api.externaldata.ExternalDataDto;
import de.symeda.sormas.api.externaldata.ExternalDataUpdateException;
import de.symeda.sormas.api.utils.Experimental;
import io.swagger.v3.oas.annotations.parameters.RequestBody;

@Path("/cases")
@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
@Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
@RolesAllowed({
	"USER",
	"REST_USER" })
public class CaseResource extends EntityDtoResource {

	@GET
	@Path("/all/{since}")
	public List<CaseDataDto> getAllCases(@PathParam("since") long since) {
		return FacadeProvider.getCaseFacade().getAllActiveCasesAfter(new Date(since));
	}

	@POST
	@Path("/query")
	public List<CaseDataDto> getByUuids(List<String> uuids) {
		return FacadeProvider.getCaseFacade().getByUuids(uuids);
	}

	@POST
	@Path("/query/persons")
	public List<CaseDataDto> getByPersonUuids(List<String> uuids) {
		return FacadeProvider.getCaseFacade().getByPersonUuids(uuids);
	}

	@POST
	@Path("/push")
	public List<PushResult> postCases(@Valid List<CaseDataDto> dtos) {
		return savePushedDto(dtos, FacadeProvider.getCaseFacade()::saveCase);
	}

	@GET
	@Path("/uuids")
	public List<String> getAllUuids() {
		return FacadeProvider.getCaseFacade().getAllActiveUuids();
	}

	@GET
	@Path("/archived/{since}")
	public List<String> getArchivedUuidsSince(@PathParam("since") long since) {
		return FacadeProvider.getCaseFacade().getArchivedUuidsSince(new Date(since));
	}

	@GET
	@Path("/deleted/{since}")
	public List<String> getDeletedUuidsSince(@PathParam("since") long since) {
		return FacadeProvider.getCaseFacade().getDeletedUuidsSince(new Date(since));
	}

	@POST
	@Path("/getduplicates")
	public List<CasePersonDto> getDuplicates(@Valid CasePersonDto casePerson) {
		return FacadeProvider.getCaseFacade().getDuplicates(casePerson);
	}

	@POST
	@Path("/getduplicates/{reportDateThreshold}")
	public List<CasePersonDto> getDuplicates(@Valid CasePersonDto casePerson, @PathParam("reportDateThreshold") int reportDateThreshold) {
		return FacadeProvider.getCaseFacade().getDuplicates(casePerson, reportDateThreshold);
	}

	@POST
	@Path("/indexList")
	public Page<CaseIndexDto> getIndexList(
		@RequestBody CriteriaWithSorting<CaseCriteria> criteriaWithSorting,
		@QueryParam("offset") int offset,
		@QueryParam("size") int size) {
		return FacadeProvider.getCaseFacade().getIndexPage(criteriaWithSorting.getCriteria(), offset, size, criteriaWithSorting.getSortProperties());
	}

	@POST
	@Path("/detailedIndexList")
	public Page<CaseIndexDetailedDto> getIndexDetailedList(
		@RequestBody CriteriaWithSorting<CaseCriteria> criteriaWithSorting,
		@QueryParam("offset") int offset,
		@QueryParam("size") int size) {
		return FacadeProvider.getCaseFacade()
			.getIndexDetailedPage(criteriaWithSorting.getCriteria(), offset, size, criteriaWithSorting.getSortProperties());
	}

	@GET
	@Path("/{uuid}")
	public CaseDataDto getByUuid(@PathParam("uuid") String uuid) {
		return FacadeProvider.getCaseFacade().getByUuid(uuid);
	}

	@POST
	@Path("/externalData")
	public Response updateExternalData(List<ExternalDataDto> externalData) {
		try {
			FacadeProvider.getCaseFacade().updateExternalData(externalData);
			return Response.status(Response.Status.OK).build();
		} catch (ExternalDataUpdateException e) {
			return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}

	/**
	 * This endpoint is used to partially update the CaseData.
	 * For allowing only a subset of the fields of the caseDataDto to be updated
	 * THIS METHOD IS EXPERIMENTAL!!!
	 * 
	 * @param uuid
	 * @param caseDataDtoJson
	 *            - a subset of caseDataDto fields, same structure as caseDataDto
	 * @return - the updated caseDataDto
	 * @throws Exception
	 */
	@POST
	@Path("/postUpdate/{uuid}")
	@Experimental
	public CaseDataDto postUpdate(@PathParam("uuid") String uuid, JsonNode caseDataDtoJson) throws Exception {
		return FacadeProvider.getCaseFacade().postUpdate(uuid, caseDataDtoJson);
	}

}
