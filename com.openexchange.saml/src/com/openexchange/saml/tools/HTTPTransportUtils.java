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

package com.openexchange.saml.tools;

/**
 * 
 * {@link HTTPTransportUtils}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.4
 */
public class HTTPTransportUtils {

    /**
     * Get the first raw (i.e. non URL-decoded) query string component with the specified parameter name.
     * 
     * The component will be returned as a string in the form 'paramName=paramValue' (minus the quotes).
     * 
     * @param queryString the raw HTTP URL query string
     * @param paramName the name of the parameter to find
     * @return the found component, or null if not found
     */
    public static String getRawQueryStringParameter(String queryString, String paramName) {
        if (queryString == null) {
            return null;
        }

        String paramPrefix = paramName + "=";
        int start = queryString.indexOf(paramPrefix);
        if (start == -1) {
            return null;
        }

        int end = queryString.indexOf('&', start);
        if (end == -1) {
            return queryString.substring(start);
        }
        return queryString.substring(start, end);
    }
}
