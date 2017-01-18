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

import java.io.UnsupportedEncodingException;
import java.util.concurrent.TimeUnit;
import org.scribe.model.Token;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import com.openexchange.oauth.OAuthAccount;
import com.openexchange.oauth.OAuthExceptionCodes;
import com.openexchange.oauth.OAuthService;
import com.openexchange.oauth.OAuthServiceMetaData;
import com.openexchange.oauth.access.AbstractOAuthAccess;
import com.openexchange.oauth.access.OAuthAccess;
import com.openexchange.oauth.access.OAuthClient;
import com.openexchange.session.Session;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;

/**
 * {@link BoxOAuthAccess}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class BoxOAuthAccess extends AbstractOAuthAccess {

    private static final Logger LOGGER = LoggerFactory.getLogger(BoxOAuthAccess.class);

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
        final BoxAPIConnection apiConnection = (BoxAPIConnection) getClient().client;
        OAuthAccount oAuthAccount = getOAuthAccount();

        String accessToken = getAccessToken(apiConnection);

        // Box SDK performs an automatic access token refresh, so we need to see if the tokens were renewed
        if (!oAuthAccount.getToken().equals(accessToken) || !oAuthAccount.getSecret().equals(apiConnection.getRefreshToken())) {
            ClusterLockService clusterLockService = Services.getService(ClusterLockService.class);
            OAuthAccount account = clusterLockService.runClusterTask(new BoxReauthorizeClusterTask(getSession(), oAuthAccount), new ExponentialBackOffRetryPolicy());
            setOAuthAccount(account);
        }
        return this;
    }

    //////////////////////////////////////////// HELPERS ///////////////////////////////////////////////////

    /**
     * Retrieves the access token from the specified {@link BoxAPIConnection}. This method spawns
     * two {@link Runnable} tasks with the {@link TimerService}: one to fetch the access token from
     * box.com and a second to act as a guard for the first one in order to cancel it after a predefined
     * amount of time (60 seconds) in case it gets stuck due to
     * <a href="https://bugs.openjdk.java.net/browse/JDK-8075484">JDK-8075484</a> (Bug 51016).
     * 
     * @param apiConnection The {@link BoxAPIConnection}
     * @return The access token
     * @throws OXException If the access token couldn't not be retrieved due to timeout
     */
    private String getAccessToken(final BoxAPIConnection apiConnection) throws OXException {
        TimerService timerService = Services.getService(TimerService.class);

        GetAccessTokenRunnable task = new GetAccessTokenRunnable(apiConnection);
        ScheduledTimerTask scheduled = timerService.schedule(task, 0);

        TaskStopper taskStopper = new TaskStopper(scheduled);
        timerService.schedule(taskStopper, 60, TimeUnit.SECONDS);

        if (taskStopper.isTimedOut()) {
            LOGGER.debug("Failed to fetch the access token for the box.com file storage account {} for user {} in context {}. box.com is facing some connectivity issues at the moment.", getAccountId(), getSession().getUserId(), getSession().getContextId());
            throw OAuthExceptionCodes.CONNECT_ERROR.create();
        }
        return task.getAccessToken();
    }

    /**
     * Creates an OAuth client for the specified {@link OAuthAccount}
     * 
     * @param account The {@link OAuthAccount} for which to create an {@link OAuthClient}
     * @throws OXException If the creation fails
     */
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
            super(Services.getServices(), session, cachedAccount);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.openexchange.cluster.lock.ClusterTask#perform()
         */
        @Override
        public Token reauthorize() throws OXException {
            // Box SDK performs an automatic access token refresh, therefore the access token and refresh token
            // should already be present in the BoxAPIConnection instance.
            BoxAPIConnection apiConnection = (BoxAPIConnection) getClient().client;
            String accessToken = getAccessToken(apiConnection);
            return new Token(accessToken, apiConnection.getRefreshToken());
        }
    }

    /**
     * {@link GetAccessTokenRunnable}
     */
    private class GetAccessTokenRunnable implements Runnable {

        String accessToken;
        private BoxAPIConnection apiConnection;

        /**
         * Initialises a new {@link BoxOAuthAccess.GetAccessTokenRunnable}.
         * 
         * @param apiConnection The {@link BoxAPIConnection}
         */
        public GetAccessTokenRunnable(BoxAPIConnection apiConnection) {
            super();
            this.apiConnection = apiConnection;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Runnable#run()
         */
        @Override
        public void run() {
            accessToken = apiConnection.getAccessToken();
        }

        /**
         * Gets the accessToken
         *
         * @return The accessToken
         */
        public String getAccessToken() {
            return accessToken;
        }
    }

    /**
     * {@link TaskStopper} - Stops (cancels) a {@link ScheduledTimerTask}
     */
    private class TaskStopper implements Runnable {

        boolean timedOut;
        private ScheduledTimerTask scheduledTimerTask;

        /**
         * Initialises a new {@link BoxOAuthAccess.TaskStopper}.
         * 
         * @param scheduledTimerTask The {@link ScheduledTimerTask} to cancel
         */
        public TaskStopper(ScheduledTimerTask scheduledTimerTask) {
            super();
            this.scheduledTimerTask = scheduledTimerTask;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Runnable#run()
         */
        @Override
        public void run() {
            timedOut = scheduledTimerTask.cancel(true);
        }

        /**
         * Gets the timedOut
         *
         * @return The timedOut
         */
        public boolean isTimedOut() {
            return timedOut;
        }
    }
}
