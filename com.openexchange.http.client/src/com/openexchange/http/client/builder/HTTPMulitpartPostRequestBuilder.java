package com.openexchange.http.client.builder;

import java.io.File;
import java.io.InputStream;

import com.openexchange.exception.OXException;

public interface HTTPMulitpartPostRequestBuilder extends
		HTTPGenericRequestBuilder<HTTPMulitpartPostRequestBuilder> {
	
	public HTTPMulitpartPostRequestBuilder part(String fieldName, File file) throws OXException;

	public HTTPMulitpartPostRequestBuilder part(String fieldName, InputStream is, String contentType, String filename) throws OXException;

	public HTTPMulitpartPostRequestBuilder part(String fieldName, InputStream is, String contentType) throws OXException;
	
	public HTTPMulitpartPostRequestBuilder part(String fieldName, String s, String contentType, String filename) throws OXException;

	public HTTPMulitpartPostRequestBuilder part(String fieldName, String s, String contentType) throws OXException;

	
}
