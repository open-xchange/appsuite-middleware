package com.openexchange.http.client.apache;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.util.URIUtil;

import com.openexchange.groupware.AbstractOXException;
import com.openexchange.http.client.builder.HTTPGenericRequestBuilder;
import com.openexchange.http.client.builder.HTTPRequest;

public abstract class CommonApacheHTTPRequest<T extends HTTPGenericRequestBuilder<T, R>, R> {

	protected String url;

	protected Map<String, String> parameters = new TreeMap<String, String>();
	protected Map<String, String> headers = new TreeMap<String, String>();

	protected ApacheClientRequestBuilder<R> coreBuilder;

	private boolean verbatimURL;

	public CommonApacheHTTPRequest(ApacheClientRequestBuilder<R> coreBuilder) {
		this.coreBuilder = coreBuilder;
	}

	public T url(String url) {
		this.url = url;
		this.verbatimURL = false;
		return (T) this;
	}
	
	public T verbatimURL(String url) {
		this.url = url;
		this.verbatimURL = true;
		return (T) this;
	}


	public T parameter(String parameter, String value) {
		parameters.put(parameter, value);
		return (T) this;
	}

	public T parameters(Map<String, String> parameters) {
		this.parameters = parameters;
		return (T) this;
	}

	public T header(String header, String value) {
		headers.put(header, value);
		return (T) this;
	}

	public T headers(Map<String, String> headers) {
		this.headers = headers;
		return (T) this;
	}

	public HTTPRequest<R> build() throws AbstractOXException {
		try {
			final HttpClient client = new HttpClient();
			final int timeout = 10000;
			client.getParams().setSoTimeout(timeout);
			client.getParams().setIntParameter("http.connection.timeout",
					timeout);

			client.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
					new DefaultHttpMethodRetryHandler(0, false));

			/*
			 * Generate URL
			 */
			
			String encodedSite = verbatimURL ? url : URIUtil.encodeQuery(url);

			final java.net.URL javaURL = new java.net.URL(encodedSite);

			if (javaURL.getProtocol().equalsIgnoreCase("https")) {
				int port = javaURL.getPort();
				if (port == -1) {
					port = 443;
				}

				final Protocol https = new Protocol("https",
						new TrustAllAdapter(), 443);
				client.getHostConfiguration().setHost(javaURL.getHost(), port,
						https);

				final HttpMethodBase m = createMethod(javaURL.getFile());
				m.getParams().setSoTimeout(10000);
				m.setQueryString(javaURL.getQuery());
				for (Map.Entry<String, String> entry : headers.entrySet()) {
					m.setRequestHeader(entry.getKey(), entry.getValue());
				}
				addParams(m, javaURL.getQuery());
				
				
				return new ApacheHTTPRequest<R>(headers, parameters, m, client,
						coreBuilder, this);
			}
			/*
			 * No https, but http
			 */
			final HttpMethodBase m = createMethod(encodedSite);
			for (Map.Entry<String, String> entry : headers.entrySet()) {
				m.setRequestHeader(entry.getKey(), entry.getValue());
			}
			addParams(m, javaURL.getQuery());

			return new ApacheHTTPRequest<R>(headers, parameters, m, client,
					coreBuilder, this);
		} catch (URIException x) {
			throw new AbstractOXException(x.getMessage(), x);
		} catch (MalformedURLException e) {
			throw new AbstractOXException(e.getMessage(), e);
		}
	}

	protected void addParams(HttpMethodBase m, String q) {

		NameValuePair[] query = new NameValuePair[parameters.size()];

		int i = 0;
		for (Map.Entry<String, String> entry : parameters.entrySet()) {
			query[i++] = new NameValuePair(entry.getKey(), entry.getValue());
		}
		m.setQueryString(query);
		String queryString = m.getQueryString();
		if (q != null) {
			if (queryString != null && queryString.length() > 0) {
				queryString = queryString+"&"+q;
			} else {
				queryString = q;
			}
		}
		m.setQueryString(queryString);
	}

	protected abstract HttpMethodBase createMethod(String encodedSite);

	public void done() {
		
	}

}
