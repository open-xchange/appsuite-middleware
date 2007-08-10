package com.openexchange.ajax.contact;


import junit.framework.Test;
import junit.framework.TestSuite;

public class ContactAJAXSuite extends TestSuite {
	
	public static Test suite(){
		TestSuite tests = new TestSuite();
		tests.addTestSuite( AllTest.class );
		tests.addTestSuite( CopyTest.class );
		tests.addTestSuite( DeleteTest.class );
		tests.addTestSuite( ListTest.class );
		tests.addTestSuite( MoveTest.class );
		tests.addTestSuite( NewTest.class );
		tests.addTestSuite( SearchTest.class );
		tests.addTestSuite( UpdateTest.class );
		tests.addTestSuite( UpdatesTest.class );
		tests.addTestSuite (MultipleTest.class );
		tests.addTest(ContactBugTestSuite.suite());
		
		return tests;
	}
}
