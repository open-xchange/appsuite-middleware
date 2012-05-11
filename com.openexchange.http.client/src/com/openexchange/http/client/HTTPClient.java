package com.openexchange.http.client;

import com.openexchange.http.client.builder.HTTPRequestBuilder;

public interface HTTPClient {
	public <R> HTTPRequestBuilder<R> getBuilder(Class<R> responseType);
}
