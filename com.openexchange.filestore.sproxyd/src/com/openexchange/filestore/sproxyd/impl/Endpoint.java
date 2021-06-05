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

package com.openexchange.filestore.sproxyd.impl;

import java.util.UUID;
import com.openexchange.java.util.UUIDs;

/**
 * Represents a sproxyd endpoint.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class Endpoint {

    private final String baseUrl;
    private final String prefix;

    /**
     * Initializes a new {@link Endpoint}.
     *
     * @param baseUrl The base URL including the namespace for OX files, e.g. <code>http://ring12.example.com/proxy/ox/</code>;
     *                must always end with a trailing slash
     * @param prefix The prefix
     */
    public Endpoint(String baseUrl, String prefix) {
        super();
        this.baseUrl = baseUrl;
        this.prefix = prefix;
    }

    /**
     * Gets the URL for the according context or user store, e.g. <code>http://ring12.example.com/proxy/ox/1337/0/</code>.
     *
     * @return The URL; always with trailing slash
     */
    public String getFullUrl() {
        return baseUrl + prefix + '/';
    }

    /**
     * Gets the URL for the given object, e.g. <code>http://ring12.example.com/proxy/ox/1337/0/411615f4a607432fa2e12cc18b8c5f9c</code>.
     *
     * @return The URL; always without trailing slash
     */
    public String getObjectUrl(UUID id) {
        return getFullUrl() + UUIDs.getUnformattedString(id);
    }

    /**
     * gets the base URL without the context or user specific sub-path , e.g. <code>http://ring12.example.com/proxy/ox/</code>.
     *
     * @return The URL; always with trailing slash
     */
    public String getBaseUrl() {
        return baseUrl;
    }

    @Override
    public String toString() {
        return getFullUrl();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((baseUrl == null) ? 0 : baseUrl.hashCode());
        result = prime * result + ((prefix == null) ? 0 : prefix.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Endpoint other = (Endpoint) obj;
        if (baseUrl == null) {
            if (other.baseUrl != null) {
                return false;
            }
        } else if (!baseUrl.equals(other.baseUrl)) {
            return false;
        }
        if (prefix == null) {
            if (other.prefix != null) {
                return false;
            }
        } else if (!prefix.equals(other.prefix)) {
            return false;
        }
        return true;
    }

}
