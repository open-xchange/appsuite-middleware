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

package com.openexchange.groupware.reminder.internal;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
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
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.reminder.ReminderExceptionCode;
import com.openexchange.groupware.reminder.ReminderObject;
import com.openexchange.groupware.reminder.ReminderStorage;

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
        } catch (final SQLException exc) {
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
