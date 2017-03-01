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
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static org.mockito.Mockito.*;
import org.mockito.Matchers;
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
import com.openexchange.pgp.core.packethandling.AddRecipientPacketProcessorHandler;
import com.openexchange.pgp.core.packethandling.PacketProcessor;
import com.openexchange.pgp.core.packethandling.RemoveRecipientPacketProcessorHandler;

/**
 * {@link ModifyRecipientTest}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v2.4.2
 */
@RunWith(value = Parameterized.class)
public class ModifyRecipientTest extends AbstractPGPTest {

    private static final String BEGIN_PGP_MARKER = "-----BEGIN PGP MESSAGE-----";
    private final boolean armored;
    private PGPKeyRetrievalStrategy keyRetrievalStrategy;
    private Identity identity;
    private Identity identity2;

    /**
     *
     * {@link Identity} Represents a Identity containing one PGP Key useful for testing purpose.
     *
     * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
     * @since v2.4.2
     */
    private class Identity {

        private final String identity;
        private final PGPPublicKey publicKey;
        private final PGPSecretKey secretKey;
        private final char[] password;

        /**
         * Initializes a new {@link Identity}.
         *
         * @param identity The name of the identity
         * @param publicKey The public key of the identity
         * @param secretKey The secret key of the identity
         * @param password The password for accessing the private key
         */
        public Identity(String identity, PGPPublicKey publicKey, PGPSecretKey secretKey, char[] password) {
            super();
            this.identity = identity;
            this.publicKey = publicKey;
            this.secretKey = secretKey;
            this.password = password;
        }

        /**
         * Gets the identity
         *
         * @return The identity
         */
        public String getIdentity() {
            return identity;
        }

        /**
         * Gets the publicKey
         *
         * @return The publicKey
         */
        public PGPPublicKey getPublicKey() {
            return publicKey;
        }

        /**
         * Gets the secretKey
         *
         * @return The secretKey
         */
        public PGPSecretKey getSecretKey() {
            return secretKey;
        }

        /**
         * Gets the password
         *
         * @return The password
         */
        public char[] getPassword() {
            return password;
        }
    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();

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

    /**
     * Initializes a new {@link ModifyRecipientTest}.
     *
     * @param armored Whether to operate in ASCII-Armored mode or Binary mode
     */
    public ModifyRecipientTest(boolean armored) {
        this.armored = armored;
    }

    @Before
    public void setup() throws Exception {
        //Setting up a keypair for the first identity
        PGPKeyRingGenerator keyGenerator = createPGPKeyPairGenerator();
        identity = new Identity(TEST_IDENTITY, getPublicKeyFromGenerator(keyGenerator), getSecretKeyFromGenerator(keyGenerator), TEST_PASSWORD);

        //Setup an additional key pair for the 2nd identity
        final String TEST_IDENTITY_2 = "user2";
        final char[] TEST_PASSWORD_2 = "secret".toCharArray();
        PGPKeyRingGenerator keyGenerator2 = createPGPKeyPairGenerator(TEST_IDENTITY_2, TEST_PASSWORD_2);
        identity2 = new Identity(TEST_IDENTITY_2, getPublicKeyFromGenerator(keyGenerator2), getSecretKeyFromGenerator(keyGenerator2), TEST_PASSWORD_2);

        //Setting up a strategy for key retrieving, this is used when decrypting data
        keyRetrievalStrategy = mock(PGPKeyRetrievalStrategy.class);
        when(keyRetrievalStrategy.getSecretKey(Matchers.eq(identity.getSecretKey().getKeyID()), Matchers.eq(identity.getIdentity()), Matchers.eq(identity.getPassword()))).thenReturn(decodePrivateKey(identity.getSecretKey(), identity.getPassword()));
        when(keyRetrievalStrategy.getSecretKey(Matchers.eq(identity2.getSecretKey().getKeyID()), Matchers.eq(identity2.getIdentity()), Matchers.eq(identity2.getPassword()))).thenReturn(decodePrivateKey(identity2.getSecretKey(), identity2.getPassword()));
    }

