package com.openexchange.http.client.apache;


import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.methods.DeleteMethod;

import com.openexchange.http.client.builder.HTTPDeleteRequestBuilder;

public class ApacheDeleteRequestBuilder extends CommonApacheHTTPRequest<HTTPDeleteRequestBuilder> implements
		HTTPDeleteRequestBuilder {

	public ApacheDeleteRequestBuilder(ApacheClientRequestBuilder coreBuilder) {
		super(coreBuilder);
	}

	@Override
	protected HttpMethodBase createMethod(String encodedSite) {
		return new DeleteMethod(encodedSite);
	}

	
}
