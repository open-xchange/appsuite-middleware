
package com.openexchange.ajax.appointment.bugtests;

import java.util.Calendar;
import java.util.TimeZone;
import org.junit.Test;
import com.openexchange.ajax.appointment.action.DeleteRequest;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.ajax.appointment.action.UpdateRequest;
import com.openexchange.ajax.appointment.action.UpdateResponse;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.CommonInsertResponse;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.container.Appointment;

public class Bug12610Test extends AbstractAJAXSession {

    public Bug12610Test() {
        super();
    }

    @Test
    public void testBugAsWritten() throws Throwable {
        AJAXClient client = null;
        Appointment appointment = new Appointment();

        try {
            client = getClient();
            int folderId = getClient().getValues().getPrivateAppointmentFolder();
            TimeZone tz = getClient().getValues().getTimeZone();

            appointment.setTitle("Bug12610Test");
            appointment.setParentFolderID(folderId);
            appointment.setIgnoreConflicts(true);
            final Calendar calendar = TimeTools.createCalendar(tz);
            calendar.set(Calendar.DAY_OF_MONTH, 15);
            calendar.set(Calendar.MONTH, Calendar.JUNE);
            calendar.set(Calendar.YEAR, 2009);
            calendar.set(Calendar.HOUR_OF_DAY, 8);
            appointment.setStartDate(calendar.getTime());
            calendar.set(Calendar.HOUR_OF_DAY, 9);
            appointment.setEndDate(calendar.getTime());
            appointment.setRecurrenceType(Appointment.YEARLY);
            appointment.setInterval(1);
            appointment.setDayInMonth(15);
            appointment.setMonth(Calendar.JUNE);
            final Calendar until = TimeTools.createCalendar(TimeZone.getTimeZone("UTC"));
            until.set(Calendar.DAY_OF_MONTH, 12);
            until.set(Calendar.MONTH, Calendar.DECEMBER);
            until.set(Calendar.YEAR, 2012);
            until.set(Calendar.HOUR_OF_DAY, 0);
            until.set(Calendar.MINUTE, 0);
            until.set(Calendar.SECOND, 0);
            until.set(Calendar.MILLISECOND, 0);
            appointment.setUntil(until.getTime());

            final InsertRequest request = new InsertRequest(appointment, tz);
            final CommonInsertResponse response = getClient().execute(request);
            appointment.setObjectID(response.getId());
            appointment.setLastModified(response.getTimestamp());

            until.set(Calendar.DAY_OF_MONTH, 18);
            until.set(Calendar.MONTH, Calendar.JULY);
            until.set(Calendar.YEAR, 2014);
            appointment.setUntil(until.getTime());

            final UpdateRequest updateRequest = new UpdateRequest(appointment, tz);
            final UpdateResponse updateResponse = getClient().execute(updateRequest);
            appointment.setLastModified(updateResponse.getTimestamp());
        } finally {
            if (client != null && appointment.getObjectID() != 0 && appointment.getLastModified() != null) {
                DeleteRequest deleteRequest = new DeleteRequest(appointment.getObjectID(), getClient().getValues().getPrivateAppointmentFolder(), appointment.getLastModified());
                getClient().execute(deleteRequest);
            }
        }
    }
}
