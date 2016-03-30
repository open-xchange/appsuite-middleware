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

    public KerberosServiceRegisterer(BundleContext context) {
        super();
        this.context = context;
    }

    @Override
    public ConfigurationService addingService(ServiceReference<ConfigurationService> reference) {
        final ConfigurationService configService = context.getService(reference);
        final boolean configured = KerberosConfiguration.configure(configService);
        if (configured) {
            impl = new KerberosServiceImpl(KerberosConfiguration.getModuleName(), KerberosConfiguration.getUserModuleName());
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
    public void removedService(ServiceReference<ConfigurationService> reference, ConfigurationService service) {
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
