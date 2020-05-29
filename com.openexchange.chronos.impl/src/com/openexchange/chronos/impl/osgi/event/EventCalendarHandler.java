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

package com.openexchange.chronos.impl.osgi.event;

import static com.openexchange.chronos.common.CalendarUtils.isSignificantChange;
import static com.openexchange.chronos.compat.Event2Appointment.asInteger;
import static com.openexchange.chronos.impl.Utils.getPersonalFolderIds;
import static com.openexchange.java.Autoboxing.i;
import java.util.Collection;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.osgi.service.event.EventAdmin;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.service.CalendarEvent;
import com.openexchange.chronos.service.CalendarHandler;
import com.openexchange.chronos.service.CreateResult;
import com.openexchange.chronos.service.DeleteResult;
import com.openexchange.chronos.service.UpdateResult;
import com.openexchange.event.CommonEvent;
import com.openexchange.exception.OXException;

/**
 * {@link EventCalendarHandler} - Throws OSGi events on changes of calendar events
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.0
 */
public class EventCalendarHandler implements CalendarHandler {

    /** The create event topic */
    private final static String CREATED = "com/openexchange/groupware/event/insert";

    /** The update or rather changed event topic */
    private final static String UPDATED = "com/openexchange/groupware/event/update";

    /** The delete event topic */
    private final static String DELETED = "com/openexchange/groupware/event/delete";

    /** The {@link EventAdmin} to propagate new events through */
    private final EventAdmin eventAdmin;

    /**
     * Initializes a new {@link EventCalendarHandler}.
     *
     * @param eventAdmin The {@link EventAdmin} to propagate events through
     */
    public EventCalendarHandler(EventAdmin eventAdmin) {
        super();
        this.eventAdmin = eventAdmin;
    }

    @Override
    public void handle(CalendarEvent event) {
        if (event == null || event.getAccountId() != CalendarAccount.DEFAULT_ACCOUNT.getAccountId()) {
            return;
        }

        // Check for new events
        for (CreateResult result : event.getCreations()) {
            Event createdEvent = result.getCreatedEvent();
            Map<Integer, Set<Integer>> affectedFoldersPerUser = getAffectedFoldersPerUser(collectFolderIds(createdEvent), event.getAffectedFoldersPerUser());
            if (false == affectedFoldersPerUser.isEmpty()) {
                triggerEvent(new ChronosCommonEvent(event.getSession(), CommonEvent.INSERT, createdEvent, null, affectedFoldersPerUser), CREATED);
            }
        }

        // Check for updated events
        for (UpdateResult result : event.getUpdates()) {
            Map<Integer, Set<Integer>> affectedFoldersPerUser = getAffectedFoldersPerUser(result, event.getAffectedFoldersPerUser());
            if (false == affectedFoldersPerUser.isEmpty()) {
                triggerEvent(new ChronosCommonEvent(event.getSession(), CommonEvent.UPDATE, result.getOriginal(), result.getUpdate(), affectedFoldersPerUser), UPDATED);
            }
        }

        // Check for deleted events
        for (DeleteResult result : event.getDeletions()) {
            Event deletedEvent = result.getOriginal();
            Map<Integer, Set<Integer>> affectedFoldersPerUser = getAffectedFoldersPerUser(collectFolderIds(deletedEvent), event.getAffectedFoldersPerUser());
            if (false == affectedFoldersPerUser.isEmpty()) {
                triggerEvent(new ChronosCommonEvent(event.getSession(), CommonEvent.DELETE, deletedEvent, null, affectedFoldersPerUser), DELETED);
            }
        }
    }

    /**
     * Triggers the OSGi event with given {@link CommonEvent} under the given topic
     *
     * @param chronosEvent The {@link Event} to propagate
     * @param topic The topic of the event
     * @throws OXException In case of missing {@link EventAdmin} service
     */
    private void triggerEvent(CommonEvent chronosEvent, String topic) {
        final Dictionary<String, CommonEvent> ht = new Hashtable<String, CommonEvent>(1);
        ht.put(CommonEvent.EVENT_KEY, chronosEvent);

        final org.osgi.service.event.Event osgievent = new org.osgi.service.event.Event(topic, ht);
        eventAdmin.postEvent(osgievent);
    }

