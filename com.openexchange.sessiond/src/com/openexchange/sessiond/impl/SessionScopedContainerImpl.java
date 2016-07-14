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

package com.openexchange.sessiond.impl;

import java.util.Collection;
import java.util.HashSet;
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

    private final SessiondSessionSpecificRetrievalService manager;

    private final String name;

    private final Lifecycle lifecycle;

    public SessionScopedContainerImpl(String name, Lifecycle lifecycle, InitialValueFactory<T> initial, CleanUp<T> cleanUp, SessiondSessionSpecificRetrievalService manager) {
        super();
        this.initial = initial;
        this.cleanUp = cleanUp;
        this.manager = manager;
        this.name = name;
        this.lifecycle = lifecycle;
    }

    @Override
    public void clear() {
        for (Session key : keySet()) {
            remove(key);
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
        key = ID(key);
        if (!delegate.containsKey(key) && initial != null) {
            T created = initial.create();
            T other = delegate.putIfAbsent((SessionKey) key, created);
            if (other != null) {
                cleanUp(created);
                return other;
            }

            return created;
        }
        return delegate.get(key);
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
