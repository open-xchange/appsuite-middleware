package com.openexchange.webdav.xml.appointment.recurrence;

import java.util.Date;

import com.openexchange.groupware.container.AppointmentObject;

public class ChangeExceptionTest extends AbstractRecurrenceTest {

    public ChangeExceptionTest(String name) {
        super(name);
    }
    
    public void testExceptionInTime() throws Throwable {
        int objectId = 0;
        try {
            final Date startDate = simpleDateFormatUTC.parse("2009-01-01 08:00:00");
            final Date endDate = simpleDateFormatUTC.parse("2009-01-01 10:00:00");
            
            final AppointmentObject appointmentObj = new AppointmentObject();
            appointmentObj.setTitle("testExceptionInTime - master");
            appointmentObj.setStartDate(startDate);
            appointmentObj.setEndDate(endDate);
            appointmentObj.setShownAs(AppointmentObject.ABSENT);
            appointmentObj.setParentFolderID(appointmentFolderId);
            appointmentObj.setRecurrenceType(AppointmentObject.DAILY);
            appointmentObj.setInterval(1);
            appointmentObj.setIgnoreConflicts(true);
            objectId = insertAppointment(getWebConversation(), appointmentObj, PROTOCOL + getHostName(), getLogin(), getPassword());
            
            final Date startDateException = simpleDateFormatUTC.parse("2009-01-03 10:00:00");
            final Date endDateException = simpleDateFormatUTC.parse("2009-01-03 12:00:00");
            
            final AppointmentObject exception = new AppointmentObject();
            exception.setTitle("testExceptionInTime - exception");
            exception.setStartDate(startDateException);
            exception.setEndDate(endDateException);
            exception.setIgnoreConflicts(true);
            final int exceptionId = updateAppointment(getWebConversation(), exception, objectId, appointmentFolderId, PROTOCOL + getHostName(), getLogin(), getPassword());
        } finally {
            if (objectId != 0) {
                deleteAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getLogin(), getPassword());
            }
        }
    }

}
