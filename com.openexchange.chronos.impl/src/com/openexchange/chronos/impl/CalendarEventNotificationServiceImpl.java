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

package com.openexchange.chronos.impl;

import static com.openexchange.java.Autoboxing.L;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.chronos.service.CalendarEvent;
import com.openexchange.chronos.service.CalendarEventNotificationService;
import com.openexchange.chronos.service.CalendarHandler;
import com.openexchange.osgi.ServiceSet;
import com.openexchange.threadpool.ThreadPools;


/**
 * {@link CalendarEventNotificationServiceImpl}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.1
 */
public class CalendarEventNotificationServiceImpl implements CalendarEventNotificationService {

    private static final Logger LOG = LoggerFactory.getLogger(CalendarEventNotificationServiceImpl.class);
    private final ServiceSet<CalendarHandler> handlers;

    /**
     * Initializes a new {@link CalendarEventNotificationServiceImpl}.
     */
    public CalendarEventNotificationServiceImpl(ServiceSet<CalendarHandler> handler) {
        this.handlers = handler;
    }

    @Override
    public void notifyHandlers(CalendarEvent event) {
        notifyHandlers(event, true);
    }

    @Override
    public void notifyHandlers(CalendarEvent event, boolean asynch) {
        if (asynch) {
            Runnable notifyRunnable = () -> {
                notifyHandlersInternal(event);
            };

            ThreadPools.submitElseExecute(ThreadPools.task(notifyRunnable));
        } else {
            notifyHandlersInternal(event);
        }

    }

    private void notifyHandlersInternal(CalendarEvent event) {
        for (CalendarHandler handler : handlers) {
            long start = System.currentTimeMillis();
            try {
                handler.handle(event);
                LOG.trace("{} handled successfully by {} ({} ms elapsed)", event, handler, L(System.currentTimeMillis() - start));
            } catch (Exception e) {
                LOG.warn("Unexpected error while handling {}: {}", event, e.getMessage(), e);
            }
        }
    }



}
