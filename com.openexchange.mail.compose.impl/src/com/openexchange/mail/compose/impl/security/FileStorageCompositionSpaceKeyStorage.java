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

package com.openexchange.mail.compose.impl.security;

import static com.openexchange.mail.compose.impl.attachment.filestore.DedicatedFileStorageAttachmentStorage.getFileStorageId;
import static com.openexchange.mail.compose.impl.util.TimeLimitedFileStorageOperation.createBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.openexchange.capabilities.CapabilitySet;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.FileStorage;
import com.openexchange.filestore.FileStorageCodes;
import com.openexchange.filestore.FileStorageService;
import com.openexchange.filestore.FileStorages;
import com.openexchange.filestore.Info;
import com.openexchange.filestore.QuotaFileStorageService;
import com.openexchange.java.Charsets;
import com.openexchange.java.Streams;
import com.openexchange.java.util.Pair;
import com.openexchange.java.util.UUIDs;
import com.openexchange.mail.compose.CompositionSpaceErrorCode;
import com.openexchange.mail.compose.impl.attachment.filestore.DedicatedFileStorageAttachmentStorage;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.threadpool.AbstractTask;
import com.openexchange.threadpool.Task;


/**
 * {@link FileStorageCompositionSpaceKeyStorage} - The key storage backed by file storage.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public class FileStorageCompositionSpaceKeyStorage extends AbstractCompositionSpaceKeyStorage {

    /** Simple class to delay initialization until needed */
    private static class LoggerHolder {
        static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(FileStorageCompositionSpaceKeyStorage.class);
    }

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
    public boolean isApplicableFor(CapabilitySet capabilities, Session session) throws OXException {
        return capabilities.contains("filestore");
    }

    @Override
    public Key getKeyFor(final UUID compositionSpaceId, final boolean createIfAbsent, final Session session) throws OXException {
        Key cachedKey = cachedKeys.getIfPresent(compositionSpaceId);
        if (null != cachedKey) {
            return cachedKey;
        }

        if (createIfAbsent) {
            try {
                Callable<Key> loader = new Callable<Key>() {

                    @Override
                    public Key call() throws Exception {
                        return loadOrCreateKeyfor(compositionSpaceId, true, session);
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

        Key existentKey = loadOrCreateKeyfor(compositionSpaceId, false, session);
        if (existentKey == null) {
            return null;
        }

        cachedKeys.put(compositionSpaceId, existentKey);
        return existentKey;
    }

    Key loadOrCreateKeyfor(UUID compositionSpaceId, boolean createIfAbsent, Session session) throws OXException {
        StorageIdentifier fileStorageLocation = loadFileStorageLocation(compositionSpaceId, session);
        if (null != fileStorageLocation) {
            FileStorageRef fileStorageRef = getFileStorage(fileStorageLocation.dedicatedFileStorageId, session);
            FileStorage fileStorage = fileStorageRef.fileStorage;
            InputStream file = null;
            try {
                Task<InputStream> getFileTask = new AbstractTask<InputStream>() {

                    @Override
                    public InputStream call() throws Exception {
                        return fileStorage.getFile(fileStorageLocation.identifier);
                    }
                };
                file = createBuilder(getFileTask, fileStorage)
                    .withOnTimeOutHandler(() -> CompositionSpaceErrorCode.FAILED_RETRIEVAL_KEY.create(UUIDs.getUnformattedString(compositionSpaceId)))
                    .withWaitTimeoutSeconds(5)
                    .buildAndSubmit()
                    .getResult();
                if (null != file) {
                    String obfuscatedBase64EncodedKey = Charsets.toAsciiString(Streams.stream2bytes(file));
                    Key key = base64EncodedString2Key(unobfuscate(obfuscatedBase64EncodedKey));
                    return key;
                }
            } catch (OXException e) {
                if (FileStorageCodes.FILE_NOT_FOUND.equals(e)) {
                    LoggerHolder.LOG.warn("Missing key file \"{}\" in file storage \"{}\"", fileStorageLocation.identifier, fileStorageRef.uri);
                    return null;
                }
                throw CompositionSpaceErrorCode.IO_ERROR.create(e, e.getMessage());
            } catch (IOException e) {
                throw CompositionSpaceErrorCode.IO_ERROR.create(e, e.getMessage());
            } finally {
                Streams.close(file);
            }
        }

        if (false == createIfAbsent) {
            return null;
        }

        FileStorageRef fileStorageRef = getFileStorage(session);
        FileStorage fileStorage = fileStorageRef.fileStorage;
        Key newRandomKey = generateRandomKey();
        String newObfuscatedBase64EncodedKey = obfuscate(key2Base64EncodedString(newRandomKey));
        byte[] bytes = Charsets.toAsciiBytes(newObfuscatedBase64EncodedKey);
        AtomicBoolean deleteLocation = new AtomicBoolean(false);
        Task<String> saveNewKeyTask = new AbstractTask<String>() {

            @Override
            public String call() throws Exception {
                String location = fileStorage.saveNewFile(Streams.newByteArrayInputStream(bytes));
                if (deleteLocation.get()) {
                    fileStorage.deleteFile(location);
                    return null;
                }
                return location;
            }
        };
        String newFileStorageLocation = createBuilder(saveNewKeyTask, fileStorage)
            .withTaskFlag(deleteLocation)
            .withWaitTimeoutSeconds(10)
            .buildAndSubmit()
            .getResult();
        try {
            insertFileStorageLocation(newFileStorageLocation, fileStorageRef.dedicatedFileStorageId, compositionSpaceId, session);
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
        List<UUID> nonDeletedKeys = null;
        for (UUID compositionSpaceId : compositionSpaceIds) {
            cachedKeys.invalidate(compositionSpaceId);
            StorageIdentifier fileStorageLocation = loadFileStorageLocation(compositionSpaceId, session);
            if (null == fileStorageLocation) {
                if (null == nonDeletedKeys) {
                    nonDeletedKeys = new ArrayList<UUID>(compositionSpaceIds.size());
                }
                nonDeletedKeys.add(compositionSpaceId);
            } else {
                FileStorageRef fileStorageRef = getFileStorage(fileStorageLocation.dedicatedFileStorageId, session);
                FileStorage fileStorage = fileStorageRef.fileStorage;
                fileStorage.deleteFile(fileStorageLocation.identifier);
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

    private StorageIdentifier loadFileStorageLocation(UUID compositionSpaceId, Session session) throws OXException {
        DatabaseService databaseService = requireDatabaseService();
        Connection con = databaseService.getReadOnly(session.getContextId());
        try {
            return loadFileStorageLocation(compositionSpaceId, session, con);
        } finally {
            databaseService.backReadOnly(session.getContextId(), con);
        }
    }

    private StorageIdentifier loadFileStorageLocation(UUID compositionSpaceId, Session session, Connection con) throws OXException {
        if (null == con) {
            return loadFileStorageLocation(compositionSpaceId, session);
        }

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT cid, user, refId, dedicatedFileStorageId FROM compositionSpaceKeyStorage WHERE uuid=?");
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

            int dedicatedFileStorageId = rs.getInt(4);
            return new StorageIdentifier(unobfuscate(rs.getString(3)), dedicatedFileStorageId);
        } catch (SQLException e) {
            throw CompositionSpaceErrorCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    private boolean deleteFileStorageLocation(UUID compositionSpaceId, Session session) throws OXException {
        DatabaseService databaseService = requireDatabaseService();
        boolean deleted = false;
        Connection con = databaseService.getWritable(session.getContextId());
        try {
            deleted = deleteFileStorageLocation(compositionSpaceId, session, con);
            return deleted;
        } finally {
            if (deleted) {
                databaseService.backWritable(session.getContextId(), con);
            } else {
                databaseService.backWritableAfterReading(session.getContextId(), con);
            }
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

    private boolean insertFileStorageLocation(String fileStorageLocation, int dedicatedFileStorageId, UUID compositionSpaceId, Session session) throws OXException {
        DatabaseService databaseService = requireDatabaseService();
        Connection con = databaseService.getWritable(session.getContextId());
        try {
            return insertFileStorageLocation(fileStorageLocation, dedicatedFileStorageId, compositionSpaceId, session, con);
        } finally {
            databaseService.backWritable(session.getContextId(), con);
        }
    }

    private boolean insertFileStorageLocation(String fileStorageLocation, int dedicatedFileStorageId, UUID compositionSpaceId, Session session, Connection con) throws OXException {
        if (null == con) {
            return insertFileStorageLocation(fileStorageLocation, dedicatedFileStorageId, compositionSpaceId, session);
        }

        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("INSERT INTO compositionSpaceKeyStorage (uuid, cid, user, refId, dedicatedFileStorageId) VALUES (?, ?, ?, ?, ?)");
            stmt.setBytes(1, UUIDs.toByteArray(compositionSpaceId));
            stmt.setInt(2, session.getContextId());
            stmt.setInt(3, session.getUserId());
            stmt.setString(4, obfuscate(fileStorageLocation));
            stmt.setInt(5, dedicatedFileStorageId);
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
     * Gets the {@link FileStorage} for given session.
     *
     * @param session The session
     * @return The file storage
     * @throws OXException If file storage cannot be returned
     */
    private FileStorageRef getFileStorage(Session session) throws OXException {
        // Acquire file storage identifier
        return getFileStorage(getFileStorageId(session.getUserId(), session.getContextId(), services), session);
    }

    /**
     * Gets the {@link FileStorage} for given arguments.
     *
     * @param fileStorageId The file storage identifier or <code>0</code>
     * @param session The session
     * @return The file storage
     * @throws OXException If file storage cannot be returned
     */
    private static FileStorageRef getFileStorage(int fileStorageId, Session session) throws OXException {
        // Acquire needed service
        FileStorageService storageService = FileStorages.getFileStorageService();
        if (null == storageService) {
            throw ServiceExceptionCode.absentService(FileStorageService.class);
        }

        if (fileStorageId > 0) {
            // Use dedicated file storage
            Pair<FileStorage, URI> fsAndUri = DedicatedFileStorageAttachmentStorage.getDedicatedFileStorage(fileStorageId, session.getContextId());
            return new FileStorageRef(fsAndUri.getFirst(), fileStorageId, fsAndUri.getSecond());
        }

        // Acquire needed service
        QuotaFileStorageService quotaStorageService = FileStorages.getQuotaFileStorageService();
        if (null == quotaStorageService) {
            throw ServiceExceptionCode.absentService(QuotaFileStorageService.class);
        }

        // Grab quota-aware file storage to determine fully qualifying URI
        URI uri = quotaStorageService.getQuotaFileStorage(session.getContextId(), Info.general()).getUri();
        return new FileStorageRef(storageService.getFileStorage(uri), 0, uri);
    }

    private static class FileStorageRef {

        final FileStorage fileStorage;
        final int dedicatedFileStorageId;
        final URI uri;

        FileStorageRef(FileStorage fileStorage, int dedicatedFileStorageId, URI uri) {
            super();
            this.fileStorage = fileStorage;
            this.dedicatedFileStorageId = dedicatedFileStorageId;
            this.uri = uri;
        }
    }

    private static class StorageIdentifier {

        final String identifier;
        final int dedicatedFileStorageId;

        StorageIdentifier(String identifier, int dedicatedFileStorageId) {
            super();
            this.identifier = identifier;
            this.dedicatedFileStorageId = dedicatedFileStorageId;
        }
    }

}
