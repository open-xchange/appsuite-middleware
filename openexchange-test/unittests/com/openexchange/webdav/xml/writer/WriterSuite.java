package com.openexchange.webdav.xml.writer;

import junit.framework.Test;
import junit.framework.TestSuite;

public class WriterSuite {
	public static Test suite(){
		final TestSuite tests = new TestSuite();
		tests.addTestSuite(LockWriterTest.class);
		tests.addTestSuite(PropertiesWriterTest.class);
		return tests;
	}
}
