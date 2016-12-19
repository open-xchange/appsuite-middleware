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

package com.openexchange.groupware.infostore.database.impl.versioncontrol;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import com.openexchange.database.Databases;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.FileStorages;
import com.openexchange.filestore.Info;
import com.openexchange.filestore.QuotaFileStorage;
import com.openexchange.filestore.QuotaFileStorageService;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.InfostoreExceptionCodes;
import com.openexchange.groupware.infostore.database.impl.DocumentMetadataImpl;
import com.openexchange.java.Streams;
import com.openexchange.tools.oxfolder.OXFolderAccess;

/**
 * {@link VersionControlUtil}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public final class VersionControlUtil {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(VersionControlUtil.class);

    /**
     * Initializes a new {@link VersionControlUtil}.
     */
    private VersionControlUtil() {
        super();
    }

    private static void releaseWriteConnection(Context context, Connection con, boolean afterReading, DBProvider provider) {
        if (afterReading) {
            provider.releaseWriteConnectionAfterReading(context, con);
        } else {
            provider.releaseWriteConnection(context, con);
        }
    }

    private static Map<Integer, DocumentMetadata> asMap(List<DocumentMetadata> documents) {
        if (null == documents) {
            return null;
        }

        Map<Integer, DocumentMetadata> m = new HashMap<Integer, DocumentMetadata>(documents.size());
        for (DocumentMetadata document : documents) {
            m.put(Integer.valueOf(document.getId()), document);
        }
        return m;
    }

    private static List<DocumentMetadata> loadVersionsFor(int id, int contextId, Connection con) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT version_number, file_store_location FROM infostore_document WHERE cid=? AND infostore_id=?");
            stmt.setInt(1, contextId);
            stmt.setInt(2, id);

            rs = stmt.executeQuery();
            if (false == rs.next()) {
                return Collections.emptyList();
            }
            List<DocumentMetadata> l = new LinkedList<DocumentMetadata>();
            do {
                String filestoreLocation = rs.getString(2);
                if (!rs.wasNull()) {
                    DocumentMetadataImpl version = new DocumentMetadataImpl();
                    version.setId(id);
                    version.setVersion(rs.getInt(1));
                    version.setFilestoreLocation(filestoreLocation);
                    l.add(version);
                }
            } while (rs.next());
            return l;
        } catch (SQLException e) {
            throw InfostoreExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    private static List<Integer> loadDocumentsFromFolder(long folderId, int contextId, Connection con) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT i.id FROM infostore AS i LEFT JOIN infostore_document AS d ON i.cid=d.cid AND i.id=d.infostore_id WHERE i.cid=? AND i.folder_id=? AND d.file_store_location IS NOT NULL");
            stmt.setInt(1, contextId);
            stmt.setLong(2, folderId);

            rs = stmt.executeQuery();
            if (false == rs.next()) {
                return Collections.emptyList();
            }

            Set<Integer> documents = new LinkedHashSet<Integer>();
            do {
                documents.add(Integer.valueOf(rs.getInt(1)));
            } while (rs.next());
            return new ArrayList<Integer>(documents);
        } catch (SQLException e) {
            throw InfostoreExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    /**
     * Changes the file storage locations for all document versions located in specified folder (if necessary) having its owner changed.
     *
     * @param previousOwner The previous folder owner
     * @param newOwner The new folder owner
     * @param folderId The folder identifier
     * @param context The context
     * @param con The connection to use
     * @return The version control results
     * @throws OXException If operation fails
     */
    public static Map<Integer, List<VersionControlResult>> changeFileStoreLocationsIfNecessary(int previousOwner, int newOwner, long folderId, Context context, Connection con) throws OXException {
        if (previousOwner == newOwner) {
            return Collections.emptyMap();
        }

        int contextId = context.getContextId();
        QuotaFileStorageService qfs = FileStorages.getQuotaFileStorageService();
        QuotaFileStorage prevFs = qfs.getQuotaFileStorage(previousOwner, contextId, Info.drive(previousOwner));
        QuotaFileStorage newFs = qfs.getQuotaFileStorage(newOwner, contextId, Info.drive(newOwner));
        if (prevFs.getUri().equals(newFs.getUri())) {
            return Collections.emptyMap();
        }

        // Need to transfer affected files
        // Load affected versions
        List<Integer> documents = loadDocumentsFromFolder(folderId, contextId, con);
        if (documents.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Integer, List<VersionControlResult>> resultMap = new LinkedHashMap<Integer, List<VersionControlResult>>(documents.size());

        for (Integer id : documents) {
            DocumentMetadataImpl document = new DocumentMetadataImpl();
            document.setId(id.intValue());

            moveVersions(document, prevFs, newFs, resultMap, context, con);
        }

        if (false == resultMap.isEmpty()) {
            applyVersionControl(resultMap, context, con);
        }

        return resultMap;
    }

    /**
     * Performs the version control for specified documents' versions.
     * <p>
     * Each file storage location is checked against the destination folder's one. If they differ, a file move for each version is required.
     * <p>
     * The returned map contains the version control results for those documents whose files were moved during that process.
     *
     * @param provider The database provider to use
     * @param documents The documents to move
     * @param oldDocuments The originating documents
     * @param destinationFolder The destination folder identifier
     * @param context The associated context
     * @return The version control results
     * @throws OXException If operation fails
     */
    public static Map<Integer, List<VersionControlResult>> doVersionControl(DBProvider provider, List<DocumentMetadata> documents, List<DocumentMetadata> oldDocuments, long destinationFolder, Context context) throws OXException {
        Map<Integer, List<VersionControlResult>> resultMap = null;

        Connection wcon = provider.getWriteConnection(context);
        try {
            resultMap = doVersionControl(documents, oldDocuments, destinationFolder, context, wcon);
        } finally {
            releaseWriteConnection(context, wcon, (null == resultMap || resultMap.isEmpty()), provider);
        }

        return resultMap;
    }

    /**
     * Performs the version control for specified documents' versions.
     * <p>
     * Each file storage location is checked against the destination folder's one. If they differ, a file move for each version is required.
     * <p>
     * The returned map contains the version control results for those documents whose files were moved during that process.
     *
     * @param documents The documents to move
     * @param oldDocuments The originating documents
     * @param destinationFolder The destination folder identifier
     * @param context The associated context
     * @param con The connection to use
     * @return The version control results
     * @throws OXException If operation fails
     */
    public static Map<Integer, List<VersionControlResult>> doVersionControl(List<DocumentMetadata> documents, List<DocumentMetadata> oldDocuments, long destinationFolder, Context context, Connection con) throws OXException {
        OXFolderAccess folderAccess = new OXFolderAccess(con, context);
        int contextId = context.getContextId();
        QuotaFileStorageService qfs = FileStorages.getQuotaFileStorageService();
        int folderOwner = folderAccess.getFolderOwner((int) destinationFolder);
        QuotaFileStorage destFs = qfs.getQuotaFileStorage(folderOwner, contextId, Info.drive(folderOwner));
        Map<Integer, DocumentMetadata> oldDocs = asMap(oldDocuments);

        Map<Integer, List<VersionControlResult>> resultMap = new LinkedHashMap<Integer, List<VersionControlResult>>(documents.size());

        for (DocumentMetadata document : documents) {
            Integer id = Integer.valueOf(document.getId());
            DocumentMetadata oldDoc = oldDocs.get(id);
            if (null != oldDoc) {
                folderOwner = folderAccess.getFolderOwner((int) oldDoc.getFolderId());
                QuotaFileStorage srcFs = qfs.getQuotaFileStorage(folderOwner, contextId, Info.drive(folderOwner));
                if (srcFs.getUri().equals(destFs.getUri())) {
                    // Same, so nothing to do
                } else {
                    // Move all versions associated with current document
                    moveVersions(document, srcFs, destFs, resultMap, context, con);
                }
            }
        }

        if (false == resultMap.isEmpty()) {
            applyVersionControl(resultMap, context, con);
        }

        return resultMap;
    }

    private static void moveVersions(DocumentMetadata document, QuotaFileStorage srcFs, QuotaFileStorage destFs, Map<Integer, List<VersionControlResult>> resultMap, Context context, Connection con) throws OXException {
        // Determine affected document versions
        List<DocumentMetadata> versions = loadVersionsFor(document.getId(), context.getContextId(), con);

        // And move them to new file storage location
        List<VersionControlResult> results = new LinkedList<VersionControlResult>();

        // Put into result map
        resultMap.put(Integer.valueOf(document.getId()), results);

        // Move versions to new file storage
        for (DocumentMetadata version : versions) {
            String copiedLocation;
            {
                InputStream in = null;
                try {
                    in = srcFs.getFile(version.getFilestoreLocation());
                    copiedLocation = destFs.saveNewFile(in);
                } catch (OXException e) {
                    for (Map.Entry<Integer, List<VersionControlResult>> documentEntry : resultMap.entrySet()) {
                        Integer documentId = documentEntry.getKey();
                        List<VersionControlResult> versionInfo = documentEntry.getValue();

                        try {
                            VersionControlUtil.restoreVersionControl(Collections.singletonMap(documentId, versionInfo), context, con);
                        } catch (Exception x) {
                            LOG.error("Failed to restore file storage locations for document {} in context {}", documentId, context.getContextId(), x);
                        }
                    }
                    throw e;
                } finally {
                    Streams.close(in);
                }
            }
            srcFs.deleteFile(version.getFilestoreLocation());
            results.add(new VersionControlResult(srcFs, destFs, version.getVersion(), version.getFilestoreLocation(), copiedLocation));
        }
    }

    private static void applyVersionControl(Map<Integer, List<VersionControlResult>> resultMap, Context context, Connection con) throws OXException {
        PreparedStatement stmt = null;
        boolean error = true;
        try {
            stmt = con.prepareStatement("UPDATE infostore_document SET file_store_location=? WHERE cid=? AND infostore_id=? AND version_number=?");

            int contextId = context.getContextId();
            for (Map.Entry<Integer, List<VersionControlResult>> entry : resultMap.entrySet()) {
                int id = entry.getKey().intValue();
                for (VersionControlResult result : entry.getValue()) {
                    stmt.setString(1, result.getDestLocation());
                    stmt.setInt(2, contextId);
                    stmt.setInt(3, id);
                    stmt.setInt(4, result.getVersion());
                    stmt.addBatch();
                }
            }

            stmt.executeBatch();
            error = false;
        } catch (SQLException e) {
            throw InfostoreExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(stmt);
            if (error) {
                for (Map.Entry<Integer, List<VersionControlResult>> documentEntry : resultMap.entrySet()) {
                    Integer documentId = documentEntry.getKey();
                    List<VersionControlResult> versionInfo = documentEntry.getValue();

                    try {
                        VersionControlUtil.restoreVersionControl(Collections.singletonMap(documentId, versionInfo), context, con);
                    } catch (Exception e) {
                        LOG.error("Failed to restore file storage locations for document {} in context {}", documentId, context.getContextId(), e);
                    }
                }
            }
        }
    }

    /**
     * Restores documents' versions file storage locations based on given version control results.
     *
     * @param provider The database provider to use
     * @param resultMap The result map as returned by {@link #doVersionControl(List, List, int, Context, Connection) doVersionControl()}
     * @param context The associated context
     * @return The restored results
     * @throws OXException If operation fails
     */
    public static Map<Integer, List<VersionControlRestored>> restoreVersionControl(DBProvider provider, Map<Integer, List<VersionControlResult>> resultMap, Context context) throws OXException {
        Map<Integer, List<VersionControlRestored>> restoredMap = null;

        Connection wcon = provider.getWriteConnection(context);
        try {
            restoredMap = restoreVersionControl(resultMap, context, wcon);
        } finally {
            releaseWriteConnection(context, wcon, (null == restoredMap || restoredMap.isEmpty()), provider);
        }

        return restoredMap;
    }

    /**
     * Restores documents' versions file storage locations based on given version control results.
     *
     * @param resultMap The result map as returned by {@link #doVersionControl(List, List, int, Context, Connection) doVersionControl()}
     * @param context The associated context
     * @param con The connection to use
     * @return The restored results
     * @throws OXException If operation fails
     */
    public static Map<Integer, List<VersionControlRestored>> restoreVersionControl(Map<Integer, List<VersionControlResult>> resultMap, Context context, Connection con) throws OXException {
        Map<Integer, List<VersionControlRestored>> restoredMap = new LinkedHashMap<Integer, List<VersionControlRestored>>(resultMap.size());

        for (Map.Entry<Integer, List<com.openexchange.groupware.infostore.database.impl.versioncontrol.VersionControlResult>> entry : resultMap.entrySet()) {
            Integer id = entry.getKey();

            List<VersionControlRestored> restoreds = new LinkedList<VersionControlRestored>();
            for (VersionControlResult result : entry.getValue()) {
                QuotaFileStorage destFs = result.getDestFileStorage();
                String restoredLocation;
                {
                    InputStream in = null;
                    try {
                        in = destFs.getFile(result.getDestLocation());
                        restoredLocation = result.getSourceFileStorage().saveNewFile(in);
                    } finally {
                        Streams.close(in);
                    }
                }
                destFs.deleteFile(result.getDestLocation());
                restoreds.add(new VersionControlRestored(result.getVersion(), restoredLocation));
            }
            restoredMap.put(id, restoreds);
        }

        if (false == restoredMap.isEmpty()) {
            applyRestoreds(restoredMap, context, con);
        }

        return restoredMap;
    }

    private static void applyRestoreds(Map<Integer, List<VersionControlRestored>> restoredMap, Context context, Connection con) throws OXException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("UPDATE infostore_document SET file_store_location=? WHERE cid=? AND infostore_id=? AND version_number=?");

            int contextId = context.getContextId();
            for (Map.Entry<Integer, List<VersionControlRestored>> entry : restoredMap.entrySet()) {
                int id = entry.getKey().intValue();
                for (VersionControlRestored restored : entry.getValue()) {
                    stmt.setString(1, restored.getLocation());
                    stmt.setInt(2, contextId);
                    stmt.setInt(3, id);
                    stmt.setInt(4, restored.getVersion());
                    stmt.addBatch();
                }
            }

            stmt.executeBatch();
        } catch (SQLException e) {
            throw InfostoreExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

}
