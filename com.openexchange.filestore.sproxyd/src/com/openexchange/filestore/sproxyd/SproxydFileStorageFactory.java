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

package com.openexchange.filestore.sproxyd;

import static com.openexchange.filestore.utils.PropertyNameBuilder.optIntProperty;
import static com.openexchange.filestore.utils.PropertyNameBuilder.requireProperty;
import static com.openexchange.java.Autoboxing.I;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.http.client.utils.URIBuilder;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.common.util.concurrent.UncheckedExecutionException;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadables;
import com.openexchange.configuration.ConfigurationExceptionCodes;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.DatabaseAccess;
import com.openexchange.filestore.DatabaseAccessService;
import com.openexchange.filestore.FileStorageInfo;
import com.openexchange.filestore.FileStorageInfoService;
import com.openexchange.filestore.FileStorageProvider;
import com.openexchange.filestore.FileStorages;
import com.openexchange.filestore.InterestsAware;
import com.openexchange.filestore.sproxyd.chunkstorage.ChunkStorage;
import com.openexchange.filestore.sproxyd.chunkstorage.RdbChunkStorage;
import com.openexchange.filestore.sproxyd.impl.EndpointPool;
import com.openexchange.filestore.sproxyd.impl.SproxydClient;
import com.openexchange.filestore.utils.DefaultDatabaseAccess;
import com.openexchange.filestore.utils.PropertyNameBuilder;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;

/**
 * {@link SproxydFileStorageFactory}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class SproxydFileStorageFactory implements FileStorageProvider, InterestsAware {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SproxydFileStorageFactory.class);

    /** The URI scheme identifying sproxyd file storages. */
    private static final String SPROXYD_SCHEME = "sproxyd";

    /** The file storage's ranking compared to other sharing the same URL scheme. */
    private static final int RANKING = 547;

    // -------------------------------------------------------------------------------------------------------------------------------------

    /** The service look-up */
    private final ServiceLookup services;

    /** The cache holding initialized end-point pools */
    private final Cache<String, EndpointPool> endpointPoolCache;

    /**
     * Initializes a new {@link SproxydFileStorageFactory}.
     *
     * @param ServiceLookup services service look-up
     */
    public SproxydFileStorageFactory(ServiceLookup services) {
        super();
        this.services = services;
        endpointPoolCache = CacheBuilder.newBuilder()
            .weakValues()
            .removalListener(new RemovalListener<String, EndpointPool>() {

                @Override
                public void onRemoval(RemovalNotification<String, EndpointPool> notification) {
                    notification.getValue().close();
                }
            })
            .build();
    }

    @Override
    public Interests getInterests() {
        return Reloadables.interestsForProperties("com.openexchange.filestore.sproxyd.*");
    }

    @Override
    public SproxydFileStorage getFileStorage(URI uri) throws OXException {
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
            LOG.debug("Using \"{}\" as filestore ID, context ID of filestore is \"{}\", user ID is \"{}\".", filestoreID, I(contextId), I(userId));
        } else {
            contextId = 0;
            userId = 0;
            databaseAccess = lookUpDatabaseAccess(uri, extractionResult.getRawPrefix());
        }

        // Ensure required tables do exist
        databaseAccess.createIfAbsent(RdbChunkStorage.getRequiredTables());

        /*
         * initialize file storage using dedicated client & chunk storage
         */
        SproxydClient client = initClient(filestoreID, extractionResult.getPrefix());
        ChunkStorage chunkStorage = new RdbChunkStorage(databaseAccess, contextId, userId);
        return new SproxydFileStorage(uri, client, chunkStorage);
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
        return new ExtractionResult(sanitizePathForPrefix(path, uri), path);
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
        return new SproxydClient(services, getEndpointPoolFor(filestoreID), prefix, filestoreID);
    }

    /**
     * Gets the end-point pool for specified Sproxyd filestore.
     *
     * @param filestoreID The filestore identifier
     * @return The end-point pool
     * @throws OXException If end-point pool cannot be initialized
     */
    private EndpointPool getEndpointPoolFor(String filestoreID) throws OXException {
        EndpointPool endpointPool = endpointPoolCache.getIfPresent(filestoreID);
        if (endpointPool == null) {
            try {
                endpointPool = endpointPoolCache.get(filestoreID, new EndpointPoolLoaderCallable(filestoreID, this));
            } catch (ExecutionException | UncheckedExecutionException e) {
                Throwable cause = e.getCause();
                if (cause instanceof OXException) {
                    throw (OXException) cause;
                }
                throw OXException.general("Failed initializing Sproxyd end-point pool.", cause);
            }
        }
        return endpointPool;
    }

    /**
     * Initializes a end-point pool for specified Sproxyd filestore.
     *
     * @param filestoreID The filestore identifier
     * @return The new end-point pool
     * @throws OXException If end-point pool cannot be initialized
     */
    EndpointPool initEndpointPoolFor(String filestoreID) throws OXException {
        ConfigurationService configService = services.getService(ConfigurationService.class);
        PropertyNameBuilder nameBuilder = new PropertyNameBuilder("com.openexchange.filestore.sproxyd.");
        // End-point configuration
        String protocol = requireProperty(filestoreID, "protocol", nameBuilder, configService);
        String path = requireProperty(filestoreID, "path", nameBuilder, configService);
        String hosts = requireProperty(filestoreID, "hosts", nameBuilder, configService);

        int heartbeatInterval = optIntProperty(filestoreID, "heartbeatInterval", 60000, nameBuilder, configService);

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
                    throw ConfigurationExceptionCodes.INVALID_CONFIGURATION.create(e, "Invalid value for 'com.openexchange.filestore.sproxyd." + filestoreID + ".hosts': " + hosts);
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
                throw ConfigurationExceptionCodes.INVALID_CONFIGURATION.create(e, "Sproxyd configuration leads to invalid URI: " + uriBuilder.toString());
            }
        }

        if (urls.isEmpty()) {
            throw ConfigurationExceptionCodes.INVALID_CONFIGURATION.create("Invalid value for 'com.openexchange.filestore.sproxyd." + filestoreID + ".hosts': " + hosts);
        }

        return new EndpointPool(filestoreID, urls, heartbeatInterval, services);
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

    // -------------------------------------------------------------------------------------------------------------------------------------

    private static class EndpointPoolLoaderCallable implements Callable<EndpointPool> {

        private final String filestoreID;
        private final SproxydFileStorageFactory factory;

        EndpointPoolLoaderCallable(String filestoreID, SproxydFileStorageFactory factory) {
            super();
            this.filestoreID = filestoreID;
            this.factory = factory;
        }

        @Override
        public EndpointPool call() throws Exception {
            return factory.initEndpointPoolFor(filestoreID);
        }
    }

}
