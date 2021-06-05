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

package com.openexchange.push.impl;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import com.google.common.collect.ImmutableSet;
import com.openexchange.log.LogProperties;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessiondEventConstants;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.ThreadPools;
import com.openexchange.threadpool.behavior.CallerRunsBehavior;

/**
 * {@link PushEventHandler} - The {@link EventHandler event handler} for mail push bundle to track newly created and removed sessions.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class PushEventHandler implements EventHandler {

    /** The logger */
    protected static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(PushEventHandler.class);

    /** The topics to consider as an added session */
    protected static final Set<String> CONSIDER_ADDED = ImmutableSet.of(SessiondEventConstants.TOPIC_ADD_SESSION, SessiondEventConstants.TOPIC_REACTIVATE_SESSION, SessiondEventConstants.TOPIC_RESTORED_SESSION);

    /**
     * Initializes a new {@link PushEventHandler}.
     */
    public PushEventHandler() {
        super();
    }

    @Override
    public void handleEvent(final Event event) {
        final Runnable r = new PushEventHandlerRunnable(event);
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

    private static final class PushEventHandlerRunnable implements Runnable {

        private final Event event;

        /**
         * Initializes a new {@link PushEventHandlerRunnable}.
         *
         * @param event The event to handle
         */
        protected PushEventHandlerRunnable(final Event event) {
            super();
            this.event = event;
        }

        @Override
        public void run() {
            final String topic = event.getTopic();
            try {
                if (SessiondEventConstants.TOPIC_REMOVE_SESSION.equals(topic)) {
                    Session session = (Session) event.getProperty(SessiondEventConstants.PROP_SESSION);
                    LogProperties.putSessionProperties(session);
                    try {
                        PushManagerRegistry registry = PushManagerRegistry.getInstance();
                        registry.stopListenerFor(session);
                    } finally {
                        LogProperties.removeSessionProperties();
                    }
                } else if (SessiondEventConstants.TOPIC_REMOVE_DATA.equals(topic) || SessiondEventConstants.TOPIC_REMOVE_CONTAINER.equals(topic)) {
                    @SuppressWarnings("unchecked")
                    Map<String, Session> sessionContainer = (Map<String, Session>) event.getProperty(SessiondEventConstants.PROP_CONTAINER);
                    PushManagerRegistry registry = PushManagerRegistry.getInstance();

                    // Stop listener for sessions
                    final Collection<Session> sessions = sessionContainer.values();
                    for (Session session : sessions) {
                        LogProperties.putSessionProperties(session);
                        try {
                            registry.stopListenerFor(session);
                        } finally {
                            LogProperties.removeSessionProperties();
                        }
                    }
                } else if (CONSIDER_ADDED.contains(topic)) {
                    Session session = (Session) event.getProperty(SessiondEventConstants.PROP_SESSION);
                    LogProperties.putSessionProperties(session);
                    try {
                        PushManagerRegistry registry = PushManagerRegistry.getInstance();
                        registry.startListenerFor(session);
                    } finally {
                        LogProperties.removeSessionProperties();
                    }
                }
            } catch (Exception e) {
                LOG.error("Error while handling SessionD event \"{}\".", topic, e);
            }
        }
    } // End of PushEventHandlerRunnable
}
