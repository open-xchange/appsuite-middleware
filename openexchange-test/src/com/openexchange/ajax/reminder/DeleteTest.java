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

import static org.junit.Assert.assertNotEquals;
import java.util.Calendar;
import java.util.TimeZone;
import org.junit.Test;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.ajax.reminder.actions.RangeRequest;
import com.openexchange.ajax.reminder.actions.RangeResponse;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.reminder.ReminderObject;
import com.openexchange.test.common.groupware.calendar.TimeTools;

public class DeleteTest extends ReminderTest {

    @Test
    public void testDelete() throws Exception {
        final TimeZone timeZone = getClient().getValues().getTimeZone();

        final Calendar c = TimeTools.createCalendar(timeZone);
        c.add(Calendar.DAY_OF_YEAR, 1);

        final int folderId = getClient().getValues().getPrivateAppointmentFolder();

        final Appointment appointmentObj = new Appointment();
        appointmentObj.setTitle("testDelete");
        appointmentObj.setStartDate(c.getTime());
        c.add(Calendar.HOUR, 1);
        appointmentObj.setEndDate(c.getTime());
        appointmentObj.setShownAs(Appointment.ABSENT);
        appointmentObj.setAlarm(45);
        appointmentObj.setParentFolderID(folderId);
        appointmentObj.setIgnoreConflicts(true);

        final int targetId = catm.insert(appointmentObj).getObjectID();

        final RangeRequest request = new RangeRequest(c.getTime());
        RangeResponse response = Executor.execute(getClient(), request);

        ReminderObject[] reminderObj = response.getReminder(timeZone);

        int pos = -1;
        for (int a = 0; a < reminderObj.length; a++) {
            if (reminderObj[a].getTargetId() == targetId) {
                pos = a;
            }
        }
        assertNotEquals("Reminder not found.", -1, pos);
        remTm.delete(reminderObj[pos]);
    }

    @Test
    public void testDeleteWithNonExisting() throws Exception {
        final TimeZone timeZone = getClient().getValues().getTimeZone();

        final Calendar c = TimeTools.createCalendar(timeZone);
        c.add(Calendar.DAY_OF_YEAR, 1);

        final int folderId = getClient().getValues().getPrivateAppointmentFolder();

        final Appointment appointmentObj = new Appointment();
        appointmentObj.setTitle("testDeleteWithNonExisting");
        appointmentObj.setStartDate(c.getTime());
        c.add(Calendar.HOUR, 1);
        appointmentObj.setEndDate(c.getTime());
        appointmentObj.setShownAs(Appointment.ABSENT);
        appointmentObj.setAlarm(45);
        appointmentObj.setParentFolderID(folderId);
        appointmentObj.setIgnoreConflicts(true);

        final int targetId = catm.insert(appointmentObj).getObjectID();

        final RangeRequest request = new RangeRequest(c.getTime());
        RangeResponse response = Executor.execute(getClient(), request);

        ReminderObject[] reminderObj = response.getReminder(timeZone);

        int pos = -1;
        for (int a = 0; a < reminderObj.length; a++) {
            if (reminderObj[a].getTargetId() == targetId) {
                pos = a;
            }
        }
        assertNotEquals("Reminder not found.", -1, pos);
    }
}
