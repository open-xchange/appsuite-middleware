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

import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import com.hazelcast.core.HazelcastException;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceNotActiveException;
import com.hazelcast.map.IMap;
import com.openexchange.exception.OXException;
import com.openexchange.push.PushExceptionCodes;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;


/**
 * {@link HzImapIdleClusterLock}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class HzImapIdleClusterLock extends AbstractImapIdleClusterLock {

    /** Simple class to delay initialization until needed */
    private static class LoggerHolder {
        static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(HzImapIdleClusterLock.class);
    }

    private volatile String mapName;
    private final AtomicBoolean notActive;

    /**
     * Initializes a new {@link HzImapIdleClusterLock}.
     */
    public HzImapIdleClusterLock(String mapName, boolean validateSessionExistence, ServiceLookup services) {
        super(validateSessionExistence, services);
        this.mapName = mapName;
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
        String mapName = this.mapName;
        if (null == mapName) {
            throw PushExceptionCodes.UNEXPECTED_ERROR.create("Missing map name");
        }
        try {
            return hzInstance.getMap(mapName);
        } catch (HazelcastInstanceNotActiveException e) {
            handleNotActiveException(e);
            // Obviously Hazelcast is absent
            return null;
        } catch (HazelcastException e) {
            throw PushExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw PushExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    private void handleNotActiveException(HazelcastInstanceNotActiveException e) {
        LoggerHolder.LOGGER.error("Hazelcast down. Hazelcast-based IMAP-IDLE cluster lock does therefore no more work.", e);
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
    public AcquisitionResult acquireLock(SessionInfo sessionInfo) throws OXException {
        if (notActive.get()) {
            return AcquisitionResult.ACQUIRED_NEW;
        }
        HazelcastInstance hzInstance = services.getOptionalService(HazelcastInstance.class);
        if (null == hzInstance) {
            throw ServiceExceptionCode.absentService(HazelcastInstance.class);
        }

        String key = generateKey(sessionInfo);
        IMap<String, String> map = map(hzInstance);
        if (null == map) {
            // Hazelcast is absent
            return AcquisitionResult.ACQUIRED_NEW;
        }

        long now = System.currentTimeMillis();
        String previous = map.putIfAbsent(key, generateValue(now, sessionInfo));

        if (null == previous) {
            // Not present before
            return AcquisitionResult.ACQUIRED_NEW;
        }

        // Check if valid
        Validity validity = validateValue(previous, now, getValidationArgs(sessionInfo, hzInstance));
        if (Validity.VALID == validity) {
            // Locked
            return AcquisitionResult.NOT_ACQUIRED;
        }

        // Invalid entry - try to replace it mutually exclusive
        boolean replaced = map.replace(key, previous, generateValue(now, sessionInfo));
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
