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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.websockets.grizzly;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessiondEventConstants;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.ThreadPools;
import com.openexchange.threadpool.behavior.CallerRunsBehavior;

/**
 * {@link GrizzlyWebSocketEventHandler}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class GrizzlyWebSocketEventHandler implements EventHandler {

    /** The logger constant */
    static final Logger LOG = org.slf4j.LoggerFactory.getLogger(GrizzlyWebSocketEventHandler.class);

    final Queue<AbstractGrizzlyWebSocketApplication<?>> apps;

    /**
     * Initializes a new {@link GrizzlyWebSocketEventHandler}.
     */
    public GrizzlyWebSocketEventHandler() {
        super();
        apps = new ConcurrentLinkedQueue<>();
    }

    /**
     * Adds specified application.
     *
     * @param app The application to add
     */
    public void addApp(AbstractGrizzlyWebSocketApplication<?> app) {
        apps.offer(app);
    }

    /**
     * Removes specified application.
     *
     * @param app The application to remove
     */
    public void removeApp(AbstractGrizzlyWebSocketApplication<?> app) {
        apps.remove(app);
    }

    @Override
    public void handleEvent(final Event event) {
        final Runnable r = new GrizzlyWebSocketEventHandlerRunnable(event);
        /*
         * Delegate to thread pool if present
         */
        final ThreadPoolService threadPoolService = ThreadPools.getThreadPool();
        if (null == threadPoolService) {
            r.run();
        } else {
            threadPoolService.submit(ThreadPools.task(r), CallerRunsBehavior.getInstance());
        }
    }

    private final class GrizzlyWebSocketEventHandlerRunnable implements Runnable {

        private final Event event;

        /**
         * Initializes a new {@link GrizzlyWebSocketEventHandlerRunnable}.
         *
         * @param event The event to handle
         */
        protected GrizzlyWebSocketEventHandlerRunnable(Event event) {
            super();
            this.event = event;
        }

        @Override
        public void run() {
            String topic = event.getTopic();

            for (AbstractGrizzlyWebSocketApplication<?> app : apps) {
                try {
                    if (SessiondEventConstants.TOPIC_REMOVE_SESSION.equals(topic)) {
                        Session session = (Session) event.getProperty(SessiondEventConstants.PROP_SESSION);
                        app.closeWebSocketForSession(session.getSessionID(), session.getUserId(), session.getContextId());
                    } else if (SessiondEventConstants.TOPIC_REMOVE_DATA.equals(topic) || SessiondEventConstants.TOPIC_REMOVE_CONTAINER.equals(topic)) {
                        @SuppressWarnings("unchecked")
                        Map<String, Session> sessionContainer = (Map<String, Session>) event.getProperty(SessiondEventConstants.PROP_CONTAINER);
                        for (Session session : sessionContainer.values()) {
                            app.closeWebSocketForSession(session.getSessionID(), session.getUserId(), session.getContextId());
                        }
                    }
                } catch (final Exception e) {
                    LOG.error("Error while handling SessionD event \"{}\".", topic, e);
                }
            }
        }
    } // End of PushEventHandlerRunnable

}
