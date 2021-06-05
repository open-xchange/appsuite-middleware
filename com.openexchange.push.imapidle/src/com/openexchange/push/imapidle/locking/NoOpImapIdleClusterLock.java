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

import com.openexchange.exception.OXException;


/**
 * {@link NoOpImapIdleClusterLock}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class NoOpImapIdleClusterLock implements ImapIdleClusterLock {

    /**
     * Initializes a new {@link NoOpImapIdleClusterLock}.
     */
    public NoOpImapIdleClusterLock() {
        super();
    }

    @Override
    public Type getType() {
        return Type.NONE;
    }

    @Override
    public AcquisitionResult acquireLock(SessionInfo sessionInfo) throws OXException {
        return AcquisitionResult.ACQUIRED_NEW;
    }

    @Override
    public void refreshLock(SessionInfo sessionInfo) throws OXException {
        // Empty
    }

    @Override
    public void releaseLock(SessionInfo sessionInfo) throws OXException {
        // Empty
    }

}
