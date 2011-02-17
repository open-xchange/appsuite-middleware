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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.messaging.facebook.session;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.ScriptException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.google.code.facebookapi.FacebookException;
import com.google.code.facebookapi.FacebookJaxbRestClient;
import com.google.code.facebookapi.IFacebookRestClient;
import com.google.code.facebookapi.Permission;
import com.google.code.facebookapi.ProfileField;
import com.openexchange.messaging.MessagingAccount;
import com.openexchange.messaging.MessagingException;
import com.openexchange.messaging.facebook.FacebookConfiguration;
import com.openexchange.messaging.facebook.FacebookConstants;
import com.openexchange.messaging.facebook.FacebookMessagingException;
import com.openexchange.messaging.facebook.FacebookMessagingExceptionCodes;
import com.openexchange.messaging.facebook.FacebookMessagingResource;
import com.openexchange.messaging.facebook.services.FacebookMessagingServiceRegistry;
import com.openexchange.oauth.OAuthAccount;
import com.openexchange.oauth.OAuthException;
import com.openexchange.oauth.OAuthService;
import com.openexchange.session.Session;

/**
 * {@link FacebookSession} - Represents a connected or un-connected facebook session.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.16
 */
public final class FacebookSession {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(FacebookMessagingResource.class);

    private static final boolean DEBUG = LOG.isDebugEnabled();

    /**
     * Gets the facebook session for given facebook messaging account.
     * 
     * @param messagingAccount The facebook messaging account providing credentials and settings
     * @param session The user session
     * @return The facebook session; either newly created or fetched from underlying registry
     * @throws FacebookMessagingException If a Facebook session could not be created
     */
    public static FacebookSession sessionFor(final MessagingAccount messagingAccount, final Session session) throws FacebookMessagingException {
        final FacebookSessionRegistry registry = FacebookSessionRegistry.getInstance();
        final int accountId = messagingAccount.getId();
        FacebookSession facebookSession = registry.getSession(session.getContextId(), session.getUserId(), accountId);
        if (null == facebookSession) {
            final FacebookSession newInstance = new FacebookSession(messagingAccount, session.getUserId(), session.getContextId());
            facebookSession = registry.addSession(session.getContextId(), session.getUserId(), accountId, newInstance);
            if (null == facebookSession) {
                facebookSession = newInstance;
            }
        }
        try {
            return facebookSession.touchLastAccessed();
        } catch (final FacebookMessagingException e) {
            /*
             * Create a new session
             */
            registry.purgeUserSession(session.getContextId(), session.getUserId(), accountId);
            final FacebookSession newInstance = new FacebookSession(messagingAccount, session.getUserId(), session.getContextId());
            facebookSession = registry.addSession(session.getContextId(), session.getUserId(), accountId, newInstance);
            if (null == facebookSession) {
                facebookSession = newInstance;
            }
            return facebookSession.touchLastAccessed();
        }
    }

    /**
     * The final mutex object.
     */
    private final Object mutex;

    /**
     * The connected flag.
     */
    private volatile boolean connected;

    /**
     * The facebook REST client.
     */
    private volatile IFacebookRestClient<Object> facebookRestClient;

    /**
     * The facebook API key.
     */
    private final String apiKey;

    /**
     * The facebook secret key.
     */
    private final String secretKey;

    /**
     * The OAuth account.
     */
    private final OAuthAccount oauthAccount;

    /**
     * The facebook user identifier; <code>-1</code> if not connected
     */
    private long facebookUserId;

    /**
     * The facebook session; <code>null</code> if not connected
     */
    private String facebookSession;

    /**
     * The last-accessed time stamp.
     */
    private volatile long lastAccessed;

    /**
     * Initializes a new {@link FacebookMessagingResource}.
     * 
     * @param messagingAccount The facebook messaging account providing credentials and settings
     * @throws MessagingException
     */
    private FacebookSession(final MessagingAccount messagingAccount, final int user, final int contextId) throws FacebookMessagingException {
        super();
        mutex = new Object();
        apiKey = FacebookConfiguration.getInstance().getApiKey();
        secretKey = FacebookConfiguration.getInstance().getSecretKey();
        /*
         * Read-in configuration
         */
        final Map<String, Object> accountConfiguration = messagingAccount.getConfiguration();
        final int oauthAccountId = Integer.parseInt(accountConfiguration.get(FacebookConstants.FACEBOOK_OAUTH_ACCOUNT).toString());
        final OAuthService oAuthService = FacebookMessagingServiceRegistry.getServiceRegistry().getService(OAuthService.class);
        try {
            oauthAccount = oAuthService.getAccount(oauthAccountId, user, contextId);
        } catch (final OAuthException e) {
            throw new FacebookMessagingException(e);
        }
        facebookUserId = -1L;
        // lastAccessed = System.currentTimeMillis();
    }

