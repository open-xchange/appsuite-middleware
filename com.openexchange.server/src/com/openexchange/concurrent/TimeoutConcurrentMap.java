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
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.openexchange.exception.OXException;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;

/**
 * {@link TimeoutConcurrentMap} - A timed concurrent map.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class TimeoutConcurrentMap<K, V> {

    /*-
     * Members
     */

    private final ConcurrentMap<K, ValueWrapper<V>> map;
    private final ScheduledTimerTask timeoutTask;
    private final boolean forceTimeout;
    private volatile TimeoutListener<V> defaultTimeoutListener;
    private volatile boolean disposed;

    /**
     * Initializes a new {@link TimeoutConcurrentMap}.
     *
     * @param shrinkerIntervalSeconds The shrinker interval in seconds
     * @throws OXException If initialization fails due to missing {@link TimerService timer service}
     */
    public TimeoutConcurrentMap(final int shrinkerIntervalSeconds) throws OXException {
        this(shrinkerIntervalSeconds, false);
    }

    /**
     * Initializes a new {@link TimeoutConcurrentMap}.
     *
     * @param shrinkerIntervalSeconds The shrinker interval in seconds
     * @param forceTimeout <code>true</code> to force initial time-out of contained elements even if they were "touched"; otherwise
     *            <code>false</code> to keep them alive as long as not timed-out
     * @throws OXException If initialization fails due to missing {@link TimerService timer service}
     */
    public TimeoutConcurrentMap(final int shrinkerIntervalSeconds, final boolean forceTimeout) throws OXException {
        super();
        this.forceTimeout = forceTimeout;
        map = new ConcurrentHashMap<K, ValueWrapper<V>>();
        final TimerService timer = getInstance().getService(TimerService.class, true);
        final int delay = shrinkerIntervalSeconds * 1000;
        timeoutTask = timer.scheduleWithFixedDelay(new TimedRunnable<K, V>(map), delay, delay);
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
     * Returns a set view of the keys contained in this map. The set is backed by the map, so changes to the map are reflected in the set,
     * and vice-versa.
     *
     * @return A set view of the keys contained in this map.
     */
    public Set<K> keySet() {
        return map.keySet();
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
     * Acts like all values kept in this time-out map receive their time-out event. <br>
     * Furthermore the map is cleared.
     *
     * @throws IllegalStateException If this time-out map was {@link #dispose() disposed} before
     */
    public void timeoutAll() {
        if (disposed) {
            throw new IllegalStateException("time-out map was disposed.");
        }
        for (final Iterator<ValueWrapper<V>> it = map.values().iterator(); it.hasNext();) {
            final ValueWrapper<V> vw = it.next();
            it.remove();
            if (vw.timeoutListener != null) {
                vw.timeoutListener.onTimeout(vw.value);
            }
        }
    }

    /**
     * Acts like the value associated with specified key receives its time-out event. <br>
     * Furthermore the value is removed from map.
     *
     * @param key The value's key
     * @throws IllegalStateException If this time-out map was {@link #dispose() disposed} before
     */
    public void timeout(final K key) {
        if (disposed) {
            throw new IllegalStateException("time-out map was disposed.");
        }
        final ValueWrapper<V> vw = map.remove(key);
        if (vw != null && vw.timeoutListener != null) {
            vw.timeoutListener.onTimeout(vw.value);
        }
    }

    /**
     * Puts specified key-value-pair into this time-out map with default time-out listener.
     *
     * @param key The value's key
     * @param value The value to put
     * @param timeToLiveSeconds The value's time-to-live seconds
     * @return The value previously associated with given key or <code>null</code>
     * @throws IllegalStateException If this time-out map was {@link #dispose() disposed} before
     */
    public V put(final K key, final V value, final int timeToLiveSeconds) {
        return put(key, value, timeToLiveSeconds, defaultTimeoutListener);
    }

    /**
     * Puts specified key-value-pair into this time-out map.
     *
     * @param key The value's key
     * @param value The value to put
     * @param timeToLiveSeconds The value's time-to-live seconds
     * @param timeoutListener The value's time-out listener triggered on its time-out event
     * @return The value previously associated with given key or <code>null</code>
     * @throws IllegalStateException If this time-out map was {@link #dispose() disposed} before
     */
    public V put(final K key, final V value, final int timeToLiveSeconds, final TimeoutListener<V> timeoutListener) {
        if (disposed) {
            throw new IllegalStateException("time-out map was disposed.");
        }
        final ValueWrapper<V> vw = map.put(key, new ValueWrapper<V>(value, timeToLiveSeconds * 1000, forceTimeout, timeoutListener));
        if (null == vw) {
            return null;
        }
        return vw.value;
    }

    /**
     * Puts specified key-value-pair into this time-out map with default time-out listener only if the specified key is not already
     * associated with a value.
     *
     * @param key The value's key
     * @param value The value to put
     * @param timeToLiveSeconds The value's time-to-live seconds
     * @return The previous value associated with specified key, or <code>null</code> if there was no mapping for key.
     * @throws IllegalStateException If this time-out map was {@link #dispose() disposed} before
     */
    public V putIfAbsent(final K key, final V value, final int timeToLiveSeconds) {
        return putIfAbsent(key, value, timeToLiveSeconds, defaultTimeoutListener);
    }

    /**
     * Puts specified key-value-pair into this time-out map only if the specified key is not already associated with a value.
     *
     * @param key The value's key
     * @param value The value to put
     * @param timeToLiveSeconds The value's time-to-live seconds
     * @param timeoutListener The value's time-out listener triggered on its time-out event
     * @return The previous value associated with specified key, or <code>null</code> if there was no mapping for key.
     * @throws IllegalStateException If this time-out map was {@link #dispose() disposed} before
     */
    public V putIfAbsent(final K key, final V value, final int timeToLiveSeconds, final TimeoutListener<V> timeoutListener) {
        if (disposed) {
            throw new IllegalStateException("time-out map was disposed.");
        }
        final ValueWrapper<V> vw =
            map.putIfAbsent(key, new ValueWrapper<V>(value, timeToLiveSeconds * 1000, forceTimeout, timeoutListener));
        if (null == vw) {
            return null;
        }
        return vw.value;
    }

    /**
     * Checks if this time-out map contains a mapping for specified key.
     *
     * @param key The key
     * @return <code>true</code> if this time-out map contains a mapping for specified key; otherwise <code>false</code>
     */
    public boolean containsKey(final K key) {
        return map.containsKey(key);
    }

    /**
     * Gets the value associated with given key.
     *
     * @param key The key
     * @return The value associated with given key or <code>null</code>
     */
    public V get(final K key) {
        /*
         * It is possible that a get() is performed while a running timer attempts to remove this value. In this case the caller receives
         * associated value although it is actually timed-out. A little misbehavior that is acceptable.
         */
        final ValueWrapper<V> vw = map.get(key);
        if (null == vw) {
            return null;
        }
        return vw.touch();
    }

    /**
     * Removes the value associated with given key.
     *
     * @param key The key
     * @return The value associated with given key or <code>null</code>
     * @throws IllegalStateException If this time-out map was {@link #dispose() disposed} before
     */
    public V remove(final K key) {
        if (disposed) {
            throw new IllegalStateException("time-out map was disposed.");
        }
        final ValueWrapper<V> vw = map.remove(key);
        if (null == vw) {
            return null;
        }
        return vw.value;
    }

    /**
     * Gets the default listener triggered on element timeout.
     *
     * @return The default listener triggered on element timeout
     */
    public TimeoutListener<V> getDefaultTimeoutListener() {
        return defaultTimeoutListener;
    }

    /**
     * Sets the default listener triggered on element timeout.
     *
     * @param defaultTimeoutListener The default listener triggered on element timeout
     */
    public void setDefaultTimeoutListener(final TimeoutListener<V> defaultTimeoutListener) {
        this.defaultTimeoutListener = defaultTimeoutListener;
    }

    private static final class ValueWrapper<V> {

        final V value;
        final long ttl;
        final TimeoutListener<V> timeoutListener;
        final boolean forceTimeout;
        volatile long lastAccessed;

        ValueWrapper(final V value, final long ttl, final boolean forceTimeout, final TimeoutListener<V> timeoutListener) {
            super();
            this.value = value;
            this.ttl = ttl;
            lastAccessed = System.currentTimeMillis();
            this.timeoutListener = timeoutListener;
            this.forceTimeout = forceTimeout;
        }

        V touch() {
            if (forceTimeout) {
                // Force time out; don't touch last-accessed time stamp.
                return value;
            }
            lastAccessed = System.currentTimeMillis();
            return value;
        }

    }

    private static final class TimedRunnable<K, V> implements Runnable {

        private final ConcurrentMap<K, ValueWrapper<V>> tmap;

        TimedRunnable(final ConcurrentMap<K, ValueWrapper<V>> tmap) {
            super();
            this.tmap = tmap;
        }

        @Override
        public void run() {
            final long now = System.currentTimeMillis();
            for (final Iterator<ValueWrapper<V>> it = tmap.values().iterator(); it.hasNext();) {
                final ValueWrapper<V> vw = it.next();
                if ((now - vw.lastAccessed) > vw.ttl) {
                    it.remove();
                    final TimeoutListener<V> timeoutListener = vw.timeoutListener;
                    if (timeoutListener != null) {
                        timeoutListener.onTimeout(vw.value);
                    }
                }
            }
        }

    }

}
