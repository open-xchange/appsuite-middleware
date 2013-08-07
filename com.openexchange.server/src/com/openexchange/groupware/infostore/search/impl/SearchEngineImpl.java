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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.groupware.infostore.search.impl;

import static com.openexchange.java.Autoboxing.I;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Queue;
import org.apache.commons.logging.Log;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.database.tx.DBService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.EnumComponent;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.InfostoreExceptionCodes;
import com.openexchange.groupware.infostore.InfostoreSearchEngine;
import com.openexchange.groupware.infostore.database.impl.DocumentMetadataImpl;
import com.openexchange.groupware.infostore.database.impl.InfostoreSecurityImpl;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.tools.iterator.FolderObjectIterator;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.java.StringAllocator;
import com.openexchange.log.LogFactory;
import com.openexchange.server.impl.EffectivePermission;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorAdapter;
import com.openexchange.tools.iterator.SearchIteratorExceptionCodes;
import com.openexchange.tools.oxfolder.OXFolderIteratorSQL;
import com.openexchange.tools.sql.SearchStrings;

/**
 * SearchEngineImpl
 *
 * @author <a href="mailto:benjamin.otterbach@open-xchange.com">Benjamin Otterbach</a>
 */
public class SearchEngineImpl extends DBService implements InfostoreSearchEngine {

