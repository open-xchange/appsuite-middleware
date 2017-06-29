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

package com.openexchange.groupware.infostore.utils;

import static com.openexchange.java.Autoboxing.*;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.FileStorage;
import com.openexchange.filestore.Info;
import com.openexchange.filestore.QuotaFileStorageService;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.database.impl.DocumentMetadataImpl;
import com.openexchange.java.Strings;
import jonelo.jacksum.algorithm.MD;

/**
 * {@link FileMD5SumHelper}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class FileMD5SumHelper {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(FileMD5SumHelper.class);

    private final DatabaseService dbSerivce;
    private final QuotaFileStorageService fsService;

    /**
     * Initializes a new {@link FileMD5SumHelper}.
     *
     * @param dbService A reference to the database service
     * @param fsService A reference to the file storage service
     */
    public FileMD5SumHelper(DatabaseService dbService, QuotaFileStorageService fsService) {
        super();
        this.dbSerivce = dbService;
        this.fsService = fsService;
    }

    /**
     * Provides a listing of all files in a context with a missing checksum property.
     *
     * @param contextId The identifier of the context to list the files for
     * @return A listing of all files in the context with a missing checksum property
     */
    public List<DocumentMetadata> listMissingInContext(int contextId) throws OXException {
        Connection connection = dbSerivce.getReadOnly(contextId);
        try {
            return listItemsWithoutChecksum(connection, contextId);
        } catch (SQLException e) {
            throw new OXException(e);
        } finally {
            dbSerivce.backReadOnly(contextId, connection);
        }
    }

    /**
     * Provides a listing of all files in a database with a missing checksum property.
     *
     * @param databaseId The read- or write-pool identifier of the database to list the files for
     * @return A listing of all files in the database per context with a missing checksum property
     */
    public Map<Integer, List<DocumentMetadata>> listMissingInDatabase(int databaseId) throws OXException {
        Map<Integer, List<DocumentMetadata>> documentsPerContext = new HashMap<Integer, List<DocumentMetadata>>();
        for (int contextId : dbSerivce.listContexts(databaseId)) {
            documentsPerContext.put(I(contextId), listMissingInContext(contextId));
        }
        return documentsPerContext;
    }

    /**
     * Provides a listing of all files in all contexts with a missing checksum property.
     *
     * @return A listing of all files per context with a missing checksum property
     */
    public Map<Integer, List<DocumentMetadata>> listAllMissing() throws OXException {
        Collection<Integer> databaseIds;
        Connection connection = dbSerivce.getReadOnly();
        try {
            databaseIds = dbSerivce.getAllSchemata(connection).values();
        } finally {
            dbSerivce.backReadOnly(connection);
        }
        Map<Integer, List<DocumentMetadata>> documentsPerContext = new HashMap<Integer, List<DocumentMetadata>>();
        for (Integer databaseId : databaseIds) {
            for (int contextId : dbSerivce.listContexts(databaseId)) {
                documentsPerContext.put(I(contextId), listMissingInContext(contextId));
            }
        }
        return documentsPerContext;
    }

    /**
     * Calculates and stores missing checksums for all files of a specific context.
     *
     * @param contextId The identifier of the context to calculate the missing checksums for
     * @return A listing of all files in the context where the missing checksum was calculated for
     */
    public List<DocumentMetadata> calculateMissingInContext(int contextId) throws OXException {
        LOG.info("Calculating missing file checksums in context {}...", I(contextId));
        List<DocumentMetadata> updatedDocuments;
        Connection connection = dbSerivce.getWritable(contextId);
        int updated = 0;
        boolean rollback = false;
        try {
            Databases.startTransaction(connection);
            rollback = true;
            List<DocumentMetadata> documents = listItemsWithoutChecksum(connection, contextId);
            if (0 < documents.size()) {
                /*
                 * map documents to their filestore owner
                 */
                Map<Integer, List<DocumentMetadata>> documentsPerOwner = getDocumentsPerOwner(connection, contextId, documents);
                /*
                 * calculate checksums
                 */
                updatedDocuments = calculateChecksums(contextId, documentsPerOwner);
                /*
                 * update document metadata
                 */
                updated = updateDocuments(connection, contextId, updatedDocuments);
            } else {
                updatedDocuments = documents;
            }
            connection.commit();
            rollback = false;
        } catch (SQLException e) {
            throw new OXException(e);
        } finally {
            if (rollback) {
                Databases.rollback(connection);
            }
            Databases.autocommit(connection);
            if (0 < updated) {
                dbSerivce.backWritable(contextId, connection);
            } else {
                dbSerivce.backWritableAfterReading(contextId, connection);
            }
        }
        if (updatedDocuments.isEmpty()) {
            LOG.info("No files with missing checksum found in context {}.", I(contextId));
        } else if (1 == updatedDocuments.size()) {
            LOG.info("Calculated and updated checksums for 1 document in context {}.", I(contextId));
        } else {
            LOG.info("Calculated and updated checksums for {} documents in context {}.", I(updatedDocuments.size()), I(contextId));
        }
        return updatedDocuments;
    }

    /**
     * Calculates and stores missing checksums for all files of a database.
     *
     * @param databaseId The read- or write-pool identifier of the database to calculate the missing checksums for
     * @return A listing of all files in the database per context where the missing checksum was calculated for
     */
    public Map<Integer, List<DocumentMetadata>> calculateMissingInDatabase(int databaseId) throws OXException {
        Map<Integer, List<DocumentMetadata>> documentsPerContext = new HashMap<Integer, List<DocumentMetadata>>();
        for (int contextId : dbSerivce.listContexts(databaseId)) {
            documentsPerContext.put(I(contextId), calculateMissingInContext(contextId));
        }
        return documentsPerContext;
    }

    /**
     * Calculates and stores missing checksums for all files in all contexts.
     *
     * @return A listing of all files per context where the missing checksum was calculated for
     */
    public Map<Integer, List<DocumentMetadata>> calculateAllMissing() throws OXException {
        Collection<Integer> databaseIds;
        Connection connection = dbSerivce.getReadOnly();
        try {
            databaseIds = dbSerivce.getAllSchemata(connection).values();
        } finally {
            dbSerivce.backReadOnly(connection);
        }
        Map<Integer, List<DocumentMetadata>> documentsPerContext = new HashMap<Integer, List<DocumentMetadata>>();
        for (Integer databaseId : databaseIds) {
            documentsPerContext.putAll(calculateMissingInDatabase(databaseId.intValue()));
        }
        return documentsPerContext;
    }

    private int updateDocuments(Connection connection, int contextId, List<DocumentMetadata> updatedDocuments) throws SQLException {
        String sql = "UPDATE infostore_document SET file_md5sum=? WHERE cid=? AND infostore_id=? AND version_number=?;";
        int[] updated;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(2, contextId);
            for (DocumentMetadata document : updatedDocuments) {
                stmt.setString(1, document.getFileMD5Sum());
                stmt.setInt(3, document.getId());
                stmt.setInt(4, document.getVersion());
                stmt.addBatch();
            }
            updated = stmt.executeBatch();
        }
        int sum = 0;
        for (int i : updated) {
            if (Statement.SUCCESS_NO_INFO == i) {
                sum++;
            } else {
                sum += i;
            }
        }
        LOG.debug("Updated {} documents in context {}.", I(sum), I(contextId));
        return sum;
    }

    private List<DocumentMetadata> calculateChecksums(int contextId, Map<Integer, List<DocumentMetadata>> documentsPerOwner) throws OXException {
        List<DocumentMetadata> updatedDocuments = new ArrayList<DocumentMetadata>();
        for (Entry<Integer, List<DocumentMetadata>> entry : documentsPerOwner.entrySet()) {
            updatedDocuments.addAll(calculateChecksums(contextId, i(entry.getKey()), entry.getValue()));
        }
        return updatedDocuments;
    }

    private List<DocumentMetadata> calculateChecksums(int contextId, int userId, List<DocumentMetadata> documents) throws OXException {
        List<DocumentMetadata> updatedDocuments = new ArrayList<DocumentMetadata>(documents.size());
        FileStorage fileStorage = getFileStorage(contextId, userId);
        for (DocumentMetadata document : documents) {
            LOG.debug("Calculating checksum for file {}...", toString(contextId, document));
            String md5;
            try (InputStream inputStream = fileStorage.getFile(document.getFilestoreLocation())) {
                md5 = calculateMD5(inputStream);
            } catch (IOException e) {
                throw new OXException(e);
            }
            LOG.debug("Checksum for file {} calculated succesfully: {}", toString(contextId, document), md5);
            DocumentMetadataImpl updatedDocument = new DocumentMetadataImpl(document);
            updatedDocument.setFileMD5Sum(md5);
            updatedDocuments.add(updatedDocument);
        }
        return updatedDocuments;
    }

    private FileStorage getFileStorage(int contextId, int userId) throws OXException {
        return fsService.getQuotaFileStorage(userId, contextId, Info.drive());
    }

    private static Map<Integer, List<DocumentMetadata>> getDocumentsPerOwner(Connection connection, int contextId, List<DocumentMetadata> documents) throws SQLException {
        Map<Integer, List<DocumentMetadata>> documentsPerOwner = new HashMap<Integer, List<DocumentMetadata>>();
        Map<Integer, List<DocumentMetadata>> documentsPerFolder = getDocumentsPerFolder(documents);
        Map<Integer, Integer> ownerPerFolder = getOwnerPerFolder(connection, contextId, documentsPerFolder.keySet());
        for (Entry<Integer, List<DocumentMetadata>> entry : documentsPerFolder.entrySet()) {
            Integer owner = ownerPerFolder.get(entry.getKey());
            put(documentsPerOwner, owner, entry.getValue());
        }
        LOG.debug("Evaluated {} different folder owners for {} documents in context {}", I(documentsPerOwner.size()), I(documents.size()), I(contextId));
        return documentsPerOwner;
    }

    private static Map<Integer, Integer> getOwnerPerFolder(Connection connection, int contextId, Collection<Integer> folderIds) throws SQLException {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("SELECT fuid,created_from FROM oxfolder_tree WHERE cid=? AND fuid");
        if (1 == folderIds.size()) {
            stringBuilder.append("=?;");
        } else {
            stringBuilder.append(" IN (?");
            for (int i = 1; i < folderIds.size(); i++) {
                stringBuilder.append(",?");
            }
            stringBuilder.append(");");
        }
        Map<Integer, Integer> ownerPerFolder = new HashMap<Integer, Integer>();
        try (PreparedStatement stmt = connection.prepareStatement(stringBuilder.toString())) {
            int paramterIndex = 1;
            stmt.setInt(paramterIndex++, contextId);
            for (Integer folderId : folderIds) {
                stmt.setInt(paramterIndex++, folderId.intValue());
            }
            try (ResultSet result = stmt.executeQuery()) {
                while (result.next()) {
                    ownerPerFolder.put(I(result.getInt(1)), I(result.getInt(2)));
                }
            }
        }
        return ownerPerFolder;
    }

    private static List<DocumentMetadata> listItemsWithoutChecksum(Connection connection, int contextId) throws SQLException {
        String sql = "SELECT i.id,i.folder_id,d.version_number,d.file_store_location,d.file_size " +
            "FROM infostore AS i LEFT JOIN infostore_document AS d ON i.cid=d.cid AND i.id=d.infostore_id " +
            "WHERE d.cid=? AND d.file_size>0 AND d.file_md5sum IS NULL;"
        ;
        List<DocumentMetadata> documents = new ArrayList<DocumentMetadata>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, contextId);
            try (ResultSet result = stmt.executeQuery()) {
                while (result.next()) {
                    DocumentMetadataImpl document = new DocumentMetadataImpl(result.getInt(1));
                    document.setFolderId(result.getLong(2));
                    document.setVersion(result.getInt(3));
                    document.setFilestoreLocation(result.getString(4));
                    document.setFileSize(result.getLong(5));
                    if (Strings.isEmpty(document.getFilestoreLocation())) {
                        LOG.warn("Skipping document with empty filestore location: {}", toString(contextId, document));
                        continue;
                    }
                    documents.add(document);
                    LOG.debug("Added document with missing checksum: {}", toString(contextId, document));
                }
            }
        }
        return documents;
    }

    private static Map<Integer, List<DocumentMetadata>> getDocumentsPerFolder(List<DocumentMetadata> documents) {
        Map<Integer, List<DocumentMetadata>> documentsPerFolder = new HashMap<Integer, List<DocumentMetadata>>();
        for (DocumentMetadata document : documents) {
            put(documentsPerFolder, I((int) document.getFolderId()), document);
        }
        return documentsPerFolder;
    }

    private static String calculateMD5(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[65535];
        try {
            MD md5 = new MD("MD5");
            int read;
            do {
                read = inputStream.read(buffer);
                if (0 < read) {
                    md5.update(buffer, 0, read);
                }
            } while (-1 != read);
            return md5.getFormattedValue();
        } catch (NoSuchAlgorithmException e) {
            throw new IOException(e);
        }
    }

    private static <K, V> boolean put(Map<K, List<V>> multiMap, K key, V value) {
        List<V> values = multiMap.get(key);
        if (null == values) {
            values = new ArrayList<>();
            multiMap.put(key, values);
        }
        return values.add(value);
    }

    private static <K, V> boolean put(Map<K, List<V>> multiMap, K key, Collection<? extends V> values) {
        List<V> list = multiMap.get(key);
        if (null == list) {
            list = new ArrayList<>();
            multiMap.put(key, list);
        }
        return list.addAll(values);
    }

    private static String toString(int contextId, DocumentMetadata document) {
        return "DocumentMetadata [contextId=" + contextId + ", folderId=" + document.getFolderId() + ", id=" + document.getId() + ", version=" + document.getVersion() +
            ", filestoreLocation=" + document.getFilestoreLocation() + ", fileSize=" + document.getFileSize() + ", fileMD5Sum=" + document.getFileMD5Sum() + "]";
    }

    public static List<String> toString(int contextId, List<DocumentMetadata> documents) {
        List<String> strings = new ArrayList<String>(documents.size());
        for (DocumentMetadata document : documents) {
            strings.add(toString(contextId, document));
        }
        return strings;
    }

    public static List<String> toString(Map<Integer, List<DocumentMetadata>> documentsPerContext) {
        List<String> strings = new ArrayList<String>(documentsPerContext.size());
        for (Entry<Integer, List<DocumentMetadata>> entry : documentsPerContext.entrySet()) {
            strings.addAll(toString(entry.getKey().intValue(), entry.getValue()));
        }
        return strings;
    }

}
