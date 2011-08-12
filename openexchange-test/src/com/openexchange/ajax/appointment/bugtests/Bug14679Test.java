
package com.openexchange.ajax.appointment.bugtests;

import static com.openexchange.groupware.calendar.TimeTools.D;
import java.util.TimeZone;
import com.openexchange.ajax.appointment.action.AllRequest;
import com.openexchange.ajax.appointment.action.AppointmentInsertResponse;
import com.openexchange.ajax.appointment.action.DeleteRequest;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.ajax.appointment.action.UpdateRequest;
import com.openexchange.ajax.appointment.action.UpdateResponse;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.CommonAllResponse;
import com.openexchange.groupware.container.Appointment;

/*
 *
 */
public class Bug14679Test extends AbstractAJAXSession {

    private Appointment appointment;

    private Appointment update;

    public Bug14679Test(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        appointment = new Appointment();
        appointment.setTitle("Bug 14679 Test");
        appointment.setParentFolderID(client.getValues().getPrivateAppointmentFolder());
        appointment.setStartDate(D("19.10.2009 12:30", TimeZone.getTimeZone("UTC")));
        appointment.setEndDate(D("19.10.2009 13:30", TimeZone.getTimeZone("UTC")));
        appointment.setRecurrenceType(Appointment.WEEKLY);
        appointment.setInterval(1);
        appointment.setDays(Appointment.MONDAY + Appointment.WEDNESDAY);
        appointment.setUntil(D("30.12.2009 00:00", TimeZone.getTimeZone("UTC")));
        appointment.setIgnoreConflicts(true);

        InsertRequest appointmentInsertRequest = new InsertRequest(appointment, getClient().getValues().getTimeZone());
        AppointmentInsertResponse appointmentInsertResponse = getClient().execute(appointmentInsertRequest);
        appointmentInsertResponse.fillAppointment(appointment);

        update = new Appointment();
        update.setObjectID(appointment.getObjectID());
        update.setParentFolderID(appointment.getParentFolderID());
        update.setLastModified(appointment.getLastModified());
        update.setRecurrenceType(Appointment.WEEKLY);
        update.setInterval(2);
        update.setDays(Appointment.MONDAY + Appointment.WEDNESDAY);
        update.setUntil(D("30.12.2009 00:00", TimeZone.getTimeZone("UTC")));
        update.setIgnoreConflicts(true);
    }

    public void testBug() throws Exception {
        UpdateRequest updateRequest = new UpdateRequest(update, client.getValues().getTimeZone());
        UpdateResponse updateResponse = client.execute(updateRequest);
        appointment.setLastModified(updateResponse.getTimestamp());

        int[] columns = new int[] { Appointment.OBJECT_ID };

        AllRequest allRequest = new AllRequest(client.getValues().getPrivateAppointmentFolder(),
            columns,
            D("01.11.2009 00:00", TimeZone.getTimeZone("UTC")),
            D("01.12.2009 00:00", TimeZone.getTimeZone("UTC")),
            TimeZone.getTimeZone("UTC"),
            false);

        CommonAllResponse allResponse = client.execute(allRequest);
        Object[][] objects = allResponse.getArray();
        int count = 0;
        for (Object[] object : objects) {
            if ((Integer)object[0] == appointment.getObjectID()) {
                count++;
            }
        }

        assertEquals("Wrong amount of occurrences.", 5, count);
    }

    @Override
    protected void tearDown() throws Exception {
        DeleteRequest appointmentDeleteRequest = new DeleteRequest(appointment);
        getClient().execute(appointmentDeleteRequest);

        super.tearDown();
    }

}
