package com.openexchange.ajax.appointment;

import java.util.Calendar;
import java.util.TimeZone;

import com.openexchange.ajax.appointment.action.ConflictObject;
import com.openexchange.ajax.appointment.action.DeleteRequest;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.CommonInsertResponse;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.container.AppointmentObject;

/**
 * 
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 *
 */
public class Bug12842Test extends AbstractAJAXSession {
    
    public Bug12842Test(String name) {
        super(name);
    }
    
    /**
     * Tests if an appointment conflicts, if the new appointment is between the start and end date of an occurrence.
     * Occurrence:     [--------]
     * Appointment:      [----]
     * @throws Throwable
     */
    public void testConflictBetween() throws Throwable {
        rangeTest(8, 12, 9, 11, AppointmentObject.DAILY, true);
        rangeTest(8, 12, 9, 11, AppointmentObject.WEEKLY, true);
        rangeTest(8, 12, 9, 11, AppointmentObject.MONTHLY, true);
        rangeTest(8, 12, 9, 11, AppointmentObject.YEARLY, true);
    }
    
    /**
     * Tests, if an appointment conflicts, if the new appointment overlaps the start date of an occurrence, but not the end date.
     * Occurrence:     [--------]
     * Appointment:  [----]
     * @throws Throwable
     */
    public void testConflictOverlappingStartDate() throws Throwable {
        rangeTest(8, 12, 7, 9, AppointmentObject.DAILY, true);
        rangeTest(8, 12, 7, 9, AppointmentObject.WEEKLY, true);
        rangeTest(8, 12, 7, 9, AppointmentObject.MONTHLY, true);
        rangeTest(8, 12, 7, 9, AppointmentObject.YEARLY, true);
    }
    
    /**
     * Tests, if an appointment conflicts, if the new appointment overlaps the end date of an occurrence, but not the start date.
     * Occurrence:     [--------]
     * Appointment:          [----]
     * @throws Throwable
     */
    public void testConflictOverlappingEndDate() throws Throwable {
        rangeTest(8, 12, 11, 13, AppointmentObject.DAILY, true);
        rangeTest(8, 12, 11, 13, AppointmentObject.WEEKLY, true);
        rangeTest(8, 12, 11, 13, AppointmentObject.MONTHLY, true);
        rangeTest(8, 12, 11, 13, AppointmentObject.YEARLY, true);
    }
    
    /**
     * Tests, if an appointment conflicts, if the the new appointment overlaps the start and end date of an occurrence.
     * Occurrence:     [--------]
     * Appointment:  [------------]
     * @throws Throwable
     */
    public void testConflictOverlapping() throws Throwable {
        rangeTest(8, 12, 7, 13, AppointmentObject.DAILY, true);
        rangeTest(8, 12, 7, 13, AppointmentObject.WEEKLY, true);
        rangeTest(8, 12, 7, 13, AppointmentObject.MONTHLY, true);
        rangeTest(8, 12, 7, 13, AppointmentObject.YEARLY, true);
    }
    
    /**
     * Tests, if an appointment conflicts, if the the new appointment touches the start date of an occurrence.
     * Occurrence:     [--------]
     * Appointment: [--]
     * @throws Throwable
     */
    public void testBoundaryStart() throws Throwable {
        rangeTest(8, 12, 6, 8, AppointmentObject.DAILY, false);
        rangeTest(8, 12, 6, 8, AppointmentObject.WEEKLY, false);
        rangeTest(8, 12, 6, 8, AppointmentObject.MONTHLY, false);
        rangeTest(8, 12, 6, 8, AppointmentObject.YEARLY, false);
    }
    
    /**
     * Tests, if an appointment conflicts, if the the new appointment touches the end date of an occurrence.
     * Occurrence:     [--------]
     * Appointment:             [--]
     * @throws Throwable
     */
    public void testBoundaryEnd() throws Throwable {
        rangeTest(8, 12, 12, 14, AppointmentObject.DAILY, false);
        rangeTest(8, 12, 12, 14, AppointmentObject.WEEKLY, false);
        rangeTest(8, 12, 12, 14, AppointmentObject.MONTHLY, false);
        rangeTest(8, 12, 12, 14, AppointmentObject.YEARLY, false);
    }
    
    /**
     * Tests, if an appointment conflicts, if the the new appointment is before an occurrence.
     * Occurrence:      [--------]
     * Appointment:[--]
     * @throws Throwable
     */
    public void testBeforeStart() throws Throwable {
        rangeTest(8, 12, 4, 6, AppointmentObject.DAILY, false);
        rangeTest(8, 12, 4, 6, AppointmentObject.WEEKLY, false);
        rangeTest(8, 12, 4, 6, AppointmentObject.MONTHLY, false);
        rangeTest(8, 12, 4, 6, AppointmentObject.YEARLY, false);
    }
    
