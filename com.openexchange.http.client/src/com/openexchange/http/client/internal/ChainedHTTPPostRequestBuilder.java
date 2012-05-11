package com.openexchange.http.client.internal;

import com.openexchange.http.client.builder.HTTPPostRequestBuilder;
import com.openexchange.http.client.builder.HTTPResponseProcessor;

public class ChainedHTTPPostRequestBuilder<O, R> extends ChainedGenericRequestBuilder<ChainedHTTPPostRequestBuilder<O, R>, HTTPPostRequestBuilder<O>, O, R>  implements
		HTTPPostRequestBuilder<R> {

	public ChainedHTTPPostRequestBuilder(HTTPPostRequestBuilder<O> put,
			HTTPResponseProcessor<O, R> processor) {
		super(put, processor);
	}

}
