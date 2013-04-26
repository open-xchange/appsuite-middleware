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

package com.openexchange.oauth;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;

/**
 * {@link AbstractOAuthServiceMetaData} - The default {@link OAuthServiceMetaData} implementation.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AbstractOAuthServiceMetaData implements OAuthServiceMetaData {

    protected String id;
    protected String displayName;
    protected String apiKey;
    protected String apiSecret;
    
    protected String apiKeyName;
    protected String apiSecretName;
    
    public static ServiceLookup SERVICES;

    /**
     * Initializes a new {@link AbstractOAuthServiceMetaData}.
     */
    protected AbstractOAuthServiceMetaData() {
        super();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String getAPIKey() {
        return apiKey;
    }
    
    
    /**
     * Used to look up the apiKey in the config cascade
     *
     * @param apiKeyName The apiKeyName to set
     */
    public void setAPIKeyName(String apiKeyName) {
        this.apiKeyName = apiKeyName;
    }
    
    
    /**
     * Used to look up the apiSecret in the config cascade
     *
     * @param apiSecretName The apiSecretName to set
     */
    public void setAPISecretName(String apiSecretName) {
        this.apiSecretName = apiSecretName;
    }
    
    @Override
    public String getAPIKey(Session session) throws OXException {
        if (session == null || apiKeyName == null) {
            return getAPIKey();
        }
        int context = 0, user = 0;
        context = session.getContextId();
        user = session.getUserId();
        return SERVICES.getService(ConfigViewFactory.class).getView(user, context).get(apiKeyName, String.class);
    }

    @Override
    public String getAPISecret() {
        return apiSecret;
    }
    

    @Override
    public String getAPISecret(Session session) throws OXException {
        if (session == null || apiSecretName == null) {
            return getAPISecret();
        }
        int context = 0, user = 0;
        context = session.getContextId();
        user = session.getUserId();
        return SERVICES.getService(ConfigViewFactory.class).getView(user, context).get(apiSecretName, String.class);
    }

    /**
     * Sets the identifier
     *
     * @param id The identifier to set
     */
    public void setId(final String id) {
        this.id = id;
    }

    /**
     * Sets the display name
     *
     * @param displayName The display name to set
     */
    public void setDisplayName(final String displayName) {
        this.displayName = displayName;
    }

    /**
     * Sets the API Key
     * @deprecated: Implement {@link #getAPIKey(Session)} instead
     * @param apiKey The API Key to set
     */
    public void setApiKey(final String apiKey) {
        this.apiKey = apiKey;
    }

    /**
     * Sets the API Secret
     *
     * @deprecated: Implement {@link #getAPISecret(Session)} instead.
     * @param apiSecret The API Secret to set
     */
    public void setApiSecret(final String apiSecret) {
        this.apiSecret = apiSecret;
    }

    @Override
    public void processArguments(final Map<String, Object> arguments, final Map<String, String> parameter, final Map<String, Object> state) throws OXException {
        // no-op
    }

    @Override
    public OAuthToken getOAuthToken(final Map<String, Object> arguments) throws OXException {
        return null;
    }

    @Override
    public OAuthInteraction initOAuth(final String callbackUrl, Session session) throws OXException {
        return null;
    }

    @Override
    public boolean needsRequestToken() {
        return true;
    }

    @Override
    public String getScope() {
        return null;
    }

    @Override
    public String processAuthorizationURL(final String authUrl) {
        return authUrl;
    }

    @Override
    public String processAuthorizationURLCallbackAware(final String authUrl, final String callback) {
        return authUrl;
    }

    @Override
    public String modifyCallbackURL(final String callbackUrl, Session session) {
        return callbackUrl;
    }

    @Override
    public boolean registerTokenBasedDeferrer() {
    	return false;
    }

    /**
     * URL-encodes specified string.
     */
    protected static String urlEncode(final String s) {
        try {
            return URLEncoder.encode(s, "ISO-8859-1");
        } catch (final UnsupportedEncodingException e) {
            return s;
        }
    }
}
