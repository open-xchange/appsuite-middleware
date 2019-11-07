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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.rest.client.httpclient.internal;

import java.util.concurrent.TimeUnit;
import org.apache.http.pool.PoolStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.rest.client.osgi.RestClientServices;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;

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
            if (totalStats.getLeased() == 0 && totalStats.getPending() == 0  && totalStats.getAvailable() == 0) {
                stop();
            }
        } catch (Exception e) {
            stop();
        }
    }
}