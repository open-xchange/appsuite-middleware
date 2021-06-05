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

package com.openexchange.mail.cache;

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

/**
 * {@link UniversalCache} - A universal cache.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class UniversalCache<K, V> {

    /**
     * The value yielder interface.
     */
    public static interface ValueYielder<K, V> {

        /**
         * Yields the value from specified argument.
         *
         * @param arg The argument
         * @return The value yielded from specified argument.
         */
        public V yieldValue(K arg);
    }

    private final ConcurrentMap<K, Future<V>> map;

    private final ValueYielder<K, V> yielder;

    /**
     * Initializes a new {@link UniversalCache}.
     */
    public UniversalCache(ValueYielder<K, V> yielder) {
        super();
        map = new ConcurrentHashMap<K, Future<V>>();
        this.yielder = yielder;
    }

    /**
     * Gets the result when passing specified input to yielder.
     *
     * @param k The input.
     * @return The (cached) result.
     * @throws InterruptedException If current thread is interrupted while waiting for result being yielded.
     */
    public V get(K k) throws InterruptedException {
        while (true) {
            Future<V> f = map.get(k);
            if (f == null) {
                final FutureTask<V> tmp = new FutureTask<V>(new UCCallable<K, V>(k, yielder));
                f = map.putIfAbsent(k, tmp);
                if (f == null) {
                    // Not inserted before
                    f = tmp;
                    tmp.run();
                }
            }
            try {
                return f.get();
            } catch (CancellationException e) {
                map.remove(k);
            } catch (ExecutionException e) {
                throw launderThrowable(e.getCause());
            }
        }
    }

    /**
     * Clears the cache-
     */
    public void clear() {
        map.clear();
    }

    private static RuntimeException launderThrowable(Throwable t) {
        if (t instanceof RuntimeException) {
            return (RuntimeException) t;
        } else if (t instanceof Error) {
            throw (Error) t;
        } else {
            throw new IllegalStateException("Not unchecked", t);
        }
    }

    private static final class UCCallable<K, V> implements Callable<V> {

        private final ValueYielder<K, V> yielder;

        private final K input;

        public UCCallable(K input, ValueYielder<K, V> yielder) {
            super();
            this.input = input;
            this.yielder = yielder;
        }

        @Override
        public V call() throws Exception {
            return yielder.yieldValue(input);
        }
    }
}
