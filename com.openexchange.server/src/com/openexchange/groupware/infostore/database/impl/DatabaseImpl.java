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

package com.openexchange.groupware.infostore.database.impl;

import static com.openexchange.tools.sql.DBUtils.getStatement;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import org.apache.commons.logging.Log;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.database.tx.DBService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.filestore.FilestoreStorage;
import com.openexchange.groupware.impl.IDGenerator;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.InfostoreExceptionCodes;
import com.openexchange.groupware.infostore.InfostoreFacade;
import com.openexchange.groupware.infostore.InfostoreTimedResult;
import com.openexchange.groupware.infostore.utils.DelUserFolderDiscoverer;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.groupware.infostore.webdav.EntityLockManager;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.results.Delta;
import com.openexchange.groupware.results.DeltaImpl;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.java.Autoboxing;
import com.openexchange.log.LogFactory;
import com.openexchange.tools.file.FileStorage;
import com.openexchange.tools.file.QuotaFileStorage;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorAdapter;
import com.openexchange.tools.iterator.SearchIteratorExceptionCodes;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.session.ServerSession;

public class DatabaseImpl extends DBService {

    private static final String TABLE_DEL_INFOSTORE_DOCUMENT = "del_infostore_document";

    private static final String TABLE_DEL_INFOSTORE = "del_infostore";

