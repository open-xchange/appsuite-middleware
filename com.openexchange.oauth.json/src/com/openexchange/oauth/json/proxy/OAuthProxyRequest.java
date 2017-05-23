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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.regex.Pattern;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.exception.OXException;
import com.openexchange.oauth.API;
import com.openexchange.oauth.KnownApi;
import com.openexchange.oauth.json.Services;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.oauth.OAuthAPIRegistry;
import com.openexchange.oauth.OAuthAccount;
import com.openexchange.oauth.OAuthExceptionCodes;
import com.openexchange.oauth.OAuthService;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

public class OAuthProxyRequest {
	private final AJAXRequestData req;
	private final OAuthService oauthService;
	private boolean analyzed;
	private HTTPMethod method;
	private Map<String, String> parameters;
	private Map<String, String> headers;
	private String url;
	private String body;
	private final ServerSession session;

	protected Map<String, List<Pattern>> whitelist = new HashMap<String,List<Pattern>>(){{
		put(KnownApi.LINKEDIN.getFullName(), 	Arrays.asList(Pattern.compile("^http:\\/\\/api\\.linkedin\\.com")));
		put(KnownApi.TWITTER.getFullName(),	Arrays.asList(Pattern.compile("^https?:\\/\\/(.*?\\.)?twitter.com")));
		put(KnownApi.YAHOO.getFullName(), 	Arrays.asList(Pattern.compile("^https?:\\/\\/(.*?\\.)?yahoo(apis)?\\.com")));
		put(KnownApi.TUMBLR.getFullName(), Arrays.asList(Pattern.compile("^https?:\\/\\/.*?\\.tumblr\\.com")));
		put(KnownApi.FLICKR.getFullName(), Arrays.asList(Pattern.compile("^https?:\\/\\/.*?\\.flickr\\.com")));
		put(KnownApi.XING.getFullName(), Arrays.asList(Pattern.compile("^https:\\/\\/api\\.xing\\.com")));
		put(KnownApi.GOOGLE.getFullName(), Arrays.asList(Pattern.compile("^https:\\/\\/www\\.googleapis\\.com")));
	}};

	public static enum HTTPMethod {
		GET, PUT, POST, DELETE
	}

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
		    OAuthAPIRegistry service = Services.getService(OAuthAPIRegistry.class);
		    if(service == null){
		        throw ServiceExceptionCode.absentService(OAuthAPIRegistry.class);
		    }
			String sApi = req.getParameter("api");
            API api = service.resolveFromServiceId(sApi);
			if (null == api) {
                throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create("api", sApi);
            }

			return oauthService.getDefaultAccount(api, session);
		}

		throw AjaxExceptionCodes.MISSING_PARAMETER.create("id");
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
		List<Pattern> patterns = whitelist.get(proposedApi.getServiceId());
		if(patterns == null){
			throw OAuthExceptionCodes.NOT_A_WHITELISTED_URL.create(checkMe, proposedApi); //TODO: debatable
		}
		for(Pattern p: patterns){
			if(p.matcher(checkMe).find()){
				return;
			}
		}
		throw OAuthExceptionCodes.NOT_A_WHITELISTED_URL.create(checkMe, proposedApi);
	}
}
