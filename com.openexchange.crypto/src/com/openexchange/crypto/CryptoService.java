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

package com.openexchange.crypto;

import java.io.InputStream;
import java.security.Key;
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
    String encrypt(String data, String password) throws OXException;

    /**
     * Decrypts specified encrypted data with given password.
     *
     * @param encryptedPayload The Base64 encoded encrypted data
     * @param password The password
     * @return The decrypted data
     * @throws OXException If decryption fails
     */
    String decrypt(String encryptedPayload, String password) throws OXException;

    // --------------------------------------------------------------------------------------------------------------------------------------

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
    EncryptedData encrypt(String data, String password, boolean useSalt) throws OXException;

    /**
     * Decrypts specified encryptedt data with the given password.
     *
     * @param data EncryptedData object with the encrypted data (Base64 String) and salt
     * @param password The password
     * @param useSalt use Salt from the given EncryptedData object if true
     * @return The decrypted data as String
     * @throws OXException If decryption fails
     */
    String decrypt(EncryptedData data, String password, boolean useSalt) throws OXException;

    // --------------------------------------------------------------------------------------------------------------------------------------

    /**
     * Encrypts specified data with given password.
     *
     * @param data The data to be encrypted
     * @param key The key
     * @param useSalt Uses generated salt for encryption and stores the salt in the return value, if true uses internal salt constant
     *            otherwise.
     * @return The encrypted data as Base64 encoded string
     * @throws OXException If encryption fails
     */
    String encrypt(String data, Key key) throws OXException;

    /**
     * Decrypts specified encryptedt data with the given password.
     *
     * @param encryptedPayload The Base64 encoded encrypted data
     * @param key The key
     * @return The decrypted data as String
     * @throws OXException If decryption fails
     */
    String decrypt(String encryptedPayload, Key key) throws OXException;

    /**
     * Gets the encrypting input stream for given stream using specified key.
     *
     * @param in The stream to encrypt
     * @param key The key
     * @return The encrypting input stream
     * @throws OXException If encrypting input stream cannot be returned
     */
    InputStream encryptingStreamFor(InputStream in, Key key) throws OXException;

    /**
     * Gets the decrypting input stream for given stream using specified key.
     *
     * @param in The stream to decrypt
     * @param key The key
     * @return The decrypting input stream
     * @throws OXException If decrypting input stream cannot be returned
     */
    InputStream decryptingStreamFor(InputStream in, Key key) throws OXException;

}
