package com.openexchange.ajax.contact;

import junit.framework.Test;
import junit.framework.TestSuite;

public class ContactBugTestSuite extends TestSuite{
	
	public static Test suite(){
		TestSuite tests = new TestSuite();
		tests.addTestSuite( Bug4409Test.class );
		return tests;
	}
}
