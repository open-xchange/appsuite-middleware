
package com.openexchange.webdav.xml.appointment;

import static org.junit.Assert.assertTrue;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import org.junit.Test;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.webdav.xml.AppointmentTest;

/**
 * {@link Bug12553Test}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public class Bug12553Test extends AppointmentTest {

    @Test
    public void testBug12553() throws Exception {
        int objectId = -1;
        try {
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

            /*
             * Create daily recurring appointment
             */
            final Appointment appointmentObj = new Appointment();
            appointmentObj.setTitle("testBug12494");
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

            objectId = insertAppointment(getWebConversation(), appointmentObj, getHostURI(), getLogin(), getPassword());

            /*
             * Create a change exception
             */
            final Appointment recurrenceUpdate = new Appointment();
            recurrenceUpdate.setTitle("testBug12494 - exception");
            recurrenceUpdate.setStartDate(new Date(startTime.getTime() + 600000));
            recurrenceUpdate.setEndDate(new Date(endTime.getTime() + 600000));
            recurrenceUpdate.setRecurrenceDatePosition(recurrenceDatePosition);
            recurrenceUpdate.setShownAs(Appointment.ABSENT);
            recurrenceUpdate.setParentFolderID(appointmentFolderId);
            recurrenceUpdate.setIgnoreConflicts(true);
            recurrenceUpdate.setUsers(users);
            // recurrenceUpdate.setAlarm(60);
            updateAppointment(getWebConversation(), recurrenceUpdate, objectId, appointmentFolderId, getHostURI(), getLogin(), getPassword());

            /*
             * Delete change exception through an update on master recurring
             * appointment
             */
            final Date[] deleteExceptions = new Date[] { recurrenceDatePosition };
            appointmentObj.setObjectID(objectId);
            appointmentObj.setDeleteExceptions(deleteExceptions);
            appointmentObj.setChangeExceptions((Date[]) null);
            appointmentObj.setIgnoreConflicts(true);
            updateAppointment(getWebConversation(), appointmentObj, objectId, appointmentFolderId, getHostURI(), getLogin(), getPassword());

            /*
             * Load updated recurring appointment
             */
            final Appointment loadedRecApp = loadAppointment(getWebConversation(), objectId, appointmentFolderId, getHostURI(), getLogin(), getPassword());

            assertEqualsAndNotNull("Unexpected delete exceptions", deleteExceptions, loadedRecApp.getDeleteException());
            final Date[] changeExceptions = loadedRecApp.getChangeException();
            assertTrue("", null == changeExceptions || changeExceptions.length == 0);
        } finally {
            if (objectId != -1) {
                deleteAppointment(getWebConversation(), objectId, appointmentFolderId, getHostURI(), getLogin(), getPassword());
            }
        }
    }

}
