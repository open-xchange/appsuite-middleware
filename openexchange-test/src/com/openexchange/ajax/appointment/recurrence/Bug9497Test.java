package com.openexchange.ajax.appointment.recurrence;

import java.util.Calendar;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.groupware.container.AppointmentObject;

public class Bug9497Test extends AbstractRecurrenceTest {
	
	private static final Log LOG = LogFactory.getLog(Bug9497Test.class);
	
	public Bug9497Test(String name) {
		super(name);
	}
	
	/**
	 * This test case checks the calculation for appointments starting before 01.01.1970
	 */
	public void testBug9497() throws Exception {
		final Date startDate = simpleDateFormatUTC.parse("1969-12-28 00:00:00");
		final Date endDate = simpleDateFormat.parse("1969-12-29 00:00:00");
		
		final String title = "testBug9497";
		final AppointmentObject appointmentObj = new AppointmentObject();
		appointmentObj.setTitle(title);
		appointmentObj.setStartDate(startDate);
		appointmentObj.setEndDate(endDate);
		appointmentObj.setFullTime(true);
		appointmentObj.setShownAs(AppointmentObject.ABSENT);
		appointmentObj.setParentFolderID(appointmentFolderId);
		appointmentObj.setRecurrenceType(AppointmentObject.YEARLY);
		appointmentObj.setInterval(1);
		appointmentObj.setDayInMonth(28);
		appointmentObj.setMonth(Calendar.DECEMBER);
		appointmentObj.setIgnoreConflicts(true);
		int objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
		appointmentObj.setObjectID(objectId);
		AppointmentObject loadAppointment = loadAppointment(getWebConversation(), objectId, 39,  appointmentFolderId, timeZone, PROTOCOL + getHostName(), getSessionId());
	}
}
