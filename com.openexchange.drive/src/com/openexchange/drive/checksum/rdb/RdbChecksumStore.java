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
import java.util.List;
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

    private static String getCreateTableStmt() {
        return "CREATE TABLE checksums (" +
            "uuid BINARY(16) NOT NULL," +
            "cid INT4 UNSIGNED NOT NULL," +
            "service VARCHAR(255) NOT NULL," +
            "account VARCHAR(255) NOT NULL," +
            "folder VARCHAR(255) NOT NULL," +
            "file VARCHAR(255) NOT NULL," +
            "version VARCHAR(255)," +
            "sequence BIGINT(20) NOT NULL," +
            "checksum BINARY(16) NOT NULL," +
            "PRIMARY KEY  (`uuid`)" +
        ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";
    }

    private static final String SELECT_CHECKSUM_STMT =
        "SELECT LOWER(HEX(checksum)) FROM checksums WHERE cid=? AND service=? AND account=? AND folder=REVERSE(?) AND file=REVERSE(?) AND sequence=?;";

    private static final String SELECT_FILES_STMT =
        "SELECT REVERSE(folder),REVERSE(file),version,sequence FROM checksums WHERE cid=? AND service=? AND account=? AND checksum=UNHEX(?);";

    private static final String INSERT_CHECKSUM_STMT =
        "INSERT INTO checksums (uuid,cid,service,account,folder,file,version,sequence,checksum) VALUES (UNHEX(?),?,?,?,REVERSE(?),REVERSE(?),?,?,UNHEX(?));";

    private static final String DELETE_CHECKSUMS_STMT =
        "DELETE FROM checksums WHERE cid=? AND service=? AND account=? AND folder=REVERSE(?) AND file=REVERSE(?);";

    private final String serviceID;
    private final String accountID;
    private final DatabaseService databaseService;

    /**
     * Initializes a new {@link RdbChecksumStore}.
     * @throws OXException
     */
    public RdbChecksumStore(String serviceID, String accountID) throws OXException {
        super();
        this.serviceID = serviceID;
        this.accountID = accountID;
        this.databaseService = DriveServiceLookup.getService(DatabaseService.class, true);
    }

    @Override
    public String getChecksum(ServerSession session, File file) throws OXException {
        Connection connection = databaseService.getReadOnly(session.getContextId());
        try {
            return selectChecksum(connection, session.getContextId(), serviceID, accountID,
                file.getFolderId(), file.getId(), file.getSequenceNumber());
        } catch (SQLException e) {
            throw DriveExceptionCodes.DB_ERROR.create(e, e.getMessage());
        } finally {
            databaseService.backReadOnly(session.getContextId(), connection);
        }
    }

    @Override
    public Collection<File> getFiles(ServerSession session, String checksum) throws OXException {
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
    public void addChecksum(ServerSession session, File file, String checksum) throws OXException {
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
    public void removeChecksums(ServerSession session, File file) throws OXException {
        Connection connection = databaseService.getWritable(session.getContextId());
        try {
            deleteChecksums(connection, session.getContextId(), serviceID, accountID, file.getFolderId(), file.getId());
        } catch (SQLException e) {
            throw DriveExceptionCodes.DB_ERROR.create(e, e.getMessage());
        } finally {
            databaseService.backReadOnly(session.getContextId(), connection);
        }
    }

    private static String selectChecksum(Connection connection, int cid, String service, String account, String folder, String file, long sequence) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        try {
            stmt = connection.prepareStatement(SELECT_CHECKSUM_STMT);
            stmt.setInt(1, cid);
            stmt.setString(2, service);
            stmt.setString(3, account);
            stmt.setString(4, folder);
            stmt.setString(5, file);
            stmt.setLong(6, sequence);
            resultSet = stmt.executeQuery();
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
            stmt = connection.prepareStatement(SELECT_FILES_STMT);
            stmt.setInt(1, cid);
            stmt.setString(2, service);
            stmt.setString(3, account);
            stmt.setString(4, checksum);
            resultSet = stmt.executeQuery();
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

    private static int insertChecksum(Connection connection, int cid, String service, String account, String folder, String file, String version, long sequence, String checksum) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(INSERT_CHECKSUM_STMT);
            stmt.setString(1, UUIDs.getUnformattedString(UUID.randomUUID()));
            stmt.setInt(2, cid);
            stmt.setString(3, service);
            stmt.setString(4, account);
            stmt.setString(5, folder);
            stmt.setString(6, file);
            stmt.setString(7, version);
            stmt.setLong(8, sequence);
            stmt.setString(9, checksum);
            return stmt.executeUpdate();
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
    }

    private static int deleteChecksums(Connection connection, int cid, String service, String account, String folder, String file) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(DELETE_CHECKSUMS_STMT);
            stmt.setInt(1, cid);
            stmt.setString(2, service);
            stmt.setString(3, account);
            stmt.setString(4, folder);
            stmt.setString(5, file);
            return stmt.executeUpdate();
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
    }

}

