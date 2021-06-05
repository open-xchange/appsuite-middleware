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

package com.openexchange.file.storage.googledrive;

import com.openexchange.annotation.NonNull;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.CapabilityAware;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageCapability;
import com.openexchange.file.storage.FileStorageCapabilityTools;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStorageFolderAccess;
import com.openexchange.file.storage.FileStorageService;
import com.openexchange.file.storage.googledrive.access.GoogleDriveOAuthAccess;
import com.openexchange.file.storage.googledrive.osgi.Services;
import com.openexchange.oauth.KnownApi;
import com.openexchange.oauth.OAuthUtil;
import com.openexchange.oauth.access.OAuthAccess;
import com.openexchange.oauth.access.OAuthAccessRegistry;
import com.openexchange.oauth.access.OAuthAccessRegistryService;
import com.openexchange.session.Session;

/**
 * {@link GoogleDriveAccountAccess}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class GoogleDriveAccountAccess implements CapabilityAware {

    private final @NonNull FileStorageAccount account;

    private final @NonNull Session session;

    private final FileStorageService service;

    private volatile OAuthAccess googleDriveAccess;

    /**
     * Initializes a new {@link GoogleDriveAccountAccess}.
     *
     * @param service The {@link FileStorageService}
     * @param account The {@link FileStorageAccount}
     * @param session The {@link Session}
     */
    public GoogleDriveAccountAccess(FileStorageService service, @NonNull FileStorageAccount account, @NonNull Session session) {
        super();
        this.service = service;
        this.account = account;
        this.session = session;
    }

    @Override
    public Boolean supports(FileStorageCapability capability) {
        if (capability.isFileAccessCapability()) {
            return FileStorageCapabilityTools.supportsByClass(GoogleDriveFileAccess.class, capability);
        }
        return FileStorageCapabilityTools.supportsFolderCapabilityByClass(GoogleDriveFolderAccess.class, capability);
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
        OAuthAccessRegistry registry = service.get(KnownApi.GOOGLE.getServiceId());
        int accountId = OAuthUtil.getAccountId(account.getConfiguration());
        OAuthAccess googleDriveAccess = registry.get(session.getContextId(), session.getUserId(), accountId);
        if (googleDriveAccess == null) {
            GoogleDriveOAuthAccess access = new GoogleDriveOAuthAccess(account, session);
            googleDriveAccess = registry.addIfAbsent(session.getContextId(), session.getUserId(), accountId, access);
            if (null == googleDriveAccess) {
                access.initialize();
                googleDriveAccess = access;
            }
            this.googleDriveAccess = googleDriveAccess;
        } else {
            this.googleDriveAccess = googleDriveAccess.ensureNotExpired();
        }
    }

    @Override
    public boolean isConnected() {
        return null != googleDriveAccess;
    }

    @Override
    public void close() {
        googleDriveAccess = null;
    }

    @Override
    public boolean ping() throws OXException {
        return googleDriveAccess.ping();
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
        GoogleDriveOAuthAccess googleDriveOAuthAccess = (GoogleDriveOAuthAccess) googleDriveAccess;
        if (null == googleDriveOAuthAccess) {
            throw FileStorageExceptionCodes.NOT_CONNECTED.create();
        }
        return new GoogleDriveFileAccess(googleDriveOAuthAccess, account, session, this, getGoogleDriveFolderAccess());
    }

    @Override
    public FileStorageFolderAccess getFolderAccess() throws OXException {
        return getGoogleDriveFolderAccess();
    }

    private GoogleDriveFolderAccess getGoogleDriveFolderAccess() throws OXException {
        GoogleDriveOAuthAccess googleDriveOAuthAccess = (GoogleDriveOAuthAccess) googleDriveAccess;
        if (null == googleDriveOAuthAccess) {
            throw FileStorageExceptionCodes.NOT_CONNECTED.create();
        }
        return new GoogleDriveFolderAccess(googleDriveOAuthAccess, account, session);
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
