package com.openexchange.http.client.internal;

import com.openexchange.http.client.builder.HTTPDeleteRequestBuilder;
import com.openexchange.http.client.builder.HTTPResponseProcessor;

public class ChainedHTTPDeleteRequestBuilder<O, R> extends ChainedGenericRequestBuilder<ChainedHTTPDeleteRequestBuilder<O, R>, HTTPDeleteRequestBuilder<O>, O, R> implements HTTPDeleteRequestBuilder<R> {

	public ChainedHTTPDeleteRequestBuilder(HTTPDeleteRequestBuilder<O> put,
			HTTPResponseProcessor<O, R> processor) {
		super(put, processor);
	}

}

