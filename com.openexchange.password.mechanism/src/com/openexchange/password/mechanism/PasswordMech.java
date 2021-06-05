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

package com.openexchange.password.mechanism;

import com.openexchange.exception.OXException;

/**
 * {@link PasswordMech}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.8.0
 */
public interface PasswordMech {

    /**
     * Returns the origin string representation of the password mechanism identifier
     *
     * @return The identifier
     */
    String getIdentifier();

    /**
     * Returns if the password mechanism should be exposed or just used internally.
     * 
     * @return <code>true</code> if the password mechanism should be exposed. Otherwise <code>false</code>
     */
    boolean isExposed();

    /**
     * Encodes the given string according to this password mechanism and returns the encoded string.
     *
     * @param password The password to encode
     * @return {@link PasswordDetails} containing details about the generation result
     * @throws OXException
     */
    PasswordDetails encode(String password) throws OXException;

    /**
     * Decodes the given string according to its password mechanism and returns the decoded string.
     *
     * @param password The password to decode
     * @param salt The salt used for encoding or <code>null</code> if no salt was used while encoding
     * @return The decoded string
     * @throws OXException
     */
    String decode(String encodedPassword, byte[] salt) throws OXException;

    /**
     * Checks if given password matches the encoded string according to this password mechanism.
     *
     * @param toCheck The password to check
     * @param encoded The encoded string to check against
     * @param salt The salt used for encoding or <code>null</code> if no salt was used while encoding
     * @return <code>true</code> if string matches; otherwise <code>false</code>
     * @throws OXException
     */
    boolean check(String toCheck, String encoded, byte[] salt) throws OXException;
}
