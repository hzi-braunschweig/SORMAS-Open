package de.symeda.sormas.backend.sormastosormas.auth;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.client.auth.oauth2.ClientCredentialsTokenRequest;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;

public class Oidc {

	private static final Logger LOGGER = LoggerFactory.getLogger(Oidc.class);

	public static String requestAccessToken(String tokenEndpoint, String clientId, String clientSecret, List<String> scopes) throws Exception {
		ClientParametersAuthentication clientAuth = new ClientParametersAuthentication(clientId, clientSecret);
		try {
			LOGGER.info(String.format("Requesting access token for client %s at %s with scope: %s", clientId, tokenEndpoint, scopes));
			TokenResponse response = new ClientCredentialsTokenRequest(new NetHttpTransport(), new GsonFactory(), new GenericUrl(tokenEndpoint))
				.setClientAuthentication(clientAuth)
				.setScopes(scopes)
				.execute();

			String token = response.getAccessToken();
			if (token == null || token.isEmpty()) {
				LOGGER.error("Could not retrieve access token.");
				throw new Exception("Could not retrieve access token.");
			}
			return token;
		} catch (IOException e) {
			LOGGER.error("Unable to connect to Keycloak.", e);
			throw e;
		}
	}

}
