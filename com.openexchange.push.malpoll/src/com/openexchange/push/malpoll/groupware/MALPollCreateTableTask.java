/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.push.malpoll.groupware;

import java.sql.Connection;
import java.sql.SQLException;
import com.openexchange.database.AbstractCreateTableImpl;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.Attributes;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.TaskAttributes;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskV2;

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
        " fullname VARCHAR(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL," +
        " hash BINARY(16) NOT NULL," +
        " PRIMARY KEY (cid,user,id,fullname)" +
        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;";
    }

    private static final String getCreateUIDsTable() {
        return "CREATE TABLE malPollUid (" +
        " cid INT4 UNSIGNED NOT NULL," +
        " hash BINARY(16) NOT NULL," +
        " uid VARCHAR(70) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL," +
        " PRIMARY KEY (cid,hash,uid(32))" +
        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;";
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
        return new String[] {};
    }

    @Override
    public TaskAttributes getAttributes() {
        return new Attributes();
    }

    @Override
    public void perform(final PerformParameters params) throws OXException {
        Connection con = params.getConnection();
        int rollback = 0;
        try {
            con.setAutoCommit(false);
            rollback = 1;

            createTable("malPollHash", getCreateHashTable(), con);
            createTable("malPollUid", getCreateUIDsTable(), con);

            con.commit();
            rollback = 2;
        } catch (SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw UpdateExceptionCodes.OTHER_PROBLEM.create(e, e.getMessage());
        } finally {
            if (rollback > 0) {
                if (rollback == 1) {
                    Databases.rollback(con);
                }
                Databases.autocommit(con);
            }
        }

        LOG.info("UpdateTask 'MALPollCreateTableTask' successfully performed!");
    }

}
