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

package com.openexchange.mail.cache.queue;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import com.openexchange.mail.cache.PooledMailAccess;


/**
 * {@link SingletonMailAccessQueue} - A thread-safe singleton queue backed by an {@link AtomicReference} instance.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SingletonMailAccessQueue implements MailAccessQueue {

    private final AtomicReference<PooledMailAccess> singleton;

    private final AtomicBoolean deprecated;

    /**
     * Initializes a new {@link SingletonMailAccessQueue}.
     */
    public SingletonMailAccessQueue() {
        super();
        singleton = new AtomicReference<PooledMailAccess>();
        deprecated = new AtomicBoolean();
    }

    @Override
    public int getCapacity() {
        return 1;
    }

    @Override
    public PooledMailAccess pollDelayed() {
        PooledMailAccess tmp;
        do {
            tmp = singleton.get();
            if (null != tmp && tmp.getDelay(TimeUnit.MILLISECONDS) <= 0 && singleton.compareAndSet(tmp, null)) {
                return tmp;
            }
        } while (!singleton.compareAndSet(tmp, tmp)); // tmp != singleton.get()
        return null;
    }

    @Override
    public boolean offer(PooledMailAccess pooledMailAccess) {
        return singleton.compareAndSet(null, pooledMailAccess);
    }

    @Override
    public PooledMailAccess poll() {
        return singleton.getAndSet(null);
    }

    @Override
    public PooledMailAccess remove() {
        PooledMailAccess tmp;
        do {
            tmp = singleton.get();
        } while (!singleton.compareAndSet(tmp, null));
        if (null == tmp) {
            throw new IllegalStateException("Queue is empty.");
        }
        return tmp;
    }

    @Override
    public PooledMailAccess peek() {
        return singleton.get();
    }

    @Override
    public PooledMailAccess element() {
        final PooledMailAccess tmp = singleton.get();
        if (null == tmp) {
            throw new IllegalStateException("Queue is empty.");
        }
        return tmp;
    }

    @Override
    public int size() {
        return null == singleton.get() ? 0 : 1;
    }

    @Override
    public boolean isEmpty() {
        return null == singleton.get();
    }

    @Override
    public boolean contains(Object o) {
        final PooledMailAccess tmp = singleton.get();
        if (null == tmp) {
            if (null == o) {
                return true;
            }
            return false;
        }
        return tmp.equals(o);
    }

    @Override
    public Iterator<PooledMailAccess> iterator() {
        final List<PooledMailAccess> list;
        final PooledMailAccess tmp = singleton.get();
        if (null == tmp) {
            list = Collections.emptyList();
        } else {
            list = Collections.singletonList(tmp);
        }
        return list.iterator();
    }

    @Override
    public Object[] toArray() {
        final List<PooledMailAccess> list;
        final PooledMailAccess tmp = singleton.get();
        if (null == tmp) {
            list = Collections.emptyList();
        } else {
            list = Collections.singletonList(tmp);
        }
        return list.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        final List<PooledMailAccess> list;
        final PooledMailAccess tmp = singleton.get();
        if (null == tmp) {
            list = Collections.emptyList();
        } else {
            list = Collections.singletonList(tmp);
        }
        return list.toArray(a);
    }

    @Override
    public boolean add(PooledMailAccess pooledMailAccess) {
        return singleton.compareAndSet(null, pooledMailAccess);
    }

    @Override
    public boolean remove(Object o) {
        final PooledMailAccess tmp = singleton.get();
        if (null == tmp) {
            return false;
        }
        return (tmp.equals(o) && singleton.compareAndSet(tmp, null));
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        if (null == c || c.isEmpty()) {
            return false;
        }
        if (c.size() > 1) {
            return false;
        }
        final Object o = c.iterator().next();
        final PooledMailAccess tmp = singleton.get();
        if (null == tmp) {
            if (null == o) {
                return true;
            }
            return false;
        }
        return tmp.equals(o);
    }

    @Override
    public boolean addAll(Collection<? extends PooledMailAccess> c) {
        if (null == c || c.isEmpty()) {
            return false;
        }
        return offer(c.iterator().next());
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        if (null == c || c.isEmpty()) {
            return false;
        }
        final Object o = c.iterator().next();
        return remove(o);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        if (null == c || c.isEmpty()) {
            return false;
        }
        final Object o = c.iterator().next();
        if (!(o instanceof PooledMailAccess)) {
            return false;
        }
        final PooledMailAccess tmp = (PooledMailAccess) o;
        do {
            if (!tmp.equals(singleton.get()) && singleton.compareAndSet(tmp, null)) {
                return true;
            }
        } while (!singleton.compareAndSet(tmp, tmp)); // tmp != singleton.get()
        return false;
    }

    @Override
    public void clear() {
        singleton.set(null);
    }

    @Override
    public void markDeprecated() {
        deprecated.set(true);
    }

    @Override
    public boolean isDeprecated() {
        return deprecated.get();
    }

}
