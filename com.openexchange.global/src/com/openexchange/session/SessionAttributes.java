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

package com.openexchange.session;

/**
 * {@link SessionAttributes} - Specifies certain session attributes, which should be changed for an existent session.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public interface SessionAttributes {

    /**
     * Gets the optional local IP address to set.
     *
     * @return The local IP address or an empty instance
     */
    SessionAttribute<String> getLocalIp();

    /**
     * Gets the optional client identifier to set.
     *
     * @return The client identifier or an empty instance
     */
    SessionAttribute<String> getClient();

    /**
     * Gets the optional hash identifier to set.
     *
     * @return The hash identifier or an empty instance
     */
    SessionAttribute<String> getHash();

    /**
     * Gets the optional User-Agent identifier to set.
     *
     * @return The User-Agent identifier or an empty instance
     */
    SessionAttribute<String> getUserAgent();

}
