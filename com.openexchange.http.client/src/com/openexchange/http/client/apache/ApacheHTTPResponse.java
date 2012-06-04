package com.openexchange.http.client.apache;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;

import com.openexchange.exception.OXException;
import com.openexchange.http.client.builder.HTTPResponse;

public class ApacheHTTPResponse implements HTTPResponse {

	private HttpMethodBase method;
	private HttpClient client;
	private ApacheClientRequestBuilder coreBuilder;
	private Object payload;

	public ApacheHTTPResponse(HttpMethodBase method, HttpClient client,
			ApacheClientRequestBuilder coreBuilder) {
		this.method = method;
		this.client = client;
		this.coreBuilder = coreBuilder;
	}

	public <R> R getPayload(Class<R> type) throws OXException {
		if (payload != null && type.isInstance(payload)) {
			return (R) payload;
		}
		return coreBuilder.extractPayload(method, type);
	}


	public Map<String, String> getHeaders() {
		Header[] responseHeaders = method.getResponseHeaders();
		Map<String, String> headers = new HashMap<String, String>();
		for (Header header : responseHeaders) {
			headers.put(header.getName(), header.getValue());
		}
		return headers;
	}

	public Map<String, String> getCookies() {
		Cookie[] cookies = client.getState().getCookies();
		Map<String, String> r = new HashMap<String, String>();
		for (Cookie cookie : cookies) {
			r.put(cookie.getName(), cookie.getValue());
		}
		return r;
	}

}
