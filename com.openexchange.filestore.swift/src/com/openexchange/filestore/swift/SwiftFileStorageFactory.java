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

package com.openexchange.filestore.swift;

import static com.openexchange.osgi.Tools.requireService;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.http.client.HttpClient;
import org.apache.http.client.utils.URIBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.config.ConfigurationService;
import com.openexchange.configuration.ConfigurationExceptionCodes;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.FileStorage;
import com.openexchange.filestore.FileStorageProvider;
import com.openexchange.filestore.swift.chunkstorage.ChunkStorage;
import com.openexchange.filestore.swift.chunkstorage.RdbChunkStorage;
import com.openexchange.filestore.swift.impl.AuthInfo;
import com.openexchange.filestore.swift.impl.ConfigType;
import com.openexchange.filestore.swift.impl.EndpointPool;
import com.openexchange.filestore.swift.impl.SwiftClient;
import com.openexchange.filestore.swift.impl.SwiftConfig;
import com.openexchange.filestore.swift.impl.TokenAndResponse;
import com.openexchange.filestore.swift.impl.token.Token;
import com.openexchange.filestore.swift.impl.token.TokenStorage;
import com.openexchange.filestore.swift.impl.token.TokenStorageImpl;
import com.openexchange.java.Strings;
import com.openexchange.rest.client.httpclient.HttpClients;
import com.openexchange.rest.client.httpclient.HttpClients.ClientConfig;
import com.openexchange.server.ServiceLookup;
import com.openexchange.timer.TimerService;

