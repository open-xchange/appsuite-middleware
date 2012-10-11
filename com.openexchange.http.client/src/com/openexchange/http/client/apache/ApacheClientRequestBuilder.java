package com.openexchange.http.client.apache;

import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpState;

import com.openexchange.exception.OXException;
import com.openexchange.filemanagement.ManagedFileManagement;
import com.openexchange.http.client.builder.HTTPDeleteRequestBuilder;
import com.openexchange.http.client.builder.HTTPGetRequestBuilder;
import com.openexchange.http.client.builder.HTTPMultipartPostRequestBuilder;
import com.openexchange.http.client.builder.HTTPPostRequestBuilder;
import com.openexchange.http.client.builder.HTTPPutRequestBuilder;
import com.openexchange.http.client.builder.HTTPRequestBuilder;
import com.openexchange.http.client.internal.AbstractBuilder;

public class ApacheClientRequestBuilder extends AbstractBuilder implements
		HTTPRequestBuilder {
	
	private HttpState state;
	
	private ManagedFileManagement fileManager;

	private ApacheHTTPClient client;
	
	public ApacheClientRequestBuilder(ManagedFileManagement mgmt, ApacheHTTPClient client) {
		fileManager = mgmt;
		this.client = client;
	}
	
	public HTTPPutRequestBuilder put() {
		return new ApachePutRequestBuilder(this);
	}
	
	public HTTPPostRequestBuilder post() {
		return new ApachePostRequestBuilder(this);
	}
	
	public HTTPMultipartPostRequestBuilder multipartPost() {
		return new ApacheMultipartPostRequestBuilder(this, fileManager);
	}
	
	public HTTPGetRequestBuilder get() {
		return new ApacheGetRequestBuilder(this);
	}
	
	public HTTPDeleteRequestBuilder delete() {
		return new ApacheDeleteRequestBuilder(this);
	}

	public <R> R extractPayload(HttpMethodBase method, Class<R> type) throws OXException {
		return client.extractPayload(method, type);
	}

	HttpState getState() {
		return state;
	}
	
	void setState(HttpState state) {
		this.state = state;
	}
	
	

}
