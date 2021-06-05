/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.ajax.appointment.bugtests;

import static com.openexchange.test.common.groupware.calendar.TimeTools.D;
import static org.junit.Assert.assertEquals;
import java.io.IOException;
import java.util.Calendar;
import java.util.TimeZone;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.config.actions.SetRequest;
import com.openexchange.ajax.config.actions.Tree;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Appointment;

/**
 * {@link Bug38079Test}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.8.0
 */
public class Bug38079Test extends AbstractAJAXSession {

    private Appointment appointment;

    public Bug38079Test() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        appointment = new Appointment();
        appointment.setTitle("Bug 38079 Test");
        appointment.setParentFolderID(getClient().getValues().getPrivateAppointmentFolder());
        appointment.setRecurrenceType(Appointment.YEARLY);
        appointment.setInterval(1);
        appointment.setDayInMonth(16);
        appointment.setIgnoreConflicts(true);
    }

    @Test
    public void testAmericaNewYork_JAN() throws Exception {
        runTest("America/New_York", Calendar.JANUARY);
    }

    @Test
    public void testEuropeBerlin_JAN() throws Exception {
        runTest("Europe/Berlin", Calendar.JANUARY);
    }

    @Test
    public void testAfricaBanjul_JAN() throws Exception {
        runTest("Africa/Banjul", Calendar.JANUARY); // This one is UTC time without DST.
    }

    @Test
    public void testAmericaNewYork_FEB() throws Exception {
        runTest("America/New_York", Calendar.FEBRUARY);
    }

    @Test
    public void testEuropeBerlin_FEB() throws Exception {
        runTest("Europe/Berlin", Calendar.FEBRUARY);
    }

    @Test
    public void testAfricaBanjul_FEB() throws Exception {
        runTest("Africa/Banjul", Calendar.FEBRUARY); // This one is UTC time without DST.
    }

    @Test
    public void testAmericaNewYork_MAR() throws Exception {
        runTest("America/New_York", Calendar.MARCH);
    }

    @Test
    public void testEuropeBerlin_MAR() throws Exception {
        runTest("Europe/Berlin", Calendar.MARCH);
    }

    @Test
    public void testAfricaBanjul_MAR() throws Exception {
        runTest("Africa/Banjul", Calendar.MARCH); // This one is UTC time without DST.
    }

    @Test
    public void testAmericaNewYork_APR() throws Exception {
        runTest("America/New_York", Calendar.APRIL);
    }

    @Test
    public void testEuropeBerlin_APR() throws Exception {
        runTest("Europe/Berlin", Calendar.APRIL);
    }

    @Test
    public void testAfricaBanjul_APR() throws Exception {
        runTest("Africa/Banjul", Calendar.APRIL); // This one is UTC time without DST.
    }

    @Test
    public void testAmericaNewYork_MAY() throws Exception {
        runTest("America/New_York", Calendar.MAY);
    }

    @Test
    public void testEuropeBerlin_MAY() throws Exception {
        runTest("Europe/Berlin", Calendar.MAY);
    }

    @Test
    public void testAfricaBanjul_MAY() throws Exception {
        runTest("Africa/Banjul", Calendar.MAY); // This one is UTC time without DST.
    }

    @Test
    public void testAmericaNewYork_JUN() throws Exception {
        runTest("America/New_York", Calendar.JUNE);
    }

    @Test
    public void testEuropeBerlin_JUN() throws Exception {
        runTest("Europe/Berlin", Calendar.JUNE);
    }

    @Test
    public void testAfricaBanjul_JUN() throws Exception {
        runTest("Africa/Banjul", Calendar.JUNE); // This one is UTC time without DST.
    }

    @Test
    public void testAmericaNewYork_JUL() throws Exception {
        runTest("America/New_York", Calendar.JULY);
    }

    @Test
    public void testEuropeBerlin_JUL() throws Exception {
        runTest("Europe/Berlin", Calendar.JULY);
    }

    @Test
    public void testAfricaBanjul_JUL() throws Exception {
        runTest("Africa/Banjul", Calendar.JULY); // This one is UTC time without DST.
    }

    @Test
    public void testAmericaNewYork_AUG() throws Exception {
        runTest("America/New_York", Calendar.AUGUST);
    }

    @Test
    public void testEuropeBerlin_AUG() throws Exception {
        runTest("Europe/Berlin", Calendar.AUGUST);
    }

    @Test
    public void testAfricaBanjul_AUG() throws Exception {
        runTest("Africa/Banjul", Calendar.AUGUST); // This one is UTC time without DST.
    }

    @Test
    public void testAmericaNewYork_SEP() throws Exception {
        runTest("America/New_York", Calendar.SEPTEMBER);
    }

    @Test
    public void testEuropeBerlin_SEP() throws Exception {
        runTest("Europe/Berlin", Calendar.SEPTEMBER);
    }

    @Test
    public void testAfricaBanjul_SEP() throws Exception {
        runTest("Africa/Banjul", Calendar.SEPTEMBER); // This one is UTC time without DST.
    }

    @Test
    public void testAmericaNewYork_OCT() throws Exception {
        runTest("America/New_York", Calendar.OCTOBER);
    }

    @Test
    public void testEuropeBerlin_OCT() throws Exception {
        runTest("Europe/Berlin", Calendar.OCTOBER);
    }

    @Test
    public void testAfricaBanjul_OCT() throws Exception {
        runTest("Africa/Banjul", Calendar.OCTOBER); // This one is UTC time without DST.
    }

    @Test
    public void testAmericaNewYork_NOV() throws Exception {
        runTest("America/New_York", Calendar.NOVEMBER);
    }

    @Test
    public void testEuropeBerlin_NOV() throws Exception {
        runTest("Europe/Berlin", Calendar.NOVEMBER);
    }

    @Test
    public void testAfricaBanjul_NOV() throws Exception {
        runTest("Africa/Banjul", Calendar.NOVEMBER); // This one is UTC time without DST.
    }

    @Test
    public void testAmericaNewYork_DEC() throws Exception {
        runTest("America/New_York", Calendar.DECEMBER);
    }

    @Test
    public void testEuropeBerlin_DEC() throws Exception {
        runTest("Europe/Berlin", Calendar.DECEMBER);
    }

    @Test
    public void testAfricaBanjul_DEC() throws Exception {
        runTest("Africa/Banjul", Calendar.DECEMBER); // This one is UTC time without DST.
    }

    private void runTest(String tzId, int month) throws OXException, IOException, JSONException {
        TimeZone tz = TimeZone.getTimeZone(tzId);
        SetRequest setRequest = new SetRequest(Tree.TimeZone, tz.getID());
        getClient().execute(setRequest);

        appointment.setMonth(month);
        appointment.setStartDate(D("05.05.2015 08:00", tz));
        appointment.setEndDate(D("05.05.2015 09:00", tz));
        catm.setTimezone(tz);
        catm.insert(appointment);
        int year = 2015;
        if (month < Calendar.MAY) {
            year++;
        }
        Appointment loadedAppointment = catm.get(appointment.getParentFolderID(), appointment.getObjectID());
        assertEquals("Wrong start for month " + (month + 1), D("16." + (month + 1) + "." + year + " 08:00", tz), loadedAppointment.getStartDate());
        assertEquals("Wrong end for month " + (month + 1), D("16." + (month + 1) + "." + year + " 09:00", tz), loadedAppointment.getEndDate());
    }

}
