package com.openexchange.webdav.xml.appointment;

import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.test.TestException;
import com.openexchange.webdav.xml.AppointmentTest;
import com.openexchange.webdav.xml.FolderTest;
import com.openexchange.webdav.xml.XmlServlet;

public class Bug6455Test extends AppointmentTest {
	
	public Bug6455Test(String name) {
		super(name);
	}
	
	public void testDummy() {
		
	}
	
	public void testBug6455() throws Exception {
		final StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append("testBug6455");
		stringBuffer.append(" - ");
		stringBuffer.append("012345678901234567890123456789012345678901234567890123456789"); // 60 chars
		stringBuffer.append("012345678901234567890123456789012345678901234567890123456789"); // 60 chars
		stringBuffer.append("012345678901234567890123456789012345678901234567890123456789"); // 60 chars
		stringBuffer.append("012345678901234567890123456789012345678901234567890123456789"); // 60 chars
		stringBuffer.append("012345678901234567890123456789012345678901234567890123456789"); // 60 chars
	
		AppointmentObject appointmentObj = new AppointmentObject();
		appointmentObj.setTitle(stringBuffer.toString());
		appointmentObj.setStartDate(startTime);
		appointmentObj.setEndDate(endTime);
		appointmentObj.setShownAs(AppointmentObject.ABSENT);
		appointmentObj.setParentFolderID(appointmentFolderId);
		appointmentObj.setIgnoreConflicts(true);
		
		int objectId = 0;
		
		try {
			objectId = insertAppointment(getWebConversation(), appointmentObj, getHostName(), getLogin(), getPassword());
			fail("permission exception expected!");
		} catch (TestException exc) {
			assertExceptionMessage(exc.getMessage(), XmlServlet.USER_INPUT_STATUS);
		}
		
		if (objectId > 0) {
			deleteAppointment(getWebConversation(), objectId, appointmentFolderId, getHostName(), getLogin(), getPassword());
		}
	}
}

