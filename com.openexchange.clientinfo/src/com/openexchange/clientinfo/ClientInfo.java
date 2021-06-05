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

package com.openexchange.clientinfo;

import java.util.Locale;

/**
 * {@link ClientInfo} - Provides certain information for a client.
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public interface ClientInfo {

    /**
     * Get client info type
     *
     * @return The client info type
     */
    ClientInfoType getType();

    /**
     * Formats a string in user locale describing client info
     *
     * @param locale The user's locale
     * @return The client's description
     */
    String getDisplayName(Locale locale);

    /**
     * Gets OS family
     * @return The OS family name
     */
    String getOSFamily();

    /**
     * Gets the OS version
     * @return The OS version
     */
    String getOSVersion();

    /**
     * Gets client name
     * @return The client name
     */
    String getClientName();

    /**
     * Gets client version
     * @return The client version
     */
    String getClientVersion();

    /**
     * Gets client family
     * @return The client family
     */
    String getClientFamily();

}
