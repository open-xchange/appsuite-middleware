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

package com.openexchange.groupware.reminder;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.server.impl.DBPool;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIterators;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * {@link ReminderServiceImpl}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.4
 */
public class ReminderServiceImpl implements ReminderService{

    /**
     * Checks whether the user has the appropriate permission for this reminder
     * @param session
     * @param reminder
     * @throws OXException
     */
    private void checkPermission(Session session, ReminderObject reminder, boolean write) throws OXException {
        if(reminder.getUser() != session.getUserId()){
            if(write){
                throw ReminderExceptionCode.NO_PERMISSION_MODIFY.create();
            } else {
                throw ReminderExceptionCode.NO_PERMISSION_READ.create();
            }
        }
    }

    @Override
    public int insertReminder(Session session, ReminderObject reminderObj) throws OXException {
        ServerSession serverSession = ServerSessionAdapter.valueOf(session);
        return ReminderHandler.getInstance().insertReminder(reminderObj, serverSession.getContext());
    }

    @Override
    public int insertReminder(Session session, ReminderObject reminderObj, Connection writeCon) throws OXException {
        ServerSession serverSession = ServerSessionAdapter.valueOf(session);
        return ReminderHandler.getInstance().insertReminder(reminderObj, writeCon, serverSession.getContext());
    }

    @Override
    public void updateReminder(Session session, ReminderObject reminder) throws OXException {
        ServerSession serverSession = ServerSessionAdapter.valueOf(session);
        Connection writeConnection = DBPool.pickupWriteable(serverSession.getContext());
        try {
            updateReminder(serverSession, reminder, writeConnection);
        } finally {
            if (writeConnection != null) {
                Database.back(serverSession.getContext(), true, writeConnection);
            }
        }
    }

    @Override
    public void updateReminder(Session session, ReminderObject reminder, Connection con) throws OXException {
        ServerSession serverSession = ServerSessionAdapter.valueOf(session);
        ReminderObject oldReminder = ReminderHandler.getInstance().loadReminder(reminder.getObjectId(), con, serverSession.getContext());
        checkPermission(session, oldReminder, true);
        ReminderHandler.getInstance().updateReminder(reminder, con, serverSession.getContext());
    }

    @Override
    public void deleteReminder(Session session, ReminderObject reminder) throws OXException {
        ServerSession serverSession = ServerSessionAdapter.valueOf(session);
        Connection writeConnection = DBPool.pickupWriteable(serverSession.getContext());
        try {
            ReminderObject oldReminder = ReminderHandler.getInstance().loadReminder(reminder.getObjectId(), writeConnection, serverSession.getContext());
            checkPermission(session, oldReminder, true);
            ReminderHandler.getInstance().deleteReminder(oldReminder, serverSession.getContext(), writeConnection);
        } finally {
            if (writeConnection != null) {
                Database.back(serverSession.getContext(), true, writeConnection);
            }
        }
    }

    @Override
    public void deleteReminder(Session session, int targetId, int module) throws OXException {
        ServerSession serverSession = ServerSessionAdapter.valueOf(session);
        Connection writeConnection = DBPool.pickupWriteable(serverSession.getContext());
        try {
            deleteReminder(serverSession, targetId, module, writeConnection);
        } finally {
            if (writeConnection != null) {
                Database.back(serverSession.getContext(), true, writeConnection);
            }
        }

    }

    @Override
    public void deleteReminder(Session session, int targetId, int module, Connection writeCon) throws OXException {
        ServerSession serverSession = ServerSessionAdapter.valueOf(session);
        ReminderObject oldReminder = ReminderHandler.getInstance().loadReminder(targetId, session.getUserId(), module, writeCon, serverSession.getContext());
        checkPermission(session, oldReminder, true);
        ReminderHandler.getInstance().deleteReminder(oldReminder.getTargetId(), oldReminder.getModule(), writeCon, serverSession.getContext());
    }

    @Override
    public void deleteReminder(Session session, int targetId, int userId, int module) throws OXException {
        ServerSession serverSession = ServerSessionAdapter.valueOf(session);
        Connection writeConnection = DBPool.pickupWriteable(serverSession.getContext());
        try {
            deleteReminder(serverSession, targetId, userId, module, writeConnection);
        } finally {
            if (writeConnection != null) {
                Database.back(serverSession.getContext(), true, writeConnection);
            }
        }

    }

    @Override
    public void deleteReminder(Session session, int targetId, int userId, int module, Connection writeCon) throws OXException {
        ServerSession serverSession = ServerSessionAdapter.valueOf(session);
        ReminderObject oldReminder = ReminderHandler.getInstance().loadReminder(targetId, userId, module, writeCon, serverSession.getContext());
        checkPermission(session, oldReminder, true);
        ReminderHandler.getInstance().deleteReminder(oldReminder.getTargetId(), userId, oldReminder.getModule(), writeCon, serverSession.getContext());

    }

