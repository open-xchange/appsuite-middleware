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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.reminder.ReminderObject;
import com.openexchange.groupware.reminder.ReminderStorage;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.ArrayIterator;
import com.openexchange.tools.iterator.SearchIterator;

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
