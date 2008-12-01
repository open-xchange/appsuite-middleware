package com.openexchange.ajax.appointment;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import com.openexchange.ajax.appointment.action.AppointmentInsertResponse;
import com.openexchange.ajax.appointment.action.ConflictObject;
import com.openexchange.ajax.appointment.action.DeleteRequest;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.ajax.appointment.action.UpdateRequest;
import com.openexchange.ajax.appointment.action.UpdateResponse;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.container.AppointmentObject;

public class Bug12432Test extends AbstractAJAXSession {

    public Bug12432Test(String name) {
        super(name);
    }
    
    public void testFirstReservedThenFree() throws Throwable {
        AJAXClient client = null;
        AppointmentObject appointmentReserved = null;
        AppointmentObject appointmentFree = null;
        
        try {
            client = getClient();
            int folderId = client.getValues().getPrivateAppointmentFolder();
            TimeZone tz = client.getValues().getTimeZone();
            
            appointmentReserved = createAppointment("Bug12432Test - reserved", AppointmentObject.RESERVED, folderId, tz);
            // Prevent conflicting with other objects.
            appointmentReserved.setIgnoreConflicts(true);
            appointmentFree = createAppointment("Bug12432Test - free", AppointmentObject.FREE, folderId, tz);
            // Must conflict.
            appointmentFree.setIgnoreConflicts(false);
            
            InsertRequest request = new InsertRequest(appointmentReserved, tz);
            AppointmentInsertResponse response = client.execute(request);
            appointmentReserved.setObjectID(response.getId());
            appointmentReserved.setLastModified(response.getTimestamp());
            
            request = new InsertRequest(appointmentFree, tz, false);
            response = client.execute(request);
            assertFalse(response.hasConflicts());
            appointmentFree.setObjectID(response.getId());
            appointmentFree.setLastModified(response.getTimestamp());
        } finally {
            deleteAppointment(appointmentReserved, client);
            deleteAppointment(appointmentFree, client);
        }
    }
    
    public void testFirstFreeThenReserved() throws Throwable {
        AJAXClient client = null;
        AppointmentObject appointmentReserved = null;
        AppointmentObject appointmentFree = null;
        
        try {
            client = getClient();
            int folderId = client.getValues().getPrivateAppointmentFolder();
            TimeZone tz = client.getValues().getTimeZone();
            
            appointmentReserved = createAppointment("Bug12432Test - reserved", AppointmentObject.RESERVED, folderId, tz);
            // Prevent conflicting with other objects.
            appointmentReserved.setIgnoreConflicts(true);
            appointmentFree = createAppointment("Bug12432Test - free", AppointmentObject.FREE, folderId, tz);
            // Must conflict.
            appointmentFree.setIgnoreConflicts(false);
            
            InsertRequest request = new InsertRequest(appointmentFree, tz);
            AppointmentInsertResponse response = client.execute(request);
            appointmentFree.setObjectID(response.getId());
            appointmentFree.setLastModified(response.getTimestamp());
            
            request = new InsertRequest(appointmentReserved, tz, false);
            response = client.execute(request);
            assertFalse(response.hasConflicts());
            appointmentReserved.setObjectID(response.getId());
            appointmentReserved.setLastModified(response.getTimestamp());
        } finally {
            deleteAppointment(appointmentReserved, client);
            deleteAppointment(appointmentFree, client);
        }
    }
    
