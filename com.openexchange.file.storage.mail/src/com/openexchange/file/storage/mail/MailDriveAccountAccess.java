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

package com.openexchange.file.storage.mail;

import com.openexchange.exception.OXException;
import com.openexchange.file.storage.CapabilityAware;
import com.openexchange.file.storage.FileStorageAccountAccess;
import com.openexchange.file.storage.FileStorageCapability;
import com.openexchange.file.storage.FileStorageCapabilityTools;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStorageFolderAccess;
import com.openexchange.file.storage.FileStorageService;
import com.openexchange.file.storage.mail.FullName.Type;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.session.Session;

/**
 * {@link MailDriveAccountAccess}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public final class MailDriveAccountAccess implements FileStorageAccountAccess, CapabilityAware {

    private final FullNameCollection fullNameCollection;
    private final Session session;
    private final FileStorageService service;
    private volatile boolean connected;

    /**
     * Initializes a new {@link MailDriveAccountAccess}.
     *
     * @throws OXException If initialization fails
     */
    public MailDriveAccountAccess(FullNameCollection fullNameCollection, FileStorageService service, Session session) {
        super();
        this.fullNameCollection = fullNameCollection;
        this.service = service;
        this.session = session;
    }

    /**
     * Gets the collection of full names for virtual attachment folders.
     *
     * @return The collection of full names for virtual attachment folders
     */
    public FullNameCollection getFullNameCollection() {
        return fullNameCollection;
    }

    /**
     * Checks given folder identifier.
     *
     * @param folderId The folder identifier to check
     * @param fullNameCollection The collection of full names for virtual attachment folders
     * @return The associated full name or <code>null</code> if folder identifier is invalid
     */
    public static FullName optFolderId(String folderId, FullNameCollection fullNameCollection) {
        Type type = Type.typeByFolderId(folderId);
        return null == type ? null : fullNameCollection.getFullNameFor(type);
    }

    @Override
    public Boolean supports(FileStorageCapability capability) {
        if (capability.isFileAccessCapability()) {
            return FileStorageCapabilityTools.supportsByClass(MailDriveFileAccess.class, capability);
        }
        return FileStorageCapabilityTools.supportsFolderCapabilityByClass(MailDriveFolderAccess.class, capability);
    }

    @Override
    public void connect() throws OXException {
        connected = true;
    }

    @Override
    public boolean isConnected() {
        return connected;
    }

    @Override
    public void close() {
        connected = false;
    }

    @Override
    public boolean ping() throws OXException {
        MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = MailAccess.getInstance(session);
        try {
            return mailAccess.ping();
        } finally {
            mailAccess.close();
        }
    }

    @Override
    public boolean cacheable() {
        return false;
    }

    @Override
    public String getAccountId() {
        return MailDriveConstants.ACCOUNT_ID;
    }

    @Override
    public FileStorageFileAccess getFileAccess() throws OXException {
        if (!connected) {
            throw FileStorageExceptionCodes.NOT_CONNECTED.create();
        }
        return new MailDriveFileAccess(fullNameCollection, session, this);
    }

    @Override
    public FileStorageFolderAccess getFolderAccess() throws OXException {
        if (!connected) {
            throw FileStorageExceptionCodes.NOT_CONNECTED.create();
        }
        return new MailDriveFolderAccess(fullNameCollection, session);
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
