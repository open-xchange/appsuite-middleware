package com.openexchange.test;

public class TestException extends Exception
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3793876245680533405L;

	public TestException() {
		super();
	}
	
	public TestException(final String message) {
		super(message);
	}
	
	public TestException(final String message, final Exception exc) {
		super(message, exc);
	}
	
	public TestException(final Exception exc) {
		super(exc);
	}
	
}
