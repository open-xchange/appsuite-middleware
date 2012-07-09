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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.http.grizzly.osgi;

import java.io.IOException;
import javax.servlet.ServletException;
import org.glassfish.grizzly.comet.CometAddOn;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.glassfish.grizzly.websockets.WebSocketAddOn;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.http.atmosphere.AtmosphereService;
import com.openexchange.http.grizzly.GrizzlyExceptionCode;
import com.openexchange.http.grizzly.addons.GrizzlOXAddOn;
import com.openexchange.http.grizzly.services.http.HttpServiceFactory;
import com.openexchange.http.requestwatcher.osgi.services.RequestWatcherService;
import com.openexchange.log.Log;
import com.openexchange.log.LogFactory;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.server.ServiceExceptionCode;

/**
 * {@link GrizzlyActivator}
 * 
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class GrizzlyActivator extends HousekeepingActivator {

    private static final org.apache.commons.logging.Log LOG = Log.valueOf(LogFactory.getLog(GrizzlyActivator.class));

    private HttpServer grizzly;

    private HttpServiceFactory serviceFactory;

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] { ConfigurationService.class, RequestWatcherService.class, AtmosphereService.class };
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.osgi.HousekeepingActivator#handleAvailability(java.lang.Class)
     */
    @Override
    protected void handleAvailability(Class<?> clazz) {
        if (LOG.isInfoEnabled()) {
            LOG.info("Service " + clazz.getName() + " is available again.");
        }
        Object service = getService(clazz);
        GrizzlyServiceRegistry.getInstance().addService(clazz, service);
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.osgi.HousekeepingActivator#handleUnavailability(java.lang.Class)
     */
    @Override
    protected void handleUnavailability(Class<?> clazz) {
        if (LOG.isInfoEnabled()) {
            LOG.info("Service " + clazz.getName() + " is no longer available.");
        }
        GrizzlyServiceRegistry.getInstance().removeService(clazz);
    }

    @Override
    protected void startBundle() throws OXException, ServletException, NamespaceException {
        try {
            if (LOG.isInfoEnabled()) {
                LOG.info("Starting Grizzly server.");
            }
            context.addFrameworkListener(new FrameworkListener() {
                
                @Override
                public void frameworkEvent(FrameworkEvent event) {
                    if(event.getBundle().getSymbolicName().equalsIgnoreCase("com.openexchange.http.grizzly")) {
                        int eventType = event.getType();
                        if(eventType == FrameworkEvent.ERROR) {
                            LOG.error(event.toString(), event.getThrowable());
                        } else {
                            LOG.info(event.toString(), event.getThrowable());
                        }
                    }
                }
                });
            
            GrizzlyServiceRegistry grizzlyServiceRegistry = GrizzlyServiceRegistry.getInstance();
            
            /*
             * initialize the registry, handleUn/Availability keeps track of services.
             * Otherwise use trackService(ConfigurationService.class) and openTrackers() to let the superclass handle the services.
             */
            initializeServiceRegistry(grizzlyServiceRegistry);

            // create addons based on given configuration
            final ConfigurationService configService = grizzlyServiceRegistry.getService(ConfigurationService.class);
            if (configService == null) {
                throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(ConfigurationService.class.getName());
            }

            /*
             *  read config properties
             */
            final String httpHost = configService.getProperty("com.openexchange.http.grizzly.httpNetworkListenerHost", "0.0.0.0");
            final int httpPort = configService.getIntProperty("com.openexchange.http.grizzly.httpNetworkListenerPort", 8080);
            final boolean hasJMXEnabled = configService.getBoolProperty("com.openexchange.http.grizzly.hasJMXEnabled", false);
            final boolean hasWebsocketsEnabled = configService.getBoolProperty("com.openexchange.http.grizzly.hasWebSocketsEnabled", false);
            final boolean hasCometEnabled = configService.getBoolProperty("com.openexchange.http.grizzly.hasCometEnabled", false);

            /*
             *  create, configure and start server
             */
            grizzly = new HttpServer();
            final NetworkListener networkListener = new NetworkListener("http-listener", httpHost, 8080);
            
            if (hasJMXEnabled) {
                if (LOG.isInfoEnabled()) {
                    LOG.info("Enabling JMX for Grizzly server.");
                }
                grizzly.getServerConfiguration().setJmxEnabled(true);
            }
            
            if (hasWebsocketsEnabled) {
                if (LOG.isInfoEnabled()) {
                    LOG.info("Enabling WebSockets for Grizzly server.");
                }
                WebSocketAddOn webSocketAddOn = new WebSocketAddOn();
                networkListener.registerAddOn(new WebSocketAddOn());
            }
            
            if (hasCometEnabled) {
                if (LOG.isInfoEnabled()) {
                    LOG.info("Enabling Comet for Grizzly server.");
                }
                networkListener.registerAddOn(new CometAddOn());
            }

            if (LOG.isInfoEnabled()) {
                LOG.info("Enabling BackendRouteAddon for Grizzly server.");
            }
            networkListener.registerAddOn(new GrizzlOXAddOn());
            
            if (LOG.isInfoEnabled()) {
                LOG.info(String.format(
                    "Registering Grizzly HttpNetworkListener on host: %s and port: %d",
                    httpHost,
                    Integer.valueOf(httpPort)));
            }
            
            grizzly.addListener(networkListener);

            /*
             * Servicefactory that creates instances of the HttpService interface that grizzly implements. Each distinct bundle that uses
             * getService() will get its own instance of HttpServiceImpl
             */
            serviceFactory = new HttpServiceFactory(grizzly, context.getBundle());

            registerService(HttpService.class.getName(), serviceFactory);
            
//            AtmosphereServiceImpl atmosphereServiceImpl = new AtmosphereServiceImpl(context.getBundle());
//            registerService(AtmosphereServiceImpl.class, atmosphereServiceImpl);
         
            grizzly.start();

            
            
        } catch (final IOException e) {
            throw GrizzlyExceptionCode.GRIZZLY_SERVER_NOT_STARTED.create(e, new Object[] {});
        }

    }

    @Override
    protected void stopBundle() throws Exception {
        /*
         * Clear the registry from the services we are tracking.
         * Otherwise use super.stopBundle(); if we let the superclass handle the services.
         */
        GrizzlyServiceRegistry.getInstance().clearRegistry();
        
        if (LOG.isInfoEnabled()) {
            LOG.info("Unregistering HttpService");
        }
        unregisterServices();
        if (LOG.isInfoEnabled()) {
            LOG.info("Stopping Grizzly OSGi HttpService");
        }
        grizzly.stop();
    }

    /**
     * Initialize the package wide service registry with the services we declared as needed.
     * @param serviceRegistry the registry to fill
     */
    private void initializeServiceRegistry(final GrizzlyServiceRegistry serviceRegistry) {
        serviceRegistry.clearRegistry();
        Class<?>[] serviceClasses = getNeededServices();
        for (Class<?> serviceClass : serviceClasses) {
            Object service = getService(serviceClass);
            if (service != null) {
                serviceRegistry.addService(serviceClass, service);
            }
        }
    }

}
