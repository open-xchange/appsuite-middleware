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

package com.openexchange.ajax.requesthandler.osgi;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.ajax.requesthandler.AJAXResultDecorator;
import com.openexchange.ajax.requesthandler.AJAXResultDecoratorRegistry;

/**
 * {@link OSGiAJAXResultDecoratorRegistry}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class OSGiAJAXResultDecoratorRegistry implements AJAXResultDecoratorRegistry, ServiceTrackerCustomizer<AJAXResultDecorator, AJAXResultDecorator> {

    /**
     * The backing map.
     */
    final ConcurrentMap<String, AJAXResultDecorator> map;

    /**
     * The bundle context.
     */
    private final BundleContext context;

    /**
     * Initializes a new {@link OSGiChatServiceRegistry}.
     */
    public OSGiAJAXResultDecoratorRegistry(final BundleContext context) {
        super();
        this.context = context;
        map = new ConcurrentHashMap<String, AJAXResultDecorator>(8, 0.9f, 1);
    }

    @Override
    public List<AJAXResultDecorator> getDecorators() {
        return new ArrayList<AJAXResultDecorator>(map.values());
    }

    @Override
    public AJAXResultDecorator getDecorator(final String identifier) {
        return map.get(identifier);
    }

    @Override
    public AJAXResultDecorator addingService(final ServiceReference<AJAXResultDecorator> reference) {
        final AJAXResultDecorator service = context.getService(reference);
        if (null == map.putIfAbsent(service.getIdentifier(), service)) {
            return service;
        }
        final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(OSGiAJAXResultDecoratorRegistry.class);
        logger.warn("Another AJAXResultDecorator is already registered with identifier: {}", service.getIdentifier());
        context.ungetService(reference);
        return null;
    }

    @Override
    public void modifiedService(final ServiceReference<AJAXResultDecorator> reference, final AJAXResultDecorator service) {
        // Nothing to do
    }

    @Override
    public void removedService(final ServiceReference<AJAXResultDecorator> reference, final AJAXResultDecorator service) {
        try {
            map.remove(service.getIdentifier());
        } finally {
            context.ungetService(reference);
        }
    }

}