    /**
     * A helper method for getting all Public Keys from a group of identities
     *
     * @param identities The identities to get the keys for
     * @return A set of Public Keys for the given identities
     */
    private PGPPublicKey[] getPublicKeysFor(List<Identity> identities) {
        PGPPublicKey[] ret = new PGPPublicKey[(identities.size())];
        for (int i = 0; i < identities.size(); i++) {
            ret[i] = identities.get(i).getPublicKey();
        }
        return ret;
    }

    /**
     * final String TEST_IDENTITY_2 = "user2";
     * final char[] TEST_PASSWORD_2 = "secret".toCharArray();
     * A helper method for getting all key IDs from a group of identities
     *
     * @param identities The identities to get the key IDs for
     * @return A set of Key Ids for the given identities
     */
    private long[] getPublicKeyIdsFor(List<Identity> identities) {
        if (identities != null) {
            long[] ret = new long[identities.size()];
            for (int i = 0; i < identities.size(); i++) {
                ret[i] = identities.get(i).getPublicKey().getKeyID();
            }
            return ret;
        }
        return null;
    }

    /**
     * A helper method for creating clear text test data which can be used for encryption
     *
     * @return A bunch of test data
     */
    private byte[] generateTestData() {
        return "test".getBytes();
    }

    /**
     * Checks if a string represents an ASCII-Armored PGP block
     *
     * @param data The data to check
     * @return true, if the string represents an ASCII-Armored PGP block, false otherwise
     */
    private boolean isAsciiArmored(String data) {
        final Pattern pattern = Pattern.compile("^(-----BEGIN PGP MESSAGE-----)(.*)(-----END PGP MESSAGE-----)$", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(data);
        return matcher.find();
    }

    /**
     * Encrypts the given data and removes a group of identities from the resulting PGP data before trying to decrypt the data again
     *
     * @param data The data to encrypt
     * @param encryptFor The recipients to encrypt the data for
     * @param remove The recipients which should be removed from the PGP data after encryption has been finished
     * @param decryptFor The recipients for which the data should be decrypted
     * @throws Exception
     */
    private void encryptThenRemoveThenDecrypt(byte[] data, List<Identity> encryptFor, List<Identity> remove, List<Identity> decryptFor) throws Exception {
        //Test data
        InputStream plainTextData = new ByteArrayInputStream(data);

        //Encrypting the data
        ByteArrayOutputStream encryptedData = new ByteArrayOutputStream();
        new PGPEncrypter().encrypt(plainTextData, encryptedData, this.armored, getPublicKeysFor(encryptFor));

        //Removing the 2nd recipient from the encrypted data
        ByteArrayOutputStream modifiedEncryptedData = new ByteArrayOutputStream();
        PacketProcessor processor = new PacketProcessor();
        processor.process(new ByteArrayInputStream(encryptedData.toByteArray()),
            modifiedEncryptedData,
            new RemoveRecipientPacketProcessorHandler(getPublicKeyIdsFor(remove)),
            this.armored);

        if (this.armored) {
            Assert.assertTrue("The modified encrypted data should be ASCII-Armored",
                isAsciiArmored(new String(modifiedEncryptedData.toByteArray(), StandardCharsets.UTF_8)));
        }

        //Decrypting the data for each recipient
        for (Identity encryptingIdentity : decryptFor) {
            ByteArrayOutputStream decryptedData = new ByteArrayOutputStream();
            List<PGPSignatureVerificationResult> verifyResults =
                new PGPDecrypter(keyRetrievalStrategy).decrypt(
                    new ByteArrayInputStream(modifiedEncryptedData.toByteArray()),
                    decryptedData,
                    encryptingIdentity.getIdentity(),
                    encryptingIdentity.getPassword());
            Assert.assertTrue("Verification results should be empty for non signed data", verifyResults.isEmpty());
            Assert.assertArrayEquals("Decrypted data should be equals to plaintext data", decryptedData.toByteArray(), data);
        }
    }

    private void encryptThenAddThenDecrypt(byte[] data, List<Identity> encryptFor, List<Identity> add, List<Identity> decryptFor) throws Exception {
        ByteArrayInputStream plainTextData = new ByteArrayInputStream(data);

        //Encrypting the data
        ByteArrayOutputStream encryptedData = new ByteArrayOutputStream();
        new PGPEncrypter().encrypt(plainTextData, encryptedData, this.armored, getPublicKeysFor(encryptFor));

        //Adding a new recipient to the encrypted data
        ByteArrayOutputStream modifiedEncryptedData = new ByteArrayOutputStream();
        PacketProcessor processor = new PacketProcessor();
        Identity adder = encryptFor.iterator().next();
        processor.process(new ByteArrayInputStream(encryptedData.toByteArray()),
            modifiedEncryptedData,
            new AddRecipientPacketProcessorHandler(decodePrivateKey(adder.getSecretKey(), adder.getPassword()), getPublicKeysFor(add)),
            this.armored);

        if (this.armored) {
            Assert.assertTrue("The modified encrypted data should be ASCII-Armored",
                isAsciiArmored(new String(modifiedEncryptedData.toByteArray(), StandardCharsets.UTF_8)));
        }

        //Decrypting the data for each recipient
        for (Identity encryptingIdentity : decryptFor) {
            ByteArrayOutputStream decryptedData = new ByteArrayOutputStream();
            List<PGPSignatureVerificationResult> verifyResults =
                new PGPDecrypter(keyRetrievalStrategy).decrypt(
                    new ByteArrayInputStream(modifiedEncryptedData.toByteArray()),
                    decryptedData,
                    encryptingIdentity.getIdentity(),
                    encryptingIdentity.getPassword());

            Assert.assertTrue("Verification results should be empty for non signed data", verifyResults.isEmpty());
            Assert.assertArrayEquals("Decrypted data should be equals to plaintext data", decryptedData.toByteArray(), data);
        }
    }

    /**
     * Test that removing a recipient from a PGP message does not affect the possibility for other recipients to decrypt the message
     *
     * @throws Exception
     */
    @Test
    public void testRemovingRecipientShouldNotAffectDecryptionForOtherRecipient() throws Exception {
        encryptThenRemoveThenDecrypt(generateTestData(), Arrays.asList(identity, identity2), Arrays.asList(identity2), Arrays.asList(identity));
    }

    /**
     * Tests that not removing a recipient from a PGP message does not affect the message at all
     *
     * @throws Exception
     */
    @Test
    public void testNotRemovingAnyRecipientsShouldNotAffectDecryption() throws Exception {
        encryptThenRemoveThenDecrypt(generateTestData(), Arrays.asList(identity, identity2), null, Arrays.asList(identity, identity2));
    }

    /**
     * Test that a recipient which has been removed from a PGP message is not able to decrypt the message
     *
     * @throws Exception
     */
    @Test
    public void testRemovingRecipientShouldResultInAnErrorWhenDecrypting() throws Exception {
        thrown.expect(OXException.class);
        thrown.expectMessage("The private key for the identity" /* expected error message substring */);
        encryptThenRemoveThenDecrypt(generateTestData(), Arrays.asList(identity, identity2), Arrays.asList(identity), Arrays.asList(identity));
    }

    /**
     * Tests adding a recipient to a PGP message
     *
     * @throws Exception
     */
    @Test
    public void testAddedRecipientShouldBeAbleToDecrypt() throws Exception {
        encryptThenAddThenDecrypt(generateTestData(), Arrays.asList(identity), Arrays.asList(identity2), Arrays.asList(identity, identity2));
    }
}
