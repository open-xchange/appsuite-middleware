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

package com.openexchange.file.storage.dropbox.access;

import org.scribe.builder.ServiceBuilder;
import org.scribe.exceptions.OAuthException;
import org.scribe.model.Token;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.http.HttpRequestor;
import com.dropbox.core.v2.DbxClientV2;
import com.openexchange.cluster.lock.ClusterLockService;
import com.openexchange.cluster.lock.ClusterTask;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.dropbox.DropboxConfiguration;
import com.openexchange.file.storage.dropbox.DropboxConstants;
import com.openexchange.file.storage.dropbox.DropboxServices;
import com.openexchange.oauth.AbstractReauthorizeClusterTask;
import com.openexchange.oauth.OAuthAccount;
import com.openexchange.oauth.OAuthService;
import com.openexchange.oauth.OAuthUtil;
import com.openexchange.oauth.access.AbstractOAuthAccess;
import com.openexchange.oauth.access.OAuthAccess;
import com.openexchange.oauth.access.OAuthClient;
import com.openexchange.oauth.api.DropboxApi2;
import com.openexchange.oauth.scope.OXScope;
import com.openexchange.policy.retry.ExponentialBackOffRetryPolicy;
import com.openexchange.session.Session;

/**
 * {@link DropboxOAuth2Access}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class DropboxOAuth2Access extends AbstractOAuthAccess {

    private final FileStorageAccount fsAccount;

    /**
     * Initializes a new {@link DropboxOAuth2Access}.
     */
    public DropboxOAuth2Access(FileStorageAccount fsAccount, Session session) {
        super(session);
        this.fsAccount = fsAccount;
    }

    @Override
    public void initialize() throws OXException {
        OAuthService oAuthService = DropboxServices.getService(OAuthService.class);
        try {
            OAuthAccount oauthAccount = oAuthService.getAccount(getSession(), getAccountId());
            verifyAccount(oauthAccount, oAuthService, OXScope.drive);
            HttpRequestor httpRequestor = new ApacheHttpClientHttpRequestor();
            DbxRequestConfig config = DbxRequestConfig.newBuilder(DropboxConfiguration.getInstance().getProductName()).withHttpRequestor(httpRequestor).build();
            String accessToken = oauthAccount.getToken();
            DbxClientV2 dbxClient = new DbxClientV2(config, accessToken);
            OAuthClient<DropboxClient> oAuthClient = new OAuthClient<DropboxClient>(new DropboxClient(dbxClient, httpRequestor), accessToken);
            setOAuthClient(oAuthClient);
            setOAuthAccount(oauthAccount);
        } catch (RuntimeException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public OAuthAccess ensureNotExpired() throws OXException {
        if (isExpired()) {
            synchronized (this) {
                if (isExpired()) {
                    if (getOAuthAccount() == null) {
                        initialize();
                    }
                    ClusterLockService clusterLockService = DropboxServices.getService(ClusterLockService.class);
                    clusterLockService.runClusterTask(new DropboxReauthorizeClusterTask(getSession(), getOAuthAccount()), new ExponentialBackOffRetryPolicy());
                    // Re-set account and client and make all proper connections
                    initialize();
                }
            }
        }
        return this;
    }

    @Override
    public boolean ping() throws OXException {
        try {
            DropboxClient client = (DropboxClient) getClient().client;
            client.dbxClient.users().getCurrentAccount();
            return true;
        } catch (DbxException e) {
            throw DropboxExceptionHandler.handle(e, getSession(), getOAuthAccount());
        }
    }

    @Override
    public int getAccountId() throws OXException {
        try {
            return getAccountId(fsAccount.getConfiguration());
        } catch (IllegalArgumentException e) {
            throw FileStorageExceptionCodes.MISSING_CONFIG.create(DropboxConstants.ID, fsAccount.getId());
        }
    }

    private class DropboxReauthorizeClusterTask extends AbstractReauthorizeClusterTask implements ClusterTask<OAuthAccount> {

        /**
         * Initialises a new {@link DropboxReauthorizeClusterTask}.
         */
        public DropboxReauthorizeClusterTask(Session session, OAuthAccount cachedAccount) {
            super(DropboxServices.getServices(), session, cachedAccount);
        }

        @Override
        public Token reauthorize() throws OXException {
            ServiceBuilder serviceBuilder = new ServiceBuilder().provider(DropboxApi2.class);
            serviceBuilder.apiKey(getCachedAccount().getMetaData().getAPIKey(getSession())).apiSecret(getCachedAccount().getMetaData().getAPISecret(getSession()));
            DropboxApi2.DropboxOAuth2Service scribeOAuthService = DropboxApi2.DropboxOAuth2Service.class.cast(serviceBuilder.build());

            // Refresh the token
            try {
                return scribeOAuthService.getAccessToken(new Token(getCachedAccount().getToken(), getCachedAccount().getSecret()), null);
            } catch (OAuthException e) {
                OAuthAccount dbAccount = getDBAccount();
                throw OAuthUtil.handleScribeOAuthException(e, dbAccount, getSession());
            }
        }
    }
}
