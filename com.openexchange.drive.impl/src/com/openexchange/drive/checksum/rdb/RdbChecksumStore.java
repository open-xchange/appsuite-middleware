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

package com.openexchange.drive.checksum.rdb;

import static com.openexchange.drive.checksum.rdb.SQL.*;
import static com.openexchange.java.Strings.reverse;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import com.openexchange.database.DatabaseService;
import com.openexchange.drive.DriveExceptionCodes;
import com.openexchange.drive.impl.checksum.ChecksumStore;
import com.openexchange.drive.impl.checksum.DirectoryChecksum;
import com.openexchange.drive.impl.checksum.FileChecksum;
import com.openexchange.drive.impl.internal.DriveServiceLookup;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.composition.FileID;
import com.openexchange.file.storage.composition.FolderID;
import com.openexchange.java.util.UUIDs;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link RdbChecksumStore}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class RdbChecksumStore implements ChecksumStore {

    private static final int DELETE_CHUNK_SIZE = 50;
    private static final int INSERT_CHUNK_SIZE = 50;
    private static final int SELECT_WHERE_IN_CHUNK_SIZE = 500;

    private final int contextID;
    private final DatabaseService databaseService;

    /**
     * Initializes a new {@link RdbChecksumStore}.
     *
     * @param contextID The context ID
     * @throws OXException
     */
    public RdbChecksumStore(int contextID) throws OXException {
        super();
        this.contextID = contextID;
        this.databaseService = DriveServiceLookup.getService(DatabaseService.class, true);
    }

    @Override
    public FileChecksum insertFileChecksum(FileChecksum fileChecksum) throws OXException {
        if (null != fileChecksum.getUuid()) {
            throw new IllegalArgumentException("New file checksums must not contain an UUID");
        }
        Connection connection = databaseService.getWritable(contextID);
        try {
            fileChecksum.setUuid(newUid());
            if (0 == insertFileChecksum(connection, contextID, fileChecksum)) {
                throw DriveExceptionCodes.DB_ERROR.create("File checksum not added: " + fileChecksum);
            }
        } catch (SQLException e) {
            throw SQL.wrap(e);
        } finally {
            databaseService.backWritable(contextID, connection);
        }
        return fileChecksum;
    }

    @Override
    public List<FileChecksum> insertFileChecksums(List<FileChecksum> fileChecksums) throws OXException {
        Connection connection = databaseService.getWritable(contextID);
        try {
            for (int i = 0; i < fileChecksums.size(); i += INSERT_CHUNK_SIZE) {
                /*
                 * prepare chunk
                 */
                int length = Math.min(fileChecksums.size(), i + INSERT_CHUNK_SIZE) - i;
                FileChecksum[] checksums = new FileChecksum[length];
                for (int j = 0; j < length; j++) {
                    FileChecksum checksum = fileChecksums.get(i + j);
                    if (null != checksum.getUuid()) {
                        throw new IllegalArgumentException("New file checksums must not contain an UUID");
                    }
                    checksum.setUuid(newUid());
                    checksums[j] = checksum;
                }
                /*
                 * insert chunk
                 */
                int inserted = insertFileChecksums(connection, contextID, checksums);
                if (checksums.length != inserted) {
                    throw DriveExceptionCodes.DB_ERROR.create(String.valueOf(checksums.length - inserted) + " file checksums not inserted");
                }
            }
        } catch (SQLException e) {
            throw SQL.wrap(e);
        } finally {
            databaseService.backWritable(contextID, connection);
        }
        return fileChecksums;
    }

    @Override
    public FileChecksum updateFileChecksum(FileChecksum fileChecksum) throws OXException {
        return updateFileChecksums(Collections.singletonList(fileChecksum)).get(0);
    }

    @Override
    public List<FileChecksum> updateFileChecksums(List<FileChecksum> fileChecksums) throws OXException {
        Connection connection = databaseService.getWritable(contextID);
        try {
            for (FileChecksum fileChecksum : fileChecksums) {
                if (null == fileChecksum.getUuid()) {
                    throw new IllegalArgumentException("Updating file checksums requires an existing UUID");
                }
                updateFileChecksum(connection, contextID, fileChecksum);
            }
        } catch (SQLException e) {
            throw SQL.wrap(e);
        } finally {
            databaseService.backWritable(contextID, connection);
        }
        return fileChecksums;
    }

    @Override
    public int updateFileChecksumFolders(FolderID folderID, FolderID newFolderID) throws OXException {
        Connection connection = databaseService.getWritable(contextID);
        try {
            return updateFileChecksumFolders(connection, contextID, folderID, newFolderID);
        } catch (SQLException e) {
            throw SQL.wrap(e);
        } finally {
            databaseService.backWritable(contextID, connection);
        }
    }

    @Override
    public boolean removeFileChecksum(FileChecksum fileChecksum) throws OXException {
        return 0 < removeFileChecksums(Collections.singletonList(fileChecksum));
    }

    @Override
    public int removeFileChecksums(List<FileChecksum> fileChecksums) throws OXException {
        Connection connection = databaseService.getWritable(contextID);
        try {
            int deleted = 0;
            for (int i = 0; i < fileChecksums.size(); i += DELETE_CHUNK_SIZE) {
                /*
                 * prepare chunk
                 */
                int length = Math.min(fileChecksums.size(), i + DELETE_CHUNK_SIZE) - i;
                String[] uuids = new String[length];
                for (int j = 0; j < length; j++) {
                    String uuid = fileChecksums.get(i + j).getUuid();
                    if (null == uuid) {
                        throw new IllegalArgumentException("Removing file checksums requires an existing UUID");
                    }
                    uuids[j] = uuid;
                }
                /*
                 * delete chunk
                 */
                deleted += deleteFileChecksums(connection, contextID, uuids);
            }
            return deleted;
        } catch (SQLException e) {
            throw SQL.wrap(e);
        } finally {
            databaseService.backWritable(contextID, connection);
        }
    }

    @Override
    public int removeFileChecksumsInFolder(FolderID folderID) throws OXException {
        return removeFileChecksumsInFolders(Collections.singletonList(folderID));
    }

    @Override
    public int removeFileChecksumsInFolders(List<FolderID> folderIDs) throws OXException {
        int deleted = 0;
        Connection connection = databaseService.getWritable(contextID);
        try {
            for (int i = 0; i < folderIDs.size(); i += DELETE_CHUNK_SIZE) {
                /*
                 * prepare chunk
                 */
                int length = Math.min(folderIDs.size(), i + DELETE_CHUNK_SIZE) - i;
                List<FolderID> chunk = folderIDs.subList(i, i + length);
                /*
                 * delete chunk
                 */
                deleted += deleteFileChecksumsInFolders(connection, contextID, chunk.toArray(new FolderID[chunk.size()]));
            }
            return deleted;
        } catch (SQLException e) {
            throw SQL.wrap(e);
        } finally {
            if (0 < deleted) {
                databaseService.backWritable(contextID, connection);
            } else {
                databaseService.backWritableAfterReading(contextID, connection);
            }
        }
    }

    @Override
    public boolean removeFileChecksum(FileID fileID, String version, long sequenceNumber) throws OXException {
        Connection connection = databaseService.getWritable(contextID);
        try {
            return 0 < deleteFileChecksum(connection, contextID, fileID, version, sequenceNumber);
        } catch (SQLException e) {
            throw SQL.wrap(e);
        } finally {
            databaseService.backWritable(contextID, connection);
        }
    }

    @Override
    public int removeFileChecksums(FileID fileID) throws OXException {
        Connection connection = databaseService.getWritable(contextID);
        try {
            return deleteFileChecksums(connection, contextID, fileID);
        } catch (SQLException e) {
            throw SQL.wrap(e);
        } finally {
            databaseService.backWritable(contextID, connection);
        }
    }

    @Override
    public int removeFileChecksums(FileID...fileIDs) throws OXException {
        int deleted = 0;
        Connection connection = databaseService.getWritable(contextID);
        try {
            for (FileID fileID : fileIDs) {
                deleted += deleteFileChecksums(connection, contextID, fileID);
            }
        } catch (SQLException e) {
            throw SQL.wrap(e);
        } finally {
            if (0 < deleted) {
                databaseService.backWritable(contextID, connection);
            } else {
                databaseService.backWritableAfterReading(contextID, connection);
            }
        }
        return deleted;
    }

    @Override
    public FileChecksum getFileChecksum(FileID fileID, String version, long sequenceNumber) throws OXException {
        Connection connection = databaseService.getReadOnly(contextID);
        try {
            return selectFileChecksum(connection, contextID, fileID, version, sequenceNumber);
        } catch (SQLException e) {
            throw SQL.wrap(e);
        } finally {
            databaseService.backReadOnly(contextID, connection);
        }
    }

    @Override
    public List<FileChecksum> getFileChecksums(FolderID folderID) throws OXException {
        Connection connection = databaseService.getReadOnly(contextID);
        try {
            return selectFileChecksumsInFolder(connection, contextID, folderID);
        } catch (SQLException e) {
            throw SQL.wrap(e);
        } finally {
            databaseService.backReadOnly(contextID, connection);
        }
    }

    @Override
    public Map<String, List<FileChecksum>> getMatchingFileChecksums(List<String> checksums) throws OXException {
        if (null == checksums) {
            return Collections.emptyMap();
        }
        Map<String, List<FileChecksum>> matchingChecksums = new HashMap<String, List<FileChecksum>>();
        Connection connection = databaseService.getReadOnly(contextID);
        try {
            /*
             * select chunk-wise
             */
            for (int i = 0; i < checksums.size(); i += SELECT_WHERE_IN_CHUNK_SIZE) {
                int length = Math.min(checksums.size(), i + SELECT_WHERE_IN_CHUNK_SIZE) - i;
                matchingChecksums.putAll(selectMatchingFileChecksums(connection, contextID, checksums.subList(i, i + length)));
            }
            return matchingChecksums;
        } catch (SQLException e) {
            throw SQL.wrap(e);
        } finally {
            databaseService.backReadOnly(contextID, connection);
        }
    }

    @Override
    public DirectoryChecksum insertDirectoryChecksum(DirectoryChecksum directoryChecksum) throws OXException {
        return insertDirectoryChecksums(Collections.singletonList(directoryChecksum)).get(0);
    }

    @Override
    public List<DirectoryChecksum> insertDirectoryChecksums(List<DirectoryChecksum> directoryChecksums) throws OXException {
        Connection connection = databaseService.getWritable(contextID);
        try {
            for (int i = 0; i < directoryChecksums.size(); i += INSERT_CHUNK_SIZE) {
                /*
                 * prepare chunk
                 */
                int length = Math.min(directoryChecksums.size(), i + INSERT_CHUNK_SIZE) - i;
                DirectoryChecksum[] checksums = new DirectoryChecksum[length];
                for (int j = 0; j < length; j++) {
                    DirectoryChecksum checksum = directoryChecksums.get(i + j);
                    if (null != checksum.getUuid()) {
                        throw new IllegalArgumentException("New directory checksums must not contain an UUID");
                    }
                    checksum.setUuid(newUid());
                    checksums[j] = checksum;
                }
                /*
                 * insert chunk
                 */
                int inserted = insertDirectoryChecksums(connection, contextID, checksums);
                if (checksums.length != inserted) {
                    throw DriveExceptionCodes.DB_ERROR.create(String.valueOf(checksums.length - inserted) + " directory checksums not inserted");
                }
            }
        } catch (SQLException e) {
            throw SQL.wrap(e);
        } finally {
            databaseService.backWritable(contextID, connection);
        }
        return directoryChecksums;
    }

    @Override
    public List<DirectoryChecksum> updateDirectoryChecksums(List<DirectoryChecksum> directoryChecksums) throws OXException {
        Connection connection = databaseService.getWritable(contextID);
        try {
            for (DirectoryChecksum directoryChecksum : directoryChecksums) {
                if (null == directoryChecksum.getUuid()) {
                    throw new IllegalArgumentException("Updating directory checksums requires an existing UUID");
                }
                updateDirectoryChecksum(connection, contextID, directoryChecksum);
            }
        } catch (SQLException e) {
            throw SQL.wrap(e);
        } finally {
            databaseService.backWritable(contextID, connection);
        }
        return directoryChecksums;
    }

    @Override
    public DirectoryChecksum updateDirectoryChecksum(DirectoryChecksum directoryChecksum) throws OXException {
        return updateDirectoryChecksums(Collections.singletonList(directoryChecksum)).get(0);
    }

    @Override
    public boolean updateDirectoryChecksumFolder(FolderID folderID, FolderID newFolderID) throws OXException {
        Connection connection = databaseService.getWritable(contextID);
        try {
            return 0 < updateDirectoryChecksumFolder(connection, contextID, folderID, newFolderID);
        } catch (SQLException e) {
            throw SQL.wrap(e);
        } finally {
            databaseService.backWritable(contextID, connection);
        }
    }

    @Override
    public boolean removeDirectoryChecksum(FolderID folderID) throws OXException {
        return 0 < removeAllDirectoryChecksums(Collections.singletonList(folderID));
    }

    @Override
    public int removeAllDirectoryChecksums(List<FolderID> folderIDs) throws OXException {
        Connection connection = databaseService.getWritable(contextID);
        try {
            return deleteDirectoryChecksums(connection, contextID, folderIDs.toArray(new FolderID[folderIDs.size()]));
        } catch (SQLException e) {
            throw SQL.wrap(e);
        } finally {
            databaseService.backWritable(contextID, connection);
        }
    }

    @Override
    public DirectoryChecksum getDirectoryChecksum(int userID, FolderID folderID, int view) throws OXException {
        List<DirectoryChecksum> directoryChecksums = getDirectoryChecksums(userID, Collections.singletonList(folderID), view);
        return 1 == directoryChecksums.size() ? directoryChecksums.get(0) : null;
    }

    @Override
    public List<DirectoryChecksum> getDirectoryChecksums(int userID, List<FolderID> folderIDs, int view) throws OXException {
        Connection connection = databaseService.getReadOnly(contextID);
        try {
            return selectDirectoryChecksums(connection, contextID, userID, folderIDs.toArray(new FolderID[folderIDs.size()]), view);
        } catch (SQLException e) {
            throw SQL.wrap(e);
        } finally {
            databaseService.backReadOnly(contextID, connection);
        }
    }

    @Override
    public int touchDirectoryChecksums(List<DirectoryChecksum> directoryChecksums) throws OXException {
        int touched = 0;
        Connection connection = databaseService.getWritable(contextID);
        try {
            for (int i = 0; i < directoryChecksums.size(); i += INSERT_CHUNK_SIZE) {
                /*
                 * prepare chunk
                 */
                int length = Math.min(directoryChecksums.size(), i + INSERT_CHUNK_SIZE) - i;
                String[] uuids = new String[length];
                for (int j = 0; j < length; j++) {
                    String uuid = directoryChecksums.get(i + j).getUuid();
                    if (null == uuid) {
                        throw new IllegalArgumentException("Touching directory checksums requires an existing UUID");
                    }
                    uuids[j] = uuid;
                }
                /*
                 * touch chunk
                 */
                touched += touchDirectoryChecksums(connection, contextID, uuids);
            }
        } catch (SQLException e) {
            throw SQL.wrap(e);
        } finally {
            if (0 < touched) {
                databaseService.backWritable(contextID, connection);
            } else {
                databaseService.backWritableAfterReading(contextID, connection);
            }
        }
        return touched;
    }

    @Override
    public List<DirectoryChecksum> getUnusedDirectoryChecksums(long unusedSince) throws OXException {
        Connection connection = databaseService.getReadOnly(contextID);
        try {
            return selectUnusedDirectoryChecksums(connection, contextID, unusedSince);
        } catch (SQLException e) {
            throw SQL.wrap(e);
        } finally {
            databaseService.backReadOnly(contextID, connection);
        }
    }

    @Override
    public List<DirectoryChecksum> getDirectoryChecksums(List<FolderID> folderIDs) throws OXException {
        if (null == folderIDs) {
            return Collections.emptyList();
        }
        List<DirectoryChecksum> checksums = new ArrayList<DirectoryChecksum>();
        Connection connection = databaseService.getReadOnly(contextID);
        try {
            /*
             * select chunk-wise
             */
            for (int i = 0; i < folderIDs.size(); i += SELECT_WHERE_IN_CHUNK_SIZE) {
                int length = Math.min(folderIDs.size(), i + SELECT_WHERE_IN_CHUNK_SIZE) - i;
                checksums.addAll(selectDirectoryChecksums(connection, contextID, folderIDs.subList(i, i + length)));
            }
            return checksums;
        } catch (SQLException e) {
            throw SQL.wrap(e);
        } finally {
            databaseService.backReadOnly(contextID, connection);
        }
    }

    @Override
    public int removeDirectoryChecksums(List<DirectoryChecksum> directoryChecksums) throws OXException {
        Connection connection = databaseService.getWritable(contextID);
        try {
            int deleted = 0;
            for (int i = 0; i < directoryChecksums.size(); i += DELETE_CHUNK_SIZE) {
                /*
                 * prepare chunk
                 */
                int length = Math.min(directoryChecksums.size(), i + DELETE_CHUNK_SIZE) - i;
                String[] uuids = new String[length];
                for (int j = 0; j < length; j++) {
                    String uuid = directoryChecksums.get(i + j).getUuid();
                    if (null == uuid) {
                        throw new IllegalArgumentException("Removing directory checksums requires an existing UUID");
                    }
                    uuids[j] = uuid;
                }
                /*
                 * delete chunk
                 */
                deleted += deleteDirectoryChecksums(connection, contextID, uuids);
            }
            return deleted;
        } catch (SQLException e) {
            throw SQL.wrap(e);
        } finally {
            databaseService.backWritable(contextID, connection);
        }
    }

    private static String newUid() {
        return UUIDs.getUnformattedString(UUID.randomUUID());
    }

    private static int insertFileChecksum(Connection connection, int cid, FileChecksum fileChecksum) throws SQLException, OXException {
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(SQL.INSERT_FILE_CHECKSUM_STMT);
            stmt.setString(1, fileChecksum.getUuid());
            stmt.setInt(2, cid);
            stmt.setString(3, escapeFolder(fileChecksum.getFileID()));
            stmt.setString(4, escapeFile(fileChecksum.getFileID()));
            stmt.setString(5, fileChecksum.getVersion());
            stmt.setLong(6, fileChecksum.getSequenceNumber());
            stmt.setString(7, fileChecksum.getChecksum());
            return SQL.logExecuteUpdate(stmt);
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
    }

    private static int insertFileChecksums(Connection connection, int cid, FileChecksum[] fileChecksums) throws SQLException, OXException {
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(SQL.INSERT_FILE_CHECKSUMS_STMT(fileChecksums.length));
            int parameterIndex = 1;
            for (FileChecksum fileChecksum : fileChecksums) {
                stmt.setString(parameterIndex++, fileChecksum.getUuid());
                stmt.setInt(parameterIndex++, cid);
                stmt.setString(parameterIndex++, escapeFolder(fileChecksum.getFileID()));
                stmt.setString(parameterIndex++, escapeFile(fileChecksum.getFileID()));
                stmt.setString(parameterIndex++, fileChecksum.getVersion());
                stmt.setLong(parameterIndex++, fileChecksum.getSequenceNumber());
                stmt.setString(parameterIndex++, fileChecksum.getChecksum());
            }
            return SQL.logExecuteUpdate(stmt);
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
    }

    private static int updateFileChecksum(Connection connection, int cid, FileChecksum fileChecksum) throws SQLException, OXException {
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(SQL.UPDATE_FILE_CHECKSUM_STMT);
            stmt.setString(1, escapeFolder(fileChecksum.getFileID()));
            stmt.setString(2, escapeFile(fileChecksum.getFileID()));
            stmt.setString(3, fileChecksum.getVersion());
            stmt.setLong(4, fileChecksum.getSequenceNumber());
            stmt.setString(5, fileChecksum.getChecksum());
            stmt.setString(6, fileChecksum.getUuid());
            stmt.setInt(7, cid);
            return SQL.logExecuteUpdate(stmt);
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
    }

    private static int updateFileChecksumFolders(Connection connection, int cid, FolderID folder, FolderID newFolder) throws SQLException, OXException {
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(SQL.UPDATE_FILE_CHECKSUM_FOLDERS_STMT);
            stmt.setString(1, escapeFolder(newFolder));
            stmt.setInt(2, cid);
            stmt.setString(3, escapeFolder(folder));
            return SQL.logExecuteUpdate(stmt);
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
    }

    private static int deleteFileChecksums(Connection connection, int cid, String[] uuids) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(SQL.DELETE_FILE_CHECKSUMS_STMT(uuids.length));
            stmt.setInt(1, cid);
            for (int i = 0; i < uuids.length; i++) {
                stmt.setBytes(i + 2, SQL.getBytes(uuids[i]));
            }
            return SQL.logExecuteUpdate(stmt);
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
    }

    private static int deleteFileChecksumsInFolders(Connection connection, int cid, FolderID[] folderIDs) throws SQLException, OXException {
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(SQL.DELETE_FILE_CHECKSUMS_IN_FOLDER_STMT(folderIDs.length));
            stmt.setInt(1, cid);
            for (int i = 0; i < folderIDs.length; i++) {
                stmt.setString(i + 2, reverse(escapeFolder(folderIDs[i])));
            }
            return SQL.logExecuteUpdate(stmt);
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
    }

    private static int deleteFileChecksum(Connection connection, int cid, FileID file, String version, long sequence) throws SQLException, OXException {
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(SQL.DELETE_FILE_CHECKSUM_STMT);
            stmt.setInt(1, cid);
            stmt.setString(2, escapeFolder(file));
            stmt.setString(3, escapeFile(file));
            stmt.setString(4, version);
            stmt.setLong(5, sequence);
            return SQL.logExecuteUpdate(stmt);
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
    }

    private static int deleteFileChecksums(Connection connection, int cid, FileID file) throws SQLException, OXException {
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(SQL.DELETE_FILE_CHECKSUMS_STMT);
            stmt.setInt(1, cid);
            stmt.setString(2, escapeFolder(file));
            stmt.setString(3, escapeFile(file));
            return SQL.logExecuteUpdate(stmt);
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
    }

    private static FileChecksum selectFileChecksum(Connection connection, int cid, FileID file, String version, long sequence) throws SQLException, OXException {
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(SQL.SELECT_FILE_CHECKSUM_STMT);
            stmt.setInt(1, cid);
            stmt.setString(2, escapeFolder(file));
            stmt.setString(3, escapeFile(file));
            stmt.setString(4, version);
            stmt.setLong(5, sequence);
            ResultSet resultSet = SQL.logExecuteQuery(stmt);
            if (resultSet.next()) {
                FileChecksum fileChecksum = new FileChecksum();
                fileChecksum.setFileID(file);
                fileChecksum.setVersion(version);
                fileChecksum.setSequenceNumber(sequence);
                fileChecksum.setUuid(resultSet.getString(1));
                fileChecksum.setChecksum(resultSet.getString(2));
                return fileChecksum;
            } else {
                return null;
            }
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
    }

    private static List<FileChecksum> selectFileChecksumsInFolder(Connection connection, int cid, FolderID folder) throws SQLException, OXException {
        List<FileChecksum> fileChecksums = new ArrayList<FileChecksum>();
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(SQL.SELECT_FILE_CHECKSUMS_IN_FOLDER_STMT);
            stmt.setInt(1, cid);
            stmt.setString(2, escapeFolder(folder));
            ResultSet resultSet = SQL.logExecuteQuery(stmt);
            while (resultSet.next()) {
                FileChecksum fileChecksum = new FileChecksum();
                fileChecksum.setUuid(resultSet.getString(1));
                fileChecksum.setFileID(unescapeFile(folder, resultSet.getString(2)));
                fileChecksum.setVersion(resultSet.getString(3));
                fileChecksum.setSequenceNumber(resultSet.getLong(4));
                fileChecksum.setChecksum(resultSet.getString(5));
                fileChecksums.add(fileChecksum);
            }
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
        return fileChecksums;
    }

    private static Map<String, List<FileChecksum>> selectMatchingFileChecksums(Connection connection, int cid, List<String> checksums) throws SQLException, OXException {
        Map<String, List<FileChecksum>> fileChecksums = new HashMap<String, List<FileChecksum>>();
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(SQL.SELECT_MATCHING_FILE_CHECKSUMS_STMT(checksums.size()));
            stmt.setInt(1, cid);
            for (int i = 0; i < checksums.size(); i++) {
                stmt.setBytes(i + 2, SQL.getBytes(checksums.get(i)));
            }
            ResultSet resultSet = SQL.logExecuteQuery(stmt);
            while (resultSet.next()) {
                FileChecksum fileChecksum = new FileChecksum();
                fileChecksum.setUuid(resultSet.getString(1));
                FolderID folderID = unescapeFolder(resultSet.getString(2));
                fileChecksum.setFileID(unescapeFile(folderID, resultSet.getString(3)));
                fileChecksum.setVersion(resultSet.getString(4));
                fileChecksum.setSequenceNumber(resultSet.getLong(5));
                String checksum = resultSet.getString(6);
                fileChecksum.setChecksum(checksum);
                List<FileChecksum> matchingChecksums = fileChecksums.get(checksum);
                if (null == matchingChecksums) {
                    matchingChecksums = new ArrayList<FileChecksum>();
                    fileChecksums.put(checksum, matchingChecksums);
                }
                matchingChecksums.add(fileChecksum);
            }
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
        return fileChecksums;
    }

    private static int insertDirectoryChecksums(Connection connection, int cid, DirectoryChecksum[] directoryChecksums) throws SQLException, OXException {
        long currentTimeMillis = System.currentTimeMillis();
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(SQL.INSERT_DIRECTORY_CHECKSUMS_STMT(directoryChecksums.length));
            int parameterIndex = 1;
            for (DirectoryChecksum directoryChecksum : directoryChecksums) {
                stmt.setString(parameterIndex++, directoryChecksum.getUuid());
                stmt.setInt(parameterIndex++, cid);
                stmt.setInt(parameterIndex++, directoryChecksum.getUserID());
                stmt.setInt(parameterIndex++, directoryChecksum.getView());
                stmt.setString(parameterIndex++, escapeFolder(directoryChecksum.getFolderID()));
                stmt.setLong(parameterIndex++, directoryChecksum.getSequenceNumber());
                stmt.setString(parameterIndex++, directoryChecksum.getETag());
                stmt.setString(parameterIndex++, directoryChecksum.getChecksum());
                stmt.setLong(parameterIndex++, currentTimeMillis);
            }
            return SQL.logExecuteUpdate(stmt);
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
    }

    private static int updateDirectoryChecksum(Connection connection, int cid, DirectoryChecksum directoryChecksum) throws SQLException, OXException {
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(SQL.UPDATE_DIRECTORY_CHECKSUM_STMT);
            stmt.setString(1, escapeFolder(directoryChecksum.getFolderID()));
            stmt.setLong(2, directoryChecksum.getSequenceNumber());
            stmt.setString(3, directoryChecksum.getETag());
            stmt.setString(4, directoryChecksum.getChecksum());
            stmt.setLong(5, System.currentTimeMillis());
            stmt.setInt(6, cid);
            stmt.setString(7, directoryChecksum.getUuid());
            return SQL.logExecuteUpdate(stmt);
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
    }

    private static int updateDirectoryChecksumFolder(Connection connection, int cid, FolderID folder, FolderID newFolder) throws SQLException, OXException {
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(SQL.UPDATE_DIRECTORY_CHECKSUM_FOLDER_STMT);
            stmt.setString(1, escapeFolder(newFolder));
            stmt.setLong(2, System.currentTimeMillis());
            stmt.setInt(3, cid);
            stmt.setString(4, escapeFolder(folder));
            return SQL.logExecuteUpdate(stmt);
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
    }

    private static int deleteDirectoryChecksums(Connection connection, int cid, FolderID[] folderIDs) throws SQLException, OXException {
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(SQL.DELETE_DIRECTORY_CHECKSUMS_FOR_FOLDER_STMT(folderIDs.length));
            stmt.setInt(1, cid);
            for (int i = 0; i < folderIDs.length; i++) {
                stmt.setString(i + 2, reverse(escapeFolder(folderIDs[i])));
            }
            return SQL.logExecuteUpdate(stmt);
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
    }

    private static List<DirectoryChecksum> selectDirectoryChecksums(Connection connection, int cid, int user, FolderID[] folderIDs, int view) throws SQLException, OXException {
        List<DirectoryChecksum> directoryChecksums = new ArrayList<DirectoryChecksum>();
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(SQL.SELECT_DIRECTORY_CHECKSUMS_STMT(folderIDs.length));
            int parameterIndex = 1;
            stmt.setInt(parameterIndex++, cid);
            stmt.setInt(parameterIndex++, user);
            stmt.setInt(parameterIndex++, view);
            for (int i = 0; i < folderIDs.length; i++) {
                stmt.setString(parameterIndex++, reverse(escapeFolder(folderIDs[i])));
            }
            ResultSet resultSet = SQL.logExecuteQuery(stmt);
            while (resultSet.next()) {
                DirectoryChecksum directoryChecksum = new DirectoryChecksum();
                directoryChecksum.setUserID(user);
                directoryChecksum.setView(view);
                directoryChecksum.setUuid(resultSet.getString(1));
                directoryChecksum.setFolderID(unescapeFolder(resultSet.getString(2)));
                directoryChecksum.setSequenceNumber(resultSet.getLong(3));
                directoryChecksum.setETag(resultSet.getString(4));
                directoryChecksum.setChecksum(resultSet.getString(5));
                directoryChecksums.add(directoryChecksum);
            }
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
        return directoryChecksums;
    }

    private static int touchDirectoryChecksums(Connection connection, int cid, String[] uuids) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(SQL.TOUCH_DIRECTORY_CHECKSUMS_STMT(uuids.length));
            stmt.setLong(1, System.currentTimeMillis());
            stmt.setInt(2, cid);
            for (int i = 0; i < uuids.length; i++) {
                stmt.setBytes(i + 3, SQL.getBytes(uuids[i]));
            }
            return SQL.logExecuteUpdate(stmt);
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
    }

    private static List<DirectoryChecksum> selectUnusedDirectoryChecksums(Connection connection, int cid, long unusedSince) throws SQLException, OXException {
        List<DirectoryChecksum> directoryChecksums = new ArrayList<DirectoryChecksum>();
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(SQL.SELECT_UNUSED_DIRECTORY_CHECKSUMS_STMT);
            stmt.setInt(1, cid);
            stmt.setLong(2, unusedSince);
            ResultSet resultSet = SQL.logExecuteQuery(stmt);
            while (resultSet.next()) {
                DirectoryChecksum directoryChecksum = new DirectoryChecksum();
                directoryChecksum.setUuid(resultSet.getString(1));
                directoryChecksum.setFolderID(unescapeFolder(resultSet.getString(2)));
                directoryChecksum.setSequenceNumber(resultSet.getLong(3));
                directoryChecksum.setETag(resultSet.getString(4));
                directoryChecksum.setUserID(resultSet.getInt(5));
                directoryChecksum.setView(resultSet.getInt(6));
                directoryChecksum.setChecksum(resultSet.getString(7));
                directoryChecksums.add(directoryChecksum);
            }
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
        return directoryChecksums;
    }

    private static List<DirectoryChecksum> selectDirectoryChecksums(Connection connection, int cid, List<FolderID> folderIDs) throws SQLException, OXException {
        List<DirectoryChecksum> directoryChecksums = new ArrayList<DirectoryChecksum>();
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(SQL.SELECT_ALL_DIRECTORY_CHECKSUMS_STMT(folderIDs.size()));
            int parameterIndex = 1;
            stmt.setInt(parameterIndex++, cid);
            for (FolderID folderID : folderIDs) {
                stmt.setString(parameterIndex++, reverse(escapeFolder(folderID)));
            }
            ResultSet resultSet = SQL.logExecuteQuery(stmt);
            while (resultSet.next()) {
                DirectoryChecksum directoryChecksum = new DirectoryChecksum();
                directoryChecksum.setUuid(resultSet.getString(1));
                directoryChecksum.setFolderID(unescapeFolder(resultSet.getString(2)));
                directoryChecksum.setSequenceNumber(resultSet.getLong(3));
                directoryChecksum.setETag(resultSet.getString(4));
                directoryChecksum.setUserID(resultSet.getInt(5));
                directoryChecksum.setView(resultSet.getInt(6));
                directoryChecksum.setChecksum(resultSet.getString(7));
                directoryChecksums.add(directoryChecksum);
            }
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
        return directoryChecksums;
    }

    private static int deleteDirectoryChecksums(Connection connection, int cid, String[] uuids) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(SQL.DELETE_DIRECTORY_CHECKSUMS_STMT(uuids.length));
            stmt.setInt(1, cid);
            for (int i = 0; i < uuids.length; i++) {
                stmt.setBytes(i + 2, SQL.getBytes(uuids[i]));
            }
            return SQL.logExecuteUpdate(stmt);
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
    }

}

