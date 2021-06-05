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

package com.openexchange.tools.servlet.http;

import com.openexchange.osgi.service.http.HttpServices;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.osgi.util.tracker.ServiceTracker;

/**
 * {@link HTTPServletRegistration} -  A simple {@link ServiceTracker service tracker} for {@link HttpService OSGi's HttpService}.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class HTTPServletRegistration extends ServiceTracker<HttpService, HttpService> {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(HTTPServletRegistration.class);

    private final Servlet servlet;

    private final String alias;

    /**
     * Initializes a new {@link HTTPServletRegistration}.
     *
     * @param context The bundle context
     * @param servlet The Servlet instance to register/de-register on {@link HttpService} presence/absence
     * @param alias The Servlet's alias
     */
    public HTTPServletRegistration(final BundleContext context, final Servlet servlet, final String alias) {
        super(context, HttpService.class, null);
        this.alias = alias;
        this.servlet = servlet;
        open();
    }

    @Override
    public HttpService addingService(final ServiceReference<HttpService> reference) {
        try {
            final HttpService service = super.addingService(reference);
            service.registerServlet(alias, servlet, null, null);
            return service;
        } catch (ServletException e) {
            LOG.error("", e);
            context.ungetService(reference);
        } catch (NamespaceException e) {
            LOG.error("", e);
            context.ungetService(reference);
        }
        return null;
    }

    @Override
    public void removedService(final ServiceReference<HttpService> reference, final HttpService service) {
        HttpServices.unregister(alias, service);
        super.removedService(reference, service);
    }

    private void unregister0() {
        final HttpService service = getService();
        if (service != null) {
            HttpServices.unregister(alias, service);
        }
    }

    /**
     * Unregisters the Servlet manually.
     */
    public void unregister() {
        unregister0();
        close();
    }
}
