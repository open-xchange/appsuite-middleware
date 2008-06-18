package com.openexchange.ajax.importexport;


import junit.framework.Test;
import junit.framework.TestSuite;

public class ICalTestSuite extends TestSuite{
	
	public static Test suite(){
		final TestSuite tests = new TestSuite();
		tests.addTestSuite( ICalImportTest.class );
		tests.addTestSuite( ICalExportTest.class );

		return tests;
	}
}
