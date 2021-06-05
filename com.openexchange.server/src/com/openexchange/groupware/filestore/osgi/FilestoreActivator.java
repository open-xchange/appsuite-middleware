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

package com.openexchange.groupware.filestore.osgi;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import com.openexchange.filestore.FileStorageInfoService;
import com.openexchange.groupware.filestore.FileStorageInfoServiceImpl;

/**
 * {@link FilestoreActivator}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class FilestoreActivator implements BundleActivator {

    private ServiceRegistration<FileStorageInfoService> registration;

    /**
     * Initializes a new {@link FilestoreActivator}.
     */
    public FilestoreActivator() {
        super();
    }

    @Override
    public synchronized void start(final BundleContext context) throws Exception {
        registration = context.registerService(FileStorageInfoService.class, new FileStorageInfoServiceImpl(), null);
    }

    @Override
    public synchronized void stop(final BundleContext context) throws Exception {
        ServiceRegistration<FileStorageInfoService> registration = this.registration;
        if (null != registration) {
            this.registration = null;
            registration.unregister();
        }
    }

}
