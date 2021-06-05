/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.saml;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStore.PasswordProtection;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.util.PrivateKeyFactory;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import org.bouncycastle.operator.DefaultSignatureAlgorithmIdentifierFinder;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.bc.BcDSAContentSignerBuilder;
import org.bouncycastle.operator.bc.BcRSAContentSignerBuilder;
import org.opensaml.security.credential.BasicCredential;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.credential.UsageType;
import org.opensaml.security.x509.BasicX509Credential;
import com.openexchange.saml.spi.CredentialProvider;
import com.openexchange.saml.spi.KeyStoreCredentialProvider;

/**
 * {@link TestCredentials}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public class TestCredentials {

    public static final String SP_KEY_STORE_PASSWORD = "lkd789013sd";

    public static final String SP_ENCRYPTION_CERT_ALIAS = "sp-encryption-cert";

    public static final String SP_ENCRYPTION_KEY_ALIAS = "sp-encryption-key";

    public static final String SP_ENCRYPTION_KEY_PASSWORD = "fsdafs78";

    public static final String SP_SIGNING_KEY_ALIAS = "sp-signing-key";

    public static final String SP_SIGNING_KEY_PASSWORD = "cpl#al56df";

    public static final String SP_SIGNING_CERT_ALIAS = "sp-signing-cert";

    public static final String IDP_KEY_STORE_PASSWORD = "dsjk546565";

    public static final String IDP_SIGNING_KEY_ALIAS = "idp-signing-key";

    public static final String IDP_SIGNING_KEY_PASSWORD = "jh!65gaasa";

    public static final String IDP_SIGNING_CERT_ALIAS = "idp-signing-cert";


    /**
     * KeyStore of the IDP. Contains:
     * <ul>
     * <li>A certificate of the SP for encrypting response data</li>
     * <li>A certificate of the SP to validate signed request data</li>
     * <li>A private key for signing response data</li>
     * </ul>
     */
    private final KeyStore idpKeyStore;

    /**
     * KeyStore of the SP. Contains:
     * <ul>
     * <li>A private key for decrypting IDP response data</li>
     * <li>A private key for signing request data</li>
     * <li>A certificate of the IDP to validate signed response data</li>
     * </ul>
     */
    private final KeyStore spKeyStore;

    public TestCredentials() throws Exception {
        KeyPairGenerator dsaGenerator = KeyPairGenerator.getInstance("DSA");
        dsaGenerator.initialize(1024);
        KeyPairGenerator rsaGenerator = KeyPairGenerator.getInstance("RSA");
        rsaGenerator.initialize(4096);

        idpKeyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        idpKeyStore.load(null, IDP_KEY_STORE_PASSWORD.toCharArray());

        spKeyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        spKeyStore.load(null, SP_KEY_STORE_PASSWORD.toCharArray());

        /*
         * Build SPs encryption key pair and certificate
         */
        KeyPair spEncryptionKeyPair = rsaGenerator.generateKeyPair();
        Certificate spEncryptionCert = getCertificateForKeyPair(spEncryptionKeyPair);
        spKeyStore.setKeyEntry(SP_ENCRYPTION_KEY_ALIAS, spEncryptionKeyPair.getPrivate(), SP_ENCRYPTION_KEY_PASSWORD.toCharArray(), new Certificate[] { spEncryptionCert });
        idpKeyStore.setCertificateEntry(SP_ENCRYPTION_CERT_ALIAS, spEncryptionCert);

        /*
         * Build SPs signing key pair and certificate
         */
        KeyPair spSigningKeyPair = dsaGenerator.generateKeyPair();
        Certificate spSigningCert = getCertificateForKeyPair(spSigningKeyPair);
        spKeyStore.setKeyEntry(SP_SIGNING_KEY_ALIAS, spSigningKeyPair.getPrivate(), SP_SIGNING_KEY_PASSWORD.toCharArray(), new Certificate[] { spSigningCert });
        idpKeyStore.setCertificateEntry(SP_SIGNING_CERT_ALIAS, spSigningCert);

        /*
         * Build IDPs signing key pair and certificate
         */
        KeyPair idpSigningKeyPair = dsaGenerator.generateKeyPair();
        Certificate idpSigningCert = getCertificateForKeyPair(idpSigningKeyPair);
        idpKeyStore.setKeyEntry(IDP_SIGNING_KEY_ALIAS, idpSigningKeyPair.getPrivate(), IDP_SIGNING_KEY_PASSWORD.toCharArray(), new Certificate[] { idpSigningCert });
        spKeyStore.setCertificateEntry(IDP_SIGNING_CERT_ALIAS, idpSigningCert);
    }

    public CredentialProvider getSPCredentialProvider() throws Exception {
        File tmpFile = File.createTempFile("sp-test-key-store", ".jks");
        tmpFile.deleteOnExit();
        spKeyStore.store(new FileOutputStream(tmpFile), SP_KEY_STORE_PASSWORD.toCharArray());
        return KeyStoreCredentialProvider.newInstance(
            tmpFile.getAbsolutePath(),
            SP_KEY_STORE_PASSWORD.toCharArray(),
            Collections.singletonList(IDP_SIGNING_CERT_ALIAS),
            SP_SIGNING_KEY_ALIAS,
            SP_SIGNING_KEY_PASSWORD.toCharArray(),
            SP_ENCRYPTION_KEY_ALIAS,
            SP_ENCRYPTION_KEY_PASSWORD.toCharArray());
    }

    public Credential getEncryptionCredential() throws Exception {
        BasicCredential credential = new BasicCredential(idpKeyStore.getCertificate(SP_ENCRYPTION_CERT_ALIAS).getPublicKey());
        credential.setUsageType(UsageType.ENCRYPTION);
        return credential;
    }

    public Credential getDecryptionCredential() throws Exception {
        BasicCredential credential = new BasicCredential(null, ((PrivateKeyEntry) spKeyStore.getEntry(SP_ENCRYPTION_KEY_ALIAS, new PasswordProtection(SP_ENCRYPTION_KEY_PASSWORD.toCharArray()))).getPrivateKey());
        credential.setUsageType(UsageType.ENCRYPTION);
        return credential;
    }

    private static Certificate getCertificateForKeyPair(KeyPair keyPair) throws OperatorCreationException, IOException, CertificateException {
        AlgorithmIdentifier sigAlgId = new DefaultSignatureAlgorithmIdentifierFinder().find("SHA1with" + keyPair.getPrivate().getAlgorithm());
        AlgorithmIdentifier digAlgId = new DefaultDigestAlgorithmIdentifierFinder().find(sigAlgId);
        SubjectPublicKeyInfo keyInfo = SubjectPublicKeyInfo.getInstance(keyPair.getPublic().getEncoded());

        X500Name issuer = new X500Name("C=DE, ST=NRW, L=Olpe, O=Open-Xchange GmbH, OU=Engineering, CN=Steffen Templin");
        X509v3CertificateBuilder myCertificateGenerator = new X509v3CertificateBuilder(
            issuer,
            new BigInteger(Long.toString(System.nanoTime())),
            new Date(System.currentTimeMillis()),
            new Date(System.currentTimeMillis() + 365 * 24 * 60 * 60 * 1000),
            issuer,
            keyInfo);

        ContentSigner sigGen;
        AsymmetricKeyParameter privateKey = PrivateKeyFactory.createKey(keyPair.getPrivate().getEncoded());
        if (keyPair.getPrivate().getAlgorithm().equals("RSA")) {
            sigGen = new BcRSAContentSignerBuilder(sigAlgId, digAlgId).build(privateKey);
        } else {
            sigGen = new BcDSAContentSignerBuilder(sigAlgId, digAlgId).build(privateKey);
        }

        X509CertificateHolder holder = myCertificateGenerator.build(sigGen);
        CertificateFactory cf = CertificateFactory.getInstance("X.509");

        // Read Certificate
        InputStream is = new ByteArrayInputStream(holder.getEncoded());
        try {
            return cf.generateCertificate(is);
        } finally {
            is.close();
        }
    }

    public Credential getIDPSigningCredential() throws Exception {
        PrivateKeyEntry signingKeyEntry = (PrivateKeyEntry) idpKeyStore.getEntry(IDP_SIGNING_KEY_ALIAS, new PasswordProtection(IDP_SIGNING_KEY_PASSWORD.toCharArray()));
        Certificate certificate = signingKeyEntry.getCertificate();
        BasicX509Credential signingCredential = new BasicX509Credential((java.security.cert.X509Certificate) certificate, signingKeyEntry.getPrivateKey());
        signingCredential.setUsageType(UsageType.SIGNING);
        signingCredential.setEntityCertificateChain(Arrays.asList((java.security.cert.X509Certificate[]) signingKeyEntry.getCertificateChain()));

        return signingCredential;
    }

    public Credential getSPSigningCredential() throws Exception {
        PrivateKeyEntry signingKeyEntry = (PrivateKeyEntry) spKeyStore.getEntry(SP_SIGNING_KEY_ALIAS, new PasswordProtection(SP_SIGNING_KEY_PASSWORD.toCharArray()));
        Certificate certificate = signingKeyEntry.getCertificate();
        BasicX509Credential signingCredential = new BasicX509Credential((java.security.cert.X509Certificate) certificate, signingKeyEntry.getPrivateKey());
        signingCredential.setUsageType(UsageType.SIGNING);
        signingCredential.setEntityCertificateChain(Arrays.asList((java.security.cert.X509Certificate[]) signingKeyEntry.getCertificateChain()));

        return signingCredential;
    }

}
