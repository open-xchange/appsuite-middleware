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
import javax.mail.Store;
import javax.mail.internet.idn.IDNA;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.api.IMailProperties;
import com.openexchange.mail.api.IMailStoreAware;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.api.MailConfig;
import com.openexchange.mail.api.MailLogicTools;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.mime.MimeMailException;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.pop3.config.MailAccountPOP3Properties;
import com.openexchange.pop3.config.POP3Config;
import com.openexchange.pop3.config.POP3SessionProperties;
import com.openexchange.pop3.connect.POP3StoreConnector;
import com.openexchange.pop3.connect.POP3SyncMessagesCallable;
import com.openexchange.pop3.services.POP3ServiceRegistry;
import com.openexchange.pop3.storage.POP3Storage;
import com.openexchange.pop3.storage.POP3StorageProperties;
import com.openexchange.pop3.storage.POP3StorageProvider;
import com.openexchange.pop3.storage.POP3StorageProviderRegistry;
import com.openexchange.pop3.storage.mailaccount.MailAccountPOP3StorageProvider;
import com.openexchange.pop3.util.POP3CapabilityCache;
import com.openexchange.pop3.util.POP3StorageUtil;
import com.openexchange.session.Session;
import com.sun.mail.pop3.POP3Store;

/**
 * {@link POP3Access} - Establishes a POP3 access and provides access to storages.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class POP3Access extends MailAccess<POP3FolderStorage, POP3MessageStorage> implements IMailStoreAware {

    /**
     * Serial Version UID
     */
    private static final long serialVersionUID = -7510487764376433468L;

    private static final transient org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(POP3Access.class);

    private static final ConcurrentMap<LoginKey, Future<Object>> SYNCHRONIZER_MAP = new ConcurrentHashMap<LoginKey, Future<Object>>();

    private static final class LoginKey {

        public static LoginKey N(final InetSocketAddress server, final String login) {
            return new LoginKey(server, login);
        }

        private final InetSocketAddress server;

        private final String login;

        private final int hash;

        private LoginKey(final InetSocketAddress server, final String login) {
            super();
            this.server = server;
            this.login = login;
            final int prime = 31;
            int result = 1;
            result = prime * result + ((login == null) ? 0 : login.hashCode());
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
            if (!(obj instanceof LoginKey)) {
                return false;
            }
            final LoginKey other = (LoginKey) obj;
            if (login == null) {
                if (other.login != null) {
                    return false;
                }
            } else if (!login.equals(other.login)) {
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

    /*-
     * Members
     */

    private transient POP3Storage pop3Storage;

    private transient POP3StorageProperties pop3StorageProperties;

    private transient POP3FolderStorage folderStorage;

    private transient POP3MessageStorage messageStorage;

    private transient MailLogicTools logicTools;

    private boolean connected;

    /**
     * Initializes a new {@link POP3Access POP3 access} for default POP3 account.
     *
     * @param session The session providing needed user data
     * @throws OXException If initialization fails
     */
    public static POP3Access newInstance(final Session session) throws OXException {
        final POP3Access pop3Access = new POP3Access(session);
        applyPOP3Storage(pop3Access);
        return pop3Access;
    }

    /**
     * Initializes a new {@link POP3Access POP3 access} for default POP3 account.
     *
     * @param session The session providing needed user data
     * @param accountId The account ID
     * @throws OXException If initialization fails
     */
    public static POP3Access newInstance(final Session session, final int accountId) throws OXException {
        final POP3Access pop3Access = new POP3Access(session, accountId);
        applyPOP3Storage(pop3Access);
        return pop3Access;
    }

    /**
     * Applies the POP3 storage to given POP3 access.
     *
     * @param pop3Access The POP3 access
     * @throws OXException If POP3 storage initialization fails
     */
    private static void applyPOP3Storage(final POP3Access pop3Access) throws OXException {
        final Session session = pop3Access.session;
        if (null != session) {
            final int user = session.getUserId();
            final int cid = session.getContextId();
            /*
             * At least this property must be kept in database
             */
            String providerName = POP3StorageUtil.getPOP3StorageProviderName(pop3Access.accountId, user, cid);
            if (null == providerName) {
                OXException e = POP3ExceptionCode.MISSING_POP3_STORAGE_NAME.create(Integer.valueOf(user), Integer.valueOf(cid));
                LOG.debug("Using fallback storage \"mailaccount\".", e);
                providerName = MailAccountPOP3StorageProvider.NAME;
                /*
                 * Add to properties if marker is absent
                 */
                if (!"validate".equals(session.getParameter("mail-account.request"))) {
                    POP3StorageUtil.setPOP3StorageProviderName(pop3Access.accountId, user, cid, providerName);
                }
            }
            final POP3StorageProvider provider = POP3StorageProviderRegistry.getInstance().getPOP3StorageProvider(providerName);
            if (null == provider) {
                throw POP3ExceptionCode.MISSING_POP3_STORAGE.create(Integer.valueOf(user), Integer.valueOf(cid));
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

    @Override
    public boolean isStoreSupported() throws OXException {
        return (pop3Storage instanceof IMailStoreAware) && ((IMailStoreAware) pop3Storage).isStoreSupported();
    }

    @Override
    public Store getStore() throws OXException {
        if (pop3Storage instanceof IMailStoreAware) {
            IMailStoreAware storeAware = (IMailStoreAware) pop3Storage;
            if (storeAware.isStoreSupported()) {
                return storeAware.getStore();
            }
        }

        throw MailExceptionCode.UNSUPPORTED_OPERATION.create();
    }

    /**
     * Gets this POP3 access' session.
     *
     * @return The session
     */
    @Override
    public Session getSession() {
        return session;
    }

    /**
     * Gets the POP3 storage.
     *
     * @return The POP3 storage
     */
    public POP3Storage getPOP3Storage() {
        return pop3Storage;
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
        if (cacheable) {
            LOG.warn("", new UnsupportedOperationException("POP3Access.setCacheable() not supported"));
        }
    }

    @Override
    protected void releaseResources() {
        if (folderStorage != null) {
            try {
                folderStorage.releaseResources();
            } catch (final OXException e) {
                LOG.error("Error while closing POP3 folder storage", e);
            } finally {
                folderStorage = null;
            }
        }
        if (messageStorage != null) {
            try {
                messageStorage.releaseResources();
            } catch (final OXException e) {
                LOG.error("Error while closing POP3 message storage", e);
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
                } catch (final OXException e) {
                    LOG.error("Error while closing POP3 storage.", e);
                }
            }
        } finally {
            /*
             * Reset
             */
            super.resetFields();
            // pop3Storage = null;
            // pop3StorageProperties = null;
            folderStorage = null;
            messageStorage = null;
            logicTools = null;
            connected = false;
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
        } catch (final OXException e) {
            /*
             * Cannot occur since already initialized
             */
            return null;
        }
    }

    @Override
    public MailFolder getRootFolder() throws OXException {
        pop3Storage.connect();
        addWarnings(pop3Storage.getWarnings());
        connected = true;
        return pop3Storage.getFolderStorage().getRootFolder();
    }

    @Override
    public int getUnreadMessagesCount(final String fullname) throws OXException {
        return pop3Storage.getUnreadMessagesCount(fullname);
    }

    @Override
    public boolean ping() throws OXException {
        final POP3Config config = getPOP3Config();
        checkFieldsBeforeConnect(config);
        POP3Store pop3Store = null;
        try {
            /*-
             * Some POP3 accounts specify a connect frequency limitation,
             * therefore skip ping check if:
             *
             * com.openexchange.pop3.allowPing=false
             */
            final ConfigurationService service = POP3ServiceRegistry.getServiceRegistry().getService(ConfigurationService.class);
            if (null == service || !service.getBoolProperty("com.openexchange.pop3.allowPing", false)) {
                if (null == service || service.getBoolProperty("com.openexchange.pop3.logDeniedPing", true)) {
                    warnings.add(POP3ExceptionCode.VALIDATE_DENIED.create());
                }
                return true;
            }
            closeQuietly(pop3Store);
            /*
             * Try to authenticate
             */
            boolean forceSecure = config.isStartTls() || config.isRequireTls() || config.getMailProperties().isEnforceSecureConnection();
            pop3Store = POP3StoreConnector.getPOP3Store(config, getMailProperties(), false, -1, session, false, forceSecure).getPop3Store();
            /*
             * Add warning if non-secure
             */
            try {
                if (!config.isSecure() && !pop3Store.capabilities().containsKey("STLS")) {
                    if ("create".equals(session.getParameter("mail-account.validate.type"))) {
                        warnings.add(MailExceptionCode.NON_SECURE_CREATION.create());
                    } else {
                        warnings.add(MailExceptionCode.NON_SECURE_WARNING.create());
                    }
                }
            } catch (final MessagingException e) {
                // Ignore
            }
        } catch (final OXException e) {
            throw e;
        } finally {
            /*
             * Close quietly
             */
            closeQuietly(pop3Store);
        }
        return true;
    }

    @Override
    protected void connectInternal() throws OXException {
        // Connect the storage
        pop3Storage.connect();
        addWarnings(pop3Storage.getWarnings());
        connected = true;
    }

    private void sync() throws OXException, Error {
        /*
         * Ensure exclusive connect through future since a POP3 account may only be connected to one client at the same time
         */
        final LoginKey key;
        try {
            final POP3Config config = getPOP3Config();
            key = LoginKey.N(new InetSocketAddress(InetAddress.getByName(IDNA.toASCII(config.getServer())), config.getPort()), config.getLogin());
        } catch (final UnknownHostException e) {
            throw MimeMailException.handleMessagingException(new MessagingException(e.getMessage(), e), getPOP3Config(), session);
        }
        Future<Object> f = SYNCHRONIZER_MAP.get(key);
        boolean removeFromMap = false;
        if (null == f) {
            final FutureTask<Object> ft =
                new FutureTask<Object>(new POP3SyncMessagesCallable(
                    this,
                    pop3Storage,
                    pop3StorageProperties,
                    getFolderStorage()));
            f = SYNCHRONIZER_MAP.putIfAbsent(key, ft);
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
            addWarnings(pop3Storage.getWarnings());
        } catch (final InterruptedException e) {
            // Keep interrupted status
            Thread.currentThread().interrupt();
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (final CancellationException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (final ExecutionException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof OXException) {
                throw ((OXException) cause);
            }
            if (cause instanceof RuntimeException) {
                throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
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
                SYNCHRONIZER_MAP.remove(key);
            }
        }
    }

    @Override
    public POP3FolderStorage getFolderStorage() throws OXException {
        if (!connected) {
            throw POP3ExceptionCode.NOT_CONNECTED.create();
        }
        if (null == folderStorage) {
            folderStorage = new POP3FolderStorage(pop3Storage);
        }
        return folderStorage;
    }

    @Override
    public POP3MessageStorage getMessageStorage() throws OXException {
        if (!connected) {
            throw POP3ExceptionCode.NOT_CONNECTED.create();
        }
        sync();
        if (null == messageStorage) {
            messageStorage = new POP3MessageStorage(pop3Storage, accountId, session);
        }
        return messageStorage;
    }

    @Override
    public MailLogicTools getLogicTools() throws OXException {
        if (!connected) {
            throw POP3ExceptionCode.NOT_CONNECTED.create();
        }
        if (null == logicTools) {
            logicTools = new MailLogicTools(session, accountId);
        }
        return logicTools;
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
    protected void startup() throws OXException {
        POP3CapabilityCache.init();
        POP3StoreConnector.startUp();
    }

    @Override
    protected void shutdown() throws OXException {
        POP3StoreConnector.shutDown();
        POP3SessionProperties.resetDefaultSessionProperties();
        POP3CapabilityCache.tearDown();
    }

    @Override
    protected boolean checkMailServerPort() {
        return true;
    }

    @Override
    protected IMailProperties createNewMailProperties() throws OXException {
        try {
            final MailAccountStorageService storageService =
                POP3ServiceRegistry.getServiceRegistry().getService(MailAccountStorageService.class, true);
            return new MailAccountPOP3Properties(storageService.getMailAccount(accountId, session.getUserId(), session.getContextId()));
        } catch (final OXException e) {
            throw e;
        }
    }

    private static void closeQuietly(POP3Store pop3Store) {
        if (null != pop3Store) {
            try {
                pop3Store.close();
            } catch (final MessagingException e) {
                LOG.warn("", e);
            }
        }
    }

}
