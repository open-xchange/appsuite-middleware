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

package com.openexchange.drive.events.subscribe.rdb;

import static com.openexchange.tools.sql.DBUtils.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import com.openexchange.database.DatabaseService;
import com.openexchange.drive.events.subscribe.internal.SubscribeServiceLookup;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;
import com.openexchange.java.util.UUIDs;
import com.openexchange.tools.update.Column;
import com.openexchange.tools.update.Tools;

/**
 * {@link DriveEventSubscriptionsMakeUuidPrimaryTask}
 *
 * Changes the column defintion for <code>uuid</code> to <code>uuid BINARY(16) NOT NULL</code> in the
 * <code>driveEventSubscriptions</code> table, fills it with random values, then changes the primary key to
 * <code>(cid,uuid)</code>. Also, an additional index for <code>(cid,service,token)</code> is added.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.2
 */
public class DriveEventSubscriptionsMakeUuidPrimaryTask extends UpdateTaskAdapter {

    @Override
    public String[] getDependencies() {
        return new String[] { DriveEventSubscriptionsAddUuidColumnTask.class.getName() };
    }

    @Override
    public void perform(PerformParameters params) throws OXException {
        int contextID = params.getContextId();
        DatabaseService dbService = SubscribeServiceLookup.getService(DatabaseService.class, true);
        Connection connection = dbService.getForUpdateTask(contextID);
        boolean committed = false;
        try {
            connection.setAutoCommit(false);
            if (false == Tools.columnExists(connection, "driveEventSubscriptions", "uuid")) {
                throw UpdateExceptionCodes.COLUMN_NOT_FOUND.create("uuid", "driveEventSubscriptions");
            }
            /*
             * fill empty uuid values, make uuid column "not null" & adjust primary key afterwards
             */
            fillUUIDs(connection);
            Tools.modifyColumns(connection, "driveEventSubscriptions", new Column("uuid", "BINARY(16) NOT NULL"));
            Tools.createPrimaryKeyIfAbsent(connection, "driveEventSubscriptions", new String[] { "cid", "uuid" });
            Tools.createIndex(connection, "driveEventSubscriptions", new String[] { "cid", "service,", "token" });
            connection.commit();
            committed = true;
        } catch (SQLException e) {
            rollback(connection);
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } catch (RuntimeException e) {
            rollback(connection);
            throw UpdateExceptionCodes.OTHER_PROBLEM.create(e, e.getMessage());
        } finally {
            autocommit(connection);
            if (committed) {
                dbService.backForUpdateTask(contextID, connection);
            } else {
                dbService.backForUpdateTaskAfterReading(contextID, connection);
            }
        }
    }

    private static int[] fillUUIDs(Connection connection) throws SQLException {
        PreparedStatement selectStatement = null;
        PreparedStatement updateStatement = null;
        ResultSet resultSet = null;
        try {
            selectStatement = connection.prepareStatement("SELECT cid,service,token FROM driveEventSubscriptions WHERE uuid IS NULL;");
            updateStatement = connection.prepareStatement("UPDATE driveEventSubscriptions SET uuid=? WHERE cid=? AND service=? AND token=?;");
            resultSet = selectStatement.executeQuery();
            while (resultSet.next()) {
                updateStatement.setBytes(1, UUIDs.toByteArray(UUID.randomUUID()));
                updateStatement.setInt(2, resultSet.getInt(1));
                updateStatement.setString(3, resultSet.getString(2));
                updateStatement.setString(4, resultSet.getString(3));
                updateStatement.addBatch();
            }
            return updateStatement.executeBatch();
        } finally {
            closeSQLStuff(resultSet, selectStatement);
            closeSQLStuff(updateStatement);
        }
    }

}
