package com.openexchange.ajax.appointment;

import com.openexchange.ajax.AppointmentTest;
import com.openexchange.groupware.container.AppointmentObject;
import java.util.Date;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class UpdatesTest extends AppointmentTest {

	private static final Log LOG = LogFactory.getLog(UpdatesTest.class);
	
	public UpdatesTest(String name) {
		super(name);
	}
	
	protected void setUp() throws Exception {
		super.setUp();
	}
	
	public void testDummy() throws Exception {
		
	}
	
	public void testModified() throws Exception {
		AppointmentTest.listModifiedAppointment(getWebConversation(), appointmentFolderId, new Date(), new Date(), new Date(System.currentTimeMillis()-(dayInMillis*7)), timeZone, PROTOCOL + getHostName(), getSessionId());
	}
	
	public void testDeleted() throws Exception {
		AppointmentTest.listDeleteAppointment(getWebConversation(), appointmentFolderId, new Date(), new Date(), new Date(System.currentTimeMillis()-(dayInMillis*7)), timeZone, PROTOCOL + getHostName(), getSessionId());
	}
	
	public void testModifiedWithoutFolderId() throws Exception {
		Date start = new Date(System.currentTimeMillis() - (7 * dayInMillis));
		Date end = new Date(System.currentTimeMillis() + (7 * dayInMillis));
		Date since = new Date();
		
		AppointmentObject appointmentObj = createAppointmentObject("testModifiedWithoutFolderId");
		appointmentObj.setIgnoreConflicts(true);
		int objectId = AppointmentTest.insertAppointment(getWebConversation(), appointmentObj, timeZone, getHostName(), getSessionId());
		
		AppointmentObject[] appointmentArray = AppointmentTest.listModifiedAppointment(getWebConversation(), start, end, since, timeZone, PROTOCOL + getHostName(), getSessionId());
		
		assertTrue("no appointment object in response", appointmentArray.length > 0);
		boolean found = false;
		
		for (int a = 0; a < appointmentArray.length; a++) {
			if (appointmentArray[a].getObjectID() == objectId) {
				found = true;
			}
		}
		
		assertTrue("created object not found in response", found);
	}
}

