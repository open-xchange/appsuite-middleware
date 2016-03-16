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
    public UniversalCache(final ValueYielder<K, V> yielder) {
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
    public V get(final K k) throws InterruptedException {
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
            } catch (final CancellationException e) {
                map.remove(k);
            } catch (final ExecutionException e) {
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

    private static RuntimeException launderThrowable(final Throwable t) {
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

        public UCCallable(final K input, final ValueYielder<K, V> yielder) {
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
