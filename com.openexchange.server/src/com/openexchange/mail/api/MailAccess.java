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

package com.openexchange.mail.api;

import java.io.Closeable;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Queue;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptions;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.groupware.userconfiguration.UserPermissionBitsStorage;
import com.openexchange.log.LogProperties;
import com.openexchange.mail.MailAccessWatcher;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailInitialization;
import com.openexchange.mail.MailProviderRegistry;
import com.openexchange.mail.MailSessionCache;
import com.openexchange.mail.MailSessionParameterNames;
import com.openexchange.mail.api.AuthenticationFailedHandler.Service;
import com.openexchange.mail.api.permittance.Permittance;
import com.openexchange.mail.api.permittance.Permitter;
import com.openexchange.mail.cache.EnqueueingMailAccessCache;
import com.openexchange.mail.cache.IMailAccessCache;
import com.openexchange.mail.cache.SingletonMailAccessCache;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.mime.MimeCleanUp;
import com.openexchange.mail.mime.MimeMailExceptionCode;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.oauth.API;
import com.openexchange.oauth.OAuthAccount;
import com.openexchange.oauth.OAuthExceptionCodes;
import com.openexchange.oauth.OAuthService;
import com.openexchange.oauth.OAuthUtil;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.PutIfAbsent;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.version.Version;

