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

package com.openexchange.file.storage.cmis;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.exceptions.CmisBaseException;
import org.apache.chemistry.opencmis.commons.spi.CmisBinding;
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
 * {@link CMISAccountAccess}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CMISAccountAccess implements FileStorageAccountAccess {

    private static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(CMISAccountAccess.class));

    /*-
     * Member stuff
     */

    private final FileStorageAccount account;

    private final Session session;

    private final String username;

    private final String password;

    private final String repository;

    private final AtomicBoolean connected;

    private volatile String rootUrl;

    private volatile org.apache.chemistry.opencmis.client.api.Session cmisSession;

    private volatile FileStorageFolderAccess folderAccess;

    private volatile FileStorageFileAccess fileAccess;

    private final FileStorageService service;

    /**
     * Initializes a new {@link CMISAccountAccess}.
     */
    public CMISAccountAccess(final FileStorageService service, final FileStorageAccount account, final Session session) {
        super();
        connected = new AtomicBoolean();
        this.account = account;
        this.session = session;
        final Map<String, Object> configuration = account.getConfiguration();
        repository = (String) configuration.get(CMISConstants.CMIS_REPOSITORY);
        username = (String) configuration.get(CMISConstants.CMIS_LOGIN);
        password = (String) configuration.get(CMISConstants.CMIS_PASSWORD);
        this.service = service;
    }

    /**
     * Gets the name of the CMIS user.
     *
     * @return The name of the CMIS user
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
            String url = (String) configuration.get(CMISConstants.CMIS_URL);
            if (null == url) {
                throw FileStorageExceptionCodes.MISSING_PARAMETER.create(CMISConstants.CMIS_URL);
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
            /*-
             * Create CMIS session
             * 
             * Default factory implementation
             */
            final SessionFactory factory = SessionFactoryImpl.newInstance();
            final Map<String, String> parameters = new HashMap<String, String>(6);
            /*
             * User credentials
             */
            parameters.put(SessionParameter.USER, username);
            parameters.put(SessionParameter.PASSWORD, password);
            /*
             * Connection settings
             */
            parameters.put(SessionParameter.ATOMPUB_URL, rootUrl);
            parameters.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());
            parameters.put(SessionParameter.REPOSITORY_ID, repository);
            /*
             * Create session
             */
            cmisSession = factory.createSession(parameters);
        } else {
            LOG.debug("CMIS account access already connected.");
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
            final org.apache.chemistry.opencmis.client.api.Session cmisSession = this.cmisSession;
            if (null != cmisSession) {
                final CmisBinding binding = cmisSession.getBinding();
                if (null != binding) {
                    binding.clearAllCaches();
                    binding.close();
                }
                cmisSession.clear();
                this.cmisSession = null;
            }
            fileAccess = null;
            folderAccess = null;
        } else {
            LOG.debug("CMIS account access already closed.");
        }
    }

    @Override
    public boolean ping() throws OXException {
        final Map<String, Object> configuration = account.getConfiguration();
        String url = (String) configuration.get(CMISConstants.CMIS_URL);
        if (null == url) {
            throw FileStorageExceptionCodes.MISSING_PARAMETER.create(CMISConstants.CMIS_URL);
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
            /*
             * Add username/password to URL
             */
            rootUrl = url;
            /*-
             * Create CMIS session
             * 
             * Default factory implementation
             */
            final SessionFactory factory = SessionFactoryImpl.newInstance();
            final Map<String, String> parameters = new HashMap<String, String>(6);
            // user credentials
            parameters.put(SessionParameter.USER, username);
            parameters.put(SessionParameter.PASSWORD, password);
            // connection settings
            parameters.put(SessionParameter.ATOMPUB_URL, rootUrl);
            parameters.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());
            parameters.put(SessionParameter.REPOSITORY_ID, repository);
            // create session
            final org.apache.chemistry.opencmis.client.api.Session cmisSession = factory.createSession(parameters);
            cmisSession.getRootFolder().getChildren();
            return true;
        } catch (final CmisBaseException e) {
            throw CMISExceptionCodes.CMIS_ERROR.create(e, e.getMessage());
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
                    folderAccess = tmp = new CMISFolderAccess(rootUrl, cmisSession, account, session);
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
                    fileAccess = tmp = new CMISFileAccess(rootUrl, cmisSession, account, session, this);
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
