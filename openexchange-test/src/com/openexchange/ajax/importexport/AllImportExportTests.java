package com.openexchange.ajax.importexport;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllImportExportTests extends TestSuite{

	public static Test suite(){
		final TestSuite tests = new TestSuite();
		tests.addTest( VCardTestSuite.suite() );
		tests.addTest( ICalTestSuite.suite() );
		tests.addTestSuite( CSVImportExportServletTest.class );
		return tests;
	}

}
