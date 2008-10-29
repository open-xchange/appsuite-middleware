package com.openexchange.ajax.appointment;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;

import com.openexchange.ajax.AppointmentTest;
import com.openexchange.ajax.appointment.action.AllRequest;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXSession;
import com.openexchange.ajax.framework.CommonAllResponse;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.groupware.container.AppointmentObject;

public class AllTest extends AppointmentTest {

	private static final Log LOG = LogFactory.getLog(AllTest.class);
	
	public AllTest(final String name) {
		super(name);
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}
	
	public void testShowAppointmentsBetween() throws Exception {
		final Date start = new Date(System.currentTimeMillis()-(dayInMillis*7));
		final Date end = new Date(System.currentTimeMillis()+(dayInMillis*7));
		
		final int cols[] = new int[]{ AppointmentObject.OBJECT_ID };
		
		final AppointmentObject[] appointmentArray = listAppointment(getWebConversation(), appointmentFolderId, cols, start, end, timeZone, false, PROTOCOL + getHostName(), getSessionId());
	}
	
	public void testShowAllAppointmentWhereIAmParticipant() throws Exception {
		final Date start = new Date(System.currentTimeMillis()-(dayInMillis*7));
		final Date end = new Date(System.currentTimeMillis()+(dayInMillis*7));
		
		final int cols[] = new int[]{ AppointmentObject.OBJECT_ID };
		
		final AppointmentObject[] appointmentArray = listAppointment(getWebConversation(), appointmentFolderId, cols, start, end, timeZone, true, PROTOCOL + getHostName(), getSessionId());
	}
	
	public void testShowFullTimeAppointments() throws Exception {
		final int cols[] = new int[]{ AppointmentObject.OBJECT_ID };
		
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		calendar.setTimeInMillis(startTime);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		
		final Date startDate = calendar.getTime();
		
		calendar.add(Calendar.DAY_OF_MONTH, 1);
		
		final Date endDate = calendar.getTime();
		
		final AppointmentObject appointmentObj = createAppointmentObject("testShowFullTimeAppointments");
		appointmentObj.setStartDate(startDate);
		appointmentObj.setEndDate(endDate);
		appointmentObj.setFullTime(true);
		appointmentObj.setIgnoreConflicts(true);
		final int objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, getHostName(), getSessionId());
		
		calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		
		Date start = calendar.getTime();
		Date end = new Date(start.getTime()+dayInMillis);
		
		AppointmentObject[] appointmentArray = listAppointment(getWebConversation(), appointmentFolderId, cols, start, end, timeZone, false, getHostName(), getSessionId());
		boolean found = false;
		for (int a = 0; a < appointmentArray.length; a++) {
			if (appointmentArray[a].getObjectID() == objectId) {
				found = true;
			}
		}
		assertTrue("appointment not found in day view", found);
		
        // one day less
		start = new Date(calendar.getTimeInMillis()-dayInMillis);
		end = new Date(start.getTime()+dayInMillis);
		appointmentArray = listAppointment(getWebConversation(), appointmentFolderId, cols, start, end, timeZone, false, getHostName(), getSessionId());
		found = false;
		for (int a = 0; a < appointmentArray.length; a++) {
			if (appointmentArray[a].getObjectID() == objectId) {
				found = true;
			}
		}
		assertFalse("appointment found one day before start date in day view", found);
		
		// one day more
		start = new Date(calendar.getTimeInMillis()+dayInMillis);
		end = new Date(start.getTime()+dayInMillis);
		appointmentArray = listAppointment(getWebConversation(), appointmentFolderId, cols, start, end, timeZone, false, getHostName(), getSessionId());
		found = false;
		for (int a = 0; a < appointmentArray.length; a++) {
			if (appointmentArray[a].getObjectID() == objectId) {
				found = true;
			}
		}
		assertFalse("appointment found one day after start date in day view", found);

		deleteAppointment(getWebConversation(), objectId, appointmentFolderId, getHostName(), getSessionId());
	}

    // Bug 12171
    public void testShowOcurrences() throws Exception {
        final int cols[] = new int[]{ AppointmentObject.OBJECT_ID, AppointmentObject.RECURRENCE_COUNT};

        final AppointmentObject appointmentObj = createAppointmentObject("testShowOcurrences");
        appointmentObj.setStartDate(new Date());
        appointmentObj.setEndDate(new Date(System.currentTimeMillis() + 60*60*1000));
        appointmentObj.setRecurrenceType(AppointmentObject.DAILY);
        appointmentObj.setInterval(1);
        appointmentObj.setOccurrence(3);
        appointmentObj.setIgnoreConflicts(true);
        final int objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, getHostName(), getSessionId());

        final AppointmentObject[] appointmentArray = listAppointment(getWebConversation(), appointmentFolderId, cols, new Date(0), new Date(Long.MAX_VALUE), timeZone, false, getHostName(), getSessionId());

        for(final AppointmentObject loaded : appointmentArray) {
            if(loaded.getObjectID() == objectId) {
                assertEquals(appointmentObj.getOccurrence(), loaded.getOccurrence());
            }
        }
    }

    // Node 2652
    public void testLastModifiedUTC() throws Exception {
        final AJAXClient client = new AJAXClient(new AJAXSession(getWebConversation(), getSessionId()));
        final int cols[] = new int[]{ AppointmentObject.OBJECT_ID, AppointmentObject.FOLDER_ID, AppointmentObject.LAST_MODIFIED_UTC};

        final AppointmentObject appointmentObj = createAppointmentObject("testShowLastModifiedUTC");
        appointmentObj.setStartDate(new Date());
        appointmentObj.setEndDate(new Date(System.currentTimeMillis() + 60*60*1000));
        appointmentObj.setIgnoreConflicts(true);
        final int objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, getHostName(), getSessionId());
        try {
            final AllRequest req = new AllRequest(appointmentFolderId, cols, new Date(0), new Date(Long.MAX_VALUE));

            final CommonAllResponse response = Executor.execute(client, req);
            final JSONArray arr = (JSONArray) response.getResponse().getData();

            assertNotNull(arr);
            final int size = arr.length();
            assertTrue(size > 0);
            for(int i = 0; i < size; i++ ){
                final JSONArray objectData = arr.optJSONArray(i);
                assertNotNull(objectData);
                assertNotNull(objectData.opt(2));
            }
        } finally {
            deleteAppointment(getWebConversation(), objectId, appointmentFolderId, getHostName(), getSessionId());    
        }
    }
}
