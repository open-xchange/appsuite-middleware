/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the Open-Xchange, Inc. group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.oauth.httpclient.impl.scribe;

import java.util.Map;
import java.util.TreeMap;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.util.URIUtil;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.Api;
import org.scribe.builder.api.DropBoxApi;
import org.scribe.builder.api.FacebookApi;
import org.scribe.builder.api.FlickrApi;
import org.scribe.builder.api.LinkedInApi;
import org.scribe.builder.api.TumblrApi;
import org.scribe.builder.api.TwitterApi;
import org.scribe.builder.api.VkontakteApi;
import org.scribe.builder.api.XingApi;
import org.scribe.builder.api.YahooApi;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;
import com.openexchange.exception.OXException;
import com.openexchange.http.client.builder.HTTPGenericRequestBuilder;
import com.openexchange.http.client.builder.HTTPRequest;
import com.openexchange.oauth.API;
import com.openexchange.oauth.httpclient.OAuthHTTPRequest;
import com.openexchange.oauth.httpclient.OAuthHTTPRequestBuilder;

public abstract class ScribeGenericHTTPRequestBuilder<T extends HTTPGenericRequestBuilder<T>> {

	protected Map<String, String> parameters = new TreeMap<String, String>();
	protected Map<String, String> headers = new TreeMap<String, String>();
	protected OAuthHTTPRequestBuilder coreBuilder;

	private boolean isVerbatimUrl;
	private String verbatimUrl;
	private String baseUrl;
	protected OAuthService service;
	protected Class<? extends org.scribe.builder.api.Api> provider;

	public ScribeGenericHTTPRequestBuilder(OAuthHTTPRequestBuilder coreBuilder) {
		this.coreBuilder = coreBuilder;
		provider = getProvider(coreBuilder.getApi());

		service = new ServiceBuilder()
        .provider(getProvider(coreBuilder.getApi()))
        .apiKey(coreBuilder.getApiKey())
        .apiSecret(coreBuilder.getSecret())
        .build();
	}

	public abstract Verb getVerb();

	protected Class<? extends Api> getProvider(API api) {
		switch(api) {
		case FACEBOOK: return FacebookApi.class;
		case LINKEDIN: return LinkedInApi.class;
		case TWITTER: return TwitterApi.class;
		case YAHOO: return YahooApi.class;
		case TUMBLR: return TumblrApi.class;
		case FLICKR: return FlickrApi.class;
		case DROPBOX: return DropBoxApi.class;
		case XING: return XingApi.class;
		case VKONTAKTE: return VkontakteApi.class;
		// Add new API enums above

		case OTHER: // fall-through
		default:
		}
		throw new IllegalStateException("Unsupported API type: "+api);
	}

	public T url(String url) {
		isVerbatimUrl = false;
		this.baseUrl = url;
		return (T) this;
	}

	public T verbatimURL(String url) {
		isVerbatimUrl = true;
		this.verbatimUrl = url;
		return (T) this;
	}

	public T parameter(String parameter, String value) {
		parameters.put(parameter, value);
		return (T) this;
	}

	public T parameters(Map<String, String> parameters) {
		this.parameters.putAll(parameters);
		return (T) this;
	}

	public T header(String header, String value) {
		headers.put(header, value);
		return (T) this;
	}

	public T headers(Map<String, String> cookies) {
		headers.putAll(cookies);
		return (T) this;
	}

	public HTTPRequest build() throws OXException {
		String finalUrl;
		try {
			finalUrl = isVerbatimUrl ? verbatimUrl : URIUtil.encodeQuery(baseUrl);
		} catch (URIException e) {
			finalUrl = baseUrl;
		}

		OAuthRequest request = new OAuthRequest(getVerb(), finalUrl);
		setParameters(parameters, request);
		setHeaders(headers, request);
		modify(request);
		service.signRequest(getToken(), request);
		return new OAuthHTTPRequest(request, parameters);
	}

	protected void setHeaders(Map<String, String> headers, OAuthRequest request) {
		for(Map.Entry<String, String> header: headers.entrySet()) {
			if (header.getKey() != null && header.getValue() != null) {
				request.addHeader(header.getKey(), header.getValue());
			}
		}
	}

	protected void setParameters(Map<String, String> params,
			OAuthRequest request) {
		for(Map.Entry<String, String> param: params.entrySet()) {
			if (param.getKey() != null && param.getValue() != null) {
				request.addQuerystringParameter(param.getKey(), param.getValue());
			}
		}
	}

	protected void modify(OAuthRequest request) {

	}

	private Token getToken() {
		return new Token(coreBuilder.getAccount().getToken(), coreBuilder.getAccount().getSecret());
	}


}
