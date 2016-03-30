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

package com.openexchange.concurrent;

import static com.openexchange.server.services.ServerServiceRegistry.getInstance;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.openexchange.exception.OXException;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;

/**
 * {@link TimeoutConcurrentSet} - A timed concurrent set.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class TimeoutConcurrentSet<E> {

    /*-
     * Members
     */

    private final ConcurrentMap<E, ElementWrapper<E>> map;

    private final ScheduledTimerTask timeoutTask;

    private final boolean forceTimeout;

    private TimeoutListener<E> defaultTimeoutListener;

    private volatile boolean disposed;

    /**
     * Initializes a new {@link TimeoutConcurrentSet}.
     *
     * @param shrinkerIntervalSeconds The shrinker interval in seconds
     * @throws OXException If initialization fails due to missing {@link TimerService timer service}
     */
    public TimeoutConcurrentSet(final int shrinkerIntervalSeconds) throws OXException {
        this(shrinkerIntervalSeconds, false);
    }

    /**
     * Initializes a new {@link TimeoutConcurrentSet}.
     *
     * @param shrinkerIntervalSeconds The shrinker interval in seconds
     * @param forceTimeout <code>true</code> to force initial time-out of contained elements even if they were "touched"; otherwise
     *            <code>false</code> to keep them alive as long as not timed-out
     * @throws OXException If initialization fails due to missing {@link TimerService timer service}
     */
    public TimeoutConcurrentSet(final int shrinkerIntervalSeconds, final boolean forceTimeout) throws OXException {
        super();
        this.forceTimeout = forceTimeout;
        map = new ConcurrentHashMap<E, ElementWrapper<E>>();
        final TimerService timer = getInstance().getService(TimerService.class, true);
        timeoutTask = timer.scheduleWithFixedDelay(new TimedRunnable<E>(map), 1000, shrinkerIntervalSeconds * 1000);
    }

    /**
     * Checks if map is empty.
     *
     * @return <code>true</code> if map is empty; otherwise <code>false</code>
     */
    public boolean isEmpty() {
        return map.isEmpty();
    }

    /**
     * Disposes this timed map. This is a shut-down command.
     */
    public void dispose() {
        if (disposed) {
            return;
        }
        synchronized (this) {
            if (disposed) {
                return;
            }
            timeoutTask.cancel(true);
            final TimerService timer = getInstance().getService(TimerService.class);
            if (null != timer) {
                timer.purge();
            }
            clear();
            disposed = true;
        }
    }

    /**
     * Clears this time-out map.
     */
    public void clear() {
        map.clear();
    }

    /**
     * Acts like all elements kept in this time-out map receive their time-out event. <br>
     * Furthermore the map is cleared.
     *
     * @throws IllegalStateException If this time-out map was {@link #dispose() disposed} before
     */
    public void timeoutAll() {
        if (disposed) {
            throw new IllegalStateException("time-out map was disposed.");
        }
        for (final Iterator<ElementWrapper<E>> it = map.values().iterator(); it.hasNext();) {
            final ElementWrapper<E> vw = it.next();
            it.remove();
            if (vw.timeoutListener != null) {
                vw.timeoutListener.onTimeout(vw.value);
            }
        }
    }

    /**
     * Acts like the element receives its time-out event. <br>
     * Furthermore the element is removed from map.
     *
     * @param element The element
     * @throws IllegalStateException If this time-out map was {@link #dispose() disposed} before
     */
    public void timeout(final E element) {
        if (disposed) {
            throw new IllegalStateException("time-out map was disposed.");
        }
        final ElementWrapper<E> vw = map.remove(element);
        if (vw != null && vw.timeoutListener != null) {
            vw.timeoutListener.onTimeout(vw.value);
        }
    }

    /**
     * Puts specified element into this time-out map with default time-out listener.
     *
     * @param element The element to put
     * @param timeToLiveSeconds The value's time-to-live seconds
     * @return <code>true</code> if this set did not already contain the specified element
     * @throws IllegalStateException If this time-out map was {@link #dispose() disposed} before
     */
    public boolean add(final E element, final int timeToLiveSeconds) {
        return add(element, timeToLiveSeconds, defaultTimeoutListener);
    }

    /**
     * Adds specified key-value-pair into this time-out map.
     *
     * @param element The element to put
     * @param timeToLiveSeconds The value's time-to-live seconds
     * @param timeoutListener The value's time-out listener triggered on its time-out event
     * @return <code>true</code> if this set did not already contain the specified element
     * @throws IllegalStateException If this time-out map was {@link #dispose() disposed} before
     */
    public boolean add(final E element, final int timeToLiveSeconds, final TimeoutListener<E> timeoutListener) {
        if (disposed) {
            throw new IllegalStateException("time-out map was disposed.");
        }
        final ElementWrapper<E> vw =
            map.putIfAbsent(element, new ElementWrapper<E>(element, timeToLiveSeconds * 1000, forceTimeout, timeoutListener));
        if (null == vw) {
            return true;
        }
        vw.touch();
        return false;
    }

    /**
     * Checks if this time-out map contains specified element.
     *
     * @param element The element
     * @return <code>true</code> if this time-out map contains a mapping for specified key; otherwise <code>false</code>
     */
    public boolean contains(final E element) {
        if (disposed) {
            throw new IllegalStateException("time-out map was disposed.");
        }
        return map.containsKey(element);
    }

    /**
     * Returns an iterator over the elements in this set. The elements are returned in no particular order (unless this set is an instance
     * of some class that provides a guarantee).
     *
     * @return an iterator over the elements in this set.
     */
    public Iterator<E> iterator() {
        if (disposed) {
            throw new IllegalStateException("time-out map was disposed.");
        }
        return new ElementIterator<E>(map.values().iterator());
    }

    /**
     * Returns any of the elements in this set.
     *
     * @return Any element or <code>null</code> if this set is empty
     */
    public E getAny() {
        if (disposed) {
            throw new IllegalStateException("time-out map was disposed.");
        }
        if (map.isEmpty()) {
            return null;
        }
        final Iterator<ElementWrapper<E>> iterator = map.values().iterator();
        return iterator.hasNext() ? iterator.next().touch() : null;
    }

    /**
     * Removes the element.
     *
     * @param element The element
     * @return <code>true</code> if the set contained the specified element.
     * @throws IllegalStateException If this time-out map was {@link #dispose() disposed} before
     */
    public boolean remove(final E element) {
        if (disposed) {
            throw new IllegalStateException("time-out map was disposed.");
        }
        return (null != map.remove(element));
    }

    /**
     * Gets the default listener triggered on element timeout.
     *
     * @return The default listener triggered on element timeout
     */
    public TimeoutListener<E> getDefaultTimeoutListener() {
        return defaultTimeoutListener;
    }

    /**
     * Sets the default listener triggered on element timeout.
     *
     * @param defaultTimeoutListener The default listener triggered on element timeout
     */
    public void setDefaultTimeoutListener(final TimeoutListener<E> defaultTimeoutListener) {
        this.defaultTimeoutListener = defaultTimeoutListener;
    }

    private static final class ElementWrapper<E> {

        public final E value;

        public final long ttl;

        public final TimeoutListener<E> timeoutListener;

        public final boolean forceTimeout;

        public volatile long lastAccessed;

        public ElementWrapper(final E value, final long ttl, final boolean forceTimeout, final TimeoutListener<E> timeoutListener) {
            super();
            this.value = value;
            this.ttl = ttl;
            lastAccessed = System.currentTimeMillis();
            this.timeoutListener = timeoutListener;
            this.forceTimeout = forceTimeout;
        }

        public E touch() {
            if (forceTimeout) {
                // Force time out; don't touch last-accessed time stamp.
                return value;
            }
            lastAccessed = System.currentTimeMillis();
            return value;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((value == null) ? 0 : value.hashCode());
            return result;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof ElementWrapper)) {
                return false;
            }
            @SuppressWarnings("unchecked") final ElementWrapper<E> other = (ElementWrapper<E>) obj;
            if (value == null) {
                if (other.value != null) {
                    return false;
                }
            } else if (!value.equals(other.value)) {
                return false;
            }
            return true;
        }

    }

    private static final class TimedRunnable<E> implements Runnable {

        private final ConcurrentMap<E, ElementWrapper<E>> tmap;

        TimedRunnable(final ConcurrentMap<E, ElementWrapper<E>> tmap) {
            super();
            this.tmap = tmap;
        }

        @Override
        public void run() {
            final long now = System.currentTimeMillis();
            for (final Iterator<ElementWrapper<E>> it = tmap.values().iterator(); it.hasNext();) {
                final ElementWrapper<E> vw = it.next();
                if ((now - vw.lastAccessed) > vw.ttl) {
                    it.remove();
                    if (vw.timeoutListener != null) {
                        vw.timeoutListener.onTimeout(vw.value);
                    }
                }
            }
        }
    }

    private static final class ElementIterator<E> implements Iterator<E> {

        private final Iterator<ElementWrapper<E>> delegatee;

        public ElementIterator(final Iterator<ElementWrapper<E>> delegatee) {
            super();
            this.delegatee = delegatee;
        }

        @Override
        public boolean hasNext() {
            return delegatee.hasNext();
        }

        @Override
        public E next() {
            return delegatee.next().touch();
        }

        @Override
        public void remove() {
            delegatee.remove();
        }

    }

}
