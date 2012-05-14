package com.openexchange.http.client.builder;

public interface HTTPRequestBuilder {
	
	public HTTPPutRequestBuilder put();
	
	public HTTPPostRequestBuilder post();
	
	public HTTPMulitpartPostRequestBuilder multipartPost();
	
	public HTTPGetRequestBuilder get();
	
	public HTTPDeleteRequestBuilder delete();
	
}
