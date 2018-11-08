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

package com.openexchange.chronos.provider.caching.basic;

import static com.openexchange.chronos.common.CalendarUtils.optTimeZone;
import static com.openexchange.tools.arrays.Arrays.contains;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.AlarmTrigger;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.common.AlarmPreparator;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.common.DefaultCalendarEvent;
import com.openexchange.chronos.common.DefaultCalendarResult;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.basic.BasicCalendarAccess;
import com.openexchange.chronos.provider.caching.internal.Services;
import com.openexchange.chronos.provider.common.AlarmHelper;
import com.openexchange.chronos.provider.extensions.PersonalAlarmAware;
import com.openexchange.chronos.service.CalendarEventNotificationService;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.CalendarResult;
import com.openexchange.chronos.service.CalendarUtilities;
import com.openexchange.chronos.service.EventID;
import com.openexchange.chronos.service.EventUpdate;
import com.openexchange.chronos.service.SearchFilter;
import com.openexchange.chronos.service.SearchOptions;
import com.openexchange.chronos.service.UpdateResult;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.exception.OXException;
import com.openexchange.java.util.TimeZones;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * {@link BasicAlarmAwareCachingCalendarAccess}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.1
 */
public abstract class BasicAlarmAwareCachingCalendarAccess extends BasicCachingCalendarAccess implements PersonalAlarmAware {

    private final CalendarUtilities calendarUtilities;
    private final CalendarEventNotificationService notificationService;

    /**
     * Initializes a new {@link BasicAlarmAwareCachingCalendarAccess}.
     * @param session
     * @param account
     * @param parameters
     */
    protected BasicAlarmAwareCachingCalendarAccess(Session session, CalendarAccount account, CalendarParameters parameters, CalendarUtilities calendarUtilities, CalendarEventNotificationService notificationService) {
        super(session, account, parameters);
        this.calendarUtilities = calendarUtilities;
        this.notificationService = notificationService;
    }

    @Override
    public CalendarResult updateAlarms(EventID eventID, List<Alarm> alarms, long clientTimestamp) throws OXException {
        if (null != eventID.getRecurrenceID()) {
            throw CalendarExceptionCodes.EVENT_RECURRENCE_NOT_FOUND.create(eventID.getObjectID(), eventID.getRecurrenceID());
        }
        Event originalEvent = getEvent(eventID.getObjectID(), eventID.getRecurrenceID());
        AlarmPreparator.getInstance().prepareEMailAlarms(session, calendarUtilities, alarms);
        UpdateResult updateResult = getAlarmHelper(Services.getServiceLookup()).updateAlarms(originalEvent, alarms);
        DefaultCalendarResult result = new DefaultCalendarResult(session, session.getUserId(), FOLDER_ID, null, null == updateResult ? null : Collections.singletonList(updateResult), null);
        return notifyHandlers(result);
    }

    private DefaultCalendarResult notifyHandlers(DefaultCalendarResult result) {
        notificationService.notifyHandlers(new DefaultCalendarEvent(    session.getContextId(),
                                                                        account.getAccountId(),
                                                                        session.getUserId(),
                                                                        Collections.singletonMap(session.getUserId(), Collections.singletonList(BasicCalendarAccess.FOLDER_ID)),
                                                                        result.getCreations(),
                                                                        result.getUpdates(),
                                                                        result.getDeletions(),
                                                                        session,
                                                                        null,
                                                                        parameters));
        return result;
    }

    @Override
    public List<AlarmTrigger> getAlarmTriggers(Set<String> actions) throws OXException {
        Date until = parameters.get(CalendarParameters.PARAMETER_RANGE_END, Date.class);
        return getAlarmHelper(Services.getServiceLookup()).getAlarmTriggers(until, actions);
    }

    /**
     * Callback routine that is invoked after a new account for the calendar provider has been created.
     */
    public void onAccountCreated() throws OXException {
        AlarmHelper alarmHelper = getAlarmHelper(Services.getServiceLookup());
        if (alarmHelper.hasDefaultAlarms()) {
            alarmHelper.insertDefaultAlarms(getEvents());
        }
    }

    @Override
    protected void delete(CalendarStorage calendarStorage, Event originalEvent) throws OXException {
        super.delete(calendarStorage, originalEvent);
        getAlarmHelper().deleteAlarms(originalEvent.getId());
    }

    @Override
    protected void create(CalendarStorage calendarStorage, List<Event> externalEvents) throws OXException {
        super.create(calendarStorage, externalEvents);
        getAlarmHelper().insertDefaultAlarms(externalEvents);
    }

    @Override
    protected void update(CalendarStorage calendarStorage, EventUpdate eventUpdate) throws OXException {
        super.update(calendarStorage, eventUpdate);
        // TODO handle event updates
    }

    public void onAccountUpdated() throws OXException {
        onAccountDeleted();
        onAccountCreated();
    }

    public void onAccountDeleted() throws OXException {
        getAlarmHelper(Services.getServiceLookup()).deleteAllAlarms();
    }

    @Override
    public Event getEvent(String eventId, RecurrenceId recurrenceId) throws OXException {
        return postProcess(super.getEvent(eventId, recurrenceId));
    }

    @Override
    public List<Event> getEvents(List<EventID> eventIDs) throws OXException {
        return postProcess(super.getEvents(eventIDs));
    }

    @Override
    public List<Event> searchEvents(List<SearchFilter> filters, List<String> queries) throws OXException {
        return postProcess(super.searchEvents(filters, queries));
    }

    private Event postProcess(Event event) throws OXException {
        if (contains(getFields(), EventField.ALARMS)) {
            event = getAlarmHelper().applyAlarms(event);
        }
        return event;
    }

    private List<Event> postProcess(List<Event> events) throws OXException {
        if (contains(getFields(), EventField.ALARMS)) {
            events = getAlarmHelper().applyAlarms(events);
        }
        TimeZone timeZone = getTimeZone();
        Date from = getFrom();
        Date until = getUntil();
        for (Iterator<Event> iterator = events.iterator(); iterator.hasNext();) {
            if (false == CalendarUtils.isInRange(iterator.next(), from, until, timeZone)) {
                iterator.remove();
            }
        }
        CalendarUtils.sortEvents(events, new SearchOptions(parameters).getSortOrders(), timeZone);
        return events;
    }

    protected Date getFrom() {
        return parameters.get(CalendarParameters.PARAMETER_RANGE_START, Date.class);
    }

    protected Date getUntil() {
        return parameters.get(CalendarParameters.PARAMETER_RANGE_END, Date.class);
    }

    private TimeZone getTimeZone() throws OXException {
        TimeZone timeZone = null != parameters ? parameters.get(CalendarParameters.PARAMETER_TIMEZONE, TimeZone.class) : null;
        return null != timeZone ? timeZone : optTimeZone(ServerSessionAdapter.valueOf(session).getUser().getTimeZone(), TimeZones.UTC);
    }

    private AlarmHelper getAlarmHelper() throws OXException {
        return getAlarmHelper(Services.getServiceLookup());
    }

    /**
     *
     * @param services The {@link ServiceLookup} to use
     * @return The {@link AlarmHelper}
     * @throws OXException
     */
    protected abstract AlarmHelper getAlarmHelper(ServiceLookup services) throws OXException;

}
