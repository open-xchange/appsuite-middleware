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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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

package com.openexchange.oauth.json.proxy;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import org.json.JSONException;
import org.json.JSONObject;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.oauth.API;
import com.openexchange.oauth.OAuthAccount;
import com.openexchange.oauth.OAuthExceptionCodes;
import com.openexchange.oauth.OAuthService;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

public class OAuthProxyRequest {

    /** The scheme for HTTP protocol */
    static final String SCHEME_HTTP = "http";

    /** The scheme for HTTPS protocol */
    static final String SCHEME_HTTPS = "https";

    /** The scheme set for HTTP and HTTPS protocols */
    static final Set<String> SCHEMES_HTTP_AND_HTTPS = ImmutableSet.<String> builder().add(SCHEME_HTTP).add(SCHEME_HTTPS).build();

    /** A matcher for a certain service */
    private static interface ServiceMatcher {

        /**
         * Checks if this matcher accepts given URL.
         *
         * @param url The URL to check
         * @return <code>true</code> if URL s accepted; otherwise <code>false</code>
         * @throws OXException If URL cannot be checked
         */
        boolean accept(String url) throws OXException;

        /**
         * Gets the API this matcher belongs to
         *
         * @return The API
         */
        API getAPI();

    } // End of interface ServiceMatcher

    private static abstract class HostAndSchemeServiceMatcher implements ServiceMatcher {

        /**
         * Initializes a new {@link UriServiceMatcher}.
         */
        protected HostAndSchemeServiceMatcher() {
            super();
        }

        /**
         * Checks if given host is accepted.
         *
         * @param host The host to check
         * @return <code>true</code> if host is accepted; otherwise <code>false</code>
         */
        protected abstract boolean acceptHost(String host);

        /**
         * Checks if given scheme is accepted.
         *
         * @param scheme The scheme to check
         * @return <code>true</code> if scheme is accepted; otherwise <code>false</code>
         */
        protected abstract boolean acceptScheme(String scheme);

        @Override
        public boolean accept(String url) throws OXException {
            try {
                URI uri = new URI(url);
                return acceptScheme(Strings.asciiLowerCase(uri.getScheme())) && acceptHost(Strings.asciiLowerCase(uri.getHost()));
            } catch (URISyntaxException e) {
                throw OAuthExceptionCodes.NOT_A_WHITELISTED_URL.create(e, url, getAPI());
            }
        }

    } // End of abstract class HostAndSchemeServiceMatcher

    private static final Map<API, List<ServiceMatcher>> WHITELIST = ImmutableMap.<API, List<ServiceMatcher>> builder()
        .put(API.LINKEDIN, Collections.<ServiceMatcher> singletonList(new HostAndSchemeServiceMatcher() {

            @Override
            protected boolean acceptScheme(String scheme) {
                return SCHEME_HTTP.equals(scheme);
            }

            @Override
            protected boolean acceptHost(String host) {
                return "api.linkedin.com".equals(host);
            }

            @Override
            public API getAPI() {
                return API.LINKEDIN;
            }
        }))
        .put(API.TWITTER, Collections.<ServiceMatcher> singletonList(new HostAndSchemeServiceMatcher() {

            @Override
            protected boolean acceptScheme(String scheme) {
                return scheme != null && SCHEMES_HTTP_AND_HTTPS.contains(scheme);
            }

            @Override
            protected boolean acceptHost(String host) {
                if (host == null) {
                    return false;
                }
                return "twitter.com".equals(host) || host.endsWith(".twitter.com");
            }

            @Override
            public API getAPI() {
                return API.TWITTER;
            }
        }))
        .put(API.YAHOO, Collections.<ServiceMatcher> singletonList(new HostAndSchemeServiceMatcher() {

            @Override
            protected boolean acceptScheme(String scheme) {
                return scheme != null && SCHEMES_HTTP_AND_HTTPS.contains(scheme);
            }

            @Override
            protected boolean acceptHost(String host) {
                if (host == null) {
                    return false;
                }
                return "yahoo.com".equals(host) || host.endsWith(".yahoo.com") || "yahooapis.com".equals(host) || host.endsWith(".yahooapis.com");
            }

            @Override
            public API getAPI() {
                return API.YAHOO;
            }
        }))
        .put(API.TUMBLR, Collections.<ServiceMatcher> singletonList(new HostAndSchemeServiceMatcher() {

            @Override
            protected boolean acceptScheme(String scheme) {
                return scheme != null && SCHEMES_HTTP_AND_HTTPS.contains(scheme);
            }

            @Override
            protected boolean acceptHost(String host) {
                if (host == null) {
                    return false;
                }
                return host.endsWith(".tumblr.com");
            }

            @Override
            public API getAPI() {
                return API.TUMBLR;
            }
        }))
        .put(API.FLICKR, Collections.<ServiceMatcher> singletonList(new HostAndSchemeServiceMatcher() {

            @Override
            protected boolean acceptScheme(String scheme) {
                return scheme != null && SCHEMES_HTTP_AND_HTTPS.contains(scheme);
            }

            @Override
            protected boolean acceptHost(String host) {
                if (host == null) {
                    return false;
                }
                return host.endsWith(".flickr.com");
            }

            @Override
            public API getAPI() {
                return API.FLICKR;
            }
        }))
        .put(API.XING, Collections.<ServiceMatcher> singletonList(new HostAndSchemeServiceMatcher() {

            @Override
            protected boolean acceptScheme(String scheme) {
                return SCHEME_HTTPS.equals(scheme);
            }

            @Override
            protected boolean acceptHost(String host) {
                return host.equals("api.xing.com");
            }

            @Override
            public API getAPI() {
                return API.XING;
            }
        }))
        .put(API.GOOGLE, Collections.<ServiceMatcher> singletonList(new HostAndSchemeServiceMatcher() {

            @Override
            protected boolean acceptScheme(String scheme) {
                return SCHEME_HTTPS.equals(scheme);
            }

            @Override
            protected boolean acceptHost(String host) {
                return host.equals("www.googleapis.com");
            }

            @Override
            public API getAPI() {
                return API.GOOGLE;
            }
        }))
        .build();

