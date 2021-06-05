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

package com.openexchange.chronos.provider.ical.auth;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.exception.OXException;
import com.openexchange.java.Charsets;

/**
 * {@link PasswordUtil} - Utility class to encrypt/decrypt passwords with a key aka <b>p</b>assword <b>b</b>ased <b>e</b>ncryption
 * (PBE).
 * <p>
 * PBE is a form of symmetric encryption where the same key or password is used to encrypt and decrypt a string.
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.0
 */
public final class PasswordUtil {

    /** The logger constant */
    static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(PasswordUtil.class);

    /** The key length. */
    private static final int KEY_LENGTH = 8;

    /** The DES algorithm. */
    private static final String ALGORITHM_DES = "DES";

    /** The transformation following pattern <i>"algorithm/mode/padding"</i>. */
    private static final String CIPHER_TYPE = ALGORITHM_DES + "/ECB/PKCS5Padding";

    /**
     * Encrypts specified password with given key.
     *
     * @param password The password
     * @param key The key
     * @return The encrypted password as Base64 encoded string
     * @throws OXException If password encryption fails
     */
    public static String encrypt(final String password, final String key) throws OXException {
        return encrypt(password, generateSecretKey(key));
    }

    /**
     * Decrypts specified encrypted password with given key.
     *
     * @param encryptedPassword The Base64 encoded encrypted password
     * @param key The key
     * @return The decrypted password
     * @throws OXException If password decryption fails
     */
    public static String decrypt(final String encryptedPassword, final String key) throws OXException {
        return decrypt(encryptedPassword, generateSecretKey(key));
    }

    /**
     * Encrypts specified password with given key.
     *
     * @param password The password to encrypt
     * @param key The key
     * @return The encrypted password as Base64 encoded string
     * @throws OXException If password encryption fails
     */
    public static String encrypt(final String password, final Key key) throws OXException {
        if (null == password || null == key) {
            return null;
        }
        try {
            Cipher cipher = Cipher.getInstance(CIPHER_TYPE);
            cipher.init(Cipher.ENCRYPT_MODE, key);

            final byte[] outputBytes = cipher.doFinal(password.getBytes(com.openexchange.java.Charsets.UTF_8));
            /*-
             * It's safe to use "US-ASCII" to turn bytes into a Base64 encoded encrypted password string.
             * Taken from RFC 2045 Section 6.8. "Base64 Content-Transfer-Encoding":
             *
             * A 65-character subset of US-ASCII is used, enabling 6 bits to be
             * represented per printable character. (The extra 65th character, "=",
             * is used to signify a special processing function.)
             *
             * NOTE: This subset has the important property that it is represented
             * identically in all versions of ISO 646, including US-ASCII, and all
             * characters in the subset are also represented identically in all
             * versions of EBCDIC. Other popular encodings, such as the encoding
             * used by the uuencode utility, Macintosh binhex 4.0 [RFC-1741], and
             * the base85 encoding specified as part of Level 2 PostScript, do not
             * share these properties, and thus do not fulfill the portability
             * requirements a binary transport encoding for mail must meet.
             *
             */
            return Charsets.toAsciiString(org.apache.commons.codec.binary.Base64.encodeBase64(outputBytes));
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            LOG.error("Error while encrypting password: {}", e.getMessage(), e);
            throw CalendarExceptionCodes.UNEXPECTED_ERROR.create(e.getMessage());
        }
    }

    /**
     * Decrypts specified encrypted password with given key.
     *
     * @param encryptedPassword The Base64 encoded encrypted password
     * @param key The key
     * @return The decrypted password
     * @throws OXException If password decryption fails
     */
    public static String decrypt(final String encryptedPassword, final Key key) throws OXException {
        if (null == encryptedPassword || null == key) {
            return null;
        }
        final byte encrypted[];
        {
            /*-
             * It's safe to use "US-ASCII" to turn Base64 encoded encrypted password string into bytes.
             * Taken from RFC 2045 Section 6.8. "Base64 Content-Transfer-Encoding":
             *
             * A 65-character subset of US-ASCII is used, enabling 6 bits to be
             * represented per printable character. (The extra 65th character, "=",
             * is used to signify a special processing function.)
             *
             * NOTE: This subset has the important property that it is represented
             * identically in all versions of ISO 646, including US-ASCII, and all
             * characters in the subset are also represented identically in all
             * versions of EBCDIC. Other popular encodings, such as the encoding
             * used by the uuencode utility, Macintosh binhex 4.0 [RFC-1741], and
             * the base85 encoding specified as part of Level 2 PostScript, do not
             * share these properties, and thus do not fulfill the portability
             * requirements a binary transport encoding for mail must meet.
             *
             */
            encrypted = org.apache.commons.codec.binary.Base64.decodeBase64(Charsets.toAsciiBytes(encryptedPassword));
        }

        Cipher cipher;
        try {
            cipher = Cipher.getInstance(CIPHER_TYPE);
            cipher.init(Cipher.DECRYPT_MODE, key);

            final byte[] outputBytes = cipher.doFinal(encrypted);

            return new String(outputBytes, com.openexchange.java.Charsets.UTF_8);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            LOG.error("Error while decrypting password: {}", e.getMessage(), e);
            throw CalendarExceptionCodes.UNEXPECTED_ERROR.create(e.getMessage());
        }
    }

    /**
     * Create a key for use in the cipher code
     */
    public static Key generateRandomKey() throws NoSuchAlgorithmException {
        final KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM_DES);
        keyGenerator.init(new SecureRandom());
        final SecretKey secretKey = keyGenerator.generateKey();
        return secretKey;
    }

    /**
     * Generates a secret key from specified key string.
     *
     * @param key The key string
     * @return A secret key generated from specified key string
     */
    public static Key generateSecretKey(final String key) {
        if (null == key) {
            return null;
        }
        return new SecretKeySpec(ensureLength(key.getBytes(com.openexchange.java.Charsets.UTF_8)), ALGORITHM_DES);
    }

    /**
     * Generates a secret key from specified bytes.
     *
     * @param bytes The bytes
     * @return A secret key generated from specified bytes
     */
    public static Key generateSecretKey(final byte[] bytes) {
        if (null == bytes) {
            return null;
        }
        return new SecretKeySpec(ensureLength(bytes), ALGORITHM_DES);
    }

    private static byte[] ensureLength(final byte[] bytes) {
        final byte[] keyBytes;
        final int len = bytes.length;
        if (len < KEY_LENGTH) {
            keyBytes = new byte[KEY_LENGTH];
            System.arraycopy(bytes, 0, keyBytes, 0, len);
            for (int i = len; i < keyBytes.length; i++) {
                keyBytes[i] = 48;
            }
        } else if (len > KEY_LENGTH) {
            keyBytes = new byte[KEY_LENGTH];
            System.arraycopy(bytes, 0, keyBytes, 0, keyBytes.length);
        } else {
            keyBytes = bytes;
        }
        return keyBytes;
    }
}
