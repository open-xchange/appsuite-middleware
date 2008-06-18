package com.openexchange.webdav.xml.appointment;


import junit.framework.Test;
import junit.framework.TestSuite;

import com.openexchange.webdav.xml.appointment.recurrence.RecurrenceTestSuite;

public class AppointmentWebdavSuite extends TestSuite{
	
	public static Test suite(){
		final TestSuite tests = new TestSuite();
		tests.addTestSuite( ConfirmTest.class );
		tests.addTestSuite( DeleteTest.class );
		tests.addTestSuite( ListTest.class );
		tests.addTestSuite( NewTest.class );
		tests.addTestSuite( UpdateTest.class );
		tests.addTest( RecurrenceTestSuite.suite() );
		tests.addTest( AppointmentBugTestSuite.suite() );
		
		return tests;
	}
}
