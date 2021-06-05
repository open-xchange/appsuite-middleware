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

/**
 * {@link QueueType} - The queue type.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public enum QueueType {

    /**
     * Synchronous queue type.
     */
    SYNCHRONOUS("synchronous", false, new IQueueProvider() {

        @Override
        public BlockingQueue<Runnable> newWorkQueue(final int fixedCapacity) {
            return QueueProvider.getInstance().newSynchronousQueue();
        }
    }),
    /**
     * Linked queue type.
     */
    LINKED("linked", true, new IQueueProvider() {

        @Override
        public BlockingQueue<Runnable> newWorkQueue(final int fixedCapacity) {
            return QueueProvider.getInstance().newLinkedQueue(fixedCapacity);
        }
    });

    private final String type;

    private final IQueueProvider queueProvider;

    private final boolean fixedSize;

    private QueueType(final String type, final boolean fixedSize, final IQueueProvider queueProvider) {
        this.fixedSize = fixedSize;
        this.type = type;
        this.queueProvider = queueProvider;
    }


    /**
     * Checks whether the queue type enforces the thread pool being at fixed-size.
     * <ul>
     * <li>A <b>synchronous</b> queue is appropriate for <code>core-size &lt; max-size</code></li>
     * <li>A <b>linked</b> queue is appropriate for <code>core-size = max-size</code></li>
     * </ul>
     *
     * @return <code>true</code> if the queue type enforces the thread pool being at fixed-size; otherwsie <code>false</code>
     */
    public boolean isFixedSize() {
        return fixedSize;
    }

    /**
     * Creates a new work queue of this type.
     *
     * @param fixedCapacity The fixed capacity
     * @return A new work queue of this type
     */
    public BlockingQueue<Runnable> newWorkQueue(final int fixedCapacity) {
        return queueProvider.newWorkQueue(fixedCapacity);
    }

    /**
     * Gets the queue type for given type string.
     *
     * @param type The type string
     * @return The queue type for given type string or <code>null</code>
     */
    public static QueueType getQueueType(final String type) {
        final QueueType[] queueTypes = QueueType.values();
        for (final QueueType queueType : queueTypes) {
            if (queueType.type.equalsIgnoreCase(type)) {
                return queueType;
            }
        }
        return null;
    }

    private static interface IQueueProvider {

        BlockingQueue<Runnable> newWorkQueue(int fixedCapacity);
    }

}