    /**
     * Constructs the effective map of user identifiers associated with a list of those folder identifiers that are actually visible for
     * each user, based on the folder identifiers from the events within the update result.
     *
     * @param updateResult The actual update result to consider
     * @param folderIdsPerUser The overall map of accessible folders per user
     * @return The effective map of affected numerical folder identifiers per user
     */
    private static Map<Integer, Set<Integer>> getAffectedFoldersPerUser(UpdateResult updateResult, Map<Integer, List<String>> foldersPerUser) {
        Map<Integer, Set<Integer>> affectedFoldersPerUser = new HashMap<Integer, Set<Integer>>(foldersPerUser.size());
        /*
         * collect folder ids from original and updated event, then retain those folders that can actually be accessed by each user, and
         * finally check if update is "significant" in one of those folder views
         */
        Set<String> actualFolderIds = collectFolderIds(updateResult.getOriginal(), updateResult.getUpdate());
        for (Entry<Integer, List<String>> entry : foldersPerUser.entrySet()) {
            Integer userId = entry.getKey();
            Set<String> affectedFolderIds = retainAffectedFolderIds(entry.getValue(), actualFolderIds);
            if (false == affectedFolderIds.isEmpty() && isSignificantChange(updateResult, i(userId), affectedFolderIds)) {
                affectedFoldersPerUser.put(userId, asInteger(affectedFolderIds));
            }
        }
        return affectedFoldersPerUser;
    }

    /**
     * Constructs the effective map of user identifiers associated with a list of those folder identifiers that are actually visible for
     * each user, from the supplied list of folder identifiers.
     *
     * @param folderIds The actual folder identifiers to consider
     * @param folderIdsPerUser The overall map of accessible folders per user
     * @return The effective map of affected numerical folder identifiers per user
     */
    private static Map<Integer, Set<Integer>> getAffectedFoldersPerUser(Set<String> folderIds, Map<Integer, List<String>> folderIdsPerUser) {
        /*
         * retain those folders that can actually be accessed by each user
         */
        Map<Integer, Set<Integer>> affectedFoldersPerUser = new HashMap<Integer, Set<Integer>>(folderIdsPerUser.size());
        for (Entry<Integer, List<String>> entry : folderIdsPerUser.entrySet()) {
            Set<String> affectedFolderIds = retainAffectedFolderIds(entry.getValue(), folderIds);
            if (false == affectedFolderIds.isEmpty()) {
                affectedFoldersPerUser.put(entry.getKey(), asInteger(affectedFolderIds));
            }
        }
        return affectedFoldersPerUser;
    }

    /**
     * Creates a new set containing only those folder ids from the supplied collection that are also contained in the specified set of
     * actually affected folder ids, effectively providing the intersection of the two collections.
     *
     * @param folderIds the folder identifiers to filter
     * @param affectedFolderIds The actually affected folder ids
     * @return A new set holding the numerical representation of the affected folder identifiers
     */
    private static Set<String> retainAffectedFolderIds(Collection<String> folderIds, Set<String> affectedFolderIds) {
        Set<String> retainedFolderIds = new HashSet<String>(folderIds);
        retainedFolderIds.retainAll(affectedFolderIds);
        return retainedFolderIds;
    }

    /**
     * Collects the identifiers of all calendar folders the events appear in.
     *
     * @param events The events to collect the folder identifiers for
     * @return The collected folder identifiers
     */
    private static Set<String> collectFolderIds(Event... events) {
        Set<String> folderIds = new HashSet<String>();
        if (null != events) {
            for (Event event : events) {
                if (null != event.getFolderId()) {
                    folderIds.add(event.getFolderId());
                }
                folderIds.addAll(getPersonalFolderIds(event.getAttendees()));
            }
        }
        return folderIds;
    }

}