    /**
     * Initializes a new {@link FacebookMessagingResource} for test purpose.
     * 
     * @param login The facebook login
     * @param password The facebook password
     * @param apiKey The API key
     * @param secretKey The secret key
     */
    public FacebookSession(final String login, final String password, final String apiKey, final String secretKey) {
        super();
        mutex = new Object();
        connected = false;
        /*
         * Create facebook REST client
         */
        this.apiKey = apiKey;
        this.secretKey = secretKey;
        // this.login = login;
        // this.password = password;
        oauthAccount = null;
        facebookUserId = -1L;
        // lastAccessed = System.currentTimeMillis();
    }

    /**
     * Touches this session's last-accessed time stamp.
     * 
     * @return This session with last-accessed time stamp touched
     * @throws FacebookMessagingException If this Facebook session is no longer valid
     */
    public FacebookSession touchLastAccessed() throws FacebookMessagingException {
        if (!connected) {
            return this;
        }
        try {
            /*
             * Perform a simple, fast operation
             */
            facebookRestClient.users_getStandardInfo(
                Collections.singleton(Long.valueOf(facebookUserId)),
                Collections.singleton(ProfileField.UID));
            lastAccessed = System.currentTimeMillis();
        } catch (final FacebookException e) {
            LOG.error(e.getMessage(), e);
            close();
            throw FacebookMessagingExceptionCodes.COMMUNICATION_ERROR.create(e, e.getMessage());
        }
        return this;
    }

    /**
     * Gets the last-accessed time stamp.
     * 
     * @return The last-accessed time stamp
     */
    public long getLastAccessed() {
        return lastAccessed;
    }

    /**
     * Performs a dummy request to facebook REST server to keep session alive.
     * 
     * @throws FacebookMessagingException If dummy request fails
     */
    public void renewSession() throws FacebookMessagingException {
        if (!connected) {
            return;
        }
        try {
            /*
             * Perform a simple, fast operation with a new client constructed with current session ID
             */
            // final long s = System.currentTimeMillis();
            final IFacebookRestClient<Object> client = facebookRestClient;
            final FacebookJaxbRestClient secondClient =
                new FacebookJaxbRestClient(client.getApiKey(), client.getSecret(), client.getCacheSessionKey());
            final long uid = secondClient.users_getLoggedInUser();
            if (uid == facebookUserId) {
                secondClient.events_get(Long.valueOf(uid), null, null, null);
                // secondClient.users_getStandardInfo(Collections.singleton(Long.valueOf(uid)), Collections.singleton(ProfileField.UID));
            } else {
                client.events_get(Long.valueOf(facebookUserId), null, null, null);
                // client.users_getStandardInfo(Collections.singleton(Long.valueOf(facebookUserId)),
                // Collections.singleton(ProfileField.UID));
            }
            // final long d = System.currentTimeMillis() - s;
            // System.out.println("Renewal took " + d + "msec.");
            lastAccessed = System.currentTimeMillis();
            if (DEBUG) {
                LOG.debug(new StringBuilder("Performed dummy request to Facebook REST server to keep Facebook session (\"").append(
                    facebookSession).append("\") alive for Facebook user ").append(oauthAccount.getDisplayName()).toString());
            }
        } catch (final FacebookException e) {
            throw FacebookMessagingException.create(e);
        }
    }

    @Override
    public String toString() {
        if (connected) {
            return new StringBuilder(32).append("{ connected=true, login=").append(oauthAccount.getDisplayName()).append(
                ", facebookUserId=").append(facebookUserId).append(", facebookSession=").append(facebookSession).append('}').toString();
        }
        return new StringBuilder(32).append("{ connected=false, login=").append(oauthAccount.getDisplayName()).append('}').toString();
    }

    /**
     * Closes this facebook session.
     */
    public void close() {
        if (!connected) {
            return;
        }
        synchronized (mutex) {
            if (!connected) {
                return;
            }
            /*
             * Mark as unconnected regardless of exit path
             */
            connected = false;
            logout();
        }
    }

    /**
     * Connects this facebook session.
     * 
     * @throws MessagingException If connect fails
     */
    public void connect() throws MessagingException {
        if (connected) {
            return;
        }
        synchronized (mutex) {
            if (connected) {
                return;
            }
            login();
            connected = true;
        }
    }

