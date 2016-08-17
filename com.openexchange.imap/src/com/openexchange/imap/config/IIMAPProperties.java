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


package com.openexchange.imap.config;

import java.util.Map;
import java.util.Set;
import com.openexchange.mail.api.IMailProperties;
import com.openexchange.mail.api.MailConfig.BoolCapVal;

/**
 * {@link IIMAPProperties} - Properties for IMAP.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface IIMAPProperties extends IMailProperties {

    /**
     * Whether client's IP address should be propagated by a NOOP command.
     *
     * @return <code>true</code> if client's IP address should be propagated by a NOOP command; otherwise <code>false</code>
     */
    public boolean isPropagateClientIPAddress();

    /**
     * Whether to use the STARTTLS command (if supported by the server) to switch the connection to a TLS-protected connection
     *
     * @return <code>true</code> to issue STARTTLS command; otherwise <code>false</code>
     */
    public boolean isEnableTls();
    
    /**
     * Whether to audit log is enabled or not to trace issued IMAP commands.
     *
     * @return <code>true</code> if audit log is enabled; otherwise <code>false</code>
     */
    public boolean isAuditLogEnabled();

    /**
     * Gets the host names to propagate to.
     *
     * @return The host names to propagate to
     */
    public Set<String> getPropagateHostNames();

    /**
     * Checks if fast <code>FETCH</code> is enabled.
     *
     * @return <code>true</code> if fast <code>FETCH</code> is enabled; otherwise <code>false</code>
     */
    public boolean isFastFetch();

    /**
     * Gets the IMAP authentication encoding.
     *
     * @return The IMAP authentication encoding
     */
    public String getImapAuthEnc();

    /**
     * Gets the IMAP connection timeout.
     *
     * @return The IMAP connection timeout
     */
    public int getImapConnectionTimeout();

    /**
     * Gets the IMAP temporary down.
     *
     * @return The IMAP temporary down
     */
    public int getImapTemporaryDown();

    /**
     * Checks if IMAP search is enabled.
     *
     * @return <code>true</code> if IMAP search is enabled; otherwise <code>false</code>
     */
    public boolean isImapSearch();

    /**
     * Checks if IMAP search is enabled and should be forced regardless of the mail fetch limit.
     *
     * @return <code>true</code> if IMAP search should be forced; otherwise <code>false</code>
     */
    public boolean forceImapSearch();

    /**
     * Checks if IMAP sort is enabled.
     *
     * @return <code>true</code> if IMAP sort is enabled; otherwise <code>false</code>
     */
    public boolean isImapSort();

    /**
     * Whether to notify about recent messages.
     *
     * @return <code>true</code> to notify about recent messages; otherwise <code>false</code>
     */
    public boolean notifyRecent();

    /**
     * Gets the frequency (in seconds) when to check for recent mails.
     *
     * @return The frequency (in seconds)
     */
    public int getNotifyFrequencySeconds();

    /**
     * Gets the comma-separated full names of the folders to check for recent mails.
     *
     * @return The comma-separated full names
     */
    public String getNotifyFullNames();

    /**
     * Gets the IMAP timeout.
     *
     * @return The IMAP timeout
     */
    public int getImapTimeout();

    /**
     * Indicates support for ACLs.
     *
     * @return The support for ACLs
     */
    public BoolCapVal getSupportsACLs();

    /**
     * Gets the block size in which large IMAP commands' UIDs/sequence numbers arguments get splitted.
     *
     * @return The block size
     */
    public int getBlockSize();

    /**
     * Gets the max. number of connections
     *
     * @return The max. number of connections
     */
    public int getMaxNumConnection();

    /**
     * Whether to allow folder caches.
     *
     * @return <code>true</code> if folder caches are allowed; otherwise <code>false</code>
     */
    public boolean allowFolderCaches();

    /**
     * Checks whether it is allowed to FETCH single headers
     *
     * @return <code>true</code> if allowed; otherwise <code>false</code>
     */
    public boolean allowFetchSingleHeaders();

    /**
     * Gets supported SSL protocols
     *
     * @return Supported SSL protocols
     */
    public String getSSLProtocols();

    /**
     * Gets the SSL cipher suites that will be enabled for SSL connections. The property value is a whitespace separated list of tokens
     * acceptable to the <code>javax.net.ssl.SSLSocket.setEnabledProtocols</code> method.
     *
     * @return The SSL cipher suites
     */
    public String getSSLCipherSuites();

    /**
     * Gets the map holding IMAP servers with new ACL Extension.
     *
     * @return The map holding IMAP servers with new ACL Extension
     * @deprecated Should be unnecessary due to new ACL extension detection
     */
    @Deprecated
    public Map<String, Boolean> getNewACLExtMap();

}
