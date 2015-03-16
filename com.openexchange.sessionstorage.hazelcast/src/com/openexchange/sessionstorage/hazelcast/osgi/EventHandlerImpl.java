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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.sessionstorage.hazelcast.osgi;

import java.util.concurrent.Future;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessiondEventConstants;
import com.openexchange.sessionstorage.hazelcast.HazelcastSessionStorageService;
import com.openexchange.threadpool.AbstractTask;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.behavior.CallerRunsBehavior;
import com.openexchange.timer.TimerService;

/**
 * {@link EventHandlerImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
final class EventHandlerImpl implements EventHandler {

    private final HazelcastSessionStorageService sessionStorage;
    private final ServiceLookup services;

    EventHandlerImpl(HazelcastSessionStorageService sessionStorage, ServiceLookup services) {
        super();
        this.sessionStorage = sessionStorage;
        this.services = services;
    }

    @Override
    public void handleEvent(Event osgiEvent) {
        if (null != osgiEvent && SessiondEventConstants.TOPIC_TOUCH_SESSION.equals(osgiEvent.getTopic())) {
            Session touchedSession = (Session) osgiEvent.getProperty(SessiondEventConstants.PROP_SESSION);
            if (null != touchedSession && null != touchedSession.getSessionID()) {
                // Handle session-touched event asynchronously if possible
                String sessionId = touchedSession.getSessionID();
                ThreadPoolService threadPool = services.getService(ThreadPoolService.class);
                TimerService timerService = services.getService(TimerService.class);
                if (null == threadPool || null == timerService) {
                    try {
                        sessionStorage.touch(sessionId);
                    } catch (Exception e) {
                        HazelcastSessionStorageActivator.LOG.warn("error handling OSGi event", e);
                    }
                } else {
                    asyncTouch(sessionId, sessionStorage, threadPool, timerService);
                }
            }
        }
    }

    private void asyncTouch(final String sessionId, final HazelcastSessionStorageService sessionStorage, ThreadPoolService threadPool, TimerService timerService) {
        AbstractTask<Void> task = new AbstractTask<Void>() {

            @Override
            public Void call() throws Exception {
                try {
                    sessionStorage.touch(sessionId);
                } catch (Exception e) {
                    HazelcastSessionStorageActivator.LOG.warn("error handling OSGi event", e);
                }
                return null;
            }
        };
        final Future<Void> submitted = threadPool.submit(task, CallerRunsBehavior.<Void> getInstance());
        timerService.schedule(new Runnable() {

            @Override
            public void run() {
                if (false == submitted.isDone()) {
                    try {
                        submitted.cancel(true);
                    } catch (Exception e) {
                        // Ignore
                    }
                }
            }
        }, 3000L);
    }
}
