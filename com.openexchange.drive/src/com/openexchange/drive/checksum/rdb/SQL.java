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
import com.openexchange.log.LogFactory;

/**
 * {@link SQL}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class SQL {

    public static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(SQL.class));

    public static String getCreateTableStmt() {
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

    public static final String SELECT_CHECKSUM_STMT =
        "SELECT LOWER(HEX(checksum)) FROM checksums " +
        "WHERE cid=? AND service=? AND account=? AND folder=REVERSE(?) AND file=REVERSE(?) AND sequence=?;";

    public static final String SELECT_FILES_STMT =
        "SELECT REVERSE(folder),REVERSE(file),version,sequence FROM checksums " +
        "WHERE cid=? AND service=? AND account=? AND checksum=UNHEX(?);";

    public static final String SELECT_FILES_IN_FOLDER_STMT =
        "SELECT REVERSE(file),version,sequence,LOWER(HEX(checksum)) FROM checksums " +
        "WHERE cid=? AND service=? AND account=? AND folder=REVERSE(?);";

    public static final String INSERT_CHECKSUM_STMT =
        "INSERT INTO checksums (uuid,cid,service,account,folder,file,version,sequence,checksum) " +
        "VALUES (UNHEX(?),?,?,?,REVERSE(?),REVERSE(?),?,?,UNHEX(?));";

    public static final String DELETE_CHECKSUMS_STMT =
        "DELETE FROM checksums " +
        "WHERE cid=? AND service=? AND account=? AND folder=REVERSE(?) AND file=REVERSE(?);";

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

