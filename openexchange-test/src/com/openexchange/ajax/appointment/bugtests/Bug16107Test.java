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

package com.openexchange.ajax.appointment.bugtests;

import java.util.Date;
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

    public Bug16107Test(String name) throws Exception {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
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

        calendarManager.insert(startAppointment);
        link(startAppointment, updateAppointment);
        updateAppointment.setRecurrenceID(startAppointment.getObjectID());
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }



    public void testFirstMonthView(){
        Date start = D("26.04.2010 00:00");
        Date end = D("07.06.2010 00:00");
        int occurences = 14;
        check("month view", start, end, occurences);
    }

    public void testLastMonthView(){
        Date start = D("31.05.2010 00:00");
        Date end = D("05.07.2010 00:00");
        int occurences = 12;
        check("month view", start, end, occurences);
    }

    public void testNextToLastWorkWeekView(){
        Date start = D("31.05.2010 00:00");
        Date end = D("05.06.2010 00:00");
        int occurences = 5;
        check("work week view (next-to-last week)", start, end, occurences);
    }


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

//        all = calendarManager.all(folder.getObjectID(), start, end, new int[]{1,20,207,206,2});
//        assertEquals("AllRequest should find starting appointment in "+name, 1, all.length);
//
//        has = calendarManager.has(start, end);
//        count = 0;
//        for(boolean b : has)
//            if(b) count++;
//
//        assertEquals("HasRequest should find the right amount of occurences "+name, occurences, count);
//

        calendarManager.update(updateAppointment);

        all = calendarManager.all(folder.getObjectID(), start, end, new int[]{1,20,207,206,2});
        assertEquals("AllRequest should find updated appointment in "+name, 1, all.length);

        has = calendarManager.has(start, end);
        count = 0;
        for(boolean b: has) {
            if(b) {
                count++;
            }
        }

        assertEquals("HasRequest should find the right amount of occurences in updated "+ name, occurences, count);
    }
}
