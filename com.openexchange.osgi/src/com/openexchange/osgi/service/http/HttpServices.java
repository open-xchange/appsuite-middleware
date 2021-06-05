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

package com.openexchange.osgi.service.http;

import org.osgi.service.http.HttpService;
import org.slf4j.Logger;
import com.openexchange.java.Strings;

/**
 * {@link HttpServices} - Utility class for OSGi HTTP service.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.5
 */
public class HttpServices {

    /** Simple class to delay initialization until needed */
    private static class LoggerHolder {
        static final Logger LOG = org.slf4j.LoggerFactory.getLogger(HttpServices.class);
    }

    /**
     * Initializes a new {@link HttpServices}.
     */
    private HttpServices() {
        super();
    }

    /**
     * (Safely) Unregisters a previous registration done by <code>HttpService</code>'s <code>registerServlet()</code> or
     * <code>registerResources()</code> methods.
     *
     * @param alias The name in the URI name-space of the registration to unregister
     * @param httpService The HTTP service to use
     */
    public static void unregister(String alias, HttpService httpService) {
        if (Strings.isNotEmpty(alias) && httpService != null) {
            try {
                httpService.unregister(alias);
            } catch (Exception e) {
                LoggerHolder.LOG.error("Failed to unregister HTTP servlet (or resource) associated with alias: {}", alias, e);
            }
        }
    }

}
