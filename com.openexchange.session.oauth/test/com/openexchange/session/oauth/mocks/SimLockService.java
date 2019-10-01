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
 *    trademarks of the OX Software GmbH. group of companies.
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