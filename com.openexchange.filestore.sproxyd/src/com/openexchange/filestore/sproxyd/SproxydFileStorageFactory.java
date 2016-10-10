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

package com.openexchange.filestore.sproxyd;

import static com.openexchange.osgi.Tools.requireService;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.http.client.HttpClient;
import org.apache.http.client.utils.URIBuilder;
import com.openexchange.config.ConfigurationService;
import com.openexchange.configuration.ConfigurationExceptionCodes;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.DatabaseAccess;
import com.openexchange.filestore.DatabaseAccessService;
import com.openexchange.filestore.FileStorage;
import com.openexchange.filestore.FileStorageInfo;
import com.openexchange.filestore.FileStorageInfoService;
import com.openexchange.filestore.FileStorageProvider;
import com.openexchange.filestore.FileStorages;
import com.openexchange.filestore.sproxyd.chunkstorage.ChunkStorage;
import com.openexchange.filestore.sproxyd.chunkstorage.RdbChunkStorage;
import com.openexchange.filestore.sproxyd.impl.EndpointPool;
import com.openexchange.filestore.sproxyd.impl.SproxydClient;
import com.openexchange.filestore.sproxyd.impl.SproxydConfig;
import com.openexchange.filestore.utils.DefaultDatabaseAccess;
import com.openexchange.java.Strings;
import com.openexchange.rest.client.httpclient.HttpClients;
import com.openexchange.rest.client.httpclient.HttpClients.ClientConfig;
import com.openexchange.server.ServiceLookup;
import com.openexchange.timer.TimerService;

