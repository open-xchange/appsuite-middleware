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

import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.security.Security;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import javax.mail.AuthenticationFailedException;
import javax.mail.MessagingException;
import com.openexchange.mail.MailException;
import com.openexchange.mail.MailServletInterface;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.api.MailConfig;
import com.openexchange.mail.api.MailLogicTools;
import com.openexchange.mail.mime.MIMEMailException;
import com.openexchange.mail.mime.MIMESessionPropertyNames;
import com.openexchange.monitoring.MonitoringInfo;
import com.openexchange.pop3.config.POP3Config;
import com.openexchange.pop3.config.POP3Properties;
import com.openexchange.pop3.config.POP3SessionProperties;
import com.openexchange.session.Session;
import com.openexchange.tools.ssl.TrustAllSSLSocketFactory;
import com.sun.mail.pop3.POP3Folder;
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

    private static final String CHARENC_ISO8859 = "ISO-8859-1";

    private static final Map<HostAndPort, Long> timedOutServers = new ConcurrentHashMap<HostAndPort, Long>();

    private static final Map<LoginAndPass, Long> failedAuths = new ConcurrentHashMap<LoginAndPass, Long>();

    /*-
     * Members
     */

    private transient POP3FolderStorage folderStorage;

    private transient POP3MessageStorage messageStorage;

    private transient MailLogicTools logicTools;

    private transient POP3Store pop3Store;

    private transient javax.mail.Session pop3Session;

    private transient POP3InboxFolder inboxFolder;

    private boolean connected;

    private boolean decrement;

    /**
     * Initializes a new {@link POP3Access POP3 access} for default POP3 account.
     * 
     * @param session The session providing needed user data
     */
    protected POP3Access(final Session session) {
        super(session);
        setMailProperties((Properties) System.getProperties().clone());
    }

    /**
     * Initializes a new {@link POP3Access POP3 access}.
     * 
     * @param session The session providing needed user data
     * @param accountId The account ID
     */
    protected POP3Access(final Session session, final int accountId) {
        super(session, accountId);
        setMailProperties((Properties) System.getProperties().clone());
    }

    private void reset() {
        super.resetFields();
        folderStorage = null;
        messageStorage = null;
        logicTools = null;
        pop3Store = null;
        pop3Session = null;
        inboxFolder = null;
        connected = false;
        decrement = false;
    }

    @Override
    public int getCacheIdleSeconds() {
        return (POP3Properties.getInstance().getPOP3ConnectionIdleTime() / 1000);
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
    }

    @Override
    protected void closeInternal() {
        try {
            if (inboxFolder != null) {
                try {
                    inboxFolder.closeAndExpunge();
                } catch (final MailException e) {
                    LOG.error("Error while closing POP3 INBOX folder.", e);
                }
                inboxFolder = null;
            }
            if (pop3Store != null) {
                try {
                    pop3Store.close();
                } catch (final MessagingException e) {
                    LOG.error("Error while closing POP3Store.", e);
                }
                pop3Store = null;
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

    private static final String PROPERTY_SECURITY_PROVIDER = "ssl.SocketFactory.provider";

    private static final String ERR_CONNECT_TIMEOUT = "connect timed out";

    @Override
    protected void connectInternal() throws MailException {
        if ((pop3Store != null) && pop3Store.isConnected()) {
            connected = true;
            return;
        }
        try {
            final boolean tmpDownEnabled = (POP3Properties.getInstance().getPOP3TemporaryDown() > 0);
            if (tmpDownEnabled) {
                /*
                 * Check if POP3 server is marked as being (temporary) down since connecting to it failed before
                 */
                checkTemporaryDown();
            }
            String tmpPass = getMailConfig().getPassword();
            if (tmpPass != null) {
                try {
                    tmpPass = new String(tmpPass.getBytes(POP3Properties.getInstance().getPOP3AuthEnc()), CHARENC_ISO8859);
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
            final Properties pop3Props = POP3SessionProperties.getDefaultSessionProperties();
            if ((null != getMailProperties()) && !getMailProperties().isEmpty()) {
                pop3Props.putAll(getMailProperties());
            }
            /*
             * Check if a secure POP3 connection should be established
             */
            if (getMailConfig().isSecure()) {
                pop3Props.put("mail.pop3.socketFactory.class", TrustAllSSLSocketFactory.class.getName());
                pop3Props.put("mail.pop3.socketFactory.port", String.valueOf(getMailConfig().getPort()));
                pop3Props.put("mail.pop3.socketFactory.fallback", "false");
                pop3Props.put("mail.pop3.starttls.enable", "true");
                /*
                 * Needed for JavaMail >= 1.4
                 */
                Security.setProperty(PROPERTY_SECURITY_PROVIDER, TrustAllSSLSocketFactory.class.getName());
            }
            /*
             * Apply properties to POP3 session
             */
            pop3Session = javax.mail.Session.getInstance(pop3Props, null);
            /*
             * Check if debug should be enabled
             */
            if (Boolean.parseBoolean(pop3Session.getProperty(MIMESessionPropertyNames.PROP_MAIL_DEBUG))) {
                pop3Session.setDebug(true);
                pop3Session.setDebugOut(System.out);
            }
            /*
             * Get store
             */
            pop3Store = (POP3Store) pop3Session.getStore(POP3Provider.PROTOCOL_POP3.getName());
            /*
             * ... and connect
             */
            try {
                pop3Store.connect(getMailConfig().getServer(), getMailConfig().getPort(), login, tmpPass);
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
                     * Remember a timed-out POP3 server on connect attempt
                     */
                    timedOutServers.put(
                        new HostAndPort(getMailConfig().getServer(), getMailConfig().getPort()),
                        Long.valueOf(System.currentTimeMillis()));
                }
                throw e;
            }
            connected = true;
            // TODO: Perform sync with storage here
            
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
            // TODO: Put time-out to pop3.properties
            if (System.currentTimeMillis() - range.longValue() <= 10000) {
                throw new AuthenticationFailedException("Login failed: authentication failure");
            }
            failedAuths.remove(key);
        }
    }

    private void checkTemporaryDown() throws MailException, POP3Exception {
        final HostAndPort key = new HostAndPort(getMailConfig().getServer(), getMailConfig().getPort());
        final Long range = timedOutServers.get(key);
        if (range != null) {
            if (System.currentTimeMillis() - range.longValue() <= POP3Properties.getInstance().getPOP3TemporaryDown()) {
                /*
                 * Still treated as being temporary broken
                 */
                throw new POP3Exception(POP3Exception.Code.CONNECT_ERROR, getMailConfig().getServer(), getMailConfig().getLogin());
            }
            timedOutServers.remove(key);
        }
    }

    @Override
    public POP3FolderStorage getFolderStorage() throws MailException {
        connected = ((pop3Store != null) && pop3Store.isConnected());
        if (connected) {
            if (null == folderStorage) {
                folderStorage = new POP3FolderStorage(pop3Store, this, session);
            }
            return folderStorage;
        }
        throw new POP3Exception(POP3Exception.Code.NOT_CONNECTED);
    }

    @Override
    public POP3MessageStorage getMessageStorage() throws MailException {
        connected = ((pop3Store != null) && pop3Store.isConnected());
        if (connected) {
            if (null == messageStorage) {
                messageStorage = new POP3MessageStorage(this, session);
            }
            return messageStorage;
        }
        throw new POP3Exception(POP3Exception.Code.NOT_CONNECTED);
    }

    @Override
    public MailLogicTools getLogicTools() throws MailException {
        connected = ((pop3Store != null) && pop3Store.isConnected());
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
        if (!connected) {
            return false;
        }
        return (connected = ((pop3Store != null) && pop3Store.isConnected()));
    }

    @Override
    public boolean isConnectedUnsafe() {
        return connected;
    }

    /**
     * Gets used POP3 session.
     * 
     * @return The POP3 session
     */
    public javax.mail.Session getSession() {
        return pop3Session;
    }

    /**
     * Gets the POP3 INBOX folder.
     * 
     * @return The POP3 INBOX folder
     * @throws MailException If INBOX folder cannot be retrieved from POP3 store
     */
    public POP3InboxFolder getInboxFolder() throws MailException {
        if (null == inboxFolder) {
            try {
                inboxFolder = new POP3InboxFolder((POP3Folder) pop3Store.getFolder("INBOX"), session.getUserId(), session.getContextId());
            } catch (final MessagingException e) {
                throw MIMEMailException.handleMessagingException(e);
            }
        }
        return inboxFolder;
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
