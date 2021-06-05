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

package com.openexchange.rest.client.httpclient.internal;

import java.util.concurrent.TimeUnit;
import org.apache.http.pool.PoolStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.rest.client.osgi.RestClientServices;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;

/**
 * 
 * {@link IdleConnectionCloser}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.10.3
 */
public class IdleConnectionCloser implements Runnable {

    private final static Logger LOGGER = LoggerFactory.getLogger(IdleConnectionCloser.class);

    private final ClientConnectionManager manager;
    private final int idleTimeoutSeconds;
    private volatile ScheduledTimerTask timerTask;

    public IdleConnectionCloser(ClientConnectionManager manager, int idleTimeoutSeconds) {
        super();
        this.manager = manager;
        this.idleTimeoutSeconds = idleTimeoutSeconds;
    }

    public void ensureRunning(int checkIntervalSeconds) {
        ScheduledTimerTask tmp = timerTask;
        if (null == tmp) {
            synchronized (IdleConnectionCloser.class) {
                tmp = timerTask;
                if (null == tmp) {
                    TimerService service = RestClientServices.getOptionalService(TimerService.class);
                    if (null == service) {
                        LOGGER.error("{} is missing. Can't execute run()", TimerService.class.getSimpleName());
                    } else {
                        tmp = service.scheduleWithFixedDelay(this, checkIntervalSeconds, checkIntervalSeconds, TimeUnit.SECONDS);
                        timerTask = tmp;
                    }
                }
            }
        }
    }

    public void stop() {
        ScheduledTimerTask tmp = timerTask;
        if (null != tmp) {
            synchronized (IdleConnectionCloser.class) {
                tmp = timerTask;
                if (null != tmp) {
                    tmp.cancel();
                    TimerService service = RestClientServices.getOptionalService(TimerService.class);
                    if (null == service) {
                        LOGGER.error("{} is missing. Can't remove canceled tasks", TimerService.class.getSimpleName());
                    } else {
                        service.purge();
                        timerTask = null;
                    }
                }
            }
        }
    }

    @Override
    public void run() {
        try {
            manager.closeExpiredConnections();
            manager.closeIdleConnections(idleTimeoutSeconds, TimeUnit.SECONDS);
            PoolStats totalStats = manager.getTotalStats();
            if (totalStats.getLeased() == 0 && totalStats.getPending() == 0 && totalStats.getAvailable() == 0) {
                stop();
            }
        } catch (@SuppressWarnings("unused") Exception e) {
            stop();
        }
    }
}
