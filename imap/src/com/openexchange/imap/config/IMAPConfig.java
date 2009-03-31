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
import com.openexchange.mail.api.MailCapabilities;
import com.openexchange.mail.api.MailConfig;
import com.openexchange.mail.config.MailConfigException;
import com.openexchange.mail.config.MailProperties;
import com.sun.mail.imap.IMAPStore;

/**
 * {@link IMAPConfig}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class IMAPConfig extends MailConfig {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(IMAPConfig.class);

    private static final String PROTOCOL_IMAP_SECURE = "imaps";

    /**
     * Gets the block size in which large IMAP commands' UIDs/sequence numbers arguments get splitted.
     * 
     * @return The block size
     */
    public static int getBlockSize() {
        return IMAPProperties.getInstance().getBlockSize();
    }

    /**
     * Gets the spam handler name
     * 
     * @return The spam handler name
     */
    public static String getSpamHandlerName() {
        return IMAPProperties.getInstance().getSpamHandlerName();
    }

    /**
     * Gets the imapAuthEnc
     * 
     * @return the imapAuthEnc
     */
    public static String getImapAuthEnc() {
        return IMAPProperties.getInstance().getImapAuthEnc();
    }

    /**
     * Gets the imapConnectionIdleTime
     * 
     * @return the imapConnectionIdleTime
     */
    public static int getImapConnectionIdleTime() {
        return IMAPProperties.getInstance().getImapConnectionIdleTime();
    }

    /**
     * Gets the imapConnectionTimeout
     * 
     * @return the imapConnectionTimeout
     */
    public static int getImapConnectionTimeout() {
        return IMAPProperties.getInstance().getImapConnectionTimeout();
    }

    /**
     * Gets the imapTemporaryDown
     * 
     * @return the imapTemporaryDown
     */
    public static int getImapTemporaryDown() {
        return IMAPProperties.getInstance().getImapTemporaryDown();
    }

    /**
     * Gets the imapTimeout
     * 
     * @return the imapTimeout
     */
    public static int getImapTimeout() {
        return IMAPProperties.getInstance().getImapTimeout();
    }

    /**
     * Gets the entity2acl implementation's canonical class name
     * 
     * @return The entity2acl implementation's canonical class name
     */
    public static String getEntity2AclImpl() {
        return IMAPProperties.getInstance().getEntity2AclImpl();
    }

    /**
     * Checks if given IMAP server implements newer ACL extension conforming to RFC 4314
     * 
     * @param imapServer The IMAP server's host name or IP address
     * @return <code>true</code> if newer ACL extension is supported by IMAP server; otherwise <code>false</code>
     */
    public static boolean hasNewACLExt(final String imapServer) {
        if (IMAPProperties.getInstance().getNewACLExtMap().containsKey(imapServer)) {
            return IMAPProperties.getInstance().getNewACLExtMap().get(imapServer).booleanValue();
        }
        return false;
    }

    /**
     * Gets the fastFetch
     * 
     * @return the fastFetch
     */
    public static boolean isFastFetch() {
        return IMAPProperties.getInstance().isFastFetch();
    }

    /**
     * Checks if mbox format is enabled
     * 
     * @return <code>true</code> if mbox format is enabled; otherwise <code>false</code>
     */
    public static boolean isMBoxEnabled() {
        return IMAPProperties.getInstance().isMBoxEnabled();
    }

    /**
     * Gets the global supportsACLs
     * 
     * @return the global supportsACLs
     */
    public static BoolCapVal isSupportsACLsConfig() {
        return IMAPProperties.getInstance().getSupportsACLs();
    }

    /**
     * Remembers if given IMAP server supports newer ACL extension conforming to RFC 4314
     * 
     * @param imapServer The IMAP server's host name or IP address
     * @param newACLExt Whether newer ACL extension is supported or not
     */
    public static void setNewACLExt(final String imapServer, final boolean newACLExt) {
        IMAPProperties.getInstance().getNewACLExtMap().put(imapServer, Boolean.valueOf(newACLExt));
    }

    private volatile IMAPCapabilities imapCapabilities;

    private int imapPort;

    private String imapServer;

    private boolean secure;

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
}
