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

package com.openexchange.sessionstorage.hazelcast;

import static com.openexchange.threadpool.ThreadPools.task;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.IMap;
import com.hazelcast.core.MapEntry;
import com.hazelcast.monitor.LocalMapStats;
import com.hazelcast.query.Expression;
import com.hazelcast.query.Predicate;
import com.openexchange.threadpool.ThreadPools;

/**
 * {@link TimeoutAwareIMap} - Delegates to a given {@link IMap} with respect to timeout setting.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class TimeoutAwareIMap implements IMap<String, HazelcastStoredSession> {

    protected final IMap<String, HazelcastStoredSession> map;
    private final long timeout;

    /**
     * Initializes a new {@link TimeoutAwareIMap}.
     */
    public TimeoutAwareIMap(final IMap<String, HazelcastStoredSession> map, final long timeout) {
        super();
        this.map = map;
        this.timeout = timeout;
    }

    private <V> V getFrom(final Future<V> f, final V defaultValue) {
        try {
            return f.get(timeout, TimeUnit.MILLISECONDS);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e.getMessage(), e);
        } catch (final ExecutionException e) {
            ThreadPools.launderThrowable(e, RuntimeException.class);
            return defaultValue;
        } catch (final TimeoutException e) {
            return defaultValue;
        } catch (final CancellationException e) {
            return defaultValue;
        }
    }

    private <V> V get(final Callable<V> task) {
        return get(task, null);
    }

    private <V> V get(final Callable<V> task, final V defaultValue) {
        return getFrom(ThreadPools.getThreadPool().submit(task(task)), defaultValue);
    }

    @Override
    public boolean containsKey(final Object key) {
        return get(new Callable<Boolean>() {

            @Override
            public Boolean call() throws Exception {
                return Boolean.valueOf(map.containsKey(key));
            }
        }, Boolean.FALSE).booleanValue();
    }

    @Override
    public InstanceType getInstanceType() {
        return get(new Callable<InstanceType>() {

            @Override
            public InstanceType call() throws Exception {
                return map.getInstanceType();
            }
        });
    }

    @Override
    public boolean containsValue(final Object value) {
        return get(new Callable<Boolean>() {

            @Override
            public Boolean call() throws Exception {
                return Boolean.valueOf(map.containsValue(value));
            }
        }, Boolean.FALSE).booleanValue();
    }

    @Override
    public void destroy() {
        get(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                map.destroy();
                return null;
            }
        });
    }

    @Override
    public HazelcastStoredSession get(final Object key) {
        return get(new Callable<HazelcastStoredSession>() {

            @Override
            public HazelcastStoredSession call() throws Exception {
                return map.get(key);
            }
        });
    }

    @Override
    public Object getId() {
        return get(new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                return map.getId();
            }
        });
    }

    @Override
    public HazelcastStoredSession put(final String key, final HazelcastStoredSession value) {
        return get(new Callable<HazelcastStoredSession>() {

            @Override
            public HazelcastStoredSession call() throws Exception {
                return map.put(key, value);
            }
        });
    }

    @Override
    public HazelcastStoredSession remove(final Object key) {
        return get(new Callable<HazelcastStoredSession>() {

            @Override
            public HazelcastStoredSession call() throws Exception {
                return map.remove(key);
            }
        });
    }

    @Override
    public int size() {
        return get(new Callable<Integer>() {

            @Override
            public Integer call() throws Exception {
                return Integer.valueOf(map.size());
            }
        }, Integer.valueOf(0)).intValue();
    }

    @Override
    public boolean remove(final Object key, final Object value) {
        return get(new Callable<Boolean>() {

            @Override
            public Boolean call() throws Exception {
                return Boolean.valueOf(map.remove(key, value));
            }
        }, Boolean.FALSE).booleanValue();
    }

    @Override
    public boolean isEmpty() {
        return get(new Callable<Boolean>() {

            @Override
            public Boolean call() throws Exception {
                return Boolean.valueOf(map.isEmpty());
            }
        }, Boolean.TRUE).booleanValue();
    }

    @Override
    public void flush() {
        get(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                map.flush();
                return null;
            }
        });
    }

    @Override
    public String getName() {
        return get(new Callable<String>() {

            @Override
            public String call() throws Exception {
                return map.getName();
            }
        });
    }

    @Override
    public Map<String, HazelcastStoredSession> getAll(final Set<String> keys) {
        return get(new Callable<Map<String, HazelcastStoredSession>>() {

            @Override
            public Map<String, HazelcastStoredSession> call() throws Exception {
                return map.getAll(keys);
            }
        });
    }

    @Override
    public Future<HazelcastStoredSession> getAsync(final String key) {
        return get(new Callable<Future<HazelcastStoredSession>>() {

            @Override
            public Future<HazelcastStoredSession> call() throws Exception {
                return map.getAsync(key);
            }
        });
    }

    @Override
    public Future<HazelcastStoredSession> putAsync(final String key, final HazelcastStoredSession value) {
        return get(new Callable<Future<HazelcastStoredSession>>() {

            @Override
            public Future<HazelcastStoredSession> call() throws Exception {
                return map.putAsync(key, value);
            }
        });
    }

    @Override
    public Future<HazelcastStoredSession> removeAsync(final String key) {
        return get(new Callable<Future<HazelcastStoredSession>>() {

            @Override
            public Future<HazelcastStoredSession> call() throws Exception {
                return map.removeAsync(key);
            }
        });
    }

    @Override
    public Object tryRemove(final String key, final long timeout, final TimeUnit timeunit) throws TimeoutException {
        return get(new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                return map.tryRemove(key, timeout, timeunit);
            }
        });
    }

    @Override
    public boolean tryPut(final String key, final HazelcastStoredSession value, final long timeout, final TimeUnit timeunit) {
        return get(new Callable<Boolean>() {

            @Override
            public Boolean call() throws Exception {
                return Boolean.valueOf(map.tryPut(key, value, timeout, timeunit));
            }
        }).booleanValue();
    }

    @Override
    public void putAll(final Map<? extends String, ? extends HazelcastStoredSession> m) {
        get(new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                map.putAll(m);
                return null;
            }
        });
    }

    @Override
    public HazelcastStoredSession put(final String key, final HazelcastStoredSession value, final long ttl, final TimeUnit timeunit) {
        return get(new Callable<HazelcastStoredSession>() {

            @Override
            public HazelcastStoredSession call() throws Exception {
                return map.put(key, value, ttl, timeunit);
            }
        });
    }

    @Override
    public void putTransient(final String key, final HazelcastStoredSession value, final long ttl, final TimeUnit timeunit) {
        get(new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                map.putTransient(key, value, ttl, timeunit);
                return null;
            }
        });
    }

    @Override
    public void clear() {
        get(new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                map.clear();
                return null;
            }
        });
    }

    @Override
    public HazelcastStoredSession putIfAbsent(final String key, final HazelcastStoredSession value) {
        return get(new Callable<HazelcastStoredSession>() {

            @Override
            public HazelcastStoredSession call() throws Exception {
                return map.putIfAbsent(key, value);
            }
        });
    }

    @Override
    public HazelcastStoredSession putIfAbsent(final String key, final HazelcastStoredSession value, final long ttl, final TimeUnit timeunit) {
        return get(new Callable<HazelcastStoredSession>() {

            @Override
            public HazelcastStoredSession call() throws Exception {
                return map.putIfAbsent(key, value, ttl, timeunit);
            }
        });
    }

    @Override
    public boolean replace(final String key, final HazelcastStoredSession oldValue, final HazelcastStoredSession newValue) {
        return get(new Callable<Boolean>() {

            @Override
            public Boolean call() throws Exception {
                return Boolean.valueOf(map.replace(key, oldValue, newValue));
            }
        }, Boolean.FALSE).booleanValue();
    }

    @Override
    public HazelcastStoredSession replace(final String key, final HazelcastStoredSession value) {
        return get(new Callable<HazelcastStoredSession>() {

            @Override
            public HazelcastStoredSession call() throws Exception {
                return map.replace(key, value);
            }
        });
    }

    @Override
    public void set(final String key, final HazelcastStoredSession value, final long ttl, final TimeUnit timeunit) {
        get(new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                map.clear();
                return null;
            }
        });

        map.set(key, value, ttl, timeunit);
    }

    @Override
    public HazelcastStoredSession tryLockAndGet(final String key, final long time, final TimeUnit timeunit) throws TimeoutException {
        return get(new Callable<HazelcastStoredSession>() {

            @Override
            public HazelcastStoredSession call() throws Exception {
                return map.tryLockAndGet(key, time, timeunit);
            }
        });
    }

    @Override
    public void putAndUnlock(final String key, final HazelcastStoredSession value) {
        get(new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                map.putAndUnlock(key, value);
                return null;
            }
        });
    }

    @Override
    public void lock(final String key) {
        get(new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                map.lock(key);
                return null;
            }
        });
    }

    @Override
    public boolean isLocked(final String key) {
        return get(new Callable<Boolean>() {

            @Override
            public Boolean call() throws Exception {
                return Boolean.valueOf(map.isLocked(key));
            }
        }, Boolean.FALSE).booleanValue();
    }

    @Override
    public boolean equals(final Object o) {
        return get(new Callable<Boolean>() {

            @Override
            public Boolean call() throws Exception {
                return Boolean.valueOf(map.equals(o));
            }
        }, Boolean.FALSE).booleanValue();
    }

    @Override
    public boolean tryLock(final String key) {
        return get(new Callable<Boolean>() {

            @Override
            public Boolean call() throws Exception {
                return Boolean.valueOf(map.tryLock(key));
            }
        }, Boolean.FALSE).booleanValue();
    }

    @Override
    public int hashCode() {
        return get(new Callable<Integer>() {

            @Override
            public Integer call() throws Exception {
                return Integer.valueOf(map.hashCode());
            }
        }, Integer.valueOf(0)).intValue();
    }

    @Override
    public boolean tryLock(final String key, final long time, final TimeUnit timeunit) {
        return get(new Callable<Boolean>() {

            @Override
            public Boolean call() throws Exception {
                return Boolean.valueOf(map.tryLock(key, time, timeunit));
            }
        }, Boolean.FALSE).booleanValue();
    }

    @Override
    public void unlock(final String key) {
        get(new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                map.unlock(key);
                return null;
            }
        });
    }

    @Override
    public void forceUnlock(final String key) {
        get(new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                map.forceUnlock(key);
                return null;
            }
        });
    }

    @Override
    public boolean lockMap(final long time, final TimeUnit timeunit) {
        return get(new Callable<Boolean>() {

            @Override
            public Boolean call() throws Exception {
                return Boolean.valueOf(map.lockMap(time, timeunit));
            }
        }, Boolean.FALSE).booleanValue();
    }

    @Override
    public void unlockMap() {
        get(new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                map.unlockMap();
                return null;
            }
        });
    }

    @Override
    public void addLocalEntryListener(final EntryListener<String, HazelcastStoredSession> listener) {
        get(new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                map.addLocalEntryListener(listener);
                return null;
            }
        });
    }

    @Override
    public void addEntryListener(final EntryListener<String, HazelcastStoredSession> listener, final boolean includeValue) {
        get(new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                map.addEntryListener(listener, includeValue);
                return null;
            }
        });
    }

    @Override
    public void removeEntryListener(final EntryListener<String, HazelcastStoredSession> listener) {
        get(new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                map.removeEntryListener(listener);
                return null;
            }
        });
    }

    @Override
    public void addEntryListener(final EntryListener<String, HazelcastStoredSession> listener, final String key, final boolean includeValue) {
        get(new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                map.addEntryListener(listener, key, includeValue);
                return null;
            }
        });
    }

    @Override
    public void removeEntryListener(final EntryListener<String, HazelcastStoredSession> listener, final String key) {
        get(new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                map.removeEntryListener(listener, key);
                return null;
            }
        });
    }

    @Override
    public MapEntry<String, HazelcastStoredSession> getMapEntry(final String key) {
        return get(new Callable<MapEntry<String, HazelcastStoredSession>>() {

            @Override
            public MapEntry<String, HazelcastStoredSession> call() throws Exception {
                return map.getMapEntry(key);
            }
        });
    }

    @Override
    public boolean evict(final Object key) {
        return get(new Callable<Boolean>() {

            @Override
            public Boolean call() throws Exception {
                return Boolean.valueOf(map.evict(key));
            }
        }, Boolean.FALSE).booleanValue();
    }

    @Override
    public Set<String> keySet() {
        return get(new Callable<Set<String>>() {

            @Override
            public Set<String> call() throws Exception {
                return map.keySet();
            }
        });
    }

    @Override
    public Collection<HazelcastStoredSession> values() {
        return get(new Callable<Collection<HazelcastStoredSession>>() {

            @Override
            public Collection<HazelcastStoredSession> call() throws Exception {
                return map.values();
            }
        });
    }

    @Override
    public Set<java.util.Map.Entry<String, HazelcastStoredSession>> entrySet() {
        return get(new Callable<Set<java.util.Map.Entry<String, HazelcastStoredSession>>>() {

            @Override
            public Set<java.util.Map.Entry<String, HazelcastStoredSession>> call() throws Exception {
                return map.entrySet();
            }
        });
    }

    @Override
    public Set<String> keySet(final Predicate predicate) {
        return get(new Callable<Set<String>>() {

            @Override
            public Set<String> call() throws Exception {
                return map.keySet(predicate);
            }
        });
    }

    @Override
    public Set<java.util.Map.Entry<String, HazelcastStoredSession>> entrySet(final Predicate predicate) {
        return get(new Callable<Set<java.util.Map.Entry<String, HazelcastStoredSession>>>() {

            @Override
            public Set<java.util.Map.Entry<String, HazelcastStoredSession>> call() throws Exception {
                return map.entrySet(predicate);
            }
        });
    }

    @Override
    public Collection<HazelcastStoredSession> values(final Predicate predicate) {
        return get(new Callable<Collection<HazelcastStoredSession>>() {

            @Override
            public Collection<HazelcastStoredSession> call() throws Exception {
                return map.values(predicate);
            }
        });
    }

    @Override
    public Set<String> localKeySet() {
        return get(new Callable<Set<String>>() {

            @Override
            public Set<String> call() throws Exception {
                return map.localKeySet();
            }
        });
    }

    @Override
    public Set<String> localKeySet(final Predicate predicate) {
        return get(new Callable<Set<String>>() {

            @Override
            public Set<String> call() throws Exception {
                return map.localKeySet(predicate);
            }
        });
    }

    @Override
    public void addIndex(final String attribute, final boolean ordered) {
        get(new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                map.addIndex(attribute, ordered);
                return null;
            }
        });
    }

    @Override
    public void addIndex(final Expression<?> expression, final boolean ordered) {
        get(new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                map.addIndex(expression, ordered);
                return null;
            }
        });
    }

    @Override
    public LocalMapStats getLocalMapStats() {
        return get(new Callable<LocalMapStats>() {

            @Override
            public LocalMapStats call() throws Exception {
                return map.getLocalMapStats();
            }
        });
    }

}
