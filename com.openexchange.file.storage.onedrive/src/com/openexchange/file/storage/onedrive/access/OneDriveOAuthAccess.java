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

package com.openexchange.file.storage.onedrive.access;

import java.util.ArrayList;
import java.util.List;
import javax.net.ssl.SSLHandshakeException;
import org.json.JSONException;
import org.json.JSONObject;
import org.scribe.builder.ServiceBuilder;
import org.scribe.exceptions.OAuthException;
import org.scribe.model.Token;
import org.slf4j.Logger;
import com.openexchange.cluster.lock.ClusterLockService;
import com.openexchange.cluster.lock.ClusterTask;
import com.openexchange.exception.ExceptionUtils;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.onedrive.OneDriveClosure;
import com.openexchange.file.storage.onedrive.OneDriveConstants;
import com.openexchange.file.storage.onedrive.osgi.Services;
import com.openexchange.java.Strings;
import com.openexchange.microsoft.graph.api.exception.MicrosoftGraphAPIExceptionCodes;
import com.openexchange.microsoft.graph.onedrive.MicrosoftGraphDriveService;
import com.openexchange.net.ssl.exception.SSLExceptionCode;
import com.openexchange.oauth.AbstractReauthorizeClusterTask;
import com.openexchange.oauth.OAuthAccount;
import com.openexchange.oauth.OAuthExceptionCodes;
import com.openexchange.oauth.OAuthService;
import com.openexchange.oauth.access.AbstractOAuthAccess;
import com.openexchange.oauth.access.OAuthAccess;
import com.openexchange.oauth.access.OAuthClient;
import com.openexchange.oauth.api.MicrosoftGraphApi;
import com.openexchange.oauth.scope.OXScope;
import com.openexchange.policy.retry.ExponentialBackOffRetryPolicy;
import com.openexchange.session.Session;

