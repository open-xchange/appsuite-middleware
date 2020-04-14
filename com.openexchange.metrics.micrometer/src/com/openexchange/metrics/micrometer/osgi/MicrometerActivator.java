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

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.servlet.ServletException;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
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
import com.openexchange.metrics.micrometer.internal.property.MicrometerFilterProperty;
import com.openexchange.metrics.micrometer.internal.property.MicrometerProperty;
import com.openexchange.metrics.micrometer.internal.property.filter.EnableMetricPropertyFilter;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.tools.strings.TimeSpanParser;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.distribution.DistributionStatisticConfig;
import io.micrometer.prometheus.PrometheusConfig;
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
    private PrometheusMeterRegistry prometheusRegistry;

    /**
     * Initializes a new {@link MicrometerActivator}.
     */
    public MicrometerActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] { HttpService.class, LeanConfigurationService.class, ConfigurationService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        applyMeterFilters(getServiceSafe(ConfigurationService.class));
        registerService(Reloadable.class, this);
        registerServlet();
        LOG.info("Bundle {} successfully started", this.context.getBundle().getSymbolicName());
    }

    @Override
    protected void stopBundle() throws Exception {
        unregisterServlet();
        LOG.info("Bundle {} successfully stopped", this.context.getBundle().getSymbolicName());
    }

    /////////////////////////////////// RELOADABLE ////////////////////////////////

    @Override
    public Interests getInterests() {
        return DefaultInterests.builder().configFileNames("micrometer.properties").build();
    }

    @Override
    public void reloadConfiguration(ConfigurationService configService) {
        try {
            applyMeterFilters(configService);
            unregisterServlet();
            registerServlet();
        } catch (Exception e) {
            LOG.error("Cannot apply meter filters", e);
        }
    }

    ///////////////////////////////////// HELPERS //////////////////////////////////////////

    private void applyMeterFilters(ConfigurationService configService) throws OXException, ServletException, NamespaceException {
        Metrics.removeRegistry(prometheusRegistry);
        prometheusRegistry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
        // Enable for default metrics such as jvm.*, process.*, etc.
        //DefaultExports.register(prometheusRegistry.getPrometheusRegistry());
        Map<String, String> enableMetrics = configService.getProperties(new EnableMetricPropertyFilter());
        final AtomicBoolean denyAll = new AtomicBoolean();
        enableMetrics.entrySet().stream().forEach(m -> {
            String k = m.getKey().replaceAll(MicrometerFilterProperty.BASE, "");
            String stripped = k.replaceAll(MicrometerFilterProperty.ENABLE.name().toLowerCase() + ".", "");
            if (k.startsWith("enable") && !k.endsWith("all")) {
                /////////////////////////////
                // Enable/Disable property //
                /////////////////////////////
                boolean enable = Boolean.parseBoolean(m.getValue());
                if (enable) {
                    prometheusRegistry.config().meterFilter(MeterFilter.acceptNameStartsWith(stripped));
                } else {
                    prometheusRegistry.config().meterFilter(MeterFilter.denyNameStartsWith(stripped));
                }
            } else if (k.startsWith("distribution")) {
                /////////////////////////////
                // Distribution statistics //
                /////////////////////////////

                // enable/disable percentiles histogram: boolean
                if (k.contains("histogram")) {
                    String distStripped = k.replaceAll(MicrometerFilterProperty.DISTRIBUTION.name().toLowerCase() + ".histogram.", "");
                    prometheusRegistry.config().meterFilter(new MeterFilter() {

                        public DistributionStatisticConfig configure(Meter.Id id, DistributionStatisticConfig config) {
                            if (id.getName().startsWith(distStripped)) {
                                return DistributionStatisticConfig.builder().percentilesHistogram(Boolean.parseBoolean(m.getValue())).build().merge(config);
                            }
                            return config;
                        }
                    });
                } else if (k.contains("min")) {
                    //minimum expected value: long
                    String distStripped = k.replaceAll(MicrometerFilterProperty.DISTRIBUTION.name().toLowerCase() + ".min.", "");
                    prometheusRegistry.config().meterFilter(MeterFilter.minExpected(distStripped, Long.parseLong(m.getValue())));
                } else if (k.contains("max")) {
                    //maximum expected value: long
                    String distStripped = k.replaceAll(MicrometerFilterProperty.DISTRIBUTION.name().toLowerCase() + ".max.", "");
                    prometheusRegistry.config().meterFilter(MeterFilter.maxExpected(distStripped, Long.parseLong(m.getValue())));
                } else if (k.contains("percentiles")) {
                    //publish concrete percentiles: list of double (example: 0.5, 0.75, 0.9, 0.95, 0.99, 0.999)
                    String distStripped = k.replaceAll(MicrometerFilterProperty.DISTRIBUTION.name().toLowerCase() + ".percentiles.", "");
                    prometheusRegistry.config().meterFilter(new MeterFilter() {

                        public DistributionStatisticConfig configure(Meter.Id id, DistributionStatisticConfig config) {
                            if (id.getName().startsWith(distStripped)) {
                                String[] p = Strings.splitByComma(m.getValue());
                                double[] percentiles = new double[p.length];
                                int index = 0;
                                for (String s : p) {
                                    percentiles[index++] = Double.parseDouble(s);
                                }
                                return DistributionStatisticConfig.builder().percentiles(percentiles).build().merge(config);
                            }
                            return config;
                        }
                    });
                } else if (k.contains("sla")) {
                    //sla to publish concrete value buckets: list of time values (example: 50ms, 100ms, 250ms, 500ms, 1s, 1m)
                    String distStripped = k.replaceAll(MicrometerFilterProperty.DISTRIBUTION.name().toLowerCase() + ".sla.", "");
                    prometheusRegistry.config().meterFilter(new MeterFilter() {

                        public DistributionStatisticConfig configure(Meter.Id id, DistributionStatisticConfig config) {
                            if (id.getName().startsWith(distStripped)) {
                                String[] p = Strings.splitByComma(m.getValue());
                                long[] sla = new long[p.length];
                                int index = 0;
                                for (String s : p) {
                                    sla[index++] = TimeSpanParser.parseTimespanToPrimitive(s);
                                }
                                return DistributionStatisticConfig.builder().sla(sla).build().merge(config);
                            }
                            return config;
                        }
                    });
                }
            } else if (k.endsWith("all")) {
                //Match all the properties as prefixes of the meter names, like Spring does, including a possible fallback named all.
                denyAll.set(!Boolean.parseBoolean(m.getValue()));
            }
        });
        if (denyAll.get()) {
            prometheusRegistry.config().meterFilter(MeterFilter.deny());
        }
        Metrics.addRegistry(prometheusRegistry);
    }

    /**
     * Registers the {@link #SERVLET_BIND_POINT} servlet
     *
     * @throws Exception if the servlet cannot be registered
     */
    private void registerServlet() throws Exception {
        HttpService httpService = getServiceSafe(HttpService.class);
        httpService.registerServlet(SERVLET_BIND_POINT, new MetricsServlet(prometheusRegistry.getPrometheusRegistry()), null, withHttpContext());
    }

    /**
     * Unregisters the {@link #SERVLET_BIND_POINT} servlet
     *
     * @throws OXException if the servlet cannot be unregistered
     */
    private void unregisterServlet() throws OXException {
        HttpService httpService = getServiceSafe(HttpService.class);
        httpService.unregister(SERVLET_BIND_POINT);
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
