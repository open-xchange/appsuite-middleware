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

package com.openexchange.continuation;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * {@link ExecutorContinuationTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.6.0
 */
public class ExecutorContinuationTest {

    private ContinuationRegistryService registry;

    /**
     * Initializes a new {@link ExecutorContinuationTest}.
     */
    public ExecutorContinuationTest() {
        super();
    }

    @Before
    public void setUp() {
        registry = new SimContinuationRegistryService();
    }

    @After
    public void tearDown() {
        registry = null;
    }

    @Test
    public void testExecutorContinuation() {
        try {
            ExecutorContinuation<String> executorContinuation = ExecutorContinuation.newContinuation(Executors.newCachedThreadPool());

            final CountDownLatch cdl = new CountDownLatch(1);

            executorContinuation.submit(new Callable<Collection<String>>() {

                @Override
                public Collection<String> call() throws Exception {
                    cdl.await();
                    return Collections.singletonList("0");
                }
            });
            executorContinuation.submit(new Callable<Collection<String>>() {

                @Override
                public Collection<String> call() throws Exception {
                    cdl.await();
                    Thread.sleep(2000);
                    return Collections.singletonList("1");
                }
            });
            executorContinuation.submit(new Callable<Collection<String>>() {

                @Override
                public Collection<String> call() throws Exception {
                    cdl.await();
                    Thread.sleep(4000);
                    return Collections.singletonList("2");
                }
            });
            executorContinuation.submit(new Callable<Collection<String>>() {

                @Override
                public Collection<String> call() throws Exception {
                    cdl.await();
                    Thread.sleep(6000);
                    return Collections.singletonList("3");
                }
            });
            executorContinuation.submit(new Callable<Collection<String>>() {

                @Override
                public Collection<String> call() throws Exception {
                    cdl.await();
                    Thread.sleep(8000);
                    return Collections.singletonList("4");
                }
            });

            // Assert null
            ContinuationResponse<Collection<String>> cr = executorContinuation.getNextResponse(1, TimeUnit.SECONDS);
            Assert.assertNull("Got a non-null result, although nothing in progress, yet", cr.getValue());

            // Release threads
            cdl.countDown();

            // "0" immediately available
            cr = executorContinuation.getNextResponse(1, TimeUnit.SECONDS);
            Assert.assertFalse("Huh...?", cr.isCompleted());
            Assert.assertNotNull("Got null...", cr.getValue());
            Assert.assertEquals("Unexpected values: " + cr.getValue(), "0", cr.getValue().iterator().next());

            // "1" after 2sec
            cr = executorContinuation.getNextResponse(2200, TimeUnit.MILLISECONDS);
            Assert.assertFalse("Huh...?", cr.isCompleted());
            Assert.assertNotNull("Got null...", cr.getValue());
            Assert.assertEquals("Unexpected values: " + cr.getValue(), Arrays.asList("0", "1"), cr.getValue());

            // "2" and "3" after 6sec
            cr = executorContinuation.getNextResponse(4200, TimeUnit.MILLISECONDS);
            Assert.assertFalse("Huh...?", cr.isCompleted());
            Assert.assertNotNull("Got null...", cr.getValue());
            Assert.assertEquals("Unexpected values: " + cr.getValue(), Arrays.asList("0", "1", "2", "3"), cr.getValue());

            // Too early
            cr = executorContinuation.getNextResponse(200, TimeUnit.MILLISECONDS);
            Assert.assertNull("Got a non-null result, although nothing in progress, yet", cr.getValue());

            // "4" after 8sec
            cr = executorContinuation.getNextResponse(2200, TimeUnit.MILLISECONDS);
            Assert.assertTrue("Huh...?", cr.isCompleted());
            Assert.assertNotNull("Got null...", cr.getValue());
            Assert.assertEquals("Unexpected values: " + cr.getValue(), Arrays.asList("0", "1", "2", "3", "4"), cr.getValue());

            // Try again
            cr = executorContinuation.getNextResponse(200, TimeUnit.MILLISECONDS);
            Assert.assertNull("Got a non-null result, although nothing in progress, yet", cr.getValue());

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

}
