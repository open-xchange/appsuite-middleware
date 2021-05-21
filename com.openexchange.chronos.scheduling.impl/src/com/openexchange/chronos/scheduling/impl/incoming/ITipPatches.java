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

package com.openexchange.chronos.scheduling.impl.incoming;

import static com.openexchange.tools.arrays.Collections.isNullOrEmpty;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.AttendeeField;
import com.openexchange.chronos.Calendar;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.Organizer;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.common.mapping.AttendeeMapper;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.exception.ProblemSeverity;
import com.openexchange.chronos.ical.ImportedCalendar;
import com.openexchange.chronos.scheduling.SchedulingMethod;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;

/**
 * {@link ITipPatches}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.6
 */
public class ITipPatches {

    private static final Logger LOG = LoggerFactory.getLogger(ITipPatches.class);

    /**
     * Applies all known patches for an imported iTIP message.
     * 
     * @param importedCalendar The calendar representing the iTIP message
     * @return The (possibly patched) calendar
     */
    public static ImportedCalendar applyAll(ImportedCalendar importedCalendar) {
        if (null == importedCalendar) {
            return importedCalendar;
        }
        List<Event> events = importedCalendar.getEvents();
        if (null == events || events.isEmpty()) {
            return importedCalendar;
        }
        List<OXException> warnings = importedCalendar.getWarnings();
        events = CalendarUtils.sortSeriesMasterFirst(new ArrayList<Event>(events));
        /*
         * apply common patches
         */
        copyCommentToAttendee(events, importedCalendar.getMethod());
        /*
         * apply microsoft-specific patches if applicable
         */
        if (looksLikeMicrosoft(importedCalendar)) {
            SchedulingMethod method = SchedulingMethod.valueOf(importedCalendar.getMethod());
            events = removeOverriddenInstanceLeftovers(method, events, warnings);
            ensureOrganizer(events);
            ensureAtteendees(events);
        }
        /*
         * initialize & return new calendar based on patched events
         */
        Calendar patchedCalendar = new Calendar(importedCalendar);
        patchedCalendar.setEvents(events);
        return new ImportedCalendar(patchedCalendar, warnings);
    }

    /**
     * Copies the comment for an event to the corresponding attendee in case of
     * an {@link SchedulingMethod#REPLY}
     *
     * @param events The events to copy the comment from
     * @param method The calendar method
     */
    private static void copyCommentToAttendee(List<Event> events, String method) {
        if (false == SchedulingMethod.REPLY.name().equals(method.toUpperCase())) {
            return;
        }
        for (Event event : events) {
            /*
             * Check if applicable
             */
            if (null == event.getAttendees() // @formatter:off
                || event.getAttendees().size() != 1
                || null == event.getExtendedProperties()
                || null == event.getExtendedProperties().get("COMMENT")) {  // @formatter:on
                return;
            }
            /*
             * Move comment to the corresponding replying attendee and remove from event
             */
            Attendee replyingAttendee = event.getAttendees().get(0);
            Object comment = event.getExtendedProperties().get("COMMENT").getValue();
            if (null != comment && Strings.isNotEmpty(comment.toString())) {
                replyingAttendee.setComment(comment.toString());
            }
            event.getExtendedProperties().removeAll("COMMENT");
        }
    }

    /**
     * Removes leftovers from overridden instances of a recurring event series, that sometimes are sent by Microsoft to attendees that
     * were previously invited to single instances only. These incomplete event neither contain the organizer or attendees, and have an
     * incorrect recurrence id value set to the 00:00 local time.
     * <p/>
     * <b>Example:</b>
     * <pre>
     * BEGIN:VEVENT
     * SUMMARY:
     * DTSTART;TZID=W. Europe Standard Time:20210420T090000
     * DTEND;TZID=W. Europe Standard Time:20210420T093000
     * UID:040000008200E00074C5B7101A82E00800000000EC4CA982E931D701000000000000000
     * 0100000008108FCBB3F72F046A2D505AD100241B8
     * RECURRENCE-ID;TZID=W. Europe Standard Time:20210420T000000
     * CLASS:PUBLIC
     * PRIORITY:5
     * DTSTAMP:20210415T114505Z
     * TRANSP:OPAQUE
     * STATUS:CONFIRMED
     * SEQUENCE:0
     * LOCATION:e
     * X-MICROSOFT-CDO-APPT-SEQUENCE:0
     * X-MICROSOFT-CDO-BUSYSTATUS:BUSY
     * X-MICROSOFT-CDO-INTENDEDSTATUS:BUSY
     * X-MICROSOFT-CDO-ALLDAYEVENT:FALSE
     * X-MICROSOFT-CDO-IMPORTANCE:1
     * X-MICROSOFT-CDO-INSTTYPE:0
     * X-MICROSOFT-DISALLOW-COUNTER:FALSE
     * END:VEVENT
     * </pre>
     * 
     * @param method The iTIP method as indicated by the imported calendar
     * @param events The events to patch
     * @param warnings A reference to collect warnings
     * @return The (possibly patched) list of events
     */
    private static List<Event> removeOverriddenInstanceLeftovers(SchedulingMethod method, List<Event> events, List<OXException> warnings) {
        if (SchedulingMethod.REQUEST.equals(method) || SchedulingMethod.CANCEL.equals(method)) {
            for (Iterator<Event> iterator = events.iterator(); iterator.hasNext();) {
                Event event = iterator.next();
                if (null != event.getRecurrenceId()) {
                    if (null == event.getOrganizer()) {
                        addInvalidDataWarning(warnings, event, EventField.ORGANIZER, "Ignoring overridden instance without organizer");
                        iterator.remove();
                    } else if (isNullOrEmpty(event.getAttendees())) {
                        addInvalidDataWarning(warnings, event, EventField.ATTENDEES, "Ignoring overridden instance without attendees");
                        iterator.remove();
                    } else if (Strings.isEmpty(event.getUid())) {
                        addInvalidDataWarning(warnings, event, EventField.UID, "Ignoring overridden instance without uid");
                        iterator.remove();
                    }
                }
            }
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
     */
    private static void ensureAtteendees(List<Event> events) {
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
        List<Attendee> copy;
        try {
            copy = AttendeeMapper.getInstance().copy(attendees, (AttendeeField[]) null);
        } catch (OXException e) {
            LOG.warn("Unexpected error copying attendees", e);
            return;
        }
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

    private static void addInvalidDataWarning(List<OXException> warnings, Event event, EventField field, String message) {
        String id = event.getUid() + " | " + event.getRecurrenceId();
        OXException warning = CalendarExceptionCodes.IGNORED_INVALID_DATA.create(id, field, ProblemSeverity.NORMAL, message);
        LOG.debug("Patching invalid data in imported calendar for event {}: {}", id, message, warning);
        warnings.add(warning);
    }

}
