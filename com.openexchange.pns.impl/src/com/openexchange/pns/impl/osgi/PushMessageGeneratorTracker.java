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

package com.openexchange.pns.impl.osgi;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import com.openexchange.exception.OXException;
import com.openexchange.pns.PushMessageGenerator;
import com.openexchange.pns.PushMessageGeneratorRegistry;

/**
 * {@link PushMessageGeneratorTracker} - The tracker for transports.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public final class PushMessageGeneratorTracker implements ServiceTrackerCustomizer<PushMessageGenerator, PushMessageGenerator>, PushMessageGeneratorRegistry {

    private final ConcurrentMap<String, PushMessageGenerator> generators;
    private final BundleContext context;

    /**
     * Initializes a new {@link PushMessageGeneratorTracker}.
     */
    public PushMessageGeneratorTracker(BundleContext context) {
        super();
        this.generators = new ConcurrentHashMap<>(4, 0.9F, 1);
        this.context = context;
    }

    @Override
    public PushMessageGenerator addingService(ServiceReference<PushMessageGenerator> reference) {
        Logger logger = org.slf4j.LoggerFactory.getLogger(PushMessageGeneratorTracker.class);

        PushMessageGenerator generator = context.getService(reference);
        if (null == generators.putIfAbsent(generator.getClient(), generator)) {
            logger.info("Successfully registered push message generator for client '{}'", generator.getClient());
            return generator;
        }

        logger.error("Failed to register push message generator for class {}. There is already such a generator for client '{}'.", generator.getClass().getName(), generator.getClient());
        context.ungetService(reference);
        return null;
    }

    @Override
    public void modifiedService(ServiceReference<PushMessageGenerator> reference, PushMessageGenerator generator) {
        // Nothing
    }

    @Override
    public void removedService(ServiceReference<PushMessageGenerator> reference, PushMessageGenerator generator) {
        Logger logger = org.slf4j.LoggerFactory.getLogger(PushMessageGeneratorTracker.class);

        if (null != generators.remove(generator.getClient())) {
            logger.info("Successfully unregistered push message generator for client '{}'", generator.getClient());
            return;
        }

        context.ungetService(reference);
    }

    @Override
    public PushMessageGenerator getGenerator(String client) throws OXException {
        if (null == client) {
            return null;
        }
        return generators.get(client);
    }

}