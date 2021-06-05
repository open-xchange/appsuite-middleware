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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import org.bouncycastle.openpgp.PGPKeyRingGenerator;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import com.openexchange.exception.OXException;

/**
 * {@link PGPEncrypterDecrypterTest}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.8.4
 */
@RunWith(value = Parameterized.class)
public class PGPEncrypterDecrypterTest extends AbstractPGPTest {

    private final boolean armored;
    private PGPPublicKey publicKey;
    private PGPSecretKey secretKey;
    private PGPKeyRetrievalStrategy keyRetrievalStrategy;

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    /**
     * Initializes a new {@link PGPEncrypterDecrypterTest}
     *
     * @param armored Whether to operate in ASCII-Armored mode or Binary mode
     */
    public PGPEncrypterDecrypterTest(boolean armored) {
        this.armored = armored;
    }

    /**
     * Defines injection of constructor parameters
     *
     * @return An iterable of Arrays which can be injected into the constructor when running the tests
     */
    @Parameters(name = "{index} - Ascii-armored: {0}")
    public static Iterable<?> parameters() {
        return Arrays.asList(new Object[][] {
            { Boolean.TRUE /* Runs the tests in ASCII-Armored mode */},
            { Boolean.FALSE /* Runs the tests in Binary-Mode */}
        });
    }

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
    public void testEncryptDecrypt() throws Exception {

        byte[] testData = "test".getBytes();
        InputStream plainTextData = new ByteArrayInputStream(testData);

        //Encrypting the data
        ByteArrayOutputStream encryptedData = new ByteArrayOutputStream();
        new PGPEncrypter().encrypt(plainTextData, encryptedData, armored, publicKey);

        //Decrypting the data
        InputStream encryptedDataInput = new ByteArrayInputStream(encryptedData.toByteArray());
        ByteArrayOutputStream decryptedData = new ByteArrayOutputStream();
        PGPDecryptionResult result = new PGPDecrypter(keyRetrievalStrategy).decrypt(encryptedDataInput, decryptedData, TEST_IDENTITY_NAME,
            TEST_IDENTITY_PASSWORD);
        List<PGPSignatureVerificationResult> verifyResults = result.getSignatureVerificationResults();
        Assert.assertTrue("Verification results should be empty for non signed data", verifyResults.isEmpty());
        Assert.assertArrayEquals("Decrypted data should be equals to plaintext data", decryptedData.toByteArray(), testData);

        //Verify that the key has been retrieved
        verify(keyRetrievalStrategy).getSecretKey(anyLong(),anyString(),any(char[].class));
    }

    @Test
    public void testEncryptSignedDecryptVerify() throws Exception {

        byte[] testData = "test".getBytes();
        InputStream plainTextData = new ByteArrayInputStream(testData);

        //Encrypting the data
        ByteArrayOutputStream encryptedData = new ByteArrayOutputStream();
        new PGPEncrypter().encryptSigned(plainTextData, encryptedData, armored, secretKey, TEST_IDENTITY_PASSWORD, publicKey);

        //Decrypting the data
        InputStream encryptedDataInput = new ByteArrayInputStream(encryptedData.toByteArray());
        ByteArrayOutputStream decryptedData = new ByteArrayOutputStream();
        PGPDecryptionResult result =
                new PGPDecrypter(keyRetrievalStrategy).decrypt(encryptedDataInput, decryptedData, TEST_IDENTITY_NAME, TEST_IDENTITY_PASSWORD);
        List<PGPSignatureVerificationResult> verifyResults = result.getSignatureVerificationResults();
        Assert.assertEquals("We should have obtained one verification result", 1, verifyResults.size());
        Assert.assertTrue("Signature should have been verified",verifyResults.get(0).isVerified());
        Assert.assertArrayEquals("Decrypted data should be equals to plaintext data", decryptedData.toByteArray(), testData);

        //Verify that both keys have been retrieved
        verify(keyRetrievalStrategy).getPublicKey(anyLong());
        verify(keyRetrievalStrategy).getSecretKey(anyLong(),anyString(),any(char[].class));
    }

