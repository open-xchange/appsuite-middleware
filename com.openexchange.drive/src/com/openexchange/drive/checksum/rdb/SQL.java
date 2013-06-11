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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.commons.logging.Log;
import com.openexchange.java.StringAllocator;
import com.openexchange.log.LogFactory;

/**
 * {@link SQL}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class SQL {

    public static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(SQL.class));

    public static String getCreateFileChecksumsTableStmt() {
        return "CREATE TABLE fileChecksums (" +
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

    public static String getCreateDirectoryChecksumsTableStmt() {
        return "CREATE TABLE directoryChecksums (" +
            "uuid BINARY(16) NOT NULL," +
            "cid INT4 UNSIGNED NOT NULL," +
            "service VARCHAR(255) NOT NULL," +
            "account VARCHAR(255) NOT NULL," +
            "folder VARCHAR(255) NOT NULL," +
            "sequence BIGINT(20) NOT NULL," +
            "checksum BINARY(16) NOT NULL," +
            "PRIMARY KEY  (`uuid`)" +
        ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";
    }

    public static final String INSERT_FILE_CHECKSUM_STMT =
        "INSERT INTO fileChecksums (uuid,cid,service,account,folder,file,version,sequence,checksum) " +
        "VALUES (UNHEX(?),?,?,?,REVERSE(?),REVERSE(?),?,?,UNHEX(?));";

    public static final String UPDATE_FILE_CHECKSUM_STMT =
        "UPDATE fileChecksums SET folder=REVERSE(?),file=REVERSE(?),version=?,sequence=?,checksum=UNHEX(?) " +
        "WHERE uuid=UNHEX(?);";

    public static final String UPDATE_FILE_CHECKSUM_FOLDERS_STMT =
        "UPDATE fileChecksums SET folder=REVERSE(?) " +
        "WHERE cid=? AND service=? AND account=? AND folder=REVERSE(?);";

    /** DELETE FROM checksums WHERE uuid IN (...);" */
    public static final String DELETE_FILE_CHECKSUMS_STMT(String[] uuids) {
        StringAllocator allocator = new StringAllocator();
        allocator.append("DELETE FROM fileChecksums WHERE uuid IN (");
        if (0 < uuids.length) {
            allocator.append("UNHEX('").append(uuids[0]).append("')");
        }
        for (int i = 1; i < uuids.length; i++) {
            allocator.append(",UNHEX('").append(uuids[0]).append("')");
        }
        allocator.append(");");
        return allocator.toString();
    }

    public static final String DELETE_FILE_CHECKSUMS_IN_FOLDER_STMT =
        "DELETE FROM fileChecksums " +
        "WHERE cid=? AND service=? AND account=? AND folder=REVERSE(?);";

    public static final String DELETE_FILE_CHECKSUM_STMT =
        "DELETE FROM fileChecksums " +
        "WHERE cid=? AND service=? AND account=? AND folder=REVERSE(?) AND file=REVERSE(?) AND version=? AND sequence=?;";

    public static final String SELECT_FILE_CHECKSUM_STMT =
        "SELECT LOWER(HEX(uuid)),LOWER(HEX(checksum)) FROM fileChecksums " +
        "WHERE cid=? AND service=? AND account=? AND folder=REVERSE(?) AND file=REVERSE(?) AND version=? AND sequence=?;";

    public static final String SELECT_FILE_CHECKSUMS_IN_FOLDER_STMT =
        "SELECT LOWER(HEX(uuid)),REVERSE(file),version,sequence,LOWER(HEX(checksum)) FROM fileChecksums " +
        "WHERE cid=? AND service=? AND account=? AND folder=REVERSE(?);";

    public static final String SELECT_MATCHING_FILE_CHECKSUMS_STMT =
        "SELECT LOWER(HEX(uuid)),REVERSE(folder),REVERSE(file),version,sequence FROM fileChecksums " +
        "WHERE cid=? AND service=? AND account=? AND checksum=UNHEX(?);";

    public static final String INSERT_DIRECTORY_CHECKSUM_STMT =
        "INSERT INTO directoryChecksums (uuid,cid,service,account,folder,sequence,checksum) " +
        "VALUES (UNHEX(?),?,?,?,REVERSE(?),?,UNHEX(?));";

    public static final String UPDATE_DIRECTORY_CHECKSUM_STMT =
        "UPDATE directoryChecksums SET folder=REVERSE(?),sequence=?,checksum=UNHEX(?) " +
        "WHERE uuid=UNHEX(?);";

    public static final String UPDATE_DIRECTORY_CHECKSUM_FOLDER_STMT =
        "UPDATE directoryChecksums SET folder=REVERSE(?) " +
        "WHERE cid=? AND service=? AND account=? AND folder=REVERSE(?);";

    public static final String DELETE_DIRECTORY_CHECKSUM_STMT =
        "DELETE FROM directoryChecksums " +
        "WHERE cid=? AND service=? AND account=? AND folder=REVERSE(?);";

    /**
     * SELECT LOWER(HEX(uuid)),REVERSE(folder),sequence,checksum FROM directoryChecksums
     * WHERE cid=? AND service=? AND account=? AND REVERSE(folder) IN (...);"
     */
    public static final String SELECT_DIRECTORY_CHECKSUMS_STMT(String[] folderIDs) {
        if (null == folderIDs || 0 == folderIDs.length) {
            throw new IllegalArgumentException("folderIDs");
        }
        StringAllocator allocator = new StringAllocator();
        allocator.append("SELECT LOWER(HEX(uuid)),REVERSE(folder),sequence,LOWER(HEX(checksum)) FROM directoryChecksums ");
        allocator.append("WHERE cid=? AND service=? AND account=? AND REVERSE(folder) IN (");
        if (0 < folderIDs.length) {
            allocator.append(folderIDs[0]);
        }
        for (int i = 1; i < folderIDs.length; i++) {
            allocator.append(',').append(folderIDs[i]);
        }
        allocator.append(");");
        return allocator.toString();
    }

    /**
     * SELECT LOWER(HEX(uuid)),REVERSE(folder),REVERSE(file),version,sequence,LOWER(HEX(checksum)) FROM fileChecksums
     * WHERE cid=? AND service=? AND account=? AND checksum IN (...);"
     */
    public static final String SELECT_MATCHING_FILE_CHECKSUMS_STMT(String[] checksums) {
        if (null == checksums || 0 == checksums.length) {
            throw new IllegalArgumentException("checksums");
        }
        StringAllocator allocator = new StringAllocator();
        allocator.append("SELECT LOWER(HEX(uuid)),REVERSE(folder),REVERSE(file),version,sequence,LOWER(HEX(checksum)) FROM fileChecksums ");
        allocator.append("WHERE cid=? AND service=? AND account=? AND LOWER(HEX(checksum))");
        if (1 == checksums.length) {
            allocator.append("='").append(checksums[0]).append("';");
        } else {
            allocator.append(" IN ('").append(checksums[0]);
            for (int i = 1; i < checksums.length; i++) {
                allocator.append("','").append(checksums[i]);
            }
            allocator.append("');");
        }
        return allocator.toString();
    }

    public static ResultSet logExecuteQuery(PreparedStatement stmt) throws SQLException {
        if (false == LOG.isDebugEnabled()) {
            return stmt.executeQuery();
        } else {
            long start = System.currentTimeMillis();
            ResultSet resultSet = stmt.executeQuery();
            LOG.debug("executeQuery: " + stmt.toString() + " - " + (System.currentTimeMillis() - start) + " ms elapsed.");
            return resultSet;
        }
    }

    public static int logExecuteUpdate(PreparedStatement stmt) throws SQLException {
        if (false == LOG.isDebugEnabled()) {
            return stmt.executeUpdate();
        } else {
            long start = System.currentTimeMillis();
            int rowCount = stmt.executeUpdate();
            LOG.debug("executeUpdate: " + stmt.toString() + " - " + rowCount + " rows affected, " +
                (System.currentTimeMillis() - start) + " ms elapsed.");
            return rowCount;
        }
    }

    private SQL() {
        super();
    }

}

