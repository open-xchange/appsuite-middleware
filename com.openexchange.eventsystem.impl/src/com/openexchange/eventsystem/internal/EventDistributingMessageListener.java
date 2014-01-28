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

package com.openexchange.eventsystem.internal;

import java.util.Map;
import java.util.Set;
import com.openexchange.eventsystem.Event;
import com.openexchange.ms.Message;
import com.openexchange.ms.MessageListener;
import com.openexchange.threadpool.AbstractTask;
import com.openexchange.threadpool.ThreadPools;
import com.openexchange.threadpool.behavior.CallerRunsBehavior;

/**
 * {@link EventDistributingMessageListener}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.4.2
 */
public final class EventDistributingMessageListener implements MessageListener<Map<String, Object>> {

    private final EventHandlerTracker handlers;
    private final boolean isAsync;

    /**
     * Initializes a new {@link EventDistributingMessageListener}.
     */
    public EventDistributingMessageListener(final EventHandlerTracker handlers, final boolean isAsync) {
        super();
        this.handlers = handlers;
        this.isAsync = isAsync;
    }

    @Override
    public void onMessage(final Message<Map<String, Object>> message) {
        dispatchEvent(EventUtility.unwrap(message.getMessageObject()), isAsync);
    }

    /**
     * Internal main method for sendEvent() and postEvent(). Dispatching an event to EventHandler. All exceptions are logged except when
     * dealing with LogEntry.
     *
     * @param event to be delivered
     * @param isAsync must be set to true for asynchronous event delivery, false for synchronous delivery.
     */
    private void dispatchEvent(final Event event, final boolean isAsync) {
        if (event == null) {
            return;
        }

        final Set<EventHandlerReference> eventHandlers = this.handlers.getHandlers(event.getTopic());
        // If there are no handlers, then we are done
        if (eventHandlers.isEmpty()) {
            return;
        }

        if (isAsync) {
            ThreadPools.getThreadPool().submit(new DispatchTask(event, eventHandlers), CallerRunsBehavior.getInstance());
        } else {
            for (final EventHandlerReference eventHandlerWrapper : eventHandlers) {
                eventHandlerWrapper.handleEvent(event);
            }
        }
    }

    // ---------------------------------------------------------------------------------- //

    private static final class DispatchTask extends AbstractTask<Object> {

        private final Event event;
        private final Set<EventHandlerReference> eventHandlers;

        DispatchTask(final Event event, final Set<EventHandlerReference> eventHandlers) {
            this.event = event;
            this.eventHandlers = eventHandlers;
        }

        @Override
        public Object call() throws Exception {
            for (final EventHandlerReference eventHandlerWrapper : eventHandlers) {
                eventHandlerWrapper.handleEvent(event);
            }
            return null;
        }
    }
}
