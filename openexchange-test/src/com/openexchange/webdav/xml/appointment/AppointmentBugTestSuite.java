package com.openexchange.webdav.xml.appointment;


import junit.framework.Test;
import junit.framework.TestSuite;

public class AppointmentBugTestSuite extends TestSuite{
	
	public static Test suite(){
		TestSuite tests = new TestSuite();
		tests.addTestSuite( Bug4395Test.class );
		tests.addTestSuite( Bug5933Test.class );
                tests.addTestSuite( Bug6056Test.class );
		return tests;
	}
}