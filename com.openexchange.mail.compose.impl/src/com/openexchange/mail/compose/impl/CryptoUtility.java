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

package com.openexchange.mail.compose.impl;

import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.spec.IvParameterSpec;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.mail.MailExceptionCode;

/**
 * {@link CryptoUtility}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public class CryptoUtility {

    /**
     * Initializes a new {@link CryptoUtility}.
     */
    private CryptoUtility() {
        super();
    }

    /**
     * The algorithm.
     */
    private static final String ALGORITHM = "AES";

    /**
     * The transformation following pattern <i>"algorithm/mode/padding"</i>.
     */
    private static final String CIPHER_TYPE = ALGORITHM + "/CBC/PKCS5Padding";

    /**
     * Parameters
     */
    private static final IvParameterSpec iv = new IvParameterSpec(new byte[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15 });

    /**
     * Encrypts specified string with given key.
     *
     * @param toEncrypt The string to encrypt
     * @param key The key
     * @return The encrypted string as Base64 encoded string
     * @throws OXException If string encryption fails
     */
    public static String encrypt(String toEncrypt, Key key) throws OXException {
        if (Strings.isEmpty(toEncrypt)) {
            return toEncrypt;
        }

        try {
            Cipher cipher = Cipher.getInstance(CIPHER_TYPE);
            cipher.init(Cipher.ENCRYPT_MODE, key, iv);
            byte[] outputBytes = cipher.doFinal(toEncrypt.getBytes(com.openexchange.java.Charsets.UTF_8));
            return Base64.getEncoder().encodeToString(outputBytes);
        } catch (GeneralSecurityException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Decrypts specified encrypted string with given key.
     *
     * @param encryptedString The Base64 encoded encrypted string
     * @param key The key
     * @return The decrypted string
     * @throws GeneralSecurityException If string decryption fails
     */
    public static String decrypt(String encryptedString, Key key) throws GeneralSecurityException {
        if (Strings.isEmpty(encryptedString)) {
            return encryptedString;
        }

        byte[] encrypted = Base64.getDecoder().decode(encryptedString);
        Cipher cipher = Cipher.getInstance(CIPHER_TYPE);
        cipher.init(Cipher.DECRYPT_MODE, key, iv);
        byte[] outputBytes = cipher.doFinal(encrypted);
        return new String(outputBytes, com.openexchange.java.Charsets.UTF_8);
    }

    /**
     * Gets the encrypting input stream for given stream using specified key.
     *
     * @param in The stream to encrypt
     * @param key The key
     * @return The encrypting input stream
     * @throws OXException If encrypting input stream cannot be returned
     */
    public static CipherInputStream encryptingStreamFor(InputStream in, Key key) throws OXException {
        if (null == in) {
            return null;
        }

        try {
            Cipher cipher = Cipher.getInstance(CIPHER_TYPE);
            cipher.init(Cipher.ENCRYPT_MODE, key, iv);
            return new CipherInputStream(in, cipher);
        } catch (GeneralSecurityException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Gets the decrypting input stream for given stream using specified key.
     *
     * @param in The stream to decrypt
     * @param key The key
     * @return The decrypting input stream
     * @throws OXException If decrypting input stream cannot be returned
     */
    public static CipherInputStream decryptingStreamFor(InputStream in, Key key) throws OXException {
        if (null == in) {
            return null;
        }

        try {
            Cipher cipher = Cipher.getInstance(CIPHER_TYPE);
            cipher.init(Cipher.DECRYPT_MODE, key, iv);
            return new CipherInputStream(in, cipher);
        } catch (GeneralSecurityException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

}
