
package com.openexchange.webdav.xml.appointment.recurrence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import org.junit.Test;
import com.openexchange.groupware.container.Appointment;

public class Bug6960Test extends AbstractRecurrenceTest {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Bug6960Test.class);

    public Bug6960Test() {
        super();
    }

    @Test
    public void testBug6960() throws Exception {
        final Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.setTime(startTime);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        calendar.add(Calendar.DAY_OF_MONTH, 3);

        final Date recurrenceDatePosition = calendar.getTime();

        calendar.add(Calendar.DAY_OF_MONTH, 3);

        final Date until = calendar.getTime();

        final Appointment appointmentObj = new Appointment();
        appointmentObj.setTitle("testBug6960");
        appointmentObj.setStartDate(startTime);
        appointmentObj.setEndDate(endTime);
        appointmentObj.setShownAs(Appointment.ABSENT);
        appointmentObj.setParentFolderID(appointmentFolderId);
        appointmentObj.setRecurrenceType(Appointment.DAILY);
        appointmentObj.setInterval(1);
        appointmentObj.setUntil(until);
        appointmentObj.setIgnoreConflicts(true);

        final int objectId = insertAppointment(getWebConversation(), appointmentObj, getHostURI(), getLogin(), getPassword());

        final Calendar calendarException = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendarException.setTime(recurrenceDatePosition);
        calendarException.set(Calendar.HOUR_OF_DAY, 10);

        final Date exceptionStartDate = calendarException.getTime();

        calendarException.set(Calendar.HOUR_OF_DAY, 12);

        final Date exceptionEndDate = calendarException.getTime();

        final Appointment exceptionAppointmentObject = new Appointment();
        exceptionAppointmentObject.setTitle("testBug6960 - change exception");
        exceptionAppointmentObject.setStartDate(exceptionStartDate);
        exceptionAppointmentObject.setEndDate(exceptionEndDate);
        exceptionAppointmentObject.setRecurrenceDatePosition(recurrenceDatePosition);
        exceptionAppointmentObject.setShownAs(Appointment.ABSENT);
        exceptionAppointmentObject.setParentFolderID(appointmentFolderId);
        exceptionAppointmentObject.setIgnoreConflicts(true);

        final int exceptionObjectId = updateAppointment(getWebConversation(), exceptionAppointmentObject, objectId, appointmentFolderId, getHostURI(), getLogin(), getPassword());

        final Appointment loadAppointment = loadAppointment(getWebConversation(), exceptionObjectId, appointmentFolderId, getHostURI(), getLogin(), getPassword());
        final Date modified = loadAppointment.getLastModified();

        deleteAppointment(getWebConversation(), exceptionObjectId, appointmentFolderId, recurrenceDatePosition, getHostURI(), getLogin(), getPassword());

        final Appointment[] appointmentArray = listAppointment(getWebConversation(), appointmentFolderId, modified, true, true, getHostURI(), getLogin(), getPassword());
        boolean found = false;

        for (int a = 0; a < appointmentArray.length; a++) {
            if (appointmentArray[a].getObjectID() == exceptionObjectId) {
                found = true;

                assertEquals("recurrence id not equals expected", objectId, appointmentArray[a].getRecurrenceID());
                break;
            }
        }

        assertTrue("object id " + exceptionObjectId + " not found in response", found);

        deleteAppointment(getWebConversation(), objectId, appointmentFolderId, getHostURI(), getLogin(), getPassword());
    }
}
