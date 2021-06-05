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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.reminder.ReminderObject;
import com.openexchange.groupware.reminder.ReminderStorage;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.ArrayIterator;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.user.User;

/**
 * Retrieves the arising reminder for a user.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class GetArisingReminder {

    private static final ReminderStorage STORAGE = ReminderStorage.getInstance();

    private final Session session;
    private final Context ctx;
    private final User user;
    private final Date end;

    public GetArisingReminder(final Session session, final Context ctx, final User user, final Date end) {
        super();
        this.session = session;
        this.ctx = ctx;
        this.user = user;
        this.end = (Date) end.clone();
    }

    public SearchIterator<ReminderObject> loadWithIterator() throws OXException {
        ReminderObject[] reminders = STORAGE.selectReminder(ctx, user, end);
        reminders = removeAppointments(reminders);
        return new ArrayIterator<ReminderObject>(reminders);
    }

    public ReminderObject[] removeAppointments(final ReminderObject[] reminders) {
        final List<ReminderObject> retval = new ArrayList<ReminderObject>(reminders.length);
        for (final ReminderObject reminder : reminders) {
            if (Types.APPOINTMENT != reminder.getModule() || Types.TASK != reminder.getModule()) {
                retval.add(reminder);
            }
        }
        return retval.toArray(new ReminderObject[retval.size()]);
    }

}
