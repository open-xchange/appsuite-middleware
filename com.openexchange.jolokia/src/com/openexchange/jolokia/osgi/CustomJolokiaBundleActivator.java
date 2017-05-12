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
import org.jolokia.osgi.security.BasicAuthenticationHttpContext;
import org.jolokia.osgi.security.BasicAuthenticator;
import org.jolokia.osgi.security.DefaultHttpContext;
import org.jolokia.osgi.servlet.JolokiaServlet;
import org.osgi.framework.BundleActivator;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.DefaultInterests;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadable;
import com.openexchange.jolokia.JolokiaConfig;
import com.openexchange.jolokia.http.OXJolokiaServlet;
import com.openexchange.osgi.HousekeepingActivator;

/**
 * {@link CustomJolokiaBundleActivator} - An OSGi {@link BundleActivator} that will start Jolokia.
 *
 * @author <a href="mailto:felix.marx@open-xchange.com">Felix Marx</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class CustomJolokiaBundleActivator extends HousekeepingActivator implements Reloadable {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CustomJolokiaBundleActivator.class);

    /** The Servlet name */
    private String usedServletName;

    /**
     * Initializes a new {@link CustomJolokiaBundleActivator}.
     */
    public CustomJolokiaBundleActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class, HttpService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        Services.setServiceLookup(this);

        // Check service availability
        ConfigurationService configService = getService(ConfigurationService.class);
        if (null == configService) {
            LOG.info("Denied start-up of Jolokia due to  missing configService");
            notStarted();
            return;
        }

        LOG.info("Starting Bundle: com.openexchange.jolokia");
        register(configService);
        LOG.info("Starting Bundle finished: com.openexchange.jolokia");
    }

    @Override
    protected void stopBundle() throws Exception {
        unregister();
        Services.setServiceLookup(null);
        super.stopBundle();
    }

    private synchronized void register(ConfigurationService configService) throws Exception {
        // Unregister first...
        unregister();

        // Check if enabled
        if (false == configService.getBoolProperty("com.openexchange.jolokia.start", false)) {
            LOG.info("Denied start-up of Jolokia due to config setting");
            notStarted();
            return;
        }

        HttpService httpService = getService(HttpService.class);
        if (null == httpService) {
            LOG.info("Denied start-up of Jolokia due to missing httpService");
            notStarted();
            return;
        }

        JolokiaConfig jolokiaConfig = JolokiaConfig.builder().init(configService).build();

        // 2nd check, because start can be stopped by missing user / password
        if (false == jolokiaConfig.getJolokiaStart()) {
            LOG.info("Denied start-up of Jolokia due to missing authentication settings");
            notStarted();
            return;
        }

        // Create & register Servlet instance
        JolokiaServlet jolServlet = new OXJolokiaServlet(context, jolokiaConfig.getRestrictor());
        try {
            LOG.info("Registering Jolokia servlet...");

            HttpContext httpContext;
            {
                String user = jolokiaConfig.getUser();
                if (user.length() == 0) {
                    httpContext = new DefaultHttpContext();
                } else {
                    String password = jolokiaConfig.getPassword();
                    httpContext = new BasicAuthenticationHttpContext("jolokia", new BasicAuthenticator(user, password));
                }
            }

            String usedServletName = jolokiaConfig.getServletName();
            httpService.registerServlet(usedServletName, jolServlet, jolokiaConfig.getJolokiaConfiguration(), httpContext);
            this.usedServletName = usedServletName;
        } catch (final ServletException e) {
            LOG.error("Registering jolokia servlet failed.", e);
            notStarted();
        } catch (final NamespaceException e) {
            LOG.error("Registering jolokia servlet failed.", e);
            notStarted();
        } catch (final RuntimeException e) {
            LOG.error("Registering jolokia servlet failed.", e);
            notStarted();
        }
    }

    private synchronized void unregister() {
        HttpService httpService = getService(HttpService.class);
        if (null != httpService) {
            String usedServletName = this.usedServletName;
            if (null != usedServletName) {
                this.usedServletName = null;
                httpService.unregister(usedServletName);
            }
        }
    }

    /**
     * Invoked in case Jolokia Servlet has not been started.
     */
    public void notStarted() {
        // Dummy method for testing
    }

    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public void reloadConfiguration(ConfigurationService configService) {
        try {
            register(configService);
        } catch (Exception e) {
            LOG.error("Registering jolokia servlet failed.", e);
        }
    }

    @Override
    public Interests getInterests() {
        return DefaultInterests.builder().propertiesOfInterest("com.openexchange.jolokia.*").configFileNames("jolokia-access.xml").build();
    }

}
