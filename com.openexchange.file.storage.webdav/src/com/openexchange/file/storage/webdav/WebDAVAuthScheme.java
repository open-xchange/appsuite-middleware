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

package com.openexchange.file.storage.webdav;

import java.util.Arrays;

/**
 * {@link WebDAVAuthScheme} contains all available auth schemes
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.5
 */
public enum WebDAVAuthScheme {
    /**
     * The basic auth scheme
     */
    BASIC("Basic"),
    /**
     * The Bearer auth scheme. Usually used for oauth
     */
    BEARER("Bearer");


    private final String schemeName;

    /**
     * Initializes a new {@link WebDAVAuthScheme}.
     *
     * @param schmeName The scheme name
     */
    private WebDAVAuthScheme(String schmeName) {
        this.schemeName = schmeName;
    }

    /**
     * Gets the {@link WebDAVAuthScheme} with the given name or {@value #BASIC} in case no scheme with the given name exists.
     *
     * @param name The auth scheme name
     * @return The {@link WebDAVAuthScheme}
     */
    public static WebDAVAuthScheme getByName(String name) {
        return Arrays.asList(WebDAVAuthScheme.values()).parallelStream().filter((scheme) -> scheme.schemeName.equalsIgnoreCase(name)).findAny().orElse(BASIC);
    }

}
