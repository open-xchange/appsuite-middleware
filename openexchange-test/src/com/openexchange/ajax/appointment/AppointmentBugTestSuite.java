package com.openexchange.ajax.appointment;


import com.openexchange.ajax.appointment.recurrence.RecurrenceTestSuite;
import junit.framework.Test;
import junit.framework.TestSuite;

public class AppointmentBugTestSuite extends TestSuite{
	
	public static Test suite(){
		TestSuite tests = new TestSuite();
		tests.addTestSuite( Bug4392Test.class );
		tests.addTestSuite( Bug4541Test.class );
		tests.addTestSuite( Bug6055Test.class );
		tests.addTestSuite( Bug8317Test.class );
		tests.addTestSuite( Bug8836Test.class );
		return tests;
	}
}
