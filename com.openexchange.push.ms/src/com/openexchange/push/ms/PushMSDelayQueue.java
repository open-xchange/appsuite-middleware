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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.push.ms;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.TimeUnit;

/**
 * {@link PushMSDelayQueue}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class PushMSDelayQueue implements BlockingQueue<DelayedPushMsObject> {

    private final DelayQueue<DelayedPushMsObject> delayQueue;
    private final ConcurrentHashMap<PushMsObject, DelayedPushMsObject> existingPushObjects;

    /**
     * Initializes a new {@link PushMSDelayQueue}.
     */
    public PushMSDelayQueue() {
        super();
        existingPushObjects = new ConcurrentHashMap<PushMsObject, DelayedPushMsObject>();
        delayQueue = new DelayQueue<DelayedPushMsObject>();
    }

    @Override
    public boolean isEmpty() {
        return delayQueue.isEmpty();
    }

    @Override
    public synchronized boolean add(DelayedPushMsObject e) {
        final DelayedPushMsObject prev = existingPushObjects.putIfAbsent(e.getPushObject(), e);
        if (null != prev) {
            // Already exists
            prev.touch();
            return false;
        }
        return delayQueue.add(e);
    }

    @Override
    public boolean contains(Object o) {
        return delayQueue.contains(o);
    }

    @Override
    public synchronized boolean offer(DelayedPushMsObject e) {
        final DelayedPushMsObject prev = existingPushObjects.putIfAbsent(e.getPushObject(), e);
        if (null != prev) {
            // Already exists
            prev.touch();
            return false;
        }
        return delayQueue.offer(e);
    }

    @Override
    public synchronized DelayedPushMsObject remove() {
        final DelayedPushMsObject removed = delayQueue.remove();
        if (null != removed) {
            existingPushObjects.remove(removed.getPushObject());
        }
        return removed;
    }

    @Override
    public synchronized void put(DelayedPushMsObject e) {
        final DelayedPushMsObject prev = existingPushObjects.putIfAbsent(e.getPushObject(), e);
        if (null != prev) {
            // Already exists
            prev.touch();
            return;
        }
        delayQueue.put(e);
    }

    @Override
    public DelayedPushMsObject element() {
        return delayQueue.element();
    }

    @Override
    public synchronized boolean offer(DelayedPushMsObject e, long timeout, TimeUnit unit) {
        final DelayedPushMsObject prev = existingPushObjects.putIfAbsent(e.getPushObject(), e);
        if (null != prev) {
            // Already exists
            prev.touch();
            return false;
        }
        return delayQueue.offer(e, timeout, unit);
    }

    @Override
    public synchronized DelayedPushMsObject poll() {
        final DelayedPushMsObject polled = delayQueue.poll();
        if (null != polled) {
            existingPushObjects.remove(polled.getPushObject());
        }
        return polled;
    }

    @Override
    public synchronized boolean addAll(Collection<? extends DelayedPushMsObject> c) {
        boolean changed = false;
        for (final DelayedPushMsObject e : c) {
            final DelayedPushMsObject prev = existingPushObjects.putIfAbsent(e.getPushObject(), e);
            if (null != prev) {
                // Already exists
                prev.touch();
            } else {
                changed = delayQueue.offer(e);
            }
        }
        return changed;
    }

    @Override
    public synchronized DelayedPushMsObject take() throws InterruptedException {
        final DelayedPushMsObject taken = delayQueue.take();
        if (null != taken) {
            existingPushObjects.remove(taken.getPushObject());
        }
        return taken;
    }

    @Override
    public DelayedPushMsObject poll(long timeout, TimeUnit unit) throws InterruptedException {
        final DelayedPushMsObject polled = delayQueue.poll(timeout, unit);
        if (null != polled) {
            existingPushObjects.remove(polled.getPushObject());
        }
        return polled;
    }

    @Override
    public DelayedPushMsObject peek() {
        return delayQueue.peek();
    }

    @Override
    public int size() {
        return delayQueue.size();
    }

    @Override
    public synchronized int drainTo(final Collection<? super DelayedPushMsObject> c) {
        if (null == c) {
            return 0;
        }
        if (c.isEmpty()) {
            final int retval = delayQueue.drainTo(c);
            for (final Object e : c) {
                existingPushObjects.remove(((DelayedPushMsObject) e).getPushObject());
            }
            return retval;
        }
        // Add to temporary collection
        final List<DelayedPushMsObject> tmp = new LinkedList<DelayedPushMsObject>();
        final int retval = delayQueue.drainTo(c);
        for (final DelayedPushMsObject e : tmp) {
            existingPushObjects.remove(e.getPushObject());
        }
        c.addAll(tmp);
        return retval;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return delayQueue.containsAll(c);
    }

    @Override
    public synchronized int drainTo(Collection<? super DelayedPushMsObject> c, int maxElements) {
        if (null == c) {
            return 0;
        }
        if (c.isEmpty()) {
            final int retval = delayQueue.drainTo(c, maxElements);
            for (final Object e : c) {
                existingPushObjects.remove(((DelayedPushMsObject) e).getPushObject());
            }
            return retval;
        }
        // Add to temporary collection
        final List<DelayedPushMsObject> tmp = new LinkedList<DelayedPushMsObject>();
        final int retval = delayQueue.drainTo(c, maxElements);
        for (final DelayedPushMsObject e : tmp) {
            existingPushObjects.remove(e.getPushObject());
        }
        c.addAll(tmp);
        return retval;
    }

    @Override
    public synchronized boolean removeAll(Collection<?> c) {
        if (null == c) {
            return false;
        }
        for (final Object object : c) {
            if (object instanceof DelayedPushMsObject) {
                final DelayedPushMsObject e = (DelayedPushMsObject) object;
                existingPushObjects.remove(e.getPushObject());
            }
        }
        return delayQueue.removeAll(c);
    }

    @Override
    public synchronized void clear() {
        existingPushObjects.clear();
        delayQueue.clear();
    }

    @Override
    public int remainingCapacity() {
        return delayQueue.remainingCapacity();
    }

    @Override
    public Object[] toArray() {
        return delayQueue.toArray();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return delayQueue.retainAll(c);
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return delayQueue.toArray(a);
    }

    @Override
    public String toString() {
        return delayQueue.toString();
    }

    @Override
    public boolean remove(Object o) {
        if (o instanceof DelayedPushMsObject) {
            final DelayedPushMsObject e = (DelayedPushMsObject) o;
            existingPushObjects.remove(e.getPushObject());
        }
        return delayQueue.remove(o);
    }

    @Override
    public Iterator<DelayedPushMsObject> iterator() {
        return delayQueue.iterator();
    }

}
