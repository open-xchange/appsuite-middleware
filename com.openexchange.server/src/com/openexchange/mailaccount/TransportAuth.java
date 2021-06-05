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
 * {@link TransportAuth}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.6.1
 */
public enum TransportAuth {

    /**
     * The transport server credentials are supposed to be taken from the mail settings.
     */
    MAIL("mail"),
    /**
     * Custom transport server credentials.
     */
    CUSTOM("custom"),
    /**
     * No transport server credentials.
     */
    NONE("none"), ;

    private final String id;

    private TransportAuth(String id) {
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
     * Gets the transport authentication mode for given identifier.
     *
     * @param id The identifier
     * @return The transport authentication mode or <code>null</code>
     */
    public static TransportAuth transportAuthFor(String id) {
        if (null == id) {
            return null;
        }

        for (TransportAuth ta : TransportAuth.values()) {
            if (ta.id.equalsIgnoreCase(id)) {
                return ta;
            }
        }
        return null;
    }

    /**
     * Whether to consider specified transport auth as mail-backed authentication.
     *
     * @param transportAuth The transport auth to check
     * @return <code>true</code> for mail-backed authentication; otherwise <code>false</code>
     */
    public static boolean considerAsMailTransportAuth(TransportAuth transportAuth) {
        return MAIL == transportAuth || NONE == transportAuth;
    }

    @Override
    public String toString() {
        return id;
    }

}
