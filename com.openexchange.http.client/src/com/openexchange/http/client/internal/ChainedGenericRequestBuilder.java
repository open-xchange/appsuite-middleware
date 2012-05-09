package com.openexchange.http.client.internal;

import java.util.Map;


import com.openexchange.groupware.AbstractOXException;
import com.openexchange.http.client.builder.HTTPGenericRequestBuilder;
import com.openexchange.http.client.builder.HTTPRequest;
import com.openexchange.http.client.builder.HTTPResponseProcessor;

public abstract class ChainedGenericRequestBuilder<T extends ChainedGenericRequestBuilder<T, M, O, R>, M extends HTTPGenericRequestBuilder<M, O>, O, R>  {
	
	protected M delegate;
	protected HTTPResponseProcessor<O, R> processor;

	public ChainedGenericRequestBuilder(M put,
			HTTPResponseProcessor<O, R> processor) {
		this.delegate = put;
		this.processor = processor;
	}

	public T url(String url) {
		delegate.url(url);
		return (T) this;	
	}
	
	public T verbatimURL(String url) {
		delegate.verbatimURL(url);
		return (T) this;	
	}


	public T parameter(String parameter, String value) {
		delegate.parameter(parameter, value);
		return (T) this;
	}

	public T parameters(Map<String, String> parameters) {
		delegate.parameters(parameters);
		return (T) this;
	}

	public T header(String header, String value) {
		delegate.header(header, value);
		return (T) this;
	}

	public T headers(Map<String, String> headers) {
		delegate.headers(headers);
		return (T) this;
	}

	public HTTPRequest<R> build() throws AbstractOXException {
		return new ChainedRequest<O, R>(delegate.build(), processor);
	}

}