    private static final String[] DEL_TABLES = new String[] { TABLE_DEL_INFOSTORE, TABLE_DEL_INFOSTORE_DOCUMENT };

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(DatabaseImpl.class));

    private final static int DOCUMENT_VERSION_NUMBER_WITHOUT_FILE = 0;

    protected static final int INFOSTORE_cid = 0;

    protected static final int INFOSTORE_id = 1;

    protected static final int INFOSTORE_folder_id = 2;

    protected static final int INFOSTORE_version = 3;

    protected static final int INFOSTORE_locked_until = 4;

    protected static final int INFOSTORE_creating_date = 5;

    protected static final int INFOSTORE_last_modified = 6;

    protected static final int INFOSTORE_created_by = 7;

    protected static final int INFOSTORE_changed_by = 8;

    protected static final int INFOSTORE_color_label = 9;

    protected static final int INFOSTORE_DOCUMENT_cid = 10;

    protected static final int INFOSTORE_DOCUMENT_infostore_id = 11;

    protected static final int INFOSTORE_DOCUMENT_version_number = 12;

    protected static final int INFOSTORE_DOCUMENT_creating_date = 13;

    protected static final int INFOSTORE_DOCUMENT_last_modified = 14;

    protected static final int INFOSTORE_DOCUMENT_created_by = 15;

    protected static final int INFOSTORE_DOCUMENT_changed_by = 16;

    protected static final int INFOSTORE_DOCUMENT_title = 17;

    protected static final int INFOSTORE_DOCUMENT_url = 18;

    protected static final int INFOSTORE_DOCUMENT_description = 19;

    protected static final int INFOSTORE_DOCUMENT_categories = 20;

    protected static final int INFOSTORE_DOCUMENT_filename = 21;

    protected static final int INFOSTORE_DOCUMENT_file_store_location = 22;

    protected static final int INFOSTORE_DOCUMENT_file_size = 23;

    protected static final int INFOSTORE_DOCUMENT_file_mimetype = 24;

    protected static final int INFOSTORE_DOCUMENT_file_md5sum = 25;

    protected static final int INFOSTORE_DOCUMENT_file_version_comment = 26;

    private static enum Fetch {
        PREFETCH, CLOSE_LATER, CLOSE_IMMEDIATELY
    }

    private static final Fetch FETCH = Fetch.PREFETCH;

    private FetchMode fetchMode;

    public DatabaseImpl() {
        this(null);
    }

    public DatabaseImpl(final DBProvider provider) {
        super(provider);

        switch (FETCH) {
        case PREFETCH:
            fetchMode = new PrefetchMode();
            break;
        case CLOSE_LATER:
            fetchMode = new CloseLaterMode();
            break;
        case CLOSE_IMMEDIATELY:
            fetchMode = new CloseImmediatelyMode();
            break;
        default:
            fetchMode = new PrefetchMode();
        }
    }

    private static final String[] INFOSTORE_DATACOLUMNS = new String[] {
        "infostore.cid", "infostore.id", "infostore.folder_id", "infostore.version", "infostore.locked_until", "infostore.creating_date",
        "infostore.last_modified", "infostore.created_by", "infostore.changed_by", "infostore.color_label", "infostore_document.cid",
        "infostore_document.infostore_id", "infostore_document.version_number", "infostore_document.creating_date",
        "infostore_document.last_modified", "infostore_document.created_by", "infostore_document.changed_by", "infostore_document.title",
        "infostore_document.url", "infostore_document.description", "infostore_document.categories", "infostore_document.filename",
        "infostore_document.file_store_location", "infostore_document.file_size", "infostore_document.file_mimetype",
        "infostore_document.file_md5sum", "infostore_document.file_version_comment" };

    private final ThreadLocal<List<String>> fileIdAddList = new ThreadLocal<List<String>>();

    private final ThreadLocal<List<String>> fileIdRemoveList = new ThreadLocal<List<String>>();

    private final ThreadLocal<Context> ctxHolder = new ThreadLocal<Context>();

    public boolean exists(final int id, final int version, final Context ctx) throws OXException {
        boolean retval = false;

        final Connection con = getReadConnection(ctx);

        final StringBuilder sql = new StringBuilder();
        if (version == InfostoreFacade.CURRENT_VERSION) {
            sql.append("SELECT id from infostore WHERE cid=? AND id=?");
        } else {
            sql.append("SELECT infostore_id FROM infostore_document WHERE cid=? AND infostore_id=? AND version_number=?");
        }
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement(sql.toString());
            stmt.setInt(1, ctx.getContextId());
            stmt.setInt(2, id);
            if (version != InfostoreFacade.CURRENT_VERSION) {
                stmt.setInt(3, version);
            }
            result = stmt.executeQuery();
            if (result.next()) {
                retval = true;
            }
            result.close();
            stmt.close();
        } catch (final SQLException e) {
            LOG.error(e.getMessage(), e);
            throw InfostoreExceptionCodes.SQL_PROBLEM.create(e, getStatement(stmt));
        } finally {
            close(stmt, result);
            releaseReadConnection(ctx, con);
        }

        return retval;
    }

    public DocumentMetadata getDocumentMetadata(final int id, final int version, final Context ctx) throws OXException {
        DocumentMetadataImpl dmi = new DocumentMetadataImpl();
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet result = null;
        String versionString = null;
        if (version == InfostoreFacade.CURRENT_VERSION) {
            versionString = "infostore.version";
        } else {
            versionString = Integer.toString(version);
        }
        try {
            final int[] columns = switchMetadata2DBColumns(Metadata.VALUES_ARRAY, false);
            con = getReadConnection(ctx);
            stmt = con.prepareStatement(getSQLSelectForInfostoreColumns(columns, false).toString() + " FROM infostore JOIN infostore_document ON infostore.cid=? AND infostore.id=? AND infostore_document.infostore_id=infostore.id AND infostore_document.cid=? AND infostore_document.version_number = " + versionString);
            stmt.setInt(1, ctx.getContextId());
            stmt.setInt(2, id);
            stmt.setInt(3, ctx.getContextId());
            result = stmt.executeQuery();
            if (result.next()) {
                dmi = fillDocumentMetadata(dmi = new DocumentMetadataImpl(), columns, result);
            }
            result.close();
            stmt.close();
        } catch (final SQLException e) {
            LOG.error(e.getMessage(), e);
            throw InfostoreExceptionCodes.SQL_PROBLEM.create(e, getStatement(stmt));
        } finally {
            close(stmt, result);
            if (con != null) {
                releaseReadConnection(ctx, con);
            }
        }

        if (dmi != null) {
            dmi.setSequenceNumber(System.currentTimeMillis());
        }

        return dmi;
    }

    public List<DocumentMetadata> getAllVersions(final Context ctx, final Metadata[] columns, final String where) throws OXException {
        final List<DocumentMetadata> result = new ArrayList<DocumentMetadata>();

        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            con = getReadConnection(ctx);
            stmt = con.prepareStatement(getSQLSelectForInfostoreColumns(switchMetadata2DBColumns(columns, true), false).toString() + " FROM infostore JOIN infostore_document ON infostore.cid=? AND infostore_document.infostore_id=infostore.id AND infostore_document.cid=? WHERE " + where);
            stmt.setInt(1, ctx.getContextId());
            stmt.setInt(2, ctx.getContextId());
            rs = stmt.executeQuery();
            while (rs.next()) {
                final DocumentMetadataImpl dm = new DocumentMetadataImpl();
                fillDocumentMetadata(dm, switchMetadata2DBColumns(columns, true), rs);
                result.add(dm);
            }
        } catch (final SQLException e) {
            LOG.error("SQLException", e);
        } finally {
            close(stmt, rs);
            releaseReadConnection(ctx, con);
        }

        return result;
    }

    public List<DocumentMetadata> getAllDocuments(final Context ctx, final Metadata[] columns, final String where) throws OXException {
        final List<DocumentMetadata> result = new ArrayList<DocumentMetadata>();

        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            con = getReadConnection(ctx);
            stmt = con.prepareStatement(getSQLSelectForInfostoreColumns(switchMetadata2DBColumns(columns, false), false).toString() + " FROM infostore JOIN infostore_document ON infostore.cid=? AND infostore_document.infostore_id=infostore.id AND infostore_document.cid=? AND infostore_document.version_number = infostore.version WHERE " + where);
            stmt.setInt(1, ctx.getContextId());
            stmt.setInt(2, ctx.getContextId());
            rs = stmt.executeQuery();
            while (rs.next()) {
                final DocumentMetadataImpl dm = new DocumentMetadataImpl();
                fillDocumentMetadata(dm, switchMetadata2DBColumns(columns, true), rs);
                result.add(dm);
            }
        } catch (final SQLException e) {
            LOG.error("SQLException", e);
        } finally {
            close(stmt, rs);
            releaseReadConnection(ctx, con);
        }

        return result;
    }

    public InputStream getDocument(final int id, final int version, final Context ctx) throws OXException {
        InputStream retval = null;

        final StringBuilder sql = new StringBuilder();
        if (version != -1) {
            sql.append("SELECT file_store_location FROM infostore_document WHERE cid=? AND infostore_id=? AND version_number=? AND file_store_location is not null");
        } else {
            sql.append("SELECT infostore_document.file_store_location from infostore_document JOIN infostore ON infostore.cid=? AND infostore.id=? AND infostore_document.cid=? AND infostore_document.infostore_id=? AND infostore_document.version_number=infostore.version AND file_store_location is not null");
        }
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            con = getReadConnection(ctx);

            stmt = con.prepareStatement(sql.toString());
            stmt.setInt(1, ctx.getContextId());
            stmt.setInt(2, id);
            if (version != -1) {
                stmt.setInt(3, version);
            } else {
                stmt.setInt(3, ctx.getContextId());
                stmt.setInt(4, id);
            }
            result = stmt.executeQuery();
            if (result.next()) {
                final FileStorage fs = QuotaFileStorage.getInstance(FilestoreStorage.createURI(ctx), ctx);
                retval = fs.getFile(result.getString(1));
                fs.close();
            }
        } catch (final SQLException x) {
            throw InfostoreExceptionCodes.SQL_PROBLEM.create(x, getStatement(stmt));
        } finally {
            close(stmt, result);
            releaseReadConnection(ctx, con);
        }
        return retval;
    }

    public int[] removeDocument(final String identifier, final Context ctx) throws OXException {
        return removeDocumentFromTable(identifier, ctx, "infostore");
    }

    public int[] removeDelDocument(final String identifier, final Context ctx) throws OXException {
        return removeDocumentFromTable(identifier, ctx, TABLE_DEL_INFOSTORE);
    }

    /**
     * @param identifier The file identifier
     * @param ctx The Context from which it should be deleted
     * @param basetablename The basename of the table e.g. del_infostore the fitting database names (del_infostore_documents etc. are
     *            self-build
     * @return The number of changed entries in the basetable in int[0] and the number of deleted entries in int[1]
     * @throws OXException
     */
    private int[] removeDocumentFromTable(final String identifier, final Context ctx, final String basetablename) throws OXException {
        /*
         * When deleting the fitting version line for the identifier we have to check the basetable, too. In the basetable we set the
         * current version to the right value. This value is determined by the sorted set.
         */
        final String documentstable = basetablename.concat("_document");

        final Connection writecon = getWriteConnection(ctx);

        final int[] retval = new int[2];
        int version_nr = 0;
        int current_version = 0;
        int infostore_id = 0;

        final StringBuilder version_select = new StringBuilder(
            "SELECT version_number, infostore_id, version FROM " + documentstable + " JOIN " + basetablename + " ON " + basetablename + ".id = " + documentstable + ".infostore_id " + " AND file_store_location=? AND " + documentstable + ".cid=?");
        final StringBuilder query_all_versions = new StringBuilder(
            "SELECT version_number FROM " + documentstable + " WHERE infostore_id=? AND cid=?");
        final StringBuilder deletefromdocumentstable = new StringBuilder(
            "DELETE FROM " + documentstable + " WHERE cid=? AND file_store_location=?");
        final StringBuilder changeversioninbasetable = new StringBuilder(
            "UPDATE " + basetablename + " SET version=? " + " WHERE id=? AND  " + basetablename + ".cid=?");

        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            startDBTransaction();
            stmt = writecon.prepareStatement(version_select.toString());
            stmt.setString(1, identifier);
            stmt.setInt(2, ctx.getContextId());
            result = stmt.executeQuery();
            if (result.next()) {
                version_nr = result.getInt(1);
                infostore_id = result.getInt(2);
                current_version = result.getInt(3);
            }
            result.close();
            stmt.close();

            // Now we have to check if the version number is the active one
            stmt = writecon.prepareStatement(query_all_versions.toString());
            stmt.setInt(1, infostore_id);
            stmt.setInt(2, ctx.getContextId());
            result = stmt.executeQuery();
            final SortedSet<Integer> set = new TreeSet<Integer>();
            while (result.next()) {
                set.add(Integer.valueOf(result.getInt(1)));
            }
            set.remove(Integer.valueOf(version_nr));
            stmt.close();

            if (version_nr == current_version) {
                stmt = writecon.prepareStatement(changeversioninbasetable.toString());
                stmt.setInt(1, set.last().intValue());
                stmt.setInt(2, infostore_id);
                stmt.setInt(3, ctx.getContextId());
                retval[0] = stmt.executeUpdate();
                stmt.close();
            }

            stmt = writecon.prepareStatement(deletefromdocumentstable.toString());
            stmt.setInt(1, ctx.getContextId());
            stmt.setString(2, identifier);
            retval[1] = stmt.executeUpdate();
            stmt.close();
            commitDBTransaction();
        } catch (final SQLException e) {
            try {
                rollbackDBTransaction();
            } catch (final OXException e1) {
                throw e1;
            }
            throw InfostoreExceptionCodes.SQL_PROBLEM.create(e, getStatement(stmt));
        } finally {
            close(stmt, result);
            finishDBTransaction();
            releaseWriteConnection(ctx, writecon);
        }
        return retval;
    }

    public int modifyDocument(final String oldidentifier, final String newidentifier, final String description, final String mimetype, final Context ctx) throws OXException {
        return modifyDocumentInTable(oldidentifier, newidentifier, description, mimetype, ctx, "infostore");
    }

    public int modifyDelDocument(final String oldidentifier, final String newidentifier, final String description, final String mimetype, final Context ctx) throws OXException {
        return modifyDocumentInTable(oldidentifier, newidentifier, description, mimetype, ctx, TABLE_DEL_INFOSTORE);
    }

    private int modifyDocumentInTable(final String oldidentifier, final String newidentifier, final String description, final String mimetype, final Context ctx, final String basetablename) throws OXException {
        final String documentstable = basetablename.concat("_document");
        int retval = -1;
        final Connection writecon = getWriteConnection(ctx);

        final StringBuilder select_description = new StringBuilder(
            "SELECT description FROM " + documentstable + " WHERE file_store_location=? AND cid=?");

        final StringBuilder updatedatabase = new StringBuilder(
            "UPDATE " + documentstable + " SET file_store_location=?,  description=?, file_mimetype=? " + "WHERE file_store_location=? AND cid=?");

        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            startDBTransaction();
            String olddescription = null;
            stmt = writecon.prepareStatement(select_description.toString());
            stmt.setString(1, oldidentifier);
            stmt.setInt(2, ctx.getContextId());
            result = stmt.executeQuery();
            if (result.next()) {
                olddescription = result.getString(1);
            }
            stmt.close();
            if (olddescription == null) {
                olddescription = description;
            } else {
                olddescription = olddescription.concat(description);
            }

            stmt = writecon.prepareStatement(updatedatabase.toString());
            stmt.setString(1, newidentifier);
            stmt.setString(2, olddescription);
            stmt.setString(3, mimetype);
            stmt.setString(4, oldidentifier);
            stmt.setInt(5, ctx.getContextId());
            retval = stmt.executeUpdate();
            stmt.close();
            commitDBTransaction();
        } catch (final SQLException e) {
            try {
                rollbackDBTransaction();
            } catch (final OXException e1) {
                throw e1;
            }
            throw InfostoreExceptionCodes.SQL_PROBLEM.create(e, getStatement(stmt));
        } finally {
            close(stmt, result);
            finishDBTransaction();
            releaseWriteConnection(ctx, writecon);
        }
        return retval;
    }

    public int[] saveDocumentMetadata(final String identifier, final DocumentMetadata document, final User user, final Context ctx) throws OXException {
        final int[] retval = new int[3];
        PreparedStatement stmt = null;
        Connection writeCon = null;
        try {
            startDBTransaction();
            final int folder_id = new OXFolderAccess(ctx).getDefaultFolder(user.getId(), FolderObject.INFOSTORE).getObjectID();
            writeCon = getWriteConnection(ctx);

            final int infostore_id = IDGenerator.getId(ctx, Types.INFOSTORE, writeCon);

            final Date date = new Date(System.currentTimeMillis());

            stmt = writeCon.prepareStatement("INSERT INTO infostore (cid, id, folder_id, version, locked_until, color_label, creating_date, last_modified, created_by, changed_by) VALUES (?,?,?,?,?,?,?,?,?,?)");
            stmt.setInt(1, ctx.getContextId());
            stmt.setInt(2, infostore_id);
            stmt.setInt(3, folder_id);
            // Because we definitely have a file, we set version number to 1
            stmt.setInt(4, 1);
            stmt.setNull(5, java.sql.Types.INTEGER);
            stmt.setLong(6, document.getColorLabel());
            stmt.setLong(7, date.getTime());
            stmt.setLong(8, date.getTime());
            stmt.setInt(9, user.getId());
            stmt.setInt(10, user.getId());
            retval[0] = stmt.executeUpdate();
            stmt.close();

            stmt = writeCon.prepareStatement("INSERT INTO infostore_document (cid, infostore_id, version_number, creating_date, last_modified, created_by, changed_by, title, url, description, categories, filename) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)");
            stmt.setInt(1, ctx.getContextId());
            stmt.setInt(2, infostore_id);
            stmt.setInt(3, DOCUMENT_VERSION_NUMBER_WITHOUT_FILE);
            stmt.setLong(4, date.getTime());
            stmt.setLong(5, date.getTime());
            stmt.setInt(6, user.getId());
            stmt.setInt(7, user.getId());
            stmt.setString(8, document.getTitle());
            stmt.setString(9, document.getURL());
            stmt.setString(10, document.getDescription());
            stmt.setString(11, document.getCategories());
            stmt.setString(12, document.getFileName());
            retval[1] = stmt.executeUpdate();
            stmt.close();

            stmt = writeCon.prepareStatement("INSERT INTO infostore_document (cid, infostore_id, version_number, creating_date, last_modified, created_by, changed_by, title, url, description, categories, filename, file_store_location, file_size, file_mimetype, file_md5sum) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
            stmt.setInt(1, ctx.getContextId());
            stmt.setInt(2, infostore_id);
            stmt.setInt(3, 1);
            stmt.setLong(4, date.getTime());
            stmt.setLong(5, date.getTime());
            stmt.setInt(6, user.getId());
            stmt.setInt(7, user.getId());
            stmt.setString(8, document.getTitle());
            stmt.setString(9, document.getURL());
            stmt.setString(10, document.getDescription());
            stmt.setString(11, document.getCategories());
            stmt.setString(12, document.getFileName());
            stmt.setString(13, identifier);
            stmt.setString(14, Long.toString(document.getFileSize()));
            stmt.setString(15, document.getFileMIMEType());
            stmt.setString(16, document.getFileMD5Sum());
            retval[2] = stmt.executeUpdate();
            document.setVersion(1);
            document.setId(infostore_id);
            commitDBTransaction();
        } catch (final SQLException e) {
            try {
                rollbackDBTransaction();
            } catch (final OXException e1) {
                throw e1;
            }
            LOG.error(e.getMessage(), e);
            throw InfostoreExceptionCodes.SQL_PROBLEM.create(e, getStatement(stmt));
        } catch (final OXException e) {
            try {
                rollbackDBTransaction();
            } catch (final OXException e1) {
                throw e1;
            }
            LOG.error(e.getMessage(), e);
            throw e;
        } finally {
            close(stmt, null);
            finishDBTransaction();
            releaseWriteConnection(ctx, writeCon);
        }
        return retval;
    }

    public TimedResult<DocumentMetadata> getDocuments(final long folderId, final Metadata[] columns, final Metadata sort, final int order, final boolean onlyOwnObjects, final Context ctx, final User user) throws OXException {
        String onlyOwn = "";
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            if (onlyOwnObjects) {
                onlyOwn = " AND created_by=?";
            }

            final int[] dbColumns = switchMetadata2DBColumns(columns, false);

            final StringBuilder sql = new StringBuilder(getSQLSelectForInfostoreColumns(dbColumns, false));
            sql.append(" FROM infostore JOIN infostore_document ON infostore.cid=? AND infostore_document.cid=? AND infostore.id=infostore_document.infostore_id AND infostore_document.version_number=infostore.version AND infostore.folder_id=?");
            sql.append(onlyOwn);

            if (sort != null) {
                sql.append(" ORDER BY ");
                sql.append(INFOSTORE_DATACOLUMNS[switchMetadata2DBColumns(new Metadata[] { sort }, false)[0]]);
                if (order == InfostoreFacade.ASC) {
                    sql.append(" ASC");
                } else if (order == InfostoreFacade.DESC) {
                    sql.append(" DESC");
                }
            }
            con = getReadConnection(ctx);
            stmt = con.prepareStatement(sql.toString());
            stmt.setInt(1, ctx.getContextId());
            stmt.setInt(2, ctx.getContextId());
            stmt.setLong(3, folderId);
            if (onlyOwn.length() > 0) {
                stmt.setInt(4, user.getId());
            }
            result = stmt.executeQuery();

            return new InfostoreTimedResult(buildIterator(result, stmt, dbColumns, this, ctx, con, true));
        } catch (final SQLException e) {
            close(stmt, result);
            releaseReadConnection(ctx, con);
            throw InfostoreExceptionCodes.SQL_PROBLEM.create(e, getStatement(stmt));
        } catch (final OXException e) {
            close(stmt, result);
            releaseReadConnection(ctx, con);
            throw InfostoreExceptionCodes.PREFETCH_FAILED.create(e);
        }
    }

    public SortedSet<String> getDocumentFileStoreLocationsperContext(final Context ctx) throws OXException {
        final SortedSet<String> _strReturnArray = new TreeSet<String>();
        Connection con = getReadConnection(ctx);

        final StringBuilder SQL = new StringBuilder(
            "SELECT file_store_location from infostore_document where infostore_document.cid=? AND file_store_location is not null");
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement(SQL.toString());
            stmt.setInt(1, ctx.getContextId());
            result = stmt.executeQuery();
            while (result.next()) {
                _strReturnArray.add(result.getString(1));
            }
        } catch (final SQLException e) {
            LOG.error(e.getMessage(), e);
            throw InfostoreExceptionCodes.SQL_PROBLEM.create(e, getStatement(stmt));
        } finally {
            close(stmt, result);
            releaseReadConnection(ctx, con);
        }
        return _strReturnArray;
    }

    public SortedSet<String> getDelDocumentFileStoreLocationsperContext(final Context ctx) throws OXException {
        final SortedSet<String> _strReturnArray = new TreeSet<String>();
        Connection con = getReadConnection(ctx);

        final StringBuilder SQL = new StringBuilder(
            "SELECT file_store_location from del_infostore_document where del_infostore_document.cid=? AND file_store_location is not null");
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement(SQL.toString());
            stmt.setInt(1, ctx.getContextId());
            result = stmt.executeQuery();
            while (result.next()) {
                _strReturnArray.add(result.getString(1));
            }
            result.close();
            stmt.close();
        } catch (final SQLException e) {
            LOG.error(e.getMessage(), e);
            throw InfostoreExceptionCodes.SQL_PROBLEM.create(e);
        } finally {
            close(stmt, result);
            releaseReadConnection(ctx, con);
        }

        return _strReturnArray;
    }

    public TimedResult<DocumentMetadata> getVersions(final int id, final Metadata[] columns, final Metadata sort, final int order, final Context ctx) throws OXException {
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            final int[] dbColumns = switchMetadata2DBColumns(columns, true);

            final StringBuilder sql = new StringBuilder(getSQLSelectForInfostoreColumns(dbColumns, false).toString());
            sql.append(" FROM infostore JOIN infostore_document ON infostore.cid=? AND infostore.id=? AND infostore_document.infostore_id=? AND infostore_document.cid=?");

            if (sort != null) {
                sql.append(" ORDER BY ");
                sql.append(INFOSTORE_DATACOLUMNS[switchMetadata2DBColumns(new Metadata[] { sort }, true)[0]]);
                if (order == InfostoreFacade.ASC) {
                    sql.append(" ASC");
                } else if (order == InfostoreFacade.DESC) {
                    sql.append(" DESC");
                }
            }
            con = getReadConnection(ctx);
            stmt = con.prepareStatement(sql.toString());
            stmt.setInt(1, ctx.getContextId());
            stmt.setInt(2, id);
            stmt.setInt(3, id);
            stmt.setInt(4, ctx.getContextId());
            result = stmt.executeQuery();

            return new InfostoreTimedResult(buildIterator(result, stmt, dbColumns, this, ctx, con, true));
        } catch (final SQLException e) {
            close(stmt, result);
            releaseReadConnection(ctx, con);
            throw InfostoreExceptionCodes.SQL_PROBLEM.create(e, getStatement(stmt));
        } catch (final OXException e) {
            close(stmt, result);
            releaseReadConnection(ctx, con);
            throw InfostoreExceptionCodes.PREFETCH_FAILED.create(e);
        }
    }

    public TimedResult<DocumentMetadata> getDocuments(final int[] ids, final Metadata[] columns, final Context ctx) throws OXException {
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            con = getReadConnection(ctx);
            final int[] dbColumns = switchMetadata2DBColumns(columns, false);
            final StringBuilder SQL_JOIN = new StringBuilder(getSQLSelectForInfostoreColumns(dbColumns, false));
            SQL_JOIN.append(" FROM infostore JOIN infostore_document ON infostore.cid=? AND infostore_document.cid=? AND infostore.id=infostore_document.infostore_id AND infostore_document.version_number=infostore.version AND (");
            for (int i = 0; i < ids.length; i++) {
                SQL_JOIN.append("infostore.id=?");
                if (i < ids.length - 1) {
                    SQL_JOIN.append(" OR ");
                }
            }
            SQL_JOIN.append(") ");

            stmt = con.prepareStatement(SQL_JOIN.toString());
            stmt.setInt(1, ctx.getContextId());
            stmt.setInt(2, ctx.getContextId());
            for (int i = 0; i < ids.length; i++) {
                stmt.setInt(i + 3, ids[i]);
            }
            result = stmt.executeQuery();

            return new InfostoreTimedResult(buildIterator(result, stmt, dbColumns, this, ctx, con, true));
        } catch (final SQLException e) {
            close(stmt, result);
            releaseReadConnection(ctx, con);
            throw InfostoreExceptionCodes.SQL_PROBLEM.create(e, getStatement(stmt));
        } catch (final OXException e) {
            close(stmt, result);
            releaseReadConnection(ctx, con);
            throw InfostoreExceptionCodes.PREFETCH_FAILED.create(e);
        }
    }

    public Delta<DocumentMetadata> getDelta(final long folderId, final long updateSince, final Metadata[] columns, final Metadata sort, final int order, final boolean onlyOwnObjects, final Context ctx, final User user) throws OXException {
        DeltaImpl<DocumentMetadata> retval = null;

        String onlyOwn = "";
        final StringBuilder ORDER = new StringBuilder();
        if (sort != null) {
            ORDER.append(" ORDER BY ");
            ORDER.append(INFOSTORE_DATACOLUMNS[switchMetadata2DBColumns(new Metadata[] { sort }, false)[0]]);
            if (order == InfostoreFacade.ASC) {
                ORDER.append(" ASC");
            } else if (order == InfostoreFacade.DESC) {
                ORDER.append(" DESC");
            }
        }

        Connection con = null;
        PreparedStatement stmtNew = null;
        PreparedStatement stmtModified = null;
        PreparedStatement stmtDeleted = null;

        ResultSet resultNew = null;
        ResultSet resultModified = null;
        ResultSet resultDeleted = null;
        try {
            con = getReadConnection(ctx);
            if (onlyOwnObjects) {
                onlyOwn = " AND created_by=?";
            }

            final int[] dbColumns = switchMetadata2DBColumns(columns, false);
            final StringBuilder SELECT = new StringBuilder(getSQLSelectForInfostoreColumns(dbColumns, false));
            final StringBuilder JOIN_NEW = new StringBuilder(
                " FROM infostore JOIN infostore_document ON infostore.cid=? AND infostore_document.cid=? AND infostore_document.infostore_id=infostore.id AND infostore_document.version_number = infostore.version AND infostore.folder_id=? AND infostore.creating_date>?").append(onlyOwn);
            final StringBuilder JOIN_MODIFIED = new StringBuilder(
                " FROM infostore JOIN infostore_document ON infostore.cid=? AND infostore_document.cid=? AND infostore_document.infostore_id=infostore.id  AND infostore_document.version_number = infostore.version AND infostore.folder_id=? AND infostore.last_modified>? AND infostore.creating_date<infostore.last_modified").append(onlyOwn);
            final StringBuilder DELETE_SELECT = new StringBuilder(
                "SELECT id FROM del_infostore WHERE cid=? AND folder_id=? AND last_modified>?").append(onlyOwn);

            stmtNew = con.prepareStatement(SELECT.toString() + JOIN_NEW.toString() + ORDER.toString());
            stmtNew.setInt(1, ctx.getContextId());
            stmtNew.setInt(2, ctx.getContextId());
            stmtNew.setLong(3, folderId);
            stmtNew.setLong(4, updateSince);
            if (onlyOwn.length() > 0) {
                stmtNew.setInt(5, user.getId());
            }
            resultNew = stmtNew.executeQuery();

            stmtModified = con.prepareStatement(SELECT.toString() + JOIN_MODIFIED.toString() + ORDER.toString());
            stmtModified.setInt(1, ctx.getContextId());
            stmtModified.setInt(2, ctx.getContextId());
            stmtModified.setLong(3, folderId);
            stmtModified.setLong(4, updateSince);
            if (onlyOwn.length() > 0) {
                stmtModified.setInt(5, user.getId());
            }
            resultModified = stmtModified.executeQuery();

            stmtDeleted = con.prepareStatement(DELETE_SELECT.toString() + ORDER.toString());
            stmtDeleted.setInt(1, ctx.getContextId());
            stmtDeleted.setLong(2, folderId);
            stmtDeleted.setLong(3, updateSince);
            if (onlyOwn.length() > 0) {
                stmtDeleted.setInt(4, user.getId());
            }
            resultDeleted = stmtDeleted.executeQuery();

            final SearchIterator<DocumentMetadata> isiNew = buildIterator(resultNew, stmtNew, dbColumns, this, ctx, con, false);
            final SearchIterator<DocumentMetadata> isiModified = buildIterator(resultModified, stmtModified, dbColumns, this, ctx, con, false);
            final SearchIterator<DocumentMetadata> isiDeleted = buildIterator(resultDeleted, stmtDeleted, new int[] { INFOSTORE_id }, this, ctx, con, false);

            retval = new DeltaImpl<DocumentMetadata>(isiNew, isiModified, isiDeleted, System.currentTimeMillis());
        } catch (final SQLException e) {
            throw InfostoreExceptionCodes.SQL_PROBLEM.create(e, getStatement(stmtNew));
        } catch (final OXException e) {
            throw InfostoreExceptionCodes.PREFETCH_FAILED.create(e);
        } finally {
            if (FETCH.equals(Fetch.PREFETCH)) {
                close(stmtNew, resultNew);
                close(stmtModified, resultModified);
                close(stmtDeleted, resultDeleted);
                releaseReadConnection(ctx, con);
            }
        }
        return retval;
    }

    public int countDocuments(final long folderId, final boolean onlyOwnObjects, final Context ctx, final User user) throws OXException {
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet result = null;

        try {
            con = getReadConnection(ctx);
            final StringBuilder SQL = new StringBuilder("SELECT count(id) from infostore where infostore.folder_id=?");
            if (onlyOwnObjects) {
                SQL.append(" AND infostore.created_by=?");
            }
            stmt = con.prepareStatement(SQL.toString());
            stmt.setLong(1, folderId);
            if (onlyOwnObjects) {
                stmt.setInt(2, user.getId());
            }
            result = stmt.executeQuery();
            if (result.next()) {
                return result.getInt(1);
            }
        } catch (final SQLException e) {
            throw InfostoreExceptionCodes.SQL_PROBLEM.create(e, getStatement(stmt));
        } finally {
            close(stmt, result);
            releaseReadConnection(ctx, con);
        }
        return 0;
    }

    public int countDocumentsperContext(final Context ctx) throws OXException {
        int retval = 0;

        final Connection con = getReadConnection(ctx);

        try {
            final StringBuilder SQL = new StringBuilder("SELECT count(id) from infostore where infostore.cid=?");
            final PreparedStatement stmt = con.prepareStatement(SQL.toString());
            stmt.setInt(1, ctx.getContextId());
            final ResultSet result = stmt.executeQuery();
            if (result.next()) {
                retval = result.getInt(1);
            }
            result.close();
            stmt.close();
        } catch (final SQLException e) {
            LOG.error(e.getMessage(), e);
            throw InfostoreExceptionCodes.SQL_PROBLEM.create(e, "");
        } finally {
            releaseReadConnection(ctx, con);
        }

        return retval;
    }

    public boolean hasFolderForeignObjects(final long folderId, final Context ctx, final User user) throws OXException {
        boolean retval = true;

        final Connection con = getReadConnection(ctx);

        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            // TODO: Check if document is unlocked too!

            stmt = con.prepareStatement("SELECT id FROM infostore WHERE cid=? AND folder_id=? AND created_by!=?");
            stmt.setInt(1, ctx.getContextId());
            stmt.setLong(2, folderId);
            stmt.setInt(3, user.getId());
            result = stmt.executeQuery();
            if (result.next()) {
                retval = true;
            } else {
                retval = false;
            }

        } catch (final SQLException e) {
            LOG.error(e.getMessage(), e);
            throw InfostoreExceptionCodes.SQL_PROBLEM.create(e, getStatement(stmt));
        } finally {
            close(stmt, result);
            releaseReadConnection(ctx, con);
        }

        return retval;
    }

    public boolean isFolderEmpty(final long folderId, final Context ctx) throws OXException {
        boolean retval = false;

        final Connection con = getReadConnection(ctx);

        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement("SELECT count(id) FROM infostore WHERE cid=? AND folder_id=?");
            stmt.setInt(1, ctx.getContextId());
            stmt.setLong(2, folderId);
            result = stmt.executeQuery();
            if (result.next() && result.getInt(1) <= 0) {
                retval = true;
            }
        } catch (final SQLException e) {
            LOG.error(e.getMessage(), e);
            throw InfostoreExceptionCodes.SQL_PROBLEM.create(e, getStatement(stmt));
        } finally {
            close(stmt, result);
            releaseReadConnection(ctx, con);
        }

        return retval;
    }

    private static final List<String> userFields = Arrays.asList("changed_by", "created_by");

    private static final List<String> tables = Arrays.asList("infostore", "infostore_document");

    public void removeUser(final int id, final Context ctx, final ServerSession session, final EntityLockManager locks) throws OXException {
        if (id != ctx.getMailadmin()) {
            removePrivate(id, ctx, session);
            assignToAdmin(id, ctx);
        } else {
            removeAll(ctx, session);
            removeFromDel(id, ctx);
        }

        locks.transferLocks(ctx, id, ctx.getMailadmin());
    }

    private void removeFromDel(final int id, final Context ctx) throws OXException {
        Connection writeCon = null;
        Statement stmt = null;
        StringBuilder query = new StringBuilder("NO QUERY");
        try {
            writeCon = getWriteConnection(ctx);
            stmt = writeCon.createStatement();
            for (final String table : DEL_TABLES) {
                query = new StringBuilder("DELETE FROM ").append(table).append(" WHERE cid = ").append(ctx.getContextId()).append(
                    " AND created_by = ").append(id);
                stmt.executeUpdate(query.toString());
            }
        } catch (final SQLException x) {
            throw InfostoreExceptionCodes.SQL_PROBLEM.create(x, query.toString());
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (final SQLException e) {
                    // Ignore
                }
            }
            if (writeCon != null) {
                releaseWriteConnection(ctx, writeCon);
            }
        }
    }

    private void removeAll(final Context ctx, final ServerSession session) throws OXException {
        try {

            final List<DocumentMetadata> documents = new ArrayList<DocumentMetadata>();

            SearchIterator<DocumentMetadata> iter = com.openexchange.groupware.infostore.database.impl.InfostoreIterator.allDocumentsWhere(
                " infostore.cid = " + ctx.getContextId(),
                Metadata.VALUES_ARRAY,
                getProvider(),
                ctx);
            if (!iter.hasNext()) {
                return; // Nothing to delete
            }
            while (iter.hasNext()) {
                final DocumentMetadata metadata = iter.next();
                documents.add(metadata);
            }

            final List<DocumentMetadata> versions = new ArrayList<DocumentMetadata>();

            iter = com.openexchange.groupware.infostore.database.impl.InfostoreIterator.allVersionsWhere(
                " infostore.cid = " + ctx.getContextId(),
                Metadata.VALUES_ARRAY,
                getProvider(),
                ctx);
            while (iter.hasNext()) {
                final DocumentMetadata metadata = iter.next();
                versions.add(metadata);
            }

            final InfostoreQueryCatalog catalog = new InfostoreQueryCatalog();

            final DeleteAllDocumentsAction deleteDocumentAction = new DeleteAllDocumentsAction();
            deleteDocumentAction.setProvider(getProvider());
            deleteDocumentAction.setContext(ctx);
            deleteDocumentAction.setDocuments(documents);
            deleteDocumentAction.setQueryCatalog(catalog);

            final DeleteAllVersionsAction deleteVersionAction = new DeleteAllVersionsAction();
            deleteVersionAction.setProvider(getProvider());
            deleteVersionAction.setContext(ctx);
            deleteVersionAction.setDocuments(versions);
            deleteVersionAction.setQueryCatalog(catalog);

            deleteVersionAction.perform();
            try {
                deleteDocumentAction.perform();
            } catch (final OXException e) {
                try {
                    deleteVersionAction.undo();
                    throw e;
                } catch (final OXException e1) {
                    LOG.fatal("Can't roll back deleting versions. Run the consistency tool.", e1);
                }
            }

            final FileStorage fs = getFileStorage(ctx);

            // Remove the files. No rolling back from this point onward

            final List<String> files = new ArrayList<String>(versions.size());
            for (final DocumentMetadata version : versions) {
                if (null != version.getFilestoreLocation()) {
                    files.add(version.getFilestoreLocation());
                }
            }
            fs.deleteFiles(files.toArray(new String[files.size()]));
          //FIXME
//            final EventClient ec = new EventClient(session);
//
//            for (final DocumentMetadata m : documents) {
//                try {
//                    ec.delete(m);
//                } catch (final Exception e) {
//                    LOG.error("", e);
//                }
//            }

        } catch (final OXException x) {
            throw x;
        }

    }

    private void removePrivate(final int id, final Context ctx, final ServerSession session) throws OXException {
        PreparedStatementHolder holder = null;

        try {
            final List<FolderObject> foldersWithPrivateItems = new DelUserFolderDiscoverer(getProvider()).discoverFolders(id, ctx);
            if (foldersWithPrivateItems.size() == 0) {
                return;
            }

            final List<String> files = new LinkedList<String>();
            holder = new PreparedStatementHolder(getProvider().getWriteConnection(session.getContext()));


            for (final FolderObject folder : foldersWithPrivateItems) {
                clearFolder(folder, session, files, holder);
            }

            final FileStorage fileStorage = getFileStorage(ctx);
            final String[] filesArray = files.toArray(new String[files.size()]);
            fileStorage.deleteFiles(filesArray);
        } catch (final SQLException x) {
            LOG.error(x.getMessage(), x);
            throw InfostoreExceptionCodes.SQL_PROBLEM.create(x, x.toString());
        } catch (final OXException x) {
            throw x;
        } finally {
            if(holder != null) {
                holder.close();
                getProvider().releaseWriteConnection(session.getContext(), holder.getConnection());
            }
        }
    }

    private void clearFolder(final FolderObject folder, final ServerSession session, final List<String> files, final PreparedStatementHolder holder) throws OXException, SQLException {
        final com.openexchange.groupware.infostore.database.impl.InfostoreIterator allDocumentsInFolder = com.openexchange.groupware.infostore.database.impl.InfostoreIterator.documents(
            folder.getObjectID(),
            Metadata.VALUES_ARRAY,
            Metadata.ID_LITERAL,
            InfostoreFacade.ASC,
            getProvider(),
            session.getContext());
        final List<DocumentMetadata> parents = new ArrayList<DocumentMetadata>();

        while (allDocumentsInFolder.hasNext()) {
            final DocumentMetadata documentMetadata = allDocumentsInFolder.next();
            parents.add(documentMetadata);
            discoverAllFiles(documentMetadata, session, files);
        }
        final InfostoreQueryCatalog queries = new InfostoreQueryCatalog();

        final List<String> parentDeletes = queries.getSingleDelete(InfostoreQueryCatalog.Table.INFOSTORE);
        final String allChildrenDelete = queries.getAllVersionsDelete(InfostoreQueryCatalog.Table.INFOSTORE_DOCUMENT);
        final Integer contextId = Autoboxing.I(session.getContextId());
        for(final DocumentMetadata documentMetadata : parents) {
            final Integer id = Autoboxing.I(documentMetadata.getId());
            holder.execute(allChildrenDelete, id, contextId);
            for (final String parentDelete : parentDeletes) {
                holder.execute(parentDelete, id, contextId);
            }
        }


      //FIXME
//        final EventClient ec = new EventClient(session);
//        for (final DocumentMetadata documentMetadata : parents) {
//            try {
//                ec.delete(documentMetadata);
//            } catch (final OXException e) {
//                LOG.error(e.getMessage(), e);
//            }
//        }
    }

    private void discoverAllFiles(final DocumentMetadata documentMetadata, final ServerSession session, final List<String> files) throws OXException {
        final com.openexchange.groupware.infostore.database.impl.InfostoreIterator allVersions = com.openexchange.groupware.infostore.database.impl.InfostoreIterator.versions(
            documentMetadata.getId(),
            Metadata.VALUES_ARRAY,
            Metadata.ID_LITERAL,
            InfostoreFacade.ASC,
            getProvider(),
            session.getContext());

        while (allVersions.hasNext()) {
            final DocumentMetadata version = allVersions.next();
            if (version.getFilestoreLocation() != null) {
                files.add(version.getFilestoreLocation());
            }
        }
    }

    private void assignToAdmin(final int id, final Context ctx) throws OXException {
        Connection writeCon = null;
        Statement stmt = null;
        StringBuilder query = null;
        try {
            writeCon = getWriteConnection(ctx);
            stmt = writeCon.createStatement();
            for (final String table : tables) {
                for (final String userField : userFields) {
                    query = new StringBuilder("UPDATE ").append(table).append(" SET ").append(userField).append(" = ").append(
                        ctx.getMailadmin()).append(" WHERE cid = ").append(ctx.getContextId()).append(" AND ").append(userField).append(
                        " = ").append(id);
                    stmt.executeUpdate(query.toString());
                }
            }
        } catch (final SQLException e) {
            throw InfostoreExceptionCodes.SQL_PROBLEM.create(e, (query != null) ? query.toString() : "");
        } catch (final OXException e) {
            throw e;
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (final SQLException e) {
                    LOG.debug("", e);
                }
            }
            releaseWriteConnection(ctx, writeCon);
        }
    }

    private StringBuffer getSQLSelectForInfostoreColumns(final int[] columns, final boolean deleteTable) {
        String delete = "";
        if (deleteTable) {
            delete = "del_";
        }
        final StringBuffer select = new StringBuffer("SELECT ");
        for (int i = 0; i < columns.length; i++) {
            select.append(delete);
            select.append(INFOSTORE_DATACOLUMNS[columns[i]]);
            if (i < columns.length - 1) {
                select.append(", ");
            }
        }
        return (select);
    }

    private int[] switchMetadata2DBColumns(final Metadata[] columns, final boolean versionPriorityHigh) {
        final int[] retval = new int[columns.length];
        for (int i = 0; i < columns.length; i++) {
            Metadata2DBSwitch: switch (columns[i].getId()) {
            default:
                break Metadata2DBSwitch;
            case Metadata.LAST_MODIFIED:
                if (versionPriorityHigh) {
                    retval[i] = INFOSTORE_DOCUMENT_last_modified;
                } else {
                    retval[i] = INFOSTORE_last_modified;
                }
                break Metadata2DBSwitch;
            case Metadata.LAST_MODIFIED_UTC:
                if (versionPriorityHigh) {
                    retval[i] = INFOSTORE_DOCUMENT_last_modified;
                } else {
                    retval[i] = INFOSTORE_last_modified;
                }
                break Metadata2DBSwitch;
            case Metadata.CREATION_DATE:
                if (versionPriorityHigh) {
                    retval[i] = INFOSTORE_DOCUMENT_creating_date;
                } else {
                    retval[i] = INFOSTORE_creating_date;
                }
                break Metadata2DBSwitch;
            case Metadata.MODIFIED_BY:
                if (versionPriorityHigh) {
                    retval[i] = INFOSTORE_DOCUMENT_changed_by;
                } else {
                    retval[i] = INFOSTORE_changed_by;
                }
                break Metadata2DBSwitch;
            case Metadata.CREATED_BY:
                if (versionPriorityHigh) {
                    retval[i] = INFOSTORE_DOCUMENT_created_by;
                } else {
                    retval[i] = INFOSTORE_created_by;
                }
                break Metadata2DBSwitch;
            case Metadata.FOLDER_ID:
                retval[i] = INFOSTORE_folder_id;
                break Metadata2DBSwitch;
            case Metadata.VERSION:
                if (versionPriorityHigh) {
                    retval[i] = INFOSTORE_DOCUMENT_version_number;
                } else {
                    retval[i] = INFOSTORE_version;
                }
                break Metadata2DBSwitch;
            case Metadata.TITLE:
                retval[i] = INFOSTORE_DOCUMENT_title;
                break Metadata2DBSwitch;
            case Metadata.FILENAME:
                retval[i] = INFOSTORE_DOCUMENT_filename;
                break Metadata2DBSwitch;
            case Metadata.SEQUENCE_NUMBER:
                retval[i] = INFOSTORE_DOCUMENT_last_modified;
                break Metadata2DBSwitch;
            case Metadata.ID:
                retval[i] = INFOSTORE_id;
                break Metadata2DBSwitch;
            case Metadata.COLOR_LABEL:
                retval[i] = INFOSTORE_color_label;
                break Metadata2DBSwitch;
            case Metadata.FILE_SIZE:
                retval[i] = INFOSTORE_DOCUMENT_file_size;
                break Metadata2DBSwitch;
            case Metadata.FILE_MIMETYPE:
                retval[i] = INFOSTORE_DOCUMENT_file_mimetype;
                break Metadata2DBSwitch;
            case Metadata.DESCRIPTION:
                retval[i] = INFOSTORE_DOCUMENT_description;
                break Metadata2DBSwitch;
            case Metadata.LOCKED_UNTIL:
                retval[i] = INFOSTORE_locked_until;
                break Metadata2DBSwitch;
            case Metadata.URL:
                retval[i] = INFOSTORE_DOCUMENT_url;
                break Metadata2DBSwitch;
            case Metadata.CATEGORIES:
                retval[i] = INFOSTORE_DOCUMENT_categories;
                break Metadata2DBSwitch;
            case Metadata.FILE_MD5SUM:
                retval[i] = INFOSTORE_DOCUMENT_file_md5sum;
                break Metadata2DBSwitch;
            case Metadata.VERSION_COMMENT:
                retval[i] = INFOSTORE_DOCUMENT_file_version_comment;
                break Metadata2DBSwitch;
            case Metadata.CURRENT_VERSION:
                retval[i] = INFOSTORE_version;
                break Metadata2DBSwitch;
            case Metadata.FILESTORE_LOCATION:
                retval[i] = INFOSTORE_DOCUMENT_file_store_location;
                break Metadata2DBSwitch;

            }
        }
        return retval;
    }

    final DocumentMetadataImpl fillDocumentMetadata(final DocumentMetadataImpl dmi, final int[] columns, final ResultSet result) throws SQLException {
        int currentVersion = -1;
        int versionNumber = -1;
        for (int i = 0; i < columns.length; i++) {
            setObjectColumns: switch (columns[i]) {
            default:
                break setObjectColumns;
            case INFOSTORE_cid:
                break setObjectColumns;
            case INFOSTORE_id:
                dmi.setId(result.getInt(i + 1));
                break setObjectColumns;
            case INFOSTORE_folder_id:
                dmi.setFolderId(result.getInt(i + 1));
                break setObjectColumns;
            case INFOSTORE_version:
                currentVersion = result.getInt(i + 1);
                break setObjectColumns;
            case INFOSTORE_locked_until:
                dmi.setLockedUntil(new Date(result.getLong(i + 1)));
                if (result.wasNull()) {
                    dmi.setLockedUntil(null);
                }
                break setObjectColumns;
            case INFOSTORE_creating_date:
                dmi.setCreationDate(new Date(result.getLong(i + 1)));
                break setObjectColumns;
            case INFOSTORE_last_modified:
                dmi.setLastModified(new Date(result.getLong(i + 1)));
                break setObjectColumns;
            case INFOSTORE_created_by:
                dmi.setCreatedBy(result.getInt(i + 1));
                break setObjectColumns;
            case INFOSTORE_changed_by:
                dmi.setModifiedBy(result.getInt(i + 1));
                break setObjectColumns;
            case INFOSTORE_color_label:
                dmi.setColorLabel(result.getInt(i + 1));
                break setObjectColumns;
            case INFOSTORE_DOCUMENT_cid:
                break setObjectColumns;
            case INFOSTORE_DOCUMENT_infostore_id:
                break setObjectColumns;
            case INFOSTORE_DOCUMENT_version_number:
                versionNumber = result.getInt(i + 1);
                break setObjectColumns;
            case INFOSTORE_DOCUMENT_creating_date:
                dmi.setCreationDate(new Date(result.getLong(i + 1)));
                break setObjectColumns;
            case INFOSTORE_DOCUMENT_last_modified:
                dmi.setLastModified(new Date(result.getLong(i + 1)));
                break setObjectColumns;
            case INFOSTORE_DOCUMENT_created_by:
                dmi.setCreatedBy(result.getInt(i + 1));
                break setObjectColumns;
            case INFOSTORE_DOCUMENT_changed_by:
                dmi.setModifiedBy(result.getInt(i + 1));
                break setObjectColumns;
            case INFOSTORE_DOCUMENT_title:
                dmi.setTitle(result.getString(i + 1));
                break setObjectColumns;
            case INFOSTORE_DOCUMENT_url:
                dmi.setURL(result.getString(i + 1));
                break setObjectColumns;
            case INFOSTORE_DOCUMENT_description:
                dmi.setDescription(result.getString(i + 1));
                break setObjectColumns;
            case INFOSTORE_DOCUMENT_categories:
                dmi.setCategories(result.getString(i + 1));
                break setObjectColumns;
            case INFOSTORE_DOCUMENT_filename:
                dmi.setFileName(result.getString(i + 1));
                break setObjectColumns;
            case INFOSTORE_DOCUMENT_file_store_location:
                dmi.setFilestoreLocation(result.getString(i + 1));
                break setObjectColumns;
            case INFOSTORE_DOCUMENT_file_size:
                dmi.setFileSize(result.getInt(i + 1));
                break setObjectColumns;
            case INFOSTORE_DOCUMENT_file_mimetype:
                dmi.setFileMIMEType(result.getString(i + 1));
                break setObjectColumns;
            case INFOSTORE_DOCUMENT_file_md5sum:
                dmi.setFileMD5Sum(result.getString(i + 1));
                break setObjectColumns;
            case INFOSTORE_DOCUMENT_file_version_comment:
                dmi.setVersionComment(result.getString(i + 1));
                break setObjectColumns;
            }
        }
        if ((currentVersion != -1) && (versionNumber != -1)) {
            if (currentVersion == versionNumber) {
                dmi.setIsCurrentVersion(true);
            }
            dmi.setVersion(versionNumber);
        } else if (versionNumber != -1) {
            dmi.setVersion(versionNumber);
        } else if (currentVersion != -1) {
            dmi.setVersion(currentVersion);
        }
        return dmi;
    }

    protected FileStorage getFileStorage(final Context ctx) throws OXException {
        return QuotaFileStorage.getInstance(FilestoreStorage.createURI(ctx), ctx);
    }

    @Override
    public void startTransaction() throws OXException {
        fileIdRemoveList.set(new ArrayList<String>());
        fileIdAddList.set(new ArrayList<String>());
        ctxHolder.set(null);
        super.startTransaction();
    }

    @Override
    public void commit() throws OXException {
        final Context ctx = ctxHolder.get();
        for (final String id : fileIdRemoveList.get()) {
            getFileStorage(ctx).deleteFile(id);
        }
        super.commit();
    }

    @Override
    public void finish() throws OXException {
        fileIdRemoveList.set(null);
        fileIdAddList.set(null);
        ctxHolder.set(null);
        super.finish();
    }

    @Override
    public void rollback() throws OXException {
        final Context ctx = ctxHolder.get();
        final List<String> list = fileIdAddList.get();
        if (null != list && !list.isEmpty()) {
            for (final String id : list) {
                getFileStorage(ctx).deleteFile(id);
            }
        }
        super.rollback();
    }

    private SearchIterator<DocumentMetadata> buildIterator(final ResultSet result, final PreparedStatement stmt, final int[] dbColumns, final DatabaseImpl service, final Context ctx, final Connection con, final boolean closeIfPossible) throws OXException, SQLException {

        return fetchMode.buildIterator(result, stmt, dbColumns, service, ctx, con, closeIfPossible);

    }

    protected static interface FetchMode {

        public SearchIterator<DocumentMetadata> buildIterator(ResultSet result, PreparedStatement stmt, int[] dbColumns, DatabaseImpl DatabaseImpl2, Context ctx, Connection con, boolean closeIfPossible) throws OXException, SQLException;
    }

    class PrefetchMode implements FetchMode {

        @Override
        public SearchIterator<DocumentMetadata> buildIterator(final ResultSet result, final PreparedStatement stmt, final int[] dbColumns, final DatabaseImpl impl, final Context ctx, final Connection con, final boolean closeIfPossible) throws SQLException {
            final List<DocumentMetadata> resultList = new ArrayList<DocumentMetadata>();
            while (result.next()) {
                final DocumentMetadataImpl dmi = new DocumentMetadataImpl();
                fillDocumentMetadata(dmi, dbColumns, result);
                resultList.add(dmi);
            }
            if (closeIfPossible) {
                close(stmt, result);
                releaseReadConnection(ctx, con);
            }
            return new SearchIteratorAdapter<DocumentMetadata>(resultList.iterator());
        }

    }

    static class CloseLaterMode implements FetchMode {

        @Override
        public SearchIterator<DocumentMetadata> buildIterator(final ResultSet result, final PreparedStatement stmt, final int[] dbColumns, final DatabaseImpl impl, final Context ctx, final Connection con, final boolean closeIfPossible) throws OXException {
            return new InfostoreIterator(result, stmt, dbColumns, impl, ctx, con);
        }

    }

    class CloseImmediatelyMode implements FetchMode {

        @Override
        public SearchIterator<DocumentMetadata> buildIterator(final ResultSet result, final PreparedStatement stmt, final int[] dbColumns, final DatabaseImpl impl, final Context ctx, final Connection con, final boolean closeIfPossible) {
            if (closeIfPossible) {
                close(stmt, result);
                releaseReadConnection(ctx, con);
            }

            return new InfostoreIterator(result, dbColumns, impl);

        }

    }

    public static class InfostoreIterator implements SearchIterator<DocumentMetadata> {

        private DocumentMetadata next;

        private Statement stmt;

        private ResultSet rs;

        private final int[] columns;

        private final DatabaseImpl d;

        private Context ctx;

        private Connection readCon;

        private final List<OXException> warnings;

        public InfostoreIterator(final ResultSet rs, final Statement stmt, final int[] columns, final DatabaseImpl d, final Context ctx, final Connection readCon) throws OXException {
            this.warnings = new ArrayList<OXException>(2);
            this.rs = rs;
            this.stmt = stmt;
            this.columns = columns;
            this.d = d;
            this.ctx = ctx;
            this.readCon = readCon;

            try {
                if (rs.next()) {
                    next = d.fillDocumentMetadata(new DocumentMetadataImpl(), columns, rs);
                } else {
                    close();
                }
            } catch (final SQLException e) {
                throw SearchIteratorExceptionCodes.SQL_ERROR.create(e, e.getMessage());
            }
        }

        public InfostoreIterator(final ResultSet rs, final int[] columns, final DatabaseImpl d) {
            this.warnings = new ArrayList<OXException>(2);
            this.rs = rs;
            this.columns = columns;
            this.d = d;
        }

        @Override
        public boolean hasNext() throws OXException {
            return next != null;
        }

        @Override
        public int size() {
            return -1;
        }

        public boolean hasSize() {
            return false;
        }

        @Override
        public DocumentMetadata next() throws OXException, OXException {
            try {
                DocumentMetadata retval = null;
                retval = next;
                if (rs.next()) {
                    next = d.fillDocumentMetadata(new DocumentMetadataImpl(), columns, rs);
                    NextObject: while (next == null) {
                        if (rs.next()) {
                            next = d.fillDocumentMetadata(new DocumentMetadataImpl(), columns, rs);
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
            } catch (final SQLException exc) {
                throw SearchIteratorExceptionCodes.SQL_ERROR.create(exc, exc.getMessage());
            }
        }

        @Override
        public void close() throws OXException {
            next = null;
            try {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
                rs = null;
                stmt = null;
            } catch (final SQLException e) {
                throw SearchIteratorExceptionCodes.SQL_ERROR.create(e, e.getMessage());
            } finally {
                if (readCon != null) {
                    d.releaseReadConnection(ctx, readCon);
                }
            }
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
    }

    public int getMaxActiveVersion(final int id, final Context context, final List<DocumentMetadata> ignoreVersions) throws OXException {
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        final StringBuilder ignoreVersionsList = new StringBuilder(ignoreVersions.size() * 4 + 2);
        ignoreVersionsList.append('(');
        for (final DocumentMetadata documentMetadata : ignoreVersions) {
            ignoreVersionsList.append(documentMetadata.getVersion()).append(',');
        }
        ignoreVersionsList.setCharAt(ignoreVersionsList.length() - 1, ')');

        try {
            con = getReadConnection(context);
            stmt = con.prepareStatement("SELECT max(version_number) FROM infostore_document WHERE cid = ? and infostore_id = ? AND NOT version_number IN " + ignoreVersionsList);
            stmt.setInt(1, context.getContextId());
            stmt.setInt(2, id);
            rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return -1;
        } catch (final SQLException e) {
            throw InfostoreExceptionCodes.SQL_PROBLEM.create(e, getStatement(stmt));
        } finally {
            close(stmt, rs);
            if (con != null) {
                releaseReadConnection(context, con);
            }
        }
    }
}
