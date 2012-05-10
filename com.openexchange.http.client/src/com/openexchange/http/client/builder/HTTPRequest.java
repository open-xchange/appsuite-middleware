package com.openexchange.http.client.builder;

import java.util.Map;

import com.openexchange.exception.OXException;

public interface HTTPRequest<R> {
	public HTTPResponse<R> execute() throws OXException;
	
	public Map<String, String> getHeaders();
	public Map<String, String> getParameters();
	
}
