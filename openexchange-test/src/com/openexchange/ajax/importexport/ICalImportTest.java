package com.openexchange.ajax.importexport;

import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.importexport.ImportResult;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.webdav.xml.AppointmentTest;
import com.openexchange.webdav.xml.TaskTest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ICalImportTest extends AbstractICalTest {
	
	private static final Log LOG = LogFactory.getLog(ICalImportTest.class);
	
	public ICalImportTest(String name) {
		super(name);
	}
	
	protected void setUp() throws Exception {
		super.setUp();
	}
	
	public void testDummy() throws Exception {
		
	}
	
	public void testImportICalWithAppointment() throws Exception {
		AppointmentObject appointmentObj = new AppointmentObject();
		appointmentObj.setTitle("testImportICalWithAppointment");
		appointmentObj.setStartDate(startTime);
		appointmentObj.setEndDate(endTime);
		appointmentObj.setShownAs(AppointmentObject.ABSENT);
		ImportResult[] importResult = importICal(getWebConversation(), new AppointmentObject[]  {appointmentObj}, appointmentFolderId, timeZone, emailaddress, getHostName(), getSessionId());
		
		assertEquals("import result size is not 1", 1, importResult.length);
		assertTrue("server errors of server", importResult[0].isCorrect());
		
		AppointmentTest.deleteAppointment(getWebConversation(), Integer.parseInt(importResult[0].getObjectId()), appointmentFolderId, getHostName(), getLogin(), getPassword());
	}
	
	public void testImportICalWithTask() throws Exception {
		Task taskObj = new Task();
		taskObj.setTitle("testImportICalWithTask");
		taskObj.setStartDate(startTime);
		taskObj.setEndDate(endTime);
		ImportResult[] importResult = importICal(getWebConversation(), new Task[] { taskObj }, taskFolderId, timeZone, emailaddress, getHostName(), getSessionId());
		
		assertEquals("import result size is not 1", 1, importResult.length);
		assertTrue("server errors of server", importResult[0].isCorrect());
		
		TaskTest.deleteTask(getWebConversation(), Integer.parseInt(importResult[0].getObjectId()), taskFolderId, getHostName(), getLogin(), getPassword());
	}
}