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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

package com.openexchange.oauth.facebook;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.openexchange.config.ConfigurationService;
import com.openexchange.oauth.AbstractOAuthServiceMetaData;
import com.openexchange.oauth.DefaultOAuthToken;
import com.openexchange.oauth.OAuthConstants;
import com.openexchange.oauth.OAuthException;
import com.openexchange.oauth.OAuthExceptionCodes;
import com.openexchange.oauth.OAuthToken;

/**
 * {@link OAuthServiceMetaDataFacebookImpl}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class OAuthServiceMetaDataFacebookImpl extends AbstractOAuthServiceMetaData {

    /**
     * The default API key.
     */
    private static final String KEY_API = "d36ebc9e274a89e3bd0c239cea4acb48";

    /**
     * The default secret key.
     */
    private static final String KEY_SECRET = "903e8006dbad9204bb74c26eb3ca2310";
    
    private ConfigurationService configurationService;

    /**
     * Initializes a new {@link OAuthServiceMetaDataFacebookImpl}.
     * @param configurationService
     */
    public OAuthServiceMetaDataFacebookImpl(ConfigurationService configurationService) {
        super();
        this.configurationService=configurationService;        
    }

    @Override
    public String getDisplayName() {
        return "Facebook";
    }

    @Override
    public String getId() {
        return "com.openexchange.oauth.facebook";
    }

    @Override
    public String getAPIKey() {        
        if (null == configurationService) {
            return KEY_API;
        }
        return configurationService.getProperty("com.openexchange.facebook.apiKey", KEY_API);
    }

    @Override
    public String getAPISecret() {        
        if (null == configurationService) {
            return KEY_SECRET;
        }
        return configurationService.getProperty("com.openexchange.facebook.secretKey", KEY_SECRET);
    }

    public boolean needsRequestToken() {
        return false;
    }

    public String getScope() {
        return "offline_access,publish_stream,read_stream,status_update,friends_birthday,friends_work_history,friends_about_me,friends_hometown";
    }

    public String processAuthorizationURL(final String authUrl) {
        final String removeMe = "response_type=token&";
        final int pos = authUrl.indexOf(removeMe);
        return pos < 0 ? authUrl : new StringBuilder(authUrl.length()).append(authUrl.substring(0, pos)).append(
            authUrl.substring(pos + removeMe.length())).toString();
    }

    @Override
    public void processArguments(final Map<String, Object> arguments, final Map<String, String> parameter, final Map<String, Object> state) {
        final String code = parameter.get("code");
        arguments.put(OAuthConstants.ARGUMENT_PIN, code);
        arguments.put(OAuthConstants.ARGUMENT_CALLBACK, state.get(OAuthConstants.ARGUMENT_CALLBACK));
    }

    @Override
    public OAuthToken getOAuthToken(final Map<String, Object> arguments) throws OAuthException {
        final String code = (String) arguments.get(OAuthConstants.ARGUMENT_PIN);
        String callback = (String) arguments.get(OAuthConstants.ARGUMENT_CALLBACK);

        BufferedReader reader = null;
        try {
            callback = URLEncoder.encode(callback, "UTF-8");
            final URL url = new URL("https://graph.facebook.com/oauth/access_token?client_id="+getAPIKey()+"&redirect_uri="+callback+"&client_secret="+getAPISecret()+"&code=" + code);
            final URLConnection connection = url.openConnection();
            connection.setConnectTimeout(2500);
            connection.setReadTimeout(2500);
            connection.connect();
            
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
            
            final StringBuilder response = new StringBuilder();
            String line;
            while((line = reader.readLine()) != null) {
                response.append(line);
            }
            
            return parseResponse(response.toString());
            
        } catch (final MalformedURLException e) {
            throw OAuthExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (final IOException e) {
            throw OAuthExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } finally {
            if(reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    // IGNORE
                }
            }
        }
    }
    
    private static final Pattern EXTRACTOR = Pattern.compile("access_token=(.*?)&?");

    private OAuthToken parseResponse(final String string) {
        final Matcher matcher = EXTRACTOR.matcher(string);
        String token = null; 
        if(matcher.matches()) {
            token = matcher.group(1);
        }
        return new DefaultOAuthToken(token, "");
    }

}