/**
 * {@link MailAccess} - Handles connecting to the mailing system while using an internal cache for connected access objects (see
 * {@link SingletonMailAccessCache}).
 * <p>
 * Moreover it provides access to either message storage, folder storage and logic tools.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class MailAccess<F extends IMailFolderStorage, M extends IMailMessageStorage> implements Serializable, Closeable {

    /**
     * Serial version UID
     */
    private static final long serialVersionUID = -2580495494392812083L;

    private static final transient org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MailAccess.class);

    // --------------------------------------------------------------------------------------------------------------------------------- //

    private static final ConcurrentMap<Key, AcquiredLatch> SYNCHRONIZER = new ConcurrentHashMap<Key, AcquiredLatch>(256);

    private static AcquiredLatch acquireFor(Key key) {
        AcquiredLatch latch = SYNCHRONIZER.get(key);
        if (null == latch) {
            AcquiredLatch newLatch = new AcquiredLatch(Thread.currentThread(), new CountDownLatch(1));
            latch = SYNCHRONIZER.putIfAbsent(key, newLatch);
            if (null == latch) {
                latch = newLatch;
            }
        }
        return latch;
    }

    private static void releaseFor(Key key) {
        SYNCHRONIZER.remove(key);
    }

    // --------------------------------------------------------------------------------------------------------------------------------- //

    private static volatile String version;

    /**
     * Gets the version (w/o revision number); e.g. <code>"7.8.0"</code>
     *
     * @return The version
     */
    public static String getVersion() {
        String tmp = version;
        if (null == tmp) {
            synchronized (MailAccess.class) {
                tmp = version;
                if (null == tmp) {
                    Version v = Version.getInstance();
                    tmp = new StringBuilder(10).append(v.getMajor()).append('.').append(v.getMinor()).append('.').append(v.getPatch()).toString();
                    version = tmp;
                }
            }
        }
        return tmp;
    }

    // --------------------------------------------------------------------------------------------------------------------------------- //

    static final class FastThrowable extends Throwable {

        FastThrowable() {
            super("tracked mail connection usage");
        }

        @Override
        public synchronized Throwable fillInStackTrace() {
            return this;
        }
    }

    // --------------------------------------------------------------------------------------------------------------------------------- //

    /** The session parameter that may hold the established {@link MailAccess} instance for the <b>primary</b> mail account */
    public static final String PARAM_MAIL_ACCESS = "__mailaccess";

    // --------------------------------------------------------------------------------------------------------------------------------- //

    /*-
     * ############### MEMBERS ###############
     */

    /** The associated session */
    protected final transient Session session;

    /** The account identifier */
    protected final int accountId;

    /** A collection of wanrings */
    protected final Collection<OXException> warnings;

    /** Whether this access is cacheable */
    protected volatile boolean cacheable;

    /** Whether this access is trackable by {@link MailAccessWatcher} */
    protected volatile boolean trackable;

    /** Indicates if <tt>MailAccess</tt> is currently held in {@link SingletonMailAccessCache}. */
    protected volatile boolean cached;

    /** A flag to check if this <tt>MailAccess</tt> is connected, but in IDLE mode, waiting for any server notifications. */
    protected volatile boolean waiting;

    /** The associated mail provider */
    protected MailProvider provider;

    private volatile boolean tracked;

    private transient MailConfig mailConfig;

    private Properties mailProperties;

    private transient Thread usingThread;
    private transient Map<String, String> usingThreadProperties;

    private StackTraceElement[] trace;

    /**
     * Initializes a new <tt>MailAccess</tt> for session user's default mail account.
     *
     * @param session The session
     */
    protected MailAccess(final Session session) {
        this(session, MailAccount.DEFAULT_ID);
    }

    /**
     * Initializes a new <tt>MailAccess</tt>.
     *
     * @param session The session
     * @param accountId The account ID
     */
    protected MailAccess(final Session session, final int accountId) {
        super();
        warnings = new ArrayList<OXException>(2);
        this.session = session;
        this.accountId = accountId;
        cacheable = true;
        trackable = true;
    }

    /**
     * Gets the session associated with this <tt>MailAccess</tt> instance.
     *
     * @return The session
     */
    public Session getSession() {
        return session;
    }

    /**
     * Sets the associated {@link MailProvider} instance.
     *
     * @param provider The mail provider
     * @return This instance with mail provider applied
     */
    protected MailAccess<F, M> setProvider(final MailProvider provider) {
        this.provider = provider;
        return this;
    }

    /**
     * Gets the associated {@link MailProvider} instance.
     *
     * @return The mail provider
     */
    public MailProvider getProvider() {
        return provider;
    }

    /**
     * Adds given warnings.
     *
     * @param warnings The warnings to add
     */
    public void addWarnings(final Collection<OXException> warnings) {
        this.warnings.addAll(warnings);
    }

    /**
     * Gets possible warnings.
     *
     * @return Possible warnings.
     */
    public Collection<OXException> getWarnings() {
        return Collections.unmodifiableCollection(warnings);
    }

    /**
     * Resets this access' settings. Should be called when {@link #closeInternal()} is invoked.
     */
    protected final void resetFields() {
        mailProperties = null;
        usingThread = null;
        usingThreadProperties = null;
        trace = null;
    }

    /**
     * Triggers all implementation-specific startup actions.
     *
     * @param mailAccess An instance of <tt>MailAccess</tt>
     * @throws OXException If implementation-specific startup fails
     */
    protected static void startupImpl(final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess) throws OXException {
        mailAccess.startup();
    }

    /**
     * Triggers all implementation-specific shutdown actions.
     *
     * @param mailAccess An instance of <tt>MailAccess</tt>
     * @throws OXException If implementation-specific shutdown fails
     */
    protected static void shutdownImpl(final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess) throws OXException {
        mailAccess.shutdown();
    }

    /**
     * - The max. number of <tt>MailAccess</tt> instanced allowed being cached concurrently for a user's account. TODO: Add to configuration?
     */
    public static final int MAX_PER_USER = 3;

    /**
     * Gets the appropriate {@link IMailAccessCache mail access cache} instance.
     *
     * @return The mail access cache
     * @throws OXException If cache cannot be initialized
     */
    public static IMailAccessCache getMailAccessCache() throws OXException {
        return 1 == MAX_PER_USER ? SingletonMailAccessCache.getInstance() : EnqueueingMailAccessCache.getInstance(MAX_PER_USER);
    }

    /**
     * (Optionally) Gets the appropriate {@link IMailAccessCache mail access cache} instance.
     *
     * @return The mail access cache instance or <code>null</code> if not yet initialized
     */
    public static IMailAccessCache optMailAccessCache() {
        return 1 == MAX_PER_USER ? SingletonMailAccessCache.optInstance() : EnqueueingMailAccessCache.optInstance();
    }

    /**
     * Gets the proper instance of <tt>MailAccess</tt> for session user's default mail account.
     * <p>
     * When starting to work with obtained {@link MailAccess mail access} at first its {@link #connect()} method is supposed to be invoked.
     * On finished work the final {@link #close(boolean)} must be called:
     *
     * <pre>
     * MailAccess mailAccess = null;
     * try {
     *  mailAccess = MailAccess.getInstance(...);
     *  mailAccess.connect();
     *  // Do something
     * } finally {
     *  if (mailAccess != null) {
     *   mailAccess.close(putToCache);
     *  }
     * }
     * </pre>
     *
     * @param session The session
     * @return A proper instance of <tt>MailAccess</tt>
     * @throws OXException If instantiation fails or a caching error occurs
     */
    public static final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> getInstance(Session session) throws OXException {
        return getInstance(session, MailAccount.DEFAULT_ID);
    }

    /**
     * Gets the proper instance of <tt>MailAccess</tt> parameterized with given session and account ID.
     * <p>
     * When starting to work with obtained {@link MailAccess mail access} at first its {@link #connect()} method is supposed to be invoked.
     * On finished work the final {@link #close(boolean)} must be called:
     *
     * <pre>
     * MailAccess mailAccess = null;
     * try {
     *  mailAccess = MailAccess.getInstance(...);
     *  mailAccess.connect();
     *  // Do something
     * } finally {
     *  if (mailAccess != null) {
     *   mailAccess.close(putToCache);
     *  }
     * }
     * </pre>
     *
     * @param session The session
     * @param accountId The account ID
     * @return A proper instance of <tt>MailAccess</tt>
     * @throws OXException If instantiation fails or a caching error occurs
     */
    public static final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> getInstance(Session session, int accountId) throws OXException {
        // Check for proper initialization
        if (!MailInitialization.getInstance().isInitialized()) {
            throw MailExceptionCode.INITIALIZATION_PROBLEM.create();
        }

        // Check login attempt
        checkLogin(session, accountId);

        // Return instance
        Permitter permitter = Permittance.acquireFor(accountId, session);
        if (null == permitter) {
            // Non-restricted
            return doGetInstance(session, accountId);
        }

        // Acquire permit, then return instance
        permitter.acquire();
        try {
            return doGetInstance(session, accountId);
        } finally {
            boolean lastOne = permitter.release();
            if (lastOne) {
                Permittance.release(permitter);
            }
        }
    }

    private static MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> doGetInstance(Session session, int accountId) throws OXException {
        Object tmp = session.getParameter("com.openexchange.mail.lookupMailAccessCache");
        if (null == tmp || toBool(tmp)) {
            // Look-up cached, already connected instance
            final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = getMailAccessCache().removeMailAccess(session, accountId);
            if (mailAccess != null) {
                return mailAccess;
            }
        }

        // Initialize a new one
        MailProvider mailProvider = MailProviderRegistry.getMailProviderBySession(session, accountId);
        return mailProvider.createNewMailAccess(session, accountId).setProvider(mailProvider);
    }

    private static boolean toBool(final Object obj) {
        if (obj instanceof Boolean) {
            return ((Boolean) obj).booleanValue();
        }
        return Boolean.parseBoolean(obj.toString().trim());
    }

    /**
     * Gets a new, un-cached <tt>MailAccess</tt> instance that is initially not connected.
     *
     * @param session The associated session
     * @param accountId The account identifier
     * @return The new, un-cached <tt>MailAccess</tt> instance
     * @throws OXException If a new, un-cached <tt>MailAccess</tt> instance cannot be returned
     */
    public static final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> getNewInstance(final Session session, final int accountId) throws OXException {
        final String name = "com.openexchange.mail.lookupMailAccessCache";
        final boolean setParam;
        {
            final Object tmp = session.getParameter("com.openexchange.mail.lookupMailAccessCache");
            setParam = (null == tmp || toBool(tmp));
        }
        if (setParam) {
            session.setParameter(name, Boolean.FALSE);
        }
        try {
            return getInstance(session, accountId);
        } finally {
            if (setParam) {
                session.setParameter(name, null);
            }
        }
    }

    /**
     * Re-connects specified <tt>MailAccess</tt> instance.
     *
     * @param mailAccess The <tt>MailAccess</tt> instance to re-connect
     * @return The re-connected <tt>MailAccess</tt> instance.
     * @throws OXException If re-connect attempt fails
     * @see #getNewInstance(Session, int)
     */
    public static final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> reconnect(MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess) throws OXException {
        if (null == mailAccess) {
            return null;
        }
        Session session = mailAccess.getSession();
        int accountId = mailAccess.getAccountId();
        mailAccess.close(true);

        // A new instance, freshly initialized
        MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> newAccess = MailAccess.getNewInstance(session, accountId);
        newAccess.connect();
        return newAccess;
    }

    /**
     * Gets the proper instance of <tt>MailAccess</tt> for specified user's default account.
     * <p>
     * When starting to work with obtained {@link MailAccess mail access} at first its {@link #connect()} method is supposed to be invoked.
     * On finished work the final {@link #close(boolean)} must be called:
     *
     * <pre>
     * MailAccess mailAccess = null;
     * try {
     *  mailAccess = MailAccess.getInstance(...);
     *  mailAccess.connect();
     *  // Do something
     * } finally {
     *  if (mailAccess != null) {
     *   mailAccess.close(putToCache);
     *  }
     * }
     * </pre>
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return An appropriate {@link MailAccess mail access}
     * @throws OXException If instantiation fails or a caching error occurs
     */
    public static final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> getInstance(int userId, int contextId) throws OXException {
        return getInstance(userId, contextId, MailAccount.DEFAULT_ID);
    }

    /**
     * Gets the proper instance of <tt>MailAccess</tt> for specified user and account ID.
     * <p>
     * When starting to work with obtained {@link MailAccess mail access} at first its {@link #connect()} method is supposed to be invoked.
     * On finished work the final {@link #close(boolean)} must be called:
     *
     * <pre>
     * MailAccess mailAccess = null;
     * try {
     *  mailAccess = MailAccess.getInstance(...);
     *  mailAccess.connect();
     *  // Do something
     * } finally {
     *  if (mailAccess != null) {
     *   mailAccess.close(putToCache);
     *  }
     * }
     * </pre>
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param accountId The account identifier
     * @return An appropriate {@link MailAccess mail access}
     * @throws OXException If instantiation fails or a caching error occurs
     */
    public static final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> getInstance(int userId, int contextId, int accountId) throws OXException {
        SessiondService sessiondService = ServerServiceRegistry.getInstance().getService(SessiondService.class);
        if (null != sessiondService) {
            Session session = sessiondService.getAnyActiveSessionForUser(userId, contextId);
            if (session != null) {
                return getInstance(session, accountId);
            }
        }

        // No appropriate session found.
        throw MailExceptionCode.UNEXPECTED_ERROR.create("No appropriate session found.");
    }

    /**
     * @return the global access counter
     */
    public static final int getCounter() {
        return MailAccessWatcher.getNumberOfMailAccesses();
    }

    /**
     * Closes specified <tt>MailAccess</tt> instance with the attempt to put it into cache for subsequent reuse.
     *
     * @param mailAccess The <tt>MailAccess</tt> instance to close
     * @since v7.6.0
     */
    public static void closeInstance(final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess) {
        closeInstance(mailAccess, true);
    }

    /**
     * Closes specified <tt>MailAccess</tt> instance.
     *
     * @param mailAccess The <tt>MailAccess</tt> instance to close
     * @param put2Cache true to try to put this mail connection into cache; otherwise false
     * @since v7.6.0
     */
    public static void closeInstance(final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess, final boolean put2Cache) {
        if (null != mailAccess) {
            try {
                mailAccess.close(put2Cache);
            } catch (Exception e) {
                LOG.error("Failed to close MailAccess instance", e);
            }
        }
    }

    /**
     * Increments the global access counter.
     * <p>
     * <b>Does nothing at all since v6.20.</b>
     */
    protected static final void incrementCounter() {
        // No-op
    }

    /**
     * Decrements the global access counter.
     * <p>
     * <b>Does nothing at all since v6.20.</b>
     */
    protected static final void decrementCounter() {
        // No-op
    }

    /**
     * Gets the optional properties used on connect.
     *
     * @return the mailProperties
     */
    public Properties getMailProperties() {
        return mailProperties;
    }

    /**
     * Sets optional properties used on connect. Herewith additional properties can be applied and checked later on.
     *
     * @param mailProperties The properties
     */
    public void setMailProperties(final Properties mailProperties) {
        this.mailProperties = mailProperties;
    }

    /**
     * Checks if all necessary fields are set in this access object.
     * <p>
     * This routine is implicitly invoked by {@link #connect()}.
     *
     * @throws OXException If a necessary field is missing
     * @see #connect()
     */
    protected void checkFieldsBeforeConnect(final MailConfig mailConfig) throws OXException {

        /*
         * Properties are implementation specific and therefore are created within connectInternal()
         */
        if (mailConfig.getServer() == null) {
            throw MailExceptionCode.MISSING_CONNECT_PARAM.create("mail server");
        } else if (checkMailServerPort() && (mailConfig.getPort() <= 0)) {
            throw MailExceptionCode.MISSING_CONNECT_PARAM.create("mail server port");
        } else if (mailConfig.getLogin() == null) {
            throw MailExceptionCode.MISSING_CONNECT_PARAM.create("login");
        }
    }

    /**
     * Pings the mail server to check if a connection can be established and and immediately closes connection.
     * <p>
     * Default implementation just delegates to {@link #connect()} but may be overridden in implementing subclass if not appropriate or a
     * faster way can be achieved.
     *
     * @return <code>true</code> if a connection can be established; otherwise <code>false</code>
     * @throws OXException If the ping fails
     */
    public boolean ping() throws OXException {
        try {
            final MailConfig mailConfig = getMailConfig();
            if (mailConfig.isRequireTls() || mailConfig.getMailProperties().isEnforceSecureConnection()) {
                if (!mailConfig.isSecure()) {
                    throw MailExceptionCode.NON_SECURE_DENIED.create(mailConfig.getServer());
                }
            }
            connect0(false);
            close(false);
            return true;
        } catch (final OXException e) {
            return false;
        }
    }

    /**
     * Opens this access. May be invoked on an already opened access.
     *
     * @throws OXException If the connection could not be established for various reasons
     */
    public final void connect() throws OXException {
        connect0(true);
    }

    /**
     * Opens this access. May be invoked on an already opened access.
     *
     * @param checkDefaultFolders <code>true</code> to check existence of default folders; otherwise <code>false</code> to omit check
     * @throws OXException If the connection could not be established for various reasons
     */
    public final void connect(final boolean checkDefaultFolders) throws OXException {
        connect0(checkDefaultFolders);
    }

    /**
     * Convenience method to obtain root folder in a fast way; meaning no default folder check is performed which is not necessary to return
     * the root folder.
     * <p>
     * The same result is yielded through calling <code>getFolderStorage().getRootFolder()</code> on a connected <tt>MailAccess</tt>.
     * <p>
     * Since this mail access instance is connected if not already done before, the {@link #close(boolean)} operation should be invoked
     * afterwards:
     *
     * <pre>
     * final MailAccess mailAccess = MailAccess.getInstance(session);
     * final MailFolder rootFolder = mailAccess.getRootFolder();
     * try {
     *  // Do something with root folder
     * } finally {
     *  mailAccess.close(putToCache)
     * }
     * </pre>
     *
     * @throws OXException If returning the root folder fails
     */
    public MailFolder getRootFolder() throws OXException {
        if (!isConnected()) {
            connect0(false);
        }
        return getFolderStorage().getRootFolder();
    }

    /**
     * Convenience method to obtain folder's number of unread messages in a fast way; meaning no default folder check is performed.
     * <p>
     * The same result is yielded through calling <code>getFolderStorage().getFolder().getUnreadMessageCount()</code> on a connected
     * <tt>MailAccess</tt>.
     * <p>
     * Since this mail access instance is connected if not already done before, the {@link #close(boolean)} operation should be invoked
     * afterwards:
     *
     * <pre>
     * final MailAccess mailAccess = MailAccess.getInstance(session);
     * final int unreadCount = mailAccess.getNumberOfUnreadMessages();
     * try {
     *  // Do something with unread count
     * } finally {
     *  mailAccess.close(putToCache)
     * }
     * </pre>
     *
     * @throws OXException If returning the unread count fails
     */
    public int getUnreadMessagesCount(final String fullname) throws OXException {
        if (!isConnected()) {
            connect0(false);
        }
        return getFolderStorage().getFolder(fullname).getUnreadMessageCount();
    }

    private final void connect0(final boolean checkDefaultFolder) throws OXException {
        applyNewThread();
        if (isConnected()) {
            if (checkDefaultFolder) {
                checkDefaultFolderOnConnect();
            }
        } else {
            MailConfig mailConfig = getMailConfig();
            checkFieldsBeforeConnect(mailConfig);
            if (!supports(mailConfig.getAuthType())) {
                throw MailExceptionCode.AUTH_TYPE_NOT_SUPPORTED.create(mailConfig.getAuthType().getName(), mailConfig.getServer());
            }
            try {
                connectInternal();
            } catch (OXException e) {
                throw handleConnectFailure(e, mailConfig);
            }
            if (checkDefaultFolder) {
                checkDefaultFolderOnConnect();
            }
        }
        if ((MailAccount.DEFAULT_ID == accountId) && (session instanceof PutIfAbsent)) {
            ((PutIfAbsent) session).setParameterIfAbsent(PARAM_MAIL_ACCESS, this);
        }
        if (isTrackable() && false == tracked) {
            MailAccessWatcher.addMailAccess(this);
            tracked = true;
        }
    }

    private OXException handleConnectFailure(OXException e, MailConfig mailConfig) {
        if (!MimeMailExceptionCode.LOGIN_FAILED.equals(e) && !MimeMailExceptionCode.INVALID_CREDENTIALS.equals(e)) {
            return e;
        }

        if (mailConfig.getAccountId() == MailAccount.DEFAULT_ID) {
            AuthenticationFailedHandlerService handlerService = ServerServiceRegistry.getInstance().getService(AuthenticationFailedHandlerService.class);
            if (null != handlerService) {
                try {
                    handlerService.handleAuthenticationFailed(e, Service.MAIL, mailConfig, session);
                    return e;
                } catch (OXException x) {
                    return x;
                }
            }
        }

        // Authentication failed... Check for OAuth-based authentication
        if (AuthType.isOAuthType(mailConfig.getAuthType())) {
            // Determine identifier of the associated OAuth account
            int oauthAccountId = mailConfig.getOAuthAccountId();
            if (oauthAccountId >= 0) {
                OAuthService oauthService = ServerServiceRegistry.getInstance().getService(OAuthService.class);
                if (null == oauthService) {
                    LOG.warn("Detected failed OAuth authentication, but unable to handle as needed service {} is missing", OAuthService.class.getSimpleName());
                } else {
                    try {
                        OAuthAccount oAuthAccount = oauthService.getAccount(oauthAccountId, session, session.getUserId(), session.getContextId());
                        String cburl = OAuthUtil.buildCallbackURL(oAuthAccount);
                        API api = oAuthAccount.getAPI();
                        Throwable cause = e.getCause();
                        return OAuthExceptionCodes.OAUTH_ACCESS_TOKEN_INVALID.create(cause, api.getShortName(), oAuthAccount.getId(), session.getUserId(), session.getContextId(), api.getFullName(), cburl);
                    } catch (Exception x) {
                        LOG.warn("Failed to handle failed OAuth authentication", x);
                    }
                }
            }
        }

        return e;
    }

    private void checkDefaultFolderOnConnect() throws OXException {
        if (isDefaultFoldersChecked()) {
            return;
        }

        Key key = new Key(session.getUserId(), session.getContextId());
        AcquiredLatch acquiredLatch = acquireFor(key);
        CountDownLatch latch = acquiredLatch.latch;
        if (Thread.currentThread() == acquiredLatch.owner) {
            // Perform the standard folder check
            try {
                getFolderStorage().checkDefaultFolders();
                acquiredLatch.result.set(Boolean.TRUE);
                return;
            } catch (OXException e) {
                acquiredLatch.result.set(e);
                throw e;
            } catch (Exception e) {
                MailConfig mailConfig = getMailConfig();
                String server = mailConfig.getServer();
                String login = mailConfig.getLogin();
                Integer contextId = Integer.valueOf(session.getContextId());
                Integer userId = Integer.valueOf(session.getUserId());

                OXException mailExc = MailExceptionCode.DEFAULT_FOLDER_CHECK_FAILED.create(e, server, userId, login, contextId, e.getMessage());
                LOG.error("", mailExc);
                closeInternal();
                acquiredLatch.result.set(mailExc);
                throw mailExc;
            } finally {
                latch.countDown();
                releaseFor(key);
            }
        }

        try {
            // Need to await 'til check done by concurrent thread
            latch.await();

            // Check if already locally available...
            Object result = acquiredLatch.result.get();
            if (result instanceof OXException) {
                throw  (OXException) result;
            }
        } catch (InterruptedException e) {
            throw MailExceptionCode.INTERRUPT_ERROR.create(e, new Object[0]);
        }
    }

    /**
     * Gets the value of <code>"mail.deffldflag"</code> cache entry.
     *
     * @return The value
     */
    protected boolean isDefaultFoldersChecked() {
        MailSessionCache cache = MailSessionCache.getInstance(session);
        if (null == cache) {
            return false;
        }
        Boolean b = cache.getParameter(accountId, MailSessionParameterNames.getParamDefaultFolderChecked());
        return (b != null) && b.booleanValue();
    }

    /**
     * Internal connect method to establish a mail connection.
     *
     * @param mailConfig The mail configuration providing connect and login data
     * @throws OXException If connection could not be established
     */
    protected abstract void connectInternal() throws OXException;

    /**
     * Checks if specified authentication type is supported by this mail access.
     *
     * @param authType The authentication type to check
     * @return <code>true</code> if authentication type is supported; otherwise <code>false</code>
     * @throws OXException If check fails
     */
    protected boolean supports(AuthType authType) throws OXException {
        return AuthType.LOGIN == authType;
    }

    @Override
    public void close() {
        try { close(true); } catch (final Exception x) { LOG.debug("Error while closing MailAccess instance.", x); }
    }

    /**
     * Closes this access.
     * <p>
     * An already closed access is not going to be put into cache and is treated as a no-op.
     *
     * @param put2Cache <code>true</code> to try to put this mail connection into cache; otherwise <code>false</code>
     */
    public final void close(final boolean put2Cache) {
        try {
            if (!isConnectedUnsafe()) {
                return;
            }
            boolean put = put2Cache;
            try {
                // Release all used, non-cachable resources
                releaseResources();
            } catch (final Exception e) {
                LOG.error("Resources could not be properly released. Dropping mail connection for safety reasons", e);
                put = false;
            }
            // resetFields();
            if (put && isCacheable()) {
                try {
                    // Cache connection if desired/possible anymore
                    if (getMailAccessCache().putMailAccess(session, accountId, this)) {
                        // Successfully cached: return
                        return;
                    }
                } catch (final Exception e) {
                    LOG.error("", e);
                }
            }
            // Close mail connection
            closeInternal();
        } finally {
            if (MailAccount.DEFAULT_ID == accountId) {
                session.setParameter(PARAM_MAIL_ACCESS, null);
            }
            // Remove from watcher no matter if cached or closed
            if (tracked) {
                MailAccessWatcher.removeMailAccess(this);
                tracked = false;
            }
            cleanUp();
        }
    }

    private static ThreadLocal<Queue<MimeCleanUp>> CLEAN_UPS = new ThreadLocal<Queue<MimeCleanUp>>() {
        @Override
        protected Queue<MimeCleanUp> initialValue() {
            return new ConcurrentLinkedQueue<MimeCleanUp>();
        }
    };

    /**
     * Remembers specified {@link MimeCleanUp} instance.
     *
     * @param mimeCleanUp The {@link MimeCleanUp} instance
     */
    public static void rememberMimeCleanUp(final MimeCleanUp mimeCleanUp) {
        if (null == mimeCleanUp) {
            return;
        }
        CLEAN_UPS.get().offer(mimeCleanUp);
    }

    private static void cleanUp() {
        final Queue<MimeCleanUp> queue = CLEAN_UPS.get();
        MimeCleanUp mimeCleanUp;
        while ((mimeCleanUp = queue.poll()) != null) {
            mimeCleanUp.cleanUp();
        }
    }

    /**
     * Logs the trace of the thread that lastly obtained this access.
     *
     */
    public void logTrace(StringBuilder sBuilder, org.slf4j.Logger log) {
        String lineSeparator = System.getProperty("line.separator");
        Thread usingThread = this.usingThread;
        if (null != usingThread) {
            Map<String, String> taskProps = usingThreadProperties;
            if (null != taskProps) {
                Map<String, String> sorted = new TreeMap<String, String>();
                for (Entry<String, String> entry : taskProps.entrySet()) {
                    String propertyName = entry.getKey();
                    String value = entry.getValue();
                    if (null != value) {
                        sorted.put(propertyName, value);
                    }
                }
                for (Map.Entry<String, String> entry : sorted.entrySet()) {
                    sBuilder.append(entry.getKey()).append('=').append(entry.getValue()).append(lineSeparator);
                }
                sBuilder.append(lineSeparator);
            }
        }
        sBuilder.append(toString());
        StackTraceElement[] traze = trace;
        int length;
        if (null != traze && (length = traze.length) > 3) {
            sBuilder.append(lineSeparator).append("Mail connection established (or fetched from cache) at: ").append(lineSeparator);
            /*
             * Start at index 3
             */
            {
                StackTraceElement[] tmp = new StackTraceElement[length - 3];
                System.arraycopy(traze, 3, tmp, 0, tmp.length);
                Throwable thr = new Throwable();
                thr.setStackTrace(tmp);
                log.info(sBuilder.toString(), thr);
                sBuilder.setLength(0);
            }
            if ((null != usingThread) && usingThread.isAlive()) {
                StackTraceElement[] trace = usingThread.getStackTrace();
                if (null != trace && trace.length > 0) {
                    sBuilder.append("Current Using Thread: ").append(usingThread.getName()).append(lineSeparator);
                    /*
                     * Only possibility to get the current working position of a thread. This is only called if a thread is caught by
                     * MailAccessWatcher.
                     */
                    Throwable thr = new FastThrowable();
                    thr.setStackTrace(trace);
                    log.info(sBuilder.toString(), thr);
                }
            }
        }
    }

    /**
     * Gets the trace of the thread that lastly obtained this access.
     * <p>
     * This is useful to detect certain threads which uses an access for a long time
     *
     * @return The trace of the thread that lastly obtained this access
     */
    public final String getTrace() {
        String lineSeparator = System.getProperty("line.separator");
        StringBuilder sBuilder = new StringBuilder(2048);
        {
            final Map<String, String> taskProps = usingThreadProperties;
            if (null != taskProps) {
                final Map<String, String> sorted = new TreeMap<String, String>();
                for (final Entry<String, String> entry : taskProps.entrySet()) {
                    final String propertyName = entry.getKey();
                    final String value = entry.getValue();
                    if (null != value) {
                        sorted.put(propertyName, value);
                    }
                }
                for (final Map.Entry<String, String> entry : sorted.entrySet()) {
                    sBuilder.append(entry.getKey()).append('=').append(entry.getValue()).append(lineSeparator);
                }
                sBuilder.append(lineSeparator);
            }
        }
        sBuilder.append(toString());
        sBuilder.append(lineSeparator).append("Mail connection established (or fetched from cache) at: ").append(lineSeparator);
        /*
         * Start at index 3
         */
        for (int i = 3; i < trace.length; i++) {
            sBuilder.append("    at ").append(trace[i]).append(lineSeparator);
        }
        if ((null != usingThread) && usingThread.isAlive()) {
            sBuilder.append("Current Using Thread: ").append(usingThread.getName()).append(lineSeparator);
            /*
             * Only possibility to get the current working position of a thread. This is only called if a thread is caught by
             * MailAccessWatcher.
             */
            StackTraceElement[] trace = usingThread.getStackTrace();
            sBuilder.append("    at ").append(trace[0]);
            for (int i = 1; i < trace.length; i++) {
                sBuilder.append(lineSeparator).append("    at ").append(trace[i]);
            }
        }
        return sBuilder.toString();
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder(256);
        builder.append("{ MailAccess [accountId=").append(accountId).append(", cached=").append(cached).append(", ");
        if (provider != null) {
            builder.append("provider=").append(provider).append(", ");
        }
        if (mailConfig != null) {
            builder.append("mailConfig=").append(mailConfig);
        }
        builder.append("] }");
        return builder.toString();
    }

    /**
     * Returns the mail configuration appropriate for current user. It provides needed connection and login information.
     *
     * @return The mail configuration
     */
    public MailConfig getMailConfig() throws OXException {
        if (null == mailConfig) {
            mailConfig = createMailConfig();
        }
        return mailConfig;
    }

    /**
     * Gets this mail access' account ID.
     *
     * @return The account ID
     */
    public int getAccountId() {
        return accountId;
    }

    /**
     * Creates a new user-specific mail configuration.
     *
     * @return A new user-specific mail configuration
     * @throws OXException If creating a new mail configuration fails
     */
    private final MailConfig createMailConfig() throws OXException {
        final MailConfig instance = createNewMailConfig();
        instance.setMailProperties(createNewMailProperties());
        return MailConfig.getConfig(instance, session, accountId);
    }

    /**
     * Apply new thread's trace information.
     */
    private final void applyNewThread() {
        usingThread = Thread.currentThread();
        usingThreadProperties = LogProperties.getPropertyMap();
        /*
         * This is faster than Thread.getStackTrace() since a native method is used to fill thread's stack trace
         */
        trace = new Throwable().getStackTrace();
    }

    /**
     * Checks if user's attempt to login to mail system is permitted or not.
     *
     * @param session The session
     * @param accountId The account identifier
     * @throws OXException If session's user denotes the context admin user and admin user's try to login to mail system is not permitted
     */
    private static final void checkLogin(Session session, int accountId) throws OXException {
        // Check permission
        UserPermissionBits permissionBits =  (session instanceof ServerSession) ? ((ServerSession) session).getUserPermissionBits() : UserPermissionBitsStorage.getInstance().getUserPermissionBits(session.getUserId(), session.getContextId());
        if (!permissionBits.hasWebMail()) {
            throw OXExceptions.noPermissionForModule("mail");
        }

        // Check admin login
        if (MailAccount.DEFAULT_ID == accountId && !MailProperties.getInstance().isAdminMailLoginEnabled()) {
            // Admin mail login is not permitted per configuration
            Context ctx = (session instanceof ServerSession) ? ((ServerSession) session).getContext() : ContextStorage.getStorageContext(session.getContextId());
            if (session.getUserId() == ctx.getMailadmin()) {
                throw MailExceptionCode.ACCOUNT_DOES_NOT_EXIST.create(Integer.valueOf(ctx.getContextId()));
            }
        }
    }

    /**
     * Indicates if this mail access is trackable by {@link MailAccessWatcher}.
     *
     * @return <code>true</code> if this mail access is trackable; otherwise <code>false</code>
     */
    public boolean isTrackable() {
        return trackable;
    }

    /**
     * Sets if this mail access is trackable by {@link MailAccessWatcher}.
     *
     * @param trackable <code>true</code> if this mail access is trackable; otherwise <code>false</code>
     */
    public void setTrackable(boolean trackable) {
        this.trackable = trackable;
    }

    /**
     * Gets the number of seconds this mail access is allowed to remain idle in {@link SingletonMailAccessCache cache} before being removed
     * and closed. If the default value shall be used for this mail access, return <code>-1</code>.
     *
     * @return The number of allowed idle seconds or <code>-1</code> to signal using default value.
     */
    public int getCacheIdleSeconds() {
        return -1;
    }

    /**
     * Indicates if this mail access is cacheable.
     *
     * @return <code>true</code> if this mail access is cacheable; otherwise <code>false</code>
     */
    public boolean isCacheable() {
        return cacheable;
    }

    /**
     * Sets whether this mail access is cacheable or not.
     *
     * @param cacheable <code>true</code> if this mail access is cacheable; otherwise <code>false</code>
     */
    public void setCacheable(final boolean cacheable) {
        this.cacheable = cacheable;
    }

    /**
     * Indicates if this mail access is currently cached in {@link IMailAccessCache}.
     *
     * @return <code>true</code> if this mail access is cached; otherwise <code>false</code>
     */
    public boolean isCached() {
        return cached;
    }

    /**
     * Sets whether this mail access is currently cached or not.
     *
     * @param cacheable <code>true</code> if this mail access is cached; otherwise <code>false</code>
     */
    public void setCached(final boolean cached) {
        this.cached = cached;
    }

    /**
     * Indicates if this mail access is currently waiting for any server notifications (idle mode).
     *
     * @return <code>true</code> if this mail access is waiting; otherwise <code>false</code>
     */
    public boolean isWaiting() {
        return waiting;
    }

    /**
     * Sets whether this mail access is currently waiting for any mail server notifications or not.
     *
     * @param waiting <code>true</code> if this mail access is waiting; otherwise <code>false</code>
     */
    public void setWaiting(boolean waiting) {
        if (waiting) {
            this.waiting = waiting;
        } else {
            boolean wasWaiting = this.waiting;
            this.waiting = waiting;
            if (wasWaiting) {
                // Switched from waiting to non-waiting mode
                if (tracked) {
                    MailAccessWatcher.touchMailAccess(this);
                }
            }
        }
    }

    /**
     * Gets an implementation-specific new instance of {@link MailConfig}.
     *
     * @return An implementation-specific new instance of {@link MailConfig}
     */
    protected abstract MailConfig createNewMailConfig();

    /**
     * Gets an implementation-specific new instance of {@link IMailProperties}.
     *
     * @return An implementation-specific new instance of {@link IMailProperties}
     * @throws OXException If creating a new instance of {@link IMailProperties} fails
     */
    protected abstract IMailProperties createNewMailProperties() throws OXException;

    /**
     * Defines if mail server port has to be present in provided mail configuration before establishing any connection.
     *
     * @return <code>true</code> if mail server port has to be set before establishing any connection; otherwise <code>false</code>
     */
    protected abstract boolean checkMailServerPort();

    /**
     * Releases all used resources prior to caching or closing a connection.
     */
    protected abstract void releaseResources();

    /**
     * Releases all used resources prior to caching or closing a connection.
     */
    public void invokeReleaseResources() {
        releaseResources();
    }

    /**
     * Internal close method to drop a mail connection.
     */
    protected abstract void closeInternal();

    /**
     * Gets the appropriate {@link IMailFolderStorage} implementation that is considered as the main entry point to a user's mailbox.
     *
     * @return The appropriate {@link IMailFolderStorage} implementation
     * @throws OXException If connection is not established
     */
    public abstract F getFolderStorage() throws OXException;

    /**
     * Gets the appropriate {@link IMailMessageStorage} implementation that provides necessary message-related operations/methods.
     *
     * @return The appropriate {@link IMailMessageStorage} implementation
     * @throws OXException If connection is not established
     */
    public abstract M getMessageStorage() throws OXException;

    /**
     * Gets the appropriate {@link MailLogicTools} implementation that provides operations/methods to create a reply/forward message from a
     * referenced message.
     *
     * @return The appropriate {@link MailLogicTools} implementation
     * @throws OXException If connection is not established
     */
    public abstract MailLogicTools getLogicTools() throws OXException;

    /**
     * Checks if this connection is currently connected.
     *
     * @return <code>true</code> if connected; otherwise <code>false</code>
     * @see #isConnectedUnsafe()
     */
    public abstract boolean isConnected();

    /**
     * Checks if this connection is currently connected in an unsafe, but faster manner than {@link #isConnected()}.
     *
     * @return <code>true</code> if connected; otherwise <code>false</code>
     * @see #isConnected()
     */
    public abstract boolean isConnectedUnsafe();

    /**
     * Triggers all necessary startup actions.
     *
     * @throws OXException If startup actions fail
     */
    protected abstract void startup() throws OXException;

    /**
     * Triggers all necessary shutdown actions.
     *
     * @throws OXException If shutdown actions fail
     */
    protected abstract void shutdown() throws OXException;

    // -----------------------------------------------------------------------------------------------------------------

    private static final class Key {

        private final int contextId;
        private final int userId;
        private final int hash;

        Key(int userId, int contextId) {
            super();
            this.userId = userId;
            this.contextId = contextId;

            int prime = 31;
            int result = prime * 1 + contextId;
            result = prime * result + userId;
            hash = result;
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof Key)) {
                return false;
            }
            Key other = (Key) obj;
            if (contextId != other.contextId) {
                return false;
            }
            if (userId != other.userId) {
                return false;
            }
            return true;
        }
    }

    private static final class AcquiredLatch {

        /** The associated latch */
        final CountDownLatch latch;

        /** The thread owning this instance */
        final Thread owner;

        /** The reference to resulting object */
        final AtomicReference<Object> result;

        AcquiredLatch(Thread owner, CountDownLatch latch) {
            super();
            this.owner = owner;
            this.latch = latch;
            result = new AtomicReference<Object>();
        }
    }

}
