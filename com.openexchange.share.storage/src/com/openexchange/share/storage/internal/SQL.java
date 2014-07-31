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

package com.openexchange.share.storage.internal;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.openexchange.groupware.update.UpdateTaskV2;

/**
 * {@link SQL}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class SQL {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SQL.class);

    public static String getCreateShareTableStmt() {
        return "CREATE TABLE share (" +
            "token binary(16) NOT NULL," +
            "cid int(10) unsigned NOT NULL," +
            "module tinyint(3) unsigned NOT NULL," +
            "folder varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL," +
            "item varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL," +
            "created bigint(64) NOT NULL," +
            "createdBy int(10) unsigned NOT NULL," +
            "lastModified bigint(64) NOT NULL," +
            "modifiedBy int(10) unsigned NOT NULL," +
            "expires bigint(64) DEFAULT NULL," +
            "guest int(10) unsigned NOT NULL," +
            "auth tinyint(3) unsigned NOT NULL," +
            "PRIMARY KEY (cid,token)," +
            "KEY createdByIndex (cid,createdBy)," +
            "KEY guestIndex (cid,guest)," +
            "KEY folderIndex (cid,folder)" +
        ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";
    }

    /**
     * Gets all known update tasks.
     *
     * @return The update tasks
     */
    public static UpdateTaskV2[] getUpdateTasks() {
        return new UpdateTaskV2[] { new ShareCreateTableTask() };
    };

    public static final String SELECT_SHARE_STMT =
        "SELECT module,folder,item,created,createdBy,lastModified,modifiedBy,expires,guest,auth " +
        "FROM share " +
        "WHERE cid=? AND token=?;"
    ;

    public static final String SELECT_SHARES_CREATED_BY_STMT =
        "SELECT token,module,folder,item,created,lastModified,modifiedBy,expires,guest,auth " +
        "FROM share " +
        "WHERE cid=? AND createdBy=?;"
    ;

    public static final String INSERT_SHARE_STMT =
        "INSERT INTO share (token,cid,module,folder,item,created,createdBy,lastModified,modifiedBy,expires,guest,auth) " +
        "VALUES (?,?,?,?,?,?,?,?,?,?,?,?);"
    ;

    public static final String UPDATE_SHARE_STMT =
        "UPDATE share " +
        "SET module=?,folder=?,item=?,created=?,createdBy=?,lastModified=?,modifiedBy=?,expires=?,guest=?,auth=?) " +
        "WHERE cid=? AND token=?;"
    ;

    public static final String DELETE_SHARE_STMT =
        "DELETE FROM share " +
        "WHERE cid=? AND token=?;"
    ;

    public static final String SELECT_EXPIRED_SHARES_STMT =
        "SELECT token,cid,module,folder,item,created,createdBy,lastModified,modifiedBy,expires,guest,auth " +
        "FROM share " +
        "WHERE expires IS NOT NULL AND expires > ?";
    ;


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

    private SQL() {
        super();
    }

}

