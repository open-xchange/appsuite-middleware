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
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import junit.framework.TestCase;

import com.openexchange.data.conversion.ical.ical4j.ICal4JParser;
import com.openexchange.data.conversion.ical.ical4j.internal.ResourceResolver;
import com.openexchange.data.conversion.ical.ical4j.internal.UserResolver;
import com.openexchange.data.conversion.ical.ical4j.internal.calendar.Participants;
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
import com.openexchange.resource.Resource;
import com.openexchange.resource.ResourceException;
import com.openexchange.server.ServiceException;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class ICalParserTest extends TestCase {

    private ICALFixtures fixtures;
    private ICalParser parser;
    private MockUserLookup users;
    private ResourceResolver oldResourceResolver;
    private UserResolver oldUserResolver;

    // Appointments

    public void testAppStartToEnd() throws ConversionError {

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

    public void testDTSTARTAsDateWithoutValue() throws ConversionError {
        Date start = D("24/02/1981 00:00");

        String icalText = fixtures.veventWithDTStartAsDateWithoutValue(start);

        AppointmentObject appointment = parseAppointment(icalText);
        assertNotNull(appointment.getStartDate());

    }

    public void testAppDuration() throws ConversionError {
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

    public void testAppPrivateFlag() throws ConversionError {
        Date start = D("24/02/1981 10:00");
        Date end = D("24/02/1981 12:00");

        TimeZone utc = TimeZone.getTimeZone("UTC");

        // Private

        String icalText = fixtures.veventWithSimpleProperties(start, end, "CLASS", "PRIVATE");
        AppointmentObject appointment = parseAppointment(icalText, utc);

        assertTrue(appointment.getPrivateFlag());

        // Public
        icalText = fixtures.veventWithSimpleProperties(start, end, "CLASS", "PUBLIC");
        appointment = parseAppointment(icalText, utc);
        assertFalse(appointment.getPrivateFlag());

    }

    public void testAppCreated() throws ConversionError {
        Date start = D("24/02/1981 10:00");
        Date end = D("24/02/1981 12:00");

        TimeZone utc = TimeZone.getTimeZone("UTC");

        
        String icalText = fixtures.veventWithSimpleProperties(start, end, "CREATED", "20081023T100000Z");
        AppointmentObject appointment = parseAppointment(icalText, utc);

        assertEquals(D("23/10/2008 10:00"), appointment.getCreationDate());

    }

    public void testAppLastModified() throws ConversionError {
        Date start = D("24/02/1981 10:00");
        Date end = D("24/02/1981 12:00");

        TimeZone utc = TimeZone.getTimeZone("UTC");


        String icalText = fixtures.veventWithSimpleProperties(start, end, "LAST-MODIFIED", "20081023T100000Z");
        AppointmentObject appointment = parseAppointment(icalText, utc);

        assertEquals(D("23/10/2008 10:00"), appointment.getLastModified());
        
    }

    public void testAppDTSTAMP() throws ConversionError {
        Date start = D("24/02/1981 10:00");
        Date end = D("24/02/1981 12:00");

        TimeZone utc = TimeZone.getTimeZone("UTC");


        String icalText = fixtures.veventWithSimpleProperties(start, end, "DTSTAMP", "20081023T100000Z");
        AppointmentObject appointment = parseAppointment(icalText, utc);

        assertEquals(D("23/10/2008 10:00"), appointment.getCreationDate());   
    }

    public void testAppNote() throws ConversionError {
        Date start = D("24/02/1981 10:00");
        Date end = D("24/02/1981 12:00");

        TimeZone utc = TimeZone.getTimeZone("UTC");


        String icalText = fixtures.veventWithSimpleProperties(start, end, "DESCRIPTION", "A fine description");
        AppointmentObject appointment = parseAppointment(icalText, utc);

        assertEquals("A fine description", appointment.getNote());

        // TODO Test encodings

    }

    public void testAppLocation() throws ConversionError {
        Date start = D("24/02/1981 10:00");
        Date end = D("24/02/1981 12:00");

        TimeZone utc = TimeZone.getTimeZone("UTC");


        String icalText = fixtures.veventWithSimpleProperties(start, end, "LOCATION", "Mars");
        AppointmentObject appointment = parseAppointment(icalText, utc);

        assertEquals("Mars", appointment.getLocation());
    }

    public void testAppTitle() throws ConversionError {
        Date start = D("24/02/1981 10:00");
        Date end = D("24/02/1981 12:00");

        TimeZone utc = TimeZone.getTimeZone("UTC");

        String icalText = fixtures.veventWithSimpleProperties(start, end, "SUMMARY", "A fine title");
        AppointmentObject appointment = parseAppointment(icalText, utc);

        assertEquals("A fine title", appointment.getTitle());
    }

    public void testAppReserved() throws ConversionError {
        Date start = D("24/02/1981 10:00");
        Date end = D("24/02/1981 12:00");

        TimeZone utc = TimeZone.getTimeZone("UTC");

        String icalText = fixtures.veventWithSimpleProperties(start, end, "TRANSP", "OPAQUE");
        AppointmentObject appointment = parseAppointment(icalText, utc);

        assertEquals(AppointmentObject.RESERVED, appointment.getShownAs());
    }

    public void testAppFree() throws ConversionError {
        Date start = D("24/02/1981 10:00");
        Date end = D("24/02/1981 12:00");

        TimeZone utc = TimeZone.getTimeZone("UTC");

        String icalText = fixtures.veventWithSimpleProperties(start, end, "TRANSP", "TRANSPARENT");
        AppointmentObject appointment = parseAppointment(icalText, utc);

        assertEquals(AppointmentObject.FREE, appointment.getShownAs());
    }

    public void testAppAttendees() throws ConversionError {
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

    public void testAppResources() throws ConversionError {
     
        Date start = D("24/02/1981 10:00");
        Date end = D("24/02/1981 12:00");

        TimeZone utc = TimeZone.getTimeZone("UTC");

        String[] resources = new String[]{"Toaster", "Deflector", "Subspace Anomaly"};

        String icalText = fixtures.veventWithResources(start, end, resources);
        AppointmentObject appointment = parseAppointment(icalText, utc);

        Set<Integer> resourceSet = new HashSet<Integer>(Arrays.asList(1,2,3));

        assertNotNull(appointment.getParticipants());
        for(Participant p  : appointment.getParticipants()) {
            assertTrue(ResourceParticipant.class.isAssignableFrom(p.getClass()));
            ResourceParticipant participant = (ResourceParticipant) p;
            assertTrue(resourceSet.remove(participant.getIdentifier()));
        }

        assertTrue(resourceSet.isEmpty());
        
    }

    public void testAppResourcesInParticipants() throws ConversionError {
        // Resources can also be specified in the attendee property with a cutype
        Date start = D("24/02/1981 10:00");
        Date end = D("24/02/1981 12:00");

        TimeZone utc = TimeZone.getTimeZone("UTC");

        String[] resources = new String[]{"Toaster", "Deflector", "Subspace Anomaly"};

        String icalText = fixtures.veventWithResourcesInAttendees(start, end, resources);
        AppointmentObject appointment = parseAppointment(icalText, utc);

        Set<Integer> resourceSet = new HashSet<Integer>(Arrays.asList(1,2,3));

        assertNotNull(appointment.getParticipants());
        for(Participant p  : appointment.getParticipants()) {
            assertTrue(ResourceParticipant.class.isAssignableFrom(p.getClass()));
            ResourceParticipant participant = (ResourceParticipant) p;
            assertTrue("Didn't expect: "+participant.getIdentifier(), resourceSet.remove(participant.getIdentifier()));
        }

        assertTrue(resourceSet.toString(), resourceSet.isEmpty());

    }

    public void testAppCategories() throws ConversionError {
        Date start = D("24/02/1981 10:00");
        Date end = D("24/02/1981 12:00");

        TimeZone utc = TimeZone.getTimeZone("UTC");

        String[] categories = new String[]{"Toaster", "Deflector", "Subspace Anomaly"};
        String categoriesString = "Toaster,Deflector,Subspace Anomaly";

        String icalText = fixtures.veventWithCategories(start, end, categories);
        AppointmentObject appointment = parseAppointment(icalText, utc);

        assertEquals(categoriesString, appointment.getCategories());
    }

    public void testAppRecurrence() throws ConversionError {

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

        // Third form: every second sunday in april

        appointment = appointmentWithRecurrence("FREQ=YEARLY;BYDAY=2SU;BYMONTH=4", start, end);
        assertEquals(3, appointment.getMonth());
        assertEquals(2, appointment.getDayInMonth());
        assertEquals(AppointmentObject.SUNDAY, appointment.getDays());
        assertEquals(1, appointment.getInterval());

        // Third form: last sunday in april
        appointment = appointmentWithRecurrence("FREQ=YEARLY;BYDAY=-1SU;BYMONTH=4", start, end);
        assertEquals(3, appointment.getMonth());
        assertEquals(5, appointment.getDayInMonth());
        assertEquals(AppointmentObject.SUNDAY, appointment.getDays());
        assertEquals(1, appointment.getInterval());
        

        // Default

        appointment = appointmentWithRecurrence("FREQ=YEARLY;INTERVAL=2;COUNT=3", start, end);

        assertEquals(AppointmentObject.YEARLY, appointment.getRecurrenceType());
        assertEquals(1, appointment.getMonth());
        assertEquals(24, appointment.getDayInMonth());


        // UNTIL

        appointment = appointmentWithRecurrence("FREQ=YEARLY;INTERVAL=2;UNTIL=19890423", start, end);

        assertEquals(D("23/04/1989 00:00"), appointment.getUntil());

    }

    public void testAppDeleteExceptions() throws ConversionError {
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

    public void testAppAlarms() throws ConversionError {
        // Relative to Start (default)

        Date start = D("24/02/1981 10:00");
        Date end = D("24/02/1981 12:00");

        TimeZone utc = TimeZone.getTimeZone("UTC");

        int MINUTES = 1;

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

    public void testAppFullTime() throws ConversionError {
        String icalText = fixtures.veventWithWholeDayEvent(D("24/02/1990 12:00"));
        AppointmentObject appointment = parseAppointment(icalText);

        assertTrue(appointment.getFullTime());
    }

    // Tasks

    public void testTskTitle() throws ConversionError {
        String icalText = fixtures.vtodoWithSimpleProperties("SUMMARY", "A nice title");
        Task task = parseTask(icalText, TimeZone.getTimeZone("UTC"));

        assertEquals("A nice title", task.getTitle());
    }


    public void testTskCreated() throws ConversionError {
        String icalText = fixtures.vtodoWithSimpleProperties("CREATED", "20081023T100000Z");
        Task task = parseTask(icalText);

        assertEquals(D("23/10/2008 10:00"), task.getCreationDate());

    }

    public void testTskLastModified() throws ConversionError {
        String icalText = fixtures.vtodoWithSimpleProperties("LAST-MODIFIED", "20081023T100000Z");
        Task task = parseTask(icalText);

        assertEquals(D("23/10/2008 10:00"), task.getLastModified());

    }

    public void testTskDTSTAMP() throws ConversionError {
        String icalText = fixtures.vtodoWithSimpleProperties("DTSTAMP", "20081023T100000Z");
        Task task = parseTask(icalText);

        assertEquals(D("23/10/2008 10:00"), task.getCreationDate());
    }

    public void testTskNote() throws ConversionError {
        String icalText = fixtures.vtodoWithSimpleProperties("DESCRIPTION", "A nice description");
        Task task = parseTask(icalText, TimeZone.getTimeZone("UTC"));

        assertEquals("A nice description", task.getNote());
            
    }

    public void testTskStartToEnd() throws ConversionError {
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

    public void testTskDuration() throws ConversionError {
        Date start = D("24/02/1981 10:00");
        Date end = D("24/02/1981 12:00");
        String duration = "P2H"; // 2 hours

        TimeZone utc = TimeZone.getTimeZone("UTC");

        String icalText = fixtures.vtodoWithLocalDTStartAndDuration(start, duration);
        Task task = parseTask(icalText, utc);

        assertEquals(start, task.getStartDate());
        assertEquals(end, task.getEndDate());

    }

    public void testTskDue() throws ConversionError {
        Date due = D("24/03/1981 10:00");

        TimeZone utc = TimeZone.getTimeZone("UTC");

        String icalText = fixtures.vtodoWithDueDate(due);
        Task task = parseTask(icalText, utc);

        assertEquals(due, task.getEndDate());
    }

    public void testTskPrivateFlag() throws ConversionError {
        TimeZone utc = TimeZone.getTimeZone("UTC");

        // Private

        String icalText = fixtures.vtodoWithSimpleProperties("CLASS", "PRIVATE");
        Task task = parseTask(icalText, utc);

        assertTrue(task.getPrivateFlag());

        // Public
        icalText = fixtures.vtodoWithSimpleProperties("CLASS", "PUBLIC");
        task = parseTask(icalText, utc);
        assertFalse(task.getPrivateFlag());
    }

    public void testTskDateCompleted() throws ConversionError {
        Date dateCompleted = D("24/03/1981 10:00");

        TimeZone utc = TimeZone.getTimeZone("UTC");

        String icalText = fixtures.vtodoWithDateCompleted(dateCompleted);
        Task task = parseTask(icalText, utc);

        assertEquals(dateCompleted, task.getDateCompleted());
    }

    public void testTskPercentComplete() throws ConversionError {
        String icalText = fixtures.vtodoWithSimpleProperties("PERCENT-COMPLETE", "23");
        Task task = parseTask(icalText);

        assertEquals(23, task.getPercentComplete());
    }

    public void testTskPriority() throws ConversionError {
        priorityTest(1, Task.HIGH);
        priorityTest(2, Task.HIGH);
        priorityTest(3, Task.NORMAL);
        priorityTest(4, Task.NORMAL);
        priorityTest(5, Task.NORMAL);
        priorityTest(6, Task.NORMAL);
        priorityTest(7, Task.LOW);
        priorityTest(8, Task.LOW);
        priorityTest(9, Task.LOW);

    }

    private void priorityTest(int priority, int expected) throws ConversionError {
        String icalText = fixtures.vtodoWithSimpleProperties("PRIORITY", new Integer(priority).toString());
        Task task = parseTask(icalText);
        assertEquals("Invalid interpretation for priority "+priority, expected, task.getPriority());
    }

    // Bug 10401
    public void testTskPriorityZero() throws ConversionError {
        priorityTest(0, Task.NORMAL);
    }

    public void testTskStatus() throws ConversionError {
        statusTest("NEEDS-ACTION", Task.NOT_STARTED);
        statusTest("IN-PROCESS", Task.IN_PROGRESS);
        statusTest("COMPLETED", Task.DONE);
        statusTest("CANCELLED", Task.DEFERRED);
    }

    private void statusTest(String status, int expected) throws ConversionError {
        String icalText = fixtures.vtodoWithSimpleProperties("STATUS", status);
        Task task = parseTask(icalText);
        assertEquals("Invalid interpretation for status "+status, expected, task.getStatus());
    }

    public void testTskAttendees() throws ConversionError {
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

    public void testTskCategories() throws ConversionError {
        TimeZone utc = TimeZone.getTimeZone("UTC");

        String[] categories = new String[]{"Toaster", "Deflector", "Subspace Anomaly"};
        String categoriesString = "Toaster,Deflector,Subspace Anomaly";

        String icalText = fixtures.vtodoWithCategories(categories);
        Task task = parseTask(icalText, utc);

        assertEquals(categoriesString, task.getCategories());
    }

    public void testTskRecurrence() throws ConversionError {
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

    public void testTskDeleteExceptions() throws ConversionError {
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

    public void testTskAlarms() throws ConversionError {
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

    public void testAppShouldIncludeErrorOnMissingStartDate() throws ConversionError {
        String icalText = fixtures.veventWithEnd(D("24/02/1981 10:00"));
        assertErrorWhenParsingAppointment(icalText, "Missing DTSTART");
    }

    public void testAppShouldIncludeErrorOnConfidentialAppointments() throws ConversionError {
        String icalText = fixtures.veventWithSimpleProperties(D("24/02/1981 10:00"), D("24/02/1981 12:00"), "CLASS", "CONFIDENTIAL");
        assertErrorWhenParsingAppointment(icalText, "Cowardly refusing to convert confidential classified objects.");
    }

    
    public void testAppShouldIncludeWarningForAdditionalRecurrences() throws ConversionError {
        String icalText = fixtures.veventWithTwoRecurrences(D("24/02/1981 10:00"), D("24/02/1981 12:00"));
        assertWarningWhenParsingAppointment(icalText, "Only converting first recurrence rule, additional recurrence rules will be ignored.");
    }

    public void testAppShouldWarnOnUnsupportedRecurrenceIntervals() throws ConversionError {
        warningOnAppRecurrence("FREQ=SECONDLY;INTERVAL=1;COUNT=3", "Can only convert DAILY, WEEKLY, MONTHLY and YEARLY recurrences");
        warningOnAppRecurrence("FREQ=MINUTELY;INTERVAL=1;COUNT=3", "Can only convert DAILY, WEEKLY, MONTHLY and YEARLY recurrences");
        warningOnAppRecurrence("FREQ=HOURLY;INTERVAL=1;COUNT=3", "Can only convert DAILY, WEEKLY, MONTHLY and YEARLY recurrences");

    }

    public void testAppShouldInlcudeWarningOnUnkownClass() throws ConversionError {
        String icalText = fixtures.veventWithSimpleProperties(D("24/02/1981 10:00"), D("24/02/1981 12:00"), "CLASS", "SUPERCALIFRAGILISTICEXPLIALIDOCIOUS");
        assertWarningWhenParsingAppointment(icalText, "Unknown Class: SUPERCALIFRAGILISTICEXPLIALIDOCIOUS");
    }

    public void testAppShouldIncludeWarningOnUndisplayableAlarms() throws ConversionError {
        String icalText = fixtures.veventWithAudioAlarm(D("24/02/1981 10:00"), D("24/02/1981 12:00"), "TRIGGER:-PT15M", "alarm.wav");
        assertWarningWhenParsingAppointment(icalText, "Can only convert DISPLAY alarms with triggers");
    }

    public void testAppShouldIncludeErrorForUnknownDayInRRule() throws ConversionError {
        String icalText =  fixtures.veventWithSimpleProperties(D("24/02/1981 10:00"), D("24/02/1981 12:00"), "RRULE", "FREQ=MONTHLY;INTERVAL=2;COUNT=3;BYDAY=WU,NI;BYWEEKNO=3");
        assertErrorWhenParsingAppointment(icalText, "Unknown day: WU");    
    }

    public void testAppShouldWarnOnUnhandleableFields() throws ConversionError {
        //TODO        
    }


    // Bug 11987
    public void testMultipleCalendars() throws ConversionError {
        Date start1 = D("24/02/1981 10:00");
        Date end1 = D("24/02/1981 12:00");

        Date start2 = D("24/02/1981 10:00");
        Date end2 = D("24/02/1981 12:00");

        StringBuilder combiner = new StringBuilder();
        combiner.append(fixtures.veventWithUTCDTStartAndDTEnd(start1, end1))
                .append("\n")
                .append(fixtures.veventWithUTCDTStartAndDTEnd(start2, end2));

        List<CalendarDataObject> appointments = parser.parseAppointments(combiner.toString(), TimeZone.getTimeZone("UTC"),null, new ArrayList<ConversionError>(), new ArrayList<ConversionWarning>());

        assertEquals(2, appointments.size());
    }

    // Bug 11869
    public void testAppShouldCorrectParticipantsInPrivateAppoitment() throws ConversionError {
        Date start = D("24/02/1981 10:00");
        Date end =   D("24/02/1981 12:00");
        String icalText = fixtures.veventWithSimpleProperties(start, end,
        "CLASS"    ,    "PRIVATE",
        "ATTENDEE" ,     "MAILTO:mickey@disney.invalid");

        assertWarningWhenParsingAppointment(icalText, "Private Appointments can not have attendees. Removing attendees and accepting appointment anyway.");

        AppointmentObject appointment = parseAppointment(icalText);

        assertNull(appointment.getParticipants());
    }

    @Override
    protected void setUp() throws Exception {
        fixtures = new ICALFixtures();
        users = new MockUserLookup();
        parser = new ICal4JParser();
        oldUserResolver = Participants.userResolver;
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
        oldResourceResolver = Participants.resourceResolver;
        Participants.resourceResolver = new ResourceResolver() {
            private List<Resource> resources = new ArrayList<Resource>() {{
                Resource toaster = new Resource();
                toaster.setDisplayName("Toaster");
                toaster.setIdentifier(1);
                add(toaster);

                Resource deflector = new Resource();
                deflector.setDisplayName("Deflector");
                deflector.setIdentifier(2);
                add(deflector);

                Resource subspaceAnomaly = new Resource();
                subspaceAnomaly.setDisplayName("Subspace Anomaly");
                subspaceAnomaly.setIdentifier(3);
                add(subspaceAnomaly);
            }};

            public List<Resource> find(List<String> names, Context ctx)
                throws ResourceException, ServiceException {
                List<Resource> retval = new ArrayList<Resource>();
                for(String name : names) {
                    for(Resource resource : resources) {
                        if(resource.getDisplayName().equals(name)) {
                            retval.add(resource);
                        }
                    }
                }
                return retval;
            }
            public Resource load(int resourceId, Context ctx)
                throws ResourceException, ServiceException {
                return null;
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
        Participants.userResolver = oldUserResolver;
        Participants.resourceResolver = oldResourceResolver;
        super.tearDown();
    }

    protected AppointmentObject parseAppointment(String icalText, TimeZone defaultTZ) throws ConversionError {
        return parser.parseAppointments(icalText, defaultTZ, new ContextImpl(23), new ArrayList<ConversionError>() , new ArrayList<ConversionWarning>() ).get(0);
    }

    protected AppointmentObject parseAppointment(String icalText) throws ConversionError {
        return parseAppointment(icalText, TimeZone.getDefault());
    }

    protected Task parseTask(String icalText,  TimeZone defaultTZ) throws ConversionError {
        return parser.parseTasks(icalText, defaultTZ, new ContextImpl(23), new ArrayList<ConversionError>() , new ArrayList<ConversionWarning>()).get(0);
    }

    protected Task parseTask(String icalText) throws ConversionError {
        return parser.parseTasks(icalText, TimeZone.getDefault(), new ContextImpl(23), new ArrayList<ConversionError>() , new ArrayList<ConversionWarning>()).get(0);
    }

    protected AppointmentObject appointmentWithRecurrence(String recurrence, Date start, Date end) throws ConversionError {

        TimeZone utc = TimeZone.getTimeZone("UTC");

        String icalText = fixtures.veventWithSimpleProperties(start, end, "RRULE", recurrence);
        AppointmentObject appointment = parseAppointment(icalText, utc);

        return appointment;
    }

    protected Task taskWithRecurrence(String recurrence, Date start, Date end) throws ConversionError {

        TimeZone utc = TimeZone.getTimeZone("UTC");

        String icalText = fixtures.vtodoWithSimpleProperties(start, end, "RRULE", recurrence);
        Task task = parseTask(icalText, utc);
        
        return task;
    }

    protected void assertWarningWhenParsingAppointment(String icalText, String warning) throws ConversionError {
        ArrayList<ConversionError> errors = new ArrayList<ConversionError>();
        ArrayList<ConversionWarning> warnings = new ArrayList<ConversionWarning>();
        List<CalendarDataObject> result = parser.parseAppointments(icalText, TimeZone.getTimeZone("UTC"), new ContextImpl(23), errors, warnings);

        assertTrue(0 != result.size()); // Warnings don't abort parsing of the object
        assertEquals(1, warnings.size());
        assertEquals(warning, warnings.get(0).getFormattedMessage());
    }

    protected void assertErrorWhenParsingAppointment(String icalText, String error) throws ConversionError {
        ArrayList<ConversionError> errors = new ArrayList<ConversionError>();
        ArrayList<ConversionWarning> warnings = new ArrayList<ConversionWarning>();
        parser.parseAppointments(icalText, TimeZone.getTimeZone("UTC"), new ContextImpl(23), errors, warnings);
        assertEquals(1, errors.size());
        assertEquals(error, errors.get(0).getFormattedMessage());
            
    }

    protected void warningOnAppRecurrence(String recurrence, String warning) throws ConversionError {
        String icalText = fixtures.veventWithSimpleProperties(D("24/02/1981 10:00"), D("24/02/1981 12:00"), "RRULE", recurrence);
        assertWarningWhenParsingAppointment(icalText, warning);

    }
}
