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

package com.openexchange.file.storage.boxcom.access;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.BoxApi;
import org.scribe.model.Token;
import org.slf4j.Logger;
import com.box.boxjavalibv2.BoxClient;
import com.box.boxjavalibv2.BoxConfigBuilder;
import com.box.boxjavalibv2.authorization.OAuthAuthorization;
import com.box.boxjavalibv2.dao.BoxOAuthToken;
import com.box.boxjavalibv2.exceptions.AuthFatalFailureException;
import com.box.boxjavalibv2.exceptions.BoxServerException;
import com.box.boxjavalibv2.jsonparsing.BoxJSONParser;
import com.box.boxjavalibv2.jsonparsing.BoxResourceHub;
import com.box.restclientv2.exceptions.BoxRestException;
import com.box.restclientv2.requestsbase.BoxDefaultRequestObject;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.boxcom.BoxClosure;
import com.openexchange.file.storage.boxcom.BoxConstants;
import com.openexchange.file.storage.boxcom.Services;
import com.openexchange.file.storage.boxcom.access.extended.ExtendedNonRefreshingBoxClient;
import com.openexchange.java.Strings;
import com.openexchange.oauth.AbstractOAuthAccess;
import com.openexchange.oauth.DefaultOAuthToken;
import com.openexchange.oauth.OAuthAccount;
import com.openexchange.oauth.OAuthConstants;
import com.openexchange.oauth.OAuthExceptionCodes;
import com.openexchange.oauth.OAuthService;
import com.openexchange.oauth.access.OAuthAccess;
import com.openexchange.oauth.access.OAuthClient;
import com.openexchange.rest.client.httpclient.HttpClients;
import com.openexchange.session.Session;

