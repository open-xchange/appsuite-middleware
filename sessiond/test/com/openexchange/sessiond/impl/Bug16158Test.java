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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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
import com.openexchange.session.SimSession;
import com.openexchange.threadpool.SimThreadPoolService;

/**
 * {@link Bug16158Test}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class Bug16158Test extends TestCase {

    private static final int RUNTIME = 10000;

    SessionData sessionData;
    SimSession session;
    private SessionFinder[] finders = new SessionFinder[2];
    private Thread[] finderThreads = new Thread[finders.length];
    private SessionRotator[] rotators = new SessionRotator[2];
    private Thread[] rotatorThreads = new Thread[rotators.length];
    private SimThreadPoolService threadPoolService;

    public Bug16158Test(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        sessionData = new SessionData(10, 1);
        threadPoolService = new SimThreadPoolService();
        sessionData.addThreadPoolService(threadPoolService);
        SessionIdGenerator idGenerator = new UUIDSessionIdGenerator();
        session = new SimSession();
        session.setSessionID(idGenerator.createSessionId(null, null));
        session.setLoginName("bug16158");
        session.setRandomToken(idGenerator.createRandomId());
        sessionData.addSession(session, RUNTIME * 2, true);
        for (int i = 0; i < finders.length; i++) {
            finders[i] = new SessionFinder();
            finderThreads[i] = new Thread(finders[i]);
        }
        for (int i = 0; i < rotators.length; i++) {
            rotators[i] = new SessionRotator();
            rotatorThreads[i] = new Thread(rotators[i]);
            
        }
        for (Thread finderThread : finderThreads) {
            finderThread.start();
        }
        for (Thread rotatorThread : rotatorThreads) {
            rotatorThread.start();
        }
    }

    @Override
    protected void tearDown() throws Exception {
        for (Thread rotatorThread : rotatorThreads) {
            rotatorThread.join();
        }
        for (Thread finderThread : finderThreads) {
            finderThread.join();
        }
        threadPoolService.shutdown();
        super.tearDown();
    }

    public void testNotFoundSession() throws Throwable {
        Thread.sleep(RUNTIME);
        for (SessionRotator rotator : rotators) {
            rotator.stop();
        }
        for (SessionFinder finder : finders) {
            finder.stop();
        }
        for (SessionFinder finder : finders) {
            assertFalse("A thread did not find the session.", finder.hasNotFound());
        }
        for (SessionRotator rotator : rotators) {
            assertFalse("Session timed out.", rotator.hasTimeout());
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
        public void run() {
            try {
                while (run && !notFound) {
                    SessionControl control = sessionData.getSession(session.getSessionID());
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
        public void run() {
            while (run && !timeout) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                List<SessionControl> removed = sessionData.rotate();
                timeout = !removed.isEmpty();
            }
        }
    }
}
