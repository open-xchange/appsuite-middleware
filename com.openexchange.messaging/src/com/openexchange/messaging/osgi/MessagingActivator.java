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

package com.openexchange.messaging.osgi;

import com.openexchange.messaging.registry.MessagingServiceRegistry;
import com.openexchange.osgi.HousekeepingActivator;

/**
 * {@link MessagingActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.16
 */
public final class MessagingActivator extends HousekeepingActivator {

    private OSGIMessagingServiceRegistry registry;

    /**
     * Initializes a new {@link MessagingActivator}.
     */
    public MessagingActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return EMPTY_CLASSES;
    }

    @Override
    protected void startBundle() throws Exception {
        final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MessagingActivator.class);
        try {
            log.info("starting bundle: com.openexchange.messaging");
            /*
             * Start registry tracking
             */
            registry = new OSGIMessagingServiceRegistry();
            registry.start(context);
            /*
             * Register services
             */
            registerService(MessagingServiceRegistry.class, registry, null);
        } catch (Exception e) {
            log.error("", e);
            throw e;
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MessagingActivator.class);
        try {
            log.info("stopping bundle: com.openexchange.messaging");
            unregisterServices();
            /*
             * Stop registry
             */
            if (null != registry) {
                registry.stop();
                registry = null;
            }
        } catch (Exception e) {
            log.error("", e);
            throw e;
        }
    }

}
