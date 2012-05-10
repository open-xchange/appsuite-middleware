package com.openexchange.http.client.builder;

import java.io.File;
import java.io.InputStream;

import com.openexchange.exception.OXException;

public interface HTTPMulitpartPostRequestBuilder<R> extends
		HTTPGenericRequestBuilder<HTTPMulitpartPostRequestBuilder<R>, R> {
	
	public HTTPMulitpartPostRequestBuilder<R> part(String fieldName, File file) throws OXException;

	public HTTPMulitpartPostRequestBuilder<R> part(String fieldName, InputStream is, String contentType, String filename) throws OXException;

	public HTTPMulitpartPostRequestBuilder<R> part(String fieldName, InputStream is, String contentType) throws OXException;
	
	public HTTPMulitpartPostRequestBuilder<R> part(String fieldName, String s, String contentType, String filename) throws OXException;

	public HTTPMulitpartPostRequestBuilder<R> part(String fieldName, String s, String contentType) throws OXException;

	
}
