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

package com.openexchange.file.storage.mail.accesscontrol;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.config.cascade.ConfigViews;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.mail.osgi.Services;
import com.openexchange.session.Session;
import com.openexchange.session.UserAndContext;

/**
 * {@link AccessControl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class AccessControl implements AutoCloseable {

    private static final int DEFAULT_MAX_ACCESSES_PER_USER = 4;

    private static int getMaxAccessesPerUser(Session session) throws OXException {
        ConfigViewFactory factory = Services.getOptionalService(ConfigViewFactory.class);
        if (null == factory) {
            return DEFAULT_MAX_ACCESSES_PER_USER;
        }

        ConfigView view = factory.getView(session.getUserId(), session.getContextId());
        return ConfigViews.getDefinedIntPropertyFrom("com.openexchange.file.storage.mail.maxAccessesPerUser", DEFAULT_MAX_ACCESSES_PER_USER, view);
    }

    private static final ConcurrentMap<UserAndContext, AccessControl> CONTROLS = new ConcurrentHashMap<>(512);

    /**
     * Gets the associated access control for given session
     *
     * @param session The session
     * @return The access control
     * @throws OXException If access control cannot be returned
     */
    public static AccessControl getAccessControl(Session session) throws OXException {
        if (null == session) {
            return null;
        }

        UserAndContext key = UserAndContext.newInstance(session);
        Integer maxAccessesPerUser = null;

        AccessControl accessControl = null;
        while (null == accessControl) {
            accessControl = CONTROLS.get(key);
            if (null == accessControl) {
                if (null == maxAccessesPerUser) {
                    maxAccessesPerUser = Integer.valueOf(getMaxAccessesPerUser(session));
                }
                AccessControl newAccessControl = new AccessControl(maxAccessesPerUser.intValue(), key);
                accessControl = CONTROLS.putIfAbsent(key, newAccessControl);
                if (null == accessControl) {
                    // Current thread grabbed the slot
                    accessControl = newAccessControl;
                } else if (accessControl.isNotAlive()) {
                    // No more alive... Retry
                    accessControl = null;
                }
            } else if (accessControl.isNotAlive()) {
                // No more alive... Retry
                accessControl = null;
            }
        }
        // Leave...
        return accessControl;
    }

    // -------------------------------------------------------------------------------------------------------------

    private final Condition accessible;
    private final UserAndContext key;
    private final Lock lock;
    private final int maxAccess;
    private int inUse;
    private int grants;

    /**
     * Initializes a new {@link AccessControl}.
     */
    private AccessControl(int maxAccess, UserAndContext key) {
        super();
        this.maxAccess = maxAccess;
        this.key = key;
        lock = new ReentrantLock();
        accessible = lock.newCondition();
        inUse = 1; // Apparently... the creating thread
        grants = 0;
    }

    /**
     * Acquires a grant from this access control; waiting for an available grant if needed.
     *
     * @throws InterruptedException If interrupted while waiting for a grant
     */
    public void acquireGrant() throws InterruptedException {
        lock.lockInterruptibly();
        try {
            while (grants >= maxAccess) {
                accessible.await();
            }
            grants++;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Checks if this access control is not alive
     *
     * @return <code>true</code> if not alive; otherwise <code>false</code> (if still alive)
     */
    private boolean isNotAlive() {
        return !isAlive();
    }

    /**
     * Checks if this access control is still alive
     *
     * @return <code>true</code> if alive; otherwise <code>false</code>
     */
    private boolean isAlive() {
        lock.lock();
        try {
            if (inUse <= 0) {
                return false;
            }

            inUse++;
            return true;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Releases this access control.
     * <p>
     * <div style="margin-left: 0.1in; margin-right: 0.5in; background-color:#FFDDDD;">May only be invoked one time per thread!</div>
     * <p>
     *
     * @return <code>true</code> if released; otherwise <code>false</code>
     */
    public boolean release() {
        lock.lock();
        try {
            inUse--;
            grants--;

            if (inUse == 0) {
                // The last one to release
                CONTROLS.remove(key);
                return true;
            }

            accessible.signal();
            return false;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void close() {
        release();
    }

}
