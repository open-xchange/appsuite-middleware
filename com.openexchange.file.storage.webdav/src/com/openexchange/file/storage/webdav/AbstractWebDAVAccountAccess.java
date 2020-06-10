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

package com.openexchange.file.storage.webdav;

import static com.openexchange.java.Autoboxing.I;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.apache.http.client.utils.URIBuilder;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.CapabilityAware;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageCapability;
import com.openexchange.file.storage.FileStorageCapabilityTools;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStorageService;
import com.openexchange.file.storage.webdav.exception.WebdavExceptionCodes;
import com.openexchange.java.Strings;
import com.openexchange.session.Session;
import com.openexchange.webdav.client.WebDAVClient;

/**
 * {@link AbstractWebDAVAccountAccess}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.10.4
 */
public abstract class AbstractWebDAVAccountAccess implements CapabilityAware {

    protected final FileStorageAccount account;
    private final Session session;
    private final AbstractWebDAVFileStorageService service;

    private volatile WebDAVClient webdavClient;

    /**
     * Initializes a new {@link AbstractWebDAVAccountAccess}.
     *
     * @param service The {@link FileStorageService}
     * @param account The {@link FileStorageAccount}
     * @param session The {@link Session}
     */
    protected AbstractWebDAVAccountAccess(AbstractWebDAVFileStorageService service, FileStorageAccount account, Session session) {
        super();
        this.service = service;
        this.account = account;
        this.session = session;
    }

    @Override
    public Boolean supports(FileStorageCapability capability) {
        Boolean supportedByAbstractClass = FileStorageCapabilityTools.supportsByClass(AbstractWebDAVFileAccess.class, capability);
        return Boolean.TRUE.equals(supportedByAbstractClass) ? Boolean.TRUE : null;
    }

    /**
     * Gets the associated account
     *
     * @return The account
     */
    public FileStorageAccount getAccount() {
        return account;
    }

    public Session getSession() {
        return session;
    }

    @Override
    public void connect() throws OXException {
        webdavClient = connectInternal().orElse(connectViaBasicAuth());
    }

    /**
     * Connects to the endpoint using basic auth
     *
     * @return The {@link WebDAVClient} or null
     * @throws OXException in case the root url is invalid
     */
    private WebDAVClient connectViaBasicAuth() throws OXException {
        Map<String, Object> configuration = account.getConfiguration();
        String configUrl = (String) configuration.get("url");
        if (Strings.isEmpty(configUrl)) {
            throw FileStorageExceptionCodes.INVALID_URL.create(configUrl, "empty");
        }
        String login = (String) configuration.get("login");
        if (Strings.isEmpty(login)) {
            throw FileStorageExceptionCodes.MISSING_CONFIG.create("login", account.getId());
        }
        String password = (String) configuration.get("password");
        if (Strings.isEmpty(login)) {
            throw FileStorageExceptionCodes.MISSING_CONFIG.create("password", account.getId());
        }
        try {
            URI uri = verifyURL(new URI(configUrl));
            URIBuilder uriBuilder = new URIBuilder();
            if (null != uri.getScheme()) {
                uriBuilder.setScheme(uri.getScheme());
            }
            if (null != uri.getHost()) {
                uriBuilder.setHost(uri.getHost());
            }
            if (0 < uri.getPort()) {
                uriBuilder.setPort(uri.getPort());
            }
            URI baseUrl = uriBuilder.build();
            return service.getClientFactory().create(getSession(), getAccountId(), baseUrl, login, password, optHttpClientId());
        } catch (URISyntaxException e) {
            throw FileStorageExceptionCodes.INVALID_URL.create(configUrl, e.getMessage());
        }
    }

    /**
     * Verifies that the given URI is allowed to be used by the session
     *
     * @param uri The URI to verify
     * @return The given URI if allowed to be used
     * @throws OXException if the given URI is not allowed to be used
     */
    protected URI verifyURL(URI uri) throws OXException {
        if (isBlacklisted(session, uri)) {
            throw WebdavExceptionCodes.PING_FAILED.create();
        }

        if (!verifyPort(session, uri)) {
            throw WebdavExceptionCodes.PING_FAILED.create();
        }
        return uri;
    }

