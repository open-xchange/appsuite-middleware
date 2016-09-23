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

import static com.openexchange.dovecot.doveadm.client.internal.HttpDoveAdmClient.close;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScheme;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthState;
import org.apache.http.auth.Credentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;
import com.openexchange.config.ConfigurationService;
import com.openexchange.dovecot.doveadm.client.DoveAdmClientExceptionCodes;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.rest.client.endpointpool.Endpoint;
import com.openexchange.rest.client.endpointpool.EndpointAvailableStrategy;
import com.openexchange.rest.client.endpointpool.EndpointManager;
import com.openexchange.rest.client.endpointpool.EndpointManagerFactory;
import com.openexchange.rest.client.httpclient.HttpClients;
import com.openexchange.rest.client.httpclient.HttpClients.ClientConfig;
import com.openexchange.version.Version;

/**
 * {@link HttpDoveAdmEndpointManager}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v1.0.0
 */
public class HttpDoveAdmEndpointManager {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(HttpDoveAdmEndpointManager.class);

    static class PreemptiveAuth implements HttpRequestInterceptor {

        @Override
        public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {

            final AuthState authState = (AuthState) context.getAttribute(ClientContext.TARGET_AUTH_STATE);

            // If no auth scheme avaialble yet, try to initialize it preemptively
            if (authState.getAuthScheme() == null) {
                final AuthScheme authScheme = (AuthScheme) context.getAttribute("preemptive-auth");
                final CredentialsProvider credsProvider = (CredentialsProvider) context.getAttribute(ClientContext.CREDS_PROVIDER);
                final HttpHost targetHost = (HttpHost) context.getAttribute(ExecutionContext.HTTP_TARGET_HOST);
                if (authScheme != null) {
                    final Credentials creds = credsProvider.getCredentials(new AuthScope(targetHost.getHostName(), targetHost.getPort()));
                    if (creds == null) {
                        throw new HttpException("No credentials for preemptive authentication");
                    }
                    authState.setAuthScheme(authScheme);
                    authState.setCredentials(creds);
                }
            }
        }
    }

    private static class EndpointListing {
        final DefaultHttpClient httpClient;
        final EndpointManager endpointManager;

        EndpointListing(DefaultHttpClient httpClient, EndpointManager endpointManager) {
            super();
            this.httpClient = httpClient;
            this.endpointManager = endpointManager;
        }
    }

    private static DefaultHttpClient newHttpClient(int totalConnections, int maxConnectionsPerRoute, int readTimeout, int connectTimeout) {
        ClientConfig clientConfig = ClientConfig.newInstance()
            .setUserAgent("OX Dovecot Http Client v" + Version.getInstance().getVersionString())
            .setMaxTotalConnections(totalConnections)
            .setMaxConnectionsPerRoute(maxConnectionsPerRoute)
            .setConnectionTimeout(connectTimeout)
            .setSocketReadTimeout(readTimeout);

        return HttpClients.getHttpClient(clientConfig);
    }

    private static EndpointListing listingFor(String endPoints, EndpointManagerFactory factory, StringBuilder propPrefix, ConfigurationService configService) throws OXException {
        // Parse end-point list
        List<String> l = Arrays.asList(Strings.splitByComma(endPoints.trim()));

        // Read properties for HTTP connections/pooling
        int totalConnections = configService.getIntProperty(propPrefix.append("totalConnections").toString(), 100);
        int maxConnectionsPerRoute = configService.getIntProperty(propPrefix.append("maxConnectionsPerRoute").toString(), 0);
        if (maxConnectionsPerRoute <= 0) {
            maxConnectionsPerRoute = totalConnections / l.size();
        }
        int readTimeout = configService.getIntProperty(propPrefix.append("readTimeout").toString(), 2500);
        int connectTimeout = configService.getIntProperty(propPrefix.append("connectTimeout").toString(), 1500);

        // Initialize HTTP client for the listing
        DefaultHttpClient httpClient = newHttpClient(totalConnections, maxConnectionsPerRoute, readTimeout, connectTimeout);

        // Setup end-point manager for the listing
        EndpointManager endpointManager = factory.createEndpointManager(l, httpClient, AVAILABILITY_STRATEGY, 60, TimeUnit.SECONDS);

        // Return listing for bundled HTTP client & end-point manager
        return new EndpointListing(httpClient, endpointManager);
    }

