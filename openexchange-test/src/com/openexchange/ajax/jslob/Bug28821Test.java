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

package com.openexchange.ajax.jslob;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import org.junit.AfterClass;
import org.junit.BeforeClass;
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

    /**
     * Initializes a new {@link Bug28821Test}.
     *
     * @param name
     */
    public Bug28821Test(final String name) {
        super(name);
    }

    @BeforeClass
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @AfterClass
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void testBug28821() {
        try {

            final int length = NUM_THREADS;
            ListAction[] actions = new ListAction[length];

            final CountDownLatch startLatch = new CountDownLatch(1);
            final CountDownLatch finishedLatch = new CountDownLatch(length);

            {
                final ListAction listAction = new ListAction(startLatch, finishedLatch, client, "io.ox/portal", "io.ox/mail", "io.ox/contacts");
                actions[0] = listAction;
                final Thread thread = new Thread(listAction);
                thread.start();
            }
            {
                final ListAction listAction = new ListAction(startLatch, finishedLatch, client, "io.ox/settings/configjump", "io.ox/calendar");
                actions[1] = listAction;
                final Thread thread = new Thread(listAction);
                thread.start();
            }
            {
                final ListAction listAction = new ListAction(startLatch, finishedLatch, client, "io.ox/files");
                actions[2] = listAction;
                final Thread thread = new Thread(listAction);
                thread.start();
            }
            {
                final ListAction listAction = new ListAction(startLatch, finishedLatch, client, "io.ox/core", "io.ox/core/updates");
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
