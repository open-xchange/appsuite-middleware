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

package com.openexchange.sessiond.impl.container;

import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import com.openexchange.session.RandomTokenContainer;
import com.openexchange.session.Session;
import com.openexchange.session.SessionSpecificContainerRetrievalService.CleanUp;
import com.openexchange.session.SessionSpecificContainerRetrievalService.Lifecycle;
import com.openexchange.sessiond.impl.UUIDSessionIdGenerator;


/**
 * {@link RandomTokenContainerImpl}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class RandomTokenContainerImpl<T> implements RandomTokenContainer<T> {

    protected CleanUp<T> cleanUp;
    protected Lifecycle lifecycle;
    protected String name;

    protected ConcurrentHashMap<String, T> delegate = new ConcurrentHashMap<String, T>();
    protected ConcurrentHashMap<String, Queue<String>> tokensPerSession = new ConcurrentHashMap<String, Queue<String>>();

    public RandomTokenContainerImpl(final String name, final Lifecycle lifecycle, final CleanUp<T> cleanUp) {
        this.name = name;
        this.lifecycle = lifecycle;
        this.cleanUp = cleanUp;
    }

    @Override
    public T get(final String token) {
        return delegate.get(token);
    }

    @Override
    public String rememberForSession(final Session session, final T value) {
        while (true) {
            final String token = UUIDSessionIdGenerator.randomUUID();
            final T original = delegate.putIfAbsent(token, value);
            if (original == null) {
                associate(session, token);
                return token;
            }
            // Try again.
        }
    }

    private void associate(final Session session, final String token) {
        final String sessionID = session.getSessionID();
        Queue<String> tokenList = tokensPerSession.get(sessionID);
        if (tokenList == null) {
            final Queue<String> tmp = new ConcurrentLinkedQueue<String>();
            tokenList = tokensPerSession.putIfAbsent(sessionID, tmp);
            if (tokenList == null) {
                tokenList = tmp;
            }
        }
        tokenList.add(token);
    }

    @Override
    public T remove(final String token) {
        final T removed = delegate.remove(token);
        if (removed != null && cleanUp != null) {
            cleanUp.clean(removed);
        }
        return removed;
    }

    public void clear(final CleanUp<T> overridingCleanUp) {
        final CleanUp<T> cleanUp = overridingCleanUp == null ? this.cleanUp : overridingCleanUp;
        final Collection<T> values = delegate.values();
        final Iterator<T> iterator = values.iterator();
        while (iterator.hasNext()) {
            final T value = iterator.next();
            if (cleanUp != null) {
                cleanUp.clean(value);
            }
            iterator.remove();
        }
    }

    public void removeForSession(final Session session) {
        final Queue<String> list = tokensPerSession.remove(session.getSessionID());
        if (list == null) {
            return;
        }
        for (final String token : list) {
            remove(token);
        }
    }

    public Lifecycle getLifecycle() {
        return lifecycle;
    }




}
