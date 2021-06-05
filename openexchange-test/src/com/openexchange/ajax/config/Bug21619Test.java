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

package com.openexchange.ajax.config;

import static org.junit.Assert.fail;
import org.apache.http.params.HttpConnectionParams;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.config.actions.SetRequest;
import com.openexchange.ajax.config.actions.Tree;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.test.common.test.pool.TestUser;

/**
 * If 2 threads have been writing the same configuration value the internal compare and set method failed and entered an endless loop. This
 * test can only try to cause this endless loop but it can not verify that this endless loop does not occur.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class Bug21619Test extends AbstractAJAXSession {

    private final ValueWriter[] writers = new ValueWriter[2];
    private final Thread[] threads = new Thread[writers.length];

    public Bug21619Test() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        for (int i = 0; i < writers.length; i++) {
            writers[i] = new ValueWriter(testUser);
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
        private final TestUser testUser;

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
                client = testUser.getAjaxClient();
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
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
