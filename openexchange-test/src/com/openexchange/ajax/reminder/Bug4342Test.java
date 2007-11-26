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


public class Bug4342Test extends ReminderTest {

	public Bug4342Test(String name) {
		super(name);
	}
	
	public void testBug4342() throws Exception {
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
		appointmentObj.setTitle("testBug4342");
		appointmentObj.setStartDate(new Date(startTime));
		appointmentObj.setEndDate(new Date(endTime));
		appointmentObj.setShownAs(AppointmentObject.ABSENT);
		appointmentObj.setAlarm(45);
		appointmentObj.setParentFolderID(folderId);
		appointmentObj.setIgnoreConflicts(true);
		
		final int targetId = AppointmentTest.insertAppointment(getWebConversation(), appointmentObj, timeZone, getHostName(), getSessionId());
		final String target = String.valueOf(targetId);
		
		ReminderObject reminderObj = new ReminderObject();
		reminderObj.setTargetId(targetId);
		reminderObj.setFolder(String.valueOf(folderId));
		reminderObj.setDate(new Date(startTime-(45*60*1000)));
		
		ReminderObject[] reminderArray = listReminder(getWebConversation(), new Date(endTime), timeZone, getHostName(), getSessionId());

		int pos = -1;
		for (int a = 0; a < reminderArray.length; a++) {
			if (target.equals(reminderArray[a].getTargetId())) {
				pos = a;
				reminderObj.setObjectId(reminderArray[a].getObjectId());
				compareReminder(reminderObj, reminderArray[a]);
			}
		}
		
		appointmentObj.removeParentFolderID();
		appointmentObj.setAlarm(30);
		
		reminderObj.setDate(new Date(startTime-(30*60*1000)));
		
		AppointmentTest.updateAppointment(getWebConversation(), appointmentObj, targetId, folderId, timeZone, getHostName(), getSessionId());
		
		reminderArray = listReminder(getWebConversation(), new Date(endTime), timeZone, getHostName(), getSessionId());

		pos = -1;
		for (int a = 0; a < reminderArray.length; a++) {
			if (target.equals(reminderArray[a].getTargetId())) {
				pos = a;
				reminderObj.setObjectId(reminderArray[a].getObjectId());
				compareReminder(reminderObj, reminderArray[a]);
			}
		}
		
		deleteReminder(getWebConversation(), reminderArray[pos].getObjectId(), getHostName(), getSessionId());
		AppointmentTest.deleteAppointment(getWebConversation(), targetId, folderId, getHostName(), getSessionId());
	} 
}

