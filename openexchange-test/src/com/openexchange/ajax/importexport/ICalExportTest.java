package com.openexchange.ajax.importexport;

import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.webdav.xml.AppointmentTest;
import com.openexchange.webdav.xml.TaskTest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ICalExportTest extends AbstractICalTest {
	
	private static final Log LOG = LogFactory.getLog(ICalImportTest.class);
	
	public ICalExportTest(String name) {
		super(name);
	}
	
	protected void setUp() throws Exception {
		super.setUp();
	}
	
	public void testDummy() throws Exception {
		
	}
	
	public void testExportICalAppointment() throws Exception {
		final String title = "testExportICalAppointment" + System.currentTimeMillis();
		
		AppointmentObject appointmentObj = new AppointmentObject();
		appointmentObj.setTitle(title);
		appointmentObj.setStartDate(startTime);
		appointmentObj.setEndDate(endTime);
		appointmentObj.setShownAs(AppointmentObject.ABSENT);
		appointmentObj.setParentFolderID(appointmentFolderId);
		
		int objectId = AppointmentTest.insertAppointment(getWebConversation(), appointmentObj, getHostName(), getLogin(), getPassword());

		AppointmentObject[] appointmentArray = exportAppointment(getWebConversation(), appointmentFolderId, emailaddress, timeZone, getHostName(), getSessionId());
		
		boolean found = false;
		for (int a = 0; a < appointmentArray.length; a++) {
			if (appointmentArray[a].getTitle().equals(title)) {
				found = true;
				AppointmentTest.compareObject(appointmentObj, appointmentArray[a]);
			}
		}
		
		assertTrue("appointment with title: " + title + " not found", found);
		
		AppointmentTest.deleteAppointment(getWebConversation(), objectId, appointmentFolderId, getHostName(), getLogin(), getPassword());
	}
	
	public void _notestExportICalTask() throws Exception {
		final String title = "testExportICalTask" + System.currentTimeMillis();
		
		Task taskObj = new Task();
		taskObj.setTitle(title);
		taskObj.setStartDate(startTime);
		taskObj.setEndDate(endTime);
		taskObj.setParentFolderID(taskFolderId);
		
		int objectId = TaskTest.insertTask(getWebConversation(), taskObj, getHostName(), getLogin(), getPassword());

		Task[] taskArray = exportTask(getWebConversation(), taskFolderId, emailaddress, timeZone, getHostName(), getSessionId());
		
		boolean found = false;
		for (int a = 0; a < taskArray.length; a++) {
			if (taskArray[a].getTitle().equals(title)) {
				found = true;
				TaskTest.compareObject(taskObj, taskArray[a]);
			}
		}
		
		assertTrue("task with id: " + objectId + " not found", found);
		
		TaskTest.deleteTask(getWebConversation(), objectId, taskFolderId, getHostName(), getLogin(), getPassword());
	}
}