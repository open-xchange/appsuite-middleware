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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.server.impl.DBPool;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIterators;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.user.User;

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
        if (reminder.getUser() != session.getUserId()){
            if (write){
                throw ReminderExceptionCode.NO_PERMISSION_MODIFY.create();
            }
            throw ReminderExceptionCode.NO_PERMISSION_READ.create();
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
                if (reminder.getModule() == Types.APPOINTMENT){
                    continue;
                }
                checkPermission(session, reminder, false);
                result.add(reminder);
            }
            return result;
        } finally {
            SearchIterators.close(reminders);
        }
    }


}