    /**
     * Checks if this facebook session is connected
     * 
     * @return <code>true</code> if this facebook session is connected; otherwise <code>false</code>
     */
    public boolean isConnected() {
        return connected;
    }

    /**
     * Pings this facebook session to check settings.
     * 
     * @return <code>true</code> for successfully ping; otherwise <code>false</code>
     */
    public boolean ping() {
        if (connected) {
            return true;
        }
        synchronized (mutex) {
            if (connected) {
                return true;
            }
            try {
                login();
                logout();
                return true;
            } catch (final FacebookMessagingException e) {
                LOG.info("Ping to facebook failed.", e);
                return false;
            }
        }
    }

    /**
     * Gets the facebook REST client
     * 
     * @return The facebook REST client
     */
    public IFacebookRestClient<Object> getFacebookRestClient() {
        return facebookRestClient;
    }

    /**
     * Gets associated OAuth account.
     * 
     * @return The OAuth account
     */
    public OAuthAccount getOauthAccount() {
        return oauthAccount;
    }

    /**
     * Gets the (numeric) facebook user identifier.
     * 
     * @return The (numeric) facebook user identifier
     */
    public long getFacebookUserId() {
        return facebookUserId;
    }

    /**
     * Gets the facebook session identifier.
     * 
     * @return The facebook session identifier
     */
    public String getFacebookSession() {
        return facebookSession;
    }

    /*-
     * ----------------------------------------------------------------------------------------
     * ------------------------------------ HELPER METHODS ------------------------------------
     * ----------------------------------------------------------------------------------------
     */

    private void login() throws FacebookMessagingException {
        /*
         * Create REST client
         */
        loginViaWebPage();
    }

    /**
     * Logins via web page and assigns facebook REST client, facebook user identifier, and session string.
     * 
     * @param webClient The web client
     * @return The obtained auth token
     * @throws FacebookMessagingException If login fails
     */
    private String loginViaWebPage() throws FacebookMessagingException {
        try {
            //final FacebookConfiguration configuration = FacebookConfiguration.getInstance();
            /*
             * Create REST client
             */
            final IFacebookRestClient<Object> client = new FacebookJaxbRestClient(apiKey, secretKey);
            facebookRestClient = client;
            /*
             * First, we need to get an auth-token to log in with
             */
            // final String token = client.auth_createToken();
            final String token = oauthAccount.getToken();
            /*
             * Check for proper pager after login
             */
            if (client.isDesktop()) {
                LOG.warn("Application is set to \"Desktop\", but should be \"Web\".");
            }
            try {
                facebookSession = client.auth_getSession(token);
            } catch (final FacebookException e) {
                /*
                 * Failed login?
                 */
                if ("Invalid parameter".equals(e.getMessage())) {
                    if (!LOG.isTraceEnabled()) {
                        throw FacebookMessagingExceptionCodes.FAILED_LOGIN.create(oauthAccount.getDisplayName());
                    }
                    final FacebookMessagingException fme =
                        FacebookMessagingExceptionCodes.FAILED_LOGIN.create(oauthAccount.getDisplayName());
                    LOG.trace(
                        new StringBuilder("Login to facebook failed for login=").append(oauthAccount.getDisplayName()).toString(),
                        fme);
                    throw fme;
                }
                throw e;
            }
            facebookUserId = client.users_getLoggedInUser();
            /*-
             * 
            if (!checkLinkExistence(pageAfterLogin.getAnchors())) {
                throw FacebookMessagingExceptionCodes.FAILED_LOGIN.create(login);
            }
             */
            if (DEBUG) {
                final StringBuilder sb = new StringBuilder(64);
                sb.append("Connect successfully performed for user ").append(oauthAccount.getDisplayName());
                sb.append(" with session=").append(facebookSession).append(" and userId=").append(facebookUserId);
                LOG.debug(sb.toString());
            }
            return token;
        } catch (final FailingHttpStatusCodeException e) {
            throw FacebookMessagingExceptionCodes.COMMUNICATION_ERROR.create(e, e.getMessage());
        } catch (final ElementNotFoundException e) {
            throw FacebookMessagingExceptionCodes.COMMUNICATION_ERROR.create(e, e.getMessage());
        } catch (final FacebookException e) {
            throw FacebookMessagingException.create(e);
        }
    }

