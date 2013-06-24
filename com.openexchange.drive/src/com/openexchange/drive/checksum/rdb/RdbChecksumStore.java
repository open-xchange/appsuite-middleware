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
 *     Copyright (C) 2004-2013 Open-Xchange, Inc.
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

import static com.openexchange.drive.checksum.rdb.SQL.escapeFile;
import static com.openexchange.drive.checksum.rdb.SQL.escapeFolder;
import static com.openexchange.drive.checksum.rdb.SQL.unescapeFile;
import static com.openexchange.drive.checksum.rdb.SQL.unescapeFolder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import com.openexchange.database.DatabaseService;
import com.openexchange.drive.DriveExceptionCodes;
import com.openexchange.drive.checksum.ChecksumStore;
import com.openexchange.drive.checksum.DirectoryChecksum;
import com.openexchange.drive.checksum.FileChecksum;
import com.openexchange.drive.internal.DriveServiceLookup;
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
    public FileChecksum insertFileChecksum(FileID fileID, String version, long sequenceNumber, String checksum) throws OXException {
        FileChecksum fileChecksum = new FileChecksum();
        fileChecksum.setFileID(fileID);
        fileChecksum.setVersion(version);
        fileChecksum.setSequenceNumber(sequenceNumber);
        fileChecksum.setChecksum(checksum);
        return insertFileChecksum(fileChecksum);
    }

    @Override
    public FileChecksum insertFileChecksum(FileChecksum fileChecksum) throws OXException {
        return insertFileChecksums(Arrays.asList(new FileChecksum[] { fileChecksum })).get(0);
    }

    @Override
    public List<FileChecksum> insertFileChecksums(List<FileChecksum> fileChecksums) throws OXException {
        Connection connection = databaseService.getWritable(contextID);
        try {
            for (FileChecksum fileChecksum : fileChecksums) {
                if (null != fileChecksum.getUuid()) {
                    throw new IllegalArgumentException("New file checksums must not contain an UUID");
                }
                fileChecksum.setUuid(newUid());
                if (0 == insertFileChecksum(connection, contextID, fileChecksum)) {
                    throw DriveExceptionCodes.DB_ERROR.create("File checksum not added: " + fileChecksum);
                }
            }
        } catch (SQLException e) {
            throw DriveExceptionCodes.DB_ERROR.create(e, e.getMessage());
        } finally {
            databaseService.backWritable(contextID, connection);
        }
        return fileChecksums;
    }

    @Override
    public FileChecksum updateFileChecksum(FileChecksum fileChecksum) throws OXException {
        return updateFileChecksums(Arrays.asList(new FileChecksum[] { fileChecksum })).get(0);
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
            throw DriveExceptionCodes.DB_ERROR.create(e, e.getMessage());
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
            throw DriveExceptionCodes.DB_ERROR.create(e, e.getMessage());
        } finally {
            databaseService.backWritable(contextID, connection);
        }
    }

    @Override
    public boolean removeFileChecksum(FileChecksum fileChecksum) throws OXException {
        return 0 < removeFileChecksums(Arrays.asList(new FileChecksum[] { fileChecksum }));
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
            throw DriveExceptionCodes.DB_ERROR.create(e, e.getMessage());
        } finally {
            databaseService.backWritable(contextID, connection);
        }
    }

    @Override
    public int removeFileChecksumsInFolder(FolderID folderID) throws OXException {
        Connection connection = databaseService.getWritable(contextID);
        try {
            return deleteFileChecksumsInFolder(connection, contextID, folderID);
        } catch (SQLException e) {
            throw DriveExceptionCodes.DB_ERROR.create(e, e.getMessage());
        } finally {
            databaseService.backWritable(contextID, connection);
        }
    }

    @Override
    public boolean removeFileChecksum(FileID fileID, String version, long sequenceNumber) throws OXException {
        Connection connection = databaseService.getWritable(contextID);
        try {
            return 0 < deleteFileChecksum(connection, contextID, fileID, version, sequenceNumber);
        } catch (SQLException e) {
            throw DriveExceptionCodes.DB_ERROR.create(e, e.getMessage());
        } finally {
            databaseService.backWritable(contextID, connection);
        }
    }

    @Override
    public FileChecksum getFileChecksum(FileID fileID, String version, long sequenceNumber) throws OXException {
        Connection connection = databaseService.getReadOnly(contextID);
        try {
            return selectFileChecksum(connection, contextID, fileID, version, sequenceNumber);
        } catch (SQLException e) {
            throw DriveExceptionCodes.DB_ERROR.create(e, e.getMessage());
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
            throw DriveExceptionCodes.DB_ERROR.create(e, e.getMessage());
        } finally {
            databaseService.backReadOnly(contextID, connection);
        }
    }

    @Override
    public List<FileChecksum> getMatchingFileChecksums(String checksum) throws OXException {
        Connection connection = databaseService.getReadOnly(contextID);
        try {
            return selectMatchingFileChecksums(connection, contextID, checksum);
        } catch (SQLException e) {
            throw DriveExceptionCodes.DB_ERROR.create(e, e.getMessage());
        } finally {
            databaseService.backReadOnly(contextID, connection);
        }
    }

    @Override
    public Map<String, List<FileChecksum>> getMatchingFileChecksums(List<String> checksums) throws OXException {
        if (null == checksums) {
            return Collections.emptyMap();
        }
        Connection connection = databaseService.getReadOnly(contextID);
        try {
            return selectMatchingFileChecksums(connection, contextID, checksums);
        } catch (SQLException e) {
            throw DriveExceptionCodes.DB_ERROR.create(e, e.getMessage());
        } finally {
            databaseService.backReadOnly(contextID, connection);
        }
    }

    @Override
    public DirectoryChecksum insertDirectoryChecksum(DirectoryChecksum directoryChecksum) throws OXException {
        return insertDirectoryChecksums(Arrays.asList(new DirectoryChecksum[] { directoryChecksum })).get(0);
    }

    @Override
    public DirectoryChecksum insertDirectoryChecksum(FolderID folderID, long sequenceNumber, String checksum) throws OXException {
        return insertDirectoryChecksum(new DirectoryChecksum(folderID, sequenceNumber, checksum));
    }

    @Override
    public List<DirectoryChecksum> insertDirectoryChecksums(List<DirectoryChecksum> directoryChecksums) throws OXException {
        Connection connection = databaseService.getWritable(contextID);
        try {
            for (DirectoryChecksum directoryChecksum : directoryChecksums) {
                if (null != directoryChecksum.getUuid()) {
                    throw new IllegalArgumentException("New directory checksums must not contain an UUID");
                }
                directoryChecksum.setUuid(newUid());
                if (0 == insertDirectoryChecksum(connection, contextID, directoryChecksum)) {
                    throw DriveExceptionCodes.DB_ERROR.create("File checksum not added: " + directoryChecksum);
                }
            }
        } catch (SQLException e) {
            throw DriveExceptionCodes.DB_ERROR.create(e, e.getMessage());
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
            throw DriveExceptionCodes.DB_ERROR.create(e, e.getMessage());
        } finally {
            databaseService.backWritable(contextID, connection);
        }
        return directoryChecksums;
    }

    @Override
    public DirectoryChecksum updateDirectoryChecksum(DirectoryChecksum directoryChecksum) throws OXException {
        return updateDirectoryChecksums(Arrays.asList(new DirectoryChecksum[] { directoryChecksum })).get(0);
    }

    @Override
    public boolean updateDirectoryChecksumFolder(FolderID folderID, FolderID newFolderID) throws OXException {
        Connection connection = databaseService.getWritable(contextID);
        try {
            return 0 < updateDirectoryChecksumFolder(connection, contextID, folderID, newFolderID);
        } catch (SQLException e) {
            throw DriveExceptionCodes.DB_ERROR.create(e, e.getMessage());
        } finally {
            databaseService.backWritable(contextID, connection);
        }
    }

    @Override
    public boolean removeDirectoryChecksum(FolderID folderID) throws OXException {
        Connection connection = databaseService.getWritable(contextID);
        try {
            return 0 < deleteDirectoryChecksum(connection, contextID, folderID);
        } catch (SQLException e) {
            throw DriveExceptionCodes.DB_ERROR.create(e, e.getMessage());
        } finally {
            databaseService.backWritable(contextID, connection);
        }
    }

    @Override
    public DirectoryChecksum getDirectoryChecksum(FolderID folderID) throws OXException {
        List<DirectoryChecksum> directoryChecksums = getDirectoryChecksums(Arrays.asList(new FolderID[] { folderID }));
        return 1 == directoryChecksums.size() ? directoryChecksums.get(0) : null;
    }

    @Override
    public List<DirectoryChecksum> getDirectoryChecksums(List<FolderID> folderIDs) throws OXException {
        Connection connection = databaseService.getReadOnly(contextID);
        try {
            return selectDirectoryChecksums(connection, contextID, folderIDs.toArray(new FolderID[folderIDs.size()]));
        } catch (SQLException e) {
            throw DriveExceptionCodes.DB_ERROR.create(e, e.getMessage());
        } finally {
            databaseService.backReadOnly(contextID, connection);
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
            stmt = connection.prepareStatement(SQL.DELETE_FILE_CHECKSUMS_STMT(uuids));
            stmt.setInt(1, cid);
            return SQL.logExecuteUpdate(stmt);
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
    }

    private static int deleteFileChecksumsInFolder(Connection connection, int cid, FolderID folder) throws SQLException, OXException {
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(SQL.DELETE_FILE_CHECKSUMS_IN_FOLDER_STMT);
            stmt.setInt(1, cid);
            stmt.setString(2, escapeFolder(folder));
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

    private static List<FileChecksum> selectMatchingFileChecksums(Connection connection, int cid, String checksum) throws SQLException, OXException {
        List<FileChecksum> fileChecksums = new ArrayList<FileChecksum>();
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(SQL.SELECT_MATCHING_FILE_CHECKSUMS_STMT);
            stmt.setInt(1, cid);
            stmt.setString(2, checksum);
            ResultSet resultSet = SQL.logExecuteQuery(stmt);
            while (resultSet.next()) {
                FileChecksum fileChecksum = new FileChecksum();
                fileChecksum.setUuid(resultSet.getString(1));
                FolderID folderID = unescapeFolder(resultSet.getString(2));
                fileChecksum.setFileID(unescapeFile(folderID, resultSet.getString(3)));
                fileChecksum.setVersion(resultSet.getString(4));
                fileChecksum.setSequenceNumber(resultSet.getLong(5));
                fileChecksum.setChecksum(checksum);
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
            stmt = connection.prepareStatement(SQL.SELECT_MATCHING_FILE_CHECKSUMS_STMT(checksums.toArray(new String[checksums.size()])));
            stmt.setInt(1, cid);
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

    private static int insertDirectoryChecksum(Connection connection, int cid, DirectoryChecksum directoryChecksum) throws SQLException, OXException {
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(SQL.INSERT_DIRECTORY_CHECKSUM_STMT);
            stmt.setString(1, directoryChecksum.getUuid());
            stmt.setInt(2, cid);
            stmt.setString(3, escapeFolder(directoryChecksum.getFolderID()));
            stmt.setLong(4, directoryChecksum.getSequenceNumber());
            stmt.setString(5, directoryChecksum.getChecksum());
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
            stmt.setString(3, directoryChecksum.getChecksum());
            stmt.setInt(4, cid);
            stmt.setString(5, directoryChecksum.getUuid());
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
            stmt.setInt(2, cid);
            stmt.setString(3, escapeFolder(folder));
            return SQL.logExecuteUpdate(stmt);
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
    }

    private static int deleteDirectoryChecksum(Connection connection, int cid, FolderID folder) throws SQLException, OXException {
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(SQL.DELETE_DIRECTORY_CHECKSUM_STMT);
            stmt.setInt(1, cid);
            stmt.setString(2, escapeFolder(folder));
            return SQL.logExecuteUpdate(stmt);
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
    }

    private static List<DirectoryChecksum> selectDirectoryChecksums(Connection connection, int cid, FolderID[] folderIDs) throws SQLException, OXException {
        List<DirectoryChecksum> directoryChecksums = new ArrayList<DirectoryChecksum>();
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(SQL.SELECT_DIRECTORY_CHECKSUMS_STMT(folderIDs));
            stmt.setInt(1, cid);
            ResultSet resultSet = SQL.logExecuteQuery(stmt);
            while (resultSet.next()) {
                DirectoryChecksum directoryChecksum = new DirectoryChecksum();
                directoryChecksum.setUuid(resultSet.getString(1));
                directoryChecksum.setFolderID(unescapeFolder(resultSet.getString(2)));
                directoryChecksum.setSequenceNumber(resultSet.getLong(3));
                directoryChecksum.setChecksum(resultSet.getString(4));
                directoryChecksums.add(directoryChecksum);
            }
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
        return directoryChecksums;
    }

}

