package com.openexchange.oauth.httpclient;

import java.util.Map;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;

import com.openexchange.exception.OXException;
import com.openexchange.http.client.builder.HTTPRequest;
import com.openexchange.http.client.builder.HTTPResponse;

public class OAuthHTTPRequest implements HTTPRequest {

	private final OAuthRequest delegate;
	private final Map<String, String> parameters;

	public OAuthHTTPRequest(OAuthRequest req, Map<String, String> parameters) {
		delegate = req;
		this.parameters = parameters;
	}

	@Override
	public HTTPResponse execute() throws OXException {
		Response response = delegate.send();
		return new HttpOauthResponse(response);
	}

	@Override
	public Map<String, String> getParameters() {
		return parameters;
	}

	@Override
	public Map<String, String> getHeaders() {
		return delegate.getHeaders();
	}
}
