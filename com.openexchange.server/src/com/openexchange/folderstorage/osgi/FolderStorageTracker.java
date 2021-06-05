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

package com.openexchange.folderstorage.osgi;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.folderstorage.FolderModifier;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.FolderStorageFolderModifier;
import com.openexchange.folderstorage.internal.FolderStorageRegistry;

/**
 * {@link FolderStorageTracker} - The tracker for folder storages.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class FolderStorageTracker implements ServiceTrackerCustomizer<FolderStorage,FolderStorage> {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(FolderStorageTracker.class);

    private final BundleContext context;
    private final ConcurrentMap<FolderStorage, ServiceTracker<FolderModifier, FolderModifier>> modifierTrackers;

    /**
     * Initializes a new {@link FolderStorageTracker}.
     */
    public FolderStorageTracker(final BundleContext context) {
        super();
        this.context = context;
        modifierTrackers = new ConcurrentHashMap<>(8, 0.9F, 1);
    }

    @Override
    public FolderStorage addingService(final ServiceReference<FolderStorage> reference) {
        FolderStorage folderStorage = context.getService(reference);

        // Get tree identifier
        final String treeId;
        {
            final Object obj = reference.getProperty("tree");
            if (null == obj) {
                LOG.error("Missing tree identifier property \"tree\" for {}", folderStorage.getClass().getName());
                // Nothing to track, return null
                context.ungetService(reference);
                return null;
            }
            treeId = obj.toString();
        }

        // Add to registry
        if (FolderStorageRegistry.getInstance().addFolderStorage(treeId, folderStorage)) {
            if (folderStorage instanceof FolderStorageFolderModifier) {
                ServiceTracker<FolderModifier, FolderModifier> tracker = new ServiceTracker<>(context, FolderModifier.class, new FolderModifierTracker((FolderStorageFolderModifier) folderStorage, context));
                modifierTrackers.put(folderStorage, tracker);
                tracker.open();
            }
            return folderStorage;
        }

        LOG.error("Failed registration to tree identifier \"{}\" for {}", treeId, folderStorage.getClass().getName());
        // Nothing to track, return null
        context.ungetService(reference);
        return null;
    }

    @Override
    public void modifiedService(ServiceReference<FolderStorage> reference, FolderStorage folderStorage) {
        // Nothing to do
    }

    @Override
    public void removedService(ServiceReference<FolderStorage> reference, FolderStorage folderStorage) {
        if (null != folderStorage) {
            try {
                // Get tree identifier
                final String treeId;
                {
                    final Object obj = reference.getProperty("tree");
                    if (null == obj) {
                        LOG.error("Missing tree identifier property \"tree\" for {}", folderStorage.getClass().getName());
                        return;
                    }
                    treeId = obj.toString();
                }

                FolderStorageRegistry.getInstance().removeFolderStorage(treeId, folderStorage);
                if (folderStorage instanceof FolderStorageFolderModifier) {
                    ServiceTracker<FolderModifier, FolderModifier> tracker = modifierTrackers.remove(folderStorage);
                    if (null != tracker) {
                        tracker.close();
                    }
                }
            } finally {
                context.ungetService(reference);
            }
        }
    }

}
