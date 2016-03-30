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

package com.openexchange.groupware.infostore.database.impl;

import static com.openexchange.tools.sql.DBUtils.getStatement;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.InfostoreExceptionCodes;
import com.openexchange.groupware.infostore.database.impl.InfostoreQueryCatalog.FieldChooser;
import com.openexchange.groupware.infostore.database.impl.InfostoreQueryCatalog.Table;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.groupware.infostore.utils.SetSwitch;
import com.openexchange.groupware.ldap.User;
import com.openexchange.java.AsciiReader;
import com.openexchange.java.Streams;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.sql.DBUtils;

public class InfostoreIterator implements SearchIterator<DocumentMetadata> {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(InfostoreIterator.class);
    private static final InfostoreQueryCatalog QUERIES = InfostoreQueryCatalog.getInstance();

    public static InfostoreIterator loadDocumentIterator(final int id, final int version, final DBProvider provider, final Context ctx) {
        final String query = QUERIES.getSelectDocument(id, version, ctx.getContextId());
        return new InfostoreIterator(query, provider, ctx, Metadata.VALUES_ARRAY, QUERIES.getChooserForVersion(version));
    }

    public static InfostoreIterator list(final int[] id, final Metadata[] metadata, final DBProvider provider, final Context ctx) {
        final String query = QUERIES.getListQuery(id,metadata,new InfostoreQueryCatalog.DocumentWins(),ctx.getContextId());
        return new InfostoreIterator(query, provider, ctx, metadata, new InfostoreQueryCatalog.DocumentWins());
    }

    public static InfostoreIterator documents(final long folderId, final Metadata[] metadata,final Metadata sort, final int order, final DBProvider provider, final Context ctx){
        final String query = QUERIES.getDocumentsQuery(folderId, metadata, sort, order, -1, -1, new InfostoreQueryCatalog.DocumentWins(), ctx.getContextId());
        return new InfostoreIterator(query, provider, ctx, metadata, new InfostoreQueryCatalog.DocumentWins());
    }

    public static InfostoreIterator documents(final long folderId, final Metadata[] metadata,final Metadata sort, final int order, int start, int end, final DBProvider provider, final Context ctx){
        final String query = QUERIES.getDocumentsQuery(folderId, metadata, sort, order, start, end, new InfostoreQueryCatalog.DocumentWins(), ctx.getContextId());
        return new InfostoreIterator(query, provider, ctx, metadata, new InfostoreQueryCatalog.DocumentWins());
    }

    public static InfostoreIterator documentsByCreator(final long folderId,final int userId, final Metadata[] metadata,final Metadata sort, final int order, final DBProvider provider, final Context ctx){
        final String query = QUERIES.getDocumentsQuery(folderId,userId, metadata, sort, order, -1, -1, new InfostoreQueryCatalog.DocumentWins(), ctx.getContextId());
        return new InfostoreIterator(query, provider, ctx, metadata, new InfostoreQueryCatalog.DocumentWins());
    }

    public static InfostoreIterator documentsByCreator(final long folderId,final int userId, final Metadata[] metadata,final Metadata sort, final int order, int start, int end, final DBProvider provider, final Context ctx){
        final String query = QUERIES.getDocumentsQuery(folderId,userId, metadata, sort, order, start, end, new InfostoreQueryCatalog.DocumentWins(), ctx.getContextId());
        return new InfostoreIterator(query, provider, ctx, metadata, new InfostoreQueryCatalog.DocumentWins());
    }

    public static InfostoreIterator versions(final int id, final Metadata[] metadata, final Metadata sort, final int order, final DBProvider provider, final Context ctx) {
        final String query = QUERIES.getVersionsQuery(id, metadata, sort, order, new InfostoreQueryCatalog.VersionWins(), ctx.getContextId());
        return new InfostoreIterator(query, provider, ctx, metadata, new InfostoreQueryCatalog.VersionWins());
    }

    public static InfostoreIterator newDocuments(final long folderId, final Metadata[] metadata, final Metadata sort, final int order, final long since, final DBProvider provider, final Context ctx) {
        final String query = QUERIES.getNewDocumentsQuery(folderId,since, metadata, sort, order, new InfostoreQueryCatalog.DocumentWins(), ctx.getContextId());
        return new InfostoreIterator(query, provider, ctx, metadata, new InfostoreQueryCatalog.DocumentWins());
    }

