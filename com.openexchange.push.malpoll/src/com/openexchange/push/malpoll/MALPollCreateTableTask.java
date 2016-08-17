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

package com.openexchange.push.malpoll;

import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import com.openexchange.database.AbstractCreateTableImpl;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.Attributes;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.TaskAttributes;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskV2;
import com.openexchange.tools.update.Tools;

/**
 * {@link MALPollCreateTableTask} - Inserts necessary tables to support MAL Poll bundle features.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MALPollCreateTableTask extends AbstractCreateTableImpl implements UpdateTaskV2 {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MALPollCreateTableTask.class);

    private static String getCreateHashTable() {
        return "CREATE TABLE malPollHash (" +
        " cid INT4 UNSIGNED NOT NULL," +
        " user INT4 UNSIGNED NOT NULL," +
        " id INT4 UNSIGNED NOT NULL," +
        " fullname VARCHAR(64) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL," +
        " hash BINARY(16) NOT NULL," +
        " PRIMARY KEY (cid,user,id,fullname)," +
        " FOREIGN KEY (cid,user) REFERENCES user(cid,id)," +
        " FOREIGN KEY (cid,user,id) REFERENCES user_mail_account(cid,user,id)" +
        ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";
    }

    private static final String getCreateUIDsTable() {
        return "CREATE TABLE malPollUid (" +
        " cid INT4 UNSIGNED NOT NULL," +
        " hash BINARY(16) NOT NULL," +
        " uid VARCHAR(70) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL," +
        " PRIMARY KEY (cid,hash,uid(32))" +
        ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";
    }

    @Override
    public String[] getCreateStatements() {
        return new String[] { getCreateHashTable(), getCreateUIDsTable() };
    }

    @Override
    public String[] requiredTables() {
        return new String[] { "user", "user_mail_account" };
    }

    @Override
    public String[] tablesToCreate() {
        return new String[] { "malPollHash", "malPollUid" };
    }

    @Override
    public String[] getDependencies() {
        return new String[] { "com.openexchange.groupware.update.tasks.FolderAddIndex4SharedFolderSearch" };
    }

    @Override
    public TaskAttributes getAttributes() {
        return new Attributes();
    }

    @Override
    public void perform(final PerformParameters params) throws OXException {
        final int contextId = params.getContextId();
        createTable("malPollHash", getCreateHashTable(), contextId);
        createTable("malPollUid", getCreateUIDsTable(), contextId);
        LOG.info("UpdateTask 'MALPollCreateTableTask' successfully performed!");
    }

    private void createTable(final String tablename, final String sqlCreate, final int contextId) throws OXException, OXException {
        final Connection writeCon = Database.get(contextId, true);
        PreparedStatement stmt = null;
        try {
            if (Tools.tableExists(writeCon, tablename)) {
                return;
            }
            stmt = writeCon.prepareStatement(sqlCreate);
            stmt.executeUpdate();
        } catch (final SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            closeSQLStuff(stmt);
            Database.back(contextId, true, writeCon);
        }
    }
}
