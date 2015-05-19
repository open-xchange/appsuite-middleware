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
 *     Copyright (C) 2004-2015 Open-Xchange, Inc.
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

package com.openexchange.oauth.provider.rmi.impl.osgi;

import java.rmi.Remote;
import java.util.Dictionary;
import java.util.Hashtable;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import com.openexchange.oauth.provider.OAuthProviderService;
import com.openexchange.oauth.provider.rmi.client.RemoteClientManagement;
import com.openexchange.oauth.provider.rmi.impl.RemoteClientManagementImpl;


/**
 * {@link OAuthProviderRMIActivator}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class OAuthProviderRMIActivator implements BundleActivator {

    private ServiceRegistration<Remote> serviceRegistration;

    private ServiceTracker<OAuthProviderService, OAuthProviderService> tracker;

    @Override
    public void start(BundleContext context) throws Exception {
        tracker = new ServiceTracker<OAuthProviderService, OAuthProviderService>(context, OAuthProviderService.class, null) {
            @Override
            public OAuthProviderService addingService(ServiceReference<OAuthProviderService> reference) {
                OAuthProviderService service = super.addingService(reference);
                if (service != null) {
                    register(context, service);
                }

                return service;
            }

            @Override
            public void remove(ServiceReference<OAuthProviderService> reference) {
                unregister();
                super.remove(reference);
            }
        };

        tracker.open();
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        unregister();
        tracker.close();
        tracker = null;
    }

    private synchronized void register(BundleContext context, OAuthProviderService service) {
        if (serviceRegistration == null) {
            Dictionary<String, Object> props = new Hashtable<String, Object>(2);
            props.put("RMIName", RemoteClientManagement.RMI_NAME);
            serviceRegistration = context.registerService(Remote.class, new RemoteClientManagementImpl(service.getClientManagement()), props);
        }
    }

    private synchronized void unregister() {
        if (serviceRegistration != null) {
            serviceRegistration.unregister();
            serviceRegistration = null;
        }
    }

}
