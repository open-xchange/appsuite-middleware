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

package com.openexchange.crypto;

import com.openexchange.exception.OXException;
import com.openexchange.osgi.annotation.SingletonService;


/**
 * The Open-Xchange crypto service.
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
@SingletonService
public interface CryptoService {

    /**
     * Encrypts specified data with given password.
     *
     * @param data The data to be encrypted
     * @param password The password
     * @return The encrypted data as Base64 encoded string
     * @throws OXException If encryption fails
     */
    public String encrypt(String data, String password) throws OXException;

    /**
     * Decrypts specified encrypted data with given password.
     *
     * @param encryptedPayload The Base64 encoded encrypted data
     * @param password The password
     * @return The decrypted data
     * @throws OXException If decryption fails
     */
    public String decrypt(String encryptedPayload, String password) throws OXException;

    /**
     * Encrypts specified data with given password.
     *
     * @param data The data to be encrypted
     * @param password The password
     * @param useSalt Uses generated salt for encryption and stores the salt in the return value, if true uses internal salt constant
     *            otherwise.
     * @return EncryptedData object with the Base64 encoded and encrypted String and the used salt
     * @throws OXException If encryption fails
     */
    public EncryptedData encrypt(String data, String password, boolean useSalt) throws OXException;

    /**
     * Decrypts specified encryptedt data with the given password.
     *
     * @param data EncryptedData object with the encrypted data (Base64 String) and salt
     * @param password The password
     * @param useSalt use Salt from the given EncryptedData object if true
     * @return The decrypted data as String
     * @throws OXException If decryption fails
     */
    public String decrypt(EncryptedData data, String password, boolean useSalt) throws OXException;
}
