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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.database.internal;

import static com.openexchange.database.internal.DBUtils.closeSQLStuff;
import static com.openexchange.java.Autoboxing.I;
import gnu.trove.list.TIntList;
import gnu.trove.list.linked.TIntLinkedList;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.database.ConfigDatabaseService;
import com.openexchange.database.DBPoolingExceptionCodes;
import com.openexchange.exception.OXException;

/**
 * ConfigDBStorage
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ConfigDBStorage {

    private static final Logger LOG = LoggerFactory.getLogger(ConfigDBStorage.class);

    private final ConfigDatabaseService configDatabaseService;

    /**
     * Default constructor-
     */
    public ConfigDBStorage(final ConfigDatabaseService configDatabaseService) {
        super();
        this.configDatabaseService = configDatabaseService;
    }

    private static final String SQL_SELECT_CONTEXTS = "SELECT cid FROM context_server2db_pool WHERE server_id=? AND write_db_pool_id=? AND db_schema=?";

    /**
     * Determines all context IDs which reside in given schema.
     * @param con a connection to the config database. It must be to the write host and in a transaction if the parameter lock is <code>true</code>.
     * @param schema the database schema
     * @param writePoolId corresponding write pool ID (master database)
     * @param lock <code>true</code> and a connection to the write host and in a transaction will create row locks on the read lines.
     * @return an array of <code>int</code> representing all retrieved context identifier
     * @throws OXException if there is no connection to the config database slave is available or reading from the database fails.
     */
    public static final int[] getContextsFromSchema(Connection con, int writePoolId, String schema, boolean lock) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            if (lock && con.getAutoCommit()) {
                throw new SQLException("The row lock can only be obtained if the connection is in a transaction.");
            }
            String sql = SQL_SELECT_CONTEXTS;
            if (lock) {
                sql += " FOR UPDATE";
            }
            stmt = con.prepareStatement(sql);
            stmt.setInt(1, Server.getServerId());
            stmt.setInt(2, writePoolId);
            stmt.setString(3, schema);
            rs = stmt.executeQuery();
            final TIntList tmp = new TIntLinkedList();
            while (rs.next()) {
                tmp.add(rs.getInt(1));
            }
            return tmp.toArray();
        } catch (SQLException e) {
            throw DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(rs, stmt);
        }
    }

    public static String[] getUnfilledSchemas(Connection con, int poolId, int maxContexts, boolean lock) throws OXException {
        PreparedStatement stmt = null;
        ResultSet result = null;
        List<String> retval = new LinkedList<String>();
        try {
            if (lock && con.getAutoCommit()) {
                throw new SQLException("The row lock can only be obtained if the connection is in a transaction.");
            }
            String sql = "SELECT db_schema,COUNT(db_schema) AS count FROM context_server2db_pool WHERE write_db_pool_id=? GROUP BY db_schema HAVING count<? ORDER BY count ASC";
            if (lock) {
                sql += " FOR UPDATE";
            }
            stmt = con.prepareStatement(sql);
            stmt.setInt(1, poolId);
            stmt.setInt(2, maxContexts);
            result = stmt.executeQuery();
            while (result.next()) {
                String schema = result.getString(1);
                int count = result.getInt(2);
                LOG.debug("schema {} is filled with {} contexts.", schema, I(count));
                retval.add(schema);
            }
        } catch (final SQLException e) {
            throw DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
        }
        return retval.toArray(new String[retval.size()]);
    }

    /**
     * Determines all context IDs which reside in given schema
     *
     * @param schema -
     *            the schema
     * @param writePoolId -
     *            corresponding write pool ID (master database)
     * @return an array of <code>int</code> representing all retrieved context
     *         IDs
     * @throws OXException
     */
    public final int[] getContextsFromSchema(final String schema, final int writePoolId) throws OXException {
        final Connection con = configDatabaseService.getReadOnly();
        try {
            return getContextsFromSchema(con, writePoolId, schema, false);
        } finally {
            configDatabaseService.backReadOnly(con);
        }
    }
}
