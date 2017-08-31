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

package com.openexchange.chronos.impl.performer;

import static com.openexchange.chronos.impl.Check.requireCalendarPermission;
import static com.openexchange.chronos.impl.Check.requireUpToDateTimestamp;
import static com.openexchange.chronos.impl.Utils.getFolderIdTerm;
import static com.openexchange.folderstorage.Permission.DELETE_OWN_OBJECTS;
import static com.openexchange.folderstorage.Permission.NO_PERMISSIONS;
import static com.openexchange.folderstorage.Permission.READ_ALL_OBJECTS;
import static com.openexchange.folderstorage.Permission.READ_FOLDER;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.chronos.Attachment;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.impl.InternalCalendarResult;
import com.openexchange.chronos.impl.Utils;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.SearchOptions;
import com.openexchange.chronos.service.SortOrder;
import com.openexchange.chronos.service.SortOrder.Order;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.search.SearchTerm;

/**
 * {@link ClearPerformer}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class ClearPerformer extends AbstractUpdatePerformer {

    private static final int BATCH_SIZE = 500;

    /**
     * Initializes a new {@link ClearPerformer}.
     *
     * @param storage The underlying calendar storage
     * @param session The calendar session
     * @param folder The calendar folder representing the current view on the events
     */
    public ClearPerformer(CalendarStorage storage, CalendarSession session, UserizedFolder folder) throws OXException {
        super(storage, session, folder);
    }

    /**
     * Performs the deletion of all events in the folder.
     *
     * @param clientTimestamp The client timestamp to catch concurrent modifications
     * @return The result
     */
    public InternalCalendarResult perform(long clientTimestamp) throws OXException {
        /*
         * check current session user's permissions; 'clear' requires access to all events in folder
         */
        requireCalendarPermission(folder, READ_FOLDER, READ_ALL_OBJECTS, NO_PERMISSIONS, DELETE_OWN_OBJECTS);
        /*
         * delete all events in folder in batches
         */
        SearchTerm<?> searchTerm = getFolderIdTerm(folder);
        SearchOptions searchOptions = new SearchOptions().addOrder(SortOrder.getSortOrder(EventField.ID, Order.ASC));
        int deleted = 0;
        do {
            int nextOffset = searchOptions.getOffset() + deleted;
            searchOptions.setLimits(nextOffset, nextOffset + BATCH_SIZE);
            deleted = deleteEvents(searchTerm, searchOptions, clientTimestamp);
        } while (deleted >= BATCH_SIZE);
        /*
         * return calendar result
         */
        return result;
    }

    private int deleteEvents(SearchTerm<?> searchTerm, SearchOptions searchOptions, long clientTimestamp) throws OXException {
        EventField[] fields = Utils.DEFAULT_FIELDS.toArray(new EventField[Utils.DEFAULT_FIELDS.size()]);
        List<Event> originalEvents = storage.getEventStorage().searchEvents(searchTerm, searchOptions, fields);
        if (null == originalEvents || 0 == originalEvents.size()) {
            return 0;
        }
        originalEvents = storage.getUtilities().loadAdditionalEventData(-1, originalEvents, null);
        /*
         * check permissions & prepare tombstone data
         */
        List<String> eventIds = new ArrayList<String>(originalEvents.size());
        Map<String, List<Attachment>> attachmentsByEvent = new HashMap<String, List<Attachment>>();
        List<Event> eventTombstones = new ArrayList<Event>(originalEvents.size());
        Map<String, List<Attendee>> attendeeTombstonesById = new HashMap<String, List<Attendee>>(originalEvents.size());
        for (Event originalEvent : originalEvents) {
            requireDeletePermissions(originalEvent);
            requireUpToDateTimestamp(originalEvent, clientTimestamp);
            eventIds.add(originalEvent.getId());
            eventTombstones.add(storage.getUtilities().getTombstone(originalEvent, timestamp, calendarUserId));
            if (null != originalEvent.getAttendees() && 0 < originalEvent.getAttendees().size()) {
                attendeeTombstonesById.put(originalEvent.getId(), storage.getUtilities().getTombstones(originalEvent.getAttendees()));
            }
            if (null != originalEvent.getAttachments() && 0 < originalEvent.getAttachments().size()) {
                attachmentsByEvent.put(originalEvent.getId(), originalEvent.getAttachments());
            }
        }
        /*
         * insert tombstone data & perform deletion
         */
        storage.getEventStorage().insertEventTombstones(eventTombstones);
        storage.getAttendeeStorage().insertAttendeeTombstones(attendeeTombstonesById);
        storage.getAlarmStorage().deleteAlarms(eventIds);
        storage.getAlarmTriggerStorage().deleteTriggers(eventIds);
        storage.getAttendeeStorage().deleteAttendees(eventIds);
        storage.getEventStorage().deleteEvents(eventIds);
        if (0 < attachmentsByEvent.size()) {
            storage.getAttachmentStorage().deleteAttachments(session.getSession(), Collections.singletonMap(folder.getID(), attachmentsByEvent));
        }
        /*
         * track deletions in result
         */
        for (Event originalEvent : originalEvents) {
            trackDeletion(originalEvent);
        }
        return originalEvents.size();
    }

}
