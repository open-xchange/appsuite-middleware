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

package com.openexchange.spellcheck.osgi;

import static com.openexchange.spellcheck.services.SpellCheckServletServiceRegistry.getServiceRegistry;
import javax.servlet.ServletException;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import com.openexchange.server.osgiservice.DeferredActivator;
import com.openexchange.server.osgiservice.ServiceRegistry;
import com.openexchange.spellcheck.SpellCheckService;
import com.openexchange.spellcheck.servlet.SpellCheckServlet;
import com.openexchange.spellcheck.servlet.SpellCheckServletException;

/**
 * {@link SpellCheckServletActivator}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SpellCheckServletActivator extends DeferredActivator {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(SpellCheckServletActivator.class);

    private static final String SC_SRVLT_ALIAS = "ajax/spellcheck";

    /**
     * Initializes a new {@link SpellCheckServletActivator}
     */
    public SpellCheckServletActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { SpellCheckService.class, HttpService.class };
    }

    @Override
    protected void handleUnavailability(final Class<?> clazz) {
        /*
         * Unregister servlet on both absent HTTP service and spell check service
         */
        final HttpService httpService = getServiceRegistry().getService(HttpService.class);
        if (httpService == null) {
            LOG.error("HTTP service is null. Spell check servlet cannot be unregistered");
        } else {
            /*
             * Unregister spell check servlet
             */
            httpService.unregister(SC_SRVLT_ALIAS);
            if (LOG.isInfoEnabled()) {
                LOG.info("Spell check servlet successfully unregistered");
            }
        }
        getServiceRegistry().removeService(clazz);
    }

    @Override
    protected void handleAvailability(final Class<?> clazz) {
        /*
         * Register servlet on both absent HTTP service and spell check service
         */
        getServiceRegistry().addService(clazz, getService(clazz));
        final HttpService httpService = getServiceRegistry().getService(HttpService.class);
        if (httpService == null) {
            LOG.error("HTTP service is null. Spell check servlet cannot be registered");
        } else {
            try {
                /*
                 * Register spell check servlet
                 */
                httpService.registerServlet(SC_SRVLT_ALIAS, new SpellCheckServlet(), null, null);
                if (LOG.isInfoEnabled()) {
                    LOG.info("Spell check servlet successfully registered");
                }
            } catch (final ServletException e) {
                LOG.error(e.getMessage(), e);
            } catch (final NamespaceException e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.server.osgiservice.DeferredActivator#startBundle()
     */
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
             * Register spell check servlet to newly available HTTP service
             */
            final HttpService httpService = getServiceRegistry().getService(HttpService.class);
            if (httpService == null) {
                LOG.error("HTTP service is null. Spell check servlet cannot be registered");
                return;
            }
            try {
                /*
                 * Register spell check servlet
                 */
                httpService.registerServlet(SC_SRVLT_ALIAS, new SpellCheckServlet(), null, null);
                if (LOG.isInfoEnabled()) {
                    LOG.info("Spell check servlet successfully registered");
                }
            } catch (final ServletException e) {
                throw new SpellCheckServletException(
                    SpellCheckServletException.Code.SERVLET_REGISTRATION_FAILED,
                    e,
                    e.getMessage());
            } catch (final NamespaceException e) {
                throw new SpellCheckServletException(
                    SpellCheckServletException.Code.SERVLET_REGISTRATION_FAILED,
                    e,
                    e.getMessage());
            }
        } catch (final Exception e) {
            LOG.error(e.getMessage(), e);
            throw e;
        }
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.server.osgiservice.DeferredActivator#stopBundle()
     */
    @Override
    protected void stopBundle() throws Exception {
        try {
            /*
             * Unregister spell check servlet
             */
            final HttpService httpService = getServiceRegistry().getService(HttpService.class);
            if (httpService == null) {
                LOG.error("HTTP service is null. Spell check servlet cannot be unregistered");
            } else {
                /*
                 * Unregister spell check servlet
                 */
                httpService.unregister(SC_SRVLT_ALIAS);
                if (LOG.isInfoEnabled()) {
                    LOG.info("Spell check servlet successfully unregistered");
                }
            }
            /*
             * Clear service registry
             */
            getServiceRegistry().clearRegistry();
        } catch (final Exception e) {
            LOG.error(e.getMessage(), e);
            throw e;
        }
    }

}