/**
 * {@link OneDriveOAuthAccess}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class OneDriveOAuthAccess extends AbstractOAuthAccess {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(OneDriveOAuthAccess.class);

    private final FileStorageAccount fsAccount;

    /**
     * Initializes a new {@link OneDriveOAuthAccess}.
     */
    public OneDriveOAuthAccess(FileStorageAccount fsAccount, Session session) {
        super(session);
        this.fsAccount = fsAccount;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.oauth.access.AbstractOAuthAccess#dispose()
     */
    @Override
    public void dispose() {
        // Nothing to dispose
        super.dispose();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.oauth.access.OAuthAccess#initialize()
     */
    @Override
    public void initialize() throws OXException {
        synchronized (this) {
            int oauthAccountId = getAccountId();
            OAuthAccount liveconnectOAuthAccount;
            OAuthService oAuthService = Services.getService(OAuthService.class);
            {
                liveconnectOAuthAccount = oAuthService.getAccount(getSession(), oauthAccountId);
            }
            setOAuthAccount(liveconnectOAuthAccount);

            OAuthAccount newAccount = recreateTokenIfExpired(liveconnectOAuthAccount, getSession());
            if (newAccount != null) {
                setOAuthAccount(newAccount);
                liveconnectOAuthAccount = newAccount;
            }
            verifyAccount(liveconnectOAuthAccount, oAuthService, OXScope.drive);
            setOAuthClient(new OAuthClient<>(Services.getService(MicrosoftGraphDriveService.class), getOAuthAccount().getToken()));
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.oauth.access.OAuthAccess#ping()
     */
    @Override
    public boolean ping() throws OXException {
        OneDriveClosure<Boolean> closure = new OneDriveClosure<Boolean>() {

            @Override
            protected Boolean doPerform() throws OXException {
                MicrosoftGraphDriveService client = (MicrosoftGraphDriveService) getOAuthClient().client;
                try {
                    client.getRootFolderId(getOAuthAccount().getToken());
                } catch (OXException e) {
                    if (MicrosoftGraphAPIExceptionCodes.ACCESS_DENIED.equals(e) || MicrosoftGraphAPIExceptionCodes.UNAUTHENTICATED.equals(e)) {
                        return Boolean.FALSE;
                    }
                }
                return Boolean.TRUE;
            }
        };
        return closure.perform(null, getSession()).booleanValue();
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
            throw FileStorageExceptionCodes.MISSING_CONFIG.create(e, OneDriveConstants.ID, fsAccount.getId());
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
                if (isExpired()) {
                    initialize();
                }
            }
        }
        return this;
    }

    /**
     * Re-creates the token if expired
     *
     * @param oauthAccount The OAuth account
     * @param session The groupware session
     * @return The updated OAuth account if the token is expired; <code>null</code> otherwise
     * @throws OXException if an error is occurred
     */
    private OAuthAccount recreateTokenIfExpired(OAuthAccount oauthAccount, Session session) throws OXException {
        // Check expiration
        if (isExpired(oauthAccount, session)) {
            // Expired...
            ClusterLockService clusterLockService = Services.getService(ClusterLockService.class);
            return clusterLockService.runClusterTask(new OneDriveReauthorizeClusterTask(session, oauthAccount), new ExponentialBackOffRetryPolicy());
        }
        return null;
    }

    /**
     * Checks whether the token is expired
     * 
     * @param oauthAccount The OAuth account
     * @param session The groupware session
     * @return <code>true</code> if the token is expired, <code>false</code> otherwise
     * @throws OXException if an error is occurred
     */
    private boolean isExpired(OAuthAccount oauthAccount, Session session) throws OXException {
        // Create Scribe Microsoft OneDrive OAuth service
        ServiceBuilder serviceBuilder = new ServiceBuilder().provider(MicrosoftGraphApi.class);
        serviceBuilder.apiKey(oauthAccount.getMetaData().getAPIKey(session)).apiSecret(oauthAccount.getMetaData().getAPISecret(session));
        MicrosoftGraphApi.MicrosoftGraphService scribeOAuthService = (MicrosoftGraphApi.MicrosoftGraphService) serviceBuilder.build();
        return scribeOAuthService.isExpired(oauthAccount.getToken());
    }

    /**
     * {@link OneDriveReauthorizeClusterTask} - The reauthorise cluster task
     */
    private static class OneDriveReauthorizeClusterTask extends AbstractReauthorizeClusterTask implements ClusterTask<OAuthAccount> {

        /**
         * Initialises a new {@link OneDriveOAuthAccess.OneDriveReauthorizeClusterTask}.
         */
        public OneDriveReauthorizeClusterTask(Session session, OAuthAccount cachedAccount) {
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
            OAuthAccount cachedAccount = getCachedAccount();

            String refreshToken = cachedAccount.getSecret();
            if (Strings.isEmpty(refreshToken)) {
                // Impossible request a new access token without a refresh token. Manual reauthorization is required.
                throw OAuthExceptionCodes.OAUTH_ACCESS_TOKEN_INVALID.create(cachedAccount.getDisplayName(), cachedAccount.getId(), session.getUserId(), session.getContextId());
            }

            ServiceBuilder serviceBuilder = new ServiceBuilder().provider(MicrosoftGraphApi.class);
            serviceBuilder.apiKey(cachedAccount.getMetaData().getAPIKey(session)).apiSecret(cachedAccount.getMetaData().getAPISecret(session));
            MicrosoftGraphApi.MicrosoftGraphService scribeOAuthService = (MicrosoftGraphApi.MicrosoftGraphService) serviceBuilder.build();

            try {
                Token accessToken = scribeOAuthService.getAccessToken(new Token(cachedAccount.getToken(), refreshToken), null);
                if (Strings.isEmpty(accessToken.getSecret())) {
                    LOGGER.warn("Received invalid request_token from Live Connect: {}. Response:{}{}", null == accessToken.getSecret() ? "null" : accessToken.getSecret(), Strings.getLineSeparator(), accessToken.getRawResponse());
                }
                return accessToken;
            } catch (org.scribe.exceptions.OAuthException e) {
                throw handleScribeOAuthException(e, cachedAccount, session);
            }
        }
    }

    /**
     * Handles the specified {@link OAuthException}
     * 
     * @param e The exception to handle
     * @param oauthAccount The {@link OAuthAccount}
     * @param session The groupware {@link Session}
     * @return the appropriate {@link OXException}
     */
    static OXException handleScribeOAuthException(org.scribe.exceptions.OAuthException e, OAuthAccount oauthAccount, Session session) {
        if (ExceptionUtils.isEitherOf(e, SSLHandshakeException.class)) {
            List<Object> displayArgs = new ArrayList<>(2);
            displayArgs.add(SSLExceptionCode.extractArgument(e, "fingerprint"));
            displayArgs.add("graph.microsoft.com");
            return SSLExceptionCode.UNTRUSTED_CERTIFICATE.create(e, displayArgs.toArray(new Object[] {}));
        }

        String exMessage = e.getMessage();
        String errorMsg = parseErrorFrom(exMessage);
        if (Strings.isEmpty(errorMsg)) {
            return OAuthExceptionCodes.OAUTH_ERROR.create(e, exMessage);
        }
        if (exMessage.contains("invalid_grant") || exMessage.contains("invalid_request")) {
            if (null != oauthAccount) {
                return OAuthExceptionCodes.OAUTH_ACCESS_TOKEN_INVALID.create(e, oauthAccount.getDisplayName(), oauthAccount.getId(), session.getUserId(), session.getContextId());
            }
            return OAuthExceptionCodes.INVALID_ACCOUNT.create(e, new Object[0]);
        }
        return OAuthExceptionCodes.OAUTH_ERROR.create(e, exMessage);
    }

    /**
     * Parses the errors from the specified message
     * 
     * @param message The message to parse errors from
     * @return The parsed error message, or <code>null</code> if no message could be parsed
     */
    private static String parseErrorFrom(String message) {
        if (Strings.isEmpty(message)) {
            return null;
        }

        String marker = "Can't extract a token from this: '";
        int pos = message.indexOf(marker);
        if (pos < 0) {
            return null;
        }

        try {
            JSONObject jo = new JSONObject(message.substring(pos + marker.length(), message.length() - 1));
            return jo.optString("error", null);
        } catch (JSONException e) {
            // Apparent no JSON response
            return null;
        }
    }
}
