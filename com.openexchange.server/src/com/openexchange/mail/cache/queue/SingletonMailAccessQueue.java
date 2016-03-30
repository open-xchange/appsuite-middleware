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
    public boolean offer(final PooledMailAccess pooledMailAccess) {
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
    public boolean contains(final Object o) {
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
    public <T> T[] toArray(final T[] a) {
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
    public boolean add(final PooledMailAccess pooledMailAccess) {
        return singleton.compareAndSet(null, pooledMailAccess);
    }

    @Override
    public boolean remove(final Object o) {
        final PooledMailAccess tmp = singleton.get();
        if (null == tmp) {
            return false;
        }
        return (tmp.equals(o) && singleton.compareAndSet(tmp, null));
    }

    @Override
    public boolean containsAll(final Collection<?> c) {
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
    public boolean addAll(final Collection<? extends PooledMailAccess> c) {
        if (null == c || c.isEmpty()) {
            return false;
        }
        return offer(c.iterator().next());
    }

    @Override
    public boolean removeAll(final Collection<?> c) {
        if (null == c || c.isEmpty()) {
            return false;
        }
        final Object o = c.iterator().next();
        return remove(o);
    }

    @Override
    public boolean retainAll(final Collection<?> c) {
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
