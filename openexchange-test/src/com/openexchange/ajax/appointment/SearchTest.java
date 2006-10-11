package com.openexchange.ajax.appointment;

import com.openexchange.ajax.AppointmentTest;
import com.openexchange.groupware.container.AppointmentObject;
import java.util.Date;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SearchTest extends AppointmentTest {

	private static final Log LOG = LogFactory.getLog(SearchTest.class);
	
	public SearchTest(String name) {
		super(name);
	}
	
	protected void setUp() throws Exception {
		super.setUp();
	}
	
	public void testSimpleSearch() throws Exception {
		AppointmentObject appointmentObj = new AppointmentObject();
		String date = String.valueOf(System.currentTimeMillis());
		appointmentObj.setTitle("testSimpleSearch" + date);
		appointmentObj.setStartDate(new Date(startTime));
		appointmentObj.setEndDate(new Date(endTime));
		appointmentObj.setParentFolderID(appointmentFolderId);
		
		int objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
		
		AppointmentObject[] appointmentArray = searchAppointment(getWebConversation(), "testSimpleSearch" + date, appointmentFolderId, APPOINTMENT_FIELDS, timeZone, PROTOCOL + getHostName(), getSessionId());
		assertTrue("appointment array size is 0", appointmentArray.length > 0);
		
		deleteAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getSessionId());
	}
}