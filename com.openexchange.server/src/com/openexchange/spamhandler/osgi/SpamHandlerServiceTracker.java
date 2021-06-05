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

package com.openexchange.spamhandler.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.spamhandler.SpamHandler;
import com.openexchange.spamhandler.SpamHandlerRegistry;

/**
 * Service tracker for mail providers
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SpamHandlerServiceTracker implements ServiceTrackerCustomizer<SpamHandler,SpamHandler> {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SpamHandlerServiceTracker.class);

    private final BundleContext context;

    /**
     * Initializes a new {@link SpamHandlerServiceTracker}
     */
    public SpamHandlerServiceTracker(final BundleContext context) {
        super();
        this.context = context;
    }

    @Override
    public SpamHandler addingService(final ServiceReference<SpamHandler> reference) {
        SpamHandler spamHandler = context.getService(reference);
        Object registrationName = reference.getProperty("name");
        if (null == registrationName) {
            LOG.error("Missing registration name in spam handler service: {}", spamHandler.getClass().getName());
            context.ungetService(reference);
            return null;
        }

        if (SpamHandlerRegistry.registerSpamHandler(registrationName.toString(), spamHandler)) {
            LOG.info("Spam handler registered for name '{}", registrationName);
            return spamHandler;
        }

        LOG.warn("Spam handler could not be registered for name '{}. Another spam handler has already been registered for the same name.", registrationName);
        context.ungetService(reference);
        return null;
    }

    @Override
    public void modifiedService(final ServiceReference<SpamHandler> reference, final SpamHandler service) {
        // Ignore
    }

    @Override
    public void removedService(final ServiceReference<SpamHandler> reference, final SpamHandler service) {
        try {
            SpamHandlerRegistry.unregisterSpamHandler(service);
        } finally {
            context.ungetService(reference);
        }
    }

}
