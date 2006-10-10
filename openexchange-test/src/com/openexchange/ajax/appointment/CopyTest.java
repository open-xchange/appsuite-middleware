package com.openexchange.ajax.appointment;

import com.openexchange.ajax.AppointmentTest;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CopyTest extends AppointmentTest {

	private static final Log LOG = LogFactory.getLog(AllTest.class);
	
	public CopyTest(String name) {
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
        Date end = new Date(start.getTime() + (24*60*60*1000*numberOfDays));
        
        boolean[] hasAppointments = hasAppointments(getWebConversation(), start, end, PROTOCOL + getHostName(), getSessionId());
        assertEquals("has array length is wrong", numberOfDays, hasAppointments.length);
    }
}