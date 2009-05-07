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

package com.openexchange.imap;

import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.security.Security;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import javax.mail.AuthenticationFailedException;
import javax.mail.MessagingException;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.imap.acl.ACLExtensionInit;
import com.openexchange.imap.config.IMAPConfig;
import com.openexchange.imap.config.IMAPSessionProperties;
import com.openexchange.imap.entity2acl.Entity2ACLException;
import com.openexchange.imap.entity2acl.Entity2ACLInit;
import com.openexchange.imap.ping.IMAPCapabilityAndGreetingCache;
import com.openexchange.mail.MailException;
import com.openexchange.mail.MailServletInterface;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.api.MailConfig;
import com.openexchange.mail.api.MailLogicTools;
import com.openexchange.mail.mime.MIMEMailException;
import com.openexchange.mail.mime.MIMESessionPropertyNames;
import com.openexchange.monitoring.MonitoringInfo;
import com.openexchange.session.Session;
import com.openexchange.tools.ssl.TrustAllSSLSocketFactory;
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

    private static final transient org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(IMAPAccess.class);

    private static final String CHARENC_ISO8859 = "ISO-8859-1";

    private static final Map<HostAndPort, Long> timedOutServers = new ConcurrentHashMap<HostAndPort, Long>();

    private static final Map<LoginAndPass, Long> failedAuths = new ConcurrentHashMap<LoginAndPass, Long>();

    private transient IMAPFolderStorage folderStorage;

    private transient IMAPMessageStorage messageStorage;

    private transient MailLogicTools logicTools;

    private transient IMAPStore imapStore;

    private transient javax.mail.Session imapSession;

    private boolean connected;

    private boolean decrement;

    /**
     * Initializes a new {@link IMAPAccess IMAP access} for default IMAP account.
     * 
     * @param session The session providing needed user data
     */
    protected IMAPAccess(final Session session) {
        super(session);
        setMailProperties((Properties) System.getProperties().clone());
    }

    /**
     * Initializes a new {@link IMAPAccess IMAP access}.
     * 
     * @param session The session providing needed user data
     * @param accountId The account ID
     */
    protected IMAPAccess(final Session session, final int accountId) {
        super(session, accountId);
        setMailProperties((Properties) System.getProperties().clone());
    }

    private void reset() {
        super.resetFields();
        folderStorage = null;
        messageStorage = null;
        logicTools = null;
        imapStore = null;
        imapSession = null;
        connected = false;
        decrement = false;
    }

    @Override
    protected void releaseResources() {
        if (folderStorage != null) {
            try {
                folderStorage.releaseResources();
            } catch (final MailException e) {
                LOG.error(new StringBuilder("Error while closing IMAP folder storage: ").append(e.getMessage()).toString(), e);
            } finally {
                folderStorage = null;
            }
        }
        if (messageStorage != null) {
            try {
                messageStorage.releaseResources();
            } catch (final MailException e) {
                LOG.error(new StringBuilder("Error while closing IMAP message storage: ").append(e.getMessage()).toString(), e);
            } finally {
                messageStorage = null;

            }
        }
        if (logicTools != null) {
            logicTools = null;
        }
    }

    @Override
    protected void closeInternal() {
        try {
            if (imapStore != null) {
                try {
                    imapStore.close();
                } catch (final MessagingException e) {
                    LOG.error("Error while closing IMAPStore", e);
                }
                imapStore = null;
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
        return new IMAPConfig();
    }

    /**
     * Gets the IMAP configuration.
     * 
     * @return The IMAP configuration
     */
    public IMAPConfig getIMAPConfig() {
        try {
            return (IMAPConfig) getMailConfig();
        } catch (final MailException e) {
            /*
             * Cannot occur since already initialized
             */
            return null;
        }
    }

    private static final String PROPERTY_SECURITY_PROVIDER = "ssl.SocketFactory.provider";

    private static final String ERR_CONNECT_TIMEOUT = "connect timed out";

    @Override
    protected void connectInternal() throws MailException {
        if ((imapStore != null) && imapStore.isConnected()) {
            connected = true;
            return;
        }
        try {
            final boolean tmpDownEnabled = (IMAPConfig.getImapTemporaryDown() > 0);
            if (tmpDownEnabled) {
                /*
                 * Check if IMAP server is marked as being (temporary) down since connecting to it failed before
                 */
                checkTemporaryDown();
            }
            String tmpPass = getMailConfig().getPassword();
            if (tmpPass != null) {
                try {
                    tmpPass = new String(tmpPass.getBytes(IMAPConfig.getImapAuthEnc()), CHARENC_ISO8859);
                } catch (final UnsupportedEncodingException e) {
                    LOG.error(e.getMessage(), e);
                }
            }
            /*
             * Check for already failed authentication
             */
            final String login = getMailConfig().getLogin();
            checkFailedAuths(login, tmpPass);
            /*
             * Get properties
             */
            final Properties imapProps = IMAPSessionProperties.getDefaultSessionProperties();
            if ((null != getMailProperties()) && !getMailProperties().isEmpty()) {
                imapProps.putAll(getMailProperties());
            }
            /*
             * Check if a secure IMAP connection should be established
             */
            if (getMailConfig().isSecure()) {
                imapProps.put(MIMESessionPropertyNames.PROP_MAIL_IMAP_SOCKET_FACTORY_CLASS, TrustAllSSLSocketFactory.class.getName());
                imapProps.put(MIMESessionPropertyNames.PROP_MAIL_IMAP_SOCKET_FACTORY_PORT, String.valueOf(getMailConfig().getPort()));
                imapProps.put(MIMESessionPropertyNames.PROP_MAIL_IMAP_SOCKET_FACTORY_FALLBACK, "false");
                imapProps.put(MIMESessionPropertyNames.PROP_MAIL_IMAP_STARTTLS_ENABLE, "true");
                /*
                 * Needed for JavaMail >= 1.4
                 */
                Security.setProperty(PROPERTY_SECURITY_PROVIDER, TrustAllSSLSocketFactory.class.getName());
            }
            /*
             * Apply properties to IMAP session
             */
            imapSession = javax.mail.Session.getInstance(imapProps, null);
            /*
             * Check if debug should be enabled
             */
            if (Boolean.parseBoolean(imapSession.getProperty(MIMESessionPropertyNames.PROP_MAIL_DEBUG))) {
                imapSession.setDebug(true);
                imapSession.setDebugOut(System.err);
            }
            /*
             * Get store
             */
            imapStore = (IMAPStore) imapSession.getStore(IMAPProvider.PROTOCOL_IMAP.getName());
            /*
             * ... and connect
             */
            try {
                imapStore.connect(getMailConfig().getServer(), getMailConfig().getPort(), login, tmpPass);
            } catch (final AuthenticationFailedException e) {
                /*
                 * Remember failed authentication's credentials (for a short amount of time) to fasten subsequent connect trials
                 */
                failedAuths.put(new LoginAndPass(login, tmpPass), Long.valueOf(System.currentTimeMillis()));
                throw e;
            } catch (final MessagingException e) {
                /*
                 * TODO: Re-think if exception's message should be part of condition or just checking if nested exception is an instance of
                 * SocketTimeoutException
                 */
                if (tmpDownEnabled && SocketTimeoutException.class.isInstance(e.getNextException()) && ((SocketTimeoutException) e.getNextException()).getMessage().toLowerCase(
                    Locale.ENGLISH).indexOf(ERR_CONNECT_TIMEOUT) != -1) {
                    /*
                     * Remember a timed-out IMAP server on connect attempt
                     */
                    timedOutServers.put(
                        new HostAndPort(getMailConfig().getServer(), getMailConfig().getPort()),
                        Long.valueOf(System.currentTimeMillis()));
                }
                throw e;
            }
            connected = true;
            /*
             * Add server's capabilities
             */
            ((IMAPConfig) getMailConfig()).initializeCapabilities(imapStore);
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
        } catch (final MessagingException e) {
            throw MIMEMailException.handleMessagingException(e, getMailConfig());
        }
    }

    private static void checkFailedAuths(final String login, final String pass) throws AuthenticationFailedException {
        final LoginAndPass key = new LoginAndPass(login, pass);
        final Long range = failedAuths.get(key);
        if (range != null) {
            // TODO: Put time-out to imap.properties
            if (System.currentTimeMillis() - range.longValue() <= 10000) {
                throw new AuthenticationFailedException("Login failed: authentication failure");
            }
            failedAuths.remove(key);
        }
    }

    private void checkTemporaryDown() throws MailException, IMAPException {
        final HostAndPort key = new HostAndPort(getMailConfig().getServer(), getMailConfig().getPort());
        final Long range = timedOutServers.get(key);
        if (range != null) {
            if (System.currentTimeMillis() - range.longValue() <= IMAPConfig.getImapTemporaryDown()) {
                /*
                 * Still treated as being temporary broken
                 */
                throw new IMAPException(IMAPException.Code.CONNECT_ERROR, getMailConfig().getServer(), getMailConfig().getLogin());
            }
            timedOutServers.remove(key);
        }
    }

    @Override
    public IMAPFolderStorage getFolderStorage() throws MailException {
        connected = ((imapStore != null) && imapStore.isConnected());
        if (connected) {
            if (null == folderStorage) {
                folderStorage = new IMAPFolderStorage(imapStore, this, session);
            }
            return folderStorage;
        }
        throw new IMAPException(IMAPException.Code.NOT_CONNECTED);
    }

    @Override
    public IMAPMessageStorage getMessageStorage() throws MailException {
        connected = ((imapStore != null) && imapStore.isConnected());
        if (connected) {
            if (null == messageStorage) {
                messageStorage = new IMAPMessageStorage(imapStore, this, session);
            }
            return messageStorage;
        }
        throw new IMAPException(IMAPException.Code.NOT_CONNECTED);
    }

    @Override
    public MailLogicTools getLogicTools() throws MailException {
        connected = ((imapStore != null) && imapStore.isConnected());
        if (connected) {
            if (null == logicTools) {
                logicTools = new MailLogicTools(session, accountId);
            }
            return logicTools;
        }
        throw new IMAPException(IMAPException.Code.NOT_CONNECTED);
    }

    @Override
    public boolean isConnected() {
        if (!connected) {
            return false;
        }
        return (connected = ((imapStore != null) && imapStore.isConnected()));
    }

    @Override
    public boolean isConnectedUnsafe() {
        return connected;
    }

    /**
     * Gets used IMAP session
     * 
     * @return The IMAP session
     */
    public javax.mail.Session getSession() {
        return imapSession;
    }

    @Override
    protected void startup() throws MailException {
        IMAPCapabilityAndGreetingCache.init();
        try {
            ACLExtensionInit.getInstance().start();
        } catch (final MailException e) {
            throw e;
        } catch (final AbstractOXException e) {
            throw new MailException(e);
        }
        try {
            Entity2ACLInit.getInstance().start();
        } catch (final Entity2ACLException e) {
            throw new MailException(e);
        } catch (final MailException e) {
            throw e;
        } catch (final AbstractOXException e) {
            throw new MailException(e);
        }
    }

    @Override
    protected void shutdown() throws MailException {
        try {
            Entity2ACLInit.getInstance().stop();
        } catch (final Entity2ACLException e) {
            throw new MailException(e);
        } catch (final MailException e) {
            throw e;
        } catch (final AbstractOXException e) {
            throw new MailException(e);
        }
        try {
            ACLExtensionInit.getInstance().stop();
        } catch (final MailException e) {
            throw e;
        } catch (final AbstractOXException e) {
            throw new MailException(e);
        }
        IMAPCapabilityAndGreetingCache.tearDown();
        IMAPSessionProperties.resetDefaultSessionProperties();
    }

    @Override
    protected boolean checkMailServerPort() {
        return true;
    }

    private static final class LoginAndPass {

        private final String login;

        private final String pass;

        private final int hashCode;

        public LoginAndPass(final String login, final String pass) {
            super();
            this.login = login;
            this.pass = pass;
            hashCode = (login.hashCode()) ^ (pass.hashCode());
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
            final LoginAndPass other = (LoginAndPass) obj;
            if (login == null) {
                if (other.login != null) {
                    return false;
                }
            } else if (!login.equals(other.login)) {
                return false;
            }
            if (pass == null) {
                if (other.pass != null) {
                    return false;
                }
            } else if (!pass.equals(other.pass)) {
                return false;
            }
            return true;
        }

    }

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

}
