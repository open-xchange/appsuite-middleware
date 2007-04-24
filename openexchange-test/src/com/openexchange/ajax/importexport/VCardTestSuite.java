package com.openexchange.ajax.importexport;


import junit.framework.Test;
import junit.framework.TestSuite;

public class VCardTestSuite extends TestSuite{
	
	public static Test suite(){
		TestSuite tests = new TestSuite();
		tests.addTestSuite( VCardImportTest.class );
		tests.addTestSuite( VCardExportTest.class );

		return tests;
	}
}
