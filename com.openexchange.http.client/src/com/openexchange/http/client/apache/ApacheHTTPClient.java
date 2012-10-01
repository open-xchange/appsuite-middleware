package com.openexchange.http.client.apache;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.httpclient.HttpMethodBase;
import com.openexchange.exception.OXException;
import com.openexchange.filemanagement.ManagedFileManagement;
import com.openexchange.http.client.AbstractHTTPClient;
import com.openexchange.http.client.HTTPClient;
import com.openexchange.http.client.builder.HTTPRequestBuilder;
import com.openexchange.http.client.builder.HTTPResponseProcessor;
import com.openexchange.http.client.exceptions.OxHttpClientExceptionCodes;

public class ApacheHTTPClient extends AbstractHTTPClient implements HTTPClient {

	private ManagedFileManagement fileManager;

	public ApacheHTTPClient(ManagedFileManagement fileManager) {
		this.fileManager = fileManager;
	}


	public String extractString(HttpMethodBase method) throws OXException {
		try {
			return method.getResponseBodyAsString();
		} catch (IOException e) {
			throw OxHttpClientExceptionCodes.APACHE_CLIENT_ERROR.create(e.getMessage(), e);
		}
	}

	public InputStream extractStream(HttpMethodBase method) throws OXException {
		try {
			return method.getResponseBodyAsStream();
		} catch (IOException e) {
			throw OxHttpClientExceptionCodes.APACHE_CLIENT_ERROR.create(e.getMessage(), e);
		}
	}

	public Reader extractReader(HttpMethodBase method) throws OXException {
		try {
			return new InputStreamReader(method.getResponseBodyAsStream(), method.getResponseCharSet());
		} catch (IOException e) {
			throw OxHttpClientExceptionCodes.APACHE_CLIENT_ERROR.create(e.getMessage(), e);
		}
	}

	public <R> R extractPayload(HttpMethodBase method, Class<R> responseType) throws OXException {
		if (responseType == String.class) {
			return (R) extractString(method);
		} else if (responseType == InputStream.class) {
			return (R) extractStream(method);
		} else if (responseType == Reader.class) {
			return (R) extractReader(method);
		}

		for(Class inputType: Arrays.asList(InputStream.class, Reader.class, String.class)) {
			List<HTTPResponseProcessor> procList = processors.get(inputType);
			for (HTTPResponseProcessor processor : procList) {
				if (processor.getTypes()[1] == responseType) {
					return (R) processor.process(extractPayload(method, inputType));
				}
			}
		}


		throw OxHttpClientExceptionCodes.APACHE_CLIENT_ERROR.create();
	}

	/*
	 *
	 */

	public  HTTPRequestBuilder getBuilder() {
		return new ApacheClientRequestBuilder(fileManager, this);
	}



}
