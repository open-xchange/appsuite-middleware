
package com.openexchange.webdav.xml.appointment;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import org.junit.Test;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.webdav.xml.AppointmentTest;

public class Bug8453Test extends AppointmentTest {

    @Test
    public void testBug8453() throws Exception {
        final TimeZone timeZoneUTC = TimeZone.getTimeZone("UTC");

        final Calendar calendar = Calendar.getInstance(timeZoneUTC);
        calendar.setTime(startTime);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        calendar.add(Calendar.DAY_OF_MONTH, 2);

        final Date recurrenceDatePosition = calendar.getTime();

        calendar.add(Calendar.DAY_OF_MONTH, 3);

        final Date until = calendar.getTime();

        final Appointment appointmentObj = new Appointment();
        appointmentObj.setTitle("testBug8453");
        appointmentObj.setStartDate(startTime);
        appointmentObj.setEndDate(endTime);
        appointmentObj.setShownAs(Appointment.ABSENT);
        appointmentObj.setParentFolderID(appointmentFolderId);
        appointmentObj.setRecurrenceType(Appointment.DAILY);
        appointmentObj.setInterval(1);
        appointmentObj.setUntil(until);
        appointmentObj.setIgnoreConflicts(true);

        final UserParticipant[] users = new UserParticipant[1];
        users[0] = new UserParticipant(userId);
        users[0].setConfirm(Appointment.ACCEPT);

        appointmentObj.setUsers(users);

        final int objectId = insertAppointment(getWebConversation(), appointmentObj, getHostURI(), getLogin(), getPassword());

        final Appointment recurrenceUpdate = new Appointment();
        recurrenceUpdate.setTitle("testBug8453 - exception");
        recurrenceUpdate.setStartDate(new Date(startTime.getTime() + 600000));
        recurrenceUpdate.setEndDate(new Date(endTime.getTime() + 600000));
        recurrenceUpdate.setRecurrenceDatePosition(recurrenceDatePosition);
        recurrenceUpdate.setShownAs(Appointment.ABSENT);
        recurrenceUpdate.setParentFolderID(appointmentFolderId);
        recurrenceUpdate.setIgnoreConflicts(true);
        recurrenceUpdate.setUsers(users);
        recurrenceUpdate.setAlarm(60);

        updateAppointment(getWebConversation(), recurrenceUpdate, objectId, appointmentFolderId, getHostURI(), getLogin(), getPassword());

        appointmentObj.setObjectID(objectId);
        Appointment loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, getHostURI(), getLogin(), getPassword());
        compareObject(appointmentObj, loadAppointment);

        final Date modified = loadAppointment.getLastModified();

        loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, decrementDate(modified), getHostURI(), getLogin(), getPassword());
        compareObject(appointmentObj, loadAppointment);

        deleteAppointment(getWebConversation(), objectId, appointmentFolderId, getHostURI(), getLogin(), getPassword());
    }
}
