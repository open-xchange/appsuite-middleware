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

package com.openexchange.proxy.servlet.osgi;

import java.util.Collection;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.proxy.servlet.ProxyRegistrationEntry;
import com.openexchange.proxy.servlet.ProxyRegistryImpl;
import com.openexchange.proxy.servlet.services.ServiceRegistry;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;

/**
 * {@link TimerServiceCustomizer}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class TimerServiceCustomizer implements ServiceTrackerCustomizer<TimerService, TimerService> {

    private final BundleContext context;

    private volatile ScheduledTimerTask scheduledTimerTask;

    /**
     * Initializes a new {@link TimerServiceCustomizer}.
     *
     * @param context The bundle context
     */
    public TimerServiceCustomizer(final BundleContext context) {
        super();
        this.context = context;
    }

    @Override
    public TimerService addingService(final ServiceReference<TimerService> reference) {
        final TimerService timerService = context.getService(reference);
        ServiceRegistry.getInstance().addService(TimerService.class, timerService);
        scheduleTask(timerService);
        return timerService;
    }

    @Override
    public void modifiedService(final ServiceReference<TimerService> reference, final TimerService service) {
        // Nope
    }

    @Override
    public void removedService(final ServiceReference<TimerService> reference, final TimerService service) {
        ServiceRegistry.getInstance().removeService(TimerService.class);
        dropTask(service);
        context.ungetService(reference);
    }

    private void scheduleTask(final TimerService timerService) {
        if (null == scheduledTimerTask) {
            final Runnable task = new Runnable() {

                @Override
                public void run() {
                    final Collection<ConcurrentMap<UUID, ProxyRegistrationEntry>> values = ProxyRegistryImpl.getInstance().values();
                    if (values.isEmpty()) {
                        return;
                    }
                    final long now = System.currentTimeMillis();
                    for (final Iterator<ConcurrentMap<UUID, ProxyRegistrationEntry>> valuesIter = values.iterator(); valuesIter.hasNext();) {
                        final ConcurrentMap<UUID, ProxyRegistrationEntry> map = valuesIter.next();
                        for (final Iterator<ProxyRegistrationEntry> entriesIter = map.values().iterator(); entriesIter.hasNext();) {
                            final ProxyRegistrationEntry entry = entriesIter.next();
                            final long ttl = entry.getTTL();
                            if ((ttl >= 0) && ((now - entry.getTimestamp()) > ttl)) {
                                /*
                                 * Exceeds time-to-live
                                 */
                                entriesIter.remove();
                            }
                        }
                        if (map.isEmpty()) {
                            valuesIter.remove();
                        }
                    }
                }
            };
            scheduledTimerTask = timerService.scheduleWithFixedDelay(task, 1000, 300000);
        }
    }

    private void dropTask(final TimerService timerService) {
        final ScheduledTimerTask scheduledTimerTask = this.scheduledTimerTask;
        if (null != scheduledTimerTask) {
            scheduledTimerTask.cancel(false);
            this.scheduledTimerTask = null;
            timerService.purge();
        }
    }

}
