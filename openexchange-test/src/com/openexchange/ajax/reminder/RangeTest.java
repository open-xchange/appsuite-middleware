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
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import com.openexchange.ajax.appointment.action.DeleteRequest;
import com.openexchange.ajax.appointment.action.GetRequest;
import com.openexchange.ajax.appointment.action.GetResponse;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.CommonInsertResponse;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.ajax.reminder.actions.RangeRequest;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.reminder.ReminderObject;
import com.openexchange.test.common.groupware.calendar.TimeTools;

public class RangeTest extends AbstractAJAXSession {

    /**
     * Default constructor.
     *
     * @param name Test name.
     */
    public RangeTest() {
        super();
    }

    @Test
    public void testRange() throws Exception {
        final AJAXClient client = getClient();
        final int userId = client.getValues().getUserId();
        final TimeZone timeZone = client.getValues().getTimeZone();

        final Calendar c = TimeTools.createCalendar(timeZone);
        c.add(Calendar.DAY_OF_YEAR, 1);

        final int folderId = client.getValues().getPrivateAppointmentFolder();

        final Appointment appointmentObj = new Appointment();
        appointmentObj.setTitle("testRange");
        appointmentObj.setStartDate(c.getTime());
        c.add(Calendar.HOUR, 1);
        appointmentObj.setEndDate(c.getTime());
        c.add(Calendar.HOUR, -1);
        appointmentObj.setShownAs(Appointment.ABSENT);
        appointmentObj.setAlarm(45);
        appointmentObj.setParentFolderID(folderId);
        appointmentObj.setIgnoreConflicts(true);

        final CommonInsertResponse aInsertR = Executor.execute(client, new InsertRequest(appointmentObj, timeZone));
        final int targetId = aInsertR.getId();
        Date timestamp = aInsertR.getTimestamp();
        try {
            final ReminderObject[] reminderObj = Executor.execute(client, new RangeRequest(new Date(c.getTime().getTime() + TimeUnit.MINUTES.toMillis(1)))).getReminder(timeZone);

            int pos = -1;
            for (int a = 0; a < reminderObj.length; a++) {
                if (reminderObj[a].getTargetId() == targetId) {
                    pos = a;
                }
            }

            assertTrue("reminder not found in response", (pos > -1));
            assertTrue("object id not found", reminderObj[pos].getObjectId() > 0);
            assertNotNull("last modified is null", reminderObj[pos].getLastModified());
            assertEquals("target id is not equals", targetId, reminderObj[pos].getTargetId());
            assertEquals("folder id is not equals", folderId, reminderObj[pos].getFolder());
            assertEquals("user id is not equals", userId, reminderObj[pos].getUser());

            c.add(Calendar.MINUTE, -45);
            final Date expected = c.getTime();
            assertEquals("alarm is not equals", expected, reminderObj[pos].getDate());

            final GetResponse aGetR = Executor.execute(client, new GetRequest(folderId, targetId));
            timestamp = aGetR.getTimestamp();
        } finally {
            Executor.execute(client, new DeleteRequest(targetId, folderId, timestamp));
        }
    }
}
