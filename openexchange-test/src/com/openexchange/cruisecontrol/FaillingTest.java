package com.openexchange.cruisecontrol;

import junit.framework.TestCase;

public class FaillingTest extends TestCase {
	public void testFailure(){
		assertTrue(false);
	}
	
	public void testError() throws Exception{
		throw new Exception("Arrr!");
	}
}
