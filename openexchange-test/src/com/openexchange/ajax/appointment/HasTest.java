package com.openexchange.ajax.appointment;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.ajax.AppointmentTest;
import com.openexchange.groupware.container.AppointmentObject;

public class HasTest extends AppointmentTest {

	private static final Log LOG = LogFactory.getLog(HasTest.class);
	
	public HasTest(String name) {
		super(name);
	}
	
	protected void setUp() throws Exception {
		super.setUp();
	}
	
    public void testHasAppointment() throws Exception {
        Calendar c = Calendar.getInstance();
        c.setTimeZone(TimeZone.getTimeZone("UTC"));
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        
        int numberOfDays = 7;
        
        Date start = c.getTime();
        Date end = new Date(start.getTime() + (dayInMillis*numberOfDays));
		
		int posInArray = 3;
		boolean[] hasAppointments = hasAppointments(getWebConversation(), start, end, PROTOCOL + getHostName(), getSessionId());

		AppointmentObject appointmentObj = new AppointmentObject();
		appointmentObj.setTitle("testHasAppointmentFullTime");
		appointmentObj.setStartDate(new Date(start.getTime()+(dayInMillis*posInArray)+(60*60*1000)));
		appointmentObj.setEndDate(new Date(start.getTime()+(dayInMillis*posInArray)+(60*60*1000)));
		appointmentObj.setShownAs(AppointmentObject.ABSENT);
		appointmentObj.setParentFolderID(appointmentFolderId);
		appointmentObj.setIgnoreConflicts(true);
		
		int objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
        
        hasAppointments = hasAppointments(getWebConversation(), start, end, PROTOCOL + getHostName(), getSessionId());
        assertEquals("has array length is wrong", numberOfDays, hasAppointments.length);
		assertEquals("boolean on pos " + posInArray + " is not true", hasAppointments[posInArray], true);
		
		deleteAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getSessionId());
    }
	
    public void testHasAppointmentFullTime() throws Exception {
        Calendar c = Calendar.getInstance();
        c.setTimeZone(TimeZone.getTimeZone("UTC"));
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        
        int numberOfDays = 7;
        
        Date start = c.getTime();
        Date end = new Date(start.getTime() + (dayInMillis*numberOfDays));
		
		boolean conflict = true;
		
		int posInArray = 3;
		boolean[] hasAppointments = hasAppointments(getWebConversation(), start, end, PROTOCOL + getHostName(), getSessionId());
		
		AppointmentObject appointmentObj = new AppointmentObject();
		appointmentObj.setTitle("testHasAppointmentFullTime");
		appointmentObj.setStartDate(new Date(start.getTime()+(dayInMillis*posInArray)));
		appointmentObj.setEndDate(new Date(start.getTime()+(dayInMillis*(posInArray+1))));
		appointmentObj.setShownAs(AppointmentObject.ABSENT);
		appointmentObj.setFullTime(true);
		appointmentObj.setParentFolderID(appointmentFolderId);
		appointmentObj.setIgnoreConflicts(true);
		
		int objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
        
        hasAppointments = hasAppointments(getWebConversation(), start, end, PROTOCOL + getHostName(), getSessionId());
        assertEquals("has array length is wrong", numberOfDays, hasAppointments.length);
		assertEquals("boolean on pos " + posInArray + " is not true", hasAppointments[posInArray], true);
		
		deleteAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getSessionId());
    }
}
