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

package com.openexchange.monitoring.impl.osgi;

import java.util.concurrent.TimeUnit;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.monitoring.impl.internal.memory.MemoryMonitoring;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;

/**
 * {@link MemoryMonitoringInitializer}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class MemoryMonitoringInitializer implements ServiceTrackerCustomizer<TimerService, TimerService> {

    private final BundleContext context;
    private final int periodMinutes;
    private final double threshold;
    private ScheduledTimerTask timerTask;

    /**
     * Initializes a new {@link MemoryMonitoringInitializer}.
     */
    public MemoryMonitoringInitializer(int periodMinutes, double threshold, BundleContext context) {
        super();
        this.periodMinutes = periodMinutes;
        this.threshold = threshold;
        this.context = context;
    }

    @Override
    public synchronized TimerService addingService(ServiceReference<TimerService> reference) {
        TimerService timerService = context.getService(reference);

        try {
            Runnable task = new MemoryMonitoring(periodMinutes, threshold);
            timerTask = timerService.scheduleAtFixedRate(task, periodMinutes, periodMinutes, TimeUnit.MINUTES);
            return timerService;
        } catch (Exception e) {
            org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(MemoryMonitoringInitializer.class);
            logger.warn("Failed to initialize memory monitoring task", e);
        }

        context.ungetService(reference);
        return null;
    }

    @Override
    public void modifiedService(ServiceReference<TimerService> reference, TimerService service) {
        // Don't care
    }

    @Override
    public synchronized void removedService(ServiceReference<TimerService> reference, TimerService service) {
        ScheduledTimerTask timerTask = this.timerTask;
        if (null != timerTask) {
            this.timerTask = null;
            timerTask.cancel();
        }
        service.purge();

        context.ungetService(reference);
    }

}
