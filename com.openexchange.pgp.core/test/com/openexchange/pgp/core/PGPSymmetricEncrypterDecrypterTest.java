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
    public static Iterable parameters() {
        return Arrays.asList(new Object[][] {
            { true /* Runs the tests in ASCII-Armored mode */},
            { false /* Runs the tests in Binary-Mode */}
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
