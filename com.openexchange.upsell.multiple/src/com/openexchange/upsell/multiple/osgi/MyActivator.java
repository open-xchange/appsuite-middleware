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

package com.openexchange.upsell.multiple.osgi;

import static com.openexchange.upsell.multiple.osgi.MyServiceRegistry.getServiceRegistry;
import java.rmi.Remote;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import com.openexchange.admin.rmi.OXContextInterface;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.context.ContextService;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.ServiceRegistry;
import com.openexchange.tools.servlet.http.HTTPServletRegistration;
import com.openexchange.upsell.multiple.api.UpsellURLService;
import com.openexchange.upsell.multiple.impl.MyServlet;
import com.openexchange.user.UserService;

public class MyActivator extends HousekeepingActivator {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(MyActivator.class);

    public MyActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] {
            UserService.class, DatabaseService.class, ContextService.class, ConfigurationService.class, ConfigViewFactory.class };
    }

    @Override
    protected void handleAvailability(final Class<?> clazz) {
        LOG.warn("Absent service: {}", clazz.getName());

        getServiceRegistry().addService(clazz, getService(clazz));
    }

    @Override
    protected void handleUnavailability(final Class<?> clazz) {
        LOG.info("Re-available service: {}", clazz.getName());
        getServiceRegistry().removeService(clazz);
    }

    @Override
    protected void startBundle() throws Exception {
        final BundleContext context = this.context;

        final ServiceRegistry registry = getServiceRegistry();
        registry.clearRegistry();
        final Class<?>[] classes = getNeededServices();
        for (final Class<?> classe : classes) {
            final Object service = getService(classe);
            if (null != service) {
                registry.addService(classe, service);
            }
        }

        // register the http info/sso servlet
        final String alias = getFromConfig("com.openexchange.upsell.multiple.servlet");
        {
            if (com.openexchange.java.Strings.isEmpty(alias)) {
                registry.clearRegistry();
                final IllegalStateException e = new IllegalStateException("Missing property in \"com.openexchange.upsell.multiple.servlet\" configuration or missing file \"upsell.properties\".");
                LOG.error("", e);
                return;
            }
        }
        rememberTracker(new HTTPServletRegistration(context, alias, new MyServlet()));
        rememberTracker(new ServiceTracker<UpsellURLService,UpsellURLService>(context, UpsellURLService.class, new UrlServiceInstallationServiceListener(context)));

        // track Remote instances
        final ServiceTrackerCustomizer<Remote, Remote> trackerCustomizer = new ServiceTrackerCustomizer<Remote, Remote>() {

            @Override
            public void removedService(final ServiceReference<Remote> reference, final Remote service) {
                if (null != service) {
                    // TODO:
                    context.ungetService(reference);
                }
            }

            @Override
            public void modifiedService(final ServiceReference<Remote> reference, final Remote service) {
                // Ignore
            }

            @Override
            public Remote addingService(final ServiceReference<Remote> reference) {
                final Remote service = context.getService(reference);
                if (service instanceof OXContextInterface) {
                    registry.addService(OXContextInterface.class, service);
                    return service;
                }
                context.ungetService(reference);
                return null;
            }
        };
        track(Remote.class, trackerCustomizer);

        // Open service trackers
        openTrackers();
    }

    private String getFromConfig(final String key) throws OXException {
        return MyServiceRegistry.getServiceRegistry().getService(ConfigurationService.class, true).getProperty(key);
    }

    @Override
    protected void stopBundle() throws Exception {
        super.stopBundle();
        getServiceRegistry().clearRegistry();
    }

}
