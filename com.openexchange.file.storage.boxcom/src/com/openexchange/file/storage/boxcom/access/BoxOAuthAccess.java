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
 *     Copyright (C) 2004-2016 Open-Xchange, Inc.
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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.BoxApi;
import org.scribe.model.Token;
import org.slf4j.Logger;
import com.box.boxjavalibv2.BoxClient;
import com.box.boxjavalibv2.BoxConfigBuilder;
import com.box.boxjavalibv2.authorization.OAuthAuthorization;
import com.box.boxjavalibv2.dao.BoxOAuthToken;
import com.box.boxjavalibv2.jsonparsing.BoxJSONParser;
import com.box.boxjavalibv2.jsonparsing.BoxResourceHub;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.boxcom.BoxConstants;
import com.openexchange.file.storage.boxcom.Services;
import com.openexchange.file.storage.boxcom.access.extended.ExtendedNonRefreshingBoxClient;
import com.openexchange.java.Strings;
import com.openexchange.oauth.DefaultOAuthToken;
import com.openexchange.oauth.OAuthAccount;
import com.openexchange.oauth.OAuthConstants;
import com.openexchange.oauth.OAuthExceptionCodes;
import com.openexchange.oauth.OAuthService;
import com.openexchange.oauth.access.OAuthAccess;
import com.openexchange.oauth.access.OAuthClient;
import com.openexchange.session.Session;

/**
 * {@link BoxOAuthAccess}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class BoxOAuthAccess implements OAuthAccess {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(BoxAccess.class);

    /** The re-check threshold in seconds (45 minutes) */
    private static final long RECHECK_THRESHOLD = 2700;

    private FileStorageAccount fsAccount;
    private Session session;

    /** The associated OAuth information */
    private final AtomicReference<BoxOAuthInfo> boxOAuthInfoRef;

    /** The last-accessed time stamp */
    private volatile long lastAccessed;

    /**
     * Initialises a new {@link BoxOAuthAccess}.
     */
    public BoxOAuthAccess(FileStorageAccount fsAccount, Session session) throws OXException {
        super();
        this.fsAccount = fsAccount;
        this.session = session;

        int oauthAccountId = getAccountId();
        // Grab Box.com OAuth account
        OAuthAccount boxOAuthAccount;
        {
            OAuthService oAuthService = Services.getService(OAuthService.class);
            boxOAuthAccount = oAuthService.getAccount(oauthAccountId, session, session.getUserId(), session.getContextId());
        }

        // Assign Box.com OAuth information
        boxOAuthInfoRef = new AtomicReference<BoxOAuthInfo>(new BoxOAuthInfo(boxOAuthAccount, session));
        lastAccessed = System.nanoTime();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.oauth.access.OAuthAccess#initialise()
     */
    @Override
    public void initialise() throws OXException {
        synchronized (this) {
            OAuthAccount newAccount = recreateTokenIfExpired(true, boxOAuthInfoRef.get(), session);
            if (newAccount != null) {
                boxOAuthInfoRef.set(new BoxOAuthInfo(newAccount, session));
                lastAccessed = System.nanoTime();
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
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.oauth.access.OAuthAccess#ping()
     */
    @Override
    public boolean ping() throws OXException {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.oauth.access.OAuthAccess#dispose()
     */
    @Override
    public void dispose() {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.oauth.access.OAuthAccess#getClient()
     */
    @Override
    public OAuthClient<?> getClient() throws OXException {
        // TODO: Don't create a new client on every get; cache it
        BoxOAuthInfo boxOAuthInfo = boxOAuthInfoRef.get();
        BoxClient boxClient = new NonRefreshingBoxClient(boxOAuthInfo.clientId, boxOAuthInfo.clientSecret, new BoxResourceHub(), new BoxJSONParser(new BoxResourceHub()), (new BoxConfigBuilder()).build());
        applyOAuthToken(boxOAuthInfo, boxClient);
        return new OAuthClient<BoxClient>(boxClient);
    }

    /**
     * Gets the extended box client
     *
     * @return The extended box client
     */
    public OAuthClient<?> getExtendedClient() {
        // TODO: Don't create a new client on every get; cache it
        BoxOAuthInfo boxOAuthInfo = boxOAuthInfoRef.get();
        ExtendedNonRefreshingBoxClient boxClient = new ExtendedNonRefreshingBoxClient(boxOAuthInfo.clientId, boxOAuthInfo.clientSecret, new BoxResourceHub(), new BoxJSONParser(new BoxResourceHub()), (new BoxConfigBuilder()).build());
        applyOAuthToken(boxOAuthInfo, boxClient);
        return new OAuthClient<ExtendedNonRefreshingBoxClient>(boxClient);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.oauth.access.OAuthAccess#getAccountId()
     */
    @Override
    public int getAccountId() throws OXException {
        // Get OAuth account identifier from messaging account's configuration
        int oauthAccountId;
        {
            Map<String, Object> configuration = fsAccount.getConfiguration();
            if (null == configuration) {
                throw FileStorageExceptionCodes.MISSING_CONFIG.create(BoxConstants.ID, fsAccount.getId());
            }
            Object accountId = configuration.get("account");
            if (null == accountId) {
                throw FileStorageExceptionCodes.MISSING_CONFIG.create(BoxConstants.ID, fsAccount.getId());
            }
            if (accountId instanceof Integer) {
                oauthAccountId = ((Integer) accountId).intValue();
            } else {
                try {
                    oauthAccountId = Strings.parseInt(accountId.toString());
                } catch (NumberFormatException e) {
                    throw FileStorageExceptionCodes.MISSING_CONFIG.create(e, BoxConstants.ID, fsAccount.getId());
                }
            }
        }
        return oauthAccountId;
    }

    //////////////////////////////////////////// HELPERS ///////////////////////////////////////////////////

    /**
     * Apply access token and refresh token from OAuth account
     *
     * @param boxOAuthInfo
     */
    private void applyOAuthToken(BoxOAuthInfo boxOAuthInfo, BoxClient boxClient) {
        Map<String, Object> tokenSpec = new HashMap<String, Object>(6);
        OAuthAccount boxOAuthAccount = boxOAuthInfo.boxOAuthAccount;
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
     * @param boxOAuthInfo The {@link BoxOAuthInfo}
     * @param session The user's {@link Session}
     * @return the {@link OAuthAccount} with the updated token
     * @throws OXException if the token cannot be recreated
     */
    private OAuthAccount recreateTokenIfExpired(boolean considerExpired, BoxOAuthInfo boxOAuthInfo, Session session) throws OXException {
        // Create Scribe Box.com OAuth service
        OAuthAccount boxOAuthAccount = boxOAuthInfo.boxOAuthAccount;
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
                LOGGER.warn("Received invalid request_token from Box.com: {}. Response:{}{}", null == accessToken.getSecret() ? "null" : accessToken.getSecret(), Strings.getLineSeparator(), accessToken.getRawResponse());
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

    /**
     * {@link BoxOAuthInfo}
     */
    private static class BoxOAuthInfo {

        /** The associated OAuth account */
        final OAuthAccount boxOAuthAccount;

        /** The client identifier */
        final String clientId;

        /** The client secret */
        final String clientSecret;

        BoxOAuthInfo(OAuthAccount boxOAuthAccount, Session session) throws OXException {
            super();
            this.boxOAuthAccount = boxOAuthAccount;
            this.clientId = boxOAuthAccount.getMetaData().getAPIKey(session);
            this.clientSecret = boxOAuthAccount.getMetaData().getAPISecret(session);
        }
    }
}
