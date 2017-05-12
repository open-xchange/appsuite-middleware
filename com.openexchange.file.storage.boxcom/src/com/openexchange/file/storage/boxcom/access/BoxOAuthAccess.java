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

import static com.openexchange.java.Autoboxing.I;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.TimeUnit;
import org.json.JSONException;
import org.json.JSONObject;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.BoxApi;
import org.scribe.builder.api.BoxApi.BoxApiService;
import org.scribe.model.Token;
import com.box.sdk.BoxAPIConnection;
import com.box.sdk.BoxAPIException;
import com.box.sdk.BoxUser;
import com.openexchange.cluster.lock.ClusterLockService;
import com.openexchange.cluster.lock.ClusterTask;
import com.openexchange.cluster.lock.policies.ExponentialBackOffRetryPolicy;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.boxcom.BoxClosure;
import com.openexchange.file.storage.boxcom.BoxConstants;
import com.openexchange.file.storage.boxcom.Services;
import com.openexchange.oauth.AbstractReauthorizeClusterTask;
import com.openexchange.oauth.API;
import com.openexchange.oauth.OAuthAccount;
import com.openexchange.oauth.OAuthExceptionCodes;
import com.openexchange.oauth.OAuthService;
import com.openexchange.oauth.OAuthServiceMetaData;
import com.openexchange.oauth.access.AbstractOAuthAccess;
import com.openexchange.oauth.access.OAuthAccess;
import com.openexchange.oauth.access.OAuthClient;
import com.openexchange.oauth.scope.OXScope;
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
            OAuthAccount boxOAuthAccount = oAuthService.getAccount(oauthAccountId, getSession(), getSession().getUserId(), getSession().getContextId());
            verifyAccount(boxOAuthAccount, OXScope.drive);
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

        /*
         * (non-Javadoc)
         *
         * @see com.openexchange.cluster.lock.ClusterTask#perform()
         */
        @Override
        public Token reauthorize() throws OXException {
            Session session = getSession();
            OAuthAccount dbAccount = getDBAccount();

            final ServiceBuilder serviceBuilder = new ServiceBuilder().provider(BoxApi.class);
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
                String message = e.getMessage();
                if (null == message) {
                    throw OAuthExceptionCodes.OAUTH_ERROR.create(e, "OAuth error");
                }

                // Check for JSON content
                int startPos = message.indexOf('{');
                if (startPos >= 0) {
                    int endPos = message.lastIndexOf('}');
                    if (endPos > startPos) {
                        try {
                            JSONObject jError = new JSONObject(message.substring(startPos, endPos + 1));
                            String error = jError.optString("error");
                            if (null != error) {
                                if ("invalid_grant".equals(error)) {
                                    API api = oAuthAccount.getAPI();
                                    throw OAuthExceptionCodes.OAUTH_ACCESS_TOKEN_INVALID.create(e, api.getName(), I(oAuthAccount.getId()), I(session.getUserId()), I(session.getContextId()));
                                }

                                throw OAuthExceptionCodes.INVALID_ACCOUNT_EXTENDED.create(e, oAuthAccount.getDisplayName(), I(oAuthAccount.getId()));
                            }
                        } catch (JSONException je) {
                            // No JSON...
                        }
                    }
                }

                throw OAuthExceptionCodes.OAUTH_ERROR.create(e, message);
            }
        }
    }
}
