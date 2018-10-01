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

package com.openexchange.rest.services.osgi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.eclipsesource.jaxrs.publisher.ApplicationConfiguration;
import com.openexchange.config.ConfigurationService;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.rest.services.RequestTool;
import com.openexchange.rest.services.jersey.AJAXFilter;
import com.openexchange.rest.services.jersey.JSONReaderWriter;
import com.openexchange.rest.services.jersey.JerseyConfiguration;
import com.openexchange.rest.services.jersey.OXExceptionMapper;
import com.openexchange.rest.services.security.AuthenticationFilter;

/**
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public class Activator implements BundleActivator {

    private List<ServiceTracker<?, ?>> trackers;
    private List<ServiceRegistration<?>> registrations;
    final AtomicBoolean authRegistered = new AtomicBoolean();
    ServiceRegistration<AJAXFilter> ajaxFilterRegistration;

    /**
     * Initializes a new {@link Activator}.
     */
    public Activator() {
        super();
    }

    @Override
    public synchronized void start(final BundleContext context) throws Exception {
        final Logger logger = LoggerFactory.getLogger(Activator.class);

        List<ServiceTracker<?, ?>> trackers = new ArrayList<>(6);
        List<ServiceRegistration<?>> registrations = new ArrayList<>(6);
        try {
            trackers.add(new ServiceTracker<ConfigurationAdmin, ConfigurationAdmin>(context, ConfigurationAdmin.class, null) {

                @Override
                public ConfigurationAdmin addingService(ServiceReference<ConfigurationAdmin> reference) {
                    ConfigurationAdmin service = super.addingService(reference);
                    if (service != null) {
                        try {
                            Configuration configuration = service.getConfiguration("com.eclipsesource.jaxrs.connector", null);
                            Dictionary<String, Object> properties = configuration.getProperties();
                            if (properties == null) {
                                properties = new Hashtable<String, Object>(1);
                            }
                            properties.put("root", "/");
                            configuration.update(properties);
                        } catch (IOException e) {
                            logger.error("Could not set root path for jersey servlet. REST API will not be available!", e);
                        }
                    }
                    return service;
                }
            });

            trackers.add(new ServiceTracker<ConfigurationService, ConfigurationService>(context, ConfigurationService.class, null) {

                @Override
                public ConfigurationService addingService(ServiceReference<ConfigurationService> reference) {
                    ConfigurationService service = super.addingService(reference);
                    if (service != null && authRegistered.compareAndSet(false, true)) {
                        String authLogin = service.getProperty("com.openexchange.rest.services.basic-auth.login");
                        String authPassword = service.getProperty("com.openexchange.rest.services.basic-auth.password");
                        context.registerService(AuthenticationFilter.class, new AuthenticationFilter(authLogin, authPassword), null);
                    }
                    return service;
                }
            });

            trackers.add(new ServiceTracker<DispatcherPrefixService, DispatcherPrefixService>(context, DispatcherPrefixService.class, null) {

                @Override
                public DispatcherPrefixService addingService(ServiceReference<DispatcherPrefixService> reference) {
                    DispatcherPrefixService service = super.addingService(reference);
                    if (service != null) {
                        RequestTool.setDispatcherPrefixService(service);
                        Activator.this.ajaxFilterRegistration = context.registerService(AJAXFilter.class, new AJAXFilter(service.getPrefix()), null);
                    }
                    return service;
                }

                @Override
                public void removedService(ServiceReference<DispatcherPrefixService> reference, DispatcherPrefixService service) {
                    RequestTool.setDispatcherPrefixService(null);
                    ServiceRegistration<AJAXFilter> ajaxFilterRegistration = Activator.this.ajaxFilterRegistration;
                    if (null != ajaxFilterRegistration) {
                        Activator.this.ajaxFilterRegistration = null;
                        ajaxFilterRegistration.unregister();
                    }
                    super.removedService(reference, service);
                }
            });
            for (ServiceTracker<?,?> tracker : trackers) {
                tracker.open();
            }

            registrations.add(context.registerService(JSONReaderWriter.class, new JSONReaderWriter(), null));
            registrations.add(context.registerService(OXExceptionMapper.class, new OXExceptionMapper(), null));
            registrations.add(context.registerService(ApplicationConfiguration.class, new JerseyConfiguration(), null));

            /*-
             * From now on all instances of registerable classes are handled/added in:
             *  com.eclipsesource.jaxrs.publisher.internal.ResourceTracker.addingService(ResourceTracker.java:45)
             *
             * A registerable instance is defined as:
             *
             *   private boolean isRegisterableAnnotationPresent( Class<?> type ) {
             *    return type.isAnnotationPresent( javax.ws.rs.Path.class ) || type.isAnnotationPresent( javax.ws.rs.ext.Provider.class );
             *   }
             */

            // All went fine
            this.trackers = trackers;
            trackers = null;

            this.registrations = registrations;
            registrations = null;
        } finally {
            if (null != registrations) {
                for (ServiceRegistration<?> serviceRegistration : registrations) {
                    serviceRegistration.unregister();
                }
            }
            if (null != trackers) {
                for (ServiceTracker<?, ?> serviceTracker : trackers) {
                    serviceTracker.close();
                }
            }
        }
    }

    @Override
    public synchronized void stop(BundleContext context) throws Exception {
        List<ServiceRegistration<?>> registrations = this.registrations;
        if (null != registrations) {
            this.registrations = null;
            for (ServiceRegistration<?> serviceRegistration : registrations) {
                serviceRegistration.unregister();
            }
        }

        List<ServiceTracker<?, ?>> trackers = this.trackers;
        if (null != trackers) {
            this.trackers = null;
            for (ServiceTracker<?, ?> serviceTracker : trackers) {
                serviceTracker.close();
            }
        }
    }

}
