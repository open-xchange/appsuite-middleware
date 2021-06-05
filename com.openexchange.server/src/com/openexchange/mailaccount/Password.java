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

package com.openexchange.mailaccount;

import java.util.Arrays;

/**
 * {@link Password}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class Password implements AutoCloseable {

    /** The type of a password string */
    public static enum Type {
        /**
         * Signals the plain password is returned. No further encoding/encryption needed.
         */
        PLAIN,
        /**
         * Singals an encoded/encrypted password is returned that is supposed to be decoded/decrypted.
         */
        ENCRYPTED;
    }

    /**
     * Creates a new encrypted password instance.
     *
     * @param encryptedPassword The string value for encrypted password
     * @return The encrypted password instance
     */
    public static Password newEncryptedPassword(String encryptedPassword) {
        return null == encryptedPassword ? null : new Password(encryptedPassword.toCharArray(), Type.ENCRYPTED);
    }

    // --------------------------------------------------------------------------------------------------------

    private final Type type;
    private final char[] password;

    /**
     * Initializes a new {@link Password}.
     */
    public Password(char[] password, Type type) {
        super();
        this.type = type;
        this.password = password;
    }

    /**
     * Gets the type
     *
     * @return The type
     */
    public Type getType() {
        return type;
    }

    /**
     * Gets the either encoded/encrypted or plain password (dependent on advertise {@link #getType() type}).
     *
     * @return The encoded/encrypted or plain password
     */
    public char[] getPassword() {
        return clone(password);
    }

    @Override
    public void close() throws Exception {
        Arrays.fill(password, '\0');
    }

    // -------------------------------------------- Helpers ------------------------------------------------------

    private static char[] clone(char[] data) {
        if (data == null) {
            return null;
        }

        char[] copy = new char[data.length];
        System.arraycopy(data, 0, copy, 0, data.length);
        return copy;
    }

}
