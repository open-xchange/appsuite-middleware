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

package com.openexchange.push.impl.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.push.PushManagerService;
import com.openexchange.push.impl.PushManagerRegistry;

/**
 * {@link PushManagerServiceTracker} - The service tracker for push managers.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class PushManagerServiceTracker implements ServiceTrackerCustomizer<PushManagerService,PushManagerService> {

    private final BundleContext context;

    /**
     * Initializes a new {@link PushManagerServiceTracker}.
     *
     * @param context The bundle context
     */
    public PushManagerServiceTracker(final BundleContext context) {
        super();
        this.context = context;
    }

    @Override
    public PushManagerService addingService(final ServiceReference<PushManagerService> reference) {
        final PushManagerService service = context.getService(reference);
        if (PushManagerRegistry.getInstance().addPushManager(service)) {
            final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PushManagerServiceTracker.class);
            log.info("Registered push manager: {}", service.getClass().getName());
            return service;
        }
        /*
         * Nothing to track
         */
        context.ungetService(reference);
        return null;
    }

    @Override
    public void modifiedService(final ServiceReference<PushManagerService> reference, final PushManagerService service) {
        // NOP
    }

    @Override
    public void removedService(final ServiceReference<PushManagerService> reference, final PushManagerService service) {
        if (null != service) {
            try {
                PushManagerRegistry.getInstance().removePushManager(service);
            } finally {
                context.ungetService(reference);
            }
        }
    }
}
