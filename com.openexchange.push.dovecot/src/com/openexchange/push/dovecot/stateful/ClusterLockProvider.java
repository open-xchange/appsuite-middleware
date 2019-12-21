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
     * Initializes this configuration instance
     *
     * @param config The configuration that determines the mechanism to use
     * @param services The service lookup to initialize the implementation with
     */
    public static ClusterLockProvider newInstance(DovecotPushConfiguration config, ServiceLookup services) {
        String mech = config.getClusterLockMech();
        Type type = DovecotPushClusterLock.Type.parse(mech);
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
