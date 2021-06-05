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

package com.openexchange.file.storage.onedrive;

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
import com.openexchange.file.storage.onedrive.access.OneDriveOAuthAccess;
import com.openexchange.file.storage.onedrive.osgi.Services;
import com.openexchange.oauth.KnownApi;
import com.openexchange.oauth.OAuthUtil;
import com.openexchange.oauth.access.OAuthAccess;
import com.openexchange.oauth.access.OAuthAccessRegistry;
import com.openexchange.oauth.access.OAuthAccessRegistryService;
import com.openexchange.session.Session;

/**
 * {@link OneDriveAccountAccess}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class OneDriveAccountAccess implements FileStorageAccountAccess, CapabilityAware {

    private final FileStorageAccount account;
    private final Session session;
    private final FileStorageService service;
    private volatile OAuthAccess oneDriveAccess;

    /**
     * Initializes a new {@link OneDriveAccountAccess}.
     */
    public OneDriveAccountAccess(FileStorageService service, FileStorageAccount account, Session session) {
        super();
        this.service = service;
        this.account = account;
        this.session = session;
    }

    @Override
    public Boolean supports(FileStorageCapability capability) {
        if (capability.isFileAccessCapability()) {
            return FileStorageCapabilityTools.supportsByClass(OneDriveFileAccess.class, capability);
        }
        return FileStorageCapabilityTools.supportsFolderCapabilityByClass(OneDriveFolderAccess.class, capability);
    }

    /**
     * Gets the associated account
     *
     * @return The account
     */
    public FileStorageAccount getAccount() {
        return account;
    }

    @Override
    public void connect() throws OXException {
        OAuthAccessRegistryService service = Services.getService(OAuthAccessRegistryService.class);
        OAuthAccessRegistry registry = service.get(KnownApi.MICROSOFT_GRAPH.getServiceId());
        int accountId = OAuthUtil.getAccountId(account.getConfiguration());

        OAuthAccess oneDriveAccess = registry.get(session.getContextId(), session.getUserId(), accountId);
        if (oneDriveAccess != null) {
            this.oneDriveAccess = oneDriveAccess.ensureNotExpired();
            return;
        }

        OneDriveOAuthAccess access = new OneDriveOAuthAccess(account, session);
        oneDriveAccess = registry.addIfAbsent(session.getContextId(), session.getUserId(), accountId, access);
        if (oneDriveAccess == null) {
            access.initialize();
            oneDriveAccess = access;
        }
        this.oneDriveAccess = oneDriveAccess;
    }

    @Override
    public boolean isConnected() {
        return null != oneDriveAccess;
    }

    @Override
    public void close() {
        oneDriveAccess = null;
    }

    @Override
    public boolean ping() throws OXException {
        return oneDriveAccess.ping();
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
    public FileStorageFileAccess getFileAccess() throws OXException {
        OAuthAccess oneDriveAccess = this.oneDriveAccess;
        if (null == oneDriveAccess) {
            throw FileStorageExceptionCodes.NOT_CONNECTED.create();
        }
        return new OneDriveFileAccess((OneDriveOAuthAccess) oneDriveAccess, account, session, this, (OneDriveFolderAccess) getFolderAccess());
    }

    @Override
    public FileStorageFolderAccess getFolderAccess() throws OXException {
        OAuthAccess oneDriveAccess = this.oneDriveAccess;
        if (null == oneDriveAccess) {
            throw FileStorageExceptionCodes.NOT_CONNECTED.create();
        }
        return new OneDriveFolderAccess((OneDriveOAuthAccess) oneDriveAccess, account, session);
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

}
