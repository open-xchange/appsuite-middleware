package com.openexchange.http.client.builder;

public interface HTTPRequestBuilder {
	
	public HTTPPutRequestBuilder put();
	
	public HTTPPostRequestBuilder post();
	
	public HTTPMultipartPostRequestBuilder multipartPost();
	
	public HTTPGetRequestBuilder get();
	
	public HTTPDeleteRequestBuilder delete();
	
}
