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

package com.openexchange.kerberos.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.kerberos.KerberosService;
import com.openexchange.kerberos.impl.KerberosConfiguration;
import com.openexchange.kerberos.impl.KerberosServiceImpl;

/**
 * Registers the service to communicate with the Kerberos KDC.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class KerberosServiceRegisterer implements ServiceTrackerCustomizer<ConfigurationService, ConfigurationService> {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(KerberosServiceRegisterer.class);

    private final BundleContext context;
    private ServiceRegistration<KerberosService> registration;
    private KerberosServiceImpl impl;

    /**
     * Initializes a new {@link KerberosServiceRegisterer}.
     *
     * @param context The bundle context to use
     */
    public KerberosServiceRegisterer(BundleContext context) {
        super();
        this.context = context;
    }

    @Override
    public synchronized ConfigurationService addingService(ServiceReference<ConfigurationService> reference) {
        ConfigurationService configService = context.getService(reference);
        KerberosConfiguration kerberosConfiguration = KerberosConfiguration.configure(configService);
        if (kerberosConfiguration.isConfigured()) {
            impl = new KerberosServiceImpl(kerberosConfiguration.getModuleName(), kerberosConfiguration.getUserModuleName());
            try {
                impl.login();
                registration = context.registerService(KerberosService.class, impl, null);
            } catch (OXException e) {
                LOG.error("Initial login to Kerberos server failed. Check Open-Xchange principal and keytab.", e);
            }
        }
        return configService;
    }

    @Override
    public void modifiedService(ServiceReference<ConfigurationService> reference, ConfigurationService service) {
        // Nothing to do.
    }

    @Override
    public synchronized void removedService(ServiceReference<ConfigurationService> reference, ConfigurationService service) {
        if (null != registration) {
            registration.unregister();
            registration = null;
        }
        if (null != impl) {
            try {
                impl.logout();
            } catch (OXException e) {
                LOG.error("Termination of the Open-Xchange ticket failed.", e);
            }
            impl = null;
        }
        context.ungetService(reference);
    }
}
