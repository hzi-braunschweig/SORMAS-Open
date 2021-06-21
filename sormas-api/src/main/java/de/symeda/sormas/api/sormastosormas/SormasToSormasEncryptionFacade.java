package de.symeda.sormas.api.sormastosormas;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.ejb.Remote;

@Remote
public interface SormasToSormasEncryptionFacade {

	SormasToSormasEncryptedDataDto signAndEncrypt(Object entities, String recipientId) throws SormasToSormasException;

	<T> T decryptAndVerify(SormasToSormasEncryptedDataDto encryptedData, Class<T> dataType) throws SormasToSormasException;

	X509Certificate getOwnCertificate()
		throws SormasToSormasException, CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException;
}
