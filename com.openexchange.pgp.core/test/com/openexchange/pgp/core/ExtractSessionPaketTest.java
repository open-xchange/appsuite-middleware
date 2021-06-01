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

import static com.openexchange.java.Autoboxing.B;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Arrays;
import java.util.List;
import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPKeyRingGenerator;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import com.openexchange.pgp.core.packethandling.ExtractSessionProcessorHandler;
import com.openexchange.pgp.core.packethandling.ExtractSessionProcessorHandler.EncryptedSession;
import com.openexchange.pgp.core.packethandling.PacketProcessor;
import com.openexchange.tools.encoding.Base64;

/**
 * {@link ExtractSessionPaketTest} tests to extract the session packets from a PGP Stream.
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.0
 */
@RunWith(value = Parameterized.class)
public class ExtractSessionPaketTest extends AbstractPGPTest {

    private final boolean armored;
    private Identity testIdentity;

    /**
     * Initializes a new {@link ExtractSessionPaketTest}.
     *
     * @param armored Whether to operate in ACII-Armored or Binary mode
     */
    public ExtractSessionPaketTest(boolean armored) {
        this.armored = armored;
    }

    @Before
    public void setup() throws NoSuchAlgorithmException, NoSuchProviderException, PGPException {
        //Setting up a keypair for the first identity
        PGPKeyRingGenerator keyGenerator = createPGPKeyPairGenerator();
        testIdentity = new Identity(TEST_IDENTITY_NAME, getPublicKeyFromGenerator(keyGenerator), getSecretKeyFromGenerator(keyGenerator), TEST_IDENTITY_PASSWORD);
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

    private String getKeyData(PGPPublicKey publicKey) throws IOException {
        try(
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ArmoredOutputStream arm = new ArmoredOutputStream(out);
            ByteArrayOutputStream bout = new ByteArrayOutputStream();) {

            publicKey.encode(bout);
            arm.write(bout.toByteArray());
            arm.close();
            return new String(out.toByteArray());
        }
    }

    private String getKeyData(PGPSecretKey secretKey) throws IOException {
        try(
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ArmoredOutputStream arm = new ArmoredOutputStream(out);
            ByteArrayOutputStream bout = new ByteArrayOutputStream();) {

            secretKey.encode(bout);
            arm.write(bout.toByteArray());
            arm.close();
            return new String(out.toByteArray());
        }
    }

    /**
     * Tests to extract the symmetric session key from encrypted PGP data and use it for decryption.
     *
     * @throws Exception
     */
    @Test
    public void testExtractSymmetricSessionKeyAndDecrypt() throws Exception {

        //Encrypt the key data into a test PGP Stream
        byte[] testData = generateTestData();
        ByteArrayInputStream testDataStream = new ByteArrayInputStream(testData);
        ByteArrayOutputStream encryptedDataStream = new ByteArrayOutputStream();
        PGPEncrypter encrypter = new PGPEncrypter();
        encrypter.encrypt(testDataStream, encryptedDataStream, armored, testIdentity.getPublicKey());
        byte[] encryptedTestData  = encryptedDataStream.toByteArray();

        //Extracting the session data out of the PGP Stream
        PacketProcessor packetProcessor = new PacketProcessor();
        ExtractSessionProcessorHandler extractSessionProcessorHandler = new ExtractSessionProcessorHandler();
        packetProcessor.process(new ByteArrayInputStream(encryptedTestData),
            null,
            extractSessionProcessorHandler,
            armored);
        EncryptedSession encryptedPgpSession =
            extractSessionProcessorHandler.getEncryptedSession(testIdentity.getPublicKey().getKeyID());
        assertNotNull("The session for the given identity should have been decrypted", encryptedPgpSession);

        //-------------------------------------------------------------------------------------------------------------
        // Decrypting the symmetric encryption key from the obtained session data

        PGPSessionKeyExtractor pgpSessionDecrypter = new PGPSessionKeyExtractor();
        PGPPrivateKey privateKey = decodePrivateKey(testIdentity.getSecretKey(), TEST_IDENTITY_PASSWORD);
        byte[] symmetricKey = pgpSessionDecrypter.decryptSymmetricSessionKey(encryptedPgpSession, privateKey);
        assertTrue("The session key should have at least some data", symmetricKey.length > 0);

        //-------------------------------------------------------------------------------------------------------------

        //Now trying to decrypt the PGP Data just with the knowledge of the session key
        PGPSessionKeyDecrypter symmetricDecrypter = new PGPSessionKeyDecrypter(symmetricKey);
        ByteArrayOutputStream decryptedData = new ByteArrayOutputStream();
        PGPDecryptionResult result = symmetricDecrypter.decrypt(new ByteArrayInputStream(encryptedTestData), decryptedData);
        List<PGPSignatureVerificationResult> verifyResults = result.getSignatureVerificationResults();
        assertNotNull("Verification results should be empty for non signed data", B(verifyResults.isEmpty()));
        assertArrayEquals("Decrypted data should be equals to plaintext data", decryptedData.toByteArray(), testData);

        //-------------------------------------------------------------------------------------------------------------

        //Test output
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ NEW TEST RUN ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        System.out.println("------------------------------------PGP-PUBLIC-KEY-----------------------------------------");
        System.out.println(getKeyData(testIdentity.getPublicKey()));
        System.out.println("------------------------------------PGP-PRIVATE-KEY-----------------------------------------");
        System.out.println("Password: " + new String(testIdentity.getPassword()));
        System.out.println(getKeyData(testIdentity.getSecretKey()));
        System.out.println("------------------------------------Encoded Session Packet-----------------------------------------");
        System.out.println(Base64.encode(encryptedPgpSession.getEncoded()));
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        //-------------------------------------------------------------------------------------------------------------
    }
}
