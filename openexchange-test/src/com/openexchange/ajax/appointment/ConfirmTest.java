package com.openexchange.ajax.appointment;

import com.openexchange.ajax.AppointmentTest;
import com.openexchange.groupware.container.AppointmentObject;
import java.util.Date;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ConfirmTest extends AppointmentTest {

	private static final Log LOG = LogFactory.getLog(AllTest.class);
	
	public ConfirmTest(String name) {
		super(name);
	}
	
	protected void setUp() throws Exception {
		super.setUp();
	}
	
    public void testConfirm() throws Exception {
        AppointmentObject appointmentObj = createAppointmentObject("testConfirm");
        int objectId = insertAppointment(getWebConversation(), appointmentObj, PROTOCOL + getHostName(), getSessionId());
        
        confirmAppointment(getWebConversation(), objectId, AppointmentObject.ACCEPT, null, PROTOCOL + getHostName(), getSessionId());
        deleteAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getSessionId());
    }
}