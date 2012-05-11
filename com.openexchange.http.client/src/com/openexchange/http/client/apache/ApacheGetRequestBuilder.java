package com.openexchange.http.client.apache;

import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.methods.GetMethod;

import com.openexchange.http.client.builder.HTTPGetRequestBuilder;

public class ApacheGetRequestBuilder<R> extends CommonApacheHTTPRequest<HTTPGetRequestBuilder<R>, R> implements HTTPGetRequestBuilder<R> {

	
	public ApacheGetRequestBuilder(ApacheClientRequestBuilder<R> coreBuilder) {
		super(coreBuilder);
	}

	protected HttpMethodBase createMethod(String site) {
		return new GetMethod(site);
	}

}