    @Override
    public boolean existsReminder(Session session, int targetId, int userId, int module) throws OXException {
        ServerSession serverSession = ServerSessionAdapter.valueOf(session);
        Connection readCon = DBPool.pickup(serverSession.getContext());
        try {
            return existsReminder(serverSession, targetId, userId, module, readCon);
        } finally {
            if (readCon != null) {
                Database.back(serverSession.getContext(), false, readCon);
            }
        }
    }

    @Override
    public boolean existsReminder(Session session, int targetId, int userId, int module, Connection writeConnection) throws OXException {
        ServerSession serverSession = ServerSessionAdapter.valueOf(session);

        try {
            ReminderObject oldReminder = ReminderHandler.getInstance().loadReminder(targetId, userId, module, writeConnection, serverSession.getContext());
            checkPermission(session, oldReminder, true);
            return true;
        } catch (OXException e) {
            if (ReminderExceptionCode.NOT_FOUND.equals(e)) {
                return false;
            }
            throw e;
        }
    }

    @Override
    public ReminderObject loadReminder(Session session, int targetId, int userId, int module) throws OXException {
        ServerSession serverSession = ServerSessionAdapter.valueOf(session);
        Connection con = DBPool.pickup(serverSession.getContext());
        try {
            return loadReminder(session, targetId, userId, module, con);
        } finally {
            if (con != null) {
                Database.back(serverSession.getContext(), false, con);
            }
        }
    }

    @Override
    public ReminderObject loadReminder(Session session, int targetId, int userId, int module, Connection con) throws OXException {
        ServerSession serverSession = ServerSessionAdapter.valueOf(session);
        ReminderObject oldReminder = ReminderHandler.getInstance().loadReminder(targetId, userId, module, con, serverSession.getContext());
        checkPermission(session, oldReminder, false);
        return oldReminder;
    }

    @Override
    public ReminderObject loadReminder(Session session, int objectId) throws OXException {
        ServerSession serverSession = ServerSessionAdapter.valueOf(session);
        ReminderObject oldReminder = ReminderHandler.getInstance().loadReminder(objectId, serverSession.getContext());
        checkPermission(session, oldReminder, false);
        return oldReminder;
    }

    @Override
    public ReminderObject[] loadReminder(Session session, int[] targetIds, int userId, int module) throws OXException {
        ServerSession serverSession = ServerSessionAdapter.valueOf(session);
        Connection con = DBPool.pickup(serverSession.getContext());
        try {
            return loadReminders(serverSession, targetIds, userId, module, con);
        } finally {
            if (con != null) {
                Database.back(serverSession.getContext(), false, con);
            }
        }
    }

    @Override
    public ReminderObject[] loadReminders(Session session, int[] targetIds, int userId, int module, Connection con) throws OXException {
        ServerSession serverSession = ServerSessionAdapter.valueOf(session);
        ReminderObject[] oldReminders = ReminderHandler.getInstance().loadReminders(targetIds, userId, module, con, serverSession.getContext());
        for(ReminderObject rem: oldReminders ){
            checkPermission(session, rem, false);
        }
        return oldReminders;
    }

    @Override
    public List<ReminderObject> getArisingReminder(Session session, Context ctx, User user, Date end) throws OXException {
        ServerSession serverSession = ServerSessionAdapter.valueOf(session);
        SearchIterator<ReminderObject> reminders = ReminderHandler.getInstance().getArisingReminder(serverSession, ctx, user, end);
        try {
            List<ReminderObject> result = new ArrayList<ReminderObject>();
            while (reminders.hasNext()) {
                ReminderObject reminder = reminders.next();
                checkPermission(session, reminder, false);
                result.add(reminder);
            }
            return result;
        } finally {
            SearchIterators.close(reminders);
        }
    }

    @Override
    public void remindAgain(ReminderObject reminder, Session session, Context ctx) throws OXException {
        ServerSession serverSession = ServerSessionAdapter.valueOf(session);
        Connection con = DBPool.pickupWriteable(serverSession.getContext());
        try {
            remindAgain(reminder, serverSession, ctx, con);
        } finally {
            if (con != null) {
                Database.back(serverSession.getContext(), true, con);
            }
        }

    }

    @Override
    public void remindAgain(ReminderObject reminder, Session session, Context ctx, Connection writeCon) throws OXException {
        ServerSession serverSession = ServerSessionAdapter.valueOf(session);
        ReminderObject oldReminder = ReminderHandler.getInstance().loadReminder(reminder.getObjectId(), serverSession.getContext());
        checkPermission(session, oldReminder, true);
        ReminderHandler.getInstance().remindAgain(reminder, session, ctx);
    }

    @Override
    public List<ReminderObject> listModifiedReminder(Session session, int userId, Date lastModified) throws OXException {
        ServerSession serverSession = ServerSessionAdapter.valueOf(session);
        SearchIterator<ReminderObject> reminders = ReminderHandler.getInstance().listModifiedReminder(userId, lastModified, serverSession.getContext());
        try {
            List<ReminderObject> result = new ArrayList<ReminderObject>();
            while (reminders.hasNext()) {
                ReminderObject reminder = reminders.next();
                checkPermission(session, reminder, false);
                result.add(reminder);
            }
            return result;
        } finally {
            SearchIterators.close(reminders);
        }
    }


}
