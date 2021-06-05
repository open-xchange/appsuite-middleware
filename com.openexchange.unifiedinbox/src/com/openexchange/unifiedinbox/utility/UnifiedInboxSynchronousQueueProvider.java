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

package com.openexchange.unifiedinbox.utility;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;

/**
 * {@link UnifiedInboxSynchronousQueueProvider} - Provider for appropriate synchronous queue instance dependent on JRE version.
 * <p>
 * Java6 synchronous queue implementation is up to 3 times faster than Java5 one.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class UnifiedInboxSynchronousQueueProvider {

    /**
     * Initializes a new {@link UnifiedInboxSynchronousQueueProvider}.
     */
    protected UnifiedInboxSynchronousQueueProvider() {
        super();
    }

    private static volatile UnifiedInboxSynchronousQueueProvider instance;

    /**
     * Initializes appropriate instance of synchronous queue provider.
     *
     * @param useBuiltInQueue <code>true</code> to use built-in {@link SynchronousQueue}; otherwise <code>false</code> to use custom
     *            {@link Java6SynchronousQueue}
     */
    public static void initInstance(final boolean useBuiltInQueue) {
        if (useBuiltInQueue) {
            instance = new UnifiedInboxSynchronousQueueProvider() {

                @Override
                public <V> BlockingQueue<V> newSynchronousQueue() {
                    return new SynchronousQueue<V>();
                }
            };
        } else {
            instance = new UnifiedInboxSynchronousQueueProvider() {

                @Override
                public <V> BlockingQueue<V> newSynchronousQueue() {
                    return new Java6SynchronousQueue<V>();
                }
            };
        }
    }

    /**
     * Releases instance of synchronous queue provider.
     */
    public static void releaseInstance() {
        instance = null;
    }

    /**
     * Gets the {@link UnifiedInboxSynchronousQueueProvider} instance.
     *
     * @return The {@link UnifiedInboxSynchronousQueueProvider} instance
     */
    public static UnifiedInboxSynchronousQueueProvider getInstance() {
        return instance;
    }

    public abstract <V> BlockingQueue<V> newSynchronousQueue();
}
