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

import com.openexchange.groupware.container.AppointmentObject;
import static com.openexchange.data.conversion.ical.DateHelper.*;
import com.openexchange.data.conversion.ical.ical4j.ICal4JEmitter;
import static com.openexchange.groupware.calendar.tools.CommonAppointments.D;

import java.util.*;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.text.SimpleDateFormat;

import junit.framework.TestCase;

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
        appointment.setDays(-1);
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

        assertProperty(ical, "RRULE", "FREQ=MONTHLY;INTERVAL=2;COUNT=3;BYDAY=MO,TU;BYWEEKNO=3");


        // Second form : the last tuesday every 2 months

        appointment.setDayInMonth(5);
        appointment.setDays(AppointmentObject.TUESDAY);

        ical = serialize(appointment);

        assertProperty(ical, "RRULE", "FREQ=MONTHLY;INTERVAL=2;COUNT=3;BYDAY=TU;BYWEEKNO=-1");

        appointment.setDays(-1);

        // YEARLY

        // First form: Every 2 years, the 23rd of March
        appointment.setRecurrenceType(AppointmentObject.YEARLY);
        appointment.setMonth(2);
        appointment.setDayInMonth(23);
        ical = serialize(appointment);

        assertProperty(ical, "RRULE", "FREQ=YEARLY;INTERVAL=2;COUNT=3;BYMONTHDAY=23;BYMONTH=3");

        // Second form: 2nd monday and wednesday in april every 2 years
        appointment.setMonth(3);
        appointment.setDayInMonth(2);

        days = 0;
        days |= AppointmentObject.MONDAY;
        days |= AppointmentObject.WEDNESDAY;
        appointment.setDays(days);
        ical = serialize(appointment);

        assertProperty(ical, "RRULE", "FREQ=YEARLY;INTERVAL=2;COUNT=3;BYDAY=MO,WE;BYMONTH=4;BYWEEKNO=2");

        // UNTIL

        appointment = getDefault();
        appointment.setRecurrenceType(AppointmentObject.YEARLY);
        appointment.setInterval(2);
        appointment.setUntil(D("04/23/1989"));
        ical = serialize(appointment);

        assertProperty(ical, "RRULE", "FREQ=YEARLY;INTERVAL=2;UNTIL=19890423");

    }


    public void testAppAlarm() {

    }

    public void testAppPrivateFlag() throws IOException {
        AppointmentObject app = getDefault();

        app.setPrivateFlag(true);

        ICalFile ical = serialize(app);

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

    public void testAppAttendees() {

    }

    public void testAppResources() {

    }
    
    public void testAppDeleteExceptions() {
        
    }


    // Tasks


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
        String valueFromFile = ical.getValue(name);
        assertNotNull(name+" missing in: \n"+ical.toString(), valueFromFile);
        assertEquals(ical.toString(), valueFromFile, value);
    }


    // Helper Class


    private ICalFile serialize(AppointmentObject app) throws IOException {
        String icalText = emitter.write(Arrays.asList(app));
        return new ICalFile(new StringReader(icalText));
    }
     

    private static class ICalFile {

        private final List<String[]> lines = new ArrayList<String[]>();
        private final StringBuffer allLines = new StringBuffer();

        public ICalFile(Reader reader) throws IOException {
            BufferedReader lines = new BufferedReader(reader);
            String line = null;
            while((line = lines.readLine()) != null) {
                addLine(line);
            }
        }

        private void addLine(String line) {
            allLines.append(line).append("\n");
            StringBuilder b = new StringBuilder();
            String key = null;
            String value = null;
            boolean buildValue = false;
            for(int i = 0, size = line.length(); i < size; i++) {
                char c = line.charAt(i);
                if(c == ':' && !buildValue) {
                    buildValue = true;
                    key = b.toString();
                    b.setLength(0);
                } else {
                    b.append(c);
                }
            }

            value = b.toString();

            lines.add(new String[]{key, value});
        }

        public List<String[]> getLines() {
            return lines;
        }

        public String getValue(String key) {
            for(String[] line : lines) {
                if(line[0].equals(key)) {
                    return line[1];
                }
            }
            return null;
        }

        public String toString() {
            return allLines.toString();
        }
    }
}
