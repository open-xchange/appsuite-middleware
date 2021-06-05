/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
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
     * Whether audit log is enabled or not to trace issued IMAP commands.
     *
     * @return <code>true</code> if audit log is enabled; otherwise <code>false</code>
     */
    public boolean isAuditLogEnabled();

    /**
     * Whether debug (traffic) log is enabled or not to trace IMAP communication.
     *
     * @return <code>true</code> if debug log is enabled; otherwise <code>false</code>
     */
    public boolean isDebugLogEnabled();

    /**
     * Whether the pre-login capabilities are supposed to be overwritten (completely replaced with the ones advertised after login)
     *
     * @return <code>true</code> to overwrite; otherwise <code>false</code> to extend
     */
    public boolean isOverwritePreLoginCapabilities();

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
     * Gets the timeout for failed authentication attempts.
     *
     * @return The timeout for failed authentication attempts
     */
    public int getImapFailedAuthTimeout();

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
     * Checks if attachment marker is enabled.
     *
     * @return <code>true</code> if attachment marker is enabled for the underlying IMAP; otherwise <code>false</code>
     */
    public boolean isAttachmentMarkerEnabled();

    /**
     * Gets the map holding IMAP servers with new ACL Extension.
     *
     * @return The map holding IMAP servers with new ACL Extension
     * @deprecated Should be unnecessary due to new ACL extension detection
     */
    @Deprecated
    public Map<String, Boolean> getNewACLExtMap();

}