    private static final EndpointAvailableStrategy AVAILABILITY_STRATEGY = new EndpointAvailableStrategy() {

        @Override
        public AvailableResult isEndpointAvailable(Endpoint endpoint, HttpClient httpClient) throws OXException {
            HttpGet get = null;
            HttpResponse response = null;
            try {
                get = new HttpGet(HttpDoveAdmClient.buildUri(new URI(endpoint.getBaseUri()), null, "/downloader/search"));
                response = httpClient.execute(get);
                int status = response.getStatusLine().getStatusCode();
                if (200 == status) {
                    return AvailableResult.AVAILABLE;
                }
                if (401 == status) {
                    return AvailableResult.NONE;
                }
            } catch (URISyntaxException e) {
                // ignore
                return AvailableResult.NONE;
            } catch (IOException e) {
                // ignore
            } finally {
                close(get, response);
            }

            return AvailableResult.UNAVAILABLE;
        }
    };

    // -------------------------------------------------------------------------------------------------------------------------------

    private final AtomicReference<EnumMap<HttpDoveAdmCall, EndpointListing>> endpointsReference;

    /**
     * Initializes a new {@link HttpDoveAdmEndpointManager}.
     */
    public HttpDoveAdmEndpointManager() {
        super();
        endpointsReference = new AtomicReference<EnumMap<HttpDoveAdmCall,EndpointListing>>(null);
    }

    /**
     * Initializes this instance.
     *
     * @param factory The end-point factory to use
     * @param configService The configuration service to read properties from
     * @return <code>true</code> if at least one valid end-point is specified; otherwise <code>false</code>
     * @throws OXException If initialization fails
     */
    public boolean init(EndpointManagerFactory factory, ConfigurationService configService) throws OXException {
        org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(HttpDoveAdmEndpointManager.class);

        EnumMap<HttpDoveAdmCall, EndpointListing> endpoints = new EnumMap<HttpDoveAdmCall, EndpointListing>(HttpDoveAdmCall.class);

        String fallBackName = "com.openexchange.dovecot.doveadm.endpoints";
        EndpointListing fallBackEntry = null;
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

                    fallBackEntry = listingFor(endPoints, factory, new StringBuilder(fallBackName.length() + 1).append(fallBackName).append('.'), configService);
                }
                endpoints.put(call, fallBackEntry);
            } else {
                endpoints.put(call, listingFor(endPoints, factory, new StringBuilder(propName.length() + 1).append(propName).append('.'), configService));
            }
        }

        endpointsReference.set(endpoints);
        return true;
    }

    /**
     * Shuts-down this instance.
     */
    public void shutDown() {
        EnumMap<HttpDoveAdmCall, EndpointListing> endpoints = endpointsReference.getAndSet(null);
        if (null != endpoints) {
            for (EndpointListing entry : endpoints.values()) {
                HttpClients.shutDown(entry.httpClient);
            }
        }
    }

    /**
     * Black-lists specified end-point for given call.
     *
     * @param call The call
     * @param endpoint The end-point to black-list
     */
    public void blacklist(HttpDoveAdmCall call, Endpoint endpoint) {
        if (null == call) {
            return;
        }

        EnumMap<HttpDoveAdmCall, EndpointListing> endpoints = endpointsReference.get();
        if (null == endpoints) {
            return;
        }

        EndpointListing entry = endpoints.get(call);
        if (null == entry) {
            return;
        }

        entry.endpointManager.blacklist(endpoint);
    }

    /**
     * Gets the HTTP client and (next available) base URI for specified call.
     *
     * @param call The call that is about to be invoked
     * @return The HTTP client and base URI
     * @throws OXException If HTTP client and base URI cannot be returned
     */
    public HttpClientAndEndpoint getHttpClientAndUri(HttpDoveAdmCall call) throws OXException {
        if (null == call) {
            throw DoveAdmClientExceptionCodes.UNKNOWN_CALL.create("null");
        }

        EnumMap<HttpDoveAdmCall, EndpointListing> endpoints = endpointsReference.get();
        if (null == endpoints) {
            throw OXException.general("DoveAdm client not initialized.");
        }

        EndpointListing entry = endpoints.get(call);
        if (null == entry) {
            throw DoveAdmClientExceptionCodes.UNKNOWN_CALL.create(call.toString());
        }

        Endpoint endpoint = entry.endpointManager.get();
        if (null == endpoint) {
            if (HttpDoveAdmCall.DEFAULT.equals(call)) {
                throw DoveAdmClientExceptionCodes.DOVEADM_NOT_REACHABLE_GENERIC.create();
            }
            throw DoveAdmClientExceptionCodes.DOVEADM_NOT_REACHABLE.create(call.toString());
        }

        return new HttpClientAndEndpoint(entry.httpClient, endpoint);
    }

}
