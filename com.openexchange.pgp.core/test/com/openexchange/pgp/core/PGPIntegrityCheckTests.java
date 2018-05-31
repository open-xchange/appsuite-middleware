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