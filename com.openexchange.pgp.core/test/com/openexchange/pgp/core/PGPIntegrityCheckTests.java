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
import org.bouncycastle.openpgp.PGPKeyRingGenerator;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import com.openexchange.exception.OXException;
import com.openexchange.pgp.core.packethandling.PGPPacket;
import com.openexchange.pgp.core.packethandling.PacketProcessor;
import com.openexchange.pgp.core.packethandling.PacketProcessorHandler;

/**
 * {@link PGPIntegrityCheckTests}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.0
 */
public class PGPIntegrityCheckTests extends AbstractPGPTest {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    private PGPDecryptionResult encryptDecryptTestData(PacketProcessorHandler modificationHandler, PGPDecrypter.MDCValidationMode mdcValidationMode, boolean writeMDC) throws Exception {

        PGPKeyRingGenerator keyGenerator = createPGPKeyPairGenerator();
        PGPSecretKey secretKey = getSecretKeyFromGenerator(keyGenerator);
        Identity testIdentity = new Identity(TEST_IDENTITY_NAME, getPublicKeyFromGenerator(keyGenerator), secretKey, TEST_IDENTITY_PASSWORD);

        boolean armored = true;
        byte[] testData = generateTestData();
        ByteArrayInputStream testDataStream = new ByteArrayInputStream(testData);
        ByteArrayOutputStream encryptedDataOutputStream = new ByteArrayOutputStream();
        PGPEncrypter encrypter = new PGPEncrypter().setWithIntegrityPacket(writeMDC);
        encrypter.encrypt(testDataStream, encryptedDataOutputStream, armored, testIdentity.getPublicKey());

        ByteArrayInputStream encryptedDataStream = new ByteArrayInputStream(encryptedDataOutputStream.toByteArray());
        ByteArrayOutputStream modifiedEncryptedDataOutputStream = new ByteArrayOutputStream();
        //Extracting the session data out of the PGP Stream
        PacketProcessor packetProcessor = new PacketProcessor();
        packetProcessor.process(
            encryptedDataStream,
            modifiedEncryptedDataOutputStream,
            modificationHandler,
            armored);

        PGPKeyRetrievalStrategy onlyFindSecretKeyStrategy = mock(PGPKeyRetrievalStrategy.class);
        when(onlyFindSecretKeyStrategy.getSecretKey(anyLong(),anyString(),any(char[].class))).thenReturn(decodePrivateKey(secretKey, TEST_IDENTITY_PASSWORD));
        when(onlyFindSecretKeyStrategy.getPublicKey(anyLong())).thenReturn(null);
        PGPDecrypter decrypter = new PGPDecrypter(onlyFindSecretKeyStrategy).setMDCValidationMode(mdcValidationMode);

        ByteArrayInputStream modifiedEncryptedDataStream = new ByteArrayInputStream(modifiedEncryptedDataOutputStream.toByteArray());
        ByteArrayOutputStream decryptedStream = new ByteArrayOutputStream();
        PGPDecryptionResult result = decrypter.decrypt(modifiedEncryptedDataStream, decryptedStream, TEST_IDENTITY_NAME, TEST_IDENTITY_PASSWORD);
        Assert.assertArrayEquals("Decrypted data should be equals to plaintext data", decryptedStream.toByteArray(), testData);
        return result;
    }

    @Test
    public void dataManipulationShouldThrowException() throws Exception{
        expectedException.expect(OXException.class);
        expectedException.expectMessage("PGP-CORE-0006");
        final boolean writeMDC = true;
        encryptDecryptTestData(new PacketProcessorHandler() {

            @Override
            public PGPPacket[] handlePacket(PGPPacket packet) throws Exception {
                return new PGPPacket[] {packet};
            }

            @Override
            public byte[] handlePacketData(PGPPacket packet, byte[] packetData) {
                //Modifying the data must produce a MDC validation error
                packetData[packetData.length - 1] = 117;
                return packetData;
            }

        },
        PGPDecrypter.MDCValidationMode.FAIL_ON_MISSING,
        writeMDC);
    }

    @Test
    public void missingMDCshouldFail() throws Exception{
        expectedException.expect(OXException.class);
        expectedException.expectMessage("PGP-CORE-0006");
        final boolean writeMDC = false;
        encryptDecryptTestData(new PacketProcessorHandler() {

            @Override
            public PGPPacket[] handlePacket(PGPPacket packet) throws Exception {
                return new PGPPacket[] {packet};
            }

            @Override
            public byte[] handlePacketData(PGPPacket packet, byte[] packetData) {
                return packetData;
            }
        },
        PGPDecrypter.MDCValidationMode.FAIL_ON_MISSING,
        writeMDC);
    }

    @Test
    public void missingMDCshouldNotFail() throws Exception{
        final boolean writeMDC = false;
        PGPDecryptionResult result = encryptDecryptTestData(new PacketProcessorHandler() {

            @Override
            public PGPPacket[] handlePacket(PGPPacket packet) throws Exception {
                return new PGPPacket[] {packet};
            }

            @Override
            public byte[] handlePacketData(PGPPacket packet, byte[] packetData) {
                return packetData;
            }
        },
        PGPDecrypter.MDCValidationMode.WARN_ON_MISSING,
        writeMDC);
        Assert.assertFalse("The decryption result should not have a MDC present", result.getMDCVerificationResult().isPresent());
        Assert.assertFalse("The decryption result should not have a validated MDC", result.getMDCVerificationResult().isVerified());
    }
}