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

package com.openexchange.metrics.micrometer.osgi;

import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.DefaultInterests;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadable;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.metrics.micrometer.internal.BasicAuthHttpContext;
import com.openexchange.metrics.micrometer.internal.RegistryInitializer;
import com.openexchange.metrics.micrometer.internal.property.MicrometerFilterProperty;
import com.openexchange.metrics.micrometer.internal.property.MicrometerProperty;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.service.http.HttpServices;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.prometheus.client.exporter.MetricsServlet;

/**
 * {@link MicrometerActivator}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.4
 */
public class MicrometerActivator extends HousekeepingActivator implements Reloadable {

    private static final Logger LOG = LoggerFactory.getLogger(MicrometerActivator.class);

    private static final String SERVLET_BIND_POINT = "/metrics";

    private final RegistryInitializer registryInitializer;

    /**
     * Initializes a new {@link MicrometerActivator}.
     */
    public MicrometerActivator() {
        super();
        registryInitializer = new RegistryInitializer(Metrics.globalRegistry);
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] { HttpService.class, LeanConfigurationService.class, ConfigurationService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        registerService(Reloadable.class, this);
        PrometheusMeterRegistry prometheusRegistry = registryInitializer.initialize(getServiceSafe(ConfigurationService.class));
        registerServlet(prometheusRegistry);
        LOG.info("Bundle {} successfully started", this.context.getBundle().getSymbolicName());
    }

    @Override
    protected synchronized void stopBundle() throws Exception {
        super.stopBundle();
        unregisterServlet();
        registryInitializer.reset();
        LOG.info("Bundle {} successfully stopped", this.context.getBundle().getSymbolicName());
    }

    /////////////////////////////////// RELOADABLE ////////////////////////////////

    @Override
    public Interests getInterests() {
        return DefaultInterests.builder().propertiesOfInterest(MicrometerFilterProperty.BASE + "*").build();
    }

    @Override
    public synchronized void reloadConfiguration(ConfigurationService configService) {
        try {
            PrometheusMeterRegistry prometheusRegistry = registryInitializer.initialize(configService);
            unregisterServlet();
            registerServlet(prometheusRegistry);
        } catch (Exception e) {
            LOG.error("Cannot apply meter filters", e);
        }
    }

    ///////////////////////////////////// HELPERS //////////////////////////////////////////

    /**
     * Registers the {@link #SERVLET_BIND_POINT} servlet
     *
     * @param prometheusRegistry
     * @throws Exception if the servlet cannot be registered
     */
    private void registerServlet(PrometheusMeterRegistry prometheusRegistry) throws Exception {
        HttpService httpService = getServiceSafe(HttpService.class);
        httpService.registerServlet(SERVLET_BIND_POINT, new MetricsServlet(prometheusRegistry.getPrometheusRegistry()), null, withHttpContext());
    }

    /**
     * Unregisters the {@link #SERVLET_BIND_POINT} servlet
     */
    private void unregisterServlet() {
        HttpService httpService = getService(HttpService.class);
        if (httpService == null) {
            return;
        }
        HttpServices.unregister(SERVLET_BIND_POINT, httpService);
    }

    /**
     * Creates a {@link BasicAuthHttpContext} if the login and password properties are set,
     * otherwise returns <code>null</code>.
     *
     * @return The {@link BasicAuthHttpContext} if the login and password properties are set,
     *         otherwise returns <code>null</code>.
     * @throws OXException if an error is occurred
     */
    private HttpContext withHttpContext() throws OXException {
        LeanConfigurationService lean = getServiceSafe(LeanConfigurationService.class);
        String login = lean.getProperty(MicrometerProperty.LOGIN);
        String password = lean.getProperty(MicrometerProperty.PASSWORD);
        return (Strings.isNotEmpty(login) && Strings.isNotEmpty(password)) ? new BasicAuthHttpContext(login, password) : null;
    }
}
