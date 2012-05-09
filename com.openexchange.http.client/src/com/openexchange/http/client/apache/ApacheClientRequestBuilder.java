package com.openexchange.http.client.apache;

import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpState;

import com.openexchange.filemanagement.ManagedFileManagement;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.http.client.builder.HTTPDeleteRequestBuilder;
import com.openexchange.http.client.builder.HTTPGetRequestBuilder;
import com.openexchange.http.client.builder.HTTPMulitpartPostRequestBuilder;
import com.openexchange.http.client.builder.HTTPPostRequestBuilder;
import com.openexchange.http.client.builder.HTTPPutRequestBuilder;
import com.openexchange.http.client.builder.HTTPRequestBuilder;
import com.openexchange.http.client.builder.HTTPResponseProcessor;
import com.openexchange.http.client.builder.HTTPStrategy;
import com.openexchange.http.client.internal.AbstractBuilder;
import com.openexchange.http.client.internal.ChainedBuilder;

public abstract class ApacheClientRequestBuilder<R> extends AbstractBuilder<R> implements
		HTTPRequestBuilder<R> {
	
	private HttpState state;
	
	private ManagedFileManagement fileManager;
	
	public ApacheClientRequestBuilder(ManagedFileManagement mgmt) {
		fileManager = mgmt;
	}
	
	public HTTPPutRequestBuilder<R> put() {
		return new ApachePutRequestBuilder<R>(this);
	}
	
	public HTTPPostRequestBuilder<R> post() {
		return new ApachePostRequestBuilder<R>(this);
	}
	
	public HTTPMulitpartPostRequestBuilder<R> multipartPost() {
		return new ApacheMultipartPostRequestBuilder<R>(this, fileManager);
	}
	
	public HTTPGetRequestBuilder<R> get() {
		return new ApacheGetRequestBuilder<R>(this);
	}
	
	public HTTPDeleteRequestBuilder<R> delete() {
		return new ApacheDeleteRequestBuilder<R>(this);
	}

	abstract R extractPayload(HttpMethodBase method) throws AbstractOXException;

	HttpState getState() {
		return state;
	}
	
	void setState(HttpState state) {
		this.state = state;
	}
	
	

}
