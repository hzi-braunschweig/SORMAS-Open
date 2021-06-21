package de.symeda.sormas.rest.security.oidc;

import java.io.IOException;
import java.net.URL;
import java.security.PublicKey;

import javax.annotation.Priority;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import org.glassfish.jersey.server.ContainerRequest;
import org.keycloak.TokenVerifier;
import org.keycloak.common.VerificationException;
import org.keycloak.jose.jwk.JSONWebKeySet;
import org.keycloak.jose.jwk.JWK;
import org.keycloak.jose.jwk.JWKParser;
import org.keycloak.representations.AccessToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.SormasToSormasConfig;
import de.symeda.sormas.api.sormastosormas.SormasToSormasEncryptedDataDto;

@Provider
@Priority(Priorities.AUTHENTICATION)
@ClientCredentials
public class S2SAuthFilter implements ContainerRequestFilter {

	private final Logger LOGGER = LoggerFactory.getLogger(getClass());
	private static final String BEARER = "Bearer";

	@Override
	public void filter(ContainerRequestContext requestContext) {
		String authorizationHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
		boolean validHeader = authorizationHeader != null && authorizationHeader.startsWith(String.format("%s ", BEARER));

		if (!validHeader) {
			requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED.getStatusCode(), "Invalid header").build());
		}

		String token = authorizationHeader.substring(BEARER.length()).trim();
		String orgIdSender = "";

		if (!requestContext.getMethod().equals(HttpMethod.GET)) {
			ContainerRequest cr = (ContainerRequest) requestContext;
			cr.bufferEntity();
			SormasToSormasEncryptedDataDto dto = cr.readEntity(SormasToSormasEncryptedDataDto.class);
			orgIdSender = dto.getOrganizationId();
		} else {
			orgIdSender = requestContext.getUriInfo()
				.getQueryParameters()
				.getFirst(SormasToSormasConfig.ORG_ID_REQUEST_PARAM);
		}
		try {
			if (!isValidToken(token, orgIdSender)) {
				requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED.getStatusCode(), "Invalid token").build());
			}
		} catch (Exception e) {
			requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED.getStatusCode(), e.getMessage()).build());
		}

	}

	private boolean isValidToken(String token, String orgIdSender) throws VerificationException {
		if (token == null) {
			throw new VerificationException("Empty token provided.");
		}

		PublicKey publicKey = fetchPublicKey();

		TokenVerifier<AccessToken> tokenVerifier = TokenVerifier.create(token, AccessToken.class);

		SormasToSormasConfig sormasToSormasConfig = FacadeProvider.getConfigFacade().getSormasToSormasConfig();

		// We assert the following:
		// 1. The token was issued by the realm we are trusting
		// 2. The token is NOT expired
		// 3. A subject is present in the token
		// 4. The token is a bearer token
		// 5. We check that the token was issued to the client we are talking to right now
		//
		// NOTE: We still need to check that we are the intended receiver. Normally you do this with an
		//       audience check, but this is not possible as we have N-many servers. Instead, we check that
		//       that the correct scope with our own prefix for our API was requested

		AccessToken verifiedToken = tokenVerifier.publicKey(publicKey)
			.withChecks(
				new TokenVerifier.RealmUrlCheck(sormasToSormasConfig.getOidcRealmUrl()),
				TokenVerifier.IS_ACTIVE,
				TokenVerifier.SUBJECT_EXISTS_CHECK,
				new TokenVerifier.TokenTypeCheck("Bearer"),
				new TokenVerifier.IssuedForCheck(orgIdSender))
			.verify()
			.getToken();

		// Check that the client requested to correct scope for our API.
		String scope = sormasToSormasConfig.getClientScope();

		if (!verifiedToken.getScope().equals(scope)) {
			throw new VerificationException(String.format("Scope mismatch. Expected: %s Was: %s", scope, verifiedToken.getScope()));
		}
		return true;

	}

	private PublicKey fetchPublicKey() throws VerificationException {
		SormasToSormasConfig sormasToSormasConfig = FacadeProvider.getConfigFacade().getSormasToSormasConfig();

		ObjectMapper mapper = new ObjectMapper();
		JSONWebKeySet jwks;
		try {
			String certEndpoint = sormasToSormasConfig.getOidcRealmCertEndoint();
			jwks = mapper.readValue(new URL(certEndpoint).openStream(), JSONWebKeySet.class);
		} catch (IOException e) {
			LOGGER.error(String.format("Could not fetch public key for realm: %s", e));
			throw new VerificationException("Could not fetch public key for realm");
		}
		JWK jwk = jwks.getKeys()[0];
		return JWKParser.create(jwk).toPublicKey();
	}

}
