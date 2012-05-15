package com.openexchange.oauth.httpclient;

import java.util.Map;

import org.scribe.model.Response;

import com.openexchange.exception.OXException;
import com.openexchange.http.client.builder.HTTPResponse;

public class HttpOauthResponse implements HTTPResponse {

	private Response delegate;

	HttpOauthResponse(Response oauthResponse){
		delegate = oauthResponse;
	}

	@Override
	public <R> R getPayload(Class<R> klass) throws OXException {
		return (R) delegate.getBody(); // TODO: Funky
	}

	@Override
	public Map<String, String> getCookies() {
		throw new UnsupportedOperationException("Implement me ;)");
		//return delegate.getHeaders(); //MAYBE?
	}

	@Override
	public Map<String, String> getHeaders() {
		return delegate.getHeaders();
	}

}
