package com.openexchange.ajax.appointment;

import com.openexchange.ajax.AppointmentTest;
import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.test.TestException;
import com.openexchange.webdav.calendar;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Bug8317Test extends AppointmentTest {
	
	private static final Log LOG = LogFactory.getLog(Bug8317Test.class);
	
	public Bug8317Test(String name) {
		super(name);
	}
	
	protected void setUp() throws Exception {
		super.setUp();
	}
	
	public void testDummy() {
		
	}
	
	public void _notestBug8317() throws Exception {
		TimeZone timeZoneUTC = TimeZone.getTimeZone("UTC");
		SimpleDateFormat simpleDateFormatUTC = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		simpleDateFormatUTC.setTimeZone(timeZoneUTC);
		
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		simpleDateFormat.setTimeZone(timeZone);
		
		Date startDate = simpleDateFormatUTC.parse("2007-06-10 00:00:00");
		Date endDate = simpleDateFormatUTC.parse("2007-07-10 00:00:00");
		
		AppointmentObject appointmentObj = new AppointmentObject();
		appointmentObj.setTitle("testBug8317");
		appointmentObj.setStartDate(startDate);
		appointmentObj.setEndDate(endDate);
		appointmentObj.setFullTime(true);
		appointmentObj.setParentFolderID(appointmentFolderId);
		appointmentObj.setShownAs(AppointmentObject.ABSENT);
		appointmentObj.setIgnoreConflicts(true);
		int objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, getHostName(), getSessionId());
		
		startDate = simpleDateFormat.parse("2007-06-10 00:30:00");
		endDate = simpleDateFormat.parse("2007-06-10 01:00:00");
		
		appointmentObj = new AppointmentObject();
		appointmentObj.setTitle("testBug8317 II");
		appointmentObj.setStartDate(startDate);
		appointmentObj.setEndDate(endDate);
		appointmentObj.setParentFolderID(appointmentFolderId);
		appointmentObj.setShownAs(AppointmentObject.ABSENT);
		
		try {
			int objectId2 = insertAppointment(getWebConversation(), appointmentObj, timeZone, getHostName(), getSessionId());
			fail("conflict exception expected!");
		} catch (TestException exc) {
			fail("exception: " + exc.toString());
		}
		
		deleteAppointment(getWebConversation(), objectId, appointmentFolderId, getHostName(), getSessionId());
	}
}