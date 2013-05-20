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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.oauth.dropbox;

import java.util.Map;
import org.apache.commons.logging.Log;
import org.scribe.builder.api.Api;
import org.scribe.builder.api.DropBoxApi;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Account;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.RequestTokenPair;
import com.dropbox.client2.session.Session.AccessType;
import com.dropbox.client2.session.WebAuthSession;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.java.StringAllocator;
import com.openexchange.java.Strings;
import com.openexchange.oauth.API;
import com.openexchange.oauth.AbstractOAuthServiceMetaData;
import com.openexchange.oauth.AbstractParameterizableOAuthInteraction;
import com.openexchange.oauth.DefaultOAuthToken;
import com.openexchange.oauth.OAuthConstants;
import com.openexchange.oauth.OAuthExceptionCodes;
import com.openexchange.oauth.OAuthInteraction;
import com.openexchange.oauth.OAuthInteractionType;
import com.openexchange.oauth.OAuthToken;

/**
 * {@link DropboxOAuthServiceMetaData}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class DropboxOAuthServiceMetaData extends AbstractOAuthServiceMetaData implements com.openexchange.oauth.ScribeAware {

    private static final Log LOG = com.openexchange.log.Log.loggerFor(DropboxOAuthServiceMetaData.class);

    /**
     * Initializes a new {@link DropboxOAuthServiceMetaData}.
     */
    public DropboxOAuthServiceMetaData(final ConfigurationService configService) {
        super();
        id = "com.openexchange.oauth.dropbox";
        displayName = "Dropbox";

        final String apiKey = configService.getProperty("com.openexchange.oauth.dropbox.apiKey");
        if (Strings.isEmpty(apiKey)) {
            throw new IllegalStateException("Missing following property in configuration: com.openexchange.oauth.dropbox.apiKey");
        }
        this.apiKey = apiKey;

        final String apiSecret = configService.getProperty("com.openexchange.oauth.dropbox.apiSecret");
        if (Strings.isEmpty(apiSecret)) {
            throw new IllegalStateException("Missing following property in configuration: com.openexchange.oauth.dropbox.apiSecret");
        }
        this.apiSecret = apiSecret;
    }

    @Override
    public API getAPI() {
        return API.DROPBOX;
    }

    @Override
    public OAuthInteraction initOAuth(final String callbackUrl) throws OXException {
        try {
            final AppKeyPair appKeys = new AppKeyPair(apiKey, apiSecret);
            final DropboxAPI<WebAuthSession> dropboxAPI =
                new DropboxAPI<WebAuthSession>(new TrustAllWebAuthSession(appKeys, AccessType.DROPBOX));
            final StringAllocator authUrl = new StringAllocator(dropboxAPI.getSession().getAuthInfo().url);
            if (!isEmpty(callbackUrl)) {
                authUrl.append('&').append(OAuthConstants.URLPARAM_OAUTH_CALLBACK).append('=').append(urlEncode(callbackUrl)).toString();
            }
            final String sAuthUrl = authUrl.toString();
            final AbstractParameterizableOAuthInteraction oAuthInteraction = new AbstractParameterizableOAuthInteraction() {

                @Override
                public String getAuthorizationURL() {
                    return sAuthUrl;
                }

                @Override
                public OAuthInteractionType getInteractionType() {
                    return OAuthInteractionType.CALLBACK;
                }

                @Override
                public OAuthToken getRequestToken() {
                    return OAuthToken.EMPTY_TOKEN;
                }

            };
            oAuthInteraction.putParameter(DropboxAPI.class.getName(), dropboxAPI);
            return oAuthInteraction;
        } catch (final DropboxException e) {
            throw OAuthExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public void processArguments(final Map<String, Object> arguments, final Map<String, String> parameter, final Map<String, Object> state) throws OXException {
        try {
            @SuppressWarnings("unchecked")
            final DropboxAPI<WebAuthSession> dropboxAPI = (DropboxAPI<WebAuthSession>) state.get(DropboxAPI.class.getName());
            final AccessTokenPair tokenPair = dropboxAPI.getSession().getAccessTokenPair();
            final RequestTokenPair tokens = new RequestTokenPair(tokenPair.key, tokenPair.secret);
            dropboxAPI.getSession().retrieveWebAccessToken(tokens); // completes initial auth
            // Retrieve access tokens for future use
            final String tokenKey = dropboxAPI.getSession().getAccessTokenPair().key; // store String returned by this call somewhere
            final String tokenSecret = dropboxAPI.getSession().getAccessTokenPair().secret; // same for this line
            final DefaultOAuthToken token = new DefaultOAuthToken();
            token.setSecret(tokenSecret);
            token.setToken(tokenKey);
            arguments.put(OAuthConstants.ARGUMENT_REQUEST_TOKEN, token);
            // Check
            {
                final AppKeyPair appKeys = new AppKeyPair(apiKey, apiSecret);
                final WebAuthSession session = new TrustAllWebAuthSession(appKeys, AccessType.APP_FOLDER);
                final DropboxAPI<WebAuthSession> mDBApi = new DropboxAPI<WebAuthSession>(session);
                // Re-auth specific stuff
                final AccessTokenPair reAuthTokens = new AccessTokenPair(tokenKey, tokenSecret);
                mDBApi.getSession().setAccessTokenPair(reAuthTokens);

                final Account accountInfo = mDBApi.accountInfo();
                LOG.info("Dropbox OAuth account successfully created for " + accountInfo.displayName);
            }
        } catch (final DropboxException e) {
            throw OAuthExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public OAuthToken getOAuthToken(final Map<String, Object> arguments) throws OXException {
        return (OAuthToken) arguments.get(OAuthConstants.ARGUMENT_REQUEST_TOKEN);
    }

    private static boolean isEmpty(final String string) {
        if (null == string) {
            return true;
        }
        final int len = string.length();
        boolean isWhitespace = true;
        for (int i = 0; isWhitespace && i < len; i++) {
            isWhitespace = com.openexchange.java.Strings.isWhitespace(string.charAt(i));
        }
        return isWhitespace;
    }

    @Override
    public Class<? extends Api> getScribeService() {
        return DropBoxApi.class;
    }

}
