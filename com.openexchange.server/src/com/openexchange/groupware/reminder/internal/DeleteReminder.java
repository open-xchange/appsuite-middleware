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

import static com.openexchange.database.Databases.autocommit;
import static com.openexchange.database.Databases.rollback;
import java.sql.Connection;
import java.sql.SQLException;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.reminder.ReminderExceptionCode;
import com.openexchange.groupware.reminder.ReminderObject;
import com.openexchange.groupware.reminder.ReminderStorage;

/**
 * {@link DeleteReminder}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class DeleteReminder {

    private static final ReminderStorage STORAGE = ReminderStorage.getInstance();

    private final Context ctx;

    private final ReminderObject reminder;

    public DeleteReminder(final Context ctx, final ReminderObject reminder) {
        super();
        this.ctx = ctx;
        this.reminder = reminder;
    }

    public void perform() throws OXException {
        final Connection con = Database.get(ctx, true);
        try {
            con.setAutoCommit(false);
            delete(con);
            con.commit();
        } catch (SQLException e) {
            throw ReminderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } catch (OXException e) {
            rollback(con);
            throw e;
        } finally {
            autocommit(con);
            Database.back(ctx, true, con);
        }
    }

    private void delete(final Connection con) throws OXException {
        STORAGE.deleteReminder(con, ctx.getContextId(), reminder.getObjectId());
        TargetRegistry.getInstance().getService(reminder.getModule()).updateTargetObject(ctx, con, reminder.getTargetId(), reminder.getUser());
    }
}
