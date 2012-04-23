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

package com.openexchange.service.messaging.internal;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import com.openexchange.service.messaging.Message;
import com.openexchange.service.messaging.MessageHandler;
import com.openexchange.service.messaging.MessagingServiceConstants;

/**
 * {@link MessageHandlerWrapper} - A wrapper for {@link MessageHandler}s.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MessageHandlerWrapper {

    private static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(MessageHandlerWrapper.class));

    private final ServiceReference reference;

    private final BundleContext context;

    private MessageHandler handler;

    private String[] topics;

    private Filter filter;

    /**
     * Initializes a new {@link MessageHandlerWrapper}.
     *
     * @param reference The reference to the message handler
     * @param context The bundle Context of the messaging service bundle
     */
    public MessageHandlerWrapper(final ServiceReference reference, final BundleContext context) {
        super();
        this.reference = reference;
        this.context = context;
    }

    /**
     * Caches values from service properties
     *
     * @return <code>true</code> if the handler should be called; <code>false</code> if the handler should not be called
     */
    public synchronized boolean init() {
        topics = null;
        filter = null;
        /*
         * Get topic names
         */
        Object o = reference.getProperty(MessagingServiceConstants.MESSAGE_TOPIC);
        if (o instanceof String) {
            topics = new String[] { (String) o };
        } else if (o instanceof String[]) {
            topics = (String[]) o;
        }
        if (topics == null) {
            /*
             * Not interested in any topic
             */
            return false;
        }
        /*
         * Get filter
         */
        o = reference.getProperty(MessagingServiceConstants.MESSAGE_FILTER);
        if (o instanceof String) {
            try {
                filter = context.createFilter((String) o);
            } catch (final InvalidSyntaxException e) {
                LOG.error(e.getMessage(), e);
                return false;
            }
        }
        return true;
    }

    /**
     * Flushes the handler service if it has been obtained.
     */
    public void flush() {
        synchronized (this) {
            if (handler == null) {
                return;
            }
            handler = null;
        }
        context.ungetService(reference);
    }

    /**
     * Gets the message topics for the wrapped handler.
     *
     * @return The wrapped handler's message topics
     */
    public synchronized String[] getTopics() {
        return topics;
    }

    /**
     * Returns the wrapped handler.
     *
     * @return The wrapped handler.
     */
    private MessageHandler getHandler() {
        synchronized (this) {
            /*
             * If we already have a handler, return it
             */
            if (handler != null) {
                return handler;
            }
        }
        /*
         * We don't have the handler, so lets get it outside the sync region
         */
        MessageHandler tempHandler = (MessageHandler) context.getService(reference);
        synchronized (this) {
            /*
             * Do we still need the handler we just got?
             */
            if (handler == null) {
                handler = tempHandler;
                return handler;
            }
            /*
             * Get the current handler
             */
            tempHandler = handler;
        }
        /*
         * Unget the handler we just got since we don't need it
         */
        context.ungetService(reference);
        /*
         * Return the current handler (copied into the local var)
         */
        return tempHandler;
    }

    /**
     * Gets the filter object
     *
     * @return The handler's filter
     */
    private synchronized Filter getFilter() {
        return filter;
    }

    /**
     * Dispatches event to handler. Performs final tests before actually calling the handler.
     *
     * @param message The message to dispatch
     */
    public void handleMessage(final Message message) {
        final Bundle bundle = reference.getBundle();
        /*
         * Is service unregistered?
         */
        if (bundle == null) {
            return;
        }
        /*
         * Filter match
         */
        final Filter eventFilter = getFilter();
        if ((eventFilter != null) && !message.matches(eventFilter)) {
            return;
        }
        /*
         * Get handler service
         */
        final MessageHandler handlerService = getHandler();
        if (handlerService == null) {
            return;
        }

        try {
            handlerService.handleMessage(message);
        } catch (final Throwable t) {
            LOG.error(t.getMessage(), t);
        }
    }

}
