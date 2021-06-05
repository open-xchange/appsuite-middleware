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

package com.openexchange.metrics.dropwizard.osgi;

import com.openexchange.management.ManagementService;
import com.openexchange.metrics.MetricService;
import com.openexchange.metrics.dropwizard.impl.DropwizardMetricService;
import com.openexchange.osgi.HousekeepingActivator;

/**
 * {@link MetricActivator}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
@SuppressWarnings("deprecation")
public class MetricActivator extends HousekeepingActivator {

    /**
     * Initialises a new {@link MetricActivator}.
     */
    public MetricActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] {};
    }

    @Override
    protected void startBundle() throws Exception {
        DropwizardMetricService dropwizardService = new DropwizardMetricService();
        registerService(MetricService.class, dropwizardService);
        track(ManagementService.class, new DropwizardMetricServiceListenerServiceTracker(dropwizardService));
        openTrackers();
    }

    @Override
    protected void stopBundle() throws Exception {
        unregisterService(MetricService.class);
        super.stopBundle();
    }
}
