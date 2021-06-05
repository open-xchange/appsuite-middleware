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

package com.openexchange.proxy.servlet.osgi;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.util.tracker.ServiceTracker;
import com.openexchange.ajax.osgi.AbstractSessionServletActivator;
import com.openexchange.net.ssl.SSLSocketFactoryProvider;
import com.openexchange.osgi.RegistryServiceTrackerCustomizer;
import com.openexchange.proxy.ProxyRegistry;
import com.openexchange.proxy.servlet.Constants;
import com.openexchange.proxy.servlet.ProxyEventHandler;
import com.openexchange.proxy.servlet.ProxyRegistryImpl;
import com.openexchange.proxy.servlet.ProxyServlet;
import com.openexchange.proxy.servlet.http.ProxyHttpClientConfiguration;
import com.openexchange.proxy.servlet.services.ServiceRegistry;
import com.openexchange.rest.client.httpclient.SpecificHttpClientConfigProvider;
import com.openexchange.rest.client.httpclient.HttpClientService;
import com.openexchange.sessiond.SessiondEventConstants;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.timer.TimerService;

/**
 * {@link ProxyServletActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ProxyServletActivator extends AbstractSessionServletActivator {

    private List<ServiceTracker<?,?>> trackers;

    private List<ServiceRegistration<?>> registrations;
    
    @Override
    public void startBundle() throws Exception {
        final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ProxyServletActivator.class);
        try {
            log.info("starting bundle: com.openexchange.proxy.servlet");
            
            Services.setServiceLookup(this);

            registerSessionServlet(Constants.PATH, new ProxyServlet());
            registerService(SpecificHttpClientConfigProvider.class, new ProxyHttpClientConfiguration());

            trackers = new ArrayList<ServiceTracker<?,?>>(4);
            trackers.add(new ServiceTracker<TimerService,TimerService>(context, TimerService.class, new TimerServiceCustomizer(context)));
            trackers.add(new ServiceTracker<SessiondService,SessiondService>(context, SessiondService.class, new RegistryServiceTrackerCustomizer<SessiondService>(context, ServiceRegistry.getInstance(), SessiondService.class)));
            trackers.add(new ServiceTracker<SSLSocketFactoryProvider,SSLSocketFactoryProvider>(context, SSLSocketFactoryProvider.class, new RegistryServiceTrackerCustomizer<SSLSocketFactoryProvider>(context, ServiceRegistry.getInstance(), SSLSocketFactoryProvider.class)));
            for (final ServiceTracker<?,?> serviceTracker : trackers) {
                serviceTracker.open();
            }

            registrations = new ArrayList<ServiceRegistration<?>>(2);
            /*
             * Register proxy registry
             */
            registrations.add(context.registerService(ProxyRegistry.class, ProxyRegistryImpl.getInstance(), null));
            /*
             * Register event handler to detect removed sessions
             */
            final Dictionary<String, Object> serviceProperties = new Hashtable<String, Object>(1);
            serviceProperties.put(EventConstants.EVENT_TOPIC, SessiondEventConstants.getAllTopics());
            registrations.add(context.registerService(EventHandler.class, new ProxyEventHandler(), serviceProperties));
        } catch (Exception e) {
            log.error("Failed start-up of bundle com.openexchange.proxy.servlet", e);
            throw e;
        }
    }

    @Override
    public void stopBundle() throws Exception {
        final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ProxyServletActivator.class);
        Services.setServiceLookup(null);
        try {
            log.info("stopping bundle: com.openexchange.proxy.servlet");
            if (null != trackers) {
                for (final ServiceTracker<?,?> serviceTracker : trackers) {
                    serviceTracker.close();
                }
                trackers = null;
            }
            if (null != registrations) {
                while (!registrations.isEmpty()) {
                    registrations.remove(0).unregister();
                }
                registrations = null;
            }
        } catch (Exception e) {
            log.error("Failed shut-down of bundle com.openexchange.proxy.servlet", e);
            throw e;
        }
    }

    @Override
    protected Class<?>[] getAdditionalNeededServices() {
        return new Class<?>[] { HttpClientService.class };
    }

}
