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

package com.openexchange.dovecot.doveadm.client.internal;

import com.openexchange.rest.client.endpointpool.Endpoint;

/**
 * {@link EndpointAndClientId} - A pair of end-point and HTTP client identifier.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.4
 */
public class EndpointAndClientId {

    private final Endpoint endpoint;
    private final String httpClientId;

    /**
     * Initializes a new {@link EndpointAndClientId}.
     *
     * @param endpoint The end-point
     * @param httpClientId The client identifier
     */
    public EndpointAndClientId(Endpoint endpoint, String httpClientId) {
        super();
        this.endpoint = endpoint;
        this.httpClientId = httpClientId;
    }

    /**
     * Gets the end-point.
     *
     * @return The end-point
     */
    public Endpoint getEndpoint() {
        return endpoint;
    }

    /**
     * Gets the HTTP client identifier.
     *
     * @return The client identifier
     */
    public String getHttpClientId() {
        return httpClientId;
    }

}
