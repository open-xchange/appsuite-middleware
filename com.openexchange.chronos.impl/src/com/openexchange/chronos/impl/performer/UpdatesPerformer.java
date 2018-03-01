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

import static com.openexchange.chronos.common.CalendarUtils.getFields;
import static com.openexchange.chronos.common.SearchUtils.getSearchTerm;
import static com.openexchange.chronos.impl.Check.requireCalendarPermission;
import static com.openexchange.chronos.impl.Utils.getCalendarUserId;
import static com.openexchange.chronos.impl.Utils.getFolder;
import static com.openexchange.chronos.impl.Utils.getFolderIdTerm;
import static com.openexchange.chronos.impl.Utils.isEnforceDefaultAttendee;
import static com.openexchange.chronos.impl.Utils.isIncludeClassifiedEvents;
import static com.openexchange.folderstorage.Permission.NO_PERMISSIONS;
import static com.openexchange.folderstorage.Permission.READ_FOLDER;
import static com.openexchange.folderstorage.Permission.READ_OWN_OBJECTS;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
import java.util.List;
import com.openexchange.chronos.AttendeeField;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.common.DefaultUpdatesResult;
import com.openexchange.chronos.impl.CalendarFolder;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.SearchOptions;
import com.openexchange.chronos.service.UpdatesResult;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.exception.OXException;
import com.openexchange.search.CompositeSearchTerm;
import com.openexchange.search.CompositeSearchTerm.CompositeOperation;
import com.openexchange.search.SearchTerm;
import com.openexchange.search.SingleSearchTerm.SingleOperation;

/**
 * {@link UpdatesPerformer}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class UpdatesPerformer extends AbstractQueryPerformer {

    /**
     * Initializes a new {@link UpdatesPerformer}.
     *
     * @param session The calendar session
     * @param storage The underlying calendar storage
     */
    public UpdatesPerformer(CalendarSession session, CalendarStorage storage) throws OXException {
        super(session, storage);
    }

    /**
     * Performs the operation.
     *
     * @param timestamp The timestamp since when the updates should be collected
     * @return The update result holding the new, modified and deleted events as requested
     */
    public UpdatesResult perform(long timestamp) throws OXException {
        /*
         * search for events the current session's user attends
         */
        SearchTerm<?> searchTerm = getSearchTerm(AttendeeField.ENTITY, SingleOperation.EQUALS, I(session.getUserId()));
        if (false == isEnforceDefaultAttendee(session)) {
            /*
             * also include not group-scheduled events associated with the calendar user
             */
            searchTerm = new CompositeSearchTerm(CompositeOperation.OR)
                .addSearchTerm(getSearchTerm(EventField.CALENDAR_USER, SingleOperation.EQUALS, I(session.getUserId())))
                .addSearchTerm(searchTerm);
        }
        /*
         * ... modified after supplied date
         */
        searchTerm = new CompositeSearchTerm(CompositeOperation.AND)
            .addSearchTerm(getSearchTerm(EventField.TIMESTAMP, SingleOperation.GREATER_THAN, L(timestamp)))
            .addSearchTerm(searchTerm);
        /*
         * perform search & userize the results for the current session's user
         */
        String[] ignore = session.get(CalendarParameters.PARAMETER_IGNORE, String[].class);
        EventField[] fields = getFields(session, EventField.ATTENDEES, EventField.ORGANIZER);
        List<Event> newAndModifiedEvents = null;
        if (false == com.openexchange.tools.arrays.Arrays.contains(ignore, "changed")) {
            List<Event> events = storage.getEventStorage().searchEvents(searchTerm, new SearchOptions(session), fields);
            storage.getUtilities().loadAdditionalEventData(session.getUserId(), events, fields);
            newAndModifiedEvents = postProcess(events, session.getUserId(), true, fields);
        }
        List<Event> deletedEvents = null;
        if (false == com.openexchange.tools.arrays.Arrays.contains(ignore, "deleted")) {
            List<Event> events = storage.getEventStorage().searchEventTombstones(searchTerm, new SearchOptions(session), fields);
            storage.getUtilities().loadAdditionalEventTombstoneData(events, fields);
            deletedEvents = postProcess(events, session.getUserId(), true, fields);
        }
        return new DefaultUpdatesResult(newAndModifiedEvents, deletedEvents);
    }

    /**
     * Performs the operation.
     *
     * @param folderId The identifier of the parent folder to get the event from
     * @param since The timestamp since when the updates should be collected
     * @return The update result holding the new, modified and deleted events as requested
     */
    public UpdatesResult perform(String folderId, long since) throws OXException {
        CalendarFolder folder = getFolder(session, folderId);
        requireCalendarPermission(folder, READ_FOLDER, READ_OWN_OBJECTS, NO_PERMISSIONS, NO_PERMISSIONS);
        /*
         * construct search term
         */
        CompositeSearchTerm searchTerm = new CompositeSearchTerm(CompositeOperation.AND)
            .addSearchTerm(getFolderIdTerm(session, folder))
            .addSearchTerm(getSearchTerm(EventField.TIMESTAMP, SingleOperation.GREATER_THAN, L(since))
        );
        /*
         * perform search & userize the results based on the requested folder
         */
        String[] ignore = session.get(CalendarParameters.PARAMETER_IGNORE, String[].class);
        EventField[] fields = getFields(session, EventField.ATTENDEES, EventField.ORGANIZER);
        List<Event> newAndModifiedEvents = null;
        if (false == com.openexchange.tools.arrays.Arrays.contains(ignore, "changed")) {
            List<Event> events = storage.getEventStorage().searchEvents(searchTerm, new SearchOptions(session), fields);
            storage.getUtilities().loadAdditionalEventData(getCalendarUserId(folder), events, fields);
            newAndModifiedEvents = postProcess(events, folder, isIncludeClassifiedEvents(session), fields);
        }
        List<Event> deletedEvents = null;
        if (false == com.openexchange.tools.arrays.Arrays.contains(ignore, "deleted")) {
            List<Event> events = storage.getEventStorage().searchEventTombstones(searchTerm, new SearchOptions(session), fields);
            storage.getUtilities().loadAdditionalEventTombstoneData(events, fields);
            Boolean oldExpandOccurrences = session.get(CalendarParameters.PARAMETER_EXPAND_OCCURRENCES, Boolean.class);
            try {
                session.set(CalendarParameters.PARAMETER_EXPAND_OCCURRENCES, Boolean.FALSE);
                deletedEvents = postProcess(events, folder, isIncludeClassifiedEvents(session), fields);
            } finally {
                session.set(CalendarParameters.PARAMETER_EXPAND_OCCURRENCES, oldExpandOccurrences);
            }
        }
        return new DefaultUpdatesResult(newAndModifiedEvents, deletedEvents);
    }

}
