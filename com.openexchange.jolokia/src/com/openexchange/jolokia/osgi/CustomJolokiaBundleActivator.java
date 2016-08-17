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

package com.openexchange.jolokia.osgi;

import javax.servlet.ServletException;
import org.jolokia.osgi.JolokiaAuthenticatedHttpContext;
import org.jolokia.osgi.JolokiaHttpContext;
import org.jolokia.osgi.servlet.JolokiaServlet;
import org.osgi.framework.BundleActivator;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import com.openexchange.config.ConfigurationService;
import com.openexchange.jolokia.JolokiaConfig;
import com.openexchange.osgi.HousekeepingActivator;

/**
 * {@link CustomJolokiaBundleActivator} - An OSGi {@link BundleActivator} that will start Jolokia.
 *
 * @author <a href="mailto:felix.marx@open-xchange.com">Felix Marx</a>
 */
public class CustomJolokiaBundleActivator extends HousekeepingActivator {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CustomJolokiaBundleActivator.class);

    volatile JolokiaConfig myConfig;

    // HttpContext used for authorization
    private HttpContext jolokiaHttpContext;

    private volatile String usedServletName;

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class, HttpService.class };
    }

    @Override
    protected void startBundle() throws Exception {

        // Check service availability
        final ConfigurationService configService = getService(ConfigurationService.class);
        if (null == configService) {
            LOG.info("Shutting down Bundle due to missing configService");
            stopBundle();
            return;
        }

        // Check if enabled
        if (false == configService.getBoolProperty("com.openexchange.jolokia.start", false)) {
            LOG.info("Shutting down Bundle due to config setting");
            stopBundle();
            return;
        }

        // Check service availability
        final HttpService httpService = getService(HttpService.class);
        if (null == httpService) {
            LOG.info("Shutting down Bundle due to missing httpService");
            stopBundle();
            return;
        }

        LOG.info("Starting Bundle: com.openexchange.jolokia");

        Services.setServiceLookup(this);

        final JolokiaConfig jolokiaConfig = JolokiaConfig.getInstance();
        myConfig = jolokiaConfig;
        jolokiaConfig.start();

        //2nd check, because start can be stopped by missing user / password
        if (false == jolokiaConfig.getJolokiaStart()) {
            LOG.info("Shutting down Bundle");
            stopBundle();
            return;
        }
        
        // Create servlet instance
        JolokiaServlet jolServlet = new JolokiaServlet(context, jolokiaConfig.getRestrictor());
        try {
            LOG.info("Registering jolokia servlet.");
            String usedServletName = jolokiaConfig.getServletName();
            this.usedServletName = usedServletName;
            httpService.registerServlet(usedServletName, jolServlet, jolokiaConfig.getJolokiaConfiguration(), getHttpContext());
        } catch (final ServletException e) {
            LOG.error("Registering jolokia servlet failed.", e);
        } catch (final NamespaceException e) {
            LOG.error("Registering jolokia servlet failed.", e);
        } catch (final RuntimeException e) {
            LOG.error("Registering jolokia servlet failed.", e);
        }
        LOG.info("Starting Bundle finished: com.openexchange.jolokia");
    }

    @Override
    protected void stopBundle() throws Exception {
        final JolokiaConfig jolokiaConfig = myConfig;
        if (jolokiaConfig != null) {
            if (jolokiaConfig.getStarted().get()) {
                jolokiaConfig.stop();
            }
            myConfig = null;
        }
        final HttpService httpService = getService(HttpService.class);
        if (null != httpService) {
            final String usedServletName = this.usedServletName;
            if (null != usedServletName) {
                httpService.unregister(usedServletName);
                this.usedServletName = null;
            }
        }
        Services.setServiceLookup(null);
        super.stopBundle();
    }

    /**
     * Get the security context for our servlet. Dependent on the configuration, this is either a no-op context or one which authenticates
     * with a given user
     *
     * @return the HttpContext with which the agent servlet gets registered.
     */
    public synchronized HttpContext getHttpContext() {
        if (jolokiaHttpContext == null) {
            final String user = myConfig.getUser();
            final String password = myConfig.getPassword();
            if (user.equalsIgnoreCase("")) {
                 jolokiaHttpContext = new JolokiaHttpContext();
             } else {
                 jolokiaHttpContext = new JolokiaAuthenticatedHttpContext(user, password);
             }
        }
        return jolokiaHttpContext;
    }
}
