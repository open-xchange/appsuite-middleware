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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.passwordchange.servlet.osgi;

import static com.openexchange.passwordchange.servlet.services.PasswordChangeServletServiceRegistry.getServiceRegistry;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.servlet.ServletException;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import com.openexchange.context.ContextService;
import com.openexchange.passwordchange.PasswordChangeService;
import com.openexchange.passwordchange.servlet.PasswordChangeServlet;
import com.openexchange.server.osgiservice.DeferredActivator;
import com.openexchange.server.osgiservice.ServiceRegistry;

/**
 * {@link PasswordChangeServletActivator}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class PasswordChangeServletActivator extends DeferredActivator {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(PasswordChangeServletActivator.class);

    private static final String PWC_SRVLT_ALIAS = "ajax/passwordchange";

    private final AtomicBoolean registered;

    /**
     * Initializes a new {@link PasswordChangeServletActivator}
     */
    public PasswordChangeServletActivator() {
        super();
        registered = new AtomicBoolean();
    }

    private static final Class<?>[] NEEDED_SERVICES = { HttpService.class, PasswordChangeService.class, ContextService.class };

    @Override
    protected Class<?>[] getNeededServices() {
        return NEEDED_SERVICES;
    }

    @Override
    protected void handleAvailability(final Class<?> clazz) {
        /*
         * Add available service to registry
         */
        getServiceRegistry().addService(clazz, getService(clazz));
        if (getServiceRegistry().size() == NEEDED_SERVICES.length) {
            /*
             * All needed services available: Register servlet
             */
            try {
                registerServlet();
            } catch (final ServletException e) {
                LOG.error(e.getMessage(), e);
            } catch (final NamespaceException e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }

    @Override
    protected void handleUnavailability(final Class<?> clazz) {
        unregisterServlet();
        /*
         * Remove unavailable service from registry
         */
        getServiceRegistry().removeService(clazz);
    }

    @Override
    protected void startBundle() throws Exception {
        try {
            /*
             * (Re-)Initialize service registry with available services
             */
            {
                final ServiceRegistry registry = getServiceRegistry();
                registry.clearRegistry();
                final Class<?>[] classes = getNeededServices();
                for (int i = 0; i < classes.length; i++) {
                    final Object service = getService(classes[i]);
                    if (null != service) {
                        registry.addService(classes[i], service);
                    }
                }
            }
            /*
             * Register servlet
             */
            registerServlet();
        } catch (final Exception e) {
            LOG.error(e.getMessage(), e);
            throw e;
        }

    }

    @Override
    protected void stopBundle() throws Exception {
        try {
            /*
             * Unregister servlet
             */
            unregisterServlet();
            /*
             * Clear service registry
             */
            getServiceRegistry().clearRegistry();
        } catch (final Exception e) {
            LOG.error(e.getMessage(), e);
            throw e;
        }

    }

    private void registerServlet() throws ServletException, NamespaceException {
        if (registered.compareAndSet(false, true)) {
            final HttpService httpService = getServiceRegistry().getService(HttpService.class);
            if (httpService == null) {
                LOG.error("HTTP service is null. Password change servlet cannot be registered");
            } else {
                /*
                 * Register servlet
                 */
                httpService.registerServlet(PWC_SRVLT_ALIAS, new PasswordChangeServlet(), null, null);
                if (LOG.isInfoEnabled()) {
                    LOG.info("Password change servlet successfully registered");
                }
            }
        }
    }

    private void unregisterServlet() {
        if (registered.compareAndSet(true, false)) {
            /*
             * Unregister servlet
             */
            final HttpService httpService = getServiceRegistry().getService(HttpService.class);
            if (httpService == null) {
                LOG.error("HTTP service is null. Password change servlet cannot be unregistered");
            } else {
                /*
                 * Unregister servlet
                 */
                httpService.unregister(PWC_SRVLT_ALIAS);
                if (LOG.isInfoEnabled()) {
                    LOG.info("Password change servlet successfully unregistered");
                }
            }
        }
    }
}
