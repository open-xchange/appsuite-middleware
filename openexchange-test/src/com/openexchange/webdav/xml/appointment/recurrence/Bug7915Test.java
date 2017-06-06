
package com.openexchange.webdav.xml.appointment.recurrence;

import java.util.Date;
import org.junit.Test;
import com.openexchange.groupware.container.Appointment;

public class Bug7915Test extends AbstractRecurrenceTest {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Bug7915Test.class);

    public Bug7915Test() {
        super();
        simpleDateFormatUTC.setTimeZone(timeZoneUTC);
    }

    @Test
    public void testBug7915() throws Exception {
        final Date startDate = simpleDateFormatUTC.parse("2007-06-01 00:00:00");
        final Date endDate = simpleDateFormatUTC.parse("2007-06-02 00:00:00");

        final Date until = simpleDateFormatUTC.parse("2007-06-15 00:00:00");

        final Appointment appointmentObj = new Appointment();
        appointmentObj.setTitle("testBug7915");
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
        exceptionAppointmentObject.setTitle("testBug7915 - change exception (2007-06-06)");
        exceptionAppointmentObject.setStartDate(exceptionStartDate);
        exceptionAppointmentObject.setEndDate(exceptionEndDate);
        exceptionAppointmentObject.setFullTime(true);
        exceptionAppointmentObject.setRecurrenceDatePosition(recurrenceDatePosition);
        exceptionAppointmentObject.setShownAs(Appointment.ABSENT);
        exceptionAppointmentObject.setParentFolderID(appointmentFolderId);
        exceptionAppointmentObject.setIgnoreConflicts(true);

        final int exceptionObjectId = updateAppointment(getWebConversation(), exceptionAppointmentObject, objectId, appointmentFolderId, getHostURI(), getLogin(), getPassword());

        appointmentObj.setObjectID(objectId);
        Appointment loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, getHostURI(), getLogin(), getPassword());
        compareObject(appointmentObj, loadAppointment);

        final Date modified = loadAppointment.getLastModified();

        exceptionAppointmentObject.setObjectID(exceptionObjectId);
        loadAppointment = loadAppointment(getWebConversation(), exceptionObjectId, appointmentFolderId, getHostURI(), getLogin(), getPassword());

        // Loaded exception MUST NOT contains any recurrence information except recurrence identifier and position.
        compareObject(exceptionAppointmentObject, loadAppointment);

        loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, decrementDate(modified), getHostURI(), getLogin(), getPassword());
        compareObject(appointmentObj, loadAppointment);

        loadAppointment = loadAppointment(getWebConversation(), exceptionObjectId, appointmentFolderId, decrementDate(modified), getHostURI(), getLogin(), getPassword());
        compareObject(exceptionAppointmentObject, loadAppointment);

        deleteAppointment(getWebConversation(), objectId, appointmentFolderId, getHostURI(), getLogin(), getPassword());
    }
}
