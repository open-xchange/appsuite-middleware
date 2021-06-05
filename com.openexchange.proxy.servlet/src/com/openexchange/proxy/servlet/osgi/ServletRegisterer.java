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

/**
 *
 */

package com.openexchange.proxy.servlet.osgi;

import javax.servlet.ServletException;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.osgi.service.http.HttpServices;
import com.openexchange.proxy.servlet.Constants;
import com.openexchange.proxy.servlet.ProxyServlet;

/**
 * {@link ServletRegisterer}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ServletRegisterer implements ServiceTrackerCustomizer<HttpService,HttpService> {

    private final BundleContext context;

    /**
     * Initializes a new {@link ServletRegisterer}.
     *
     * @param context The bundle context
     */
    public ServletRegisterer(final BundleContext context) {
        super();
        this.context = context;
    }

    @Override
    public HttpService addingService(final ServiceReference<HttpService> reference) {
        final HttpService service = context.getService(reference);
        tryRegistering(service);
        return service;
    }

    private static void tryRegistering(final HttpService httpService) {
        if (httpService == null) {
            return;
        }
        try {
            httpService.registerServlet(Constants.PATH, new ProxyServlet(), null, null);
        } catch (ServletException e) {
            org.slf4j.LoggerFactory.getLogger(ServletRegisterer.class).error("", e);
        } catch (NamespaceException e) {
            org.slf4j.LoggerFactory.getLogger(ServletRegisterer.class).error("", e);
        }

    }

    @Override
    public void modifiedService(final ServiceReference<HttpService> reference, final HttpService service) {
        // Nope
    }

    @Override
    public void removedService(final ServiceReference<HttpService> reference, final HttpService service) {
        HttpServices.unregister(Constants.PATH, service);
        context.ungetService(reference);
    }

}
