package com.openexchange.http.client.builder;

import java.io.InputStream;
import java.io.Reader;

import com.openexchange.exception.OXException;

public interface HTTPPutRequestBuilder extends HTTPGenericRequestBuilder<HTTPPutRequestBuilder> {
	public HTTPPutRequestBuilder body(String body) throws OXException;
	public HTTPPutRequestBuilder body(InputStream body) throws OXException;
	
	public HTTPPutRequestBuilder contentType(String ctype);
}
