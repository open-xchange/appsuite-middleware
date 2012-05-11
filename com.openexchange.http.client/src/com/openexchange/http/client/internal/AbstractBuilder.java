package com.openexchange.http.client.internal;

import java.util.ArrayList;
import java.util.List;

import com.openexchange.http.client.builder.HTTPRequestBuilder;
import com.openexchange.http.client.builder.HTTPResponseProcessor;
import com.openexchange.http.client.builder.HTTPStrategy;

public abstract class AbstractBuilder<R> implements HTTPRequestBuilder<R> {
	
	protected List<HTTPStrategy<R>> strategies = new ArrayList<HTTPStrategy<R>>();
	
	public <T> HTTPRequestBuilder<T> chain(HTTPResponseProcessor<R, T> processor) {
		return new ChainedBuilder<R, T>(processor, this);
	}
	
	public HTTPRequestBuilder<R> addStrategy(HTTPStrategy<R> strategy) {
		strategies.add(strategy);
		return this;
	}
}