    public static InfostoreIterator modifiedDocuments(final long folderId, final Metadata[] metadata, final Metadata sort, final int order, final long since, final DBProvider provider, final Context ctx) {
        final String query = QUERIES.getModifiedDocumentsQuery(folderId,since, metadata, sort, order, new InfostoreQueryCatalog.DocumentWins(), ctx.getContextId());
        return new InfostoreIterator(query, provider, ctx, metadata, new InfostoreQueryCatalog.DocumentWins());
    }

    public static InfostoreIterator deletedDocuments(final long folderId, final Metadata sort, final int order, final long since, final DBProvider provider, final Context ctx) {
        final String query = QUERIES.getDeletedDocumentsQuery(folderId,since, sort, order, new InfostoreQueryCatalog.DocumentWins(), ctx.getContextId());
        return new InfostoreIterator(query, provider, ctx, new Metadata[]{Metadata.ID_LITERAL, Metadata.FOLDER_ID_LITERAL}, new InfostoreQueryCatalog.DocumentWins());
    }

    public static InfostoreIterator newDocumentsByCreator(final long folderId,final int userId, final Metadata[] metadata, final Metadata sort, final int order, final long since, final DBProvider provider, final Context ctx) {
        final String query = QUERIES.getNewDocumentsQuery(folderId,userId, since, metadata, sort, order, new InfostoreQueryCatalog.DocumentWins(), ctx.getContextId());
        return new InfostoreIterator(query, provider, ctx, metadata, new InfostoreQueryCatalog.DocumentWins());
    }

    public static InfostoreIterator modifiedDocumentsByCreator(final long folderId,final int userId, final Metadata[] metadata, final Metadata sort, final int order, final long since, final DBProvider provider, final Context ctx) {
        final String query = QUERIES.getModifiedDocumentsQuery(folderId,userId, since, metadata, sort, order, new InfostoreQueryCatalog.DocumentWins(), ctx.getContextId());
        return new InfostoreIterator(query, provider, ctx, metadata, new InfostoreQueryCatalog.DocumentWins());
    }

    public static InfostoreIterator deletedDocumentsByCreator(final long folderId,final int userId, final Metadata sort, final int order, final long since, final DBProvider provider, final Context ctx) {
        final String query = QUERIES.getDeletedDocumentsQuery(folderId,userId, since, sort, order, new InfostoreQueryCatalog.DocumentWins(), ctx.getContextId());
        return new InfostoreIterator(query, provider, ctx, new Metadata[]{Metadata.ID_LITERAL, Metadata.FOLDER_ID_LITERAL}, new InfostoreQueryCatalog.DocumentWins());
    }

    public static InfostoreIterator allDocumentsWhere(final String where, final Metadata[] metadata, final DBProvider provider, final Context ctx){
        final String query = QUERIES.getAllDocumentsQuery(where,metadata,new InfostoreQueryCatalog.DocumentWins(), ctx.getContextId());
        return new InfostoreIterator(query, provider, ctx, metadata, new InfostoreQueryCatalog.DocumentWins());
    }

    public static InfostoreIterator allVersionsWhere(final String where, final Metadata[] metadata, final DBProvider provider, final Context ctx){
        final String query = QUERIES.getAllVersionsQuery(where,metadata,new InfostoreQueryCatalog.VersionWins(), ctx.getContextId());
        return new InfostoreIterator(query, provider, ctx, metadata, new InfostoreQueryCatalog.VersionWins());
    }

    public static InfostoreIterator documentsByFilename(final long folderId, final String filename, final Metadata[] metadata, final DBProvider provider, final Context ctx){
        final String query = QUERIES.getCurrentFilenameQuery(folderId,metadata,new InfostoreQueryCatalog.DocumentWins(), ctx.getContextId());
        return new InfostoreIterator(query, provider, ctx, metadata, new InfostoreQueryCatalog.DocumentWins(), filename);
    }

