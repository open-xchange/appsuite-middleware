/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.file.storage.boxcom.access;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.TimeUnit;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.BoxApi;
import org.scribe.builder.api.BoxApi.BoxApiService;
import org.scribe.model.Token;
import com.box.sdk.BoxAPIConnection;
import com.box.sdk.BoxAPIException;
import com.box.sdk.BoxUser;
import com.openexchange.cluster.lock.ClusterLockService;
import com.openexchange.cluster.lock.ClusterTask;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.boxcom.BoxClosure;
import com.openexchange.file.storage.boxcom.BoxConstants;
import com.openexchange.file.storage.boxcom.Services;
import com.openexchange.oauth.AbstractReauthorizeClusterTask;
import com.openexchange.oauth.OAuthAccount;
import com.openexchange.oauth.OAuthService;
import com.openexchange.oauth.OAuthServiceMetaData;
import com.openexchange.oauth.OAuthUtil;
import com.openexchange.oauth.access.AbstractOAuthAccess;
import com.openexchange.oauth.access.OAuthAccess;
import com.openexchange.oauth.access.OAuthClient;
import com.openexchange.oauth.scope.OXScope;
import com.openexchange.policy.retry.ExponentialBackOffRetryPolicy;
import com.openexchange.session.Session;

/**
 * {@link BoxOAuthAccess}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class BoxOAuthAccess extends AbstractOAuthAccess {

    private final FileStorageAccount fsAccount;

    private final static long THRESHOLD = TimeUnit.MINUTES.toMillis(5);

    /**
     * Initializes a new {@link BoxOAuthAccess}.
     */
    public BoxOAuthAccess(FileStorageAccount fsAccount, Session session) {
        super(session);
        this.fsAccount = fsAccount;
    }

    @Override
    public void initialize() throws OXException {
        synchronized (this) {
            // Grab Box.com OAuth account
            int oauthAccountId = getAccountId();
            OAuthService oAuthService = Services.getService(OAuthService.class);
            OAuthAccount boxOAuthAccount = oAuthService.getAccount(getSession(), oauthAccountId);
            verifyAccount(boxOAuthAccount, oAuthService, OXScope.drive);
            setOAuthAccount(boxOAuthAccount);
            createOAuthClient(boxOAuthAccount);
        }
    }

    @Override
    public boolean ping() throws OXException {
        BoxClosure<Boolean> closure = new BoxClosure<Boolean>() {

            @Override
            protected Boolean doPerform() throws OXException, BoxAPIException, UnsupportedEncodingException {
                try {
                    ensureNotExpired();
                    BoxAPIConnection api = (BoxAPIConnection) getClient().client;
                    BoxUser user = BoxUser.getCurrentUser(api);
                    user.getInfo();
                    return Boolean.TRUE;
                } catch (BoxAPIException e) {
                    if (e.getResponseCode() == 401 || e.getResponseCode() == 403) {
                        return Boolean.FALSE;
                    }
                    throw e;
                }
            }
        };
        return closure.perform(null, this, getSession()).booleanValue();
    }

    @Override
    public int getAccountId() throws OXException {
        try {
            return getAccountId(fsAccount.getConfiguration());
        } catch (IllegalArgumentException e) {
            throw FileStorageExceptionCodes.MISSING_CONFIG.create(e, BoxConstants.ID, fsAccount.getId());
        }
    }

    @Override
    public OAuthAccess ensureNotExpired() throws OXException {
        BoxAPIConnection apiConnection = (BoxAPIConnection) getClient().client;
        OAuthAccount oAuthAccount = getOAuthAccount();

        long timeNow = System.currentTimeMillis();
        long delta = timeNow - apiConnection.getLastRefresh();
        boolean expired = delta >= (apiConnection.getExpires() - THRESHOLD);
        if (expired) {
            ClusterLockService clusterLockService = Services.getService(ClusterLockService.class);
            OAuthAccount account = clusterLockService.runClusterTask(new BoxReauthorizeClusterTask(getSession(), oAuthAccount), new ExponentialBackOffRetryPolicy());
            setOAuthAccount(account);
        }
        return this;
    }

    //////////////////////////////////////////// HELPERS ///////////////////////////////////////////////////

    private void createOAuthClient(OAuthAccount account) throws OXException {
        OAuthServiceMetaData boxMetaData = account.getMetaData();
        BoxAPIConnection boxAPI = new BoxAPIConnection(boxMetaData.getAPIKey(getSession()), boxMetaData.getAPISecret(getSession()), account.getToken(), account.getSecret());
        boxAPI.setAutoRefresh(false);

        OAuthClient<BoxAPIConnection> oAuthClient = new OAuthClient<>(boxAPI, account.getToken());
        setOAuthClient(oAuthClient);
    }

    private class BoxReauthorizeClusterTask extends AbstractReauthorizeClusterTask implements ClusterTask<OAuthAccount> {

        /**
         * Initialises a new {@link BoxOAuthAccess.BoxReauthorizeClusterTask}.
         */
        public BoxReauthorizeClusterTask(Session session, OAuthAccount cachedAccount) {
            super(Services.getServices(), session, cachedAccount);
        }

        @Override
        public Token reauthorize() throws OXException {
            Session session = getSession();
            OAuthAccount dbAccount = getDBAccount();

            ServiceBuilder serviceBuilder = new ServiceBuilder().provider(BoxApi.class);
            serviceBuilder.apiKey(dbAccount.getMetaData().getAPIKey(session)).apiSecret(dbAccount.getMetaData().getAPISecret(session));
            BoxApi.BoxApiService oAuthService = (BoxApiService) serviceBuilder.build();
            Token accessToken = getTokenUsing(session, oAuthService);

            BoxAPIConnection apiConnection = (BoxAPIConnection) getClient().client;
            apiConnection.setLastRefresh(System.currentTimeMillis());
            apiConnection.setExpires(accessToken.getExpiry().getTime());
            apiConnection.setAccessToken(accessToken.getToken());
            apiConnection.setRefreshToken(accessToken.getSecret());

            return accessToken;
        }

        private Token getTokenUsing(Session session, BoxApi.BoxApiService oAuthService) throws OXException {
            OAuthAccount oAuthAccount = getCachedAccount();
            try {
                return oAuthService.getAccessToken(new Token(oAuthAccount.getToken(), oAuthAccount.getSecret()), null);
            } catch (org.scribe.exceptions.OAuthException e) {
                throw OAuthUtil.handleScribeOAuthException(e, oAuthAccount, session);
            }
        }
    }
}