    public static enum HTTPMethod {
        GET, PUT, POST, DELETE
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

	private final AJAXRequestData req;
	private final OAuthService oauthService;
	private boolean analyzed;
	private HTTPMethod method;
	private Map<String, String> parameters;
	private Map<String, String> headers;
	private String url;
	private String body;
	private final ServerSession session;

	public OAuthProxyRequest(AJAXRequestData req, ServerSession session, OAuthService oauthService) {
		this.req = req;
		this.oauthService = oauthService;
		this.session = session;
	}

	private void analyzeBody() throws OXException {
		if (analyzed) {
			return;
		}
		analyzed = true;

		JSONObject proxyRequest = (JSONObject) req.getData();
		if(proxyRequest == null){
			throw OAuthExceptionCodes.MISSING_BODY.create();
		}

		String methodName = proxyRequest.optString("type");
		if (methodName == null || 0 == methodName.length()) {
			methodName = "GET";
		} else {
			methodName = methodName.toUpperCase();
		}

		method = HTTPMethod.valueOf(methodName);

		JSONObject paramsObj = proxyRequest.optJSONObject("params");
		parameters = new TreeMap<String,String>();
		if(paramsObj != null){
			for(Entry<String, Object> entry : paramsObj.entrySet()){
				parameters.put(entry.getKey(), String.valueOf(entry.getValue()));
			}
		}

		JSONObject headerObj = proxyRequest.optJSONObject("header");
		headers = new TreeMap<String,String>();
		if(headerObj != null){
			for(Entry<String, Object> entry : headerObj.entrySet()){
				headers.put(entry.getKey(), String.valueOf(entry.getValue()));
			}
		}

		if(proxyRequest.hasAndNotNull("content-type")){
			headers.put("Content-Type", String.valueOf(proxyRequest.opt("content-type")));
		}

		if (!headers.containsKey("Content-Type")) {
			headers.put("Content-Type", "application/x-www-form-urlencoded");
		}

		if(proxyRequest.hasAndNotNull("accepts")){
			headers.put("Accepts", String.valueOf(proxyRequest.opt("accepts")));
		}

		try {
			url = proxyRequest.getString("url");
		} catch (JSONException e) {
			throw AjaxExceptionCodes.MISSING_PARAMETER.create("url");
		}

		body = proxyRequest.optString("data");

	}

	public OAuthAccount getAccount() throws OXException {
		if (req.isSet("id")) {
			int id = req.getParameter("id", int.class);

			return oauthService.getAccount(id, session, session.getUserId(), session.getContextId());
		}

		if (req.isSet("api")){
			API api = API.valueOf(req.getParameter("api").toUpperCase());

			return oauthService.getDefaultAccount(api, session);
		}
		req.require("id");
		return null; //should not be reached
	}

	public API getAPI() throws OXException {
		analyzeBody();
		return getAccount().getAPI();
	}

	public HTTPMethod getMethod() throws OXException {
		analyzeBody();
		return method;
	}

	public Map<String, String> getParameters() throws OXException {
		analyzeBody();
		return parameters;
	}

	public Map<String, String> getHeaders() throws OXException {
		analyzeBody();
		return headers;
	}

	public String getBody() throws OXException {
		analyzeBody();
		return body;
	}

	public String getUrl() throws OXException {
		whitelist(url);
		analyzeBody();
		return url;
	}


    private void whitelist(String checkMe) throws OXException {
        API proposedApi = getAccount().getAPI();
        List<ServiceMatcher> matchers = WHITELIST.get(proposedApi);
        if (matchers == null) {
            throw OAuthExceptionCodes.NOT_A_WHITELISTED_URL.create(checkMe, proposedApi.name()); //TODO: debatable
        }
        for (ServiceMatcher matcher : matchers) {
            if (matcher.accept(checkMe)) {
                return;
            }
        }
        throw OAuthExceptionCodes.NOT_A_WHITELISTED_URL.create(checkMe, proposedApi.name());
    }

}
