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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.openexchange.drive.DriveExceptionCodes;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.composition.FileID;
import com.openexchange.file.storage.composition.FolderID;
import com.openexchange.groupware.update.UpdateTaskV2;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link SQL}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class SQL {

    public static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SQL.class);

    public static String getCreateFileChecksumsTableStmt() {
        return "CREATE TABLE fileChecksums (" +
            "uuid BINARY(16) NOT NULL," +
            "cid INT4 UNSIGNED NOT NULL," +
            "folder VARCHAR(512) NOT NULL," +
            "file VARCHAR(255) NOT NULL," +
            "version VARCHAR(255)," +
            "sequence BIGINT(20) NOT NULL," +
            "checksum BINARY(16) NOT NULL," +
            "PRIMARY KEY (cid, uuid)," +
            "INDEX (cid, folder)," +
            "INDEX (cid, checksum)" +
        ") ENGINE=InnoDB DEFAULT CHARSET=ascii;";
    }

    public static String getCreateDirectoryChecksumsTableStmt() {
        return "CREATE TABLE directoryChecksums (" +
            "uuid BINARY(16) NOT NULL," +
            "cid INT4 UNSIGNED NOT NULL," +
            "user INT4 UNSIGNED DEFAULT NULL," +
            "view INT NOT NULL DEFAULT 0," +
            "folder VARCHAR(512) NOT NULL," +
            "sequence BIGINT(20) DEFAULT NULL," +
            "etag VARCHAR(255) DEFAULT NULL," +
            "checksum BINARY(16) NOT NULL," +
            "used BIGINT(20) NOT NULL DEFAULT 0," +
            "PRIMARY KEY (cid, uuid)," +
            "INDEX (cid, user, view, folder)," +
            "INDEX (cid, checksum)" +
        ") ENGINE=InnoDB DEFAULT CHARSET=ascii;";
    }

    /**
     * Gets all known update tasks.
     *
     * @return The update tasks
     */
    public static UpdateTaskV2[] getUpdateTasks() {
        return new UpdateTaskV2[] {
            new DriveCreateTableTask(),
            new DirectoryChecksumsAddUserAndETagColumnTask(),
            new DirectoryChecksumsReIndexTask(),
            new FileChecksumsReIndexTask(),
            new DirectoryChecksumsAddViewColumnTask(),
            new DirectoryChecksumsAddUsedColumnTask(),
            new DirectoryChecksumsReIndexTaskV2()
        };
    };

    public static final String INSERT_FILE_CHECKSUM_STMT =
        "INSERT INTO fileChecksums (uuid,cid,folder,file,version,sequence,checksum) " +
        "VALUES (UNHEX(?),?,REVERSE(?),REVERSE(?),?,?,UNHEX(?));";

    public static final String UPDATE_FILE_CHECKSUM_STMT =
        "UPDATE fileChecksums SET folder=REVERSE(?),file=REVERSE(?),version=?,sequence=?,checksum=UNHEX(?) " +
        "WHERE uuid=UNHEX(?) AND cid=?;";

    public static final String UPDATE_FILE_CHECKSUM_FOLDERS_STMT =
        "UPDATE fileChecksums SET folder=REVERSE(?) " +
        "WHERE cid=? AND folder=REVERSE(?);";

    /** DELETE FROM checksums WHERE cid=? AND uuid IN (?,?,...);" */
    public static final String DELETE_FILE_CHECKSUMS_STMT(int length) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("DELETE FROM fileChecksums WHERE cid=? AND uuid");
        return appendPlaceholders(stringBuilder, length).append(';').toString();
    }

    public static final String INSERT_FILE_CHECKSUMS_STMT(int count) {
        StringBuilder allocator = new StringBuilder();
        allocator.append("INSERT INTO fileChecksums (uuid,cid,folder,file,version,sequence,checksum) ");
        if (0 < count) {
            allocator.append("VALUES (UNHEX(?),?,REVERSE(?),REVERSE(?),?,?,UNHEX(?))");
        }
        for (int i = 1; i < count; i++) {
            allocator.append(",(UNHEX(?),?,REVERSE(?),REVERSE(?),?,?,UNHEX(?))");
        }
        allocator.append(';');
        return allocator.toString();
    }

    public static final String DELETE_FILE_CHECKSUM_STMT =
        "DELETE FROM fileChecksums " +
        "WHERE cid=? AND folder=REVERSE(?) AND file=REVERSE(?) AND version=? AND sequence=?;";

    public static final String DELETE_FILE_CHECKSUMS_STMT =
        "DELETE FROM fileChecksums " +
        "WHERE cid=? AND folder=REVERSE(?) AND file=REVERSE(?);";

    public static final String SELECT_FILE_CHECKSUM_STMT =
        "SELECT LOWER(HEX(uuid)),LOWER(HEX(checksum)) FROM fileChecksums " +
        "WHERE cid=? AND folder=REVERSE(?) AND file=REVERSE(?) AND version=? AND sequence=?;";

    public static final String SELECT_FILE_CHECKSUMS_IN_FOLDER_STMT =
        "SELECT LOWER(HEX(uuid)),REVERSE(file),version,sequence,LOWER(HEX(checksum)) FROM fileChecksums " +
        "WHERE cid=? AND folder=REVERSE(?);";

    /** INSERT INTO directoryChecksums (uuid,cid,user,view,folder,sequence,etag,checksum,used) VALUES (UNHEX(?),?,?,?,REVERSE(?),?,?,UNHEX(?),?), ...; */
    public static final String INSERT_DIRECTORY_CHECKSUMS_STMT(int count) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("INSERT INTO directoryChecksums (uuid,cid,user,view,folder,sequence,etag,checksum,used) ");
        if (0 < count) {
            stringBuilder.append("VALUES (UNHEX(?),?,?,?,REVERSE(?),?,?,UNHEX(?),?)");
        }
        for (int i = 1; i < count; i++) {
            stringBuilder.append(",(UNHEX(?),?,?,?,REVERSE(?),?,?,UNHEX(?),?)");
        }
        stringBuilder.append(';');
        return stringBuilder.toString();
    }

    public static final String UPDATE_DIRECTORY_CHECKSUM_STMT =
        "UPDATE directoryChecksums SET folder=REVERSE(?),sequence=?,etag=?,checksum=UNHEX(?),used=? " +
        "WHERE cid=? AND uuid=UNHEX(?);";

    /** UPDATE directoryChecksums SET used=? WHERE cid=? AND uuid IN (?,?,...);" */
    public static final String TOUCH_DIRECTORY_CHECKSUMS_STMT(int length) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("UPDATE directoryChecksums SET used=? WHERE cid=? AND uuid");
        return appendPlaceholders(stringBuilder, length).append(';').toString();
    }

    public static final String UPDATE_DIRECTORY_CHECKSUM_FOLDER_STMT =
        "UPDATE directoryChecksums SET folder=REVERSE(?),used=? " +
        "WHERE cid=? AND folder=REVERSE(?);";

    /**
     * DELETE FROM fileChecksums
     * WHERE cid=? AND folder IN (...);"
     */
    public static final String DELETE_FILE_CHECKSUMS_IN_FOLDER_STMT(int length) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("DELETE FROM fileChecksums WHERE cid=? AND folder");
        return appendPlaceholders(stringBuilder, length).append(';').toString();
    }

    /**
     * DELETE FROM directoryChecksums
     * WHERE cid=? AND folder IN (?,?,...);"
     */
    public static final String DELETE_DIRECTORY_CHECKSUMS_FOR_FOLDER_STMT(int length) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("DELETE FROM directoryChecksums WHERE cid=? AND folder");
        return appendPlaceholders(stringBuilder, length).append(';').toString();
    }

    /**
     * SELECT LOWER(HEX(uuid)),REVERSE(folder),sequence,etag,LOWER(HEX(checksum)) FROM directoryChecksums
     * WHERE cid=? AND user=? AND view=? AND folder IN (?,?,...);"
     */
    public static final String SELECT_DIRECTORY_CHECKSUMS_STMT(int length) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("SELECT LOWER(HEX(uuid)),REVERSE(folder),sequence,etag,LOWER(HEX(checksum)) FROM directoryChecksums ");
        stringBuilder.append("WHERE cid=? AND user=? AND view=? AND folder");
        return appendPlaceholders(stringBuilder, length).append(';').toString();
    }

    /**
     * SELECT LOWER(HEX(uuid)),REVERSE(folder),sequence,etag,user,view,LOWER(HEX(checksum)) FROM directoryChecksums
     * WHERE cid=? AND folder IN (?,?,...);"
     */
    public static final String SELECT_ALL_DIRECTORY_CHECKSUMS_STMT(int length) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("SELECT LOWER(HEX(uuid)),REVERSE(folder),sequence,etag,user,view,LOWER(HEX(checksum)) FROM directoryChecksums ");
        stringBuilder.append("WHERE cid=? AND folder");
        return appendPlaceholders(stringBuilder, length).append(';').toString();
    }

    public static final String SELECT_UNUSED_DIRECTORY_CHECKSUMS_STMT =
        "SELECT LOWER(HEX(uuid)),REVERSE(folder),sequence,etag,user,view,LOWER(HEX(checksum)) " +
        "FROM directoryChecksums WHERE cid=? AND used<?;"
    ;

    /**
     * SELECT LOWER(HEX(uuid)),REVERSE(folder),REVERSE(file),version,sequence,LOWER(HEX(checksum)) FROM fileChecksums
     * WHERE cid=? AND checksum IN (?,?,...);"
     */
    public static final String SELECT_MATCHING_FILE_CHECKSUMS_STMT(int length) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("SELECT LOWER(HEX(uuid)),REVERSE(folder),REVERSE(file),version,sequence,LOWER(HEX(checksum)) ");
        stringBuilder.append("FROM fileChecksums WHERE cid=? AND checksum");
        return appendPlaceholders(stringBuilder, length).append(';').toString();
    }

    /** DELETE FROM directoryChecksums WHERE cid=? AND uuid IN (?,?,...);" */
    public static final String DELETE_DIRECTORY_CHECKSUMS_STMT(int length) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("DELETE FROM directoryChecksums WHERE cid=? AND uuid");
        return appendPlaceholders(stringBuilder, length).append(';').toString();
    }

    public static ResultSet logExecuteQuery(PreparedStatement stmt) throws SQLException {
        if (false == LOG.isDebugEnabled()) {
            return stmt.executeQuery();
        } else {
            long start = System.currentTimeMillis();
            ResultSet resultSet = stmt.executeQuery();
            LOG.debug("executeQuery: {} - {} ms elapsed.", stmt.toString(), (System.currentTimeMillis() - start));
            return resultSet;
        }
    }

    public static int logExecuteUpdate(PreparedStatement stmt) throws SQLException {
        if (false == LOG.isDebugEnabled()) {
            return stmt.executeUpdate();
        } else {
            long start = System.currentTimeMillis();
            int rowCount = stmt.executeUpdate();
            LOG.debug("executeUpdate: {} - {} rows affected, {} ms elapsed.", stmt.toString(), rowCount, (System.currentTimeMillis() - start));
            return rowCount;
        }
    }

    public static OXException wrap(SQLException e) {
        if (DBUtils.isTransactionRollbackException(e)) {
            return DriveExceptionCodes.DB_ERROR_RETRY.create(e, e.getMessage());
        }
        return DriveExceptionCodes.DB_ERROR.create(e, e.getMessage());
    }

    public static FolderID unescapeFolder(String escaped) throws OXException {
        return new FolderID(unescape(escaped));
    }

    public static FileID unescapeFile(FolderID folderID, String escaped) throws OXException {
        return new FileID(folderID.getService(), folderID.getAccountId(), folderID.getFolderId(), unescape(escaped));
    }

    public static String escapeFolder(FolderID folderID) throws OXException {
        return escape(folderID.toUniqueID());
    }

    public static String escapeFolder(FileID fileID) throws OXException {
        return escapeFolder(new FolderID(fileID.getService(), fileID.getAccountId(), fileID.getFolderId()));
    }

    public static String escapeFile(FileID fileID) throws OXException {
        return escape(fileID.getFileId());
    }

    public static String escape(String value) throws OXException {
        if (null == value) {
            return null;
        }
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw DriveExceptionCodes.DB_ERROR.create(e, e.getMessage());
        }
    }

    public static String unescape(String value) throws OXException {
        try {
            return URLDecoder.decode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw DriveExceptionCodes.DB_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Converts the supplied checksum from it's hex string representation into a byte array.
     *
     * @param checksum The checksum
     * @return The byte array
     */
    public static byte[] getBytes(String checksum) {
        int length = checksum.length();
        byte[] bytes = new byte[length / 2];
        for (int i = 0; i < length; i += 2) {
            bytes[i / 2] = (byte) ((Character.digit(checksum.charAt(i), 16) << 4) + Character.digit(checksum.charAt(i + 1), 16));
        }
        return bytes;
    }

    /**
     * Appends a SQL clause for the given number of placeholders, i.e. either <code>=?</code> if <code>count</code> is <code>1</code>, or
     * an <code>IN</code> clause like <code>IN (?,?,?,?)</code> in case <code>count</code> is greater than <code>1</code>.
     *
     * @param stringBuilder The string builder to append the clause
     * @param count The number of placeholders to append
     * @return The string builder
     */
    private static StringBuilder appendPlaceholders(StringBuilder stringBuilder, int count) {
        if (0 >= count) {
            throw new IllegalArgumentException("count");
        }
        if (1 == count) {
            stringBuilder.append("=?");
        } else {
            stringBuilder.append(" IN (?");
            for (int i = 1; i < count; i++) {
                stringBuilder.append(",?");
            }
            stringBuilder.append(')');
        }
        return stringBuilder;
    }

    private SQL() {
        super();
    }

}

