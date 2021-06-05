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

package com.openexchange.sso.osgi;

import javax.servlet.ServletException;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.osgi.service.http.HttpServices;
import com.openexchange.sso.SSOConstants;
import com.openexchange.sso.services.SSOServiceRegistry;
import com.openexchange.sso.servlet.SSOServlet;

/**
 * {@link SSOServletRegisterer} - Registers the single sign-on servlet.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SSOServletRegisterer implements ServiceTrackerCustomizer<HttpService, HttpService> {

    private final BundleContext context;
    private volatile String alias;

    /**
     * Initializes a new {@link SSOServletRegisterer}.
     *
     * @param context The bundle context
     */
    public SSOServletRegisterer(final BundleContext context) {
        super();
        this.context = context;
    }

    @Override
    public HttpService addingService(final ServiceReference<HttpService> reference) {
        final HttpService service = context.getService(reference);
        try {
            String alias = SSOServiceRegistry.getInstance().getService(DispatcherPrefixService.class).getPrefix() + SSOConstants.SERVLET_PATH_APPENDIX;
            service.registerServlet(alias, new SSOServlet(), null, null);
            this.alias = alias;
        } catch (ServletException e) {
            org.slf4j.LoggerFactory.getLogger(SSOServletRegisterer.class).error("", e);
        } catch (NamespaceException e) {
            org.slf4j.LoggerFactory.getLogger(SSOServletRegisterer.class).error("", e);
        }
        return service;
    }

    @Override
    public void modifiedService(final ServiceReference<HttpService> reference, final HttpService service) {
        // Nothing to do.
    }

    @Override
    public void removedService(final ServiceReference<HttpService> reference, final HttpService service) {
        final HttpService httpService = service;

        String alias = this.alias;
        if (null != httpService) {
            HttpServices.unregister(alias, httpService);
            this.alias = null;
        }

        context.ungetService(reference);
    }

}
