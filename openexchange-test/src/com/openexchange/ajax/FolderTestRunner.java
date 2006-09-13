package com.openexchange.ajax;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;


public class FolderTestRunner {


	public static Test createFolderTestSuite() {
		TestSuite testSuite = new TestSuite();
		testSuite.addTest(new FolderTest("testSharedFolder"));
		return testSuite;
	}
	
	public static void main(String[] args) {
		TestRunner.run(createFolderTestSuite());
	}
}
