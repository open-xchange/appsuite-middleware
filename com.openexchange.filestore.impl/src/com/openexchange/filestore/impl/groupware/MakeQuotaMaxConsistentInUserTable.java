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

package com.openexchange.filestore.impl.groupware;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.slf4j.Logger;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.impl.osgi.Services;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;
import com.openexchange.tools.update.Column;
import com.openexchange.tools.update.Tools;

/**
 * {@link MakeQuotaMaxConsistentInUserTable} - Ensures a NOT NULL value for "quota_max" column in "user" and "del_user" tables.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class MakeQuotaMaxConsistentInUserTable extends UpdateTaskAdapter {

    /**
     * Initializes a new {@link MakeQuotaMaxConsistentInUserTable}.
     */
    public MakeQuotaMaxConsistentInUserTable() {
        super();
    }

    @Override
    public void perform(PerformParameters params) throws OXException {
        Logger log = org.slf4j.LoggerFactory.getLogger(MakeQuotaMaxConsistentInUserTable.class);
        log.info("Performing update task {}", MakeQuotaMaxConsistentInUserTable.class.getSimpleName());

        DatabaseService databaseService = Services.requireService(DatabaseService.class);

        int ctxId = params.getContextId();
        Connection con = databaseService.getForUpdateTask(ctxId);
        boolean rollback = false;
        try {
            Databases.startTransaction(con);
            rollback = true;

            // Turns all NULL values to -1
            turnNulltoNumber("user", con);
            turnNulltoNumber("del_user", con);

            // Changes "quota_max BIGINT(20) DEFAULT NULL" to "quota_max BIGINT(20) NOT NULL DEFAULT -1" (if not yet performed)
            Column column = new Column("quota_max", "BIGINT(20) NOT NULL DEFAULT -1");
            if (Tools.isNullable(con, "user", "quota_max")) {
                Tools.modifyColumns(con, "user", column);
            }
            if (Tools.isNullable(con, "del_user", "quota_max")) {
                Tools.modifyColumns(con, "del_user", column);
            }

            con.commit();
            rollback = false;
        } catch (SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw UpdateExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            if (rollback) {
                Databases.rollback(con);
            }
            Databases.autocommit(con);
            databaseService.backForUpdateTask(ctxId, con);
        }

        log.info("{} successfully performed.", MakeQuotaMaxConsistentInUserTable.class.getSimpleName());
    }

    private void turnNulltoNumber(String table, Connection con) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("UPDATE " + table + " SET quota_max=-1 WHERE quota_max IS NULL");
            stmt.executeUpdate();
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    @Override
    public String[] getDependencies() {
        return new String[] { AddFilestoreColumnsToUserTable.class.getName() };
    }
}
