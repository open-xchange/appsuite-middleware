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

package com.openexchange.test.common.test.pool;

import static com.openexchange.java.Autoboxing.b;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.mail.internet.AddressException;
import org.junit.Assert;
import com.openexchange.admin.rmi.exceptions.ContextExistsException;
import com.openexchange.admin.rmi.exceptions.DatabaseUpdateException;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.exception.OXException;
import com.openexchange.java.ConcurrentList;
import com.openexchange.test.common.configuration.AJAXConfig;

/**
 * {@link TestContextPool} - This class will manage the context handling, esp. providing unused contexts and queue related requests
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.3
 */
public class TestContextPool {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(TestContextPool.class);

    private static AtomicBoolean initialized = new AtomicBoolean(false);

    private static List<TestContext> allTimeContexts = new ConcurrentList<>();

    private static Semaphore semaphore = new Semaphore(20, true);

    private static final boolean DELETE_AFTER_USAGE = b(Boolean.valueOf(AJAXConfig.getProperty(AJAXConfig.Property.DELETE_CONTEXT, Boolean.TRUE.toString())));
    private static final boolean PRE_PROVISION_CONTEXTS = b(Boolean.valueOf(AJAXConfig.getProperty(AJAXConfig.Property.PRE_PROVISION_CONTEXTS, Boolean.TRUE.toString())));

    /**
     * Initializes the {@link TestContextPool}
     *
     * Pre-provisions 2000 context if configured
     */
    public static void init() {
        if (initialized.get() == false) {
            synchronized (initialized) {
                if (initialized.get() == false) {
                    if (false == PRE_PROVISION_CONTEXTS) {
                        initialized.set(true);
                        return;
                    }

                    ExecutorService executor = Executors.newFixedThreadPool(10);
                    int taskSize = 20;
                    List<Callable<Void>> tasks = new ArrayList<>(taskSize);
                    for (int i = 0; i < taskSize; i++) {
                        tasks.add(new CreateHundredContextTask(pool));
                    }

                    try {
                        executor.invokeAll(tasks);
                        executor.shutdown();
                    } catch (InterruptedException e) {
                        LOG.debug("Failed", e);
                    }
                    initialized.set(true);
                }
            }
        }
    }

    /**
     * Deletes pre-provisioned context that weren't used
     */
    public static void down() {
        TestContext context = pool.poll();
        while (null != context) {
            try {
                ProvisioningService.getInstance().deleteContext(context.getId());
            } catch (RemoteException | StorageException | InvalidCredentialsException | InvalidDataException | NoSuchContextException | DatabaseUpdateException | MalformedURLException | NotBoundException e) {
                LOG.warn("unable to delete context");
            }
            context = pool.poll();
        }
    }

    /**
     * Returns an exclusive {@link TestContext} which means this context is currently not used by any other test.<br>
     * <br>
     * <b>Caution: After using the {@link TestContext} make sure it will be returned to pool by using {@link #backContext(TestContext)}!</b>
     *
     * @param acquiredBy The name of the class that acquires the context (for logging purposes)
     * @return {@link TestContext} to be used for tests.
     * @throws OXException In case context can't be aquired
     */
    public static TestContext acquireContext(String acquiredBy) throws OXException {
        return acquireContext(acquiredBy, Optional.empty());
    }

    /**
     * Returns an exclusive {@link TestContext} which means this context is currently not used by any other test.<br>
     * <br>
     * <b>Caution: After using the {@link TestContext} make sure it will be returned to pool by using {@link #backContext(TestContext)}!</b>
     *
     * @param acquiredBy The name of the class that acquires the context (for logging purposes)
     * @param optConfig The optional config for the contexts
     * @return {@link TestContext} to be used for tests.
     * @throws OXException In case context can't be acquired
     */
    public static TestContext acquireContext(String acquiredBy, Optional<Map<String, String>> optConfig) throws OXException {
        List<TestContext> contextList;
        try {
            contextList = aquireContexts(1, optConfig);
            Assert.assertFalse("Unable to acquire test context due to an empty pool.", contextList == null || contextList.isEmpty());
            TestContext context = contextList.get(0);
            LOG.debug("Context '{}' has been acquired by {}.", context.getName(), acquiredBy);
            return context;
        } catch (RemoteException | StorageException | InvalidCredentialsException | InvalidDataException | ContextExistsException | MalformedURLException | NotBoundException | AddressException e) {
            // Should not happen
            LOG.error(e.getMessage());
            throw new OXException(e);
        }
    }

