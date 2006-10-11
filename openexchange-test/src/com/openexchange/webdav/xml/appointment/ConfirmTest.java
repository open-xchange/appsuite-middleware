package com.openexchange.webdav.xml.appointment;

import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.webdav.xml.AppointmentTest;

public class ConfirmTest extends AppointmentTest {
	
	
	public void testConfirm() throws Exception {
		AppointmentObject appointmentObj = createAppointmentObject("testConfirm");
		int objectId = insertAppointment(webCon, appointmentObj, PROTOCOL + hostName, login, password);
		confirmAppointment(webCon, objectId, CalendarObject.DECLINE, null, PROTOCOL + hostName, login, password);
		
		int[][] objectIdAndFolderId = { {objectId, appointmentFolderId } };
		deleteAppointment(webCon, objectIdAndFolderId, PROTOCOL + hostName, login, password );
	}
}

