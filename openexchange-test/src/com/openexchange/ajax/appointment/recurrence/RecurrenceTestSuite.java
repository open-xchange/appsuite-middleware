package com.openexchange.ajax.appointment.recurrence;


import junit.framework.Test;
import junit.framework.TestSuite;

public class RecurrenceTestSuite extends TestSuite{
	
	public static Test suite(){
		TestSuite tests = new TestSuite();
		tests.addTestSuite( DailyRecurrenceTest.class );
		tests.addTestSuite( WeeklyRecurrenceTest.class );
		tests.addTestSuite( Bug9497Test.class );
		return tests;
	}
}