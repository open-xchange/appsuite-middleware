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

package com.openexchange.chronos.impl.performer;

import static com.openexchange.chronos.common.CalendarUtils.find;
import static com.openexchange.chronos.common.CalendarUtils.getFolderView;
import static com.openexchange.chronos.common.CalendarUtils.isClassifiedFor;
import static com.openexchange.chronos.common.CalendarUtils.isFirstOccurrence;
import static com.openexchange.chronos.common.CalendarUtils.isInRange;
import static com.openexchange.chronos.common.CalendarUtils.isLastOccurrence;
import static com.openexchange.chronos.common.CalendarUtils.isSeriesException;
import static com.openexchange.chronos.common.CalendarUtils.isSeriesMaster;
import static com.openexchange.chronos.common.CalendarUtils.sortEvents;
import static com.openexchange.chronos.impl.Utils.anonymizeIfNeeded;
import static com.openexchange.chronos.impl.Utils.applyExceptionDates;
import static com.openexchange.chronos.impl.Utils.getFolder;
import static com.openexchange.chronos.impl.Utils.getFrom;
import static com.openexchange.chronos.impl.Utils.getTimeZone;
import static com.openexchange.chronos.impl.Utils.getUntil;
import static com.openexchange.chronos.impl.Utils.isResolveOccurrences;
import static com.openexchange.java.Autoboxing.i;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.Classification;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.EventFlag;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.common.DefaultEventsResult;
import com.openexchange.chronos.common.DefaultRecurrenceData;
import com.openexchange.chronos.common.SelfProtectionFactory.SelfProtection;
import com.openexchange.chronos.impl.CalendarFolder;
import com.openexchange.chronos.impl.Check;
import com.openexchange.chronos.impl.Utils;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.EventsResult;
import com.openexchange.chronos.service.RecurrenceData;
import com.openexchange.chronos.service.SearchOptions;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.type.PublicType;
import com.openexchange.tools.arrays.Arrays;

