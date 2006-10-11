package com.openexchange.ajax.appointment;

import com.openexchange.ajax.AppointmentTest;
import com.openexchange.groupware.container.AppointmentObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DeleteTest extends AppointmentTest {

	private static final Log LOG = LogFactory.getLog(DeleteTest.class);
	
	public DeleteTest(String name) {
		super(name);
	}
	
	protected void setUp() throws Exception {
		super.setUp();
	}
	
    public void testDelete() throws Exception {
        AppointmentObject appointmentObj = createAppointmentObject("testDelete");
        int objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
        int id = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
        
        deleteAppointment(getWebConversation(), id, appointmentFolderId, PROTOCOL + getHostName(), getSessionId());
    }
}

