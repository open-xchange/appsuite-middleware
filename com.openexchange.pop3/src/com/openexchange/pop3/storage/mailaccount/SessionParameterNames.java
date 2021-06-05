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

package com.openexchange.pop3.storage.mailaccount;

/**
 * {@link SessionParameterNames} - Constants for session parameter names.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SessionParameterNames {

    /**
     * Initializes a new {@link SessionParameterNames}.
     */
    private SessionParameterNames() {
        super();
    }

    /**
     * Property name prefix for maps.
     */
    private static final String PROP_MAP = "pop3.uidlmap";

    /**
     * Gets the property name for UIDL map.
     *
     * @param accountId The account ID
     * @return The property name for UIDL map
     */
    public static String getUIDLMap(final int accountId) {
        return new StringBuilder(PROP_MAP.length() + 4).append(PROP_MAP).append(accountId).toString();
    }

    /**
     * Property name prefix for properties.
     */
    private static final String PROP_PROPS = "pop3.props";

    /**
     * Gets the property name for POP3 storage properties.
     *
     * @param accountId The account ID
     * @return The property name for POP3 storage properties
     */
    public static String getStorageProperties(final int accountId) {
        return new StringBuilder(PROP_PROPS.length() + 4).append(PROP_PROPS).append(accountId).toString();
    }

    /**
     * Property name prefix for trash container.
     */
    private static final String PROP_TRASH = "pop3.trash";

    /**
     * Gets the property name for trash container.
     *
     * @param accountId The account ID
     * @return The property name for trash container
     */
    public static String getTrashContainer(final int accountId) {
        return new StringBuilder(PROP_TRASH.length() + 4).append(PROP_TRASH).append(accountId).toString();
    }

}
