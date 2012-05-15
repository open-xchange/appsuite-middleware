package com.openexchange.oauth.httpclient;

import java.util.Map;
import java.util.TreeMap;

import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;

import com.openexchange.exception.OXException;
import com.openexchange.http.client.builder.HTTPRequest;
import com.openexchange.http.client.builder.HTTPResponse;

public class OAuthHTTPRequest implements HTTPRequest {

	private OAuthRequest delegate;

	public OAuthHTTPRequest(OAuthRequest req) {
		delegate = req;
	}

	@Override
	public HTTPResponse execute() throws OXException {
		Response response = delegate.send();
		return new HttpOauthResponse(response);
	}

	@Override
	public Map<String, String> getParameters() {
		Map<String, String> parameters = new TreeMap<String, String>();
		parameters.putAll(delegate.getQueryStringParams());
		parameters.putAll(delegate.getBodyParams());
		return parameters;
	}

	@Override
	public Map<String, String> getHeaders() {
		return delegate.getHeaders();
	}
}
