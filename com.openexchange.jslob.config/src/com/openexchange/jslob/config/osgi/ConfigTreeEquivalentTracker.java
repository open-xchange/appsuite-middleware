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
import com.openexchange.jslob.ConfigTreeEquivalent;
import com.openexchange.jslob.config.ConfigJSlobService;


/**
 * {@link ConfigTreeEquivalentTracker} - The tracker for registered {@link ConfigTreeEquivalent}s.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.4.1
 */
public final class ConfigTreeEquivalentTracker implements ServiceTrackerCustomizer<ConfigTreeEquivalent, ConfigTreeEquivalent> {

    private final BundleContext context;
    private final ConfigJSlobService configJSlobService;

    /**
     * Initializes a new {@link ConfigTreeEquivalentTracker}.
     */
    public ConfigTreeEquivalentTracker(final ConfigJSlobService configJSlobService, final BundleContext context) {
        super();
        this.configJSlobService = configJSlobService;
        this.context = context;
    }

    @Override
    public ConfigTreeEquivalent addingService(final ServiceReference<ConfigTreeEquivalent> reference) {
        final ConfigTreeEquivalent service = context.getService(reference);
        configJSlobService.addConfigTreeEquivalent(service.getConfigTreePath(), service.getJslobPath());
        return service;
    }

    @Override
    public void modifiedService(final ServiceReference<ConfigTreeEquivalent> reference, final ConfigTreeEquivalent service) {
        // Nothing to do
    }

    @Override
    public void removedService(final ServiceReference<ConfigTreeEquivalent> reference, final ConfigTreeEquivalent service) {
        try {
            configJSlobService.removeConfigTreeEquivalent(service.getConfigTreePath(), service.getJslobPath());
        } finally {
            context.ungetService(reference);
        }
    }

}
