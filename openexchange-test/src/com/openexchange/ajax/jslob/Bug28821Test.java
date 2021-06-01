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

package com.openexchange.ajax.jslob;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import org.junit.Test;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.jslob.actions.ListRequest;
import com.openexchange.ajax.jslob.actions.ListResponse;
import com.openexchange.jslob.JSlob;

/**
 * {@link Bug28821Test}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class Bug28821Test extends AbstractJSlobTest {

    private static final int NUM_THREADS = 4;

    @Test
    public void testBug28821() {
        try {

            final int length = NUM_THREADS;
            ListAction[] actions = new ListAction[length];

            final CountDownLatch startLatch = new CountDownLatch(1);
            final CountDownLatch finishedLatch = new CountDownLatch(length);

            {
                final ListAction listAction = new ListAction(startLatch, finishedLatch, getClient(), "io.ox/portal", "io.ox/mail", "io.ox/contacts");
                actions[0] = listAction;
                final Thread thread = new Thread(listAction);
                thread.start();
            }
            {
                final ListAction listAction = new ListAction(startLatch, finishedLatch, getClient(), "io.ox/settings/configjump", "io.ox/calendar");
                actions[1] = listAction;
                final Thread thread = new Thread(listAction);
                thread.start();
            }
            {
                final ListAction listAction = new ListAction(startLatch, finishedLatch, getClient(), "io.ox/files");
                actions[2] = listAction;
                final Thread thread = new Thread(listAction);
                thread.start();
            }
            {
                final ListAction listAction = new ListAction(startLatch, finishedLatch, getClient(), "io.ox/core", "io.ox/core/updates");
                actions[3] = listAction;
                final Thread thread = new Thread(listAction);
                thread.start();
            }

            // Release threads
            startLatch.countDown();

            // Await completion
            finishedLatch.await();

            for (int i = 0; i < actions.length; i++) {
                final Throwable throwable = actions[i].getThrowable();
                assertNull("An error occurred: " + throwable, throwable);
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    private static final class ListAction implements Runnable {

        private final AJAXClient client;
        private final String[] identifiers;
        private final CountDownLatch startLatch;
        private final CountDownLatch finishedLatch;
        private Throwable throwable;

        ListAction(CountDownLatch startLatch, CountDownLatch finishedLatch, AJAXClient client, final String... identifiers) {
            super();
            this.startLatch = startLatch;
            this.finishedLatch = finishedLatch;
            this.client = client;
            this.identifiers = identifiers;
        }

        public Throwable getThrowable() {
            return throwable;
        }

        @Override
        public void run() {
            try {
                startLatch.await();

                String[] identifiers = this.identifiers;
                ListRequest listRequest = new ListRequest(identifiers);
                ListResponse listResponse = client.execute(listRequest);

                List<JSlob> jSlobs = listResponse.getJSlobs();

                assertEquals("Number of JSlobs does not match.", identifiers.length, jSlobs.size());

                int length = identifiers.length;
                for (int i = 0; i < length; i++) {
                    final String id = identifiers[i];
                    boolean found = false;

                    for (final Iterator<JSlob> iterator = jSlobs.iterator(); !found && iterator.hasNext();) {
                        final JSlob jSlob = iterator.next();
                        found = id.equals(jSlob.getId().getId());
                    }

                    assertTrue("JSlob not found: " + id, found);
                }
            } catch (Exception e) {
                throwable = e;
            } catch (AssertionError e) {
                throwable = e;
            } finally {
                finishedLatch.countDown();
            }
        }
    } // End of class ListAction

}
