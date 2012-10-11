package com.openexchange.oauth.httpclient.impl.scribe;


import org.scribe.model.Verb;

import com.openexchange.http.client.builder.HTTPGetRequestBuilder;
import com.openexchange.oauth.httpclient.OAuthHTTPRequestBuilder;

public class ScribeHTTPGetRequestBuilder extends ScribeGenericHTTPRequestBuilder<HTTPGetRequestBuilder> implements HTTPGetRequestBuilder {

	public ScribeHTTPGetRequestBuilder(OAuthHTTPRequestBuilder coreBuilder) {
		super(coreBuilder);
	}
	
	@Override
    public Verb getVerb(){
		return Verb.GET;
	}
}