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
    public void handleEvent(final Event event) {
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

        CustomRunnable(final Event event) {
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
                    for (final Session session : sessionContainer.values()) {
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
            } catch (final Exception e) {
                LOG.error("Error while handling session event \"{}\"", topic, e);
            }
        }

        private static void dropSessionCaches(final Session session) {
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
            } catch (final OXException e) {
                LOG.error("", e);
            }
            LOG.debug("All session-related caches cleared for removed session {}", session.getSessionID());
        }

    } // End of CustomRunnable

}
