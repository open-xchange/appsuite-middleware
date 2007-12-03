package com.openexchange.webdav.protocol;

import junit.framework.Test;
import junit.framework.TestSuite;

public class ProtocolTestSuite extends TestSuite {

	public static Test suite(){
		TestSuite tests = new TestSuite();
		tests.addTestSuite(ResourceTest.class);
		tests.addTestSuite(CollectionTest.class);
		tests.addTestSuite(LockTest.class);
		tests.addTestSuite(LockInteractionTest.class);
		return tests;
	}

}