/**
 * {@link BoxOAuthAccess}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class BoxOAuthAccess extends AbstractOAuthAccess {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(BoxOAuthAccess.class);

    private FileStorageAccount fsAccount;
    private Session session;

    /**
     * Initialises a new {@link BoxOAuthAccess}.
     */
    public BoxOAuthAccess(FileStorageAccount fsAccount, Session session) throws OXException {
        super();
        this.fsAccount = fsAccount;
        this.session = session;

        int oauthAccountId = getAccountId();
        // Grab Box.com OAuth account
        OAuthService oAuthService = Services.getService(OAuthService.class);
        OAuthAccount boxOAuthAccount = oAuthService.getAccount(oauthAccountId, session, session.getUserId(), session.getContextId());
        setOAuthAccount(boxOAuthAccount);

        createOAuthClient();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.oauth.access.OAuthAccess#initialise()
     */
    @Override
    public void initialise() throws OXException {
        synchronized (this) {
            OAuthAccount newAccount = recreateTokenIfExpired(true);
            if (newAccount != null) {
                createOAuthClient();
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.oauth.access.OAuthAccess#revoke()
     */
    @Override
    public void revoke() throws OXException {
        // No Java API call
        // More information here: https://docs.box.com/reference#revoke
        try {
            DefaultHttpClient httpClient = HttpClients.getHttpClient("Open-Xchange box.com Client");
            // TODO: include the client id as a property in the boxcomoauth.properties
            HttpGet request = new HttpGet("https://api.box.com/oauth2/revoke?client_id=" + getOAuthAccount().getMetaData().getId() + "&client_secret" + getOAuthAccount().getMetaData().getAPISecret(session) + "&token=" + getOAuthAccount().getToken());

            HttpResponse httpResponse = httpClient.execute(request);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                return;
            } else {
                LOG.warn("The box.com OAuth token couldn't not be revoked for user '{}' in context '{}'. Status Code: {}, {}", session.getUserId(), session.getContextId(), statusCode, httpResponse.getStatusLine().getReasonPhrase());
            }
        } catch (ClientProtocolException e) {
            throw FileStorageExceptionCodes.PROTOCOL_ERROR.create(e, "HTTP", e.getMessage());
        } catch (IOException e) {
            throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.oauth.access.OAuthAccess#ping()
     */
    @Override
    public boolean ping() throws OXException {
        BoxClosure<Boolean> closure = new BoxClosure<Boolean>() {

            @Override
            protected Boolean doPerform(BoxOAuthAccess boxAccess) throws OXException, BoxRestException, BoxServerException, AuthFatalFailureException, UnsupportedEncodingException {
                try {
                    BoxClient client = (BoxClient) boxAccess.getClient().client;
                    client.getUsersManager().getCurrentUser(new BoxDefaultRequestObject());
                    return Boolean.TRUE;
                } catch (final BoxRestException e) {
                    if (401 == e.getStatusCode() || 403 == e.getStatusCode()) {
                        return Boolean.FALSE;
                    }
                    throw e;
                } catch (final BoxServerException e) {
                    if (401 == e.getStatusCode() || 403 == e.getStatusCode()) {
                        return Boolean.FALSE;
                    }
                    throw e;
                } catch (AuthFatalFailureException e) {
                    return Boolean.FALSE;
                }
            }
        };
        return closure.perform(null, this, session).booleanValue();
    }

    /**
     * Gets the extended box client
     *
     * @return The extended box client
     * @throws OXException
     */
    public OAuthClient<?> getExtendedClient() throws OXException {
        // TODO: Don't create a new client on every get; cache it
        OAuthAccount account = getOAuthAccount();
        ExtendedNonRefreshingBoxClient boxClient = new ExtendedNonRefreshingBoxClient(account.getMetaData().getAPIKey(session), account.getMetaData().getAPISecret(session), new BoxResourceHub(), new BoxJSONParser(new BoxResourceHub()), (new BoxConfigBuilder()).build());
        applyOAuthToken(boxClient);
        return new OAuthClient<ExtendedNonRefreshingBoxClient>(boxClient, getOAuthAccount().getToken());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.oauth.access.OAuthAccess#getAccountId()
     */
    @Override
    public int getAccountId() throws OXException {
        try {
            return getAccountId(fsAccount.getConfiguration());
        } catch (IllegalArgumentException e) {
            throw FileStorageExceptionCodes.MISSING_CONFIG.create(BoxConstants.ID, fsAccount.getId());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.oauth.access.OAuthAccess#ensureNotExpired()
     */
    @Override
    public OAuthAccess ensureNotExpired() throws OXException {
        if (isExpired()) {
            synchronized (this) {
                OAuthAccount newAccount = recreateTokenIfExpired(false);
                if (newAccount != null) {
                    createOAuthClient();
                }
            }
        }
        return this;
    }

    //////////////////////////////////////////// HELPERS ///////////////////////////////////////////////////

    /**
     * Apply access token and refresh token from OAuth account
     *
     * @param boxOAuthInfo
     */
    private void applyOAuthToken(BoxClient boxClient) {
        Map<String, Object> tokenSpec = new HashMap<String, Object>(6);
        OAuthAccount boxOAuthAccount = getOAuthAccount();
        tokenSpec.put(BoxOAuthToken.FIELD_ACCESS_TOKEN, boxOAuthAccount.getToken());
        tokenSpec.put(BoxOAuthToken.FIELD_REFRESH_TOKEN, boxOAuthAccount.getSecret());
        tokenSpec.put(BoxOAuthToken.FIELD_TOKEN_TYPE, "bearer");
        tokenSpec.put(BoxOAuthToken.FIELD_EXPIRES_IN, Integer.valueOf(3600));
        ((OAuthAuthorization) boxClient.getAuth()).setOAuthData(new BoxOAuthToken(tokenSpec));
    }

    /**
     * Re-creates the OAuth token if it is expired
     * 
     * @param considerExpired flag to consider the token as expired
     * @return the {@link OAuthAccount} with the updated token
     * @throws OXException if the token cannot be recreated
     */
    private OAuthAccount recreateTokenIfExpired(boolean considerExpired) throws OXException {
        // Create Scribe Box.com OAuth service
        OAuthAccount boxOAuthAccount = getOAuthAccount();
        final ServiceBuilder serviceBuilder = new ServiceBuilder().provider(BoxApi.class);
        serviceBuilder.apiKey(boxOAuthAccount.getMetaData().getAPIKey(session)).apiSecret(boxOAuthAccount.getMetaData().getAPISecret(session));
        BoxApi.BoxApiService scribeOAuthService = (BoxApi.BoxApiService) serviceBuilder.build();

        // Check expiration
        if (considerExpired || scribeOAuthService.isExpired(boxOAuthAccount.getToken())) {
            // Expired...
            String refreshToken = boxOAuthAccount.getSecret();
            Token accessToken;
            try {
                accessToken = scribeOAuthService.getAccessToken(new Token(boxOAuthAccount.getToken(), boxOAuthAccount.getSecret()), null);
            } catch (org.scribe.exceptions.OAuthException e) {
                throw OAuthExceptionCodes.INVALID_ACCOUNT_EXTENDED.create(e, boxOAuthAccount.getDisplayName(), boxOAuthAccount.getId());
            }
            if (Strings.isEmpty(accessToken.getSecret())) {
                LOG.warn("Received invalid request_token from Box.com: {}. Response:{}{}", null == accessToken.getSecret() ? "null" : accessToken.getSecret(), Strings.getLineSeparator(), accessToken.getRawResponse());
            } else {
                refreshToken = accessToken.getSecret();
            }
            // Update account
            OAuthService oAuthService = Services.getService(OAuthService.class);
            int accountId = boxOAuthAccount.getId();
            Map<String, Object> arguments = new HashMap<String, Object>(3);
            arguments.put(OAuthConstants.ARGUMENT_REQUEST_TOKEN, new DefaultOAuthToken(accessToken.getToken(), refreshToken));
            arguments.put(OAuthConstants.ARGUMENT_SESSION, session);
            oAuthService.updateAccount(accountId, arguments, session.getUserId(), session.getContextId());

            // Reload
            return oAuthService.getAccount(accountId, session, session.getUserId(), session.getContextId());
        }
        return null;
    }

    private OAuthClient<BoxClient> createOAuthClient() throws OXException {
        OAuthAccount account = getOAuthAccount();
        BoxClient boxClient = new NonRefreshingBoxClient(account.getMetaData().getAPIKey(session), account.getMetaData().getAPISecret(session), new BoxResourceHub(), new BoxJSONParser(new BoxResourceHub()), (new BoxConfigBuilder()).build());
        applyOAuthToken(boxClient);
        return new OAuthClient<BoxClient>(boxClient, getOAuthAccount().getToken());
    }
}
