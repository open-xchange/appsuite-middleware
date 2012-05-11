package com.openexchange.http.client.apache;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.ObjectOutputStream.PutField;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;

import com.openexchange.http.client.builder.HTTPPutRequestBuilder;
import com.openexchange.http.client.builder.HTTPRequest;

public class ApachePutRequestBuilder<R> extends CommonApacheHTTPRequest<HTTPPutRequestBuilder<R>, R> implements HTTPPutRequestBuilder<R> {

	private String stringBody;
	private InputStream isBody;
	private String cType;

	public ApachePutRequestBuilder(ApacheClientRequestBuilder<R> coreBuilder) {
		super(coreBuilder);
	}

	@Override
	protected HttpMethodBase createMethod(String encodedSite) {
		PutMethod putMethod = new PutMethod(encodedSite);
		RequestEntity entity = null;
		if (stringBody != null) {
			try {
				entity = new StringRequestEntity(stringBody, (cType != null) ? cType : "text/plain", "UTF-8");
			} catch (UnsupportedEncodingException e) {
			}
		} else if (isBody != null) {
			entity = new InputStreamRequestEntity(isBody, (cType != null) ? cType : "application/octet-stream");
		}
		
		if (entity != null) {
			putMethod.setRequestEntity(entity);
		}
		return putMethod;
	}

	public HTTPPutRequestBuilder<R> body(String body) {
		clearBody();
		this.stringBody = body;
		return this;
	}

	public HTTPPutRequestBuilder<R> body(InputStream body) {
		clearBody();
		this.isBody = body;
		return this;
	}

	private void clearBody() {
		stringBody = null;
		
		if (isBody != null) {
			try {
				isBody.close();
			} catch (IOException x) {
			}
			isBody = null;
		}
	}

	public HTTPPutRequestBuilder<R> contentType(String ctype) {
		this.cType = ctype;
		return this;
	}

}
