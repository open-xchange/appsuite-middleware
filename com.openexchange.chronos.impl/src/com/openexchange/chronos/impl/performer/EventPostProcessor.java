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

import static com.openexchange.chronos.common.CalendarUtils.getFlags;
import static com.openexchange.chronos.common.CalendarUtils.getFolderView;
import static com.openexchange.chronos.common.CalendarUtils.isSeriesMaster;
import static com.openexchange.chronos.common.CalendarUtils.sortEvents;
import static com.openexchange.chronos.impl.Utils.anonymizeIfNeeded;
import static com.openexchange.chronos.impl.Utils.applyExceptionDates;
import static com.openexchange.chronos.impl.Utils.getCalendarUserId;
import static com.openexchange.chronos.impl.Utils.getFrom;
import static com.openexchange.chronos.impl.Utils.getUntil;
import static com.openexchange.chronos.impl.Utils.isExcluded;
import static com.openexchange.chronos.impl.Utils.isResolveOccurrences;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.common.DefaultEventsResult;
import com.openexchange.chronos.common.SelfProtectionFactory;
import com.openexchange.chronos.common.SelfProtectionFactory.SelfProtection;
import com.openexchange.chronos.impl.CalendarFolder;
import com.openexchange.chronos.impl.Check;
import com.openexchange.chronos.impl.Utils;
import com.openexchange.chronos.impl.osgi.Services;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.EventsResult;
import com.openexchange.chronos.service.SearchOptions;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.exception.OXException;
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
    private final boolean skipClassified;
    private final List<Event> events;

    private SelfProtection selfProtection;
    private long maximumTimestamp;

    /**
     * Initializes a new {@link EventPostProcessor}.
     *
     * @param storage The underlying calendar storage
     * @param session The calendar session
     * @param skipClassified <code>true</code> to skip events that are <i>classified</i> for the user, <code>false</code>, otherwise
     */
    public EventPostProcessor(CalendarSession session, CalendarStorage storage, boolean skipClassified) {
        super();
        this.session = session;
        this.storage = storage;
        this.events = new ArrayList<Event>();
        this.requestedFields = session.get(CalendarParameters.PARAMETER_FIELDS, EventField[].class);
        this.skipClassified = skipClassified;
    }

    /**
     * Post-processes a list of events prior returning it to the client. This includes
     * <ul>
     * <li>excluding events that are excluded as per {@link Utils#isExcluded(Event, CalendarSession, boolean)}</li>
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
        int calendarUserId = getCalendarUserId(inFolder);
        for (Event event : events) {
            process(event, inFolder.getId(), calendarUserId);
            checkResultSizeNotExceeded();
        }
        return this;
    }

    /**
     * Post-processes an event prior returning it to the client. This includes
     * <ul>
     * <li>excluding events that are excluded as per {@link Utils#isExcluded(Event, CalendarSession, boolean)}</li>
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
        int calendarUserId = getCalendarUserId(inFolder);
        process(event, inFolder.getId(), calendarUserId);
        checkResultSizeNotExceeded();
        return this;
    }

    /**
     * Post-processes a list of events prior returning it to the client. This includes
     * <ul>
     * <li>excluding events that are excluded as per {@link Utils#isExcluded(Event, CalendarSession, boolean)}</li>
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
            process(event, getFolderView(event, forUser), forUser);
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
     * Gets the maximum timestamp of the processed events.
     *
     * @return The maximum timestamp, or <code>0</code> if none were processed
     */
    public long getMaximumTimestamp() {
        return maximumTimestamp;
    }

    private boolean process(Event event, String folderId, int calendarUserId) throws OXException {
        if (isExcluded(event, session, skipClassified)) {
            return false;
        }
        event.setFolderId(folderId);
        if (null == requestedFields || Arrays.contains(requestedFields, EventField.FLAGS)) {
            event.setFlags(getFlags(event, calendarUserId));
        }
        maximumTimestamp = Math.max(maximumTimestamp, event.getTimestamp());
        event = anonymizeIfNeeded(session, event);
        if (isSeriesMaster(event)) {
            if (isResolveOccurrences(session)) {
                /*
                 * add resolved occurrences; no need to apply individual exception dates here, as a removed attendee can only occur in exceptions
                 */
                return events.addAll(resolveOccurrences(event));
            }
            /*
             * add series master event with 'userized' exception dates
             */
            return events.add(applyExceptionDates(storage, event, calendarUserId));
        }
        return events.add(event);
    }

    private void checkResultSizeNotExceeded() throws OXException {
        Check.resultSizeNotExceeded(getSelfProtection(), events, requestedFields);
    }

    private SelfProtection getSelfProtection() throws OXException {
        if (selfProtection == null) {
            LeanConfigurationService leanConfigurationService = Services.getService(LeanConfigurationService.class);
            selfProtection = SelfProtectionFactory.createSelfProtection(leanConfigurationService);
        }
        return selfProtection;
    }

    private List<Event> resolveOccurrences(Event master) throws OXException {
        Date from = getFrom(session);
        Date until = getUntil(session);
        TimeZone timeZone = session.getEntityResolver().getTimeZone(session.getUserId());
        Iterator<Event> itrerator = Utils.resolveOccurrences(session, master);
        List<Event> list = new ArrayList<Event>();
        while (itrerator.hasNext()) {
            Event event = itrerator.next();
            if (CalendarUtils.isInRange(event, from, until, timeZone)) {
                list.add(event);
                checkResultSizeNotExceeded();
            }
        }
        return list;
    }

}

