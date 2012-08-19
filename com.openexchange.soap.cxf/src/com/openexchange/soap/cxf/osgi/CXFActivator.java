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

import javax.servlet.ServletException;
import org.apache.commons.logging.Log;
import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.transport.servlet.CXFNonSpringServlet;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.osgi.HousekeepingActivator;

/**
 * {@link CXFActivator} - The activator for CXF bundle.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class CXFActivator extends HousekeepingActivator {

    /**
     * The logger.
     */
    static final Log LOG = com.openexchange.log.Log.loggerFor(CXFActivator.class);

    /**
     * The CXF Servlet's alias.
     */
    static final String ALIAS = "/webservices";

    /**
     * The Axis2 Servlet's alias.
     */
    static final String ALIAS2 = "/servlet/axis2/services";

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] { HttpService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        try {
            LOG.info("Starting Bundle: com.openexchange.soap.cxf");
            final BundleContext context = this.context;
            final ServiceTrackerCustomizer<HttpService, HttpService> trackerCustomizer =
                new ServiceTrackerCustomizer<HttpService, HttpService>() {

                    private volatile WebserviceCollector collector;

                    @Override
                    public void removedService(final ServiceReference<HttpService> reference, final HttpService service) {
                        final HttpService httpService = getService(HttpService.class);
                        if (httpService != null) {
                            try {
                                httpService.unregister(ALIAS);
                                httpService.unregister(ALIAS2);
                            } catch (final Exception e) {
                                // Ignore
                            }
                        }
                        final WebserviceCollector collector = this.collector;
                        if (null != collector) {
                            try {
                                collector.close();
                            } catch (final Exception e) {
                                // Ignore
                            }
                            this.collector = null;
                        }
                        context.ungetService(reference);
                    }

                    @Override
                    public void modifiedService(final ServiceReference<HttpService> reference, final HttpService service) {
                        // Ignore
                    }

                    @Override
                    public HttpService addingService(final ServiceReference<HttpService> reference) {
                        final HttpService httpService = context.getService(reference);
                        boolean servletRegistered = false;
                        boolean collectorOpened = false;
                        try {
                            /*
                             * Create CXF Servlet
                             */
                            final CXFNonSpringServlet cxf = new CXFNonSpringServlet();
                            final Bus bus = cxf.getBus();
                            /*
                             * Add interceptors here
                             */
                            bus.getInInterceptors().add(new RemoveGenericLabelledElementsInterceptor());
                            /*
                             * Set properties
                             */
                            bus.setProperty("set-jaxb-validation-event-handler", Boolean.FALSE);
                            /*
                             * Apply as default bus
                             */
                            BusFactory.setDefaultBus(bus);
                            /*
                             * Register CXF Servlet
                             */
                            httpService.registerServlet(ALIAS, cxf, null, null);
                            LOG.info("Registered CXF Servlet under: " + ALIAS);
                            httpService.registerServlet(ALIAS2, cxf, null, null);
                            LOG.info("Registered CXF Servlet under: " + ALIAS2);
                            servletRegistered = true;
                            /*
                             * Initialize Webservice collector
                             */
                            final WebserviceCollector collector = new WebserviceCollector(context);
                            context.addServiceListener(collector);
                            collector.open();
                            this.collector = collector;
                            collectorOpened = true;
                            LOG.info("CXF SOAP service is up and running");
                            /*
                             * Return tracked HTTP service
                             */
                            return httpService;
                        } catch (final ServletException e) {
                            LOG.error("Couldn't register CXF Servlet: " + e.getMessage(), e);
                        } catch (final NamespaceException e) {
                            LOG.error("Couldn't register CXF Servlet: " + e.getMessage(), e);
                        } catch (final RuntimeException e) {
                            if (servletRegistered) {
                                try {
                                    httpService.unregister(ALIAS);
                                    httpService.unregister(ALIAS2);
                                } catch (final Exception e1) {
                                    // Ignore
                                }
                            }
                            if (collectorOpened) {
                                final WebserviceCollector collector = this.collector;
                                if (null != collector) {
                                    try {
                                        collector.close();
                                    } catch (final Exception e1) {
                                        // Ignore
                                    }
                                    this.collector = null;
                                }
                            }
                            LOG.error("Couldn't register CXF Servlet: " + e.getMessage(), e);
                        }
                        context.ungetService(reference);
                        return null;
                    }
                };
                track(HttpService.class, trackerCustomizer);
                openTrackers();
        } catch (final Exception e) {
            LOG.error(e.getMessage(), e);
            throw e;
        }
    }

}
