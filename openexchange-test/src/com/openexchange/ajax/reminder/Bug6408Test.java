
package com.openexchange.ajax.reminder;

import static org.junit.Assert.assertTrue;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import org.junit.Test;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.ajax.reminder.actions.RangeRequest;
import com.openexchange.ajax.reminder.actions.RangeResponse;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.reminder.ReminderObject;

public class Bug6408Test extends ReminderTest {

    public Bug6408Test() {
        super();
    }

    @Test
    public void testBug6408() throws Exception {
        final TimeZone timeZone = getClient().getValues().getTimeZone();

        final Calendar c = Calendar.getInstance();
        c.setTimeZone(timeZone);
        c.add(Calendar.DAY_OF_MONTH, +2);

        c.set(Calendar.HOUR_OF_DAY, 8);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        final long startTime = c.getTimeInMillis();
        final long endTime = startTime + 3600000;

        final int folderId = getClient().getValues().getPrivateAppointmentFolder();

        final int alarmMinutes = 60;

        final Appointment appointmentObj = new Appointment();
        appointmentObj.setTitle("testBug6408");
        appointmentObj.setStartDate(new Date(startTime));
        appointmentObj.setEndDate(new Date(endTime));
        appointmentObj.setShownAs(Appointment.ABSENT);
        appointmentObj.setAlarm(alarmMinutes);
        appointmentObj.setParentFolderID(folderId);
        appointmentObj.setIgnoreConflicts(true);

        final int targetId = catm.insert(appointmentObj).getObjectID();

        final ReminderObject reminderObj = new ReminderObject();
        reminderObj.setTargetId(targetId);
        reminderObj.setFolder(folderId);
        reminderObj.setDate(new Date(startTime - (alarmMinutes * 60 * 1000)));

        final RangeRequest request = new RangeRequest(c.getTime());
        RangeResponse response = Executor.execute(getClient(), request);
        ReminderObject[] reminderArray = response.getReminder(timeZone);

        int pos = -1;
        for (int a = 0; a < reminderArray.length; a++) {
            if (reminderArray[a].getTargetId() == targetId) {
                pos = a;
                reminderObj.setObjectId(reminderArray[a].getObjectId());
                compareReminder(reminderObj, reminderArray[a]);
            }
        }

        final int newAlarmMinutes = 60 * 24 * 7 * 4;

        appointmentObj.removeParentFolderID();
        appointmentObj.setAlarm(newAlarmMinutes);

        final long alarmInMillies = newAlarmMinutes * 60 * 1000L;

        reminderObj.setDate(new Date(startTime - alarmInMillies));

        catm.update(folderId, appointmentObj);

        response = Executor.execute(getClient(), request);
        reminderArray = response.getReminder(timeZone);

        boolean found = false;

        pos = -1;
        for (int a = 0; a < reminderArray.length; a++) {
            if (reminderArray[a].getTargetId() == targetId) {
                pos = a;
                reminderObj.setObjectId(reminderArray[a].getObjectId());
                compareReminder(reminderObj, reminderArray[a]);
                found = true;
            }
        }

        assertTrue("no reminder find for target id " + targetId + " in response", found);

        remTm.delete(reminderArray[pos]);
    }
}
