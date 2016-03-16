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

package com.openexchange.tools.servlet.http;

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

    private static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(HTTPServletRegistration.class);

    private final Servlet servlet;

    private final String alias;

    /**
     * Initializes a new {@link HTTPServletRegistration}.
     *
     * @param context The bundle context
     * @param alias The Servlet's alias
     * @param servlet The Servlet instance to register/de-register on {@link HttpService} presence/absence
     */
    public HTTPServletRegistration(final BundleContext context, final String alias, final Servlet servlet) {
        super(context, HttpService.class, null);
        this.alias = alias;
        this.servlet = servlet;
        open();
    }

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
        } catch (final ServletException e) {
            LOG.error("", e);
            context.ungetService(reference);
        } catch (final NamespaceException e) {
            LOG.error("", e);
            context.ungetService(reference);
        }
        return null;
    }

    @Override
    public void removedService(final ServiceReference<HttpService> reference, final HttpService service) {
        service.unregister(alias);
        super.removedService(reference, service);
    }

    private void unregister0() {
        final HttpService service = getService();
        if (service != null) {
            service.unregister(alias);
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
