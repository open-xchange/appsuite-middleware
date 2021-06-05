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

package com.openexchange.messaging.sms.osgi;

import static com.openexchange.messaging.sms.osgi.MessagingSMSServiceRegistry.getServiceRegistry;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.http.HttpService;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.groupware.settings.PreferencesItemService;
import com.openexchange.messaging.sms.impl.SMSPreferencesItem;
import com.openexchange.messaging.sms.service.MessagingNewService;
import com.openexchange.osgi.DeferredActivator;
import com.openexchange.osgi.ServiceRegistry;

/**
 * @author Benjamin Otterbach
 *
 */
public class Activator extends DeferredActivator {

    private static transient final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Activator.class);

    private ServletRegisterer servletRegisterer;
    private ServiceRegistration<PreferencesItemService> serviceRegistration;

    /**
     * Initializes a new {@link Activator}.
     */
    public Activator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { HttpService.class, MessagingNewService.class, DispatcherPrefixService.class };
    }

    @Override
    protected synchronized void handleAvailability(final Class<?> clazz) {
        LOG.warn("Absent service: {}", clazz.getName());
        getServiceRegistry().addService(clazz, getService(clazz));
        if (HttpService.class.equals(clazz)) {
            servletRegisterer.registerServlet();
        }
    }

    @Override
    protected synchronized void handleUnavailability(final Class<?> clazz) {
        LOG.info("Re-available service: {}", clazz.getName());
        if (HttpService.class.equals(clazz)) {
            servletRegisterer.unregisterServlet();
        }
        getServiceRegistry().removeService(clazz);
    }

    @Override
    protected synchronized void startBundle() throws Exception {
        try {
            {
                final ServiceRegistry registry = getServiceRegistry();
                registry.clearRegistry();
                final Class<?>[] classes = getNeededServices();
                for (int i = 0; i < classes.length; i++) {
                    final Object service = getService(classes[i]);
                    if (null != service) {
                         registry.addService(classes[i], service);
                    }
                }
            }
            ServletRegisterer.PREFIX.set(getService(DispatcherPrefixService.class));
            final ServletRegisterer servletRegisterer = new ServletRegisterer();
            this.servletRegisterer = servletRegisterer;
            servletRegisterer.registerServlet();

            serviceRegistration = context.registerService(PreferencesItemService.class, new SMSPreferencesItem(), null);
        } catch (Throwable t) {
            LOG.error("", t);
            throw t instanceof Exception ? (Exception) t : new Exception(t);
        }
    }

    @Override
    protected synchronized void stopBundle() throws Exception {
        try {
            final ServiceRegistration<PreferencesItemService> serviceRegistration = this.serviceRegistration;
            if (null != serviceRegistration) {
                serviceRegistration.unregister();
                this.serviceRegistration = null;
            }

            final ServletRegisterer servletRegisterer = this.servletRegisterer;
            if (null != servletRegisterer) {
                servletRegisterer.unregisterServlet();
                this.servletRegisterer = null;
            }
            getServiceRegistry().clearRegistry();
            ServletRegisterer.PREFIX.set(null);
        } catch (Throwable t) {
            LOG.error("", t);
            throw t instanceof Exception ? (Exception) t : new Exception(t);
        }
    }
}
