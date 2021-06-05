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

package com.openexchange.global.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.i18n.I18nService;
import com.openexchange.i18n.internal.I18nServiceRegistryImpl;


/**
 * {@link I18nServiceTracker}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.4
 */
public class I18nServiceTracker implements ServiceTrackerCustomizer<I18nService, I18nService> {

    private final BundleContext context;
    private final I18nServiceRegistryImpl registry;

    /**
     * Initializes a new {@link I18nServiceTracker}.
     */
    public I18nServiceTracker(I18nServiceRegistryImpl registry, BundleContext context) {
        super();
        this.registry = registry;
        this.context = context;
    }

    @Override
    public I18nService addingService(ServiceReference<I18nService> reference) {
        I18nService service = context.getService(reference);
        if (registry.addI18nService(service)) {
            return service;
        }

        context.ungetService(reference);
        return null;
    }

    @Override
    public void modifiedService(ServiceReference<I18nService> reference, I18nService service) {
        // Ignore
    }

    @Override
    public void removedService(ServiceReference<I18nService> reference, I18nService service) {
        registry.removeI18nService(service);
        context.ungetService(reference);
    }

}
