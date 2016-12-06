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
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import com.openexchange.database.Databases;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.database.tx.DBService;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.FileStorage;
import com.openexchange.filestore.FileStorages;
import com.openexchange.filestore.Info;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
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
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorAdapter;
import com.openexchange.tools.iterator.SearchIteratorExceptionCodes;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.session.ServerSession;

public class DatabaseImpl extends DBService {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DatabaseImpl.class);

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

    private static final String[] INFOSTORE_DATACOLUMNS = new String[] {
        "infostore.cid", "infostore.id", "infostore.folder_id", "infostore.version", "infostore.locked_until", "infostore.creating_date",
        "infostore.last_modified", "infostore.created_by", "infostore.changed_by", "infostore.color_label", "infostore_document.cid",
        "infostore_document.infostore_id", "infostore_document.version_number", "infostore_document.creating_date",
        "infostore_document.last_modified", "infostore_document.created_by", "infostore_document.changed_by", "infostore_document.title",
        "infostore_document.url", "infostore_document.description", "infostore_document.categories", "infostore_document.filename",
        "infostore_document.file_store_location", "infostore_document.file_size", "infostore_document.file_mimetype",
        "infostore_document.file_md5sum", "infostore_document.file_version_comment" };

    private static class FileInfo {

        final String fileId;
        final int folderAdmin;
        final int contextId;

        FileInfo(String fileId, int folderAdmin, int contextId) {
            super();
            this.fileId = fileId;
            this.folderAdmin = folderAdmin;
            this.contextId = contextId;
        }
    }

    // ------------------------------------------------------------------------------------------------------------------------------------

    private final ThreadLocal<List<FileInfo>> fileIdAddList = new ThreadLocal<List<FileInfo>>();
    private final ThreadLocal<List<FileInfo>> fileIdRemoveList = new ThreadLocal<List<FileInfo>>();
    private FetchMode fetchMode;

    /**
     * Initializes a new {@link DatabaseImpl}.
     */
    public DatabaseImpl() {
        this(null);
    }

    /**
     * Initializes a new {@link DatabaseImpl}.
     *
     * @param provider The initial database provider instance
     */
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
            LOG.error("", e);
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
            stmt = con.prepareStatement(getSQLSelectForInfostoreColumns(columns).toString() + " FROM infostore JOIN infostore_document ON infostore.cid=? AND infostore.id=? AND infostore_document.infostore_id=infostore.id AND infostore_document.cid=? AND infostore_document.version_number = " + versionString);
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
            LOG.error("", e);
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
            stmt = con.prepareStatement(getSQLSelectForInfostoreColumns(switchMetadata2DBColumns(columns, true)).toString() + " FROM infostore JOIN infostore_document ON infostore.cid=? AND infostore_document.infostore_id=infostore.id AND infostore_document.cid=? WHERE " + where);
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
            stmt = con.prepareStatement(getSQLSelectForInfostoreColumns(switchMetadata2DBColumns(columns, false)).toString() + " FROM infostore JOIN infostore_document ON infostore.cid=? AND infostore_document.infostore_id=infostore.id AND infostore_document.cid=? AND infostore_document.version_number = infostore.version WHERE " + where);
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

    public InputStream getDocument(int id, int version, Context ctx) throws OXException {
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            con = getReadConnection(ctx);

            if (version != -1) {
                stmt = con.prepareStatement("SELECT d.file_store_location, i.folder_id FROM infostore_document AS d JOIN infostore AS i ON d.cid=i.cid AND d.infostore_id=i.id WHERE d.cid=? AND d.infostore_id=? AND d.version_number=? AND d.file_store_location IS NOT NULL");
                stmt.setInt(3, version);
            } else {
                stmt = con.prepareStatement("SELECT d.file_store_location, i.folder_id from infostore_document AS d JOIN infostore AS i ON i.cid=? AND i.id=? AND d.cid=? AND d.infostore_id=? AND d.version_number=i.version AND d.file_store_location is not null");
                stmt.setInt(3, ctx.getContextId());
                stmt.setInt(4, id);
            }
            stmt.setInt(1, ctx.getContextId());
            stmt.setInt(2, id);
            result = stmt.executeQuery();
            if (false == result.next()) {
                return null;
            }

            String fileStorageLoaction = result.getString(1);
            int folderId = result.getInt(2);
            close(stmt, result);
            result = null;
            stmt = null;

            int folderOwner = new OXFolderAccess(con, ctx).getFolderOwner(folderId);
            releaseReadConnection(ctx, con);
            con = null;

            FileStorage fs = FileStorages.getQuotaFileStorageService().getQuotaFileStorage(folderOwner, ctx.getContextId(), Info.drive(folderOwner));
            return fs.getFile(fileStorageLoaction);
        } catch (final SQLException x) {
            throw InfostoreExceptionCodes.SQL_PROBLEM.create(x, getStatement(stmt));
        } finally {
            close(stmt, result);
            releaseReadConnection(ctx, con);
        }
    }

    public int[] removeDocument(final String identifier, final Context ctx) throws OXException {
        return removeDocumentFromTable(identifier, ctx, "infostore");
    }

    /**
     * @param identifier The file identifier
     * @param ctx The Context from which it should be deleted
     * @param basetablename The basename of the table e.g. infostore the fitting database names (infostore_documents etc. are
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

        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            startDBTransaction();
            stmt = writecon.prepareStatement("SELECT version_number, infostore_id, version FROM " + documentstable + " JOIN " + basetablename + " ON " + basetablename + ".id = " + documentstable + ".infostore_id " + " AND file_store_location=? AND " + documentstable + ".cid=?");
            stmt.setString(1, identifier);
            stmt.setInt(2, ctx.getContextId());
            result = stmt.executeQuery();
            if (!result.next()) {
                // Apparently no such file is known
                return retval;
            }
            int version_nr = result.getInt(1);
            int infostore_id = result.getInt(2);
            int current_version = result.getInt(3);
            result.close();
            stmt.close();

            // Now we have to check if the version number is the active one
            stmt = writecon.prepareStatement("SELECT version_number FROM " + documentstable + " WHERE infostore_id=? AND cid=?");
            stmt.setInt(1, infostore_id);
            stmt.setInt(2, ctx.getContextId());
            result = stmt.executeQuery();
            final SortedSet<Integer> set = new TreeSet<Integer>();
            while (result.next()) {
                set.add(Integer.valueOf(result.getInt(1)));
            }
            set.remove(Integer.valueOf(version_nr));
            stmt.close();

            if (set.isEmpty()) {
                // There is no other version available. Keep that entry, but NULL'ify file store location
                stmt = writecon.prepareStatement("UPDATE " + documentstable + " SET file_store_location=?, file_size=?, file_mimetype=?, file_md5sum=? WHERE cid=? AND infostore_id=? AND version_number=?");
                stmt.setNull(1, java.sql.Types.VARCHAR);
                stmt.setLong(2, 0L);
                stmt.setNull(3, java.sql.Types.VARCHAR);
                stmt.setNull(4, java.sql.Types.VARCHAR);
                stmt.setInt(5, ctx.getContextId());
                stmt.setInt(6, infostore_id);
                stmt.setInt(7, version_nr);
                stmt.executeUpdate();
                stmt.close();
            } else {
                if (version_nr == current_version) {
                    stmt = writecon.prepareStatement("UPDATE " + basetablename + " SET version=? " + " WHERE id=? AND  " + basetablename + ".cid=?");
                    stmt.setInt(1, set.last().intValue());
                    stmt.setInt(2, infostore_id);
                    stmt.setInt(3, ctx.getContextId());
                    retval[0] = stmt.executeUpdate();
                    stmt.close();
                }

                stmt = writecon.prepareStatement("DELETE FROM " + documentstable + " WHERE cid=? AND file_store_location=?");
                stmt.setInt(1, ctx.getContextId());
                stmt.setString(2, identifier);
                retval[1] = stmt.executeUpdate();
                stmt.close();
            }

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

    /**
     * Gets the identifier of the user holding the document (owner of the folder in which the document resides)
     *
     * @param fileIdentifier The identifier of the document in file storage
     * @param ctx The context
     * @return The document holder or <code>-1</code>
     * @throws OXException If document holder cannot be returned
     */
    public int getDocumentHolderFor(String fileIdentifier, Context ctx) throws OXException {
        Connection con = getReadConnection(ctx);
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT t.created_from FROM infostore AS i JOIN infostore_document AS d ON i.cid=d.cid AND i.id=d.infostore_id JOIN oxfolder_tree AS t ON i.cid=t.cid AND i.folder_id=t.fuid WHERE i.cid=? AND d.file_store_location=?");
            stmt.setInt(1, ctx.getContextId());
            stmt.setString(2, fileIdentifier);
            rs = stmt.executeQuery();
            return rs.next() ? rs.getInt(1) : -1;
        } catch (final SQLException e) {
            throw InfostoreExceptionCodes.SQL_PROBLEM.create(e, getStatement(stmt));
        } finally {
            Databases.closeSQLStuff(rs, stmt);
            releaseReadConnection(ctx, con);
        }
    }

    public int modifyDocument(final String oldidentifier, final String newidentifier, final String description, final String mimetype, final Context ctx) throws OXException {
        return modifyDocumentInTable(oldidentifier, newidentifier, description, mimetype, ctx, "infostore");
    }

    private int modifyDocumentInTable(final String oldidentifier, final String newidentifier, final String description, final String mimetype, final Context ctx, final String basetablename) throws OXException {
        final String documentstable = basetablename.concat("_document");
        int retval = -1;
        final Connection writecon = getWriteConnection(ctx);

        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            startDBTransaction();
            String olddescription = null;
            stmt = writecon.prepareStatement("SELECT description FROM " + documentstable + " WHERE file_store_location=? AND cid=?");
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

            stmt = writecon.prepareStatement("UPDATE " + documentstable + " SET file_store_location=?,  description=?, file_mimetype=? " + "WHERE file_store_location=? AND cid=?");
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
            final int folder_id = new OXFolderAccess(ctx).getDefaultFolderID(user.getId(), FolderObject.INFOSTORE);
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
            LOG.error("", e);
            throw InfostoreExceptionCodes.SQL_PROBLEM.create(e, getStatement(stmt));
        } catch (final OXException e) {
            try {
                rollbackDBTransaction();
            } catch (final OXException e1) {
                throw e1;
            }
            LOG.error("", e);
            throw e;
        } finally {
            close(stmt, null);
            finishDBTransaction();
            if (null != writeCon) {
                releaseWriteConnection(ctx, writeCon);
            }
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

            final StringBuilder sql = new StringBuilder(getSQLSelectForInfostoreColumns(dbColumns));
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

    public SortedSet<String> getDocumentFileStoreLocationsperContext(Context ctx) throws OXException {
        Connection con = getReadConnection(ctx);
        try {
            return getDocumentFileStoreLocationsperContext(ctx, con);
        } finally {
            releaseReadConnection(ctx, con);
        }
    }

    public SortedSet<String> getDocumentFileStoreLocationsperContext(Context ctx, Connection con) throws OXException {
        int contextId = ctx.getContextId();
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            // Determine users with a specific file storage set
            stmt = con.prepareStatement("SELECT DISTINCT user.id FROM user WHERE user.cid=? AND user.filestore_id>0");
            stmt.setInt(1, contextId);
            result = stmt.executeQuery();
            Set<Integer> userIds;
            if (result.next()) {
                userIds = new LinkedHashSet<Integer>(16, 0.9F);
                do {
                    userIds.add(Integer.valueOf(result.getInt(1)));
                } while (result.next());
            } else {
                userIds = null;
            }
            close(stmt, result);
            result = null;
            stmt = null;

            SortedSet<String> fileStorageLocations;
            if (null == userIds) {
                // There are no users in this context with a specific file storage. Just grab all from "infostore_document" table for given context.
                stmt = con.prepareStatement("SELECT file_store_location FROM infostore_document WHERE infostore_document.cid=? AND file_store_location IS NOT NULL");
                stmt.setInt(1, contextId);
                result = stmt.executeQuery();
                fileStorageLocations = new TreeSet<String>();
                while (result.next()) {
                    fileStorageLocations.add(result.getString(1));
                }
            } else {
                // All in context w/o user-association
                stmt = con.prepareStatement("SELECT d.file_store_location FROM infostore_document AS d JOIN infostore AS i ON d.cid=i.cid AND d.infostore_id=i.id WHERE d.cid=? AND d.file_store_location IS NOT NULL AND i.folder_id NOT IN (SELECT t.fuid FROM oxfolder_tree AS t WHERE t.cid=? AND t.module=? AND t.created_from IN (SELECT DISTINCT user.id FROM user WHERE user.cid=? AND user.filestore_id>0))");
                stmt.setInt(1, contextId);
                stmt.setInt(2, contextId);
                stmt.setInt(3, FolderObject.INFOSTORE);
                stmt.setInt(4, contextId);
                result = stmt.executeQuery();
                fileStorageLocations = new TreeSet<String>();
                while (result.next()) {
                    fileStorageLocations.add(result.getString(1));
                }
                close(stmt, result);
                result = null;
                stmt = null;

                // Iterate users with a specific file storage
                for (Integer userId : userIds) {
                    stmt = con.prepareStatement("SELECT d.file_store_location FROM infostore_document AS d JOIN infostore AS i ON d.cid=i.cid AND d.infostore_id=i.id WHERE d.cid=? AND d.file_store_location IS NOT NULL AND i.folder_id IN (SELECT t.fuid FROM oxfolder_tree AS t WHERE t.cid=? AND t.module=? AND t.created_from=?)");
                    stmt.setInt(1, contextId);
                    stmt.setInt(2, contextId);
                    stmt.setInt(3, FolderObject.INFOSTORE);
                    stmt.setInt(4, userId.intValue());
                    result = stmt.executeQuery();
                    while (result.next()) {
                        fileStorageLocations.add(result.getString(1));
                    }
                    close(stmt, result);
                    result = null;
                    stmt = null;
                }
            }

            return fileStorageLocations;
        } catch (final SQLException e) {
            LOG.error("", e);
            throw InfostoreExceptionCodes.SQL_PROBLEM.create(e, getStatement(stmt));
        } finally {
            close(stmt, result);
        }
    }

    /**
     * Get the document file store locations for the specified user in the specified context
     *
     * @param ctx The context
     * @param usr The user
     * @return A sorted set of all document file store locations for the specified user in the specified context
     * @throws OXException
     */
    public SortedSet<String> getDocumentFileStoreLocationsPerUser(Context ctx, User usr) throws OXException {
        Connection connection = getReadConnection(ctx);
        try {
            return getDocumentFileStoreLocationsPerUser(ctx, usr, connection);
        } finally {
            releaseReadConnection(ctx, connection);
        }
    }

    /**
     * Get the document file store locations for the specified user in the specified context
     *
     * @param ctx The context
     * @param usr The user
     * @param connection A read-only database connection for the specified context
     * @return A sorted set of all document file store locations for the specified user in the specified context
     * @throws OXException
     */
    private SortedSet<String> getDocumentFileStoreLocationsPerUser(Context ctx, User usr, Connection connection) throws OXException {
        int contextId = ctx.getContextId();
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            SortedSet<String> fileStorageLocations = new TreeSet<String>();
            stmt = connection.prepareStatement("SELECT d.file_store_location FROM infostore_document AS d JOIN infostore AS i ON d.cid=i.cid AND d.infostore_id=i.id WHERE d.cid=? AND d.file_store_location IS NOT NULL AND i.folder_id IN (SELECT t.fuid FROM oxfolder_tree AS t WHERE t.cid=? AND t.module=? AND t.created_from=?)");
            stmt.setInt(1, contextId);
            stmt.setInt(2, contextId);
            stmt.setInt(3, FolderObject.INFOSTORE);
            stmt.setInt(4, usr.getId());
            result = stmt.executeQuery();
            while (result.next()) {
                fileStorageLocations.add(result.getString(1));
            }
            close(stmt, result);
            result = null;
            stmt = null;

            return fileStorageLocations;
        } catch (final SQLException e) {
            LOG.error("", e);
            throw InfostoreExceptionCodes.SQL_PROBLEM.create(e, getStatement(stmt));
        } finally {
            close(stmt, result);
        }
    }

    public TimedResult<DocumentMetadata> getVersions(final int id, final Metadata[] columns, final Metadata sort, final int order, final Context ctx) throws OXException {
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            final int[] dbColumns = switchMetadata2DBColumns(columns, true);

            final StringBuilder sql = new StringBuilder(getSQLSelectForInfostoreColumns(dbColumns).toString());
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
            final StringBuilder SQL_JOIN = new StringBuilder(getSQLSelectForInfostoreColumns(dbColumns));
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
            final StringBuilder SELECT = new StringBuilder(getSQLSelectForInfostoreColumns(dbColumns));
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
            final StringBuilder SQL = new StringBuilder("SELECT count(id) from infostore where infostore.cid=? AND infostore.folder_id=?");
            if (onlyOwnObjects) {
                SQL.append(" AND infostore.created_by=?");
            }
            stmt = con.prepareStatement(SQL.toString());
            stmt.setInt(1, ctx.getContextId());
            stmt.setLong(2, folderId);
            if (onlyOwnObjects) {
                stmt.setInt(3, user.getId());
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
            LOG.error("", e);
            throw InfostoreExceptionCodes.SQL_PROBLEM.create(e, "");
        } finally {
            releaseReadConnection(ctx, con);
        }

        return retval;
    }

    /**
     * Gets the total size of all document versions in a folder.
     *
     * @param context The context
     * @param folderId The folder identifier
     * @return The total size of all document versions in a folder
     */
    public long getTotalSize(Context context, long folderId) throws OXException {
        String sql = new StringBuilder()
            .append("SELECT SUM(infostore_document.file_size) ")
            .append("FROM infostore LEFT JOIN infostore_document ")
            .append("ON infostore.cid=infostore_document.cid AND infostore.id=infostore_document.infostore_id ")
            .append("WHERE infostore.cid=? AND infostore.folder_id=?;")
        .toString();
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            connection = getReadConnection(context);
            stmt = connection.prepareStatement(sql);
            stmt.setInt(1, context.getContextId());
            stmt.setLong(2, folderId);
            result = stmt.executeQuery();
            return result.next() ? result.getLong(1) : 0L;
        } catch (SQLException e) {
            throw InfostoreExceptionCodes.SQL_PROBLEM.create(e, getStatement(stmt));
        } finally {
            close(stmt, result);
            releaseReadConnection(context, connection);
        }
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
            LOG.error("", e);
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
            LOG.error("", e);
            throw InfostoreExceptionCodes.SQL_PROBLEM.create(e, getStatement(stmt));
        } finally {
            close(stmt, result);
            releaseReadConnection(ctx, con);
        }

        return retval;
    }

    private static final List<String> userFields = Arrays.asList("changed_by", "created_by");

    private static final List<String> tables = Arrays.asList("infostore", "infostore_document");

    public void removeUser(final int id, final Context ctx, Integer destUser, final ServerSession session, final EntityLockManager locks) throws OXException {
        if (destUser == null) {
            destUser = ctx.getMailadmin();
        }
        if (id != ctx.getMailadmin()) {
            if (destUser <= 0) {
                removeAllForUser(id, ctx, session, true);
            } else {
                removePrivate(id, ctx, session);
                assignToUser(id, ctx, destUser);
            }
        } else {
            removeAll(ctx, session);
        }
        removeFromDel(id, ctx);
        locks.transferLocks(ctx, id, destUser.intValue());
    }

    private void removeFromDel(final int id, final Context ctx) throws OXException {
        Connection writeCon = null;
        Statement stmt = null;
        StringBuilder query = new StringBuilder("NO QUERY");
        try {
            writeCon = getWriteConnection(ctx);
            stmt = writeCon.createStatement();
            for (final String table : new String[] { "del_infostore", "del_infostore_document" }) {
                query = new StringBuilder("DELETE FROM ").append(table).append(" WHERE cid = ").append(ctx.getContextId()).append(
                    " AND created_by = ").append(id);
                stmt.executeUpdate(query.toString());
            }
        } catch (final SQLException x) {
            throw InfostoreExceptionCodes.SQL_PROBLEM.create(x, query.toString());
        } finally {
            Databases.closeSQLStuff(stmt);
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
                this,
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
                this,
                ctx);
            while (iter.hasNext()) {
                final DocumentMetadata metadata = iter.next();
                versions.add(metadata);
            }

            final InfostoreQueryCatalog catalog = InfostoreQueryCatalog.getInstance();

            final DeleteAllDocumentsAction deleteDocumentAction = new DeleteAllDocumentsAction(session);
            deleteDocumentAction.setProvider(this);
            deleteDocumentAction.setContext(ctx);
            deleteDocumentAction.setDocuments(documents);
            deleteDocumentAction.setQueryCatalog(catalog);

            final DeleteAllVersionsAction deleteVersionAction = new DeleteAllVersionsAction(session);
            deleteVersionAction.setProvider(this);
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
                    LOG.error("Can't roll back deleting versions. Run the consistency tool.", e1);
                }
            }

            // Remove the files. No rolling back from this point onward

            final List<String> files = new ArrayList<String>(versions.size());
            for (final DocumentMetadata version : versions) {
                if (null != version.getFilestoreLocation()) {
                    files.add(version.getFilestoreLocation());
                }
            }

            if (!files.isEmpty()) {
                List<FileStorage> fileStorages = getFileStorages(ctx);
                for (String fileId : files) {
                    for (FileStorage fileStorage : fileStorages) {
                        try {
                            fileStorage.deleteFile(fileId);
                        } catch (Exception e) {
                            // Ignore
                        }
                    }
                }
            }

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
        removeAllForUser(id, ctx, session, false);
    }

    private void removeAllForUser(final int id, final Context ctx, final ServerSession session, boolean includeShared) throws OXException {
        PreparedStatementHolder holder = null;

        try {
            final List<FolderObject> foldersWithPrivateItems = new DelUserFolderDiscoverer(this).discoverFolders(id, ctx, includeShared);
            if (foldersWithPrivateItems.size() == 0) {
                return;
            }

            final List<String> files = new LinkedList<String>();
            holder = new PreparedStatementHolder(this.getWriteConnection(session.getContext()));

            for (final FolderObject folder : foldersWithPrivateItems) {
                clearFolder(folder, session, files, holder);
            }

            if (!files.isEmpty()) {
                List<FileStorage> fileStorages = getFileStorages(ctx);
                for (String fileId : files) {
                    for (FileStorage fileStorage : fileStorages) {
                        try {
                            fileStorage.deleteFile(fileId);
                        } catch (Exception e) {
                            // Ignore
                        }
                    }
                }
            }
        } catch (final SQLException x) {
            LOG.error("", x);
            throw InfostoreExceptionCodes.SQL_PROBLEM.create(x, x.toString());
        } catch (final OXException x) {
            throw x;
        } finally {
            if (holder != null) {
                holder.close();
                releaseWriteConnection(session.getContext(), holder.getConnection());
            }
        }
    }

    private void clearFolder(final FolderObject folder, final ServerSession session, final List<String> files, final PreparedStatementHolder holder) throws OXException, SQLException {
        final com.openexchange.groupware.infostore.database.impl.InfostoreIterator allDocumentsInFolder = com.openexchange.groupware.infostore.database.impl.InfostoreIterator.documents(
            folder.getObjectID(),
            Metadata.VALUES_ARRAY,
            Metadata.ID_LITERAL,
            InfostoreFacade.ASC,
            -1,
            -1,
            this,
            session.getContext());
        final List<DocumentMetadata> parents = new ArrayList<DocumentMetadata>();

        while (allDocumentsInFolder.hasNext()) {
            final DocumentMetadata documentMetadata = allDocumentsInFolder.next();
            parents.add(documentMetadata);
            discoverAllFiles(documentMetadata, session, files);
        }
        final InfostoreQueryCatalog queries = InfostoreQueryCatalog.getInstance();

        final List<String> parentDeletes = queries.getSingleDelete(InfostoreQueryCatalog.Table.INFOSTORE);
        final String allChildrenDelete = queries.getAllVersionsDelete(InfostoreQueryCatalog.Table.INFOSTORE_DOCUMENT);
        final Integer contextId = Autoboxing.I(session.getContextId());
        for (final DocumentMetadata documentMetadata : parents) {
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
        //                LOG.error("", e);
        //            }
        //        }
    }

    private void discoverAllFiles(final DocumentMetadata documentMetadata, final ServerSession session, final List<String> files) throws OXException {
        final com.openexchange.groupware.infostore.database.impl.InfostoreIterator allVersions = com.openexchange.groupware.infostore.database.impl.InfostoreIterator.versions(
            documentMetadata.getId(),
            Metadata.VALUES_ARRAY,
            Metadata.ID_LITERAL,
            InfostoreFacade.ASC,
            this,
            session.getContext());

        while (allVersions.hasNext()) {
            final DocumentMetadata version = allVersions.next();
            final String filestoreLocation = version.getFilestoreLocation();
            if (filestoreLocation != null) {
                files.add(filestoreLocation);
            }
        }
    }

    private void assignToUser(final int id, final Context ctx, int destUser) throws OXException {
        Connection writeCon = null;
        Statement stmt = null;
        StringBuilder query = null;
        try {
            writeCon = getWriteConnection(ctx);
            stmt = writeCon.createStatement();
            for (final String table : tables) {
                for (final String userField : userFields) {
                    query = new StringBuilder("UPDATE ").append(table).append(" SET ").append(userField).append(" = ").append(
                        destUser).append(" WHERE cid = ").append(ctx.getContextId()).append(" AND ").append(userField).append(
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
            if (null != writeCon) {
                releaseWriteConnection(ctx, writeCon);
            }
        }
    }

    private StringBuffer getSQLSelectForInfostoreColumns(final int[] columns) {
        String delete = "";
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
                    dmi.setFileSize(result.getLong(i + 1));
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

    protected List<FileStorage> getFileStorages(final Context ctx) throws OXException {
        return FileStorages.getFileStorage2EntitiesResolver().getFileStoragesUsedBy(ctx.getContextId(), true);
    }

    protected FileStorage getFileStorage(int folderOwner, int contextId) throws OXException {
        return FileStorages.getQuotaFileStorageService().getQuotaFileStorage(folderOwner, contextId, Info.drive(folderOwner));
    }

    @Override
    public void startTransaction() throws OXException {
        fileIdRemoveList.set(new ArrayList<FileInfo>());
        fileIdAddList.set(new ArrayList<FileInfo>());
        super.startTransaction();
    }

    @Override
    public void commit() throws OXException {
        List<FileInfo> list = fileIdRemoveList.get();
        if (null != list && !list.isEmpty()) {
            if (1 == list.size()) {
                FileInfo removeInfo = list.get(0);
                getFileStorage(removeInfo.folderAdmin, removeInfo.contextId).deleteFile(removeInfo.fileId);
            } else {
                Map<FileStorage, List<String>> removalsPerStorage = new HashMap<FileStorage, List<String>>();
                for (FileInfo removeInfo : list) {
                    FileStorage fileStorage = getFileStorage(removeInfo.folderAdmin, removeInfo.contextId);
                    List<String> removals = removalsPerStorage.get(fileStorage);
                    if (null == removals) {
                        removals = new ArrayList<String>();
                        removalsPerStorage.put(fileStorage, removals);
                    }
                    removals.add(removeInfo.fileId);
                }
                for (Map.Entry<FileStorage, List<String>> entry : removalsPerStorage.entrySet()) {
                    entry.getKey().deleteFiles(entry.getValue().toArray(new String[entry.getValue().size()]));
                }
            }
        }
        super.commit();
    }

    @Override
    public void finish() throws OXException {
        fileIdRemoveList.set(null);
        fileIdAddList.set(null);
        super.finish();
    }

    @Override
    public void rollback() throws OXException {
        List<FileInfo> list = fileIdAddList.get();
        if (null != list && !list.isEmpty()) {
            if (1 == list.size()) {
                FileInfo removeInfo = list.get(0);
                getFileStorage(removeInfo.folderAdmin, removeInfo.contextId).deleteFile(removeInfo.fileId);
            } else {
                Map<FileStorage, List<String>> removalsPerStorage = new HashMap<FileStorage, List<String>>();
                for (FileInfo removeInfo : list) {
                    FileStorage fileStorage = getFileStorage(removeInfo.folderAdmin, removeInfo.contextId);
                    List<String> removals = removalsPerStorage.get(fileStorage);
                    if (null == removals) {
                        removals = new ArrayList<String>();
                        removalsPerStorage.put(fileStorage, removals);
                    }
                    removals.add(removeInfo.fileId);
                }
                for (Map.Entry<FileStorage, List<String>> entry : removalsPerStorage.entrySet()) {
                    entry.getKey().deleteFiles(entry.getValue().toArray(new String[entry.getValue().size()]));
                }
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
        public void close() {
            next = null;

            Databases.closeSQLStuff(rs, stmt);
            rs = null;
            stmt = null;

            if (readCon != null) {
                d.releaseReadConnection(ctx, readCon);
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
