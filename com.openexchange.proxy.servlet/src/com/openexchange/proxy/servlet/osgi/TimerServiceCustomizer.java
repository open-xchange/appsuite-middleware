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
 *    trademarks of the OX Software GmbH group of companies.
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
