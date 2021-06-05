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

package com.openexchange.folderstorage.virtual.osgi;

import java.util.Dictionary;
import java.util.Hashtable;
import org.osgi.framework.BundleActivator;
import com.openexchange.database.DatabaseService;
import com.openexchange.folderstorage.FolderField;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.virtual.VirtualFolderDeleteListener;
import com.openexchange.folderstorage.virtual.VirtualFolderStorage;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.osgi.HousekeepingActivator;

/**
 * {@link VirtualFolderStorageActivator} - {@link BundleActivator Activator} for virtual folder storage.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class VirtualFolderStorageActivator extends HousekeepingActivator {

    /**
     * Initializes a new {@link VirtualFolderStorageActivator}.
     */
    public VirtualFolderStorageActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { DatabaseService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        try {
            Services.setServiceLookup(this);

            // Trackers
            track(FolderStorage.class, new VirtualFolderStorageServiceTracker(context));
            openTrackers();

            // Register services
            registerService(DeleteListener.class, new VirtualFolderDeleteListener(), null);

            // Register folder properties
            registerService(FolderField.class, VirtualFolderStorage.FIELD_NAME_PAIR_PREDEFINED);

            final Dictionary<String, String> dictionary = new Hashtable<String, String>(2);
            dictionary.put("tree", FolderStorage.ALL_TREE_ID);
            registerService(FolderStorage.class, VirtualFolderStorage.getInstance(), dictionary);
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(VirtualFolderStorageActivator.class).error("", e);
            throw e;
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        Services.setServiceLookup(null);
        super.stopBundle();
    }

}
