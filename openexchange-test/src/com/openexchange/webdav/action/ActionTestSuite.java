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
		tests.addTestSuite(MoveTest.class);
		tests.addTestSuite(CopyTest.class);
		tests.addTestSuite(MkcolTest.class);
		tests.addTestSuite(PropfindTest.class);
		tests.addTestSuite(ProppatchTest.class);
		tests.addTestSuite(LockTest.class);
		tests.addTestSuite(UnlockTest.class);
		tests.addTestSuite(IfMatchTest.class);
		tests.addTestSuite(IfTest.class);
		tests.addTestSuite(DefaultHeaderTest.class);
		tests.addTestSuite(NotExistTest.class);
		tests.addTestSuite(MaxUploadSizeActionTest.class);
		return tests;
	}
}
