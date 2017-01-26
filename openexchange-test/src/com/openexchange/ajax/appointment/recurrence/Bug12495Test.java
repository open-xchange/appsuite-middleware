
package com.openexchange.ajax.appointment.recurrence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import org.json.JSONException;
import org.junit.Test;
import org.xml.sax.SAXException;
import com.openexchange.ajax.appointment.action.AllRequest;
import com.openexchange.ajax.appointment.action.AppointmentInsertResponse;
import com.openexchange.ajax.appointment.action.DeleteRequest;
import com.openexchange.ajax.appointment.action.GetRequest;
import com.openexchange.ajax.appointment.action.GetResponse;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.ajax.appointment.action.ListRequest;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.CommonAllResponse;
import com.openexchange.ajax.framework.CommonListResponse;
import com.openexchange.ajax.framework.ListIDs;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.UserParticipant;

/**
 * User 1 creates an appointment series, user 2 deletes one exception, check whether
 * recurrence position is present in change exception.
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class Bug12495Test extends AbstractAJAXSession {

    private TimeZone myLocalTimeZone = null;
    private int privateFolderOfUser1;
    private int privateFolderOfUser2;

    public Bug12495Test() {
        super();
    }

    public Appointment createAppointmentInOneHour(String title) {
        Appointment appointment = new Appointment();
        appointment.setParentFolderID(privateFolderOfUser1);
        Calendar calendar = TimeTools.createCalendar(myLocalTimeZone);
        calendar.setTime(new Date());
        appointment.setTitle(title);
        calendar.add(Calendar.HOUR, 1);
        appointment.setStartDate(calendar.getTime());
        calendar.add(Calendar.HOUR, 1);
        appointment.setEndDate(calendar.getTime());
        appointment.setIgnoreConflicts(true);
        return appointment;
    }

    public Appointment makeAppointmentRecurrDaily(Appointment appointment, int numberOfTimes) {
        appointment.setRecurrenceType(Appointment.DAILY);
        appointment.setInterval(1);
        appointment.setOccurrence(numberOfTimes);
        appointment.setDays(127);
        return appointment;
    }

    /**
     * Assuming that recurrence_date_position is included in an AllRequest,
     * this test checks whether it is included in a GetRequest and a ListRequest.
     *
     */
    @Test
    public void testShouldNotLoseRecurrenceDatePositionWhenOtherUserDeletesOneOccurrence() throws OXException, IOException, SAXException, JSONException, OXException, OXException {
        //setup
        AJAXClient client1 = getClient();
        AJAXClient client2 = new AJAXClient(testContext.acquireUser());
        myLocalTimeZone = client1.getValues().getTimeZone();
        privateFolderOfUser1 = client1.getValues().getPrivateAppointmentFolder();
        privateFolderOfUser2 = client2.getValues().getPrivateAppointmentFolder();

        //appointment
        Appointment series = createAppointmentInOneHour("Bug 12495");
        makeAppointmentRecurrDaily(series, 3);
        series.addParticipant(new UserParticipant(client2.getValues().getUserId()));

        //insert series
        InsertRequest insertRequest = new InsertRequest(series, myLocalTimeZone);
        AppointmentInsertResponse insertResponse = client1.execute(insertRequest);
        try {
            insertResponse.fillAppointment(series);
            int seriesId = series.getObjectID();
            //delete with user2
            int occurenceToBeChanged = 2;

            DeleteRequest deleteRequest = new DeleteRequest(seriesId, privateFolderOfUser2, occurenceToBeChanged, series.getLastModified());
            client2.execute(deleteRequest);

            //find exception id via all request
            int columns[] = new int[] { Appointment.OBJECT_ID, Appointment.RECURRENCE_DATE_POSITION };
            AllRequest allRequest = new AllRequest(privateFolderOfUser1, columns, series.getStartDate(), new Date(Long.MAX_VALUE), myLocalTimeZone);
            CommonAllResponse allResponse = client1.execute(allRequest);
            int exceptionId = -1;
            long recurrenceDatePosition = -1;
            //search all request
            Object[][] responseColumns = allResponse.getArray();
            for (Object[] obj : responseColumns) {
                if (obj[1] != null) {
                    exceptionId = ((Integer) obj[0]).intValue();
                    recurrenceDatePosition = ((Long) obj[1]).longValue();
                }
            }
            assertTrue("Should be able to find both a recurrence_date_position and an exception_id", (exceptionId != -1) && (recurrenceDatePosition != -1));

            //list exception with user1 by using exception_id, then compare to recurrence_date_position
            ListIDs ids = ListIDs.l(new int[] { privateFolderOfUser1, exceptionId });
            ListRequest listRequest = new ListRequest(ids, columns);
            CommonListResponse listResponse = client1.execute(listRequest);
            long recurrence_date_position_in_result = ((Long) listResponse.getArray()[0][1]).longValue();
            assertEquals("Must contain matching recurrence_date_position in list request", recurrenceDatePosition, recurrence_date_position_in_result);

            //get exception with user1
            GetRequest getRequest = new GetRequest(privateFolderOfUser1, exceptionId);
            GetResponse getResponse = client1.execute(getRequest);
            Appointment exception = getResponse.getAppointment(myLocalTimeZone);
            assertTrue("Must contain recurrence_date_position in get request", exception.containsRecurrenceDatePosition());

        } finally {
            GetRequest getRequest = new GetRequest(series);
            series = client1.execute(getRequest).getAppointment(myLocalTimeZone);
            DeleteRequest deleteRequest = new DeleteRequest(series);
            client1.execute(deleteRequest);
        }
    }
}
