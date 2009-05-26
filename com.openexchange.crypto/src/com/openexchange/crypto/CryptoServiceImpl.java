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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.crypto;

import static com.openexchange.crypto.CryptoErrorMessage.BadPassword;
import static com.openexchange.crypto.CryptoErrorMessage.EncodingException;
import static com.openexchange.crypto.CryptoErrorMessage.SecurityException;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base64;

/**
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class CryptoServiceImpl implements CryptoService {

    /**
     * The DES algorithm.
     */
    private final String ALGORITHM = "AES";

    /**
     * The mode.
     */
    private final String MODE = "CBC";

    /**
     * The padding.
     */
    private final String PADDING = "PKCS5Padding";

    /**
     * The transformation following pattern <i>"algorithm/mode/padding"</i>.
     */
    private final String CIPHER_TYPE = ALGORITHM + "/" + MODE + "/" + PADDING;

    /**
     * Parameters
     */
    private static IvParameterSpec iv = new IvParameterSpec(new byte[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15 });

    /**
     * Encrypts specified payload with given password.
     * 
     * @param payload The payload to be encrypted
     * @param password The password
     * @return The encrypted payload as Base64 encoded string
     * @throws GeneralSecurityException If encryption fails
     */
    public String encrypt(String payload, String password) throws CryptoException {
        return encrypt(payload, generateSecretKey(password));
    }

    /**
     * Decrypts specified encrypted payload with given password.
     * 
     * @param encryptedPayload The Base64 encoded encrypted payload
     * @param password The password
     * @return The decrypted payload
     * @throws GeneralSecurityException If decryption fails
     */
    public String decrypt(String encryptedPayload, String password) throws CryptoException {
        return decrypt(encryptedPayload, generateSecretKey(password));
    }

    /**
     * Encrypts specified payload with given key.
     * 
     * @param payload The payload to encrypt
     * @param password The password
     * @return The encrypted payload as Base64 encoded string
     * @throws CryptoException
     */
    private String encrypt(final String payload, final Key password) throws CryptoException {
        String retval = null;
        try {
            final Cipher cipher = Cipher.getInstance(CIPHER_TYPE);
            cipher.init(Cipher.ENCRYPT_MODE, password, iv);
            final byte[] outputBytes = cipher.doFinal(payload.getBytes("UTF-8"));
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
            retval = new String(Base64.encodeBase64(outputBytes), "US-ASCII");
        } catch (final UnsupportedEncodingException e) {
            throw EncodingException.create(e);
        } catch (GeneralSecurityException e) {
            throw SecurityException.create(e);
        }
        
        return retval;
    }

    /**
     * Decrypts specified encrypted payload with given Key.
     * 
     * @param encryptedPayload The Base64 encoded encrypted payload
     * @param key The Key
     * @return The decrypted payload
     * @throws CryptoException 
     */
    private String decrypt(final String encryptedPayload, final Key key) throws CryptoException {
        String retval = null;
        Cipher cipher = null;
        final byte encrypted[];
        try {
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
            encrypted = Base64.decodeBase64(encryptedPayload.getBytes("US-ASCII"));

            cipher = Cipher.getInstance(CIPHER_TYPE);
            cipher.init(Cipher.DECRYPT_MODE, key, iv);
        } catch (final UnsupportedEncodingException e) {
            throw EncodingException.create(e);
        } catch (GeneralSecurityException e) {
            throw SecurityException.create(e);
        }
        
        try {
            final byte[] outputBytes = cipher.doFinal(encrypted);
            retval = new String(outputBytes, "UTF-8");
        } catch (final UnsupportedEncodingException e) {
            throw EncodingException.create(e);
        } catch (GeneralSecurityException e) {
            throw BadPassword.create(e);
        }
        
        return retval;
    }

    private final int KEY_LENGTH = 16;

    /**
     * Generates a secret key from specified password string.
     * 
     * @param password The password string
     * @return A secret key generated from specified password string
     * @throws CryptoException 
     */
    private Key generateSecretKey(final String password) throws CryptoException {
        try {
            return new SecretKeySpec(ensureLength(password.getBytes("UTF-8")), ALGORITHM);
        } catch (final UnsupportedEncodingException e) {
            throw EncodingException.create(e);
        }
    }

    private byte[] ensureLength(final byte[] bytes) {
        final byte[] keyBytes;
        final int len = bytes.length;
        if (len < KEY_LENGTH) {
            keyBytes = new byte[KEY_LENGTH];
            System.arraycopy(bytes, 0, keyBytes, 0, len);
            for (int i = len; i < keyBytes.length; i++) {
                keyBytes[i] = 48;
            }
        } else if (len > KEY_LENGTH) {
            keyBytes = new byte[8];
            System.arraycopy(bytes, 0, keyBytes, 0, keyBytes.length);
        } else {
            keyBytes = bytes;
        }
        return keyBytes;
    }

}
