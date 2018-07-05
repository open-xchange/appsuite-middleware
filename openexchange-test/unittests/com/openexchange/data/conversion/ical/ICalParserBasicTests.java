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

package com.openexchange.data.conversion.ical;

import static com.openexchange.groupware.calendar.tools.CommonAppointments.D;
import static com.openexchange.groupware.calendar.tools.CommonAppointments.recalculate;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import org.junit.Test;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.ExternalUserParticipant;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.contexts.impl.ContextImpl;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.tasks.Task;

/**
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class ICalParserBasicTests extends AbstractICalParserTest {

    @Test
    public void testTskTitle() throws ConversionError {
        final String icalText = fixtures.vtodoWithSimpleProperties("SUMMARY", "A nice title");
        final Task task = parseTask(icalText, TimeZone.getTimeZone("UTC"));

        assertEquals("A nice title", task.getTitle());
    }

    @Test
    public void testTskCreated() throws ConversionError {
        final String icalText = fixtures.vtodoWithSimpleProperties("CREATED", "20081023T100000Z");
        final Task task = parseTask(icalText);

        assertEquals(D("23/10/2008 10:00"), task.getCreationDate());

    }

    @Test
    public void testTskLastModified() throws ConversionError {
        final String icalText = fixtures.vtodoWithSimpleProperties("LAST-MODIFIED", "20081023T100000Z");
        final Task task = parseTask(icalText);

        assertEquals(D("23/10/2008 10:00"), task.getLastModified());

    }

    @Test
    public void testTskDTSTAMP() throws ConversionError {
        final String icalText = fixtures.vtodoWithSimpleProperties("DTSTAMP", "20081023T100000Z");
        final Task task = parseTask(icalText);

        assertEquals(D("23/10/2008 10:00"), task.getCreationDate());
    }

    @Test
    public void testTskNote() throws ConversionError {
        final String icalText = fixtures.vtodoWithSimpleProperties("DESCRIPTION", "A nice description");
        final Task task = parseTask(icalText, TimeZone.getTimeZone("UTC"));

        assertEquals("A nice description", task.getNote());

    }

    @Test
    public void testTskStartToEnd() throws ConversionError {
        final Date start = D("24/02/1981 10:00");
        final Date end = D("24/02/1981 12:00");

        final TimeZone utc = TimeZone.getTimeZone("UTC");

        // Local Time

        String icalText = fixtures.vtodoWithLocalDTStartAndDue(start, end);
        Task task = parseTask(icalText, utc);

        assertEquals(start, task.getStartDate());
        assertEquals(end, task.getEndDate());

        // UTC

        icalText = fixtures.vtodoWithUTCDTStartAndDue(start, end);
        task = parseTask(icalText);

        assertEquals(start, task.getStartDate());
        assertEquals(end, task.getEndDate());

        // Known TZID

        final TimeZone timeZone = TimeZone.getTimeZone("America/Los_Angeles");
        assertFalse("Bad test TimeZone", timeZone.equals(TimeZone.getDefault()));

        icalText = fixtures.vtodoWithDTStartAndDueInTimeZone(start, end, timeZone);
        task = parseTask(icalText);

        assertEquals(recalculate(start, utc, timeZone), task.getStartDate());
        assertEquals(recalculate(end, utc, timeZone), task.getEndDate());

        // VTIMEZONE

        icalText = fixtures.vtodoWithDTStartAndDueInCustomTimezone(start, end);
        task = parseTask(icalText);

        assertEquals(D("24/02/1981 01:00"), task.getStartDate());
        assertEquals(D("24/02/1981 03:00"), task.getEndDate());
    }

    @Test
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

    @Test
    public void testTskDue() throws ConversionError {
        final Date due = D("24/03/1981 10:00");

        final TimeZone utc = TimeZone.getTimeZone("UTC");

        final String icalText = fixtures.vtodoWithDueDate(due);
        final Task task = parseTask(icalText, utc);

        assertEquals(due, task.getEndDate());
    }

    @Test
    public void testTskDueWithoutTimeZone() throws ConversionError {
        final Date due = D("31/07/2007 10:00");

        final TimeZone utc = TimeZone.getTimeZone("Europe/Berlin"); // Should have no effect

        final String icalText = fixtures.vtodoWithDueDateWithoutTZ(due);
        final Task task = parseTask(icalText, utc);

        assertEquals(D("31/07/2007 00:00"), task.getEndDate());
    }

    @Test
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

    @Test
    public void testTskDateCompleted() throws ConversionError {
        final Date dateCompleted = D("24/03/1981 10:00");

        final TimeZone utc = TimeZone.getTimeZone("UTC");

        final String icalText = fixtures.vtodoWithDateCompleted(dateCompleted);
        final Task task = parseTask(icalText, utc);

        assertEquals(dateCompleted, task.getDateCompleted());
    }

    @Test
    public void testTskPercentComplete() throws ConversionError {
        final String icalText = fixtures.vtodoWithSimpleProperties("PERCENT-COMPLETE", "23");
        final Task task = parseTask(icalText);

        assertEquals(23, task.getPercentComplete());
    }

    @Test
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
        assertEquals("Invalid interpretation for priority " + priority, expected, task.getPriority().intValue());
    }

    // Bug 10401
    @Test
    public void testTskPriorityZero() throws ConversionError {
        priorityTest(0, Task.NORMAL);
    }

    @Test
    public void testTskStatus() throws ConversionError {
        statusTest("NEEDS-ACTION", Task.NOT_STARTED);
        statusTest("IN-PROCESS", Task.IN_PROGRESS);
        statusTest("COMPLETED", Task.DONE);
        statusTest("CANCELLED", Task.DEFERRED);
    }

    private void statusTest(final String status, final int expected) throws ConversionError {
        final String icalText = fixtures.vtodoWithSimpleProperties("STATUS", status);
        final Task task = parseTask(icalText);
        assertEquals("Invalid interpretation for status " + status, expected, task.getStatus());
    }

    @Test
    public void testTskAttendees() throws ConversionError {
        final TimeZone utc = TimeZone.getTimeZone("UTC");

        String[] mails = new String[3];
        int i = 0;
        for (final User user : U(1, 2, 5)) {
            mails[i++] = user.getMail();
        }

        // Internal Users

        String icalText = fixtures.vtodoWithAttendees(mails);
        Task appointment = parseTask(icalText, utc);

        final Set<Integer> ids = new HashSet<Integer>(Arrays.asList(1, 2, 5));
        assertNotNull(appointment.getParticipants());
        for (final Participant p : appointment.getParticipants()) {
            assertTrue(UserParticipant.class.isAssignableFrom(p.getClass()));
            final UserParticipant participant = (UserParticipant) p;
            assertTrue(ids.remove(participant.getIdentifier()));
        }
        assertTrue(ids.isEmpty());

        // TODO: Status ?

        // External Users

        mails = new String[] { "mickey@disney.invalid", "donald@disney.invalid", "goofy@disney.invalid" };
        icalText = fixtures.vtodoWithAttendees(mails);
        appointment = parseTask(icalText, utc);

        final Set<String> mailSet = new HashSet<String>(Arrays.asList(mails));
        assertNotNull(appointment.getParticipants());
        for (final Participant p : appointment.getParticipants()) {
            assertTrue(ExternalUserParticipant.class.isAssignableFrom(p.getClass()));
            final ExternalUserParticipant participant = (ExternalUserParticipant) p;
            assertTrue(mailSet.remove(participant.getEmailAddress()));
        }
        assertTrue(mailSet.isEmpty());

    }

    @Test
    public void testTskCategories() throws ConversionError {
        final TimeZone utc = TimeZone.getTimeZone("UTC");

        final String[] categories = new String[] { "Toaster", "Deflector", "Subspace Anomaly" };
        final String categoriesString = "Toaster,Deflector,Subspace Anomaly";

        final String icalText = fixtures.vtodoWithCategories(categories);
        final Task task = parseTask(icalText, utc);

        assertEquals(categoriesString, task.getCategories());
    }

    @Test
    public void testTskRecurrence() throws ConversionError {
        final Date start = D("24/02/1981 10:00");
        final Date end = D("24/02/1981 12:00");

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

        // Second form : the 3rd monday and tuesday every 2 months

        task = taskWithRecurrence("FREQ=MONTHLY;INTERVAL=2;COUNT=3;BYDAY=MO,TU;BYSETPOS=3", start, end);
        days = task.getDays();
        assertEquals(3, task.getDayInMonth());
        assertTrue(CalendarObject.MONDAY == (CalendarObject.MONDAY & days));
        assertTrue(CalendarObject.TUESDAY == (CalendarObject.TUESDAY & days)); // Is this correct? Can an appintment recur on more than one day in MONTHLY series?

        // Second form : the last tuesday every 2 months

        task = taskWithRecurrence("FREQ=MONTHLY;INTERVAL=2;COUNT=3;BYDAY=TU;BYSETPOS=-1", start, end);
        days = task.getDays();
        assertEquals(5, task.getDayInMonth());
        assertTrue(CalendarObject.TUESDAY == (CalendarObject.TUESDAY & days));

        // Default taken from start date

        task = taskWithRecurrence("FREQ=MONTHLY;INTERVAL=2;COUNT=3", start, end);
        assertEquals(24, task.getDayInMonth());

        // YEARLY

        // First form: Every 2 years, the 23rd of March
        task = taskWithRecurrence("FREQ=YEARLY;INTERVAL=2;COUNT=3;BYMONTHDAY=23;BYMONTH=3", start, end);

        assertEquals(CalendarObject.YEARLY, task.getRecurrenceType());
        assertEquals(2, task.getMonth());
        assertEquals(23, task.getDayInMonth());

        // Second form: 2nd monday and wednesday in april every 2 years

        task = taskWithRecurrence("FREQ=YEARLY;INTERVAL=2;COUNT=3;BYDAY=MO,WE;BYMONTH=4;BYSETPOS=2", start, end);
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
        final Date[] exceptions = new Date[] { D("26/01/1981 12:00"), D("30/01/1981 12:00") };

        String icalText = fixtures.vtodoWithDeleteExceptionsAsDateTime(start, end, rrule, exceptions);
        Task task = parseTask(icalText, utc);

        // Do we need the "time" in an exception?

        Set<Date> expectedExceptions = new HashSet<Date>(Arrays.asList(exceptions));

        assertNotNull(task.getDeleteException());
        for (final Date exception : task.getDeleteException()) {
            assertTrue("Didn't expect: " + exception + " Expected one of: " + expectedExceptions, expectedExceptions.remove(exception));
        }
        assertTrue(expectedExceptions.isEmpty());

        icalText = fixtures.vtodoWithDeleteExceptionsAsDate(start, end, rrule, exceptions);

        task = parseTask(icalText, utc);

        expectedExceptions = new HashSet<Date>(Arrays.asList(D("26/01/1981 00:00"), D("30/01/1981 00:00")));

        assertNotNull(task.getDeleteException());
        for (final Date exception : task.getDeleteException()) {
            assertTrue("Didn't expect: " + exception + " Expected one of: " + expectedExceptions, expectedExceptions.remove(exception));
        }
        assertTrue(expectedExceptions.isEmpty());
    }

    @Test
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

    @Test
    public void testTskUid() throws ConversionError {
        Date start = D("24/02/1981 10:00");
        Date end = D("24/02/1981 12:00");

        TimeZone utc = TimeZone.getTimeZone("UTC");

        String icalText = fixtures.vtodoWithSimpleProperties(start, end, "UID", "nrw3rn2983nxi");
        Task task = parseTask(icalText, utc);

        assertEquals("nrw3rn2983nxi", task.getUid());
    }

    public void no_testTskOrganizer() throws ConversionError {
        Date start = D("24/02/1981 10:00");
        Date end = D("24/02/1981 12:00");

        TimeZone utc = TimeZone.getTimeZone("UTC");

        String icalText = fixtures.vtodoWithSimpleProperties(start, end, "ORGANIZER", "mailto:bla@example.invalid");
        Task task = parseTask(icalText, utc);

        assertEquals("bla@example.invalid", task.getOrganizer());
    }

    // Errors and Warnings

    @Test
    public void testShouldLimitAmountOfImports() throws Exception {
        final ArrayList<ConversionError> errors = new ArrayList<ConversionError>();
        final ArrayList<ConversionWarning> warnings = new ArrayList<ConversionWarning>();
        try {
            parser.setLimit(10);
            List<Task> tasks = parser.parseTasks(fixtures.severalVtodos(20), TimeZone.getTimeZone("UTC"), new ContextImpl(23), errors, warnings).getImportedObjects();
            assertEquals("Ten tasks only", 10, tasks.size());
        } finally {
            parser.setLimit(-1);
        }
    }
}
