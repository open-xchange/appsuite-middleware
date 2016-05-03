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
 *    trademarks of the OX Software GmbH. group of companies.
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.json.JSONException;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.Category;
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
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.java.Streams;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.json.compose.share.DefaultAttachmentStorage.DefaultAttachmentStorageContext;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.services.ServerServiceRegistry;
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
public class DefaultAttachmentStoragePeriodicCleaner implements Runnable {

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
    public void run() {
        long start = System.currentTimeMillis();
        try {
            DatabaseService databaseService = ServerServiceRegistry.getInstance().getService(DatabaseService.class);
            if (null == databaseService) {
                throw ServiceExceptionCode.absentService(DatabaseService.class);
            }

            List<Integer> contextsIdInDifferentSchemas = new LinkedList<Integer>();
            {
                List<Integer> contextIds = ContextStorage.getInstance().getAllContextIds();
                int size = contextIds.size();
                Set<Integer> processed = new HashSet<Integer>(size, 0.9F);
                Iterator<Integer> iter = contextIds.iterator();
                for (int k = size; k-- > 0;) {
                    Integer contextId = iter.next();
                    if (processed.add(contextId)) {
                        contextsIdInDifferentSchemas.add(contextId);
                        for (int contextInSameSchema : databaseService.getContextsInSameSchema(contextId.intValue())) {
                            processed.add(I(contextInSameSchema));
                        }
                    }
                }
            }

            int size = contextsIdInDifferentSchemas.size();
            LOG.info("Periodic cleanup task for shared mail attachments starts. Going to check {} schemas...", I(size));

            long logTimeDistance = TimeUnit.SECONDS.toMillis(10);
            long lastLogTime = start;
            Thread currentThread = Thread.currentThread();

            Iterator<Integer> iter = contextsIdInDifferentSchemas.iterator();
            for (int i = 0, k = size; k-- > 0; i++) {
                int contextIdInSchema = iter.next().intValue();
                String schemaName = databaseService.getSchemaName(contextIdInSchema);
                for (int retry = 3; retry-- > 0;) {
                    if (currentThread.isInterrupted() || false == active.get()) {
                        LOG.info("Periodic cleanup task for shared mail attachments interrupted or stopped.");
                        return;
                    }

                    long now = System.currentTimeMillis();
                    if (now > lastLogTime + logTimeDistance) {
                        LOG.info("Periodic share cleanup task {}% finished ({}/{}).", I(i * 100 / size), I(i), I(size)); lastLogTime = now;
                    }

                    try {
                        cleanupSchema(contextIdInSchema, start, schemaName, databaseService);
                        break;
                    } catch (OXException e) {
                        if (Category.CATEGORY_TRY_AGAIN.equals(e.getCategory()) && retry > 0) {
                            long delay = 10000 + retry * 20000;
                            LOG.debug("Error during periodic cleanup task for shared mail attachments for schema {}: {}; trying again in {}ms...", schemaName, e.getMessage(), L(delay));
                            Thread.sleep(delay);
                        } else {
                            LOG.error("Error during periodic cleanup task for shared mail attachments for schema {}", schemaName, e);
                            break;
                        }
                    }
                }
            }
        }  catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.warn("Interrupted during periodic cleanup task for shared mail attachments: {}", e.getMessage(), e);
        } catch (Exception e) {
            LOG.error("Error during periodic cleanup task for shared mail attachments: {}", e.getMessage(), e);
        }
        LOG.info("Periodic cleanup task for shared mail attachments finished after {}ms.", L(System.currentTimeMillis() - start));
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
     * @param contextIdInSchema The identifier of a contact in the schema
     * @param threshold The threshold date
     * @param schemaName The name of the processed database schema
     * @param databaseService The database service to use
     */
    private void cleanupSchema(int contextIdInSchema, long threshold, String schemaName, DatabaseService databaseService) throws OXException {
        Map<Integer, Map<Integer, List<ExpiredFolder>>> contextExpiredFolders = null;

        Connection con = databaseService.getReadOnly(contextIdInSchema);
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT cid, fuid, created_from, meta FROM oxfolder_tree WHERE meta LIKE '%\"expiration-date-%'");
            rs = stmt.executeQuery();
            if (false == rs.next()) {
                return;
            }

            contextExpiredFolders = new LinkedHashMap<Integer, Map<Integer, List<ExpiredFolder>>>();
            do {
                int contextId = rs.getInt(1);
                int fuid = rs.getInt(2);
                int owner = rs.getInt(3);
                Map<String, Object> meta = parseMeta(rs, fuid, contextId);
                Long millis = parseExpirationMillis(meta);
                if ((null != millis) && (millis.longValue() < threshold)) {
                    Map<Integer, List<ExpiredFolder>> userExpiredFolders = contextExpiredFolders.get(I(contextId));
                    if (null == userExpiredFolders) {
                        userExpiredFolders = new LinkedHashMap<>();
                        contextExpiredFolders.put(I(contextId), userExpiredFolders);
                    }

                    List<ExpiredFolder> folderIds = userExpiredFolders.get(I(owner));
                    if (null == folderIds) {
                        folderIds = new LinkedList<>();
                        userExpiredFolders.put(I(owner), folderIds);
                    }

                    folderIds.add(new ExpiredFolder(fuid, meta));
                }
            } while (rs.next());
        } catch (OXException e) {
            throw e;
        } catch (Exception e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, "Unexpected error during cleanup of shared mail attachments in schema \"" + schemaName + "\"");
        } finally {
            Databases.closeSQLStuff(rs, stmt);
            databaseService.backReadOnly(contextIdInSchema, con);
        }

        cleanupExpiredFolders(contextExpiredFolders, threshold);
    }

    private void cleanupExpiredFolders(Map<Integer, Map<Integer, List<ExpiredFolder>>> contextExpiredFolders, long threshold) throws OXException {
        for (Map.Entry<Integer, Map<Integer, List<ExpiredFolder>>> contextEntry : contextExpiredFolders.entrySet()) {
            int contextId = contextEntry.getKey().intValue();
            for (Map.Entry<Integer, List<ExpiredFolder>> userEntry : contextEntry.getValue().entrySet()) {
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

            for (ExpiredFolder expiredFolder : folders) {
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
        public void setLocalIp(final String ip) {
            // Nothing to do
        }

        @Override
        public String getLoginName() {
            return null;
        }

        @Override
        public boolean containsParameter(final String name) {
            return parameters.containsKey(name);
        }

        @Override
        public Object getParameter(final String name) {
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
        public void setParameter(final String name, final Object value) {
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
        public void setHash(final String hash) {
            // Nope
        }

        @Override
        public String getClient() {
            return null;
        }

        @Override
        public void setClient(final String client) {
            // Nothing to do
        }

        @Override
        public boolean isTransient() {
            return false;
        }

        @Override
        public Set<String> getParameterNames() {
            return parameters.keySet();
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
