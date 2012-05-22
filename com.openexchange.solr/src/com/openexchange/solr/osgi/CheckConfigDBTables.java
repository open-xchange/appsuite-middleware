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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.solr.osgi;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.solr.SolrExceptionCodes;
import com.openexchange.tools.sql.DBUtils;


/**
 * {@link CheckConfigDBTables}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CheckConfigDBTables {

    private final DatabaseService databaseService;

    /**
     * Initializes a new {@link CheckConfigDBTables}.
     */
    public CheckConfigDBTables(final DatabaseService databaseService) {
        super();
        this.databaseService = databaseService;
    }

    /**
     * Checks needed OAuth provider tables in configDB.
     * 
     * @throws OXException If check fails
     */
    public void checkTables() throws OXException {
        final Connection con = databaseService.getWritable();
        try {
            DBUtils.startTransaction(con);
            checkNonce(con);
            con.commit();
        } catch (final SQLException e) {
            DBUtils.rollback(con);
            throw SolrExceptionCodes.ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            DBUtils.rollback(con);
            throw SolrExceptionCodes.ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.autocommit(con);
            databaseService.backWritable(con);
        }
    }

    private void checkNonce(final Connection con) throws SQLException {
        final String createSql = "CREATE TABLE `solrCoreStores` (\n" + 
        		" `id` INT4 UNSIGNED NOT NULL," + 
        		" `uri` VARCHAR(255) COLLATE utf8_unicode_ci NOT NULL," + 
        		" `maxCores` INT4 UNSIGNED NOT NULL," + 
        		" `numCores` INT4 UNSIGNED NOT NULL," + 
        		" PRIMARY KEY (`id`)" + 
        		") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci";
        createIfAbsent("solrCoreStores", createSql, con);
    }
  
    private void createIfAbsent(final String name, final String createSql, final Connection con) throws SQLException {
        if (!tableExists(con, name)) {
            Statement stmt = null;
            try {
                stmt = con.createStatement();
                stmt.execute(createSql);
            } finally {
                DBUtils.closeSQLStuff(stmt);
            }
        }
    }

    private static final boolean tableExists(final Connection con, final String table) throws SQLException {
        final DatabaseMetaData metaData = con.getMetaData();
        ResultSet rs = null;
        boolean retval = false;
        try {
            rs = metaData.getTables(null, null, table, new String[] { "TABLE" });
            retval = (rs.next() && rs.getString("TABLE_NAME").equalsIgnoreCase(table));
        } finally {
            DBUtils.closeSQLStuff(rs);
        }
        return retval;
    }

}
