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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import org.junit.Test;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.reminder.ReminderObject;
import com.openexchange.test.CalendarTestManager;

public class UpdatesTest extends ReminderTest {

    @Test
    public void testRange() throws Exception {
        final int userId = getClient().getValues().getUserId();
        final TimeZone timeZone = getClient().getValues().getTimeZone();

        Calendar c = TimeTools.createCalendar(timeZone);
        c.add(Calendar.HOUR_OF_DAY, 2);

        final long startTime = c.getTimeInMillis();
        final long endTime = startTime + 3600000;

        final int parentFolderId = getClient().getValues().getPrivateAppointmentFolder();

        final Appointment appointmentObj = CalendarTestManager.createAppointmentObject(parentFolderId, "testRange", new Date(startTime), new Date(endTime));
        appointmentObj.setAlarm(45);
        appointmentObj.setIgnoreConflicts(true);

        final int targetId = catm.insert(appointmentObj).getObjectID();
        
        List<ReminderObject> reminderObj = remTm.updates(new Date(System.currentTimeMillis() - 5000));
       
        ReminderObject selected = null;
        for (ReminderObject current : reminderObj) {
            if (current.getTargetId() == targetId) {
                selected = current;
            }
        }

        assertTrue("reminder not found in response", (selected.getTargetId() > -1));

        assertTrue("object id not found", selected.getObjectId() > 0);
        assertNotNull("last modified is null", selected.getLastModified());
        assertEquals("target id is not equals", targetId, selected.getTargetId());
        assertEquals("folder id is not equals", parentFolderId, selected.getFolder());
        assertEquals("user id is not equals", userId, selected.getUser());

        final long expectedAlarm = startTime - (45 * 60 * 1000);
        assertEquals("alarm is not equals", new Date(expectedAlarm), selected.getDate());
    }
}
