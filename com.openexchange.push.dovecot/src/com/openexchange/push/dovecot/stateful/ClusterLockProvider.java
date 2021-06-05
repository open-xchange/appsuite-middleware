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

package com.openexchange.push.dovecot.stateful;

import com.openexchange.push.dovecot.DovecotPushConfiguration;
import com.openexchange.push.dovecot.locking.DbDovecotPushClusterLock;
import com.openexchange.push.dovecot.locking.DovecotPushClusterLock;
import com.openexchange.push.dovecot.locking.DovecotPushClusterLock.Type;
import com.openexchange.push.dovecot.locking.HzDovecotPushClusterLock;
import com.openexchange.push.dovecot.locking.NoOpDovecotPushClusterLock;
import com.openexchange.server.ServiceLookup;

/**
 * {@link ClusterLockProvider}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.6.2
 */
public class ClusterLockProvider {

    /**
     * Initializes this configuration instance
     *
     * @param config The configuration that determines the mechanism to use
     * @param services The service lookup to initialize the implementation with
     */
    public static ClusterLockProvider newInstance(DovecotPushConfiguration config, ServiceLookup services) {
        String mech = config.getClusterLockMech();
        Type type = DovecotPushClusterLock.Type.parse(mech);
        if (type == null) {
            throw new IllegalArgumentException("Illegal cluster lock type: " + mech);
        }
        DovecotPushClusterLock clusterLock;
        switch (type) {
            case HAZELCAST:
                clusterLock = new HzDovecotPushClusterLock(services);
                break;
            case DATABASE:
                clusterLock = new DbDovecotPushClusterLock(services);
                break;
            case NONE:
                clusterLock = new NoOpDovecotPushClusterLock();
                break;
            default:
                throw new IllegalArgumentException("Illegal cluster lock type: " + mech);
        }

        return new ClusterLockProvider(clusterLock);
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private final DovecotPushClusterLock clusterLock;

    /**
     * Initializes a new {@link ClusterLockProvider}.
     * @param clusterLock
     */
    private ClusterLockProvider(DovecotPushClusterLock clusterLock) {
        super();
        this.clusterLock = clusterLock;
    }

    /**
     * Gets the {@link Type} of the lock that is returned by {@link #getClusterLock()}
     *
     * @return The type
     */
    public Type getLockType() {
        return clusterLock.getType();
    }

    /**
     * Gets the cluster lock
     *
     * @return The cluster lock
     */
    public DovecotPushClusterLock getClusterLock() {
        return clusterLock;
    }

}
