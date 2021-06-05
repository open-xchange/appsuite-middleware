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

package com.openexchange.mobile.configuration.generator.osgi;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import javax.servlet.ServletException;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import com.openexchange.config.ConfigurationService;
import com.openexchange.html.HtmlService;
import com.openexchange.mobile.configuration.generator.MobileConfigServlet;
import com.openexchange.mobile.configuration.generator.configuration.ConfigurationException;
import com.openexchange.mobile.configuration.generator.configuration.MobileConfigProperties;
import com.openexchange.mobile.configuration.generator.configuration.Property;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.service.http.HttpServices;
import com.openexchange.templating.TemplateService;
import com.openexchange.threadpool.ThreadPoolService;

/**
 * {@link Activator}
 */
public class Activator extends HousekeepingActivator {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Activator.class);

    public static final String ALIAS = "/servlet/mobileconfig";

    private boolean registered;

    /**
     * Initializes a new {@link Activator}.
     */
    public Activator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class, TemplateService.class, ThreadPoolService.class, HttpService.class, HtmlService.class };
    }

    @Override
    protected void handleAvailability(final Class<?> clazz) {
        LOG.info("Re-available service: {}", clazz.getName());
        register();
    }

    @Override
    protected void handleUnavailability(final Class<?> clazz) {
        LOG.warn("Absent service: {}", clazz.getName());
        unregister();
    }

    @Override
    protected void startBundle() throws Exception {
        Services.setServiceLookup(this);
        // Check configuration
        checkConfiguration();
        // Test encoding:
        try {
            URLEncoder.encode("test", "UTF-8");
        } catch (UnsupportedEncodingException e) {
            LOG.error("Stopping mobileconfig bundle because UTF-8 charset encoding is not available: ", e);
            throw e;
        }
        register();
    }

    private void checkConfiguration() throws ConfigurationException {
        MobileConfigProperties.check(Property.values(), "Mobileconfig");
    }

    @Override
    protected void stopBundle() throws Exception {
        unregister();
        Services.setServiceLookup(null);
        super.stopBundle();
    }

    /**
     * Registers the servlet.
     */
    private synchronized void register() {
        if (registered) {
            return;
        }

        HttpService service = getService(HttpService.class);
        if (null == service) {
            return;
        }

        try {
            service.registerServlet(ALIAS, new MobileConfigServlet(), null, null);
            registered = true;
        } catch (ServletException e) {
            LOG.error("", e);
        } catch (NamespaceException e) {
            LOG.error("", e);
        }
        LOG.info("MobileConfig servlet registered");
    }

    /**
     * Unregisters the servlet.
     */
    private synchronized void unregister() {
        if (!registered) {
            return;
        }

        HttpService service = getService(HttpService.class);
        if (null != service) {
            boolean registered = this.registered;
            if (registered) {
                this.registered = false;
                HttpServices.unregister(ALIAS, service);
            }
        }
    }

}
