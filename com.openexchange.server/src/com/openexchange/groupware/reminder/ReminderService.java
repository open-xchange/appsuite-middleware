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
import java.util.Date;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.session.Session;

/**
 * {@link ReminderService}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.4
 */
public interface ReminderService {

    /**
     * Inserts a given reminder
     *
     * @param session The session
     * @param reminderObj The reminder to insert
     * @return The id of the reminder
     * @throws OXException
     */
    public int insertReminder(Session session, ReminderObject reminderObj) throws OXException;

    /**
     * Inserts a given reminder
     *
     * @param session The session
     * @param reminderObj The reminder to insert
     * @param writeCon A writable database connection
     * @return The id of the inserted reminder
     * @throws OXException
     */
    public int insertReminder(Session session, ReminderObject reminderObj, Connection writeCon) throws OXException;

    /**
     * Updates a reminder
     *
     * @param session The session
     * @param reminder Object with the new values for the reminder
     * @throws OXException
     */
    void updateReminder(Session session, ReminderObject reminder) throws OXException;

    /**
     * Updates a reminder
     *
     * @param session The session
     * @param reminder Object with new values for the reminder.
     * @param con A writable database connection.
     * @throws OXException
     */
    void updateReminder(Session session, ReminderObject reminder, Connection con) throws OXException;

    /**
     * Deletes a reminder
     *
     * @param session The session
     * @param reminder The reminder object containing at least the reminder id
     * @throws OXException
     */
    public void deleteReminder(Session session, ReminderObject reminder) throws OXException;

    /**
     * Deletes all reminder for a given target
     *
     * @param session The session
     * @param targetId The target id
     * @param module The module
     * @throws OXException
     */
    public void deleteReminder(Session session, int targetId, int module) throws OXException;

    /**
     * Deletes all reminder for a given target
     *
     * @param session The session
     * @param targetId The target id
     * @param module The module
     * @param writeCon A writable database connection
     * @throws OXException
     */
    public void deleteReminder(Session session, int targetId, int module, Connection writeCon) throws OXException;

    /**
     * Deletes a users reminder for a given target
     *
     * @param session The session
     * @param targetId The target id
     * @param userId The user id
     * @param module The module
     * @throws OXException
     */
    public void deleteReminder(Session session, int targetId, int userId, int module) throws OXException;

    /**
     * Deletes a users reminder for a given target
     *
     * @param session The session
     * @param targetId The target id
     * @param userId The user id
     * @param module The module
     * @param writeCon A writable database connection
     * @throws OXException
     */
    public void deleteReminder(Session session, int targetId, int userId, int module, Connection writeCon) throws OXException;

    /**
     * Checks whether a reminder exists for the given target and user
     *
     * @param session The session
     * @param targetId The target id
     * @param userId The user id
     * @param module The module
     * @return true if a reminder exists, false otherwise
     * @throws OXException
     */
    public boolean existsReminder(Session session, int targetId, int userId, int module) throws OXException;

    /**
     * Checks whether a reminder exists for the given target and user
     *
     * @param session The session
     * @param targetId The target id
     * @param userId The user id
     * @param module The module
     * @param con A database connection
     * @return true if a reminder exists, false otherwise
     * @throws OXException
     */
    public boolean existsReminder(Session session, int targetId, int userId, int module, Connection con) throws OXException;

    /**
     * Load the users reminder for a given target
     *
     * @param session The session
     * @param targetId Ther target id
     * @param userId The user id
     * @param module The module
     * @return The reminder
     * @throws OXException
     */
    public ReminderObject loadReminder(Session session, int targetId, int userId, int module) throws OXException;

    /**
     * Loads the reminder with the given reminder id
     *
     * @param session The session
     * @param objectId The reminder id
     * @return The reminder
     * @throws OXException if no reminder with this id exists
     */
    public ReminderObject loadReminder(Session session, int objectId) throws OXException;

    /**
     * Loads the users reminder for a given target
     *
     * @param session The session
     * @param targetId The target id
     * @param userId The user id
     * @param module The module
     * @param con A database connection
     * @return The reminder
     * @throws OXException
     */
    public ReminderObject loadReminder(Session session, final int targetId, final int userId, final int module, final Connection con) throws OXException;

    /**
     * Loads the reminder for several target objects.
     *
     * @param targetIds unique identifier of several target objects.
     * @param userId The user id
     * @param module module type of target objects.
     * @return an array of found reminders.
     * @throws OXException if reading the reminder fails.
     */
    ReminderObject[] loadReminder(Session session, int[] targetIds, int userId, int module) throws OXException;

    /**
     * Loads the reminder for several target objects.
     *
     * @param targetIds unique identifier of several target objects.
     * @param userId The user id
     * @param module module type of target objects.
     * @param con A database connection
     * @return an array of found reminders.
     * @throws OXException if reading the reminder fails.
     */
    ReminderObject[] loadReminders(Session session, int[] targetIds, int userId, int module, Connection con) throws OXException;

    /**
     * Returns a list of reminders that should pop up in the time frame starting now and ending at the given end date.
     *
     * @param session The session.
     * @param ctx The context.
     * @param user The user
     * @param end End of the wanted time frame.
     * @return a list of reminder that should pop up.
     * @throws OXException if loading the reminder fails in some way.
     */
    List<ReminderObject> getArisingReminder(Session session, Context ctx, User user, Date end) throws OXException;

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
     * List all modified reminders that where modified since lastModified
     *
     * @param session The session
     * @param userId The user id
     * @param lastModified The last known modification time
     * @return A list of modified reminders
     * @throws OXException
     */
    public List<ReminderObject> listModifiedReminder(Session session, int userId, Date lastModified) throws OXException;

}
