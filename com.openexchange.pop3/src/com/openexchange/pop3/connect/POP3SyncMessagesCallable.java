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

package com.openexchange.pop3.connect;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.Callable;
import javax.mail.MessagingException;
import com.openexchange.mail.MailException;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.mime.MIMEMailException;
import com.openexchange.pop3.POP3Access;
import com.openexchange.pop3.config.IPOP3Properties;
import com.openexchange.pop3.config.POP3Config;
import com.openexchange.pop3.storage.POP3Storage;
import com.openexchange.pop3.storage.POP3StorageConnectCounter;
import com.openexchange.pop3.storage.POP3StorageProperties;
import com.openexchange.pop3.storage.POP3StoragePropertyNames;
import com.openexchange.pop3.util.POP3CapabilityCache;

/**
 * {@link POP3SyncMessagesCallable} - {@link Callable} to connect to POP3 account and synchronize its messages with POP3 storage.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class POP3SyncMessagesCallable implements Callable<Object> {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(POP3SyncMessagesCallable.class);

    private final POP3Access pop3Access;

    private final POP3Storage pop3Storage;

    private final POP3StorageProperties pop3StorageProperties;

    private final IMailFolderStorage folderStorage;

    private final POP3StorageConnectCounter connectCounter;

    /**
     * Initializes a new {@link POP3SyncMessagesCallable}.
     * 
     * @param pop3Access The POP3 access
     * @param pop3Storage The POP3 storage
     * @param pop3StorageProperties The POP3 storage properties
     * @param folderStorage The POP3 storage's folder storage instance
     * @param server Either the host name or textual representation of the IP address of the POP3 server
     */
    public POP3SyncMessagesCallable(final POP3Access pop3Access, final POP3Storage pop3Storage, final POP3StorageProperties pop3StorageProperties, final IMailFolderStorage folderStorage, final POP3StorageConnectCounter connectCounter) {
        super();
        this.pop3Access = pop3Access;
        this.pop3Storage = pop3Storage;
        this.pop3StorageProperties = pop3StorageProperties;
        this.folderStorage = folderStorage;
        this.connectCounter = connectCounter;
    }

    public Object call() throws Exception {
        /*
         * Is it allowed to connect to real POP3 account to synchronize messages?
         */
        final long refreshRate = getRefreshRateMillis();
        if (isConnectable(refreshRate)) {
            final String server;
            /*
             * Check refresh rate setting
             */
            try {
                final POP3Config pop3Config = pop3Access.getPOP3Config();
                server = pop3Config.getServer();
                final int port = pop3Config.getPort();
                final String capabilities = POP3CapabilityCache.getCapability(
                    InetAddress.getByName(server),
                    port,
                    pop3Config.isSecure(),
                    (IPOP3Properties) pop3Config.getMailProperties());
                /*
                 * Check refresh rate against minimum allowed seconds between logins provided that "LOGIN-DELAY" is contained in
                 * capabilities
                 */
                final int pos = capabilities.indexOf("LOGIN-DELAY");
                if (pos >= 0) {
                    final StringBuilder builder = new StringBuilder(16);
                    final char c = capabilities.charAt(pos + 11);
                    while ('\r' != c && '\n' != c) {
                        if (Character.isDigit(c)) {
                            builder.append(c);
                        }
                    }
                    try {
                        final int min = Integer.parseInt(builder.toString());
                        if ((min * 1000) > refreshRate) {
                            builder.setLength(0);
                            LOG.warn(builder.append("Refresh rate of ").append(refreshRate / 1000).append(
                                "sec is lower than minimum allowed seconds between logins (").append(min).append(')'));
                        }

                    } catch (final NumberFormatException nfe) {
                        LOG.warn("Cannot parse LOGIN-DELAY seconds from capabilities: " + nfe.getMessage(), nfe);
                    }
                }
            } catch (final UnknownHostException e) {
                throw MIMEMailException.handleMessagingException(new MessagingException(e.getMessage(), e));
            } catch (final IOException e) {
                throw new MailException(MailException.Code.IO_ERROR, e, e.getMessage());
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("\n\tSynchronizing messages with POP3 account: " + server);
            }
            /*
             * Check default folders since INBOX folder must be present prior to appending to it
             */
            folderStorage.checkDefaultFolders();
            /*
             * Sync messages
             */
            try {
                /*
                 * Access POP3 account and synchronize
                 */
                pop3Storage.syncMessages(isExpungeOnQuit(), connectCounter);
                /*
                 * Update last-accessed time stamp
                 */
                pop3StorageProperties.addProperty(
                    POP3StoragePropertyNames.PROPERTY_LAST_ACCESSED,
                    String.valueOf(System.currentTimeMillis()));
                if (LOG.isDebugEnabled()) {
                    LOG.debug("\n\tSynchronization successfully performed for POP3 account: " + server);
                }
            } catch (final MailException e) {
                LOG.warn("Connect to POP3 account failed: " + e.getMessage(), e);
            }
        }
        return null;
    }

    private boolean isConnectable(final long refreshRateMillis) throws MailException {
        final Long lastAccessed = getLastAccessed();
        return ((null == lastAccessed) || ((System.currentTimeMillis() - lastAccessed.longValue()) >= refreshRateMillis));
    }

    private Long getLastAccessed() throws MailException {
        final String lastAccessedStr = pop3StorageProperties.getProperty(POP3StoragePropertyNames.PROPERTY_LAST_ACCESSED);
        if (null == lastAccessedStr) {
            return null;
        }
        try {
            return Long.valueOf(lastAccessedStr);
        } catch (final NumberFormatException e) {
            LOG.warn(e.getMessage(), e);
            return null;
        }
    }

    private static final int FALLBACK_MINUTES = 10;

    private long getRefreshRateMillis() throws MailException {
        final String frequencyStr = pop3StorageProperties.getProperty(POP3StoragePropertyNames.PROPERTY_REFRESH_RATE);
        if (null != frequencyStr) {
            int minutes = 0;
            try {
                minutes = Integer.parseInt(frequencyStr);
            } catch (final NumberFormatException e) {
                LOG.warn(new StringBuilder(128).append("POP3 property \"").append(POP3StoragePropertyNames.PROPERTY_REFRESH_RATE).append(
                    "\" is not a number: ``").append(frequencyStr).append("''. Using fallback of ").append(FALLBACK_MINUTES).append(
                    " minutes."), e);
                minutes = FALLBACK_MINUTES;
            }
            return minutes * 60 * 1000;
        }
        // Fallback to 10 minutes
        LOG.warn(new StringBuilder(128).append("Missing POP3 property \"").append(POP3StoragePropertyNames.PROPERTY_REFRESH_RATE).append(
            "\"").append(". Using fallback of ").append(FALLBACK_MINUTES).append(" minutes."), new Throwable());
        return FALLBACK_MINUTES * 60 * 1000;
    }

    private boolean isExpungeOnQuit() throws MailException {
        final String expungeStr = pop3StorageProperties.getProperty(POP3StoragePropertyNames.PROPERTY_EXPUNGE);
        if (null != expungeStr) {
            return Boolean.parseBoolean(expungeStr);
        }
        return false;
    }

}
