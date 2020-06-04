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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.chronos.impl.performer;

import static com.openexchange.chronos.common.CalendarUtils.getFields;
import static com.openexchange.chronos.common.CalendarUtils.getObjectIDs;
import static com.openexchange.chronos.common.SearchUtils.getSearchTerm;
import static com.openexchange.java.Autoboxing.I;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import com.openexchange.chronos.AttendeeField;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.impl.Utils;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.SearchOptions;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.exception.OXException;
import com.openexchange.search.CompositeSearchTerm;
import com.openexchange.search.CompositeSearchTerm.CompositeOperation;
import com.openexchange.search.SearchTerm;
import com.openexchange.search.SingleSearchTerm.SingleOperation;
import net.fortuna.ical4j.model.Date;

/**
 * {@link NeedsActionPerformer}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.4
 */
public class NeedsActionPerformer extends AbstractQueryPerformer {

    /**
     * Initializes a new {@link NeedsActionPerformer}.
     *
     * @param session The calendar session
     * @param storage The underlying calendar storage
     */
    public NeedsActionPerformer(CalendarSession session, CalendarStorage storage) {
        super(session, storage);
    }

    // @formatter:off
    private static final EventField[] QUERY_FIELDS = new EventField[] { EventField.UID, EventField.SUMMARY, EventField.FOLDER_ID, EventField.LOCATION,
        EventField.DESCRIPTION, EventField.ATTACHMENTS, EventField.GEO, EventField.ORGANIZER, EventField.START_DATE, EventField.END_DATE,
        EventField.TRANSP, EventField.RECURRENCE_RULE
    };
    // @formatter:on

    /**
     * Performs the operation by querying all events that technically need-action and prepares them to be reduced to a human readable set of events which means for instance leaving out occurrences that only have changed participant status.
     *
     * @return The events that need user action
     */
    public List<Event> perform() throws OXException {
        List<Event> events = getEventsNeedingAction();

        Map<String, List<Event>> eventsByUID = CalendarUtils.getEventsByUID(events, true);

        List<Event> filteredEvents = reduceEventsNeedingAction(eventsByUID);

        EventField[] requestedFields = session.get(CalendarParameters.PARAMETER_FIELDS, EventField[].class);
        EventField[] queriedFields = getFields(requestedFields, QUERY_FIELDS);
        return postProcessor(getObjectIDs(filteredEvents), session.getUserId(), requestedFields, queriedFields).process(filteredEvents, session.getUserId()).getEvents();
    }

    /**
     * Returns all events that technically are defined to 'NEED-ACTION'
     *
     * @return {@link List} of {@link Event}s that need action
     * @throws OXException
     */
    private List<Event> getEventsNeedingAction() throws OXException {
        SearchTerm<?> searchTerm = new CompositeSearchTerm(CompositeOperation.AND).addSearchTerm(getSearchTerm(AttendeeField.ENTITY, SingleOperation.EQUALS, I(session.getUserId()))).addSearchTerm(getSearchTerm(AttendeeField.PARTSTAT, SingleOperation.EQUALS, ParticipationStatus.NEEDS_ACTION));
        EventField[] queriedFields = getFields(session.get(CalendarParameters.PARAMETER_FIELDS, EventField[].class), QUERY_FIELDS);
        List<Event> events = storage.getEventStorage().searchEvents(searchTerm, new SearchOptions(session), queriedFields);
        return storage.getUtilities().loadAdditionalEventData(session.getUserId(), events, queriedFields);
    }

    /**
     * Reduces the technically based list of events that need action to a minimum list that really needs action by a user.
     * <p/>
     * If multiple overridden instances of a recurring event series could be reduced into the series master event, this series master
     * event's change exception dates are adjusted implicitly so that they no longer list those exception dates that were skipped.
     *
     * @param eventsByUID {@link Map} containing all events in a mapped way which means each event series including any change exceptions are grouped separately.
     * @return {@link List} of {@link Event}s that need user action
     * @throws OXException
     */
    protected List<Event> reduceEventsNeedingAction(Map<String, List<Event>> eventsByUID) throws OXException {
        if (eventsByUID == null || eventsByUID.size() == 0) {
            return Collections.emptyList();
        }
        List<Event> filteredEvents = new ArrayList<Event>();
        for (List<Event> series : eventsByUID.values()) {
            if (series == null || series.size() == 0) {
                continue;
            }
            if (series.size() == 1) {
                filteredEvents.add(series.get(0));
                continue;
            }
            List<Event> sortedSeries = CalendarUtils.sortSeriesMasterFirst(series);
            Event master = sortedSeries.get(0);
            if (false == CalendarUtils.isSeriesMaster(master)) {
                filteredEvents.addAll(series);
                continue;
            }

            SortedSet<RecurrenceId> newChangeExceptionDates = new TreeSet<RecurrenceId>();
            if (null != master.getChangeExceptionDates()) {
                newChangeExceptionDates.addAll(master.getChangeExceptionDates());
            }
            Date timestamp = new Date();
            for (int i = 1; i < sortedSeries.size(); i++) {
                Event event = sortedSeries.get(i);
                Event originalOccurrence = prepareException(master, event.getRecurrenceId(), event.getId(), timestamp);
                /*
                 * include re-scheduled change exceptions, otherwise skip & remove from master's change exception dates
                 */
                if (Utils.isReschedule(originalOccurrence, event)) {
                    filteredEvents.add(event);
                } else {
                    newChangeExceptionDates.removeIf(r -> r.matches(event.getRecurrenceId()));
                }
            }
            master.setChangeExceptionDates(newChangeExceptionDates);
            filteredEvents.add(master);
        }
        return filteredEvents;
    }
}
