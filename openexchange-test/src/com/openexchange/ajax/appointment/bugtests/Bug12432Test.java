
package com.openexchange.ajax.appointment.bugtests;

import static org.junit.Assert.assertFalse;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import org.junit.Test;
import com.openexchange.ajax.appointment.action.AppointmentInsertResponse;
import com.openexchange.ajax.appointment.action.ConflictObject;
import com.openexchange.ajax.appointment.action.DeleteRequest;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.ajax.appointment.action.UpdateRequest;
import com.openexchange.ajax.appointment.action.UpdateResponse;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.container.Appointment;

public class Bug12432Test extends AbstractAJAXSession {

    public Bug12432Test() {
        super();
    }

    @Test
    public void testFirstReservedThenFree() throws Throwable {
        AJAXClient client = null;
        Appointment appointmentReserved = null;
        Appointment appointmentFree = null;

        try {
            client = getClient();
            int folderId = getClient().getValues().getPrivateAppointmentFolder();
            TimeZone tz = getClient().getValues().getTimeZone();

            appointmentReserved = createAppointment("Bug12432Test - reserved", Appointment.RESERVED, folderId, tz);
            // Prevent conflicting with other objects.
            appointmentReserved.setIgnoreConflicts(true);
            appointmentFree = createAppointment("Bug12432Test - free", Appointment.FREE, folderId, tz);
            // Must conflict.
            appointmentFree.setIgnoreConflicts(false);

            InsertRequest request = new InsertRequest(appointmentReserved, tz);
            AppointmentInsertResponse response = getClient().execute(request);
            appointmentReserved.setObjectID(response.getId());
            appointmentReserved.setLastModified(response.getTimestamp());

            request = new InsertRequest(appointmentFree, tz, false);
            response = getClient().execute(request);
            assertFalse(response.hasConflicts());
            appointmentFree.setObjectID(response.getId());
            appointmentFree.setLastModified(response.getTimestamp());
        } finally {
            deleteAppointment(appointmentReserved, client);
            deleteAppointment(appointmentFree, client);
        }
    }

    @Test
    public void testFirstFreeThenReserved() throws Throwable {
        AJAXClient client = null;
        Appointment appointmentReserved = null;
        Appointment appointmentFree = null;

        try {
            client = getClient();
            int folderId = getClient().getValues().getPrivateAppointmentFolder();
            TimeZone tz = getClient().getValues().getTimeZone();

            appointmentReserved = createAppointment("Bug12432Test - reserved", Appointment.RESERVED, folderId, tz);
            // Prevent conflicting with other objects.
            appointmentReserved.setIgnoreConflicts(true);
            appointmentFree = createAppointment("Bug12432Test - free", Appointment.FREE, folderId, tz);
            // Must conflict.
            appointmentFree.setIgnoreConflicts(false);

            InsertRequest request = new InsertRequest(appointmentFree, tz);
            AppointmentInsertResponse response = getClient().execute(request);
            appointmentFree.setObjectID(response.getId());
            appointmentFree.setLastModified(response.getTimestamp());

            request = new InsertRequest(appointmentReserved, tz, false);
            response = getClient().execute(request);
            assertFalse(response.hasConflicts());
            appointmentReserved.setObjectID(response.getId());
            appointmentReserved.setLastModified(response.getTimestamp());
        } finally {
            deleteAppointment(appointmentReserved, client);
            deleteAppointment(appointmentFree, client);
        }
    }

    @Test
    public void testChangeFree() throws Throwable {
        AJAXClient client = null;
        Appointment appointmentReserved = null;
        Appointment appointmentFree = null;

        try {
            client = getClient();
            int folderId = getClient().getValues().getPrivateAppointmentFolder();
            TimeZone tz = getClient().getValues().getTimeZone();

            appointmentReserved = createAppointment("Bug12432Test - reserved", Appointment.RESERVED, folderId, tz);
            // Prevent conflicting with other objects.
            appointmentReserved.setIgnoreConflicts(true);
            appointmentFree = createAppointment("Bug12432Test - free", Appointment.FREE, folderId, tz);
            // Prevent conflicting dring creation.
            appointmentFree.setIgnoreConflicts(true);

            InsertRequest request = new InsertRequest(appointmentReserved, tz);
            AppointmentInsertResponse response = getClient().execute(request);
            appointmentReserved.setObjectID(response.getId());
            appointmentReserved.setLastModified(response.getTimestamp());

            request = new InsertRequest(appointmentFree, tz, false);
            response = getClient().execute(request);
            appointmentFree.setObjectID(response.getId());
            appointmentFree.setLastModified(response.getTimestamp());

            Appointment updateAppointment = new Appointment();
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
            UpdateResponse updateResponse = getClient().execute(updateRequest);
            assertFalse("Update on free Appointment should not conflict.", updateResponse.hasConflicts());

            appointmentFree.setLastModified(updateResponse.getTimestamp());
        } finally {
            deleteAppointment(appointmentReserved, client);
            deleteAppointment(appointmentFree, client);
        }
    }

