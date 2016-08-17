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

import static com.openexchange.groupware.calendar.TimeTools.D;
import java.io.IOException;
import java.util.Calendar;
import java.util.TimeZone;
import org.json.JSONException;
import com.openexchange.ajax.config.actions.GetRequest;
import com.openexchange.ajax.config.actions.GetResponse;
import com.openexchange.ajax.config.actions.SetRequest;
import com.openexchange.ajax.config.actions.Tree;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.test.CalendarTestManager;

/**
 * {@link Bug38079Test}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.8.0
 */
public class Bug38079Test extends AbstractAJAXSession {

    private CalendarTestManager ctm;
    private Appointment appointment;
    private String origTimeZone;

    public Bug38079Test(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        GetRequest getRequest = new GetRequest(Tree.TimeZone);
        GetResponse getResponse = getClient().execute(getRequest);
        origTimeZone = getResponse.getString();

        ctm = new CalendarTestManager(client);
        appointment = new Appointment();
        appointment.setTitle("Bug 38079 Test");
        appointment.setParentFolderID(client.getValues().getPrivateAppointmentFolder());
        appointment.setRecurrenceType(Appointment.YEARLY);
        appointment.setInterval(1);
        appointment.setDayInMonth(16);
        appointment.setIgnoreConflicts(true);
    }

    public void testAmericaNewYork_JAN() throws Exception {
        runTest("America/New_York", Calendar.JANUARY);
    }

    public void testEuropeBerlin_JAN() throws Exception {
        runTest("Europe/Berlin", Calendar.JANUARY);
    }

    public void testAfricaBanjul_JAN() throws Exception {
        runTest("Africa/Banjul", Calendar.JANUARY); // This one is UTC time without DST.
    }

    public void testAmericaNewYork_FEB() throws Exception {
        runTest("America/New_York", Calendar.FEBRUARY);
    }

    public void testEuropeBerlin_FEB() throws Exception {
        runTest("Europe/Berlin", Calendar.FEBRUARY);
    }

    public void testAfricaBanjul_FEB() throws Exception {
        runTest("Africa/Banjul", Calendar.FEBRUARY); // This one is UTC time without DST.
    }

    public void testAmericaNewYork_MAR() throws Exception {
        runTest("America/New_York", Calendar.MARCH);
    }

    public void testEuropeBerlin_MAR() throws Exception {
        runTest("Europe/Berlin", Calendar.MARCH);
    }

    public void testAfricaBanjul_MAR() throws Exception {
        runTest("Africa/Banjul", Calendar.MARCH); // This one is UTC time without DST.
    }

    public void testAmericaNewYork_APR() throws Exception {
        runTest("America/New_York", Calendar.APRIL);
    }

    public void testEuropeBerlin_APR() throws Exception {
        runTest("Europe/Berlin", Calendar.APRIL);
    }

    public void testAfricaBanjul_APR() throws Exception {
        runTest("Africa/Banjul", Calendar.APRIL); // This one is UTC time without DST.
    }

    public void testAmericaNewYork_MAY() throws Exception {
        runTest("America/New_York", Calendar.MAY);
    }

    public void testEuropeBerlin_MAY() throws Exception {
        runTest("Europe/Berlin", Calendar.MAY);
    }

    public void testAfricaBanjul_MAY() throws Exception {
        runTest("Africa/Banjul", Calendar.MAY); // This one is UTC time without DST.
    }

    public void testAmericaNewYork_JUN() throws Exception {
        runTest("America/New_York", Calendar.JUNE);
    }

    public void testEuropeBerlin_JUN() throws Exception {
        runTest("Europe/Berlin", Calendar.JUNE);
    }

    public void testAfricaBanjul_JUN() throws Exception {
        runTest("Africa/Banjul", Calendar.JUNE); // This one is UTC time without DST.
    }

    public void testAmericaNewYork_JUL() throws Exception {
        runTest("America/New_York", Calendar.JULY);
    }

    public void testEuropeBerlin_JUL() throws Exception {
        runTest("Europe/Berlin", Calendar.JULY);
    }

    public void testAfricaBanjul_JUL() throws Exception {
        runTest("Africa/Banjul", Calendar.JULY); // This one is UTC time without DST.
    }

    public void testAmericaNewYork_AUG() throws Exception {
        runTest("America/New_York", Calendar.AUGUST);
    }

    public void testEuropeBerlin_AUG() throws Exception {
        runTest("Europe/Berlin", Calendar.AUGUST);
    }

    public void testAfricaBanjul_AUG() throws Exception {
        runTest("Africa/Banjul", Calendar.AUGUST); // This one is UTC time without DST.
    }

    public void testAmericaNewYork_SEP() throws Exception {
        runTest("America/New_York", Calendar.SEPTEMBER);
    }

    public void testEuropeBerlin_SEP() throws Exception {
        runTest("Europe/Berlin", Calendar.SEPTEMBER);
    }

    public void testAfricaBanjul_SEP() throws Exception {
        runTest("Africa/Banjul", Calendar.SEPTEMBER); // This one is UTC time without DST.
    }

    public void testAmericaNewYork_OCT() throws Exception {
        runTest("America/New_York", Calendar.OCTOBER);
    }

    public void testEuropeBerlin_OCT() throws Exception {
        runTest("Europe/Berlin", Calendar.OCTOBER);
    }

    public void testAfricaBanjul_OCT() throws Exception {
        runTest("Africa/Banjul", Calendar.OCTOBER); // This one is UTC time without DST.
    }

    public void testAmericaNewYork_NOV() throws Exception {
        runTest("America/New_York", Calendar.NOVEMBER);
    }

    public void testEuropeBerlin_NOV() throws Exception {
        runTest("Europe/Berlin", Calendar.NOVEMBER);
    }

    public void testAfricaBanjul_NOV() throws Exception {
        runTest("Africa/Banjul", Calendar.NOVEMBER); // This one is UTC time without DST.
    }

    public void testAmericaNewYork_DEC() throws Exception {
        runTest("America/New_York", Calendar.DECEMBER);
    }

    public void testEuropeBerlin_DEC() throws Exception {
        runTest("Europe/Berlin", Calendar.DECEMBER);
    }

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
        ctm.setTimezone(tz);
        ctm.insert(appointment);
        int year = 2015;
        if (month < Calendar.MAY) {
            year++;
        }
        Appointment loadedAppointment = ctm.get(appointment.getParentFolderID(), appointment.getObjectID());
        assertEquals("Wrong start for month " + (month+1), D("16." + (month+1) + "." + year + " 08:00", tz), loadedAppointment.getStartDate());
        assertEquals("Wrong end for month " + (month+1), D("16." + (month+1) + "." + year + " 09:00", tz), loadedAppointment.getEndDate());
    }

    @Override
    public void tearDown() throws Exception {
        ctm.cleanUp();
        SetRequest setRequest = new SetRequest(Tree.TimeZone, origTimeZone);
        getClient().execute(setRequest);
        super.tearDown();
    }

}
