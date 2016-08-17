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

package com.openexchange.groupware.update.tasks;

import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.Attributes;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.TaskAttributes;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.java.util.UUIDs;
import com.openexchange.tools.sql.DBUtils;
import com.openexchange.tools.update.Tools;

/**
 * {@link MigrateAliasUpdateTask}
 *
 * @author <a href="mailto:lars.hoogestraat@open-xchange.com">Lars Hoogestraat</a>
 * @since v7.8.0
 */
public class MigrateAliasUpdateTask extends AbstractUserAliasTableUpdateTask {

    @Override
    public void perform(PerformParameters params) throws OXException {
        int ctxId = params.getContextId();
        Connection conn = Database.getNoTimeout(ctxId, true);
        try {
            conn.setAutoCommit(false);
            if (false == Tools.tableExists(conn, "user_alias")) {
                createTable(conn);
            }

            Set<Alias> aliases = getAllAliasesInUserAttributes(conn);
            if (aliases != null && false == aliases.isEmpty()) {
                insertAliases(conn, aliases);
            }
            conn.commit();
        } catch (SQLException e) {
            DBUtils.rollback(conn);
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } catch (RuntimeException e) {
            DBUtils.rollback(conn);
            throw UpdateExceptionCodes.OTHER_PROBLEM.create(e, e.getMessage());
        } finally {
            DBUtils.autocommit(conn);
            Database.backNoTimeout(ctxId, true, conn);
        }
    }

    private void createTable(Connection conn) throws SQLException {
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            stmt.execute("CREATE TABLE `user_alias` ( " // --> Also specified in com.openexchange.admin.mysql.CreateLdap2SqlTables.createAliasTable
            + "`cid` INT4 UNSIGNED NOT NULL, " 
            + "`user` INT4 UNSIGNED NOT NULL, " 
            + "`alias` VARCHAR(255) NOT NULL, " 
            + "`uuid` BINARY(16) DEFAULT NULL," 
            + "PRIMARY KEY (`cid`, `user`, `alias`) " 
            + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;");
        } finally {
            closeSQLStuff(stmt);
        }
    }
    
    private int insertAliases(Connection conn, Set<Alias> aliases) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement("REPLACE INTO user_alias (cid, user, alias, uuid) VALUES(?, ?, ?, ?)");
            int index;
            for (Alias alias : aliases) {
                index = 0;
                stmt.setInt(++index, alias.getCid());
                stmt.setInt(++index, alias.getUserId());
                stmt.setString(++index, alias.getAlias());
                stmt.setBytes(++index, UUIDs.toByteArray(alias.getUuid()));
                stmt.addBatch();
            }
            int[] updateCounts = stmt.executeBatch();

            int updated = 0;
            for (int updateCount : updateCounts) {
                updated += updateCount;
            }
            return updated;
        } finally {
            closeSQLStuff(stmt);
        }
    }

    @Override
    public String[] getDependencies() {
        return new String[] {};
    }

    @Override
    public TaskAttributes getAttributes() {
        return new Attributes(com.openexchange.groupware.update.UpdateConcurrency.BLOCKING);
    }
}
