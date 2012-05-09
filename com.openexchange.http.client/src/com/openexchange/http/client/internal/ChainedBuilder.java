package com.openexchange.http.client.internal;

import com.openexchange.http.client.builder.HTTPDeleteRequestBuilder;
import com.openexchange.http.client.builder.HTTPGetRequestBuilder;
import com.openexchange.http.client.builder.HTTPMulitpartPostRequestBuilder;
import com.openexchange.http.client.builder.HTTPPostRequestBuilder;
import com.openexchange.http.client.builder.HTTPPutRequestBuilder;
import com.openexchange.http.client.builder.HTTPRequestBuilder;
import com.openexchange.http.client.builder.HTTPResponseProcessor;
import com.openexchange.http.client.builder.HTTPStrategy;

public class ChainedBuilder<O, R> extends AbstractBuilder<R> {
	
	private HTTPRequestBuilder<O> wrappedBuilder;
	private HTTPResponseProcessor<O, R> processor;

	public ChainedBuilder(HTTPResponseProcessor<O, R> processor,
			HTTPRequestBuilder<O> wrappedBuilder) {

		this.wrappedBuilder = wrappedBuilder;
		this.processor = processor;
	}

	public HTTPPutRequestBuilder<R> put() {
		return new ChainedHTTPPutRequestBuilder<O, R>(wrappedBuilder.put(), processor);
	}

	public HTTPPostRequestBuilder<R> post() {
		return new ChainedHTTPPostRequestBuilder<O, R>(wrappedBuilder.post(), processor);
	}
	
	public HTTPMulitpartPostRequestBuilder<R> multipartPost() {
		return new ChainedHTTPMulitpartPostRequestBuilder<O, R>(wrappedBuilder.multipartPost(), processor);
	}
	
	public HTTPGetRequestBuilder<R> get() {
		return new ChainedHTTPGetRequestBuilder<O, R>(wrappedBuilder.get(), processor);
	}

	public HTTPDeleteRequestBuilder<R> delete() {
		return new ChainedHTTPDeleteRequestBuilder<O, R>(wrappedBuilder.delete(), processor);
	}


}
