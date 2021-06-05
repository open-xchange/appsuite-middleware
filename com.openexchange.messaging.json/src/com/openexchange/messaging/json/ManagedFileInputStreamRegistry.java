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

package com.openexchange.messaging.json;

import java.io.IOException;
import java.io.InputStream;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.exception.OXException;
import com.openexchange.filemanagement.ManagedFile;
import com.openexchange.filemanagement.ManagedFileManagement;

/**
 * {@link ManagedFileInputStreamRegistry}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ManagedFileInputStreamRegistry implements MessagingInputStreamRegistry {

    private static volatile ManagedFileInputStreamRegistry instance = null;

    /**
     * Gets the instance.
     *
     * @return The instance
     */
    public static ManagedFileInputStreamRegistry getInstance() {
        ManagedFileInputStreamRegistry tmp = instance;
        if (null == tmp) {
            synchronized (ManagedFileInputStreamRegistry.class) {
                tmp = instance;
                if (null == tmp) {
                    tmp = new ManagedFileInputStreamRegistry();
                    instance = tmp;
                }
            }
        }
        return tmp;
    }

    /**
     * Drops the instance.
     */
    public static void dropInstance() {
        instance = null;
    }

    protected volatile ManagedFileManagement fileManagement;

    private volatile ServiceTracker<ManagedFileManagement, ManagedFileManagement> tracker;

    /**
     * Initializes a new {@link ManagedFileInputStreamRegistry}.
     */
    public ManagedFileInputStreamRegistry() {
        super();
    }

    /**
     * Starts this registry.
     *
     * @param context The bundle context used to track needed service
     */
    public void start(final BundleContext context) {
        if (null != tracker) {
            return;
        }
        tracker =
            new ServiceTracker<ManagedFileManagement, ManagedFileManagement>(
                context,
                ManagedFileManagement.class,
                new ServiceTrackerCustomizer<ManagedFileManagement, ManagedFileManagement>() {

                    @Override
                    public ManagedFileManagement addingService(final ServiceReference<ManagedFileManagement> reference) {
                        final ManagedFileManagement service = context.getService(reference);
                        fileManagement = service;
                        return service;
                    }

                    @Override
                    public void modifiedService(final ServiceReference<ManagedFileManagement> reference, final ManagedFileManagement service) {
                        // Mope
                    }

                    @Override
                    public void removedService(final ServiceReference<ManagedFileManagement> reference, final ManagedFileManagement service) {
                        fileManagement = null;
                        context.ungetService(reference);
                    }
                });
        tracker.open();
    }

    /**
     * Stops this registry orderly.
     */
    public void stop() {
        fileManagement = null;
        if (null != tracker) {
            tracker.close();
            tracker = null;
        }
    }

    @Override
    public Object getRegistryEntry(final Object id) throws OXException {
        return fileManagement.getByID(id.toString());
    }

    @Override
    public InputStream get(final Object id) throws OXException, IOException {
        final ManagedFile managedFile = fileManagement.getByID(id.toString());
        if (null == managedFile) {
            throw new IOException("No managed file associated with id: " + id);
        }
        return managedFile.getInputStream();
    }

}
