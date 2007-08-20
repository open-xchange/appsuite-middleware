package com.openexchange.ajax.appointment;

import com.openexchange.ajax.AppointmentTest;
import com.openexchange.ajax.appointment.action.NewAppointmentSearchRequest;
import com.openexchange.ajax.appointment.action.NewAppointmentSearchResponse;
import com.openexchange.ajax.framework.AJAXSession;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.CommonObject;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.container.FolderChildObject;
import java.util.Calendar;
import java.util.Date;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PortalSearchTest extends AppointmentTest {

	private static final Log LOG = LogFactory.getLog(PortalSearchTest.class);
	
	private int[] columns = {
		DataObject.OBJECT_ID,
		FolderChildObject.FOLDER_ID,
		CommonObject.PRIVATE_FLAG,
		CommonObject.CATEGORIES,
		CalendarObject.TITLE,
		AppointmentObject.LOCATION,
		CalendarObject.START_DATE,
		CalendarObject.END_DATE,
		CalendarObject.NOTE,
		CalendarObject.RECURRENCE_TYPE,
		AppointmentObject.SHOWN_AS,
		AppointmentObject.FULL_TIME,
		AppointmentObject.COLOR_LABEL
	};
	
	public PortalSearchTest(String name) {
		super(name);
	}
	
	protected void setUp() throws Exception {
		super.setUp();
	}
	
    public void testNewAppointmentsSearch() throws Exception {
		final Calendar calendar = Calendar.getInstance(timeZone);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.add(Calendar.DAY_OF_MONTH, -2);
		
		final Date start = calendar.getTime();
		
		calendar.add(Calendar.YEAR, 1);
		
		final Date end = calendar.getTime();
		
		final AppointmentObject appointmentObj = createAppointmentObject("testNewAppointmentsSearch");
		appointmentObj.setIgnoreConflicts(true);
		
		final int objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, getHostName(), getSessionId());
		appointmentObj.setObjectID(objectId);
		
		final AJAXSession ajaxSession = new AJAXSession(getWebConversation(), getSessionId());
		final NewAppointmentSearchRequest request = new NewAppointmentSearchRequest(start, end, 10000, timeZone, columns);
		final NewAppointmentSearchResponse response = (NewAppointmentSearchResponse)Executor.execute(ajaxSession, request);
		
		if (response.hasError()) {
			throw new Exception("json error: " + response.getResponse().getErrorMessage());
		}
		
		final AppointmentObject[] appointmentArray = response.getAppointments();
		
		boolean found = false;
		for (int a = 0; a < appointmentArray.length; a++) {
			if (appointmentArray[a].getObjectID() == objectId) {
				compareObject(appointmentObj, appointmentArray[a]);
				found = true;
			}
		}
		
		assertTrue("object with id " + objectId + " not found in response", found);
		
		deleteAppointment(getWebConversation(), objectId, appointmentFolderId, getHostName(), getSessionId());
    }
}

