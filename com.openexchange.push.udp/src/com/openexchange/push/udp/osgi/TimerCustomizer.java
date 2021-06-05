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

package com.openexchange.push.udp.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.push.udp.PushConfiguration;
import com.openexchange.push.udp.PushDiscoverySender;
import com.openexchange.push.udp.PushInit;
import com.openexchange.timer.TimerService;

/**
 * {@link TimerCustomizer}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class TimerCustomizer implements ServiceTrackerCustomizer<TimerService,TimerService> {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(TimerCustomizer.class);

    private final BundleContext context;
    private volatile PushDiscoverySender sender;

    /**
     * Initializes a new {@link TimerCustomizer}.
     */
    public TimerCustomizer(final BundleContext context) {
        super();
        this.context = context;
    }

    @Override
    public TimerService addingService(final ServiceReference<TimerService> reference) {
        final TimerService timer = context.getService(reference);
        final PushConfiguration config = PushInit.getInstance().getConfig();
        if (config.isMultiCastEnabled()) {
            LOG.info("Starting push multicast discovery sender.");
            final PushDiscoverySender tmp = new PushDiscoverySender(config);
            tmp.startSender(timer);
            this.sender = tmp;
        } else {
            LOG.info("Push multicast discovery is disabled.");
        }
        return timer;
    }

    @Override
    public void modifiedService(final ServiceReference<TimerService> reference, final TimerService service) {
        // Nothing to do.
    }

    @Override
    public void removedService(final ServiceReference<TimerService> reference, final TimerService service) {
        final PushDiscoverySender tmp = this.sender;
        if (null != tmp) {
            LOG.info("Stopping push multicast discovery sender.");
            tmp.stopSender();
            this.sender = null;
        }
        context.ungetService(reference);
    }
}
