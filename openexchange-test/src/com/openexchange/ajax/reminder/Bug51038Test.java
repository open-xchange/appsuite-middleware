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

package com.openexchange.ajax.reminder;

import java.util.Calendar;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.ajax.reminder.actions.RangeRequest;
import com.openexchange.ajax.reminder.actions.RangeResponse;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.CommonDeleteResponse;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.reminder.ReminderObject;
import com.openexchange.test.CalendarTestManager;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class Bug51038Test extends AbstractAJAXSession {

    private CalendarTestManager ctm1;
    private AJAXClient client2;
    private CalendarTestManager ctm2;
    private int nextYear;
    private ReminderObject reminder;
    private Appointment appointment;

    public Bug51038Test(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        nextYear = Calendar.getInstance().get(Calendar.YEAR) + 1;
        super.setUp();
        ctm1 = new CalendarTestManager(client);
        client2 = new AJAXClient(User.User2);
        ctm2 = new CalendarTestManager(client2);

        appointment = new Appointment();
        appointment.setTitle(this.getClass().getSimpleName());
        appointment.setStartDate(TimeTools.D("17.01." + nextYear + " 11:00"));
        appointment.setEndDate(TimeTools.D("17.01." + nextYear + " 12:00"));
        appointment.setAlarm(15);
        appointment.setParentFolderID(client.getValues().getPrivateAppointmentFolder());
        appointment.setIgnoreConflicts(true);

        ctm1.insert(appointment);
    }

    public void testBug51038() throws Exception {

        Calendar cal = Calendar.getInstance(client.getValues().getTimeZone());
        cal.setTime(appointment.getEndDate());
        RangeRequest rangeReq = new RangeRequest(cal.getTime());
        RangeResponse rangeResp = client.execute(rangeReq);
        reminder = ReminderTools.searchByTarget(rangeResp.getReminder(client.getValues().getTimeZone()), appointment.getObjectID());
        int reminderId = reminder.getObjectId();
        
        com.openexchange.ajax.reminder.actions.DeleteRequest delReminderReq = new com.openexchange.ajax.reminder.actions.DeleteRequest(reminder, false);
        CommonDeleteResponse response = client2.execute(delReminderReq);
        assertTrue("Expected error.", response.hasError());

        rangeReq = new RangeRequest(cal.getTime());
        rangeResp = client.execute(rangeReq);
        reminder = ReminderTools.searchByTarget(rangeResp.getReminder(client.getValues().getTimeZone()), appointment.getObjectID());
        assertNotNull("Missing reminder.", reminder);
        assertEquals("Wrong reminder.", reminderId, reminder.getObjectId());
    }

    @Override
    public void tearDown() throws Exception {
        ctm1.cleanUp();
        ctm2.cleanUp();
        super.tearDown();
    }

}
