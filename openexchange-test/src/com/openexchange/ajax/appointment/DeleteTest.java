
package com.openexchange.ajax.appointment;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import org.junit.Test;
import com.openexchange.ajax.AppointmentTest;
import com.openexchange.groupware.container.Appointment;

public class DeleteTest extends AppointmentTest {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DeleteTest.class);

    @Test
    public void testDelete() throws Exception {
        final Appointment appointmentObj = createAppointmentObject("testDelete");
        appointmentObj.setIgnoreConflicts(true);
        final int id = catm.insert(appointmentObj).getObjectID();

        catm.delete(appointmentObj, false, false);

        catm.delete(appointmentObj, false, true);
        assertTrue(catm.getLastResponse().hasError());
    }

    @Test
    public void testDeleteRecurrenceWithPosition() throws Exception {
        final Calendar c = Calendar.getInstance();
        c.setTimeZone(TimeZone.getTimeZone("UTC"));
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        final Date until = new Date(c.getTimeInMillis() + (15 * dayInMillis));

        final int changeExceptionPosition = 3;

        Appointment appointmentObj = new Appointment();
        appointmentObj.setTitle("testDeleteRecurrenceWithPosition");
        appointmentObj.setStartDate(new Date(startTime));
        appointmentObj.setEndDate(new Date(endTime));
        appointmentObj.setShownAs(Appointment.ABSENT);
        appointmentObj.setParentFolderID(appointmentFolderId);
        appointmentObj.setRecurrenceType(Appointment.DAILY);
        appointmentObj.setOrganizer(testUser.getUser());
        appointmentObj.setInterval(1);
        appointmentObj.setUntil(until);
        appointmentObj.setIgnoreConflicts(true);
        final int objectId = catm.insert(appointmentObj).getObjectID();

        Appointment loadAppointment = catm.get(appointmentFolderId, objectId);
        compareObject(appointmentObj, loadAppointment, startTime, endTime);

        appointmentObj = catm.createIdentifyingCopy(appointmentObj);
        appointmentObj.setTitle("testDeleteRecurrenceWithPosition - exception");
        appointmentObj.setStartDate(new Date(startTime + 60 * 60 * 1000));
        appointmentObj.setEndDate(new Date(endTime + 60 * 60 * 1000));
        appointmentObj.setOrganizer(testUser.getUser());
        appointmentObj.setShownAs(Appointment.ABSENT);
        appointmentObj.setOrganizer(testUser.getUser());
        appointmentObj.setParentFolderID(appointmentFolderId);
        appointmentObj.setRecurrencePosition(changeExceptionPosition);
        appointmentObj.setIgnoreConflicts(true);
//        appointmentObj.setLastModified(new Date(System.currentTimeMillis() + APPEND_MODIFIED));

        catm.update(appointmentFolderId, appointmentObj);
        int exceptionId = appointmentObj.getObjectID();

        assertFalse("object id of the update is equals with the old object id", exceptionId == objectId);

        loadAppointment = catm.get(appointmentFolderId, exceptionId);

        // Loaded exception MUST NOT contain any recurrence information except recurrence identifier and position.
        compareObject(appointmentObj, loadAppointment, appointmentObj.getStartDate().getTime(), appointmentObj.getEndDate().getTime());

        loadAppointment = catm.get(appointmentFolderId, exceptionId);
        compareObject(appointmentObj, loadAppointment, appointmentObj.getStartDate().getTime(), appointmentObj.getEndDate().getTime());
    }

    // Bug #12173 
    @Test
    public void testDeleteRecurrenceWithDate() throws Exception {
        final Calendar c = Calendar.getInstance();
        c.setTimeZone(TimeZone.getTimeZone("UTC"));
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        final Date until = new Date(c.getTimeInMillis() + (15 * dayInMillis));

        final int changeExceptionPosition = 3;
        final Date exceptionDate = new Date(c.getTimeInMillis() + (changeExceptionPosition * dayInMillis));

        final Appointment appointmentObj = new Appointment();
        appointmentObj.setTitle("testDeleteRecurrenceWithDate");
        appointmentObj.setStartDate(new Date(startTime));
        appointmentObj.setEndDate(new Date(endTime));
        appointmentObj.setShownAs(Appointment.ABSENT);
        appointmentObj.setParentFolderID(appointmentFolderId);
        appointmentObj.setRecurrenceType(Appointment.DAILY);
        appointmentObj.setOrganizer(testUser.getUser());
        appointmentObj.setInterval(1);
        appointmentObj.setUntil(until);
        appointmentObj.setIgnoreConflicts(true);
        final int objectId = catm.insert(appointmentObj).getObjectID();
        
        Appointment loadAppointment = catm.get(appointmentFolderId, objectId);
        compareObject(appointmentObj, loadAppointment, startTime, endTime);
        
        catm.delete(appointmentObj, exceptionDate);
        assertFalse(catm.getLastResponse().hasError());
        
        loadAppointment = catm.get(appointmentFolderId, objectId);
        
        assertNotNull(loadAppointment);
    }
}
