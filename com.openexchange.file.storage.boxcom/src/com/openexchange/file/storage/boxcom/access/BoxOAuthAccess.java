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
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import com.box.sdk.BoxAPIConnection;
import com.box.sdk.BoxAPIException;
import com.box.sdk.BoxAPIRequest;
import com.box.sdk.BoxAPIResponse;
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
import com.openexchange.oauth.DefaultOAuthToken;
import com.openexchange.oauth.OAuthAccount;
import com.openexchange.oauth.OAuthConstants;
import com.openexchange.oauth.OAuthExceptionCodes;
import com.openexchange.oauth.OAuthService;
import com.openexchange.oauth.OAuthServiceMetaData;
import com.openexchange.oauth.OAuthUtil;
import com.openexchange.oauth.access.AbstractOAuthAccess;
import com.openexchange.oauth.access.OAuthAccess;
import com.openexchange.oauth.access.OAuthClient;
import com.openexchange.session.Session;

/**
 * {@link BoxOAuthAccess}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class BoxOAuthAccess extends AbstractOAuthAccess {

    private final FileStorageAccount fsAccount;

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
            verifyAccount(boxOAuthAccount);
            setOAuthAccount(boxOAuthAccount);
            createOAuthClient(boxOAuthAccount);
        }
    }

    @Override
    public void revoke() throws OXException {
        synchronized (this) {
            // No Java API call
            // More information here: https://docs.box.com/reference#revoke
            try {
                URL url = new URL("https://api.box.com/oauth2/revoke?client_id=" + getOAuthAccount().getMetaData().getId() + "&client_secret" + getOAuthAccount().getMetaData().getAPISecret(getSession()) + "&token=" + getOAuthAccount().getToken());
                BoxAPIRequest request = new BoxAPIRequest((BoxAPIConnection) getOAuthClient().client, url, "GET");
                BoxAPIResponse apiResponse = request.send();

                // The Box SDK already checks for status code 200, so no need to check again
                apiResponse.getResponseCode();

                return;
            } catch (BoxAPIException e) {
                throw FileStorageExceptionCodes.PROTOCOL_ERROR.create(e, "HTTP", e.getMessage());
            } catch (IOException e) {
                throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
            }
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

        // Box SDK performs an automatic access token refresh, so we need to see if the tokens were renewed
        try {
            if (!oAuthAccount.getToken().equals(apiConnection.getAccessToken()) || !oAuthAccount.getSecret().equals(apiConnection.getRefreshToken())) {
                ClusterLockService clusterLockService = Services.getService(ClusterLockService.class);
                clusterLockService.runClusterTask(new BoxReauthorizeClusterTask(getSession(), oAuthAccount), new ExponentialBackOffRetryPolicy());
            }
        } catch (BoxAPIException e) {
            if (e.getResponse().contains("invalid_grant")) {
                //TODO: Maybe try to automatically re-authorise once instead of directly throwing an exception?
                String cburl = OAuthUtil.buildCallbackURL(getSession(), oAuthAccount);
                throw OAuthExceptionCodes.OAUTH_ACCESS_TOKEN_INVALID.create(oAuthAccount.getAPI().getFullName(), cburl);
            }
            throw OAuthExceptionCodes.OAUTH_ERROR.create(e.getMessage(), e);
        }
        return this;
    }

    //////////////////////////////////////////// HELPERS ///////////////////////////////////////////////////

    private void createOAuthClient(OAuthAccount account) throws OXException {
        OAuthServiceMetaData boxMetaData = account.getMetaData();
        BoxAPIConnection boxAPI = new BoxAPIConnection(boxMetaData.getAPIKey(getSession()), boxMetaData.getAPISecret(getSession()), account.getToken(), account.getSecret());
        OAuthClient<BoxAPIConnection> oAuthClient = new OAuthClient<>(boxAPI, account.getToken());
        setOAuthClient(oAuthClient);
    }

    private class BoxReauthorizeClusterTask extends AbstractReauthorizeClusterTask implements ClusterTask<OAuthAccount> {

        /**
         * Initialises a new {@link BoxOAuthAccess.BoxReauthorizeClusterTask}.
         */
        public BoxReauthorizeClusterTask(Session session, OAuthAccount cachedAccount) {
            super(session, cachedAccount);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.openexchange.cluster.lock.ClusterTask#perform()
         */
        @Override
        public OAuthAccount perform() throws OXException {
            OAuthService oauthService = Services.getService(OAuthService.class);
            OAuthAccount dbAccount = oauthService.getAccount(getCachedAccount().getId(), getSession(), getSession().getUserId(), getSession().getContextId());

            if (dbAccount.getToken().equals(getCachedAccount().getToken()) && dbAccount.getSecret().equals(getCachedAccount().getSecret())) {
                BoxAPIConnection apiConnection = (BoxAPIConnection) getClient().client;
                Map<String, Object> arguments = new HashMap<>();
                arguments.put(OAuthConstants.ARGUMENT_REQUEST_TOKEN, new DefaultOAuthToken(apiConnection.getAccessToken(), apiConnection.getRefreshToken()));
                arguments.put(OAuthConstants.ARGUMENT_SESSION, getSession());

                int userId = getSession().getUserId();
                int contextId = getSession().getContextId();
                oauthService.updateAccount(dbAccount.getId(), arguments, userId, contextId, dbAccount.getEnabledScopes());
                setOAuthAccount(oauthService.getAccount(dbAccount.getId(), getSession(), userId, contextId));
                return getOAuthAccount();
            } else {
                return dbAccount;
            }
        }

    }
}
