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

package com.openexchange.ajax.appointment;

import static com.openexchange.groupware.calendar.TimeTools.D;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;
import org.json.JSONArray;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.config.actions.GetRequest;
import com.openexchange.ajax.config.actions.SetRequest;
import com.openexchange.ajax.config.actions.Tree;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.CommonAllResponse;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.java.util.TimeZones;
import com.openexchange.test.CTMUtils;

/**
 * {@link RangeQueryTest}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class RangeQueryTest extends AbstractAJAXSession {

    private String originalTimeZone;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        originalTimeZone = getClient().execute(new GetRequest(Tree.TimeZone)).getString();
    }

    @Override
    @After
    public void tearDown() throws Exception {
        try {
            if (null != originalTimeZone) {
                setTimeZone(TimeZone.getTimeZone(originalTimeZone));
            }
        } finally {
            super.tearDown();
        }
    }

    @Test
    public void testPacificHonolulu() throws Exception {
        test("Pacific/Honolulu");
    }

    @Test
    public void testPacificSamoa() throws Exception {
        test("Pacific/Samoa");
    }

    @Test
    public void testAmericaNewYork() throws Exception {
        test("America/New_York");
    }

    @Test
    public void testAsiaMacao() throws Exception {
        test("Asia/Macao");
    }

    @Test
    public void testPacificNauro() throws Exception {
        test("Pacific/Nauro");
    }

    @Test
    public void testPacificApia() throws Exception {
        test("Pacific/Apia");
    }

    private void test(String timeZoneID) throws Exception {
        TimeZone timeZone = TimeZone.getTimeZone(timeZoneID);
        setTimeZone(timeZone);
        /*
         * test non-all day appointments
         */
        testAtMidnight(timeZone);
        testAtNoon(timeZone);
        testUntilMidnight(timeZone);
        testOverMidnight(timeZone);
        testOneDay(timeZone);
        /*
         * test all-day appointments
         */
        testAllDay(timeZone);
        testAllDayTwoDays(timeZone);
    }

    private void testAllDay(TimeZone timeZone) throws Exception {
        /*
         * insert all day appointment using API dates
         */
        Appointment appointment = insertAllDayAppointment("today at 00:00:00", "tomorrow at 00:00:00");
        /*
         * derive distinct API dates for queries around the appointment
         * ("Dates without time are transmitted as the number of milliseconds between 00:00 UTC on that date and 1970-01-01 00:00 UTC")
         */
        Date weekBefore = getAPIDate(TimeZones.UTC, appointment.getStartDate(), -7);
        Date dayBefore = getAPIDate(TimeZones.UTC, appointment.getStartDate(), -1);
        Date day = getAPIDate(TimeZones.UTC, appointment.getStartDate(), 0);
        Date dayAfter = getAPIDate(TimeZones.UTC, appointment.getStartDate(), 1);
        Date weekAfter = getAPIDate(TimeZones.UTC, appointment.getStartDate(), 7);
        /*
         * check 'all' requests with different ranges
         */
        assertNotContains(getAllAppointments(timeZone, weekBefore, dayBefore), appointment.getObjectID());
        assertNotContains(getAllAppointments(timeZone, weekBefore, day), appointment.getObjectID());
        assertContains(getAllAppointments(timeZone, weekBefore, dayAfter), appointment.getObjectID());
        assertContains(getAllAppointments(timeZone, weekBefore, weekAfter), appointment.getObjectID());
        assertNotContains(getAllAppointments(timeZone, dayBefore, day), appointment.getObjectID());
        assertContains(getAllAppointments(timeZone, dayBefore, dayAfter), appointment.getObjectID());
        assertContains(getAllAppointments(timeZone, dayBefore, weekAfter), appointment.getObjectID());
        assertContains(getAllAppointments(timeZone, day, dayAfter), appointment.getObjectID());
        assertContains(getAllAppointments(timeZone, day, weekAfter), appointment.getObjectID());
        assertNotContains(getAllAppointments(timeZone, dayAfter, weekAfter), appointment.getObjectID());
    }

    private void testAllDayTwoDays(TimeZone timeZone) throws Exception {
        /*
         * insert all day appointment using API dates
         */
        Appointment appointment = insertAllDayAppointment("yesterday at 00:00:00", "tomorrow at 00:00:00");
        /*
         * derive distinct API dates for queries around the appointment
         * ("Dates without time are transmitted as the number of milliseconds between 00:00 UTC on that date and 1970-01-01 00:00 UTC")
         */
        Date weekBefore = getAPIDate(TimeZones.UTC, appointment.getStartDate(), -7);
        Date dayBefore = getAPIDate(TimeZones.UTC, appointment.getStartDate(), -1);
        Date firstDay = getAPIDate(TimeZones.UTC, appointment.getStartDate(), 0);
        Date secondDay = getAPIDate(TimeZones.UTC, appointment.getStartDate(), 1);
        Date dayAfter = getAPIDate(TimeZones.UTC, appointment.getEndDate(), 0);
        Date weekAfter = getAPIDate(TimeZones.UTC, appointment.getEndDate(), 7);
        /*
         * check 'all' requests with different ranges
         */
        assertNotContains(getAllAppointments(timeZone, weekBefore, dayBefore), appointment.getObjectID());
        assertNotContains(getAllAppointments(timeZone, weekBefore, firstDay), appointment.getObjectID());
        assertContains(getAllAppointments(timeZone, weekBefore, secondDay), appointment.getObjectID());
        assertContains(getAllAppointments(timeZone, weekBefore, dayAfter), appointment.getObjectID());
        assertContains(getAllAppointments(timeZone, weekBefore, weekAfter), appointment.getObjectID());
        assertNotContains(getAllAppointments(timeZone, dayBefore, firstDay), appointment.getObjectID());
        assertContains(getAllAppointments(timeZone, dayBefore, secondDay), appointment.getObjectID());
        assertContains(getAllAppointments(timeZone, dayBefore, dayAfter), appointment.getObjectID());
        assertContains(getAllAppointments(timeZone, dayBefore, weekAfter), appointment.getObjectID());
        assertContains(getAllAppointments(timeZone, firstDay, secondDay), appointment.getObjectID());
        assertContains(getAllAppointments(timeZone, firstDay, dayAfter), appointment.getObjectID());
        assertContains(getAllAppointments(timeZone, firstDay, weekAfter), appointment.getObjectID());
        assertContains(getAllAppointments(timeZone, secondDay, dayAfter), appointment.getObjectID());
        assertContains(getAllAppointments(timeZone, secondDay, weekAfter), appointment.getObjectID());
        assertNotContains(getAllAppointments(timeZone, dayAfter, weekAfter), appointment.getObjectID());
    }

    private void testAtMidnight(TimeZone timeZone) throws Exception {
        /*
         * insert appointment using local start- and endtime
         */
        Appointment appointment = insertAppointment(timeZone, "tomorrow at 00:00:00", "tomorrow at 01:00:00");
        /*
         * derive distinct API dates for queries around the appointment
         * ("Dates without time are transmitted as the number of milliseconds between 00:00 UTC on that date and 1970-01-01 00:00 UTC")
         */
        Date weekBefore = getAPIDate(timeZone, appointment.getStartDate(), -7);
        Date dayBefore = getAPIDate(timeZone, appointment.getStartDate(), -1);
        Date day = getAPIDate(timeZone, appointment.getStartDate(), 0);
        Date dayAfter = getAPIDate(timeZone, appointment.getStartDate(), 1);
        Date weekAfter = getAPIDate(timeZone, appointment.getStartDate(), 7);
        /*
         * check 'all' requests with different ranges
         */
        assertNotContains(getAllAppointments(timeZone, weekBefore, dayBefore), appointment.getObjectID());
        assertNotContains(getAllAppointments(timeZone, weekBefore, day), appointment.getObjectID());
        assertContains(getAllAppointments(timeZone, weekBefore, dayAfter), appointment.getObjectID());
        assertContains(getAllAppointments(timeZone, weekBefore, weekAfter), appointment.getObjectID());
        assertNotContains(getAllAppointments(timeZone, dayBefore, day), appointment.getObjectID());
        assertContains(getAllAppointments(timeZone, dayBefore, dayAfter), appointment.getObjectID());
        assertContains(getAllAppointments(timeZone, dayBefore, weekAfter), appointment.getObjectID());
        assertContains(getAllAppointments(timeZone, day, dayAfter), appointment.getObjectID());
        assertContains(getAllAppointments(timeZone, day, weekAfter), appointment.getObjectID());
        assertNotContains(getAllAppointments(timeZone, dayAfter, weekAfter), appointment.getObjectID());
    }

    private void testAtNoon(TimeZone timeZone) throws Exception {
        /*
         * insert appointment using local start- and endtime
         */
        Appointment appointment = insertAppointment(timeZone, "tomorrow at 12:00:00", "tomorrow at 13:00:00");
        /*
         * derive distinct API dates for queries around the appointment
         * ("Dates without time are transmitted as the number of milliseconds between 00:00 UTC on that date and 1970-01-01 00:00 UTC")
         */
        Date weekBefore = getAPIDate(timeZone, appointment.getStartDate(), -7);
        Date dayBefore = getAPIDate(timeZone, appointment.getStartDate(), -1);
        Date day = getAPIDate(timeZone, appointment.getStartDate(), 0);
        Date dayAfter = getAPIDate(timeZone, appointment.getStartDate(), 1);
        Date weekAfter = getAPIDate(timeZone, appointment.getStartDate(), 7);
        /*
         * check 'all' requests with different ranges
         */
        assertNotContains(getAllAppointments(timeZone, weekBefore, dayBefore), appointment.getObjectID());
        assertNotContains(getAllAppointments(timeZone, weekBefore, day), appointment.getObjectID());
        assertContains(getAllAppointments(timeZone, weekBefore, dayAfter), appointment.getObjectID());
        assertContains(getAllAppointments(timeZone, weekBefore, weekAfter), appointment.getObjectID());
        assertNotContains(getAllAppointments(timeZone, dayBefore, day), appointment.getObjectID());
        assertContains(getAllAppointments(timeZone, dayBefore, dayAfter), appointment.getObjectID());
        assertContains(getAllAppointments(timeZone, dayBefore, weekAfter), appointment.getObjectID());
        assertContains(getAllAppointments(timeZone, day, dayAfter), appointment.getObjectID());
        assertContains(getAllAppointments(timeZone, day, weekAfter), appointment.getObjectID());
        assertNotContains(getAllAppointments(timeZone, dayAfter, weekAfter), appointment.getObjectID());
    }

    private void testUntilMidnight(TimeZone timeZone) throws Exception {
        /*
         * insert appointment using local start- and endtime
         */
        Appointment appointment = insertAppointment(timeZone, "today at 23:00:00", "tomorrow at 00:00:00");
        /*
         * derive distinct API dates for queries around the appointment
         * ("Dates without time are transmitted as the number of milliseconds between 00:00 UTC on that date and 1970-01-01 00:00 UTC")
         */
        Date weekBefore = getAPIDate(timeZone, appointment.getStartDate(), -7);
        Date dayBefore = getAPIDate(timeZone, appointment.getStartDate(), -1);
        Date day = getAPIDate(timeZone, appointment.getStartDate(), 0);
        Date dayAfter = getAPIDate(timeZone, appointment.getStartDate(), 1);
        Date weekAfter = getAPIDate(timeZone, appointment.getStartDate(), 7);
        /*
         * check 'all' requests with different ranges
         */
        assertNotContains(getAllAppointments(timeZone, weekBefore, dayBefore), appointment.getObjectID());
        assertNotContains(getAllAppointments(timeZone, weekBefore, day), appointment.getObjectID());
        assertContains(getAllAppointments(timeZone, weekBefore, dayAfter), appointment.getObjectID());
        assertContains(getAllAppointments(timeZone, weekBefore, weekAfter), appointment.getObjectID());
        assertNotContains(getAllAppointments(timeZone, dayBefore, day), appointment.getObjectID());
        assertContains(getAllAppointments(timeZone, dayBefore, dayAfter), appointment.getObjectID());
        assertContains(getAllAppointments(timeZone, dayBefore, weekAfter), appointment.getObjectID());
        assertContains(getAllAppointments(timeZone, day, dayAfter), appointment.getObjectID());
        assertContains(getAllAppointments(timeZone, day, weekAfter), appointment.getObjectID());
        assertNotContains(getAllAppointments(timeZone, dayAfter, weekAfter), appointment.getObjectID());
    }

    private void testOverMidnight(TimeZone timeZone) throws Exception {
        /*
         * insert appointment using local start- and endtime
         */
        Appointment appointment = insertAppointment(timeZone, "today at 23:00:00", "tomorrow at 01:00:00");
        /*
         * derive distinct API dates for queries around the appointment
         * ("Dates without time are transmitted as the number of milliseconds between 00:00 UTC on that date and 1970-01-01 00:00 UTC")
         */
        Date weekBefore = getAPIDate(timeZone, appointment.getStartDate(), -7);
        Date dayBefore = getAPIDate(timeZone, appointment.getStartDate(), -1);
        Date firstDay = getAPIDate(timeZone, appointment.getStartDate(), 0);
        Date secondDay = getAPIDate(timeZone, appointment.getEndDate(), 0);
        Date dayAfter = getAPIDate(timeZone, appointment.getEndDate(), 1);
        Date weekAfter = getAPIDate(timeZone, appointment.getEndDate(), 7);
        /*
         * check 'all' requests with different ranges
         */
        assertNotContains(getAllAppointments(timeZone, weekBefore, dayBefore), appointment.getObjectID());
        assertNotContains(getAllAppointments(timeZone, weekBefore, firstDay), appointment.getObjectID());
        assertContains(getAllAppointments(timeZone, weekBefore, secondDay), appointment.getObjectID());
        assertContains(getAllAppointments(timeZone, weekBefore, dayAfter), appointment.getObjectID());
        assertContains(getAllAppointments(timeZone, weekBefore, weekAfter), appointment.getObjectID());
        assertNotContains(getAllAppointments(timeZone, dayBefore, firstDay), appointment.getObjectID());
        assertContains(getAllAppointments(timeZone, dayBefore, secondDay), appointment.getObjectID());
        assertContains(getAllAppointments(timeZone, dayBefore, dayAfter), appointment.getObjectID());
        assertContains(getAllAppointments(timeZone, dayBefore, weekAfter), appointment.getObjectID());
        assertContains(getAllAppointments(timeZone, firstDay, secondDay), appointment.getObjectID());
        assertContains(getAllAppointments(timeZone, firstDay, dayAfter), appointment.getObjectID());
        assertContains(getAllAppointments(timeZone, firstDay, weekAfter), appointment.getObjectID());
        assertContains(getAllAppointments(timeZone, secondDay, dayAfter), appointment.getObjectID());
        assertContains(getAllAppointments(timeZone, secondDay, weekAfter), appointment.getObjectID());
        assertNotContains(getAllAppointments(timeZone, dayAfter, weekAfter), appointment.getObjectID());
    }

    private void testOneDay(TimeZone timeZone) throws Exception {
        /*
         * insert appointment using local start- and endtime
         */
        Appointment appointment = insertAppointment(timeZone, "today at 00:00:00", "tomorrow at 00:00:00");
        /*
         * derive distinct API dates for queries around the appointment
         * ("Dates without time are transmitted as the number of milliseconds between 00:00 UTC on that date and 1970-01-01 00:00 UTC")
         */
        Date weekBefore = getAPIDate(timeZone, appointment.getStartDate(), -7);
        Date dayBefore = getAPIDate(timeZone, appointment.getStartDate(), -1);
        Date day = getAPIDate(timeZone, appointment.getStartDate(), 0);
        Date dayAfter = getAPIDate(timeZone, appointment.getStartDate(), 1);
        Date weekAfter = getAPIDate(timeZone, appointment.getStartDate(), 7);
        /*
         * check 'all' requests with different ranges
         */
        assertNotContains(getAllAppointments(timeZone, weekBefore, dayBefore), appointment.getObjectID());
        assertNotContains(getAllAppointments(timeZone, weekBefore, day), appointment.getObjectID());
        assertContains(getAllAppointments(timeZone, weekBefore, dayAfter), appointment.getObjectID());
        assertContains(getAllAppointments(timeZone, weekBefore, weekAfter), appointment.getObjectID());
        assertNotContains(getAllAppointments(timeZone, dayBefore, day), appointment.getObjectID());
        assertContains(getAllAppointments(timeZone, dayBefore, dayAfter), appointment.getObjectID());
        assertContains(getAllAppointments(timeZone, dayBefore, weekAfter), appointment.getObjectID());
        assertContains(getAllAppointments(timeZone, day, dayAfter), appointment.getObjectID());
        assertContains(getAllAppointments(timeZone, day, weekAfter), appointment.getObjectID());
        assertNotContains(getAllAppointments(timeZone, dayAfter, weekAfter), appointment.getObjectID());
    }

    private Appointment[] getAllAppointments(TimeZone timeZone, Date from, Date until) throws Exception {
        int[] columns = new int[] { Appointment.OBJECT_ID };
        AllRequest allRequest = new AllRequest(catm.getPrivateFolder(), columns, from, until, timeZone);
        CommonAllResponse response = getClient().execute(allRequest);
        return CTMUtils.jsonArray2AppointmentArray((JSONArray) response.getData(), response.getColumns(), timeZone);
    }

    private Appointment insertAppointment(TimeZone timeZone, String start, String end) throws Exception {
        Appointment appointment = new Appointment();
        appointment.setTimezone(timeZone.getID());
        appointment.setIgnoreConflicts(true);
        appointment.setParentFolderID(catm.getPrivateFolder());
        appointment.setTitle("RangeQueryTest");
        appointment.setStartDate(D(start, timeZone));
        appointment.setEndDate(D(end, timeZone));
        return catm.insert(appointment);
    }

    private Appointment insertAllDayAppointment(String start, String end) throws Exception {
        Appointment appointment = new Appointment();
        appointment.setIgnoreConflicts(true);
        appointment.setParentFolderID(catm.getPrivateFolder());
        appointment.setTitle("RangeQueryTest");
        appointment.setFullTime(true);
        appointment.setStartDate(D(start, TimeZones.UTC));
        appointment.setEndDate(D(end, TimeZones.UTC));
        return catm.insert(appointment);
    }

    private Date getAPIDate(TimeZone localtimeZone, Date localDate, int datesToAdd) {
        Calendar localCalendar = GregorianCalendar.getInstance(localtimeZone);
        localCalendar.setTime(localDate);
        localCalendar.add(Calendar.DAY_OF_YEAR, datesToAdd);
        Calendar utcCalendar = GregorianCalendar.getInstance(TimeZones.UTC);
        utcCalendar.set(localCalendar.get(Calendar.YEAR), localCalendar.get(Calendar.MONTH), localCalendar.get(Calendar.DATE), 0, 0, 0);
        utcCalendar.set(Calendar.MILLISECOND, 0);
        return utcCalendar.getTime();
    }

    private void setTimeZone(TimeZone timeZone) throws Exception {
        getClient().execute(new SetRequest(Tree.TimeZone, timeZone.getID()));
        catm.setTimezone(timeZone);
    }

    private static void assertContains(Appointment[] appointments, int objectID) {
        assertNotNull(find(appointments, objectID));
    }

    private static void assertNotContains(Appointment[] appointments, int objectID) {
        assertNull(find(appointments, objectID));
    }

    private static Appointment find(Appointment[] appointments, int objectID) {
        if (null != appointments) {
            for (Appointment appointment : appointments) {
                if (appointment.getObjectID() == objectID) {
                    return appointment;
                }
            }
        }
        return null;
    }

    private static final class AllRequest extends com.openexchange.ajax.appointment.action.AllRequest {

        private final Date start;
        private final Date end;

        public AllRequest(int folderId, int[] columns, Date start, Date end, TimeZone tz) {
            super(folderId, columns, start, end, tz);
            this.start = start;
            this.end = end;
        }

        @Override
        public Parameter[] getParameters() {
            List<Parameter> parameters = new ArrayList<Parameter>();
            for (Parameter parameter : super.getParameters()) {
                switch (parameter.getName()) {
                    case AJAXServlet.PARAMETER_START:
                        parameters.add(new Parameter(AJAXServlet.PARAMETER_START, start));
                        break;
                    case AJAXServlet.PARAMETER_END:
                        parameters.add(new Parameter(AJAXServlet.PARAMETER_END, end));
                        break;
                    default:
                        parameters.add(parameter);
                        break;
                }
            }
            return parameters.toArray(new Parameter[parameters.size()]);
        }

    }

}
