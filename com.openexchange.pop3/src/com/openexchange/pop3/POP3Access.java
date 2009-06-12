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

package com.openexchange.pop3;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Properties;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import javax.mail.MessagingException;
import com.openexchange.mail.MailException;
import com.openexchange.mail.MailServletInterface;
import com.openexchange.mail.api.IMailProperties;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.api.MailConfig;
import com.openexchange.mail.api.MailLogicTools;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.mime.MIMEMailException;
import com.openexchange.mailaccount.MailAccountException;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.monitoring.MonitoringInfo;
import com.openexchange.pop3.config.MailAccountPOP3Properties;
import com.openexchange.pop3.config.POP3Config;
import com.openexchange.pop3.config.POP3SessionProperties;
import com.openexchange.pop3.connect.POP3ConnectCallable;
import com.openexchange.pop3.connect.POP3StoreConnector;
import com.openexchange.pop3.services.POP3ServiceRegistry;
import com.openexchange.pop3.storage.POP3Storage;
import com.openexchange.pop3.storage.POP3StorageProperties;
import com.openexchange.pop3.storage.POP3StorageProvider;
import com.openexchange.pop3.storage.POP3StorageProviderRegistry;
import com.openexchange.pop3.storage.mailaccount.MailAccountPOP3StorageProvider;
import com.openexchange.pop3.util.POP3StorageUtil;
import com.openexchange.server.ServiceException;
import com.openexchange.session.Session;
import com.sun.mail.pop3.POP3Store;

