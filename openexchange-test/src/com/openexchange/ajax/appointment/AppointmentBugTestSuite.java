package com.openexchange.ajax.appointment;


import junit.framework.Test;
import junit.framework.TestSuite;

public class AppointmentBugTestSuite extends TestSuite{
	
	public static Test suite(){
		TestSuite tests = new TestSuite();
		tests.addTestSuite( Bug4392Test.class );
		tests.addTestSuite( Bug4344Test.class );
		
		return tests;
	}
}