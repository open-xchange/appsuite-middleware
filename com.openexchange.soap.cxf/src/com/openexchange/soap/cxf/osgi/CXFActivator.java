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

package com.openexchange.soap.cxf.osgi;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Dictionary;
import java.util.Hashtable;
import javax.servlet.ServletException;
import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.staxutils.StaxUtils;
import org.apache.cxf.transport.http.HttpDestinationFactory;
import org.apache.cxf.transport.servlet.CXFNonSpringServlet;
import org.apache.cxf.transport.servlet.ServletDestinationFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.config.ConfigurationService;
import com.openexchange.java.Strings;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.soap.cxf.interceptor.TransformGenericElementsInterceptor;

/**
 * {@link CXFActivator} - The activator for CXF bundle.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class CXFActivator extends HousekeepingActivator {

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] { HttpService.class, ConfigurationService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CXFActivator.class);
        try {
            log.info("Starting Bundle: com.openexchange.soap.cxf");
            // Set jdk.xml.entityExpansionLimit
            {
                final int defaulEntityExpansionLimit = 128000;
                final int entityExpansionLimit = getService(ConfigurationService.class).getIntProperty("com.openexchange.soap.cxf.entityExpansionLimit", defaulEntityExpansionLimit);
                System.setProperty("jdk.xml.entityExpansionLimit", Integer.toString(entityExpansionLimit < 0 ? defaulEntityExpansionLimit : entityExpansionLimit));
            }
            // Set logger class
            //LogUtils.setLoggerClass(com.openexchange.soap.cxf.logger.Slf4jLogger.class);
            LogUtils.setLoggerClass(org.apache.cxf.common.logging.Slf4jLogger.class);
            // Continue start-up
            final BundleContext context = this.context;
            final String alias = "/webservices";
            final String alias2 = "/servlet/axis2/services";
            /*
             * Initialize ServiceTrackerCustomizer
             */
            final ServiceTrackerCustomizer<HttpService, HttpService> trackerCustomizer = new ServiceTrackerCustomizer<HttpService, HttpService>() {

                    private volatile WebserviceCollector collector;
                    private volatile String alias3;

                    @Override
                    public void removedService(final ServiceReference<HttpService> reference, final HttpService service) {
                        final HttpService httpService = service;
                        if (httpService != null) {
                            try {
                                httpService.unregister(alias);
                                httpService.unregister(alias2);
                                String servletAlias = alias3;
                                if (null != servletAlias) {
                                    httpService.unregister(servletAlias);
                                    alias3 = null;
                                }
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
                            System.setProperty(StaxUtils.ALLOW_INSECURE_PARSER, "true");
                            // System.setProperty("org.apache.cxf.servlet.base-address", "http://localhost/foo/");
                            final CXFNonSpringServlet cxfServlet = new CXFNonSpringServlet();
                            /*
                             * Register CXF Servlet
                             */
                            String baseAddress = null;
                            {
                                // Servlet config; see org.apache.cxf.transport.servlet.ServletController.init()
                                final ConfigurationService configService = getService(ConfigurationService.class);
                                Dictionary<String, Object> config = null;
                                if (null != configService) {
                                    baseAddress = configService.getProperty("com.openexchange.soap.cxf.baseAddress");
                                    baseAddress = Strings.isEmpty(baseAddress) ? null : baseAddress.trim();
                                    if (null != baseAddress) {
                                        config = new Hashtable<String, Object>(4);
                                        config.put("base-address", baseAddress);
                                    }
                                    final String hideServiceListPage = configService.getProperty("com.openexchange.soap.cxf.hideServiceListPage");
                                    if (null != hideServiceListPage) {
                                        if (null == config) {
                                            config = new Hashtable<String, Object>(4);
                                        }
                                        config.put("hide-service-list-page", hideServiceListPage.trim());
                                    }
                                    final String disableAddressUpdates = configService.getProperty("com.openexchange.soap.cxf.disableAddressUpdates");
                                    if (disableAddressUpdates != null) {
                                        if (config == null) {
                                            config = new Hashtable<String, Object>(4);
                                        }
                                        config.put("disable-address-updates", disableAddressUpdates);
                                    }
                                }
                                // Registration
                                httpService.registerServlet(alias, cxfServlet, config, null);
                                log.info("Registered CXF Servlet under: {}", alias);
                                httpService.registerServlet(alias2, cxfServlet, config, null);
                                log.info("Registered CXF Servlet under: {}", alias2);
                                if (null != baseAddress) {
                                    try {
                                        URL url = new URL(baseAddress);
                                        String servletAlias = url.getPath();
                                        if (!alias.equals(servletAlias) && !alias2.equals(servletAlias)) {
                                            alias3 = servletAlias;
                                            httpService.registerServlet(servletAlias, cxfServlet, config, null);
                                            log.info("Registered CXF Servlet under: {}", alias2);
                                        }
                                    } catch (MalformedURLException e) {
                                        throw new IllegalStateException("Invalid URL specified in property \"com.openexchange.soap.cxf.baseAddress\": \"" + baseAddress + "\"", e);
                                    }
                                }
                                servletRegistered = true;
                            }
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
                            bus.setExtension(new ServletDestinationFactory(), HttpDestinationFactory.class);
                            /*
                             * Apply as default bus
                             */
                            BusFactory.setDefaultBus(bus);
                            /*
                             * Initialize Webservice collector
                             */
                            baseAddress = null;
                            final WebserviceCollector collector = new WebserviceCollector(baseAddress, context);
                            context.addServiceListener(collector);
                            collector.open();
                            this.collector = collector;
                            collectorOpened = true;
                            log.info("CXF SOAP service is up and running");
                            /*
                             * Return tracked HTTP service
                             */
                            return httpService;
                        } catch (final ServletException e) {
                            log.error("Couldn't register CXF Servlet", e);
                        } catch (final NamespaceException e) {
                            log.error("Couldn't register CXF Servlet", e);
                        } catch (final RuntimeException e) {
                            if (servletRegistered) {
                                try {
                                    httpService.unregister(alias);
                                    httpService.unregister(alias2);
                                    String servletAlias = alias3;
                                    if (null != servletAlias) {
                                        httpService.unregister(servletAlias);
                                        alias3 = null;
                                    }
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
                            log.error("Couldn't register CXF Servlet", e);
                        }
                        context.ungetService(reference);
                        return null;
                    }
                };
                track(HttpService.class, trackerCustomizer);
                openTrackers();
        } catch (final Exception e) {
            log.error("", e);
            throw e;
        }
    }

}
