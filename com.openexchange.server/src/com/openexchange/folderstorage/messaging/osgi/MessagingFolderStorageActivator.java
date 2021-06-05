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

package com.openexchange.folderstorage.messaging.osgi;

import java.util.Arrays;
import java.util.Dictionary;
import java.util.Hashtable;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.mail.contentType.MailContentType;
import com.openexchange.folderstorage.messaging.MessagingFolderStorage;
import com.openexchange.messaging.registry.MessagingServiceRegistry;
import com.openexchange.osgi.HousekeepingActivator;

/**
 * {@link MessagingFolderStorageActivator} - {@link BundleActivator Activator} for messaging folder storage.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MessagingFolderStorageActivator extends HousekeepingActivator {

    /**
     * Initializes a new {@link MessagingFolderStorageActivator}.
     */
    public MessagingFolderStorageActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { MessagingServiceRegistry.class };
    }

    @Override
    protected void startBundle() throws Exception {
        try {
            Services.setServiceLookup(this);

            // Trackers
            rememberTracker(new ServiceTracker<FolderStorage,FolderStorage>(context, FolderStorage.class, new Switcher(context)));
            openTrackers();

            // Register folder storage
            final Dictionary<String, String> dictionary = new Hashtable<String, String>(2);
            dictionary.put("tree", FolderStorage.REAL_TREE_ID);
            registerService(FolderStorage.class, new MessagingFolderStorage(this), dictionary);
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(MessagingFolderStorageActivator.class).error("", e);
            throw e;
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        Services.setServiceLookup(null);
        super.stopBundle();
    }

    private static final class Switcher implements ServiceTrackerCustomizer<FolderStorage,FolderStorage> {

        private final BundleContext context;

        Switcher(final BundleContext context) {
            super();
            this.context = context;
        }

        @Override
        public FolderStorage addingService(final ServiceReference<FolderStorage> reference) {
            final FolderStorage folderStorage = context.getService(reference);
            if (Arrays.asList(folderStorage.getSupportedContentTypes()).contains(MailContentType.getInstance())) {
                MessagingFolderStorage.setMailFolderStorageAvailable(true);
                return folderStorage;
            }
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
                final FolderStorage folderStorage = service;
                if (Arrays.asList(folderStorage.getSupportedContentTypes()).contains(MailContentType.getInstance())) {
                    MessagingFolderStorage.setMailFolderStorageAvailable(false);
                }
                context.ungetService(reference);
            }
        }

    }

}