    public void testChangeFree() throws Throwable {
        AJAXClient client = null;
        AppointmentObject appointmentReserved = null;
        AppointmentObject appointmentFree = null;
        
        try {
            client = getClient();
            int folderId = client.getValues().getPrivateAppointmentFolder();
            TimeZone tz = client.getValues().getTimeZone();
            
            appointmentReserved = createAppointment("Bug12432Test - reserved", AppointmentObject.RESERVED, folderId, tz);
            // Prevent conflicting with other objects.
            appointmentReserved.setIgnoreConflicts(true);
            appointmentFree = createAppointment("Bug12432Test - free", AppointmentObject.FREE, folderId, tz);
            // Prevent conflicting dring creation.
            appointmentFree.setIgnoreConflicts(true);
            
            InsertRequest request = new InsertRequest(appointmentReserved, tz);
            AppointmentInsertResponse response = client.execute(request);
            appointmentReserved.setObjectID(response.getId());
            appointmentReserved.setLastModified(response.getTimestamp());
            
            request = new InsertRequest(appointmentFree, tz, false);
            response = client.execute(request);
            appointmentFree.setObjectID(response.getId());
            appointmentFree.setLastModified(response.getTimestamp());
            
            AppointmentObject updateAppointment = new AppointmentObject();
            updateAppointment.setParentFolderID(folderId);
            updateAppointment.setObjectID(appointmentFree.getObjectID());
            updateAppointment.setLastModified(appointmentFree.getLastModified());
            Calendar calendar = new GregorianCalendar(tz);
            calendar.setTime(appointmentFree.getStartDate());
            calendar.add(Calendar.HOUR_OF_DAY, -1);
            updateAppointment.setStartDate(calendar.getTime());
            calendar = new GregorianCalendar(tz);
            calendar.setTime(appointmentFree.getEndDate());
            calendar.add(Calendar.HOUR_OF_DAY, 1);
            updateAppointment.setEndDate(calendar.getTime());
            updateAppointment.setIgnoreConflicts(false);
            
            UpdateRequest updateRequest = new UpdateRequest(updateAppointment, tz);
            UpdateResponse updateResponse = client.execute(updateRequest);
            assertFalse("Update on free Appointment should not conflict.", updateResponse.hasConflicts());
            
            appointmentFree.setLastModified(updateResponse.getTimestamp());
        } finally {
            deleteAppointment(appointmentReserved, client);
            deleteAppointment(appointmentFree, client);
        }
    }
    
    public void testChangeReserved() throws Throwable {
        AJAXClient client = null;
        AppointmentObject appointmentReserved = null;
        AppointmentObject appointmentFree = null;
        
        try {
            client = getClient();
            int folderId = client.getValues().getPrivateAppointmentFolder();
            TimeZone tz = client.getValues().getTimeZone();
            
            appointmentReserved = createAppointment("Bug12432Test - reserved", AppointmentObject.RESERVED, folderId, tz);
            // Prevent conflicting with other objects.
            appointmentReserved.setIgnoreConflicts(true);
            appointmentFree = createAppointment("Bug12432Test - free", AppointmentObject.FREE, folderId, tz);
            // Prevent conflicting dring creation.
            appointmentFree.setIgnoreConflicts(true);
            
            InsertRequest request = new InsertRequest(appointmentReserved, tz);
            AppointmentInsertResponse response = client.execute(request);
            appointmentReserved.setObjectID(response.getId());
            appointmentReserved.setLastModified(response.getTimestamp());
            
            request = new InsertRequest(appointmentFree, tz, false);
            response = client.execute(request);
            appointmentFree.setObjectID(response.getId());
            appointmentFree.setLastModified(response.getTimestamp());
            
            AppointmentObject updateAppointment = new AppointmentObject();
            updateAppointment.setParentFolderID(folderId);
            updateAppointment.setObjectID(appointmentReserved.getObjectID());
            updateAppointment.setLastModified(appointmentReserved.getLastModified());
            Calendar calendar = new GregorianCalendar(tz);
            calendar.setTime(appointmentReserved.getStartDate());
            calendar.add(Calendar.HOUR_OF_DAY, -1);
            updateAppointment.setStartDate(calendar.getTime());
            calendar = new GregorianCalendar(tz);
            calendar.setTime(appointmentReserved.getEndDate());
            calendar.add(Calendar.HOUR_OF_DAY, 1);
            updateAppointment.setEndDate(calendar.getTime());
            updateAppointment.setIgnoreConflicts(false);
            
            UpdateRequest updateRequest = new UpdateRequest(updateAppointment, tz);
            UpdateResponse updateResponse = client.execute(updateRequest);
            if (updateResponse.hasConflicts()){
                for(ConflictObject conflict: updateResponse.getConflicts()){
                    assertFalse("Update on Appointment should not conflict with free Appointment.", conflict.getId() == appointmentFree.getObjectID());
                }
            }
            
            appointmentReserved.setLastModified(updateResponse.getTimestamp());
        } finally {
            deleteAppointment(appointmentReserved, client);
            deleteAppointment(appointmentFree, client);
        }
    }
    
