
package com.openexchange.ajax.appointment.bugtests;

import static com.openexchange.groupware.calendar.TimeTools.D;
import static org.junit.Assert.assertEquals;
import java.util.TimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
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

    public Bug14679Test() {
        super();
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();

        appointment = new Appointment();
        appointment.setTitle("Bug 14679 Test");
        appointment.setParentFolderID(getClient().getValues().getPrivateAppointmentFolder());
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

    @Test
    public void testBug() throws Exception {
        UpdateRequest updateRequest = new UpdateRequest(update, getClient().getValues().getTimeZone());
        UpdateResponse updateResponse = getClient().execute(updateRequest);
        appointment.setLastModified(updateResponse.getTimestamp());

        int[] columns = new int[] { Appointment.OBJECT_ID };

        AllRequest allRequest = new AllRequest(getClient().getValues().getPrivateAppointmentFolder(), columns, D("01.11.2009 00:00", TimeZone.getTimeZone("UTC")), D("01.12.2009 00:00", TimeZone.getTimeZone("UTC")), TimeZone.getTimeZone("UTC"), false);

        CommonAllResponse allResponse = getClient().execute(allRequest);
        Object[][] objects = allResponse.getArray();
        int count = 0;
        for (Object[] object : objects) {
            if ((Integer) object[0] == appointment.getObjectID()) {
                count++;
            }
        }

        assertEquals("Wrong amount of occurrences.", 5, count);
    }

    @After
    public void tearDown() throws Exception {
        try {
            DeleteRequest appointmentDeleteRequest = new DeleteRequest(appointment);
            getClient().execute(appointmentDeleteRequest);

        } finally {
            super.tearDown();
        }
    }

}
