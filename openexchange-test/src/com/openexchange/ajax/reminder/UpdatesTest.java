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

package com.openexchange.ajax.reminder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import org.junit.Test;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.reminder.ReminderObject;
import com.openexchange.test.CalendarTestManager;
import com.openexchange.test.common.groupware.calendar.TimeTools;

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
        assertNotNull(selected);
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
