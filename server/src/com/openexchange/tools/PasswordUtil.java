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

package com.openexchange.tools;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base64;

/**
 * {@link PasswordUtil} - Utility class to encrypt/decrypt passwords with a key.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class PasswordUtil {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(PasswordUtil.class);

    private static final String CIPHER_TYPE = "DES/ECB/PKCS5Padding";

    /**
     * Encrypts specified password with given key.
     * 
     * @param password The password
     * @param key The key
     * @return The encrypted password
     * @throws GeneralSecurityException If password encryption fails
     */
    public static String encrypt(final String password, final String key) throws GeneralSecurityException {
        return encrypt(password, generateSecretKey(key));
    }

    /**
     * Encrypts specified password with given key.
     * 
     * @param password The password
     * @param key The key
     * @return The encrypted password
     * @throws GeneralSecurityException If password encryption fails
     */
    public static String encrypt(final String password, final Key key) throws GeneralSecurityException {
        final Cipher cipher = Cipher.getInstance(CIPHER_TYPE);
        cipher.init(Cipher.ENCRYPT_MODE, key);

        final byte[] outputBytes = cipher.doFinal(password.getBytes());

        try {
            return new String(Base64.encodeBase64(outputBytes), "US-ASCII");
        } catch (final UnsupportedEncodingException e) {
            // Cannot occur
            LOG.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * Decrypts specified password with given key.
     * 
     * @param password The password
     * @param key The key
     * @return The decrypted password
     * @throws GeneralSecurityException If password decryption fails
     */
    public static String decrypt(final String password, final String key) throws GeneralSecurityException {
        return decrypt(password, generateSecretKey(key));
    }

    /**
     * Decrypts specified password with given key.
     * 
     * @param password The password
     * @param key The key
     * @return The decrypted password
     * @throws GeneralSecurityException If password decryption fails
     */
    public static String decrypt(final String password, final Key key) throws GeneralSecurityException {
        final byte encrypted[];
        try {
            encrypted = Base64.decodeBase64(password.getBytes("US-ASCII"));
        } catch (final UnsupportedEncodingException e) {
            // Cannot occur
            LOG.error(e.getMessage(), e);
            return null;
        }

        final Cipher cipher = Cipher.getInstance(CIPHER_TYPE);
        cipher.init(Cipher.DECRYPT_MODE, key);

        final byte[] outputBytes = cipher.doFinal(encrypted);

        return new String(outputBytes);
    }

    /**
     * Create a key for use in the cipher code
     */
    public static Key generateRandomKey() throws NoSuchAlgorithmException {
        final KeyGenerator keyGenerator = KeyGenerator.getInstance("DES");
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
        final String ks;
        final int len = key.length();
        if (len < 8) {
            final StringBuilder tmp = new StringBuilder(8).append(key);
            final int diff = 8 - len;
            for (int i = 0; i < diff; i++) {
                tmp.append('0');
            }
            ks = tmp.toString();
        } else if (len > 8) {
            ks = key.substring(0, 8);
        } else {
            ks = key;
        }
        try {
            return new SecretKeySpec(ks.getBytes("UTF-8"), "DES");
        } catch (final UnsupportedEncodingException e) {
            throw new RuntimeException("Failed to generate secret key", e);
        }
    }

    /*-
     * Encode a secret key as a string that can be stored for later use.
     * 
     * @param key
     * @return
    public static String encodeKey(final Key key) {
        final BASE64Encoder encoder = new BASE64Encoder();
        return encoder.encode(key.getEncoded());
    }
     */

    /*-
     * Reconstruct a secret key from a string representation.
     * 
     * @param encodedKey
     * @return
     * @throws IOException
    public static Key decodeKey(final String encodedKey) throws IOException {
        final BASE64Decoder decoder = new BASE64Decoder();
        final byte raw[] = decoder.decodeBuffer(encodedKey);
        final SecretKey key = new SecretKeySpec(raw, "DES");
        return key;
    }
     */
}
