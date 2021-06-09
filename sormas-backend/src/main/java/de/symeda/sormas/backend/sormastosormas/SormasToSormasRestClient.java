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

package de.symeda.sormas.backend.sormastosormas;

import static de.symeda.sormas.api.sormastosormas.SormasToSormasApiConstants.SORMAS_REST_PATH;

import java.io.IOException;
import java.net.ConnectException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.ResponseProcessingException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Strings;
import de.symeda.sormas.api.sormastosormas.SormasToSormasEncryptedDataDto;
import de.symeda.sormas.api.sormastosormas.SormasToSormasErrorResponse;
import de.symeda.sormas.api.sormastosormas.SormasToSormasException;
import de.symeda.sormas.api.sormastosormas.ValidationErrors;
import de.symeda.sormas.backend.common.StartupShutdownService;
import de.symeda.sormas.backend.util.ClientHelper;

public class SormasToSormasRestClient {

	public static final String SORMAS_REST_URL_TEMPLATE = "http://%s" + SORMAS_REST_PATH + "%s";
	private static final Logger LOGGER = LoggerFactory.getLogger(SormasToSormasRestClient.class);
	private final ServerAccessDataService serverAccessDataService;

	private final SormasToSormasEncryptionService encryptionService;

	private final ObjectMapper mapper;

	public SormasToSormasRestClient(ServerAccessDataService serverAccessDataService, SormasToSormasEncryptionService encryptionService) {
		this.serverAccessDataService = serverAccessDataService;
		this.encryptionService = encryptionService;

		mapper = new ObjectMapper();
		mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
		mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
	}

	public <T> T post(String receiverId, String endpoint, Object body, Class<T> responseType) throws SormasToSormasException {
		return sendRequest(receiverId, endpoint, body, responseType, HttpMethod.POST);

	}

	public <T> T put(String receiverId, String endpoint, Object body, Class<T> responseType) throws SormasToSormasException {
		return sendRequest(receiverId, endpoint, body, responseType, HttpMethod.PUT);
	}

	private String buildAuthToken(OrganizationServerAccessData targetServerAccessData) {
		String userCredentials = StartupShutdownService.SORMAS_TO_SORMAS_USER_NAME + ":" + targetServerAccessData.getRestUserPassword();
		return "Basic " + new String(Base64.getEncoder().encode(userCredentials.getBytes(StandardCharsets.UTF_8)));
	}

	private Invocation.Builder buildRestClient(String receiverId, String endpoint) throws SormasToSormasException {
		OrganizationServerAccessData targetServerAccessData = serverAccessDataService.getServerListItemById(receiverId)
			.orElseThrow(() -> new SormasToSormasException(I18nProperties.getString(Strings.errorSormasToSormasServerAccess)));

		String host = targetServerAccessData.getHostName();
		String authToken = buildAuthToken(targetServerAccessData);

		return ClientHelper.newBuilderWithProxy()
			.build()
			.target(String.format(SORMAS_REST_URL_TEMPLATE, host, endpoint))
			.request()
			.header("Authorization", authToken);
	}

	private <T> T sendRequest(String receiverId, String endpoint, Object body, Class<T> responseType, String method) throws SormasToSormasException {
		try {
			SormasToSormasEncryptedDataDto encryptedBody = encryptionService.signAndEncrypt(body, receiverId);
			Entity<String> entity = Entity.entity(mapper.writeValueAsString(encryptedBody), MediaType.APPLICATION_JSON_TYPE);
			Invocation.Builder invocation = buildRestClient(receiverId, endpoint);

			Response response;
			switch (method) {
			case HttpMethod.POST:
				response = invocation.post(entity);
				break;
			case HttpMethod.PUT:
				response = invocation.put(entity);
				break;
			default:
				throw new SormasToSormasException("Invalid HTTP verb used");
			}
			return handleResponse(response, responseType);
		} catch (JsonProcessingException e) {
			LOGGER.error("Unable to send data sormas", e);
			throw new SormasToSormasException(I18nProperties.getString(Strings.errorSormasToSormasSend));
		} catch (ResponseProcessingException e) {
			LOGGER.error("Unable to process sormas response", e);
			throw new SormasToSormasException(I18nProperties.getString(Strings.errorSormasToSormasResult));
		} catch (ProcessingException e) {
			LOGGER.error("Unable to send data to sormas", e);

			String processingErrorMessage = I18nProperties.getString(Strings.errorSormasToSormasSend);
			if (ConnectException.class.isAssignableFrom(e.getCause().getClass())) {
				processingErrorMessage = I18nProperties.getString(Strings.errorSormasToSormasConnection);
			}
			throw new SormasToSormasException(processingErrorMessage);
		}
	}

	private <T> T handleResponse(Response response, Class<T> responseType) throws SormasToSormasException {
		int statusCode = response.getStatus();
		if (statusCode != HttpStatus.SC_NO_CONTENT && statusCode != HttpStatus.SC_OK) {
			String errorMessage = response.readEntity(String.class);
			Map<String, ValidationErrors> errors = null;

			try {
				SormasToSormasErrorResponse errorResponse = mapper.readValue(errorMessage, SormasToSormasErrorResponse.class);
				errorMessage = I18nProperties.getString(Strings.errorSormasToSormasShare);
				errors = errorResponse.getErrors();
			} catch (IOException e) {
				// do nothing, keep the unparsed response as error message
			}

			if (statusCode != HttpStatus.SC_BAD_REQUEST) {
				// don't log validation errors, will be displayed on the UI
				LOGGER.error("Share request failed: {}; {}", statusCode, errorMessage);
			}
			throw new SormasToSormasException(errorMessage, errors);
		}
		return responseType != null ? response.readEntity(responseType) : null;
	}
}
