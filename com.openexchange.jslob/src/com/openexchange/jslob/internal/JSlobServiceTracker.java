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

package com.openexchange.jslob.internal;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.exception.OXException;
import com.openexchange.jslob.JSlobService;

/**
 * {@link JSlobServiceTracker}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class JSlobServiceTracker implements ServiceTrackerCustomizer<JSlobService, JSlobService> {

    private final BundleContext context;

    /**
     * Initializes a new {@link JSlobServiceTracker}.
     */
    public JSlobServiceTracker(final BundleContext context) {
        super();
        this.context = context;
    }

    @Override
    public JSlobService addingService(final ServiceReference<JSlobService> reference) {
        final JSlobService service = context.getService(reference);
        if (JSlobServiceRegistryImpl.getInstance().putJSlobService(service)) {
            return service;
        }
        /*
         * Nothing to track
         */
        context.ungetService(reference);
        return null;
    }

    @Override
    public void modifiedService(final ServiceReference<JSlobService> reference, final JSlobService service) {
        // Nothing to do
    }

    @Override
    public void removedService(final ServiceReference<JSlobService> reference, final JSlobService service) {
        try {
            JSlobServiceRegistryImpl.getInstance().removeJSlobService(service);
        } catch (OXException e) {
            org.slf4j.LoggerFactory.getLogger(JSlobServiceTracker.class).error("", e);
        } finally {
            context.ungetService(reference);
        }
    }

}
