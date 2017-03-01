
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import org.bouncycastle.openpgp.PGPKeyRingGenerator;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * {@link PGPSignVerifyTests}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v2.4.2
 */
public class PGPSignVerifyTests extends AbstractPGPTest {

    private PGPPublicKey publicKey;
    private PGPSecretKey secretKey;
    private PGPKeyRetrievalStrategy keyRetrievalStrategy;

    @Before
    public void setup() throws Exception {
        PGPKeyRingGenerator keyGenerator = createPGPKeyPairGenerator();
        publicKey = getPublicKeyFromGenerator(keyGenerator);
        secretKey = getSecretKeyFromGenerator(keyGenerator);

        //Setting up a strategy for key retrieving, this is used when decrypting data
        keyRetrievalStrategy = mock(PGPKeyRetrievalStrategy.class);
        when(keyRetrievalStrategy.getPublicKey(anyLong())).thenReturn(publicKey);
        when(keyRetrievalStrategy.getSecretKey(anyLong(),anyString(),any(char[].class))).thenReturn(decodePrivateKey(secretKey, TEST_PASSWORD));
    }

    @Test
    public void testSignVerify() throws Exception {
        byte[] testData = "test".getBytes();
        InputStream plainTextData = new ByteArrayInputStream(testData);

        //Create the signature
        ByteArrayOutputStream signatureData = new ByteArrayOutputStream();
        new PGPSignatureCreator().createSignature(plainTextData, signatureData, true, secretKey, TEST_PASSWORD);
        String signature = new String(signatureData.toByteArray());
        ByteArrayInputStream signatureStream = new ByteArrayInputStream(signature.getBytes());
        Assert.assertTrue(signature.startsWith("-----BEGIN PGP SIGNATURE-----"));
        Assert.assertTrue(signature.trim().contains("-----END PGP SIGNATURE-----"));

        plainTextData = new ByteArrayInputStream("test".getBytes());
        List<PGPSignatureVerificationResult> verifyResults = new PGPSignatureVerifier(keyRetrievalStrategy).verifySignatures(plainTextData,signatureStream);
        Assert.assertTrue("Verification should create at least one result", verifyResults.size() > 0);
        for(PGPSignatureVerificationResult result : verifyResults) {
            Assert.assertTrue("The created signature should have been verified", result.isVerified());
            Assert.assertTrue("The signature should have been created with the user's key id", result.getSignature().getKeyID() == secretKey.getKeyID());
            System.out.println(result.getSignature().getKeyID());
        }
    }

    @Test
    public void testSignVerifyWithouthKnownPublicKeyShouldReturnFalseSignatureVerificationResult() throws Exception{
        //A strategy which does find the secret key, but not the public key in order to test verification result
        PGPKeyRingGenerator keyGenerator = createPGPKeyPairGenerator();
        secretKey = getSecretKeyFromGenerator(keyGenerator);
        publicKey = getPublicKeyFromGenerator(keyGenerator);
        PGPKeyRetrievalStrategy onlyFindSecretKeyStrategy = mock(PGPKeyRetrievalStrategy.class);
        when(onlyFindSecretKeyStrategy.getSecretKey(anyLong(),anyString(),any(char[].class))).thenReturn(decodePrivateKey(secretKey, TEST_PASSWORD));
        when(onlyFindSecretKeyStrategy.getPublicKey(anyLong())).thenReturn(null);

        byte[] testData = "test".getBytes();
        InputStream plainTextData = new ByteArrayInputStream(testData);

        //Create the signature
        ByteArrayOutputStream signatureData = new ByteArrayOutputStream();
        new PGPSignatureCreator().createSignature(plainTextData, signatureData, true, secretKey, TEST_PASSWORD);
        String signature = new String(signatureData.toByteArray());
        ByteArrayInputStream signatureStream = new ByteArrayInputStream(signature.getBytes());
        Assert.assertTrue(signature.startsWith("-----BEGIN PGP SIGNATURE-----"));
        Assert.assertTrue(signature.trim().contains("-----END PGP SIGNATURE-----"));

        plainTextData = new ByteArrayInputStream("test".getBytes());
        List<PGPSignatureVerificationResult> verifyResults = new PGPSignatureVerifier(onlyFindSecretKeyStrategy).verifySignatures(plainTextData,signatureStream);
        Assert.assertTrue("One Verification result expected", verifyResults.size() == 1);
        Assert.assertFalse("Signature should NOT have been verified due to missing public key", verifyResults.get(0).isVerified());
    }
}
