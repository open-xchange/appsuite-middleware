package com.openexchange.ajax.appointment;


import junit.framework.Test;
import junit.framework.TestSuite;

import com.openexchange.ajax.appointment.recurrence.RecurrenceTestSuite;

public class AppointmentAJAXSuite extends TestSuite{
	
	public static Test suite(){
		TestSuite tests = new TestSuite();
		tests.addTestSuite( AllTest.class );
		tests.addTestSuite( ConfirmTest.class );
		tests.addTestSuite( CopyTest.class );
		tests.addTestSuite( DeleteTest.class );
		tests.addTestSuite( FreeBusyTest.class );
		tests.addTestSuite( HasTest.class );
		tests.addTestSuite( ListTest.class );
		tests.addTestSuite( MoveTest.class );
		tests.addTestSuite( NewTest.class );
		tests.addTestSuite( SearchTest.class );
		tests.addTestSuite( UpdateTest.class );
		tests.addTestSuite( UpdatesTest.class );
		tests.addTestSuite( ConflictTest.class );
		tests.addTestSuite( MultipleTest.class );
		tests.addTestSuite( PortalSearchTest.class );
		tests.addTestSuite(FunambolTest.class);
		tests.addTestSuite(Bug10836Test.class);
		tests.addTestSuite(NewListTest.class);
		
		tests.addTest( RecurrenceTestSuite.suite() );
		tests.addTest( AppointmentBugTestSuite.suite() );
		
		
		return tests;
	}
}
