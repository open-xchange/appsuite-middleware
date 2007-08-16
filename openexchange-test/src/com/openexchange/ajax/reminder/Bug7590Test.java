package com.openexchange.ajax.reminder;

import com.openexchange.ajax.AppointmentTest;
import com.openexchange.ajax.FolderTest;
import com.openexchange.ajax.config.ConfigTools;
import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.reminder.ReminderObject;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class Bug7590Test extends ReminderTest {

	public Bug7590Test(String name) {
		super(name);
	}
	
	public void testBug7590() throws Exception {
		final int userId = ConfigTools.getUserId(getWebConversation(), getHostName(), getSessionId());
		final TimeZone timeZone = ConfigTools.getTimeZone(getWebConversation(), getHostName(), getSessionId());
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeZone(timeZone);
		calendar.set(Calendar.HOUR_OF_DAY, 8);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		
		long startTime = calendar.getTimeInMillis();
		startTime += timeZone.getOffset(startTime);
		long endTime = startTime + 3600000;
		
		calendar.add(Calendar.HOUR_OF_DAY, -1);
		final long alarmLong = calendar.getTimeInMillis();
		final Date alarm = new Date(alarmLong+timeZone.getOffset(alarmLong));		
		
		final FolderObject folderObj = FolderTest.getStandardCalendarFolder(getWebConversation(), getHostName(), getSessionId());
		final int folderId = folderObj.getObjectID();
		
		int alarmMinutes = 60;
		
		AppointmentObject appointmentObj = new AppointmentObject();
		appointmentObj.setTitle("testBug7590");
		appointmentObj.setStartDate(new Date(startTime));
		appointmentObj.setEndDate(new Date(endTime));
		appointmentObj.setShownAs(AppointmentObject.ABSENT);
		appointmentObj.setAlarm(alarmMinutes);
		appointmentObj.setParentFolderID(folderId);
		appointmentObj.setRecurrenceType(AppointmentObject.DAILY);
		appointmentObj.setInterval(1);
		appointmentObj.setOccurrence(3);
		appointmentObj.setIgnoreConflicts(true);
		
		final int targetId = AppointmentTest.insertAppointment(getWebConversation(), appointmentObj, timeZone, getHostName(), getSessionId());
		final String target = String.valueOf(targetId);
		
		ReminderObject reminderObj = new ReminderObject();
		reminderObj.setTargetId(targetId);
		reminderObj.setFolder(String.valueOf(folderId));
		reminderObj.setDate(alarm);
		
		ReminderObject[] reminderArray = listReminder(getWebConversation(), new Date(endTime), timeZone, getHostName(), getSessionId());

		boolean found = false;
		
		int pos = -1;
		for (int a = 0; a < reminderArray.length; a++) {
			if (target.equals(reminderArray[a].getTargetId())) {
				pos = a;
				reminderObj.setObjectId(reminderArray[a].getObjectId());
				compareReminder(reminderObj, reminderArray[a]);
				found = true;
				break;
			}
		}

		assertTrue("no reminder find for target id " + targetId + " in response", found);
		
		deleteReminder(getWebConversation(), reminderArray[pos].getObjectId(), getHostName(), getSessionId());
		AppointmentTest.deleteAppointment(getWebConversation(), targetId, folderId, getHostName(), getSessionId());
	} 
}

