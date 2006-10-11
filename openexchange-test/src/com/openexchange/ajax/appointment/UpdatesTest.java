package com.openexchange.ajax.appointment;

import com.openexchange.ajax.AppointmentTest;
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
	
	public void testUpdates() throws Exception {
		AppointmentTest.listModifiedAppointment(getWebConversation(), appointmentFolderId, new Date(System.currentTimeMillis()-(dayInMillis*7)), timeZone, PROTOCOL + getHostName(), getSessionId());
	}
}

