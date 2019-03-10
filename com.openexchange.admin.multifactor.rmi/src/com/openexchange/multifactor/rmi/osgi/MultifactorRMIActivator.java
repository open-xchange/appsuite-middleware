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

package com.openexchange.multifactor.rmi.osgi;

import java.rmi.Remote;
import java.util.Dictionary;
import java.util.Hashtable;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.auth.Authenticator;
import com.openexchange.multifactor.MultifactorProviderRegistry;
import com.openexchange.multifactor.rmi.MultifactorManagementRemoteService;
import com.openexchange.multifactor.rmi.MultifactorManagementRemoteServiceImpl;
import com.openexchange.osgi.HousekeepingActivator;

/**
 * {@link MultifactorRMIActivator}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.2
 */
public class MultifactorRMIActivator extends HousekeepingActivator {

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] {Authenticator.class};
    }

    @Override
    protected void startBundle() throws Exception {
        final BundleContext context = this.context;
        org.slf4j.LoggerFactory.getLogger(MultifactorRMIActivator.class).info("Starting bundle {}", context.getBundle().getSymbolicName());
        Dictionary<String, Object> props = new Hashtable<>(2);
        props.put("RMIName", MultifactorManagementRemoteService.RMI_NAME);
        MultifactorManagementRemoteServiceImpl multifactorRMIService = new MultifactorManagementRemoteServiceImpl();

        ServiceTrackerCustomizer<MultifactorProviderRegistry, MultifactorProviderRegistry> registryTracker =
            new ServiceTrackerCustomizer<MultifactorProviderRegistry, MultifactorProviderRegistry>(){

            @Override
            public MultifactorProviderRegistry addingService(ServiceReference<MultifactorProviderRegistry> reference) {
                MultifactorProviderRegistry registry = context.getService(reference);
                multifactorRMIService.setRegistry(registry);
                return registry;
            }

            @Override
            public void modifiedService(ServiceReference<MultifactorProviderRegistry> reference, MultifactorProviderRegistry service) {
                //no-op
            }

            @Override
            public void removedService(ServiceReference<MultifactorProviderRegistry> reference, MultifactorProviderRegistry service) {
                multifactorRMIService.setRegistry(null);
            }
        };
        track(MultifactorProviderRegistry.class, registryTracker);

        openTrackers();
        registerService(Remote.class, multifactorRMIService, props);
    }

    @Override
    protected void stopBundle() throws Exception {
        org.slf4j.LoggerFactory.getLogger(MultifactorRMIActivator.class).info("Stopping bundle {}", context.getBundle().getSymbolicName());
        super.stopBundle();
    }
}
