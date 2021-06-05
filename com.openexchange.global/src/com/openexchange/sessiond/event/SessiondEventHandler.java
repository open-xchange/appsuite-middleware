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
