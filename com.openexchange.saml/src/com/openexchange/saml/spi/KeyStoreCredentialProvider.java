/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.saml.spi;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStore.Entry;
import java.security.KeyStore.PasswordProtection;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.KeyStore.ProtectionParameter;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.codec.digest.DigestUtils;
import org.opensaml.xml.security.credential.BasicCredential;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.security.credential.UsageType;
import org.opensaml.xml.security.x509.BasicX509Credential;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.saml.SAMLExceptionCode;


/**
 * A {@link CredentialProvider} based on a {@link KeyStore}.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public class KeyStoreCredentialProvider extends AbstractCredentialProvider {

    private KeyStoreCredentialProvider(List<Credential> idpCertificateCredential, Credential signingCredential, Credential decryptionCredential) {
        super(idpCertificateCredential, signingCredential, decryptionCredential);
    }

    /**
     * Creates a new instance of a {@link KeyStoreCredentialProvider}.
     *
     * @param keyStorePath The file system path of the key store file
     * @param keyStorePassword The key stores password or <code>null</code> if it's unprotected
     * @param idpCertAlias The alias of the entry of the IDPs certificate to validate signatures or <code>null</code>
     * @param signingKeyAlias The alias of the private key entry used for signing SP requests or <code>null</code>
     * @param signingKeyPassword The password to load the signing key or <code>null</code>
     * @param decryptionKeyAlias The alias of the private key entry used to decrypt encrypted data or encryption keys or <code>null</code>
     * @param decryptionKeyPassword The password to load the decryption key or <code>null</code>
     */
    public static KeyStoreCredentialProvider newInstance(String keyStorePath, char[] keyStorePassword, String idpCertAlias, String signingKeyAlias, char[] signingKeyPassword, String decryptionKeyAlias, char[] decryptionKeyPassword) throws OXException {
        return newInstance(keyStorePath, keyStorePassword, Collections.singletonList(idpCertAlias), signingKeyAlias, signingKeyPassword, decryptionKeyAlias, decryptionKeyPassword);
    }

    /**
     * Creates a new instance of a {@link KeyStoreCredentialProvider}.
     *
     * @param keyStorePath The file system path of the key store file
     * @param keyStorePassword The key stores password or <code>null</code> if it's unprotected
     * @param idpCertAliases The list of aliases of the entry of the IDPs certificate to validate signatures or <code>null</code>
     * @param signingKeyAlias The alias of the private key entry used for signing SP requests or <code>null</code>
     * @param signingKeyPassword The password to load the signing key or <code>null</code>
     * @param decryptionKeyAlias The alias of the private key entry used to decrypt encrypted data or encryption keys or <code>null</code>
     * @param decryptionKeyPassword The password to load the decryption key or <code>null</code>
     */
    public static KeyStoreCredentialProvider newInstance(String keyStorePath, char[] keyStorePassword, List<String> idpCertAliases, String signingKeyAlias, char[] signingKeyPassword, String decryptionKeyAlias, char[] decryptionKeyPassword) throws OXException {
        if (Strings.isEmpty(keyStorePath)) {
            throw SAMLExceptionCode.CREDENTIAL_PROBLEM.create("a keystore path must be set");
        }
        BasicCredential signingCredential = null;
        BasicCredential encryptionCredential = null;
        List<Credential> idpCertificateCredentials = new ArrayList<>();
        try {
            KeyStore keyStore = initKeyStore(keyStorePath, keyStorePassword);
            boolean keystoreHasCertificates = false;
            if (idpCertAliases.isEmpty() == false) {
                keystoreHasCertificates = loadCertificates(idpCertAliases, idpCertificateCredentials, keyStore, keystoreHasCertificates);
                if (keystoreHasCertificates == false) {
                    throw SAMLExceptionCode.CREDENTIAL_PROBLEM.create("key store contains no certificate for alias '" + idpCertAliases + "'");
                }
            }
            signingCredential = credentialFromKeyPair(keyStore, signingKeyAlias, signingKeyPassword, UsageType.SIGNING);
            encryptionCredential = credentialFromKeyPair(keyStore, decryptionKeyAlias, decryptionKeyPassword, UsageType.ENCRYPTION);
        } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableEntryException | CertificateEncodingException e) {
            throw SAMLExceptionCode.CREDENTIAL_PROBLEM.create(e, e.getMessage());
        } 

        return new KeyStoreCredentialProvider(idpCertificateCredentials, signingCredential, encryptionCredential);
    }

    private static boolean loadCertificates(List<String> idpCertAliases, List<Credential> idpCertificateCredentials, KeyStore keyStore, boolean keystoreHasCertificates) throws KeyStoreException, CertificateEncodingException {
        BasicCredential idpCertificateCredential;
        for (String alias : idpCertAliases) {
            Certificate certificate = keyStore.getCertificate(alias);
            if (certificate != null) {
                keystoreHasCertificates = true;
                idpCertificateCredential = new BasicCredential();
                idpCertificateCredential.setUsageType(UsageType.SIGNING);
                idpCertificateCredential.setPublicKey(certificate.getPublicKey());
                idpCertificateCredentials.add(idpCertificateCredential);
            }
        }
        return keystoreHasCertificates;
    }

    private static BasicCredential credentialFromKeyPair(KeyStore keyStore, String alias, char[] password, UsageType type) throws NoSuchAlgorithmException, UnrecoverableEntryException, KeyStoreException, OXException, CertificateEncodingException {
        if (Strings.isEmpty(alias)) {
            return null;
        }

        ProtectionParameter pp = null;
        if (password != null) {
            pp = new PasswordProtection(password);
        }

        Entry keyStoreEntry = keyStore.getEntry(alias, pp);
        if (keyStoreEntry == null) {
            throw SAMLExceptionCode.CREDENTIAL_PROBLEM.create("key store contains no entry key for alias '" + alias + "'");
        }

        if (!(keyStoreEntry instanceof KeyStore.PrivateKeyEntry)) {
            throw SAMLExceptionCode.CREDENTIAL_PROBLEM.create("key store entry for alias '" + alias + "' is not a private key");
        }

        PrivateKeyEntry pke = (PrivateKeyEntry) keyStoreEntry;
        Certificate certificate = pke.getCertificate();
        if (certificate instanceof X509Certificate) {
            // An X.509 certificate
            BasicX509Credential x509credential = new BasicX509Credential();
            x509credential.setUsageType(type);
            x509credential.setEntityCertificate((X509Certificate) certificate);
            x509credential.setPrivateKey(pke.getPrivateKey());
            x509credential.setPublicKey(pke.getCertificate().getPublicKey());
            return x509credential;
        }

        // Default one...
        BasicCredential credential = new BasicCredential();
        credential.setUsageType(type);
        credential.setPublicKey(certificate.getPublicKey());
        credential.setPrivateKey(pke.getPrivateKey());
        return credential;
    }

    private static KeyStore initKeyStore(String path, char[] password) throws OXException, KeyStoreException {
        File keyStoreFile = new File(path);
        if (!keyStoreFile.exists() || !keyStoreFile.isFile()) {
            throw SAMLExceptionCode.CREDENTIAL_PROBLEM.create("The keystore path " + path + " points to an invalid file");
        }

        if (!keyStoreFile.canRead()) {
            throw SAMLExceptionCode.CREDENTIAL_PROBLEM.create("The keystore file " + path + " is not readable");
        }

        try (FileInputStream inputStream = new FileInputStream(keyStoreFile)){
            KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
            keystore.load(inputStream, password == null ? null : password);
            return keystore;
        } catch (NoSuchAlgorithmException | CertificateException | IOException e) {
            throw SAMLExceptionCode.CREDENTIAL_PROBLEM.create(e, e.getMessage());
        }
    }
}
