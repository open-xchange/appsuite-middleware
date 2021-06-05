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

package com.openexchange.pns.transport.websocket.internal;

/**
 * {@link ClientAndPathFilter} - A pair of client identifier and path filter expression.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class ClientAndPathFilter {

    private final String client;
    private final String pathFilter;

    /**
     * Initializes a new {@link ClientAndPathFilter}.
     */
    public ClientAndPathFilter(String client, String pathFilter) {
        super();
        this.client = client;
        this.pathFilter = pathFilter;
    }

    /**
     * Gets the client identifier
     *
     * @return The client identifier
     */
    public String getClient() {
        return client;
    }

    /**
     * Gets the applicable path filter expression
     *
     * @return The path filter expression
     */
    public String getPathFilter() {
        return pathFilter;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((client == null) ? 0 : client.hashCode());
        result = prime * result + ((pathFilter == null) ? 0 : pathFilter.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ClientAndPathFilter)) {
            return false;
        }
        ClientAndPathFilter other = (ClientAndPathFilter) obj;
        if (client == null) {
            if (other.client != null) {
                return false;
            }
        } else if (!client.equals(other.client)) {
            return false;
        }
        if (pathFilter == null) {
            if (other.pathFilter != null) {
                return false;
            }
        } else if (!pathFilter.equals(other.pathFilter)) {
            return false;
        }
        return true;
    }

}
