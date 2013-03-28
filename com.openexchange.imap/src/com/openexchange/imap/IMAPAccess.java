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

package com.openexchange.imap;

import gnu.trove.ConcurrentTIntObjectHashMap;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.nio.charset.UnsupportedCharsetException;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import javax.mail.AuthenticationFailedException;
import javax.mail.MessagingException;
import javax.mail.event.FolderEvent;
import javax.mail.event.FolderListener;
import javax.mail.internet.idn.IDNA;
import javax.security.auth.Subject;
import org.cliffc.high_scale_lib.NonBlockingHashMap;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.imap.acl.ACLExtension;
import com.openexchange.imap.acl.ACLExtensionInit;
import com.openexchange.imap.cache.ListLsubCache;
import com.openexchange.imap.cache.ListLsubEntry;
import com.openexchange.imap.cache.MBoxEnabledCache;
import com.openexchange.imap.config.IIMAPProperties;
import com.openexchange.imap.config.IMAPConfig;
import com.openexchange.imap.config.IMAPSessionProperties;
import com.openexchange.imap.config.MailAccountIMAPProperties;
import com.openexchange.imap.converters.IMAPFolderConverter;
import com.openexchange.imap.entity2acl.Entity2ACLInit;
import com.openexchange.imap.notify.internal.IMAPNotifierMessageRecentListener;
import com.openexchange.imap.notify.internal.IMAPNotifierRegistry;
import com.openexchange.imap.ping.IMAPCapabilityAndGreetingCache;
import com.openexchange.imap.services.IMAPServiceRegistry;
import com.openexchange.java.Charsets;
import com.openexchange.java.StringAllocator;
import com.openexchange.log.Log;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.Protocol;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.IMailProperties;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.api.MailConfig;
import com.openexchange.mail.api.MailLogicTools;
import com.openexchange.mail.cache.IMailAccessCache;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.mime.MimeMailException;
import com.openexchange.mail.mime.MimeSessionPropertyNames;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.session.Session;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;
import com.openexchange.tools.ssl.TrustAllSSLSocketFactory;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;

