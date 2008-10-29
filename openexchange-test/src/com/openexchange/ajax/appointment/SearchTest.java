package com.openexchange.ajax.appointment;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;

import com.openexchange.ajax.AppointmentTest;
import com.openexchange.ajax.appointment.action.SearchRequest;
import com.openexchange.ajax.appointment.action.SearchResponse;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXSession;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.groupware.container.AppointmentObject;

public class SearchTest extends AppointmentTest {

	private static final Log LOG = LogFactory.getLog(SearchTest.class);
	
	public SearchTest(final String name) {
		super(name);
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}
	
	public void testSimpleSearch() throws Exception {
		final AppointmentObject appointmentObj = new AppointmentObject();
		final String date = String.valueOf(System.currentTimeMillis());
		appointmentObj.setTitle("testSimpleSearch" + date);
		appointmentObj.setStartDate(new Date(startTime));
		appointmentObj.setEndDate(new Date(endTime));
		appointmentObj.setParentFolderID(appointmentFolderId);
		appointmentObj.setIgnoreConflicts(true);
		
		final int objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
		
		final AppointmentObject[] appointmentArray = searchAppointment(getWebConversation(), "testSimpleSearch" + date, appointmentFolderId, new Date(), new Date(), APPOINTMENT_FIELDS, timeZone, PROTOCOL + getHostName(), getSessionId());
		assertTrue("appointment array size is 0", appointmentArray.length > 0);
		
		deleteAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getSessionId());
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
            final SearchRequest searchRequest = new SearchRequest("testShowLastModifiedUTC", appointmentFolderId, cols, true);
            final SearchResponse response = Executor.execute(client, searchRequest);
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