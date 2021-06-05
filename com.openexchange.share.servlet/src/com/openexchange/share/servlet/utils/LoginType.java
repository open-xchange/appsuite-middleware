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

package com.openexchange.share.servlet.utils;


/**
 * {@link LoginType} - An enumeration for known login types.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public enum LoginType {

    /**
     * The <code>"message"</code> login type.
     */
    MESSAGE("message"),
    /**
     * The <code>"message_continue"</code> login type.
     */
    MESSAGE_CONTINUE("message_continue"),
    /**
     * The <code>"reset_password"</code> login type.
     */
    RESET_PASSWORD("reset_password"),
    /**
     * The <code>"guest"</code> login type.
     */
    GUEST("guest"),
    /**
     * The <code>"guest_password"</code> login type.
     */
    GUEST_PASSWORD("guest_password"),
    /**
     * The <code>"anonymous_password"</code> login type.
     */
    ANONYMOUS_PASSWORD("anonymous_password"),


    ;

    private final String loginType;

    private LoginType(String loginType) {
        this.loginType = loginType;
    }

    /**
     * Gets the login type identifier
     *
     * @return The identifier
     */
    public String getId() {
        return loginType;
    }

    /**
     * Gets the login type for specified identifier.
     *
     * @param id The identifier to look-up
     * @return The associated login type or <code>null</code>
     */
    public static LoginType loginTypeFor(String id) {
        for (LoginType loginType : LoginType.values()) {
            if (loginType.loginType.equalsIgnoreCase(id)) {
                return loginType;
            }
        }
        return null;
    }
}
