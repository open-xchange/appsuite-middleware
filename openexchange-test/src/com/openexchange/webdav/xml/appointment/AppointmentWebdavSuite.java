package com.openexchange.webdav.xml.appointment;


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
		
		return tests;
	}
}
