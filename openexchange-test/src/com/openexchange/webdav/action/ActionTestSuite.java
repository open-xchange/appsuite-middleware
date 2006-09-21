package com.openexchange.webdav.action;

import junit.framework.Test;
import junit.framework.TestSuite;

public class ActionTestSuite {
	public static Test suite(){
		TestSuite tests = new TestSuite();
		tests.addTestSuite(GetTest.class);
		tests.addTestSuite(HeadTest.class);
		tests.addTestSuite(PutTest.class);
		tests.addTestSuite(DeleteTest.class);
		tests.addTestSuite(OptionsTest.class);
		tests.addTestSuite(TraceTest.class);
		
		return tests;
	}
}
