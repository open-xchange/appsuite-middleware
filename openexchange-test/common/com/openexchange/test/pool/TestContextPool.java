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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.commons.lang.SerializationUtils;
import org.junit.Assert;
import com.openexchange.java.ConcurrentList;
import com.openexchange.java.Strings;

/**
 * {@link TestContextPool} - This class will manage the context handling, esp. providing unused contexts and queue related requests
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.3
 */
public class TestContextPool {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(TestContextPool.class);

    private static BlockingQueue<TestContext> contexts = new LinkedBlockingQueue<>(50);

    private static List<TestContext> allTimeContexts = new ConcurrentList<>();

    private static AtomicReference<TestContextWatcher> contextWatcher = new AtomicReference<>();

    private static AtomicBoolean watcherInitialized = new AtomicBoolean(false);

    public static synchronized void addContext(TestContext context) {
        remember(context);
        contexts.add(context);
        startWatcher();
        LOG.info("Added context '{}' with users {} to pool.", context.getName(), Strings.concat(",", context.getCopyOfAll()));
    }

    private static void remember(TestContext context) {
        if (allTimeContexts.contains(context)) {
            return;
        }
        allTimeContexts.add((TestContext) SerializationUtils.clone(context));
        LOG.info("Added context {} to all time available context list.", context.getName());
    }

    /**
     * Returns an exclusive {@link TestContext} which means this context is currently not used by any other test.<br>
     * <br>
     * <b>Caution: After using the {@link TestContext} make sure it will be returned to pool by using {@link #backContext(TestContext)}!</b>
     * 
     * @param acquiredBy The name of the class that acquires the context (for logging purposes)
     * @return {@link TestContext} to be used for tests.
     */
    public static TestContext acquireContext(String acquiredBy) {
        try {
            TestContext context = contexts.take();
            Assert.assertNotNull("Unable to acquire test context due to an empty pool.", context);
            context.setAcquiredBy(acquiredBy);
            contextWatcher.get().contextInUse(context);
            LOG.debug("Context '{}' has been acquired by {}.", context.getName(), acquiredBy);
            return context;
        } catch (InterruptedException e) {
            // should not happen
            LOG.error("", e);
        }
        return null;
    }

    public static void backContext(TestContext context) {
        if (context == null) {
            return;
        }
        contextWatcher.get().contextSuccessfullyReturned(context);
        if (contexts.contains(context)) {
            return;
        }
        try {
            context.reset();
            contexts.put(context);
        } catch (InterruptedException e) {
            // should not happen
            LOG.error("", e);
        }
    }

    // the admin is not handled to be acquired only by one party
    private static TestUser oxAdminMaster = null;

    public static TestUser getOxAdminMaster() {
        return oxAdminMaster;
    }

    public static void setOxAdminMaster(TestUser oxAdminMaster) {
        TestContextPool.oxAdminMaster = oxAdminMaster;
    }

    // the rest admin user is not handled to be acquired only by one party
    private static TestUser restUser = null;

    public static String getRestAuth() {
        return restUser.getUser() + ":" + restUser.getPassword();
    }

    public static TestUser getRestUser() {
        return restUser;
    }

    public static void setRestUser(TestUser restUser) {
        TestContextPool.restUser = restUser;
    }

    public static void startWatcher() {
        if (!watcherInitialized.getAndSet(true)) {
            TestContextWatcher contextWatcherTask = new TestContextWatcher();
            contextWatcher.compareAndSet(null, contextWatcherTask);
            Thread contextWatcherThread = new Thread(contextWatcherTask, "TestContextWatcher");
            contextWatcherThread.start();
        }
    }

    public static synchronized List<TestContext> getAllTimeAvailableContexts() {
        List<TestContext> cloned = new ArrayList<>(allTimeContexts.size());
        for (TestContext current : allTimeContexts) {
            cloned.add((TestContext) deepClone(current));
        }
        return cloned;
    }

    private static Object deepClone(Object object) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(object);
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bais);
            return ois.readObject();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
