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

package com.openexchange.mail.cache;

import java.util.Map;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import com.openexchange.exception.OXException;
import com.openexchange.mail.MailSessionCache;
import com.openexchange.mail.event.EventPool;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessiondEventConstants;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.ThreadPools;
import com.openexchange.threadpool.behavior.CallerRunsBehavior;

/**
 * {@link MailSessionEventHandler} - The {@link EventHandler event handler} for mail bundle to track removed sessions.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailSessionEventHandler implements EventHandler {

    /**
     * The logger constant.
     */
    static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MailSessionEventHandler.class);

    /**
     * Gets the topics.
     *
     * @return The topics
     */
    public static String[] getTopics() {
        return new String[] { SessiondEventConstants.TOPIC_REMOVE_SESSION, SessiondEventConstants.TOPIC_REMOVE_DATA, SessiondEventConstants.TOPIC_REMOVE_CONTAINER, SessiondEventConstants.TOPIC_LAST_SESSION };
    }

    /**
     * Initializes a new {@link MailSessionEventHandler}.
     */
    public MailSessionEventHandler() {
        super();
    }

    @Override
    public void handleEvent(Event event) {
        final Runnable r = new CustomRunnable(event);
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

    private static final class CustomRunnable implements Runnable {

        private final Event event;

        CustomRunnable(Event event) {
            super();
            this.event = event;
        }

        @Override
        public void run() {
            final String topic = event.getTopic();
            try {
                if (SessiondEventConstants.TOPIC_REMOVE_SESSION.equals(topic)) {
                    dropSessionCaches((Session) event.getProperty(SessiondEventConstants.PROP_SESSION));
                } else if (SessiondEventConstants.TOPIC_REMOVE_DATA.equals(topic) || SessiondEventConstants.TOPIC_REMOVE_CONTAINER.equals(topic)) {
                    @SuppressWarnings("unchecked") final Map<String, Session> sessionContainer =
                        (Map<String, Session>) event.getProperty(SessiondEventConstants.PROP_CONTAINER);
                    for (Session session : sessionContainer.values()) {
                        dropSessionCaches(session);
                    }
                } else if (SessiondEventConstants.TOPIC_LAST_SESSION.equals(topic)) {
                    Integer contextId = (Integer) event.getProperty(SessiondEventConstants.PROP_CONTEXT_ID);
                    if (null != contextId) {
                        Integer userId = (Integer) event.getProperty(SessiondEventConstants.PROP_USER_ID);
                        if (null != userId) {
                            final EventPool eventPool = EventPool.getInstance();
                            if (null != eventPool) {
                                eventPool.removeByUser(userId.intValue(), contextId.intValue());
                                LOG.debug("Removed all pooled mail events for user {} in context {}", userId, contextId);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                LOG.error("Error while handling session event \"{}\"", topic, e);
            }
        }

        private static void dropSessionCaches(Session session) {
            if (null == session || session.isTransient()) {
                return;
            }
            /*
             * Session caches
             */
            final int userId = session.getUserId();
            final int contextId = session.getContextId();
            MailSessionCache.dropInstance(session);
            /*
             * Message cache
             */
            try {
                MailMessageCache.getInstance().removeUserMessages(userId, contextId);
            } catch (OXException e) {
                LOG.error("", e);
            }
            LOG.debug("All session-related caches cleared for removed session {}", session.getSessionID());
        }

    } // End of CustomRunnable

}
