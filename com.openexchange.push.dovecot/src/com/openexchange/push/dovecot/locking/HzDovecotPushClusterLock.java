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

package com.openexchange.push.dovecot.locking;

import java.util.concurrent.atomic.AtomicBoolean;
import com.hazelcast.core.HazelcastException;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceNotActiveException;
import com.hazelcast.map.IMap;
import com.openexchange.exception.OXException;
import com.openexchange.push.PushExceptionCodes;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;


/**
 * {@link HzDovecotPushClusterLock}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class HzDovecotPushClusterLock extends AbstractDovecotPushClusterLock {

    private volatile String mapName;
    private final AtomicBoolean notActive;

    /**
     * Initializes a new {@link HzDovecotPushClusterLock}.
     */
    public HzDovecotPushClusterLock(ServiceLookup services) {
        super(services);
        notActive = new AtomicBoolean();
    }

    /**
     * Sets the map name
     *
     * @param mapName The map name to set
     */
    public void setMapName(String mapName) {
        this.mapName = mapName;
    }

    private IMap<String, String> map(HazelcastInstance hzInstance) throws OXException {
        String mapName_tmp = this.mapName;
        if (null == mapName_tmp) {
            throw PushExceptionCodes.UNEXPECTED_ERROR.create("Missing map name");
        }
        try {
            return hzInstance.getMap(mapName_tmp);
        } catch (HazelcastInstanceNotActiveException e) {
            handleNotActiveException();
            // Obviously Hazelcast is absent
            return null;
        } catch (HazelcastException e) {
            throw PushExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw PushExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    private void handleNotActiveException() {
        notActive.set(true);
    }

    private String generateKey(SessionInfo sessionInfo) {
        return new StringBuilder(16).append(sessionInfo.getUserId()).append('@').append(sessionInfo.getContextId()).toString();
    }

    @Override
    public Type getType() {
        return Type.HAZELCAST;
    }

    @Override
    public boolean acquireLock(SessionInfo sessionInfo) throws OXException {
        if (notActive.get()) {
            return true;
        }
        HazelcastInstance hzInstance = services.getOptionalService(HazelcastInstance.class);
        if (null == hzInstance) {
            throw ServiceExceptionCode.absentService(HazelcastInstance.class);
        }

        String key = generateKey(sessionInfo);
        IMap<String, String> map = map(hzInstance);
        if (null == map) {
            // Hazelcast is absent
            return true;
        }

        long now = System.currentTimeMillis();
        String previous = map.putIfAbsent(key, generateValue(now, sessionInfo));

        if (null == previous) {
            // Not present before
            return true;
        }

        // Check if valid
        if (validValue(previous, now)) {
            // Locked
            return false;
        }

        // Invalid entry - try to replace it mutually exclusive
        return map.replace(key, previous, generateValue(now, sessionInfo));
    }

    @Override
    public void refreshLock(SessionInfo sessionInfo) throws OXException {
        if (notActive.get()) {
            return;
        }
        HazelcastInstance hzInstance = services.getOptionalService(HazelcastInstance.class);
        if (null == hzInstance) {
            throw ServiceExceptionCode.absentService(HazelcastInstance.class);
        }

        IMap<String, String> map = map(hzInstance);
        if (null == map) {
            // Hazelcast is absent
            return;
        }

        map.put(generateKey(sessionInfo), generateValue(System.currentTimeMillis(), sessionInfo));
    }

    @Override
    public void releaseLock(SessionInfo sessionInfo) throws OXException {
        if (notActive.get()) {
            return;
        }
        HazelcastInstance hzInstance = services.getOptionalService(HazelcastInstance.class);
        if (null == hzInstance) {
            throw ServiceExceptionCode.absentService(HazelcastInstance.class);
        }

        IMap<String, String> map = map(hzInstance);
        if (null == map) {
            // Hazelcast is absent
            return;
        }

        map.remove(generateKey(sessionInfo));
    }

}
