package com.openexchange.http.client.internal;

import java.io.InputStream;
import java.io.Reader;

import com.openexchange.http.client.builder.HTTPPutRequestBuilder;
import com.openexchange.http.client.builder.HTTPResponseProcessor;

public class ChainedHTTPPutRequestBuilder<O, R> extends ChainedGenericRequestBuilder<ChainedHTTPPutRequestBuilder<O, R>, HTTPPutRequestBuilder<O>, O, R> implements HTTPPutRequestBuilder<R> {


	public ChainedHTTPPutRequestBuilder(HTTPPutRequestBuilder<O> put,
			HTTPResponseProcessor<O, R> processor) {
		super(put, processor);
	}

	public HTTPPutRequestBuilder<R> body(String body) {
		delegate.body(body);
		return this;
	}

	public HTTPPutRequestBuilder<R> body(InputStream body) {
		delegate.body(body);
		return this;
	}

	public HTTPPutRequestBuilder<R> contentType(String ctype) {
		delegate.contentType(ctype);
		return this;
	}

}
