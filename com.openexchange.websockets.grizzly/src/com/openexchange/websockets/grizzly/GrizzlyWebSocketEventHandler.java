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
                } catch (Exception e) {
                    LOG.error("Error while handling SessionD event \"{}\".", topic, e);
                }
            }
        }
    } // End of PushEventHandlerRunnable

}
