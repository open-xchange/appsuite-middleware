package com.openexchange.http.client.internal;

import com.openexchange.http.client.builder.HTTPGetRequestBuilder;
import com.openexchange.http.client.builder.HTTPResponseProcessor;

public class ChainedHTTPGetRequestBuilder<O, R> extends ChainedGenericRequestBuilder<ChainedHTTPGetRequestBuilder<O, R>, HTTPGetRequestBuilder<O>, O, R> implements HTTPGetRequestBuilder<R> {

	public ChainedHTTPGetRequestBuilder(HTTPGetRequestBuilder<O> put,
			HTTPResponseProcessor<O, R> processor) {
		super(put, processor);
	}

}
