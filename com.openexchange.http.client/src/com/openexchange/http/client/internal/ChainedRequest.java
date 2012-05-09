package com.openexchange.http.client.internal;

import java.util.Map;

import com.openexchange.groupware.AbstractOXException;
import com.openexchange.http.client.builder.HTTPRequest;
import com.openexchange.http.client.builder.HTTPResponse;
import com.openexchange.http.client.builder.HTTPResponseProcessor;

public class ChainedRequest<O, R> implements HTTPRequest<R> {

	private HTTPRequest<O> delegate;
	private HTTPResponseProcessor<O, R> processor;

	public ChainedRequest(HTTPRequest<O> request,
			HTTPResponseProcessor<O, R> processor) {
		this.delegate = request;
		this.processor = processor;
	}

	public HTTPResponse<R> execute() throws AbstractOXException {
		HTTPResponse<O> response = delegate.execute();
		return processor.process(response);
	}

	public Map<String, String> getHeaders() {
		return delegate.getHeaders();
	}

	public Map<String, String> getParameters() {
		return delegate.getParameters();
	}

}
