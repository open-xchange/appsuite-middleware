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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import gnu.trove.list.TIntList;
import gnu.trove.list.linked.TIntLinkedList;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.openexchange.database.ConfigDatabaseService;
import com.openexchange.database.DBPoolingExceptionCodes;
import com.openexchange.exception.OXException;

/**
 * ConfigDBStorage
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public class ConfigDBStorage {

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
        try {
            Connection con = null;
            PreparedStatement stmt = null;
            ResultSet rs = null;
            try {
                /*
                 * Get write pool
                 */
                con = configDatabaseService.getReadOnly();
                stmt = con.prepareStatement(SQL_SELECT_CONTEXTS);
                stmt.setInt(1, Server.getServerId());
                stmt.setInt(2, writePoolId);
                stmt.setString(3, schema);
                rs = stmt.executeQuery();
                final TIntList tmp = new TIntLinkedList();
                while (rs.next()) {
                    tmp.add(rs.getInt(1));
                }
                return tmp.toArray();
            } finally {
                closeSQLStuff(rs, stmt);
                if (con != null) {
                    configDatabaseService.backReadOnly(con);
                }
            }
        } catch (final SQLException e) {
            throw DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        }
    }
}
