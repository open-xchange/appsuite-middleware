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

package com.openexchange.file.storage.xctx;

import com.openexchange.capabilities.CapabilityService;
import com.openexchange.capabilities.CapabilitySet;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.CapabilityAware;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageAccountAccess;
import com.openexchange.file.storage.FileStorageCapability;
import com.openexchange.file.storage.FileStorageCapabilityTools;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStorageFolderAccess;
import com.openexchange.file.storage.FileStorageService;
import com.openexchange.java.Strings;
import com.openexchange.osgi.ShutDownRuntimeException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.share.ShareExceptionCodes;
import com.openexchange.share.core.tools.ShareLinks;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * {@link XctxAccountAccess}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since 7.10.5
 */
public class XctxAccountAccess implements FileStorageAccountAccess, CapabilityAware {

    private final FileStorageAccount account;
    private final ServerSession session;
    private final ServiceLookup services;
    private final XctxSessionCache sessionCache;

    private ServerSession guestSession;

    /**
     * Initializes a new {@link XctxAccountAccess}.
     *
     * @param services A service lookup reference
     * @param account The account
     * @param session The user's session
     * @param sessionCache The guest session cache to use
     */
    protected XctxAccountAccess(ServiceLookup services, FileStorageAccount account, Session session, XctxSessionCache sessionCache) throws OXException {
        super();
        this.services = services;
        this.account = account;
        this.session = ServerSessionAdapter.valueOf(session);
        this.sessionCache = sessionCache;
    }
    /**
     * Gets the service of specified type. Throws error if service is absent.
     *
     * @param clazz The service's class
     * @return The service instance
     * @throws ShutDownRuntimeException If system is currently shutting down
     * @throws OXException In case of missing service
     */
    public <S extends Object> S getServiceSafe(Class<? extends S> clazz) throws OXException {
        return services.getServiceSafe(clazz);
    }

    @Override
    public Boolean supports(FileStorageCapability capability) {
        if (FileStorageCapability.RESTORE.equals(capability)) {
            return Boolean.FALSE;
        }
        return FileStorageCapabilityTools.supportsByClass(XctxFileAccess.class, capability);
    }

    @Override
    public void connect() throws OXException {
        if (false == hasCapability(session)) {
            throw ShareExceptionCodes.NO_SUBSCRIBE_SHARE_PERMISSION.create();
        }
        String shareUrl = (String) account.getConfiguration().get("url");
        if (Strings.isEmpty(shareUrl)) {
            throw FileStorageExceptionCodes.MISSING_CONFIG.create("url", account.getId());
        }
        String password = (String) account.getConfiguration().get("password");
        String baseToken = ShareLinks.extractBaseToken(shareUrl);
        if (null == baseToken) {
            throw ShareExceptionCodes.INVALID_LINK.create(shareUrl);
        }
        this.guestSession = ServerSessionAdapter.valueOf(sessionCache.getGuestSession(session, baseToken, password));
    }

    @Override
    public boolean isConnected() {
        return null != guestSession;
    }

    @Override
    public void close() {
        this.guestSession = null; // guest session still kept in cache
    }

    @Override
    public boolean ping() throws OXException {
        return true;
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
    public FileStorageFileAccess getFileAccess() throws OXException {
        if (false == isConnected()) {
            throw FileStorageExceptionCodes.NOT_CONNECTED.create();
        }
        return new XctxFileAccess(this, session, guestSession);
    }

    @Override
    public FileStorageFolderAccess getFolderAccess() throws OXException {
        if (false == isConnected()) {
            throw FileStorageExceptionCodes.NOT_CONNECTED.create();
        }
        return new XctxFolderAccess(this, session, guestSession);
    }

    @Override
    public FileStorageFolder getRootFolder() throws OXException {
        throw FileStorageExceptionCodes.NO_SUCH_FOLDER.create();
    }

    @Override
    public FileStorageService getService() {
        return account.getFileStorageService();
    }

    private boolean hasCapability(Session session) throws OXException {
        CapabilitySet capabilities = services.getServiceSafe(CapabilityService.class).getCapabilities(session);
        return capabilities.contains("filestorage_xctx");
    }

}
