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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.file.storage.cifs;

import static java.text.MessageFormat.format;
import java.io.IOException;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.WarningsAware;
import com.openexchange.file.storage.cifs.cache.SmbFileMap;
import com.openexchange.file.storage.cifs.cache.SmbFileMapManagement;
import com.openexchange.session.Session;

/**
 * {@link AbstractCIFSAccess}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AbstractCIFSAccess {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractCIFSAccess.class);

    /**
     * The string constant for <code>'/'</code> character.
     */
    protected static final String SLASH = "/";

    /**
     * The root URL of CIFS/SMB server as specified through account configuration.
     */
    protected final String rootUrl;

    /**
     * The credentials the client should use for authentication.
     */
    protected final NtlmPasswordAuthentication auth;

    /**
     * The session.
     */
    protected final Session session;

    /**
     * The associated file account.
     */
    protected final FileStorageAccount account;

    /**
     * The warnings-aware reference.
     */
    protected final WarningsAware warningsAware;

    /**
     * Initializes a new {@link AbstractCIFSAccess}.
     */
    protected AbstractCIFSAccess(final String rootUrl, final NtlmPasswordAuthentication auth, final FileStorageAccount account, final Session session, final WarningsAware warningsAware) {
        super();
        this.warningsAware = warningsAware;
        this.rootUrl = rootUrl;
        this.account = account;
        this.session = session;
        this.auth = auth;
    }

    /**
     * Gets the root URL.
     *
     * @return The root URL
     */
    public String getRootUrl() {
        return rootUrl;
    }

    /**
     * Gets the associated SMB file.
     *
     * @param path The path
     * @return The SMB file; either newly created or fetched from cache
     * @throws IOException If an I/O error occurs in case of newly created SMB file
     */
    protected SmbFile getSmbFile(final String path) throws IOException {
        // Check in map
        final SmbFileMap smbFileMap = SmbFileMapManagement.getInstance().getFor(session);
        SmbFile smbFile = smbFileMap.get(path);
        if (null == smbFile) {
            // The associated SMB file
            final SmbFile newSmbFolder = new SmbFile(path, auth);
            smbFile = smbFileMap.putIfAbsent(newSmbFolder);
            if (null == smbFile) {
                smbFile = newSmbFolder;
            }
        }
        return smbFile;
    }

    /**
     * Checks for existence of specified SMB file.
     *
     * @param smbFolder The CIFS/SMB file
     * @return <code>true</code> if existent; otherwise <code>false</code>
     * @throws SmbException If a CIFS/SMB exception occurs
     */
    protected boolean exists(final SmbFile smbFolder) throws SmbException {
        try {
            if (!smbFolder.exists()) {
                return false;
            }
            return !smbFolder.getName().endsWith("$/");
        } catch (final SmbException e) {
            final int status = e.getNtStatus();
            if (SmbException.NT_STATUS_BAD_NETWORK_NAME == status || SmbException.NT_STATUS_ACCESS_DENIED == status) {
                // This means that the named share was not found.
                warningsAware.addWarning(CIFSExceptionCodes.forSmbException(e));
                return false;
            }
            throw e;
        }
    }

    /**
     * Checks if specified <code>SmbException</code> indicates that associated resource is not readable.
     *
     * @param e The SMB exception to examine
     * @return <code>true</code> if <code>SmbException</code> indicates that associated resource is not readable; otherwise <code>false</code>
     */
    protected boolean indicatesNotReadable(final SmbException e) {
        final int status = e.getNtStatus();
        if (SmbException.NT_STATUS_ACCESS_DENIED == status) {
            return true;
        }
        final String message = e.getMessage();
        if (message.startsWith("Invalid operation") || "Access is denied.".equals(message) || "Failed to connect to server".equals(message)) {
            return true;
        } else if (message.startsWith("0x")) {
            // Unspecified error occurred
            LOG.warn(format("Unspecified error received from CIFS/SMB server \"{0}\" for login {1}: {2}", rootUrl, auth.getUsername() ,message), e);
            return true;
        }
        return false;
    }

}
