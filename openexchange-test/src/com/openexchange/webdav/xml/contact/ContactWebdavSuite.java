package com.openexchange.webdav.xml.contact;


import junit.framework.Test;
import junit.framework.TestSuite;

public class ContactWebdavSuite extends TestSuite{
	
	public static Test suite(){
		TestSuite tests = new TestSuite();
		tests.addTestSuite( DeleteTest.class );
		tests.addTestSuite( ListTest.class );
		tests.addTestSuite( NewTest.class );
		tests.addTestSuite( UpdateTest.class );
		
		return tests;
	}
}
