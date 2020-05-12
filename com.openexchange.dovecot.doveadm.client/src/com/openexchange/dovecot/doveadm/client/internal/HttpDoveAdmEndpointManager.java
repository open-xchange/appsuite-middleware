/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.dovecot.doveadm.client.internal;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import com.openexchange.config.ConfigurationService;
import com.openexchange.dovecot.doveadm.client.DoveAdmClientExceptionCodes;
import com.openexchange.dovecot.doveadm.client.http.DoveAdmHttpClientConfig;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.rest.client.endpointpool.Endpoint;
import com.openexchange.rest.client.endpointpool.EndpointAvailableStrategy;
import com.openexchange.rest.client.endpointpool.EndpointManager;
import com.openexchange.rest.client.endpointpool.EndpointManagerFactory;

/**
 * {@link HttpDoveAdmEndpointManager}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v1.0.0
 */
public class HttpDoveAdmEndpointManager {

    public static final String DOVEADM_ENDPOINTS = "com.openexchange.dovecot.doveadm.endpoints";

    // -------------------------------------------------------------------------------------------------------------------------------

    private final AtomicReference<EnumMap<HttpDoveAdmCall, ClientIdAndEndpointManager>> endpointsReference;

    /**
     * Initializes a new {@link HttpDoveAdmEndpointManager}.
     */
    public HttpDoveAdmEndpointManager() {
        super();
        endpointsReference = new AtomicReference<EnumMap<HttpDoveAdmCall, ClientIdAndEndpointManager>>(null);
    }

    /**
     * Initializes this instance.
     *
     * @param factory The end-point factory to use
     * @param availableStrategy The strategy to use for checking re-accessible end-points
     * @param configService The configuration service to read properties from
     * @return <code>true</code> if at least one valid end-point is specified; otherwise <code>false</code>
     * @throws OXException If initialization fails
     */
    public boolean init(EndpointManagerFactory factory, EndpointAvailableStrategy availableStrategy, ConfigurationService configService) throws OXException {
        org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(HttpDoveAdmEndpointManager.class);

        EnumMap<HttpDoveAdmCall, ClientIdAndEndpointManager> endpoints = new EnumMap<HttpDoveAdmCall, ClientIdAndEndpointManager>(HttpDoveAdmCall.class);

        String fallBackName = DOVEADM_ENDPOINTS;
        EndpointManager fallBackEntry = null;
        for (HttpDoveAdmCall call : HttpDoveAdmCall.values()) {
            String propName = "com.openexchange.dovecot.doveadm.endpoints." + call.getName();
            String endPoints = configService.getProperty(propName);
            if (Strings.isEmpty(endPoints)) {
                if (null == fallBackEntry) {
                    endPoints = configService.getProperty(fallBackName);
                    if (Strings.isEmpty(endPoints)) {
                        // No end-point
                        logger.info("No Dovecot DoceAdm REST interface end-points defined via property {}", propName);
                        return false;
                    }

                    fallBackEntry = getEndpointManager(endPoints, "doveadm", factory, availableStrategy, new StringBuilder(fallBackName.length() + 1).append(fallBackName).append('.'), configService);
                }
                endpoints.put(call, new ClientIdAndEndpointManager("doveadm", fallBackEntry));
            } else {
                String httpClientId = DoveAdmHttpClientConfig.getClientId(call);
                endpoints.put(call, new ClientIdAndEndpointManager(httpClientId, getEndpointManager(endPoints, httpClientId, factory, availableStrategy, new StringBuilder(propName.length() + 1).append(propName).append('.'), configService)));
            }
        }

        endpointsReference.set(endpoints);
        return true;
    }

    private static EndpointManager getEndpointManager(String endPoints, String httpClientId, EndpointManagerFactory factory, EndpointAvailableStrategy availableStrategy, StringBuilder propPrefix, ConfigurationService configService) throws OXException {
        // Parse end-point list
        List<String> l = Arrays.asList(Strings.splitByComma(endPoints.trim()));

        // Read property for heartbeat interval
        int checkInterval = configService.getIntProperty(propPrefix.append("checkInterval").toString(), 60000);

        // Setup end-point manager for the listing
        EndpointManager endpointManager = factory.createEndpointManager(l, httpClientId, availableStrategy, checkInterval, TimeUnit.MILLISECONDS);

        // Return end-point manager
        return endpointManager;
    }

    /**
     * Shuts-down this instance.
     */
    public void shutDown() {
        endpointsReference.getAndSet(null);
    }

    /**
     * Black-lists specified end-point for given call (only if there other alternative ones).
     *
     * @param call The call
     * @param endpoint The end-point to black-list
     * @return <code>true</code> if end-point has been added to black-list; otherwise <code>false</code>
     */
    public boolean blacklist(HttpDoveAdmCall call, Endpoint endpoint) {
        if (null == call) {
            return false;
        }

        EnumMap<HttpDoveAdmCall, ClientIdAndEndpointManager> endpoints = endpointsReference.get();
        if (null == endpoints) {
            return false;
        }

        ClientIdAndEndpointManager c = endpoints.get(call);
        if (null == c) {
            return false;
        }

        if (c.endpointManager.getNumberOfEndpoints() <= 1) {
            return false;
        }

        c.endpointManager.blacklist(endpoint);
        return true;
    }

    /**
     * Gets the HTTP client and (next available) base URI for specified call.
     *
     * @param call The call that is about to be invoked
     * @return The base URI
     * @throws OXException If HTTP client and base URI cannot be returned
     */
    public EndpointAndClientId getEndpoint(HttpDoveAdmCall call) throws OXException {
        if (null == call) {
            throw DoveAdmClientExceptionCodes.UNKNOWN_CALL.create("null");
        }

        EnumMap<HttpDoveAdmCall, ClientIdAndEndpointManager> endpoints = endpointsReference.get();
        if (null == endpoints) {
            throw OXException.general("DoveAdm client not initialized.");
        }

        ClientIdAndEndpointManager c = endpoints.get(call);
        if (null == c) {
            throw DoveAdmClientExceptionCodes.UNKNOWN_CALL.create(call.toString());
        }


        Endpoint endpoint = c.endpointManager.get();
        if (null == endpoint) {
            if (HttpDoveAdmCall.DEFAULT.equals(call)) {
                throw DoveAdmClientExceptionCodes.DOVEADM_NOT_REACHABLE_GENERIC.create();
            }
            throw DoveAdmClientExceptionCodes.DOVEADM_NOT_REACHABLE.create(call.toString());
        }

        return new EndpointAndClientId(endpoint, c.httpClientId);
    }

    private static class ClientIdAndEndpointManager {

        final EndpointManager endpointManager;
        final String httpClientId;

        ClientIdAndEndpointManager(String httpClientId, EndpointManager endpointManager) {
            super();
            this.httpClientId = httpClientId;
            this.endpointManager = endpointManager;
        }
    }

}
