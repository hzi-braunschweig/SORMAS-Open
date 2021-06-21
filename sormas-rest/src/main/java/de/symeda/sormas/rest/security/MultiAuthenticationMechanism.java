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
package de.symeda.sormas.rest.security;

import java.util.Collections;
import java.util.HashSet;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.security.enterprise.AuthenticationException;
import javax.security.enterprise.AuthenticationStatus;
import javax.security.enterprise.authentication.mechanism.http.BasicAuthenticationMechanismDefinition;
import javax.security.enterprise.authentication.mechanism.http.HttpAuthenticationMechanism;
import javax.security.enterprise.authentication.mechanism.http.HttpMessageContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.glassfish.soteria.cdi.BasicAuthenticationMechanismDefinitionAnnotationLiteral;
import org.glassfish.soteria.mechanisms.BasicAuthenticationMechanism;

import de.symeda.sormas.api.AuthProvider;
import de.symeda.sormas.api.ConfigFacade;
import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.sormastosormas.SormasToSormasApiConstants;
import de.symeda.sormas.api.user.UserRole;
import de.symeda.sormas.api.utils.DefaultUserHelper;
import de.symeda.sormas.rest.security.config.KeycloakConfigResolver;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

/**
 * Mechanism which allows configuration of multiple providers trough a system property.
 * <p/>
 * Supported at the moment: Sormas and Keycloak.
 * <p/>
 * Sormas provider uses the {@link BasicAuthenticationMechanism}. The {@link SormasIdentityStore} validated the credentials and obtains the
 * user's roles.
 * <p/>
 * Keycloak provider uses {@link KeycloakHttpAuthenticationMechanism} and accepts Bearer and if enabled also Basic authentication.
 * Configuration provided by {@link KeycloakConfigResolver}.<br/>
 * The token validation is done trough {@link KeycloakFilter} and then the user is authenticated and it's roles are obtained by
 * {@link KeycloakIdentityStore}.<br/>
 * <b>Note:</b> As a precondition the Keycloak user needs a valid role which the {@link KeycloakFilter} will use to pre-validate the
 * request.
 *
 * @author Alex Vidrean
 * @see ConfigFacade#getAuthenticationProvider()
 * @see SormasIdentityStore
 * @see KeycloakIdentityStore
 * @see KeycloakConfigResolver
 * @see KeycloakHttpAuthenticationMechanism
 */
@OpenAPIDefinition(security = {
	@SecurityRequirement(name = "basicAuth"),
	@SecurityRequirement(name = "bearerAuth") })
@SecurityScheme(name = "basicAuth", type = SecuritySchemeType.HTTP, scheme = "basic")
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
@ApplicationScoped
public class MultiAuthenticationMechanism implements HttpAuthenticationMechanism {

	private final HttpAuthenticationMechanism authenticationMechanism;

	@Inject
	public MultiAuthenticationMechanism(KeycloakHttpAuthenticationMechanism keycloakHttpAuthenticationMechanism) {
		String authenticationProvider = FacadeProvider.getConfigFacade().getAuthenticationProvider();
		if (authenticationProvider.equals(AuthProvider.KEYCLOAK)) {
			authenticationMechanism = keycloakHttpAuthenticationMechanism;
		} else {
			BasicAuthenticationMechanismDefinition definition = new BasicAuthenticationMechanismDefinitionAnnotationLiteral("sormas-rest-realm");
			authenticationMechanism = new BasicAuthenticationMechanism(definition);
		}
	}

	@Override
	public AuthenticationStatus validateRequest(HttpServletRequest request, HttpServletResponse response, HttpMessageContext context)
		throws AuthenticationException {
		if (request.getPathInfo().startsWith(SormasToSormasApiConstants.RESOURCE_PATH)) {
			return validateRequestS2S(context);
		}
		return authenticationMechanism.validateRequest(request, response, context);
	}

	@Override
	public AuthenticationStatus secureResponse(HttpServletRequest request, HttpServletResponse response, HttpMessageContext httpMessageContext)
		throws AuthenticationException {
		return authenticationMechanism.secureResponse(request, response, httpMessageContext);
	}

	@Override
	public void cleanSubject(HttpServletRequest request, HttpServletResponse response, HttpMessageContext httpMessageContext) {
		authenticationMechanism.cleanSubject(request, response, httpMessageContext);
	}

	private AuthenticationStatus validateRequestS2S(HttpMessageContext context) {

		return context.notifyContainerAboutLogin(
			() -> DefaultUserHelper.SORMAS_TO_SORMAS_USER_NAME,
			new HashSet<>(Collections.singletonList(UserRole.SORMAS_TO_SORMAS_CLIENT.name())));
	}
}
