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

package com.openexchange.threadpool.internal;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;

/**
 * {@link QueueProvider} - Provider for appropriate queue instance dependent on JRE version.
 * <p>
 * Java6 synchronous queue implementation is up to 3 times faster than Java5 one.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class QueueProvider {

    private static QueueProvider INSTANCE = new QueueProvider();

    /**
     * Gets the {@link QueueProvider} instance.
     *
     * @return The {@link QueueProvider} instance
     */
    public static QueueProvider getInstance() {
        return INSTANCE;
    }

    /**
     * Gets a newly created synchronous queue.
     *
     * @param <V> The queue's type
     * @return A newly created synchronous queue
     */
    public <V> BlockingQueue<V> newSynchronousQueue() {
        return new SynchronousQueue<V>();
    }

    /**
     * Gets a newly created synchronous queue.
     *
     * @param <V> The queue's type
     * @param clazz The queue's type class
     * @return A newly created synchronous queue
     */
    public <V extends Object> BlockingQueue<V> newSynchronousQueue(final Class<? extends V> clazz) {
        return new SynchronousQueue<V>();
    }

    /**
     * Gets a newly created linked queue.
     *
     * @param <V> The queue's type
     * @param fixedCapacity The fixed capacity
     * @return A newly created linked queue
     */
    public final <V> BlockingQueue<V> newLinkedQueue(final int fixedCapacity) {
        return fixedCapacity > 0 ? new LinkedBlockingQueue<V>(fixedCapacity) : new LinkedBlockingQueue<V>();
    }

    /**
     * Gets a newly created linked queue.
     *
     * @param <V> The queue's type
     * @param clazz The queue's type class
     * @param fixedCapacity The fixed capacity
     * @return A newly created linked queue
     */
    public final <V extends Object> BlockingQueue<V> newLinkedQueue(final Class<? extends V> clazz, final int fixedCapacity) {
        return fixedCapacity > 0 ? new LinkedBlockingQueue<V>(fixedCapacity) : new LinkedBlockingQueue<V>();
    }
}
