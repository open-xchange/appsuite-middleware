package com.openexchange.http.client.builder;

import java.util.Map;

import com.openexchange.groupware.AbstractOXException;

public class HTTPResponseWrapper<R> implements HTTPResponse<R> {

	private HTTPResponse<?> delegate;
	private R payload;

	public HTTPResponseWrapper(HTTPResponse<?> delegate, R payload) {
		this.delegate = delegate;
		this.payload = payload;
	}
	
	public R getPayload() throws AbstractOXException {
		return payload;
	}

	public void setPayload(R payload) {
		this.payload = payload;
	}

	public Map<String, String> getHeaders() {
		return delegate.getHeaders();
	}

	public Map<String, String> getCookies() {
		return delegate.getCookies();
	}

}
