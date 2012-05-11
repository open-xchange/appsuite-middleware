package com.openexchange.http.client.builder;

import java.io.InputStream;
import java.io.Reader;

public interface HTTPPutRequestBuilder<R> extends HTTPGenericRequestBuilder<HTTPPutRequestBuilder<R>, R> {
	public HTTPPutRequestBuilder<R> body(String body);
	public HTTPPutRequestBuilder<R> body(InputStream body);
	
	public HTTPPutRequestBuilder<R> contentType(String ctype);
}
