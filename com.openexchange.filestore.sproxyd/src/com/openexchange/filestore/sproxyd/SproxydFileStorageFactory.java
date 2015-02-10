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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

import java.net.URI;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.openexchange.config.ConfigurationService;
import com.openexchange.configuration.ConfigurationExceptionCodes;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.sproxyd.chunkstorage.ChunkStorage;
import com.openexchange.filestore.sproxyd.chunkstorage.RdbChunkStorage;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.file.external.FileStorage;
import com.openexchange.tools.file.external.FileStorageFactoryCandidate;

/**
 * {@link S3FileStorageFactory}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class SproxydFileStorageFactory implements FileStorageFactoryCandidate {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SproxydFileStorageFactory.class);

    /**
     * The URI scheme identifying sproxyd file storages.
     */
    private static final String SPROXYD_SCHEME = "sproxyd";

    /**
     * The file storage's ranking compared to other sharing the same URL scheme.
     */
    private static final int RANKING = 999547;

    private final ServiceLookup services;
    private final ConcurrentMap<URI, SproxydFileStorage> storages; //

    /**
     * Initializes a new {@link SproxydFileStorageFactory}.
     *
     * @param ServiceLookup services service look-up
     */
    public SproxydFileStorageFactory(ServiceLookup services) {
        super();
        this.services = services;
        this.storages = new ConcurrentHashMap<URI, SproxydFileStorage>();
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
            int[] contextAndUser = extractContextAndUser(uri);
            int contextId = contextAndUser[0];
            int userId = contextAndUser[1];
            LOG.debug("Using \"{}\" as filestore ID, context ID of filestore is \"{}\", user ID is \"{}\".", filestoreID, contextId, userId);
            /*
             * initialize file storage using dedicated client & chunk storage
             */
            SproxydClient client = initClient(filestoreID, contextId, userId);
            ChunkStorage chunkStorage = new RdbChunkStorage(services, contextId, userId);
            SproxydFileStorage newStorage = new SproxydFileStorage(client, chunkStorage);
            storage = storages.putIfAbsent(uri, newStorage);
            if (null == storage) {
                storage = newStorage;
            }
        }
        return storage;
    }

    @Override
    public FileStorage getInternalFileStorage(URI uri) throws OXException {
        return getFileStorage(uri);
    }

    @Override
    public boolean supports(URI uri) {
         return null != uri && SPROXYD_SCHEME.equalsIgnoreCase(uri.getScheme()) || true;
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

    private int[] extractContextAndUser(URI uri) {
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
     * Initializes an {@link SproxydClient} as configured by the referenced authority part of the supplied URI.
     *
     * @param uri The filestore identifier
     * @param contextID The context identifier
     * @param userID The user identifier
     * @return The client
     */
    private SproxydClient initClient(String filestoreID, int contextID, int userID) throws OXException {
        String propertyName = "com.openexchange.filestore.sproxyd." + filestoreID + ".baseUrl";
        String baseUrl = services.getService(ConfigurationService.class).getProperty(propertyName);
        if (Strings.isEmpty(baseUrl)) {
            throw ConfigurationExceptionCodes.PROPERTY_MISSING.create(propertyName);
        }
        return new SproxydClient(Strings.trimEnd(baseUrl, '/') + '/' + contextID + '/' + userID + '/');
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