    /**
     * Tests, if an appointment conflicts, if the the new appointment is after an occurrence.
     * Occurrence:     [--------]
     * Appointment:               [--]
     * @throws Throwable
     */
    public void testAfterEnd() throws Throwable {
        rangeTest(8, 12, 14, 16, AppointmentObject.DAILY, false);
        rangeTest(8, 12, 14, 16, AppointmentObject.WEEKLY, false);
        rangeTest(8, 12, 14, 16, AppointmentObject.MONTHLY, false);
        rangeTest(8, 12, 14, 16, AppointmentObject.YEARLY, false);
    }
    
    
    /**
     * Each test-method does nearly the same, there is only a small variance in the timeframe of the conflicting appointment.
     * This Method does the main work.
     * @param start start hour of the sequence
     * @param end end hour of the sequence
     * @param conflictStart start hour of the conflicting appointment
     * @param conflictEnd end hour of the conflicting appointment
     * @param type recurrence type
     * @param shouldConflict
     * @throws Throwable
     */
    private void rangeTest(int start, int end, int conflictStart, int conflictEnd, int type, boolean shouldConflict) throws Throwable {
        AJAXClient client = null;
        AppointmentObject appointment = new AppointmentObject();
        AppointmentObject conflictAppointment = new AppointmentObject();
        
        try {
            client = getClient();
            int folderId = client.getValues().getPrivateAppointmentFolder();
            TimeZone tz = client.getValues().getTimeZone();
            
            //Sequence
            appointment = new AppointmentObject();
            appointment.setTitle("Bug12842Test");
            appointment.setParentFolderID(folderId);
            appointment.setIgnoreConflicts(true);
            Calendar calendar = TimeTools.createCalendar(tz);
            calendar.set(Calendar.HOUR_OF_DAY, start);
            appointment.setStartDate(calendar.getTime());
            calendar.set(Calendar.HOUR_OF_DAY, end);
            appointment.setEndDate(calendar.getTime());
            appointment.setRecurrenceType(type);
            appointment.setInterval(1);
            
            switch (type) {
            case AppointmentObject.YEARLY:
                appointment.setMonth(calendar.get(Calendar.MONTH));
            case AppointmentObject.MONTHLY:
                appointment.setDayInMonth(calendar.get(Calendar.DAY_OF_MONTH));
                break;
            case AppointmentObject.WEEKLY:
                appointment.setDays((int) Math.pow(2, (calendar.get(Calendar.DAY_OF_WEEK)-1))); //Transforming java.util.Calendar.DAY_OF_WEEK to com.openexchange.groupware.container.CalendarObject.days
            case AppointmentObject.DAILY:
                break;
            default:
                break;
            }
            
            InsertRequest request = new InsertRequest(appointment, tz);
            CommonInsertResponse response = client.execute(request);
            appointment.setObjectID(response.getId());
            appointment.setLastModified(response.getTimestamp());
            
            //Conflicting appointment
            conflictAppointment.setTitle("conflict");
            conflictAppointment.setParentFolderID(folderId);
            conflictAppointment.setIgnoreConflicts(false);
            calendar = TimeTools.createCalendar(tz);
            
            switch (type) {
            case AppointmentObject.YEARLY:
                calendar.add(Calendar.YEAR, 1);
                break;
            case AppointmentObject.MONTHLY:
                calendar.add(Calendar.MONTH, 1);
                break;
            case AppointmentObject.WEEKLY:
                calendar.add(Calendar.WEEK_OF_YEAR, 1);
                break;
            case AppointmentObject.DAILY:
                calendar.add(Calendar.DAY_OF_MONTH, 1);
                break;
            default:
                break;
            }
            
            calendar.set(Calendar.HOUR_OF_DAY, conflictStart);
            conflictAppointment.setStartDate(calendar.getTime());
            calendar.set(Calendar.HOUR_OF_DAY, conflictEnd);
            conflictAppointment.setEndDate(calendar.getTime());
            request = new InsertRequest(conflictAppointment, tz, false);
            response = client.execute(request);
            
            if (shouldConflict) {
                if (!response.hasConflicts()) {
                    conflictAppointment.setObjectID(response.getId());
                    conflictAppointment.setLastModified(response.getTimestamp());
                    fail("Conflict expected.");
                }
            } else {
                if (response.hasConflicts()) {
                    for (ConflictObject conflict : response.getConflicts()) {
                        if (conflict.getTitle().startsWith("Bug12842Test")) {
                            fail("No conflict expected.");
                        }
                    }
                }
                conflictAppointment.setObjectID(response.getId());
                conflictAppointment.setLastModified(response.getTimestamp());
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
