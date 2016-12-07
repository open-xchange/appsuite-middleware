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

package com.openexchange.ajax.config;

import static org.junit.Assert.fail;
import org.apache.http.params.HttpConnectionParams;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.config.actions.GetRequest;
import com.openexchange.ajax.config.actions.SetRequest;
import com.openexchange.ajax.config.actions.Tree;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.test.pool.TestUser;

/**
 * If 2 threads have been writing the same configuration value the internal compare and set method failed and entered an endless loop. This
 * test can only try to cause this endless loop but it can not verify that this endless loop does not occur.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class Bug21619Test extends AbstractAJAXSession {

    @SuppressWarnings("hiding")
    private AJAXClient client;
    private String origValue;
    private final ValueWriter[] writers = new ValueWriter[2];
    private final Thread[] threads = new Thread[writers.length];

    public Bug21619Test() {
        super();
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        client = getClient();
        origValue = client.execute(new GetRequest(Tree.TaskUIConfiguration)).getString();
        for (int i = 0; i < writers.length; i++) {
            writers[i] = new ValueWriter(testUser);
        }
    }

    @After
    public void tearDown() throws Exception {
        try {
            client.execute(new SetRequest(Tree.TaskUIConfiguration, origValue));
        } finally {
            super.tearDown();
        }
    }

    @Test
    public void testForBug() throws InterruptedException {
        for (int i = 0; i < writers.length; i++) {
            threads[i] = new Thread(writers[i]);
            threads[i].start();
        }
        Thread.sleep(20000);
        for (final ValueWriter writer : writers) {
            writer.stop();
        }
        for (final Thread thread : threads) {
            thread.join();
        }
        for (final ValueWriter writer : writers) {
            if (null != writer.getThrowable()) {
                fail(writer.getThrowable().getMessage());
            }
        }
    }

    private static class ValueWriter implements Runnable {

        private boolean run = true;
        private Throwable t;
        private TestUser testUser;

        ValueWriter(TestUser testUser) {
            super();
            this.testUser = testUser;
        }

        public void stop() {
            run = false;
        }

        public Throwable getThrowable() {
            return t;
        }

        @Override
        public void run() {
            AJAXClient client = null;
            try {
                client = new AJAXClient(testUser);
                HttpConnectionParams.setConnectionTimeout(client.getSession().getHttpClient().getParams(), 10000);
                int value = 1;
                while (run) {
                    client.execute(new SetRequest(Tree.TaskUIConfiguration, Integer.toString(value++)));
                }
            } catch (Throwable t1) {
                t = t1;
            } finally {
                if (null != client) {
                    try {
                        client.logout();
                    } catch (final Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
