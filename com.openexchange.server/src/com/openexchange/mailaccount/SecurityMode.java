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


/**
 * {@link SecurityMode} - The security mode associated with a mail/transport account.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.6.1
 */
public enum SecurityMode {

    /**
     * SSL security mode.
     */
    SSL("ssl"),
    /**
     * STARTTLS security mode.
     */
    STARTTLS("starttls"),
    /**
     * No security mode required.
     */
    NONE_REQUIRED("none"), ;

    private final String id;

    private SecurityMode(String id) {
        this.id = id;
    }

    /**
     * Gets the identifier
     *
     * @return The identifier
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the security mode mode for given identifier.
     *
     * @param id The identifier
     * @return The security mode or <code>null</code>
     */
    public static SecurityMode mailSecureFor(String id) {
        if (null == id) {
            return null;
        }

        for (SecurityMode sm : SecurityMode.values()) {
            if (sm.id.equalsIgnoreCase(id)) {
                return sm;
            }
        }
        return null;
    }

}
