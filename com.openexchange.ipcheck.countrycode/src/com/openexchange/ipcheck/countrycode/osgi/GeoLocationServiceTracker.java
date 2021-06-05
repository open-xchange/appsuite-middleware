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

package com.openexchange.ipcheck.countrycode.osgi;

import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.ajax.ipcheck.spi.IPChecker;
import com.openexchange.exception.OXException;
import com.openexchange.geolocation.GeoLocationService;
import com.openexchange.ipcheck.countrycode.CountryCodeIpChecker;
import com.openexchange.ipcheck.countrycode.mbean.IPCheckMBean;
import com.openexchange.ipcheck.countrycode.mbean.IPCheckMBeanImpl;
import com.openexchange.ipcheck.countrycode.mbean.IPCheckMetricCollector;
import com.openexchange.management.ManagementService;
import com.openexchange.osgi.MultipleServiceTracker;
import com.openexchange.server.ServiceLookup;

/**
 * {@link GeoLocationServiceTracker} - Multiple service tracker for services
 * {@link GeoLocationService}, {@link ManagementService} and {@link MetricService}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.2
 */
public class GeoLocationServiceTracker extends MultipleServiceTracker {

    private static final Logger LOG = LoggerFactory.getLogger(GeoLocationServiceTracker.class);

    private IPCheckMBeanImpl metricsMBean;
    private final ServiceLookup services;
    private final BundleContext bundleContext;

    /**
     * Initialises a new {@link GeoLocationServiceTracker}.
     *
     * @param services The {@link ServiceLookup} instance
     * @param bundleContext The {@link BundleContext}
     */
    public GeoLocationServiceTracker(ServiceLookup services, BundleContext bundleContext) {
        super(bundleContext, GeoLocationService.class, ManagementService.class);
        this.services = services;
        this.bundleContext = bundleContext;
    }

    @Override
    protected void onAllAvailable() {
        CountryCodeIpChecker service = new CountryCodeIpChecker(services, new IPCheckMetricCollector());
        bundleContext.registerService(IPChecker.class, service, null);

        ManagementService managementService = getTrackedService(ManagementService.class);
        if (managementService != null) {
            try {
                metricsMBean = new IPCheckMBeanImpl(services, service);
                managementService.registerMBean(new ObjectName(IPCheckMBean.NAME), metricsMBean);
            } catch (NotCompliantMBeanException | MalformedObjectNameException | OXException e) {
                LOG.error("Could not start bundle '{}': {}", bundleContext.getBundle().getSymbolicName(), e.getMessage(), e);
                return;
            }
        }
        LOG.info("Bundle successfully started: {}", bundleContext.getBundle().getSymbolicName());

    }

    @Override
    protected boolean serviceRemoved(Object service) {
        if (metricsMBean != null) {
            metricsMBean.stop();
        }

        ManagementService managementService = service instanceof ManagementService ? (ManagementService) service : getTrackedService(ManagementService.class);
        if (managementService == null) {
            LOG.info("Bundle successfully stopped: {}", bundleContext.getBundle().getSymbolicName());
            return true;
        }
        try {
            managementService.unregisterMBean(new ObjectName(IPCheckMBean.NAME));
        } catch (MalformedObjectNameException | OXException e) {
            LOG.error("Could not stop bundle '{}': {}", bundleContext.getBundle().getSymbolicName(), e.getMessage(), e);
        }
        return true;
    }
}