    public static InfostoreIterator sharedDocumentsForUser(final Context ctx, final User user, final int leastPermission, final Metadata[] metadata, Metadata sort, int order, int start, int end, final DBProvider provider) {
        final String query = QUERIES.getSharedDocumentsForUserQuery(ctx.getContextId(), user.getId(), user.getGroups(), leastPermission, metadata, sort, order, start, end, new InfostoreQueryCatalog.DocumentWins());
        return new InfostoreIterator(query, provider, ctx, metadata, new InfostoreQueryCatalog.DocumentWins());
    }

    public static InfostoreIterator sharedDocumentsByUser(Context ctx, User user, Metadata[] metadata, Metadata sort, int order, int start, int end, DBProvider provider) {
        String query = QUERIES.getSharedDocumentsByUserQuery(ctx.getContextId(), user.getId(), metadata, sort, order, start, end, new InfostoreQueryCatalog.DocumentWins());
        return new InfostoreIterator(query, provider, ctx, metadata, new InfostoreQueryCatalog.DocumentWins());
    }

    public static InfostoreIterator newSharedDocumentsForUser(final Context ctx, final User user, final Metadata[] metadata, final Metadata sort, final int order, final long since, final DBProvider provider) {
        final String query = QUERIES.getNewSharedDocumentsSince(ctx.getContextId(), user.getId(), user.getGroups(), since, metadata, sort, order, new InfostoreQueryCatalog.DocumentWins());
        return new InfostoreIterator(query, provider, ctx, metadata, new InfostoreQueryCatalog.DocumentWins());
    }

    public static InfostoreIterator modifiedSharedDocumentsForUser(final Context ctx, final User user, final Metadata[] metadata, final Metadata sort, final int order, final long since, final DBProvider provider) {
        final String query = QUERIES.getModifiedSharedDocumentsSince(ctx.getContextId(), user.getId(), user.getGroups(), since, metadata, sort, order, new InfostoreQueryCatalog.DocumentWins());
        return new InfostoreIterator(query, provider, ctx, metadata, new InfostoreQueryCatalog.DocumentWins());
    }

    public static InfostoreIterator deletedSharedDocumentsForUser(final Context ctx, final User user, final Metadata[] metadata, final Metadata sort, final int order, final long since, final DBProvider provider) {
        final String query = QUERIES.getDeletedSharedDocumentsSince(ctx.getContextId(), user.getId(), user.getGroups(), since, metadata, sort, order, new InfostoreQueryCatalog.DocumentWins());
        return new InfostoreIterator(query, provider, ctx, metadata, new InfostoreQueryCatalog.DocumentWins());
    }

    private final Object[] args;
    private final DBProvider provider;
    private final String query;
    private boolean queried;
    private boolean initNext;
    private PreparedStatement stmt;
    private Connection con;
    private ResultSet rs;
    private boolean next;
    private OXException exception;
    private final List<OXException> warnings;
    private DocumentCustomizer cutomizer;
    private final Context ctx;
    private final Metadata[] fields;
    private final FieldChooser chooser;

    protected InfostoreIterator(final String query,final DBProvider provider, final Context ctx, final Metadata[] fields, final FieldChooser chooser, final Object...args){
        this.warnings =  new ArrayList<OXException>(2);
        this.query = query;
        this.provider = provider;
        this.args = args;
        this.ctx = ctx;
        this.fields = fields;
        this.chooser = chooser;
    }

    @Override
    public void close() {
        if (rs == null) {
            return;
        }

        try {
            DBUtils.closeSQLStuff(rs, stmt);
            provider.releaseReadConnection(ctx, con);
        } finally {
            con = null;
            stmt = null;
            rs = null;
        }
    }

    @Override
    public boolean hasNext() throws OXException {
        if(!queried) {
            query();
        }
        if(exception != null) {
            return true;
        }
        if(initNext) {
            Statement stmt = null;
            try {
                stmt = rs.getStatement();
                next = rs.next();
                if(!next) {
                    close();
                }
            } catch (final SQLException e) {
                this.exception = InfostoreExceptionCodes.SQL_PROBLEM.create(e, getStatement(stmt));
            }
        }
        initNext = false;
        return next;
    }

    @Override
    public void addWarning(final OXException warning) {
        warnings.add(warning);
    }

