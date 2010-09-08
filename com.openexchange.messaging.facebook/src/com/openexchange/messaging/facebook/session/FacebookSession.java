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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.HttpWebConnection;
import com.gargoylesoftware.htmlunit.ScriptException;
import com.gargoylesoftware.htmlunit.ThreadedRefreshHandler;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebConnection;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlPasswordInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
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
            final FacebookSession newInstance = new FacebookSession(messagingAccount);
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
            final FacebookSession newInstance = new FacebookSession(messagingAccount);
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
     * The user's facebook home page.
     */
    private volatile HtmlPage homePage;

    /**
     * The web client to access user's facebook home page.
     */
    private volatile WebClient webClient;

    /**
     * Whether to log user out of current session as well as Facebook. If the user has sessions with other connected apps, these sessions
     * will be closed as well.
     */
    private final boolean performWebLogout;

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
     * The facebook login.
     */
    private final String login;

    /**
     * The facebook password.
     */
    private final String password;

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
     */
    private FacebookSession(final MessagingAccount messagingAccount) {
        super();
        mutex = new Object();
        performWebLogout = true;
        apiKey = FacebookConfiguration.getInstance().getApiKey();
        secretKey = FacebookConfiguration.getInstance().getSecretKey();
        /*
         * Read-in configuration
         */
        final Map<String, Object> accountConfiguration = messagingAccount.getConfiguration();
        this.login = accountConfiguration.get(FacebookConstants.FACEBOOK_LOGIN).toString();
        this.password = accountConfiguration.get(FacebookConstants.FACEBOOK_PASSWORD).toString();
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
        this.login = login;
        this.password = password;
        facebookUserId = -1L;
        performWebLogout = true;
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
                    facebookSession).append("\") alive for Facebook user ").append(login).toString());
            }
        } catch (final FacebookException e) {
            throw FacebookMessagingException.create(e);
        }
    }

    @Override
    public String toString() {
        if (connected) {
            return new StringBuilder(32).append("{ connected=true, login=").append(login).append(", facebookUserId=").append(facebookUserId).append(
                ", facebookSession=").append(facebookSession).append('}').toString();
        }
        return new StringBuilder(32).append("{ connected=false, login=").append(login).append('}').toString();
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
            login(true, true);
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
                login(false, true);
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
     * Gets the login.
     * 
     * @return The login
     */
    public String getLogin() {
        return login;
    }

    /**
     * Gets the password.
     * 
     * @return The password
     */
    public String getPassword() {
        return password;
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

    private void login(final boolean checkPermissions, final boolean useThreadedRefreshHandler) throws FacebookMessagingException {
        /*
         * Emulate a known client, hopefully keeping our profile low
         */
        WebClient wc = new WebClient(BrowserVersion.INTERNET_EXPLORER_7);
        webClient = wc;
        /*
         * JavaScript needs to be disabled for security reasons
         */
        wc.setJavaScriptEnabled(false);
        if (useThreadedRefreshHandler) {
            wc.setRefreshHandler(new ThreadedRefreshHandler());
        }
        /*
         * Create REST client
         */
        loginViaWebPage(wc);
        /*
         * Check permissions
         */
        if (checkPermissions && checkPermissions(wc, true)) {
            /*
             * Logout REST client to clean its state
             */
            logout();
            /*
             * ... and login again to bring up new permissions
             */
            wc = new WebClient(BrowserVersion.FIREFOX_2);
            webClient = wc;
            /*
             * Javascript needs to be disabled for security reasons
             */
            wc.setJavaScriptEnabled(false);
            if (useThreadedRefreshHandler) {
                wc.setRefreshHandler(new ThreadedRefreshHandler());
            }
            loginViaWebPage(wc);
        }
    }

    private static final int FORM_INDEX = 0;

    /**
     * Logins via web page and assigns facebook REST client, facebook user identifier, and session string.
     * 
     * @param webClient The web client
     * @return The obtained auth token
     * @throws FacebookMessagingException If login fails
     */
    private String loginViaWebPage(final WebClient webClient) throws FacebookMessagingException {
        try {
            final FacebookConfiguration configuration = FacebookConfiguration.getInstance();
            /*
             * Create REST client
             */
            final IFacebookRestClient<Object> client = new FacebookJaxbRestClient(apiKey, secretKey);
            facebookRestClient = client;
            /*
             * First, we need to get an auth-token to log in with
             */
            final String token = client.auth_createToken();
            /*
             * Access login page and get the login form
             */
            HtmlForm loginForm = null;
            final String nameOfUserField = configuration.getNameOfUserField();
            {
                final String actionOfLoginForm = configuration.getActionOfLoginForm();
                final List<HtmlForm> forms =
                    webClient.<HtmlPage> getPage(
                        new StringBuilder(64).append(configuration.getLoginPageBaseURL()).append("?api_key=").append(apiKey).append("&v=").append(
                            configuration.getApiVersion()).append("&auth_token=").append(token).toString()).getForms();
                final int size = forms.size();
                for (int i = 0; i < size; i++) {
                    if (FORM_INDEX == i) {
                        final HtmlForm form = forms.get(i);
                        if (form.getActionAttribute().startsWith(actionOfLoginForm) && (form.getInputsByName(nameOfUserField) != null)) {
                            loginForm = form;
                        }
                    }
                }
            }
            /*
             * Check if form was found
             */
            if (loginForm == null) {
                throw FacebookMessagingExceptionCodes.LOGIN_FORM_NOT_FOUND.create(configuration.getLoginPageBaseURL());
            }
            loginForm.<HtmlTextInput> getInputByName(nameOfUserField).setValueAttribute(login);
            loginForm.<HtmlPasswordInput> getInputByName(configuration.getNameOfPasswordField()).setValueAttribute(password);
            final HtmlPage pageAfterLogin = (HtmlPage) loginForm.submit(null);
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
                        throw FacebookMessagingExceptionCodes.FAILED_LOGIN.create(login);
                    }
                    final FacebookMessagingException fme = FacebookMessagingExceptionCodes.FAILED_LOGIN.create(login);
                    LOG.trace(
                        new StringBuilder("Login to facebook failed for login=").append(login).append(", password=").append(password).toString(),
                        fme);
                    throw fme;
                }
                throw e;
            }
            facebookUserId = client.users_getLoggedInUser();
            /*
             * Check if expected link after login is available
             */
            homePage = pageAfterLogin;
            /*-
             * 
            if (!checkLinkExistence(pageAfterLogin.getAnchors())) {
                throw FacebookMessagingExceptionCodes.FAILED_LOGIN.create(login);
            }
             */
            if (DEBUG) {
                final StringBuilder sb = new StringBuilder(64);
                sb.append("Connect successfully performed for user ").append(login);
                sb.append(" with session=").append(facebookSession).append(" and userId=").append(facebookUserId);
                LOG.debug(sb.toString());
            }
            return token;
        } catch (final FailingHttpStatusCodeException e) {
            throw FacebookMessagingExceptionCodes.COMMUNICATION_ERROR.create(e, e.getMessage());
        } catch (final MalformedURLException e) {
            throw FacebookMessagingExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (final ElementNotFoundException e) {
            throw FacebookMessagingExceptionCodes.COMMUNICATION_ERROR.create(e, e.getMessage());
        } catch (final IOException e) {
            throw FacebookMessagingExceptionCodes.IO_ERROR.create(e, e.getMessage());
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
                            login,
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
                            login,
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
                            login,
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
            HtmlPage page = webClient.<HtmlPage> getPage(getPromptURL(perms.toArray(new Permission[perms.size()])));
            /*
             * Temporary allow JavaScript
             */
            webClient.setJavaScriptEnabled(true);
            try {
                page = grantPermissions(page);
            } finally {
                webClient.setJavaScriptEnabled(false);
            }
            /*
             * Check if expected link after granting permission is available
             */
            homePage = page;
            /*-
             * 
            if (!checkLinkExistence(page.getAnchors())) {
                throw FacebookMessagingExceptionCodes.FAILED_LOGIN.create(login);
            }
             */
            return true;
        } catch (final ScriptException e) {
            if (!perms.isEmpty()) {
                final Permission p = perms.iterator().next();
                throw FacebookMessagingExceptionCodes.MISSING_PERMISSION.create(e, p.getName(), login, getPromptURL(p));
            }
            throw FacebookMessagingExceptionCodes.COMMUNICATION_ERROR.create(e, e.getMessage());
        } catch (final FacebookException e) {
            throw FacebookMessagingException.create(e);
        } catch (final FailingHttpStatusCodeException e) {
            throw FacebookMessagingExceptionCodes.COMMUNICATION_ERROR.create(e, e.getMessage());
        } catch (final MalformedURLException e) {
            throw FacebookMessagingExceptionCodes.COMMUNICATION_ERROR.create(e, e.getMessage());
        } catch (final IOException e) {
            throw FacebookMessagingExceptionCodes.COMMUNICATION_ERROR.create(e, e.getMessage());
        }
    }

    private HtmlPage grantPermissions(final HtmlPage page) throws FacebookMessagingException {
        final List<HtmlAnchor> anchors = page.getAnchors();
        for (final HtmlAnchor htmlAnchor : anchors) {
            final String attribute = htmlAnchor.getAttribute("onclick");
            if (null != attribute && attribute.startsWith("UIPermissions.onAllowPermission(")) {
                try {
                    return grantPermissions(htmlAnchor.<HtmlPage> click());
                } catch (final IOException e) {
                    throw FacebookMessagingExceptionCodes.IO_ERROR.create(e, e.getMessage());
                }

            }
        }
        return page;
    }

    private boolean no_checkLinkExistence(final List<HtmlAnchor> anchors) {
        final int size = anchors.size();
        final Iterator<HtmlAnchor> iterator = anchors.iterator();
        for (int i = 0; i < size; i++) {
            final String hrefAttribute = iterator.next().getHrefAttribute();
            if (hrefAttribute.startsWith("http://www.facebook.com/logout.php?h=")) {
                /*
                 * Found logout link
                 */
                return true;
            }
        }
        return false;
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
                /*
                 * Logout from web front-end to log user out of current session as well as Facebook
                 */
                if (performWebLogout) {
                    final HtmlPage tmp = homePage;
                    if (null != tmp) {
                        final List<HtmlAnchor> anchors = tmp.getAnchors();
                        for (final HtmlAnchor htmlAnchor : anchors) {
                            final String hrefAttribute = htmlAnchor.getHrefAttribute();
                            if (null != hrefAttribute && hrefAttribute.startsWith("http://www.facebook.com/logout.php?")) {
                                try {
                                    htmlAnchor.click();
                                } catch (final IOException e) {
                                    LOG.warn("Logout from facebook web front-end failed.", e);
                                }
                                break;
                            }
                        }
                    }
                }
            } catch (final FacebookException e) {
                LOG.warn("Logout of facebook failed.", e);
            } finally {
                facebookRestClient = null;
                facebookSession = null;
                facebookUserId = -1L;
                homePage = null;
                final WebClient tmp = webClient;
                if (null != tmp) {
                    closeWebClient(tmp);
                    webClient = null;
                }
            }
        }
    }

    /**
     * Closes specified {@link WebClient web client} in a safe manner.
     * 
     * @param webClient The web client to close
     */
    private static void closeWebClient(final WebClient webClient) {
        if (null != webClient) {
            /*
             * Close all windows
             */
            try {
                webClient.closeAllWindows();
            } catch (final Exception e) {
                LOG.error(e.getMessage(), e);
            }
            /*
             * Close associated manager
             */
            closeAssociatedManager(webClient);
        }
    }

    private static final Class<HttpWebConnection> CLASS_WEB_CON = HttpWebConnection.class;

    private static final Class<MultiThreadedHttpConnectionManager> CLASS_MUL_HTTP_CON_MANAGER = MultiThreadedHttpConnectionManager.class;

    private static final Class<HttpClient> CLASS_HTTP_CLIENT = HttpClient.class;

    /**
     * Closes the {@link MultiThreadedHttpConnectionManager manager} possibly associated with specified web client.
     * 
     * @param client The web client whose manager should be closed
     */
    private static void closeAssociatedManager(final WebClient client) {
        try {
            final WebConnection webConnection = client.getWebConnection();
            if (webConnection == null) {
                return;
            }
            if (!CLASS_WEB_CON.isInstance(webConnection)) {
                LOG.error(MessageFormat.format(
                    "Cannot close webclient: webConnection is not of class {0} but of class {1}",
                    CLASS_WEB_CON.getName(),
                    webConnection.getClass().getName()));
                return;
            }
            final Object httpClient = FacebookConstants.HTTP_CLIENT_FIELD.get(webConnection);
            if (!CLASS_HTTP_CLIENT.isInstance(httpClient)) {
                final String appendix = null == httpClient ? "is null" : "of class " + httpClient.getClass().getName();
                LOG.error(MessageFormat.format(
                    "Cannot close webclient: httpClient_ is not of class {0} but {1}",
                    CLASS_HTTP_CLIENT.getName(),
                    appendix));
                return;
            }
            final Object manager = FacebookConstants.CONNECTION_MANAGER_FIELD.get(httpClient);
            /*
             * Check manager instance
             */
            if (!CLASS_MUL_HTTP_CON_MANAGER.isInstance(manager)) {
                final String appendix = null == manager ? "is null" : "of class " + manager.getClass().getName();
                LOG.error(MessageFormat.format(
                    "Cannot close webclient: httpConnectionManager is not of class {0} but {1}",
                    CLASS_MUL_HTTP_CON_MANAGER.getName(),
                    appendix));
                return;
            }
            /*
             * Then shut down
             */
            ((MultiThreadedHttpConnectionManager) manager).shutdown();
        } catch (final IllegalArgumentException e) {
            LOG.error(e.getMessage(), e);
        } catch (final IllegalAccessException e) {
            LOG.error(e.getMessage(), e);
        } catch (final Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return;
    }

}