    /**
     * Similar to {@link #acquireContext(String)} but returns any number of contexts instead of only one <br>
     * <br>
     * <b>Caution: After using the {@link TestContext}s make sure to return them to the pool by using {@link #backContext(List)}!</b>
     *
     * @param acquiredBy The name of the class that acquires the context (for logging purposes)
     * @param amount The amount of contexts to aquire
     * @param optConfig The optional ctx config
     * @return a list of {@link TestContext} to be used for tests.
     * @throws OXException In case context can't be acquired
     */
    public static List<TestContext> acquireContext(String acquiredBy, Optional<Map<String, String>> optConfig, int amount) throws OXException {
        try {
            List<TestContext> result = aquireContexts(amount, optConfig);
            Assert.assertFalse("Unable to acquire test context due to an empty pool.", result == null || result.isEmpty());
            result.forEach((c) -> {
                LOG.info("Context '{}' has been acquired by {}.", c.getName(), acquiredBy);
            });

            return result;
        } catch (RemoteException | StorageException | InvalidCredentialsException | InvalidDataException | ContextExistsException | MalformedURLException | NotBoundException | AddressException e) {
            // Should not happen
            LOG.error(e.getMessage());
            Assert.fail("Error: " + e.getMessage());
            throw new OXException(e);
        }
    }

    private static Queue<TestContext> pool = new LinkedBlockingDeque<TestContext>(180);

    private static Optional<TestContext> tryPool() {
        return Optional.ofNullable(pool.poll());
    }

    private static List<TestContext> aquireContexts(int amount, Optional<Map<String, String>> optConfig) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException, ContextExistsException, MalformedURLException, NotBoundException, AddressException {
        if (initialized.get() == false) {
            init();
        }
        List<TestContext> result = new ArrayList<TestContext>(amount);
        try {
            Assert.assertTrue("Aquisation of semaphore took too long", semaphore.tryAcquire(amount, 10, TimeUnit.MINUTES));
        } catch (@SuppressWarnings("unused") InterruptedException e) {
            Assert.fail("Unable to aquire context");
            return null;
        }
        boolean success = false;
        try {
            for (int i = amount; i > 0; i--) {
                if (optConfig.isPresent()) {
                    result.add(ProvisioningService.getInstance().createContext(optConfig));
                } else {
                    Optional<TestContext> optContext = tryPool();
                    if (optContext.isPresent()) {
                        result.add(optContext.get());
                    } else {
                        result.add(ProvisioningService.getInstance().createContext());
                    }
                }
            }
            success = true;
        } finally {
            // release samphore in case context creation fails
            if (success == false) {
                semaphore.release(amount);
            }
        }
        return result;
    }

    public static void backContext(List<TestContext> contexts) throws OXException {
        if (contexts != null) {
            for (TestContext context : contexts) {
                backContext(context);
            }
        }
    }

    public static void backContext(TestContext context) throws OXException {
        semaphore.release();
        if (context == null || DELETE_AFTER_USAGE == false) {
            return;
        }
        try {
            ProvisioningService.getInstance().deleteContext(context.getId());
        } catch (NoSuchContextException e) {
            // ignore
            LOG.debug("Context already deleted", e);
        } catch (RemoteException | StorageException | InvalidCredentialsException | InvalidDataException | DatabaseUpdateException | MalformedURLException | NotBoundException e) {
            // should not happen
            LOG.error("", e);
            throw new OXException(e);
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

    /**
     * 
     * {@link CreateHundredContextTask}
     *
     * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
     * @since v8.0.0
     */
    private static class CreateHundredContextTask implements Callable<Void> {

        private Queue<TestContext> poolReference;

        public CreateHundredContextTask(Queue<TestContext> pool) {
            super();
            this.poolReference = pool;
        }

        @Override
        public Void call() throws Exception {
            for (int x = 0; x < 100; x++) {
                TestContext ctx = ProvisioningService.getInstance().createContext();
                poolReference.add(ctx);
            }
            return null;
        }

    }

}
