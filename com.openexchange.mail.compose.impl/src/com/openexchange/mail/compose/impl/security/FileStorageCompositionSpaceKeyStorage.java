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

package com.openexchange.mail.compose.impl.security;

import java.io.IOException;
import java.io.InputStream;
import java.security.Key;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.FileStorage;
import com.openexchange.filestore.FileStorages;
import com.openexchange.filestore.Info;
import com.openexchange.filestore.QuotaFileStorage;
import com.openexchange.filestore.QuotaFileStorageService;
import com.openexchange.java.Charsets;
import com.openexchange.java.Streams;
import com.openexchange.java.util.UUIDs;
import com.openexchange.mail.compose.CompositionSpaceErrorCode;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;


/**
 * {@link FileStorageCompositionSpaceKeyStorage} - The key storage backed by file storage.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public class FileStorageCompositionSpaceKeyStorage extends AbstractCompositionSpaceKeyStorage {

    private static final AtomicReference<FileStorageCompositionSpaceKeyStorage> INSTANCE_REFERENCE = new AtomicReference<>();

    /**
     * Initializes the singleton instance for <code>FileStorageCompositionSpaceKeyStorage</code>.
     *
     * @param services The service look-up
     * @return The initialized instance or <code>null</code> if already initialized
     */
    public static FileStorageCompositionSpaceKeyStorage initInstance(ServiceLookup services) {
        FileStorageCompositionSpaceKeyStorage newInstance = new FileStorageCompositionSpaceKeyStorage(services);
        return INSTANCE_REFERENCE.compareAndSet(null, newInstance) ? newInstance : null;
    }

    /**
     * Unsets the singleton instance for <code>FileStorageCompositionSpaceKeyStorage</code>.
     */
    public static void unsetInstance() {
        INSTANCE_REFERENCE.set(null);
    }

    /**
     * Gets the singleton instance for <code>FileStorageCompositionSpaceKeyStorage</code>.
     *
     * @return The <code>FileStorageCompositionSpaceKeyStorage</code> instance or <code>null</code> if not yet initialized
     */
    public static FileStorageCompositionSpaceKeyStorage getInstance() {
        return INSTANCE_REFERENCE.get();
    }

    // ---------------------------------------------------------------------------------------------------------------------------------

    private final Cache<UUID, Key> cachedKeys;

    /**
     * Initializes a new {@link FileStorageCompositionSpaceKeyStorage}.
     */
    private FileStorageCompositionSpaceKeyStorage(ServiceLookup services) {
        super(services);
        cachedKeys = CacheBuilder.newBuilder().expireAfterAccess(Duration.ofMinutes(30)).maximumSize(250000).build();
    }

    /**
     * Clears the cache
     */
    public void clearCache() {
        cachedKeys.invalidateAll();
    }

    @Override
    public List<String> neededCapabilities() {
        return Collections.singletonList("filestore");
    }

    @Override
    public Key getKeyFor(final UUID compositionSpaceId, final boolean createIfAbsent, final Session session) throws OXException {
        Key cachedKey = cachedKeys.getIfPresent(compositionSpaceId);
        if (null != cachedKey) {
            return cachedKey;
        }

        try {
            Callable<Key> loader = new Callable<Key>() {

                @Override
                public Key call() throws Exception {
                    return loadOrCreateKeyfor(compositionSpaceId, createIfAbsent, session);
                }
            };
            return cachedKeys.get(compositionSpaceId, loader);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof OXException) {
                throw (OXException) cause;
            }
            throw CompositionSpaceErrorCode.ERROR.create(cause, cause.getMessage());
        }
    }

    Key loadOrCreateKeyfor(UUID compositionSpaceId, boolean createIfAbsent, Session session) throws OXException {
        QuotaFileStorage fileStorage = getFileStorage(session);

        String fileStorageLocation = loadFileStorageLocation(compositionSpaceId, session);
        if (null != fileStorageLocation) {
            InputStream file = fileStorage.getFile(fileStorageLocation);
            try {
                if (null != file) {
                    String obfuscatedBase64EncodedKey = Charsets.toAsciiString(Streams.stream2bytes(file));
                    Key key = base64EncodedString2Key(unobfuscate(obfuscatedBase64EncodedKey));
                    return key;
                }
            } catch (IOException e) {
                throw CompositionSpaceErrorCode.IO_ERROR.create(e, e.getMessage());
            } finally {
                Streams.close(file);
            }
        }

        if (false == createIfAbsent) {
            return null;
        }

        Key newRandomKey = generateRandomKey();
        String newObfuscatedBase64EncodedKey = obfuscate(key2Base64EncodedString(newRandomKey));
        byte[] bytes = Charsets.toAsciiBytes(newObfuscatedBase64EncodedKey);
        String newFileStorageLocation = fileStorage.saveNewFile(Streams.newByteArrayInputStream(bytes), bytes.length);
        try {
            insertFileStorageLocation(newFileStorageLocation, compositionSpaceId, session);
            newFileStorageLocation = null;
            return newRandomKey;
        } finally {
            if (null != newFileStorageLocation) {
                fileStorage.deleteFile(newFileStorageLocation);
            }
        }
    }

    @Override
    public List<UUID> deleteKeysFor(Collection<UUID> compositionSpaceIds, Session session) throws OXException {
        QuotaFileStorage fileStorage = getFileStorage(session);

        List<UUID> nonDeletedKeys = null;
        for (UUID compositionSpaceId : compositionSpaceIds) {
            cachedKeys.invalidate(compositionSpaceId);
            String fileStorageLocation = loadFileStorageLocation(compositionSpaceId, session);
            if (null == fileStorageLocation) {
                if (null == nonDeletedKeys) {
                    nonDeletedKeys = new ArrayList<UUID>(compositionSpaceIds.size());
                }
                nonDeletedKeys.add(compositionSpaceId);
            } else {
                fileStorage.deleteFile(fileStorageLocation);
                if (!deleteFileStorageLocation(compositionSpaceId, session)) {
                    if (null == nonDeletedKeys) {
                        nonDeletedKeys = new ArrayList<UUID>(compositionSpaceIds.size());
                    }
                    nonDeletedKeys.add(compositionSpaceId);
                }
            }
        }
        return null == nonDeletedKeys ? Collections.emptyList() : nonDeletedKeys;
    }

    private String loadFileStorageLocation(UUID compositionSpaceId, Session session) throws OXException {
        DatabaseService databaseService = requireDatabaseService();
        Connection con = databaseService.getReadOnly(session.getContextId());
        try {
            return loadFileStorageLocation(compositionSpaceId, session, con);
        } finally {
            databaseService.backReadOnly(session.getContextId(), con);
        }
    }

    private String loadFileStorageLocation(UUID compositionSpaceId, Session session, Connection con) throws OXException {
        if (null == con) {
            return loadFileStorageLocation(compositionSpaceId, session);
        }

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT cid, user, refId FROM compositionSpaceKeyStorage WHERE uuid=?");
            stmt.setBytes(1, UUIDs.toByteArray(compositionSpaceId));
            rs = stmt.executeQuery();
            if (false == rs.next()) {
                return null;
            }

            if (session.getContextId() != rs.getInt(1)) {
                // Context does not match
                return null;
            }
            if (session.getUserId() != rs.getInt(2)) {
                // User does not match
                return null;
            }

            return unobfuscate(rs.getString(3));
        } catch (SQLException e) {
            throw CompositionSpaceErrorCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    private boolean deleteFileStorageLocation(UUID compositionSpaceId, Session session) throws OXException {
        DatabaseService databaseService = requireDatabaseService();
        Connection con = databaseService.getWritable(session.getContextId());
        try {
            return deleteFileStorageLocation(compositionSpaceId, session, con);
        } finally {
            databaseService.backWritable(session.getContextId(), con);
        }
    }

    private boolean deleteFileStorageLocation(UUID compositionSpaceId, Session session, Connection con) throws OXException {
        if (null == con) {
            return deleteFileStorageLocation(compositionSpaceId, session);
        }

        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("DELETE FROM compositionSpaceKeyStorage WHERE uuid=?");
            stmt.setBytes(1, UUIDs.toByteArray(compositionSpaceId));
            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            throw CompositionSpaceErrorCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    private boolean insertFileStorageLocation(String fileStorageLocation, UUID compositionSpaceId, Session session) throws OXException {
        DatabaseService databaseService = requireDatabaseService();
        Connection con = databaseService.getWritable(session.getContextId());
        try {
            return insertFileStorageLocation(fileStorageLocation, compositionSpaceId, session, con);
        } finally {
            databaseService.backWritable(session.getContextId(), con);
        }
    }

    private boolean insertFileStorageLocation(String fileStorageLocation, UUID compositionSpaceId, Session session, Connection con) throws OXException {
        if (null == con) {
            return insertFileStorageLocation(fileStorageLocation, compositionSpaceId, session);
        }

        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("INSERT INTO compositionSpaceKeyStorage (uuid, cid, user, refId) VALUES (?, ?, ?, ?)");
            stmt.setBytes(1, UUIDs.toByteArray(compositionSpaceId));
            stmt.setInt(2, session.getContextId());
            stmt.setInt(3, session.getUserId());
            stmt.setString(4, obfuscate(fileStorageLocation));
            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            throw CompositionSpaceErrorCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    /**
     * Requires the database service
     *
     * @return The database service
     * @throws OXException If database service is not available
     */
    private DatabaseService requireDatabaseService() throws OXException {
        DatabaseService databaseService = services.getOptionalService(DatabaseService.class);
        if (null == databaseService) {
            throw ServiceExceptionCode.absentService(DatabaseService.class);
        }
        return databaseService;
    }

    /**
     * Returns the {@link FileStorage} assigned to session-associated context
     *
     * @param session The session providing context identifier
     * @return The file storage
     * @throws OXException If file storage cannot be returned
     */
    private static QuotaFileStorage getFileStorage(Session session) throws OXException {
        return getFileStorage(session.getContextId());
    }

    /**
     * Returns the {@link FileStorage} assigned to the given context
     *
     * @param contextId The context identifier
     * @return The file storage
     * @throws OXException If file storage cannot be returned
     */
    private static QuotaFileStorage getFileStorage(int contextId) throws OXException {
        QuotaFileStorageService storageService = FileStorages.getQuotaFileStorageService();
        if (null == storageService) {
            throw ServiceExceptionCode.absentService(QuotaFileStorageService.class);
        }
        return storageService.getQuotaFileStorage(contextId, Info.general());
    }

}
