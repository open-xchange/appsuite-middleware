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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.data.conversion.ical;

import static com.openexchange.groupware.calendar.tools.CommonAppointments.D;
import static com.openexchange.groupware.calendar.tools.CommonAppointments.recalculate;
import com.openexchange.groupware.calendar.CalendarDataObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import junit.framework.TestCase;

import com.openexchange.data.conversion.ical.ical4j.ICal4JParser;
import com.openexchange.data.conversion.ical.ical4j.internal.calendar.Participants;
import com.openexchange.data.conversion.ical.ical4j.internal.UserResolver;
import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.ExternalUserParticipant;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.ResourceParticipant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextImpl;
import com.openexchange.groupware.ldap.LdapException;
import com.openexchange.groupware.ldap.MockUserLookup;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.tasks.Task;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class ICalParserTest extends TestCase {

    private ICALFixtures fixtures;
    private ICalParser parser;
    private MockUserLookup users;

    // Appointments

    public void testAppStartToEnd() {

        Date start = D("24/02/1981 10:00");
        Date end = D("24/02/1981 12:00");

        TimeZone utc = TimeZone.getTimeZone("UTC");

        // Local Time

        String icalText = fixtures.veventWithLocalDTStartAndDTEnd(start, end);
        AppointmentObject appointment = parseAppointment(icalText, utc);
        
        assertEquals(start, appointment.getStartDate());
        assertEquals(end, appointment.getEndDate());
        assertEquals("UTC", appointment.getTimezone());

        // UTC

        icalText = fixtures.veventWithUTCDTStartAndDTEnd(start, end);
        appointment = parseAppointment(icalText);

        assertEquals(start, appointment.getStartDate());
        assertEquals(end, appointment.getEndDate());
        assertEquals("UTC", appointment.getTimezone());

        // Known TZID

        TimeZone timeZone = TimeZone.getTimeZone("America/Los_Angeles");
        assertFalse("Bad test TimeZone",timeZone.equals(TimeZone.getDefault()));

        icalText = fixtures.veventWithDTStartAndEndInTimeZone(start, end, timeZone);
        appointment = parseAppointment(icalText);

        assertEquals(recalculate(start, utc , timeZone), appointment.getStartDate());
        assertEquals(recalculate(end, utc, timeZone), appointment.getEndDate());
        assertEquals(timeZone.getID(), appointment.getTimezone());

        // VTIMEZONE

        icalText = fixtures.veventWithDTStartAndDTEndInCustomTimezone(start, end);
        appointment = parseAppointment(icalText);

        assertEquals(D("24/02/1981 01:00"), appointment.getStartDate());
        assertEquals(D("24/02/1981 03:00"), appointment.getEndDate());
        assertEquals("UTC", appointment.getTimezone());
    }

    public void testAppDuration() {
        Date start = D("24/02/1981 10:00");
        Date end = D("24/02/1981 12:00");
        String duration = "P2H"; // 2 hours

        TimeZone utc = TimeZone.getTimeZone("UTC");

        String icalText = fixtures.veventWithLocalDTStartAndDuration(start, duration);
        AppointmentObject appointment = parseAppointment(icalText, utc);

        assertEquals(start, appointment.getStartDate());
        assertEquals(end, appointment.getEndDate());
        assertEquals("UTC", appointment.getTimezone());

    }

    public void testAppPrivateFlag() {
        Date start = D("24/02/1981 10:00");
        Date end = D("24/02/1981 12:00");

        TimeZone utc = TimeZone.getTimeZone("UTC");

        // Private

        String icalText = fixtures.veventWithSimpleProperties(start, end, "CLASS", "private");
        AppointmentObject appointment = parseAppointment(icalText, utc);

        assertTrue(appointment.getPrivateFlag());

        // Public
        icalText = fixtures.veventWithSimpleProperties(start, end, "CLASS", "public");
        appointment = parseAppointment(icalText, utc);
        assertFalse(appointment.getPrivateFlag());

    }

    public void testAppNote() {
        Date start = D("24/02/1981 10:00");
        Date end = D("24/02/1981 12:00");

        TimeZone utc = TimeZone.getTimeZone("UTC");


        String icalText = fixtures.veventWithSimpleProperties(start, end, "DESCRIPTION", "A fine description");
        AppointmentObject appointment = parseAppointment(icalText, utc);

        assertEquals("A fine description", appointment.getNote());

        // TODO Test encodings

    }

    public void testAppLocation() {
        Date start = D("24/02/1981 10:00");
        Date end = D("24/02/1981 12:00");

        TimeZone utc = TimeZone.getTimeZone("UTC");


        String icalText = fixtures.veventWithSimpleProperties(start, end, "LOCATION", "Mars");
        AppointmentObject appointment = parseAppointment(icalText, utc);

        assertEquals("Mars", appointment.getLocation());
    }

    public void testAppTitle() {
        Date start = D("24/02/1981 10:00");
        Date end = D("24/02/1981 12:00");

        TimeZone utc = TimeZone.getTimeZone("UTC");

        String icalText = fixtures.veventWithSimpleProperties(start, end, "SUMMARY", "A fine title");
        AppointmentObject appointment = parseAppointment(icalText, utc);

        assertEquals("A fine title", appointment.getTitle());
    }

    public void testAppReserved() {
        Date start = D("24/02/1981 10:00");
        Date end = D("24/02/1981 12:00");

        TimeZone utc = TimeZone.getTimeZone("UTC");

        String icalText = fixtures.veventWithSimpleProperties(start, end, "TRANSP", "OPAQUE");
        AppointmentObject appointment = parseAppointment(icalText, utc);

        assertEquals(AppointmentObject.RESERVED, appointment.getShownAs());
    }

    public void testAppFree() {
        Date start = D("24/02/1981 10:00");
        Date end = D("24/02/1981 12:00");

        TimeZone utc = TimeZone.getTimeZone("UTC");

        String icalText = fixtures.veventWithSimpleProperties(start, end, "TRANSP", "TRANSPARENT");
        AppointmentObject appointment = parseAppointment(icalText, utc);

        assertEquals(AppointmentObject.FREE, appointment.getShownAs());
    }

    public void testAppAttendees() {
        Date start = D("24/02/1981 10:00");
        Date end = D("24/02/1981 12:00");

        TimeZone utc = TimeZone.getTimeZone("UTC");

        String[] mails = new String[3];
        int i = 0;
        for(User user : U(1,2,5)) {
            mails[i++] = user.getMail();
        }

        // Internal Users

        String icalText = fixtures.veventWithAttendees(start, end, mails);
        AppointmentObject appointment = parseAppointment(icalText, utc);

        Set<Integer> ids = new HashSet<Integer>(Arrays.asList(1,2,5));
        assertNotNull(appointment.getParticipants());
        for(Participant p  : appointment.getParticipants()) {
            assertTrue(UserParticipant.class.isAssignableFrom(p.getClass()));
            UserParticipant participant = (UserParticipant) p;
            assertTrue(ids.remove(participant.getIdentifier()));
        }
        assertTrue(ids.isEmpty());
        

        // TODO: Status ?

        // External Users

        mails = new String[]{"mickey@disney.invalid", "donald@disney.invalid", "goofy@disney.invalid"};
        icalText = fixtures.veventWithAttendees(start, end, mails);
        appointment = parseAppointment(icalText, utc);


        Set<String> mailSet = new HashSet<String>(Arrays.asList(mails));
        assertNotNull(appointment.getParticipants());
        for(Participant p  : appointment.getParticipants()) {
            assertTrue(ExternalUserParticipant.class.isAssignableFrom(p.getClass()));
            ExternalUserParticipant participant = (ExternalUserParticipant) p;
            assertTrue(mailSet.remove(participant.getEmailAddress()));
        }
        assertTrue(mailSet.isEmpty());

    }

    public void testAppResources() {
        //FIXME: This is a bit fishy. Only DisplayNames are involved, resources are not resolved.

        Date start = D("24/02/1981 10:00");
        Date end = D("24/02/1981 12:00");

        TimeZone utc = TimeZone.getTimeZone("UTC");

        String[] resources = new String[]{"Toaster", "Deflector", "Subspace Anomaly"};

        String icalText = fixtures.veventWithResources(start, end, resources);
        AppointmentObject appointment = parseAppointment(icalText, utc);

        Set<String> resourceSet = new HashSet<String>(Arrays.asList(resources));

        assertNotNull(appointment.getParticipants());
        for(Participant p  : appointment.getParticipants()) {
            assertTrue(ResourceParticipant.class.isAssignableFrom(p.getClass()));
            ResourceParticipant participant = (ResourceParticipant) p;
            assertTrue(resourceSet.remove(participant.getDisplayName()));
        }

        assertTrue(resourceSet.isEmpty());
        
    }

    public void testAppCategories() {
        Date start = D("24/02/1981 10:00");
        Date end = D("24/02/1981 12:00");

        TimeZone utc = TimeZone.getTimeZone("UTC");

        String[] categories = new String[]{"Toaster", "Deflector", "Subspace Anomaly"};
        String categoriesString = "Toaster,Deflector,Subspace Anomaly";

        String icalText = fixtures.veventWithCategories(start, end, categories);
        AppointmentObject appointment = parseAppointment(icalText, utc);

        assertEquals(categoriesString, appointment.getCategories());
    }

    public void testAppRecurrence() {

        Date start = D("24/02/1981 10:00");
        Date end =   D("24/02/1981 12:00");

        // DAILY

        AppointmentObject appointment = appointmentWithRecurrence("FREQ=DAILY;INTERVAL=2;COUNT=3", start, end);
        assertEquals(3, appointment.getRecurrenceCount());
        assertEquals(AppointmentObject.DAILY, appointment.getRecurrenceType());
        assertEquals(2, appointment.getInterval());

        // WEEKLY

        appointment = appointmentWithRecurrence("FREQ=WEEKLY;INTERVAL=2;COUNT=3;BYDAY=MO,WE,FR", start, end);

        assertEquals(3, appointment.getRecurrenceCount());
        assertEquals(AppointmentObject.WEEKLY, appointment.getRecurrenceType());
        assertEquals(2, appointment.getInterval());

        int days = appointment.getDays();
        assertTrue(AppointmentObject.MONDAY == (AppointmentObject.MONDAY & days));
        assertTrue(AppointmentObject.WEDNESDAY == (AppointmentObject.WEDNESDAY & days));
        assertTrue(AppointmentObject.FRIDAY == (AppointmentObject.FRIDAY & days));

        assertFalse(AppointmentObject.TUESDAY == (AppointmentObject.TUESDAY & days));
        assertFalse(AppointmentObject.THURSDAY == (AppointmentObject.THURSDAY & days));
        assertFalse(AppointmentObject.SATURDAY == (AppointmentObject.SATURDAY & days));
        assertFalse(AppointmentObject.SUNDAY == (AppointmentObject.SUNDAY & days));


        // Default Day taken from DTSTART

        appointment = appointmentWithRecurrence("FREQ=WEEKLY;INTERVAL=2;COUNT=3", start, end);
        days = appointment.getDays();

        assertEquals(3, appointment.getRecurrenceCount());
        assertEquals(AppointmentObject.WEEKLY, appointment.getRecurrenceType());
        assertEquals(2, appointment.getInterval());
        assertTrue(AppointmentObject.TUESDAY == (AppointmentObject.TUESDAY & days)); // Start Date is a Tuesday

        // MONTHLY

        // First form: on 23rd day every 2 months

        appointment = appointmentWithRecurrence("FREQ=MONTHLY;INTERVAL=2;COUNT=3;BYMONTHDAY=23", start, end);
        assertEquals(3, appointment.getRecurrenceCount());
        assertEquals(AppointmentObject.MONTHLY, appointment.getRecurrenceType());
        assertEquals(2, appointment.getInterval());
        assertEquals(23, appointment.getDayInMonth());

        // Second form : the 2nd monday and tuesday every 2 months

        appointment = appointmentWithRecurrence("FREQ=MONTHLY;INTERVAL=2;COUNT=3;BYDAY=MO,TU;BYWEEKNO=3", start, end);
        days = appointment.getDays();
        assertEquals(3, appointment.getDayInMonth());
        assertTrue(AppointmentObject.MONDAY == (AppointmentObject.MONDAY & days));
        assertTrue(AppointmentObject.TUESDAY == (AppointmentObject.TUESDAY & days)); // Is this correct? Can an appintment recur on more than one day in MONTHLY series?


        // Second form : the last tuesday every 2 months

        appointment = appointmentWithRecurrence("FREQ=MONTHLY;INTERVAL=2;COUNT=3;BYDAY=TU;BYWEEKNO=-1", start, end);
        days = appointment.getDays();
        assertEquals(5, appointment.getDayInMonth());
        assertTrue(AppointmentObject.TUESDAY == (AppointmentObject.TUESDAY & days));


        // Default taken from start date

        appointment = appointmentWithRecurrence("FREQ=MONTHLY;INTERVAL=2;COUNT=3", start, end);
        assertEquals(24, appointment.getDayInMonth());

        appointment = appointmentWithRecurrence("FREQ=MONTHLY;INTERVAL=2;COUNT=3;BYWEEKNO=3", start, end);
        days = appointment.getDays();
        assertTrue(AppointmentObject.TUESDAY == (AppointmentObject.TUESDAY & days));


        // YEARLY

        // First form: Every 2 years, the 23rd of March
        appointment = appointmentWithRecurrence("FREQ=YEARLY;INTERVAL=2;COUNT=3;BYMONTHDAY=23;BYMONTH=3", start, end);

        assertEquals(AppointmentObject.YEARLY, appointment.getRecurrenceType());
        assertEquals(2, appointment.getMonth());
        assertEquals(23, appointment.getDayInMonth());

        // Second form: 2nd monday and wednesday in april every 2 years

        appointment = appointmentWithRecurrence("FREQ=YEARLY;INTERVAL=2;COUNT=3;BYDAY=MO,WE;BYMONTH=4;BYWEEKNO=2", start, end);
        days = appointment.getDays();

        assertEquals(3, appointment.getMonth());
        assertEquals(2, appointment.getDayInMonth());
        assertTrue(AppointmentObject.MONDAY == (AppointmentObject.MONDAY & days));
        assertTrue(AppointmentObject.WEDNESDAY == (AppointmentObject.WEDNESDAY & days)); // Is this correct? Can an appintment recur on more than one day in YEARLY series?


        // Default

        appointment = appointmentWithRecurrence("FREQ=YEARLY;INTERVAL=2;COUNT=3", start, end);

        assertEquals(AppointmentObject.YEARLY, appointment.getRecurrenceType());
        assertEquals(1, appointment.getMonth());
        assertEquals(24, appointment.getDayInMonth());


        // UNTIL

        appointment = appointmentWithRecurrence("FREQ=YEARLY;INTERVAL=2;UNTIL=19890423", start, end);

        assertEquals(D("23/04/1989 00:00"), appointment.getUntil());

    }

    public void testAppDeleteExceptions() {
        Date start = D("24/01/1981 10:00");
        Date end = D("24/01/1981 12:00");

        TimeZone utc = TimeZone.getTimeZone("UTC");

        String rrule = "FREQ=DAILY;INTERVAL=2;COUNT=5";
        Date[] exceptions = new Date[]{D("26/01/1981 12:00"), D("30/01/1981 12:00")};

        String icalText = fixtures.veventWithDeleteExceptionsAsDateTime(start, end, rrule, exceptions);
        AppointmentObject appointment = parseAppointment(icalText, utc);

        // Do we need the "time" in an exception?

        Set<Date> expectedExceptions = new HashSet<Date>(Arrays.asList(exceptions));

        assertNotNull(appointment.getDeleteException());
        for(Date exception  : appointment.getDeleteException()) {
            assertTrue("Didn't expect: "+exception+ " Expected one of: "+expectedExceptions, expectedExceptions.remove(exception));
        }
        assertTrue(expectedExceptions.isEmpty());

        icalText = fixtures.veventWithDeleteExceptionsAsDate(start, end, rrule, exceptions);
        
        appointment = parseAppointment(icalText, utc);


        expectedExceptions = new HashSet<Date>(Arrays.asList(D("26/01/1981 00:00"), D("30/01/1981 00:00")));


        assertNotNull(appointment.getDeleteException());
        for(Date exception  : appointment.getDeleteException()) {
            assertTrue("Didn't expect: "+exception+ " Expected one of: "+expectedExceptions, expectedExceptions.remove(exception));
        }
        assertTrue(expectedExceptions.isEmpty());
        assertEquals("UTC", appointment.getTimezone());


    }

    public void testAppAlarms() {
        // Relative to Start (default)

        Date start = D("24/02/1981 10:00");
        Date end = D("24/02/1981 12:00");

        TimeZone utc = TimeZone.getTimeZone("UTC");

        int MINUTES = 60*1000;

        String icalText = fixtures.veventWithDisplayAlarm(start, end, "TRIGGER:-PT15M", "Description");
        AppointmentObject appointment = parseAppointment(icalText, utc);


        assertEquals(15 *MINUTES ,appointment.getAlarm());
        assertTrue(appointment.getAlarmFlag());

        // Relative to Start (explicit)

        icalText = fixtures.veventWithDisplayAlarm(start, end, "TRIGGER;RELATED=START:-PT20M", "Description");
        appointment = parseAppointment(icalText, utc);

        assertEquals(20 *MINUTES ,appointment.getAlarm());
        assertTrue(appointment.getAlarmFlag());


        // Absolute Trigger

        icalText = fixtures.veventWithDisplayAlarm(start, end, "TRIGGER;VALUE=DATE-TIME:19810224T091000", "Description");

        appointment = parseAppointment(icalText, utc);

        assertEquals(50 *MINUTES ,appointment.getAlarm());
        assertTrue(appointment.getAlarmFlag());
    }
    
    // Tasks

    public void testTskTitle() {
        String icalText = fixtures.vtodoWithSimpleProperties("SUMMARY", "A nice title");
        Task task = parseTask(icalText, TimeZone.getTimeZone("UTC"));

        assertEquals("A nice title", task.getTitle());
    }

    public void testTskNote() {
        String icalText = fixtures.vtodoWithSimpleProperties("DESCRIPTION", "A nice description");
        Task task = parseTask(icalText, TimeZone.getTimeZone("UTC"));

        assertEquals("A nice description", task.getNote());
            
    }

    public void testTskStartToEnd() {
        Date start = D("24/02/1981 10:00");
        Date end = D("24/02/1981 12:00");

        TimeZone utc = TimeZone.getTimeZone("UTC");

        // Local Time

        String icalText = fixtures.vtodoWithLocalDTStartAndDTEnd(start, end);
        Task task = parseTask(icalText, utc);

        assertEquals(start, task.getStartDate());
        assertEquals(end, task.getEndDate());

        // UTC

        icalText = fixtures.vtodoWithUTCDTStartAndDTEnd(start, end);
        task = parseTask(icalText);

        assertEquals(start, task.getStartDate());
        assertEquals(end, task.getEndDate());

        // Known TZID

        TimeZone timeZone = TimeZone.getTimeZone("America/Los_Angeles");
        assertFalse("Bad test TimeZone",timeZone.equals(TimeZone.getDefault()));

        icalText = fixtures.vtodoWithDTStartAndEndInTimeZone(start, end, timeZone);
        task = parseTask(icalText);

        assertEquals(recalculate(start, utc , timeZone), task.getStartDate());
        assertEquals(recalculate(end, utc, timeZone), task.getEndDate());

        // VTIMEZONE

        icalText = fixtures.vtodoWithDTStartAndDTEndInCustomTimezone(start, end);
        task = parseTask(icalText);

        assertEquals(D("24/02/1981 01:00"), task.getStartDate());
        assertEquals(D("24/02/1981 03:00"), task.getEndDate());
    }

    public void testTskDuration() {
        Date start = D("24/02/1981 10:00");
        Date end = D("24/02/1981 12:00");
        String duration = "P2H"; // 2 hours

        TimeZone utc = TimeZone.getTimeZone("UTC");

        String icalText = fixtures.vtodoWithLocalDTStartAndDuration(start, duration);
        Task task = parseTask(icalText, utc);

        assertEquals(start, task.getStartDate());
        assertEquals(end, task.getEndDate());

    }

    public void testTskDue() {
        Date due = D("24/03/1981 10:00");

        TimeZone utc = TimeZone.getTimeZone("UTC");

        String icalText = fixtures.vtodoWithDueDate(due);
        Task task = parseTask(icalText, utc);

        assertEquals(due, task.getEndDate());
    }

    public void testTskPrivateFlag() {
        TimeZone utc = TimeZone.getTimeZone("UTC");

        // Private

        String icalText = fixtures.vtodoWithSimpleProperties("CLASS", "private");
        Task task = parseTask(icalText, utc);

        assertTrue(task.getPrivateFlag());

        // Public
        icalText = fixtures.vtodoWithSimpleProperties("CLASS", "public");
        task = parseTask(icalText, utc);
        assertFalse(task.getPrivateFlag());
    }

    public void testTskDateCompleted() {
        Date dateCompleted = D("24/03/1981 10:00");

        TimeZone utc = TimeZone.getTimeZone("UTC");

        String icalText = fixtures.vtodoWithDateCompleted(dateCompleted);
        Task task = parseTask(icalText, utc);

        assertEquals(dateCompleted, task.getDateCompleted());
    }

    public void testTskPercentComplete() {
        String icalText = fixtures.vtodoWithSimpleProperties("PERCENT-COMPLETE", "23");
        Task task = parseTask(icalText);

        assertEquals(23, task.getPercentComplete());
    }

    public void testTskPriority() {
        priorityTest(1, Task.HIGH);
        priorityTest(2, Task.HIGH);
        priorityTest(3, Task.HIGH);
        priorityTest(4, Task.HIGH);
        priorityTest(5, Task.NORMAL);
        priorityTest(6, Task.LOW);
        priorityTest(7, Task.LOW);
        priorityTest(8, Task.LOW);
        priorityTest(9, Task.LOW);

    }

    private void priorityTest(int priority, int expected) {
        String icalText = fixtures.vtodoWithSimpleProperties("PRIORITY", new Integer(priority).toString());
        Task task = parseTask(icalText);
        assertEquals("Invalid interpretation for priority "+priority, expected, task.getPriority());
    }

    public void testTskStatus() {
        statusTest("NEEDS-ACTION", Task.NOT_STARTED);
        statusTest("IN-PROCESS", Task.IN_PROGRESS);
        statusTest("COMPLETED", Task.DONE);
        statusTest("CANCELLED", Task.DEFERRED);
    }

    private void statusTest(String status, int expected) {
        String icalText = fixtures.vtodoWithSimpleProperties("STATUS", status);
        Task task = parseTask(icalText);
        assertEquals("Invalid interpretation for status "+status, expected, task.getStatus());
    }

    public void testTskAttendees() {
        TimeZone utc = TimeZone.getTimeZone("UTC");

        String[] mails = new String[3];
        int i = 0;
        for(User user : U(1,2,5)) {
            mails[i++] = user.getMail();
        }

        // Internal Users

        String icalText = fixtures.vtodoWithAttendees(mails);
        Task appointment = parseTask(icalText, utc);

        Set<Integer> ids = new HashSet<Integer>(Arrays.asList(1,2,5));
        assertNotNull(appointment.getParticipants());
        for(Participant p  : appointment.getParticipants()) {
            assertTrue(UserParticipant.class.isAssignableFrom(p.getClass()));
            UserParticipant participant = (UserParticipant) p;
            assertTrue(ids.remove(participant.getIdentifier()));
        }
        assertTrue(ids.isEmpty());


        // TODO: Status ?

        // External Users

        mails = new String[]{"mickey@disney.invalid", "donald@disney.invalid", "goofy@disney.invalid"};
        icalText = fixtures.vtodoWithAttendees(mails);
        appointment = parseTask(icalText, utc);


        Set<String> mailSet = new HashSet<String>(Arrays.asList(mails));
        assertNotNull(appointment.getParticipants());
        for(Participant p  : appointment.getParticipants()) {
            assertTrue(ExternalUserParticipant.class.isAssignableFrom(p.getClass()));
            ExternalUserParticipant participant = (ExternalUserParticipant) p;
            assertTrue(mailSet.remove(participant.getEmailAddress()));
        }
        assertTrue(mailSet.isEmpty());
        
    }

    public void testTskCategories() {
        TimeZone utc = TimeZone.getTimeZone("UTC");

        String[] categories = new String[]{"Toaster", "Deflector", "Subspace Anomaly"};
        String categoriesString = "Toaster,Deflector,Subspace Anomaly";

        String icalText = fixtures.vtodoWithCategories(categories);
        Task task = parseTask(icalText, utc);

        assertEquals(categoriesString, task.getCategories());
    }

    public void testTskRecurrence() {
        Date start = D("24/02/1981 10:00");
        Date end =   D("24/02/1981 12:00");

        // DAILY

        Task task = taskWithRecurrence("FREQ=DAILY;INTERVAL=2;COUNT=3", start, end);
        assertEquals(3, task.getRecurrenceCount());
        assertEquals(CalendarObject.DAILY, task.getRecurrenceType());
        assertEquals(2, task.getInterval());

        // WEEKLY

        task = taskWithRecurrence("FREQ=WEEKLY;INTERVAL=2;COUNT=3;BYDAY=MO,WE,FR", start, end);

        assertEquals(3, task.getRecurrenceCount());
        assertEquals(CalendarObject.WEEKLY, task.getRecurrenceType());
        assertEquals(2, task.getInterval());

        int days = task.getDays();
        assertTrue(CalendarObject.MONDAY == (CalendarObject.MONDAY & days));
        assertTrue(CalendarObject.WEDNESDAY == (CalendarObject.WEDNESDAY & days));
        assertTrue(CalendarObject.FRIDAY == (CalendarObject.FRIDAY & days));

        assertFalse(CalendarObject.TUESDAY == (CalendarObject.TUESDAY & days));
        assertFalse(CalendarObject.THURSDAY == (CalendarObject.THURSDAY & days));
        assertFalse(CalendarObject.SATURDAY == (CalendarObject.SATURDAY & days));
        assertFalse(CalendarObject.SUNDAY == (CalendarObject.SUNDAY & days));


        // Default Day taken from DTSTART

        task = taskWithRecurrence("FREQ=WEEKLY;INTERVAL=2;COUNT=3", start, end);
        days = task.getDays();

        assertEquals(3, task.getRecurrenceCount());
        assertEquals(CalendarObject.WEEKLY, task.getRecurrenceType());
        assertEquals(2, task.getInterval());
        assertTrue(CalendarObject.TUESDAY == (CalendarObject.TUESDAY & days)); // Start Date is a Tuesday

        // MONTHLY

        // First form: on 23rd day every 2 months

        task = taskWithRecurrence("FREQ=MONTHLY;INTERVAL=2;COUNT=3;BYMONTHDAY=23", start, end);
        assertEquals(3, task.getRecurrenceCount());
        assertEquals(CalendarObject.MONTHLY, task.getRecurrenceType());
        assertEquals(2, task.getInterval());
        assertEquals(23, task.getDayInMonth());

        // Second form : the 2nd monday and tuesday every 2 months

        task = taskWithRecurrence("FREQ=MONTHLY;INTERVAL=2;COUNT=3;BYDAY=MO,TU;BYWEEKNO=3", start, end);
        days = task.getDays();
        assertEquals(3, task.getDayInMonth());
        assertTrue(CalendarObject.MONDAY == (CalendarObject.MONDAY & days));
        assertTrue(CalendarObject.TUESDAY == (CalendarObject.TUESDAY & days)); // Is this correct? Can an appintment recur on more than one day in MONTHLY series?


        // Second form : the last tuesday every 2 months

        task = taskWithRecurrence("FREQ=MONTHLY;INTERVAL=2;COUNT=3;BYDAY=TU;BYWEEKNO=-1", start, end);
        days = task.getDays();
        assertEquals(5, task.getDayInMonth());
        assertTrue(CalendarObject.TUESDAY == (CalendarObject.TUESDAY & days));


        // Default taken from start date

        task = taskWithRecurrence("FREQ=MONTHLY;INTERVAL=2;COUNT=3", start, end);
        assertEquals(24, task.getDayInMonth());

        task = taskWithRecurrence("FREQ=MONTHLY;INTERVAL=2;COUNT=3;BYWEEKNO=3", start, end);
        days = task.getDays();
        assertTrue(CalendarObject.TUESDAY == (CalendarObject.TUESDAY & days));


        // YEARLY

        // First form: Every 2 years, the 23rd of March
        task = taskWithRecurrence("FREQ=YEARLY;INTERVAL=2;COUNT=3;BYMONTHDAY=23;BYMONTH=3", start, end);

        assertEquals(CalendarObject.YEARLY, task.getRecurrenceType());
        assertEquals(2, task.getMonth());
        assertEquals(23, task.getDayInMonth());

        // Second form: 2nd monday and wednesday in april every 2 years

        task = taskWithRecurrence("FREQ=YEARLY;INTERVAL=2;COUNT=3;BYDAY=MO,WE;BYMONTH=4;BYWEEKNO=2", start, end);
        days = task.getDays();

        assertEquals(3, task.getMonth());
        assertEquals(2, task.getDayInMonth());
        assertTrue(CalendarObject.MONDAY == (CalendarObject.MONDAY & days));
        assertTrue(CalendarObject.WEDNESDAY == (CalendarObject.WEDNESDAY & days)); // Is this correct? Can an appintment recur on more than one day in YEARLY series?


        // Default

        task = taskWithRecurrence("FREQ=YEARLY;INTERVAL=2;COUNT=3", start, end);

        assertEquals(CalendarObject.YEARLY, task.getRecurrenceType());
        assertEquals(1, task.getMonth());
        assertEquals(24, task.getDayInMonth());


        // UNTIL

        task = taskWithRecurrence("FREQ=YEARLY;INTERVAL=2;UNTIL=19890423", start, end);

        assertEquals(D("23/04/1989 00:00"), task.getUntil());
    }

    public void testTskDeleteExceptions() {
        Date start = D("24/01/1981 10:00");
        Date end = D("24/01/1981 12:00");

        TimeZone utc = TimeZone.getTimeZone("UTC");

        String rrule = "FREQ=DAILY;INTERVAL=2;COUNT=5";
        Date[] exceptions = new Date[]{D("26/01/1981 12:00"), D("30/01/1981 12:00")};

        String icalText = fixtures.vtodoWithDeleteExceptionsAsDateTime(start, end, rrule, exceptions);
        Task task = parseTask(icalText, utc);

        // Do we need the "time" in an exception?

        Set<Date> expectedExceptions = new HashSet<Date>(Arrays.asList(exceptions));

        assertNotNull(task.getDeleteException());
        for(Date exception  : task.getDeleteException()) {
            assertTrue("Didn't expect: "+exception+ " Expected one of: "+expectedExceptions, expectedExceptions.remove(exception));
        }
        assertTrue(expectedExceptions.isEmpty());

        icalText = fixtures.vtodoWithDeleteExceptionsAsDate(start, end, rrule, exceptions);

        task = parseTask(icalText, utc);


        expectedExceptions = new HashSet<Date>(Arrays.asList(D("26/01/1981 00:00"), D("30/01/1981 00:00")));


        assertNotNull(task.getDeleteException());
        for(Date exception  : task.getDeleteException()) {
            assertTrue("Didn't expect: "+exception+ " Expected one of: "+expectedExceptions, expectedExceptions.remove(exception));
        }
        assertTrue(expectedExceptions.isEmpty());
    }

    public void testTskAlarms() {
        // Relative to Start (default)

        Date start = D("24/02/1981 10:00");
        Date end = D("24/02/1981 12:00");

        TimeZone utc = TimeZone.getTimeZone("UTC");


        String icalText = fixtures.vtodoWithDisplayAlarm(start, end, "TRIGGER:-PT15M", "Description");

        Task task = parseTask(icalText, utc);


        assertEquals(D("24/02/1981 09:45"), task.getAlarm());
        assertTrue(task.getAlarmFlag());

        // Relative to Start (explicit)

        icalText = fixtures.vtodoWithDisplayAlarm(start, end, "TRIGGER;RELATED=START:-PT20M", "Description");
        task = parseTask(icalText, utc);

        assertEquals(D("24/02/1981 09:40"), task.getAlarm());
        assertTrue(task.getAlarmFlag());


        // Absolute Trigger

        icalText = fixtures.vtodoWithDisplayAlarm(start, end, "TRIGGER;VALUE=DATE-TIME:19810224T091000", "Description");

        task = parseTask(icalText, utc);

        assertEquals(D("24/02/1981 09:10"), task.getAlarm());
        assertTrue(task.getAlarmFlag());
    }


    // Errors and Warnings

    public void testAppShouldIncludeErrorOnMissingStartDate() {
        String icalText = fixtures.veventWithEnd(D("24/02/1981 10:00"));
        assertErrorWhenParsingAppointment(icalText, "Missing DTSTART");
    }

    public void testAppShouldIncludeErrorOnMissingEndDateAndMissingDuration() {
        String icalText = fixtures.veventWithStart(D("24/02/1981 10:00"));
        assertErrorWhenParsingAppointment(icalText, "DTEND or Duration required");
    }

    public void testAppShouldIncludeErrorOnConfidentialAppointments() {
        String icalText = fixtures.veventWithSimpleProperties(D("24/02/1981 10:00"), D("24/02/1981 12:00"), "CLASS", "CONFIDENTIAL");
        assertErrorWhenParsingAppointment(icalText, "Cowardly refusing to convert confidential appointment");
    }

    
    public void testAppShouldIncludeWarningForAdditionalRecurrences() {
        String icalText = fixtures.veventWithTwoRecurrences(D("24/02/1981 10:00"), D("24/02/1981 12:00"));
        assertWarningWhenParsingAppointment(icalText, "Only converting first recurrence rule, additional recurrence rules will be ignored.");
    }

    public void testAppShouldWarnOnUnsupportedRecurrenceIntervals() {
        warningOnAppRecurrence("FREQ=SECONDLY;INTERVAL=1;COUNT=3", "Can only convert DAILY, WEEKLY, MONTHLY and YEARLY recurrences");
        warningOnAppRecurrence("FREQ=MINUTELY;INTERVAL=1;COUNT=3", "Can only convert DAILY, WEEKLY, MONTHLY and YEARLY recurrences");
        warningOnAppRecurrence("FREQ=HOURLY;INTERVAL=1;COUNT=3", "Can only convert DAILY, WEEKLY, MONTHLY and YEARLY recurrences");

    }

    public void testAppShouldInlcudeWarningOnUnkownClass() {
        String icalText = fixtures.veventWithSimpleProperties(D("24/02/1981 10:00"), D("24/02/1981 12:00"), "CLASS", "SUPERCALIFRAGILISTICEXPLIALIDOCIOUS");
        assertWarningWhenParsingAppointment(icalText, "Unknown Class: SUPERCALIFRAGILISTICEXPLIALIDOCIOUS");
    }

    public void testAppShouldIncludeWarningOnUndisplayableAlarms() {
        String icalText = fixtures.veventWithAudioAlarm(D("24/02/1981 10:00"), D("24/02/1981 12:00"), "TRIGGER:-PT15M", "alarm.wav");
        assertWarningWhenParsingAppointment(icalText, "Can only convert DISPLAY alarms");
    }

    public void testAppShouldIncludeErrorForUnknownDayInRRule() {
        String icalText =  fixtures.veventWithSimpleProperties(D("24/02/1981 10:00"), D("24/02/1981 12:00"), "RRULE", "FREQ=MONTHLY;INTERVAL=2;COUNT=3;BYDAY=WU,NI;BYWEEKNO=3");
        assertErrorWhenParsingAppointment(icalText, "Unknown day: WU");    
    }

    public void testAppShouldWarnOnUnhandleableFields() {
        //TODO        
    }

    @Override
    protected void setUp() throws Exception {
        fixtures = new ICALFixtures();
        users = new MockUserLookup();
        parser = new ICal4JParser();
        Participants.userResolver = new UserResolver(){
            public List<User> findUsers(List<String> mails, Context ctx) {
                List<User> found = new LinkedList<User>();
                for(String mail : mails) {
                    User user = ICalParserTest.this.users.getUserByMail(mail);
                    if(user != null) {
                        found.add( user );
                    }
                }

                return found;
            }

            public User loadUser(int userId, Context ctx) throws LdapException {
                return ICalParserTest.this.users.getUser(userId);
            }
        };
    }

    protected List<User> U(int...ids) {
        List<User> found = new LinkedList<User>();
        for(int i : ids) {
            try {
                found.add( users.getUser(i) );
            } catch (LdapException e) {
                //IGNORE
            }
        }
        return found;
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    protected AppointmentObject parseAppointment(String icalText, TimeZone defaultTZ) {
        return parser.parseAppointments(icalText, defaultTZ, new ContextImpl(23), new ArrayList<ConversionError>() , new ArrayList<ConversionWarning>() ).get(0);
    }

    protected AppointmentObject parseAppointment(String icalText) {
        return parseAppointment(icalText, TimeZone.getDefault());
    }

    protected Task parseTask(String icalText,  TimeZone defaultTZ) {
        return parser.parseTasks(icalText, defaultTZ, new ContextImpl(23), new ArrayList<ConversionError>() , new ArrayList<ConversionWarning>()).get(0);
    }

    protected Task parseTask(String icalText) {
        return parser.parseTasks(icalText, TimeZone.getDefault(), new ContextImpl(23), new ArrayList<ConversionError>() , new ArrayList<ConversionWarning>()).get(0);
    }

    protected AppointmentObject appointmentWithRecurrence(String recurrence, Date start, Date end) {

        TimeZone utc = TimeZone.getTimeZone("UTC");

        String icalText = fixtures.veventWithSimpleProperties(start, end, "RRULE", recurrence);
        AppointmentObject appointment = parseAppointment(icalText, utc);

        return appointment;
    }

    protected Task taskWithRecurrence(String recurrence, Date start, Date end) {

        TimeZone utc = TimeZone.getTimeZone("UTC");

        String icalText = fixtures.vtodoWithSimpleProperties(start, end, "RRULE", recurrence);
        Task task = parseTask(icalText, utc);
        
        return task;
    }

    protected void assertWarningWhenParsingAppointment(String icalText, String warning) {
        ArrayList<ConversionError> errors = new ArrayList<ConversionError>();
        ArrayList<ConversionWarning> warnings = new ArrayList<ConversionWarning>();
        List<CalendarDataObject> result = parser.parseAppointments(icalText, TimeZone.getTimeZone("UTC"), new ContextImpl(23), errors, warnings);

        assertTrue(0 != result.size()); // Warnings don't abort parsing of the object
        assertEquals(1, warnings.size());
        assertEquals(warning, warnings.get(0).getFormattedMessage());
    }

    protected void assertErrorWhenParsingAppointment(String icalText, String error) {
        ArrayList<ConversionError> errors = new ArrayList<ConversionError>();
        ArrayList<ConversionWarning> warnings = new ArrayList<ConversionWarning>();
        parser.parseAppointments(icalText, TimeZone.getTimeZone("UTC"), new ContextImpl(23), errors, warnings);
        assertEquals(1, errors.size());
        assertEquals(error, errors.get(0).getFormattedMessage());
            
    }

    protected void warningOnAppRecurrence(String recurrence, String warning) {
        String icalText = fixtures.veventWithSimpleProperties(D("24/02/1981 10:00"), D("24/02/1981 12:00"), "RRULE", recurrence);
        assertWarningWhenParsingAppointment(icalText, warning);

    }
}
