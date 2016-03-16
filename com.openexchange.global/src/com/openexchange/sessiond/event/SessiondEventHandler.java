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

package com.openexchange.sessiond.event;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessionExceptionCodes;
import com.openexchange.sessiond.SessiondEventConstants;

/**
 * {@link SessiondEventHandler} - A convenience SessionD {@link EventHandler event handler} which delegates incoming events to registered
 * listeners.
 * <p>
 * The corresponding code inside {@link BundleActivator#start(BundleContext) activator.start()} should be like:
 *
 * <pre>
 *
 * final SessiondEventHandler eventHandler = new SessiondEventHandler();
 * // register some listeners ...
 * eventHandler.addListener(new MyListener());
 * // Remember returned ServiceRegistration for proper unregistration on stop
 * serviceRegistration = eventHandler.registerSessiondEventHandler(context);
 *
 * </pre>
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SessiondEventHandler implements EventHandler {

    private final List<SessiondEventListener> listeners;

    private final Set<Class<? extends SessiondEventListener>> classes;

    /**
     * Initializes a new {@link SessiondEventHandler sessiond event handler}
     */
    public SessiondEventHandler() {
        super();
        listeners = new ArrayList<SessiondEventListener>();
        classes = new HashSet<Class<? extends SessiondEventListener>>();
    }

    /**
     * Registers this sessiond event handler to specified {@link BundleContext bundle context}.
     *
     * @param context The {@link BundleContext bundle context} to register to
     * @return The appropriate {@link ServiceRegistration service registration}.
     */
    public ServiceRegistration<EventHandler> registerSessiondEventHandler(final BundleContext context) {
        final Dictionary<String, Object> properties = new Hashtable<String, Object>(1);
        properties.put(EventConstants.EVENT_TOPIC, SessiondEventConstants.getAllTopics());
        return context.registerService(EventHandler.class, this, properties);

    }

    /**
     * Adds a listener to this sessiond event handler
     *
     * @param listener The listener to add
     * @return <code>true</code> if listener has been successfully added; otherwise <code>false</code>
     */
    public boolean addListener(final SessiondEventListener listener) {
        final Class<? extends SessiondEventListener> clazz = listener.getClass();
        if (classes.contains(clazz)) {
            return false;
        }
        classes.add(clazz);
        listeners.add(listener);
        return true;
    }

    /**
     * Removes specified listener from this sessiond event handler
     *
     * @param listener The listener to remove
     * @return <code>true</code> if listener has been successfully removed; otherwise <code>false</code>
     */
    public boolean removeListener(final SessiondEventListener listener) {
        final Class<? extends SessiondEventListener> clazz = listener.getClass();
        if (!classes.contains(clazz)) {
            return false;
        }
        classes.remove(clazz);
        listeners.remove(listener);
        return true;
    }

    @Override
    public void handleEvent(final Event event) {
        final String topic = event.getTopic();
        if (SessiondEventConstants.TOPIC_REMOVE_SESSION.equals(topic)) {
            final Session session = (Session) event.getProperty(SessiondEventConstants.PROP_SESSION);
            for (final SessiondEventListener listener : listeners) {
                listener.handleSessionRemoval(session);
            }
        } else if (SessiondEventConstants.TOPIC_REMOVE_CONTAINER.equals(topic)) {
            final @SuppressWarnings("unchecked") Map<String, Session> sessions =
                (Map<String, Session>) event.getProperty(SessiondEventConstants.PROP_CONTAINER);
            for (final SessiondEventListener listener : listeners) {
                listener.handleContainerRemoval(sessions);
            }
        } else if (SessiondEventConstants.TOPIC_REMOVE_DATA.equals(topic)) {
            final @SuppressWarnings("unchecked") Map<String, Session> sessions =
                (Map<String, Session>) event.getProperty(SessiondEventConstants.PROP_CONTAINER);
            for (final SessiondEventListener listener : listeners) {
                listener.handleSessionDataRemoval(sessions);
            }
        } else if (SessiondEventConstants.TOPIC_REACTIVATE_SESSION.equals(topic)) {
            final Session session = (Session) event.getProperty(SessiondEventConstants.PROP_SESSION);
            for (final SessiondEventListener listener : listeners) {
                listener.handleSessionReactivation(session);
            }
        } else {
            final OXException error = SessionExceptionCodes.UNKNOWN_EVENT_TOPIC.create(topic == null ? "null" : topic);
            for (final SessiondEventListener listener : listeners) {
                listener.handleError(error);
            }
        }
    }

}