/**
 * {@link SwiftFileStorageFactory} - The factory for Swift file storages
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public class SwiftFileStorageFactory implements FileStorageProvider {

    /** The logger constant */
    static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SwiftFileStorageFactory.class);

    /**
     * The URI scheme identifying <a href="http://docs.openstack.org/developer/swift/">Swift</a> file storages.
     */
    private static final String SWIFT_SCHEME = "swift";

    /**
     * The file storage's ranking compared to other sharing the same URL scheme.
     */
    private static final int RANKING = 547;

    // -------------------------------------------------------------------------------------------------------------------- //

    final ServiceLookup services;
    private final ConcurrentMap<URI, Future<SwiftFileStorage>> storages;
    private final ConcurrentMap<String, SwiftConfig> swiftConfigs;
    private final TokenStorage tokenStorage;

    /**
     * Initializes a new {@link SwiftFileStorageFactory}.
     *
     * @param ServiceLookup services service look-up
     */
    public SwiftFileStorageFactory(ServiceLookup services) {
        super();
        this.services = services;
        this.storages = new ConcurrentHashMap<URI, Future<SwiftFileStorage>>();
        this.swiftConfigs = new ConcurrentHashMap<String, SwiftConfig>();
        tokenStorage = new TokenStorageImpl(services);
    }

    @Override
    public SwiftFileStorage getFileStorage(final URI uri) throws OXException {
        // Expect something like "swift://myswift/57462_ctx_3_user_store"
        Future<SwiftFileStorage> f = storages.get(uri);
        if (null == f) {
            FutureTask<SwiftFileStorage> ft = new FutureTask<SwiftFileStorage>(new Callable<SwiftFileStorage>() {

                @Override
                public SwiftFileStorage call() throws OXException {
                    LOG.debug("Initializing Swift file storage for {}", uri);

                    // Extract context and user from URI path
                    String filestoreID = extractFilestoreID(uri);
                    int[] contextAndUser = extractContextAndUser(uri);
                    int contextId = contextAndUser[0];
                    int userId = contextAndUser[1];
                    LOG.debug("Using \"{}\" as filestore ID, context ID of filestore is \"{}\", user ID is \"{}\".", filestoreID, contextId, userId);

                    // Initialize file storage using dedicated client & chunk storage
                    SwiftClient client = initClient(filestoreID, contextId, userId);
                    ChunkStorage chunkStorage = new RdbChunkStorage(services, contextId, userId);
                    return new SwiftFileStorage(client, chunkStorage);
                }
            });

            f = storages.putIfAbsent(uri, ft);
            if (null == f) {
                ft.run();
                f = ft;
            }
        }

        try {
            return f.get();
        } catch (InterruptedException e) {
            // Keep interrupted status
            Thread.currentThread().interrupt();
            throw SwiftExceptionCode.UNEXPECTED_ERROR.create(e, "Interrupted");
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof OXException) {
                throw (OXException) cause;
            }
            throw SwiftExceptionCode.UNEXPECTED_ERROR.create(cause, cause.getMessage());
        }
    }

    @Override
    public FileStorage getInternalFileStorage(URI uri) throws OXException {
        return getFileStorage(uri);
    }

    @Override
    public boolean supports(URI uri) {
         return null != uri && SWIFT_SCHEME.equalsIgnoreCase(uri.getScheme());
    }

    @Override
    public int getRanking() {
        return RANKING;
    }

    // ---------------------------------------------------------------------------------------------------------------

    /**
     * The expected pattern for file store names associated with a context - defined by
     * com.openexchange.filestore.FileStorages.getNameForContext(int) ,
     * so expect nothing else; e.g. <code>"57462_ctx_store"</code>
     */
    private static final Pattern CTX_STORE_PATTERN = Pattern.compile("(\\d+)_ctx_store");

    /**
     * The expected pattern for file store names associated with a user - defined by
     * com.openexchange.filestore.FileStorages.getNameForUser(int, int) ,
     * so expect nothing else; e.g. <code>"57462_ctx_5_user_store"</code>
     */
    private static final Pattern USER_STORE_PATTERN = Pattern.compile("(\\d+)_ctx_(\\d+)_user_store");

    /**
     * Extracts context and user identifiers from specified URI; e.g. <code>"swift://myswift/57462_ctx_3_user_store"</code>
     *
     * @param uri The URI to extract from
     * @return The extracted context and user identifiers
     * @throws IllegalArgumentException If URI's path does not follow expected pattern
     */
    int[] extractContextAndUser(URI uri) {
        String path = uri.getPath();
        while (0 < path.length() && '/' == path.charAt(0)) {
            path = path.substring(1);
        }
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        if (path.endsWith("ctx_store")) {
            Matcher matcher = CTX_STORE_PATTERN.matcher(path);
            if (false == matcher.matches()) {
                throw new IllegalArgumentException("Path does not match the expected pattern \"\\d+_ctx_store\"");
            }
            return new int[] {Integer.parseInt(matcher.group(1)), 0};
        }

        // Expect user store identifier
        Matcher matcher = USER_STORE_PATTERN.matcher(path);
        if (false == matcher.matches()) {
            throw new IllegalArgumentException("Path does not match the expected pattern \"(\\d+)_ctx_(\\d+)_user_store\"");
        }
        return new int[] {Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2))};
    }

    /**
     * Initializes an {@link SwiftClient} as configured by the referenced authority part of the supplied end-points.
     *
     * @param uri The file storage identifier
     * @param contextID The context identifier
     * @param userID The user identifier
     * @return The client
     */
    SwiftClient initClient(String filestoreID, int contextID, int userID) throws OXException {
        SwiftConfig swiftConfig = swiftConfigs.get(filestoreID);
        if (swiftConfig == null) {
            SwiftConfig newSwiftConfig = initSwiftConfig(filestoreID);
            swiftConfig = swiftConfigs.putIfAbsent(filestoreID, newSwiftConfig);
            if (swiftConfig == null) {
                swiftConfig = newSwiftConfig;
            } else {
                newSwiftConfig.getEndpointPool().close();
            }
        }

        return new SwiftClient(swiftConfig, contextID, userID, tokenStorage);
    }

    /**
     * Initializes a new HTTP client and end-point pool for a configured Swift file storage.
     *
     * @param filestoreID The  file storage ID
     * @return The configured items
     * @throws OXException
     */
    private SwiftConfig initSwiftConfig(String filestoreID) throws OXException {
        ConfigurationService config = services.getService(ConfigurationService.class);

        // User name
        String userName = requireProperty(filestoreID, "userName", config);

        // API type & value
        AuthInfo.Type authType = AuthInfo.Type.typeFor(requireProperty(filestoreID, "authType", config));
        if (null == authType) {
            throw ConfigurationExceptionCodes.INVALID_CONFIGURATION.create("Unsupported auth type: " + authType);
        }
        String authValue = requireProperty(filestoreID, "authValue", config);

        // Config type
        ConfigType configType;
        {
            String sConfigType = config.getProperty(property(filestoreID, "configType"), ConfigType.MANUAL.getId());
            configType = ConfigType.configTypeFor(sConfigType);
            if (null == configType) {
                throw ConfigurationExceptionCodes.INVALID_CONFIGURATION.create("Invalid value for 'com.openexchange.filestore.swift." + filestoreID + ".configType': " + sConfigType);
            }
        }

        // Tenant name, identity URL, and domain
        String tenantName = optProperty(filestoreID, "tenantName", config);
        String identityUrl = optProperty(filestoreID, "identityUrl", config);
        String domain = optProperty(filestoreID, "domain", config);

        // HTTP client configuration
        int maxConnections = config.getIntProperty(property(filestoreID, "maxConnections"), 100);
        int maxConnectionsPerHost = config.getIntProperty(property(filestoreID, "maxConnectionsPerHost"), 100);
        int connectionTimeout = config.getIntProperty(property(filestoreID, "connectionTimeout"), 5000);
        int socketReadTimeout = config.getIntProperty(property(filestoreID, "socketReadTimeout"), 15000);
        int heartbeatInterval = config.getIntProperty(property(filestoreID, "heartbeatInterval"), 60000);

        // Create the HTTP client
        HttpClient httpClient = HttpClients.getHttpClient(ClientConfig.newInstance()
            .setMaxTotalConnections(maxConnections)
            .setMaxConnectionsPerRoute(maxConnectionsPerHost)
            .setConnectionTimeout(connectionTimeout)
            .setSocketReadTimeout(socketReadTimeout));

        // Create the auth info
        AuthInfo authInfo = new AuthInfo(authValue, authType, tenantName, domain, identityUrl);

        // End-points...
        EndpointPool endpointPool;
        if (ConfigType.MANUAL == configType) {
            // Manual end-point configuration
            String protocol = requireProperty(filestoreID, "protocol", config);
            String path = requireProperty(filestoreID, "path", config);
            String hosts = requireProperty(filestoreID, "hosts", config);

            List<String> urls = new LinkedList<String>();
            for (String host : Strings.splitAndTrim(hosts, ",")) {
                URIBuilder uriBuilder = new URIBuilder().setScheme(protocol);
                String[] hostAndPort = Strings.splitByColon(host);
                if (hostAndPort.length == 1) {
                    uriBuilder.setHost(host);
                } else if (hostAndPort.length == 2) {
                    try {
                        uriBuilder.setHost(hostAndPort[0]).setPort(Integer.parseInt(hostAndPort[1]));
                    } catch (NumberFormatException e) {
                        throw ConfigurationExceptionCodes.INVALID_CONFIGURATION.create(e, "Invalid value for 'com.openexchange.filestore.swift." + filestoreID + ".hosts': " + hosts);
                    }
                } else {
                    throw ConfigurationExceptionCodes.INVALID_CONFIGURATION.create("Invalid value for 'com.openexchange.filestore.swift." + filestoreID + ".hosts': " + hosts);
                }

                uriBuilder.setPath(path);
                try {
                    String baseUrl = uriBuilder.build().toString();
                    if (baseUrl.endsWith("/")) {
                        baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
                    }
                    urls.add(baseUrl);
                } catch (URISyntaxException e) {
                    throw ConfigurationExceptionCodes.INVALID_CONFIGURATION.create(e, "Swift configuration leads to invalid URI: " + uriBuilder.toString());
                }
            }

            if (urls.isEmpty()) {
                throw ConfigurationExceptionCodes.INVALID_CONFIGURATION.create("Invalid value for 'com.openexchange.filestore.swift." + filestoreID + ".hosts': " + hosts);
            }

            endpointPool = new EndpointPool(filestoreID, urls, httpClient, heartbeatInterval, null, requireService(TimerService.class, services));
        } else {
            if (AuthInfo.Type.PASSWORD_V3 != authInfo.getType()) {
                throw ConfigurationExceptionCodes.INVALID_CONFIGURATION.create("Invalid value for 'com.openexchange.filestore.swift." + filestoreID + ".configType': " + configType.getId() + ". Only supported for " + AuthInfo.Type.PASSWORD_V3.getId() + " auth type.");
            }

            // By interface, region and container name
            String sInterface = requireProperty(filestoreID, "interface", config);
            String region = requireProperty(filestoreID, "region", config);
            String containerName = requireProperty(filestoreID, "containerName", config);

            try {
                TokenAndResponse tokenAndResponse = SwiftClient.doAcquireNewToken(userName, authInfo, httpClient);
                Token initialToken = tokenAndResponse.getToken();
                JSONArray jCatalog = tokenAndResponse.getJsonResponse().getJSONObject("token").getJSONArray("catalog");
                JSONArray jEndpoints = null;
                for (int k = jCatalog.length(), i = 0; null == jEndpoints && k-- > 0; i++) {
                    JSONObject jCatalogEntry = jCatalog.getJSONObject(i);
                    if ("swift".equals(jCatalogEntry.optString("name", null)) && "object-store".equals(jCatalogEntry.optString("type", null))) {
                        jEndpoints = jCatalogEntry.getJSONArray("endpoints");
                    }
                }
                if (null == jEndpoints) {
                    throw ConfigurationExceptionCodes.INVALID_CONFIGURATION.create("No such catalog entry with \"name\"=\"swift\" and \"type\"=\"object-store\".");
                }

                List<String> urls = new ArrayList<String>(jEndpoints.length());
                for (int k = jEndpoints.length(), i = 0; k-- > 0; i++) {
                    JSONObject jEndpoint = jEndpoints.getJSONObject(i);
                    if (sInterface.equals(jEndpoint.optString("interface", null)) && region.equals(jEndpoint.optString("region", null))) {
                        urls.add(jEndpoint.getString("url") + "/" + containerName);
                    }
                }

                if (urls.isEmpty()) {
                    throw ConfigurationExceptionCodes.INVALID_CONFIGURATION.create("No such end-points for \"interface\"=\"" + sInterface + "\" and \"region\"=\"" + region + "\".");
                }

                endpointPool = new EndpointPool(filestoreID, urls, httpClient, heartbeatInterval, initialToken, requireService(TimerService.class, services));
            } catch (JSONException e) {
                throw ConfigurationExceptionCodes.INVALID_CONFIGURATION.create(e, "Unexpected JSON response.");
            }
        }

        return new SwiftConfig(filestoreID, userName, authInfo, httpClient, endpointPool);
    }

    private static String requireProperty(String filestoreID, String property, ConfigurationService config) throws OXException {
        String propName = property(filestoreID, property);
        String value = config.getProperty(propName);
        if (Strings.isEmpty(value)) {
            throw ConfigurationExceptionCodes.PROPERTY_MISSING.create(propName);
        }
        return value;
    }

    private static String optProperty(String filestoreID, String property, ConfigurationService config) {
        String propName = property(filestoreID, property);
        String value = config.getProperty(propName);
        return Strings.isEmpty(value) ? null : value;
    }

    private static String property(String filestoreID, String property) {
        return new StringBuilder("com.openexchange.filestore.swift.").append(filestoreID).append('.').append(property).toString();
    }

    /**
     * Extracts the file storage identifier from the configured file store URI, i.e. the 'authority' part from the URI.
     *
     * @param uri The file store URI
     * @return The file storage identifier
     * @throws IllegalArgumentException If no valid ID could be extracted
     */
    String extractFilestoreID(URI uri) throws IllegalArgumentException {
        String authority = uri.getAuthority();
        if (null == authority) {
            throw new IllegalArgumentException("No 'authority' part specified in filestore URI");
        }
        while (0 < authority.length() && '/' == authority.charAt(0)) {
            authority = authority.substring(1);
        }
        if (0 == authority.length()) {
            throw new IllegalArgumentException("No 'authority' part specified in filestore URI");
        }
        return authority;
    }

}
