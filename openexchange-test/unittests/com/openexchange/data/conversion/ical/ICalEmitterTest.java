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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import junit.framework.TestCase;

import com.openexchange.data.conversion.ical.ical4j.ICal4JEmitter;
import com.openexchange.groupware.container.*;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.groupware.tasks.ExternalParticipant;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class ICalEmitterTest extends TestCase {

    private ICal4JEmitter emitter;

    private AppointmentObject getDefault() {
        AppointmentObject app = new AppointmentObject();

        Date start = D("24/02/1981 10:00");
        Date end = D("24/02/1981 12:00");

        app.setStartDate(start);
        app.setEndDate(end);

        return app;

    }

    public void testSimpleAppointment() throws Exception {
        AppointmentObject app = new AppointmentObject();

        app.setTitle("The Title");
        app.setNote("The Note");
        app.setCategories("cat1, cat2, cat3");
        app.setLocation("The Location");

        Date start = D("24/02/1981 10:00");
        Date end = D("24/02/1981 12:00");

        app.setStartDate(start);
        app.setEndDate(end);

        ICalFile ical = serialize(app);

        assertStandardAppFields(ical, start, end);
        assertProperty(ical, "SUMMARY","The Title");
        assertProperty(ical, "DESCRIPTION","The Note");
        assertProperty(ical, "CATEGORIES","cat1, cat2, cat3");
        assertProperty(ical, "LOCATION","The Location");
    }

    public void testAppRecurrence() throws IOException {

        // DAILY

        AppointmentObject appointment = getDefault();
        appointment.setRecurrenceCount(3);
        appointment.setRecurrenceType(AppointmentObject.DAILY);
        appointment.setInterval(2);

        ICalFile ical = serialize(appointment);

        assertProperty(ical, "RRULE", "FREQ=DAILY;INTERVAL=2;COUNT=3");

        // WEEKLY

        appointment.setRecurrenceType(AppointmentObject.WEEKLY);

        int days = 0;
        days |= AppointmentObject.MONDAY;
        days |= AppointmentObject.WEDNESDAY;
        days |= AppointmentObject.FRIDAY;

        appointment.setDays(days);

        ical = serialize(appointment);

        assertProperty(ical, "RRULE", "FREQ=WEEKLY;INTERVAL=2;COUNT=3;BYDAY=MO,WE,FR");

        // MONTHLY

        // First form: on 23rd day every 2 months

        appointment.setRecurrenceType(AppointmentObject.MONTHLY);
        appointment.removeDays();
        appointment.setDayInMonth(23);

        ical = serialize(appointment);

        assertProperty(ical, "RRULE", "FREQ=MONTHLY;INTERVAL=2;COUNT=3;BYMONTHDAY=23");


        // Second form : the 2nd monday and tuesday every 2 months

        appointment.setDayInMonth(3);

        days = 0;
        days |= AppointmentObject.MONDAY;
        days |= AppointmentObject.TUESDAY;
        appointment.setDays(days);

        ical = serialize(appointment);

        assertProperty(ical, "RRULE", "FREQ=MONTHLY;INTERVAL=2;COUNT=3;BYWEEKNO=3;BYDAY=MO,TU");


        // Second form : the last tuesday every 2 months

        appointment.setDayInMonth(5);
        appointment.setDays(AppointmentObject.TUESDAY);

        ical = serialize(appointment);

        assertProperty(ical, "RRULE", "FREQ=MONTHLY;INTERVAL=2;COUNT=3;BYWEEKNO=-1;BYDAY=TU");

        appointment.removeDays();

        // YEARLY

        // First form: Every 2 years, the 23rd of March
        appointment.removeDays();
        appointment.setRecurrenceType(AppointmentObject.YEARLY);
        appointment.setMonth(2);
        appointment.setDayInMonth(23);
        ical = serialize(appointment);

        assertProperty(ical, "RRULE", "FREQ=YEARLY;INTERVAL=2;COUNT=3;BYMONTH=3;BYMONTHDAY=23");

        // Second form: 2nd monday and wednesday in april every 2 years
        appointment.setMonth(3);
        appointment.setDayInMonth(2);

        days = 0;
        days |= AppointmentObject.MONDAY;
        days |= AppointmentObject.WEDNESDAY;
        appointment.setDays(days);
        ical = serialize(appointment);

        assertProperty(ical, "RRULE", "FREQ=YEARLY;INTERVAL=2;COUNT=3;BYMONTH=4;BYWEEKNO=2;BYDAY=MO,WE");

        // UNTIL

        appointment = getDefault();
        appointment.setRecurrenceType(AppointmentObject.DAILY);
        appointment.setInterval(2);
        appointment.setUntil(D("23/04/1989 00:00"));
        ical = serialize(appointment);

        assertProperty(ical, "RRULE", "FREQ=DAILY;INTERVAL=2;UNTIL=19890423");

    }


    public void testAppAlarm() throws IOException {
        int MINUTES = 60*1000;

        AppointmentObject appointment = getDefault();
        appointment.setAlarm(15 *MINUTES);
        appointment.setAlarmFlag(true);

        ICalFile ical = serialize(appointment);

        assertProperty(ical, "BEGIN", "VALARM");
        assertProperty(ical, "ACTION", "DISPLAY");
        assertProperty(ical, "TRIGGER", "-PT15M");


    }

    public void testAppPrivateFlag() throws IOException {
        AppointmentObject app = getDefault();

        ICalFile ical = serialize(app);

        assertProperty(ical, "CLASS", "public");

        app.setPrivateFlag(true);
        ical = serialize(app);

        assertProperty(ical, "CLASS", "private");
    }

    public void testAppTransparency() throws IOException {
        // RESERVED

        AppointmentObject app = getDefault();
        app.setShownAs(AppointmentObject.RESERVED);


        ICalFile ical = serialize(app);

        assertProperty(ical, "TRANSP", "OPAQUE");

        // FREE

        app.setShownAs(AppointmentObject.FREE);


        ical = serialize(app);

        assertProperty(ical, "TRANSP", "TRANSPARENT");


    }

    public void testAppAttendees() throws IOException {
        AppointmentObject app = getDefault();
        setParticipants(app, new String[]{"user1@internal.invalid", "user2@internal.invalid"}, new String[]{"external1@external.invalid", "external2@external.invalid"});

        ICalFile ical = serialize(app);

        assertProperty(ical, "ATTENDEE", "MAILTO:user1@internal.invalid");
        assertProperty(ical, "ATTENDEE", "MAILTO:user2@internal.invalid");
        assertProperty(ical, "ATTENDEE", "MAILTO:external1@external.invalid");
        assertProperty(ical, "ATTENDEE", "MAILTO:external2@external.invalid");


    }



    private void setParticipants(CalendarObject calendarObject, String[] internal, String[] external) {
        Participant[] allParticipants = new Participant[internal.length+ external.length];
        UserParticipant[] users = new UserParticipant[internal.length];


        int i = 0,j = 0;
        for(String mail : internal) {
            UserParticipant p = new UserParticipant(-1);
            p.setEmailAddress(mail);
            allParticipants[i++] = p;
            users[j++] = p;
        }

        j = 0;
        for(String mail : external) {
            ExternalUserParticipant p = new ExternalUserParticipant(mail);
            p.setEmailAddress(mail);
            allParticipants[i++] = p;

        }

        calendarObject.setParticipants(allParticipants);
        calendarObject.setUsers(users);

    }


    public void testAppResources() throws IOException {
        AppointmentObject app = getDefault();
        setResources(app, "beamer", "toaster", "deflector");
        ICalFile ical = serialize(app);

        assertProperty(ical, "RESOURCES", "beamer,toaster,deflector");


    }

    private void setResources(CalendarObject calendarObject, String...displayNames) {
        Participant[] participants = new Participant[displayNames.length];
        int i = 0;
        for(String displayName : displayNames) {
            ResourceParticipant p = new ResourceParticipant(-1);
            p.setDisplayName(displayName);
            participants[i++] = p;
        }
        calendarObject.setParticipants(participants);

    }

    public void testAppDeleteExceptions() throws IOException {
        AppointmentObject app = getDefault();
        app.setRecurrenceType(AppointmentObject.DAILY);
        app.setInterval(3);
        app.setRecurrenceCount(5);
        app.setDeleteExceptions(new Date[]{D("25/02/2009 10:00"), D("28/02/2009 12:00")});

        ICalFile ical = serialize(app);

        assertProperty(ical, "EXDATE", "20090225T100000Z,20090228T120000Z");
    }

    // Omitting: DURATION. This is all handled with DTStart and DTEnd in emitting


    // --------------------------------- Tasks ---------------------------------

    /**
     * Tests task emitter for title and note.
     */
    public void testTaskSimpleFields() throws IOException {
        final Task task = new Task();
        task.setTitle("The Title");
        task.setNote("The Note");
        task.setCategories("cat1, cat2, cat3");
        task.setDateCompleted(D("24/02/2009 10:00"));
        task.setPercentComplete(23);

        final ICalFile ical = serialize(task);
        assertProperty(ical, "SUMMARY", "The Title");
        assertProperty(ical, "DESCRIPTION", "The Note");
        assertProperty(ical, "CATEGORIES","cat1, cat2, cat3");
        assertProperty(ical, "COMPLETED", "20090224T100000Z");
        assertProperty(ical, "PERCENT-COMPLETE", "23");
    }

    public void testTaskDateFields() throws IOException {
        final Task task = new Task();
        final Date start = D("13/07/1976 15:00");
        final Date end = D("13/07/1976 17:00");
        task.setStartDate(start);
        task.setEndDate(end);
        final ICalFile ical = serialize(task);
        assertStandardAppFields(ical, start, end);
    }

    // SetUp

    public void setUp() {
        emitter = new ICal4JEmitter();
    }

    // Asserts
    private static SimpleDateFormat utc = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
    static {
        utc.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    private static void assertStandardAppFields(ICalFile ical, Date start, Date end) {
        assertProperty(ical, "DTSTART", utc.format(start));
        assertProperty(ical, "DTEND", utc.format(end));
    }

    private static void assertProperty(ICalFile ical, String name, String value) {

        assertTrue(name+" missing in: \n"+ical.toString(), ical.containsPair(name, value));
    }

    private static void assertLine(ICalFile ical, String line) {
        assertTrue(line+" missing in: \n"+ical.toString(), ical.containsLine(line));
    }


    // Helper Class


    private ICalFile serialize(AppointmentObject app) throws IOException {
        String icalText = emitter.writeAppointments(Arrays.asList(app), new ArrayList<ConversionError>(), new ArrayList<ConversionWarning>());
        return new ICalFile(new StringReader(icalText));
    }

    /**
     * Serializes a task.
     * @param task task to serialize.
     * @return an iCal file.
     * @throws IOException if serialization fails.
     */
    private ICalFile serialize(final Task task) throws IOException {
        return new ICalFile(new StringReader(
            emitter.writeTasks(
                Arrays.asList(task),
                new ArrayList<ConversionError>(),
                new ArrayList<ConversionWarning>())));
    }

    private static class ICalFile {

        private final List<String[]> lines = new ArrayList<String[]>();

        public ICalFile(final Reader reader) throws IOException {
            final BufferedReader lines = new BufferedReader(reader);
            String line = null;
            while((line = lines.readLine()) != null) {
                addLine(line);
            }
        }

        private void addLine(final String line) {
            int colonPos = line.indexOf(':');
            final String key;
            final String value;
            if (-1 == colonPos) {
                key = line;
                value = "";
            } else {
                key = line.substring(0, colonPos);
                value = line.substring(colonPos + 1);
            }
            lines.add(new String[]{key, value});
        }

        public List<String[]> getLines() {
            return lines;
        }

        public String getValue(final String key) {
            for(final String[] line : lines) {
                if(line[0].equals(key)) {
                    return line[1];
                }
            }
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder();
            for (final String[] line : lines) {
                final String key = line[0];
                final String value = line[1];
                sb.append(key);
                if (!"".equals(value)) {
                    sb.append(':');
                    sb.append(value);
                }
                sb.append('\n');
            }
            return sb.toString();
        }

        public boolean containsPair(String name, String value) {
            for (final String[] line : lines) {
                final String key = line[0];
                final String val = line[1];
                if(key.equals(name) && val.equals(value)) {
                    return true;
                }
            }
            return false;
        }
        public boolean containsLine(String line) {
            for (final String[] l : lines) {
                final String key = l[0];
                if(key.equals(line)) {
                    return true;
                }
            }
            return false;
        }
    }
}
