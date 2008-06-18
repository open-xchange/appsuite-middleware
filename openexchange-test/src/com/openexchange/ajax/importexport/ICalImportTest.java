package com.openexchange.ajax.importexport;

import java.io.ByteArrayInputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.importexport.ImportResult;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.webdav.xml.AppointmentTest;
import com.openexchange.webdav.xml.TaskTest;

public class ICalImportTest extends AbstractICalTest {
	
	private static final Log LOG = LogFactory.getLog(ICalImportTest.class);
	
	public ICalImportTest(final String name) {
		super(name);
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}
	
	public void testDummy() throws Exception {
		
	}
	
	public void _notestImportICalWithAppointment() throws Exception {
		final AppointmentObject appointmentObj = new AppointmentObject();
		appointmentObj.setTitle("testImportICalWithAppointment" + System.currentTimeMillis());
		appointmentObj.setStartDate(startTime);
		appointmentObj.setEndDate(endTime);
		appointmentObj.setShownAs(AppointmentObject.RESERVED);
		final ImportResult[] importResult = importICal(getWebConversation(), new AppointmentObject[]  {appointmentObj}, appointmentFolderId, timeZone, emailaddress, getHostName(), getSessionId());
		
		assertEquals("import result size is not 1", 1, importResult.length);
		assertTrue("server errors of server", importResult[0].isCorrect());
		
		final int objectId = Integer.parseInt(importResult[0].getObjectId());
		
		assertTrue("object id is 0", objectId > 0);
		
		final AppointmentObject[] appointmentArray = exportAppointment(getWebConversation(), appointmentFolderId, emailaddress, timeZone, getHostName(), getSessionId());
		
		boolean found = false;
		
		for (int a = 0; a < appointmentArray.length; a++) {
			if (appointmentArray[a].getTitle().equals(appointmentObj.getTitle())) {
				appointmentObj.setParentFolderID(appointmentFolderId);
				AppointmentTest.compareObject(appointmentObj, appointmentArray[a]);
				
				found = true;
			}
		}
		
		assertTrue("inserted object not found in response", found);
		
		AppointmentTest.deleteAppointment(getWebConversation(), Integer.parseInt(importResult[0].getObjectId()), appointmentFolderId, getHostName(), getLogin(), getPassword());
	}
	
	public void _notestImportICalWithTask() throws Exception {
		final Task taskObj = new Task();
		taskObj.setTitle("testImportICalWithTask" + System.currentTimeMillis());
		taskObj.setStartDate(startTime);
		taskObj.setEndDate(endTime);
		final ImportResult[] importResult = importICal(getWebConversation(), new Task[] { taskObj }, taskFolderId, timeZone, emailaddress, getHostName(), getSessionId());
		
		assertEquals("import result size is not 1", 1, importResult.length);
		assertTrue("server errors of server", importResult[0].isCorrect());
		
		final int objectId = Integer.parseInt(importResult[0].getObjectId());
		
		assertTrue("object id is 0", objectId > 0);
		
		final Task[] taskArray = exportTask(getWebConversation(), taskFolderId, emailaddress, timeZone, getHostName(), getSessionId());
		
		boolean found = false;
		
		for (int a = 0; a < taskArray.length; a++) {
			if (taskArray[a].getTitle().equals(taskObj.getTitle())) {
				taskObj.setParentFolderID(appointmentFolderId);
				TaskTest.compareObject(taskObj, taskArray[a]);
				
				found = true;
			}
		}
		
		assertTrue("inserted object not found in response", found);
		
		TaskTest.deleteTask(getWebConversation(), Integer.parseInt(importResult[0].getObjectId()), taskFolderId, getHostName(), getLogin(), getPassword());
	}
	
	public void _testImportICalWithBrokenAppointment() throws Exception {
		final String title1 = "testImportICalWithBrokenAppointment1_" + System.currentTimeMillis();
		final String title2 = "testImportICalWithBrokenAppointment2_" + System.currentTimeMillis();
		final String title3 = "testImportICalWithBrokenAppointment3_" + System.currentTimeMillis();
		
		final StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append("BEGIN:VCALENDAR").append('\n');
		stringBuffer.append("VERSION:2.0").append('\n');
		stringBuffer.append("PRODID:OPEN-XCHANGE").append('\n');		
		
		// app1
		stringBuffer.append("BEGIN:VEVENT").append('\n');
		stringBuffer.append("CLASS:PUBLIC").append('\n');
		stringBuffer.append("DTSTART:20070101T080000Z").append('\n');
		stringBuffer.append("DTEND:20070101T100000Z").append('\n');
		stringBuffer.append("SUMMARY:" + title1).append('\n');
		stringBuffer.append("TRANSP:OPAQUE").append('\n');
		stringBuffer.append("END:VEVENT").append('\n');

		// app2
		stringBuffer.append("BEGIN:VEVENT").append('\n');
		stringBuffer.append("CLASS:PUBLIC").append('\n');
		stringBuffer.append("DTSTART:INVALID_DATE").append('\n');
		stringBuffer.append("DTEND:20070101T100000Z").append('\n');
		stringBuffer.append("SUMMARY:" + title2).append('\n');
		stringBuffer.append("TRANSP:OPAQUE").append('\n');
		stringBuffer.append("END:VEVENT").append('\n');
		
		// app3	
		stringBuffer.append("BEGIN:VEVENT").append('\n');
		stringBuffer.append("CLASS:PUBLIC").append('\n');
		stringBuffer.append("DTSTART:20070101T080000Z").append('\n');
		stringBuffer.append("DTEND:20070101T100000Z").append('\n');
		stringBuffer.append("SUMMARY:" + title3).append('\n');
		stringBuffer.append("TRANSP:OPAQUE").append('\n');
		stringBuffer.append("END:VEVENT").append('\n');

		stringBuffer.append("END:VCALENDAR").append('\n');
		
		final ImportResult[] importResult = importICal(getWebConversation(), new ByteArrayInputStream(stringBuffer.toString().getBytes()), appointmentFolderId, -1, timeZone, emailaddress, getHostName(), getSessionId());
		
		assertEquals("invalid import result array size", 3, importResult.length);
		
		assertTrue("server errors of server", importResult[0].isCorrect());
		assertTrue("server errors of server", importResult[1].hasError());
		assertTrue("server errors of server", importResult[2].isCorrect());
		
		final AppointmentObject[] appointmentArray = exportAppointment(getWebConversation(), appointmentFolderId, emailaddress, timeZone, getHostName(), getSessionId());
		
		AppointmentTest.deleteAppointment(getWebConversation(), Integer.parseInt(importResult[0].getObjectId()), appointmentFolderId, getHostName(), getLogin(), getPassword());
		AppointmentTest.deleteAppointment(getWebConversation(), Integer.parseInt(importResult[2].getObjectId()), appointmentFolderId, getHostName(), getLogin(), getPassword());
	}
	
}