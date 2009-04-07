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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.mail.api;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.mail.MailException;
import com.openexchange.mail.config.MailConfigException;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.partmodifier.DummyPartModifier;
import com.openexchange.mail.partmodifier.PartModifier;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountException;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.server.ServiceException;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.tools.PasswordUtil;

/**
 * {@link MailConfig} - The user-specific mail properties; e.g. containing user's login data.
 * <p>
 * Provides access to global mail properties.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class MailConfig {

    public static enum BoolCapVal {

        /**
         * TRUE
         */
        TRUE("true"),
        /**
         * FALSE
         */
        FALSE("false"),
        /**
         * AUTO
         */
        AUTO("auto");

        private final String str;

        private BoolCapVal(final String str) {
            this.str = str;
        }

        @Override
        public String toString() {
            return str;
        }

        /**
         * Parses given capability value. If given value equals ignore-case to string <code>true</code>, constant {@link #TRUE} will be
         * returned. Else if given value equals ignore-case to string <code>auto</code>, constant {@link #AUTO} will be returned. Otherwise
         * {@link #FALSE} will be returned.
         * 
         * @param capVal - the string value to parse
         * @return an instance of <code>BoolCapVal</code>: either {@link #TRUE}, {@link #FALSE}, or {@link #AUTO}
         */
        public final static BoolCapVal parseBoolCapVal(final String capVal) {
            if (TRUE.str.equalsIgnoreCase(capVal)) {
                return TRUE;
            } else if (AUTO.str.equalsIgnoreCase(capVal)) {
                return AUTO;
            }
            return FALSE;
        }
    }

    public static enum LoginSource {

        /**
         * Login is taken from user.imapLogin kept in storage; e.g. <code>test</code>
         */
        USER_IMAPLOGIN("login"),
        /**
         * Login is taken from user.mail kept in storage; e.g. <code>test@foo.bar</code>
         */
        PRIMARY_EMAIL("mail"),
        /**
         * Login is user's name; e.g. <code>test</code>
         */
        USER_NAME("name");

        private final String str;

        private LoginSource(final String str) {
            this.str = str;
        }

        @Override
        public String toString() {
            return str;
        }

        /**
         * Parses specified string into a login source.
         * 
         * @param loginSourceStr The string to parse to a login source
         * @return An appropriate login source or <code>null</code> if string could not be parsed to a login source
         */
        public static final LoginSource parse(final String loginSourceStr) {
            final LoginSource[] values = LoginSource.values();
            for (final LoginSource loginSource : values) {
                if (loginSource.str.equalsIgnoreCase(loginSourceStr)) {
                    return loginSource;
                }
            }
            return null;
        }
    }

    public static enum ServerSource {

        /**
         * Server is taken from appropriate property
         */
        GLOBAL("global"),
        /**
         * Server is taken from user
         */
        USER("user");

        private final String str;

        private ServerSource(final String str) {
            this.str = str;
        }

        @Override
        public String toString() {
            return str;
        }

        /**
         * Parses specified string into a server source.
         * 
         * @param serverSourceStr The string to parse to a server source
         * @return An appropriate server source or <code>null</code> if string could not be parsed to a server source
         */
        public static final ServerSource parse(final String serverSourceStr) {
            final ServerSource[] values = ServerSource.values();
            for (final ServerSource serverSource : values) {
                if (serverSource.str.equalsIgnoreCase(serverSourceStr)) {
                    return serverSource;
                }
            }
            return null;
        }
    }

    public static enum PasswordSource {

        /**
         * Password is taken from appropriate property
         */
        GLOBAL("global"),
        /**
         * Password is equal to session password
         */
        SESSION("session");

        private final String str;

        private PasswordSource(final String str) {
            this.str = str;
        }

        @Override
        public String toString() {
            return str;
        }

        /**
         * Parses specified string into a password source.
         * 
         * @param passwordSourceStr The string to parse to a password source
         * @return An appropriate password source or <code>null</code> if string could not be parsed to a password source
         */
        public static final PasswordSource parse(final String passwordSourceStr) {
            final PasswordSource[] values = PasswordSource.values();
            for (final PasswordSource passwordSource : values) {
                if (passwordSource.str.equalsIgnoreCase(passwordSourceStr)) {
                    return passwordSource;
                }
            }
            return null;
        }
    }

    /**
     * Initializes a new {@link MailConfig}
     */
    protected MailConfig() {
        super();
    }

    protected static final Class<?>[] CONSTRUCTOR_ARGS = new Class[0];

    protected static final Object[] INIT_ARGS = new Object[0];

    /**
     * Gets the user-specific mail configuration.
     * 
     * @param <C> The return value type
     * @param clazz The mail configuration type
     * @param mailConfig A newly created {@link MailConfig mail configuration}
     * @param session The session providing needed user data
     * @param accountId The mail account ID
     * @return The user-specific mail configuration
     * @throws MailException If user-specific mail configuration cannot be determined
     */
    public static final <C extends MailConfig> C getConfig(final Class<? extends C> clazz, final C mailConfig, final Session session, final int accountId) throws MailException {
        /*
         * Fetch mail account
         */
        final MailAccount mailAccount;
        try {
            final MailAccountStorageService storage = ServerServiceRegistry.getInstance().getService(MailAccountStorageService.class, true);
            if (accountId == MailAccount.DEFAULT_ID) {
                mailAccount = storage.getDefaultMailAccount(session.getUserId(), session.getContextId());
            } else {
                mailAccount = storage.getMailAccount(accountId, session.getUserId(), session.getContextId());
            }
        } catch (final ServiceException e) {
            throw new MailException(e);
        } catch (final MailAccountException e) {
            throw new MailException(e);
        }
        mailConfig.accountId = accountId;
        fillLoginAndPassword(
            mailConfig,
            session.getPassword(),
            UserStorage.getStorageUser(session.getUserId(), session.getContextId()).getLoginInfo(),
            mailAccount);
        String serverURL = MailConfig.getMailServerURL(mailAccount);
        if (serverURL == null) {
            if (ServerSource.GLOBAL.equals(MailProperties.getInstance().getMailServerSource())) {
                throw new MailConfigException(
                    new StringBuilder(128).append("Property \"").append("com.openexchange.mail.mailServer").append(
                        "\" not set in mail properties").toString());
            }
            throw new MailConfigException(new StringBuilder(128).append("Cannot determine mail server URL for user ").append(
                session.getUserId()).append(" in context ").append(session.getContextId()).toString());
        }
        {
            /*
             * Remove ending '/' character
             */
            final int lastPos = serverURL.length() - 1;
            if (serverURL.charAt(lastPos) == '/') {
                serverURL = serverURL.substring(0, lastPos);
            }
        }
        mailConfig.parseServerURL(serverURL);
        return mailConfig;
    }

    /*-
     * User-specific fields
     */
    protected String login;

    protected String password;

    protected int accountId;

    /**
     * Gets the mail server URL appropriate to configured mail server source.
     * 
     * @param mailAccount The user
     * @return The appropriate mail server URL or <code>null</code>
     */
    public static final String getMailServerURL(final MailAccount mailAccount) {
        if (!mailAccount.isDefaultAccount()) {
            return mailAccount.getMailServerURL();
        }
        if (ServerSource.GLOBAL.equals(MailProperties.getInstance().getMailServerSource())) {
            return MailProperties.getInstance().getMailServer();
        }
        return mailAccount.getMailServerURL();
    }

    /**
     * Gets the mail server URL appropriate to configured mail server source.
     * 
     * @param session The user session
     * @param accountId The account ID
     * @return The appropriate mail server URL or <code>null</code>
     * @throws MailException If mail server URL cannot be returned
     */
    public static final String getMailServerURL(final Session session, final int accountId) throws MailException {
        try {
            final MailAccountStorageService storage = ServerServiceRegistry.getInstance().getService(MailAccountStorageService.class, true);
            return getMailServerURL(storage.getMailAccount(accountId, session.getUserId(), session.getContextId()));
        } catch (final ServiceException e) {
            throw new MailException(e);
        } catch (final MailAccountException e) {
            throw new MailException(e);
        }
    }

    /**
     * Gets the mail login with respect to configured login source.
     * 
     * @param mailAccount The mail account used to determine the login
     * @return The mail login of specified user
     */
    public static final String getMailLogin(final MailAccount mailAccount, final String userLoginInfo) {
        if (!mailAccount.isDefaultAccount()) {
            return mailAccount.getLogin();
        }
        final LoginSource loginSource = MailProperties.getInstance().getLoginSource();
        if (LoginSource.USER_IMAPLOGIN.equals(loginSource)) {
            return mailAccount.getLogin();
        }
        if (LoginSource.PRIMARY_EMAIL.equals(loginSource)) {
            return mailAccount.getPrimaryAddress();
        }
        return userLoginInfo;
    }

    /**
     * Resolves the user IDs by specified pattern dependent on configuration's setting for mail login source.
     * 
     * @param pattern The pattern
     * @param server The server address
     * @param ctx The context
     * @return The user IDs from specified pattern dependent on configuration's setting for mail login source
     * @throws AbstractOXException If resolving user by specified pattern fails
     */
    public static int[] getUserIDsByMailLogin(final String pattern, final boolean isDefaultAccount, final InetSocketAddress server, final Context ctx) throws AbstractOXException {
        final MailAccountStorageService storageService = ServerServiceRegistry.getInstance().getService(
            MailAccountStorageService.class,
            true);
        if (isDefaultAccount) {
            final LoginSource loginSource = MailProperties.getInstance().getLoginSource();
            if (LoginSource.USER_IMAPLOGIN.equals(loginSource)) {
                /*
                 * Find user name by user's imap login
                 */
                final MailAccount[] accounts = storageService.resolveLogin(pattern, server, ctx.getContextId());
                final int[] retval = new int[accounts.length];
                for (int i = 0; i < retval.length; i++) {
                    retval[i] = accounts[i].getUserId();
                }
                return retval;
            }
            if (LoginSource.PRIMARY_EMAIL.equals(loginSource)) {
                final MailAccount[] accounts = storageService.resolvePrimaryAddr(pattern, server, ctx.getContextId());
                final int[] retval = new int[accounts.length];
                for (int i = 0; i < retval.length; i++) {
                    retval[i] = accounts[i].getUserId();
                }
                return retval;
            }
            return new int[] { UserStorage.getInstance().getUserId(pattern, ctx) };
        }
        /*
         * Find user name by user's imap login
         */
        final MailAccount[] accounts = storageService.resolveLogin(pattern, server, ctx.getContextId());
        final int[] retval = new int[accounts.length];
        for (int i = 0; i < retval.length; i++) {
            retval[i] = accounts[i].getUserId();
        }
        return retval;
    }

    /**
     * Fills login and password in specified instance of {@link MailConfig}.
     * 
     * @param mailConfig The mail config whose login and password shall be set
     * @param sessionPassword The session password
     * @param mailAccount The mail account
     * @throws MailConfigException
     */
    protected static final void fillLoginAndPassword(final MailConfig mailConfig, final String sessionPassword, final String userLoginInfo, final MailAccount mailAccount) throws MailConfigException {
        // Assign login
        mailConfig.login = getMailLogin(mailAccount, userLoginInfo);
        // Assign password
        if (mailAccount.isDefaultAccount()) {
            final PasswordSource cur = MailProperties.getInstance().getPasswordSource();
            if (PasswordSource.GLOBAL.equals(cur)) {
                final String masterPw = MailProperties.getInstance().getMasterPassword();
                if (masterPw == null) {
                    throw new MailConfigException(
                        new StringBuilder().append("Property \"").append("masterPassword").append("\" not set").toString());
                }
                mailConfig.password = masterPw;
            } else {
                mailConfig.password = sessionPassword;
            }
        } else {
            mailConfig.password = PasswordUtil.decrypt(mailAccount.getPassword(), sessionPassword);
        }
    }

    /**
     * Gets the login source.
     * 
     * @return the login source
     */
    public static final LoginSource getLoginSource() {
        return MailProperties.getInstance().getLoginSource();
    }

    /**
     * Gets the password source.
     * 
     * @return the password source
     */
    public static PasswordSource getPasswordSource() {
        return MailProperties.getInstance().getPasswordSource();
    }

    /**
     * Gets the mail server source.
     * 
     * @return the mail server source
     */
    public static ServerSource getMailServerSource() {
        return MailProperties.getInstance().getMailServerSource();
    }

    /**
     * Gets the transport server source.
     * 
     * @return the transport server source
     */
    public static ServerSource getTransportServerSource() {
        return MailProperties.getInstance().getTransportServerSource();
    }

    /**
     * Checks if default folders (e.g. "Sent Mail", "Drafts") are supposed to be created below personal namespace folder (INBOX) even though
     * mail server indicates to create them on the same level as personal namespace folder.
     * <p>
     * <b>Note</b> that personal namespace folder must allow subfolder creation.
     * 
     * @return <code>true</code> if default folders are supposed to be created below personal namespace folder; otherwise <code>false</code>
     */
    public static final boolean isAllowNestedDefaultFolderOnAltNamespace() {
        return MailProperties.getInstance().isAllowNestedDefaultFolderOnAltNamespace();
    }

    /**
     * Gets the max. allowed size for attachment for being displayed.
     * 
     * @return the max. allowed size for attachment for being displayed
     */
    public static final int getAttachDisplaySize() {
        return MailProperties.getInstance().getAttachDisplaySize();
    }

    /**
     * Gets the mail server.
     * 
     * @return the mail server
     */
    public static final String getMailServer() {
        return MailProperties.getInstance().getMailServer();
    }

    /**
     * Gets the transport server.
     * 
     * @return the transport server
     */
    public static final String getTransportServer() {
        return MailProperties.getInstance().getTransportServer();
    }

    /**
     * Gets the master password.
     * 
     * @return the master password
     */
    public static final String getMasterPassword() {
        return MailProperties.getInstance().getMasterPassword();
    }

    /**
     * Gets the default MIME charset.
     * 
     * @return the default MIME charset
     */
    public static final String getDefaultMimeCharset() {
        return MailProperties.getInstance().getDefaultMimeCharset();
    }

    /**
     * Gets the default mail provider.
     * 
     * @return the default mail provider
     */
    public static final String getDefaultMailProvider() {
        return MailProperties.getInstance().getDefaultMailProvider();
    }

    /**
     * Gets the defaultSeparator.
     * 
     * @return the defaultSeparator
     */
    public static final char getDefaultSeparator() {
        return MailProperties.getInstance().getDefaultSeparator();
    }

    /**
     * Gets the max. number of connections.
     * 
     * @return the max. number of connections
     */
    public static final int getMaxNumOfConnections() {
        return MailProperties.getInstance().getMaxNumOfConnections();
    }

    /**
     * Checks if subscriptions are ignored.
     * 
     * @return <code>true</code> if subscriptions are ignored; otherwise <code>false</code>
     */
    public static final boolean isIgnoreSubscription() {
        return MailProperties.getInstance().isIgnoreSubscription();
    }

    /**
     * Checks if subscriptions are supported.
     * 
     * @return code>true</code> if subscriptions are supported; otherwise <code>false</code>
     */
    public static final boolean isSupportSubscription() {
        return MailProperties.getInstance().isSupportSubscription();
    }

    /**
     * Gets the mail fetch limit.
     * 
     * @return the mail fetch limit.
     */
    public static final int getMailFetchLimit() {
        return MailProperties.getInstance().getMailFetchLimit();
    }

    private static volatile Boolean usePartModifier;

    /**
     * Checks if a part modifier shall be used, that is {@link PartModifier#getInstance()} is not <code>null</code> and not
     * assignment-compatible to {@link DummyPartModifier} (which does nothing at all).
     * 
     * @return <code>true</code> if part modifier shall be used; otherwise <code>false</code>
     */
    public static final boolean usePartModifier() {
        if (usePartModifier == null) {
            synchronized (MailConfig.class) {
                if (usePartModifier == null) {
                    final PartModifier pm = PartModifier.getInstance();
                    usePartModifier = Boolean.valueOf(pm != null && !DummyPartModifier.class.isInstance(pm));
                }
            }
        }
        return usePartModifier.booleanValue();
    }

    /**
     * Gets the part modifier.
     * 
     * @return the part modifier.
     */
    public static final PartModifier getPartModifier() {
        return PartModifier.getInstance();
    }

    /**
     * Gets the quote line colors.
     * 
     * @return the quote line colors
     */
    public static final String[] getQuoteLineColors() {
        return MailProperties.getInstance().getQuoteLineColors();
    }

    /**
     * Checks if user flags are enabled
     * 
     * @return <code>true</code> if user flags are enabled; otherwise <code>false</code>
     */
    public static final boolean isUserFlagsEnabled() {
        return MailProperties.getInstance().isUserFlagsEnabled();
    }

    /**
     * Gets the JavaMail properties to apply.
     * 
     * @return the JavaMail properties to apply
     */
    public static Properties getJavaMailProperties() {
        return MailProperties.getInstance().getJavaMailProperties();
    }

    /**
     * Checks if watcher is enabled.
     * 
     * @return <code>true</code> if watcher is enabled; otherwise <code>false</code>
     */
    public static boolean isWatcherEnabled() {
        return MailProperties.getInstance().isWatcherEnabled();
    }

    /**
     * Gets the watcher frequency.
     * 
     * @return the watcher frequency
     */
    public static int getWatcherFrequency() {
        return MailProperties.getInstance().getWatcherFrequency();
    }

    /**
     * Checks if watcher is allowed to close tracked connections.
     * 
     * @return <code>true</code> if watcher is allowed to close tracked connections; otherwise <code>false</code>
     */
    public static boolean isWatcherShallClose() {
        return MailProperties.getInstance().isWatcherShallClose();
    }

    /**
     * Gets the watcher time.
     * 
     * @return the watcher time
     */
    public static int getWatcherTime() {
        return MailProperties.getInstance().getWatcherTime();
    }

    /**
     * Gets the phishing headers
     * 
     * @return The phishing headers or <code>null</code> if none defined
     */
    public static String[] getPhishingHeaders() {
        return MailProperties.getInstance().getPhishingHeaders();
    }

    /**
     * Indicates if admin mail login is enabled; meaning whether admin user's try to login to mail system is permitted or not.
     * 
     * @return <code>true</code> if admin mail login is enabled; otherwise <code>false</code>
     */
    public static boolean isAdminMailLoginEnabled() {
        return MailProperties.getInstance().isAdminMailLoginEnabled();
    }

    /**
     * Gets the login.
     * 
     * @return the login
     */
    public final String getLogin() {
        return login;
    }

    /**
     * Sets the login (externally).
     * 
     * @param login The login
     */
    public void setLogin(final String login) {
        this.login = login;
    }

    /**
     * Gets the password.
     * 
     * @return the password
     */
    public final String getPassword() {
        return password;
    }

    /**
     * Sets the password (externally).
     * 
     * @param password The password
     */
    public void setPassword(final String password) {
        this.password = password;
    }

    /**
     * Gets the account ID.
     * 
     * @return The account ID
     */
    public int getAccountId() {
        return accountId;
    }

    /**
     * Sets the account ID (externally).
     * 
     * @param accountId The account ID
     */
    public void setAccountId(final int accountId) {
        this.accountId = accountId;
    }

    /**
     * Parses given server URL which is then accessible through {@link #getServer()} and optional {@link #getPort()}.
     * <p>
     * The implementation is supposed to use {@link #parseProtocol(String)} to determine the protocol.
     * <p>
     * Moreover this method should check if a secure connection shall be established dependent on URL's protocol. The result is then
     * accessible via {@link #isSecure()}.
     * 
     * @param serverURL The server URL of the form:<br>
     *            (&lt;protocol&gt;://)?&lt;host&gt;(:&lt;port&gt;)?
     * @throws MailException If server URL cannot be parsed
     */
    protected abstract void parseServerURL(String serverURL) throws MailException;

    /**
     * Gets the host name or IP address of the server.
     * 
     * @return The host name or IP address of the server.
     */
    public abstract String getServer();

    /**
     * Sets the host name or IP address of the server (externally).
     * 
     * @param server The host name or IP address of the server
     */
    public abstract void setServer(String server);

    /**
     * Gets the optional port of the server.
     * 
     * @return The optional port of the server obtained via {@link #getServer()} or <code>-1</code> if no port needed.
     */
    public abstract int getPort();

    /**
     * Sets the port (externally).
     * 
     * @param port The port
     */
    public abstract void setPort(int port);

    /**
     * Checks if a secure connection shall be established.
     * 
     * @return <code>true</code> if a secure connection shall be established; otherwise <code>false</code>
     */
    public abstract boolean isSecure();

    /**
     * Sets (externally) whether a secure connection should be established or not.
     * 
     * @param secure <code>true</code> if a secure connection should be established; otherwise <code>false</code>
     */
    public abstract void setSecure(boolean secure);

    /**
     * Gets the mail system's capabilities
     * 
     * @return The mail system's capabilities
     */
    public abstract MailCapabilities getCapabilities();

    /*-
     * TEST TEST TEST TEST TEST
     */

    protected static final String TEST_PW = "oxTEST";

    private static Map<Integer, String> TEST_LOGIN_MAP;

    static {
        final StringBuilder sb = new StringBuilder();
        TEST_LOGIN_MAP = new HashMap<Integer, String>();
        for (int i = 1; i <= 10000; i++) {
            TEST_LOGIN_MAP.put(Integer.valueOf(i), sb.append("ox-test-").append(i).append("@nms112.de").toString());
            sb.setLength(0);
        }
    }

    private static final Random RANDOM = new Random();

    protected static String getRandomTestLogin() {
        final int num = 1 + Math.abs(RANDOM.nextInt()) % 10000;
        return TEST_LOGIN_MAP.get(Integer.valueOf(num));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((login == null) ? 0 : login.hashCode());
        result = prime * result + ((password == null) ? 0 : password.hashCode());
        result = prime * result + (getPort());
        result = prime * result + ((getServer() == null) ? 0 : getServer().hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (getClass() != obj.getClass()) {
            return false;
        }
        final MailConfig other = (MailConfig) obj;
        if (login == null) {
            if (other.login != null) {
                return false;
            }
        } else if (!login.equals(other.login)) {
            return false;
        }
        if (password == null) {
            if (other.password != null) {
                return false;
            }
        } else if (!password.equals(other.password)) {
            return false;
        }
        if (getPort() != other.getPort()) {
            return false;
        }
        if (getServer() == null) {
            if (other.getServer() != null) {
                return false;
            }
        } else if (!getServer().equals(other.getServer())) {
            return false;
        }
        return true;
    }

    /**
     * Parses protocol out of specified server string according to URL specification; e.g. <i>mailprotocol://dev.myhost.com:1234</i>
     * 
     * @param server The server string
     * @return An array of {@link String} with length <code>2</code>. The first element is the protocol and the second the server. If no
     *         protocol pattern could be found <code>null</code> is returned; meaning no protocol is present in specified server string.
     */
    public final static String[] parseProtocol(final String server) {
        final int len = server.length();
        char c = '\0';
        for (int i = 0; (i < len) && ((c = server.charAt(i)) != '/'); i++) {
            if (c == ':' && ((c = server.charAt(i + 1)) == '/') && ((c = server.charAt(i + 2)) == '/')) {
                final String s = server.substring(0, i).toLowerCase(Locale.ENGLISH);
                if (isValidProtocol(s)) {
                    int start = i + 1;
                    while (server.charAt(start) == '/') {
                        start++;
                    }
                    return new String[] { s, server.substring(start) };
                }
                break;
            }
        }
        return null;
    }

    private final static boolean isValidProtocol(final String protocol) {
        final int len = protocol.length();
        if (len < 1) {
            return false;
        }
        char c = protocol.charAt(0);
        if (!Character.isLetter(c)) {
            return false;
        }
        for (int i = 1; i < len; i++) {
            c = protocol.charAt(i);
            if (!Character.isLetterOrDigit(c) && (c != '.') && (c != '+') && (c != '-')) {
                return false;
            }
        }
        return true;
    }
}
