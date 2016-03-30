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

package com.openexchange.api2;

import java.sql.Connection;
import java.util.Date;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.reminder.ReminderObject;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIterator;

/**
 * This is the central interface to the reminder component.
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public interface ReminderService {

    public int insertReminder(ReminderObject reminderObj) throws OXException;

    public int insertReminder(ReminderObject reminderObj, Connection writeCon) throws OXException;

    void updateReminder(ReminderObject reminder) throws OXException;

    /**
     * This method updates a reminder.
     * @param reminder object with new values for the reminder.
     * @param con writable database connection.
     * @throws OXException TODO
     */
    void updateReminder(ReminderObject reminder, Connection con)
        throws OXException;

    public void deleteReminder(ReminderObject reminder) throws OXException;

    public void deleteReminder(int targetId, int module) throws OXException;

    public void deleteReminder(int targetId, int module, Connection writeCon) throws OXException;

    public void deleteReminder(int targetId, int userId, int module) throws OXException;

    public void deleteReminder(int targetId, int userId, int module, Connection writeCon) throws OXException;

    public boolean existsReminder(int targetId, int userId, int module) throws OXException;

    public boolean existsReminder(int targetId, int userId, int module, Connection con) throws OXException;

    public ReminderObject loadReminder(int targetId, int userId, int module) throws OXException;

    public ReminderObject loadReminder(int objectId) throws OXException;

    public ReminderObject loadReminder(final int targetId, final int userId, final int module, final Connection con) throws OXException;

    /**
     * This method loads the reminder for several target objects.
     * @param targetIds unique identifier of several target objects.
     * @param userId unique identifier of the user.
     * @param module module type of target objects.
     * @return an array of found reminders.
     * @throws OXException if reading the reminder fails.
     */
    ReminderObject[] loadReminder(int[] targetIds, int userId, int module)
        throws OXException;

    ReminderObject[] loadReminders(int[] targetIds, int userId, int module, Connection con)
        throws OXException;

    SearchIterator<ReminderObject> listReminder(int module, int targetId) throws OXException;

    /**
     * Fetches the list of reminder that should pop up in the time frame starting now and ending at the given end date.
     * @param session the session.
     * @param ctx the context.
     * @param user reminder should be for this user.
     * @param end end of the wanted time frame.
     * @return a list of reminder that should pop up.
     * @throws OXException if loading the reminder failes in some way.
     */
    SearchIterator<ReminderObject> getArisingReminder(Session session, Context ctx, User user, Date end) throws OXException;

    /**
     * Updates the alarm of specified reminder.
     *
     * @param reminder The reminder with all fields and new alarm date set
     * @param session The session
     * @param ctx The context
     * @param user The user
     * @param tz The time zone
     * @throws OXException If alarm of reminder cannot be updated
     */
    public void remindAgain(ReminderObject reminder, Session session, Context ctx) throws OXException;

    /**
     * Updates the alarm of specified reminder.
     *
     * @param reminder The reminder with all fields and new alarm date set
     * @param session The session
     * @param ctx The context
     * @param user The user
     * @param tz The time zone
     * @param writeCon The read-write connection
     * @throws OXException If alarm of reminder cannot be updated
     */
    public void remindAgain(ReminderObject reminder, Session session, Context ctx, Connection writeCon) throws OXException;

    public SearchIterator listModifiedReminder(int userId, Date lastModified) throws OXException;

}
