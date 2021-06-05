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

package com.openexchange.secret;

import com.openexchange.exception.OXException;
import com.openexchange.session.Session;


/**
 * {@link SecretEncryptionService} - The secret encryption/decryption service.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface SecretEncryptionService<T> {

    /**
     * Encrypts specified string using given session data.
     * <p>
     * The caller is required to have a special treatment for the {@link SecretExceptionCodes#EMPTY_SECRET} error code, which hints either
     * to a setup error or to a session providing insufficient data; e.g. missing password in single-sign on scenarios.
     *
     * @param session The session providing data
     * @param toEncrypt The string to encrypt
     * @return The encrypted string
     * @throws OXException If encryption fails
     * @see SecretExceptionCodes#EMPTY_SECRET
     */
    String encrypt(Session session, String toEncrypt) throws OXException;

    /**
     * Decrypts specified string using given session data.
     * <p>
     * The caller is required to have a special treatment for the {@link SecretExceptionCodes#EMPTY_SECRET} error code, which hints either
     * to a setup error or to a session providing insufficient data; e.g. missing password in single-sign on scenarios.
     *
     * @param session The session providing data
     * @param toDecrypt The string to decrypt
     * @return The decrypted string
     * @throws OXException If decryption fails
     * @see SecretExceptionCodes#EMPTY_SECRET
     */
    String decrypt(Session session, String toDecrypt) throws OXException;

    /**
     * Decrypts specified string using given session data.
     * <p>
     * The caller is required to have a special treatment for the {@link SecretExceptionCodes#EMPTY_SECRET} error code, which hints either
     * to a setup error or to a session providing insufficient data; e.g. missing password in single-sign on scenarios.
     *
     * @param session The session providing data
     * @param toDecrypt The string to decrypt
     * @param customizationNote The optional customization note
     * @return The decrypted string
     * @throws OXException If decryption fails
     * @see SecretExceptionCodes#EMPTY_SECRET
     */
    String decrypt(Session session, String toDecrypt, T customizationNote) throws OXException;

}
