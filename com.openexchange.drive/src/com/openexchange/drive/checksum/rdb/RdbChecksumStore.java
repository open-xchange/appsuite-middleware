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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import com.openexchange.database.DatabaseService;
import com.openexchange.drive.DriveExceptionCodes;
import com.openexchange.drive.checksum.ChecksumStore;
import com.openexchange.drive.internal.DriveServiceLookup;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.File;
import com.openexchange.java.util.UUIDs;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link RdbChecksumStore}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class RdbChecksumStore implements ChecksumStore {

    private final String serviceID;
    private final String accountID;
    private final DatabaseService databaseService;
    private final ServerSession session;

    /**
     * Initializes a new {@link RdbChecksumStore}.
     *
     * @param session The session
     * @param serviceID The service ID
     * @param accountID The account ID
     * @throws OXException
     */
    public RdbChecksumStore(ServerSession session, String serviceID, String accountID) throws OXException {
        super();
        this.serviceID = serviceID;
        this.accountID = accountID;
        this.session = session;
        this.databaseService = DriveServiceLookup.getService(DatabaseService.class, true);
    }

    @Override
    public String getChecksum(File file) throws OXException {
        Connection connection = databaseService.getReadOnly(session.getContextId());
        try {
            return selectChecksum(connection, session.getContextId(), serviceID, accountID,
                file.getFolderId(), file.getId(), file.getVersion(), file.getSequenceNumber());
        } catch (SQLException e) {
            throw DriveExceptionCodes.DB_ERROR.create(e, e.getMessage());
        } finally {
            databaseService.backReadOnly(session.getContextId(), connection);
        }
    }

    @Override
    public Collection<File> getFiles(String checksum) throws OXException {
        Connection connection = databaseService.getReadOnly(session.getContextId());
        try {
            return selectFiles(connection, session.getContextId(), serviceID, accountID, checksum);
        } catch (SQLException e) {
            throw DriveExceptionCodes.DB_ERROR.create(e, e.getMessage());
        } finally {
            databaseService.backReadOnly(session.getContextId(), connection);
        }
    }

    @Override
    public Map<File, String> getFilesInFolder(String folderID) throws OXException {
        Connection connection = databaseService.getReadOnly(session.getContextId());
        try {
            return selectFilesInFolder(connection, session.getContextId(), serviceID, accountID, folderID);
        } catch (SQLException e) {
            throw DriveExceptionCodes.DB_ERROR.create(e, e.getMessage());
        } finally {
            databaseService.backReadOnly(session.getContextId(), connection);
        }
    }

    @Override
    public void addChecksum(File file, String checksum) throws OXException {
        Connection connection = databaseService.getWritable(session.getContextId());
        try {
            insertChecksum(connection, session.getContextId(), serviceID, accountID,
                file.getFolderId(), file.getId(), file.getVersion(), file.getLastModified().getTime(), checksum);
        } catch (SQLException e) {
            throw DriveExceptionCodes.DB_ERROR.create(e, e.getMessage());
        } finally {
            databaseService.backReadOnly(session.getContextId(), connection);
        }
    }

    @Override
    public void removeChecksums(File file) throws OXException {
        Connection connection = databaseService.getWritable(session.getContextId());
        try {
            deleteChecksums(connection, session.getContextId(), serviceID, accountID, file.getFolderId(), file.getId());
        } catch (SQLException e) {
            throw DriveExceptionCodes.DB_ERROR.create(e, e.getMessage());
        } finally {
            databaseService.backReadOnly(session.getContextId(), connection);
        }
    }

    @Override
    public void updateFolderIDs(String currentFolderID, String newFolderID) throws OXException {
        Connection connection = databaseService.getWritable(session.getContextId());
        try {
            updateFolderIDs(connection, session.getContextId(), serviceID, accountID, currentFolderID, newFolderID);
        } catch (SQLException e) {
            throw DriveExceptionCodes.DB_ERROR.create(e, e.getMessage());
        } finally {
            databaseService.backReadOnly(session.getContextId(), connection);
        }
    }

    private static String selectChecksum(Connection connection, int cid, String service, String account, String folder, String file, String version, long sequence) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        try {
            stmt = connection.prepareStatement(SQL.SELECT_CHECKSUM_STMT);
            stmt.setInt(1, cid);
            stmt.setString(2, service);
            stmt.setString(3, account);
            stmt.setString(4, folder);
            stmt.setString(5, file);
            stmt.setString(6, version);
            stmt.setLong(7, sequence);
            resultSet = SQL.logExecuteQuery(stmt);
            return resultSet.next() ? resultSet.getString(1) : null;
        } finally {
            DBUtils.closeSQLStuff(resultSet, stmt);
        }
    }

    private static List<File> selectFiles(Connection connection, int cid, String service, String account, String checksum) throws SQLException {
        List<File> files = new ArrayList<File>();
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        try {
            stmt = connection.prepareStatement(SQL.SELECT_FILES_STMT);
            stmt.setInt(1, cid);
            stmt.setString(2, service);
            stmt.setString(3, account);
            stmt.setString(4, checksum);
            resultSet = SQL.logExecuteQuery(stmt);
            while (resultSet.next()) {
                File file = new DefaultFile();
                file.setFolderId(resultSet.getString(1));
                file.setId(resultSet.getString(2));
                file.setVersion(resultSet.getString(3));
                file.setLastModified(new Date(resultSet.getLong(4)));
//                    file.setFileMD5Sum(checksum);
                files.add(file);
            }
        } finally {
            DBUtils.closeSQLStuff(resultSet, stmt);
        }
        return files;
    }

    private static Map<File, String> selectFilesInFolder(Connection connection, int cid, String service, String account, String folderID) throws SQLException {
        Map<File, String> files = new HashMap<File, String>();
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        try {
            stmt = connection.prepareStatement(SQL.SELECT_FILES_IN_FOLDER_STMT);
            stmt.setInt(1, cid);
            stmt.setString(2, service);
            stmt.setString(3, account);
            stmt.setString(4, folderID);
            resultSet = SQL.logExecuteQuery(stmt);
            while (resultSet.next()) {
                File file = new DefaultFile();
                file.setFolderId(folderID);
                file.setId(resultSet.getString(1));
                file.setVersion(resultSet.getString(2));
                file.setLastModified(new Date(resultSet.getLong(3)));
                String checksum = resultSet.getString(4);
                files.put(file, checksum);
            }
        } finally {
            DBUtils.closeSQLStuff(resultSet, stmt);
        }
        return files;
    }

    private static int insertChecksum(Connection connection, int cid, String service, String account, String folder, String file, String version, long sequence, String checksum) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(SQL.INSERT_CHECKSUM_STMT);
            stmt.setString(1, UUIDs.getUnformattedString(UUID.randomUUID()));
            stmt.setInt(2, cid);
            stmt.setString(3, service);
            stmt.setString(4, account);
            stmt.setString(5, folder);
            stmt.setString(6, file);
            stmt.setString(7, version);
            stmt.setLong(8, sequence);
            stmt.setString(9, checksum);
            return SQL.logExecuteUpdate(stmt);
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
    }

    private static int deleteChecksums(Connection connection, int cid, String service, String account, String folder, String file) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(SQL.DELETE_CHECKSUMS_STMT);
            stmt.setInt(1, cid);
            stmt.setString(2, service);
            stmt.setString(3, account);
            stmt.setString(4, folder);
            stmt.setString(5, file);
            return SQL.logExecuteUpdate(stmt);
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
    }

    private static int updateFolderIDs(Connection connection, int cid, String service, String account, String currentFolder, String newFolder) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(SQL.UPDATE_FOLDERS_STMT);
            stmt.setString(1, newFolder);
            stmt.setInt(2, cid);
            stmt.setString(3, service);
            stmt.setString(4, account);
            stmt.setString(5, currentFolder);
            return SQL.logExecuteUpdate(stmt);
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
    }

}

