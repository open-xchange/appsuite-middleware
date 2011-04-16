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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.oauth.AbstractOAuthServiceMetaData;
import com.openexchange.oauth.DefaultOAuthToken;
import com.openexchange.oauth.OAuthConstants;
import com.openexchange.oauth.OAuthException;
import com.openexchange.oauth.OAuthExceptionCodes;
import com.openexchange.oauth.OAuthInteraction;
import com.openexchange.oauth.OAuthInteractionType;
import com.openexchange.oauth.OAuthToken;

/**
 * {@link OAuthServiceMetaDataMSNImpl}
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class OAuthServiceMetaDataMSNImpl extends AbstractOAuthServiceMetaData {

    private static final Log LOG = LogFactory.getLog(OAuthServiceMetaDataMSNImpl.class);

    private static final String accessTokenGrabber = "https://consent.live.com/AccessToken.aspx";

    private static final Object REFRESH_TOKEN_KEY = "wrap_refresh_token";

    public OAuthServiceMetaDataMSNImpl(String apiKey, String apiSecret) {
        setId("com.openexchange.oauth.msn");
        setApiKey(apiKey);
        setApiSecret(apiSecret);
        setDisplayName("WindowsLive / MSN");
    }

    @Override
    public OAuthInteraction initOAuth(String callbackUrl) throws OAuthException {
        try {
            final String authUrl = new StringBuilder("https://consent.live.com/connect.aspx?wrap_client_id=").append(getAPIKey()).append(
                "&wrap_callback=").append(URLEncoder.encode(callbackUrl, "UTF-8")).append(
                "&wrap_client_state=js_close_window&mkt=en-us&wrap_scope=WL_Profiles.View,WL_Contacts.View,Messenger.SignIn").toString();

            return new OAuthInteraction() {

                public String getAuthorizationURL() {
                    return authUrl;
                }

                public OAuthInteractionType getInteractionType() {
                    return OAuthInteractionType.CALLBACK;
                }

                public OAuthToken getRequestToken() {
                    return new DefaultOAuthToken();
                }

            };

        } catch (UnsupportedEncodingException e) {
            LOG.error(e.getMessage(), e);
        }
        return super.initOAuth(callbackUrl);
    }

    @Override
    public void processArguments(Map<String, Object> arguments, Map<String, String> parameter, Map<String, Object> state) {
        String verifier = parameter.get("wrap_verification_code");
        if (null == verifier){
            LOG.error("No wrap_verification_code present.");
        }
        arguments.put(OAuthConstants.ARGUMENT_PIN, verifier);
        arguments.put(OAuthConstants.ARGUMENT_CALLBACK, state.get(OAuthConstants.ARGUMENT_CALLBACK));
        super.processArguments(arguments, parameter, state);
    }

    @Override
    public OAuthToken getOAuthToken(Map<String, Object> arguments) throws OAuthException {
        OutputStreamWriter writer = null;
        BufferedReader reader = null;
        try {
            String verifier = (String) arguments.get(OAuthConstants.ARGUMENT_PIN);
            String callback = (String) arguments.get(OAuthConstants.ARGUMENT_CALLBACK);

            StringBuilder params = new StringBuilder();
            params.append("wrap_client_id=").append(getAPIKey());
            params.append("&wrap_client_secret=").append(getAPISecret());
            params.append("&wrap_callback=").append(URLEncoder.encode(callback, "UTF-8"));
            params.append("&wrap_verification_code=").append(verifier);

            final URL url = new URL(accessTokenGrabber);            
            final URLConnection connection = url.openConnection();
            connection.setConnectTimeout(2500);
            connection.setReadTimeout(2500);
            connection.setDoOutput(true);

            writer = new OutputStreamWriter(connection.getOutputStream());

            writer.write(params.toString());
            writer.flush();
            
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String line = null;
            
            DefaultOAuthToken token = new DefaultOAuthToken();
            token.setSecret("");
            
            while((line = reader.readLine()) != null) {
                String[] keyValuePairs = line.split("&");
                for (String keyValuePair : keyValuePairs) {
                    String[] split = keyValuePair.split("=");
                    if(split[0].equals(REFRESH_TOKEN_KEY)) {
                        token.setToken(split[1]);
                        return token;
                    }
                }
            }
            
        } catch (UnsupportedEncodingException x) {
            LOG.error(x.getMessage(), x);
        } catch (IOException e) {
            throw OAuthExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    // IGNORE
                }
            }
            if(reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    // IGNORE
                }
            }
        }

        return super.getOAuthToken(arguments);
    }

}
