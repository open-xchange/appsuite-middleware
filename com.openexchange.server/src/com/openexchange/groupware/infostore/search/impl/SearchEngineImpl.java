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

package com.openexchange.groupware.infostore.search.impl;

import static com.openexchange.groupware.infostore.InfostoreSearchEngine.*;
import static com.openexchange.java.Autoboxing.I;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.database.Databases;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.database.tx.DBService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.EnumComponent;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.InfostoreExceptionCodes;
import com.openexchange.groupware.infostore.database.impl.DocumentMetadataImpl;
import com.openexchange.groupware.infostore.database.impl.InfostoreQueryCatalog;
import com.openexchange.groupware.infostore.database.impl.InfostoreSecurityImpl;
import com.openexchange.groupware.infostore.search.SearchTerm;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.java.AsciiReader;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorAdapter;
import com.openexchange.tools.iterator.SearchIteratorExceptionCodes;
import com.openexchange.tools.iterator.SearchIterators;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.sql.DBUtils;
import com.openexchange.tools.sql.SearchStrings;

/**
 * SearchEngineImpl
 *
 * @author <a href="mailto:benjamin.otterbach@open-xchange.com">Benjamin Otterbach</a>
 */
public class SearchEngineImpl extends DBService {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SearchEngineImpl.class);
    private final InfostoreSecurityImpl security = new InfostoreSecurityImpl();

    private static final String[] SEARCH_FIELDS = new String[] { "infostore_document.title", "infostore_document.url", "infostore_document.description", "infostore_document.categories", "infostore_document.filename", "infostore_document.file_version_comment" };

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

    /**
     * Performs a term-based search.
     *
     * @param session The session
     * @param searchTerm The search term
     * @param all A collection of folder identifiers the user is able to read "all" items from
     * @param own A collection of folder identifiers the user is able to read only "own" items from
     * @param cols The metadata to include in the results
     * @param sortedBy The field used to sort the results
     * @param dir The sort direction
     * @param start The start of the requested range
     * @param end The end of the requested range
     * @return The search results
     */
    public SearchIterator<DocumentMetadata> search(ServerSession session, SearchTerm<?> searchTerm, List<Integer> all, List<Integer> own, Metadata[] cols, Metadata sortedBy, int dir, int start, int end) throws OXException {
        ToMySqlQueryVisitor visitor = new ToMySqlQueryVisitor(session, all, own, getResultFieldsSelect(cols), sortedBy, dir, start, end);
        searchTerm.visit(visitor);
        String sqlQuery = visitor.getMySqlQuery();
        boolean successful = false;
        PreparedStatement stmt = null;
        Connection con = null;
        InfostoreSearchIterator iter = null;
        try {
            con = getReadConnection(session.getContext());
            stmt = con.prepareStatement(sqlQuery);
            iter = new InfostoreSearchIterator(stmt.executeQuery(), this, cols, session.getContext(), con, stmt);
            // Iterator has been successfully generated, thus closing DB resources is performed by iterator instance.
            successful = true;
            return iter;
        } catch (SQLException e) {
            if (e.getCause() instanceof java.net.SocketTimeoutException) {
                // Communications link failure
                throw InfostoreExceptionCodes.SEARCH_TOOK_TOO_LONG.create(e, session.getUserId(), session.getContextId(), sqlQuery);
            }
            LOG.error("", e);
            throw InfostoreExceptionCodes.SQL_PROBLEM.create(e, sqlQuery);
        } catch (OXException e) {
            LOG.error("", e);
            throw InfostoreExceptionCodes.PREFETCH_FAILED.create(e);
        } finally {
            if (!successful) {
                if (iter != null) {
                    SearchIterators.close(iter);
                } else if (con != null) {
                    releaseReadConnection(session.getContext(), con);
                    DBUtils.closeSQLStuff(stmt);
                }
            }
        }
    }

    /**
     * Performs a simple, pattern-based search.
     *
     * @param session The session
     * @param query The pattern, or <code>null</code> / <code>*</code> to search for all items
     * @param all A collection of folder identifiers the user is able to read "all" items from
     * @param own A collection of folder identifiers the user is able to read only "own" items from
     * @param cols The metadata to include in the results
     * @param sortedBy The field used to sort the results
     * @param dir The sort direction
     * @param start The start of the requested range
     * @param end The end of the requested range
     * @return The search results
     */
    public SearchIterator<DocumentMetadata> search(ServerSession session, String query, List<Integer> all, List<Integer> own, Metadata[] cols, Metadata sortedBy, int dir, int start, int end) throws OXException {
        if (Strings.isEmpty(query) || "*".equals(query)) {
            int maxResults;
            if (NOT_SET != start && NOT_SET != end && end >= start) {
                maxResults = end + 1 - start;
            } else if (NOT_SET != start) {
                maxResults = 200;
            } else if (NOT_SET != end) {
                maxResults = end + 1;
            } else {
                maxResults = NOT_SET;
            }
            if (NOT_SET != maxResults && own.size() + all.size() > maxResults && (null == sortedBy || InfostoreQueryCatalog.Table.INFOSTORE.getFieldSet().contains(sortedBy))) {
                /*
                 * no pattern, ordering possible, and more folders queried than results needed - use optimized query
                 */
                return get(session, all, own, cols, sortedBy, dir, start, end);
            }
        }

        final StringBuilder SQL_QUERY = new StringBuilder(512);
        SQL_QUERY.append(getResultFieldsSelect(cols));
        SQL_QUERY.append(" FROM infostore JOIN infostore_document ON infostore_document.cid = infostore.cid AND infostore_document.infostore_id = infostore.id AND infostore_document.version_number = infostore.version WHERE infostore.cid = ").append(session.getContextId());

        appendFolders(SQL_QUERY, session.getContextId(), session.getUserId(), all, own);

        boolean addQuery = false;
        String q = query;
        if (q.length() > 0 && !"*".equals(q)) {
            checkPatternLength(q);
            final boolean containsWildcard = q.indexOf('*') >= 0 || 0 <= q.indexOf('?');
            addQuery = true;

            q = q.replaceAll("\\\\", "\\\\\\\\");
            q = q.replaceAll("%", "\\\\%"); // Escape \ twice, due to regexp parser in replaceAll
            q = q.replace('*', '%');
            q = q.replace('?', '_');
            q = q.replaceAll("'", "\\\\'"); // Escape \ twice, due to regexp parser in replaceAll

            if (!containsWildcard) {
                q = "%" + q + "%";
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

        appendOrderBy(SQL_QUERY, sortedBy, dir);
        appendLimit(SQL_QUERY, start, end);

        {
            Connection con = getReadConnection(session.getContext());
            boolean successful = false;
            PreparedStatement stmt = null;
            InfostoreSearchIterator iter = null;
            try {
                stmt = con.prepareStatement(SQL_QUERY.toString());
                if (addQuery) {
                    for (int i = 0; i < SEARCH_FIELDS.length; i++) {
                        stmt.setString(i + 1, q);
                    }
                }
                iter = new InfostoreSearchIterator(stmt.executeQuery(), this, cols, session.getContext(), con, stmt);
                // Iterator has been successfully generated, thus closing DB resources is performed by iterator instance.
                successful = true;
                return iter;
            } catch (final SQLException e) {
                LOG.error("", e);
                throw InfostoreExceptionCodes.SQL_PROBLEM.create(e, SQL_QUERY.toString());
            } catch (final OXException e) {
                LOG.error("", e);
                throw InfostoreExceptionCodes.PREFETCH_FAILED.create(e);
            } finally {
                if (!successful) {
                    if (iter != null) {
                        SearchIterators.close(iter);
                    } else if (con != null) {
                        releaseReadConnection(session.getContext(), con);
                        DBUtils.closeSQLStuff(stmt);
                    }
                }
            }
        }
    }

    /**
     * Appends a UNION-clause to restrict the results to the supplied set of folders. An appropriate condition for the special folder
     * holding single shared files (10) is appended automatically if the <code>readAllFolders</code> collection contains it.
     *
     * @param session The requesting user's session
     * @param sqlQuery The string builder holding the current SQL query w/o WHERE
     * @param filter An optional filter expression that is supposed to be appended to WHERE clause
     * @param readAllFolders A collection of folder identifiers the user is able to read "all" items from
     * @param readOwnFolders A collection of folder identifiers the user is able to read only "own" items from
     */
    protected static void appendFoldersAsUnion(ServerSession session, StringBuilder sqlQuery, String filter, List<Integer> readAllFolders, List<Integer> readOwnFolders) {
        if (readAllFolders.isEmpty() && readOwnFolders.isEmpty()) {
            if (null != filter) {
                sqlQuery.append(" WHERE ").append(filter);
            }
            return;
        }
        int contextID = session.getContextId();
        int userID = session.getUserId();
        String prefix = sqlQuery.toString();

        boolean appendUnion = false;
        if (!readAllFolders.isEmpty()) {
            Integer sharedFilesFolderID = I(FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_ID);
            if (readAllFolders.contains(sharedFilesFolderID)) {
                // Remove virtual folder identifier
                readAllFolders = new ArrayList<Integer>(readAllFolders);
                readAllFolders.remove(sharedFilesFolderID);

                sqlQuery.append(" JOIN object_permission ON infostore.cid=object_permission.cid AND infostore.id=object_permission.object_id");
                sqlQuery.append(" WHERE infostore.cid=").append(contextID);
                sqlQuery.append(" AND object_permission.module=").append(FolderObject.INFOSTORE);
                sqlQuery.append(" AND ((group_flag<>1 AND permission_id=").append(userID).append(')');

                int[] groups = session.getUser().getGroups();
                if (null == groups || 0 == groups.length) {
                    sqlQuery.append(')');
                } else {
                    sqlQuery.append(" OR (group_flag=1 AND permission_id IN (").append(Strings.join(groups, ",")).append(")");
                }

                sqlQuery.append("))");
                if (null != filter) {
                    sqlQuery.append(" AND ").append(filter);
                }
                appendUnion = true;
            }
            if (!readAllFolders.isEmpty()) {
                if (appendUnion) {
                    sqlQuery.append(" UNION ").append(prefix);
                }

                Iterator<Integer> iter = readAllFolders.iterator();
                sqlQuery.append(" INNER JOIN (SELECT ").append(iter.next()).append(" AS fid");
                while (iter.hasNext()) {
                    sqlQuery.append(" UNION ALL SELECT ").append(iter.next());
                }
                sqlQuery.append(") AS x ON infostore.folder_id = x.fid");

                sqlQuery.append(" WHERE infostore.cid = ").append(contextID);
                if (null != filter) {
                    sqlQuery.append(" AND ").append(filter);
                }
                appendUnion = true;
            }
        }
        if (!readOwnFolders.isEmpty()) {
            if (appendUnion) {
                sqlQuery.append(" UNION ").append(prefix);
            }

            Iterator<Integer> iter = readOwnFolders.iterator();
            sqlQuery.append(" INNER JOIN (SELECT ").append(iter.next()).append(" AS fid");
            while (iter.hasNext()) {
                sqlQuery.append(" UNION ALL SELECT ").append(iter.next());
            }
            sqlQuery.append(") AS x ON infostore.folder_id = x.fid");

            sqlQuery.append(" WHERE infostore.cid = ").append(contextID);
            sqlQuery.append(" AND infostore.created_by=").append(userID);
            if (null != filter) {
                sqlQuery.append(" AND ").append(filter);
            }
        }
    }

    /**
     * Appends a WHERE-clause to restrict the results to the supplied set of folders. An appropriate condition for the special folder
     * holding single shared files (10) is appended automatically if the <code>readAllFolders</code> collection contains it.
     *
     * @param sqlQuery The string builder holding the current SQL query
     * @param contextID The context identifier
     * @param userID The identifier of the requesting user
     * @param readAllFolders A collection of folder identifiers the user is able to read "all" items from
     * @param readOwnFolders A collection of folder identifiers the user is able to read only "own" items from
     */
    protected static void appendFolders(StringBuilder sqlQuery, int contextID, int userID, List<Integer> readAllFolders, List<Integer> readOwnFolders) {
        if (0 == readAllFolders.size() && 0 == readOwnFolders.size()) {
            return;
        }
        boolean appendOr = false;
        sqlQuery.append(" AND (");
        if (0 < readAllFolders.size()) {
            Integer sharedFilesFolderID = I(FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_ID);
            if (readAllFolders.contains(sharedFilesFolderID)) {
                readAllFolders = new ArrayList<Integer>(readAllFolders);
                readAllFolders.remove(sharedFilesFolderID);
                sqlQuery.append("(infostore.id in (SELECT object_id FROM object_permission WHERE object_permission.module=").append(FolderObject.INFOSTORE).append(" AND object_permission.cid=").append(contextID).append(" AND permission_id=").append(userID).append("))");
                appendOr = true;
            }
            if (0 < readAllFolders.size()) {
                if (appendOr) {
                    sqlQuery.append(" OR ");
                }
                if (1 == readAllFolders.size()) {
                    sqlQuery.append("infostore.folder_id=").append(readAllFolders.get(0));
                } else {
                    sqlQuery.append("(infostore.folder_id IN (");
                    Strings.join(readAllFolders, ",", sqlQuery);
                    sqlQuery.append("))");
                }
                appendOr = true;
            }
        }
        if (0 < readOwnFolders.size()) {
            if (appendOr) {
                sqlQuery.append(" OR ");
            }
            sqlQuery.append("(infostore.created_by=").append(userID);
            if (1 == readOwnFolders.size()) {
                sqlQuery.append(" AND infostore.folder_id=").append(readOwnFolders.get(0)).append(')');
            } else {
                sqlQuery.append(" AND infostore.folder_id in (");
                Strings.join(readOwnFolders, ",", sqlQuery);
                sqlQuery.append("))");
            }
        }
        sqlQuery.append(')');
    }

    protected static void appendLimit(StringBuilder sqlQuery, int start, int end) {
        if ((start != NOT_SET) && (end != NOT_SET)) {
            if (end >= start) {
                sqlQuery.append(" LIMIT ");
                sqlQuery.append(start);
                sqlQuery.append(", ");
                sqlQuery.append(((end + 1) - start));
            }
        } else {
            if (start != NOT_SET) {
                sqlQuery.append(" LIMIT ");
                sqlQuery.append(start);
                sqlQuery.append(",200");
            }
            if (end != NOT_SET) {
                sqlQuery.append(" LIMIT ");
                sqlQuery.append(end + 1);
            }
        }
    }

    protected static void appendOrderBy(StringBuilder sqlQuery, Metadata sortedBy, int dir) {
        if (sortedBy != null && dir != NOT_SET) {
            final String[] orderColumn = switchMetadata2DBColumns(new Metadata[] { sortedBy });
            if ((orderColumn != null) && (orderColumn[0] != null)) {
                if (dir == DESC) {
                    sqlQuery.append(" ORDER BY ");
                    sqlQuery.append(orderColumn[0]);
                    sqlQuery.append(" DESC");
                } else if (dir == ASC) {
                    sqlQuery.append(" ORDER BY ");
                    sqlQuery.append(orderColumn[0]);
                    sqlQuery.append(" ASC");
                }
            }
        }
    }

    private SearchIterator<DocumentMetadata> get(ServerSession session, List<Integer> readAllFolders, List<Integer> readOwnFolders, Metadata[] cols, Metadata sortedBy, int dir, int start, int end) throws OXException {
        Connection connection = null;
        boolean closeResources = true;
        try {
            connection = getReadConnection(session.getContext());
            /*
             * get matching object IDs first
             */
            StringBuilder sqlQuery = new StringBuilder();
            sqlQuery.append("SELECT infostore.id FROM infostore WHERE infostore.cid=").append(session.getContextId());
            appendFolders(sqlQuery, session.getContextId(), session.getUserId(), readAllFolders, readOwnFolders);
            appendOrderBy(sqlQuery, sortedBy, dir);
            appendLimit(sqlQuery, start, end);

            List<Integer> objectIDs = new ArrayList<Integer>();
            PreparedStatement statement = null;
            ResultSet results = null;
            try {
                statement = connection.prepareStatement(sqlQuery.toString());
                results = statement.executeQuery();
                while (results.next()) {
                    objectIDs.add(results.getInt(1));
                }
            } catch (SQLException e) {
                LOG.error("", e);
                throw InfostoreExceptionCodes.SQL_PROBLEM.create(e, sqlQuery.toString());
            } finally {
                DBUtils.closeSQLStuff(results, statement);
            }
            if (0 == objectIDs.size()) {
                return SearchIteratorAdapter.emptyIterator();
            }
            /*
             * get requested metadata in a second step
             */
            sqlQuery = new StringBuilder();
            sqlQuery.append(getResultFieldsSelect(cols));
            sqlQuery.append(" FROM infostore JOIN infostore_document ON infostore_document.cid = infostore.cid AND infostore_document.infostore_id = infostore.id AND infostore_document.version_number = infostore.version WHERE infostore.cid = ").append(session.getContextId()).append(" AND infostore.id IN (").append(join(objectIDs)).append(")");
            appendOrderBy(sqlQuery, sortedBy, dir);
            PreparedStatement stmt = null;
            InfostoreSearchIterator iter = null;
            try {
                stmt = connection.prepareStatement(sqlQuery.toString());
                iter = new InfostoreSearchIterator(stmt.executeQuery(), this, cols, session.getContext(), connection, stmt);
                // Iterator has been successfully generated, thus closing DB resources is performed by iterator instance.
                closeResources = false;
                return iter;
            } catch (final SQLException e) {
                LOG.error("", e);
                throw InfostoreExceptionCodes.SQL_PROBLEM.create(e, sqlQuery.toString());
            } catch (final OXException e) {
                LOG.error("", e);
                throw InfostoreExceptionCodes.PREFETCH_FAILED.create(e);
            } finally {
                if (closeResources) {
                    if (iter != null) {
                        SearchIterators.close(iter);
                    } else if (connection != null) {
                        DBUtils.closeSQLStuff(stmt);
                    }
                }
            }
        } finally {
            if (closeResources && null != connection) {
                releaseReadConnection(session.getContext(), connection);
            }
        }
    }

    public static void checkPatternLength(final String pattern) throws OXException {
        int minimumSearchCharacters = ServerConfig.getInt(ServerConfig.Property.MINIMUM_SEARCH_CHARACTERS);
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

    public void index(final DocumentMetadata document, final Context ctx, final User user, final UserPermissionBits userPermissions) {
        // Nothing to do.
    }

    public void unIndex0r(final int id, final Context ctx, final User user, final UserPermissionBits userPermissions) {
        // Nothing to do.
    }

    private static String[] switchMetadata2DBColumns(final Metadata[] columns) {
        final List<String> retval = new ArrayList<String>(columns.length);
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
                case Metadata.META:
                    retval.add("infostore_document.meta");
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

        private Statement stmt;

        private Connection readCon;

        private ResultSet rs;

        private DocumentMetadata next;

        private final Metadata[] columns;

        private final SearchEngineImpl s;

        private final Context ctx;

        private final List<OXException> warnings;

        private final SearchIterator<DocumentMetadata> delegate;

        public InfostoreSearchIterator(final ResultSet rs, final SearchEngineImpl s, final Metadata[] columns, final Context ctx, final Connection readCon, final Statement stmt) throws OXException {
            super();
            this.warnings = new ArrayList<OXException>(2);
            this.rs = rs;
            this.s = s;
            this.columns = columns;
            this.ctx = ctx;
            this.readCon = readCon;
            this.stmt = stmt;
            SearchIterator<DocumentMetadata> delegate = null;
            try {
                if (rs.next()) {
                    // Preload?
                    if (false && Arrays.asList(columns).contains(Metadata.CONTENT_LITERAL)) { // Metadata.CONTENT_LITERAL is mapped to
                                                                                             // description in fillDocumentMetadata()
                        next = fillDocumentMetadata(new DocumentMetadataImpl(), columns, rs);
                    } else {
                        final List<DocumentMetadata> list = new LinkedList<DocumentMetadata>();

                        boolean goahead = true;
                        DocumentMetadata current = null;

                        while (goahead) {
                            current = fillDocumentMetadata(new DocumentMetadataImpl(), columns, rs);
                            NextObject: while (current == null) {
                                if (rs.next()) {
                                    current = fillDocumentMetadata(new DocumentMetadataImpl(), columns, rs);
                                } else {
                                    break NextObject;
                                }
                            }
                            if (current == null) {
                                goahead = false;
                            } else {
                                list.add(current);
                                current = null;
                                goahead = rs.next();
                            }
                            if (!goahead) {
                                close();
                            }
                        }

                        delegate = new SearchIteratorAdapter<DocumentMetadata>(list.iterator(), list.size());
                    }
                } else {
                    close();
                }
            } catch (final Exception e) {
                throw SearchIteratorExceptionCodes.SQL_ERROR.create(e, EnumComponent.INFOSTORE);
            }
            this.delegate = delegate;
        }

        @Override
        public boolean hasNext() throws OXException {
            return null == delegate ? next != null : delegate.hasNext();
        }

        @Override
        public DocumentMetadata next() throws OXException {
            if (null != delegate) {
                return delegate.next();
            }
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
        public void close() {
            next = null;

            Databases.closeSQLStuff(rs, stmt);
            stmt = null;
            rs = null;

            if (null != readCon) {
                s.releaseReadConnection(ctx, readCon);
                readCon = null;
            }
        }

        @Override
        public int size() {
            if (null != delegate) {
                return delegate.size();
            }
            return -1;
        }

        public boolean hasSize() {
            return false;
        }

        @Override
        public void addWarning(final OXException warning) {
            if (null == delegate) {
                warnings.add(warning);
            } else {
                delegate.addWarning(warning);
            }
        }

        @Override
        public OXException[] getWarnings() {
            if (null != delegate) {
                return delegate.getWarnings();
            }
            return warnings.isEmpty() ? null : warnings.toArray(new OXException[warnings.size()]);
        }

        @Override
        public boolean hasWarnings() {
            if (null != delegate) {
                return delegate.hasWarnings();
            }
            return !warnings.isEmpty();
        }

        private DocumentMetadataImpl fillDocumentMetadata(final DocumentMetadataImpl retval, final Metadata[] columns, final ResultSet result) throws SQLException, OXException {
            int columnIndex = 0;
            for (Metadata metadata : columns) {
                switch (metadata.getId()) {
                    case Metadata.LAST_MODIFIED:
                    case Metadata.LAST_MODIFIED_UTC:
                        long lastModified = result.getLong(++columnIndex);
                        retval.setLastModified(rs.wasNull() ? null : new Date(lastModified));
                        break;
                    case Metadata.CREATION_DATE:
                        long creationDate = result.getLong(++columnIndex);
                        retval.setCreationDate(rs.wasNull() ? null : new Date(creationDate));
                        break;
                    case Metadata.MODIFIED_BY:
                        retval.setModifiedBy(result.getInt(++columnIndex));
                        break;
                    case Metadata.FOLDER_ID:
                        retval.setFolderId(result.getInt(++columnIndex));
                        break;
                    case Metadata.TITLE:
                        retval.setTitle(result.getString(++columnIndex));
                        break;
                    case Metadata.VERSION:
                        retval.setVersion(result.getInt(++columnIndex));
                        break;
                    case Metadata.FILENAME:
                        retval.setFileName(result.getString(++columnIndex));
                        break;
                    case Metadata.SEQUENCE_NUMBER:
                        retval.setSequenceNumber(result.getLong(++columnIndex));
                        break;
                    case Metadata.ID:
                        retval.setId(result.getInt(++columnIndex));
                        break;
                    case Metadata.FILE_SIZE:
                        retval.setFileSize(result.getLong(++columnIndex));
                        break;
                    case Metadata.FILE_MIMETYPE:
                        retval.setFileMIMEType(result.getString(++columnIndex));
                        break;
                    case Metadata.DESCRIPTION:
                        retval.setDescription(result.getString(++columnIndex));
                        break;
                    case Metadata.LOCKED_UNTIL:
                        long lockedUntil = result.getLong(++columnIndex);
                        retval.setLockedUntil(rs.wasNull() ? null : new Date(lockedUntil));
                        break;
                    case Metadata.URL:
                        retval.setURL(result.getString(++columnIndex));
                        break;
                    case Metadata.CREATED_BY:
                        retval.setCreatedBy(result.getInt(++columnIndex));
                        break;
                    case Metadata.CATEGORIES:
                        retval.setCategories(result.getString(++columnIndex));
                        break;
                    case Metadata.FILE_MD5SUM:
                        retval.setFileMD5Sum(result.getString(++columnIndex));
                        break;
                    case Metadata.VERSION_COMMENT:
                        retval.setVersionComment(result.getString(++columnIndex));
                        break;
                    case Metadata.COLOR_LABEL:
                        retval.setColorLabel(result.getInt(++columnIndex));
                        break;
                    case Metadata.META:
                        final InputStream jsonBlobStream = rs.getBinaryStream(++columnIndex);
                        if (false == rs.wasNull() && null != jsonBlobStream) {
                            try {
                                retval.setMeta(new JSONObject(new AsciiReader(jsonBlobStream)).asMap());
                            } catch (JSONException e) {
                                throw InfostoreExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
                            } finally {
                                Streams.close(jsonBlobStream);
                            }
                        }
                        break;
                    default:
                        break;
                }
            }
            retval.setIsCurrentVersion(true);
            return retval;
        }

    }

}
