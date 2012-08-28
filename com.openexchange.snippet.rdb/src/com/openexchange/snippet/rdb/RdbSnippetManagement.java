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

import static com.openexchange.snippet.rdb.Services.getService;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.openexchange.context.ContextService;
import com.openexchange.database.DatabaseService;
import com.openexchange.datatypes.genericonf.storage.GenericConfigurationStorageService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.filestore.FilestoreStorage;
import com.openexchange.session.Session;
import com.openexchange.snippet.Attachment;
import com.openexchange.snippet.DefaultAttachment;
import com.openexchange.snippet.DefaultSnippet;
import com.openexchange.snippet.Snippet;
import com.openexchange.snippet.SnippetExceptionCodes;
import com.openexchange.snippet.SnippetManagement;
import com.openexchange.snippet.SnippetProperties;
import com.openexchange.tools.file.QuotaFileStorage;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.sql.DBUtils;


/**
 * {@link RdbSnippetManagement}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class RdbSnippetManagement implements SnippetManagement {

    private static DatabaseService getDatabaseService() {
        return getService(DatabaseService.class);
    }

    private static GenericConfigurationStorageService getStorageService() {
        return getService(GenericConfigurationStorageService.class);
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

    /**
     * Initializes a new {@link RdbSnippetManagement}.
     */
    public RdbSnippetManagement(final Session session) {
        super();
        this.session = session;
        this.userId = session.getUserId();
        this.contextId = session.getContextId();
    }

    private static Context getContext(final Session session) throws OXException {
        if (session instanceof ServerSession) {
            return ((ServerSession) session).getContext();
        }
        return getService(ContextService.class).getContext(session.getContextId());
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
        public QuotaFileStorageStreamProvider(String filestoreLocation, QuotaFileStorage fileStorage) {
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
    public Snippet getSnippet(final int id) throws OXException {
        final DatabaseService databaseService = getDatabaseService();
        final int contextId = this.contextId;
        final Connection con = databaseService.getReadOnly(contextId);
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT accountId, displayName, module, type, shared, confId FROM snippet WHERE cid=? AND user=? AND id=?");
            int pos = 0;
            stmt.setInt(++pos, contextId);
            stmt.setInt(++pos, userId);
            stmt.setInt(++pos, id);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                throw SnippetExceptionCodes.SNIPPET_NOT_FOUND.create(Integer.valueOf(id));
            }
            final DefaultSnippet snippet = new DefaultSnippet();
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
            final int confId = rs.getInt(6);
            DBUtils.closeSQLStuff(rs, stmt);
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
            DBUtils.closeSQLStuff(rs, stmt);
            rs = null;
            stmt = null;
            /*
             * Load attachments
             */
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
                        attachment.setContentType(fileStorage.getMimeType(referenceId));
                        attachment.setContentDisposition("attachment; filename=\"" + rs.getString(2) + "\"");
                        attachment.setStreamProvider(new QuotaFileStorageStreamProvider(referenceId, fileStorage));
                        attachments.add(attachment);
                    }
                } while (rs.next());
                DBUtils.closeSQLStuff(rs, stmt);
                rs = null;
                stmt = null;
                snippet.setAttachments(attachments);
            }
            /*
             * Finally return snippet
             */
            return snippet;
        } catch (final SQLException e) {
            throw SnippetExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
            databaseService.backReadOnly(contextId, con);
        }
    }

    @Override
    public int createSnippet(final Snippet snippet) throws OXException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void updateSnippet(final int id, final Snippet snippet, final Set<SnippetProperties> properties) throws OXException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void deleteSnippet(final int id) throws OXException {
        // TODO Auto-generated method stub
        
    }

}
