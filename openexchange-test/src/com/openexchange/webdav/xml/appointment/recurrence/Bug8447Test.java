
package com.openexchange.webdav.xml.appointment.recurrence;

import static org.junit.Assert.fail;
import java.util.Date;
import java.util.Locale;
import org.junit.Test;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.webdav.xml.XmlServlet;

public class Bug8447Test extends AbstractRecurrenceTest {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Bug8447Test.class);

    public Bug8447Test() {
        super();
        simpleDateFormatUTC.setTimeZone(timeZoneUTC);
    }

    @Test
    public void testBug8447() throws Exception {
        new Date();

        final Date startDate = simpleDateFormatUTC.parse("2007-06-01 00:00:00");
        final Date endDate = simpleDateFormatUTC.parse("2007-06-02 00:00:00");

        final Date until = simpleDateFormatUTC.parse("2007-06-15 00:00:00");

        final Appointment appointmentObj = new Appointment();
        appointmentObj.setTitle("testBug8447");
        appointmentObj.setStartDate(startDate);
        appointmentObj.setEndDate(endDate);
        appointmentObj.setFullTime(true);
        appointmentObj.setShownAs(Appointment.ABSENT);
        appointmentObj.setParentFolderID(appointmentFolderId);
        appointmentObj.setRecurrenceType(Appointment.DAILY);
        appointmentObj.setInterval(1);
        appointmentObj.setUntil(until);
        appointmentObj.setIgnoreConflicts(true);

        final int objectId = insertAppointment(getWebConversation(), appointmentObj, getHostURI(), getLogin(), getPassword());

        final Date exceptionStartDate = simpleDateFormatUTC.parse("2007-06-06 00:00:00");
        final Date exceptionEndDate = simpleDateFormatUTC.parse("2007-06-07 00:00:00");

        final Date recurrenceDatePosition = simpleDateFormatUTC.parse("2007-06-06 00:00:00");

        final Appointment exceptionAppointmentObject = new Appointment();
        exceptionAppointmentObject.setTitle("testBug8447 - change exception (2007-06-06)");
        exceptionAppointmentObject.setStartDate(exceptionStartDate);
        exceptionAppointmentObject.setEndDate(exceptionEndDate);
        exceptionAppointmentObject.setFullTime(true);
        exceptionAppointmentObject.setRecurrenceDatePosition(recurrenceDatePosition);
        exceptionAppointmentObject.setShownAs(Appointment.ABSENT);
        exceptionAppointmentObject.setParentFolderID(appointmentFolderId);
        exceptionAppointmentObject.setIgnoreConflicts(true);

        final int exceptionObjectId = updateAppointment(getWebConversation(), exceptionAppointmentObject, objectId, appointmentFolderId, getHostURI(), getLogin(), getPassword());

        appointmentObj.setObjectID(objectId);
        appointmentObj.setDeleteExceptions(new Date[] { recurrenceDatePosition });

        updateAppointment(getWebConversation(), appointmentObj, objectId, appointmentFolderId, getHostURI(), getLogin(), getPassword());

        try {
            loadAppointment(webCon, exceptionObjectId, appointmentFolderId, getHostURI(), login, password);
            fail("object not found exception expected!");
        } catch (final OXException exc) {
            assertExceptionMessage(exc.getDisplayMessage(Locale.ENGLISH), XmlServlet.OBJECT_NOT_FOUND_STATUS);
        }

        deleteAppointment(getWebConversation(), objectId, appointmentFolderId, getHostURI(), getLogin(), getPassword());
    }
}
