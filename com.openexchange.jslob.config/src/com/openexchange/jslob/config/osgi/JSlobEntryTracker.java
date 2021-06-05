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

package com.openexchange.jslob.config.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import com.openexchange.exception.OXException;
import com.openexchange.jslob.JSlobEntry;
import com.openexchange.jslob.config.JSlobEntryRegistry;

/**
 * {@link JSlobEntryTracker} - Tracks registered {@link JSlobEntry} instances.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class JSlobEntryTracker implements ServiceTrackerCustomizer<JSlobEntry, JSlobEntry> {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(JSlobEntryTracker.class);

    private final JSlobEntryRegistry jSlobEntryRegistry;
    private final BundleContext context;

    /**
     * Initializes a new {@link JSlobEntryTracker}.
     */
    public JSlobEntryTracker(JSlobEntryRegistry jSlobEntryRegistry, BundleContext context) {
        super();
        this.jSlobEntryRegistry = jSlobEntryRegistry;
        this.context = context;
    }

    @Override
    public JSlobEntry addingService(ServiceReference<JSlobEntry> reference) {
        JSlobEntry jSlobEntry = context.getService(reference);
        try {
            if (jSlobEntryRegistry.addJSlobEntry(jSlobEntry)) {
                return jSlobEntry;
            }
        } catch (OXException e) {
            LOG.error("Failed to register JSlob entry", e);
        }

        context.ungetService(reference);
        return null;
    }

    @Override
    public void modifiedService(ServiceReference<JSlobEntry> reference, JSlobEntry jSlobEntry) {
        // Don't care
    }

    @Override
    public void removedService(ServiceReference<JSlobEntry> reference, JSlobEntry jSlobEntry) {
        jSlobEntryRegistry.removeJSlobEntry(jSlobEntry);
        context.ungetService(reference);
    }

}
