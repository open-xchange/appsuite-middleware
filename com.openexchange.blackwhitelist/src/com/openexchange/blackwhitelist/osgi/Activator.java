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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.blackwhitelist.osgi;

import org.apache.commons.logging.Log;
import org.osgi.service.http.HttpService;
import com.openexchange.blackwhitelist.BlackWhiteListInterface;
import com.openexchange.blackwhitelist.BlackWhiteListServlet;
import com.openexchange.log.LogFactory;
import com.openexchange.osgi.DeferredActivator;
import com.openexchange.osgi.ServiceRegistry;

/**
 * {@link Activator}
 * 
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class Activator extends DeferredActivator {

    private static final Log LOG = LogFactory.getLog(Activator.class);

    private static final String ALIAS = "/ajax/blackwhitelist";

    private BlackWhiteListServlet servlet;

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { HttpService.class, BlackWhiteListInterface.class };
    }

    @Override
    protected void handleAvailability(final Class<?> clazz) {
        registerServlet();
    }

    @Override
    protected void handleUnavailability(final Class<?> clazz) {
        unregisterServlet();
    }

    @Override
    protected void startBundle() throws Exception {
        final ServiceRegistry registry = ServletServiceRegistry.getInstance();
        registry.clearRegistry();
        final Class<?>[] classes = getNeededServices();
        for (int i = 0; i < classes.length; i++) {
            final Object service = getService(classes[i]);
            if (service != null) {
                registry.addService(classes[i], service);
            }
        }

        registerServlet();
    }

    @Override
    protected void stopBundle() throws Exception {
        unregisterServlet();
        ServletServiceRegistry.getInstance().clearRegistry();
    }

    private void registerServlet() {
        final ServiceRegistry registry = ServletServiceRegistry.getInstance();
        final HttpService httpService = registry.getService(HttpService.class);
        if (servlet == null) {
            try {
                httpService.registerServlet(ALIAS, servlet = new BlackWhiteListServlet(), null, null);
                LOG.info("Black-/Whitelist Servlet registered.");
            } catch (final Exception e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }

    private void unregisterServlet() {
        final HttpService httpService = getService(HttpService.class);
        if (httpService != null && servlet != null) {
            httpService.unregister(ALIAS);
            servlet = null;
            LOG.info("Black-/Whitelist Servlet unregistered.");
        }
    }

}