/**
 * {@link IMAPAccess} - Establishes an IMAP access and provides access to storages.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class IMAPAccess extends MailAccess<IMAPFolderStorage, IMAPMessageStorage> {

    /**
     * Serial Version UID
     */
    private static final long serialVersionUID = -7510487764376433468L;

    /**
     * The logger instance for {@link IMAPAccess} class.
     */
    private static final transient org.apache.commons.logging.Log LOG =
        com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(IMAPAccess.class));

    /**
     * The max. temporary-down value; 5 Minutes.
     */
    private static final long MAX_TEMP_DOWN = 300000L;

    /**
     * The timeout for failed logins.
     */
    protected static final AtomicLong FAILED_AUTH_TIMEOUT = new AtomicLong();

    /**
     * Whether info logging is enabled for this class.
     */
    private static final boolean INFO = LOG.isInfoEnabled();

    /**
     * Whether debug logging is enabled for this class.
     */
    private static final boolean DEBUG = LOG.isDebugEnabled();

    private static final String KERBEROS_SESSION_SUBJECT = "kerberosSubject";

    private static final class SaslImapLoginAction implements PrivilegedExceptionAction<Object> {

        private final IMAPStore is;
        private final String server;
        private final int port;
        private final String login;
        private final String pw;

        protected SaslImapLoginAction(final IMAPStore is,final String server, final int port, final String login, final String pw) {
            super();
            this.is = is;
            this.server = server;
            this.port = port;
            this.login = login;
            this.pw = pw;
        }

        @Override
        public Object run() throws MessagingException {
            is.connect(server, port, login, pw);
            return null;
        }
    }

    private static final class Key {
        final int contextId;
        final int userId;
        final int hash;
        Key(final int userId, final int contextId) {
            super();
            this.userId = userId;
            this.contextId = contextId;
            final int prime = 31;
            int result = 1;
            result = prime * result + contextId;
            result = prime * result + userId;
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
            if (!(obj instanceof Key)) {
                return false;
            }
            final Key other = (Key) obj;
            if (contextId != other.contextId) {
                return false;
            }
            if (userId != other.userId) {
                return false;
            }
            return true;
        }
    }

    private static volatile Boolean validityDisabled;

    private static boolean validityDisabled() {
        Boolean tmp = validityDisabled;
        if (null == tmp) {
            synchronized (IMAPAccess.class) {
                tmp = validityDisabled;
                if (null == tmp) {
                    final ConfigurationService service = IMAPServiceRegistry.getServiceRegistry().getService(ConfigurationService.class);
                    tmp = Boolean.valueOf(service != null && service.getBoolProperty("com.openexchange.imap.validityDisabled", false));
                    validityDisabled = tmp;
                }
            }
        }
        return tmp.booleanValue();
    }

    /**
     * The validity map.
     */
    private static final ConcurrentMap<Key, ConcurrentTIntObjectHashMap<AtomicLong>> VALIDITY_MAP = new NonBlockingHashMap<IMAPAccess.Key, ConcurrentTIntObjectHashMap<AtomicLong>>();

    /**
     * Drops the user-associated validity.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     */
    public static void dropValidity(final int userId, final int contextId) {
        VALIDITY_MAP.remove(new Key(userId, contextId));
    }

    /**
     * Gets the current validity value.
     *
     * @param accountId The account identifier
     * @param session The associated session
     * @return The current validity or <code>0</code> if not initialized, yet
     */
    public static long getCurrentValidity(final int accountId, final Session session) {
        if (validityDisabled()) {
            return 0L;
        }
        final ConcurrentTIntObjectHashMap<AtomicLong> map = VALIDITY_MAP.get(new Key(session.getUserId(), session.getContextId()));
        if (null == map) {
            return 0L;
        }
        final AtomicLong validity = map.get(accountId);
        return null == validity ? 0L : validity.get();
    }

    /**
     * Gets the IMAP validity.
     *
     * @param imapAccess The IMAP access
     * @return The IMAP validity
     */
    private static IMAPValidity getIMAPValidity(final IMAPAccess imapAccess) {
        final Session session = imapAccess.session;
        final Key key = new Key(session.getUserId(), session.getContextId());
        ConcurrentTIntObjectHashMap<AtomicLong> map = VALIDITY_MAP.get(key);
        if (null == map) {
            final ConcurrentTIntObjectHashMap<AtomicLong> newMap = new ConcurrentTIntObjectHashMap<AtomicLong>(8);
            map = VALIDITY_MAP.putIfAbsent(key, newMap);
            if (null == map) {
                map = newMap;
            }
        }
        AtomicLong validity = map.get(imapAccess.accountId);
        if (null == validity) {
            final AtomicLong al = new AtomicLong(0L);
            validity = map.putIfAbsent(imapAccess.accountId, al);
            if (null == validity) {
                validity = al;
            }
        }
        return new DefaultIMAPValidity(validity, imapAccess);
    }

    /**
     * Increases current validity by one.
     *
     * @param accountId The account identifier
     * @param session The associated session
     * @return The increased validity value
     */
    public static long increaseCurrentValidity(final int accountId, final Session session) {
        if (validityDisabled()) {
            return 0L;
        }
        final Key key = new Key(session.getUserId(), session.getContextId());
        ConcurrentTIntObjectHashMap<AtomicLong> map = VALIDITY_MAP.get(key);
        if (null == map) {
            final ConcurrentTIntObjectHashMap<AtomicLong> newMap = new ConcurrentTIntObjectHashMap<AtomicLong>(8);
            map = VALIDITY_MAP.putIfAbsent(key, newMap);
            if (null == map) {
                map = newMap;
            }
        }
        AtomicLong validity = map.get(accountId);
        if (null == validity) {
            final AtomicLong al = new AtomicLong(0L);
            validity = map.putIfAbsent(accountId, al);
            if (null == validity) {
                validity = al;
            }
        }
        return validity.incrementAndGet();
    }

    /**
     * The flag indicating whether to use IMAPStoreCache.
     */
    private static final AtomicBoolean USE_IMAP_STORE_CACHE = new AtomicBoolean(true);

    /**
     * Remembers timed out servers for {@link IIMAPProperties#getImapTemporaryDown()} milliseconds. Any further attempts to connect to such
     * a server-port-pair will throw an appropriate exception.
     */
    private static volatile Map<HostAndPort, Long> timedOutServers;

    /**
     * Remembers whether a certain IMAP server supports the ACL extension.
     */
    private static volatile ConcurrentMap<String, Boolean> aclCapableServers;

    /**
     * The scheduled timer task to clean-up maps.
     */
    private static volatile ScheduledTimerTask cleanUpTimerTask;

    /*-
     * Member section
     */

    /**
     * The folder storage.
     */
    private transient IMAPFolderStorage folderStorage;

    /**
     * The message storage.
     */
    private transient IMAPMessageStorage messageStorage;

    /**
     * The mail logic tools.
     */
    private transient MailLogicTools logicTools;

    /**
     * The IMAP store.
     */
    private transient AccessedIMAPStore imapStore;

    /**
     * The IMAP session.
     */
    private transient javax.mail.Session imapSession;

    /**
     * The Kerberos subject.
     */
    private transient Subject kerberosSubject;

    /**
     * The IMAP protocol.
     */
    private final Protocol protocol;

    /**
     * The max. connection count.
     */
    private int maxCount;

    /**
     * The connected flag.
     */
    private boolean connected;

    /**
     * The server's host name.
     */
    private String server;

    /**
     * The server's port.
     */
    private int port;

    /**
     * The user's login name.
     */
    private String login;

    /**
     * The user's password.
     */
    private String password;

    /**
     * The client IP.
     */
    private String clientIp;

    /**
     * The IMAP configuration.
     */
    private volatile IMAPConfig imapConfig;

    /**
     * The validity counter.
     */
    private long validity;

    /**
     * A simple cache for max. count values per server.
     */
    private static volatile ConcurrentMap<String, Integer> maxCountCache;

    /**
     * Initializes a new {@link IMAPAccess IMAP access} for default IMAP account.
     *
     * @param session The session providing needed user data
     */
    protected IMAPAccess(final Session session) {
        super(session);
        validity = 0L;
        setMailProperties((Properties) System.getProperties().clone());
        maxCount = -1;
        protocol = IMAPProvider.PROTOCOL_IMAP;
    }

    /**
     * Initializes a new {@link IMAPAccess IMAP access}.
     *
     * @param session The session providing needed user data
     * @param accountId The account ID
     */
    protected IMAPAccess(final Session session, final int accountId) {
        super(session, accountId);
        validity = 0L;
        setMailProperties((Properties) System.getProperties().clone());
        maxCount = -1;
        protocol = IMAPProvider.PROTOCOL_IMAP;
    }

    private int getMaxCount() throws OXException {
        final ConcurrentMap<String, Integer> cache = maxCountCache;
        if (null == cache) {
            return protocol.getMaxCount(server, MailAccount.DEFAULT_ID == accountId);
        }
        Integer maxCount = cache.get(server);
        if (null == maxCount) {
            final Integer i = Integer.valueOf(protocol.getMaxCount(server, MailAccount.DEFAULT_ID == accountId));
            maxCount = cache.putIfAbsent(server, i);
            if (null == maxCount) {
                maxCount = i;
            }
        }
        return maxCount.intValue();
    }

    /**
     * Checks the validity for being put into cache.
     *
     * @return <code>true</code> if valid; otherwise <code>false</code>
     */
    public boolean checkValidity() {
        final long currentValidity = getCurrentValidity(accountId, session);
        return currentValidity <= 0 || validity >= currentValidity;
    }

    /**
     * Gets the underlying IMAP store.
     *
     * @return The IMAP store or <code>null</code> if this IMAP access is not connected
     */
    public AccessedIMAPStore getIMAPStore() {
        return imapStore;
    }

    /**
     * Checks if Kerberos authentication is supposed to be performed.
     *
     * @return <code>true</code> for Kerberos authentication; otherwise <code>false</code>
     */
    private boolean isKerberosAuth() {
        return MailAccount.DEFAULT_ID == accountId && null != kerberosSubject;
    }

    private static void handlePrivilegedActionException(final PrivilegedActionException e) throws MessagingException, OXException {
        if (null == e) {
            return;
        }
        final Exception cause = e.getException();
        if (null == cause) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e.getCause(), e.getMessage());
        }
        if (cause instanceof MessagingException) {
            throw (MessagingException) cause;
        }
        if (cause instanceof OXException) {
            throw (OXException) cause;
        }
        throw MailExceptionCode.UNEXPECTED_ERROR.create(cause.getCause(), cause.getMessage());
    }

    private void reset() {
        super.resetFields();
        folderStorage = null;
        messageStorage = null;
        logicTools = null;
        imapStore = null;
        imapSession = null;
        connected = false;
        kerberosSubject = null;
    }

    @Override
    public void releaseResources() {
        /*-
         *
         * Don't need to close when cached!
         *
        if (folderStorage != null) {
            try {
                folderStorage.releaseResources();
            } catch (final OXException e) {
                LOG.error(new StringBuilder("Error while closing IMAP folder storage: ").append(e.getMessage()).toString(), e);
            } finally {
                folderStorage = null;
            }
        }
        if (messageStorage != null) {
            try {
                messageStorage.releaseResources();
            } catch (final OXException e) {
                LOG.error(new StringBuilder("Error while closing IMAP message storage: ").append(e.getMessage()).toString(), e);
            } finally {
                messageStorage = null;

            }
        }
        if (logicTools != null) {
            logicTools = null;
        }
         */
    }

    @Override
    protected void closeInternal() {
        try {
            if (folderStorage != null) {
                try {
                    folderStorage.releaseResources();
                } catch (final OXException e) {
                    LOG.error("Error while closing IMAP folder storage,", e);
                }
            }
            if (null != messageStorage) {
                try {
                    messageStorage.releaseResources();
                } catch (final OXException e) {
                    LOG.error("Error while closing IMAP message storage.", e);
                }
            }
            if (imapStore != null) {
                if (useIMAPStoreCache()) {
                    IMAPStoreCache.getInstance().returnIMAPStore(imapStore.dropAndGetImapStore(), server, port, login, getIMAPValidity(this));
                } else {
                    try {
                        imapStore.close();
                        if (!checkValidity()) {
                            clearCachedConnections();
                        }
                    } catch (final MessagingException e) {
                        LOG.error("Error while closing IMAP store.", e);
                    } catch (final RuntimeException e) {
                        LOG.error("Error while closing IMAP store.", e);
                    }
                }
                // Drop in associated IMAPConfig instance
                final IMAPConfig ic = getIMAPConfig();
                if (null != ic) {
                    ic.dropImapStore();
                }
                imapStore = null;
            }
        } finally {
            reset();
        }
    }

    @Override
    protected MailConfig createNewMailConfig() {
        return new IMAPConfig(accountId);
    }

    @Override
    public MailConfig getMailConfig() throws OXException {
        IMAPConfig tmp = imapConfig;
        if (null == tmp) {
            synchronized (this) {
                tmp = imapConfig;
                if (null == tmp) {
                    imapConfig = tmp = (IMAPConfig) super.getMailConfig();
                }
            }
        }
        return tmp;
    }

    /**
     * Gets the IMAP configuration.
     *
     * @return The IMAP configuration
     */
    public IMAPConfig getIMAPConfig() {
        final IMAPConfig tmp = imapConfig;
        if (null == tmp) {
            try {
                return (IMAPConfig) getMailConfig();
            } catch (final OXException e) {
                // Cannot occur
                return null;
            }
        }
        return tmp;
    }

    @Override
    public int getUnreadMessagesCount(final String fullname) throws OXException {
        if (!isConnected()) {
            connect(false);
        }
        /*
         * Check for root folder
         */
        if (MailFolder.DEFAULT_FOLDER_ID.equals(fullname)) {
            return 0;
        }
        try {
            /*
             * Obtain IMAP folder
             */
            final IMAPFolder imapFolder = (IMAPFolder) imapStore.getFolder(fullname);
            final ListLsubEntry listEntry = ListLsubCache.getCachedLISTEntry(fullname, accountId, imapFolder, session);
            final boolean exists = "INBOX".equals(fullname) || (listEntry.exists());
            final IMAPConfig imapConfig = getIMAPConfig();
            if (!exists) {
                throw IMAPException.create(IMAPException.Code.FOLDER_NOT_FOUND, imapConfig, session, fullname);
            }
            final Set<String> attrs = listEntry.getAttributes();
            if (null != attrs) {
                for (final String attribute : attrs) {
                    if ("\\NonExistent".equalsIgnoreCase(attribute)) {
                        throw IMAPException.create(IMAPException.Code.FOLDER_NOT_FOUND, imapConfig, session, fullname);
                    }
                }
            }
            final int retval;
            /*
             * Selectable?
             */
            if (listEntry.canOpen()) {
                /*
                 * Check read access
                 */
                final ACLExtension aclExtension = imapConfig.getACLExtension();
                if (!aclExtension.aclSupport() || aclExtension.canRead(IMAPFolderConverter.getOwnRights(imapFolder, session, imapConfig))) {
                    retval = IMAPFolderConverter.getUnreadCount(imapFolder);
                } else {
                    // ACL support AND no read access
                    retval = -1;
                }
            } else {
                retval = -1;
            }
            return retval;
        } catch (final MessagingException e) {
            throw MimeMailException.handleMessagingException(e, getMailConfig(), session);
        }
    }

    @Override
    public boolean ping() throws OXException {
        final IMAPConfig config = getIMAPConfig();
        checkFieldsBeforeConnect(config);
        try {
            /*
             * Try to connect to IMAP server
             */
            final IIMAPProperties imapConfProps = (IIMAPProperties) config.getMailProperties();
            String tmpPass = getMailConfig().getPassword();
            if (tmpPass != null) {
                try {
                    tmpPass = new String(tmpPass.getBytes(Charsets.forName(imapConfProps.getImapAuthEnc())), Charsets.ISO_8859_1);
                } catch (final UnsupportedCharsetException e) {
                    LOG.error(e.getMessage(), e);
                }
            }
            /*
             * Get properties
             */
            final Properties imapProps = IMAPSessionProperties.getDefaultSessionProperties();
            if ((null != getMailProperties()) && !getMailProperties().isEmpty()) {
                imapProps.putAll(getMailProperties());
            }
            /*
             * Get parameterized IMAP session
             */
            final javax.mail.Session imapSession =
                setConnectProperties(config, imapConfProps.getImapTimeout(), imapConfProps.getImapConnectionTimeout(), imapProps);
            /*
             * Check if debug should be enabled
             */
            if (Boolean.parseBoolean(imapSession.getProperty(MimeSessionPropertyNames.PROP_MAIL_DEBUG))) {
                imapSession.setDebug(true);
                imapSession.setDebugOut(System.err);
            }
            IMAPStore imapStore = null;
            try {
                /*
                 * Get store
                 */
                imapStore = connectIMAPStore(false, imapSession, IDNA.toASCII(config.getServer()), config.getPort(), config.getLogin(), tmpPass, null);
                /*
                 * Add warning if non-secure
                 */
                try {
                    if (!config.isSecure() && !imapStore.hasCapability("STARTTLS")) {
                        if ("create".equals(session.getParameter("mail-account.validate.type"))) {
                            warnings.add(MailExceptionCode.NON_SECURE_CREATION.create());
                        } else {
                            warnings.add(MailExceptionCode.NON_SECURE_WARNING.create());
                        }
                    }
                } catch (final MessagingException e) {
                    // Ignore
                }
            } catch (final MessagingException e) {
                throw MimeMailException.handleMessagingException(e, config, session);
            } finally {
                if (null != imapStore) {
                    try {
                        imapStore.close();
                    } catch (final MessagingException e) {
                        LOG.warn(e.getMessage(), e);
                    }
                }
            }
            return true;
        } catch (final OXException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(new com.openexchange.java.StringAllocator("Ping to IMAP server \"").append(config.getServer()).append("\" failed").toString(), e);
            }
            return false;
        }
    }

    @Override
    protected void connectInternal() throws OXException {
        if (connected) {
            return;
        }
        final IMAPConfig config = getIMAPConfig();
        final Session s = config.getSession();
        this.kerberosSubject = (Subject) s.getParameter(KERBEROS_SESSION_SUBJECT);
        try {
            final IIMAPProperties imapConfProps = (IIMAPProperties) config.getMailProperties();
            final boolean tmpDownEnabled = (imapConfProps.getImapTemporaryDown() > 0);
            if (tmpDownEnabled) {
                /*
                 * Check if IMAP server is marked as being (temporary) down since connecting to it failed before
                 */
                checkTemporaryDown(imapConfProps);
            }
            String tmpPass = config.getPassword();
            if (tmpPass != null) {
                try {
                    tmpPass = new String(tmpPass.getBytes(Charsets.forName(imapConfProps.getImapAuthEnc())), Charsets.ISO_8859_1);
                } catch (final UnsupportedCharsetException e) {
                    LOG.error(e.getMessage(), e);
                }
            }
            final boolean certainPassword = false; //("10.20.30.205".equals(config.getServer()) && 17 == session.getUserId());
            if (certainPassword) {
                tmpPass = "secret";
            }
            final String proxyDelimiter = MailProperties.getInstance().getAuthProxyDelimiter();
            /*
             * Check for already failed authentication
             */
            String login;
            try {
                login = new String(config.getLogin().getBytes(Charsets.forName(imapConfProps.getImapAuthEnc())), Charsets.ISO_8859_1);
            } catch (final UnsupportedCharsetException e) {
                LOG.error(e.getMessage(), e);
                login = config.getLogin();
            }
            String user = login;
            String proxyUser = null;
            boolean isProxyAuth = false;
            if (proxyDelimiter != null && login.contains(proxyDelimiter)) {
                isProxyAuth = true;
                proxyUser = login.substring(0, login.indexOf(proxyDelimiter));
                user = login.substring(login.indexOf(proxyDelimiter) + proxyDelimiter.length(), login.length());
            }
            /*
             * Get properties
             */
            final Properties imapProps = IMAPSessionProperties.getDefaultSessionProperties();
            if ((null != getMailProperties()) && !getMailProperties().isEmpty()) {
                imapProps.putAll(getMailProperties());
            }
            /*
             * Kerberos and/or proxy authentication
             */
            final boolean kerberosAuth = isKerberosAuth();
            if (kerberosAuth || isProxyAuth) {
                imapProps.put("mail.imap.sasl.enable", "true");
                imapProps.put("mail.imap.sasl.authorizationid", user);
                imapProps.put("mail.imap.sasl.mechanisms", (kerberosAuth ? "GSSAPI" : "PLAIN"));
            }
            /*
             * Get parameterized IMAP session
             */
            imapSession = setConnectProperties(config, imapConfProps.getImapTimeout(), imapConfProps.getImapConnectionTimeout(), imapProps);
            /*
             * Check if debug should be enabled
             */
            final boolean certainUser = false; // ("devel-mail.netline.de".equals(config.getServer()) && 17 == session.getUserId());
            if (certainUser || Boolean.parseBoolean(imapSession.getProperty(MimeSessionPropertyNames.PROP_MAIL_DEBUG))) {
                imapSession.setDebug(true);
                imapSession.setDebugOut(System.out);
            }
            /*
             * Check if client IP address should be propagated
             */
            String clientIp = null;
            if (imapConfProps.isPropagateClientIPAddress() && isPropagateAccount(imapConfProps)) {
                final String ip = session.getLocalIp();
                if (!isEmpty(ip)) {
                    clientIp = ip;
                } else if (DEBUG) {
                    LOG.debug(new com.openexchange.java.StringAllocator(256).append("\n\n\tMissing client IP in session \"").append(session.getSessionID()).append(
                        "\" of user ").append(session.getUserId()).append(" in context ").append(session.getContextId()).append(".\n"));
                }
            } else if (DEBUG && MailAccount.DEFAULT_ID == accountId) {
                LOG.debug(new com.openexchange.java.StringAllocator(256).append("\n\n\tPropagating client IP address disabled on Open-Xchange server \"").append(
                    IMAPServiceRegistry.getService(ConfigurationService.class).getProperty("AJP_JVM_ROUTE")).append("\"\n").toString());
            }
            /*
             * Get connected store
             */
            this.server = IDNA.toASCII(config.getServer());
            this.port = config.getPort();
            this.login = isProxyAuth ? proxyUser : user;
            this.password = tmpPass;
            this.clientIp = clientIp;
            maxCount = getMaxCount();
            try {
                imapStore = new AccessedIMAPStore(this, connectIMAPStore(maxCount > 0), imapSession);
                if (DEBUG) {
                    final String lineSeparator = System.getProperty("line.separator");
                    final StringAllocator sb = new StringAllocator(1024);
                    sb.append(lineSeparator).append(lineSeparator);
                    sb.append("IMAP login performed...").append(lineSeparator);
                    sb.append("Queued in cache: ").append(MailAccess.getMailAccessCache().numberOfMailAccesses(session, accountId));
                    if (Log.appendTraceToMessage()) {
                        sb.append(lineSeparator);
                        appendStackTrace(new Throwable().getStackTrace(), lineSeparator, sb);
                        sb.append(lineSeparator).append(lineSeparator);
                        LOG.debug(sb.toString());
                    } else {
                        sb.append(lineSeparator).append(lineSeparator);
                        LOG.debug(sb.toString(), new Throwable());
                    }
                }
                final long currentValidity = getCurrentValidity(accountId, session);
                imapStore.setValidity(currentValidity);
                validity = currentValidity;
            } catch (final AuthenticationFailedException e) {
                throw e;
            } catch (final MessagingException e) {
                /*
                 * Check for a SocketTimeoutException
                 */
                if (tmpDownEnabled) {
                    final Exception nextException = e.getNextException();
                    if (SocketTimeoutException.class.isInstance(nextException)) {
                        /*
                         * Remember a timed-out IMAP server on connect attempt
                         */
                        timedOutServers.put(new HostAndPort(config.getServer(), config.getPort()), Long.valueOf(System.currentTimeMillis()));
                    }
                }
                throw e;
            }
            this.connected = true;
            /*
             * Register notifier task if enabled
             */
            if (MailAccount.DEFAULT_ID == accountId && config.getIMAPProperties().notifyRecent()) {
                /*
                 * This call is re-invoked during IMAPNotifierTask's run
                 */
                if (IMAPNotifierRegistry.getInstance().addTaskFor(accountId, session) && INFO) {
                    final com.openexchange.java.StringAllocator tmp = new com.openexchange.java.StringAllocator("\n\tStarted IMAP notifier for server \"").append(config.getServer());
                    tmp.append("\" with login \"").append(user);
                    tmp.append("\" (user=").append(session.getUserId());
                    tmp.append(", context=").append(session.getContextId()).append(").");
                    LOG.info(tmp.toString());
                }
            }
            /*
             * Add folder listener
             */
            // imapStore.addFolderListener(new ListLsubCacheFolderListener(accountId, session));
            /*
             * Add server's capabilities
             */
            config.initializeCapabilities(imapStore, session);
            /*
             * Special check for ACLs
             */
            if (config.isSupportsACLs()) {
                final String key = new com.openexchange.java.StringAllocator(server).append('@').append(port).toString();
                Boolean b = aclCapableServers.get(key);
                if (null == b) {
                    Boolean nb;
                    final IMAPFolder dummy = (IMAPFolder) imapStore.getFolder("INBOX");
                    try {
                        dummy.myRights();
                        nb = Boolean.TRUE;
                    } catch (MessagingException e) {
                        // MessagingException - If the server doesn't support the ACL extension
                        nb = Boolean.FALSE;
                    }
                    b = aclCapableServers.putIfAbsent(key, nb);
                    if (null == b) {
                        b = nb;
                    }
                }
                if (!b.booleanValue()) {
                    // MessagingException - If the server doesn't support the ACL extension
                    config.setAcl(false);
                }
            }
        } catch (final MessagingException e) {
            throw MimeMailException.handleMessagingException(e, config, session);
        }
    }

    private boolean isPropagateAccount(final IIMAPProperties imapConfProps) throws OXException {
        if (MailAccount.DEFAULT_ID == accountId) {
            return true;
        }

        final MailAccountStorageService storageService = IMAPServiceRegistry.getService(MailAccountStorageService.class);
        if (null == storageService) {
            return false;
        }
        final int[] ids = storageService.getByHostNames(imapConfProps.getPropagateHostNames(), session.getUserId(), session.getContextId());
        return Arrays.binarySearch(ids, accountId) >= 0;
    }

    /**
     * Gets a connected IMAP store
     *
     * @param fromCache <code>true</code> from cache; otherwise <code>false</code>
     * @return The connected IMAP store
     * @throws MessagingException If a messaging error occurs
     * @throws OXException If another error occurs
     */
    public IMAPStore connectIMAPStore(final boolean fromCache) throws MessagingException, OXException {
        return connectIMAPStore(fromCache, imapSession, server, port, login, password, clientIp);
    }

    /**
     * Clears cached IMAP connections.
     */
    protected void clearCachedConnections() {
        final IMAPStoreContainer container = IMAPStoreCache.getInstance().optContainer(server, port, login);
        if (null != container) {
            container.clear();
        }
        try {
            final IMailAccessCache mailAccessCache = MailAccess.getMailAccessCache();
            MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> tmp;
            while ((tmp = mailAccessCache.removeMailAccess(session, accountId)) != null) {
                tmp.close(false);
            }
        } catch (final Exception e) {
            // Ignore
        }
    }

    private static final String PROTOCOL = IMAPProvider.PROTOCOL_IMAP.getName();

    private IMAPStore connectIMAPStore(final boolean fromCache, final javax.mail.Session imapSession, final String server, final int port, final String login, final String pw, final String clientIp) throws MessagingException, OXException {
        final long st = DEBUG ? System.currentTimeMillis() : 0L;
        /*
         * Propagate client IP address
         */
        if (clientIp != null) {
            imapSession.getProperties().put("mail.imap.propagate.clientipaddress", clientIp);
        }
        imapSession.getProperties().put("mail.imap.failOnNOFetch", "true");
        /*
         * Cache failed authentication attempts
         */
        final long authTimeout = FAILED_AUTH_TIMEOUT.get();
        if (authTimeout > 0) {
            imapSession.getProperties().put("mail.imap.authTimeout", Long.toString(authTimeout));
        }
        /*
         * Get store...
         */
        if (fromCache && useIMAPStoreCache()) {
            return IMAPStoreCache.getInstance().borrowIMAPStore(accountId, imapSession, server, port, login, pw, session, getIMAPValidity(this));
        }
        /*
         * Establish a new one...
         */
        IMAPStore imapStore = (IMAPStore) imapSession.getStore(PROTOCOL);
        /*
         * ... and connect it
         */
        try {
            if (isKerberosAuth()) {
                try {
                    Subject.doAs(kerberosSubject, new SaslImapLoginAction(imapStore, server, port, login, pw));
                } catch (final PrivilegedActionException e) {
                    handlePrivilegedActionException(e);
                }
            } else {
                imapStore.connect(server, port, login, pw);
            }
        } catch (final AuthenticationFailedException e) {
            /*
             * Retry connect with AUTH=PLAIN disabled
             */
            imapSession.getProperties().put("mail.imap.auth.login.disable", "true");
            imapStore = (IMAPStore) imapSession.getStore(PROTOCOL);
            imapStore.connect(server, port, login, pw);
        }
        /*
         * Done
         */
        if (DEBUG) {
            final long dur = System.currentTimeMillis() - st;
            LOG.debug("IMAPAccess.connectIMAPStore() took " + dur + "msec.");
        }
        return imapStore;
    }

    private void checkTemporaryDown(final IIMAPProperties imapConfProps) throws OXException, IMAPException {
        final MailConfig mailConfig = getMailConfig();
        final HostAndPort key = new HostAndPort(mailConfig.getServer(), mailConfig.getPort());
        final Map<HostAndPort, Long> map = timedOutServers;
        if (null == map) {
            return;
        }
        final Long range = map.get(key);
        if (range != null) {
            if (System.currentTimeMillis() - range.longValue() <= imapConfProps.getImapTemporaryDown()) {
                /*
                 * Still treated as being temporary broken
                 */
                throw IMAPException.create(IMAPException.Code.CONNECT_ERROR, mailConfig.getServer(), mailConfig.getLogin());
            }
            map.remove(key);
        }
    }

    @Override
    public IMAPFolderStorage getFolderStorage() throws OXException {
        // connected = ((imapStore != null) && imapStore.isConnected());
        if (!connected) {
            throw IMAPException.create(IMAPException.Code.NOT_CONNECTED, getMailConfig(), session, new Object[0]);
        }
        if (null == folderStorage) {
            folderStorage = new IMAPFolderStorage(imapStore, this, session);
        }
        return folderStorage;
    }

    @Override
    public IMAPMessageStorage getMessageStorage() throws OXException {
        // connected = ((imapStore != null) && imapStore.isConnected());
        if (!connected) {
            throw IMAPException.create(IMAPException.Code.NOT_CONNECTED, getMailConfig(), session, new Object[0]);
        }
        if (null == messageStorage) {
            messageStorage = new IMAPMessageStorage(imapStore, this, session);
        }
        return messageStorage;
    }

    @Override
    public MailLogicTools getLogicTools() throws OXException {
        // connected = ((imapStore != null) && imapStore.isConnected());
        if (!connected) {
            throw IMAPException.create(IMAPException.Code.NOT_CONNECTED, getMailConfig(), session, new Object[0]);
        }
        if (null == logicTools) {
            logicTools = new MailLogicTools(session, accountId);
        }
        return logicTools;
    }

    @Override
    public boolean isConnected() {
        /*-
         *
        if (!connected) {
            return false;
        }
        return (connected = ((imapStore != null) && imapStore.isConnected()));
         */
        return connected;
    }

    @Override
    public boolean isConnectedUnsafe() {
        return connected;
    }

    @Override
    public boolean isCacheable() {
        if (!checkValidity()) {
            return false;
        }
        if (useIMAPStoreCache()) {
            return false;
        }
        return true;
    }

    private boolean useIMAPStoreCache() {
        return ((maxCount > 0) && USE_IMAP_STORE_CACHE.get());
    }

    /**
     * Gets used IMAP session
     *
     * @return The IMAP session
     */
    public javax.mail.Session getMailSession() {
        return imapSession;
    }

    @Override
    protected void startup() throws OXException {
        initMaps();
        IMAPCapabilityAndGreetingCache.init();
        MBoxEnabledCache.init();
        ACLExtensionInit.getInstance().start();
        Entity2ACLInit.getInstance().start();
        maxCountCache = new NonBlockingHashMap<String, Integer>(16);

        final ConfigurationService confService = IMAPServiceRegistry.getService(ConfigurationService.class);
        final boolean useIMAPStoreCache = null == confService ? true : confService.getBoolProperty("com.openexchange.imap.useIMAPStoreCache", true);
        USE_IMAP_STORE_CACHE.set(useIMAPStoreCache);
        long failedAuthTimeout;
        try {
            failedAuthTimeout = null == confService ? 10000L : Long.parseLong(confService.getProperty("com.openexchange.imap.failedAuthTimeout", "10000"));
        } catch (final NumberFormatException e) {
            failedAuthTimeout = 10000L;
        }
        FAILED_AUTH_TIMEOUT.set(failedAuthTimeout);

        {
            Field mc;
            try {
                mc = IMAPFolder.class.getDeclaredField("messageCache");
                mc.setAccessible(true);
            } catch (final SecurityException e) {
                mc = null;
            } catch (final NoSuchFieldException e) {
                mc = null;
            }
            IMAPFolderWorker.messageCacheField = mc;
        }

        {
            Field mss;
            try {
                mss = com.sun.mail.imap.MessageCache.class.getDeclaredField("messages");
                mss.setAccessible(true);
            } catch (final SecurityException e) {
                mss = null;
            } catch (final NoSuchFieldException e) {
                mss = null;
            }
            IMAPFolderWorker.messagesField = mss;
        }

        {
            Field ut;
            try {
                ut = IMAPFolder.class.getDeclaredField("uidTable");
                ut.setAccessible(true);
            } catch (final SecurityException e) {
                ut = null;
            } catch (final NoSuchFieldException e) {
                ut = null;
            }
            IMAPFolderWorker.uidTableField = ut;
        }
    }

    private static synchronized void initMaps() {
        if (null == timedOutServers) {
            timedOutServers = new NonBlockingHashMap<HostAndPort, Long>();
        }
        if (null == aclCapableServers) {
            aclCapableServers = new NonBlockingHashMap<String, Boolean>();
        }
        if (null == cleanUpTimerTask) {
            final TimerService timerService = IMAPServiceRegistry.getService(TimerService.class);
            if (null != timerService) {
                final Map<HostAndPort, Long> map1 = timedOutServers;
                final Runnable r = new Runnable() {

                    @Override
                    public void run() {
                        /*
                         * Clean-up temporary-down map
                         */
                        for (final Iterator<Entry<HostAndPort, Long>> iter = map1.entrySet().iterator(); iter.hasNext();) {
                            final Entry<HostAndPort, Long> entry = iter.next();
                            if (System.currentTimeMillis() - entry.getValue().longValue() > MAX_TEMP_DOWN) {
                                iter.remove();
                            }
                        }
                        /*
                         * Clean-up failed-login map
                         */
                        IMAPStore.cleanUpFailedAuths(FAILED_AUTH_TIMEOUT.get());
                    }
                };
                /*
                 * Schedule every minute
                 */
                cleanUpTimerTask = timerService.scheduleWithFixedDelay(r, 60000, 60000);
            }
        }
    }

    @Override
    protected void shutdown() throws OXException {
        USE_IMAP_STORE_CACHE.set(true);
        maxCountCache = null;
        validityDisabled = null;
        Entity2ACLInit.getInstance().stop();
        ACLExtensionInit.getInstance().stop();
        IMAPCapabilityAndGreetingCache.tearDown();
        MBoxEnabledCache.tearDown();
        IMAPSessionProperties.resetDefaultSessionProperties();
        IMAPNotifierMessageRecentListener.dropFullNameChecker();
        dropMaps();
    }

    private static synchronized void dropMaps() {
        final ScheduledTimerTask cleanUpTimerTask = IMAPAccess.cleanUpTimerTask;
        if (null != cleanUpTimerTask) {
            cleanUpTimerTask.cancel(false);
            IMAPAccess.cleanUpTimerTask = null;
        }
        if (null != aclCapableServers) {
            aclCapableServers = null;
        }
        timedOutServers = null;
    }

    @Override
    protected boolean checkMailServerPort() {
        return true;
    }

    private static final class ListLsubCacheFolderListener implements FolderListener {

        private final int accountId;

        private final Session session;

        ListLsubCacheFolderListener(final int accountId, final Session session) {
            this.accountId = accountId;
            this.session = session;
        }

        @Override
        public void folderRenamed(final FolderEvent e) {
            ListLsubCache.clearCache(accountId, session);
        }

        @Override
        public void folderDeleted(final FolderEvent e) {
            ListLsubCache.clearCache(accountId, session);
        }

        @Override
        public void folderCreated(final FolderEvent e) {
            ListLsubCache.clearCache(accountId, session);
        }
    } // End of ListLsubCacheFolderListener

    private static final class HostAndPort {

        private final String host;

        private final int port;

        private final int hashCode;

        public HostAndPort(final String host, final int port) {
            super();
            if (port < 0 || port > 0xFFFF) {
                throw new IllegalArgumentException("port out of range:" + port);
            }
            if (host == null) {
                throw new IllegalArgumentException("hostname can't be null");
            }
            this.host = host;
            this.port = port;
            hashCode = (host.toLowerCase(Locale.ENGLISH).hashCode()) ^ port;
        }

        @Override
        public int hashCode() {
            return hashCode;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final HostAndPort other = (HostAndPort) obj;
            if (host == null) {
                if (other.host != null) {
                    return false;
                }
            } else if (!host.equals(other.host)) {
                return false;
            }
            if (port != other.port) {
                return false;
            }
            return true;
        }
    }

    @Override
    protected IMailProperties createNewMailProperties() throws OXException {
        final MailAccountStorageService storageService = IMAPServiceRegistry.getService(MailAccountStorageService.class, true);
        return new MailAccountIMAPProperties(storageService.getMailAccount(accountId, session.getUserId(), session.getContextId()));
    }

    private static javax.mail.Session setConnectProperties(final IMAPConfig config, final int timeout, final int connectionTimeout, final Properties imapProps) {
        /*
         * Set timeouts
         */
        if (timeout > 0) {
            imapProps.put("mail.imap.timeout", String.valueOf(timeout));
        }
        if (connectionTimeout > 0) {
            imapProps.put("mail.imap.connectiontimeout", String.valueOf(connectionTimeout));
        }
        /*
         * Check if a secure IMAP connection should be established
         */
        final String sPort = String.valueOf(config.getPort());
        final String socketFactoryClass = TrustAllSSLSocketFactory.class.getName();
        if (config.isSecure()) {
            /*
             * Enables the use of the STARTTLS command.
             */
            // imapProps.put("mail.imap.starttls.enable", "true");
            /*
             * Set main socket factory to a SSL socket factory
             */
            imapProps.put("mail.imap.socketFactory.class", socketFactoryClass);
            imapProps.put("mail.imap.socketFactory.port", sPort);
            imapProps.put("mail.imap.socketFactory.fallback", "false");
            /*
             * Needed for JavaMail >= 1.4
             */
            // Security.setProperty("ssl.SocketFactory.provider", socketFactoryClass);
        } else {
            /*
             * Enables the use of the STARTTLS command (if supported by the server) to switch the connection to a TLS-protected connection.
             */
            if (config.getIMAPProperties().isEnableTls()) {
                try {
                    final InetSocketAddress socketAddress = new InetSocketAddress(IDNA.toASCII(config.getServer()), config.getPort());
                    final Map<String, String> capabilities =
                        IMAPCapabilityAndGreetingCache.getCapabilities(socketAddress, false, config.getIMAPProperties());
                    if (null != capabilities) {
                        if (capabilities.containsKey("STARTTLS")) {
                            imapProps.put("mail.imap.starttls.enable", "true");
                        }
                    } else {
                        imapProps.put("mail.imap.starttls.enable", "true");
                    }
                } catch (final IOException e) {
                    imapProps.put("mail.imap.starttls.enable", "true");
                }
            }
            /*
             * Specify the javax.net.ssl.SSLSocketFactory class, this class will be used to create IMAP SSL sockets if TLS handshake says
             * so.
             */
            imapProps.put("mail.imap.socketFactory.port", sPort);
            imapProps.put("mail.imap.ssl.socketFactory.class", socketFactoryClass);
            imapProps.put("mail.imap.ssl.socketFactory.port", sPort);
            imapProps.put("mail.imap.socketFactory.fallback", "false");
            /*
             * Specify SSL protocols
             */
            imapProps.put("mail.imap.ssl.protocols", "SSLv3 TLSv1");
            // imapProps.put("mail.imap.ssl.enable", "true");
            /*
             * Needed for JavaMail >= 1.4
             */
            // Security.setProperty("ssl.SocketFactory.provider", socketFactoryClass);
        }
        /*
         * Create new IMAP session from initialized properties
         */
        return javax.mail.Session.getInstance(imapProps, null);
    }

    @Override
    public String toString() {
        if (null != imapStore) {
            return imapStore.toString();
        }
        return "[not connected]";
    }

    /**
     * Checks if given string is empty.
     *
     * @param s The string to check
     * @return <code>true</code> if empty; otherwise <code>false</code>
     */
    private static boolean isEmpty(final String string) {
        if (null == string) {
            return true;
        }
        final int len = string.length();
        boolean isWhitespace = true;
        for (int i = 0; isWhitespace && i < len; i++) {
            isWhitespace = Character.isWhitespace(string.charAt(i));
        }
        return isWhitespace;
    }

    private static final int MAX_STACK_TRACE_ELEMENTS = 1000;

    private static void appendStackTrace(final StackTraceElement[] trace, final String lineSeparator, final com.openexchange.java.StringAllocator sb) {
        if (null == trace) {
            return;
        }
        final int length = (MAX_STACK_TRACE_ELEMENTS <= trace.length) ? MAX_STACK_TRACE_ELEMENTS : trace.length;
        for (int i = 0; i < length; i++) {
            final StackTraceElement ste = trace[i];
            final String className = ste.getClassName();
            if (null != className) {
                sb.append("    at ").append(className).append('.').append(ste.getMethodName());
                if (ste.isNativeMethod()) {
                    sb.append("(Native Method)");
                } else {
                    final String fileName = ste.getFileName();
                    if (null == fileName) {
                        sb.append("(Unknown Source)");
                    } else {
                        final int lineNumber = ste.getLineNumber();
                        sb.append('(').append(fileName);
                        if (lineNumber >= 0) {
                            sb.append(':').append(lineNumber);
                        }
                        sb.append(')');
                    }
                }
                sb.append(lineSeparator);
            }
        }
    }

}
