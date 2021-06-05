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

import java.util.Stack;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;
import com.openexchange.config.ConfigurationService;
import com.openexchange.kerberos.KerberosService;
import com.openexchange.kerberos.session.KerberosSessionSerialization;
import com.openexchange.osgi.DependentServiceRegisterer;
import com.openexchange.osgi.Tools;
import com.openexchange.session.SessionSerializationInterceptor;

/**
 * Registers the service tracker that waits for the {@link ConfigrationService} and then registers the {@link KerberosService}.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class KDCActivator implements BundleActivator {

    private final Stack<ServiceTracker<?, ?>> trackers = new Stack<ServiceTracker<?, ?>>();

    @Override
    public void start(BundleContext context) {
        trackers.push(new ServiceTracker<ConfigurationService, ConfigurationService>(context, ConfigurationService.class.getName(), new KerberosServiceRegisterer(context)));
        DependentServiceRegisterer<SessionSerializationInterceptor> registerer = new DependentServiceRegisterer<SessionSerializationInterceptor>(context, SessionSerializationInterceptor.class, KerberosSessionSerialization.class, null, KerberosService.class);
        trackers.push(new ServiceTracker<Object, Object>(context, KerberosService.class.getName(), registerer));
        Tools.open(trackers);
    }

    @Override
    public void stop(BundleContext context) {
        Tools.close(trackers);
    }
}
