package com.openexchange.http.client.builder;

import java.io.InputStream;
import java.io.Reader;

public interface HTTPPutRequestBuilder extends HTTPGenericRequestBuilder<HTTPPutRequestBuilder> {
	public HTTPPutRequestBuilder body(String body);
	public HTTPPutRequestBuilder body(InputStream body);
	
	public HTTPPutRequestBuilder contentType(String ctype);
}
