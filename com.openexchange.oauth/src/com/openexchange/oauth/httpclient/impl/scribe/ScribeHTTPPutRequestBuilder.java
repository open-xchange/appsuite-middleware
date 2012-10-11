package com.openexchange.oauth.httpclient.impl.scribe;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.scribe.model.OAuthRequest;
import org.scribe.model.Verb;

import com.openexchange.exception.OXException;
import com.openexchange.http.client.builder.HTTPPutRequestBuilder;
import com.openexchange.http.client.exceptions.OxHttpClientExceptionCodes;
import com.openexchange.oauth.httpclient.OAuthHTTPRequestBuilder;

public class ScribeHTTPPutRequestBuilder extends
		ScribeGenericHTTPRequestBuilder<HTTPPutRequestBuilder> implements
		HTTPPutRequestBuilder {

	private String payload = "";
	private String contentType = "application/octet-stream";

	public ScribeHTTPPutRequestBuilder(OAuthHTTPRequestBuilder coreBuilder) {
		super(coreBuilder);
	}

	@Override
	public Verb getVerb() {
		return Verb.PUT;
	}

	@Override
	public HTTPPutRequestBuilder body(String body) {
		payload = body;
		return this;
	}

	@Override
	public HTTPPutRequestBuilder body(InputStream body) throws OXException {
		InputStreamReader isr;
		try {
			isr = new InputStreamReader(new BufferedInputStream(body), "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			return null;
		}
		StringBuilder b = new StringBuilder();
		int ch = -1;
		try {
			while ((ch = isr.read()) != -1) {
				b.append((char) ch);
			}
		} catch (IOException e) {
			throw OxHttpClientExceptionCodes.IO_ERROR.create(e.getMessage());
		} finally {
			try {
				isr.close();
			} catch (IOException e) {
			}
		}
		return this;
	}

	@Override
	public HTTPPutRequestBuilder contentType(String ctype) {
		this.contentType = ctype;
		return this;
	}

	@Override
	protected void modify(OAuthRequest request) {
		request.addPayload(payload);
		if (contentType != null) {
			request.addHeader("Content-Type", contentType);
		}
	}

}
