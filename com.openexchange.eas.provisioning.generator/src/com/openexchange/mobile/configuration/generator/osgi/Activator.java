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

package com.openexchange.mobile.configuration.generator.osgi;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import javax.servlet.ServletException;
import org.apache.commons.logging.Log;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import com.openexchange.config.ConfigurationService;
import com.openexchange.log.LogFactory;
import com.openexchange.mobile.configuration.generator.MobileConfigServlet;
import com.openexchange.mobile.configuration.generator.configuration.ConfigurationException;
import com.openexchange.mobile.configuration.generator.configuration.MobileConfigProperties;
import com.openexchange.mobile.configuration.generator.configuration.Property;
import com.openexchange.mobile.configuration.generator.services.MobileConfigServiceRegistry;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.ServiceRegistry;
import com.openexchange.templating.TemplateService;
import com.openexchange.threadpool.ThreadPoolService;

/**
 * {@link Activator}
 */
public class Activator extends HousekeepingActivator {

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(Activator.class));

    public static final String ALIAS = "/servlet/mobileconfig";

    /**
     * Initializes a new {@link Activator}.
     */
    public Activator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] {
            ConfigurationService.class, TemplateService.class, ThreadPoolService.class, HttpService.class };
    }

    @Override
    protected void handleAvailability(final Class<?> clazz) {
        if (LOG.isInfoEnabled()) {
            LOG.info("Re-available service: " + clazz.getName());
        }
        MobileConfigServiceRegistry.getServiceRegistry().addService(clazz, getService(clazz));
        register();
    }

    @Override
    protected void handleUnavailability(final Class<?> clazz) {
        if (LOG.isWarnEnabled()) {
            LOG.warn("Absent service: " + clazz.getName());
        }
        MobileConfigServiceRegistry.getServiceRegistry().removeService(clazz);
        unregister();
    }

    @Override
    protected void startBundle() throws Exception {
        {
            final ServiceRegistry registry = MobileConfigServiceRegistry.getServiceRegistry();
            registry.clearRegistry();
            final Class<?>[] classes = getNeededServices();
            for (int i = 0; i < classes.length; i++) {
                final Object service = getService(classes[i]);
                if (null != service) {
                    registry.addService(classes[i], service);
                }
            }
        }
        // Check configuration
        checkConfiguration();
        // Test encoding:
        try {
            URLEncoder.encode("test", "UTF-8");
        } catch (final UnsupportedEncodingException e) {
            LOG.error("Stopping mobileconfig bundle because UTF-8 charset encoding is not available: ", e);
            throw e;
        }
        register();
    }

    private void checkConfiguration() throws ConfigurationException {
        MobileConfigProperties.check(MobileConfigServiceRegistry.getServiceRegistry(), Property.values(), "Mobileconfig");
    }

    @Override
    protected void stopBundle() throws Exception {
        unregister();

        MobileConfigServiceRegistry.getServiceRegistry().clearRegistry();
    }

    private void register() {
        final HttpService service = getService(HttpService.class);
        if (null == service) {
            return;
        }
        try {
            service.registerServlet(ALIAS, new MobileConfigServlet(), null, null);
        } catch (ServletException e) {
            LOG.error(e.getMessage(), e);
        } catch (NamespaceException e) {
            LOG.error(e.getMessage(), e);
        }
        LOG.info("MobileConfig servlet registered");
    }

    public void unregister() {
        final HttpService service = getService(HttpService.class);
        if (null != service) {
            service.unregister(ALIAS);
        }
    }

}
