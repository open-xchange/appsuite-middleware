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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.ajp13;

import java.util.concurrent.atomic.AtomicBoolean;
import com.openexchange.ajp13.timertask.AJPv13JSessionIDCleaner;
import com.openexchange.server.Initialization;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;

/**
 * {@link AJPv13TimerTaskStarter} - Starts timer tasks for AJP module.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class AJPv13TimerTaskStarter implements Initialization {

    private static volatile AJPv13TimerTaskStarter instance;

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(AJPv13TimerTaskStarter.class);

    /**
     * Gets the singleton instance of {@link AJPv13TimerTaskStarter}
     * 
     * @return The singleton instance of {@link AJPv13TimerTaskStarter}
     */
    public static AJPv13TimerTaskStarter getInstance() {
        if (instance == null) {
            synchronized (AJPv13TimerTaskStarter.class) {
                if (instance == null) {
                    instance = new AJPv13TimerTaskStarter();
                }
            }
        }
        return instance;
    }

    /**
     * Releases the singleton instance of {@link AJPv13TimerTaskStarter}
     */
    public static void releaseInstance() {
        if (instance != null) {
            synchronized (AJPv13TimerTaskStarter.class) {
                if (instance != null) {
                    if (instance.task != null && instance.started.compareAndSet(false, true)) {
                        instance.task.cancel(false);
                        instance.task = null;
                        final TimerService timer = ServerServiceRegistry.getInstance().getService(TimerService.class);
                        if (null != timer) {
                            timer.purge();
                        }
                        LOG.info(AJPv13TimerTaskStarter.class.getName() + " successfully stopped due to singleton release");
                    }
                    instance = null;
                }
            }
        }
    }

    private final AtomicBoolean started;

    private ScheduledTimerTask task;

    /**
     * Initializes a new {@link AJPv13TimerTaskStarter}
     */
    private AJPv13TimerTaskStarter() {
        super();
        started = new AtomicBoolean();
    }

    public void start() {
        if (!started.compareAndSet(false, true)) {
            LOG.error(this.getClass().getName() + " already started");
        }
        if (task != null) {
            return;
        }
        final TimerService timer = ServerServiceRegistry.getInstance().getService(TimerService.class);
        if (null != timer) {
            task = timer.scheduleWithFixedDelay(new AJPv13JSessionIDCleaner(AJPv13ForwardRequest.jsessionids), 1000, 3600000); // every hour
        }
        LOG.info(this.getClass().getName() + " successfully started");
    }

    public void stop() {
        if (!started.compareAndSet(true, false)) {
            LOG.error(this.getClass().getName() + " already stopped");
        }
        if (task == null) {
            return;
        }
        task.cancel(false);
        task = null;
        final TimerService timer = ServerServiceRegistry.getInstance().getService(TimerService.class);
        if (null != timer) {
            timer.purge();
        }
        LOG.info(this.getClass().getName() + " successfully stopped");
    }

}
