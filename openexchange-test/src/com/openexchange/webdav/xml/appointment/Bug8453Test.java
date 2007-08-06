package com.openexchange.webdav.xml.appointment;

import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.webdav.xml.AppointmentTest;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Bug8453Test extends AppointmentTest {
	
	private static final Log LOG = LogFactory.getLog(Bug6535Test.class);
	
	public Bug8453Test(String name) {
		super(name);
	}
	
	protected void setUp() throws Exception {
		super.setUp();
	}
	
	public void testBug8453() throws Exception {
		Date modified = new Date();
		
		TimeZone timeZoneUTC = TimeZone.getTimeZone("UTC");
		
		Calendar calendar = Calendar.getInstance(timeZoneUTC);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		
		calendar.add(Calendar.DAY_OF_MONTH, 2);
		
		Date recurrenceDatePosition = calendar.getTime();
		
		calendar.add(Calendar.DAY_OF_MONTH, 3);
		
		Date until = calendar.getTime();
		
		AppointmentObject appointmentObj = new AppointmentObject();
		appointmentObj.setTitle("testBug8453");
		appointmentObj.setStartDate(startTime);
		appointmentObj.setEndDate(endTime);
		appointmentObj.setShownAs(AppointmentObject.ABSENT);
		appointmentObj.setParentFolderID(appointmentFolderId);
		appointmentObj.setRecurrenceType(AppointmentObject.DAILY);
		appointmentObj.setInterval(1);
		appointmentObj.setUntil(until);
		appointmentObj.setIgnoreConflicts(true);
		
		UserParticipant[] users = new UserParticipant[1];
		users[0] = new UserParticipant();
		users[0].setIdentifier(userId);
		users[0].setConfirm(AppointmentObject.ACCEPT);
		
		appointmentObj.setUsers(users);

		int objectId = insertAppointment(getWebConversation(), appointmentObj, PROTOCOL + getHostName(), getLogin(), getPassword());
		
		AppointmentObject recurrenceUpdate = new AppointmentObject();
		recurrenceUpdate.setTitle("testBug8453 - exception");
		recurrenceUpdate.setStartDate(new Date(startTime.getTime()+600000));
		recurrenceUpdate.setEndDate(new Date(endTime.getTime()+600000));
		recurrenceUpdate.setRecurrenceDatePosition(recurrenceDatePosition);
		recurrenceUpdate.setShownAs(AppointmentObject.ABSENT);
		recurrenceUpdate.setParentFolderID(appointmentFolderId);
		recurrenceUpdate.setIgnoreConflicts(true);
		recurrenceUpdate.setUsers(users);
		recurrenceUpdate.setAlarm(60);

		updateAppointment(getWebConversation(), recurrenceUpdate, objectId, appointmentFolderId, getHostName(), getLogin(), getPassword());
		
		appointmentObj.setObjectID(objectId);
		AppointmentObject loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getLogin(), getPassword());
		compareObject(appointmentObj, loadAppointment);
		
		loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, modified, getHostName(), getLogin(), getPassword());
		compareObject(appointmentObj, loadAppointment);
		
		deleteAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getLogin(), getPassword());
	}
}