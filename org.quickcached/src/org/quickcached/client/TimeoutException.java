package org.quickcached.client;

/**
 *
 * @author akshath
 */
public class TimeoutException extends Exception {
	public TimeoutException() {
		super();
	}
	
	public TimeoutException(String name) {
		super(name);
	}
}
