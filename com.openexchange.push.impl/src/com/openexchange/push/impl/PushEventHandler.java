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

package com.openexchange.push.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
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
    protected static final Set<String> CONSIDER_ADDED = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(SessiondEventConstants.TOPIC_ADD_SESSION, SessiondEventConstants.TOPIC_REACTIVATE_SESSION, SessiondEventConstants.TOPIC_RESTORED_SESSION)));

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
                    PushManagerRegistry registry = PushManagerRegistry.getInstance();
                    registry.stopListenerFor(session);
                } else if (SessiondEventConstants.TOPIC_REMOVE_DATA.equals(topic) || SessiondEventConstants.TOPIC_REMOVE_CONTAINER.equals(topic)) {
                    @SuppressWarnings("unchecked")
                    Map<String, Session> sessionContainer = (Map<String, Session>) event.getProperty(SessiondEventConstants.PROP_CONTAINER);
                    PushManagerRegistry registry = PushManagerRegistry.getInstance();

                    // Stop listener for sessions
                    final Collection<Session> sessions = sessionContainer.values();
                    for (Session session : sessions) {
                        registry.stopListenerFor(session);
                    }
                } else if (CONSIDER_ADDED.contains(topic)) {
                    Session session = (Session) event.getProperty(SessiondEventConstants.PROP_SESSION);
                    PushManagerRegistry registry = PushManagerRegistry.getInstance();
                    registry.startListenerFor(session);
                }
            } catch (final Exception e) {
                LOG.error("Error while handling SessionD event \"{}\".", topic, e);
            }
        }
    } // End of PushEventHandlerRunnable
}
