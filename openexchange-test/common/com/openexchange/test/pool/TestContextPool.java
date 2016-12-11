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

package com.openexchange.test.pool;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * {@link TestContextPool} - This class will manage the context handling, esp. providing unused contexts and queue related requests
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.3
 */
public class TestContextPool {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(TestContextPool.class);

    private static BlockingQueue<TestContext> contexts = new LinkedBlockingQueue<>(50);

    //    private TestContextWatcher contextWatcher = new TestContextWatcher(contexts);
    private static AtomicReference<TestContextWatcher> contextWatcher = new AtomicReference<>();

    private static AtomicBoolean watcherInitialized = new AtomicBoolean(false);

    public static synchronized void addContext(TestContext context) {
        contexts.add(context);
        startWatcher();
        LOG.info("Added context '{}' with id {} to pool.", context.getName(), context.getId());
    }

    public static TestContext acquireContext() {
        synchronized (TestContextPool.class) {
            try {
                TestContext context = contexts.poll(10L, TimeUnit.SECONDS);
                contextWatcher.get().contextInUse(context);
                LOG.info("Context '{}' with id {} has been acquired.", context.getName(), context.getId(), new Throwable());
                return context;
            } catch (InterruptedException e) {
                // should not happen
                LOG.error("", e);
            }
            return null;
        }
    }

    public static void backContext(TestContext context) {
        try {
            context.reset();
            contexts.put(context);
            contextWatcher.get().contextSuccessfullyReturned(context);
        } catch (InterruptedException e) {
            // should not happen
            LOG.error("", e);
        }
    }

    public static List<Integer> getAvailableContexts() {
        List<Integer> contextIds = new ArrayList<>();
        for (TestContext context : contexts) {
            contextIds.add(context.getId());
        }
        return contextIds;
    }

    // the admin is not handled to be acquired only by one party
    private static TestUser oxAdminMaster = null;

    public static TestUser getOxAdminMaster() {
        return oxAdminMaster;
    }

    public static void setOxAdminMaster(TestUser oxAdminMaster) {
        TestContextPool.oxAdminMaster = oxAdminMaster;
    }

    public static void startWatcher() {
        if (!watcherInitialized.getAndSet(true)) {
            TestContextWatcher contextWatcherTask = new TestContextWatcher();
            contextWatcher.compareAndSet(null, contextWatcherTask);
            Thread contextWatcher = new Thread(contextWatcherTask, "TestContextWatcher");
            contextWatcher.start();
        }
    }
}
