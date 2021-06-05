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

package com.openexchange.ajax.osgi;

import static com.openexchange.ajax.AJAXServlet.PARAMETER_ACTION;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Stack;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.EventAdmin;
import org.osgi.util.tracker.ServiceTracker;
import com.openexchange.ajax.SessionServletInterceptor;
import com.openexchange.ajax.login.LoginRequestHandler;
import com.openexchange.ajax.login.handler.KerberosTicketReload;
import com.openexchange.ajax.session.MissingKerberosTicketInterceptor;
import com.openexchange.kerberos.KerberosService;
import com.openexchange.osgi.DependentServiceRegisterer;
import com.openexchange.osgi.Tools;
import com.openexchange.sessiond.SessiondService;

/**
 * Registers the Kerberos ticket reload login server action.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 * @since 7.6.0
 */
public final class KerberosAJAXActivator implements BundleActivator {

    private final Stack<ServiceTracker<?, ?>> trackers = new Stack<ServiceTracker<?, ?>>();

    private ServiceRegistration<SessionServletInterceptor> registration;

    public KerberosAJAXActivator() {
        super();
    }

    @Override
    public void start(BundleContext context) throws Exception {
        registration = context.registerService(SessionServletInterceptor.class, new MissingKerberosTicketInterceptor(), null);
        Dictionary<String, String> d = new Hashtable<String, String>();
        d.put(PARAMETER_ACTION, "ticketReload");
        DependentServiceRegisterer<LoginRequestHandler> registerer = new DependentServiceRegisterer<LoginRequestHandler>(context, LoginRequestHandler.class, KerberosTicketReload.class, d, SessiondService.class, KerberosService.class, EventAdmin.class);
        trackers.push(new ServiceTracker<Object, Object>(context, registerer.getFilter(), registerer));
        Tools.open(trackers);

    }

    @Override
    public void stop(BundleContext context) {
        Tools.close(trackers);
        registration.unregister();
    }
}
