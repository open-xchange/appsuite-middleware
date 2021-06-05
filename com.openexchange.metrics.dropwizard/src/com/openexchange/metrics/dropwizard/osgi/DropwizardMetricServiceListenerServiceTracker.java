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

import java.util.concurrent.atomic.AtomicReference;
import org.osgi.framework.ServiceReference;
import com.openexchange.management.ManagementService;
import com.openexchange.metrics.dropwizard.impl.DropwizardMetricService;
import com.openexchange.metrics.dropwizard.jmx.DropwizardMetricMBeanFactory;
import com.openexchange.metrics.dropwizard.jmx.DropwizardMetricServiceMBeanListener;
import com.openexchange.osgi.SimpleRegistryListener;

/**
 * {@link DropwizardMetricServiceListenerServiceTracker}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class DropwizardMetricServiceListenerServiceTracker implements SimpleRegistryListener<ManagementService> {

    private DropwizardMetricService dropwizardMetricService;
    private final AtomicReference<DropwizardMetricServiceMBeanListener> listener;

    /**
     * Initialises a new {@link DropwizardMetricServiceListenerServiceTracker}.
     */
    public DropwizardMetricServiceListenerServiceTracker(DropwizardMetricService dropwizardMetricService) {
        super();
        this.dropwizardMetricService = dropwizardMetricService;
        listener = new AtomicReference<DropwizardMetricServiceMBeanListener>();
    }

    @Override
    public void added(ServiceReference<ManagementService> ref, ManagementService service) {
        if (listener.compareAndSet(null, new DropwizardMetricServiceMBeanListener(service, new DropwizardMetricMBeanFactory()))) {
            dropwizardMetricService.addListener(listener.get());
        }
    }

    @Override
    public void removed(ServiceReference<ManagementService> ref, ManagementService service) {
        DropwizardMetricServiceMBeanListener registerer = listener.get();
        if (registerer == null) {
            return;
        }
        registerer.unregisterAll();
        dropwizardMetricService.removeListener(registerer);
    }
}
