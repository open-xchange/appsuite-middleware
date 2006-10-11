package com.openexchange.ajax.appointment;

import com.openexchange.ajax.AppointmentTest;
import com.openexchange.groupware.container.AppointmentObject;
import java.util.Date;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AllTest extends AppointmentTest {

	private static final Log LOG = LogFactory.getLog(AllTest.class);
	
	public AllTest(String name) {
		super(name);
	}
	
	protected void setUp() throws Exception {
		super.setUp();
	}
	
	public void testAll() throws Exception {
		
		
		Date start = new Date(System.currentTimeMillis()-(dayInMillis*7));
		Date end = new Date(System.currentTimeMillis()+(dayInMillis*7));
		
		final int cols[] = new int[]{ AppointmentObject.OBJECT_ID };
		
		AppointmentObject[] appointmentArray = listAppointment(getWebConversation(), appointmentFolderId, cols, start, end, timeZone, PROTOCOL + getHostName(), getSessionId());
	}
}