    @Test
    public void testChangeReserved() throws Throwable {
        AJAXClient client = null;
        Appointment appointmentReserved = null;
        Appointment appointmentFree = null;

        try {
            client = getClient();
            int folderId = getClient().getValues().getPrivateAppointmentFolder();
            TimeZone tz = getClient().getValues().getTimeZone();

            appointmentReserved = createAppointment("Bug12432Test - reserved", Appointment.RESERVED, folderId, tz);
            // Prevent conflicting with other objects.
            appointmentReserved.setIgnoreConflicts(true);
            appointmentFree = createAppointment("Bug12432Test - free", Appointment.FREE, folderId, tz);
            // Prevent conflicting dring creation.
            appointmentFree.setIgnoreConflicts(true);

            InsertRequest request = new InsertRequest(appointmentReserved, tz);
            AppointmentInsertResponse response = getClient().execute(request);
            appointmentReserved.setObjectID(response.getId());
            appointmentReserved.setLastModified(response.getTimestamp());

            request = new InsertRequest(appointmentFree, tz, false);
            response = getClient().execute(request);
            appointmentFree.setObjectID(response.getId());
            appointmentFree.setLastModified(response.getTimestamp());

            Appointment updateAppointment = new Appointment();
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
            UpdateResponse updateResponse = getClient().execute(updateRequest);
            if (updateResponse.hasConflicts()) {
                for (ConflictObject conflict : updateResponse.getConflicts()) {
                    assertFalse("Update on Appointment should not conflict with free Appointment.", conflict.getId() == appointmentFree.getObjectID());
                }
            }

            appointmentReserved.setLastModified(updateResponse.getTimestamp());
        } finally {
            deleteAppointment(appointmentReserved, client);
            deleteAppointment(appointmentFree, client);
        }
    }

    @Test
    public void testBugAsWritten() throws Throwable {
        AJAXClient client = null;
        Appointment appointmentA = null;
        Appointment appointmentB = null;

        try {
            client = getClient();
            int folderId = getClient().getValues().getPrivateAppointmentFolder();
            TimeZone tz = getClient().getValues().getTimeZone();

            //Step 1
            appointmentA = createAppointment("Just-for-Info", Appointment.FREE, folderId, tz);
            Calendar calendar = TimeTools.createCalendar(tz);
            calendar.set(Calendar.HOUR_OF_DAY, 10);
            appointmentA.setStartDate(calendar.getTime());
            calendar.set(Calendar.HOUR_OF_DAY, 18);
            appointmentA.setEndDate(calendar.getTime());
            // Prevent conflicting with other objects.
            appointmentA.setIgnoreConflicts(true);

            InsertRequest request = new InsertRequest(appointmentA, tz, false);
            AppointmentInsertResponse response = getClient().execute(request);
            appointmentA.setObjectID(response.getId());
            appointmentA.setLastModified(response.getTimestamp());

            //Step 2
            appointmentB = createAppointment("Conf-Call", Appointment.RESERVED, folderId, tz);
            calendar = TimeTools.createCalendar(tz);
            calendar.set(Calendar.HOUR_OF_DAY, 12);
            appointmentB.setStartDate(calendar.getTime());
            calendar.set(Calendar.HOUR_OF_DAY, 13);
            appointmentB.setEndDate(calendar.getTime());
            // Prevent conflicting dring creation.
            appointmentB.setIgnoreConflicts(true);

            request = new InsertRequest(appointmentB, tz);
            response = getClient().execute(request);
            appointmentB.setObjectID(response.getId());
            appointmentB.setLastModified(response.getTimestamp());

            //Step 3
            Appointment updateAppointmentA = new Appointment();
            updateAppointmentA.setParentFolderID(folderId);
            updateAppointmentA.setObjectID(appointmentA.getObjectID());
            updateAppointmentA.setLastModified(appointmentA.getLastModified());
            calendar = TimeTools.createCalendar(tz);
            calendar.set(Calendar.HOUR_OF_DAY, 16);
            updateAppointmentA.setEndDate(calendar.getTime());
            updateAppointmentA.setIgnoreConflicts(false);

            UpdateRequest updateRequest = new UpdateRequest(updateAppointmentA, tz);
            UpdateResponse updateResponse = getClient().execute(updateRequest);
            assertFalse("Update on free Appointment should not conflict.", updateResponse.hasConflicts());

            appointmentA.setLastModified(updateResponse.getTimestamp());
        } finally {
            deleteAppointment(appointmentB, client);
            deleteAppointment(appointmentA, client);
        }
    }

    private Appointment createAppointment(String title, int shownAs, int folderId, TimeZone tz) {
        Appointment appointment = new Appointment();
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

    private void deleteAppointment(Appointment appointment, AJAXClient client) throws Throwable {
        if (client != null && appointment.getObjectID() != 0 && appointment.getLastModified() != null) {
            DeleteRequest deleteRequest = new DeleteRequest(appointment.getObjectID(), getClient().getValues().getPrivateAppointmentFolder(), appointment.getLastModified());
            getClient().execute(deleteRequest);
        }
    }

}
