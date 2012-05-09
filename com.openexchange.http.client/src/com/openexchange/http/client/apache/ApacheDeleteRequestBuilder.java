package com.openexchange.http.client.apache;


import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.methods.DeleteMethod;

import com.openexchange.http.client.builder.HTTPDeleteRequestBuilder;

public class ApacheDeleteRequestBuilder<R> extends CommonApacheHTTPRequest<HTTPDeleteRequestBuilder<R>, R> implements
		HTTPDeleteRequestBuilder<R> {

	public ApacheDeleteRequestBuilder(ApacheClientRequestBuilder<R> coreBuilder) {
		super(coreBuilder);
	}

	@Override
	protected HttpMethodBase createMethod(String encodedSite) {
		return new DeleteMethod(encodedSite);
	}

	
}
