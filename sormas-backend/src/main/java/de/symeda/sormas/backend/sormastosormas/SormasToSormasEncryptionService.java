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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;

import org.bouncycastle.cms.CMSException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import de.symeda.sormas.api.SormasToSormasConfig;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Strings;
import de.symeda.sormas.api.sormastosormas.SormasToSormasException;
import de.symeda.sormas.backend.crypt.CmsCreator;
import de.symeda.sormas.backend.crypt.CmsReader;

@Stateless
@LocalBean
public class SormasToSormasEncryptionService {

	private static final Logger LOGGER = LoggerFactory.getLogger(SormasToSormasEncryptionService.class);

	@Inject
	protected SormasToSormasConfig sormasToSormasConfig;
	@EJB
	protected ServerAccessDataService serverAccessDataService;

	private KeyStore getKeystore() throws CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException {
		return loadStore(sormasToSormasConfig.getKeystoreName(), sormasToSormasConfig.getKeystorePass());
	}

	private KeyStore getTruststore() throws CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException {
		return loadStore(sormasToSormasConfig.getTruststoreName(), sormasToSormasConfig.getTruststorePass());
	}

	private KeyStore loadStore(String name, String password) throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
		String filePath = sormasToSormasConfig.getPath();
		Path storePath = Paths.get(filePath, name);
		KeyStore store = KeyStore.getInstance("pkcs12");
		try (BufferedInputStream in = new BufferedInputStream(Files.newInputStream(storePath))) {
			store.load(in, password.toCharArray());
		}

		return store;
	}

	private enum Mode {
		ENCRYPTION,
		DECRYPTION
	}

	private byte[] cipher(Mode mode, byte[] data, String otherId)
		throws SormasToSormasException, CMSException, CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException,
		UnrecoverableKeyException {
		String ownId = getOrganizationId();
		KeyStore keystore = getKeystore();
		KeyStore truststore = getTruststore();
		X509Certificate ownCert = (X509Certificate) keystore.getCertificate(ownId);
		// todo private key should have own password
		PrivateKey ownKey = (PrivateKey) keystore.getKey(ownId, sormasToSormasConfig.getKeystorePass().toCharArray());
		X509Certificate otherCert = (X509Certificate) truststore.getCertificate(otherId);

		switch (mode) {
		case ENCRYPTION:
			return CmsCreator.signAndEncrypt(data, ownCert, ownKey, otherCert, true);
		case DECRYPTION:
			return CmsReader.decryptAndVerify(data, Lists.newArrayList(otherCert), ownCert, ownKey);
		default:
			throw new IllegalArgumentException("Unknown mode " + mode);
		}
	}

	public byte[] signAndEncrypt(byte[] data, String recipientId) throws SormasToSormasException {
		try {
			return cipher(Mode.ENCRYPTION, data, recipientId);
		} catch (Exception e) {
			LOGGER.error("Could not sign and encrypt data", e);
			throw new SormasToSormasException(I18nProperties.getString(Strings.errorSormasToSormasEncrypt));
		}
	}

	public byte[] decryptAndVerify(byte[] data, String senderId) throws SormasToSormasException {
		try {
			return cipher(Mode.DECRYPTION, data, senderId);
		} catch (Exception e) {
			LOGGER.error("Could not decrypt and verify data", e);
			throw new SormasToSormasException(I18nProperties.getString(Strings.errorSormasToSormasDecrypt));
		}
	}

	private String getOrganizationId() throws SormasToSormasException {
		return serverAccessDataService.getServerAccessData()
			.orElseThrow(() -> new SormasToSormasException(I18nProperties.getString(Strings.errorSormasToSormasCertNotGenerated)))
			.getId();
	}
}
