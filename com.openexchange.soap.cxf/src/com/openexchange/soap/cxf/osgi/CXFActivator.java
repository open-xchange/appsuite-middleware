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

package com.openexchange.soap.cxf.osgi;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import javax.servlet.ServletException;
import org.apache.commons.logging.Log;
import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.transport.servlet.CXFNonSpringServlet;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.groupware.notify.hostname.HostnameService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.soap.cxf.interceptor.TransformGenericElementsInterceptor;
import com.openexchange.soap.cxf.logger.CommonsLoggingLogger;
import com.openexchange.soap.cxf.servlet.BoundedCXFNonSpringServlet;

/**
 * {@link CXFActivator} - The activator for CXF bundle.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class CXFActivator extends HousekeepingActivator {

    /** The host name service */
    final AtomicReference<HostnameService> hostnameServiceRef;

    /** The HTTP service */
    final AtomicReference<HttpService> httpServiceRef;

    /** The WebserviceCollector */
    final AtomicReference<WebserviceCollector> collectorRef;

    /** The mutex */
    final Object mutex;

    /** The registered aliases */
    final Map<String, Object> registeredAliases;

    /**
     * Initializes a new {@link CXFActivator}.
     */
    public CXFActivator() {
        super();
        hostnameServiceRef = new AtomicReference<HostnameService>();
        httpServiceRef = new AtomicReference<HttpService>();
        collectorRef = new AtomicReference<WebserviceCollector>();
        registeredAliases = new HashMap<String, Object>(4);
        mutex = new Object();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] { HttpService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        final Log log = com.openexchange.log.Log.loggerFor(CXFActivator.class);
        try {
            log.info("Starting Bundle: com.openexchange.soap.cxf");
            LogUtils.setLoggerClass(CommonsLoggingLogger.class);
            final BundleContext context = this.context;
            final Set<String> aliases = new HashSet<String>(4);
            aliases.add("/webservices");
            aliases.add("/servlet/axis2/services");
            aliases.add("/axis2");
            /*
             * Initialize ServiceTrackerCustomizer
             */
            final ServiceTrackerCustomizer<HttpService, HttpService> trackerCustomizer =
                new ServiceTrackerCustomizer<HttpService, HttpService>() {

                    @Override
                    public void removedService(final ServiceReference<HttpService> reference, final HttpService service) {
                        final HttpService httpService = getService(HttpService.class);
                        synchronized (mutex) {
                            unregister(httpService, aliases, log);
                        }
                        context.ungetService(reference);
                        httpServiceRef.set(null);
                    }

                    @Override
                    public void modifiedService(final ServiceReference<HttpService> reference, final HttpService service) {
                        // Ignore
                    }

                    @Override
                    public HttpService addingService(final ServiceReference<HttpService> reference) {
                        final HttpService httpService = context.getService(reference);
                        synchronized (mutex) {
                            if (register(httpService, aliases, log)) {
                                httpServiceRef.set(httpService);
                                return httpService;
                            }
                        }
                        context.ungetService(reference);
                        httpServiceRef.set(null);
                        return null;
                    }
                };
                track(HttpService.class, trackerCustomizer);
                track(HostnameService.class, new ServiceTrackerCustomizer<HostnameService, HostnameService>() {

                    @Override
                    public HostnameService addingService(ServiceReference<HostnameService> reference) {
                        final HostnameService service = context.getService(reference);
                        if (hostnameServiceRef.compareAndSet(null, service)) {
                            final HttpService httpService = httpServiceRef.get();
                            if (null != httpService) {
                                synchronized (mutex) {
                                    unregister(httpService, aliases, log);
                                    register(httpService, aliases, log);
                                }
                            }
                        }
                        return service;
                    }

                    @Override
                    public void modifiedService(ServiceReference<HostnameService> reference, HostnameService service) {
                        // Ignore
                    }

                    @Override
                    public void removedService(ServiceReference<HostnameService> reference, HostnameService service) {
                        if (hostnameServiceRef.compareAndSet(service, null)) {
                            final HttpService httpService = httpServiceRef.get();
                            if (null != httpService) {
                                synchronized (mutex) {
                                    unregister(httpService, aliases, log);
                                    register(httpService, aliases, log);
                                }
                            }
                        }
                        context.ungetService(reference);
                    }
                });
                openTrackers();
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    boolean register(final HttpService httpService, final Collection<String> aliases, final Log log) {
        boolean servletRegistered = false;
        boolean collectorOpened = false;
        try {
            // Set logger
            System.setProperty("org.apache.cxf.Logger", "com.openexchange.soap.cxf.logger.CommonsLoggingLogger");
            // Create Servlet instance
            final CXFNonSpringServlet cxfServlet;
            {
                final HostnameService hostnameService = hostnameServiceRef.get();
                if (null == hostnameService) {
                    cxfServlet = new CXFNonSpringServlet();
                } else {
                    cxfServlet = new BoundedCXFNonSpringServlet(hostnameService);
                }
            }
            /*
             * Register CXF Servlet
             */
            for (final String alias : aliases) {
                if (!registeredAliases.containsKey(alias)) {
                    httpService.registerServlet(alias, cxfServlet, null, null);
                    registeredAliases.put(alias, mutex);
                    log.info("Registered CXF Servlet under: " + alias);
                }
            }
            servletRegistered = true;
            /*
             * Get CXF bus
             */
            Bus bus = cxfServlet.getBus();
            if (null == bus) {
                bus = BusFactory.newInstance().createBus();
                cxfServlet.setBus(bus);
            }
            /*
             * Add interceptors here
             */
            bus.getInInterceptors().add(new TransformGenericElementsInterceptor());
            /*
             * Apply as default bus
             */
            BusFactory.setDefaultBus(bus);
            /*
             * Initialize Webservice collector
             */
            final WebserviceCollector collector = new WebserviceCollector(context);
            context.addServiceListener(collector);
            collector.open();
            collectorRef.set(collector);
            collectorOpened = true;
            log.info("CXF SOAP service is up and running");
            return true;
        } catch (final ServletException e) {
            log.error("Couldn't register CXF Servlet: " + e.getMessage(), e);
        } catch (final NamespaceException e) {
            log.error("Couldn't register CXF Servlet: " + e.getMessage(), e);
        } catch (final RuntimeException e) {
            if (servletRegistered) {
                for (final String alias : aliases) {
                    try {
                        httpService.unregister(alias);
                    } catch (final Exception e1) {
                        // Ignore
                    }
                }
            }
            if (collectorOpened) {
                final WebserviceCollector collector = collectorRef.get();
                if (null != collector) {
                    try {
                        collector.close();
                    } catch (final Exception e1) {
                        // Ignore
                    }
                    collectorRef.set(null);
                }
            }
            log.error("Couldn't register CXF Servlet: " + e.getMessage(), e);
        }
        return false;
    }

    void unregister(final HttpService httpService, final Collection<String> aliases, final Log log) {
        if (httpService != null) {
            for (final String alias : aliases) {
                if (registeredAliases.containsKey(alias)) {
                    try {
                        httpService.unregister(alias);
                    } catch (final Exception e) {
                        // Ignore
                    }
                    registeredAliases.remove(alias);
                }
            }
        }
        final WebserviceCollector collector = collectorRef.get();
        if (null != collector) {
            try {
                collector.close();
            } catch (final Exception e) {
                // Ignore
            }
            collectorRef.set(null);
        }
        log.info("CXF SOAP service stopped");
    }

}
