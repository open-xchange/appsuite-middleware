
package com.openexchange.webdav.xml.appointment;

import static org.junit.Assert.fail;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import org.junit.Test;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.webdav.xml.AppointmentTest;
import com.openexchange.webdav.xml.XmlServlet;

public class DeleteTest extends AppointmentTest {

    public DeleteTest() {
        super();
    }

    @Test
    public void testDelete() throws Exception {
        final Appointment appointmentObj = createAppointmentObject("testDelete");
        appointmentObj.setIgnoreConflicts(true);
        final int objectId1 = insertAppointment(webCon, appointmentObj, getHostURI(), login, password);
        final int objectId2 = insertAppointment(webCon, appointmentObj, getHostURI(), login, password);

        final int[][] objectIdAndFolderId = { { objectId1, appointmentFolderId }, { objectId2, appointmentFolderId } };

        deleteAppointment(webCon, objectIdAndFolderId, getHostURI(), login, password);
    }

    @Test
    public void testDeleteConcurentConflict() throws Exception {
        final Appointment appointmentObj = createAppointmentObject("testUpdateAppointmentConcurentConflict");
        appointmentObj.setIgnoreConflicts(true);
        final int objectId = insertAppointment(webCon, appointmentObj, getHostURI(), login, password);

        try {
            deleteAppointment(webCon, objectId, appointmentFolderId, new Date(0), getHostURI(), login, password);
            fail("expected concurent modification exception!");
        } catch (final OXException exc) {
            assertExceptionMessage(exc.getDisplayMessage(Locale.ENGLISH), XmlServlet.MODIFICATION_STATUS);
        }

        deleteAppointment(webCon, objectId, appointmentFolderId, getHostURI(), login, password);
    }

    @Test
    public void testDeleteNotFound() throws Exception {
        final Appointment appointmentObj = createAppointmentObject("testUpdateAppointmentNotFound");
        appointmentObj.setIgnoreConflicts(true);
        final int objectId = insertAppointment(webCon, appointmentObj, getHostURI(), login, password);

        try {
            deleteAppointment(webCon, (objectId + 1000), appointmentFolderId, getHostURI(), login, password);
            fail("expected object not found exception!");
        } catch (final OXException exc) {
            assertExceptionMessage(exc.getDisplayMessage(Locale.ENGLISH), XmlServlet.OBJECT_NOT_FOUND_STATUS);
        }

        deleteAppointment(webCon, objectId, appointmentFolderId, getHostURI(), login, password);
    }

    @Test
    public void testDeleteRecurrenceWithDatePosition() throws Exception {
        final Calendar c = Calendar.getInstance();
        c.setTimeZone(TimeZone.getTimeZone("UTC"));
        c.setTime(startTime);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        final Date until = new Date(c.getTimeInMillis() + (15 * dayInMillis));

        final int changeExceptionPosition = 3;

        final Appointment appointmentObj = new Appointment();
        appointmentObj.setTitle("testDeleteRecurrenceWithDatePosition");
        appointmentObj.setStartDate(startTime);
        appointmentObj.setEndDate(endTime);
        appointmentObj.setShownAs(Appointment.ABSENT);
        appointmentObj.setParentFolderID(appointmentFolderId);
        appointmentObj.setRecurrenceType(Appointment.DAILY);
        appointmentObj.setInterval(1);
        appointmentObj.setUntil(until);
        appointmentObj.setIgnoreConflicts(true);
        final int objectId = insertAppointment(webCon, appointmentObj, getHostURI(), login, password);
        appointmentObj.setObjectID(objectId);
        Appointment loadAppointment = loadAppointment(webCon, objectId, appointmentFolderId, getHostURI(), login, password);
        compareObject(appointmentObj, loadAppointment);

        final Date modified = loadAppointment.getLastModified();

        deleteAppointment(webCon, objectId, appointmentFolderId, modified, new Date(c.getTimeInMillis() + changeExceptionPosition * dayInMillis), getHostURI(), getLogin(), getPassword());

        loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, getHostURI(), getLogin(), getPassword());
        assertEqualsAndNotNull("delete exception is not equals", loadAppointment.getDeleteException(), new Date[] { new Date(c.getTimeInMillis() + changeExceptionPosition * dayInMillis) });

        deleteAppointment(webCon, new int[][] { { objectId, appointmentFolderId } }, getHostURI(), login, password);
    }

    @Test
    public void testDeleteRecurrenceWithDeleteExceptions() throws Exception {
        final Calendar c = Calendar.getInstance();
        c.setTimeZone(TimeZone.getTimeZone("UTC"));
        c.setTime(startTime);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        final Date until = new Date(c.getTimeInMillis() + (15 * dayInMillis));

        final int changeExceptionPosition = 3;

        final Appointment appointmentObj = new Appointment();
        appointmentObj.setTitle("testDeleteRecurrenceWithDeleteExceptions");
        appointmentObj.setStartDate(startTime);
        appointmentObj.setEndDate(endTime);
        appointmentObj.setShownAs(Appointment.ABSENT);
        appointmentObj.setParentFolderID(appointmentFolderId);
        appointmentObj.setRecurrenceType(Appointment.DAILY);
        appointmentObj.setInterval(1);
        appointmentObj.setUntil(until);
        appointmentObj.setIgnoreConflicts(true);
        final int objectId = insertAppointment(webCon, appointmentObj, getHostURI(), login, password);
        appointmentObj.setObjectID(objectId);

        Appointment loadAppointment = loadAppointment(webCon, objectId, appointmentFolderId, getHostURI(), login, password);
        final Date modified = loadAppointment.getLastModified();

        loadAppointment = loadAppointment(webCon, objectId, appointmentFolderId, decrementDate(modified), getHostURI(), login, password);
        compareObject(appointmentObj, loadAppointment);

        appointmentObj.setDeleteExceptions(new Date[] { new Date(c.getTimeInMillis() + changeExceptionPosition * dayInMillis) });

        updateAppointment(getWebConversation(), appointmentObj, objectId, appointmentFolderId, getHostURI(), getLogin(), getPassword());

        loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, decrementDate(modified), getHostURI(), getLogin(), getPassword());
        compareObject(appointmentObj, loadAppointment);

        deleteAppointment(webCon, new int[][] { { objectId, appointmentFolderId } }, getHostURI(), login, password);
    }

}
