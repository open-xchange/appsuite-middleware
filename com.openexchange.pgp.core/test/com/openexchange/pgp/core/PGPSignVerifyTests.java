
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


package com.openexchange.pgp.core;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;
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
 * @since v7.8.4
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
        when(keyRetrievalStrategy.getSecretKey(anyLong(),anyString(),any(char[].class))).thenReturn(decodePrivateKey(secretKey, TEST_IDENTITY_PASSWORD));
    }

    @Test
    public void testSignVerify() throws Exception {
        byte[] testData = "test".getBytes();
        InputStream plainTextData = new ByteArrayInputStream(testData);

        //Create the signature
        ByteArrayOutputStream signatureData = new ByteArrayOutputStream();
        new PGPSignatureCreator().createSignature(plainTextData, signatureData, true, secretKey, TEST_IDENTITY_PASSWORD);
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
        when(onlyFindSecretKeyStrategy.getSecretKey(anyLong(),anyString(),any(char[].class))).thenReturn(decodePrivateKey(secretKey, TEST_IDENTITY_PASSWORD));
        when(onlyFindSecretKeyStrategy.getPublicKey(anyLong())).thenReturn(null);

        byte[] testData = "test".getBytes();
        InputStream plainTextData = new ByteArrayInputStream(testData);

        //Create the signature
        ByteArrayOutputStream signatureData = new ByteArrayOutputStream();
        new PGPSignatureCreator().createSignature(plainTextData, signatureData, true, secretKey, TEST_IDENTITY_PASSWORD);
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
