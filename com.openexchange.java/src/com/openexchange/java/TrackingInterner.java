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

package com.openexchange.java;

import java.util.concurrent.atomic.AtomicInteger;
import com.google.common.collect.Interner;
import com.google.common.collect.Interners;

/**
 * {@link TrackingInterner} - Wraps a Google Common's {@link Interner} instance by tracking of cache misses and hits.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.12.0
 */
public class TrackingInterner<E> implements Interner<E> {

    /**
     * Returns a new thread-safe {@code Interner} instance which retains a strong reference to each instance it has interned, thus
     * preventing these instances from being garbage-collected. If this retention is acceptable, this implementation may perform better than
     * an {@code Interner} instance retaining weak references.
     *
     * @param <E> The interned type
     * @return The {@code Interner} instance retaining a strong references
     */
    public static <E> TrackingInterner<E> newTrackingStrongInterner() {
        return new TrackingInterner<E>(Interners.newStrongInterner());
    }

    /**
     * Returns a new thread-safe {@code Interner} instance which retains a weak reference to each instance it has interned, and so does not
     * prevent these instances from being garbage-collected. This most likely does not perform as well as {@link #newStrongInterner}, but is
     * the best alternative when the memory usage of that implementation is unacceptable.
     *
     * @param <E> The interned type
     * @return The {@code Interner} instance retaining a weak references
     */
    public static <E> TrackingInterner<E> newTrackingWeakInterner() {
        return new TrackingInterner<E>(Interners.newWeakInterner());
    }

    /**
     * Creates a new tracking {@code Interner} instance for given {@code Interner} instance.
     *
     * @param <E> The interned type
     * @param interner The {@code Interner} instance to track cache misses and hits for
     * @return The tracking interner or <code>null</code> (if passed instance is <code>null</code>)
     */
    public static <E> TrackingInterner<E> trackingInternerFor(Interner<E> interner) {
        if (interner == null) {
            return null;
        }

        return (interner instanceof TrackingInterner) ? (TrackingInterner<E>) interner : new TrackingInterner<E>(interner);
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private final Interner<E> interner;
    private final AtomicInteger cacheHits;
    private final AtomicInteger cacheMisses;

    /**
     * Initializes a new {@link TrackingInterner}.
     *
     * @param interner The {@code Interner} instance to track cache misses and hits for
     */
    private TrackingInterner(Interner<E> interner) {
        super();
        this.interner = interner;
        cacheHits = new AtomicInteger(0);
        cacheMisses = new AtomicInteger(0);
    }

    @Override
    public E intern(E sample) {
        if (sample == null) {
            return null;
        }

        E interned = interner.intern(sample);
        if (System.identityHashCode(interned) == System.identityHashCode(sample)) {
            // Was not interned before. Same sample object returned.
            cacheMisses.incrementAndGet();
        } else {
            // Already interned. Another (cached) object returned.
            cacheHits.incrementAndGet();
        }
        return interned;
    }

    /**
     * Gets the number of cache hits.
     *
     * @return The number of cache hits
     */
    public int getCacheHits() {
        return cacheHits.get() & 0x7fffffff;
    }

    /**
     * Gets the number of cache misses.
     *
     * @return The number of cache misses
     */
    public int getCacheMisses() {
        return cacheMisses.get() & 0x7fffffff;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(32);
        builder.append("[cacheHits=").append(cacheHits.get() & 0x7fffffff);
        builder.append(", cacheMisses=").append(cacheMisses.get() & 0x7fffffff).append(']');
        return builder.toString();
    }

}
