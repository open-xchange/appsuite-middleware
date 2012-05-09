package com.openexchange.http.client.internal;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;

import com.openexchange.groupware.AbstractOXException;
import com.openexchange.http.client.builder.HTTPMulitpartPostRequestBuilder;
import com.openexchange.http.client.builder.HTTPMulitpartPostRequestBuilder;
import com.openexchange.http.client.builder.HTTPResponseProcessor;

public class ChainedHTTPMulitpartPostRequestBuilder<O, R> extends ChainedGenericRequestBuilder<ChainedHTTPMulitpartPostRequestBuilder<O, R>, HTTPMulitpartPostRequestBuilder<O>, O, R>  implements
HTTPMulitpartPostRequestBuilder<R> {


	public ChainedHTTPMulitpartPostRequestBuilder(
			HTTPMulitpartPostRequestBuilder<O> put,
			HTTPResponseProcessor<O, R> processor) {
		super(put, processor);
	}

	public HTTPMulitpartPostRequestBuilder<R> part(String fieldName, File file) throws AbstractOXException {
		delegate.part(fieldName, file);
		return this;
	}

	public HTTPMulitpartPostRequestBuilder<R> part(String fieldName,
			InputStream is, String contentType, String filename) throws AbstractOXException {
		delegate.part(fieldName, is, contentType, filename);
		return this;
	}

	public HTTPMulitpartPostRequestBuilder<R> part(String fieldName,
			InputStream is, String contentType) throws AbstractOXException {
		delegate.part(fieldName, is, contentType);
		return this;
	}

	public HTTPMulitpartPostRequestBuilder<R> part(String fieldName, String s,
			String contentType, String filename) throws AbstractOXException {
		delegate.part(fieldName, s, contentType, filename);
		return this;
	}

	public HTTPMulitpartPostRequestBuilder<R> part(String fieldName, String s,
			String contentType) throws AbstractOXException {
		delegate.part(fieldName, s, contentType);
		return this;
	}

	

}
