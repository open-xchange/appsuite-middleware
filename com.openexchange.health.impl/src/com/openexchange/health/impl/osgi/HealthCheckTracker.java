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

package com.openexchange.health.impl.osgi;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponse.Status;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.health.MWHealthCheck;
import com.openexchange.health.MWHealthCheckResponse;
import com.openexchange.health.impl.MWHealthCheckResponseImpl;
import com.openexchange.health.impl.MWHealthCheckServiceImpl;

/**
 * {@link HealthCheckTracker}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.10.1
 */
public class HealthCheckTracker implements ServiceTrackerCustomizer<HealthCheck, HealthCheck> {

    private final BundleContext context;
    private final MWHealthCheckServiceImpl healthCheckService;

    public HealthCheckTracker(BundleContext context, MWHealthCheckServiceImpl healthCheckService) {
        super();
        this.context = context;
        this.healthCheckService = healthCheckService;
    }

    @Override
    public HealthCheck addingService(ServiceReference<HealthCheck> reference) {
        HealthCheck check = context.getService(reference);
        MWHealthCheck wrappingCheck = new MWHealthCheck() {

            @Override
            public String getName() {
                return check.getClass().getName();
            }

            @Override
            public MWHealthCheckResponse call() {
                HealthCheckResponse response = check.call();
                return new MWHealthCheckResponseImpl(getName(), response.getData().isPresent() ? response.getData().get() : null, Status.UP.equals(response.getStatus()));
            }
        };
        if (healthCheckService.addCheck(wrappingCheck)) {
            return check;
        }

        context.ungetService(reference);
        return null;
    }

    @Override
    public void modifiedService(ServiceReference<HealthCheck> reference, HealthCheck service) {
        // nothing to do
    }

    @Override
    public void removedService(ServiceReference<HealthCheck> reference, HealthCheck service) {
        healthCheckService.removeCheck(service.getClass().getName());
        context.ungetService(reference);
    }

}
