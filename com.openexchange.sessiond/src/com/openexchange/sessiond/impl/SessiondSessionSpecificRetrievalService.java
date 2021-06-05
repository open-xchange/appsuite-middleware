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

package com.openexchange.sessiond.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.openexchange.exception.OXException;
import com.openexchange.session.RandomTokenContainer;
import com.openexchange.session.Session;
import com.openexchange.session.SessionScopedContainer;
import com.openexchange.session.SessionSpecificContainerRetrievalService;
import com.openexchange.sessiond.event.SessiondEventListener;
import com.openexchange.sessiond.impl.container.RandomTokenContainerImpl;

/**
 * {@link SessiondSessionSpecificRetrievalService}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class SessiondSessionSpecificRetrievalService implements SessionSpecificContainerRetrievalService, SessiondEventListener {

    private final ConcurrentHashMap<String, SessionScopedContainerImpl<?>> containers = new ConcurrentHashMap<String, SessionScopedContainerImpl<?>>();
    private final ConcurrentHashMap<String, RandomTokenContainerImpl<?>> randomTokenContainer = new ConcurrentHashMap<String, RandomTokenContainerImpl<?>>();

    @Override
    public <T> SessionScopedContainer<T> getContainer(String name, Lifecycle lifecycle, InitialValueFactory<T> initial, CleanUp<T> cleanUp) {
        if (containers.containsKey(name)) {
            return (SessionScopedContainer<T>) containers.get(name);
        }
        if (lifecycle == null) {
            lifecycle = DEFAULT_LIFECYCLE;
        }
        SessionScopedContainerImpl<T> newValue = new SessionScopedContainerImpl<T>(name, lifecycle, initial, cleanUp);
        SessionScopedContainerImpl<?> other = containers.putIfAbsent(name, newValue);
        if (other != null) {
            return (SessionScopedContainer<T>) other;
        }
        return newValue;
    }

    @Override
    public void destroyContainer(String name, CleanUp cleanUp) {
        SessionScopedContainerImpl<?> removed = containers.remove(name);
        if (removed != null) {
            removed.clear(cleanUp);
        }
    }

    public void handleLifecycleChange(Session session, Lifecycle newLifecycle) {
        for (SessionScopedContainerImpl<?> container : containers.values()) {
            if (container.getLifecycle().includes(newLifecycle)) {
                container.remove(session);
            }
        }
        for(RandomTokenContainerImpl<?> container : randomTokenContainer.values()) {
            if (container.getLifecycle().includes(newLifecycle)) {
                container.removeForSession(session);
            }
        }
    }

    @Override
    public <T> RandomTokenContainer<T> getRandomTokenContainer(String name, Lifecycle lifecycle, CleanUp<T> cleanUp) {
        if (randomTokenContainer.containsKey(name)) {
            return (RandomTokenContainer<T>) randomTokenContainer.get(name);
        }
        if (lifecycle == null) {
            lifecycle = DEFAULT_LIFECYCLE;
        }
        RandomTokenContainerImpl<T> container = new RandomTokenContainerImpl<T>(name, lifecycle, cleanUp);
        RandomTokenContainerImpl<?> other = randomTokenContainer.putIfAbsent(name, container);
        if (other != null) {
            return (RandomTokenContainer<T>) other;
        }
        return container;
    }

    @Override
    public void destroyRandomTokenContainer(String name, CleanUp cleanUp) {
        RandomTokenContainerImpl<?> container = randomTokenContainer.get(name);
        container.clear(cleanUp);
    }

    // Event Handling

    @Override
    public void handleContainerRemoval(Map<String, Session> sessions) {
        for (Session session : sessions.values()) {
            handleLifecycleChange(session, Lifecycle.TERMINATE);
        }
    }

    @Override
    public void handleError(OXException error) {
        // IGNORE
    }

    @Override
    public void handleSessionDataRemoval(Map<String, Session> sessions) {
        for (Session session : sessions.values()) {
            handleLifecycleChange(session, Lifecycle.HIBERNATE);
        }
    }

    @Override
    public void handleSessionReactivation(Session session) {
        // IGNORE
    }

    @Override
    public void handleSessionRemoval(Session session) {
        handleLifecycleChange(session, Lifecycle.TERMINATE);
    }





}
