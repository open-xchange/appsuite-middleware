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

package com.openexchange.sessiond.impl;

import java.util.List;
import junit.framework.TestCase;
import com.openexchange.threadpool.SimThreadPoolService;

/**
 * {@link Bug16158Test}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class Bug16158Test extends TestCase {

    private static final int RUNTIME = 10000;

    SessionData sessionData;
    SessionImpl session;
    private final SessionFinder[] finders = new SessionFinder[2];
    private final Thread[] finderThreads = new Thread[finders.length];
    private final SessionRotator[] rotators = new SessionRotator[1];
    private final Thread[] rotatorThreads = new Thread[rotators.length];
    private SimThreadPoolService threadPoolService;

    public Bug16158Test(final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        sessionData = new SessionData(100, 1, 60000, 167, false);
        threadPoolService = new SimThreadPoolService();
        sessionData.addThreadPoolService(threadPoolService);
        final SessionIdGenerator idGenerator = new UUIDSessionIdGenerator();
        session = new SessionImpl(-1, "bug16158", null, 0, idGenerator.createSessionId(null, null), null, idGenerator.createRandomId(), null, null, null, null, null, false);
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

    @Override
    protected void tearDown() throws Exception {
        threadPoolService.shutdown();
        super.tearDown();
    }

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
        /* testing for a proper result of this is is tricky. it may happen - due to scheduling of threads in front of the lock - that the
         * session times out - is removed from last session container by rotator thread.
         * - on timeout:
         *   + all finders must have notFound true.
         *   + exactly one rotator found the timeout.
         * - no timeout:
         *   + all finders must always have found the session.
         *   + no rotator has a timeout.
         */
        boolean wasTimeout = false;
        for (final SessionRotator rotator : rotators) {
            wasTimeout |= rotator.hasTimeout();
        }
        for (final SessionFinder finder : finders) {
            assertEquals("Expected shows timeout. Actual represents not found session.", wasTimeout, finder.hasNotFound());
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
                    final SessionControl control = sessionData.getSession(session.getSessionID());
                    notFound = null == control;
                }
            } catch (final NullPointerException e) {
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
                } catch (final InterruptedException e) {
                    e.printStackTrace();
                }
                final List<SessionControl> removed = sessionData.rotateShort();
                timeout = !removed.isEmpty();
            }
        }
    }
}
