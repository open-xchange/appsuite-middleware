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

package com.openexchange.user.copy.internal.infostore;

import static com.openexchange.java.Autoboxing.i;
import static com.openexchange.user.copy.internal.CopyTools.replaceIdsInQuery;
import static com.openexchange.user.copy.internal.CopyTools.setStringOrNull;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.filestore.Info;
import com.openexchange.filestore.QuotaFileStorage;
import com.openexchange.filestore.QuotaFileStorageService;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.impl.IDGenerator;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.database.impl.DocumentMetadataImpl;
import com.openexchange.java.Streams;
import com.openexchange.tools.sql.DBUtils;
import com.openexchange.user.copy.CopyUserTaskService;
import com.openexchange.user.copy.ObjectMapping;
import com.openexchange.user.copy.UserCopyExceptionCodes;
import com.openexchange.user.copy.internal.CopyTools;
import com.openexchange.user.copy.internal.connection.ConnectionFetcherTask;
import com.openexchange.user.copy.internal.context.ContextLoadTask;
import com.openexchange.user.copy.internal.folder.FolderCopyTask;
import com.openexchange.user.copy.internal.user.UserCopyTask;


/**
 * {@link InfostoreCopyTask}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class InfostoreCopyTask implements CopyUserTaskService {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(InfostoreCopyTask.class);

    private static final String SELECT_INFOSTORE_MASTERS =
        "SELECT " +
            "id, folder_id, version, color_label, creating_date, " +
            "last_modified, created_by, changed_by " +
        "FROM " +
            "infostore " +
        "WHERE " +
            "cid = ? " +
        "AND " +
            "folder_id IN (#IDS#)";

    private static final String SELECT_INFOSTORE_VERSIONS =
        "SELECT " +
            "version_number, creating_date, last_modified, title, url, " +
            "description, categories, filename, file_store_location, file_size, " +
            "file_mimetype, file_md5sum, file_version_comment " +
        "FROM " +
            "infostore_document " +
        "WHERE " +
            "cid = ? " +
        "AND " +
            "infostore_id = ?";

    private static final String INSERT_INFOSTORE_MASTERS =
        "INSERT INTO " +
            "infostore " +
            "(cid, id, folder_id, version, locked_until, color_label, " +
            "creating_date, last_modified, created_by, changed_by) " +
        "VALUES " +
            "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String INSERT_INFOSTORE_VERSIONS =
        "INSERT INTO " +
            "infostore_document " +
            "(cid, infostore_id, version_number, creating_date, last_modified, " +
            "created_by, changed_by, title, url, description, categories, filename, " +
            "file_store_location, file_size, file_mimetype, file_md5sum, file_version_comment) " +
        "VALUES " +
            "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private final QuotaFileStorageService qfsf;


    public InfostoreCopyTask(final QuotaFileStorageService qfsf) {
        super();
        this.qfsf = qfsf;
    }

    /**
     * @see com.openexchange.user.copy.CopyUserTaskService#getAlreadyCopied()
     */
    @Override
    public String[] getAlreadyCopied() {
        return new String[] {
            UserCopyTask.class.getName(),
            ContextLoadTask.class.getName(),
            ConnectionFetcherTask.class.getName(),
            FolderCopyTask.class.getName()
        };
    }

    /**
     * @see com.openexchange.user.copy.CopyUserTaskService#getObjectName()
     */
    @Override
    public String getObjectName() {
        return File.class.getName();
    }

    /**
     * @see com.openexchange.user.copy.CopyUserTaskService#copyUser(java.util.Map)
     */
    @Override
    public ObjectMapping<?> copyUser(final Map<String, ObjectMapping<?>> copied) throws OXException {
        final CopyTools copyTools = new CopyTools(copied);
        final Context srcCtx = copyTools.getSourceContext();
        final Context dstCtx = copyTools.getDestinationContext();
        final Integer srcCtxId = copyTools.getSourceContextId();
        final Integer dstCtxId = copyTools.getDestinationContextId();
        final Integer dstUsrId = copyTools.getDestinationUserId();
        final Connection srcCon = copyTools.getSourceConnection();
        final Connection dstCon = copyTools.getDestinationConnection();
        final ObjectMapping<FolderObject> folderMapping = copyTools.getFolderMapping();
        final List<Integer> infostoreFolders = detectInfostoreFolders(folderMapping);

        final Map<DocumentMetadata, List<DocumentMetadata>> originDocuments = loadInfostoreDocumentsFromDB(infostoreFolders, srcCon, i(srcCtxId));
        QuotaFileStorage srcFileStorage = null;
        QuotaFileStorage dstFileStorage = null;
        srcFileStorage = qfsf.getQuotaFileStorage(copyTools.getSourceUserId(), srcCtxId, Info.drive(copyTools.getSourceUserId()));
        dstFileStorage = qfsf.getQuotaFileStorage(dstUsrId, dstCtxId, Info.drive(dstUsrId));

        copyFiles(originDocuments, srcFileStorage, dstFileStorage);
        exchangeFolderIds(originDocuments, folderMapping, dstCon, i(dstCtxId));
        writeInfostoreDocumentsToDB(originDocuments, dstCon, i(dstCtxId), i(dstUsrId));

        return null;
    }

    private void writeInfostoreDocumentsToDB(final Map<DocumentMetadata, List<DocumentMetadata>> documents, final Connection con, final int cid, final int uid) throws OXException {
        PreparedStatement stmt1 = null;
        PreparedStatement stmt2 = null;
        try {
            stmt1 = con.prepareStatement(INSERT_INFOSTORE_MASTERS);
            stmt2 = con.prepareStatement(INSERT_INFOSTORE_VERSIONS);
            for (final DocumentMetadata master : documents.keySet()) {
                final int newId = IDGenerator.getId(cid, Types.INFOSTORE, con);
                int i = 1;
                stmt1.setInt(i++, cid);
                stmt1.setInt(i++, newId);
                stmt1.setInt(i++, (int) master.getFolderId());
                stmt1.setInt(i++, master.getVersion());
                stmt1.setNull(i++, java.sql.Types.BIGINT);
                stmt1.setInt(i++, master.getColorLabel());
                stmt1.setLong(i++, master.getCreationDate().getTime());
                stmt1.setLong(i++, master.getLastModified().getTime());
                stmt1.setInt(i++, uid);
                stmt1.setInt(i++, uid);

                stmt1.addBatch();
                final List<DocumentMetadata> versions = documents.get(master);
                for (final DocumentMetadata version : versions) {
                    i = 1;
                    stmt2.setInt(i++, cid);
                    stmt2.setInt(i++, newId);
                    stmt2.setInt(i++, version.getVersion());
                    stmt2.setLong(i++, version.getCreationDate().getTime());
                    stmt2.setLong(i++, version.getLastModified().getTime());
                    stmt2.setInt(i++, uid);
                    stmt2.setInt(i++, uid);
                    setStringOrNull(i++, stmt2, version.getTitle());
                    setStringOrNull(i++, stmt2, version.getURL());
                    setStringOrNull(i++, stmt2, version.getDescription());
                    setStringOrNull(i++, stmt2, version.getCategories());
                    setStringOrNull(i++, stmt2, version.getFileName());
                    setStringOrNull(i++, stmt2, version.getFilestoreLocation());
                    stmt2.setLong(i++, version.getFileSize());
                    setStringOrNull(i++, stmt2, version.getFileMIMEType());
                    setStringOrNull(i++, stmt2, version.getFileMD5Sum());
                    setStringOrNull(i++, stmt2, version.getVersionComment());

                    stmt2.addBatch();
                }
            }

            stmt1.executeBatch();
            stmt2.executeBatch();
        } catch (final SQLException e) {
            throw UserCopyExceptionCodes.SQL_PROBLEM.create(e);
        } finally {
            DBUtils.closeSQLStuff(stmt1);
            DBUtils.closeSQLStuff(stmt2);
        }

    }

    private void exchangeFolderIds(final Map<DocumentMetadata, List<DocumentMetadata>> documents, final ObjectMapping<FolderObject> folderMapping, final Connection con, final int cid) throws OXException {
        for (final DocumentMetadata master : documents.keySet()) {
            final FolderObject source = folderMapping.getSource((int) master.getFolderId());
            final FolderObject target = folderMapping.getDestination(source);
            master.setFolderId(target.getObjectID());
        }
    }

    private void copyFiles(final Map<DocumentMetadata, List<DocumentMetadata>> documents, final QuotaFileStorage srcFileStorage, final QuotaFileStorage dstFileStorage) throws OXException {
        for (final DocumentMetadata master : documents.keySet()) {
            final List<DocumentMetadata> versions = documents.get(master);
            for (final DocumentMetadata version : versions) {
                final String location = version.getFilestoreLocation();
                if (location != null) {
                    InputStream is = null;
                    try {
                        is = srcFileStorage.getFile(location);
                        if (is == null) {
                            LOG.warn("Did not find file for infostore document {} ({}).", master.getId(), master.getFileName());
                            continue;
                        }

                        final String newId = dstFileStorage.saveNewFile(is);
                        version.setFilestoreLocation(newId);
                    } finally {
                        Streams.close(is);
                    }
                }
            }
        }
    }

    Map<DocumentMetadata, List<DocumentMetadata>> loadInfostoreDocumentsFromDB(final List<Integer> infostoreFolders, final Connection con, final int cid) throws OXException {
        // TODO: Locks will be ignored.
        final Map<DocumentMetadata, List<DocumentMetadata>> documents = new HashMap<DocumentMetadata, List<DocumentMetadata>>();
        if (!infostoreFolders.isEmpty()) {
            PreparedStatement stmt = null;
            ResultSet rs = null;
            try {
                final String sql = replaceIdsInQuery("#IDS#", SELECT_INFOSTORE_MASTERS, infostoreFolders);
                stmt = con.prepareStatement(sql);
                stmt.setInt(1, cid);

                rs = stmt.executeQuery();
                while (rs.next()) {
                    final DocumentMetadata document = new DocumentMetadataImpl();
                    int i = 1;
                    document.setId(rs.getInt(i++));
                    document.setFolderId(rs.getInt(i++));
                    document.setVersion(rs.getInt(i++));
                    document.setColorLabel(rs.getInt(i++));
                    document.setCreationDate(new Date(rs.getLong(i++)));
                    document.setLastModified(new Date(rs.getLong(i++)));

                    documents.put(document, new ArrayList<DocumentMetadata>());
                }
            } catch (final SQLException e) {
                throw UserCopyExceptionCodes.SQL_PROBLEM.create(e);
            } finally {
                DBUtils.closeSQLStuff(rs, stmt);
            }
            for (final DocumentMetadata master : documents.keySet()) {
                final List<DocumentMetadata> versions = documents.get(master);
                try {
                    stmt = con.prepareStatement(SELECT_INFOSTORE_VERSIONS);
                    stmt.setInt(1, cid);
                    stmt.setInt(2, master.getId());

                    rs = stmt.executeQuery();
                    while (rs.next()) {
                        final DocumentMetadata document = new DocumentMetadataImpl();
                        int i = 1;
                        document.setVersion(rs.getInt(i++));
                        document.setCreationDate(new Date(rs.getLong(i++)));
                        document.setLastModified(new Date(rs.getLong(i++)));
                        document.setTitle(rs.getString(i++));
                        document.setURL(rs.getString(i++));
                        document.setDescription(rs.getString(i++));
                        document.setCategories(rs.getString(i++));
                        document.setFileName(rs.getString(i++));
                        document.setFilestoreLocation(rs.getString(i++));
                        document.setFileSize(rs.getLong(i++));
                        document.setFileMIMEType(rs.getString(i++));
                        document.setFileMD5Sum(rs.getString(i++));
                        document.setVersionComment(rs.getString(i++));

                        versions.add(document);
                    }
                } catch (final SQLException e) {
                    throw UserCopyExceptionCodes.SQL_PROBLEM.create(e);
                } finally {
                    DBUtils.closeSQLStuff(rs, stmt);
                }
            }
        }
        return documents;
    }

    List<Integer> detectInfostoreFolders(final ObjectMapping<FolderObject> folderMapping) {
        final List<Integer> ids = new ArrayList<Integer>();
        final List<Integer> folderIds = new ArrayList<Integer>(folderMapping.getSourceKeys());
        for (final Integer sourceId : folderIds) {
            final FolderObject source = folderMapping.getSource(sourceId);

            if (source.getModule() == FolderObject.INFOSTORE) {
                ids.add(sourceId);
            }
        }

        return ids;
    }

    /**
     * @see com.openexchange.user.copy.CopyUserTaskService#done(java.util.Map, boolean)
     */
    @Override
    public void done(final Map<String, ObjectMapping<?>> copied, final boolean failed) {
    }

}
