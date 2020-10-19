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

import static com.openexchange.file.storage.infostore.folder.AbstractInfostoreFolderAccess.PUBLIC_INFOSTORE_FOLDER_ID;
import static com.openexchange.file.storage.infostore.folder.AbstractInfostoreFolderAccess.USER_INFOSTORE_FOLDER_ID;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;
import com.openexchange.conversion.ConversionService;
import com.openexchange.conversion.DataHandler;
import com.openexchange.conversion.datahandler.DataHandlers;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.CapabilityAware;
import com.openexchange.file.storage.ErrorStateFolderAccess;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageAccountAccess;
import com.openexchange.file.storage.FileStorageAccountErrorHandler;
import com.openexchange.file.storage.FileStorageCapability;
import com.openexchange.file.storage.FileStorageCapabilityTools;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStorageFolderAccess;
import com.openexchange.file.storage.FileStorageService;
import com.openexchange.folderstorage.FederatedSharingFolders;
import com.openexchange.groupware.notify.hostname.HostData;
import com.openexchange.java.Strings;
import com.openexchange.osgi.ShutDownRuntimeException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.share.ShareExceptionCodes;
import com.openexchange.share.core.subscription.SubscribedHelper;
import com.openexchange.share.core.tools.ShareLinks;
import com.openexchange.share.subscription.XctxHostData;
import com.openexchange.share.subscription.XctxSessionManager;
import com.openexchange.tools.arrays.Collections;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * {@link XctxAccountAccess}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since 7.10.5
 */
public class XctxAccountAccess implements FileStorageAccountAccess, CapabilityAware {

    /** The identifiers of the parent folders where adjusting the subscribed flag is supported, which mark the entry points for shared and public files */
    private static final Set<String> SUBSCRIBE_PARENT_IDS = Collections.unmodifiableSet(USER_INFOSTORE_FOLDER_ID, PUBLIC_INFOSTORE_FOLDER_ID);

    private final FileStorageAccount account;
    private final ServerSession session;
    private final ServiceLookup services;
    private final FileStorageAccountErrorHandler errorHandler;

    private ServerSession guestSession;
    private boolean isConnected;

    /**
     * Initializes a new {@link XctxAccountAccess}.
     *
     * @param services A service lookup reference
     * @param account The account
     * @param session The user's session
     * @param retryAfterError The amount of seconds after which accessing an error afflicted account should be retried.
     */
    protected XctxAccountAccess(ServiceLookup services, FileStorageAccount account, Session session, int retryAfterError) throws OXException {
        super();
        this.services = services;
        this.account = account;
        this.session = ServerSessionAdapter.valueOf(session);

        ConversionService conversionService = getServiceSafe(ConversionService.class);
        DataHandler ox2jsonDataHandler = conversionService.getDataHandler(DataHandlers.OXEXCEPTION2JSON);
        DataHandler json2oxDataHandler = conversionService.getDataHandler(DataHandlers.JSON2OXEXCEPTION);
        this.errorHandler = new FileStorageAccountErrorHandler(ox2jsonDataHandler, json2oxDataHandler, this, session, retryAfterError);
    }

    /**
     * Gets the underlying filestorage account.
     * 
     * @return The file storage account
     */
    public FileStorageAccount getAccount() {
        return account;
    }

    /**
     * Gets a {@link SubscribedHelper} suitable for the connected file storage account.
     * 
     * @return The subscribed helper
     */
    public SubscribedHelper getSubscribedHelper() {
        return new SubscribedHelper(account, SUBSCRIBE_PARENT_IDS);
    }

    /**
     * Gets the service of specified type. Throws error if service is absent.
     *
     * @param <S> The class type
     * @param clazz The service's class
     * @return The service instance
     * @throws ShutDownRuntimeException If system is currently shutting down
     * @throws OXException In case of missing service
     */
    public <S extends Object> S getServiceSafe(Class<? extends S> clazz) throws OXException {
        return services.getServiceSafe(clazz);
    }

    /**
     * Gets a {@link HostData} implementation under the perspective of the guest user.
     *
     * @return The host data for the guest
     * @throws OXException In case URL is missing or invalid
     */
    public HostData getGuestHostData() throws OXException {
        String shareUrl = (String) account.getConfiguration().get("url");
        if (Strings.isEmpty(shareUrl)) {
            throw FileStorageExceptionCodes.MISSING_CONFIG.create("url", account.getId());
        }
        URI uri;
        try {
            uri = new URI(shareUrl);
        } catch (URISyntaxException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e.getMessage(), e);
        }
        return new XctxHostData(uri, guestSession) {

            @Override
            protected DispatcherPrefixService getDispatcherPrefixService() throws OXException {
                return getServiceSafe(DispatcherPrefixService.class);
            }
        };
    }

    /**
     * Resets the last known, recent, error for this account
     *
     * @throws OXException
     */
    public void resetRecentError() throws OXException {
        errorHandler.removeRecentException();
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
        String shareUrl = (String) account.getConfiguration().get("url");
        if (Strings.isEmpty(shareUrl)) {
            throw FileStorageExceptionCodes.MISSING_CONFIG.create("url", account.getId());
        }
        String password = (String) account.getConfiguration().get("password");
        String baseToken = ShareLinks.extractBaseToken(shareUrl);
        if (null == baseToken) {
            throw ShareExceptionCodes.INVALID_LINK.create(shareUrl);
        }

        boolean hasKnownError = errorHandler.hasRecentException();
        if (hasKnownError) {
            //We do not really connect because we have known errors,
            //but dummy folders should still be returned
            isConnected = true;
            return;
        }

        try {
            this.guestSession = ServerSessionAdapter.valueOf(services.getServiceSafe(XctxSessionManager.class).getGuestSession(session, baseToken, password));
        } catch (OXException e) {
            errorHandler.handleException(e);
        }

        isConnected = true;
    }

    @Override
    public boolean isConnected() {
        return isConnected;
    }

    @Override
    public void close() {
        this.guestSession = null; // guest session still kept in cache
        isConnected = false;
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
        this.errorHandler.assertNoRecentException();
        return new XctxFileAccess(this, session, guestSession);
    }

    @Override
    public FileStorageFolderAccess getFolderAccess() throws OXException {
        if (false == isConnected()) {
            throw FileStorageExceptionCodes.NOT_CONNECTED.create();
        }
        OXException recentException = this.errorHandler.getRecentException();
        if (recentException != null) {
            //In case of an error state: we return an implementation which will only allow to get the last known folders
            //@formatter:off
            return new ErrorStateFolderAccess(
                recentException,
                (String folderId) -> FederatedSharingFolders.getLastKnownFolder(account, folderId, session));
            //@formatter:on
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
}
