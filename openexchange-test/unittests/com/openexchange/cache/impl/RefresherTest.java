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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.cache.impl;

import java.io.InputStream;
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import junit.framework.TestCase;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheElement;
import com.openexchange.caching.CacheExceptionCode;
import com.openexchange.caching.CacheKey;
import com.openexchange.caching.CacheService;
import com.openexchange.caching.CacheStatistics;
import com.openexchange.caching.ElementAttributes;
import com.openexchange.caching.dynamic.OXObjectFactory;
import com.openexchange.caching.dynamic.Refresher;
import com.openexchange.exception.OXException;
import com.openexchange.server.services.ServerServiceRegistry;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class RefresherTest extends TestCase {

    private static final String KEY = "RefresherTest";

    private Consumer consumer1;

    private Consumer consumer2;

    private Remover remover;

    private Thread thread1;

    private Thread thread2;

    private Thread thread3;

    /**
     * Default constructor.
     * @param name test name.
     */
    public RefresherTest(final String name) {
        super(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        // Some other tests let there other Cache instances.
        ServerServiceRegistry.getInstance().clearRegistry();
        ServerServiceRegistry.getInstance().addService(CacheService.class, new CacheService() {
            private final Cache cache = new Cache() {
                private Serializable value;
                @Override
                public void clear() {
                    throw new UnsupportedOperationException();
                }
                @Override
                public void dispose() {
                    throw new UnsupportedOperationException();
                }
                @Override
                public Object get(final Serializable key) {
                    Object retval = null;
                    if (KEY.equals(key)) {
                        retval = value;
                    }
                    return retval;
                }
                @Override
                public CacheElement getCacheElement(final Serializable key) {
                    throw new UnsupportedOperationException();
                }
                @Override
                public ElementAttributes getDefaultElementAttributes() {
                    throw new UnsupportedOperationException();
                }
                @Override
                public Object getFromGroup(final Serializable key, final String group) {
                    throw new UnsupportedOperationException();
                }
                @Override
                public CacheStatistics getStatistics() {
                    throw new UnsupportedOperationException();
                }
                @Override
                public void invalidateGroup(final String group) {
                    throw new UnsupportedOperationException();
                }
                @Override
                public CacheKey newCacheKey(final int contextId, final int objectId) {
                    throw new UnsupportedOperationException();
                }
                @Override
                public CacheKey newCacheKey(final int contextId, final Serializable... obj) {
                    throw new UnsupportedOperationException();
                }
                @Override
                public void put(final Serializable key, final Serializable obj) throws OXException {
                    if (!KEY.equals(key)) {
                        throw CacheExceptionCode.CACHE_ERROR.create(key);
                    }
                    if (!(obj instanceof Condition || obj instanceof Integer)) {
                        fail("Wrong value: " + obj.getClass().getName());
                    }
                    this.value = obj;
//                    if (obj instanceof Condition) {
//                        this.value = obj;
//                    } else if (rand.nextBoolean()) {
//                        this.value = obj;
//                    } else {
//                        this.value = null;
//                    }
                }
                @Override
                public void put(final Serializable key, final Serializable val, final ElementAttributes attr) {
                    throw new UnsupportedOperationException();
                }
                @Override
                public void putInGroup(final Serializable key, final String groupName, final Object value, final ElementAttributes attr) {
                    throw new UnsupportedOperationException();
                }
                @Override
                public void putInGroup(final Serializable key, final String groupName, final Serializable value) {
                    throw new UnsupportedOperationException();
                }
                @Override
                public void putSafe(final Serializable key, final Serializable obj) throws OXException {
                    if (!KEY.equals(key)) {
                        throw CacheExceptionCode.CACHE_ERROR.create(key);
                    }
                    if (null != value) {
                        throw CacheExceptionCode.FAILED_SAFE_PUT.create();
                    }
                    if (!(obj instanceof Condition || obj instanceof Integer)) {
                        fail("Wrong value: " + obj.getClass().getName());
                    }
                    this.value = obj;
//                    if (obj instanceof Condition) {
//                        this.value = obj;
//                    } else if (rand.nextBoolean()) {
//                        this.value = obj;
//                    } else {
//                        this.value = null;
//                    }
                }
                @Override
                public void remove(final Serializable key) throws OXException {
                    if (!KEY.equals(key)) {
                        throw CacheExceptionCode.CACHE_ERROR.create(key);
                    }
                    if (!(value instanceof Condition)) {
                        // Cache only removes normal object if it times out.
                        value = null;
                    }
                }
                @Override
                public void removeFromGroup(final Serializable key, final String group) {
                    throw new UnsupportedOperationException();
                }
                @Override
                public void setDefaultElementAttributes(final ElementAttributes attr) {
                    throw new UnsupportedOperationException();
                }
                @Override
                public void localRemove(final Serializable key) {
                    throw new UnsupportedOperationException();
                }
                @Override
                public void localRemoveFromGroup(Serializable key, String group) {
                    throw new UnsupportedOperationException();
                }
                @Override
                public void localPut(Serializable key, Serializable value2) {
                    throw new UnsupportedOperationException();
                }
                @Override
                public boolean isDistributed() {
                    throw new UnsupportedOperationException();
                }
                @Override
                public boolean isReplicated() {
                    throw new UnsupportedOperationException();
                }
            };
            @Override
            public void freeCache(final String name) {
                throw new UnsupportedOperationException();
            }
            @Override
            public Cache getCache(final String name) {
                Cache retval = null;
                if (KEY.equals(name)) {
                    retval = cache;
                }
                return retval;
            }
            @Override
            public void loadConfiguration(final String cacheConfigFile) {
                throw new UnsupportedOperationException();
            }
            @Override
            public void loadConfiguration(final InputStream inputStream) throws OXException {
                throw new UnsupportedOperationException();
            }
            @Override
            public void loadDefaultConfiguration() {
                throw new UnsupportedOperationException();
            }
            @Override
            public CacheKey newCacheKey(final int contextId, final int objectId) {
                throw new UnsupportedOperationException();
            }
            @Override
            public CacheKey newCacheKey(final int contextId, final Serializable obj) {
                throw new UnsupportedOperationException();
            }
            @Override
            public CacheKey newCacheKey(final int contextId, final Serializable... objs) {
                throw new UnsupportedOperationException();
            }
        });
        final Refreshed refreshed = new Refreshed();
        consumer1 = new Consumer(refreshed);
        consumer2 = new Consumer(refreshed);
        remover = new Remover();
        thread1 = new Thread(consumer1);
        thread2 = new Thread(consumer2);
        thread3 = new Thread(remover);
    }

    public void testRefresher() throws InterruptedException {
        thread3.start();
        thread1.start();
        thread2.start();
        Thread.sleep(100000);
        remover.stop();
        consumer1.stop();
        consumer2.stop();
        thread3.join();
        thread1.join();
        thread2.join();
        if (null != remover.getE()) {
            remover.getE().printStackTrace();
            fail(remover.getE().toString());
        }
        if (null != consumer1.getE()) {
            consumer1.getE().printStackTrace();
            fail(consumer1.getE().toString());
        }
        if (null != consumer2.getE()) {
            consumer2.getE().printStackTrace();
            fail(consumer2.getE().toString());
        }
    }

    static final OXObjectFactory<Integer> factory = new OXObjectFactory<Integer>() {
        private final Lock lock = new ReentrantLock();
        @Override
        public Lock getCacheLock() {
            return lock;
        }
        @Override
        public Serializable getKey() {
            return KEY;
        }
        @Override
        public Integer load() throws OXException {
            try {
                Thread.sleep(100);
            } catch (final InterruptedException e) {
                throw new OXException(e);
            }
            return Integer.valueOf(1);
        }
    };

    private static class Refreshed extends Refresher<Integer> {
        private Integer delegate;
        Refreshed() throws OXException {
            super(factory, KEY, false);
            delegate = refresh();
        }
        private int getValue() {
            try {
                final Object tmp = refresh();
                if (tmp instanceof Integer) {
                    delegate = (Integer) tmp;
                } else {
                    throw new ClassCastException("tmp is an " + tmp.getClass().getName());
                }
            } catch (final OXException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
            return delegate.intValue();
        }
    }

    private static class Consumer implements Runnable {
        private final Refreshed refreshed;
        private final AtomicBoolean run = new AtomicBoolean(true);
        private Exception e;
        public Consumer(final Refreshed refreshed) {
            super();
            this.refreshed = refreshed;
        }
        public void stop() {
            run.set(false);
        }
        @Override
        public void run() {
            try {
                while (run.get()) {
                    refreshed.getValue();
                    Thread.sleep(10);
                }
            } catch (final Exception e) {
                this.e = e;
            }
        }
        public Exception getE() {
            return e;
        }
    }

    private static class Remover implements Runnable {
        private final AtomicBoolean run = new AtomicBoolean(true);
        private Exception e;
        public Remover() {
            super();
        }
        public void stop() {
            run.set(false);
        }
        @Override
        public void run() {
            try {
                final Cache cache = ServerServiceRegistry.getInstance().getService(CacheService.class).getCache(KEY);
                while (run.get()) {
                    cache.remove(KEY);
                    Thread.sleep(15);
                }
            } catch (final Exception e) {
                this.e = e;
            }
        }
        public Exception getE() {
            return e;
        }
    }
}
