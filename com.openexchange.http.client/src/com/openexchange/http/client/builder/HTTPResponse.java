package com.openexchange.http.client.builder;

import java.util.Map;

import com.openexchange.exception.OXException;

public interface HTTPResponse {
	public <R> R getPayload(Class<R> klass) throws OXException;
	
	public Map<String, String> getHeaders();
	public Map<String, String> getCookies();
}