    @Test
    public void testDecryptNonPGPDataShouldThrowException() throws Exception {
        expectedException.expect(OXException.class);
        expectedException.expectMessage("PGP-CORE-0001");

        //Trying to decrypt emtpy data should result in an OXException
        InputStream nonPGPData = new ByteArrayInputStream(new byte[] {});
        new PGPDecrypter(keyRetrievalStrategy).decrypt(nonPGPData, new ByteArrayOutputStream(), TEST_IDENTITY_NAME, TEST_IDENTITY_PASSWORD);
    }

    @Test
    public void testDecryptWithoutKnownPrivateKeyShouldThrowException() throws Exception {

        expectedException.expect(OXException.class);
        expectedException.expectMessage("PGP-CORE-0002");

        //A strategy which does not find any keys in order to test error handling
        PGPKeyRetrievalStrategy noKeyFoundRetrievalStrategy = mock(PGPKeyRetrievalStrategy.class);
        when(noKeyFoundRetrievalStrategy.getPublicKey(anyLong())).thenReturn(null);

        byte[] testData = "test".getBytes();
        InputStream plainTextData = new ByteArrayInputStream(testData);

        //Encrypting the data
        ByteArrayOutputStream encryptedData = new ByteArrayOutputStream();
        new PGPEncrypter().encryptSigned(plainTextData, encryptedData, armored, secretKey, TEST_IDENTITY_PASSWORD, publicKey);

        //Decrypting the data
        InputStream encryptedDataInput = new ByteArrayInputStream(encryptedData.toByteArray());
        ByteArrayOutputStream decryptedData = new ByteArrayOutputStream();
        new PGPDecrypter(noKeyFoundRetrievalStrategy).decrypt(encryptedDataInput, decryptedData, TEST_IDENTITY_NAME, TEST_IDENTITY_PASSWORD);
    }

    @Test
    public void testVerifySignatureWithoutKnownPublicKeyShouldReturnFalseSignatureVerificationResult() throws Exception{
        //A strategy which does find the secret key, but not the public key in order to test verification result
        PGPKeyRingGenerator keyGenerator = createPGPKeyPairGenerator();
        secretKey = getSecretKeyFromGenerator(keyGenerator);
        publicKey = getPublicKeyFromGenerator(keyGenerator);
        PGPKeyRetrievalStrategy onlyFindSecretKeyStrategy = mock(PGPKeyRetrievalStrategy.class);
        when(onlyFindSecretKeyStrategy.getSecretKey(anyLong(),anyString(),any(char[].class))).thenReturn(decodePrivateKey(secretKey, TEST_IDENTITY_PASSWORD));
        when(onlyFindSecretKeyStrategy.getPublicKey(anyLong())).thenReturn(null);

        byte[] testData = "test".getBytes();
        InputStream plainTextData = new ByteArrayInputStream(testData);

        //Encrypting the data
        ByteArrayOutputStream encryptedData = new ByteArrayOutputStream();
        new PGPEncrypter().encryptSigned(plainTextData, encryptedData, armored, secretKey, TEST_IDENTITY_PASSWORD, publicKey);

        //Decrypting the data
        InputStream encryptedDataInput = new ByteArrayInputStream(encryptedData.toByteArray());
        ByteArrayOutputStream decryptedData = new ByteArrayOutputStream();
        PGPDecryptionResult result =
            new PGPDecrypter(onlyFindSecretKeyStrategy).decrypt(encryptedDataInput, decryptedData, TEST_IDENTITY_NAME, TEST_IDENTITY_PASSWORD);
        List<PGPSignatureVerificationResult> verifyResults = result.getSignatureVerificationResults();

        Assert.assertEquals("We should have obtained one verification result", 1, verifyResults.size());
        Assert.assertFalse("Signature should NOT have been verified due to missing public key",verifyResults.get(0).isVerified());
        Assert.assertArrayEquals("Decrypted data should be equals to plaintext data", decryptedData.toByteArray(), testData);
    }

}
