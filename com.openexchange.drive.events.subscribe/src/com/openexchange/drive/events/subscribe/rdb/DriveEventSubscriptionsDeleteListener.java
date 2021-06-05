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

package com.openexchange.drive.events.subscribe.rdb;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.delete.DeleteEvent;
import com.openexchange.groupware.delete.DeleteFailedExceptionCodes;
import com.openexchange.groupware.delete.DeleteListener;

/**
 * {@link DriveEventSubscriptionsDeleteListener}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class DriveEventSubscriptionsDeleteListener implements DeleteListener {

    @Override
    public void deletePerformed(DeleteEvent event, Connection readCon, Connection writeCon) throws OXException {
        if (DeleteEvent.TYPE_CONTEXT == event.getType()) {
            try {
                deleteSubscriptions(writeCon, event.getContext().getContextId());
            } catch (SQLException e) {
                if ("42S02".equals(e.getSQLState())) {
                    // "Table 'driveEventSubscriptions' doesn't exist" => no update task for drive tables in this schema yet, so ignore
                } else {
                    throw DeleteFailedExceptionCodes.SQL_ERROR.create(e, e.getMessage());
                }
            } catch (Exception e) {
                throw DeleteFailedExceptionCodes.ERROR.create(e, e.getMessage());
            }
        } else if (DeleteEvent.TYPE_USER == event.getType()) {
            try {
                if (event.getContext().getMailadmin() == event.getId()) {
                    // delete all subscriptions for context in case mailadmin is deleted
                    deleteSubscriptions(writeCon, event.getContext().getContextId());
                } else {
                    deleteSubscriptions(writeCon, event.getContext().getContextId(), event.getId());
                }
            } catch (SQLException e) {
                if ("42S02".equals(e.getSQLState())) {
                    // "Table 'driveEventSubscriptions' doesn't exist" => no update task for drive tables in this schema yet, so ignore
                } else {
                    throw DeleteFailedExceptionCodes.SQL_ERROR.create(e, e.getMessage());
                }
            } catch (Exception e) {
                throw DeleteFailedExceptionCodes.ERROR.create(e, e.getMessage());
            }
        }
    }

    private static int deleteSubscriptions(Connection connection, int cid) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(SQL.DELETE_SUBSCRIPTIONS_IN_CONTEXT_STMT);
            stmt.setInt(1, cid);
            return SQL.logExecuteUpdate(stmt);
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    private static int deleteSubscriptions(Connection connection, int cid, int user) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(SQL.DELETE_SUBSCRIPTIONS_FOR_USER_STMT);
            stmt.setInt(1, cid);
            stmt.setInt(2, user);
            return SQL.logExecuteUpdate(stmt);
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

}
