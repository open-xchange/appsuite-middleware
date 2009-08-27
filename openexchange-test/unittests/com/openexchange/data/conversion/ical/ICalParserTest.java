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

import java.util.*;

import junit.framework.TestCase;

import com.openexchange.data.conversion.ical.ical4j.ICal4JParser;
import com.openexchange.data.conversion.ical.ical4j.internal.ResourceResolver;
import com.openexchange.data.conversion.ical.ical4j.internal.UserResolver;
import com.openexchange.data.conversion.ical.ical4j.internal.calendar.Participants;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.ExternalUserParticipant;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.ResourceParticipant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextImpl;
import com.openexchange.groupware.ldap.MockUserLookup;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserException;
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

        final Date start = D("24/02/1981 10:00");
        final Date end = D("24/02/1981 12:00");

        final TimeZone utc = TimeZone.getTimeZone("UTC");

        // Local Time

        String icalText = fixtures.veventWithLocalDTStartAndDTEnd(start, end);
        Appointment appointment = parseAppointment(icalText, utc);
        
        assertEquals(start, appointment.getStartDate());
        assertEquals(end, appointment.getEndDate());
        assertEquals("UTC", appointment.getTimezoneFallbackUTC());

        // UTC

        icalText = fixtures.veventWithUTCDTStartAndDTEnd(start, end);
        appointment = parseAppointment(icalText);

        assertEquals(start, appointment.getStartDate());
        assertEquals(end, appointment.getEndDate());
        assertEquals("UTC", appointment.getTimezoneFallbackUTC());

        // Known TZID

        final TimeZone timeZone = TimeZone.getTimeZone("America/Los_Angeles");
        assertFalse("Bad test TimeZone",timeZone.equals(TimeZone.getDefault()));

        icalText = fixtures.veventWithDTStartAndEndInTimeZone(start, end, timeZone);
        appointment = parseAppointment(icalText);

        assertEquals(recalculate(start, utc , timeZone), appointment.getStartDate());
        assertEquals(recalculate(end, utc, timeZone), appointment.getEndDate());
        assertEquals(timeZone.getID(), appointment.getTimezoneFallbackUTC());

        // VTIMEZONE

        icalText = fixtures.veventWithDTStartAndDTEndInCustomTimezone(start, end);
        appointment = parseAppointment(icalText);

        assertEquals(D("24/02/1981 01:00"), appointment.getStartDate());
        assertEquals(D("24/02/1981 03:00"), appointment.getEndDate());
        assertEquals("UTC", appointment.getTimezoneFallbackUTC());
    }

    public void testDTSTARTAsDateWithoutValue() throws ConversionError {
        final Date start = D("24/02/1981 00:00");

        final String icalText = fixtures.veventWithDTStartAsDateWithoutValue(start);

        final Appointment appointment = parseAppointment(icalText);
        assertNotNull(appointment.getStartDate());

    }

    public void testAppDuration() throws ConversionError {
        final Date start = D("24/02/1981 10:00");
        final Date end = D("24/02/1981 12:00");
        final String duration = "P2H"; // 2 hours

        final TimeZone utc = TimeZone.getTimeZone("UTC");

        final String icalText = fixtures.veventWithLocalDTStartAndDuration(start, duration);
        final Appointment appointment = parseAppointment(icalText, utc);

        assertEquals(start, appointment.getStartDate());
        assertEquals(end, appointment.getEndDate());
        assertEquals("UTC", appointment.getTimezoneFallbackUTC());

    }

    public void testAppPrivateFlag() throws ConversionError {
        final Date start = D("24/02/1981 10:00");
        final Date end = D("24/02/1981 12:00");

        final TimeZone utc = TimeZone.getTimeZone("UTC");

        // Private

        String icalText = fixtures.veventWithSimpleProperties(start, end, "CLASS", "PRIVATE");
        Appointment appointment = parseAppointment(icalText, utc);

        assertTrue(appointment.getPrivateFlag());

        // Public
        icalText = fixtures.veventWithSimpleProperties(start, end, "CLASS", "PUBLIC");
        appointment = parseAppointment(icalText, utc);
        assertFalse(appointment.getPrivateFlag());

    }

    public void testAppCreated() throws ConversionError {
        final Date start = D("24/02/1981 10:00");
        final Date end = D("24/02/1981 12:00");

        final TimeZone utc = TimeZone.getTimeZone("UTC");

        
        final String icalText = fixtures.veventWithSimpleProperties(start, end, "CREATED", "20081023T100000Z");
        final Appointment appointment = parseAppointment(icalText, utc);

        assertEquals(D("23/10/2008 10:00"), appointment.getCreationDate());

    }

    public void testAppLastModified() throws ConversionError {
        final Date start = D("24/02/1981 10:00");
        final Date end = D("24/02/1981 12:00");

        final TimeZone utc = TimeZone.getTimeZone("UTC");


        final String icalText = fixtures.veventWithSimpleProperties(start, end, "LAST-MODIFIED", "20081023T100000Z");
        final Appointment appointment = parseAppointment(icalText, utc);

        assertEquals(D("23/10/2008 10:00"), appointment.getLastModified());
        
    }

    public void testAppDTSTAMP() throws ConversionError {
        final Date start = D("24/02/1981 10:00");
        final Date end = D("24/02/1981 12:00");

        final TimeZone utc = TimeZone.getTimeZone("UTC");


        final String icalText = fixtures.veventWithSimpleProperties(start, end, "DTSTAMP", "20081023T100000Z");
        final Appointment appointment = parseAppointment(icalText, utc);

        assertEquals(D("23/10/2008 10:00"), appointment.getCreationDate());   
    }

    public void testAppNote() throws ConversionError {
        final Date start = D("24/02/1981 10:00");
        final Date end = D("24/02/1981 12:00");

        final TimeZone utc = TimeZone.getTimeZone("UTC");


        final String icalText = fixtures.veventWithSimpleProperties(start, end, "DESCRIPTION", "A fine description");
        final Appointment appointment = parseAppointment(icalText, utc);

        assertEquals("A fine description", appointment.getNote());

        // TODO Test encodings

    }

    public void testAppLocation() throws ConversionError {
        final Date start = D("24/02/1981 10:00");
        final Date end = D("24/02/1981 12:00");

        final TimeZone utc = TimeZone.getTimeZone("UTC");


        final String icalText = fixtures.veventWithSimpleProperties(start, end, "LOCATION", "Mars");
        final Appointment appointment = parseAppointment(icalText, utc);

        assertEquals("Mars", appointment.getLocation());
    }

    public void testAppTitle() throws ConversionError {
        final Date start = D("24/02/1981 10:00");
        final Date end = D("24/02/1981 12:00");

        final TimeZone utc = TimeZone.getTimeZone("UTC");

        final String icalText = fixtures.veventWithSimpleProperties(start, end, "SUMMARY", "A fine title");
        final Appointment appointment = parseAppointment(icalText, utc);

        assertEquals("A fine title", appointment.getTitle());
    }

    public void testAppReserved() throws ConversionError {
        final Date start = D("24/02/1981 10:00");
        final Date end = D("24/02/1981 12:00");

        final TimeZone utc = TimeZone.getTimeZone("UTC");

        final String icalText = fixtures.veventWithSimpleProperties(start, end, "TRANSP", "OPAQUE");
        final Appointment appointment = parseAppointment(icalText, utc);

        assertEquals(Appointment.RESERVED, appointment.getShownAs());
    }

    public void testAppFree() throws ConversionError {
        final Date start = D("24/02/1981 10:00");
        final Date end = D("24/02/1981 12:00");

        final TimeZone utc = TimeZone.getTimeZone("UTC");

        final String icalText = fixtures.veventWithSimpleProperties(start, end, "TRANSP", "TRANSPARENT");
        final Appointment appointment = parseAppointment(icalText, utc);

        assertEquals(Appointment.FREE, appointment.getShownAs());
    }

    public void testAppAttendees() throws ConversionError {
        final Date start = D("24/02/1981 10:00");
        final Date end = D("24/02/1981 12:00");

        final TimeZone utc = TimeZone.getTimeZone("UTC");

        String[] mails = new String[3];
        int i = 0;
        for(final User user : U(1,2,5)) {
            mails[i++] = user.getMail();
        }

        // Internal Users

        String icalText = fixtures.veventWithAttendees(start, end, mails);
        Appointment appointment = parseAppointment(icalText, utc);

        final Set<Integer> ids = new HashSet<Integer>(Arrays.asList(1,2,5));
        assertNotNull(appointment.getParticipants());
        for(final Participant p  : appointment.getParticipants()) {
            assertTrue(UserParticipant.class.isAssignableFrom(p.getClass()));
            final UserParticipant participant = (UserParticipant) p;
            assertTrue(ids.remove(participant.getIdentifier()));
        }
        assertTrue(ids.isEmpty());
        

        // TODO: Status ?

        // External Users

        mails = new String[]{"mickey@disney.invalid", "donald@disney.invalid", "goofy@disney.invalid"};
        icalText = fixtures.veventWithAttendees(start, end, mails);
        appointment = parseAppointment(icalText, utc);


        final Set<String> mailSet = new HashSet<String>(Arrays.asList(mails));
        assertNotNull(appointment.getParticipants());
        for(final Participant p  : appointment.getParticipants()) {
            assertTrue(ExternalUserParticipant.class.isAssignableFrom(p.getClass()));
            final ExternalUserParticipant participant = (ExternalUserParticipant) p;
            assertTrue(mailSet.remove(participant.getEmailAddress()));
        }
        assertTrue(mailSet.isEmpty());

    }

    public void testAppResources() throws ConversionError {
     
        final Date start = D("24/02/1981 10:00");
        final Date end = D("24/02/1981 12:00");

        final TimeZone utc = TimeZone.getTimeZone("UTC");

        final String[] resources = new String[]{"Toaster", "Deflector", "Subspace Anomaly"};

        final String icalText = fixtures.veventWithResources(start, end, resources);
        final Appointment appointment = parseAppointment(icalText, utc);

        final Set<Integer> resourceSet = new HashSet<Integer>(Arrays.asList(1,2,3));

        assertNotNull(appointment.getParticipants());
        for(final Participant p  : appointment.getParticipants()) {
            assertTrue(ResourceParticipant.class.isAssignableFrom(p.getClass()));
            final ResourceParticipant participant = (ResourceParticipant) p;
            assertTrue(resourceSet.remove(participant.getIdentifier()));
        }

        assertTrue(resourceSet.isEmpty());
        
    }

    public void testAppResourcesInParticipants() throws ConversionError {
        // Resources can also be specified in the attendee property with a cutype
        final Date start = D("24/02/1981 10:00");
        final Date end = D("24/02/1981 12:00");

        final TimeZone utc = TimeZone.getTimeZone("UTC");

        final String[] resources = new String[]{"Toaster", "Deflector", "Subspace Anomaly"};

        final String icalText = fixtures.veventWithResourcesInAttendees(start, end, resources);
        final Appointment appointment = parseAppointment(icalText, utc);

        final Set<Integer> resourceSet = new HashSet<Integer>(Arrays.asList(1,2,3));

        assertNotNull(appointment.getParticipants());
        for(final Participant p  : appointment.getParticipants()) {
            assertTrue(ResourceParticipant.class.isAssignableFrom(p.getClass()));
            final ResourceParticipant participant = (ResourceParticipant) p;
            assertTrue("Didn't expect: "+participant.getIdentifier(), resourceSet.remove(participant.getIdentifier()));
        }

        assertTrue(resourceSet.toString(), resourceSet.isEmpty());

    }

    public void testAppCategories() throws ConversionError {
        final Date start = D("24/02/1981 10:00");
        final Date end = D("24/02/1981 12:00");

        final TimeZone utc = TimeZone.getTimeZone("UTC");

        final String[] categories = new String[]{"Toaster", "Deflector", "Subspace Anomaly"};
        final String categoriesString = "Toaster,Deflector,Subspace Anomaly";

        final String icalText = fixtures.veventWithCategories(start, end, categories);
        final Appointment appointment = parseAppointment(icalText, utc);

        assertEquals(categoriesString, appointment.getCategories());
    }

    public void testAppRecurrence() throws ConversionError {

        final Date start = D("24/02/1981 10:00");
        final Date end =   D("24/02/1981 12:00");

        // DAILY

        Appointment appointment = appointmentWithRecurrence("FREQ=DAILY;INTERVAL=2;COUNT=3", start, end);
        assertEquals(3, appointment.getOccurrence());
        assertEquals(Appointment.DAILY, appointment.getRecurrenceType());
        assertEquals(2, appointment.getInterval());

        // WEEKLY

        appointment = appointmentWithRecurrence("FREQ=WEEKLY;INTERVAL=2;COUNT=3;BYDAY=MO,WE,FR", start, end);

        assertEquals(3, appointment.getOccurrence());
        assertEquals(Appointment.WEEKLY, appointment.getRecurrenceType());
        assertEquals(2, appointment.getInterval());

        int days = appointment.getDays();
        assertTrue(Appointment.MONDAY == (Appointment.MONDAY & days));
        assertTrue(Appointment.WEDNESDAY == (Appointment.WEDNESDAY & days));
        assertTrue(Appointment.FRIDAY == (Appointment.FRIDAY & days));

        assertFalse(Appointment.TUESDAY == (Appointment.TUESDAY & days));
        assertFalse(Appointment.THURSDAY == (Appointment.THURSDAY & days));
        assertFalse(Appointment.SATURDAY == (Appointment.SATURDAY & days));
        assertFalse(Appointment.SUNDAY == (Appointment.SUNDAY & days));


        // Default Day taken from DTSTART

        appointment = appointmentWithRecurrence("FREQ=WEEKLY;INTERVAL=2;COUNT=3", start, end);
        days = appointment.getDays();

        assertEquals(3, appointment.getOccurrence());
        assertEquals(Appointment.WEEKLY, appointment.getRecurrenceType());
        assertEquals(2, appointment.getInterval());
        assertTrue(Appointment.TUESDAY == (Appointment.TUESDAY & days)); // Start Date is a Tuesday

        // MONTHLY

        // First form: on 23rd day every 2 months

        appointment = appointmentWithRecurrence("FREQ=MONTHLY;INTERVAL=2;COUNT=3;BYMONTHDAY=23", start, end);
        assertEquals(3, appointment.getOccurrence());
        assertEquals(Appointment.MONTHLY, appointment.getRecurrenceType());
        assertEquals(2, appointment.getInterval());
        assertEquals(23, appointment.getDayInMonth());

        // Second form : the 3rd monday and tuesday every 2 months

        appointment = appointmentWithRecurrence("FREQ=MONTHLY;INTERVAL=2;COUNT=3;BYDAY=MO,TU;BYWEEKNO=3", start, end);
        days = appointment.getDays();
        assertEquals(3, appointment.getDayInMonth());
        assertTrue(Appointment.MONDAY == (Appointment.MONDAY & days));
        assertTrue(Appointment.TUESDAY == (Appointment.TUESDAY & days)); // Is this correct? Can an appintment recur on more than one day in MONTHLY series?


        // Second form : the last tuesday every 2 months

        appointment = appointmentWithRecurrence("FREQ=MONTHLY;INTERVAL=2;COUNT=3;BYDAY=TU;BYWEEKNO=-1", start, end);
        days = appointment.getDays();
        assertEquals(5, appointment.getDayInMonth());
        assertTrue(Appointment.TUESDAY == (Appointment.TUESDAY & days));


        // Default taken from start date

        appointment = appointmentWithRecurrence("FREQ=MONTHLY;INTERVAL=2;COUNT=3", start, end);
        assertEquals(24, appointment.getDayInMonth());

        appointment = appointmentWithRecurrence("FREQ=MONTHLY;INTERVAL=2;COUNT=3;BYWEEKNO=3", start, end);
        days = appointment.getDays();
        assertTrue(Appointment.TUESDAY == (Appointment.TUESDAY & days));


        // YEARLY

        // First form: Every 2 years, the 23rd of March
        appointment = appointmentWithRecurrence("FREQ=YEARLY;INTERVAL=2;COUNT=3;BYMONTHDAY=23;BYMONTH=3", start, end);

        assertEquals(Appointment.YEARLY, appointment.getRecurrenceType());
        assertEquals(2, appointment.getMonth());
        assertEquals(23, appointment.getDayInMonth());

        // Second form: 2nd monday and wednesday in april every 2 years

        appointment = appointmentWithRecurrence("FREQ=YEARLY;INTERVAL=2;COUNT=3;BYDAY=MO,WE;BYMONTH=4;BYWEEKNO=2", start, end);
        days = appointment.getDays();

        assertEquals(3, appointment.getMonth());
        assertEquals(2, appointment.getDayInMonth());
        assertTrue(Appointment.MONDAY == (Appointment.MONDAY & days));
        assertTrue(Appointment.WEDNESDAY == (Appointment.WEDNESDAY & days)); // Is this correct? Can an appintment recur on more than one day in YEARLY series?

        // Third form: every second sunday in april

        appointment = appointmentWithRecurrence("FREQ=YEARLY;BYDAY=2SU;BYMONTH=4", start, end);
        assertEquals(3, appointment.getMonth());
        assertEquals(2, appointment.getDayInMonth());
        assertEquals(Appointment.SUNDAY, appointment.getDays());
        assertEquals(1, appointment.getInterval());

        // Third form: last sunday in april
        appointment = appointmentWithRecurrence("FREQ=YEARLY;BYDAY=-1SU;BYMONTH=4", start, end);
        assertEquals(3, appointment.getMonth());
        assertEquals(5, appointment.getDayInMonth());
        assertEquals(Appointment.SUNDAY, appointment.getDays());
        assertEquals(1, appointment.getInterval());
        

        // Default

        appointment = appointmentWithRecurrence("FREQ=YEARLY;INTERVAL=2;COUNT=3", start, end);

        assertEquals(Appointment.YEARLY, appointment.getRecurrenceType());
        assertEquals(1, appointment.getMonth());
        assertEquals(24, appointment.getDayInMonth());


        // UNTIL

        appointment = appointmentWithRecurrence("FREQ=YEARLY;INTERVAL=2;UNTIL=19890423", start, end);

        assertEquals(D("23/04/1989 00:00"), appointment.getUntil());

    }

    public void testAppDeleteExceptions() throws ConversionError {
        final Date start = D("24/01/1981 10:00");
        final Date end = D("24/01/1981 12:00");

        final TimeZone utc = TimeZone.getTimeZone("UTC");

        final String rrule = "FREQ=DAILY;INTERVAL=2;COUNT=5";
        final Date[] exceptions = new Date[]{D("26/01/1981 12:00"), D("30/01/1981 12:00")};

        String icalText = fixtures.veventWithDeleteExceptionsAsDateTime(start, end, rrule, exceptions);
        Appointment appointment = parseAppointment(icalText, utc);

        // Do we need the "time" in an exception?

        Set<Date> expectedExceptions = new HashSet<Date>(Arrays.asList(exceptions));

        assertNotNull(appointment.getDeleteException());
        for(final Date exception  : appointment.getDeleteException()) {
            assertTrue("Didn't expect: "+exception+ " Expected one of: "+expectedExceptions, expectedExceptions.remove(exception));
        }
        assertTrue(expectedExceptions.isEmpty());

        icalText = fixtures.veventWithDeleteExceptionsAsDate(start, end, rrule, exceptions);
        
        appointment = parseAppointment(icalText, utc);


        expectedExceptions = new HashSet<Date>(Arrays.asList(D("26/01/1981 00:00"), D("30/01/1981 00:00")));


        assertNotNull(appointment.getDeleteException());
        for(final Date exception  : appointment.getDeleteException()) {
            assertTrue("Didn't expect: "+exception+ " Expected one of: "+expectedExceptions, expectedExceptions.remove(exception));
        }
        assertTrue(expectedExceptions.isEmpty());
        assertEquals("UTC", appointment.getTimezoneFallbackUTC());


    }

    public void testAppAlarms() throws ConversionError {
        // Relative to Start (default)

        final Date start = D("24/02/1981 10:00");
        final Date end = D("24/02/1981 12:00");

        final TimeZone utc = TimeZone.getTimeZone("UTC");

        final int MINUTES = 1;

        String icalText = fixtures.veventWithDisplayAlarm(start, end, "TRIGGER:-PT15M", "Description");
        Appointment appointment = parseAppointment(icalText, utc);


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
        final String icalText = fixtures.veventWithWholeDayEvent(D("24/02/1990 12:00"));
        final Appointment appointment = parseAppointment(icalText);

        assertTrue(appointment.getFullTime());

        Calendar start = Calendar.getInstance();
        start.setTimeZone(TimeZone.getTimeZone("UTC"));
        start.setTime(appointment.getStartDate());

        assertEquals(1990, start.get(Calendar.YEAR));
        assertEquals(Calendar.FEBRUARY, start.get(Calendar.MONTH));
        assertEquals(24, start.get(Calendar.DAY_OF_MONTH));
        assertEquals(0, start.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, start.get(Calendar.MINUTE));
        assertEquals(0, start.get(Calendar.SECOND));


        Calendar end = Calendar.getInstance();
        end.setTimeZone(TimeZone.getTimeZone("UTC"));
        end.setTime(appointment.getEndDate());

        assertEquals(1990, end.get(Calendar.YEAR));
        assertEquals(Calendar.FEBRUARY, end.get(Calendar.MONTH));
        assertEquals(25, end.get(Calendar.DAY_OF_MONTH));
        assertEquals(0, end.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, end.get(Calendar.MINUTE));
        assertEquals(0, end.get(Calendar.SECOND));
    }

    // Tasks

    public void testTskTitle() throws ConversionError {
        final String icalText = fixtures.vtodoWithSimpleProperties("SUMMARY", "A nice title");
        final Task task = parseTask(icalText, TimeZone.getTimeZone("UTC"));

        assertEquals("A nice title", task.getTitle());
    }


    public void testTskCreated() throws ConversionError {
        final String icalText = fixtures.vtodoWithSimpleProperties("CREATED", "20081023T100000Z");
        final Task task = parseTask(icalText);

        assertEquals(D("23/10/2008 10:00"), task.getCreationDate());

    }

    public void testTskLastModified() throws ConversionError {
        final String icalText = fixtures.vtodoWithSimpleProperties("LAST-MODIFIED", "20081023T100000Z");
        final Task task = parseTask(icalText);

        assertEquals(D("23/10/2008 10:00"), task.getLastModified());

    }

    public void testTskDTSTAMP() throws ConversionError {
        final String icalText = fixtures.vtodoWithSimpleProperties("DTSTAMP", "20081023T100000Z");
        final Task task = parseTask(icalText);

        assertEquals(D("23/10/2008 10:00"), task.getCreationDate());
    }

    public void testTskNote() throws ConversionError {
        final String icalText = fixtures.vtodoWithSimpleProperties("DESCRIPTION", "A nice description");
        final Task task = parseTask(icalText, TimeZone.getTimeZone("UTC"));

        assertEquals("A nice description", task.getNote());
            
    }

    public void testTskStartToEnd() throws ConversionError {
        final Date start = D("24/02/1981 10:00");
        final Date end = D("24/02/1981 12:00");

        final TimeZone utc = TimeZone.getTimeZone("UTC");

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

        final TimeZone timeZone = TimeZone.getTimeZone("America/Los_Angeles");
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
        final Date start = D("24/02/1981 10:00");
        final Date end = D("24/02/1981 12:00");
        final String duration = "P2H"; // 2 hours

        final TimeZone utc = TimeZone.getTimeZone("UTC");

        final String icalText = fixtures.vtodoWithLocalDTStartAndDuration(start, duration);
        final Task task = parseTask(icalText, utc);

        assertEquals(start, task.getStartDate());
        assertEquals(end, task.getEndDate());

    }

    public void testTskDue() throws ConversionError {
        final Date due = D("24/03/1981 10:00");

        final TimeZone utc = TimeZone.getTimeZone("UTC");

        final String icalText = fixtures.vtodoWithDueDate(due);
        final Task task = parseTask(icalText, utc);

        assertEquals(due, task.getEndDate());
    }
    
    public void testTskDueWithoutTimeZone() throws ConversionError {
        final Date due = D("31/07/2007 10:00");

        final TimeZone utc = TimeZone.getTimeZone("Europe/Berlin"); // Should have no effect

        final String icalText = fixtures.vtodoWithDueDateWithoutTZ(due);
        final Task task = parseTask(icalText, utc);

        assertEquals(D("31/07/2007 00:00"), task.getEndDate());
    }
    
    

    public void testTskPrivateFlag() throws ConversionError {
        final TimeZone utc = TimeZone.getTimeZone("UTC");

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
        final Date dateCompleted = D("24/03/1981 10:00");

        final TimeZone utc = TimeZone.getTimeZone("UTC");

        final String icalText = fixtures.vtodoWithDateCompleted(dateCompleted);
        final Task task = parseTask(icalText, utc);

        assertEquals(dateCompleted, task.getDateCompleted());
    }

    public void testTskPercentComplete() throws ConversionError {
        final String icalText = fixtures.vtodoWithSimpleProperties("PERCENT-COMPLETE", "23");
        final Task task = parseTask(icalText);

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

    private void priorityTest(final int priority, final int expected) throws ConversionError {
        final String icalText = fixtures.vtodoWithSimpleProperties("PRIORITY", new Integer(priority).toString());
        final Task task = parseTask(icalText);
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

    private void statusTest(final String status, final int expected) throws ConversionError {
        final String icalText = fixtures.vtodoWithSimpleProperties("STATUS", status);
        final Task task = parseTask(icalText);
        assertEquals("Invalid interpretation for status "+status, expected, task.getStatus());
    }

    public void testTskAttendees() throws ConversionError {
        final TimeZone utc = TimeZone.getTimeZone("UTC");

        String[] mails = new String[3];
        int i = 0;
        for(final User user : U(1,2,5)) {
            mails[i++] = user.getMail();
        }

        // Internal Users

        String icalText = fixtures.vtodoWithAttendees(mails);
        Task appointment = parseTask(icalText, utc);

        final Set<Integer> ids = new HashSet<Integer>(Arrays.asList(1,2,5));
        assertNotNull(appointment.getParticipants());
        for(final Participant p  : appointment.getParticipants()) {
            assertTrue(UserParticipant.class.isAssignableFrom(p.getClass()));
            final UserParticipant participant = (UserParticipant) p;
            assertTrue(ids.remove(participant.getIdentifier()));
        }
        assertTrue(ids.isEmpty());


        // TODO: Status ?

        // External Users

        mails = new String[]{"mickey@disney.invalid", "donald@disney.invalid", "goofy@disney.invalid"};
        icalText = fixtures.vtodoWithAttendees(mails);
        appointment = parseTask(icalText, utc);


        final Set<String> mailSet = new HashSet<String>(Arrays.asList(mails));
        assertNotNull(appointment.getParticipants());
        for(final Participant p  : appointment.getParticipants()) {
            assertTrue(ExternalUserParticipant.class.isAssignableFrom(p.getClass()));
            final ExternalUserParticipant participant = (ExternalUserParticipant) p;
            assertTrue(mailSet.remove(participant.getEmailAddress()));
        }
        assertTrue(mailSet.isEmpty());
        
    }

    public void testTskCategories() throws ConversionError {
        final TimeZone utc = TimeZone.getTimeZone("UTC");

        final String[] categories = new String[]{"Toaster", "Deflector", "Subspace Anomaly"};
        final String categoriesString = "Toaster,Deflector,Subspace Anomaly";

        final String icalText = fixtures.vtodoWithCategories(categories);
        final Task task = parseTask(icalText, utc);

        assertEquals(categoriesString, task.getCategories());
    }

    public void testTskRecurrence() throws ConversionError {
        final Date start = D("24/02/1981 10:00");
        final Date end =   D("24/02/1981 12:00");

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

    /**
     * Tasks do not have delete exceptions.
     */
    public void notestTskDeleteExceptions() throws ConversionError {
        final Date start = D("24/01/1981 10:00");
        final Date end = D("24/01/1981 12:00");

        final TimeZone utc = TimeZone.getTimeZone("UTC");

        final String rrule = "FREQ=DAILY;INTERVAL=2;COUNT=5";
        final Date[] exceptions = new Date[]{D("26/01/1981 12:00"), D("30/01/1981 12:00")};

        String icalText = fixtures.vtodoWithDeleteExceptionsAsDateTime(start, end, rrule, exceptions);
        Task task = parseTask(icalText, utc);

        // Do we need the "time" in an exception?

        Set<Date> expectedExceptions = new HashSet<Date>(Arrays.asList(exceptions));

        assertNotNull(task.getDeleteException());
        for(final Date exception  : task.getDeleteException()) {
            assertTrue("Didn't expect: "+exception+ " Expected one of: "+expectedExceptions, expectedExceptions.remove(exception));
        }
        assertTrue(expectedExceptions.isEmpty());

        icalText = fixtures.vtodoWithDeleteExceptionsAsDate(start, end, rrule, exceptions);

        task = parseTask(icalText, utc);


        expectedExceptions = new HashSet<Date>(Arrays.asList(D("26/01/1981 00:00"), D("30/01/1981 00:00")));


        assertNotNull(task.getDeleteException());
        for(final Date exception  : task.getDeleteException()) {
            assertTrue("Didn't expect: "+exception+ " Expected one of: "+expectedExceptions, expectedExceptions.remove(exception));
        }
        assertTrue(expectedExceptions.isEmpty());
    }

    public void testTskAlarms() throws ConversionError {
        // Relative to Start (default)

        final Date start = D("24/02/1981 10:00");
        final Date end = D("24/02/1981 12:00");

        final TimeZone utc = TimeZone.getTimeZone("UTC");


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
        final String icalText = fixtures.veventWithEnd(D("24/02/1981 10:00"));
        assertErrorWhenParsingAppointment(icalText, "Missing DTSTART");
    }

    public void testShouldThrowErrorOnNonICalFile() throws ConversionError {
        final String noIcalText = "I am not an iCal file.";
        try {
            parseAppointment(noIcalText);
            fail("Excpected Exception");
        } catch (ConversionError e) {
            assertEquals("This does not look like an iCal file. Please check the file.", e.getOrigMessage());
        }
    }

    
    public void testAppShouldIncludeWarningForAdditionalRecurrences() throws ConversionError {
        final String icalText = fixtures.veventWithTwoRecurrences(D("24/02/1981 10:00"), D("24/02/1981 12:00"));
        assertWarningWhenParsingAppointment(icalText, "Only converting first recurrence rule, additional recurrence rules will be ignored.");
    }

    public void testAppShouldWarnOnUnsupportedRecurrenceIntervals() throws ConversionError {
        warningOnAppRecurrence("FREQ=SECONDLY;INTERVAL=1;COUNT=3", "Can only convert DAILY, WEEKLY, MONTHLY and YEARLY recurrences");
        warningOnAppRecurrence("FREQ=MINUTELY;INTERVAL=1;COUNT=3", "Can only convert DAILY, WEEKLY, MONTHLY and YEARLY recurrences");
        warningOnAppRecurrence("FREQ=HOURLY;INTERVAL=1;COUNT=3", "Can only convert DAILY, WEEKLY, MONTHLY and YEARLY recurrences");

    }

    public void testAppShouldInlcudeWarningOnUnkownClass() throws ConversionError {
        final String icalText = fixtures.veventWithSimpleProperties(D("24/02/1981 10:00"), D("24/02/1981 12:00"), "CLASS", "SUPERCALIFRAGILISTICEXPLIALIDOCIOUS");
        assertWarningWhenParsingAppointment(icalText, "Unknown Class: SUPERCALIFRAGILISTICEXPLIALIDOCIOUS");
    }

    public void testAppShouldIncludeWarningOnUndisplayableAlarms() throws ConversionError {
        final String icalText = fixtures.veventWithAudioAlarm(D("24/02/1981 10:00"), D("24/02/1981 12:00"), "TRIGGER:-PT15M", "alarm.wav");
        assertWarningWhenParsingAppointment(icalText, "Can only convert DISPLAY alarms with triggers");
    }

    public void testAppShouldIncludeErrorForUnknownDayInRRule() throws ConversionError {
        final String icalText =  fixtures.veventWithSimpleProperties(D("24/02/1981 10:00"), D("24/02/1981 12:00"), "RRULE", "FREQ=MONTHLY;INTERVAL=2;COUNT=3;BYDAY=WU,NI;BYWEEKNO=3");
        assertErrorWhenParsingAppointment(icalText, "Unknown day: WU");    
    }

    public void testAppShouldWarnOnUnhandleableFields() throws ConversionError {
        //TODO        
    }


    // Bug 11987
    public void testMultipleCalendars() throws ConversionError {
        final Date start1 = D("24/02/1981 10:00");
        final Date end1 = D("24/02/1981 12:00");

        final Date start2 = D("24/02/1981 10:00");
        final Date end2 = D("24/02/1981 12:00");

        final StringBuilder combiner = new StringBuilder();
        combiner.append(fixtures.veventWithUTCDTStartAndDTEnd(start1, end1))
                .append("\n")
                .append(fixtures.veventWithUTCDTStartAndDTEnd(start2, end2));

        final List<CalendarDataObject> appointments = parser.parseAppointments(combiner.toString(), TimeZone.getTimeZone("UTC"),null, new ArrayList<ConversionError>(), new ArrayList<ConversionWarning>());

        assertEquals(2, appointments.size());
    }

    // Bug 11869
    public void testAppShouldCorrectParticipantsInPrivateAppoitment() throws ConversionError {
        final Date start = D("24/02/1981 10:00");
        final Date end =   D("24/02/1981 12:00");
        final String icalText = fixtures.veventWithSimpleProperties(start, end,
        "CLASS"    ,    "PRIVATE",
        "ATTENDEE" ,     "MAILTO:mickey@disney.invalid");

        assertWarningWhenParsingAppointment(icalText, "Private Appointments can not have attendees. Removing attendees and accepting appointment anyway.");

        final Appointment appointment = parseAppointment(icalText);

        assertNull(appointment.getParticipants());
    }
    

    public void testAppShouldInterpretConfidentialAsPrivate() throws ConversionError {
        final String icalText = fixtures.veventWithSimpleProperties(D("24/02/1981 10:00"), D("24/02/1981 12:00"), "CLASS", "CONFIDENTIAL");
        final Appointment appointment = parseAppointment(icalText);
        
        assertTrue("CLASS:CONFIDENTIAL should resolve to private appointments", appointment.getPrivateFlag());
    }

    

    // Bug 11958 - a timezone element in a file should be relevant for all data, even if listed afterwards
    public void testTimezoneShouldBeRelevantForAllData() throws ConversionError{
    	final String timezone = "BEGIN:VTIMEZONE\nTZID:/mozilla.org/20050126_1/America/New_York\nX-LIC-LOCATION:America/New_York\nBEGIN:STANDARD\nTZOFFSETFROM:-0400\nTZOFFSETTO:-0500\nTZNAME:EST\nDTSTART:19701025T020000\nRRULE:FREQ=YEARLY;INTERVAL=1;BYDAY=-1SU;BYMONTH=10\nEND:STANDARD\nBEGIN:DAYLIGHT\nTZOFFSETFROM:-0500\nTZOFFSETTO:-0400\nTZNAME:EDT\nDTSTART:19700405T020000\nRRULE:FREQ=YEARLY;INTERVAL=1;BYDAY=1SU;BYMONTH=4\nEND:DAYLIGHT\nEND:VTIMEZONE\n";
    	final String icalText1 = "BEGIN:VCALENDAR\nVERSION:2.0\nPRODID:-//Mozilla.org/NONSGML Mozilla Calendar V1.1//EN\n"+timezone+"BEGIN:VEVENT\nCREATED:20080216T152600Z\nLAST-MODIFIED:20080216T152600Z\nDTSTAMP:20080216T152600Z\nUID:3a289f91-f83a-4614-83c6-660c7740abd8\nSUMMARY:New York, 2008-08-31 09:00 - 10:00 (EST)\nDTSTART;TZID=/mozilla.org/20050126_1/America/New_York:20080831T090000\nDTEND;TZID=/mozilla.org/20050126_1/America/New_York:20080831T100000\nEND:VEVENT\nEND:VCALENDAR\n";
    	final String icalText2 = "BEGIN:VCALENDAR\nVERSION:2.0\nPRODID:-//Mozilla.org/NONSGML Mozilla Calendar V1.1//EN\nBEGIN:VEVENT\nCREATED:20080216T152600Z\nLAST-MODIFIED:20080216T152600Z\nDTSTAMP:20080216T152600Z\nUID:3a289f91-f83a-4614-83c6-660c7740abd8\nSUMMARY:New York, 2008-08-31 09:00 - 10:00 (EST)\nDTSTART;TZID=/mozilla.org/20050126_1/America/New_York:20080831T090000\nDTEND;TZID=/mozilla.org/20050126_1/America/New_York:20080831T100000\nEND:VEVENT\n"+ timezone + "END:VCALENDAR\n";
    	final Appointment appointmentThatTroublesUs = parseAppointment(icalText1);
        final Appointment appointmentAsExpected = parseAppointment(icalText2);
        assertEquals("Start dates should be equal, independent of the placement of the timezone information", 
        		appointmentAsExpected.getStartDate(), 
        		appointmentThatTroublesUs.getStartDate());
        assertEquals("End dates should be equal, independent of the placement of the timezone information", 
        		appointmentAsExpected.getEndDate(), 
        		appointmentThatTroublesUs.getEndDate());
    }
    
    /* Bug 7470 - part III: Multiple line arguments are supposed to be 'unfolded'
     * according to RFC 2445 - iCal4J does not seem to do this. This test is meant to confirm
     * the wrapping parser does handle the case.
     */
    public void testShouldHandleMultipleLineArguments() throws ConversionError{
    	final String foldedSummary = "This is a so called 'folded' argument\n meaning it is split\n into multiple lines\n each starting with a whitespace character";
    	final String unfoldedSummary = foldedSummary.replace("\n ","");
    	final String icalText = fixtures.veventWithSimpleProperties(new Date(), new Date(), "SUMMARY", foldedSummary);
    	final Appointment appointment = parseAppointment(icalText);
    	assertEquals("Unfolded summary does not match transformed title", unfoldedSummary, appointment.getTitle());
    }

    @Override
    protected void setUp() throws Exception {
        fixtures = new ICALFixtures();
        users = new MockUserLookup();
        parser = new ICal4JParser();
        oldUserResolver = Participants.userResolver;
        Participants.userResolver = new UserResolver(){
            public List<User> findUsers(final List<String> mails, final Context ctx) {
                final List<User> found = new LinkedList<User>();
                for(final String mail : mails) {
                    final User user = ICalParserTest.this.users.getUserByMail(mail);
                    if(user != null) {
                        found.add( user );
                    }
                }

                return found;
            }

            public User loadUser(final int userId, final Context ctx) throws UserException {
                return ICalParserTest.this.users.getUser(userId);
            }
        };
        oldResourceResolver = Participants.resourceResolver;
        Participants.resourceResolver = new ResourceResolver() {
            private final List<Resource> resources = new ArrayList<Resource>() {{
                final Resource toaster = new Resource();
                toaster.setDisplayName("Toaster");
                toaster.setIdentifier(1);
                add(toaster);

                final Resource deflector = new Resource();
                deflector.setDisplayName("Deflector");
                deflector.setIdentifier(2);
                add(deflector);

                final Resource subspaceAnomaly = new Resource();
                subspaceAnomaly.setDisplayName("Subspace Anomaly");
                subspaceAnomaly.setIdentifier(3);
                add(subspaceAnomaly);
            }};

            public List<Resource> find(final List<String> names, final Context ctx)
                throws ResourceException, ServiceException {
                final List<Resource> retval = new ArrayList<Resource>();
                for(final String name : names) {
                    for(final Resource resource : resources) {
                        if(resource.getDisplayName().equals(name)) {
                            retval.add(resource);
                        }
                    }
                }
                return retval;
            }
            public Resource load(final int resourceId, final Context ctx)
                throws ResourceException, ServiceException {
                return null;
            }
        };
    }
    

    protected List<User> U(final int...ids) {
        final List<User> found = new LinkedList<User>();
        for(final int i : ids) {
            try {
                found.add( users.getUser(i) );
            } catch (final UserException e) {
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

    protected Appointment parseAppointment(final String icalText, final TimeZone defaultTZ) throws ConversionError {
        return parser.parseAppointments(icalText, defaultTZ, new ContextImpl(23), new ArrayList<ConversionError>() , new ArrayList<ConversionWarning>() ).get(0);
    }

    protected Appointment parseAppointment(final String icalText) throws ConversionError {
        return parseAppointment(icalText, TimeZone.getDefault());
    }

    protected Task parseTask(final String icalText,  final TimeZone defaultTZ) throws ConversionError {
        return parser.parseTasks(icalText, defaultTZ, new ContextImpl(23), new ArrayList<ConversionError>() , new ArrayList<ConversionWarning>()).get(0);
    }

    protected Task parseTask(final String icalText) throws ConversionError {
        return parser.parseTasks(icalText, TimeZone.getDefault(), new ContextImpl(23), new ArrayList<ConversionError>() , new ArrayList<ConversionWarning>()).get(0);
    }

    protected Appointment appointmentWithRecurrence(final String recurrence, final Date start, final Date end) throws ConversionError {

        final TimeZone utc = TimeZone.getTimeZone("UTC");

        final String icalText = fixtures.veventWithSimpleProperties(start, end, "RRULE", recurrence);
        final Appointment appointment = parseAppointment(icalText, utc);

        return appointment;
    }

    protected Task taskWithRecurrence(final String recurrence, final Date start, final Date end) throws ConversionError {

        final TimeZone utc = TimeZone.getTimeZone("UTC");

        final String icalText = fixtures.vtodoWithSimpleProperties(start, end, "RRULE", recurrence);
        final Task task = parseTask(icalText, utc);
        
        return task;
    }

    protected void assertWarningWhenParsingAppointment(final String icalText, final String warning) throws ConversionError {
        final ArrayList<ConversionError> errors = new ArrayList<ConversionError>();
        final ArrayList<ConversionWarning> warnings = new ArrayList<ConversionWarning>();
        final List<CalendarDataObject> result = parser.parseAppointments(icalText, TimeZone.getTimeZone("UTC"), new ContextImpl(23), errors, warnings);

        assertTrue(0 != result.size()); // Warnings don't abort parsing of the object
        assertEquals(1, warnings.size());
        assertEquals(warning, warnings.get(0).getFormattedMessage());
    }

    protected void assertErrorWhenParsingAppointment(final String icalText, final String error) throws ConversionError {
        final ArrayList<ConversionError> errors = new ArrayList<ConversionError>();
        final ArrayList<ConversionWarning> warnings = new ArrayList<ConversionWarning>();
        parser.parseAppointments(icalText, TimeZone.getTimeZone("UTC"), new ContextImpl(23), errors, warnings);
        assertEquals(1, errors.size());
        assertEquals(error, errors.get(0).getFormattedMessage());

    }

    protected void warningOnAppRecurrence(final String recurrence, final String warning) throws ConversionError {
        final String icalText = fixtures.veventWithSimpleProperties(D("24/02/1981 10:00"), D("24/02/1981 12:00"), "RRULE", recurrence);
        assertWarningWhenParsingAppointment(icalText, warning);

    }
}
