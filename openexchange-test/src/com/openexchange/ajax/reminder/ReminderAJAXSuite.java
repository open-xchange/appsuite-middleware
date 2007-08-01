package com.openexchange.ajax.reminder;


import junit.framework.Test;
import junit.framework.TestSuite;

public class ReminderAJAXSuite extends TestSuite{
	
	public static Test suite(){
		TestSuite tests = new TestSuite();
		tests.addTestSuite( RangeTest.class );
		tests.addTestSuite( UpdatesTest.class );
		tests.addTestSuite( DeleteTest.class );
		tests.addTestSuite( Bug4342Test.class );
		tests.addTestSuite( Bug5128Test.class );
		
		return tests;
	}
}
