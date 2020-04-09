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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.metrics.micrometer.osgi;

import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.metrics.micrometer.internal.BasicAuthHttpContext;
import com.openexchange.metrics.micrometer.internal.MicrometerProperty;
import com.openexchange.osgi.HousekeepingActivator;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.prometheus.client.exporter.MetricsServlet;
import io.prometheus.client.hotspot.DefaultExports;

/**
 * {@link MicrometerActivator}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.4
 */
public class MicrometerActivator extends HousekeepingActivator {

    private static final String SERVLET_BIND_POINT = "/metrics";

    private static final Logger LOG = LoggerFactory.getLogger(MicrometerActivator.class);

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] { HttpService.class, LeanConfigurationService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        PrometheusMeterRegistry prometheusRegistry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
        Metrics.addRegistry(prometheusRegistry);
        DefaultExports.register(prometheusRegistry.getPrometheusRegistry());

        HttpService httpService = getServiceSafe(HttpService.class);
        httpService.registerServlet(SERVLET_BIND_POINT, new MetricsServlet(prometheusRegistry.getPrometheusRegistry()), null, withHttpContext());
        LOG.info("Bundle {} successfully started", this.context.getBundle().getSymbolicName());
    }

    @Override
    protected void stopBundle() throws Exception {
        HttpService httpService = getServiceSafe(HttpService.class);
        httpService.unregister(SERVLET_BIND_POINT);
        LOG.info("Bundle {} successfully stopped", this.context.getBundle().getSymbolicName());
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
