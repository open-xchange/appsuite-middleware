package com.openexchange.oauth.httpclient.impl.scribe;

import org.scribe.model.Verb;

import com.openexchange.http.client.builder.HTTPDeleteRequestBuilder;
import com.openexchange.oauth.httpclient.OAuthHTTPRequestBuilder;

public class ScribeHTTPDeleteRequestBuilder extends ScribeGenericHTTPRequestBuilder<HTTPDeleteRequestBuilder>implements HTTPDeleteRequestBuilder {

	public ScribeHTTPDeleteRequestBuilder(OAuthHTTPRequestBuilder coreBuilder) {
		super(coreBuilder);
	}

	@Override
	public Verb getVerb() {
		return Verb.DELETE;
	}


}
