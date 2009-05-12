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

package com.openexchange.imap.config;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import javax.mail.MessagingException;
import com.openexchange.imap.IMAPCapabilities;
import com.openexchange.imap.IMAPException;
import com.openexchange.mail.api.IMailProperties;
import com.openexchange.mail.api.MailCapabilities;
import com.openexchange.mail.api.MailConfig;
import com.openexchange.mail.config.MailConfigException;
import com.openexchange.mail.config.MailProperties;
import com.sun.mail.imap.IMAPStore;

/**
 * {@link IMAPConfig} - The IMAP configuration.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class IMAPConfig extends MailConfig {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(IMAPConfig.class);

    private static final String PROTOCOL_IMAP_SECURE = "imaps";

    private volatile IMAPCapabilities imapCapabilities;

    private int imapPort;

    private String imapServer;

    private boolean secure;

    private IIMAPProperties mailProperties;

    private InetAddress imapServerAddress;

    private InetSocketAddress imapServerSocketAddress;

    /**
     * Default constructor
     */
    public IMAPConfig() {
        super();
    }

    @Override
    public MailCapabilities getCapabilities() {
        return imapCapabilities == null ? MailCapabilities.EMPTY_CAPS : imapCapabilities;
    }

    /**
     * Gets the IMAP capabilities
     * 
     * @return The IMAP capabilities
     */
    public IMAPCapabilities getImapCapabilities() {
        return imapCapabilities;
    }

    /**
     * Gets the imapPort
     * 
     * @return the imapPort
     */
    @Override
    public int getPort() {
        return imapPort;
    }

    @Override
    public void setPort(final int imapPort) {
        this.imapPort = imapPort;
    }

    /**
     * Gets the imapServer
     * 
     * @return the imapServer
     */
    @Override
    public String getServer() {
        return imapServer;
    }

    @Override
    public void setServer(final String imapServer) {
        this.imapServer = imapServer;
    }

    /**
     * Initializes IMAP server's capabilities if not done, yet
     * 
     * @param imapStore The IMAP store from which to fetch the capabilities
     * @throws MailConfigException If IMAP capabilities cannot be initialized
     */
    public void initializeCapabilities(final IMAPStore imapStore) throws MailConfigException {
        if (imapCapabilities == null) {
            synchronized (this) {
                if (imapCapabilities != null) {
                    return;
                }
                try {
                    final IMAPCapabilities imapCaps = new IMAPCapabilities();
                    imapCaps.setACL(imapStore.hasCapability(IMAPCapabilities.CAP_ACL));
                    imapCaps.setThreadReferences(imapStore.hasCapability(IMAPCapabilities.CAP_THREAD_REFERENCES));
                    imapCaps.setThreadOrderedSubject(imapStore.hasCapability(IMAPCapabilities.CAP_THREAD_ORDEREDSUBJECT));
                    imapCaps.setQuota(imapStore.hasCapability(IMAPCapabilities.CAP_QUOTA));
                    imapCaps.setSort(imapStore.hasCapability(IMAPCapabilities.CAP_SORT));
                    imapCaps.setIMAP4(imapStore.hasCapability(IMAPCapabilities.CAP_IMAP4));
                    imapCaps.setIMAP4rev1(imapStore.hasCapability(IMAPCapabilities.CAP_IMAP4_REV1));
                    imapCaps.setUIDPlus(imapStore.hasCapability(IMAPCapabilities.CAP_UIDPLUS));
                    imapCaps.setNamespace(imapStore.hasCapability(IMAPCapabilities.CAP_NAMESPACE));
                    imapCaps.setIdle(imapStore.hasCapability(IMAPCapabilities.CAP_IDLE));
                    imapCaps.setChildren(imapStore.hasCapability(IMAPCapabilities.CAP_CHILDREN));
                    imapCaps.setHasSubscription(!MailProperties.getInstance().isIgnoreSubscription());
                    imapCapabilities = imapCaps;
                } catch (final MessagingException e) {
                    throw new MailConfigException(e);
                }
            }
        }
    }

    /**
     * Checks if IMAP search is configured and corresponding capability is available.
     * 
     * @return <code>true</code> if IMAP search is configured and corresponding capability is available; otherwise <code>false</code>
     */
    public boolean isImapSearch() {
        final boolean imapSearch = IMAPProperties.getInstance().isImapSearch();
        if (imapCapabilities != null) {
            return (imapSearch && (imapCapabilities.hasIMAP4rev1() || imapCapabilities.hasIMAP4()));
        }
        return imapSearch;
    }

    /**
     * Checks if IMAP sort is configured and corresponding capability is available.
     * 
     * @return <code>true</code> if IMAP sort is configured and corresponding capability is available; otherwise <code>false</code>
     */
    public boolean isImapSort() {
        final boolean imapSort = IMAPProperties.getInstance().isImapSort();
        if (imapCapabilities != null) {
            return (imapSort && imapCapabilities.hasSort());
        }
        return imapSort;
    }

    @Override
    public boolean isSecure() {
        return secure;
    }

    @Override
    public void setSecure(final boolean secure) {
        this.secure = secure;
    }

    /**
     * Checks if ACLs are supported
     * 
     * @return <code>true</code> if ACLs are supported; otherwise <code>false</code>
     */
    public boolean isSupportsACLs() {
        final BoolCapVal supportsACLs = IMAPProperties.getInstance().getSupportsACLs();
        if (imapCapabilities != null && BoolCapVal.AUTO.equals(supportsACLs)) {
            return imapCapabilities.hasPermissions();
        }
        return BoolCapVal.TRUE.equals(supportsACLs) ? true : false;
    }

    @Override
    protected void parseServerURL(final String serverURL) {
        imapServer = serverURL;
        imapPort = 143;
        {
            final String[] parsed = parseProtocol(imapServer);
            if (parsed == null) {
                secure = false;
            } else {
                secure = PROTOCOL_IMAP_SECURE.equals(parsed[0]);
                imapServer = parsed[1];
            }
            final int pos = imapServer.indexOf(':');
            if (pos > -1) {
                try {
                    imapPort = Integer.parseInt(imapServer.substring(pos + 1));
                } catch (final NumberFormatException e) {
                    LOG.error("IMAP port could not be parsed to an integer value. Using fallback value 143", e);
                    imapPort = 143;
                }
                imapServer = imapServer.substring(0, pos);
            }
        }
    }

    /**
     * Gets the internet address of the IMAP server.
     * 
     * @return The internet address of the IMAP server.
     * @throws IMAPException If IMAP server cannot be resolved
     */
    public InetAddress getImapServerAddress() throws IMAPException {
        if (null == imapServerAddress) {
            try {
                imapServerAddress = InetAddress.getByName(imapServer);
            } catch (final UnknownHostException e) {
                throw new IMAPException(IMAPException.Code.IO_ERROR, e, e.getMessage());
            }
        }
        return imapServerAddress;
    }

    /**
     * Gets the socket address (internet address + port) of the IMAP server.
     * 
     * @return The socket address (internet address + port) of the IMAP server.
     * @throws IMAPException If IMAP server cannot be resolved
     */
    public InetSocketAddress getImapServerSocketAddress() throws IMAPException {
        if (null == imapServerSocketAddress) {
            imapServerSocketAddress = new InetSocketAddress(getImapServerAddress(), imapPort);
        }
        return imapServerSocketAddress;
    }

    @Override
    public IMailProperties getMailProperties() {
        return mailProperties;
    }

    public IIMAPProperties getIMAPProperties() {
        return mailProperties;
    }

    @Override
    public void setMailProperties(final IMailProperties mailProperties) {
        this.mailProperties = (IIMAPProperties) mailProperties;
    }

}
