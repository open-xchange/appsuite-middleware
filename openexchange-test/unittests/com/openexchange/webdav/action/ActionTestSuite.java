package com.openexchange.webdav.action;

import com.openexchange.webdav.action.ifheader.IgnoreLocksIfHeaderApplyTest;
import com.openexchange.webdav.action.ifheader.StandardIfHeaderApplyTest;
import junit.framework.Test;
import junit.framework.TestSuite;

public class ActionTestSuite {
	public static Test suite(){
		final TestSuite tests = new TestSuite();
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

        tests.addTestSuite(StandardIfHeaderApplyTest.class);
		tests.addTestSuite(IgnoreLocksIfHeaderApplyTest.class);
        tests.addTestSuite(Bug33505Test.class);
        tests.addTestSuite(Bug34283Test.class);
        tests.addTestSuite(Bug49057Test.class);
		return tests;
	}
}