/**
 * {@link SproxydFileStorageFactory}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class SproxydFileStorageFactory implements FileStorageProvider {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SproxydFileStorageFactory.class);

    /**
     * The URI scheme identifying sproxyd file storages.
     */
    private static final String SPROXYD_SCHEME = "sproxyd";

    /**
     * The file storage's ranking compared to other sharing the same URL scheme.
     */
    private static final int RANKING = 547;

    private final ServiceLookup services;
    private final ConcurrentMap<URI, SproxydFileStorage> storages; // local cache may be removed with v7.8.0
    private final ConcurrentMap<String, SproxydConfig> sproxydConfigs;

    /**
     * Initializes a new {@link SproxydFileStorageFactory}.
     *
     * @param ServiceLookup services service look-up
     */
    public SproxydFileStorageFactory(ServiceLookup services) {
        super();
        this.services = services;
        this.storages = new ConcurrentHashMap<URI, SproxydFileStorage>();
        this.sproxydConfigs = new ConcurrentHashMap<String, SproxydConfig>();
    }


    @Override
    public SproxydFileStorage getFileStorage(URI uri) throws OXException {
        SproxydFileStorage storage = storages.get(uri);
        if (null == storage) {
            LOG.debug("Initializing sproxyd file storage for {}", uri);
            /*
             * extract context and user from URI path
             */
            String filestoreID = extractFilestoreID(uri);
            ExtractionResult extractionResult = extractFrom(uri);

            DatabaseAccess databaseAccess;
            int contextId;
            int userId;
            if (extractionResult.hasContextUserAssociation()) {
                contextId = extractionResult.getContextId();
                userId = extractionResult.getUserId();
                databaseAccess = new DefaultDatabaseAccess(userId, contextId, services.getService(DatabaseService.class));
                LOG.debug("Using \"{}\" as filestore ID, context ID of filestore is \"{}\", user ID is \"{}\".", filestoreID, contextId, userId);
            } else {
                contextId = 0;
                userId = 0;
                databaseAccess = lookUpDatabaseAccess(uri, extractionResult.getPrefix());
            }

            // Ensure required tables do exist
            databaseAccess.createIfAbsent(RdbChunkStorage.getRequiredTables());

            /*
             * initialize file storage using dedicated client & chunk storage
             */
            SproxydClient client = initClient(filestoreID, extractionResult.getPrefix());
            ChunkStorage chunkStorage = new RdbChunkStorage(databaseAccess, contextId, userId);
            SproxydFileStorage newStorage = new SproxydFileStorage(client, chunkStorage);
            storage = storages.putIfAbsent(uri, newStorage);
            if (null == storage) {
                storage = newStorage;
            }
        }
        return storage;
    }

    static DatabaseAccess lookUpDatabaseAccess(URI uri, String prefix) throws OXException {
        FileStorageInfoService infoService = FileStorages.getFileStorageInfoService();
        FileStorageInfo info = infoService.getFileStorageIdFor(uri);

        DatabaseAccessService databaseAccessService = FileStorages.getDatabaseAccessService();
        DatabaseAccess databaseAccess = databaseAccessService.getAccessFor(info.getId(), prefix);
        if (null == databaseAccess) {
            throw new IllegalArgumentException("No database access for file storage " + info.getId() + " with prefix \"" + prefix + "\"");
        }

        return databaseAccess;
    }

    @Override
    public FileStorage getInternalFileStorage(URI uri) throws OXException {
        return getFileStorage(uri);
    }

    @Override
    public boolean supports(URI uri) {
         return null != uri && SPROXYD_SCHEME.equalsIgnoreCase(uri.getScheme());
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
     * Extracts context and user identifiers from specified URI; e.g. <code>"sproxyd://mysproxyd/57462_ctx_3_user_store"</code>
     *
     * @param uri The URI to extract from
     * @return The extracted context and user identifiers
     * @throws IllegalArgumentException If URI's path does not follow expected pattern
     */
    ExtractionResult extractFrom(URI uri) {
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
            return new ExtractionResult(0, Integer.parseInt(matcher.group(1)));
        }

        if (path.endsWith("user_store")) {
            // Expect user store identifier
            Matcher matcher = USER_STORE_PATTERN.matcher(path);
            if (false == matcher.matches()) {
                throw new IllegalArgumentException("Path does not match the expected pattern \"(\\d+)_ctx_(\\d+)_user_store\"");
            }
            return new ExtractionResult(Integer.parseInt(matcher.group(2)), Integer.parseInt(matcher.group(1)));
        }

        // Any path that serves as prefix; e.g. "photos"
        return new ExtractionResult(sanitizePathForPrefix(path, uri));
    }

    private static String sanitizePathForPrefix(String path, URI uri) {
        if (Strings.isEmpty(path)) {
            throw new IllegalArgumentException("Path is empty in URI: " + uri);
        }

        StringBuilder sb = null;
        for (int k = path.length(), i = 0; k-- > 0; i++) {
            char ch = path.charAt(i);
            if ('_' == ch) {
                // Underscore not allowed
                if (null == sb) {
                    sb = new StringBuilder(path.length());
                    sb.append(path, 0, i);
                }
            } else {
                // Append
                if (null != sb) {
                    sb.append(ch);
                }
            }
        }
        return null == sb ? path : sb.toString();
    }

    /**
     * Initializes an {@link SproxydClient} as configured by the referenced authority part of the supplied endpoints.
     *
     * @param uri The filestore identifier
     * @param prefix The prefix to use
     * @return The client
     */
    private SproxydClient initClient(String filestoreID, String prefix) throws OXException {
        SproxydConfig sproxydConfig = sproxydConfigs.get(filestoreID);
        if (sproxydConfig == null) {
            SproxydConfig newSproxydConfig = initSproxydConfig(filestoreID);
            sproxydConfig = sproxydConfigs.putIfAbsent(filestoreID, newSproxydConfig);
            if (sproxydConfig == null) {
                sproxydConfig = newSproxydConfig;
            } else {
                newSproxydConfig.getEndpointPool().close();
            }
        }

        return new SproxydClient(sproxydConfig, prefix);
    }

    /**
     * Initializes a new HTTP client and endpoint pool for a configured sproxyd filestore.
     *
     * @param filestoreID The filestore ID
     * @return The configured items
     * @throws OXException
     */
    private SproxydConfig initSproxydConfig(String filestoreID) throws OXException {
        ConfigurationService config = services.getService(ConfigurationService.class);
        // endpoint config
        String protocol = config.getProperty(property(filestoreID, "protocol"));
        if (Strings.isEmpty(protocol)) {
            throw ConfigurationExceptionCodes.PROPERTY_MISSING.create(property(filestoreID, "protocol"));
        }
        String path = config.getProperty(property(filestoreID, "path"));
        if (Strings.isEmpty(path)) {
            throw ConfigurationExceptionCodes.PROPERTY_MISSING.create(property(filestoreID, "path"));
        }
        String hosts = config.getProperty(property(filestoreID, "hosts"));
        if (Strings.isEmpty(hosts)) {
            throw ConfigurationExceptionCodes.PROPERTY_MISSING.create(property(filestoreID, "hosts"));
        }

        // HTTP client config
        int maxConnections = config.getIntProperty(property(filestoreID, "maxConnections"), 100);
        int maxConnectionsPerHost = config.getIntProperty(property(filestoreID, "maxConnectionsPerHost"), 100);
        int connectionTimeout = config.getIntProperty(property(filestoreID, "connectionTimeout"), 5000);
        int socketReadTimeout = config.getIntProperty(property(filestoreID, "socketReadTimeout"), 15000);
        int heartbeatInterval = config.getIntProperty(property(filestoreID, "heartbeatInterval"), 60000);

        List<String> urls = new LinkedList<String>();
        for (String host : Strings.splitAndTrim(hosts, ",")) {
            URIBuilder uriBuilder = new URIBuilder().setScheme(protocol);
            String[] hostAndPort = host.split(":");
            if (hostAndPort.length == 1) {
                uriBuilder.setHost(host);
            } else if (hostAndPort.length == 2) {
                try {
                    uriBuilder.setHost(hostAndPort[0]).setPort(Integer.parseInt(hostAndPort[1]));
                } catch (NumberFormatException e) {
                    throw ConfigurationExceptionCodes.INVALID_CONFIGURATION.create("Invalid value for 'com.openexchange.filestore.sproxyd." + filestoreID + ".hosts': " + hosts);
                }
            } else {
                throw ConfigurationExceptionCodes.INVALID_CONFIGURATION.create("Invalid value for 'com.openexchange.filestore.sproxyd." + filestoreID + ".hosts': " + hosts);
            }

            uriBuilder.setPath(path);
            try {
                String baseUrl = uriBuilder.build().toString();
                if (!baseUrl.endsWith("/")) {
                    baseUrl = baseUrl + '/';
                }
                urls.add(baseUrl);
            } catch (URISyntaxException e) {
                throw ConfigurationExceptionCodes.INVALID_CONFIGURATION.create("Sproxyd configuration leads to invalid URI: " + uriBuilder.toString());
            }
        }

        if (urls.isEmpty()) {
            throw ConfigurationExceptionCodes.INVALID_CONFIGURATION.create("Invalid value for 'com.openexchange.filestore.sproxyd." + filestoreID + ".hosts': " + hosts);
        }

        HttpClient httpClient = HttpClients.getHttpClient(ClientConfig.newInstance()
            .setMaxTotalConnections(maxConnections)
            .setMaxConnectionsPerRoute(maxConnectionsPerHost)
            .setConnectionTimeout(connectionTimeout)
            .setSocketReadTimeout(socketReadTimeout));
        EndpointPool endpointPool = new EndpointPool(filestoreID, urls, httpClient, heartbeatInterval, requireService(TimerService.class, services));
        return new SproxydConfig(httpClient, endpointPool);
    }

    private static final String property(String filestoreID, String property) {
        return "com.openexchange.filestore.sproxyd." + filestoreID + '.' + property;
    }

    /**
     * Extracts the filestore ID from the configured file store URI, i.e. the 'authority' part from the URI.
     *
     * @param uri The file store URI
     * @return The filestore ID
     * @throws IllegalArgumentException If no valid ID could be extracted
     */
    private static String extractFilestoreID(URI uri) throws IllegalArgumentException {
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
