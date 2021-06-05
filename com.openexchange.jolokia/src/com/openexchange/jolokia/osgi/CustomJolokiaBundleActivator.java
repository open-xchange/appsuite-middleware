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
import com.openexchange.osgi.service.http.HttpServices;

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
        } catch (ServletException e) {
            LOG.error("Registering jolokia servlet failed.", e);
            notStarted();
        } catch (NamespaceException e) {
            LOG.error("Registering jolokia servlet failed.", e);
            notStarted();
        } catch (RuntimeException e) {
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
                HttpServices.unregister(usedServletName, httpService);
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