    /**
     * Checks if the host specified in given URL is blacklisted for a session
     *
     * @param session The session to check
     * @param uri The URI to check
     * @return <code>true</code>, if the uri's host is blacklisted, <code>false</code> otherwise
     * @throws OXException
     */
    protected boolean isBlacklisted(Session session, URI uri) throws OXException {
        if (uri != null) {
            return isBlacklisted(session, uri.getHost());
        }
        return false;
    }

    /**
     * Checks if the given host is blacklisted
     *
     * @param session The session to get the black listed hosts for
     * @param host The host to check
     * @return <code>true</code> , if the given host is blacklisted, <code>false</code> otherwise
     * @throws OXException
     */
    protected boolean isBlacklisted(Session session, String host) throws OXException {
        return service.getBlackListedHosts(session).contains(host);
    }

    /**
     * Verifies that the port of the given URI is allowed
     *
     * @param session The session
     * @param uri The URI to verify
     * @return <code>true</code> if the URI's port is allowed <code>false</code> otherwise
     * @throws OXException
     */
    protected boolean verifyPort(Session session, URI uri) throws OXException {
        if (uri != null) {
            return isAllowed(session, uri.getPort());
        }
        return false;
    }

    /**
     * Checks if the given port is allowed
     *
     * @param session The session to check
     * @param port The port to check
     * @return <code>true</code> if the given port is allowed <code>false</code> otherwise
     * @throws OXException
     */
    protected boolean isAllowed(Session session, int port) throws OXException {
        if (port < 0) {
            // port not set; always allow
            return true;
        }

        if (port > 65535) {
            // invalid port
            return false;
        }
        Optional<Set<Integer>> optAllowedPorts = service.getAllowedPorts(session);
        return optAllowedPorts.isPresent() ? optAllowedPorts.get().contains(I(port)) : true;
    }

    /**
     * Provides an {@link WebDAVClient}.
     *
     * @return The {@link WebDAVClient} or {@link Optional#empty()} if not applicable
     * @throws OXException In case of an error while connecting
     */
    @SuppressWarnings("unused")
    protected Optional<WebDAVClient> connectInternal() throws OXException {
        return Optional.empty();
    }

    @Override
    public boolean isConnected() {
        return null != webdavClient;
    }

    @Override
    public void close() {
        webdavClient = null;
    }

    @Override
    public boolean ping() throws OXException {
        try {
            connect();
            WebDAVFolder rootFolder = getFolderAccess().getRootFolder();
            getFolderAccess().getFolder(rootFolder.getId());
            return true;
        }
        finally {
            close();
        }
    }

    @Override
    public boolean cacheable() {
        return false;
    }

    @Override
    public String getAccountId() {
        return account.getId();
    }

    @Override
    public AbstractWebDAVFileAccess getFileAccess() throws OXException {
        if (null == webdavClient) {
            throw FileStorageExceptionCodes.NOT_CONNECTED.create();
        }
        return initWebDAVFileAccess(webdavClient);
    }

    /**
     * Initializes the WebDAV file access.
     *
     * @param webdavClient The {@link WebDAVClient}
     * @return The WebDAV file access
     * @throws OXException If WebDAV file access cannot be initialized
     */
    protected abstract AbstractWebDAVFileAccess initWebDAVFileAccess(WebDAVClient webdavClient) throws OXException;

    @Override
    public AbstractWebDAVFolderAccess getFolderAccess() throws OXException {
        if (null == webdavClient) {
            throw FileStorageExceptionCodes.NOT_CONNECTED.create();
        }
        return initWebDAVFolderAccess(webdavClient);
    }

    /**
     * Initializes the WebDAV folder access.
     *
     * @param webdavClient The {@link WebDAVClient}
     * @return The WebDAV folder access
     * @throws OXException If WebDAV folder access cannot be initialized
     */
    protected abstract AbstractWebDAVFolderAccess initWebDAVFolderAccess(WebDAVClient webdavClient) throws OXException;

    @Override
    public FileStorageFolder getRootFolder() throws OXException {
        connect();
        return getFolderAccess().getRootFolder();
    }

    @Override
    public FileStorageService getService() {
        return service;
    }

    /**
     * Gets the optional client identifier for the http client.
     * If none is provided, then the default "webdav" is used instead.
     *
     * @return The optional http client identifier
     */
    protected Optional<String> optHttpClientId() {
        return Optional.empty();
    }
}
