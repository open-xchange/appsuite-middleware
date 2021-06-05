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

package com.openexchange.sessionstorage.hazelcast.serialization.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.session.ObfuscatorService;
import com.openexchange.sessionstorage.hazelcast.serialization.PortableMultipleSessionRemoteLookUp;
import com.openexchange.sessionstorage.hazelcast.serialization.PortableSessionRemoteLookUp;
import com.openexchange.sessionstorage.hazelcast.serialization.PortableSessionRemoteRetrieval;

/**
 * {@link ObfuscatorServiceTracker}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
class ObfuscatorServiceTracker implements ServiceTrackerCustomizer<ObfuscatorService, ObfuscatorService> {

    private final BundleContext context;

    ObfuscatorServiceTracker(BundleContext context) {
        this.context = context;
    }

    @Override
    public void removedService(ServiceReference<ObfuscatorService> reference, ObfuscatorService service) {
        PortableSessionRemoteLookUp.setObfuscatorServiceReference(null);
        PortableSessionRemoteRetrieval.setObfuscatorServiceReference(null);
        PortableMultipleSessionRemoteLookUp.setObfuscatorServiceReference(null);
        context.ungetService(reference);
    }

    @Override
    public void modifiedService(ServiceReference<ObfuscatorService> reference, ObfuscatorService service) {
        // Ignore
    }

    @Override
    public ObfuscatorService addingService(ServiceReference<ObfuscatorService> reference) {
        ObfuscatorService service = context.getService(reference);
        PortableSessionRemoteLookUp.setObfuscatorServiceReference(service);
        PortableSessionRemoteRetrieval.setObfuscatorServiceReference(service);
        PortableMultipleSessionRemoteLookUp.setObfuscatorServiceReference(service);
        return service;
    }

}
