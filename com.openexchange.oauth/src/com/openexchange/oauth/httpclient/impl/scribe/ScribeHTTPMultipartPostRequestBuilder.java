package com.openexchange.oauth.httpclient.impl.scribe;

import java.io.File;
import java.io.InputStream;

import org.scribe.model.OAuthRequest;
import org.scribe.model.Verb;

import com.openexchange.exception.OXException;
import com.openexchange.http.client.builder.HTTPMultipartPostRequestBuilder;
import com.openexchange.oauth.httpclient.OAuthHTTPRequestBuilder;

public class ScribeHTTPMultipartPostRequestBuilder extends
		ScribeGenericHTTPRequestBuilder<HTTPMultipartPostRequestBuilder>
		implements HTTPMultipartPostRequestBuilder {

	public ScribeHTTPMultipartPostRequestBuilder(
			OAuthHTTPRequestBuilder coreBuilder) {
		super(coreBuilder);
	}

	@Override
	public HTTPMultipartPostRequestBuilder part(String fieldName, File file)
			throws OXException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HTTPMultipartPostRequestBuilder part(String fieldName,
			InputStream is, String contentType, String filename)
			throws OXException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HTTPMultipartPostRequestBuilder part(String fieldName,
			InputStream is, String contentType) throws OXException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HTTPMultipartPostRequestBuilder part(String fieldName, String s,
			String contentType, String filename) throws OXException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HTTPMultipartPostRequestBuilder part(String fieldName, String s,
			String contentType) throws OXException {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	protected void modify(OAuthRequest request) {
		
	}

	@Override
	public Verb getVerb() {
		return Verb.POST;
	}

}
