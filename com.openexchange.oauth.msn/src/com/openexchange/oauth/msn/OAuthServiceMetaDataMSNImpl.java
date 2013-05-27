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

package com.openexchange.oauth.msn;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.logging.Log;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.http.deferrer.DeferringURLService;
import com.openexchange.log.LogFactory;
import com.openexchange.oauth.API;
import com.openexchange.oauth.AbstractOAuthServiceMetaData;
import com.openexchange.oauth.DefaultOAuthToken;
import com.openexchange.oauth.OAuthConstants;
import com.openexchange.oauth.OAuthExceptionCodes;
import com.openexchange.oauth.OAuthInteraction;
import com.openexchange.oauth.OAuthInteractionType;
import com.openexchange.oauth.OAuthToken;
import com.openexchange.session.Session;

/**
 * {@link OAuthServiceMetaDataMSNImpl}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class OAuthServiceMetaDataMSNImpl extends AbstractOAuthServiceMetaData {

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(OAuthServiceMetaDataMSNImpl.class));

    private static final String API_KEY = "com.openexchange.oauth.msn.apiKey";

    private static final String API_SECRET = "com.openexchange.oauth.msn.apiSecret";

    private static final String accessTokenGrabber = "https://login.live.com/oauth20_token.srf";

    private static final String REFRESH_TOKEN_KEY = "refresh_token";

    private final DeferringURLService deferrer;


    public OAuthServiceMetaDataMSNImpl(DeferringURLService deferrer) {
        setId("com.openexchange.oauth.msn");
        setDisplayName("WindowsLive / MSN");
        this.deferrer = deferrer;

        setAPIKeyName(API_KEY);
        setAPISecretName(API_SECRET);
    }

    @Override
    public OAuthInteraction initOAuth(String callbackUrl, Session session) throws OXException {
        try {
            if (deferrer != null) {
                callbackUrl = deferrer.getDeferredURL(callbackUrl);
            }
            // https://login.live.com/oauth20_authorize.srf?client_id=CLIENT_ID&scope=wl.signin&response_type=RESPONSE_TYPE&redirect_uri=REDIRECT_URL
            final String authUrl = new StringBuilder("https://login.live.com/oauth20_authorize.srf?client_id=")
            	.append(getAPIKey(session))
            	.append("&scope=wl.basic,wl.contacts_birthday,wl.offline_access,wl.contacts_photos,wl.contacts_skydrive,wl.contacts_emails,wl.photos,wl.postal_addresses,wl.skydrive&response_type=code&redirect_uri=")
            	.append(URLEncoder.encode(callbackUrl, "UTF-8")).toString();


            return new OAuthInteraction() {

                @Override
                public String getAuthorizationURL() {
                    return authUrl;
                }

                @Override
                public OAuthInteractionType getInteractionType() {
                    return OAuthInteractionType.CALLBACK;
                }

                @Override
                public OAuthToken getRequestToken() {
                    return new DefaultOAuthToken();
                }

            };

        } catch (UnsupportedEncodingException e) {
            LOG.error(e.getMessage(), e);
        }
        return super.initOAuth(callbackUrl, session);
    }

    @Override
    public void processArguments(Map<String, Object> arguments, Map<String, String> parameter, Map<String, Object> state) throws OXException {
        String verifier = parameter.get("code");
        if (null == verifier) {
            LOG.error("No wrap_verification_code present.");
        }
        arguments.put(OAuthConstants.ARGUMENT_PIN, verifier);
        arguments.put(OAuthConstants.ARGUMENT_CALLBACK, deferrer.getDeferredURL((String) state.get(OAuthConstants.ARGUMENT_CALLBACK)));
        super.processArguments(arguments, parameter, state);
    }

    @Override
    public OAuthToken getOAuthToken(Map<String, Object> arguments) throws OXException {
        try {
            String verifier = (String) arguments.get(OAuthConstants.ARGUMENT_PIN);
            String callback = (String) arguments.get(OAuthConstants.ARGUMENT_CALLBACK);
            Session session = (Session) arguments.get(OAuthConstants.ARGUMENT_SESSION);

            StringBuilder params = new StringBuilder();
            params.append("?client_id=").append(getAPIKey(session));
            params.append("&redirect_uri=").append(URLEncoder.encode(callback, "UTF-8"));
            params.append("&client_secret=").append(URLEncoder.encode(getAPISecret(session), "UTF-8"));
            params.append("&code=").append(verifier);
            params.append("&grant_type=authorization_code");

            HttpClient httpClient = new HttpClient();
            final int timeout = 10000;
            httpClient.getParams().setSoTimeout(timeout);
            httpClient.getParams().setIntParameter("http.connection.timeout", timeout);
            httpClient.getParams().setParameter("http.protocol.single-cookie-header", Boolean.TRUE);
            httpClient.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
            Protocol protocol = new Protocol("https", new TrustAllAdapter(), 443);
            httpClient.getHostConfiguration().setHost("login.live.com", 443, protocol);
            String urlString = accessTokenGrabber;
            PostMethod postMethod = new PostMethod(urlString + params);

            addParameter(postMethod, "client_id", getAPIKey(session));
            addParameter(postMethod,"redirect_uri", callback);
            addParameter(postMethod,"client_secret", getAPISecret());
            addParameter(postMethod,"code", verifier);
            addParameter(postMethod,"grant_type", "authorization_code");

            httpClient.executeMethod(postMethod);

            DefaultOAuthToken token = new DefaultOAuthToken();
            token.setSecret(new JSONObject().put("callback", callback).toString());
            String response = postMethod.getResponseBodyAsString();
            JSONObject responseObj = new JSONObject(response);
            token.setToken(responseObj.getString(REFRESH_TOKEN_KEY));
            return token;
        } catch (UnsupportedEncodingException x) {
            LOG.error(x.getMessage(), x);
        } catch (IOException e) {
            throw OAuthExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (JSONException e) {
			throw OAuthExceptionCodes.JSON_ERROR.create(e, e.getMessage());
		}

        return super.getOAuthToken(arguments);
        // throw OAuthExceptionCodes.IO_ERROR.create(" ***** Something went terribly wrong!");
    }

	private void addParameter(PostMethod postMethod, String param,
			String value) {
		if (value == null) {
            return;
        }
		postMethod.addParameter(param, value);

	}

	@Override
	public API getAPI() {
		return API.MSN;
	}

}
