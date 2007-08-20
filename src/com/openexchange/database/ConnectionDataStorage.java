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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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



package com.openexchange.database;

import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import com.openexchange.server.DBPoolingException;
import com.openexchange.server.DBPoolingException.Code;

/**
 * Reads a database connection from the config DB.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class ConnectionDataStorage {

    private static final String SELECT = "SELECT "
        + "url,driver,login,password,hardlimit,max,initial "
        + "FROM db_pool "
        + "WHERE db_pool_id=?";

    /**
     * Prevent instantiation
     */
    private ConnectionDataStorage() {
        super();
    }

    static ConnectionData loadPoolData(final int poolId)
        throws DBPoolingException {
        ConnectionData retval = null;
        final Connection con = Database.get(false);
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement(SELECT);
            stmt.setInt(1, poolId);
            result = stmt.executeQuery();
            if (result.next()) {
                retval = new ConnectionData();
                retval.props = new Properties();
                int pos = 1;
                retval.url = result.getString(pos++);
                retval.driverClass = result.getString(pos++);
                retval.props.put("user", result.getString(pos++));
                retval.props.put("password", result.getString(pos++));
                retval.block = result.getBoolean(pos++);
                retval.max = result.getInt(pos++);
                retval.min = result.getInt(pos++);
            } else {
                throw new DBPoolingException(Code.NO_DBPOOL, Integer.valueOf(poolId));
            }
        } catch (SQLException e) {
            throw new DBPoolingException(Code.SQL_ERROR, e, e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
            Database.back(false, con);
        }
        // TODO parse url parameters to properties
        return retval;
    }

    static class ConnectionData {
        ConnectionData() {
            super();
        }
        String url;
        String driverClass;
        Properties props;
        boolean block;
        int max;
        int min;
    }
}
