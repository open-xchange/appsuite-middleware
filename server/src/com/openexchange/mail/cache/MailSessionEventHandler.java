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

package com.openexchange.mail.cache;

import java.text.MessageFormat;
import java.util.Map;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import com.openexchange.cache.OXCachingException;
import com.openexchange.mail.MailSessionCache;
import com.openexchange.mail.event.EventPool;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessiondService;
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
    static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(MailSessionEventHandler.class);

    /**
     * Whether logger allows debug.
     */
    static final boolean DEBUG = LOG.isDebugEnabled();

    /**
     * The topic on single session removal.
     */
    static final String TOPIC_REMOVE_SESSION = "com/openexchange/sessiond/remove/session";

    /**
     * The topic on session container removal.
     */
    static final String TOPIC_REMOVE_CONTAINER = "com/openexchange/sessiond/remove/container";

    /**
     * The property for a single session kept in event's properties.
     * <p>
     * Target object is an instance of <tt>com.openexchange.session.Session</tt>.
     */
    static final String PROP_SESSION = "com.openexchange.sessiond.session";

    /**
     * The property for a session container kept in event's properties.
     * <p>
     * Target object is an instance of <tt>java.util.Map&lt;String, Session&gt;</tt>.
     */
    static final String PROP_CONTAINER = "com.openexchange.sessiond.container";

    /**
     * Gets the topics.
     * 
     * @return The topics
     */
    public static String[] getTopics() {
        return new String[] { TOPIC_REMOVE_SESSION, TOPIC_REMOVE_CONTAINER };
    }

    /**
     * Initializes a new {@link MailSessionEventHandler}.
     */
    public MailSessionEventHandler() {
        super();
    }

    public void handleEvent(final Event event) {
        final Runnable r = new CustomRunnable(event);
        /*
         * Delegate to thread pool if present
         */
        final ThreadPoolService threadPoolService = ServerServiceRegistry.getInstance().getService(ThreadPoolService.class);
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

        public void run() {
            final String topic = event.getTopic();
            try {
                if (TOPIC_REMOVE_SESSION.equals(topic)) {
                    dropSessionCaches((Session) event.getProperty(PROP_SESSION));
                } else if (TOPIC_REMOVE_CONTAINER.equals(topic)) {
                    @SuppressWarnings("unchecked") final Map<String, Session> sessionContainer =
                        (Map<String, Session>) event.getProperty(PROP_CONTAINER);
                    for (final Session session : sessionContainer.values()) {
                        dropSessionCaches(session);
                    }
                }
            } catch (final Exception e) {
                LOG.error(MessageFormat.format("Error while handling SessionD event \"{0}\": {1}", topic, e.getMessage()), e);
            }
        }

        private static void dropSessionCaches(final Session session) {
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
            } catch (final OXCachingException e) {
                LOG.error(e.getMessage(), e);
            }
            /*
             * JSON message cache
             */
            final JSONMessageCache cache = JSONMessageCache.getInstance();
            if (null != cache) {
                cache.removeUser(userId, contextId);
            }
            if (DEBUG) {
                LOG.debug(new StringBuilder("All session-related caches cleared for removed session ").append(session.getSessionID()).toString());
            }
            /*
             * Pooled events: Last session removed?
             */
            final SessiondService sessiondService = ServerServiceRegistry.getInstance().getService(SessiondService.class);
            if (null != sessiondService && 0 == sessiondService.getUserSessions(userId, contextId)) {
                final EventPool eventPool = EventPool.getInstance();
                if (null != eventPool) {
                    eventPool.removeByUser(userId, contextId);
                    if (DEBUG) {
                        LOG.debug(new StringBuilder("Removed all pooled mail events for user ").append(userId).append(" in context ").append(
                            contextId).toString());
                    }
                }
            }
        }

    } // End of CustomRunnable

}
