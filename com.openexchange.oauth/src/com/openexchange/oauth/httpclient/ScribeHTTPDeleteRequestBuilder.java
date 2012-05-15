package com.openexchange.oauth.httpclient;

import org.scribe.model.Verb;

import com.openexchange.http.client.builder.HTTPDeleteRequestBuilder;
import com.openexchange.oauth.httpclient.impl.scribe.ScribeGenericHTTPRequestBuilder;

public class ScribeHTTPDeleteRequestBuilder extends ScribeGenericHTTPRequestBuilder<HTTPDeleteRequestBuilder>implements HTTPDeleteRequestBuilder {

	public ScribeHTTPDeleteRequestBuilder(OAuthHTTPRequestBuilder coreBuilder) {
		super(coreBuilder);
	}

	@Override
	public Verb getVerb() {
		return Verb.DELETE;
	}


}
