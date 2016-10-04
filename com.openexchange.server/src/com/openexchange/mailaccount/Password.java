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
 *    trademarks of the OX Software GmbH. group of companies.
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
