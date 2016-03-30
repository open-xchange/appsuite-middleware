/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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

    /**
     * The tracker instance.
     */
    private ServiceTracker<MessagingService,MessagingService> tracker;

    private ServiceTracker<ConfigViewFactory,ConfigViewFactory> configTracker;

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
        if (null == tracker) {
            tracker = new ServiceTracker<MessagingService,MessagingService>(context, MessagingService.class, new Customizer(context));
            tracker.open();
        }
        if (null == configTracker) {
            configTracker = new ServiceTracker<ConfigViewFactory,ConfigViewFactory>(context, ConfigViewFactory.class, null);
            configTracker.open();
        }
    }

    /**
     * Stops the tracker.
     */
    public void stop() {
        if (null != tracker) {
            tracker.close();
            tracker = null;
        }
        if (null != configTracker) {
            configTracker.close();
            configTracker = null;
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
        } catch (final OXException e) {
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
        } catch (final OXException x) {
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
        } catch (final OXException e) {
            throw e;
        }
        return false;
    }

    private ConfigView getView(final int user, final int context) throws OXException {
        final ConfigViewFactory service = configTracker.getService();
        return service.getView(user, context);
    }

    @Override
    public boolean containsMessagingService(final String id, final int user, final int context) {
        try {
            return null == id ? false : (map.containsKey(id) && isAllowed(id, user, context));
        } catch (final OXException e) {
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
