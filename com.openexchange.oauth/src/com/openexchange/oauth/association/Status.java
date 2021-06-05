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

package com.openexchange.oauth.association;

/**
 * {@link Status}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.4
 */
public enum Status {

    /**
     * The "OK" status. All fine.
     */
    OK("ok"),
    /**
     * Referenced account currently carries invalid credentials and is therefore unable to connect. Credentials are supposed to be corrected through a re-authorization by user.
     */
    INVALID_GRANT("invalid_grant"),
    /**
     * Account is broken and needs to be re-created
     */
    RECREATION_NEEDED("recreation_needed"),
    /**
     * Account is disabled.
     */
    DISABLED("disabled"),

    ;

    private final String id;

    private Status(String id) {
        this.id = id;
    }

    /**
     * Gets the status identifier
     *
     * @return The status identifier
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the status for given identifier
     *
     * @param identifier The status' identifier
     * @return The status or <code>null</code>
     */
    public static Status statusFor(String identifier) {
        if (null == identifier) {
            return null;
        }
        for (Status s : Status.values()) {
            if (identifier.equalsIgnoreCase(s.id)) {
                return s;
            }
        }
        return null;
    }

}
