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

import java.io.Serializable;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.mail.MailAccessWatcher;
import com.openexchange.mail.MailException;
import com.openexchange.mail.MailInitialization;
import com.openexchange.mail.MailProviderRegistry;
import com.openexchange.mail.MailSessionParameterNames;
import com.openexchange.mail.cache.MailAccessCache;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.session.Session;

/**
 * {@link MailAccess} - Handles connecting to the mailing system while using an internal cache for connected access objects (see
 * {@link MailAccessCache}).
 * <p>
 * Moreover it provides access to either message storage, folder storage and logic tools.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class MailAccess<F extends IMailFolderStorage, M extends IMailMessageStorage> implements Serializable {

    /**
     * Serial version UID
     */
    private static final long serialVersionUID = -2580495494392812083L;

    private static final transient org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(MailAccess.class);

    private static final AtomicInteger COUNTER = new AtomicInteger();

    private static final transient Lock LOCK_CON = new ReentrantLock();

    private static final transient Condition LOCK_CON_CONDITION = LOCK_CON.newCondition();

    /*-
     * ############### MEMBERS ###############
     */

    protected final transient Session session;

    protected final int accountId;

    protected boolean cacheable;

    private transient MailConfig mailConfig;

    private Properties mailProperties;

    private transient Thread usingThread;

    private StackTraceElement[] trace;

    /**
     * Initializes a new {@link MailAccess} for session user's default mail account.
     * 
     * @param session The session
     */
    protected MailAccess(final Session session) {
        this(session, MailAccount.DEFAULT_ID);
    }

    /**
     * Initializes a new {@link MailAccess}.
     * 
     * @param session The session
     * @param accountId The account ID
     */
    protected MailAccess(final Session session, final int accountId) {
        super();
        this.session = session;
        this.accountId = accountId;
        cacheable = true;
    }

    /**
     * Resets this access' settings.
     */
    protected final void resetFields() {
        mailProperties = null;
        usingThread = null;
        trace = null;
    }

    /**
     * Triggers all implementation-specific startup actions.
     * 
     * @param mailAccess An instance of {@link MailAccess}
     * @throws MailException If implementation-specific startup fails
     */
    static void startupImpl(final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess) throws MailException {
        mailAccess.startup();
    }

    /**
     * Triggers all implementation-specific shutdown actions.
     * 
     * @param mailAccess An instance of {@link MailAccess}
     * @throws MailException If implementation-specific shutdown fails
     */
    static void shutdownImpl(final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess) throws MailException {
        mailAccess.shutdown();
    }

    /**
     * Gets the proper instance of {@link MailAccess} for session user's default mail account.
     * <p>
     * When starting to work with obtained {@link MailAccess mail access} at first its {@link #connect()} method is supposed to be invoked.
     * On finished work the final {@link #close(boolean)} must be called:
     * 
     * <pre>
     * final MailAccess mailAccess = MailAccess.getInstance(session);
     * mailAccess.connect();
     * try {
     *  // Do something
     * } finally {
     *  mailAccess.close(putToCache)
     * }
     * </pre>
     * 
     * @param session The session
     * @return A proper instance of {@link MailAccess}
     * @throws MailException If instantiation fails or a caching error occurs
     */
    public static final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> getInstance(final Session session) throws MailException {
        return getInstance(session, MailAccount.DEFAULT_ID);
    }

    /**
     * Gets the proper instance of {@link MailAccess} parameterized with given session and account ID.
     * <p>
     * When starting to work with obtained {@link MailAccess mail access} at first its {@link #connect()} method is supposed to be invoked.
     * On finished work the final {@link #close(boolean)} must be called:
     * 
     * <pre>
     * final MailAccess mailAccess = MailAccess.getInstance(session, accountID);
     * mailAccess.connect();
     * try {
     * 	// Do something
     * } finally {
     * 	mailAccess.close(putToCache)
     * }
     * </pre>
     * 
     * @param session The session
     * @param accountId The account ID
     * @return A proper instance of {@link MailAccess}
     * @throws MailException If instantiation fails or a caching error occurs
     */
    public static final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> getInstance(final Session session, final int accountId) throws MailException {
        /*
         * Check for proper initialization
         */
        if (!MailInitialization.getInstance().isInitialized()) {
            throw new MailException(MailException.Code.INITIALIZATION_PROBLEM);
        }
        if (MailAccessCache.getInstance().containsMailAccess(session, accountId)) {
            final MailAccess<?, ?> mailAccess = MailAccessCache.getInstance().removeMailAccess(session, accountId);
            if (mailAccess != null) {
                return mailAccess;
            }
        }
        if (MailAccount.DEFAULT_ID == accountId) {
            /*
             * No cached connection available, check for admin login
             */
            checkAdminLogin(session, accountId);
        }
        /*
         * Check if a new connection may be established
         */
        if ((MailProperties.getInstance().getMaxNumOfConnections() > 0) && (COUNTER.get() > MailProperties.getInstance().getMaxNumOfConnections())) {
            LOCK_CON.lock();
            try {
                while (COUNTER.get() > MailProperties.getInstance().getMaxNumOfConnections()) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Too many mail connections currently established. Going asleep.");
                    }
                    LOCK_CON_CONDITION.await();
                }
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Woke up & mail access may again be established");
                }
                /*
                 * Try to fetch from cache again
                 */
                if (MailAccessCache.getInstance().containsMailAccess(session, accountId)) {
                    final MailAccess<?, ?> mailAccess = MailAccessCache.getInstance().removeMailAccess(session, accountId);
                    if (mailAccess != null) {
                        return mailAccess;
                    }
                }
            } catch (final InterruptedException e) {
                LOG.error(e.getMessage(), e);
                throw new MailException(MailException.Code.INTERRUPT_ERROR, e, new Object[0]);
            } finally {
                LOCK_CON.unlock();
            }
        }
        /*
         * Create a new mail access through user's mail provider
         */
        return MailProviderRegistry.getMailProviderBySession(session, accountId).createNewMailAccess(session, accountId);
    }

    /**
     * @return the global access counter
     */
    public static final int getCounter() {
        return COUNTER.get();
    }

    /**
     * Increments the global access counter.
     */
    protected static final void incrementCounter() {
        COUNTER.incrementAndGet();
    }

    /**
     * Decrements the global access counter.
     */
    protected static final void decrementCounter() {
        COUNTER.decrementAndGet();
    }

    /**
     * Gets the optional properties used on connect.
     * 
     * @return the mailProperties
     */
    public final Properties getMailProperties() {
        return mailProperties;
    }

    /**
     * Sets optional properties used on connect. Herewith additional properties can be applied and checked later on.
     * 
     * @param mailProperties The properties
     */
    public final void setMailProperties(final Properties mailProperties) {
        this.mailProperties = mailProperties;
    }

    /**
     * Checks if all necessary fields are set in this access object.
     * <p>
     * This routine is implicitly invoked by {@link #connect()}.
     * 
     * @throws MailException If a necessary field is missing
     * @see #connect()
     */
    protected final void checkFieldsBeforeConnect(final MailConfig mailConfig) throws MailException {

        /*
         * Properties are implementation specific and therefore are created within connectInternal()
         */
        if (mailConfig.getServer() == null) {
            throw new MailException(MailException.Code.MISSING_CONNECT_PARAM, "mail server");
        } else if (checkMailServerPort() && (mailConfig.getPort() <= 0)) {
            throw new MailException(MailException.Code.MISSING_CONNECT_PARAM, "mail server port");
        } else if (mailConfig.getLogin() == null) {
            throw new MailException(MailException.Code.MISSING_CONNECT_PARAM, "login");
        } else if (mailConfig.getPassword() == null) {
            throw new MailException(MailException.Code.MISSING_CONNECT_PARAM, "password");
        }
    }

    /**
     * Pings the mail server to check if a connection can be established and and immediately closes connection.
     * <p>
     * Default implementation just delegates to {@link #connect()} but may be overridden in implementing subclass if not appropriate or a
     * faster way can be achieved.
     * 
     * @return <code>true</code> if a connection can be established; otherwise <code>false</code>
     * @throws MailException If the ping fails
     */
    public boolean ping() throws MailException {
        try {
            connect0(false);
            close(false);
            return true;
        } catch (final MailException e) {
            return false;
        }
    }

    /**
     * Opens this access. May be invoked on an already opened access.
     * 
     * @throws MailException If the connection could not be established for various reasons
     */
    public final void connect() throws MailException {
        connect0(true);
    }

    /**
     * Convenience method to obtain root folder in a fast way; meaning no default folder check is performed which is not necessary to return
     * the root folder.
     * <p>
     * The same result is yielded through calling <code>getFolderStorage().getRootFolder()</code> on a connected {@link MailAccess}.
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
     * @throws MailException If returning the root folder fails
     */
    public MailFolder getRootFolder() throws MailException {
        connect0(false);
        return getFolderStorage().getRootFolder();
    }

    private final void connect0(final boolean checkDefaultFolder) throws MailException {
        applyNewThread();
        if (isConnected()) {
            getFolderStorage().checkDefaultFolders();
            MailAccessWatcher.addMailAccess(this);
            return;
        }
        checkFieldsBeforeConnect(getMailConfig());
        connectInternal();
        if (checkDefaultFolder) {
            try {
                getFolderStorage().checkDefaultFolders();
            } catch (final MailException e) {
                throw e;
            } catch (final Exception e) {
                final MailConfig mailConfig = getMailConfig();
                final MailException mailExc = new MailException(
                    MailException.Code.DEFAULT_FOLDER_CHECK_FAILED,
                    e,
                    mailConfig.getServer(),
                    Integer.valueOf(session.getUserId()),
                    mailConfig.getLogin(),
                    Integer.valueOf(session.getContextId()),
                    e.getMessage());
                LOG.error(mailExc.getMessage(), mailExc);
                closeInternal();
                throw mailExc;
            }
        }
        MailAccessWatcher.addMailAccess(this);
    }

    /**
     * Internal connect method to establish a mail connection.
     * 
     * @param mailConfig The mail configuration providing connect and login data
     * @throws MailException If connection could not be established
     */
    protected abstract void connectInternal() throws MailException;

    /**
     * Closes this access.
     * <p>
     * An already closed access is not going to be put into cache and is treated as a no-op.
     * 
     * @param put2Cache <code>true</code> to try to put this mail connection into cache; otherwise <code>false</code>
     */
    public final void close(final boolean put2Cache) {
        if (!isConnectedUnsafe()) {
            return;
        }
        boolean put = put2Cache;
        try {
            try {
                /*
                 * Release all used, non-cachable resources
                 */
                releaseResources();
            } catch (final Throwable t) {
                /*
                 * Dropping
                 */
                LOG.error("Resources could not be properly released. Dropping mail connection for safety reasons", t);
                put = false;
            }
            // resetFields();
            try {
                /*
                 * Cache connection if desired/possible anymore
                 */
                if (put && isCacheable() && MailAccessCache.getInstance().putMailAccess(session, accountId, this)) {
                    /*
                     * Successfully cached: signal & return
                     */
                    signalAvailableConnection();
                    return;
                }
            } catch (final MailException e) {
                LOG.error(e.getMessage(), e);
            }
            /*
             * Close mail connection
             */
            closeInternal();
            signalAvailableConnection();
        } finally {
            /*
             * Remove from watcher no matter if cached or closed
             */
            MailAccessWatcher.removeMailAccess(this);
        }
    }

    /**
     * Gets the trace of the thread that lastly obtained this access.
     * <p>
     * This is useful to detect certain threads which uses an access for a long time
     * 
     * @return the trace of the thread that lastly obtained this access
     */
    public final String getTrace() {
        final StringBuilder sBuilder = new StringBuilder(512);
        sBuilder.append(toString());
        sBuilder.append("\nIMAP connection established (or fetched from cache) at: ").append('\n');
        /*
         * Start at index 3
         */
        for (int i = 3; i < trace.length; i++) {
            sBuilder.append("\tat ").append(trace[i]).append('\n');
        }
        if ((null != usingThread) && usingThread.isAlive()) {
            sBuilder.append("Current Using Thread: ").append(usingThread.getName()).append('\n');
            /*
             * Only possibility to get the current working position of a thread. This is only called if a thread is caught by
             * MailAccessWatcher.
             */
            final StackTraceElement[] trace = usingThread.getStackTrace();
            sBuilder.append("\tat ").append(trace[0]);
            for (int i = 1; i < trace.length; i++) {
                sBuilder.append('\n').append("\tat ").append(trace[i]);
            }
        }
        return sBuilder.toString();
    }

    /**
     * Returns the mail configuration appropriate for current user. It provides needed connection and login information.
     * 
     * @return The mail configuration
     */
    public final MailConfig getMailConfig() throws MailException {
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
     * @throws MailException If creating a new mail configuration fails
     */
    private final MailConfig createMailConfig() throws MailException {
        final MailConfig instance = createNewMailConfig();
        instance.setMailProperties(createNewMailProperties());
        return MailConfig.getConfig(instance.getClass(), instance, session, accountId);
    }

    /**
     * Signals an available connection.
     */
    private void signalAvailableConnection() {
        if (MailProperties.getInstance().getMaxNumOfConnections() > 0) {
            LOCK_CON.lock();
            try {
                LOCK_CON_CONDITION.signalAll();
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Sending signal to possible waiting threads");
                }
            } finally {
                LOCK_CON.unlock();
            }
        }
    }

    /**
     * Apply new thread's trace information.
     */
    private final void applyNewThread() {
        usingThread = Thread.currentThread();
        /*
         * This is faster than Thread.getStackTrace() since a native method is used to fill thread's stack trace
         */
        trace = new Throwable().getStackTrace();
    }

    /**
     * Checks if session's user denotes the context admin user and whether admin user's try to login to mail system is permitted or not.
     * 
     * @param session The session
     * @param accountId The account ID
     * @throws MailException If session's user denotes the context admin user and admin user's try to login to mail system is not permitted
     */
    private static final void checkAdminLogin(final Session session, final int accountId) throws MailException {
        if (!MailProperties.getInstance().isAdminMailLoginEnabled()) {
            /*
             * Admin mail login is not permitted per configuration
             */
            Context ctx;
            try {
                ctx = (Context) session.getParameter(MailSessionParameterNames.getParamSessionContext(accountId));
            } catch (final ClassCastException e1) {
                ctx = null;
            }
            if (ctx == null) {
                try {
                    ctx = ContextStorage.getStorageContext(session.getContextId());
                } catch (final ContextException e) {
                    throw new MailException(e);
                }
            }
            if (session.getUserId() == ctx.getMailadmin()) {
                throw new MailException(MailException.Code.ACCOUNT_DOES_NOT_EXIST, Integer.valueOf(ctx.getContextId()));
            }
        }
    }

    /**
     * Gets the number of seconds this mail access is allowed to remain idle in {@link MailAccessCache cache} before being removed and
     * closed. If the default value shall be used for this mail access, return <code>-1</code>.
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
     * Gets an implementation-specific new instance of {@link MailConfig}.
     * 
     * @return An implementation-specific new instance of {@link MailConfig}
     */
    protected abstract MailConfig createNewMailConfig();

    /**
     * Gets an implementation-specific new instance of {@link IMailProperties}.
     * 
     * @return An implementation-specific new instance of {@link IMailProperties}
     * @throws MailException If creating a new instance of {@link IMailProperties} fails
     */
    protected abstract IMailProperties createNewMailProperties() throws MailException;

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
     * Internal close method to drop a mail connection.
     */
    protected abstract void closeInternal();

    /**
     * Gets the appropriate {@link IMailFolderStorage} implementation that is considered as the main entry point to a user's mailbox.
     * 
     * @return The appropriate {@link IMailFolderStorage} implementation
     * @throws MailException If connection is not established
     */
    public abstract F getFolderStorage() throws MailException;

    /**
     * Gets the appropriate {@link IMailMessageStorage} implementation that provides necessary message-related operations/methods.
     * 
     * @return The appropriate {@link IMailMessageStorage} implementation
     * @throws MailException If connection is not established
     */
    public abstract M getMessageStorage() throws MailException;

    /**
     * Gets the appropriate {@link MailLogicTools} implementation that provides operations/methods to create a reply/forward message from a
     * referenced message.
     * 
     * @return The appropriate {@link MailLogicTools} implementation
     * @throws MailException If connection is not established
     */
    public abstract MailLogicTools getLogicTools() throws MailException;

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
     * @throws MailException If startup actions fail
     */
    protected abstract void startup() throws MailException;

    /**
     * Triggers all necessary shutdown actions.
     * 
     * @throws MailException If shutdown actions fail
     */
    protected abstract void shutdown() throws MailException;

}
