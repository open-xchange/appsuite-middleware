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

package com.openexchange.file.storage.infostore.internal;

import com.openexchange.exception.OXException;
import com.openexchange.file.storage.CapabilityAware;
import com.openexchange.file.storage.FileStorageAccountAccess;
import com.openexchange.file.storage.FileStorageCapability;
import com.openexchange.file.storage.FileStorageCapabilityTools;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStorageFolderAccess;
import com.openexchange.file.storage.FileStorageService;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;


/**
 * {@link InfostoreAccountAccess}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class InfostoreAccountAccess implements FileStorageAccountAccess, CapabilityAware {

    private final ServerSession session;
    private final InfostoreFileStorageService service;
    private InfostoreFolderAccess folders;
    private InfostoreAdapterFileAccess files;

    public InfostoreAccountAccess(final Session session, final InfostoreFileStorageService service) throws OXException {
        this.session = ServerSessionAdapter.valueOf(session);
        this.service = service;
    }

    @Override
    public Boolean supports(FileStorageCapability capability) {
        if (capability.isFileAccessCapability()) {
            return FileStorageCapabilityTools.supportsByClass(InfostoreAdapterFileAccess.class, capability);
        }
        return FileStorageCapabilityTools.supportsFolderCapabilityByClass(InfostoreFolderAccess.class, capability);
    }

    @Override
    public String getAccountId() {
        return InfostoreDefaultAccountManager.DEFAULT_ID;
    }

    @Override
    public FileStorageFileAccess getFileAccess() throws OXException {
        if (files != null) {
            return files;
        }
        return files = new InfostoreAdapterFileAccess(session, service.getInfostore(), service.getSearch(), this);
    }

    @Override
    public FileStorageFolderAccess getFolderAccess() throws OXException {
        if (folders != null) {
            return folders;
        }
        return folders = new InfostoreFolderAccess(session, service.getInfostore());
    }

    @Override
    public FileStorageFolder getRootFolder() throws OXException {
        return getFolderAccess().getRootFolder();
    }

    @Override
    public boolean cacheable() {
        return false;
    }

    @Override
    public void close() {
        // Nope
    }

    @Override
    public void connect() throws OXException {
        // Bypassed...
    }

    @Override
    public boolean isConnected() {
        return true;
    }

    @Override
    public boolean ping() throws OXException {
        return true;
    }

    @Override
    public FileStorageService getService() {
        return service;
    }

}
