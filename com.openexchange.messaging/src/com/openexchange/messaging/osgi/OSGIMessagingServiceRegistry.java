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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.config.cascade.ComposedConfigProperty;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.messaging.MessagingExceptionCodes;
import com.openexchange.messaging.MessagingService;
import com.openexchange.messaging.registry.MessagingServiceRegistry;
import com.openexchange.server.ServiceExceptionCode;

/**
 * {@link OSGIMessagingServiceRegistry}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.16
 */
public class OSGIMessagingServiceRegistry implements MessagingServiceRegistry {

    /**
     * The backing map.
     */
    final ConcurrentMap<String, MessagingService> map;

    private volatile ServiceTracker<MessagingService,MessagingService> tracker;
    private volatile ServiceTracker<ConfigViewFactory,ConfigViewFactory> configTracker;

    /**
     * Initializes a new {@link OSGIMessagingServiceRegistry}.
     */
    public OSGIMessagingServiceRegistry() {
        super();
        map = new ConcurrentHashMap<String, MessagingService>(8, 0.9f, 1);
    }

    /**
     * Starts the tracker.
     *
     * @param context The bundle context
     */
    public void start(final BundleContext context) {
        ServiceTracker<MessagingService,MessagingService> tracker = this.tracker;
        if (null == tracker) {
            tracker = new ServiceTracker<MessagingService,MessagingService>(context, MessagingService.class, new Customizer(context));
            tracker.open();
            this.tracker = tracker;
        }

        ServiceTracker<ConfigViewFactory,ConfigViewFactory> configTracker = this.configTracker;
        if (null == configTracker) {
            configTracker = new ServiceTracker<ConfigViewFactory,ConfigViewFactory>(context, ConfigViewFactory.class, null);
            configTracker.open();
            this.configTracker = configTracker;
        }
    }

    /**
     * Stops the tracker.
     */
    public void stop() {
        ServiceTracker<MessagingService,MessagingService> tracker = this.tracker;
        if (null != tracker) {
            tracker.close();
            this.tracker = null;
        }

        ServiceTracker<ConfigViewFactory,ConfigViewFactory> configTracker = this.configTracker;
        if (null != configTracker) {
            configTracker.close();
            this.configTracker = null;
        }
    }

    @Override
    public List<MessagingService> getAllServices(final int user, final int context) throws OXException {
        return filter(new ArrayList<MessagingService>(map.values()), user, context);
    }


    @Override
    public MessagingService getMessagingService(final String id, final int user, final int context) throws OXException {
        final MessagingService messagingService = map.get(id);
        if (null == messagingService || ! isAllowed(id, user, context)) {
            throw MessagingExceptionCodes.UNKNOWN_MESSAGING_SERVICE.create(id);
        }
        return messagingService;
    }

    private boolean isAllowed(final String id, final int user, final int context) throws OXException {
        if (user == -1 && context == -1) {
            return true; // Quite the hack
        }
        try {
            final ConfigView configView = getView(user, context);
            if ( !isMessagingEnabled(configView)) {
                return false;
            }
            final ComposedConfigProperty<Boolean> configProperty = configView.property(id, boolean.class);
            return (!configProperty.isDefined() || configProperty.get().booleanValue());
        } catch (OXException e) {
            throw e;
        }
    }

    private List<MessagingService> filter(final List<MessagingService> arrayList, final int user, final int context) throws OXException {
        final List<MessagingService> filteredList = new ArrayList<MessagingService>(arrayList.size());
        try {
            final ConfigView configView = getView(user, context);
            if (!isMessagingEnabled(configView)) {
                return Collections.emptyList();
            }
            for (final MessagingService messagingService : arrayList) {
                final ComposedConfigProperty<Boolean> configProperty = configView.property(messagingService.getId(), boolean.class);
                if (!configProperty.isDefined() || configProperty.get().booleanValue()) {
                    filteredList.add(messagingService);
                }
            }
        } catch (OXException x) {
            throw x;
        }

        return filteredList;
    }

    private boolean isMessagingEnabled(final ConfigView configView) throws OXException {
        try {
            final ComposedConfigProperty<Boolean> configProperty = configView.property("com.openexchange.messaging.enabled", boolean.class);
            if (!configProperty.isDefined() || configProperty.get().booleanValue()) {
                return true;
            }
        } catch (OXException e) {
            throw e;
        }
        return false;
    }

    private ConfigView getView(final int user, final int context) throws OXException {
        ServiceTracker<ConfigViewFactory,ConfigViewFactory> configTracker = this.configTracker;
        if (null == configTracker) {
            throw ServiceExceptionCode.absentService(ConfigViewFactory.class);
        }

        ConfigViewFactory service = configTracker.getService();
        if (null == service) {
            throw ServiceExceptionCode.absentService(ConfigViewFactory.class);
        }
        return service.getView(user, context);
    }

    @Override
    public boolean containsMessagingService(final String id, final int user, final int context) {
        try {
            return null == id ? false : (map.containsKey(id) && isAllowed(id, user, context));
        } catch (OXException e) {
            return false;
        }
    }

    private final class Customizer implements ServiceTrackerCustomizer<MessagingService,MessagingService> {

        private final BundleContext context;

        Customizer(final BundleContext context) {
            super();
            this.context = context;
        }

        @Override
        public MessagingService addingService(final ServiceReference<MessagingService> reference) {
            final MessagingService service = context.getService(reference);
            {
                final MessagingService addMe = service;
                if (null == map.putIfAbsent(addMe.getId(), addMe)) {
                    return service;
                }
                final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(OSGIMessagingServiceRegistry.Customizer.class);
                logger.warn("Messaging service {} could not be added to registry. Another service is already registered with identifier: {}", addMe.getDisplayName(), addMe.getId());
            }
            /*
             * Adding to registry failed
             */
            context.ungetService(reference);
            return null;
        }

        @Override
        public void modifiedService(final ServiceReference<MessagingService> reference, final MessagingService service) {
            // Nothing to do
        }

        @Override
        public void removedService(final ServiceReference<MessagingService> reference, final MessagingService service) {
            if (null != service) {
                try {
                    {
                        final MessagingService removeMe = service;
                        map.remove(removeMe.getId());
                    }
                } finally {
                    context.ungetService(reference);
                }
            }
        }
    } // End of Customizer class


}
