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

package com.openexchange.push.imapidle.locking;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.UserAndContext;


/**
 * {@link LocalImapIdleClusterLock}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class LocalImapIdleClusterLock extends AbstractImapIdleClusterLock {

    private final ConcurrentMap<UserAndContext, String> locks;

    /**
     * Initializes a new {@link LocalImapIdleClusterLock}.
     */
    public LocalImapIdleClusterLock(boolean validateSessionExistence, ServiceLookup services) {
        super(validateSessionExistence, services);
        locks = new ConcurrentHashMap<>(2048, 0.9F, 1);
    }

    private UserAndContext generateKey(SessionInfo sessionInfo) {
        return UserAndContext.newInstance(sessionInfo.getUserId(), sessionInfo.getContextId());
    }

    @Override
    public Type getType() {
        return Type.LOCAL;
    }

    @Override
    public AcquisitionResult acquireLock(SessionInfo sessionInfo) throws OXException {
        UserAndContext key = generateKey(sessionInfo);

        long now = System.currentTimeMillis();
        String previous = locks.putIfAbsent(key, generateValue(now, sessionInfo));

        if (null == previous) {
            // Not present before
            return AcquisitionResult.ACQUIRED_NEW;
        }

        // Check if valid
        Validity validity = validateValue(previous, now, getValidationArgs(sessionInfo, null));
        if (Validity.VALID == validity) {
            // Locked
            return AcquisitionResult.NOT_ACQUIRED;
        }

        // Invalid entry - try to replace it mutually exclusive
        boolean replaced = locks.replace(key, previous, generateValue(now, sessionInfo));
        if (false == replaced) {
            return AcquisitionResult.NOT_ACQUIRED;
        }

        switch (validity) {
            case NO_SUCH_SESSION:
                return AcquisitionResult.ACQUIRED_NO_SUCH_SESSION;
            case TIMED_OUT:
                return AcquisitionResult.ACQUIRED_TIMED_OUT;
            default:
                return AcquisitionResult.ACQUIRED_NEW;
        }
    }

    @Override
    public void refreshLock(SessionInfo sessionInfo) throws OXException {
        locks.put(generateKey(sessionInfo), generateValue(System.currentTimeMillis(), sessionInfo));
    }

    @Override
    public void releaseLock(SessionInfo sessionInfo) throws OXException {
        locks.remove(generateKey(sessionInfo));
    }

}
