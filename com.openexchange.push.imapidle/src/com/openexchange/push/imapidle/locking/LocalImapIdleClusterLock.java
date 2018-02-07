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

package com.openexchange.push.imapidle.locking;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;


/**
 * {@link LocalImapIdleClusterLock}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class LocalImapIdleClusterLock extends AbstractImapIdleClusterLock {

    private final ConcurrentMap<Key, String> locks;

    /**
     * Initializes a new {@link LocalImapIdleClusterLock}.
     */
    public LocalImapIdleClusterLock(ServiceLookup services) {
        super(services);
        locks = new ConcurrentHashMap<>(2048, 0.9F, 1);
    }

    private Key generateKey(SessionInfo sessionInfo) {
        return new Key(sessionInfo.getUserId(), sessionInfo.getContextId());
    }

    @Override
    public Type getType() {
        return Type.LOCAL;
    }

    @Override
    public boolean acquireLock(SessionInfo sessionInfo) throws OXException {
        Key key = generateKey(sessionInfo);

        long now = System.currentTimeMillis();
        String previous = locks.putIfAbsent(key, generateValue(now, sessionInfo));

        if (null == previous) {
            // Not present before
            return true;
        }

        // Check if valid
        if (validValue(previous, now, sessionInfo.isTransient(), null)) {
            // Locked
            return false;
        }

        // Invalid entry - try to replace it mutually exclusive
        return locks.replace(key, previous, generateValue(now, sessionInfo));
    }

    @Override
    public void refreshLock(SessionInfo sessionInfo) throws OXException {
        locks.put(generateKey(sessionInfo), generateValue(System.currentTimeMillis(), sessionInfo));
    }

    @Override
    public void releaseLock(SessionInfo sessionInfo) throws OXException {
        locks.remove(generateKey(sessionInfo));
    }

    // ------------------------------------------------------------------------------------------------

    private static final class Key {

        private final int userId;
        private final int contextId;
        private final int hash;

        Key(int userId, int contextId) {
            super();
            this.userId = userId;
            this.contextId = contextId;
            int prime = 31;
            int result = 1;
            result = prime * result + contextId;
            result = prime * result + userId;
            this.hash = result;
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            Key other = (Key) obj;
            if (contextId != other.contextId) {
                return false;
            }
            if (userId != other.userId) {
                return false;
            }
            return true;
        }
    }

}