/**
 * {@link EventPostProcessor}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class EventPostProcessor {

    private final CalendarSession session;
    private final CalendarStorage storage;
    private final EventField[] requestedFields;
    private final List<Event> events;
    private final Map<String, RecurrenceData> knownRecurrenceData;
    private final SelfProtection selfProtection;

    private long maximumTimestamp;
    private Map<String, Boolean> attachmentsPerEventId;
    private Map<String, Boolean> alarmTriggersPerEventId;
    private Map<String, Integer> attendeeCountsPerEventId;
    private Map<String, Attendee> userAttendeePerEventId;

    /**
     * Initializes a new {@link EventPostProcessor}.
     *
     * @param storage The underlying calendar storage
     * @param session The calendar session
     * @param selfProtection A reference to the self protection utility
     */
    public EventPostProcessor(CalendarSession session, CalendarStorage storage, SelfProtection selfProtection) {
        super();
        this.session = session;
        this.storage = storage;
        this.selfProtection = selfProtection;
        this.events = new ArrayList<Event>();
        this.requestedFields = session.get(CalendarParameters.PARAMETER_FIELDS, EventField[].class);
        this.knownRecurrenceData = new HashMap<String, RecurrenceData>();
    }

    /**
     * Sets a map holding additional hints to assign the {@link EventFlag#ATTACHMENTS} when processing the events.
     * 
     * @param attachmentsPerEventId A map that associates the identifiers of those events where at least one attachment stored to {@link Boolean#TRUE}
     * @return A self reference
     */
    EventPostProcessor setAttachmentsFlagInfo(Map<String, Boolean> attachmentsPerEventId) {
        this.attachmentsPerEventId = attachmentsPerEventId;
        return this;
    }

    /**
     * Sets a map holding additional hints to assign the {@link EventFlag#ALARMS} when processing the events.
     * 
     * @param alarmTriggersPerEventId A map that associates the identifiers of those events where at least one alarm trigger is stored for the user to {@link Boolean#TRUE}
     * @return A self reference
     */
    EventPostProcessor setAlarmsFlagInfo(Map<String, Boolean> alarmTriggersPerEventId) {
        this.alarmTriggersPerEventId = alarmTriggersPerEventId;
        return this;
    }

    /**
     * Sets a map holding additional hints to assign the {@link EventFlag#SCHEDULED} when processing the events.
     * 
     * @param attendeeCountsPerEventId The number of attendees, mapped to the identifiers of the corresponding events
     * @return A self reference
     */
    EventPostProcessor setScheduledFlagInfo(Map<String, Integer> attendeeCountsPerEventId) {
        this.attendeeCountsPerEventId = attendeeCountsPerEventId;
        return this;
    }

    /**
     * Sets a map holding essential information about the calendar user attendee when processing the events.
     * 
     * @param userAttendeePerEventId The calendar user attendees, mapped to the identifiers of the corresponding events
     * @return A self reference
     */
    EventPostProcessor setUserAttendeeInfo(Map<String, Attendee> userAttendeePerEventId) {
        this.userAttendeePerEventId = userAttendeePerEventId;
        return this;
    }

    /**
     * Post-processes a list of events prior returning it to the client. This includes
     * <ul>
     * <li>excluding or anonymizing events that are classified for the current user</li>
     * <li>excluding events that are not within the requested range</li>
     * <li>applying the folder identifier from the passed folder</li>
     * <li>generate and apply event flags</li>
     * <li>resolving occurrences of the series master event as per {@link Utils#isResolveOccurrences(com.openexchange.chronos.service.CalendarParameters)}</li>
     * <li>apply <i>userized</i> versions of change- and delete-exception dates in the series master event based on the calendar user's actual attendance</li>
     * <li>sorting the resulting event list based on the requested sort options</li>
     * </ul>
     *
     * @param events The events to post-process
     * @param inFolder The parent folder representing the view on the events
     * @return A self reference
     */
    public EventPostProcessor process(Collection<Event> events, CalendarFolder inFolder) throws OXException {
        for (Event event : events) {
            doProcess(event, inFolder);
            checkResultSizeNotExceeded();
        }
        return this;
    }

    /**
     * Post-processes an event prior returning it to the client. This includes
     * <ul>
     * <li>excluding or anonymizing events that are classified for the current user</li>
     * <li>excluding events that are not within the requested range</li>
     * <li>applying the folder identifier from the passed folder</li>
     * <li>generate and apply event flags</li>
     * <li>resolving occurrences of the series master event as per {@link Utils#isResolveOccurrences(com.openexchange.chronos.service.CalendarParameters)}</li>
     * <li>apply <i>userized</i> versions of change- and delete-exception dates in the series master event based on the calendar user's actual attendance</li>
     * <li>sorting the resulting event list based on the requested sort options</li>
     * </ul>
     *
     * @param event The event to post-process
     * @param inFolder The parent folder representing the view on the event
     * @return A self reference
     */
    public EventPostProcessor process(Event event, CalendarFolder inFolder) throws OXException {
        doProcess(event, inFolder);
        checkResultSizeNotExceeded();
        return this;
    }

    /**
     * Post-processes a list of events prior returning it to the client. This includes
     * <ul>
     * <li>excluding or anonymizing events that are classified for the current user</li>
     * <li>excluding events that are not within the requested range</li>
     * <li>selecting the appropriate parent folder identifier for the specific user</li>
     * <li>generate and apply event flags</li>
     * <li>resolving occurrences of the series master event as per {@link Utils#isResolveOccurrences(com.openexchange.chronos.service.CalendarParameters)}</li>
     * <li>apply <i>userized</i> versions of change- and delete-exception dates in the series master event based on the user's actual attendance</li>
     * <li>sorting the resulting event list based on the requested sort options</li>
     * </ul>
     *
     * @param events The events to post-process
     * @param forUser The identifier of the user to apply the parent folder identifier for
     * @param includePrivate <code>true</code> to include private or confidential events in non-private folders, <code>false</code>, otherwise
     * @param fields The event fields to consider, or <code>null</code> if not specified
     * @return A self reference
     */
    public EventPostProcessor process(Collection<Event> events, int forUser) throws OXException {
        for (Event event : events) {
            doProcess(event, getFolder(session, getFolderView(event, forUser), false));
            checkResultSizeNotExceeded();
        }
        return this;
    }

    /**
     * Gets a list of all previously processed events, sorted based on the requested sort options, within an events result structure.
     *
     * @return The events result
     */
    public EventsResult getEventsResult() throws OXException {
        return new DefaultEventsResult(getEvents(), getMaximumTimestamp());
    }

    /**
     * Gets a list of all previously processed events, sorted based on the requested sort options.
     *
     * @return The sorted list of processed events, or an empty list there are none
     */
    public List<Event> getEvents() throws OXException {
        return sortEvents(events, new SearchOptions(session).getSortOrders(), Utils.getTimeZone(session));
    }

    /**
     * Gets the first event of the previously processed events.
     * 
     * @return The first event, or <code>null</code> if there is none
     */
    public Event getFirstEvent() throws OXException {
        if (1 == events.size()) {
            return events.get(0);
        }
        List<Event> events = getEvents();
        return events.isEmpty() ? null : events.get(0);
    }

    /**
     * Gets the maximum timestamp of the processed events.
     *
     * @return The maximum timestamp, or <code>0</code> if none were processed
     */
    public long getMaximumTimestamp() {
        return maximumTimestamp;
    }

    protected boolean doProcess(Event event, CalendarFolder folder) throws OXException {
        if (null != userAttendeePerEventId) {
            /*
             * inject data for attendee of underlying calendar user
             */
            Attendee attendee = userAttendeePerEventId.get(event.getId());
            if (null != attendee) {
                event.setAttendees(Collections.singletonList(attendee));
            }
        }
        if (Classification.PRIVATE.equals(event.getClassification()) && isClassifiedFor(event, session.getUserId())) {
            /*
             * excluded if classified as private for the session user
             */
            return false;
        }
        Attendee attendee = find(event.getAttendees(), folder.getCalendarUserId());
        if (null != attendee && attendee.isHidden()) {
            /*
             * excluded if marked as hidden for the calendar user
             */
            //TODO: public folder?
            return false;
        }
        if (isSeriesMaster(event)) {
            knownRecurrenceData.put(event.getSeriesId(), new DefaultRecurrenceData(event));
        }
        event.setFolderId(folder.getId());
        if (null == requestedFields || Arrays.contains(requestedFields, EventField.FLAGS)) {
            event.setFlags(getFlags(event, folder));
        }
        maximumTimestamp = Math.max(maximumTimestamp, event.getTimestamp());
        event = anonymizeIfNeeded(session, event);
        if (isSeriesMaster(event)) {
            if (isResolveOccurrences(session)) {
                /*
                 * add resolved occurrences; no need to apply individual exception dates here, as a removed attendee can only occur in exceptions
                 */
                return events.addAll(resolveOccurrences(event));
            } else if (false == session.getRecurrenceService().iterateEventOccurrences(event, getFrom(session), getUntil(session)).hasNext()) {
                /*
                 * exclude series master event if there are no occurrences in requested range
                 */
                return false;
            }
            /*
             * apply 'userized' exception dates to series master as requested
             */
            if (null == requestedFields || Arrays.contains(requestedFields, EventField.CHANGE_EXCEPTION_DATES) || 
                Arrays.contains(requestedFields, EventField.DELETE_EXCEPTION_DATES)) {
                return events.add(applyExceptionDates(storage, event, folder.getCalendarUserId()));
            }
            return events.add(event);
        }
        if (null != event.getStartDate() && false == isInRange(event, getFrom(session), getUntil(session), getTimeZone(session))) {
            /*
             * excluded if not in requested range
             */
            return false;
        }
        return events.add(event);
    }

    protected EnumSet<EventFlag> getFlags(Event event, CalendarFolder folder) throws OXException {
        /*
         * get default flags for event data & derive recurrence position info
         */
        EnumSet<EventFlag> flags = CalendarUtils.getFlags(event, folder.getCalendarUserId(), session.getUserId(), PublicType.getInstance().equals(folder.getType()));
        if (isSeriesException(event)) {
            RecurrenceData recurrenceData = optRecurrenceData(event);
            if (null != recurrenceData) {
                if (isLastOccurrence(event.getRecurrenceId(), recurrenceData, session.getRecurrenceService())) {
                    flags.add(EventFlag.LAST_OCCURRENCE);
                }
                if (isFirstOccurrence(event.getRecurrenceId(), recurrenceData, session.getRecurrenceService())) {
                    flags.add(EventFlag.FIRST_OCCURRENCE);
                }
            }
        }
        /*
         * inject additional flags based on available data
         */
        if (null != attachmentsPerEventId && Boolean.TRUE.equals(attachmentsPerEventId.get(event.getId()))) {
            flags.add(EventFlag.ATTACHMENTS);
        }
        if (null != alarmTriggersPerEventId && Boolean.TRUE.equals(alarmTriggersPerEventId.get(event.getId()))) {
            flags.add(EventFlag.ALARMS);
        }
        if (null != attendeeCountsPerEventId) {
            Integer attendeeCount = attendeeCountsPerEventId.get(event.getId());
            if (null != attendeeCount && 1 < i(attendeeCount)) {
                flags.add(EventFlag.SCHEDULED);
            }
        }
        return flags;
    }

    private RecurrenceData optRecurrenceData(Event event) throws OXException {
        String seriesId = event.getSeriesId();
        if (null == seriesId) {
            return null;
        }
        if (RecurrenceData.class.isInstance(event.getRecurrenceId())) {
            return ((RecurrenceData) event.getRecurrenceId());
        }
        RecurrenceData recurrenceData = knownRecurrenceData.get(seriesId);
        if (null == recurrenceData) {
            EventField[] fields = new EventField[] { EventField.RECURRENCE_RULE, EventField.START_DATE, EventField.RECURRENCE_DATES, EventField.CHANGE_EXCEPTION_DATES, EventField.DELETE_EXCEPTION_DATES };
            Event seriesMaster = storage.getEventStorage().loadEvent(seriesId, fields);
            if (null != seriesMaster) {
                recurrenceData = new DefaultRecurrenceData(seriesMaster);
                knownRecurrenceData.put(seriesId, recurrenceData);
            }
        }
        return recurrenceData;
    }

    private void checkResultSizeNotExceeded() throws OXException {
        if (null != selfProtection) {
            Check.resultSizeNotExceeded(selfProtection, events, requestedFields);
        }
    }

    private List<Event> resolveOccurrences(Event master) throws OXException {
        Date from = getFrom(session);
        Date until = getUntil(session);
        TimeZone timeZone = getTimeZone(session);
        Iterator<Event> itrerator = session.getRecurrenceService().iterateEventOccurrences(master, from, until);
        List<Event> list = new ArrayList<Event>();
        while (itrerator.hasNext()) {
            Event event = itrerator.next();
            if (isInRange(event, from, until, timeZone)) {
                list.add(event);
            }
        }
        return list;
    }

}