/**
 * {@link POP3Access} - Establishes a POP3 access and provides access to storages.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class POP3Access extends MailAccess<POP3FolderStorage, POP3MessageStorage> {

    /**
     * Serial Version UID
     */
    private static final long serialVersionUID = -7510487764376433468L;

    private static final transient org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(POP3Access.class);

    private static final ConcurrentMap<InetSocketAddress, Future<Object>> CONNECT_MAP = new ConcurrentHashMap<InetSocketAddress, Future<Object>>();

    /*-
     * Members
     */

    private POP3Storage pop3Storage;

    private POP3StorageProperties pop3StorageProperties;

    private transient POP3FolderStorage folderStorage;

    private transient POP3MessageStorage messageStorage;

    private transient MailLogicTools logicTools;

    private boolean connected;

    private boolean decrement;

    /**
     * Initializes a new {@link POP3Access POP3 access} for default POP3 account.
     * 
     * @param session The session providing needed user data
     * @throws MailException If initialization fails
     */
    protected static POP3Access newInstance(final Session session) throws MailException {
        final POP3Access pop3Access = new POP3Access(session);
        applyPOP3Storage(pop3Access);
        return pop3Access;
    }

    /**
     * Initializes a new {@link POP3Access POP3 access} for default POP3 account.
     * 
     * @param session The session providing needed user data
     * @param accountId The account ID
     * @throws MailException If initialization fails
     */
    protected static POP3Access newInstance(final Session session, final int accountId) throws MailException {
        final POP3Access pop3Access = new POP3Access(session, accountId);
        applyPOP3Storage(pop3Access);
        return pop3Access;
    }

    /**
     * Applies the POP3 storage to given POP3 access.
     * 
     * @param pop3Access The POP3 access
     * @throws MailException If POP3 storage initialization fails
     */
    private static void applyPOP3Storage(final POP3Access pop3Access) throws MailException {
        final Session session = pop3Access.session;
        if (null != session) {
            final int user = session.getUserId();
            final int cid = session.getContextId();
            // At least this property must be kept in database
            String providerName = POP3StorageUtil.getPOP3StorageProviderName(pop3Access.accountId, user, cid);
            if (null == providerName) {
                final POP3Exception e = new POP3Exception(
                    POP3Exception.Code.MISSING_POP3_STORAGE_NAME,
                    Integer.valueOf(user),
                    Integer.valueOf(cid));
                LOG.warn("Using fallback storage \"mailaccount\". Error: " + e.getMessage(), e);
                providerName = MailAccountPOP3StorageProvider.NAME;
                // Add to properties
                POP3StorageUtil.setPOP3StorageProviderName(pop3Access.accountId, user, cid, providerName);
            }
            final POP3StorageProvider provider = POP3StorageProviderRegistry.getInstance().getPOP3StorageProvider(providerName);
            if (null == provider) {
                throw new POP3Exception(POP3Exception.Code.MISSING_POP3_STORAGE, Integer.valueOf(user), Integer.valueOf(cid));
            }
            final POP3StorageProperties properties = provider.getPOP3StorageProperties(pop3Access);
            pop3Access.pop3Storage = provider.getPOP3Storage(pop3Access, properties);
            pop3Access.pop3StorageProperties = properties;
        }
    }

    /**
     * Initializes a new {@link POP3Access POP3 access} for default POP3 account.
     * 
     * @param session The session providing needed user data
     */
    private POP3Access(final Session session) {
        super(session);
        setMailProperties((Properties) System.getProperties().clone());
    }

    /**
     * Initializes a new {@link POP3Access POP3 access}.
     * 
     * @param session The session providing needed user data
     * @param accountId The account ID
     */
    private POP3Access(final Session session, final int accountId) {
        super(session, accountId);
        setMailProperties((Properties) System.getProperties().clone());
    }

    private void reset() {
        super.resetFields();
        pop3Storage = null;
        pop3StorageProperties = null;
        folderStorage = null;
        messageStorage = null;
        logicTools = null;
        connected = false;
        decrement = false;
    }

    /**
     * Gets this POP3 access' session.
     * 
     * @return The session
     */
    public Session getSession() {
        return session;
    }

    /**
     * Since POP3 account's messages are kept in a separate storage, a {@link POP3Access POP3 access} is not supposed to be cached.
     * 
     * @see com.openexchange.mail.api.MailAccess#isCacheable()
     */
    @Override
    public boolean isCacheable() {
        return false;
    }

    @Override
    public void setCacheable(final boolean cacheable) {
        if (cacheable && LOG.isWarnEnabled()) {
            final UnsupportedOperationException e = new UnsupportedOperationException("POP3Access.setCacheable() not supported");
            LOG.warn(e.getMessage(), e);
        }
    }

    @Override
    protected void releaseResources() {
        if (folderStorage != null) {
            try {
                folderStorage.releaseResources();
            } catch (final MailException e) {
                LOG.error(new StringBuilder("Error while closing POP3 folder storage: ").append(e.getMessage()).toString(), e);
            } finally {
                folderStorage = null;
            }
        }
        if (messageStorage != null) {
            try {
                messageStorage.releaseResources();
            } catch (final MailException e) {
                LOG.error(new StringBuilder("Error while closing POP3 message storage: ").append(e.getMessage()).toString(), e);
            } finally {
                messageStorage = null;

            }
        }
        if (logicTools != null) {
            logicTools = null;
        }
        if (pop3Storage != null) {
            pop3Storage.releaseResources();
        }
    }

    @Override
    protected void closeInternal() {
        try {
            if (pop3Storage != null) {
                try {
                    pop3Storage.close();
                } catch (final MailException e) {
                    LOG.error("Error while closing POP3 storage.", e);
                }
                pop3Storage = null;
            }
        } finally {
            if (decrement) {
                /*
                 * Decrease counters
                 */
                MailServletInterface.mailInterfaceMonitor.changeNumActive(false);
                MonitoringInfo.decrementNumberOfConnections(MonitoringInfo.IMAP);
                decrementCounter();
            }
            /*
             * Reset
             */
            reset();
        }
    }

    @Override
    protected MailConfig createNewMailConfig() {
        return new POP3Config();
    }

    /**
     * Gets the POP3 configuration.
     * 
     * @return The POP3 configuration
     */
    public POP3Config getPOP3Config() {
        try {
            return (POP3Config) getMailConfig();
        } catch (final MailException e) {
            /*
             * Cannot occur since already initialized
             */
            return null;
        }
    }

    @Override
    public MailFolder getRootFolder() throws MailException {
        pop3Storage.connect();
        return pop3Storage.getFolderStorage().getRootFolder();
    }

    @Override
    public void ping() throws MailException {
        final POP3Config config = getPOP3Config();
        checkFieldsBeforeConnect(config);
        final POP3Store pop3Store = POP3StoreConnector.getPOP3Store(config, getMailProperties(), false, session);
        /*
         * Close quietly
         */
        try {
            pop3Store.close();
        } catch (final MessagingException e) {
            LOG.warn(e.getMessage(), e);
        }
    }

    @Override
    protected void connectInternal() throws MailException {
        // Connect the storage
        pop3Storage.connect();
        connected = true;
        /*
         * Increase counter
         */
        MailServletInterface.mailInterfaceMonitor.changeNumActive(true);
        MonitoringInfo.incrementNumberOfConnections(MonitoringInfo.IMAP);
        incrementCounter();
        /*
         * Remember to decrement
         */
        decrement = true;
        /*
         * Ensure exclusive connect through future since a POP3 account may only be connected to one client at the same time
         */
        final InetSocketAddress server;
        try {
            server = new InetSocketAddress(InetAddress.getByName(getPOP3Config().getServer()), getPOP3Config().getPort());
        } catch (final UnknownHostException e) {
            throw MIMEMailException.handleMessagingException(new MessagingException(e.getMessage(), e), getPOP3Config(), session);
        }
        Future<Object> f = CONNECT_MAP.get(server);
        boolean removeFromMap = false;
        if (null == f) {
            final FutureTask<Object> ft = new FutureTask<Object>(new POP3ConnectCallable(
                pop3Storage,
                pop3StorageProperties,
                getFolderStorage(),
                getPOP3Config().getServer()));
            f = CONNECT_MAP.putIfAbsent(server, ft);
            if (f == null) {
                /*
                 * Yap, this thread's future task was put to map
                 */
                f = ft;
                removeFromMap = true;
                ft.run();
            }
        }
        /*
         * Get future's result
         */
        try {
            f.get();
        } catch (final InterruptedException e) {
            // Keep interrupted status
            Thread.currentThread().interrupt();
            throw new MailException(MailException.Code.UNEXPECTED_ERROR, e, e.getMessage());
        } catch (final CancellationException e) {
            throw new MailException(MailException.Code.UNEXPECTED_ERROR, e, e.getMessage());
        } catch (final ExecutionException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof MailException) {
                throw ((MailException) cause);
            }
            if (cause instanceof RuntimeException) {
                throw new MailException(MailException.Code.UNEXPECTED_ERROR, e, e.getMessage());
            }
            if (cause instanceof Error) {
                throw (Error) cause;
            }
            throw new IllegalStateException("Not unchecked", cause);
        } finally {
            if (removeFromMap) {
                /*
                 * And remove from map
                 */
                CONNECT_MAP.remove(server);
            }
        }
    }

    @Override
    public POP3FolderStorage getFolderStorage() throws MailException {
        if (connected) {
            if (null == folderStorage) {
                folderStorage = new POP3FolderStorage(pop3Storage);
            }
            return folderStorage;
        }
        throw new POP3Exception(POP3Exception.Code.NOT_CONNECTED);
    }

    @Override
    public POP3MessageStorage getMessageStorage() throws MailException {
        if (connected) {
            if (null == messageStorage) {
                messageStorage = new POP3MessageStorage(pop3Storage, accountId, session);
            }
            return messageStorage;
        }
        throw new POP3Exception(POP3Exception.Code.NOT_CONNECTED);
    }

    @Override
    public MailLogicTools getLogicTools() throws MailException {
        if (connected) {
            if (null == logicTools) {
                logicTools = new MailLogicTools(session, accountId);
            }
            return logicTools;
        }
        throw new POP3Exception(POP3Exception.Code.NOT_CONNECTED);
    }

    @Override
    public boolean isConnected() {
        return connected;
    }

    @Override
    public boolean isConnectedUnsafe() {
        return connected;
    }

    @Override
    protected void startup() throws MailException {
        // Nothing to start
    }

    @Override
    protected void shutdown() throws MailException {
        POP3SessionProperties.resetDefaultSessionProperties();
    }

    @Override
    protected boolean checkMailServerPort() {
        return true;
    }

    @Override
    protected IMailProperties createNewMailProperties() throws MailException {
        try {
            final MailAccountStorageService storageService = POP3ServiceRegistry.getServiceRegistry().getService(
                MailAccountStorageService.class,
                true);
            return new MailAccountPOP3Properties(storageService.getMailAccount(accountId, session.getUserId(), session.getContextId()));
        } catch (final ServiceException e) {
            throw new POP3Exception(e);
        } catch (final MailAccountException e) {
            throw new POP3Exception(e);
        }
    }

}