    /**
     * Checks presence of following permissions: {@link Permission#STATUS_UPDATE}, {@link Permission#READ_STREAM},
     * {@link Permission#PUBLISH_STREAM}, and {@link Permission#OFFLINE_ACCESS}.
     * 
     * @param webClient The web client
     * @param autoClick <code>true</code> to click HTML buttons automatically; otherwise <code>false</code> to throw an error
     * @return <code>true</code> if any permission was missing and therefore had to be granted; otherwise <code>false</code>
     * @throws FacebookMessagingException If a facebook messaging error occurs
     */
    private boolean checkPermissions(final WebClient webClient, final boolean autoClick) throws FacebookMessagingException {
        final EnumSet<Permission> perms = EnumSet.noneOf(Permission.class);
        try {
            final IFacebookRestClient<Object> client = facebookRestClient;
            /*
             * Gather missing permissions
             */
            {
                final Permission statusUpdate = Permission.STATUS_UPDATE;
                if (!client.users_hasAppPermission(statusUpdate)) {
                    if (!autoClick) {
                        throw FacebookMessagingExceptionCodes.MISSING_PERMISSION.create(
                            statusUpdate.getName(),
                            oauthAccount.getDisplayName(),
                            getPromptURL(statusUpdate));
                    }
                    perms.add(statusUpdate);
                }
            }
            {
                final Permission readStream = Permission.READ_STREAM;
                if (!client.users_hasAppPermission(readStream)) {
                    if (!autoClick) {
                        throw FacebookMessagingExceptionCodes.MISSING_PERMISSION.create(
                            readStream.getName(),
                            oauthAccount.getDisplayName(),
                            getPromptURL(readStream));
                    }
                    perms.add(readStream);
                }
            }
            {
                final Permission publishStream = Permission.PUBLISH_STREAM;
                if (!client.users_hasAppPermission(publishStream)) {
                    if (!autoClick) {
                        throw FacebookMessagingExceptionCodes.MISSING_PERMISSION.create(
                            publishStream.getName(),
                            oauthAccount.getDisplayName(),
                            getPromptURL(publishStream));
                    }
                    perms.add(publishStream);
                }
            }
            /*-
             * 
            {
                final Permission offlineAccess = Permission.OFFLINE_ACCESS;
                if (!client.users_hasAppPermission(offlineAccess)) {
                    if (!autoClick) {
                        throw FacebookMessagingExceptionCodes.MISSING_PERMISSION.create(
                            offlineAccess.getName(),
                            getPromptURL(offlineAccess));
                    }
                    perms.add(offlineAccess);
                }
            }
             */
            if (perms.isEmpty()) {
                return false;
            }
            return true;
        } catch (final ScriptException e) {
            if (!perms.isEmpty()) {
                final Permission p = perms.iterator().next();
                throw FacebookMessagingExceptionCodes.MISSING_PERMISSION.create(e, p.getName(), oauthAccount.getDisplayName(), getPromptURL(p));
            }
            throw FacebookMessagingExceptionCodes.COMMUNICATION_ERROR.create(e, e.getMessage());
        } catch (final FacebookException e) {
            throw FacebookMessagingException.create(e);
        } catch (final FailingHttpStatusCodeException e) {
            throw FacebookMessagingExceptionCodes.COMMUNICATION_ERROR.create(e, e.getMessage());
        }
    }

    private String getPromptURL(final Permission... permissions) {
        if (1 == permissions.length) {
            return new StringBuilder(128).append("http://www.facebook.com/connect/prompt_permissions.php?api_key=").append(apiKey).append(
                "&v=").append(FacebookConfiguration.getInstance().getApiVersion()).append("&ext_perm=").append(permissions[0].getName()).toString();
        }
        final StringBuilder sb =
            new StringBuilder(128).append("http://www.facebook.com/connect/prompt_permissions.php?api_key=").append(apiKey).append("&v=").append(
                FacebookConfiguration.getInstance().getApiVersion()).append("&ext_perm=");
        sb.append(permissions[0].getName());
        for (int i = 1; i < permissions.length; i++) {
            sb.append(',').append(permissions[i].getName());
        }
        return sb.toString();
    }

    /**
     * Performs a logout of currently assigned facebook REST client and releases assigned resources.
     */
    private void logout() {
        final IFacebookRestClient<Object> client = facebookRestClient;
        if (null != client) {
            try {
                /*
                 * Drop all
                 */
                if (!client.auth_expireSession()) {
                    LOG.warn("Logout of current facebook session failed.");
                }
                /*
                 * Clear cached user id
                 */
                client.setCacheSession(client.getCacheSessionKey(), null, client.getCacheSessionExpires());
            } catch (final FacebookException e) {
                LOG.warn("Logout of facebook failed.", e);
            } finally {
                facebookRestClient = null;
                facebookSession = null;
                facebookUserId = -1L;
            }
        }
    }

}
