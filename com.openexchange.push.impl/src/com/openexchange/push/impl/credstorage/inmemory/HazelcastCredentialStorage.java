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

package com.openexchange.push.impl.credstorage.inmemory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.slf4j.Logger;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceNotActiveException;
import com.hazelcast.map.IMap;
import com.openexchange.concurrent.Blocker;
import com.openexchange.concurrent.ConcurrentBlocker;
import com.openexchange.exception.OXException;
import com.openexchange.push.PushUser;
import com.openexchange.push.credstorage.CredentialStorage;
import com.openexchange.push.credstorage.Credentials;
import com.openexchange.push.credstorage.DefaultCredentials;
import com.openexchange.push.impl.credstorage.Obfuscator;
import com.openexchange.push.impl.credstorage.inmemory.portable.PortableCredentials;
import com.openexchange.push.impl.portable.HazelcastInstanceNotActiveExceptionHandler;
import com.openexchange.push.impl.portable.PortablePushUser;
import com.openexchange.server.ServiceLookup;


/**
 * {@link HazelcastCredentialStorage}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.6.2
 */
public class HazelcastCredentialStorage implements CredentialStorage {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(HazelcastCredentialStorage.class);

    private final Blocker blocker = new ConcurrentBlocker();
    private final ConcurrentMap<PushUser, Credentials> sources;
    private final HazelcastInstanceNotActiveExceptionHandler notActiveExceptionHandler;
    private final ServiceLookup services;
    private final Obfuscator obfuscator;

    private volatile String hzMapName;
    private volatile boolean useHzMap = false;

    /**
     * Initializes a new {@link HazelcastCredentialStorage}.
     */
    public HazelcastCredentialStorage(Obfuscator obfuscator, HazelcastInstanceNotActiveExceptionHandler notActiveExceptionHandler, ServiceLookup services) {
        super();
        this.obfuscator = obfuscator;
        this.services = services;
        this.notActiveExceptionHandler = notActiveExceptionHandler;
        sources = new ConcurrentHashMap<PushUser, Credentials>(256);
    }

    private void handleNotActiveException(HazelcastInstanceNotActiveException e) {
        LOG.warn("Encountered a {} error.", HazelcastInstanceNotActiveException.class.getSimpleName());
        changeBackingMapToLocalMap();

        HazelcastInstanceNotActiveExceptionHandler notActiveExceptionHandler = this.notActiveExceptionHandler;
        if (null != notActiveExceptionHandler) {
            notActiveExceptionHandler.propagateNotActive(e);
        }
    }

    /**
     * Gets the Hazelcast map or <code>null</code> if unavailable.
     */
    private IMap<PortablePushUser, PortableCredentials> hzMap(String mapIdentifier) {
        if (null == mapIdentifier) {
            LOG.trace("Name of Hazelcast map is missing for token login service.");
            return null;
        }
        HazelcastInstance hazelcastInstance = services.getService(HazelcastInstance.class);
        if (hazelcastInstance == null) {
            LOG.trace("Hazelcast instance is not available.");
            return null;
        }
        try {
            return hazelcastInstance.getMap(mapIdentifier);
        } catch (HazelcastInstanceNotActiveException e) {
            handleNotActiveException(e);
            return null;
        }
    }

    /**
     * Sets the name for the Hazelcast map
     *
     * @param hzMapName The map name to set
     */
    public void setHzMapName(String hzMapName) {
        this.hzMapName = hzMapName;
    }

    // ---------------------------------------------------------------------------------------------------

    private void putCredentials(PushUser user, Credentials newObfuscatedCredentials) {
        blocker.acquire();
        try {
            if (useHzMap) {
                putCredentialsToHzMap(hzMapName, user, newObfuscatedCredentials);
            } else {
                sources.put(user, newObfuscatedCredentials);
            }
        } finally {
            blocker.release();
        }
    }

    private void putCredentialsToHzMap(String mapIdentifier, PushUser user, Credentials newObfuscatedCredentials) {
        IMap<PortablePushUser, PortableCredentials> hzMap = hzMap(mapIdentifier);
        if (null == hzMap) {
            LOG.trace("Hazelcast map for remote credentials is not available.");
        } else {
            hzMap.put(new PortablePushUser(user), new PortableCredentials(newObfuscatedCredentials));
        }
    }

