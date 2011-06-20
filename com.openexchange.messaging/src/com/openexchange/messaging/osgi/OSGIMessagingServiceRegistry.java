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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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
import com.openexchange.config.cascade.ConfigCascadeException;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.messaging.MessagingException;
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
    private ServiceTracker tracker;

    private ServiceTracker configTracker;
    
    /**
     * Initializes a new {@link OSGIMessagingServiceRegistry}.
     */
    public OSGIMessagingServiceRegistry() {
        super();
        map = new ConcurrentHashMap<String, MessagingService>(8);
    }

    /**
     * Starts the tracker.
     * 
     * @param context The bundle context
     */
    public void start(final BundleContext context) {
        if (null == tracker) {
            tracker = new ServiceTracker(context, MessagingService.class.getName(), new Customizer(context));
            tracker.open();
        }
        if (null == configTracker) {
            configTracker = new ServiceTracker(context, ConfigViewFactory.class.getName(), null);
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

    public List<MessagingService> getAllServices(int user, int context) throws MessagingException {
        return filter(new ArrayList<MessagingService>(map.values()), user, context);
    }


    public MessagingService getMessagingService(final String id, int user, int context) throws MessagingException {
        final MessagingService messagingService = map.get(id);
        if (null == messagingService || ! isAllowed(id, user, context)) {
            throw MessagingExceptionCodes.UNKNOWN_MESSAGING_SERVICE.create(id);
        }
        return messagingService;
    }

    private boolean isAllowed(String id, int user, int context) throws MessagingException {
        if (user == -1 && context == -1) {
            return true; // Quite the hack
        }
        try {
            ConfigView configView = getView(user, context);
            if ( !isMessagingEnabled(configView)) {
                return false;
            }
            ComposedConfigProperty<Boolean> configProperty = configView.property(id, boolean.class);
            return (configProperty.isDefined() && configProperty.get());
        } catch (ConfigCascadeException e) {
            throw new MessagingException(e);
        }        
    }

    private List<MessagingService> filter(ArrayList<MessagingService> arrayList, int user, int context) throws MessagingException {
        List<MessagingService> filteredList = new ArrayList<MessagingService>(arrayList.size());
        try {
            ConfigView configView = getView(user, context);
            if (!isMessagingEnabled(configView)) {
                return Collections.emptyList();
            }
            for (MessagingService messagingService : arrayList) {
                ComposedConfigProperty<Boolean> configProperty = configView.property(messagingService.getId(), boolean.class);
                if (configProperty.isDefined() && configProperty.get()) {
                    filteredList.add(messagingService);
                }
            }
        } catch (ConfigCascadeException x) {
            throw new MessagingException(x);
        }
        
        return filteredList;
    }

    private boolean isMessagingEnabled(ConfigView configView) throws MessagingException {
        try {
            ComposedConfigProperty<Boolean> property = configView.property("com.openexchange.messaging.enabled", boolean.class);
            if (property.isDefined() && property.get()) {
                return true;
            }
        } catch (ConfigCascadeException e) {
            throw new MessagingException(e);
        }
        return false;
    }

    private ConfigView getView(int user, int context) throws ConfigCascadeException {
        ConfigViewFactory service = (ConfigViewFactory) configTracker.getService();
        return service.getView(user, context);
    }

    public boolean containsMessagingService(final String id, int user, int context) {
        try {
            return null == id ? false : (map.containsKey(id) && isAllowed(id, user, context));
        } catch (MessagingException e) {
            return false;
        }
    }

    private final class Customizer implements ServiceTrackerCustomizer {

        private final BundleContext context;

        Customizer(final BundleContext context) {
            super();
            this.context = context;
        }

        public Object addingService(final ServiceReference reference) {
            final Object service = context.getService(reference);
            if ((service instanceof MessagingService)) {
                final MessagingService addMe = (MessagingService) service;
                if (null == map.putIfAbsent(addMe.getId(), addMe)) {
                    return service;
                }
                final org.apache.commons.logging.Log logger =
                    org.apache.commons.logging.LogFactory.getLog(OSGIMessagingServiceRegistry.Customizer.class);
                if (logger.isWarnEnabled()) {
                    logger.warn(new StringBuilder(128).append("Messaging service ").append(addMe.getDisplayName()).append(
                        " could not be added to registry. Another service is already registered with identifier: ").append(addMe.getId()).toString());
                }
            }
            /*
             * Adding to registry failed
             */
            context.ungetService(reference);
            return null;
        }

        public void modifiedService(final ServiceReference reference, final Object service) {
            // Nothing to do
        }

        public void removedService(final ServiceReference reference, final Object service) {
            if (null != service) {
                try {
                    if (service instanceof MessagingService) {
                        final MessagingService removeMe = (MessagingService) service;
                        map.remove(removeMe.getId());
                    }
                } finally {
                    context.ungetService(reference);
                }
            }
        }
    } // End of Customizer class

    
}
