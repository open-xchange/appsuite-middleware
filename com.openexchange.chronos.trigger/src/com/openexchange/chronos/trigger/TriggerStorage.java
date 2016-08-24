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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.chronos.trigger;

import static com.openexchange.java.Autoboxing.i;
import java.sql.Connection;
import java.util.List;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.reminder.ReminderExceptionCode;
import com.openexchange.groupware.reminder.ReminderHandler;
import com.openexchange.groupware.reminder.ReminderObject;

/**
 * {@link TriggerStorage}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class TriggerStorage {

    private static final int MODULE = Types.APPOINTMENT;

    private final ReminderHandler reminderHandler;
    private final Connection connection;

    /**
     * Initializes a new {@link TriggerStorage}.
     *
     * @param context The context to operate in
     * @param connection The (writable) database connection to use
     */
    public TriggerStorage(Context context, Connection connection) {
        super();
        this.connection = connection;
        this.reminderHandler = new ReminderHandler(context);
    }

    /**
     * Inserts multiple new reminder triggers.
     *
     * @param triggers The triggers to insert
     * @return The number of updated rows
     */
    public int insertTriggers(List<ReminderTrigger> triggers) throws OXException {
        int updated = 0;
        for (ReminderTrigger trigger : triggers) {
            trigger.setObjectId(reminderHandler.insertReminder(trigger, connection));
            updated++;
        }
        return updated;
    }

    /**
     * Replaces the reminder triggers of multiple users for a specific event.
     *
     * @param objectID The identifier of the event to replace the triggers for
     * @param triggersPerUser A map holding the reminder triggers per user identifier, or an empty list if there are none
     * @return The number of updated rows
     */
    public int replaceTriggers(int objectID, Map<Integer, List<ReminderTrigger>> triggersPerUser) throws OXException {
        int updated = 0;
        for (Map.Entry<Integer, List<ReminderTrigger>> entry : triggersPerUser.entrySet()) {
            if (null == entry.getValue() || 0 == entry.getValue().size()) {
                updated += removeTriggers(objectID, i(entry.getKey()));
            } else {
                updated += replaceTriggers(entry.getValue());
            }
        }
        return updated;
    }

    /**
     * Replaces one or more specific reminder triggers.
     *
     * @param triggers The remindr triggers to replace
     * @return The number of updated rows
     */
    public int replaceTriggers(List<ReminderTrigger> triggers) throws OXException {
        int updated = 0;
        for (ReminderTrigger trigger : triggers) {
            try {
                ReminderObject existingTrigger = reminderHandler.loadReminder(trigger.getTargetId(), trigger.getUser(), MODULE, connection);
                trigger.setObjectId(existingTrigger.getObjectId());
                reminderHandler.updateReminder(trigger, connection);
            } catch (OXException e) {
                if (false == ReminderExceptionCode.NOT_FOUND.equals(e)) {
                    throw e;
                }
                trigger.setObjectId(reminderHandler.insertReminder(trigger, connection));
            }
            updated++;
        }
        return updated;
    }

    /**
     * Removes all reminder triggers for an event of a specific user.
     *
     * @param objectID The identifier of the event to remove the triggers for
     * @param userID The identifier of the user to remove the triggers for
     * @return The number of updated rows
     */
    public int removeTriggers(int objectID, int userID) throws OXException {
        try {
            reminderHandler.deleteReminder(objectID, userID, MODULE, connection);
            return 1;
        } catch (OXException e) {
            if (ReminderExceptionCode.NOT_FOUND.equals(e)) {
                return 0;
            }
            throw e;
        }
    }

    /**
     * Removes all reminder triggers for an event.
     *
     * @param objectID The identifier of the event to remove the triggers for
     * @return The number of updated rows
     */
    public int removeTriggers(int objectID) throws OXException {
        try {
            reminderHandler.deleteReminder(objectID, MODULE, connection);
            return 1;
        } catch (OXException e) {
            if (ReminderExceptionCode.NOT_FOUND.equals(e)) {
                return 0;
            }
            throw e;
        }
    }

}
