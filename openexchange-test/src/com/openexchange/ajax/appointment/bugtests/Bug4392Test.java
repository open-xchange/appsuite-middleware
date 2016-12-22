
package com.openexchange.ajax.appointment.bugtests;

import static org.junit.Assert.fail;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import org.junit.Test;
import com.openexchange.ajax.AppointmentTest;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Appointment;

public class Bug4392Test extends AppointmentTest {

    /**
     * This test case check the until date of recurrence appointments
     */
    @Test
    public void testBug4392() throws Exception {
        final int occurrences = 4;

        final Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.setTimeInMillis(startTime);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        calendar.add(Calendar.DAY_OF_MONTH, occurrences - 1);
        final Date until = calendar.getTime();

        final Appointment appointmentObj = createAppointmentObject("testBug4392");
        appointmentObj.setRecurrenceType(Appointment.DAILY);
        appointmentObj.setInterval(1);
        //appointmentObj.setOccurrence(4);
        appointmentObj.setUntil(until);
        appointmentObj.setIgnoreConflicts(true);
        final int objectId = catm.insert(appointmentObj).getObjectID();
        appointmentObj.setObjectID(objectId);

        final Appointment loadAppointment = catm.get(appointmentFolderId, objectId);
        try {
            compareObject(appointmentObj, loadAppointment, appointmentObj.getStartDate().getTime(), appointmentObj.getEndDate().getTime());
        } catch (final OXException exc) {
            fail("exception: " + exc.toString());
        }
    }
}
