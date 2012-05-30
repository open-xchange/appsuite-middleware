package com.openexchange.oauth.httpclient.impl.scribe;

import java.util.Map;

import org.scribe.model.OAuthRequest;
import org.scribe.model.Verb;

import com.openexchange.http.client.builder.HTTPPostRequestBuilder;
import com.openexchange.oauth.httpclient.OAuthHTTPRequestBuilder;

public class ScribeHTTPPostRequestBuilder extends ScribeGenericHTTPRequestBuilder<HTTPPostRequestBuilder> implements HTTPPostRequestBuilder {

	public ScribeHTTPPostRequestBuilder(OAuthHTTPRequestBuilder coreBuilder) {
		super(coreBuilder);
	}

	@Override
	public Verb getVerb() {
		return Verb.POST;
	}
	
	@Override
	protected void setParameters(Map<String, String> params,
			OAuthRequest request) {
		for(Map.Entry<String, String> param: params.entrySet()) {
			request.addBodyParameter(param.getKey(), param.getValue());
		}
	}

}
