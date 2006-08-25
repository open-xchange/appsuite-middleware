package com.openexchange.ajax.infostore;


import junit.framework.Test;
import junit.framework.TestSuite;

public class InfostoreAJAXSuite extends TestSuite{
	public static Test suite(){
		
		TestSuite tests = new TestSuite();
		tests.addTestSuite( AllTest.class );
		tests.addTestSuite( DeleteTest.class );
		tests.addTestSuite( GetTest.class );
		tests.addTestSuite( ListTest.class );
		tests.addTestSuite( NewTest.class );
		tests.addTestSuite( UpdatesTest.class );
		tests.addTestSuite( UpdateTest.class );
		tests.addTestSuite( VersionsTest.class );
		tests.addTestSuite( DetachTest.class );
		tests.addTestSuite( DocumentTest.class );
		tests.addTestSuite( CopyTest.class );
		
		
		return tests;
	}
}
