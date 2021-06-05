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

package com.openexchange.mail.compose.impl.security;

import java.security.Key;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.slf4j.Logger;
import com.hazelcast.core.HazelcastException;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceNotActiveException;
import com.hazelcast.map.IMap;
import com.openexchange.capabilities.CapabilitySet;
import com.openexchange.exception.OXException;
import com.openexchange.java.util.UUIDs;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;


/**
 * {@link HazelcastCompositionSpaceKeyStorage} - The (default) key storage backed by Hazelcast.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public class HazelcastCompositionSpaceKeyStorage extends AbstractCompositionSpaceKeyStorage {

    /** Simple class to delay initialization until needed */
    private static class LoggerHolder {
        static final Logger LOG = org.slf4j.LoggerFactory.getLogger(HazelcastCompositionSpaceKeyStorage.class);
    }

    private final ConcurrentMap<String, String> inMemoryMap;
    private final Lock lock;
    private HazelcastInstance hzInstance; // Guarded by lock
    private String mapName; // Guarded by lock

    /**
     * Initializes a new {@link HazelcastCompositionSpaceKeyStorage}.
     *
     * @param services The service look-up
     */
    public HazelcastCompositionSpaceKeyStorage(ServiceLookup services) {
        super(services);
        inMemoryMap = new ConcurrentHashMap<>(128);
        lock = new ReentrantLock();
    }

    @Override
    public boolean isApplicableFor(CapabilitySet capabilities, Session session) throws OXException {
        return true;
    }

    @Override
    public Key getKeyFor(UUID compositionSpaceId, boolean createIfAbsent, Session session) throws OXException {
        if (null == compositionSpaceId) {
            return null;
        }

        // Get the map reference
        Map<String, String> hzMap = getMap();

        String csid = UUIDs.getUnformattedString(compositionSpaceId);
        String obfuscatedBase64EncodedKey = hzMap.get(csid);
        if (null != obfuscatedBase64EncodedKey) {
            return base64EncodedString2Key(unobfuscate(obfuscatedBase64EncodedKey));
        }

        if (false == createIfAbsent) {
            return null;
        }

        Key newRandomKey = generateRandomKey();
        String newObfuscatedBase64EncodedKey = obfuscate(key2Base64EncodedString(newRandomKey));
        obfuscatedBase64EncodedKey = hzMap.putIfAbsent(csid, newObfuscatedBase64EncodedKey);
        return null == obfuscatedBase64EncodedKey ? newRandomKey : base64EncodedString2Key(unobfuscate(obfuscatedBase64EncodedKey));
    }

    @Override
    public List<UUID> deleteKeysFor(Collection<UUID> compositionSpaceIds, Session session) throws OXException {
        if (null == compositionSpaceIds || compositionSpaceIds.isEmpty()) {
            return Collections.emptyList();
        }

        // Get the map reference
        Map<String, String> hzMap = getMap();

        List<UUID> nonDeletedKeys = null;
        for (UUID compositionSpaceId : compositionSpaceIds) {
            String csid = UUIDs.getUnformattedString(compositionSpaceId);
            if (hzMap.remove(csid) == null) {
                // Not removed from Hazelcast map
                if (null == nonDeletedKeys) {
                    nonDeletedKeys = new ArrayList<UUID>(compositionSpaceIds.size());
                }
                nonDeletedKeys.add(compositionSpaceId);
            }
        }
        return null == nonDeletedKeys ? Collections.emptyList() : nonDeletedKeys;
    }

    private Map<String, String> getMap() throws OXException {
        lock.lock();
        try {
            HazelcastInstance hzInstance = this.hzInstance;
            if (null == hzInstance) {
                LoggerHolder.LOG.warn("Missing Hazelcast instance. Using non-distributed map instead");
                return inMemoryMap;
            }

            String mapName = this.mapName;
            if (null == mapName) {
                LoggerHolder.LOG.warn("Missing Hazelcast map name. Using non-distributed map instead");
                return inMemoryMap;
            }

            // Get the Hazelcast map reference
            Map<String, String> hzMap = map(mapName, hzInstance);
            if (null == hzMap) {
                LoggerHolder.LOG.warn("Missing Hazelcast map (Hazelcast inactive?). Using non-distributed map instead");
                return inMemoryMap;
            }

            return hzMap;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Sets the Hazelcast resources to use.
     *
     * @param hzInstance The Hazelcast instance
     * @param mapName The name of the associated Hazelcast map
     */
    public void setHazelcastResources(HazelcastInstance hzInstance, String mapName) {
        lock.lock();
        try {
            this.hzInstance = hzInstance;
            this.mapName = mapName;

            // Get the Hazelcast map reference
            Map<String, String> hzMap = map(mapName, hzInstance);
            if (null == hzMap) {
                LoggerHolder.LOG.warn("Missing Hazelcast map (Hazelcast inactive?).");
                return;
            }

            for (Map.Entry<String, String> csid2key : inMemoryMap.entrySet()) {
                hzMap.put(csid2key.getKey(), csid2key.getValue());
            }
            inMemoryMap.clear();
        } catch (Exception e) {
            LoggerHolder.LOG.error("Hazelcast map could not be initialized", e);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Unsets the previously set Hazelcast resources (if any).
     */
    public void unsetHazelcastResources(boolean forShutDown) {
        lock.lock();
        try {
            HazelcastInstance hzInstance = this.hzInstance;
            if (null == hzInstance) {
                return;
            }
            this.hzInstance = null;

            String mapName = this.mapName;
            if (null == mapName) {
                return;
            }
            this.mapName = null;

            if (false == forShutDown) {
                // Get the Hazelcast map reference
                IMap<String, String> hzMap = map(mapName, hzInstance);
                if (null == hzMap) {
                    LoggerHolder.LOG.warn("Missing Hazelcast map (Hazelcast inactive?).");
                    return;
                }

                for (String csid : hzMap.localKeySet()) {
                    String base64EncodedKey = hzMap.get(csid);
                    inMemoryMap.put(csid, base64EncodedKey);
                }
            }
        } catch (Exception e) {
            LoggerHolder.LOG.error("Hazelcast map could not be unset", e);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Gets the reference form Hazelcast Map with the associations:<br>
     * <div style="margin-left: 0.1in; margin-right: 0.5in; margin-bottom: 0.1in;">
     *   &lt;compositionSpaceId&gt; --&gt; &lt;base64EncodedAESKey&gt;
     * </div>
     *
     * @param mapName The name of the Hazelcast map
     * @param hzInstance The Hazlectas instance to use
     * @return The Hazelcast map
     * @throws OXException If Hazelcast map cannot be returned
     */
    IMap<String, String> map(String mapName, HazelcastInstance hzInstance) throws OXException {
        try {
            return hzInstance.getMap(mapName);
        } catch (HazelcastInstanceNotActiveException e) {
            // Obviously Hazelcast is absent
            LoggerHolder.LOG.debug("HazelcastInstance not active", e);
            return null;
        } catch (HazelcastException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

}
