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

package com.openexchange.rest.client.endpointpool;

import java.net.URI;
import java.util.List;
import java.util.concurrent.TimeUnit;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.annotation.SingletonService;

/**
 * {@link EndpointManagerFactory} - A factory service for new end-point managers.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
@SingletonService
public interface EndpointManagerFactory {

    /**
     * Creates a new end-point manager for the specified end-point URIs.
     *
     * @param endpointUris The end-point URIs
     * @param httpClientId The associated HTTP client
     * @param availableStrategy The strategy to decide whether an end-point is available or not
     * @param heartbeatInterval The heart-beat interval to check if a black-listed end-point URI is available again
     * @param timeUnit The time unit for the heart-beat interval
     * @return A new end-point manager
     * @throws OXException If a new end-point manager cannot be returned
     * @throws IllegalArgumentException If passed URIs are empty or invalid
     */
    EndpointManager createEndpointManagerByUris(List<URI> endpointUris, String httpClientId, EndpointAvailableStrategy availableStrategy, long heartbeatInterval, TimeUnit timeUnit) throws OXException;

    /**
     * Creates a new end-point manager for the specified end-point URIs.
     *
     * @param endpoints The end-points
     * @param httpClientId The associated HTTP client
     * @param availableStrategy The strategy to decide whether an end-point is available or not
     * @param heartbeatInterval The heart-beat interval to check if a black-listed end-point URI is available again
     * @param timeUnit The time unit for the heart-beat interval
     * @return A new end-point manager
     * @throws OXException If a new end-point manager cannot be returned
     * @throws IllegalArgumentException If passed URIs are empty or invalid
     */
    EndpointManager createEndpointManager(List<String> endpoints, String httpClientId, EndpointAvailableStrategy availableStrategy, long heartbeatInterval, TimeUnit timeUnit) throws OXException;

}
