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
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.chemistry.opencmis.client.api.Repository;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.bindings.CmisBindingFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.spi.CmisBinding;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageAccountAccess;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStorageFolderAccess;
import com.openexchange.file.storage.FileStorageService;
import com.openexchange.file.storage.cmis.http.CMISFileStorageHttpInvoker;
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

    private final String binding;

    private final String authType;

    private final AtomicBoolean connected;

    private final int readTimeout;

    private volatile String rootUrl;

    private volatile org.apache.chemistry.opencmis.client.api.Session cmisSession;

    private volatile FileStorageFolderAccess folderAccess;

    private volatile FileStorageFileAccess fileAccess;

    private final FileStorageService service;

    private final Map<String, Object> configuration;

    /**
     * Initializes a new {@link CMISAccountAccess}.
     */
    public CMISAccountAccess(final FileStorageService service, final FileStorageAccount account, final Session session) {
        super();
        connected = new AtomicBoolean();
        this.account = account;
        this.session = session;
        final Map<String, Object> configuration = account.getConfiguration();
        this.configuration = configuration;
        String tmp = (String) configuration.get(CMISConstants.CMIS_REPOSITORY);
        this.repository = tmp == null ? "" : tmp;
        tmp = (String) configuration.get(CMISConstants.CMIS_BINDING);
        this.binding = null == tmp ? "atompub" : tmp;
        username = (String) configuration.get(CMISConstants.CMIS_LOGIN);
        password = (String) configuration.get(CMISConstants.CMIS_PASSWORD);
        tmp = (String) configuration.get(CMISConstants.CMIS_AUTH_TYPE);
        authType = tmp == null ? "basic" : tmp.trim();
        readTimeout = parseInt(configuration.get(CMISConstants.CMIS_TIMEOUT), -1);
        this.service = service;
    }

    private boolean useAtomPub() {
        return "atompub".equalsIgnoreCase(binding);
    }

    private boolean useWebService() {
        return "webservice".equalsIgnoreCase(binding);
    }

    private boolean useHttpBasic() {
        return "basic".equalsIgnoreCase(authType);
    }

    private boolean useSoapUsernameToken() {
        return "soap".equalsIgnoreCase(authType);
    }

    private boolean useNtlm() {
        return "ntlm".equalsIgnoreCase(authType);
    }

    private static int parseInt(final Object value, final int defaultValue) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value instanceof String) {
            return Integer.parseInt(((String) value).trim());
        }
        return defaultValue;
    }

    private static boolean isTrue(final Object value) {
        if (value instanceof Boolean) {
            return ((Boolean) value).booleanValue();
        }
        if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        }
        return false;
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
            try {
                final Map<String, Object> configuration = account.getConfiguration();
                String url = (String) configuration.get(CMISConstants.CMIS_URL);
                if (null == url) {
                    throw FileStorageExceptionCodes.MISSING_PARAMETER.create(CMISConstants.CMIS_URL);
                }
                url = url.trim();
                /*
                 * Add username/password to URL
                 */
                rootUrl = url;
                /*-
                 * Create CMIS session
                 *
                 * Default factory implementation
                 */
                final Map<String, String> parameters = new HashMap<String, String>(6);
                /*
                 * User credentials
                 */
                parameters.put(SessionParameter.USER, username);
                parameters.put(SessionParameter.PASSWORD, password);
                /*
                 * Connection settings
                 */
                if (useWebService()) {
                    parameters.put(SessionParameter.BINDING_TYPE, BindingType.WEBSERVICES.value());
                    parameters.put(SessionParameter.WEBSERVICES_ACL_SERVICE, url);
                    parameters.put(SessionParameter.WEBSERVICES_DISCOVERY_SERVICE, url);
                    parameters.put(SessionParameter.WEBSERVICES_MULTIFILING_SERVICE, url);
                    parameters.put(SessionParameter.WEBSERVICES_NAVIGATION_SERVICE, url);
                    parameters.put(SessionParameter.WEBSERVICES_OBJECT_SERVICE, url);
                    parameters.put(SessionParameter.WEBSERVICES_POLICY_SERVICE, url);
                    parameters.put(SessionParameter.WEBSERVICES_RELATIONSHIP_SERVICE, url);
                    parameters.put(SessionParameter.WEBSERVICES_REPOSITORY_SERVICE, url);
                    parameters.put(SessionParameter.WEBSERVICES_VERSIONING_SERVICE, url);
                } else if (useAtomPub()) {
                    parameters.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());
                    parameters.put(SessionParameter.ATOMPUB_URL, url);
                    /*-
                     * If NTLM is enabled on SharePoint, you have to activate the OpenCMIS NTLM authentication provider.
                     *
                     * See: http://chemistry.apache.org/java/developing/dev-repository-specific-notes.html
                     * See: http://stackoverflow.com/questions/1168526/authenticate-with-ntlm-or-kerberos-using-java-urlconnection
                     * See: http://www.intersult.com/wiki/page/Intersult%20HTTP
                     * See: http://technet.microsoft.com/en-us/library/ff934619.aspx
                     *
                     * Something like:
                     * org.apache.chemistry.opencmis.user=xyz
                     * org.apache.chemistry.opencmis.password=xyz
                     * org.apache.chemistry.opencmis.binding.spi.type=atompub
                     * org.apache.chemistry.opencmis.binding.atompub.url=http://spserver/_vti_bin/cmis/rest/60dae9c3-b9b0-4cc7-90e4-3af5b6ff25f6?getrepositoryinfo
                     * org.apache.chemistry.opencmis.session.repository.id=60dae9c3-b9b0-4cc7-90e4-3af5b6ff25f6
                     *
                     * http://spserver/_vti_bin/CMISSoapwsdl.aspx
                     */
                }
                /*
                 * Check auth type
                 */
                if (useNtlm()) {
                    /*
                     * Http invoker
                     */
                    parameters.put("org.apache.chemistry.opencmis.binding.auth.ntlm", "true");
                    parameters.put("org.apache.chemistry.opencmis.binding.HttpInvoker", CMISFileStorageHttpInvoker.class.getName());
                } else {
                    parameters.put(SessionParameter.AUTHENTICATION_PROVIDER_CLASS, CmisBindingFactory.STANDARD_AUTHENTICATION_PROVIDER);
                    parameters.put(SessionParameter.AUTH_HTTP_BASIC, useHttpBasic() ? "true" : "false");
                    parameters.put(SessionParameter.AUTH_SOAP_USERNAMETOKEN, useSoapUsernameToken() ? "true" : "false");
                    parameters.put(SessionParameter.COOKIES, "true");
                }
                // parameters.put("org.apache.chemistry.opencmis.binding.clienttransferencoding", "base64");
                /*
                 * Timeout parameters
                 */
                parameters.put(SessionParameter.CONNECT_TIMEOUT, "10000");
                parameters.put(SessionParameter.READ_TIMEOUT, Integer.toString(readTimeout));
                /*-
                 * Check if repository is specified
                 *
                 * Either create session by listed repositories or by directly referencing repository
                 */
                final SessionFactory factory = SessionFactoryImpl.newInstance();
                final String repository = this.repository;
                if (isEmpty(repository)) {
                    /*
                     * Get available repositories
                     */
                    final List<Repository> repositories = factory.getRepositories(parameters);
                    /*
                     * Create session
                     */
                    cmisSession = repositories.get(0).createSession();
                } else {
                    parameters.put(SessionParameter.REPOSITORY_ID, repository);
                    /*
                     * Create session
                     */
                    cmisSession = factory.createSession(parameters);
                }
            } catch (final RuntimeException e) {
                throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
            }
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
            try {
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
            } catch (final Exception e) {
                LOG.debug("CMIS account access could not be successfully closed.", e);
            }
        } else {
            LOG.debug("CMIS account access already closed.");
        }
    }

    @Override
    public boolean ping() throws OXException {
        try {
            connect();
            close();
            return true;
        } catch (final Exception e) {
            return false;
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
                    folderAccess = tmp = new CMISFolderAccess(rootUrl, cmisSession, account, session, this);
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

    private static boolean isEmpty(final String string) {
        if (null == string) {
            return true;
        }
        final int len = string.length();
        boolean isWhitespace = true;
        for (int i = 0; isWhitespace && i < len; i++) {
            isWhitespace = com.openexchange.java.Strings.isWhitespace(string.charAt(i));
        }
        return isWhitespace;
    }

}
