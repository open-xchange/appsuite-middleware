package com.openexchange.ajax.reminder;


import junit.framework.Test;
import junit.framework.TestSuite;

public class ReminderBugTestSuite extends TestSuite{
	
	public static Test suite(){
		TestSuite tests = new TestSuite();
		tests.addTestSuite( Bug4342Test.class );
		tests.addTestSuite( Bug5128Test.class );
		tests.addTestSuite (Bug6408Test.class );
		tests.addTestSuite (Bug7590Test.class );

		return tests;
	}
}