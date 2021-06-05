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

package com.openexchange.file.storage.composition.osgi;

import org.osgi.service.event.EventAdmin;
import com.openexchange.config.ConfigurationService;
import com.openexchange.file.storage.FileStorageAccountManagerLookupService;
import com.openexchange.file.storage.composition.FileStreamHandlerRegistry;
import com.openexchange.file.storage.composition.IDBasedFileAccessFactory;
import com.openexchange.file.storage.composition.IDBasedFolderAccessFactory;
import com.openexchange.file.storage.composition.crypto.CryptoAwareSharingService;
import com.openexchange.file.storage.composition.internal.AbstractCompositingIDBasedFileAccess;
import com.openexchange.file.storage.composition.internal.CompositingIDBasedFolderAccess;
import com.openexchange.file.storage.composition.internal.FileStreamHandlerRegistryImpl;
import com.openexchange.file.storage.composition.internal.Services;
import com.openexchange.file.storage.registry.FileStorageServiceRegistry;
import com.openexchange.marker.OXThreadMarkers;
import com.openexchange.objectusecount.ObjectUseCountService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.principalusecount.PrincipalUseCountService;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.share.ShareService;
import com.openexchange.share.notification.ShareNotificationService;
import com.openexchange.threadpool.ThreadPoolService;


/**
 * {@link FileStorageCompositionActivator}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class FileStorageCompositionActivator extends HousekeepingActivator {

    /**
     * Initializes a new {@link FileStorageCompositionActivator}.
     */
    public FileStorageCompositionActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { FileStorageServiceRegistry.class, EventAdmin.class, ThreadPoolService.class, ConfigurationService.class, FileStorageAccountManagerLookupService.class };
    }

    @Override
    protected Class<?>[] getOptionalServices() {
        return new Class<?>[]{ CryptoAwareSharingService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        final ServiceLookup services = this;
        Services.setServiceLookup(services);

        IDBasedFileAccessListenerRegistry listenerRegistry = IDBasedFileAccessListenerRegistry.initInstance(context);
        rememberTracker(listenerRegistry);

        // The tracking factory
        TrackingIDBasedFileAccessFactory fileAccessFactory = new TrackingIDBasedFileAccessFactory(services, context);
        rememberTracker(fileAccessFactory);

        // Start-up & register FileStreamHandlerRegistry
        FileStreamHandlerRegistryImpl registry = new FileStreamHandlerRegistryImpl(context);
        AbstractCompositingIDBasedFileAccess.setHandlerRegistry(registry);
        rememberTracker(registry);
        trackService(ShareService.class);
        trackService(ShareNotificationService.class);
        trackService(ObjectUseCountService.class);
        trackService(PrincipalUseCountService.class);
        openTrackers();

        // Register file access factory
        registerService(IDBasedFileAccessFactory.class, fileAccessFactory);

        // Register folder access factory
        registerService(IDBasedFolderAccessFactory.class, new IDBasedFolderAccessFactory() {

            @Override
            public CompositingIDBasedFolderAccess createAccess(Session session) {
                CompositingIDBasedFolderAccess folderAccess = new CompositingIDBasedFolderAccess(session, services);
                OXThreadMarkers.rememberCloseable(folderAccess);
                return folderAccess;
            }

        });

        registerService(FileStreamHandlerRegistry.class, registry);
    }

    @Override
    protected void stopBundle() throws Exception {
        super.stopBundle();
        AbstractCompositingIDBasedFileAccess.setHandlerRegistry(null);
        IDBasedFileAccessListenerRegistry.dropInstance();
        Services.setServiceLookup(null);
    }

}