    @Override
    public OXException[] getWarnings() {
        return warnings.isEmpty() ? null : warnings.toArray(new OXException[warnings.size()]);
    }

    @Override
    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }

    public void setCustomizer(DocumentCustomizer customizer) {
        this.cutomizer = customizer;
    }

    private void query() {
        queried = true;
        initNext=true;
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        boolean close = true;
        try{
            con = provider.getReadConnection(ctx);
            stmt = con.prepareStatement(query);
            int i = 1;
            for(final Object arg : args) {
                stmt.setObject(i++,arg);
            }
            LOG.trace("{}", stmt);
            rs = stmt.executeQuery();
            close = false;
            this.stmt = stmt;
            this.rs = rs;
            this.con = con;
        } catch (final SQLException x) {
            if(stmt != null) {
                DBUtils.closeSQLStuff(null, stmt);
            }
            if(con != null) {
                provider.releaseReadConnection(ctx, con);
            }
            this.exception = InfostoreExceptionCodes.SQL_PROBLEM.create(x, getStatement(stmt, query));
        } catch (final OXException e) {
            this.exception =e;
        } finally {
            if (close) {
                DBUtils.closeSQLStuff(rs, stmt);
                provider.releaseReadConnection(ctx, con);
            }
        }
    }

    public boolean hasSize() {
        return false;
    }

    @Override
    public DocumentMetadata next() throws OXException {
        hasNext();
        if(exception != null) {
            throw exception;
        }
        initNext = true;

        return getDocument();
    }

    private DocumentMetadata getDocument() throws OXException {
        final DocumentMetadata dm = new DocumentMetadataImpl();
        final SetSwitch set = new SetSwitch(dm);
        final StringBuilder sb = new StringBuilder(100);
        SetValues: for (final Metadata m : fields) {
            if (m == Metadata.CURRENT_VERSION_LITERAL) {
                Statement stmt = null;
                try {
                    stmt = rs.getStatement();
                    dm.setIsCurrentVersion(rs.getBoolean("current_version"));
                    continue SetValues;
                } catch (final SQLException e) {
                    throw InfostoreExceptionCodes.SQL_PROBLEM.create(e, getStatement(stmt));
                }
            }
            final Table t = chooser.choose(m);
            final String colName = (String) m.doSwitch(t.getFieldSwitcher());
            if (colName == null) {
                continue;
            }
            Statement stmt = null;
            try {
                stmt = rs.getStatement();

                final String column = sb.append(t.getTablename()).append('.').append(colName).toString();
                if (m == Metadata.META_LITERAL) {
                    final InputStream jsonBlobStream = rs.getBinaryStream(column);
                    if (!rs.wasNull() && null != jsonBlobStream) {
                        try {
                            set.setValue(new JSONObject(new AsciiReader(jsonBlobStream)).asMap());
                        } catch (final JSONException e) {
                            throw InfostoreExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
                        } finally {
                            Streams.close(jsonBlobStream);
                        }
                    } else {
                        set.setValue(null);
                    }
                } else {
                    set.setValue(process(m, rs.getObject(column)));
                }
                sb.setLength(0);
            } catch (final SQLException e) {
                throw InfostoreExceptionCodes.SQL_PROBLEM.create(e, getStatement(stmt));
            }
            m.doSwitch(set);
        }

        if (cutomizer != null) {
            return cutomizer.handle(dm);
        }
        return dm;
    }

    private Object process(final Metadata m, final Object object) {
        switch(m.getId()) {
        default : return object;
        case Metadata.LAST_MODIFIED : case Metadata.CREATION_DATE : case Metadata.LAST_MODIFIED_UTC: return new Date(((Long)object).longValue());
        case Metadata.MODIFIED_BY : case Metadata.CREATED_BY : case Metadata.VERSION : case Metadata.ID:case  Metadata.COLOR_LABEL:
            return Integer.valueOf(((Long)object).intValue());
        }
    }

    @Override
    public int size() {
        return -1;
    }


    public List<DocumentMetadata> asList() throws OXException {
        try {
            final List<DocumentMetadata> result = new ArrayList<DocumentMetadata>();
            while(hasNext()) {
                result.add(next());
            }

            return result;
        } finally {
            close();
        }
    }

}
