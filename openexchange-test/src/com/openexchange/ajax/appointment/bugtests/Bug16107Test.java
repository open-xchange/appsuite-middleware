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

import static org.junit.Assert.assertEquals;
import java.util.Date;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.appointment.recurrence.ManagedAppointmentTest;
import com.openexchange.groupware.container.Appointment;

/**
 * Displaying an appointment that spanned more than one month and was changed from fulltime to a small time period breaks in several GUI
 * views. This is due to some requests working differently than others. This test documents that.
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class Bug16107Test extends ManagedAppointmentTest {

    private Appointment startAppointment;

    private Appointment updateAppointment;

    public Bug16107Test() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        startAppointment = new Appointment();
        startAppointment.setParentFolderID(folder.getObjectID());
        startAppointment.setTitle("Bug 16107");
        startAppointment.setStartDate(D("24.05.2010 00:00"));
        startAppointment.setEndDate(D("25.05.2010 00:00"));
        startAppointment.setRecurrenceType(Appointment.DAILY);
        startAppointment.setUntil(D("11.06.2010 07:30"));
        startAppointment.setFullTime(true);
        startAppointment.setInterval(1);

        updateAppointment = new Appointment();
        updateAppointment.setTitle("Bug 16107 (updated)");
        updateAppointment.setStartDate(D("24.05.2010 07:00"));
        updateAppointment.setEndDate(D("24.05.2010 07:30"));
        updateAppointment.setFullTime(false);
        updateAppointment.setRecurrenceType(Appointment.DAILY);
        updateAppointment.setRecurrencePosition(0);
        updateAppointment.setInterval(1);
        updateAppointment.setRecurringStart(D("24.05.2010 00:00").getTime());
        updateAppointment.setUntil(D("11.06.2010 07:30"));

        catm.insert(startAppointment);
        link(startAppointment, updateAppointment);
        updateAppointment.setRecurrenceID(startAppointment.getObjectID());
    }

    @Test
    public void testFirstMonthView() {
        Date start = D("26.04.2010 00:00");
        Date end = D("07.06.2010 00:00");
        int occurences = 14;
        check("month view", start, end, occurences);
    }

    @Test
    public void testLastMonthView() {
        Date start = D("31.05.2010 00:00");
        Date end = D("05.07.2010 00:00");
        int occurences = 12;
        check("month view", start, end, occurences);
    }

    @Test
    public void testNextToLastWorkWeekView() {
        Date start = D("31.05.2010 00:00");
        Date end = D("05.06.2010 00:00");
        int occurences = 5;
        check("work week view (next-to-last week)", start, end, occurences);
    }

    @Test
    public void testLastWorkWeekView() {
        Date start = D("07.06.2010 00:00");
        Date end = D("12.06.2010 00:00");
        int occurences = 5;
        check("work week view (last week)", start, end, occurences);
    }

    private void check(String name, Date start, Date end, int occurences) {
        boolean[] has;
        Appointment[] all;
        int count = 0;

        //        all = catm.all(folder.getObjectID(), start, end, new int[]{1,20,207,206,2});
        //        assertEquals("AllRequest should find starting appointment in "+name, 1, all.length);
        //
        //        has = catm.has(start, end);
        //        count = 0;
        //        for(boolean b : has)
        //            if (b) count++;
        //
        //        assertEquals("HasRequest should find the right amount of occurences "+name, occurences, count);
        //

        catm.update(updateAppointment);

        all = catm.all(folder.getObjectID(), start, end, new int[] { 1, 20, 207, 206, 2 });
        assertEquals("AllRequest should find updated appointment in " + name, 1, all.length);

        has = catm.has(start, end);
        count = 0;
        for (boolean b : has) {
            if (b) {
                count++;
            }
        }

        assertEquals("HasRequest should find the right amount of occurences in updated " + name, occurences, count);
    }
}
