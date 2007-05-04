package com.openexchange.webdav.xml.appointment;


import com.openexchange.webdav.xml.appointment.recurrence.RecurrenceTestSuite;
import junit.framework.Test;
import junit.framework.TestSuite;

public class AppointmentWebdavSuite extends TestSuite{
	
	public static Test suite(){
		TestSuite tests = new TestSuite();
		tests.addTestSuite( ConfirmTest.class );
		tests.addTestSuite( DeleteTest.class );
		tests.addTestSuite( ListTest.class );
		tests.addTestSuite( NewTest.class );
		tests.addTestSuite( UpdateTest.class );
		tests.addTest( RecurrenceTestSuite.suite() );
		
		return tests;
	}
}
