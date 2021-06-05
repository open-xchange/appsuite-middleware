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

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import com.openexchange.counter.MailCounter;
import com.openexchange.monitoring.MonitoringInfo;

/**
 * {@link MailCounterServiceTracker}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailCounterServiceTracker extends ServiceTracker<MailCounter, MailCounter> {

    /**
     * Initializes a new {@link MailCounterServiceTracker}.
     *
     * @param context The bundle context
     */
    public MailCounterServiceTracker(final BundleContext context) {
        super(context, MailCounter.class, null);
    }

    @Override
    public MailCounter addingService(final ServiceReference<MailCounter> reference) {
        final MailCounter service = context.getService(reference);
        if (MonitoringInfo.putIfAbsent(MonitoringInfo.IMAP, service)) {
            return service;
        }
        context.ungetService(reference);
        return null;
    }

    @Override
    public void removedService(final ServiceReference<MailCounter> reference, final MailCounter service) {
        if (null != service) {
            MonitoringInfo.remove(MonitoringInfo.IMAP);
            context.ungetService(reference);
        }
    }

}
