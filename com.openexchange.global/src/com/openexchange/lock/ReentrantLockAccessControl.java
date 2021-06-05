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

package com.openexchange.lock;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * {@link ReentrantLockAccessControl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class ReentrantLockAccessControl implements AccessControl {

    private final ReentrantLock lock;

    /**
     * Initializes a new {@link ReentrantLockAccessControl}.
     */
    public ReentrantLockAccessControl() {
        this(new ReentrantLock());
    }

    /**
     * Initializes a new {@link ReentrantLockAccessControl}.
     *
     * @param lock The reentrant lock to use
     * @throws IllegalArgumentException If specified lock is <code>null</code>
     */
    public ReentrantLockAccessControl(ReentrantLock lock) {
        super();
        if (null == lock) {
            throw new IllegalArgumentException("lock is null");
        }
        this.lock = lock;
    }

    @Override
    public void close() throws Exception {
        release();
    }

    @Override
    public void acquireGrant() throws InterruptedException {
        lock.lock();
    }

    @Override
    public boolean tryAcquireGrant() {
        return lock.tryLock();
    }

    @Override
    public boolean tryAcquireGrant(long timeout, TimeUnit unit) throws InterruptedException {
        return lock.tryLock(timeout, unit);
    }

    @Override
    public boolean release() {
        return release(true);
    }

    @Override
    public boolean release(boolean acquired) {
        if (acquired) {
            lock.unlock();
        }
        return true;
    }

}
