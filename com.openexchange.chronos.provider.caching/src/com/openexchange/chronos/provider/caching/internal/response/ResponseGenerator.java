/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.chronos.provider.caching.internal.response;

import static com.openexchange.chronos.common.CalendarUtils.getFlags;
import static com.openexchange.chronos.common.CalendarUtils.isInRange;
import static com.openexchange.chronos.common.CalendarUtils.isSeriesMaster;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.provider.caching.basic.BasicCachingCalendarAccess;
import com.openexchange.chronos.provider.caching.internal.Services;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.RecurrenceIterator;
import com.openexchange.chronos.service.RecurrenceService;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.java.util.TimeZones;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * {@link ResponseGenerator}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.0
 */
public abstract class ResponseGenerator {

    private static EventField[] IGNORED_FIELDS = { EventField.ATTACHMENTS };

    protected final BasicCachingCalendarAccess cachedCalendarAccess;

    /**
     * Initializes a new response generator.
     * 
     * @param cachedCalendarAccess The underlying calendar access
     */
    protected ResponseGenerator(BasicCachingCalendarAccess cachedCalendarAccess) {
        super();
        this.cachedCalendarAccess = cachedCalendarAccess;
    }

    /**
     * Post-processes a list of events from the storage. This includes
     * <ul>
     * <li>applying flags to the events</li>
     * <li>resolving occurrences of event series as needed</li>
     * <li>restricting the returned events to the requested range as needed</li>
     * </ul>
     * 
     * @param events The events to post-process
     * @return The post processed events, or an empty list if none are left
     */
    protected List<Event> postProcess(List<Event> events) throws OXException {
        CalendarParameters parameters = cachedCalendarAccess.getParameters();
        Date from = getFrom(parameters);
        Date until = getUntil(parameters);
        TimeZone timeZone = getTimeZone(parameters, cachedCalendarAccess.getSession());
        List<Event> processedEvents = new ArrayList<Event>(events.size());
        for (Event event : events) {
            event.setFlags(getFlags(event, cachedCalendarAccess.getAccount().getUserId()));
            if (isSeriesMaster(event) && Strings.isNotEmpty(event.getRecurrenceRule())) {
                RecurrenceIterator<Event> iterator = Services.getService(RecurrenceService.class).iterateEventOccurrences(event, from, until);
                if (isResolveOccurrences(parameters)) {
                    while (iterator.hasNext()) {
                        Event occurrence = iterator.next();
                        if (isInRange(occurrence, from, until, timeZone)) {
                            processedEvents.add(occurrence);
                        }
                    }
                } else if (iterator.hasNext()) {
                    processedEvents.add(event);
                }
            } else if (isInRange(event, from, until, timeZone)) {
                processedEvents.add(event);
            }
        }
        return processedEvents;
    }

    protected static EventField[] getFields(EventField[] requestedFields, EventField... requiredFields) {
        EventField[] fields = CalendarUtils.getFields(requestedFields, requiredFields);
        fields = com.openexchange.tools.arrays.Arrays.remove(fields, IGNORED_FIELDS);
        return fields;
    }

    protected static boolean isResolveOccurrences(CalendarParameters parameters) {
        return parameters.get(CalendarParameters.PARAMETER_EXPAND_OCCURRENCES, Boolean.class, Boolean.FALSE).booleanValue();
    }

    /**
     * Extracts the "from" date used for range-queries from the parameter {@link CalendarParameters#PARAMETER_RANGE_START}.
     *
     * @param parameters The calendar parameters to evaluate
     * @return The "from" date, or <code>null</code> if not set
     */
    protected static Date getFrom(CalendarParameters parameters) {
        return parameters.get(CalendarParameters.PARAMETER_RANGE_START, Date.class);
    }

    /**
     * Extracts the "until" date used for range-queries from the parameter {@link CalendarParameters#PARAMETER_RANGE_END}.
     *
     * @param parameters The calendar parameters to evaluate
     * @return The "until" date, or <code>null</code> if not set
     */
    protected static Date getUntil(CalendarParameters parameters) {
        return parameters.get(CalendarParameters.PARAMETER_RANGE_END, Date.class);
    }

    /**
     * Gets the timezone valid for the current request, which is either the (possibly overridden) timezone defined via
     * {@link CalendarParameters#PARAMETER_TIMEZONE}, or as fallback, the session user's default timezone.
     *
     * @param parameters The calendar parameters to evaluate
     * @param session The current session
     * @return The timezone
     */
    public static TimeZone getTimeZone(CalendarParameters parameters, Session session) throws OXException {
        TimeZone timeZone = parameters.get(CalendarParameters.PARAMETER_TIMEZONE, TimeZone.class);
        if (null == timeZone) {
            String timeZoneId = ServerSessionAdapter.valueOf(session).getUser().getTimeZone();
            timeZone = CalendarUtils.optTimeZone(timeZoneId, TimeZones.UTC);
        }
        return timeZone;
    }

}