    private Credentials peekCredentials(PushUser user) {
        blocker.acquire();
        try {
            return useHzMap ? peekCredentialsFromHzMap(hzMapName, user) : sources.get(user);
        } finally {
            blocker.release();
        }
    }

    private Credentials peekCredentialsFromHzMap(String mapIdentifier, PushUser user) {
        IMap<PortablePushUser, PortableCredentials> hzMap = hzMap(mapIdentifier);
        if (null == hzMap) {
            LOG.trace("Hazelcast map for remote credentials is not available.");
            return null;
        }

        PortableCredentials portableCredentials = hzMap.get(new PortablePushUser(user));
        if (null == portableCredentials) {
            return null;
        }

        DefaultCredentials.Builder credentials = DefaultCredentials.builder();
        credentials.withContextId(portableCredentials.getContextId());
        credentials.withUserId(portableCredentials.getUserId());
        credentials.withPassword(portableCredentials.getPassword());
        credentials.withLogin(portableCredentials.getLogin());
        return credentials.build();
    }

    private Credentials pollCredentials(PushUser user) {
        blocker.acquire();
        try {
            return useHzMap ? pollCredentialsFromHzMap(hzMapName, user) : sources.remove(user);
        } finally {
            blocker.release();
        }
    }

    private Credentials pollCredentialsFromHzMap(String mapIdentifier, PushUser user) {
        IMap<PortablePushUser, PortableCredentials> hzMap = hzMap(mapIdentifier);
        if (null == hzMap) {
            LOG.trace("Hazelcast map for remote credentials is not available.");
            return null;
        }

        PortableCredentials portableCredentials = hzMap.remove(new PortablePushUser(user));
        if (null == portableCredentials) {
            return null;
        }

        DefaultCredentials.Builder credentials = DefaultCredentials.builder();
        credentials.withContextId(portableCredentials.getContextId());
        credentials.withUserId(portableCredentials.getUserId());
        credentials.withPassword(portableCredentials.getPassword());
        credentials.withLogin(portableCredentials.getLogin());
        return credentials.build();
    }

    // ---------------------------------------------------------------------------------------------------

    @Override
    public Credentials getCredentials(int userId, int contextId) throws OXException {
        return obfuscator.unobfuscateCredentials(peekCredentials(new PushUser(userId, contextId)));
    }

    @Override
    public void storeCredentials(Credentials credentials) throws OXException {
        PushUser pushUser = new PushUser(credentials.getUserId(), credentials.getContextId());
        Credentials obfuscatedCredentials = obfuscator.obfuscateCredentials(credentials);

        Credentials curObfuscatedCredentials = peekCredentials(pushUser);
        if (null == curObfuscatedCredentials) {
            putCredentials(pushUser, obfuscatedCredentials);
        } else {
            if (curObfuscatedCredentials.getLogin().equals(obfuscatedCredentials.getLogin()) && curObfuscatedCredentials.getPassword().equals(obfuscatedCredentials.getPassword())) {
                return;
            }

            putCredentials(pushUser, obfuscatedCredentials);
        }
    }

    @Override
    public Credentials deleteCredentials(int userId, int contextId) throws OXException {
        return obfuscator.unobfuscateCredentials(pollCredentials(new PushUser(userId, contextId)));
    }

    // ---------------------------------------------------------------------------------------------------

   /**
    *
    */
    public void changeBackingMapToLocalMap() {
        blocker.block();
        try {
            // This happens if Hazelcast is removed in the meantime. We cannot copy any information back to the local map.
            useHzMap = false;
            LOG.info("Binary sources backing map changed to local");
        } finally {
            blocker.unblock();
        }
    }

   /**
    *
    */
    public void changeBackingMapToHz() {
        blocker.block();
        try {
            if (useHzMap) {
                return;
            }

            IMap<PortablePushUser, PortableCredentials> hzMap = hzMap(hzMapName);
            if (null == hzMap) {
                LOG.trace("Hazelcast map is not available.");
            } else {
                // This MUST be synchronous!
                for (Map.Entry<PushUser, Credentials> entry : sources.entrySet()) {
                    hzMap.put(new PortablePushUser(entry.getKey()), new PortableCredentials(entry.getValue()));
                }
                sources.clear();
            }
            useHzMap = true;
            LOG.info("Remote credentials backing map changed to hazelcast");
        } finally {
            blocker.unblock();
        }
    }

    // ---------------------------------------------------------------------------------------------------------

}
