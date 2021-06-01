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

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import com.openexchange.session.Session;
import com.openexchange.session.SessionScopedContainer;
import com.openexchange.session.SessionSpecificContainerRetrievalService.CleanUp;
import com.openexchange.session.SessionSpecificContainerRetrievalService.InitialValueFactory;
import com.openexchange.session.SessionSpecificContainerRetrievalService.Lifecycle;

/**
 * {@link SessionScopedContainerImpl}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class SessionScopedContainerImpl<T> implements SessionScopedContainer<T> {

    protected final ConcurrentHashMap<SessionKey, T> delegate = new ConcurrentHashMap<SessionKey, T>();

    protected final InitialValueFactory<T> initial;
    protected final CleanUp<T> cleanUp;
    private final String name;
    private final Lifecycle lifecycle;

    public SessionScopedContainerImpl(String name, Lifecycle lifecycle, InitialValueFactory<T> initial, CleanUp<T> cleanUp) {
        super();
        this.initial = initial;
        this.cleanUp = cleanUp;
        this.name = name;
        this.lifecycle = lifecycle;
    }

    @Override
    public void clear() {
        for (Iterator<Session> iterator = keySet().iterator(); iterator.hasNext();) {
            remove(iterator.next());
        }
    }

    @Override
    public boolean containsKey(Object key) {
        return delegate.containsKey(ID(key));
    }

    @Override
    public boolean containsValue(Object value) {
        return delegate.containsValue(value);
    }

    @Override
    public Set<java.util.Map.Entry<Session, T>> entrySet() {
        Set<Map.Entry<Session, T>> entrySet = new HashSet<Map.Entry<Session, T>>();
        for (final Map.Entry<SessionKey, T> entry : delegate.entrySet()) {
            entrySet.add(new Map.Entry<Session, T>() {

                @Override
                public Session getKey() {
                    return entry.getKey().session;
                }

                @Override
                public T getValue() {
                    return entry.getValue();
                }

                @Override
                public T setValue(T value) {
                    return entry.setValue(value);
                }

            });
        }
        return entrySet;
    }

    @Override
    public T get(Object key) {
        SessionKey sessionKey = ID(key);
        if (!delegate.containsKey(sessionKey) && initial != null) {
            T created = initial.create();
            T other = delegate.putIfAbsent(sessionKey, created);
            if (other != null) {
                cleanUp(created);
                return other;
            }

            return created;
        }
        return delegate.get(sessionKey);
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public Set<Session> keySet() {
        Set<Session> keySet = new HashSet<Session>();
        for (SessionKey key : delegate.keySet()) {
            keySet.add(key.session);
        }
        return keySet;
    }

    @Override
    public T put(Session key, T value) {
        return delegate.put(ID(key), value);
    }

    @Override
    public void putAll(Map<? extends Session, ? extends T> m) {
        for (Map.Entry<? extends Session, ? extends T> entry : m.entrySet()) {
            delegate.put(ID(entry.getKey()), entry.getValue());
        }
    }

    @Override
    public T remove(Object key) {
        T removed = delegate.remove(ID(key));
        cleanUp(removed);
        return removed;
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public Collection<T> values() {
        return delegate.values();
    }

    public void clear(CleanUp<T> overrideCleanUp) {
        CleanUp<T> relevantCleanUp = (overrideCleanUp == null) ? this.cleanUp : overrideCleanUp;
        for (SessionKey session : delegate.keySet()) {
            T removed = delegate.remove(session);
            if (relevantCleanUp != null) {
                relevantCleanUp.clean(removed);
            }
        }
    }

    protected void cleanUp(T value) {
        if (cleanUp != null) {
            cleanUp.clean(value);
        }
    }

    public String getName() {
        return name;
    }

    public Lifecycle getLifecycle() {
        return lifecycle;
    }

    protected SessionKey ID(Object key) {
        return new SessionKey((Session) key);
    }

    protected final class SessionKey {

        public Session session;

        public SessionKey(Session session) {
            this.session = session;
        }

        @Override
        public boolean equals(Object obj) {
            if (String.class.isInstance(obj)) {
                return session.getSessionID().equals(obj);
            }
            return session.getSessionID().equals(((SessionKey) obj).session.getSessionID());
        }

        @Override
        public int hashCode() {
            return session.getSessionID().hashCode();
        }
    }

}
