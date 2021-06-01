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
import org.apache.cxf.transport.servlet.ServletDestinationFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.google.common.collect.ImmutableSet;
import com.openexchange.config.ConfigurationService;
import com.openexchange.java.Strings;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.soap.cxf.custom.CXFOsgiServlet;
import com.openexchange.soap.cxf.interceptor.DropDeprecatedElementsInterceptor;
import com.openexchange.soap.cxf.interceptor.TransformGenericElementsInterceptor;

/**
 * {@link CXFActivator} - The activator for CXF bundle.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class CXFActivator extends HousekeepingActivator {

    /** The logger constant */
    static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CXFActivator.class);

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] { HttpService.class, ConfigurationService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        Services.setServiceLookup(this);
        try {
            LOG.info("Starting Bundle: com.openexchange.soap.cxf");
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

                    private WebserviceCollector collector;
                    private String alias3;

                    @Override
                    public synchronized void removedService(final ServiceReference<HttpService> reference, final HttpService service) {
                        final HttpService httpService = service;
                        if (httpService != null) {
                            unregisterHttpAlias(alias, httpService, true);
                            unregisterHttpAlias(alias2, httpService, false);
                            String servletAlias = alias3;
                            if (null != servletAlias) {
                                alias3 = null;
                                unregisterHttpAlias(servletAlias, httpService, false);
                            }
                        }
                        final WebserviceCollector collector = this.collector;
                        if (null != collector) {
                            try {
                                collector.close();
                            } catch (Exception e) {
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

                    private String getBaseAddress(ConfigurationService configService) {
                        if (configService == null) {
                            return null;
                        }
                        String baseAddress = configService.getProperty("com.openexchange.soap.cxf.baseAddress");
                        return Strings.isEmpty(baseAddress) ? null : baseAddress.trim();
                    }

                    private Dictionary<String, Object> buildConfig(String baseAddress, ConfigurationService configService) {
                        Dictionary<String, Object> config = null;
                        if (null != configService) {
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
                        return config;
                    }

                    private Bus registerCxfServlet(String alias, HttpService httpService, Dictionary<String, Object> config, Bus buzz) throws ServletException, NamespaceException {
                        // Create a register Servlet
                        CXFOsgiServlet cxfServlet = new CXFOsgiServlet();
                        httpService.registerServlet(alias, cxfServlet, config, null);
                        LOG.info("Registered CXF Servlet under: {}", alias2);

                        // Get CXF bus
                        Bus bus;
                        if (buzz == null) {
                            Bus createdBus = cxfServlet.getBus();
                            if (null == createdBus) {
                                createdBus = BusFactory.newInstance().createBus();
                                cxfServlet.setBus(createdBus);
                            }
                            bus = createdBus;
                        } else {
                            cxfServlet.setBus(buzz);
                            bus = buzz;
                        }

                        // Add interceptors here
                        bus.getInInterceptors().add(new TransformGenericElementsInterceptor());
                        bus.getInInterceptors().add(new DropDeprecatedElementsInterceptor(ImmutableSet.of("clusterWeight")));
                        bus.setExtension(new ServletDestinationFactory(), HttpDestinationFactory.class);
                        return bus;
                    }

                    @Override
                    public synchronized HttpService addingService(final ServiceReference<HttpService> reference) {
                        HttpService httpService = context.getService(reference);
                        boolean servletRegistered = false;
                        boolean collectorOpened = false;
                        try {
                            System.setProperty(StaxUtils.ALLOW_INSECURE_PARSER, "true");

                            // Determine base address & configuration
                            ConfigurationService configService = getService(ConfigurationService.class);
                            String baseAddress = getBaseAddress(configService);
                            Dictionary<String, Object> config = buildConfig(baseAddress, configService);

                            // Register CXF Serlvet for different aliases
                            Bus bus = registerCxfServlet(alias, httpService, config, null);
                            registerCxfServlet(alias2, httpService, config, bus);
                            if (null != baseAddress) {
                                try {
                                    URL url = new URL(baseAddress);
                                    String servletAlias = url.getPath();
                                    if (!alias.equals(servletAlias) && !alias2.equals(servletAlias)) {
                                        alias3 = servletAlias;
                                        registerCxfServlet(servletAlias, httpService, config, bus);
                                    }
                                } catch (MalformedURLException e) {
                                    throw new IllegalStateException("Invalid URL specified in property \"com.openexchange.soap.cxf.baseAddress\": \"" + baseAddress + "\"", e);
                                }
                            }
                            // Apply as default bus
                            BusFactory.setDefaultBus(bus);
                            servletRegistered = true;

                            // Initialize Webservice collector
                            final WebserviceCollector collector = new WebserviceCollector(baseAddress, context);
                            context.addServiceListener(collector);
                            collector.open();
                            this.collector = collector;
                            collectorOpened = true;
                            LOG.info("CXF SOAP service is up and running");
                            /*
                             * Return tracked HTTP service
                             */
                            return httpService;
                        } catch (ServletException e) {
                            LOG.error("Couldn't register CXF Servlet", e);
                        } catch (NamespaceException e) {
                            LOG.error("Couldn't register CXF Servlet", e);
                        } catch (RuntimeException e) {
                            if (servletRegistered) {
                                unregisterHttpAlias(alias, httpService, true);
                                unregisterHttpAlias(alias2, httpService, false);
                                String servletAlias = alias3;
                                if (null != servletAlias) {
                                    unregisterHttpAlias(servletAlias, httpService, false);
                                    alias3 = null;
                                }
                            }
                            if (collectorOpened) {
                                final WebserviceCollector collector = this.collector;
                                if (null != collector) {
                                    try {
                                        collector.close();
                                    } catch (Exception e1) {
                                        // Ignore
                                    }
                                    this.collector = null;
                                }
                            }
                            LOG.error("Couldn't register CXF Servlet", e);
                        }
                        context.ungetService(reference);
                        return null;
                    }
                };
                track(HttpService.class, trackerCustomizer);
                openTrackers();
        } catch (Exception e) {
            LOG.error("", e);
            throw e;
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        super.stopBundle();
        Services.setServiceLookup(null);
    }

    static void unregisterHttpAlias(String alias, HttpService httpService, boolean logError) {
        if (Strings.isNotEmpty(alias) && httpService != null) {
            try {
                httpService.unregister(alias);
            } catch (Exception e) {
                if (logError) {
                    LOG.error("Failed to unregister HTTP servlet (or resource) associated with alias: {}", alias, e);
                }
            }
        }
    }

}
