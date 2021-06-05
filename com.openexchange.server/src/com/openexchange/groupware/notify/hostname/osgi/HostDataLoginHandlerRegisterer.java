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

package com.openexchange.groupware.notify.hostname.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceRegistration;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.groupware.notify.hostname.HostnameService;
import com.openexchange.groupware.notify.hostname.internal.HostDataLoginHandler;
import com.openexchange.login.LoginHandlerService;
import com.openexchange.osgi.DependentServiceStarter;
import com.openexchange.server.ServiceLookup;
import com.openexchange.systemname.SystemNameService;

/**
 * {@link HostDataLoginHandlerRegisterer}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class HostDataLoginHandlerRegisterer extends DependentServiceStarter {

    private static final Class<?>[] NEEDED = new Class<?>[] { SystemNameService.class, DispatcherPrefixService.class };

    private static final Class<?>[] OPTIONAL = new Class<?>[] { HostnameService.class };

    private ServiceRegistration<LoginHandlerService> registration;


    public HostDataLoginHandlerRegisterer(BundleContext context) throws InvalidSyntaxException {
        super(context, NEEDED, OPTIONAL);
    }


    @Override
    protected void start(ServiceLookup services) throws Exception {
        HostDataLoginHandler loginHandler = new HostDataLoginHandler(services);
        registration = context.registerService(LoginHandlerService.class, loginHandler, null);
    }

    @Override
    protected void stop(ServiceLookup services) throws Exception {
        if (registration != null) {
            registration.unregister();
            registration = null;
        }
    }
}
