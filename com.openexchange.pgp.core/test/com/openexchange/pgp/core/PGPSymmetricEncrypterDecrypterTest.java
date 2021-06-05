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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import org.bouncycastle.openpgp.PGPException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * {@link PGPSymmetricEncrypterDecrypterTest}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.2
 */
@RunWith(value = Parameterized.class)
public class PGPSymmetricEncrypterDecrypterTest {

    private final boolean armored;
    private static final byte[] TEST_DATA = "Hello World!".getBytes();
    private static final char[] TEST_PASSWORD = new char[] { 'S', 'E', 'C', 'R', 'E', 'T' };
    private static final int AES_256 = 9; //see RFC 4880
    private static final int ZLIB = 2; //see RFC 4880

    /**
     * Initializes a new {@link PGPSymmetricEncrypterDecrypterTest}.
     *
     * @param armored Whether to operate in ASCII-Armored mode or Binary mode
     */
    public PGPSymmetricEncrypterDecrypterTest(boolean armored) {
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

    @Test
    public void testEncryptDecrypt() throws Exception {
        //Encrypt
        ByteArrayInputStream plaintextStream = new ByteArrayInputStream(TEST_DATA);
        ByteArrayOutputStream encryptedData = new ByteArrayOutputStream();
        new PGPSymmetricEncrypter().encrypt(plaintextStream, encryptedData, armored, TEST_PASSWORD);

        //Decrypt
        ByteArrayOutputStream decryptedStream = new ByteArrayOutputStream();
        new PGPSymmetricDecrypter().decrypt(new ByteArrayInputStream(encryptedData.toByteArray()), decryptedStream, TEST_PASSWORD);

        //Verify
        Assert.assertArrayEquals(TEST_DATA, decryptedStream.toByteArray());
    }

    @Test
    public void testEncryptDecryptCompressed() throws Exception {
        //Encrypt
        ByteArrayInputStream plaintextStream = new ByteArrayInputStream(TEST_DATA);
        ByteArrayOutputStream encryptedData = new ByteArrayOutputStream();
        new PGPSymmetricEncrypter(AES_256, ZLIB).encrypt(plaintextStream, encryptedData, armored, TEST_PASSWORD);

        //Decrypt
        ByteArrayOutputStream decryptedStream = new ByteArrayOutputStream();
        new PGPSymmetricDecrypter().decrypt(new ByteArrayInputStream(encryptedData.toByteArray()), decryptedStream, TEST_PASSWORD);

        //Verify
        Assert.assertArrayEquals(TEST_DATA, decryptedStream.toByteArray());
    }

    @Test(expected = PGPException.class)
    public void testDecryptWithWrongKeyShouldFail() throws Exception {
        //Encrypt
        ByteArrayInputStream plaintextStream = new ByteArrayInputStream(TEST_DATA);
        ByteArrayOutputStream encryptedData = new ByteArrayOutputStream();
        new PGPSymmetricEncrypter().encrypt(plaintextStream, encryptedData, armored, TEST_PASSWORD);

        //Decrypt with wrong password should throw a PGPException
        final char[] WRONG_PASSWORD = new char[] { 'W', 'R', 'O', 'N', 'G', '!' };
        ByteArrayOutputStream decryptedStream = new ByteArrayOutputStream();
        new PGPSymmetricDecrypter().decrypt(new ByteArrayInputStream(encryptedData.toByteArray()), decryptedStream, WRONG_PASSWORD);
    }
}
