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

package com.openexchange.groupware.infostore.database.impl.versioncontrol;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.QuotaFileStorage;
import com.openexchange.filestore.QuotaFileStorageService;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.InfostoreExceptionCodes;
import com.openexchange.groupware.infostore.database.impl.DocumentMetadataImpl;
import com.openexchange.tools.file.FileStorages;
import com.openexchange.tools.oxfolder.OXFolderAccess;

/**
 * {@link VersionControlUtil}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public final class VersionControlUtil {

    /**
     * Initializes a new {@link VersionControlUtil}.
     */
    private VersionControlUtil() {
        super();
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
                DocumentMetadataImpl version = new DocumentMetadataImpl();
                version.setId(id);
                version.setVersion(rs.getInt(1));
                version.setFilestoreLocation(rs.getString(2));
                l.add(version);
            } while (rs.next());
            return l;
        } catch (SQLException e) {
            throw InfostoreExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    public static Map<Integer, List<Result>> doVersionControl(List<DocumentMetadata> documents, List<DocumentMetadata> oldDocuments, int destinationFolder, Context context, Connection con) throws OXException {
        OXFolderAccess folderAccess = new OXFolderAccess(con, context);
        int contextId = context.getContextId();
        QuotaFileStorageService qfs = FileStorages.getQuotaFileStorageService();
        QuotaFileStorage destFs = qfs.getQuotaFileStorage(folderAccess.getFolderOwner(destinationFolder), contextId);
        Map<Integer, DocumentMetadata> oldDocs = asMap(oldDocuments);

        Map<Integer, List<Result>> resultMap = new LinkedHashMap<Integer, List<Result>>(documents.size());

        for (DocumentMetadata document : documents) {
            Integer id = Integer.valueOf(document.getId());
            DocumentMetadata oldDoc = oldDocs.get(id);
            if (null != oldDoc) {
                QuotaFileStorage srcFs = qfs.getQuotaFileStorage(folderAccess.getFolderOwner((int) oldDoc.getFolderId()), contextId);
                if (srcFs.getUri().equals(destFs.getUri())) {
                    // Same, so nothing to do
                } else {
                    // Determine affected document versions
                    List<DocumentMetadata> versions = loadVersionsFor(document.getId(), contextId, con);

                    // And move them to new file storage location
                    List<Result> results = new LinkedList<Result>();
                    for (DocumentMetadata version : versions) {
                        String copiedLocation = destFs.saveNewFile(srcFs.getFile(version.getFilestoreLocation()));
                        srcFs.deleteFile(version.getFilestoreLocation());
                        results.add(new Result(srcFs, destFs, version.getVersion(), version.getFilestoreLocation(), copiedLocation));
                    }

                    // Put into result map
                    resultMap.put(id, results);
                }
            }
        }

        applyVersionControl(resultMap, context, con);

        return resultMap;
    }

    private static void applyVersionControl(Map<Integer, List<Result>> resultMap, Context context, Connection con) throws OXException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("UPDATE infostore_document SET file_store_location=? WHERE cid=? AND infostore_id=? AND version_number=?");

            int contextId = context.getContextId();
            for (Map.Entry<Integer, List<Result>> entry : resultMap.entrySet()) {
                int id = entry.getKey().intValue();
                for (Result result : entry.getValue()) {
                    stmt.setString(1, result.getDestLocation());
                    stmt.setInt(2, contextId);
                    stmt.setInt(3, id);
                    stmt.setInt(4, result.getVersion());
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

    public static Map<Integer, List<Restored>> restoreVersionControl(Map<Integer, List<Result>> resultMap, Context context, Connection con) throws OXException {
        Map<Integer, List<Restored>> restoredMap = new LinkedHashMap<Integer, List<Restored>>(resultMap.size());

        for (Map.Entry<Integer, List<com.openexchange.groupware.infostore.database.impl.versioncontrol.Result>> entry : resultMap.entrySet()) {
            Integer id = entry.getKey();

            List<Restored> restoreds = new LinkedList<Restored>();
            for (Result result : entry.getValue()) {
                QuotaFileStorage destFs = result.getDestFileStorage();
                String restoredLocation = result.getSourceFileStorage().saveNewFile(destFs.getFile(result.getDestLocation()));
                destFs.deleteFile(result.getDestLocation());
                restoreds.add(new Restored(result.getVersion(), restoredLocation));
            }
            restoredMap.put(id, restoreds);
        }

        applyRestoreds(restoredMap, context, con);

        return restoredMap;
    }

    private static void applyRestoreds(Map<Integer, List<Restored>> restoredMap, Context context, Connection con) throws OXException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("UPDATE infostore_document SET file_store_location=? WHERE cid=? AND infostore_id=? AND version_number=?");

            int contextId = context.getContextId();
            for (Map.Entry<Integer, List<Restored>> entry : restoredMap.entrySet()) {
                int id = entry.getKey().intValue();
                for (Restored restored : entry.getValue()) {
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
