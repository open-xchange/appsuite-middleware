package com.openexchange.ajax.reminder;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import com.openexchange.ajax.AppointmentTest;
import com.openexchange.ajax.FolderTest;
import com.openexchange.ajax.config.ConfigTools;
import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.reminder.ReminderObject;

public class RangeTest extends ReminderTest {
	
	public RangeTest(String name) {
		super(name);
	}
	
	public void testRange() throws Exception {
		final int userId = ConfigTools.getUserId(getWebConversation(), getHostName(), getSessionId());
		final TimeZone timeZone = ConfigTools.getTimeZone(getWebConversation(), getHostName(), getSessionId());
		
		Calendar c = Calendar.getInstance();
		c.setTimeZone(timeZone);
		c.set(Calendar.HOUR_OF_DAY, 8);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		
		long startTime = c.getTimeInMillis();
		startTime += timeZone.getOffset(startTime);
		long endTime = startTime + 3600000;
		
		
		final FolderObject folderObj = FolderTest.getStandardCalendarFolder(getWebConversation(), getHostName(), getSessionId());
		final int folderId = folderObj.getObjectID();
		
		AppointmentObject appointmentObj = new AppointmentObject();
		appointmentObj.setTitle("testRange");
		appointmentObj.setStartDate(new Date(startTime));
		appointmentObj.setEndDate(new Date(endTime));
		appointmentObj.setShownAs(AppointmentObject.ABSENT);
		appointmentObj.setAlarm(45);
		appointmentObj.setParentFolderID(folderId);
		appointmentObj.setIgnoreConflicts(true);
		
		final int targetId = AppointmentTest.insertAppointment(getWebConversation(), appointmentObj, timeZone, getHostName(), getSessionId());
		final String target = String.valueOf(targetId);
		ReminderObject[] reminderObj = listReminder(getWebConversation(), new Date(endTime), timeZone, getHostName(), getSessionId());

		int pos = -1;
		for (int a = 0; a < reminderObj.length; a++) {
			if (target.equals(reminderObj[a].getTargetId())) {
				pos = a;
			}
		}
		
		assertTrue("reminder not found in response", (pos > -1));
		
		assertTrue("object id not found", reminderObj[pos].getObjectId() > 0);
		assertNotNull("last modified is null", reminderObj[pos].getLastModified());
		assertEquals("target id is not equals", target, reminderObj[pos].getTargetId());
		assertEquals("folder id is not equals", String.valueOf(folderId), reminderObj[pos].getFolder());
		assertEquals("user id is not equals", userId, reminderObj[pos].getUser());
		
		final long expectedAlarm = endTime - (45*60*1000);
		
		// assertEquals("alarm is not equals", expectedAlarm, reminderObj[pos].getDate().getTime());
	}
}

