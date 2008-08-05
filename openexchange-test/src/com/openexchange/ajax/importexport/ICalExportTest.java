package com.openexchange.ajax.importexport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.webdav.xml.AppointmentTest;
import com.openexchange.webdav.xml.TaskTest;

public class ICalExportTest extends AbstractICalTest {
	
	private static final Log LOG = LogFactory.getLog(ICalImportTest.class);
	
	public ICalExportTest(final String name) {
		super(name);
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}
	
	public void testDummy() throws Exception {
		
	}
	
	public void testExportICalAppointment() throws Exception {
		final String title = "testExportICalAppointment" + System.currentTimeMillis();
		
		final AppointmentObject appointmentObj = new AppointmentObject();
		appointmentObj.setTitle(title);
		appointmentObj.setStartDate(startTime);
		appointmentObj.setEndDate(endTime);
		appointmentObj.setShownAs(AppointmentObject.RESERVED);
		appointmentObj.setParentFolderID(appointmentFolderId);
		appointmentObj.setIgnoreConflicts(true);
		
		final int objectId = AppointmentTest.insertAppointment(getWebConversation(), appointmentObj, getHostName(), getLogin(), getPassword());

		final AppointmentObject[] appointmentArray = exportAppointment(getWebConversation(), appointmentFolderId, emailaddress, timeZone, getHostName(), getSessionId(), ctx);
		
		boolean found = false;
		for (int a = 0; a < appointmentArray.length; a++) {
			if ((null != appointmentArray[a].getTitle()) && (appointmentArray[a].getTitle().equals(title))) {
				found = true;
				//java.util.Date d = null;
				//appointmentArray[a].setUntil(d);
				appointmentObj.setUntil(appointmentArray[a].getUntil());
				appointmentArray[a].setParentFolderID(appointmentFolderId); // Not Exported
				AppointmentTest.compareObject(appointmentObj, appointmentArray[a]);
			}
		}
		
		assertTrue("appointment with title: " + title + " not found", found);
		
		AppointmentTest.deleteAppointment(getWebConversation(), objectId, appointmentFolderId, getHostName(), getLogin(), getPassword());
	}
	
	
	public void testExportICalTask() throws Exception {
		final String title = "testExportICalTask" + System.currentTimeMillis();
		
		final Task taskObj = new Task();
		taskObj.setTitle(title);
		taskObj.setStartDate(startTime);
		taskObj.setEndDate(endTime);
		taskObj.setParentFolderID(taskFolderId);
		
		final int objectId = TaskTest.insertTask(getWebConversation(), taskObj, getHostName(), getLogin(), getPassword());

		final Task[] taskArray = exportTask(getWebConversation(), taskFolderId, emailaddress, timeZone, getHostName(), getSessionId(), ctx);
		
		boolean found = false;
		for (int a = 0; a < taskArray.length; a++) {
			if ((null != taskArray[a].getTitle()) && (taskArray[a].getTitle().equals(title))) {
				found = true;
				taskObj.setStartDate(taskArray[a].getStartDate());

				//System.out.println(taskObj.getEndDate().getTimezoneOffset()+" | "+taskArray[a].getEndDate().getTimezoneOffset());
				taskArray[a].setParentFolderID(taskFolderId);		
				TaskTest.compareObject(taskObj, taskArray[a]);
			}
		}
		
		assertTrue("task with id: " + objectId + " not found", found);
		
		TaskTest.deleteTask(getWebConversation(), objectId, taskFolderId, getHostName(), getLogin(), getPassword());
	}
}