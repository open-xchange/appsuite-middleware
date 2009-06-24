package com.openexchange.subscribe.crawler;

public class WorkflowException extends Exception {
	String message;
	public WorkflowException(String message) {
		super();
		this.message = message;
	}
	public String getMessage() {
		return message;
	}
}
