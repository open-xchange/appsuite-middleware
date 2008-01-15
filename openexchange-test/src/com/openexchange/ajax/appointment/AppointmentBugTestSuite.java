package com.openexchange.ajax.appointment;


import junit.framework.Test;
import junit.framework.TestSuite;

import com.openexchange.ajax.appointment.recurrence.Bug10760Test;

public class AppointmentBugTestSuite extends TestSuite{
	
	public static Test suite(){
		TestSuite tests = new TestSuite();
		tests.addTestSuite( Bug4392Test.class );
		tests.addTestSuite( Bug4541Test.class );
		tests.addTestSuite( Bug6055Test.class );
		tests.addTestSuite( Bug8317Test.class );
		tests.addTestSuite( Bug8836Test.class );
		tests.addTestSuite( Bug8724Test.class );
		tests.addTestSuite( Bug9089Test.class );
		tests.addTestSuite( Bug10760Test.class );
		return tests;
	}
}
