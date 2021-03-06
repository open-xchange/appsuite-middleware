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

package com.openexchange.mail.json.compose.share;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
import java.io.InputStream;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import org.json.JSONException;
import com.openexchange.database.Databases;
import com.openexchange.database.cleanup.CleanUpExecution;
import com.openexchange.database.cleanup.CleanUpExecutionConnectionProvider;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFileStorageFolder;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.composition.FileID;
import com.openexchange.file.storage.composition.FolderID;
import com.openexchange.file.storage.composition.IDBasedFileAccess;
import com.openexchange.file.storage.composition.IDBasedFileAccessFactory;
import com.openexchange.file.storage.composition.IDBasedFolderAccess;
import com.openexchange.file.storage.composition.IDBasedFolderAccessFactory;
import com.openexchange.java.Streams;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.json.compose.share.DefaultAttachmentStorage.DefaultAttachmentStorageContext;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Origin;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIterators;
import com.openexchange.tools.oxfolder.OXFolderExceptionCode;
import com.openexchange.tools.oxfolder.OXFolderUtility;

/**
 * {@link DefaultAttachmentStoragePeriodicCleaner} - The periodic cleaner for default attachment storage.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public class DefaultAttachmentStoragePeriodicCleaner implements CleanUpExecution {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultAttachmentStoragePeriodicCleaner.class);

    private final String id;
    private final AtomicBoolean active;

    /**
     * Initializes a new {@link DefaultAttachmentStoragePeriodicCleaner}.
     *
     * @param id The attachment storage's identifier
     */
    public DefaultAttachmentStoragePeriodicCleaner(String id) {
        super();
        this.id = id;
        active = new AtomicBoolean(true);
    }

    @Override
    public boolean isApplicableFor(String schema, int representativeContextId, int databasePoolId, Map<String, Object> state, CleanUpExecutionConnectionProvider connectionProvider) throws OXException {
        return active.get();
    }

    @Override
    public void executeFor(String schema, int representativeContextId, int databasePoolId, Map<String, Object> state, CleanUpExecutionConnectionProvider connectionProvider) throws OXException {
        long start = System.currentTimeMillis();
        try {
            LOG.info("Periodic cleanup task for shared mail attachments starts for schema {}", schema);
            cleanupSchema(start, schema, connectionProvider);
        } catch (Exception e) {
            LOG.error("Error during periodic cleanup task for shared mail attachments for schema {}", schema, e);
        }
        LOG.info("Periodic cleanup task for shared mail attachments finished after {}ms for schema {}.", L(System.currentTimeMillis() - start), schema);
    }

    /**
     * Stops all background processing by signaling termination flag.
     */
    public void stop() {
        active.set(false);
    }

    /**
     * Cleans obsolete shared mail attachments in context-associated schema.
     *
     * @param threshold The threshold date
     * @param schemaName The name of the processed database schema
     * @param databaseService The database service to use
     */
    private void cleanupSchema(long threshold, String schemaName, CleanUpExecutionConnectionProvider connectionProvider) throws OXException {
        Map<Integer, Map<Integer, List<ExpiredFolder>>> expiredFoldersInSchema = determineExpiredFoldersInSchema(threshold, schemaName, connectionProvider.getConnection());
        cleanupExpiredFolders(expiredFoldersInSchema, threshold);
    }

    /**
     * Determines obsolete shared mail attachments in context-associated schema.
     *
     * @param threshold The threshold date
     * @param schemaName The name of the processed database schema
     * @param con The schema-associated connection to use
     * @return The expired folder in given schema
     */
    private Map<Integer, Map<Integer, List<ExpiredFolder>>> determineExpiredFoldersInSchema(long threshold, String schemaName, Connection con) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT cid, fuid, created_from, meta FROM oxfolder_tree WHERE meta LIKE '%\"expiration-date-%'");
            rs = stmt.executeQuery();
            if (false == rs.next()) {
                return Collections.emptyMap();
            }

            Map<Integer, Map<Integer, List<ExpiredFolder>>> expiredFoldersInSchema = new LinkedHashMap<Integer, Map<Integer, List<ExpiredFolder>>>();
            do {
                int contextId = rs.getInt(1);
                int fuid = rs.getInt(2);
                int owner = rs.getInt(3);
                Map<String, Object> meta = parseMeta(rs, fuid, contextId);
                if (null == meta) {
                    continue;
                }
                Long millis = parseExpirationMillis(meta);
                if ((null != millis) && (millis.longValue() < threshold)) {
                    Map<Integer, List<ExpiredFolder>> userExpiredFolders = expiredFoldersInSchema.get(I(contextId));
                    if (null == userExpiredFolders) {
                        userExpiredFolders = new LinkedHashMap<>();
                        expiredFoldersInSchema.put(I(contextId), userExpiredFolders);
                    }

                    List<ExpiredFolder> folderIds = userExpiredFolders.get(I(owner));
                    if (null == folderIds) {
                        folderIds = new LinkedList<>();
                        userExpiredFolders.put(I(owner), folderIds);
                    }

                    folderIds.add(new ExpiredFolder(fuid, meta));
                }
            } while (rs.next() && active.get());
            return expiredFoldersInSchema;
        } catch (OXException e) {
            throw e;
        } catch (SQLException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, "Unexpected error during cleanup of shared mail attachments in schema \"" + schemaName + "\"");
        } catch (RuntimeException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, "Unexpected error during cleanup of shared mail attachments in schema \"" + schemaName + "\"");
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    private void cleanupExpiredFolders(Map<Integer, Map<Integer, List<ExpiredFolder>>> expiredFoldersInSchema, long threshold) throws OXException {
        for (Iterator<Entry<Integer, Map<Integer, List<ExpiredFolder>>>> schemaEntryIter = expiredFoldersInSchema.entrySet().iterator(); active.get() && schemaEntryIter.hasNext();) {
            Map.Entry<Integer, Map<Integer, List<ExpiredFolder>>> contextEntry = schemaEntryIter.next();
            int contextId = contextEntry.getKey().intValue();
            for (Iterator<Entry<Integer, List<ExpiredFolder>>> ctxEntryIter = contextEntry.getValue().entrySet().iterator(); active.get() && ctxEntryIter.hasNext();) {
                Map.Entry<Integer, List<ExpiredFolder>> userEntry = ctxEntryIter.next();
                int userId = userEntry.getKey().intValue();
                FakeSession session = new FakeSession(userId, contextId);

                DefaultAttachmentStorageContext context = new DefaultAttachmentStorageContext(getFileAccess(session), getFolderAccess(session), session);
                cleanupExpiredUserFolder(userEntry.getValue(), threshold, context);
            }
        }
    }

    private void cleanupExpiredUserFolder(List<ExpiredFolder> folders, long threshold, DefaultAttachmentStorageContext context) throws OXException {
        boolean rollback = false;
        try {
            context.startTransaction();
            rollback = true;

            for (Iterator<ExpiredFolder> it = folders.iterator(); active.get() && it.hasNext();) {
                ExpiredFolder expiredFolder = it.next();
                FolderID folderId = createFolderIDFor(expiredFolder.folderId);
                SearchIterator<File> si = context.fileAccess.getDocuments(folderId.toUniqueID(), Arrays.asList(Field.ID, Field.META)).results();
                List<String> toDelete = new LinkedList<>();
                boolean leftOver = false;
                boolean dropExpirationFromFolder = false;
                Long nextExpiration = null;
                try {
                    while (si.hasNext()) {
                        File file = si.next();
                        Long fileMillis = parseExpirationMillis(file.getMeta());
                        if (null == fileMillis) {
                            // File has no expiration
                            leftOver = true;
                            dropExpirationFromFolder = true;
                        } else {
                            // Check expiration against threshold
                            if (fileMillis.longValue() < threshold) {
                                // Expired...
                                toDelete.add(file.getId());
                            } else {
                                // Not yet expired
                                leftOver = true;
                                if (false == dropExpirationFromFolder && (nextExpiration == null || nextExpiration.longValue() > fileMillis.longValue())) {
                                    nextExpiration = fileMillis;
                                }
                            }
                        }
                    }
                } finally {
                    SearchIterators.close(si);
                }

                if (!toDelete.isEmpty()) {
                    context.fileAccess.removeDocument(toDelete, FileStorageFileAccess.DISTANT_FUTURE, true);
                }

                if (false == leftOver) {
                    context.folderAccess.deleteFolder(folderId.toUniqueID(), true);
                } else {
                    if (dropExpirationFromFolder) {
                        DefaultFileStorageFolder folder = new DefaultFileStorageFolder();
                        folder.setId(folderId.toUniqueID());
                        Map<String, Object> meta = new LinkedHashMap<>(expiredFolder.meta);
                        meta.remove("expiration-date-" + id);
                        folder.setMeta(meta);
                        context.folderAccess.updateFolder(folderId.toUniqueID(), folder);
                    } else if (null != nextExpiration) {
                        DefaultFileStorageFolder folder = new DefaultFileStorageFolder();
                        folder.setId(folderId.toUniqueID());
                        Map<String, Object> meta = new LinkedHashMap<>(expiredFolder.meta);
                        meta.put("expiration-date-" + id, nextExpiration);
                        folder.setMeta(meta);
                        context.folderAccess.updateFolder(folderId.toUniqueID(), folder);
                    }
                }
            }

            context.commit();
            rollback = false;
        } finally {
            if (rollback) {
                context.rollback();
            }
            context.finish();
        }
    }

    private FolderID createFolderIDFor(int folderId) {
        return new FolderID(FileID.INFOSTORE_SERVICE_ID, FileID.INFOSTORE_ACCOUNT_ID, Integer.toString(folderId));
    }

    private IDBasedFileAccess getFileAccess(Session session) throws OXException {
        IDBasedFileAccessFactory factory = ServerServiceRegistry.getServize(IDBasedFileAccessFactory.class);
        if (null == factory) {
            throw ServiceExceptionCode.absentService(IDBasedFileAccessFactory.class);
        }
        return factory.createAccess(session);
    }

    private IDBasedFolderAccess getFolderAccess(Session session) throws OXException {
        IDBasedFolderAccessFactory factory = ServerServiceRegistry.getServize(IDBasedFolderAccessFactory.class);
        if (null == factory) {
            throw ServiceExceptionCode.absentService(IDBasedFolderAccessFactory.class);
        }
        return factory.createAccess(session);
    }

    private Map<String, Object> parseMeta(ResultSet rs, int fuid, int cid) throws SQLException, OXException {
        InputStream jsonBlobStream = rs.getBinaryStream(4);
        if (!rs.wasNull() && null != jsonBlobStream) {
            try {
                return OXFolderUtility.deserializeMeta(jsonBlobStream);
            } catch (JSONException e) {
                throw OXFolderExceptionCode.FOLDER_COULD_NOT_BE_LOADED.create(e, Integer.toString(fuid), Integer.toString(cid));
            } finally {
                Streams.close(jsonBlobStream);
            }
        }
        return null;
    }

    private Long parseExpirationMillis(Map<String, Object> meta) {
        Object object = meta.get("expiration-date-" + id);
        if (object instanceof Number) {
            return Long.valueOf(((Number) object).longValue());
        }
        return null;
    }

    // ----------------------------------------------------------------------------------------------------------------------------------

    private static final class FakeSession implements Session, Serializable {

        private static final long serialVersionUID = -7064871783038587316L;

        private final int userId;
        private final int contextId;
        private final ConcurrentMap<String, Object> parameters;

        FakeSession(int userId, int contextId) {
            super();
            this.userId = userId;
            this.contextId = contextId;
            parameters = new ConcurrentHashMap<String, Object>(8, 0.9f, 1);
        }

        @Override
        public int getContextId() {
            return contextId;
        }

        @Override
        public String getLocalIp() {
            return null;
        }

        @Override
        public void setLocalIp(String ip) {
            // Nothing to do
        }

        @Override
        public String getLoginName() {
            return null;
        }

        @Override
        public boolean containsParameter(String name) {
            return parameters.containsKey(name);
        }

        @Override
        public Object getParameter(String name) {
            return parameters.get(name);
        }

        @Override
        public String getPassword() {
            return null;
        }

        @Override
        public String getRandomToken() {
            return null;
        }

        @Override
        public String getSecret() {
            return null;
        }

        @Override
        public String getSessionID() {
            return null;
        }

        @Override
        public int getUserId() {
            return userId;
        }

        @Override
        public String getUserlogin() {
            return null;
        }

        @Override
        public String getLogin() {
            return null;
        }

        @Override
        public void setParameter(String name, Object value) {
            if (null == value) {
                parameters.remove(name);
            } else {
                parameters.put(name, value);
            }
        }

        @Override
        public String getAuthId() {
            return null;
        }

        @Override
        public String getHash() {
            return null;
        }

        @Override
        public void setHash(String hash) {
            // Nope
        }

        @Override
        public String getClient() {
            return null;
        }

        @Override
        public void setClient(String client) {
            // Nothing to do
        }

        @Override
        public boolean isTransient() {
            return false;
        }

        @Override
        public boolean isStaySignedIn() {
            return false;
        }

        @Override
        public Set<String> getParameterNames() {
            return parameters.keySet();
        }

        @Override
        public Origin getOrigin() {
            return Origin.SYNTHETIC;
        }

    }

    private static final class ExpiredFolder {

        final int folderId;
        final Map<String, Object> meta;

        ExpiredFolder(int folderId, Map<String, Object> meta) {
            super();
            this.folderId = folderId;
            this.meta = meta;
        }
    }

}
