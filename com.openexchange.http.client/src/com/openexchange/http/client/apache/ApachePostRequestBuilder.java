package com.openexchange.http.client.apache;

import java.util.Map;

import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.methods.PostMethod;

import com.openexchange.http.client.builder.HTTPPostRequestBuilder;
import com.openexchange.http.client.builder.HTTPRequest;

public class ApachePostRequestBuilder<R> extends CommonApacheHTTPRequest<HTTPPostRequestBuilder<R>, R>implements HTTPPostRequestBuilder<R> {

	public ApachePostRequestBuilder(ApacheClientRequestBuilder<R> coreBuilder) {
		super(coreBuilder);
	}

	@Override
	protected HttpMethodBase createMethod(String encodedSite) {
		return new PostMethod(encodedSite);
	}
	
	@Override
	protected void addParams(HttpMethodBase m, String qString) {
		PostMethod pm = (PostMethod) m;
		
		for(Map.Entry<String, String> entry: parameters.entrySet()) {
			pm.setParameter(entry.getKey(), entry.getValue());
		}
	}

	
}
