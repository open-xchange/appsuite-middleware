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

package com.openexchange.messaging.sms.osgi;

import static com.openexchange.messaging.sms.osgi.MessagingSMSServiceRegistry.getServiceRegistry;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.http.HttpService;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.groupware.settings.PreferencesItemService;
import com.openexchange.messaging.sms.impl.SMSPreferencesItem;
import com.openexchange.messaging.sms.service.MessagingNewService;
import com.openexchange.osgi.DeferredActivator;
import com.openexchange.osgi.ServiceRegistry;

/**
 * @author Benjamin Otterbach
 *
 */
public class Activator extends DeferredActivator {

    private static transient final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Activator.class);

    private volatile ServletRegisterer servletRegisterer;

    private volatile ServiceRegistration<PreferencesItemService> serviceRegistration;

    public Activator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { HttpService.class, MessagingNewService.class, DispatcherPrefixService.class };
    }

    @Override
    protected void handleAvailability(final Class<?> clazz) {
        LOG.warn("Absent service: {}", clazz.getName());
        getServiceRegistry().addService(clazz, getService(clazz));
        if (HttpService.class.equals(clazz)) {
            servletRegisterer.registerServlet();
        }
    }

    @Override
    protected void handleUnavailability(final Class<?> clazz) {
        LOG.info("Re-available service: {}", clazz.getName());
        if (HttpService.class.equals(clazz)) {
            servletRegisterer.unregisterServlet();
        }
        getServiceRegistry().removeService(clazz);
    }

    @Override
    protected void startBundle() throws Exception {
        try {
            {
                final ServiceRegistry registry = getServiceRegistry();
                registry.clearRegistry();
                final Class<?>[] classes = getNeededServices();
                for (int i = 0; i < classes.length; i++) {
                    final Object service = getService(classes[i]);
                    if (null != service) {
                         registry.addService(classes[i], service);
                    }
                }
            }
            ServletRegisterer.PREFIX.set(getService(DispatcherPrefixService.class));
            final ServletRegisterer servletRegisterer = new ServletRegisterer();
            this.servletRegisterer = servletRegisterer;
            servletRegisterer.registerServlet();

            serviceRegistration = context.registerService(PreferencesItemService.class, new SMSPreferencesItem(), null);
        } catch (final Throwable t) {
            LOG.error("", t);
            throw t instanceof Exception ? (Exception) t : new Exception(t);
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        try {
            final ServiceRegistration<PreferencesItemService> serviceRegistration = this.serviceRegistration;
            if (null != serviceRegistration) {
                serviceRegistration.unregister();
                this.serviceRegistration = null;
            }

            final ServletRegisterer servletRegisterer = this.servletRegisterer;
            if (null != servletRegisterer) {
                servletRegisterer.unregisterServlet();
                this.servletRegisterer = null;
            }
            getServiceRegistry().clearRegistry();
            ServletRegisterer.PREFIX.set(null);
        } catch (final Throwable t) {
            LOG.error("", t);
            throw t instanceof Exception ? (Exception) t : new Exception(t);
        }
    }
}
