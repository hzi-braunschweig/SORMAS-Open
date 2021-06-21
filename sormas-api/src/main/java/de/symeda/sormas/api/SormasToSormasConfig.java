package de.symeda.sormas.api;

import java.io.Serializable;
import java.util.Objects;

import javax.enterprise.inject.Alternative;

@Alternative
public class SormasToSormasConfig implements Serializable, Cloneable {

	private static final long serialVersionUID = -7981351672462016280L;

	// We normally just send encrypted data DTOs between instances which already carry the org id of the sender, however,
	// this does not work for GET request. Therefore we include a query parameter in this case. This variable cannot
	// resort in the REST client, as it needs to be shared between REST client and sormas-rest.
	public static final String ORG_ID_REQUEST_PARAM = "orgIdSender";

	private String path;
	private String serverAccessDataFileName;
	private String keystoreName;
	private String keystorePass;
	private String truststoreName;
	private String truststorePass;
	private boolean retainCaseExternalToken;
	private String oidcServer;
	private String oidcRealm;
	private String oidcClientId;
	private String oidcClientSecret;

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getServerAccessDataFileName() {
		return serverAccessDataFileName;
	}

	public void setServerAccessDataFileName(String serverAccessDataFileName) {
		this.serverAccessDataFileName = serverAccessDataFileName;
	}

	public String getKeystoreName() {
		return keystoreName;
	}

	public void setKeystoreName(String keystoreName) {
		this.keystoreName = keystoreName;
	}

	public String getKeystorePass() {
		return keystorePass;
	}

	public void setKeystorePass(String keystorePass) {
		this.keystorePass = keystorePass;
	}

	public String getTruststoreName() {
		return truststoreName;
	}

	public void setTruststoreName(String truststoreName) {
		this.truststoreName = truststoreName;
	}

	public String getTruststorePass() {
		return truststorePass;
	}

	public void setTruststorePass(String truststorePass) {
		this.truststorePass = truststorePass;
	}

	public boolean getRetainCaseExternalToken() {
		return retainCaseExternalToken;
	}

	public void setRetainCaseExternalToken(boolean retainCaseExternalToken) {
		this.retainCaseExternalToken = retainCaseExternalToken;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		SormasToSormasConfig that = (SormasToSormasConfig) o;
		return Objects.equals(path, that.path)
			&& Objects.equals(serverAccessDataFileName, that.serverAccessDataFileName)
			&& Objects.equals(keystorePass, that.keystorePass)
			&& Objects.equals(truststoreName, that.truststoreName)
			&& Objects.equals(truststorePass, that.truststorePass);
	}

	@Override
	public int hashCode() {
		return Objects.hash(path, serverAccessDataFileName, keystorePass, truststoreName, truststorePass);
	}

	@Override
	public SormasToSormasConfig clone() {
		try {
			return (SormasToSormasConfig) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException("Clone failed", e);
		}
	}

	public String getOidcServer() {
		return oidcServer;
	}

	public void setOidcServer(String oidcServer) {
		this.oidcServer = oidcServer;
	}

	public String getOidcRealm() {
		return oidcRealm;
	}

	public void setOidcRealm(String oidcRealm) {
		this.oidcRealm = oidcRealm;
	}

	/**
	 * Computed property to get access to our realm URL.
	 *
	 * @return The complete URL to our S2S realm
	 */
	public String getOidcRealmUrl() {
		return getOidcServer() + "/auth/realms/" + getOidcRealm();
	}

	/**
	 * Computed property to get access to our realm's token URL.
	 *
	 * @return The complete URL to our S2S realm's token URL.
	 */
	public String getOidcRealmTokenEndoint() {
		return getOidcRealmUrl() + "/protocol/openid-connect/token";
	}

	public String getOidcClientId() {
		return oidcClientId;
	}

	public void setOidcClientId(String oidcClientId) {
		this.oidcClientId = oidcClientId;
	}

	public String getOidcClientSecret() {
		return oidcClientSecret;
	}

	public void setOidcClientSecret(String oidcClientSecret) {
		this.oidcClientSecret = oidcClientSecret;
	}

	/**
	 * Computed property to get access to our realm's certificate URL.
	 *
	 * @return The complete URL to our S2S realm's certificate URL.
	 */
	public String getOidcRealmCertEndoint() {
		return getOidcRealmUrl() + "/protocol/openid-connect/certs";
	}

	/**
	 * Computed property to get access to the name of our own client scope.
	 *
	 * @return The complete URL to our S2S realm's certificate URL.
	 */
	public String getClientScope() {
		return "s2s-" + getOidcClientId();
	}
}
