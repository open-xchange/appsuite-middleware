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

package com.openexchange.chronos.itip.json.action;

import static com.openexchange.chronos.itip.json.action.Utils.convertToResult;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;
import org.json.JSONException;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.AttendeeField;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.Organizer;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.common.DefaultCalendarObjectResource;
import com.openexchange.chronos.common.IncomingSchedulingMessageBuilder;
import com.openexchange.chronos.common.mapping.AttendeeMapper;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.ical.ImportedCalendar;
import com.openexchange.chronos.scheduling.SchedulingBroker;
import com.openexchange.chronos.scheduling.SchedulingMethod;
import com.openexchange.chronos.scheduling.SchedulingSource;
import com.openexchange.chronos.service.CalendarResult;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.CreateResult;
import com.openexchange.chronos.service.UpdateResult;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link IncomingSchedulingAction}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.4
 */
public class IncomingSchedulingAction {

    private final ServiceLookup services;
    private final SchedulingMethod method;

    /**
     * Initializes a new {@link IncomingSchedulingAction}.
     * 
     * @param method The scheduling method to handle
     * @param services The service lookup
     */
    public IncomingSchedulingAction(SchedulingMethod method, ServiceLookup services) {
        super();
        this.method = method;
        this.services = services;
    }

    /**
     * Gets a value indicating whether this instance can handle a calendar update
     *
     * @param calendar The calendar with the update
     * @return <code>true</code> if this instance can handle the update, <code>false</code> otherwise
     */
    public boolean canPerform(ImportedCalendar calendar) {
        return method.name().equalsIgnoreCase(calendar.getMethod());
    }

    /**
     * Tries to apply the designated method by applying updates to the calendar.
     *
     * @param request The request
     * @param calendar The calendar
     * @param session The session
     * @param tz The timezone for the user
     * @return Either an result for the client or
     *         <code>null</code> if the request could not be served, e.g. when the method
     *         in the calendar doesn't match the expected method to handle
     * @throws OXException In case of an error while updating
     */
    public AJAXRequestResult perform(AJAXRequestData request, ImportedCalendar calendar, CalendarSession session, TimeZone tz) throws OXException {
        CalendarResult result = perform(request, calendar, session);
        if (null == result) {
            return null;
        }

        /*
         * Transform results to align with expected API output
         */
        List<Event> updatedEvents = new ArrayList<>();
        for (CreateResult createResult : result.getCreations()) {
            updatedEvents.add(createResult.getCreatedEvent());
        }
        for (UpdateResult updateResult : result.getUpdates()) {
            updatedEvents.add(updateResult.getUpdate());
        }
        try {
            return convertToResult(session.getSession(), tz, updatedEvents);
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e);
        }
    }

    /**
     * Performs the designated method announced set for this instance and performs
     * the corresponding update in the calendar.
     *
     * @param request The request to get the information from
     * @param session The session
     * @return A {@link CalendarResult} of the update or
     *         <code>null</code> to indicate that no processing has been performed
     * @throws OXException In case of error
     * @see <a href="https://tools.ietf.org/html/rfc5546">RFC 5546</a>
     */
    private CalendarResult perform(AJAXRequestData request, ImportedCalendar calendar, CalendarSession session) throws OXException {
        /*
         * Build message and send to scheduling broker for processing
         */
        IncomingSchedulingMessageBuilder builder = IncomingSchedulingMessageBuilder.newBuilder();
        builder.setMethod(method);
        builder.setTargetUser(session.getUserId());
        builder.setResource(new DefaultCalendarObjectResource(purifyEvents(calendar)));
        builder.setSchedulingObject(new IncomingSchedulingMail(services, request, session.getSession()));

        SchedulingBroker schedulingBroker = services.getServiceSafe(SchedulingBroker.class);
        return schedulingBroker.handleIncomingScheduling(session, SchedulingSource.API, builder.build());
    }

    /**
     * Purifies the calendar events based on known client flaws that need to be treated before further
     * processing.
     *
     * @param calendar The calendar to process
     * @return A list of purified events
     * @throws OXException In case no events are found
     */
    private static List<Event> purifyEvents(ImportedCalendar calendar) throws OXException {
        if (null == calendar.getEvents() || calendar.getEvents().isEmpty()) {
            throw CalendarExceptionCodes.UNEXPECTED_ERROR.create("There are no eventes to process.");
        }
        List<Event> events = CalendarUtils.sortSeriesMasterFirst(new LinkedList<>(calendar.getEvents()));
        if (looksLikeMicrosoft(calendar)) {
            /*
             * Special handling for Microsoft behavior
             */
            ensureOrganizer(events);
            ensureAtteendees(events);
        }

        return events;
    }

    /**
     * Adds (if at least present once) the organizer instance to all given events
     *
     * @param events The events to add the organizer to
     */
    private static void ensureOrganizer(List<Event> events) {
        /*
         * Check that the organizer value can and must be added to some events
         */
        if (1 == events.size()) {
            return;
        }
        Optional<Event> organizerEvent = events.stream().filter(e -> null != e.getOrganizer()).findFirst();
        if (false == organizerEvent.isPresent()) {
            return;
        }
        /*
         * Add organizer to events that have no organizer
         */
        Organizer organizer = organizerEvent.get().getOrganizer();
        events.stream().filter(e -> null == e.getOrganizer()).forEach((e) -> e.setOrganizer(new Organizer(organizer)));
    }
    
    /**
     * Adds (if at least present once) attendees(s) to all given events
     *
     * @param events The events to add the attendee(s) to
     * @throws OXException If attendees can't be copied
     */
    private static void ensureAtteendees(List<Event> events) throws OXException {
        /*
         * Check that the attendee value can and must be added to some events
         */
        if (1 == events.size()) {
            return;
        }
        Optional<Event> attendeeEvent = events.stream().filter(e -> null != e.getAttendees() && false == e.getAttendees().isEmpty()).findFirst();
        if (false == attendeeEvent.isPresent()) {
            return;
        }
        /*
         * Add attendees to events that have no attendees
         */
        List<Attendee> attendees = attendeeEvent.get().getAttendees();
        List<Attendee> copy = AttendeeMapper.getInstance().copy(attendees, (AttendeeField[]) null);
        events.stream().filter(e -> null == e.getAttendees()).forEach((e) -> e.setAttendees(copy));
    }

    /**
     * Gets a value indicating whether the imported calendar looks like it was
     * generated by a Microsoft software or not
     *
     * @param calendar The imported calendar
     * @return <code>true</code> if the calendar looks like it was generated with a Microsoft software,
     *         <code>false</code> otherwise
     */
    private static boolean looksLikeMicrosoft(ImportedCalendar calendar) {
        String property = calendar.getProdId();
        return Strings.isNotEmpty(property) && Strings.toLowerCase(property).indexOf("microsoft") >= 0;
    }

}
