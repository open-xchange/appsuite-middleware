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

package com.openexchange.sessiond.impl;

import static com.openexchange.java.Autoboxing.B;
import static org.junit.Assert.assertEquals;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.sessiond.impl.container.SessionControl;
import com.openexchange.sessiond.impl.util.RotateShortResult;
import com.openexchange.threadpool.SimThreadPoolService;

/**
 * {@link Bug16158Test}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class Bug16158Test {
    private static final int RUNTIME = 10000;

    SessionData sessionData;
    SessionImpl session;
    private final SessionFinder[] finders = new SessionFinder[2];
    private final Thread[] finderThreads = new Thread[finders.length];
    private final SessionRotator[] rotators = new SessionRotator[1];
    private final Thread[] rotatorThreads = new Thread[rotators.length];
    private SimThreadPoolService threadPoolService;

    @Before
    public void setUp() throws Exception {
        sessionData = new SessionData(100, 1, 60000, 167);
        threadPoolService = new SimThreadPoolService();
        sessionData.addThreadPoolService(threadPoolService);
        final SessionIdGenerator idGenerator = UUIDSessionIdGenerator.getInstance();
        session = new SessionImpl(-1, "bug16158", null, 0, idGenerator.createSessionId(null), null, idGenerator.createRandomId(), null, null, null, null, null, false, false, null);
        sessionData.addSession(session, true);
        for (int i = 0; i < finders.length; i++) {
            finders[i] = new SessionFinder();
            finderThreads[i] = new Thread(finders[i]);
        }
        for (int i = 0; i < rotators.length; i++) {
            rotators[i] = new SessionRotator();
            rotatorThreads[i] = new Thread(rotators[i]);

        }
    }

    @After
    public void tearDown() {
        threadPoolService.shutdown();
    }

    @Test
    public void testNotFoundSession() throws Throwable {
        for (final Thread finderThread : finderThreads) {
            finderThread.start();
        }
        for (final Thread rotatorThread : rotatorThreads) {
            rotatorThread.start();
        }
        Thread.sleep(RUNTIME);
        for (final SessionRotator rotator : rotators) {
            rotator.stop();
        }
        for (final Thread rotatorThread : rotatorThreads) {
            rotatorThread.join();
        }

        //ensure that the finders are able to set notFound to true if no session is present.
        Thread.sleep(50);

        for (final SessionFinder finder : finders) {
            finder.stop();
        }
        for (final Thread finderThread : finderThreads) {
            finderThread.join();
        }
        /*
         * testing for a proper result of this is is tricky. it may happen - due to scheduling of threads in front of the lock - that the
         * session times out - is removed from last session container by rotator thread.
         * - on timeout:
         * + all finders must have notFound true.
         * + exactly one rotator found the timeout.
         * - no timeout:
         * + all finders must always have found the session.
         * + no rotator has a timeout.
         */
        boolean wasTimeout = false;
        for (final SessionRotator rotator : rotators) {
            wasTimeout |= rotator.hasTimeout();
        }
        for (final SessionFinder finder : finders) {
            assertEquals("Expected shows timeout. Actual represents not found session.", B(wasTimeout), B(finder.hasNotFound()));
        }
        if (wasTimeout) {
            int foundTimeouts = 0;
            for (final SessionRotator rotator : rotators) {
                if (rotator.hasTimeout()) {
                    foundTimeouts++;
                }
            }
            assertEquals("Only a single rotator should have a timeout.", 1, foundTimeouts);
        }
    }

    private class SessionFinder implements Runnable {

        private boolean run = true;
        private boolean notFound = false;

        SessionFinder() {
            super();
        }

        boolean hasNotFound() {
            return notFound;
        }

        void stop() {
            run = false;
        }

        @Override
        public void run() {
            try {
                while (run && !notFound) {
                    final SessionControl control = sessionData.getSession(session.getSessionID(), false);
                    notFound = null == control;
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
                notFound = true;
            }
        }
    }

    private class SessionRotator implements Runnable {

        private boolean run = true;
        private boolean timeout = false;

        SessionRotator() {
            super();
        }

        boolean hasTimeout() {
            return timeout;
        }

        void stop() {
            run = false;
        }

        @Override
        public void run() {
            while (run && !timeout) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                final RotateShortResult removed = sessionData.rotateShort();
                timeout = !removed.isEmpty();
            }
        }
    }
}
