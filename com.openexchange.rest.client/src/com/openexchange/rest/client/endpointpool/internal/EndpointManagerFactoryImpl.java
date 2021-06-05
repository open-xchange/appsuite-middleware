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

package com.openexchange.rest.client.endpointpool.internal;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import com.openexchange.exception.OXException;
import com.openexchange.rest.client.endpointpool.EndpointAvailableStrategy;
import com.openexchange.rest.client.endpointpool.EndpointManager;
import com.openexchange.rest.client.endpointpool.EndpointManagerFactory;
import com.openexchange.rest.client.httpclient.HttpClientService;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.timer.TimerService;


/**
 * {@link EndpointManagerFactoryImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class EndpointManagerFactoryImpl implements EndpointManagerFactory {

    private final HttpClientService httpClientService;
    private final ServiceLookup services;

    /**
     * Initializes a new {@link EndpointManagerFactoryImpl}.
     *
     * @param httpClientService The HTTP client service to use
     * @param services The service look-up to obtain needed OSGi services
     */
    public EndpointManagerFactoryImpl(HttpClientService httpClientService, ServiceLookup services) {
        super();
        this.httpClientService = httpClientService;
        this.services = services;
    }

    @Override
    public EndpointManager createEndpointManagerByUris(List<URI> endpointUris, String httpClientId, EndpointAvailableStrategy availableStrategy, long heartbeatInterval, TimeUnit timeUnit) throws OXException {
        TimerService timerService = services.getOptionalService(TimerService.class);
        if (null == timerService) {
            throw ServiceExceptionCode.absentService(TimerService.class);
        }

        return new EndpointManagerImpl(endpointUris, httpClientId, availableStrategy, timeUnit.toMillis(heartbeatInterval), timerService, httpClientService);
    }

    @Override
    public EndpointManager createEndpointManager(List<String> endpoints, String httpClientId, EndpointAvailableStrategy availableStrategy, long heartbeatInterval, TimeUnit timeUnit) throws OXException {
        TimerService timerService = services.getOptionalService(TimerService.class);
        if (null == timerService) {
            throw ServiceExceptionCode.absentService(TimerService.class);
        }

        if (null == endpoints) {
            throw new IllegalArgumentException("End-points must not be null");
        }

        int size = endpoints.size();
        if (size == 0) {
            throw new IllegalArgumentException("End-points must not be empty");
        }

        try {
            List<URI> endpointUris = new ArrayList<URI>(size);
            for (String sEndpoint : endpoints) {
                endpointUris.add(new URI(sEndpoint.endsWith("/") ? sEndpoint.substring(0, sEndpoint.length() - 1) : sEndpoint));
            }

            return new EndpointManagerImpl(endpointUris, httpClientId, availableStrategy, timeUnit.toMillis(heartbeatInterval), timerService, httpClientService);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid URIs", e);
        }
    }

}
