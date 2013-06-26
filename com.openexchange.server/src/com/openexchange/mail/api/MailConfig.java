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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

import static com.openexchange.mail.utils.ProviderUtility.toSocketAddr;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import java.net.InetSocketAddress;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import javax.mail.internet.AddressException;
import javax.mail.internet.idn.IDNA;
import com.javacodegeeks.concurrent.ConcurrentLinkedHashMap;
import com.javacodegeeks.concurrent.LRUPolicy;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.config.MailConfigException;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.mime.QuotedInternetAddress;
import com.openexchange.mail.partmodifier.DummyPartModifier;
import com.openexchange.mail.partmodifier.PartModifier;
import com.openexchange.mail.utils.MailFolderUtility;
import com.openexchange.mail.utils.MailPasswordUtil;
import com.openexchange.mail.utils.StorageUtility;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountExceptionCodes;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.threadpool.ThreadPools;

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
         * AUTO
         */
        AUTO("auto"),
        /**
         * FALSE
         */
        FALSE("false"),
        /**
         * TRUE
         */
        TRUE("true");

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

        private final String str;

        private BoolCapVal(final String str) {
            this.str = str;
        }

        @Override
        public String toString() {
            return str;
        }
    }

    public static enum LoginSource {

        /**
         * Login is taken from user.mail kept in storage; e.g. <code>test@foo.bar</code>
         */
        PRIMARY_EMAIL("mail"),
        /**
         * Login is taken from user.imapLogin kept in storage; e.g. <code>test</code>
         */
        USER_IMAPLOGIN("login"),
        /**
         * Login is user's name; e.g. <code>test</code>
         */
        USER_NAME("name");

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

        private final String str;

        private LoginSource(final String str) {
            this.str = str;
        }

        @Override
        public String toString() {
            return str;
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

        private final String str;

        private PasswordSource(final String str) {
            this.str = str;
        }

        @Override
        public String toString() {
            return str;
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

        private final String str;

        private ServerSource(final String str) {
            this.str = str;
        }

        @Override
        public String toString() {
            return str;
        }
    }

    private static volatile Boolean usePartModifier;

    protected static final Class<?>[] CONSTRUCTOR_ARGS = new Class[0];

    protected static final Object[] INIT_ARGS = new Object[0];

    /**
     * Gets the user-specific mail configuration.
     *
     * @param <C> The return value type
     * @param mailConfig A newly created {@link MailConfig mail configuration}
     * @param session The session providing needed user data
     * @param accountId The mail account ID
     * @return The user-specific mail configuration
     * @throws OXException If user-specific mail configuration cannot be determined
     */
    public static final <C extends MailConfig> C getConfig(final C mailConfig, final Session session, final int accountId) throws OXException {
        /*
         * Fetch mail account
         */
        final MailAccount mailAccount;
        final int userId = session.getUserId();
        final int contextId = session.getContextId();
        {
            final MailAccountStorageService storage = ServerServiceRegistry.getInstance().getService(MailAccountStorageService.class, true);
            if (accountId == MailAccount.DEFAULT_ID) {
                mailAccount = storage.getDefaultMailAccount(userId, contextId);
            } else {
                mailAccount = storage.getMailAccount(accountId, userId, contextId);
            }
        }
        mailConfig.accountId = accountId;
        mailConfig.session = session;
        mailConfig.applyStandardNames(mailAccount);
        fillLoginAndPassword(mailConfig, session, UserStorage.getStorageUser(userId, contextId).getLoginInfo(), mailAccount);
        String serverURL = MailConfig.getMailServerURL(mailAccount);
        if (serverURL == null) {
            if (ServerSource.GLOBAL.equals(MailProperties.getInstance().getMailServerSource())) {
                throw MailConfigException.create(
                    new com.openexchange.java.StringAllocator(64).append("Property \"").append("com.openexchange.mail.mailServer").append(
                        "\" not set in mail properties").toString());
            }
            throw MailConfigException.create(new com.openexchange.java.StringAllocator(64).append("Cannot determine mail server URL for user ").append(userId).append(
                " in context ").append(contextId).toString());
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

    /**
     * Gets the mail login with respect to configured login source.
     *
     * @param mailAccount The mail account used to determine the login
     * @param userLoginInfo The login information of the user
     * @return The mail login of specified user
     */
    public static final String getMailLogin(final MailAccount mailAccount, final String userLoginInfo) {
        return saneLogin(getMailLogin0(mailAccount, userLoginInfo));
    }

    /**
     * Gets the mail login with respect to configured login source.
     *
     * @param mailAccount The mail account used to determine the login
     * @param userLoginInfo The login information of the user
     * @return The mail login of specified user
     */
    private static final String getMailLogin0(final MailAccount mailAccount, final String userLoginInfo) {
        if (!mailAccount.isDefaultAccount()) {
            return mailAccount.getLogin();
        }
        final LoginSource loginSource = MailProperties.getInstance().getLoginSource();
        if (LoginSource.USER_IMAPLOGIN.equals(loginSource)) {
            return mailAccount.getLogin();
        }
        if (LoginSource.PRIMARY_EMAIL.equals(loginSource)) {
            try {
                return QuotedInternetAddress.toACE(mailAccount.getPrimaryAddress());
            } catch (final AddressException e) {
                final String primaryAddress = mailAccount.getPrimaryAddress();
                com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(MailConfig.class)).warn(
                    "Login source primary email address \"" + primaryAddress + "\" could not be converted to ASCII. Using unicode representation.",
                    e);
                return primaryAddress;
            }
        }
        return userLoginInfo;
    }

    /**
     * Gets the mail server URL appropriate to configured mail server source.
     *
     * @param mailAccount The user
     * @return The appropriate mail server URL or <code>null</code>
     */
    public static final String getMailServerURL(final MailAccount mailAccount) {
        if (!mailAccount.isDefaultAccount()) {
            return mailAccount.generateMailServerURL();
        }
        if (ServerSource.GLOBAL.equals(MailProperties.getInstance().getMailServerSource())) {
            return IDNA.toASCII(MailProperties.getInstance().getMailServer());
        }
        return mailAccount.generateMailServerURL();
    }

    /**
     * Gets the mail server URL appropriate to configured mail server source.
     *
     * @param session The user session
     * @param accountId The account ID
     * @return The appropriate mail server URL or <code>null</code>
     * @throws OXException If mail server URL cannot be returned
     */
    public static final String getMailServerURL(final Session session, final int accountId) throws OXException {
        if (MailAccount.DEFAULT_ID == accountId && ServerSource.GLOBAL.equals(MailProperties.getInstance().getMailServerSource())) {
            return IDNA.toASCII(MailProperties.getInstance().getMailServer());
        }
        final MailAccountStorageService storage = ServerServiceRegistry.getInstance().getService(MailAccountStorageService.class, true);
        return storage.getMailAccount(accountId, session.getUserId(), session.getContextId()).generateMailServerURL();
    }

    /**
     * Gets the part modifier.
     *
     * @return the part modifier.
     */
    public static final PartModifier getPartModifier() {
        return PartModifier.getInstance();
    }

    private static final class UserID {

        private final int contextId;
        private final String pattern;
        private final String server;
        private final int hash;

        protected UserID(final String pattern, final String server, final int contextId) {
            super();
            this.pattern = pattern;
            this.server = server;
            this.contextId = contextId;
            final int prime = 31;
            int result = 1;
            result = prime * result + contextId;
            result = prime * result + ((pattern == null) ? 0 : pattern.hashCode());
            result = prime * result + ((server == null) ? 0 : server.hashCode());
            hash = result;
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof UserID)) {
                return false;
            }
            final UserID other = (UserID) obj;
            if (contextId != other.contextId) {
                return false;
            }
            if (pattern == null) {
                if (other.pattern != null) {
                    return false;
                }
            } else if (!pattern.equals(other.pattern)) {
                return false;
            }
            if (server == null) {
                if (other.server != null) {
                    return false;
                }
            } else if (!server.equals(other.server)) {
                return false;
            }
            return true;
        }
    }

    private static final ConcurrentMap<UserID, Future<int[]>> USER_ID_CACHE = new ConcurrentLinkedHashMap<UserID, Future<int[]>>(8192, 0.75F, 16, 65536 << 1, new LRUPolicy());

    /**
     * Resolves the user IDs by specified pattern dependent on configuration's setting for mail login source.
     *
     * @param pattern The pattern
     * @param server The server address
     * @param ctx The context
     * @return The user IDs from specified pattern dependent on configuration's setting for mail login source
     * @throws OXException If resolving user by specified pattern fails
     */
    public static int[] getUserIDsByMailLogin(final String pattern, final boolean isDefaultAccount, final InetSocketAddress server, final Context ctx) throws OXException {
        final MailAccountStorageService storageService = ServerServiceRegistry.getInstance().getService(MailAccountStorageService.class, true);
        if (isDefaultAccount) {
            final UserID userID = new UserID(pattern, server.getHostName() + ':' + Integer.toString(server.getPort()), ctx.getContextId());
            Future<int[]> f = USER_ID_CACHE.get(userID);
            if (null == f) {
                final FutureTask<int[]> ft = new FutureTask<int[]>(new Callable<int[]>() {

                    @Override
                    public int[] call() throws OXException {
                        return forDefaultAccount(pattern, server, ctx, storageService);
                    }
                });
                f = USER_ID_CACHE.putIfAbsent(userID, ft);
                if (null == f) {
                    f = ft;
                    ft.run();
                }
            }
            boolean remove = true;
            try {
                final int[] retval = f.get();
                remove = false;
                return retval;
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                throw MailExceptionCode.INTERRUPT_ERROR.create(e, e.getMessage());
            } catch (final ExecutionException e) {
                ThreadPools.launderThrowable(e, OXException.class);
            } finally {
                if (remove) {
                    USER_ID_CACHE.remove(userID);
                }
            }
        }
        // Find user name by user's imap login
        final MailAccount[] accounts = storageService.resolveLogin(pattern, server, ctx.getContextId());
        final int[] retval = new int[accounts.length];
        for (int i = 0; i < retval.length; i++) {
            retval[i] = accounts[i].getUserId();
        }
        return retval;
    }

    /**
     * Resolves the user IDs by specified pattern dependent on configuration's setting for mail login source for default account
     */
    protected static int[] forDefaultAccount(final String pattern, final InetSocketAddress server, final Context ctx, final MailAccountStorageService storageService) throws OXException {
        switch (MailProperties.getInstance().getLoginSource()) {
        case USER_IMAPLOGIN:
        case PRIMARY_EMAIL:
            final MailAccount[] accounts;
            switch (MailProperties.getInstance().getLoginSource()) {
            case USER_IMAPLOGIN:
                accounts = storageService.resolveLogin(pattern, ctx.getContextId());
                break;
            case PRIMARY_EMAIL:
                accounts = storageService.resolvePrimaryAddr(pattern, ctx.getContextId());
                break;
            default:
                throw MailAccountExceptionCodes.UNEXPECTED_ERROR.create("Unimplemented mail login source.");
            }
            final TIntSet userIds;
            if (accounts.length == 1) {
                // On ASE some accounts are configured to connect to localhost, some to the full qualified local host name. The socket
                // would then not match. If we only find one then, use it.
                userIds = new TIntHashSet(1);
                userIds.add(accounts[0].getUserId());
            } else {
                userIds = new TIntHashSet(accounts.length);
                for (final MailAccount candidate : accounts) {
                    final InetSocketAddress shouldMatch;
                    switch (MailProperties.getInstance().getMailServerSource()) {
                    case USER:
                        shouldMatch = toSocketAddr(candidate.generateMailServerURL(), 143);
                        break;
                    case GLOBAL:
                        shouldMatch = toSocketAddr(MailProperties.getInstance().getMailServer(), 143);
                        break;
                    default:
                        throw MailAccountExceptionCodes.UNEXPECTED_ERROR.create("Unimplemented mail server source.");
                    }
                    if (server.equals(shouldMatch)) {
                        userIds.add(candidate.getUserId());
                    }
                }
            }
            // Prefer the default mail account.
            final int size = userIds.size();
            final TIntSet notDefaultAccount = new TIntHashSet(size);
            if (size > 0) {
                final TIntIterator iter = userIds.iterator();
                for (int i = size; i-- > 0;) {
                    final int userId = iter.next();
                    for (final MailAccount candidate : accounts) {
                        if (candidate.getUserId() == userId && !candidate.isDefaultAccount()) {
                            notDefaultAccount.add(userId);
                        }
                    }
                }
            }
            if (notDefaultAccount.size() < size) {
                userIds.removeAll(notDefaultAccount);
            }
            return userIds.toArray();
        case USER_NAME:
            return new int[] { UserStorage.getInstance().getUserId(pattern, ctx) };
        default:
            throw MailAccountExceptionCodes.UNEXPECTED_ERROR.create("Unimplemented mail login source.");
        }
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

    /**
     * Checks if a part modifier shall be used, that is {@link PartModifier#getInstance()} is not <code>null</code> and not
     * assignment-compatible to {@link DummyPartModifier} (which does nothing at all).
     *
     * @return <code>true</code> if part modifier shall be used; otherwise <code>false</code>
     */
    public static final boolean usePartModifier() {
        Boolean tmp = usePartModifier;
        if (tmp == null) {
            synchronized (MailConfig.class) {
                tmp = usePartModifier;
                if (tmp == null) {
                    final PartModifier pm = PartModifier.getInstance();
                    tmp = usePartModifier = Boolean.valueOf(pm != null && !DummyPartModifier.class.isInstance(pm));
                }
            }
        }
        return tmp.booleanValue();
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

    /**
     * Gets the sane (puny-code) representation of passed login in case it appears to be an Internet address.
     *
     * @param login The login
     * @return The sane login
     */
    private static final String saneLogin(final String login) {
        final ConfigurationService service = ServerServiceRegistry.getInstance().getService(ConfigurationService.class);
        if (!(null == service ? true : service.getBoolProperty("com.openexchange.mail.saneLogin", true))) {
            return login;
        }
        try {
            return IDNA.toACE(login);
        } catch (final Exception e) {
            return login;
        }
    }

    /**
     * Fills login and password in specified instance of {@link MailConfig}.
     *
     * @param mailConfig The mail config whose login and password shall be set
     * @param sessionPassword The session password
     * @param mailAccount The mail account
     * @throws OXException If a configuration error occurs
     */
    protected static final void fillLoginAndPassword(final MailConfig mailConfig, final Session session, final String userLoginInfo, final MailAccount mailAccount) throws OXException {
        final String proxyDelimiter = MailProperties.getInstance().getAuthProxyDelimiter();
        // Assign login
        final String slogin = session.getLoginName();
        if (proxyDelimiter != null && slogin.contains(proxyDelimiter)) {
            mailConfig.login = saneLogin(slogin);
        } else {
            mailConfig.login = getMailLogin(mailAccount, userLoginInfo);
        }
        // Assign password
        if (mailAccount.isDefaultAccount()) {
            final PasswordSource cur = MailProperties.getInstance().getPasswordSource();
            if (PasswordSource.GLOBAL.equals(cur)) {
                final String masterPw = MailProperties.getInstance().getMasterPassword();
                if (masterPw == null) {
                    throw MailConfigException.create(new com.openexchange.java.StringAllocator().append("Property \"masterPassword\" not set").toString());
                }
                mailConfig.password = masterPw;
            } else {
                mailConfig.password = session.getPassword();
            }
        } else {
            final String mailAccountPassword = mailAccount.getPassword();
            if (null == mailAccountPassword || mailAccountPassword.length() == 0) {
                // Set to empty string
                mailConfig.password = "";
            } else {
                // Mail account's password
                mailConfig.password = MailPasswordUtil.decrypt(mailAccountPassword, session, mailAccount.getId(), mailAccount.getLogin(), mailAccount.getMailServer());
            }
        }
        mailConfig.doCustomParsing(mailAccount, session);
    }

    private static final int LENGTH = 6;

    /*-
     * Member section
     */

    protected int accountId;
    protected Session session;
    protected String login;
    protected String password;
    protected final String[] standardNames;
    protected final String[] standardFullNames;

    /**
     * Initializes a new {@link MailConfig}
     */
    protected MailConfig() {
        super();
        standardFullNames = new String[LENGTH];
        standardNames = new String[LENGTH];
    }

    /**
     * Gets the standard names.
     *
     * @return The standard names
     */
    public String[] getStandardNames() {
        final String[] ret = new String[LENGTH];
        System.arraycopy(standardNames, 0, ret, 0, LENGTH);
        return ret;
    }

    /**
     * Gets the standard full names.
     *
     * @return The standard full names
     */
    public String[] getStandardFullNames() {
        final String[] ret = new String[LENGTH];
        System.arraycopy(standardFullNames, 0, ret, 0, LENGTH);
        return ret;
    }

    /**
     * Applies folder name information from given mail account
     *
     * @param mailAccount The mail account
     */
    public void applyStandardNames(final MailAccount mailAccount) {
        if (null == mailAccount) {
            return;
        }
        put(StorageUtility.INDEX_CONFIRMED_HAM, mailAccount.getConfirmedHam(), standardNames);
        put(StorageUtility.INDEX_CONFIRMED_SPAM, mailAccount.getConfirmedSpam(), standardNames);
        put(StorageUtility.INDEX_DRAFTS, mailAccount.getDrafts(), standardNames);
        put(StorageUtility.INDEX_SENT, mailAccount.getSent(), standardNames);
        put(StorageUtility.INDEX_SPAM, mailAccount.getSpam(), standardNames);
        put(StorageUtility.INDEX_TRASH, mailAccount.getTrash(), standardNames);

        put(StorageUtility.INDEX_CONFIRMED_HAM, mailAccount.getConfirmedHamFullname(), standardFullNames);
        put(StorageUtility.INDEX_CONFIRMED_SPAM, mailAccount.getConfirmedSpamFullname(), standardFullNames);
        put(StorageUtility.INDEX_DRAFTS, mailAccount.getDraftsFullname(), standardFullNames);
        put(StorageUtility.INDEX_SENT, mailAccount.getSentFullname(), standardFullNames);
        put(StorageUtility.INDEX_SPAM, mailAccount.getSpamFullname(), standardFullNames);
        put(StorageUtility.INDEX_TRASH, mailAccount.getTrashFullname(), standardFullNames);
    }

    private static void put(final int index, final String value, final String[] arr) {
        if (isEmpty(value)) {
            return;
        }
        arr[index] = MailFolderUtility.prepareMailFolderParam(value).getFullname();
    }

    private static boolean isEmpty(final String string) {
        if (null == string) {
            return true;
        }
        final int len = string.length();
        boolean isWhitespace = true;
        for (int i = 0; isWhitespace && i < len; i++) {
            isWhitespace = com.openexchange.java.Strings.isWhitespace(string.charAt(i));
        }
        return isWhitespace;
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
     * Gets the account ID.
     *
     * @return The account ID
     */
    public int getAccountId() {
        return accountId;
    }

    /**
     * Gets the session.
     *
     * @return The session
     */
    public Session getSession() {
        return session;
    }

    /**
     * Gets the mail system's capabilities
     *
     * @return The mail system's capabilities
     */
    public abstract MailCapabilities getCapabilities();

    /**
     * Gets the login.
     *
     * @return the login
     */
    public final String getLogin() {
        return login;
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
     * Gets the optional port of the server.
     *
     * @return The optional port of the server obtained via {@link #getServer()} or <code>-1</code> if no port needed.
     */
    public abstract int getPort();

    /**
     * Gets the host name or IP address of the server.
     *
     * @return The host name or IP address of the server.
     */
    public abstract String getServer();

    @Override
    public String toString() {
        final com.openexchange.java.StringAllocator builder = new com.openexchange.java.StringAllocator();
        builder.append("{ MailConfig [accountId=").append(accountId).append(", ");
        if (login != null) {
            builder.append("login=").append(login).append(", ");
        }
        if (password != null) {
            // builder.append("password=").append(password).append(", ");
        }
        builder.append("getPort()=").append(getPort()).append(", ");
        if (getServer() != null) {
            builder.append("getServer()=").append(getServer()).append(", ");
        }
        builder.append("isSecure()=").append(isSecure()).append("] }");
        return builder.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((login == null) ? 0 : login.hashCode());
        result = prime * result + ((password == null) ? 0 : password.hashCode());
        result = prime * result + (getPort());
        final String server = getServer();
        result = prime * result + ((server == null) ? 0 : server.hashCode());
        return result;
    }

    /**
     * Checks if a secure connection shall be established.
     *
     * @return <code>true</code> if a secure connection shall be established; otherwise <code>false</code>
     */
    public abstract boolean isSecure();

    /**
     * Sets the account ID (externally).
     *
     * @param accountId The account ID
     */
    public void setAccountId(final int accountId) {
        this.accountId = accountId;
    }

    /**
     * Sets the session
     *
     * @param session The session
     */
    public void setSession(final Session session) {
        this.session = session;
    }

    /**
     * Sets the login (externally).
     *
     * @param login The login
     */
    public void setLogin(final String login) {
        this.login = saneLogin(login);
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
     * Performs optional custom parsing.
     * <p>
     * Returns <code>false</code> by default.
     *
     * @param account The associated mail account
     * @param session The user's session
     * @return <code>true</code> if custom parsing has been performed; otherwise <code>false</code>
     * @throws OXException If custom parsing fails
     */
    protected boolean doCustomParsing(final MailAccount account, final Session session) throws OXException {
        return false;
    }

    /**
     * Sets the port (externally).
     *
     * @param port The port
     */
    public abstract void setPort(int port);

    /**
     * Sets (externally) whether a secure connection should be established or not.
     *
     * @param secure <code>true</code> if a secure connection should be established; otherwise <code>false</code>
     */
    public abstract void setSecure(boolean secure);

    /**
     * Sets the host name or IP address of the server (externally).
     *
     * @param server The host name or IP address of the server
     */
    public abstract void setServer(String server);

    /**
     * Gets the mail properties for this mail configuration.
     *
     * @return The mail properties for this mail configuration
     */
    public abstract IMailProperties getMailProperties();

    /**
     * Sets the mail properties for this mail configuration.
     *
     * @param mailProperties The mail properties for this mail configuration
     */
    public abstract void setMailProperties(IMailProperties mailProperties);

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
     * @throws OXException If server URL cannot be parsed
     */
    protected abstract void parseServerURL(String serverURL) throws OXException;
}
