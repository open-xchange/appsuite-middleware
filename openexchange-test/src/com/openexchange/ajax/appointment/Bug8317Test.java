package com.openexchange.ajax.appointment;

import java.util.Calendar;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.ajax.AppointmentTest;
import com.openexchange.api.OXConflictException;
import com.openexchange.groupware.container.AppointmentObject;

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
	
        
        /*
         INFO: This testcase must be done at least today + 1 days because otherwise
         no cconflict resolution is made because past appointments do not conflict!
         *
         Therefor i changed this to a future date and i fixed the test case.
         
         TODO: Create a dynamic date/time in the future for testing.
         */
	public void testBug8317() throws Exception {
		
		Calendar calendar = Calendar.getInstance(timeZone);
		calendar.setTimeInMillis(startTime);
		
		calendar.add(Calendar.DAY_OF_MONTH, 5);
		
		final int year = calendar.get(Calendar.YEAR);
		final int month = calendar.get(Calendar.MONTH);
		final int day = calendar.get(Calendar.DAY_OF_MONTH);
		
		calendar.set(year, month, day, 0, 0, 0);
		Date startDate = calendar.getTime();
		calendar.add(Calendar.DAY_OF_MONTH, 1);
		Date endDate = calendar.getTime();
		
		AppointmentObject appointmentObj = new AppointmentObject();
		appointmentObj.setTitle("testBug8317");
		appointmentObj.setStartDate(startDate);
		appointmentObj.setEndDate(endDate);
		appointmentObj.setFullTime(true);
		appointmentObj.setParentFolderID(appointmentFolderId);
		appointmentObj.setShownAs(AppointmentObject.ABSENT);
		appointmentObj.setIgnoreConflicts(true);
		int objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, getHostName(), getSessionId());
		
		calendar.set(year, month, day, 0, 30, 0);
		startDate = calendar.getTime();
		
		calendar.set(year, month, day, 1, 0, 0);
		endDate = calendar.getTime();
		
		appointmentObj = new AppointmentObject();
		appointmentObj.setTitle("testBug8317 II");
		appointmentObj.setStartDate(startDate);
		appointmentObj.setEndDate(endDate);
		appointmentObj.setParentFolderID(appointmentFolderId);
		appointmentObj.setShownAs(AppointmentObject.ABSENT);
		appointmentObj.setIgnoreConflicts(false);
                
		try {
			int objectId2 = insertAppointment(getWebConversation(), appointmentObj, timeZone, getHostName(), getSessionId());
			fail("conflict exception expected!");
		} catch (OXConflictException exc) {
			// Perfect. The insertAppointment throws a OXConflictException
            // And this is what we expect here !!!
		}
		
		deleteAppointment(getWebConversation(), objectId, appointmentFolderId, getHostName(), getSessionId());
	}
}