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

package com.openexchange.session.oauth.mocks;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import com.openexchange.exception.OXException;
import com.openexchange.lock.AccessControl;
import com.openexchange.lock.LockService;

/**
 * {@link SimSessionStorageService}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.10.3
 */
public class SimLockService implements LockService {

    private static final ConcurrentMap<String, SimLockService.SimAccessControl> ACCESS_CONTROLS = new ConcurrentHashMap<String, SimLockService.SimAccessControl>();

    @Override
    public Lock getLockFor(String identifier) throws OXException {
        return null;
    }

    @Override
    public Lock getSelfCleaningLockFor(String identifier) throws OXException {
        return null;
    }

    @Override
    public void removeLockFor(String identifier) {

    }

    @Override
    public AccessControl getAccessControlFor(String identifier, int permits, int userId, int contextId) throws OXException {
        String fqid = identifier + "-" + contextId + "-" + userId;
        SimLockService.SimAccessControl accessControl = ACCESS_CONTROLS.get(fqid);
        if (accessControl == null) {
            accessControl = new SimAccessControl(permits);
            SimLockService.SimAccessControl other = ACCESS_CONTROLS.putIfAbsent(fqid, accessControl);
            if (other != null) {
                accessControl = other;
            }
        }

        return accessControl;
    }

    static final class SimAccessControl implements AccessControl {

        private final Semaphore semaphore;

        SimAccessControl(int permits) {
            this.semaphore = new Semaphore(permits);
        }

        @Override
        public void close() throws Exception {
            release();
        }

        @Override
        public void acquireGrant() throws InterruptedException {
            semaphore.acquire();
        }

        @Override
        public boolean tryAcquireGrant() {
            return semaphore.tryAcquire();
        }

        @Override
        public boolean tryAcquireGrant(long timeout, TimeUnit unit) throws InterruptedException {
            return semaphore.tryAcquire(timeout, unit);
        }

        @Override
        public boolean release() {
            return release(true);
        }

        @Override
        public boolean release(boolean acquired) {
            if (acquired) {
                semaphore.release();
                return true;
            }
            return false;
        }
    }
}