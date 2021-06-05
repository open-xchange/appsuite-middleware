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

package com.openexchange.caching.events;

import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

/**
 * {@link DefaultCondition} - The default implementation for a condition.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class DefaultCondition implements Condition {

    /** Main lock guarding all access */
    private final ReentrantLock lock;

    /** Condition for waiting */
    private final java.util.concurrent.locks.Condition notNull;

    /** The wrapped flag */
    private final AtomicReference<Boolean> flagReference;

    /**
     * Initializes a new {@link DefaultCondition}.
     */
    public DefaultCondition() {
        super();
        flagReference = new AtomicReference<Boolean>(null);
        lock = new ReentrantLock();
        notNull = lock.newCondition();
    }

    @Override
    public boolean shouldDeliver() throws InterruptedException {
        ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        try {
            Boolean flag;
            while ((flag = flagReference.get()) == null) {
                notNull.await();
            }
            return flag.booleanValue();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int peekShouldDeliver() {
        Boolean flag = flagReference.get();
        if (null == flag) {
            return -1;
        }
        return flag.booleanValue() ? 1 : 0;
    }

    /**
     * Sets the given value for this condition.
     *
     * @param notify The flag to set
     */
    public void set(boolean notify) {
        flagReference.set((notify ? Boolean.TRUE : Boolean.FALSE));

        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            notNull.signal();
        } finally {
            lock.unlock();
        }
    }

}
