package com.openexchange.http.client.builder;

import java.util.Map;

import com.openexchange.exception.OXException;

public interface HTTPGenericRequestBuilder<T extends HTTPGenericRequestBuilder<T>> {
	public T url(String url);
	public T verbatimURL(String url);
	
	public T parameter(String parameter, String value);
	public T parameters(Map<String, String> parameters);
		
	public T header(String header, String value);
	public T headers(Map<String, String> cookies);
	
	public HTTPRequest build() throws OXException;
}
