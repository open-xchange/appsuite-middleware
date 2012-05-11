package com.openexchange.http.client.builder;

public interface HTTPRequestBuilder<R> {
	
	public HTTPPutRequestBuilder<R> put();
	
	public HTTPPostRequestBuilder<R> post();
	
	public HTTPMulitpartPostRequestBuilder<R> multipartPost();
	
	public HTTPGetRequestBuilder<R> get();
	
	public HTTPDeleteRequestBuilder<R> delete();
	
	public <T> HTTPRequestBuilder<T> chain(HTTPResponseProcessor<R, T> processor);
	
	public HTTPRequestBuilder<R> addStrategy(HTTPStrategy<R> strategy);
}
