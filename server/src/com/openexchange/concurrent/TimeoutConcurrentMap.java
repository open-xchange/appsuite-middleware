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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.openexchange.server.ServiceException;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;

/**
 * {@link TimeoutConcurrentMap} - A timed concurrent map.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class TimeoutConcurrentMap<K, V> {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(TimeoutConcurrentMap.class);

    /*-
     * Members
     */

    private final ConcurrentMap<K, ValueWrapper<V>> map;

    private final ScheduledTimerTask timeoutTask;

    private TimeoutListener<V> defaultTimeoutListener;

    /**
     * Initializes a new {@link TimeoutConcurrentMap}.
     * 
     * @param shrinkerIntervalSeconds The shrinker interval in seconds
     * @throws ServiceException If initialization fails due to missing {@link TimerService timer service}
     */
    public TimeoutConcurrentMap(final int shrinkerIntervalSeconds) throws ServiceException {
        super();
        map = new ConcurrentHashMap<K, ValueWrapper<V>>();
        final TimerService timer = ServerServiceRegistry.getInstance().getService(TimerService.class, true);
        timeoutTask = timer.scheduleWithFixedDelay(new TimedRunnable<K, V>(map), 1000, shrinkerIntervalSeconds * 1000);
    }

    /**
     * Disposes this timed map. This is a shut-down command.
     */
    public void dispose() {
        timeoutTask.cancel(true);
        try {
            final TimerService timer = ServerServiceRegistry.getInstance().getService(TimerService.class, true);
            timer.purge();
        } catch (final ServiceException e) {
            LOG.warn(e.getMessage(), e);
        }
        clear();
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
     */
    public void timeoutAll() {
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
     */
    public void timeout(final K key) {
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
     */
    public V put(final K key, final V value, final int timeToLiveSeconds, final TimeoutListener<V> timeoutListener) {
        final ValueWrapper<V> vw = map.put(key, new ValueWrapper<V>(value, timeToLiveSeconds * 1000, timeoutListener));
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
     */
    public V putIfAbsent(final K key, final V value, final int timeToLiveSeconds, final TimeoutListener<V> timeoutListener) {
        final ValueWrapper<V> vw = map.putIfAbsent(key, new ValueWrapper<V>(value, timeToLiveSeconds * 1000, timeoutListener));
        if (null == vw) {
            return null;
        }
        return vw.value;
    }

    /**
     * Gets the value associated with given key.
     * 
     * @param key The key
     * @return The value associated with given key or <code>null</code>
     */
    public V get(final K key) {
        // Remove from map to avoid time-out event in the meantime
        final ValueWrapper<V> vw = map.remove(key);
        if (null == vw) {
            return null;
        }
        vw.touch();
        // Restore to map
        map.put(key, vw);
        return vw.value;
    }

    /**
     * Removes the value associated with given key.
     * 
     * @param key The key
     * @return The value associated with given key or <code>null</code>
     */
    public V remove(final K key) {
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

        public final V value;

        public final long ttl;

        public final TimeoutListener<V> timeoutListener;

        private long lastAccessed;

        public ValueWrapper(final V value, final long ttl, final TimeoutListener<V> timeoutListener) {
            super();
            this.value = value;
            this.ttl = ttl;
            lastAccessed = System.currentTimeMillis();
            this.timeoutListener = timeoutListener;
        }

        public void touch() {
            lastAccessed = System.currentTimeMillis();
        }

        public long getLastAccessed() {
            return lastAccessed;
        }

    }

    private static final class TimedRunnable<K, V> implements Runnable {

        private final ConcurrentMap<K, ValueWrapper<V>> tmap;

        public TimedRunnable(final ConcurrentMap<K, ValueWrapper<V>> tmap) {
            super();
            this.tmap = tmap;
        }

        public void run() {
            final long now = System.currentTimeMillis();
            for (final Iterator<ValueWrapper<V>> it = tmap.values().iterator(); it.hasNext();) {
                final ValueWrapper<V> vw = it.next();
                if ((now - vw.getLastAccessed()) > vw.ttl) {
                    it.remove();
                    if (vw.timeoutListener != null) {
                        vw.timeoutListener.onTimeout(vw.value);
                    }
                }
            }
        }

    }

    /**
     * The time-out listener triggered on element time-out.
     */
    public static interface TimeoutListener<V> {

        /**
         * Performs the actions on timed-out element.
         * 
         * @param element The timed-out element
         */
        public void onTimeout(V element);
    }
}
