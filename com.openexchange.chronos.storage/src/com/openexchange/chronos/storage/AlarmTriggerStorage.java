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

package com.openexchange.chronos.storage;

import java.util.List;
import java.util.Map;
import java.util.Set;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.AlarmTrigger;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.service.RangeOption;
import com.openexchange.exception.OXException;

/**
 * {@link AlarmTriggerStorage} is a storage for alarm triggers.
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public interface AlarmTriggerStorage {

    /**
     * Inserts all necessary triggers for the given event. In case of an update remove all existing triggers first.
     *
     * @param event The event to insert triggers for
     * @param exceptions The exceptions of the event
     * @throws OXException
     */
    void insertTriggers(Event event, Set<RecurrenceId> exceptions) throws OXException;

    /**
     * Inserts all necessary triggers for the given alarm objects.
     * This method adds alarm trigger objects as a batch operation and does some additional performance optimizations.
     *
     * @param alarmsPerAttendee A map of alarms per user per event.
     * @param event A list of events.
     * @param exceptions A map of exceptions per event.
     * @throws OXException
     */
    public void insertTriggers(Map<String, Map<Integer, List<Alarm>>> alarms, List<Event> events, Map<String, Set<RecurrenceId>> exceptions) throws OXException;

    /**
     * Removes all existing triggers for the given event
     *
     * @param eventId The event id
     * @throws OXException
     */
    void deleteTriggers(String eventId) throws OXException;

    /**
     * Deletes any existing triggers for multiple events.
     *
     * @param eventIds The identifiers of the events to delete the triggers for
     * @throws OXException
     */
    void deleteTriggers(List<String> eventIds) throws OXException;

    /**
     * Lists all alarm triggers for the given user which will trigger within the given {@link RangeOption}
     *
     * @param userId The user id
     * @param option The {@link RangeOption}
     * @return A list of {@link AlarmTrigger}s
     * @throws OXException
     */
    List<AlarmTrigger> loadTriggers(int userId, RangeOption option) throws OXException;

    /**
     * Recalculates the trigger time for floating events. E.g. to adapt to a timezone change of the user.
     *
     * @param userId The user id
     * @return The number of changed alarm triggers
     * @throws OXException
     */
    Integer recalculateFloatingAlarmTriggers(int userId) throws OXException;

}
