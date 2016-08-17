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

package com.openexchange.tools.servlet.ratelimit;

import java.util.Random;
import junit.framework.TestCase;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.SimConfigurationService;
import com.openexchange.java.ConcurrentHashSet;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.threadpool.internal.ThreadPoolProperties;
import com.openexchange.threadpool.internal.ThreadPoolServiceImpl;
import com.openexchange.timer.TimerService;
import com.openexchange.timer.internal.CustomThreadPoolExecutorTimerService;

/**
 * {@link Bug41499Test}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Bug41499Test extends TestCase {

    private static final int MAX_RATE = 1500;
    private static final int MAX_RATE_TIME_WINDOW = 5000;
    private static final int THREAD_COUNT = 10;
    private static final int REQUESTS_PER_THREAD = 10;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        ServerServiceRegistry services = ServerServiceRegistry.getInstance();
        SimConfigurationService configService = new SimConfigurationService();
        configService.stringProperties.put("com.openexchange.servlet.maxRate", String.valueOf(MAX_RATE));
        configService.stringProperties.put("com.openexchange.servlet.maxRateTimeWindow", String.valueOf(MAX_RATE_TIME_WINDOW));
        services.addService(ConfigurationService.class, configService);
        ThreadPoolServiceImpl threadPoolService = ThreadPoolServiceImpl.newInstance(new ThreadPoolProperties().init(configService));
        TimerService timerService = new CustomThreadPoolExecutorTimerService(threadPoolService.getThreadPoolExecutor());
        services.addService(TimerService.class, timerService);
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testRateLimiterEviction() throws Exception {
        final ConcurrentHashSet<Key> expectedSlots = new ConcurrentHashSet<Key>();
        final Random random = new Random();
        Thread[] threads = new Thread[THREAD_COUNT];
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                for (int i = 0; i < REQUESTS_PER_THREAD; i++) {
                    String userAgent = "userAgent" + random.nextInt(10);
                    String remoteAddr = "192.168.32." + random.nextInt(256);
                    Key key = new Key(0, remoteAddr, userAgent, null);
                    expectedSlots.add(key);
                    RateLimiter.checkRateLimitFor(key, MAX_RATE, MAX_RATE_TIME_WINDOW, null);
                }
            }
        };
        for (int i = 0; i < THREAD_COUNT; i++) {
            threads[i] = new Thread(runnable);
        }
        for (int i = 0; i < THREAD_COUNT; i++) {
            threads[i].start();
        }
        for (int i = 0; i < THREAD_COUNT; i++) {
            threads[i].join();
        }
        assertEquals(THREAD_COUNT * REQUESTS_PER_THREAD, RateLimiter.getProcessedRequests());
        assertEquals(expectedSlots.size(), (int) RateLimiter.getSlotCount(true));
        Thread.sleep(MAX_RATE_TIME_WINDOW * 2);
        assertEquals(0, RateLimiter.getSlotCount(true));
    }

}
