package com.openexchange.test;

public class TestException extends Exception
{

	public TestException() {
		super();
	}
	
	public TestException(String message) {
		super(message);
	}
	
	public TestException(String message, Exception exc) {
		super(message, exc);
	}
	
	public TestException(Exception exc) {
		super(exc);
	}
	
}
