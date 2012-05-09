package com.openexchange.http.client.builder;

import java.util.Map;

import com.openexchange.groupware.AbstractOXException;

public interface HTTPRequest<R> {
	public HTTPResponse<R> execute() throws AbstractOXException;
	
	public Map<String, String> getHeaders();
	public Map<String, String> getParameters();
	
}
