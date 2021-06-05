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

package com.openexchange.folderstorage.filestorage.osgi;

import org.osgi.framework.BundleActivator;
import com.openexchange.file.storage.composition.IDBasedFileAccessFactory;
import com.openexchange.file.storage.composition.IDBasedFolderAccessFactory;
import com.openexchange.file.storage.registry.FileStorageServiceRegistry;
import com.openexchange.folderstorage.FolderField;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.filestorage.impl.AccountErrorField;
import com.openexchange.folderstorage.filestorage.impl.FileStorageFolderStorage;
import com.openexchange.osgi.HousekeepingActivator;

/**
 * {@link FileStorageFolderStorageActivator} - {@link BundleActivator Activator} for file storage folder storage.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class FileStorageFolderStorageActivator extends HousekeepingActivator {

    /**
     * Initializes a new {@link FileStorageFolderStorageActivator}.
     */
    public FileStorageFolderStorageActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { FileStorageServiceRegistry.class, IDBasedFolderAccessFactory.class, IDBasedFileAccessFactory.class };
    }

    @Override
    protected void startBundle() throws Exception {
        final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(FileStorageFolderStorageActivator.class);
        try {
            Services.setServiceLookup(this);
            // Register folder storage
            registerService(FolderStorage.class, new FileStorageFolderStorage(this), singletonDictionary("tree", FolderStorage.REAL_TREE_ID));

            //register custom folder fields
            registerService(FolderField.class, AccountErrorField.getInstance());
        } catch (Exception e) {
            logger.error("", e);
            throw e;
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        Services.setServiceLookup(null);
        super.stopBundle();
    }

}
