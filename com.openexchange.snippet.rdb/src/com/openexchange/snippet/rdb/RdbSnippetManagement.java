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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.snippet.rdb;

import static com.openexchange.snippet.SnippetUtils.sanitizeContent;
import static com.openexchange.snippet.rdb.Services.getService;
import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.procedure.TIntProcedure;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.commons.logging.Log;
import com.openexchange.config.cascade.ComposedConfigProperty;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.context.ContextService;
import com.openexchange.database.DatabaseService;
import com.openexchange.datatypes.genericonf.storage.GenericConfigurationStorageService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.filestore.FilestoreStorage;
import com.openexchange.id.IDGeneratorService;
import com.openexchange.mail.mime.ContentDisposition;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.session.Session;
import com.openexchange.snippet.Attachment;
import com.openexchange.snippet.DefaultAttachment;
import com.openexchange.snippet.DefaultSnippet;
import com.openexchange.snippet.GetSwitch;
import com.openexchange.snippet.Property;
import com.openexchange.snippet.ReferenceType;
import com.openexchange.snippet.Snippet;
import com.openexchange.snippet.SnippetExceptionCodes;
import com.openexchange.snippet.SnippetManagement;
import com.openexchange.tools.file.QuotaFileStorage;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link RdbSnippetManagement}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class RdbSnippetManagement implements SnippetManagement {

    private static final Log LOG = com.openexchange.log.Log.loggerFor(RdbSnippetManagement.class);

    private static DatabaseService getDatabaseService() {
        return getService(DatabaseService.class);
    }

    private static GenericConfigurationStorageService getStorageService() {
        return getService(GenericConfigurationStorageService.class);
    }

    private static IDGeneratorService getIdGeneratorService() {
        return getService(IDGeneratorService.class);
    }

    private static String extractFilename(final Attachment attachment) {
        if (null == attachment) {
            return null;
        }
        try {
            final String sContentDisposition = attachment.getContentDisposition();
            String fn = null == sContentDisposition ? null : new ContentDisposition(sContentDisposition).getFilenameParameter();
            if (fn == null) {
                final String sContentType = attachment.getContentType();
                fn = null == sContentType ? null : new ContentType(sContentType).getNameParameter();
            }
            return fn;
        } catch (final Exception e) {
            return null;
        }
    }

    private static final ConcurrentMap<Integer, QuotaFileStorage> FILE_STORE_CACHE = new ConcurrentHashMap<Integer, QuotaFileStorage>();

    private static QuotaFileStorage getFileStorage(final Context ctx) throws OXException {
        final Integer key = Integer.valueOf(ctx.getContextId());
        QuotaFileStorage qfs = FILE_STORE_CACHE.get(key);
        if (null == qfs) {
            final QuotaFileStorage quotaFileStorage = QuotaFileStorage.getInstance(FilestoreStorage.createURI(ctx), ctx);
            qfs = FILE_STORE_CACHE.putIfAbsent(key, quotaFileStorage);
            if (null == qfs) {
                qfs = quotaFileStorage;
            }
        }
        return qfs;
    }

    private final int contextId;
    private final int userId;
    private final Session session;
    private final boolean supportsAttachments;

    /**
     * Initializes a new {@link RdbSnippetManagement}.
     */
    public RdbSnippetManagement(final Session session) {
        super();
        this.session = session;
        this.userId = session.getUserId();
        this.contextId = session.getContextId();

        final ConfigViewFactory factory = Services.optService(ConfigViewFactory.class);
        if (null == factory) {
            supportsAttachments = false;
        } else {
            boolean supportsAttachments;
            try {
                final ComposedConfigProperty<Boolean> property = factory.getView(userId, contextId).property("com.openexchange.snippet.rdb.supportsAttachments", boolean.class);
                supportsAttachments = property.isDefined() ? property.get().booleanValue() : false;
            } catch (final Exception e) {
                LOG.error(e.getMessage(), e);
                supportsAttachments = false;
            }
            this.supportsAttachments = supportsAttachments;
        }
    }

    private static Context getContext(final Session session) throws OXException {
        if (session instanceof ServerSession) {
            return ((ServerSession) session).getContext();
        }
        return getService(ContextService.class).getContext(session.getContextId());
    }

    private static Context getContext(final int contextId) throws OXException {
        return getService(ContextService.class).getContext(contextId);
    }

    /**
     * {@link QuotaFileStorageStreamProvider} - File store stream provider.
     *
     * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
     */
    public static final class QuotaFileStorageStreamProvider implements DefaultAttachment.InputStreamProvider {

        private final String filestoreLocation;
        private final QuotaFileStorage fileStorage;

        /**
         * Initializes a new {@link QuotaFileStorageStreamProvider}.
         *
         * @param filestoreLocation The file store location
         * @param fileStorage The file store
         */
        public QuotaFileStorageStreamProvider(final String filestoreLocation, final QuotaFileStorage fileStorage) {
            super();
            this.filestoreLocation = filestoreLocation;
            this.fileStorage = fileStorage;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            try {
                return fileStorage.getFile(filestoreLocation);
            } catch (final OXException e) {
                throw new IOException("Loading file from file store failed.", e);
            }
        }

    }

    @Override
    public List<Snippet> getSnippets(final String... types) throws OXException {
        final DatabaseService databaseService = getDatabaseService();
        final int contextId = this.contextId;
        final Connection con = databaseService.getReadOnly(contextId);
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            final com.openexchange.java.StringAllocator sql = new com.openexchange.java.StringAllocator("SELECT id FROM snippet WHERE cid=? AND (user=? OR shared>0) AND refType=").append(ReferenceType.GENCONF.getType());
            if (null != types && types.length > 0) {
                sql.append(" AND (");
                sql.append("type=?");
                for (int i = 1; i < types.length; i++) {
                    sql.append(" OR type=?");
                }
                sql.append(')');
            }
            stmt = con.prepareStatement(sql.toString());
            int pos = 0;
            stmt.setInt(++pos, contextId);
            stmt.setInt(++pos, userId);
            if (null != types && types.length > 0) {
                for (final String type : types) {
                    stmt.setString(++pos, type);
                }
            }
            rs = stmt.executeQuery();
            if (!rs.next()) {
                return Collections.emptyList();
            }
            // Collect identifiers
            final TIntList ids = new TIntArrayList(8);
            do {
                ids.add(Integer.parseInt(rs.getString(1)));
            } while (rs.next());
            if (ids.isEmpty()) {
                return Collections.emptyList();
            }
            // Load by identifiers
            final List<Snippet> list = new ArrayList<Snippet>(ids.size());
            final AtomicReference<OXException> error = new AtomicReference<OXException>();
            ids.forEach(new TIntProcedure() {

                @Override
                public boolean execute(final int id) {
                    try {
                        list.add(getSnippet0(Integer.toString(id), con));
                        return true;
                    } catch (final OXException e) {
                        error.set(e);
                        return false;
                    }
                }
            });
            final OXException e = error.get();
            if (null != e) {
                throw e;
            }
            return list;
        } catch (final SQLException e) {
            throw SnippetExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(rs, stmt);
            databaseService.backReadOnly(contextId, con);
        }
    }

    @Override
    public List<Snippet> getOwnSnippets() throws OXException {
        final DatabaseService databaseService = getDatabaseService();
        final int contextId = this.contextId;
        final Connection con = databaseService.getReadOnly(contextId);
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            final com.openexchange.java.StringAllocator sql = new com.openexchange.java.StringAllocator("SELECT id FROM snippet WHERE cid=? AND user=? AND refType=").append(ReferenceType.GENCONF.getType());
            stmt = con.prepareStatement(sql.toString());
            int pos = 0;
            stmt.setInt(++pos, contextId);
            stmt.setInt(++pos, userId);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                return Collections.emptyList();
            }
            final TIntList ids = new TIntArrayList(8);
            do {
                ids.add(Integer.parseInt(rs.getString(1)));
            } while (rs.next());
            if (ids.isEmpty()) {
                return Collections.emptyList();
            }
            final List<Snippet> list = new ArrayList<Snippet>(ids.size());
            final AtomicReference<OXException> error = new AtomicReference<OXException>();
            ids.forEach(new TIntProcedure() {

                @Override
                public boolean execute(final int id) {
                    try {
                        list.add(getSnippet0(Integer.toString(id), con));
                        return true;
                    } catch (final OXException e) {
                        error.set(e);
                        return false;
                    }
                }
            });
            final OXException e = error.get();
            if (null != e) {
                throw e;
            }
            return list;
        } catch (final SQLException e) {
            throw SnippetExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(rs, stmt);
            databaseService.backReadOnly(contextId, con);
        }
    }

    @Override
    public Snippet getSnippet(final String identifier) throws OXException {
        final DatabaseService databaseService = getDatabaseService();
        final int contextId = this.contextId;
        final Connection con = databaseService.getReadOnly(contextId);
        try {
            return getSnippet0(identifier, con);
        } finally {
            databaseService.backReadOnly(contextId, con);
        }
    }

    Snippet getSnippet0(final String identifier, final Connection con) throws OXException {
        if (null == identifier) {
            return null;
        }
        if (null == con) {
            return getSnippet(identifier);
        }
        final int id = Integer.parseInt(identifier);
        final int contextId = this.contextId;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT accountId, displayName, module, type, shared, refId, user FROM snippet WHERE cid=? AND id=? AND refType=" + ReferenceType.GENCONF.getType());
            int pos = 0;
            stmt.setInt(++pos, contextId);
            stmt.setString(++pos, Integer.toString(id));
            rs = stmt.executeQuery();
            if (!rs.next()) {
                throw SnippetExceptionCodes.SNIPPET_NOT_FOUND.create(Integer.valueOf(id));
            }
            final DefaultSnippet snippet = new DefaultSnippet().setId(identifier).setCreatedBy(rs.getInt(7));
            {
                final int accountId = rs.getInt(1);
                if (!rs.wasNull()) {
                    snippet.setAccountId(accountId);
                }
            }
            snippet.setDisplayName(rs.getString(2));
            snippet.setModule(rs.getString(3));
            snippet.setType(rs.getString(4));
            snippet.setShared(rs.getInt(5) > 0);
            final int confId = Integer.parseInt(rs.getString(6));
            closeSQLStuff(rs, stmt);
            rs = null;
            stmt = null;
            /*
             * Load unnamed properties
             */
            final Context context = getContext(session);
            {
                final GenericConfigurationStorageService storageService = getStorageService();
                final Map<String, Object> configuration = new HashMap<String, Object>(8);
                storageService.fill(con, context, confId, configuration);
                snippet.putUnnamedProperties(configuration);
            }
            /*
             * Load content
             */
            stmt = con.prepareStatement("SELECT content FROM snippetContent WHERE cid=? AND user=? AND id=?");
            pos = 0;
            stmt.setInt(++pos, contextId);
            stmt.setInt(++pos, userId);
            stmt.setInt(++pos, id);
            rs = stmt.executeQuery();
            if (rs.next()) {
                snippet.setContent(rs.getString(1));
            }
            closeSQLStuff(rs, stmt);
            rs = null;
            stmt = null;
            /*
             * Load JSON data
             */
            stmt = con.prepareStatement("SELECT json FROM snippetMisc WHERE cid=? AND user=? AND id=?");
            pos = 0;
            stmt.setInt(++pos, contextId);
            stmt.setInt(++pos, userId);
            stmt.setInt(++pos, id);
            rs = stmt.executeQuery();
            if (rs.next()) {
                snippet.setMisc(rs.getString(1));
            }
            closeSQLStuff(rs, stmt);
            rs = null;
            stmt = null;
            /*
             * Load attachments
             */
            if (supportsAttachments) {
                stmt = con.prepareStatement("SELECT referenceId, fileName FROM snippetAttachment WHERE cid=? AND user=? AND id=?");
                pos = 0;
                stmt.setInt(++pos, contextId);
                stmt.setInt(++pos, userId);
                stmt.setInt(++pos, id);
                rs = stmt.executeQuery();
                if (rs.next()) {
                    final QuotaFileStorage fileStorage = getFileStorage(context);
                    final List<Attachment> attachments = new LinkedList<Attachment>();
                    do {
                        final String referenceId = rs.getString(1);
                        if (!rs.wasNull()) {
                            final DefaultAttachment attachment = new DefaultAttachment();
                            attachment.setId(referenceId);
                            attachment.setContentType(fileStorage.getMimeType(referenceId));
                            attachment.setContentDisposition("attachment; filename=\"" + rs.getString(2) + "\"");
                            attachment.setStreamProvider(new QuotaFileStorageStreamProvider(referenceId, fileStorage));
                            attachments.add(attachment);
                        }
                    } while (rs.next());
                    closeSQLStuff(rs, stmt);
                    rs = null;
                    stmt = null;
                    snippet.setAttachments(attachments);
                }
            }
            /*
             * Finally return snippet
             */
            return snippet;
        } catch (final SQLException e) {
            throw SnippetExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(rs, stmt);
        }
    }

    @Override
    public String createSnippet(final Snippet snippet) throws OXException {
        final DatabaseService databaseService = getDatabaseService();
        final int contextId = this.contextId;
        final Connection con = databaseService.getWritable(contextId);
        PreparedStatement stmt = null;
        boolean rollback = false;
        try {
            con.setAutoCommit(false); // BEGIN;
            rollback = true;
            /*-
             * Obtain identifier
             * 
             * Yes, please use "com.openexchange.snippet.mime" since both implementations use shared table 'snippet'.
             */
            final int id = getIdGeneratorService().getId("com.openexchange.snippet.mime", contextId);
            // Store attachments
            if (supportsAttachments) {
                final List<Attachment> attachments = snippet.getAttachments();
                updateAttachments(id, attachments, false, getContext(session), userId, contextId, con);
            }
            // Store content
            {
                final String content = sanitizeContent(snippet.getContent());
                if (null != content) {
                    stmt = con.prepareStatement("INSERT INTO snippetContent (cid, user, id, content) VALUES (?, ?, ?, ?)");
                    stmt.setInt(1, contextId);
                    stmt.setInt(2, userId);
                    stmt.setInt(3, id);
                    stmt.setString(4, content);
                    stmt.executeUpdate();
                    DBUtils.closeSQLStuff(stmt);
                    stmt = null;
                }
            }
            // Store JSON object
            {
                final Object misc = snippet.getMisc();
                if (null != misc) {
                    stmt = con.prepareStatement("INSERT INTO snippetMisc (cid, user, id, json) VALUES (?, ?, ?, ?)");
                    stmt.setInt(1, contextId);
                    stmt.setInt(2, userId);
                    stmt.setInt(3, id);
                    stmt.setString(4, misc.toString());
                    stmt.executeUpdate();
                    DBUtils.closeSQLStuff(stmt);
                    stmt = null;
                }
            }
            // Store unnamed properties
            final int confId;
            {
                final Map<String, Object> unnamedProperties = snippet.getUnnamedProperties();
                if (null == unnamedProperties || unnamedProperties.isEmpty()) {
                    confId = -1;
                } else {
                    final GenericConfigurationStorageService storageService = getStorageService();
                    final Map<String, Object> configuration = new HashMap<String, Object>(unnamedProperties);
                    confId = storageService.save(con, getContext(session), configuration);
                }
            }
            // Store snippet
            stmt = con.prepareStatement("INSERT INTO snippet (cid, user, id, accountId, displayName, module, type, shared, lastModified, refId, refType) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, "+ReferenceType.GENCONF.getType()+")");
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            stmt.setString(3, Integer.toString(id));
            {
                final int accountId = snippet.getAccountId();
                if (accountId >= 0) {
                    stmt.setInt(4, accountId);
                } else {
                    stmt.setNull(4, Types.INTEGER);
                }
            }
            stmt.setString(5, snippet.getDisplayName());
            stmt.setString(6, snippet.getModule());
            stmt.setString(7, snippet.getType());
            stmt.setInt(8, snippet.isShared() ? 1 : 0);
            stmt.setLong(9, System.currentTimeMillis());
            stmt.setString(10, Integer.toString(confId));
            stmt.executeUpdate();
            /*
             * Commit & return identifier
             */
            con.commit(); // COMMIT
            DBUtils.autocommit(con);
            rollback = false;
            return Integer.toString(id);
        } catch (final SQLException e) {
            throw SnippetExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            if (rollback) {
                DBUtils.rollback(con);
                DBUtils.autocommit(con);
            }
            DBUtils.closeSQLStuff(stmt);
            databaseService.backReadOnly(contextId, con);
        }
    }

    @Override
    public String updateSnippet(final String identifier, final Snippet snippet, final Set<Property> properties, final Collection<Attachment> addAttachments, final Collection<Attachment> removeAttachments) throws OXException {
        if (null == identifier) {
            return identifier;
        }
        final int id = Integer.parseInt(identifier);
        final DatabaseService databaseService = getDatabaseService();
        final int contextId = this.contextId;
        final Connection con = databaseService.getWritable(contextId);
        PreparedStatement stmt = null;
        boolean rollback = false;
        try {
            con.setAutoCommit(false); // BEGIN;
            rollback = true;
            /*
             * Update snippet if necessary
             */
            final UpdateSnippetBuilder updateBuilder = new UpdateSnippetBuilder();
            for (final Property property : properties) {
                property.doSwitch(updateBuilder);
            }
            final Set<Property> modifiableProperties = updateBuilder.getModifiableProperties();
            if (null != modifiableProperties && !modifiableProperties.isEmpty()) {
                stmt = con.prepareStatement(updateBuilder.getUpdateStatement());
                final GetSwitch getter = new GetSwitch(snippet);
                int pos = 0;
                for (final Property modifiableProperty : modifiableProperties) {
                    final Object value = modifiableProperty.doSwitch(getter);
                    if (null != value) {
                        stmt.setObject(++pos, value);
                    }
                }
                stmt.setLong(++pos, contextId);
                stmt.setLong(++pos, userId);
                stmt.setString(++pos, Integer.toString(id));
                if (LOG.isDebugEnabled()) {
                    final String query = stmt.toString();
                    LOG.debug(new com.openexchange.java.StringAllocator(query.length() + 32).append("Trying to perform SQL update query for attributes ").append(
                        modifiableProperties).append(" :\n").append(query.substring(query.indexOf(':') + 1)));
                }
                stmt.executeUpdate();
                closeSQLStuff(stmt);
                stmt = null;
            }
            /*
             * Update content
             */
            if (properties.contains(Property.CONTENT)) {
                String content = snippet.getContent();
                if (null == content) {
                    content = "";
                }
                stmt = con.prepareStatement("UPDATE snippetContent SET content=? WHERE cid=? AND user=? AND id=?");
                int pos = 0;
                stmt.setString(++pos, sanitizeContent(content));
                stmt.setLong(++pos, contextId);
                stmt.setLong(++pos, userId);
                stmt.setLong(++pos, id);
                stmt.executeUpdate();
                closeSQLStuff(stmt);
                stmt = null;
            }
            /*
             * Update unnamed properties
             */
            final Context context = getContext(session);
            if (properties.contains(Property.PROPERTIES)) {
                final int confId;
                {
                    ResultSet rs = null;
                    try {
                        stmt = con.prepareStatement("SELECT refId FROM snippet WHERE cid=? AND user=? AND id=? AND refType="+ReferenceType.GENCONF.getType());
                        int pos = 0;
                        stmt.setLong(++pos, contextId);
                        stmt.setLong(++pos, userId);
                        stmt.setString(++pos, Integer.toString(id));
                        rs = stmt.executeQuery();
                        confId = rs.next() ? Integer.parseInt(rs.getString(1)) : -1;
                    } finally {
                        closeSQLStuff(rs, stmt);
                        stmt = null;
                        rs = null;
                    }
                }
                if (confId > 0) {
                    final Map<String, Object> unnamedProperties = snippet.getUnnamedProperties();
                    final GenericConfigurationStorageService storageService = getStorageService();
                    if (null == unnamedProperties || unnamedProperties.isEmpty()) {
                        storageService.delete(con, context, confId);
                    } else {
                        storageService.update(con, context, confId, unnamedProperties);
                    }
                }
            }
            /*
             * Update misc
             */
            if (properties.contains(Property.MISC)) {
                final Object misc = snippet.getMisc();
                if (null == misc) {
                    stmt = con.prepareStatement("DELETE FROM snippetMisc WHERE cid=? AND user=? AND id=?");
                    int pos = 0;
                    stmt.setLong(++pos, contextId);
                    stmt.setLong(++pos, userId);
                    stmt.setLong(++pos, id);
                    stmt.executeUpdate();
                } else {
                    stmt = con.prepareStatement("UPDATE snippetMisc SET json=? WHERE cid=? AND user=? AND id=?");
                    int pos = 0;
                    stmt.setString(++pos, misc.toString());
                    stmt.setLong(++pos, contextId);
                    stmt.setLong(++pos, userId);
                    stmt.setLong(++pos, id);
                    stmt.executeUpdate();
                }
                closeSQLStuff(stmt);
                stmt = null;
            }
            /*
             * Update attachments
             */
            if (supportsAttachments) {
                if (null != removeAttachments && !removeAttachments.isEmpty()) {
                    removeAttachments(id, removeAttachments, context, userId, contextId, con);
                }
                if (null != addAttachments && !addAttachments.isEmpty()) {
                    updateAttachments(id, addAttachments, false, context, userId, contextId, con);
                }
            }
            /*
             * Commit & return
             */
            con.commit(); // COMMIT
            DBUtils.autocommit(con);
            rollback = false;
            return identifier;
        } catch (final SQLException e) {
            throw SnippetExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            if (rollback) {
                DBUtils.rollback(con);
                DBUtils.autocommit(con);
            }
            closeSQLStuff(stmt);
            databaseService.backReadOnly(contextId, con);
        }
    }

    private static void updateAttachments(final int id, final Collection<Attachment> attachments, final boolean deleteExisting, final Context context, final int userId, final int contextId, final Connection con) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            QuotaFileStorage fileStorage = null;
            if (deleteExisting) {
                // Check existing ones...
                stmt = con.prepareStatement("SELECT referenceId FROM snippetAttachment WHERE cid=? AND user=? AND id=?");
                int pos = 0;
                stmt.setLong(++pos, contextId);
                stmt.setLong(++pos, userId);
                stmt.setLong(++pos, id);
                rs = stmt.executeQuery();
                fileStorage = getFileStorage(context);
                if (rs.next()) {
                    final List<String> referenceIds = new LinkedList<String>();
                    do {
                        referenceIds.add(rs.getString(1));
                    } while (rs.next());
                    closeSQLStuff(rs, stmt);
                    rs = null;
                    stmt = null;
                    // ... and delete from file storage
                    fileStorage.deleteFiles(referenceIds.toArray(new String[referenceIds.size()]));
                    // Delete from table, too
                    stmt = con.prepareStatement("DELETE FROM snippetAttachment WHERE cid=? AND user=? AND id=?");
                    pos = 0;
                    stmt.setLong(++pos, contextId);
                    stmt.setLong(++pos, userId);
                    stmt.setLong(++pos, id);
                    stmt.executeUpdate();
                    closeSQLStuff(stmt);
                    stmt = null;
                }
            }
            // Check passed ones
            if (null != attachments && !attachments.isEmpty()) {
                if (null == fileStorage) {
                    fileStorage = getFileStorage(context);
                }
                stmt = con.prepareStatement("INSERT INTO snippetAttachment (cid, user, id, referenceId, fileName) VALUES (?, ?, ?, ?, ?)");
                for (final Attachment attachment : attachments) {
                    final String referenceId = fileStorage.saveNewFile(attachment.getInputStream());
                    stmt.setInt(1, contextId);
                    stmt.setInt(2, userId);
                    stmt.setInt(3, id);
                    stmt.setString(4, referenceId);
                    final String fileName = extractFilename(attachment);
                    if (null == fileName) {
                        stmt.setNull(5, Types.VARCHAR);
                    } else {
                        stmt.setString(5, fileName);
                    }
                    stmt.addBatch();
                }
                stmt.executeBatch();
                DBUtils.closeSQLStuff(stmt);
                stmt = null;
            }
        } catch (final SQLException e) {
            throw SnippetExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } catch (final IOException e) {
            throw SnippetExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(rs, stmt);
        }
    }

    private static void removeAttachments(final int id, final Collection<Attachment> attachments, final Context context, final int userId, final int contextId, final Connection con) throws OXException {
        PreparedStatement stmt = null;
        try {
            final QuotaFileStorage fileStorage = getFileStorage(context);
            for (final Attachment attachment : attachments) {
                final String attachmentId = attachment.getId();
                fileStorage.deleteFile(attachmentId);
                // Delete from table, too
                stmt = con.prepareStatement("DELETE FROM snippetAttachment WHERE cid=? AND user=? AND id=? AND referenceId=?");
                int pos = 0;
                stmt.setLong(++pos, contextId);
                stmt.setLong(++pos, userId);
                stmt.setLong(++pos, id);
                stmt.setString(++pos, attachmentId);
                stmt.executeUpdate();
                closeSQLStuff(stmt);
                stmt = null;
            }
        } catch (final SQLException e) {
            throw SnippetExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(stmt);
        }
    }

    @Override
    public void deleteSnippet(final String identifier) throws OXException {
        if (null == identifier) {
            return;
        }
        final int id = Integer.parseInt(identifier);
        final DatabaseService databaseService = getDatabaseService();
        final int contextId = this.contextId;
        final Connection con = databaseService.getWritable(contextId);
        boolean rollback = false;
        try {
            con.setAutoCommit(false); // BEGIN;
            rollback = true;
            deleteSnippet(id, userId, contextId, supportsAttachments, con);
            con.commit(); // COMMIT
            rollback = false;
        } catch (final SQLException e) {
            throw SnippetExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            if (rollback) {
                DBUtils.rollback(con);
            }
            DBUtils.autocommit(con);
            databaseService.backReadOnly(contextId, con);
        }
    }

    /**
     * Deletes specified snippet.
     *
     * @param id The snippet identifier
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param con The connection to use
     * @throws OXException If delete attempt fails
     */
    public static void deleteSnippet(final int id, final int userId, final int contextId, final boolean supportsAttachments, final Connection con) throws OXException {
        PreparedStatement stmt = null;
        try {
            // Delete attachments
            if (supportsAttachments) {
                updateAttachments(id, null, true, getContext(contextId), userId, contextId, con);
            }
            // Delete content
            stmt = con.prepareStatement("DELETE FROM snippetContent WHERE cid=? AND user=? AND id=?");
            int pos = 0;
            stmt.setLong(++pos, contextId);
            stmt.setLong(++pos, userId);
            stmt.setLong(++pos, id);
            stmt.executeUpdate();
            DBUtils.closeSQLStuff(stmt);
            stmt = null;
            // Delete JSON object
            stmt = con.prepareStatement("DELETE FROM snippetMisc WHERE cid=? AND user=? AND id=?");
            pos = 0;
            stmt.setLong(++pos, contextId);
            stmt.setLong(++pos, userId);
            stmt.setLong(++pos, id);
            stmt.executeUpdate();
            DBUtils.closeSQLStuff(stmt);
            stmt = null;
            // Delete unnamed properties
            final int confId;
            {
                ResultSet rs = null;
                try {
                    stmt = con.prepareStatement("SELECT refId FROM snippet WHERE cid=? AND user=? AND id=? AND refType="+ReferenceType.GENCONF.getType());
                    pos = 0;
                    stmt.setLong(++pos, contextId);
                    stmt.setLong(++pos, userId);
                    stmt.setString(++pos, Integer.toString(id));
                    rs = stmt.executeQuery();
                    confId = rs.next() ? Integer.parseInt(rs.getString(1)) : -1;
                } finally {
                    closeSQLStuff(rs, stmt);
                    stmt = null;
                    rs = null;
                }
            }
            if (confId > 0) {
                final GenericConfigurationStorageService storageService = getStorageService();
                storageService.delete(con, getContext(contextId), confId);
            }
            // Delete snippet
            stmt = con.prepareStatement("DELETE FROM snippet WHERE cid=? AND user=? AND id=? AND refType="+ReferenceType.GENCONF.getType());
            pos = 0;
            stmt.setLong(++pos, contextId);
            stmt.setLong(++pos, userId);
            stmt.setString(++pos, Integer.toString(id));
            stmt.executeUpdate();
            DBUtils.closeSQLStuff(stmt);
            stmt = null;
        } catch (final SQLException e) {
            throw SnippetExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
    }

}
