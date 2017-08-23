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

package com.openexchange.chronos.provider.caching.internal.handler.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.LoggerFactory;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.provider.caching.internal.handler.CachingHandler;
import com.openexchange.chronos.service.EventID;

/**
 * {@link HandlerHelper} contains utility methods used by the {@link CachingHandler} implementation
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.0
 */
public class HandlerHelper {

    /**
     * Enriches the list of {@link Event}s with its related folder identifier if not existing. If no corresponding {@link EventID} can be found the {@link Event} won't get a folder identifier.
     * 
     * @param events The list to add folder ids
     * @param eventIDs The list containing the folder id
     */
    public static void setFolderId(List<Event> events, final List<EventID> eventIDs) {
        for (Event event : events) {
            if (event.containsFolderId()) {
                continue;
            }
            EventID eventID = getEventIdForEvent(eventIDs, event);
            if (eventID != null) {
                event.setFolderId(eventID.getFolderID());
            }
        }
    }

    protected static EventID getEventIdForEvent(List<EventID> eventIDs, Event event) {
        for (EventID eventID : eventIDs) {
            if (eventID.getObjectID().equals(event.getId()) && eventID.getRecurrenceID().equals(event.getRecurrenceId())) {
                return eventID;
            }
        }
        LoggerFactory.getLogger(HandlerHelper.class).debug("Unable to find an EventID matching the event with object id {} and recurrence id {}", event.getId(), event.getRecurrenceId().toString());
        return null;
    }

    /**
     * Enriches the list of {@link Event}s with the provided folder identifier. If there is already a folder identifier set it will not be overwritten.
     * 
     * @param events The list to add the folder identifier
     * @param folderId The folder identifier to set
     */
    public static void setFolderId(List<Event> events, String folderId) {
        for (Event event : events) {
            if (!event.containsFolderId()) {
                event.setFolderId(folderId);
            }
        }
    }

    /**
     * Sorts the given list of {@link EventID}s based on their folder identifier.
     * 
     * @param events The {@link EventID}s to sort
     * @return Map containing the {@link EventID}s sorted by their folder identifier
     */
    public static Map<String, List<EventID>> sortEventIDsPerFolderId(List<EventID> events) {
        Map<String, List<EventID>> sortedList = new HashMap<String, List<EventID>>();

        for (EventID event : events) {
            if (sortedList.containsKey(event.getFolderID())) {
                List<EventID> list = sortedList.get(event.getFolderID());
                list.add(event);
                continue;
            }
            List<EventID> newList = new ArrayList<>();
            newList.add(event);
            sortedList.put(event.getFolderID(), newList);
        }
        return sortedList;
    }
}
