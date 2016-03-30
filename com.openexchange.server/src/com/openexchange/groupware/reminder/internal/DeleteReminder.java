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

import static com.openexchange.tools.sql.DBUtils.autocommit;
import static com.openexchange.tools.sql.DBUtils.rollback;
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
        } catch (final SQLException e) {
            throw ReminderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } catch (final OXException e) {
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
