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

package com.openexchange.pgp.core;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.util.Date;
import org.bouncycastle.bcpg.HashAlgorithmTags;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.PGPEncryptedData;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPKeyPair;
import org.bouncycastle.openpgp.PGPKeyRingGenerator;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.PGPSignature;
import org.bouncycastle.openpgp.operator.PBESecretKeyDecryptor;
import org.bouncycastle.openpgp.operator.PGPDigestCalculator;
import org.bouncycastle.openpgp.operator.bc.BcPBESecretKeyDecryptorBuilder;
import org.bouncycastle.openpgp.operator.bc.BcPGPDigestCalculatorProvider;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPContentSignerBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPDigestCalculatorProviderBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPKeyPair;
import org.bouncycastle.openpgp.operator.jcajce.JcePBESecretKeyEncryptorBuilder;

/**
 * {@link AbstractPGPTest}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v2.4.2
 */
public class AbstractPGPTest {

    protected int TEST_KEY_SIZE = 1024;
    protected String TEST_IDENTITY = "Max Mustermann";
    protected char[] TEST_PASSWORD = "secret".toCharArray();

    /**
     * Initializes a new {@link AbstractPGPTest}.
     */
    public AbstractPGPTest() {
        //Adding BouncyCastleProvider if not already added
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    /**
     * Generates a basic KeyPair for testing purpose
     * 
     * @return A KeyPair
     * @throws NoSuchAlgorithmException
     * @throws NoSuchProviderException
     */
    public KeyPair generateKeyPair() throws NoSuchAlgorithmException, NoSuchProviderException {
        //Generating RSA key pair
        final KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA", "BC");
        keyPairGenerator.initialize(TEST_KEY_SIZE);
        return keyPairGenerator.generateKeyPair();
    }

    /**
     * Create a new key pair generator using the default test credentials
     * 
     * @return The new generator
     * @throws NoSuchAlgorithmException
     * @throws NoSuchProviderException
     * @throws PGPException
     */
    public PGPKeyRingGenerator createPGPKeyPairGenerator() throws NoSuchAlgorithmException, NoSuchProviderException, PGPException {
        return createPGPKeyPairGenerator(TEST_IDENTITY, TEST_PASSWORD);
    }

    /**
     * Creates a new key pair generator
     * 
     * @param identity the identify of to set for the key pair
     * @param passphrase the passphrase for protecting the SecretKey
     * @return the new generator
     * @throws PGPException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchProviderException
     */
    public PGPKeyRingGenerator createPGPKeyPairGenerator(String identity, char[] passphrase) throws PGPException, NoSuchAlgorithmException, NoSuchProviderException {
        PGPKeyPair pgpKeyPair = new JcaPGPKeyPair(PGPPublicKey.RSA_GENERAL, generateKeyPair(), new Date());

        PGPDigestCalculator sha1Calculator = new JcaPGPDigestCalculatorProviderBuilder().build().get(HashAlgorithmTags.SHA1);
        return new PGPKeyRingGenerator(PGPSignature.POSITIVE_CERTIFICATION,
            pgpKeyPair,
            identity,
            sha1Calculator,
            null,
            null,
            new JcaPGPContentSignerBuilder(pgpKeyPair.getPublicKey().getAlgorithm(), HashAlgorithmTags.SHA1),
            new JcePBESecretKeyEncryptorBuilder(PGPEncryptedData.AES_256, sha1Calculator).setProvider("BC").build(passphrase));
    }

    /**
     * Extracts the public key from the generator
     * 
     * @param generator the generator to extract the public key from
     * @return the extracted public key
     */
    public PGPPublicKey getPublicKeyFromGenerator(PGPKeyRingGenerator generator) {
        return generator.generatePublicKeyRing().getPublicKey();
    }

    /**
     * Extracts the secret key from the generator
     * 
     * @param generator the generator to extract the secret key from
     * @return the extracted secret key
     */
    public PGPSecretKey getSecretKeyFromGenerator(PGPKeyRingGenerator generator) {
        return generator.generateSecretKeyRing().getSecretKey();
    }
    
    /**
     * Extracts a private key from a PGPSecretKey object
     * 
     * @param secretKey The PGPSecretKeyObject
     * @param password The password 
     * @return The decoded private key
     * @throws PGPException
     */
    public PGPPrivateKey decodePrivateKey(PGPSecretKey secretKey, char[] password) throws PGPException {        
        PBESecretKeyDecryptor extractor = new BcPBESecretKeyDecryptorBuilder(new BcPGPDigestCalculatorProvider()).build(password);
        return secretKey.extractPrivateKey(extractor);
    }
}