    public void testBugAsWritten() throws Throwable {
        AJAXClient client = null;
        AppointmentObject appointmentA = null;
        AppointmentObject appointmentB = null;
        
        try {
            client = getClient();
            int folderId = client.getValues().getPrivateAppointmentFolder();
            TimeZone tz = client.getValues().getTimeZone();

            //Step 1
            appointmentA = createAppointment("Just-for-Info", AppointmentObject.FREE, folderId, tz);
            Calendar calendar = TimeTools.createCalendar(tz);
            calendar.set(Calendar.HOUR_OF_DAY, 10);
            appointmentA.setStartDate(calendar.getTime());
            calendar.set(Calendar.HOUR_OF_DAY, 18);
            appointmentA.setEndDate(calendar.getTime());
            // Prevent conflicting with other objects.
            appointmentA.setIgnoreConflicts(true);
            
            InsertRequest request = new InsertRequest(appointmentA, tz, false);
            AppointmentInsertResponse response = client.execute(request);
            appointmentA.setObjectID(response.getId());
            appointmentA.setLastModified(response.getTimestamp());
            
            //Step 2
            appointmentB = createAppointment("Conf-Call", AppointmentObject.RESERVED, folderId, tz);
            calendar = TimeTools.createCalendar(tz);
            calendar.set(Calendar.HOUR_OF_DAY, 12);
            appointmentB.setStartDate(calendar.getTime());
            calendar.set(Calendar.HOUR_OF_DAY, 13);
            appointmentB.setEndDate(calendar.getTime());
            // Prevent conflicting dring creation.
            appointmentB.setIgnoreConflicts(true);
            
            request = new InsertRequest(appointmentB, tz);
            response = client.execute(request);
            appointmentB.setObjectID(response.getId());
            appointmentB.setLastModified(response.getTimestamp());
            
            //Step 3
            AppointmentObject updateAppointmentA = new AppointmentObject();
            updateAppointmentA.setParentFolderID(folderId);
            updateAppointmentA.setObjectID(appointmentA.getObjectID());
            updateAppointmentA.setLastModified(appointmentA.getLastModified());
            calendar = TimeTools.createCalendar(tz);
            calendar.set(Calendar.HOUR_OF_DAY, 16);
            updateAppointmentA.setEndDate(calendar.getTime());
            updateAppointmentA.setIgnoreConflicts(false);
            
            UpdateRequest updateRequest = new UpdateRequest(updateAppointmentA, tz);
            UpdateResponse updateResponse = client.execute(updateRequest);
            assertFalse("Update on free Appointment should not conflict.", updateResponse.hasConflicts());
            
            appointmentA.setLastModified(updateResponse.getTimestamp());
        } finally {
            deleteAppointment(appointmentB, client);
            deleteAppointment(appointmentA, client);
        }
    }
    
    private AppointmentObject createAppointment(String title, int shownAs, int folderId, TimeZone tz) {
        AppointmentObject appointment = new AppointmentObject();
        appointment.setTitle(title);
        appointment.setParentFolderID(folderId);
        appointment.setShownAs(shownAs);
        final Calendar calendar = TimeTools.createCalendar(tz);
        calendar.set(Calendar.HOUR_OF_DAY, 8);
        appointment.setStartDate(calendar.getTime());
        calendar.set(Calendar.HOUR_OF_DAY, 9);
        appointment.setEndDate(calendar.getTime());
        
        return appointment;
    }
    
    private void deleteAppointment(AppointmentObject appointment, AJAXClient client) throws Throwable{
        if (client != null && appointment.getObjectID() != 0 && appointment.getLastModified() != null) {
            DeleteRequest deleteRequest = new DeleteRequest(appointment.getObjectID(), client.getValues().getPrivateAppointmentFolder(), appointment.getLastModified());
            client.execute(deleteRequest);
        }
    }

}
