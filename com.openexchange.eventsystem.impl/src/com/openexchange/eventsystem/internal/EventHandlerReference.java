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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

import java.util.Collection;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import com.openexchange.eventsystem.Event;
import com.openexchange.eventsystem.EventConstants;
import com.openexchange.eventsystem.EventHandler;

/**
 * {@link EventHandlerReference} - A reference for <code>EventHandler</code>s.
 * <p>
 * This class caches property values and performs final checks before calling the wrapped handler.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.4.2
 */
public class EventHandlerReference {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(EventHandlerReference.class);

    private final ServiceReference<EventHandler> reference;
    private final BundleContext context;
    private EventHandler handler;
    private String[] topics;

    /**
     * Create an EventHandlerWrapper.
     *
     * @param reference Reference to the EventHandler
     * @param context Bundle Context of the Event Admin bundle
     * @param log LogService object for logging
     */
    public EventHandlerReference(final ServiceReference<EventHandler> reference, final BundleContext context) {
        super();
        this.reference = reference;
        this.context = context;
    }

    /**
     * Cache values from service properties
     *
     * @return true if the handler should be called; false if the handler should not be called
     */
    public synchronized boolean init() {
        topics = null;

        // Get topic names
        final Object o = reference.getProperty(EventConstants.EVENT_TOPIC);
        if (o instanceof String) {
            topics = new String[] { (String) o };
        } else if (o instanceof String[]) {
            topics = (String[]) o;
        } else if (o instanceof Collection) {
            try {
                @SuppressWarnings("unchecked") final Collection<String> c = (Collection<String>) o;
                topics = c.toArray(new String[c.size()]);
            } catch (final ArrayStoreException e) {
                LOG.error("Invalid event handler topics", e);
            }
        }

        if (topics == null) {
            return false;
        }

        return true;
    }

    /**
     * Flush the handler service if it has been obtained.
     */
    public void flush() {
        synchronized (this) {
            if (handler == null) {
                return;
            }
            handler = null;
        }
        try {
            context.ungetService(reference);
        } catch (final IllegalStateException e) {
            // ignore event admin must have stopped
        }
    }

    /**
     * Get the event topics for the wrapped handler.
     *
     * @return The wrapped handler's event topics
     */
    public synchronized String[] getTopics() {
        return topics;
    }

    /**
     * Return the wrapped handler.
     *
     * @return The wrapped handler.
     */
    private EventHandler getHandler() {
        synchronized (this) {
            // if we already have a handler, return it
            if (handler != null) {
                return handler;
            }
        }

        // we don't have the handler, so lets get it outside the sync region
        EventHandler tempHandler = null;
        try {
            tempHandler = context.getService(reference);
        } catch (final IllegalStateException e) {
            // ignore; service may have stopped
        }

        synchronized (this) {
            // do we still need the handler we just got?
            if (handler == null) {
                handler = tempHandler;
                return handler;
            }
            // get the current handler
            tempHandler = handler;
        }

        // unget the handler we just got since we don't need it
        try {
            context.ungetService(reference);
        } catch (final IllegalStateException e) {
            // ignore; event admin may have stopped
        }

        // return the current handler (copied into the local var)
        return tempHandler;
    }

    /**
     * Dispatch event to handler. Perform final tests before actually calling the handler.
     *
     * @param event The event to dispatch
     */
    public void handleEvent(final Event event) {
        final Bundle bundle = reference.getBundle();
        // is service unregistered?
        if (bundle == null) {
            return;
        }

        // get handler service
        final EventHandler handler = getHandler();
        if (handler == null) {
            return;
        }

        try {
            handler.handleEvent(event);
        } catch (Throwable t) {
            if (event.getTopic().startsWith("org/osgi/service/log/LogEntry")) {
                final Object exception = event.getProperty("exception");
                if (exception instanceof LogTopicException) {
                    return; // avoid endless event dispatching
                }
                // wrap exception in a LogTopicException to detect endless event dispatching
                t = new LogTopicException(t);
            }
            // log/handle any Throwable thrown by the listener
            LOG.error("Event could not be dispatched", t);
        }
    }

    static class LogTopicException extends RuntimeException {

        private static final long serialVersionUID = -2386940335620739632L;

        public LogTopicException(final Throwable cause) {
            super(cause);
        }
    }
}
