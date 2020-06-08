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

package com.openexchange.database.internal;

import com.openexchange.exception.OXException;
import com.openexchange.lock.AccessControl;
import java.util.concurrent.locks.Lock;

/**
 * {@link CacheLock}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public abstract class CacheLock {

    /**
     * Gets the cache lock backed by given lock instance
     *
     * @param lock The lock to use
     * @return The cache lock
     */
    public static CacheLock cacheLockFor(Lock lock) {
        return null == lock ? null : new CommonCacheLock(lock);
    }

    /**
     * Gets the cache lock backed by given access control instance
     *
     * @param accessControl The access control to use
     * @return The cache lock
     */
    public static CacheLock cacheLockFor(AccessControl accessControl) {
        return null == accessControl ? null : new AccessControlCacheLock(accessControl);
    }

    // ---------------------------------------------------------------

    /**
     * Initializes a new {@link CacheLock}.
     */
    protected CacheLock() {
        super();
    }

    /**
     * Acquires the lock.
     *
     * @throws OXException If interrupted while waiting for lock
     */
    public abstract void lock() throws OXException;

    /**
     * Releases the lock.
     */
    public abstract void unlock();

    // ---------------------------------------------------------------

    private static class CommonCacheLock extends CacheLock {

        private final Lock lock;

        CommonCacheLock(Lock lock) {
            super();
            this.lock = lock;
        }

        @Override
        public void lock() {
            lock.lock();
        }

        @Override
        public void unlock() {
            lock.unlock();
        }
    }

    private static class AccessControlCacheLock extends CacheLock {

        private final AccessControl accessControl;

        AccessControlCacheLock(AccessControl accessControl) {
            super();
            this.accessControl = accessControl;
        }

        @Override
        public void lock() throws OXException {
            try {
                accessControl.acquireGrant();
            } catch (InterruptedException e) {
                // Keep interrupted state
                Thread.currentThread().interrupt();
                throw OXException.general("Interrupted while acquiring grant", e);
            }
        }

        @Override
        public void unlock() {
            accessControl.release();
        }
    }
}
