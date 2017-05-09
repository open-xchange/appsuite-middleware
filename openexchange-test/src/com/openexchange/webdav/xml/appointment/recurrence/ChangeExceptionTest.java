
package com.openexchange.webdav.xml.appointment.recurrence;

import java.util.Date;
import org.junit.Test;
import com.openexchange.groupware.container.Appointment;

public class ChangeExceptionTest extends AbstractRecurrenceTest {

    public ChangeExceptionTest() {
        super();
    }

    @Test
    public void testExceptionInTime() throws Throwable {
        int objectId = 0;
        try {
            final Date startDate = simpleDateFormatUTC.parse("2009-01-01 08:00:00");
            final Date endDate = simpleDateFormatUTC.parse("2009-01-01 10:00:00");

            final Appointment appointmentObj = new Appointment();
            appointmentObj.setTitle("testExceptionInTime - master");
            appointmentObj.setStartDate(startDate);
            appointmentObj.setEndDate(endDate);
            appointmentObj.setShownAs(Appointment.ABSENT);
            appointmentObj.setParentFolderID(appointmentFolderId);
            appointmentObj.setRecurrenceType(Appointment.DAILY);
            appointmentObj.setInterval(1);
            appointmentObj.setIgnoreConflicts(true);
            objectId = insertAppointment(getWebConversation(), appointmentObj, getHostURI(), getLogin(), getPassword());

            final Date startDateException = simpleDateFormatUTC.parse("2009-01-03 10:00:00");
            final Date endDateException = simpleDateFormatUTC.parse("2009-01-03 12:00:00");

            final Appointment exception = new Appointment();
            exception.setTitle("testExceptionInTime - exception");
            exception.setStartDate(startDateException);
            exception.setEndDate(endDateException);
            exception.setIgnoreConflicts(true);
            updateAppointment(getWebConversation(), exception, objectId, appointmentFolderId, getHostURI(), getLogin(), getPassword());
        } finally {
            if (objectId != 0) {
                deleteAppointment(getWebConversation(), objectId, appointmentFolderId, getHostURI(), getLogin(), getPassword());
            }
        }
    }

}
