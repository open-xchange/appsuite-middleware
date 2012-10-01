package com.openexchange.http.client.apache;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import com.openexchange.http.client.builder.HTTPPutRequestBuilder;

public class ApachePutRequestBuilder extends CommonApacheHTTPRequest<HTTPPutRequestBuilder> implements HTTPPutRequestBuilder {

	private String stringBody;
	private InputStream isBody;
	private String cType;

	public ApachePutRequestBuilder(final ApacheClientRequestBuilder coreBuilder) {
		super(coreBuilder);
	}

	@Override
	protected HttpMethodBase createMethod(final String encodedSite) {
		final PutMethod putMethod = new PutMethod(encodedSite);
		RequestEntity entity = null;
		if (stringBody != null) {
			try {
				entity = new StringRequestEntity(stringBody, (cType != null) ? cType : "text/plain", "UTF-8");
			} catch (final UnsupportedEncodingException e) {
			}
		} else if (isBody != null) {
			entity = new InputStreamRequestEntity(isBody, (cType != null) ? cType : "application/octet-stream");
		}

		if (entity != null) {
			putMethod.setRequestEntity(entity);
		}
		return putMethod;
	}

	public HTTPPutRequestBuilder body(final String body) {
		clearBody();
		this.stringBody = body;
		return this;
	}

	public HTTPPutRequestBuilder body(final InputStream body) {
		clearBody();
		this.isBody = body;
		return this;
	}

	private void clearBody() {
		stringBody = null;

		if (isBody != null) {
			try {
				isBody.close();
			} catch (final IOException x) {
			}
			isBody = null;
		}
	}

	public HTTPPutRequestBuilder contentType(final String ctype) {
		this.cType = ctype;
		return this;
	}

}
