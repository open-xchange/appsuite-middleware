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

package com.openexchange.proxy.servlet.osgi;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.util.tracker.ServiceTracker;
import com.openexchange.ajax.osgi.AbstractSessionServletActivator;
import com.openexchange.net.ssl.SSLSocketFactoryProvider;
import com.openexchange.osgi.RegistryServiceTrackerCustomizer;
import com.openexchange.proxy.ProxyRegistry;
import com.openexchange.proxy.servlet.Constants;
import com.openexchange.proxy.servlet.ProxyEventHandler;
import com.openexchange.proxy.servlet.ProxyRegistryImpl;
import com.openexchange.proxy.servlet.ProxyServlet;
import com.openexchange.proxy.servlet.services.ServiceRegistry;
import com.openexchange.sessiond.SessiondEventConstants;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.timer.TimerService;

/**
 * {@link ProxyServletActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ProxyServletActivator extends AbstractSessionServletActivator {

    private List<ServiceTracker<?,?>> trackers;

    private List<ServiceRegistration<?>> registrations;

    @Override
    public void startBundle() throws Exception {
        final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ProxyServletActivator.class);
        try {
            log.info("starting bundle: com.openexchange.proxy.servlet");

            registerSessionServlet(Constants.PATH, new ProxyServlet());

            trackers = new ArrayList<ServiceTracker<?,?>>(4);
            trackers.add(new ServiceTracker<TimerService,TimerService>(context, TimerService.class, new TimerServiceCustomizer(context)));
            trackers.add(new ServiceTracker<SessiondService,SessiondService>(context, SessiondService.class, new RegistryServiceTrackerCustomizer<SessiondService>(context, ServiceRegistry.getInstance(), SessiondService.class)));
            trackers.add(new ServiceTracker<SSLSocketFactoryProvider,SSLSocketFactoryProvider>(context, SSLSocketFactoryProvider.class, new RegistryServiceTrackerCustomizer<SSLSocketFactoryProvider>(context, ServiceRegistry.getInstance(), SSLSocketFactoryProvider.class)));
            for (final ServiceTracker<?,?> serviceTracker : trackers) {
                serviceTracker.open();
            }

            registrations = new ArrayList<ServiceRegistration<?>>(2);
            /*
             * Register proxy registry
             */
            registrations.add(context.registerService(ProxyRegistry.class, ProxyRegistryImpl.getInstance(), null));
            /*
             * Register event handler to detect removed sessions
             */
            final Dictionary<String, Object> serviceProperties = new Hashtable<String, Object>(1);
            serviceProperties.put(EventConstants.EVENT_TOPIC, SessiondEventConstants.getAllTopics());
            registrations.add(context.registerService(EventHandler.class, new ProxyEventHandler(), serviceProperties));
        } catch (final Exception e) {
            log.error("Failed start-up of bundle com.openexchange.proxy.servlet", e);
            throw e;
        }
    }

    @Override
    public void stopBundle() throws Exception {
        final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ProxyServletActivator.class);
        try {
            log.info("stopping bundle: com.openexchange.proxy.servlet");
            if (null != trackers) {
                for (final ServiceTracker<?,?> serviceTracker : trackers) {
                    serviceTracker.close();
                }
                trackers = null;
            }
            if (null != registrations) {
                while (!registrations.isEmpty()) {
                    registrations.remove(0).unregister();
                }
                registrations = null;
            }
        } catch (final Exception e) {
            log.error("Failed shut-down of bundle com.openexchange.proxy.servlet", e);
            throw e;
        }
    }

    @Override
    protected Class<?>[] getAdditionalNeededServices() {
        return null;
    }

}
