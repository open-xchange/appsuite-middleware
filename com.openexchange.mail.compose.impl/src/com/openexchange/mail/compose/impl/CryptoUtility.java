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
import java.security.Key;
import com.openexchange.crypto.CryptoErrorMessage;
import com.openexchange.crypto.CryptoService;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;

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
     * Encrypts specified string with given key.
     *
     * @param toEncrypt The string to encrypt
     * @param key The key
     * @param cryptoService The crypto service to use
     * @return The encrypted string as Base64 encoded string
     * @throws OXException If string encryption fails
     */
    public static String encrypt(String toEncrypt, Key key, CryptoService cryptoService) throws OXException {
        if (Strings.isEmpty(toEncrypt)) {
            return toEncrypt;
        }

        return cryptoService.encrypt(toEncrypt, key);
    }

    /**
     * Decrypts specified encrypted string with given key.
     *
     * @param encryptedString The Base64 encoded encrypted string
     * @param key The key
     * @param cryptoService The crypto service to use
     * @return The decrypted string
     * @throws OXException If string decryption fails
     * @see CryptoErrorMessage#BadPassword
     */
    public static String decrypt(String encryptedString, Key key, CryptoService cryptoService) throws OXException {
        if (Strings.isEmpty(encryptedString)) {
            return encryptedString;
        }

        return cryptoService.decrypt(encryptedString, key);
    }

    /**
     * Gets the encrypting input stream for given stream using specified key.
     *
     * @param in The stream to encrypt
     * @param key The key
     * @return The encrypting input stream
     * @throws OXException If encrypting input stream cannot be returned
     */
    public static InputStream encryptingStreamFor(InputStream in, Key key, CryptoService cryptoService) throws OXException {
        if (null == in) {
            return null;
        }

        return cryptoService.encryptingStreamFor(in, key);
    }

    /**
     * Gets the decrypting input stream for given stream using specified key.
     *
     * @param in The stream to decrypt
     * @param key The key
     * @return The decrypting input stream
     * @throws OXException If decrypting input stream cannot be returned
     */
    public static InputStream decryptingStreamFor(InputStream in, Key key, CryptoService cryptoService) throws OXException {
        if (null == in) {
            return null;
        }

        return cryptoService.decryptingStreamFor(in, key);
    }

}
