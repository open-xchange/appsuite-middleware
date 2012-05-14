package com.openexchange.http.client.apache;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpState;

import com.openexchange.exception.OXException;
import com.openexchange.http.client.builder.HTTPGenericRequestBuilder;
import com.openexchange.http.client.builder.HTTPRequest;
import com.openexchange.http.client.builder.HTTPResponse;
import com.openexchange.http.client.exceptions.OxHttpClientExceptionCodes;

public class ApacheHTTPRequest implements HTTPRequest {

	private Map<String, String> headers;
	private Map<String, String> parameters;

	private HttpMethodBase method;
	private HttpClient client;
	private ApacheClientRequestBuilder coreBuilder;
	private CommonApacheHTTPRequest reqBuilder;
	
	
	
	public ApacheHTTPRequest(Map<String, String> headers, Map<String, String> parameters,
			HttpMethodBase method, HttpClient client, ApacheClientRequestBuilder coreBuilder, CommonApacheHTTPRequest builder) {
		super();
		this.headers = headers;
		this.parameters = parameters;
		this.method = method;
		this.client = client;
		this.coreBuilder = coreBuilder;
		this.reqBuilder = builder;
	}

	public HTTPResponse execute() throws OXException {
		try {
			HttpState state = coreBuilder.getState();
			if (state != null) {
				client.setState(state);
			} else {
				coreBuilder.setState(client.getState());
			}
			int status = client.executeMethod(method);
			if (status == 302) {
				String location = method.getResponseHeader("Location").getValue();
				reqBuilder.url(location);
				return reqBuilder.build().execute();
			}
			return new ApacheHTTPResponse(method, client, coreBuilder);
		} catch (HttpException e) {
			throw OxHttpClientExceptionCodes.APACHE_CLIENT_ERROR.create(e.getMessage(), e);
		} catch (IOException e) {
			throw OxHttpClientExceptionCodes.APACHE_CLIENT_ERROR.create(e.getMessage(), e);
		} finally {
			reqBuilder.done();
		}
	}

	public Map<String, String> getHeaders() {
		return headers;
	}


	public Map<String, String> getParameters() {
		return parameters;
	}

}
