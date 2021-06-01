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

package com.openexchange.groupware.reminder.internal;

import static com.openexchange.database.Databases.closeSQLStuff;
import static com.openexchange.java.Autoboxing.I;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.reminder.ReminderExceptionCode;
import com.openexchange.groupware.reminder.ReminderObject;
import com.openexchange.groupware.reminder.ReminderStorage;
import com.openexchange.user.User;

/**
 * {@link RdbReminderStorage}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class RdbReminderStorage extends ReminderStorage {

    public RdbReminderStorage() {
        super();
    }

    @Override
    public ReminderObject[] selectReminder(final Context ctx, final Connection con, final User user, final Date end) throws OXException {
        return selectReminder(ctx, con, user.getId(), end);
    }

    @Override
    public ReminderObject[] selectReminder(final Context ctx, final Connection con, final int userId, final Date end) throws OXException {
        PreparedStatement stmt = null;
        ResultSet result = null;
        List<ReminderObject> retval = new ArrayList<ReminderObject>();
        try {
            stmt = con.prepareStatement(SQL.SELECT_RANGE);
            stmt.setInt(1, ctx.getContextId());
            stmt.setInt(2, userId);
            stmt.setTimestamp(3, new Timestamp(end.getTime()));
            result = stmt.executeQuery();
            while (result.next()) {
                ReminderObject reminder = new ReminderObject();
                readResult(result, reminder);
                retval.add(reminder);
            }
        } catch (SQLException e) {
            throw ReminderExceptionCode.SQL_ERROR.create(e);
        } finally {
            closeSQLStuff(result, stmt);
        }
        return retval.toArray(new ReminderObject[retval.size()]);
    }

    @Override
    public void deleteReminder(Connection con, int ctxId, int reminderId) throws OXException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(SQL.DELETE_WITH_ID);
            int pos = 1;
            stmt.setInt(pos++, ctxId);
            stmt.setInt(pos++, reminderId);
            int deleted = stmt.executeUpdate();
            if (deleted == 0) {
                throw ReminderExceptionCode.NOT_FOUND.create(I(reminderId), I(ctxId));
            }
        } catch (SQLException exc) {
            throw ReminderExceptionCode.DELETE_EXCEPTION.create(exc);
        } finally {
            closeSQLStuff(stmt);
        }
    }

    private static void readResult(ResultSet result, ReminderObject reminder) throws SQLException {
        int pos = 1;
        reminder.setObjectId(result.getInt(pos++));
        reminder.setTargetId(result.getInt(pos++));
        reminder.setModule(result.getInt(pos++));
        reminder.setUser(result.getInt(pos++));
        reminder.setDate(result.getTimestamp(pos++));
        reminder.setRecurrenceAppointment(result.getBoolean(pos++));
        reminder.setDescription(result.getString(pos++));
        reminder.setFolder(result.getInt(pos++));
        reminder.setLastModified(new Date(result.getLong(pos++)));
    }

    @Override
    public void writeReminder(final Connection con, final int ctxId, final ReminderObject reminder) throws OXException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(SQL.sqlInsert);
            stmt.setInt(1, reminder.getObjectId());
            stmt.setInt(2, ctxId);
            stmt.setInt(3, reminder.getTargetId());
            stmt.setInt(4, reminder.getModule());
            stmt.setInt(5, reminder.getUser());
            stmt.setTimestamp(6, new Timestamp(reminder.getDate().getTime()));
            stmt.setInt(7, reminder.getRecurrencePosition());
            stmt.setLong(8, reminder.getLastModified().getTime());
            stmt.setInt(9, reminder.getFolder());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw ReminderExceptionCode.INSERT_EXCEPTION.create(e);
        } finally {
            closeSQLStuff(stmt);
        }
    }
}
