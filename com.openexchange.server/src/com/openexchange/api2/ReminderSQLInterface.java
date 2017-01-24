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
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 */
public interface ReminderSQLInterface {

    /**
     * This method inserts a reminder
     *
     * @param reminderObj The reminder to insert
     * @param context The context
     * @return The id of the new reminder
     * @throws OXException
     */
    public int insertReminder(ReminderObject reminderObj, Context context) throws OXException;

    /**
     * This method inserts a reminder
     *
     * @param reminderObj The reminder to insert
     * @param writeCon A writable database connection
     * @param context The context
     * @return The id of the new reminder
     * @throws OXException
     */
    public int insertReminder(ReminderObject reminderObj, Connection writeCon, Context context) throws OXException;

    /**
     * This method updates a reminder
     *
     * @param reminder Object with new values for the reminder
     * @param context The context
     * @throws OXException
     */
    void updateReminder(ReminderObject reminder, Context context) throws OXException;

    /**
     * This method updates a reminder.
     *
     * @param reminder Object with new values for the reminder.
     * @param con A writable database connection.
     * @param context The context
     * @throws OXException
     */
    void updateReminder(ReminderObject reminder, Connection con, Context context) throws OXException;

    /**
     * Deletes a reminder
     *
     * @param reminder A reminder object containing at least the reminder id
     * @param context The context
     * @throws OXException
     */
    public void deleteReminder(ReminderObject reminder, Context context) throws OXException;

    /**
     * Deletes all reminders referencing one target
     *
     * @param targetId The target id
     * @param module The module
     * @param context The context
     * @throws OXException
     */
    public void deleteReminder(int targetId, int module, Context context) throws OXException;

    /**
     * Deletes all reminders referencing one target
     *
     * @param targetId The target id
     * @param module The module
     * @param writeCon A writable database connection
     * @param context The context
     * @throws OXException
     */
    public void deleteReminder(int targetId, int module, Connection writeCon, Context context) throws OXException;

    /**
     * Deletes all reminders off one user referencing one target
     *
     * @param targetId The target id
     * @param userId The user id
     * @param module The module
     * @param context The context
     * @throws OXException
     */
    public void deleteReminder(int targetId, int userId, int module, Context context) throws OXException;

    /**
     * Deletes all reminders off one user referencing one target
     *
     * @param targetId The target id
     * @param userId The user id
     * @param module The module
     * @param writeCon A writable database connection
     * @param context The context
     * @throws OXException
     */
    public void deleteReminder(int targetId, int userId, int module, Connection writeCon, Context context) throws OXException;

    /**
     * Checks whether a reminder exists for the target and user id or not
     *
     * @param targetId The target id
     * @param userId The user id
     * @param module The module
     * @param context The context
     * @return true if a reminder exists, false otherwise.
     * @throws OXException
     */
    public boolean existsReminder(int targetId, int userId, int module, Context context) throws OXException;

    /**
     * Checks whether a reminder exists for the target and user id or not
     *
     * @param targetId The target id
     * @param userId The user id
     * @param module The module
     * @param con A database connection
     * @param context The context
     * @return true if a reminder exists, false otherwise.
     * @throws OXException
     */
    public boolean existsReminder(int targetId, int userId, int module, Connection con, Context context) throws OXException;

    /**
     * Loads the reminder for the given target and user
     *
     * @param targetId The target id
     * @param userId The user id
     * @param module The module
     * @param context The context
     * @return The reminder object
     * @throws OXException
     */
    public ReminderObject loadReminder(int targetId, int userId, int module, Context context) throws OXException;

    /**
     * Loads the reminder object with the given id
     *
     * @param objectId The reminder id
     * @param context The context
     * @return The reminder object
     * @throws OXException
     */
    public ReminderObject loadReminder(int objectId, Context context) throws OXException;

    /**
     * Loads the reminder for the given target and user
     *
     * @param targetId The target id
     * @param userId The user id
     * @param module The module
     * @param con A database connection
     * @param context The context
     * @return the reminder object
     * @throws OXException
     */
    public ReminderObject loadReminder(final int targetId, final int userId, final int module, final Connection con, Context context) throws OXException;

    /**
     * This method loads the reminder for several target objects.
     *
     * @param targetIds unique identifier of several target objects.
     * @param userId unique identifier of the user.
     * @param module module type of target objects.
     * @param context The context
     * @return an array of found reminders.
     * @throws OXException if reading the reminder fails.
     */
    ReminderObject[] loadReminder(int[] targetIds, int userId, int module, Context context) throws OXException;

    /**
     * This method loads the reminder for several target objects.
     *
     * @param targetIds unique identifier of several target objects.
     * @param userId unique identifier of the user.
     * @param module module type of target objects.
     * @param con A database connection
     * @param context The context
     * @return an array of found reminders.
     * @throws OXException if reading the reminder fails.
     */
    ReminderObject[] loadReminders(int[] targetIds, int userId, int module, Connection con, Context context) throws OXException;

    /**
     * List all reminders for the given target
     *
     * @param module The module
     * @param targetId The target id
     * @param context The context
     * @return A list of reminders
     * @throws OXException
     */
    SearchIterator<ReminderObject> listReminder(int module, int targetId, Context context) throws OXException;

    /**
     * Fetches the list of reminder that should pop up in the time frame starting now and ending at the given end date.
     *
     * @param session the session.
     * @param ctx the context.
     * @param user reminder should be for this user.
     * @param end end of the wanted time frame.
     * @return a list of reminder that should pop up.
     * @throws OXException if loading the reminder fails in some way.
     */
    SearchIterator<ReminderObject> getArisingReminder(Session session, Context ctx, User user, Date end) throws OXException;

    /**
     * Updates the alarm of specified reminder.
     *
     * @param reminder The reminder with all fields and new alarm date set
     * @param session The session
     * @param ctx The context
     * @throws OXException If alarm of reminder cannot be updated
     */
    public void remindAgain(ReminderObject reminder, Session session, Context ctx) throws OXException;

    /**
     * Updates the alarm of specified reminder.
     *
     * @param reminder The reminder with all fields and new alarm date set
     * @param session The session
     * @param ctx The context
     * @param writeCon The read-write connection
     * @throws OXException If alarm of reminder cannot be updated
     */
    public void remindAgain(ReminderObject reminder, Session session, Context ctx, Connection writeCon) throws OXException;

    /**
     * List all modified reminders for the given user since lastModified
     *
     * @param userId The user id
     * @param lastModified The last known modification time
     * @param context The context
     * @return A list of modified reminders
     * @throws OXException
     */
    public SearchIterator<ReminderObject> listModifiedReminder(int userId, Date lastModified, Context context) throws OXException;

}
