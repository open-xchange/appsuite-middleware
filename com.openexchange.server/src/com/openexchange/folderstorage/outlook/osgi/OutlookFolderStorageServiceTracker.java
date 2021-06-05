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

package com.openexchange.folderstorage.outlook.osgi;

import static com.openexchange.folderstorage.outlook.OutlookFolderStorageRegistry.getInstance;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.folderstorage.FolderStorage;

/**
 * {@link OutlookFolderStorageServiceTracker} - A {@link ServiceTrackerCustomizer customizer} for MS Outlook folder storage which tracks
 * real folder storages; meaning those storages which indicate to handle the {@link FolderStorage#REAL_TREE_ID} tree identifier.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class OutlookFolderStorageServiceTracker implements ServiceTrackerCustomizer<FolderStorage,FolderStorage> {

    private static final org.slf4j.Logger LOG =
        org.slf4j.LoggerFactory.getLogger(OutlookFolderStorageServiceTracker.class);

    private final BundleContext context;

    public OutlookFolderStorageServiceTracker(final BundleContext context) {
        super();
        this.context = context;
    }

    @Override
    public FolderStorage addingService(final ServiceReference<FolderStorage> reference) {
        final FolderStorage addedService = context.getService(reference);
        // Get tree identifier
        final String treeId;
        {
            final Object obj = reference.getProperty("tree");
            if (null == obj) {
                LOG.error("Missing tree identifier property \"tree\" for {}", addedService.getClass().getName());
                // Nothing to track, return null
                context.ungetService(reference);
                return null;
            }
            treeId = obj.toString();
        }
        // Add to registry
        if (getInstance().addFolderStorage(treeId, addedService)) {
            return addedService;
        }
        // Nothing to track, return null
        context.ungetService(reference);
        return null;
    }

    @Override
    public void modifiedService(final ServiceReference<FolderStorage> reference, final FolderStorage service) {
        // Nothing to do
    }

    @Override
    public void removedService(final ServiceReference<FolderStorage> reference, final FolderStorage service) {
        if (null != service) {
            try {
                // Get tree identifier
                final String treeId;
                {
                    final Object obj = reference.getProperty("tree");
                    if (null == obj) {
                        LOG.error("Missing tree identifier property \"tree\" for {}", service.getClass().getName());
                        return;
                    }
                    treeId = obj.toString();
                }
                getInstance().removeFolderStorage(treeId, service);
            } finally {
                context.ungetService(reference);
            }
        }
    }

}