    static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(SearchEngineImpl.class));

    private final InfostoreSecurityImpl security = new InfostoreSecurityImpl();

    private static final String[] SEARCH_FIELDS = new String[] {
        "infostore_document.title", "infostore_document.url", "infostore_document.description", "infostore_document.categories",
        "infostore_document.filename", "infostore_document.file_version_comment" };

    public SearchEngineImpl() {
        super(null);
    }

    public SearchEngineImpl(final DBProvider provider) {
        super(provider);
        security.setProvider(provider);
    }

    @Override
    public void setProvider(final DBProvider provider) {
        super.setProvider(provider);
        if (security != null) {
            security.setProvider(provider);
        }
    }

    @Override
    public SearchIterator<DocumentMetadata> search(String query, final Metadata[] cols, final int folderId, final Metadata sortedBy, final int dir, final int start, final int end, final Context ctx, final User user, final UserPermissionBits userPermissions) throws OXException {

        List<Integer> all = new ArrayList<Integer>();
        List<Integer> own = new ArrayList<Integer>();

        boolean addQuery = false;
        Connection con = null;
        boolean keepConnection = false;
        try {
        	con = getReadConnection(ctx);
	        {
	        	final int userId = user.getId();
	            if (folderId == NOT_SET || folderId == NO_FOLDER) {
	                final Queue<FolderObject> queue = ((FolderObjectIterator) OXFolderIteratorSQL.getAllVisibleFoldersIteratorOfModule(
	                    userId,
	                    user.getGroups(),
	                    userPermissions.getAccessibleModules(),
	                    FolderObject.INFOSTORE,
	                    ctx, con)).asQueue();
	                for (final FolderObject folder : queue) {
                        final EffectivePermission perm = folder.getEffectiveUserPermission(userId, userPermissions);
                        if (perm.canReadAllObjects()) {
                            all.add(Integer.valueOf(folder.getObjectID()));
                        } else if (perm.canReadOwnObjects()) {
                            own.add(Integer.valueOf(folder.getObjectID()));
                        }
	                }
	            } else {
	                final EffectivePermission perm = security.getFolderPermission(folderId, ctx, user, userPermissions, con);
	                if (perm.canReadAllObjects()) {
                        all.add(Integer.valueOf(folderId));
                    } else if (perm.canReadOwnObjects()) {
                        own.add(Integer.valueOf(folderId));
                    } else {
                        return SearchIteratorAdapter.emptyIterator();
                    }
	            }
	            if (all.isEmpty() && own.isEmpty()) {
	                return SearchIteratorAdapter.emptyIterator();
	            }
	            all = Collections.unmodifiableList(all);
	            own = Collections.unmodifiableList(own);
	        }

	        final StringAllocator SQL_QUERY = new StringAllocator(512);
	        SQL_QUERY.append(getResultFieldsSelect(cols));
	        SQL_QUERY.append(
	            " FROM infostore JOIN infostore_document ON infostore_document.cid = infostore.cid AND infostore_document.infostore_id = infostore.id AND infostore_document.version_number = infostore.version WHERE infostore.cid = ").append(
	            ctx.getContextId());
	        boolean needOr = false;

	        if (!all.isEmpty()) {
	            SQL_QUERY.append(" AND ((infostore.folder_id IN (").append(join(all)).append("))");
	            needOr = true;
	        }

	        if (!own.isEmpty()) {
	            if (needOr) {
	                SQL_QUERY.append(" OR ");
	            } else {
	                SQL_QUERY.append(" AND (");
	            }
	            SQL_QUERY.append("(infostore.created_by = ").append(user.getId()).append(" AND infostore.folder_id in (").append(join(own)).append(
	                ")))");
	        } else {
	            SQL_QUERY.append(')');
	        }
	        if (query.length() > 0 && !"*".equals(query)) {
	            checkPatternLength(query);
	            final boolean containsWildcard = query.indexOf('*') >= 0;
	            addQuery = true;

	            query = query.replaceAll("\\\\", "\\\\\\\\");
	            query = query.replaceAll("%", "\\\\%"); // Escape \ twice, due to regexp parser in replaceAll
	            query = query.replace('*', '%');
	            query = query.replace('?', '_');
	            query = query.replaceAll("'", "\\\\'"); // Escape \ twice, due to regexp parser in replaceAll

	            if (!containsWildcard) {
	                query = "%" + query + "%";
	            }

	            final StringBuffer SQL_QUERY_OBJECTS = new StringBuffer();
	            for (final String currentField : SEARCH_FIELDS) {
	                if (SQL_QUERY_OBJECTS.length() > 0) {
	                    SQL_QUERY_OBJECTS.append(" OR ");
	                }

	                SQL_QUERY_OBJECTS.append(currentField);
	                SQL_QUERY_OBJECTS.append(" LIKE (?)");
	            }
	            if (SQL_QUERY_OBJECTS.length() > 0) {
	                SQL_QUERY.append(" AND (");
	                SQL_QUERY.append(SQL_QUERY_OBJECTS);
	                SQL_QUERY.append(") ");
	            }
	        }

	        if (sortedBy != null && dir != NOT_SET) {
	            final String[] orderColumn = switchMetadata2DBColumns(new Metadata[] { sortedBy });
	            if ((orderColumn != null) && (orderColumn[0] != null)) {
	                if (dir == DESC) {
	                    SQL_QUERY.append(" ORDER BY ");
	                    SQL_QUERY.append(orderColumn[0]);
	                    SQL_QUERY.append(" DESC");
	                } else if (dir == ASC) {
	                    SQL_QUERY.append(" ORDER BY ");
	                    SQL_QUERY.append(orderColumn[0]);
	                    SQL_QUERY.append(" ASC");
	                }
	            }
	        }

	        if ((start != NOT_SET) && (end != NOT_SET)) {
	            if (end >= start) {
	                SQL_QUERY.append(" LIMIT ");
	                SQL_QUERY.append(start);
	                SQL_QUERY.append(", ");
	                SQL_QUERY.append(((end + 1) - start));
	            }
	        } else {
	            if (start != NOT_SET) {
	                SQL_QUERY.append(" LIMIT ");
	                SQL_QUERY.append(start);
	                SQL_QUERY.append(",200");
	            }
	            if (end != NOT_SET) {
	                SQL_QUERY.append(" LIMIT ");
	                SQL_QUERY.append(end + 1);
	            }
	        }

	        PreparedStatement stmt = null;
	        try {
	            stmt = con.prepareStatement(SQL_QUERY.toString());
	            if(addQuery) {
	                for(int i = 0; i < SEARCH_FIELDS.length; i++) {
	                    stmt.setString(i+1, query);
	                }
	            }
	            keepConnection = true;
	            return new InfostoreSearchIterator(stmt.executeQuery(), this, cols, ctx, con, stmt);
	        } catch (final SQLException e) {
	            LOG.error(e.getMessage(), e);
	            throw InfostoreExceptionCodes.SQL_PROBLEM.create(e, SQL_QUERY.toString());
	        } catch (final OXException e) {
	            LOG.error(e.getMessage(), e);
	            throw InfostoreExceptionCodes.PREFETCH_FAILED.create(e);
	        }
        } finally {
        	if (con != null && !keepConnection) {
        		releaseReadConnection(ctx, con);
        	}
        }
    }

    public static void checkPatternLength(final String pattern) throws OXException {
        final int minimumSearchCharacters;
        try {
            minimumSearchCharacters = ServerConfig.getInt(ServerConfig.Property.MINIMUM_SEARCH_CHARACTERS);
        } catch (final OXException e) {
            throw e;
        }
        if (0 == minimumSearchCharacters) {
            return;
        }
        if (null != pattern && SearchStrings.lengthWithoutWildcards(pattern) < minimumSearchCharacters) {
            throw InfostoreExceptionCodes.PATTERN_NEEDS_MORE_CHARACTERS.create(I(minimumSearchCharacters));
        }
    }

    private String join(final List<Integer> all) {
        final StringBuffer joined = new StringBuffer();
        for (final Integer i : all) {
            joined.append(i.toString());
            joined.append(',');
        }
        joined.setLength(joined.length() - 1);
        return joined.toString();
    }

    @Override
    public void index(final DocumentMetadata document, final Context ctx, final User user, final UserPermissionBits userPermissions) {
        // Nothing to do.
    }

    @Override
    public void unIndex0r(final int id, final Context ctx, final User user, final UserPermissionBits userPermissions) {
        // Nothing to do.
    }

    private String[] switchMetadata2DBColumns(final Metadata[] columns) {
        final List<String> retval = new ArrayList<String>();
        for (final Metadata current : columns) {
            Metadata2DBSwitch: switch (current.getId()) {
            default:
                break Metadata2DBSwitch;
            case Metadata.LAST_MODIFIED:
                retval.add("infostore.last_modified");
                break Metadata2DBSwitch;
            case Metadata.LAST_MODIFIED_UTC:
                retval.add("infostore.last_modified");
                break Metadata2DBSwitch;
            case Metadata.CREATION_DATE:
                retval.add("infostore.creating_date");
                break Metadata2DBSwitch;
            case Metadata.MODIFIED_BY:
                retval.add("infostore.changed_by");
                break Metadata2DBSwitch;
            case Metadata.FOLDER_ID:
                retval.add("infostore.folder_id");
                break Metadata2DBSwitch;
            case Metadata.TITLE:
                retval.add("infostore_document.title");
                break Metadata2DBSwitch;
            case Metadata.VERSION:
                retval.add("infostore.version");
                break Metadata2DBSwitch;
            case Metadata.CONTENT:
                retval.add("infostore_document.description");
                break Metadata2DBSwitch;
            case Metadata.FILENAME:
                retval.add("infostore_document.filename");
                break Metadata2DBSwitch;
            case Metadata.SEQUENCE_NUMBER:
                retval.add("infostore.id");
                break Metadata2DBSwitch;
            case Metadata.ID:
                retval.add("infostore.id");
                break Metadata2DBSwitch;
            case Metadata.FILE_SIZE:
                retval.add("infostore_document.file_size");
                break Metadata2DBSwitch;
            case Metadata.FILE_MIMETYPE:
                retval.add("infostore_document.file_mimetype");
                break Metadata2DBSwitch;
            case Metadata.DESCRIPTION:
                retval.add("infostore_document.description");
                break Metadata2DBSwitch;
            case Metadata.LOCKED_UNTIL:
                retval.add("infostore.locked_until");
                break Metadata2DBSwitch;
            case Metadata.URL:
                retval.add("infostore_document.url");
                break Metadata2DBSwitch;
            case Metadata.CREATED_BY:
                retval.add("infostore.created_by");
                break Metadata2DBSwitch;
            case Metadata.CATEGORIES:
                retval.add("infostore_document.categories");
                break Metadata2DBSwitch;
            case Metadata.FILE_MD5SUM:
                retval.add("infostore_document.file_md5sum");
                break Metadata2DBSwitch;
            case Metadata.VERSION_COMMENT:
                retval.add("infostore_document.file_version_comment");
                break Metadata2DBSwitch;
            case Metadata.COLOR_LABEL:
                retval.add("infostore.color_label");
                break Metadata2DBSwitch;
            }
        }
        return (retval.toArray(new String[0]));
    }

    private String getResultFieldsSelect(final Metadata[] RESULT_FIELDS) {
        final String[] DB_RESULT_FIELDS = switchMetadata2DBColumns(RESULT_FIELDS);

        final StringBuilder selectFields = new StringBuilder();
        boolean id = false;
        for (String currentField : DB_RESULT_FIELDS) {
            if (currentField.equals("infostore.id")) {
                currentField = "infostore.id";
                id = true;
            }
            selectFields.append(currentField);
            selectFields.append(", ");
        }
        if (!id) {
            selectFields.append("infostore.id,");
        }

        String retval = "";
        if (selectFields.length() > 0) {
            retval = "SELECT DISTINCT " + selectFields.toString();
            retval = retval.substring(0, retval.lastIndexOf(", "));
        }
        return retval;
    }

    public static class InfostoreSearchIterator implements SearchIterator<DocumentMetadata> {

        private DocumentMetadata next;

        private ResultSet rs;

        private final Metadata[] columns;

        private final SearchEngineImpl s;

        private final Context ctx;

        private Connection readCon;

        private Statement stmt;

        private final List<OXException> warnings;

        public InfostoreSearchIterator(final ResultSet rs, final SearchEngineImpl s, final Metadata[] columns, final Context ctx, final Connection readCon, final Statement stmt) throws OXException {
            this.warnings = new ArrayList<OXException>(2);
            this.rs = rs;
            this.s = s;
            this.columns = columns;
            this.ctx = ctx;
            this.readCon = readCon;
            this.stmt = stmt;
            try {
                if (rs.next()) {
                    next = fillDocumentMetadata(new DocumentMetadataImpl(), columns, rs);
                } else {
                    close();
                }
            } catch (final Exception e) {
                throw SearchIteratorExceptionCodes.SQL_ERROR.create(e, EnumComponent.INFOSTORE);
            }
        }

        @Override
        public boolean hasNext() throws OXException {
            return next != null;
        }

        @Override
        public DocumentMetadata next() throws OXException, OXException {
            try {
                DocumentMetadata retval = null;
                retval = next;
                if (rs.next()) {
                    next = fillDocumentMetadata(new DocumentMetadataImpl(), columns, rs);
                    NextObject: while (next == null) {
                        if (rs.next()) {
                            next = fillDocumentMetadata(new DocumentMetadataImpl(), columns, rs);
                        } else {
                            break NextObject;
                        }
                    }
                    if (next == null) {
                        close();
                    }
                } else {
                    close();
                }
                return retval;
            } catch (final Exception exc) {
                throw SearchIteratorExceptionCodes.SQL_ERROR.create(exc, EnumComponent.INFOSTORE);
            }
        }

        @Override
        public void close() throws OXException {
            next = null;
            try {
                if (rs != null) {
                    rs.close();
                }
                rs = null;
            } catch (final SQLException e) {
                LOG.debug("", e);
            }

            try {
                if (stmt != null) {
                    stmt.close();
                }
                stmt = null;
            } catch (final SQLException e) {
                LOG.debug("", e);
            }

            if (null != readCon) {
                s.releaseReadConnection(ctx, readCon);
                readCon = null;
            }
        }

        @Override
        public int size() {
            return -1;
        }

        public boolean hasSize() {
            return false;
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

        private DocumentMetadataImpl fillDocumentMetadata(final DocumentMetadataImpl retval, final Metadata[] columns, final ResultSet result) throws SQLException {
            for (int i = 0; i < columns.length; i++) {
                FillDocumentMetadata: switch (columns[i].getId()) {
                default:
                    break FillDocumentMetadata;
                case Metadata.LAST_MODIFIED:
                    retval.setLastModified(new Date(result.getLong(i + 1)));
                    break FillDocumentMetadata;
                case Metadata.LAST_MODIFIED_UTC:
                    retval.setLastModified(new Date(result.getLong(i + 1)));
                    break FillDocumentMetadata;
                case Metadata.CREATION_DATE:
                    retval.setCreationDate(new Date(result.getLong(i + 1)));
                    break FillDocumentMetadata;
                case Metadata.MODIFIED_BY:
                    retval.setModifiedBy(result.getInt(i + 1));
                    break FillDocumentMetadata;
                case Metadata.FOLDER_ID:
                    retval.setFolderId(result.getInt(i + 1));
                    break FillDocumentMetadata;
                case Metadata.TITLE:
                    retval.setTitle(result.getString(i + 1));
                    break FillDocumentMetadata;
                case Metadata.VERSION:
                    retval.setVersion(result.getInt(i + 1));
                    break FillDocumentMetadata;
                case Metadata.CONTENT:
                    retval.setDescription(result.getString(i + 1));
                    break FillDocumentMetadata;
                case Metadata.FILENAME:
                    retval.setFileName(result.getString(i + 1));
                    break FillDocumentMetadata;
                case Metadata.SEQUENCE_NUMBER:
                    retval.setId(result.getInt(i + 1));
                    break FillDocumentMetadata;
                case Metadata.ID:
                    retval.setId(result.getInt(i + 1));
                    break FillDocumentMetadata;
                case Metadata.FILE_SIZE:
                    retval.setFileSize(result.getInt(i + 1));
                    break FillDocumentMetadata;
                case Metadata.FILE_MIMETYPE:
                    retval.setFileMIMEType(result.getString(i + 1));
                    break FillDocumentMetadata;
                case Metadata.DESCRIPTION:
                    retval.setDescription(result.getString(i + 1));
                    break FillDocumentMetadata;
                case Metadata.LOCKED_UNTIL:
                    retval.setLockedUntil(new Date(result.getLong(i + 1)));
                    if (result.wasNull()) {
                        retval.setLockedUntil(null);
                    }
                    break FillDocumentMetadata;
                case Metadata.URL:
                    retval.setURL(result.getString(i + 1));
                    break FillDocumentMetadata;
                case Metadata.CREATED_BY:
                    retval.setCreatedBy(result.getInt(i + 1));
                    break FillDocumentMetadata;
                case Metadata.CATEGORIES:
                    retval.setCategories(result.getString(i + 1));
                    break FillDocumentMetadata;
                case Metadata.FILE_MD5SUM:
                    retval.setFileMD5Sum(result.getString(i + 1));
                    break FillDocumentMetadata;
                case Metadata.VERSION_COMMENT:
                    retval.setVersionComment(result.getString(i + 1));
                    break FillDocumentMetadata;
                case Metadata.COLOR_LABEL:
                    retval.setColorLabel(result.getInt(i + 1));
                    break FillDocumentMetadata;
                }
            }
            retval.setIsCurrentVersion(true);

            return retval;
        }

    }

}
