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

package com.openexchange.file.storage.cifs;

import java.net.MalformedURLException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageAccountAccess;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStorageFolderAccess;
import com.openexchange.file.storage.FileStorageService;
import com.openexchange.session.Session;

/**
 * {@link CIFSAccountAccess}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CIFSAccountAccess implements FileStorageAccountAccess {

    private static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(CIFSAccountAccess.class));

    /*-
     * Member stuff
     */

    private final FileStorageAccount account;

    private final Session session;

    private final String username;

    private final String password;

    private final String domain;

    private final AtomicBoolean connected;

    private volatile String rootUrl;

    private volatile NtlmPasswordAuthentication auth;

    private volatile FileStorageFolderAccess folderAccess;

    private volatile FileStorageFileAccess fileAccess;

    private final FileStorageService service;

    /**
     * Initializes a new {@link CIFSAccountAccess}.
     */
    public CIFSAccountAccess(final FileStorageService service, final FileStorageAccount account, final Session session) {
        super();
        connected = new AtomicBoolean();
        this.account = account;
        this.session = session;
        final Map<String, Object> configuration = account.getConfiguration();
        domain = (String) configuration.get(CIFSConstants.CIFS_DOMAIN);
        username = (String) configuration.get(CIFSConstants.CIFS_LOGIN);
        password = (String) configuration.get(CIFSConstants.CIFS_PASSWORD);
        this.service = service;
    }

    /**
     * Gets the name of the CIFS/SMB user.
     *
     * @return The name of the CIFS/SMB user
     */
    public String getUser() {
        return username;
    }

    /**
     * Gets the associated session
     *
     * @return The session
     */
    public Session getSession() {
        return session;
    }

    @Override
    public void connect() throws OXException {
        if (connected.compareAndSet(false, true)) {
            final Map<String, Object> configuration = account.getConfiguration();
            String url = (String) configuration.get(CIFSConstants.CIFS_URL);
            if (null == url) {
                throw FileStorageExceptionCodes.MISSING_PARAMETER.create(CIFSConstants.CIFS_URL);
            }
            url = url.trim();
            /*
             * Ensure ending slash character
             */
            if (!url.endsWith("/")) {
                url = url + '/';
            }
            /*
             * Add username/password to URL
             */
            rootUrl = url;
            auth = new NtlmPasswordAuthentication(domain, username, password);
        } else {
            LOG.debug("CIFS account access already connected.");
        }
    }

    @Override
    public boolean isConnected() {
        return connected.get();
    }

    @Override
    public void close() {
        if (connected.compareAndSet(true, false)) {
            rootUrl = null;
            auth = null;
            fileAccess = null;
            folderAccess = null;
        } else {
            LOG.debug("CIFS account access already closed.");
        }
    }

    @Override
    public boolean ping() throws OXException {
        final Map<String, Object> configuration = account.getConfiguration();
        String url = (String) configuration.get(CIFSConstants.CIFS_URL);
        if (null == url) {
            throw FileStorageExceptionCodes.MISSING_PARAMETER.create(CIFSConstants.CIFS_URL);
        }
        url = url.trim();
        /*
         * Ensure ending slash character
         */
        if (!url.endsWith("/")) {
            url = url + '/';
        }
        /*
         * Check
         */
        try {
            final NtlmPasswordAuthentication auth =
                new NtlmPasswordAuthentication(
                    (String) configuration.get(CIFSConstants.CIFS_DOMAIN),
                    (String) configuration.get(CIFSConstants.CIFS_LOGIN),
                    (String) configuration.get(CIFSConstants.CIFS_PASSWORD));
            return new SmbFile(url, auth).exists();
        } catch (final MalformedURLException e) {
            throw CIFSExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (final SmbException e) {
            throw CIFSExceptionCodes.SMB_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public boolean cacheable() {
        return true;
    }

    @Override
    public String getAccountId() {
        return account.getId();
    }

    @Override
    public FileStorageFolderAccess getFolderAccess() throws OXException {
        if (!connected.get()) {
            throw FileStorageExceptionCodes.NOT_CONNECTED.create();
        }
        FileStorageFolderAccess tmp = folderAccess;
        if (null == tmp) {
            synchronized (this) {
                tmp = folderAccess;
                if (null == tmp) {
                    folderAccess = tmp = new CIFSFolderAccess(rootUrl, auth, account, session);
                }
            }
        }
        return tmp;
    }

    @Override
    public FileStorageFileAccess getFileAccess() throws OXException {
        if (!connected.get()) {
            throw FileStorageExceptionCodes.NOT_CONNECTED.create();
        }
        FileStorageFileAccess tmp = fileAccess;
        if (null == tmp) {
            synchronized (this) {
                tmp = fileAccess;
                if (null == tmp) {
                    fileAccess = tmp = new CIFSFileAccess(rootUrl, auth, account, session, this);
                }
            }
        }
        return tmp;
    }

    @Override
    public FileStorageFolder getRootFolder() throws OXException {
        connect();
        return getFolderAccess().getRootFolder();
    }

    @Override
    public FileStorageService getService() {
        return service;
    }

    /*-
     * ------------------------------------------------------------------------------------------------------------------------------------
     * ----------------------------------------------------------- Helper methods ---------------------------------------------------------
     * ------------------------------------------------------------------------------------------------------------------------------------
     */

}
