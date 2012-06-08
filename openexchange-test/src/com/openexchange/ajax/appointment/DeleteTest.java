
package com.openexchange.ajax.appointment;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.ajax.AppointmentTest;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.groupware.container.Appointment;

public class DeleteTest extends AppointmentTest {

    private static final Log LOG = LogFactory.getLog(DeleteTest.class);

    public DeleteTest(final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testDelete() throws Exception {
        final Appointment appointmentObj = createAppointmentObject("testDelete");
        appointmentObj.setIgnoreConflicts(true);
        insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
        final int id = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());

        deleteAppointment(getWebConversation(), id, appointmentFolderId, PROTOCOL + getHostName(), getSessionId());
        try {
            deleteAppointment(getWebConversation(), id, appointmentFolderId, PROTOCOL + getHostName(), getSessionId());
            fail("OXObjectNotFoundException expected!");
        } catch (final Exception ex) {
            assertTrue(true);
        }
    }

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
        appointmentObj.setOrganizer(User.User1.name());
        appointmentObj.setInterval(1);
        appointmentObj.setUntil(until);
        appointmentObj.setIgnoreConflicts(true);
        final int objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
        appointmentObj.setObjectID(objectId);
        Appointment loadAppointment = loadAppointment(
            getWebConversation(),
            objectId,
            appointmentFolderId,
            timeZone,
            PROTOCOL + getHostName(),
            getSessionId());
        compareObject(appointmentObj, loadAppointment, startTime, endTime);

        appointmentObj = new Appointment();
        appointmentObj.setTitle("testDeleteRecurrenceWithPosition - exception");
        appointmentObj.setStartDate(new Date(startTime + 60 * 60 * 1000));
        appointmentObj.setEndDate(new Date(endTime + 60 * 60 * 1000));
        appointmentObj.setOrganizer(User.User1.name());
        appointmentObj.setShownAs(Appointment.ABSENT);
        appointmentObj.setOrganizer(User.User1.name());
        appointmentObj.setParentFolderID(appointmentFolderId);
        appointmentObj.setRecurrencePosition(changeExceptionPosition);
        appointmentObj.setIgnoreConflicts(true);

        final int newObjectId = updateAppointment(
            getWebConversation(),
            appointmentObj,
            objectId,
            appointmentFolderId,
            timeZone,
            PROTOCOL + getHostName(),
            getSessionId());
        appointmentObj.setObjectID(newObjectId);

        assertFalse("object id of the update is equals with the old object id", newObjectId == objectId);

        loadAppointment = loadAppointment(
            getWebConversation(),
            newObjectId,
            appointmentFolderId,
            timeZone,
            PROTOCOL + getHostName(),
            getSessionId());

        // Loaded exception MUST NOT contain any recurrence information except recurrence identifier and position.
        compareObject(appointmentObj, loadAppointment, appointmentObj.getStartDate().getTime(), appointmentObj.getEndDate().getTime());

        loadAppointment = loadAppointment(
            getWebConversation(),
            newObjectId,
            appointmentFolderId,
            timeZone,
            PROTOCOL + getHostName(),
            getSessionId());
        compareObject(appointmentObj, loadAppointment, appointmentObj.getStartDate().getTime(), appointmentObj.getEndDate().getTime());

        deleteAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getSessionId());
    }

    // Bug #12173
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
        appointmentObj.setOrganizer(User.User1.name());
        appointmentObj.setInterval(1);
        appointmentObj.setUntil(until);
        appointmentObj.setIgnoreConflicts(true);
        final int objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
        appointmentObj.setObjectID(objectId);
        Appointment loadAppointment = loadAppointment(
            getWebConversation(),
            objectId,
            appointmentFolderId,
            timeZone,
            PROTOCOL + getHostName(),
            getSessionId());
        compareObject(appointmentObj, loadAppointment, startTime, endTime);

        deleteAppointment(
            getWebConversation(),
            objectId,
            appointmentFolderId,
            exceptionDate,
            new Date(Long.MAX_VALUE),
            PROTOCOL + getHostName(),
            getSessionId());

        loadAppointment = loadAppointment(
            getWebConversation(),
            objectId,
            appointmentFolderId,
            timeZone,
            PROTOCOL + getHostName(),
            getSessionId());
        // May not fail

        // Delete all
        deleteAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getSessionId());

    }
}
