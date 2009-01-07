package com.openexchange.ajax.appointment;

import java.util.Calendar;
import java.util.TimeZone;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.openexchange.ajax.appointment.action.DeleteRequest;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.CommonInsertResponse;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.container.AppointmentObject;

public class Bug12842Test extends AbstractAJAXSession {
    
    public Bug12842Test(String name) {
        super(name);
    }
    
    /**
     * Tests if an appointment conflicts, if the new appointment is between the start and end date of an occurrence.
     * @throws Throwable
     */
    public void testConflictBetween() throws Throwable {
        rangeTest(9, 11);
    }
    
    /**
     * Tests, if an appointment conflicts, if the new appointment overlaps the start date of an occurrence, but not the end date.
     * @throws Throwable
     */
    public void testConflictOverlappingStartDate() throws Throwable {
        rangeTest(7, 9);
    }
    
    /**
     * Tests, if an appointment conflicts, if the new appointment overlaps the end date of an occurrence, but not the start date.
     * @throws Throwable
     */
    public void testConflictOverlappingEndDate() throws Throwable {
        rangeTest(11, 13);
    }
    
    /**
     * Tests, if an appointment conflicts, if the the new appointment overlaps the start and end date of an occurrence.
     * @throws Throwable
     */
    public void testConflictOverlapping() throws Throwable {
        rangeTest(7, 13);
    }
    
    /**
     * Each test-method does nearly the same, there is only a small variance in the timeframe of the conflicting appointment.
     * This Method does the main work.
     * @param start
     * @param end
     * @throws Throwable
     */
    private void rangeTest(int start, int end) throws Throwable {
        AJAXClient client = null;
        AppointmentObject appointment = new AppointmentObject();
        AppointmentObject conflictAppointment = new AppointmentObject();
        
        try {
            client = getClient();
            int folderId = client.getValues().getPrivateAppointmentFolder();
            TimeZone tz = client.getValues().getTimeZone();
            
            appointment = new AppointmentObject();
            appointment.setTitle("Bug12842Test");
            appointment.setParentFolderID(folderId);
            appointment.setIgnoreConflicts(true);
            Calendar calendar = TimeTools.createCalendar(tz);
            calendar.set(Calendar.HOUR_OF_DAY, 8);
            appointment.setStartDate(calendar.getTime());
            calendar.set(Calendar.HOUR_OF_DAY, 12);
            appointment.setEndDate(calendar.getTime());
            appointment.setRecurrenceType(AppointmentObject.DAILY);
            appointment.setInterval(1);
            InsertRequest request = new InsertRequest(appointment, tz);
            CommonInsertResponse response = client.execute(request);
            appointment.setObjectID(response.getId());
            appointment.setLastModified(response.getTimestamp());
            
            conflictAppointment.setTitle("conflict");
            conflictAppointment.setParentFolderID(folderId);
            conflictAppointment.setIgnoreConflicts(false);
            calendar = TimeTools.createCalendar(tz);
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            calendar.set(Calendar.HOUR_OF_DAY, start);
            conflictAppointment.setStartDate(calendar.getTime());
            calendar.set(Calendar.HOUR_OF_DAY, end);
            conflictAppointment.setEndDate(calendar.getTime());
            request = new InsertRequest(conflictAppointment, tz, false);
            response = client.execute(request);
            
            if (!response.hasConflicts()) {
                conflictAppointment.setObjectID(response.getId());
                conflictAppointment.setLastModified(response.getTimestamp());
                fail("Expected conflict.");
            }
        } finally {
            if (client != null && conflictAppointment.getObjectID() != 0 && conflictAppointment.getLastModified() != null) {
                DeleteRequest deleteRequest = new DeleteRequest(conflictAppointment.getObjectID(), client.getValues().getPrivateAppointmentFolder(), conflictAppointment.getLastModified());
                client.execute(deleteRequest);
            }
            if (client != null && appointment.getObjectID() != 0 && appointment.getLastModified() != null) {
                DeleteRequest deleteRequest = new DeleteRequest(appointment.getObjectID(), client.getValues().getPrivateAppointmentFolder(), appointment.getLastModified());
                client.execute(deleteRequest);
            }
        }
    }

}
