package com.openexchange.http.client.apache;

import java.util.Map;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.methods.PostMethod;
import com.openexchange.http.client.builder.HTTPPostRequestBuilder;

public class ApachePostRequestBuilder extends CommonApacheHTTPRequest<HTTPPostRequestBuilder>implements HTTPPostRequestBuilder {

	public ApachePostRequestBuilder(final ApacheClientRequestBuilder coreBuilder) {
		super(coreBuilder);
	}

	@Override
	protected HttpMethodBase createMethod(final String encodedSite) {
		return new PostMethod(encodedSite);
	}

	@Override
	protected void addParams(final HttpMethodBase m, final String qString) {
		final PostMethod pm = (PostMethod) m;

		for(final Map.Entry<String, String> entry: parameters.entrySet()) {
			pm.setParameter(entry.getKey(), entry.getValue());
		}
	}


}
