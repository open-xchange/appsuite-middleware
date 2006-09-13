package com.openexchange.ajax;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;


public class FolderTestRunner {


	public static Test suite() {
		TestSuite testSuite = new TestSuite();
		testSuite.addTest(new FolderTest("testDeleteFolder"));
		testSuite.addTest(new FolderTest("testFailDeleteFolder"));
		return testSuite;
	}
	
	public static void main(String[] args) {
		TestRunner.run(suite());
	}
}
