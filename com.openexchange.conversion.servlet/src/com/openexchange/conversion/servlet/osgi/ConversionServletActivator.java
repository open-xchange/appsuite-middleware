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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.conversion.servlet.osgi;

import javax.servlet.Servlet;
import org.osgi.service.http.HttpService;
import com.openexchange.conversion.ConversionService;
import com.openexchange.conversion.servlet.ConversionServlet;
import com.openexchange.conversion.servlet.ConversionServletServiceRegistry;
import com.openexchange.server.osgiservice.DeferredActivator;
import com.openexchange.server.osgiservice.ServiceRegistry;
import com.openexchange.tools.service.SessionServletRegistration;

/**
 * {@link ConversionServletActivator}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ConversionServletActivator extends DeferredActivator {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(ConversionServletActivator.class);

    private static final String ALIAS = "ajax/conversion";

    private Servlet conversionServlet;

    /**
     * Initializes a new {@link ConversionServletActivator}
     */
    public ConversionServletActivator() {
        super();
    }

    private static final Class<?>[] NEEDED_SERVICES = { ConversionService.class };

    private SessionServletRegistration servletRegistration;

    @Override
    protected Class<?>[] getNeededServices() {
        return NEEDED_SERVICES;
    }

    @Override
    protected void handleAvailability(final Class<?> clazz) {
        ConversionServletServiceRegistry.getServiceRegistry().addService(clazz, getService(clazz));
        if (!allAvailable()) {
            return;
        }
    }

    @Override
    protected void handleUnavailability(final Class<?> clazz) {
        if(servletRegistration != null) {
            servletRegistration.close();
            servletRegistration = null;
        }
        ConversionServletServiceRegistry.getServiceRegistry().removeService(clazz);
    }

    @Override
    protected void startBundle() throws Exception {
        try {
            /*
             * (Re-)Initialize service registry with available services
             */
            {
                final ServiceRegistry registry = ConversionServletServiceRegistry.getServiceRegistry();
                registry.clearRegistry();
                final Class<?>[] classes = getNeededServices();
                for (final Class<?> classe : classes) {
                    final Object service = getService(classe);
                    if (null != service) {
                        registry.addService(classe, service);
                    }
                }
            }
            servletRegistration = new SessionServletRegistration(context, new ConversionServlet(), ALIAS);
            servletRegistration.open();
        } catch (final Exception e) {
            LOG.error(e.getMessage(), e);
            throw e;
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        try {
            /*
             * Unregister on bundle stop
             */
            final HttpService httpService = getService(HttpService.class);
            if (httpService != null && conversionServlet != null) {
                httpService.unregister(ALIAS);
                conversionServlet = null;
                LOG.info(ConversionServlet.class.getName() + " unregistered due to bundle stop");
            }
            /*
             * Clear service registry
             */
            ConversionServletServiceRegistry.getServiceRegistry().clearRegistry();
        } catch (final Exception e) {
            LOG.error(e.getMessage(), e);
            throw e;
        }
    }

}
