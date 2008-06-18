package com.openexchange.ajax;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;


public class FolderTestRunner {


	public static Test createFolderTestSuite() {
		final TestSuite testSuite = new TestSuite();
		testSuite.addTest(new FolderTest("testGetUserId"));
		return testSuite;
	}
	
	public static void main(final String[] args) {
		TestRunner.run(createFolderTestSuite());
	}
}
