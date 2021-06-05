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

package com.openexchange.rest.client.v2;

import org.apache.http.HttpHeaders;
import com.openexchange.java.Strings;

/**
 * {@link RESTResponseUtil}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public final class RESTResponseUtil {

    /**
     * Returns the Last-Modified value from the specified {@link RESTResponse}
     * if present
     * 
     * @param response The {@link RESTResponse}
     * @return The Last-Modified value or <code>0</code> if the header is absent or if
     *         the header's value is indeed <code>0</code>.
     */
    public static long getLastModified(RESTResponse response) {
        String value = response.getHeader(HttpHeaders.LAST_MODIFIED);
        try {
            return Strings.isEmpty(value) ? 0 : Long.parseLong(value);
        } catch (@SuppressWarnings("unused") NumberFormatException e) {
            return 0;
        }
    }
}
