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

package com.openexchange.groupware.reminder;

import java.sql.Connection;
import java.util.Date;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.reminder.internal.RdbReminderStorage;
import com.openexchange.user.User;

/**
 * {@link ReminderStorage}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public abstract class ReminderStorage {

    private static final ReminderStorage SINGLETON = new RdbReminderStorage();

    protected ReminderStorage() {
        super();
    }

    public static ReminderStorage getInstance() {
        return SINGLETON;
    }

    public ReminderObject[] selectReminder(final Context ctx, final User user, final Date end) throws OXException {
        final Connection con = Database.get(ctx, false);
        try {
            return selectReminder(ctx, con, user, end);
        } finally {
            Database.back(ctx, false, con);
        }
    }

    public abstract ReminderObject[] selectReminder(Context ctx, Connection con, User user, Date end) throws OXException;

    public abstract ReminderObject[] selectReminder(Context ctx, Connection con, int userId, Date end) throws OXException;

    public void deleteReminder(final Context ctx, final ReminderObject reminder) throws OXException {
        final Connection con = Database.get(ctx, true);
        try {
            deleteReminder(con, ctx.getContextId(), reminder.getObjectId());
        } finally {
            Database.back(ctx, true, con);
        }
    }

    public abstract void deleteReminder(Connection con, int ctxId, int reminderId) throws OXException;

    public void writeReminder(final Context ctx, final ReminderObject reminder) throws OXException {
        final Connection con = Database.get(ctx, true);
        try {
            writeReminder(con, ctx.getContextId(), reminder);
        } finally {
            Database.back(ctx, true, con);
        }
    }

    public abstract void writeReminder(Connection con, int ctxId, ReminderObject reminder) throws OXException;
}
