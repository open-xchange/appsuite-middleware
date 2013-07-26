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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.realtime.client.impl.connection;

/**
 * {@link CountDown} - A resettable CountDown that executes a list of Runnables once counted down.
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */

import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
 
public class ResettableCountDown {
    
    private Set<Runnable> callbacks = new HashSet<Runnable>();
    private Timer timer;
    private long timeout;
    private TimeUnit unit;
    private CountDownTask countDownTask;
    ExecutorService executorService = Executors.newFixedThreadPool(50);
    
    public ResettableCountDown(int timeout, TimeUnit unit) {
        this.timeout = timeout;
        this.unit=unit;
        timer = new Timer();
        countDownTask = new CountDownTask(timeout);
    }

    public void addRunnable(Runnable runnable) {
        callbacks.add(runnable);
    }

    public void removeRunnable(Runnable runnable) {
        callbacks.remove(runnable);
    }
    
    public void start() {
        timer.schedule(countDownTask, 0, unit.toMillis(timeout));
    }

    /**
     * Resets the Countdown. Don't forget to call start again.
     */
    public void reset() {
        timer.cancel();
        timer = new Timer();
        countDownTask = new CountDownTask(timeout);
    }

    private class CountDownTask extends TimerTask {
        private long count;
        
        public CountDownTask(long count) {
            this.count = count;
        }

        @Override
        public void run() {
            if (count > 0) {
                count--;
            }

            if (count == 0) {
                for (Runnable runnable : callbacks) {
                    executorService.execute(runnable);
                }
            }
        }

    };
    
}
