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

package com.openexchange.caching.events.ms.internal;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.ClassDefinition;
import com.hazelcast.nio.serialization.ClassDefinitionBuilder;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.openexchange.caching.CacheKey;
import com.openexchange.caching.CacheKeyService;
import com.openexchange.hazelcast.serialization.AbstractCustomPortable;

/**
 * {@link PortableCacheKey}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class PortableCacheKey extends AbstractCustomPortable {

    private static final AtomicReference<CacheKeyService> CKS_REFERENCE = new AtomicReference<CacheKeyService>();

    /**
     * Sets the specified {@link CacheKeyService}.
     *
     * @param service The {@link CacheKeyService}
     */
    public static void setCacheKeyService(CacheKeyService service) {
        CKS_REFERENCE.set(service);
    }

    /** The unique portable class ID of the {@link PortableCacheKey} */
    public static final int CLASS_ID = 17;

    /** The class definition */
    public static ClassDefinition CLASS_DEFINITION = new ClassDefinitionBuilder(FACTORY_ID, CLASS_ID)
        .addIntField("k")
        .addIntField("c")
        .addIntField("s")
    .build();

    /**
     * Wraps the supplied serializable cache key into a portable cache key.
     *
     * @param key The key to wrap
     * @return The portable key
     */
    public static PortableCacheKey wrap(Serializable key) {
        PortableCacheKey portableCacheKey = new PortableCacheKey();
        portableCacheKey.key = key;
        return portableCacheKey;
    }

    /**
     * Wraps the supplied serializable cache keys into an array of portable cache keys.
     *
     * @param keys The keys to wrap
     * @return The portable keys
     */
    public static PortableCacheKey[] wrap(List<Serializable> keys) {
        if (null == keys) {
            return null;
        }
        PortableCacheKey[] portableCacheKeys = new PortableCacheKey[keys.size()];
        for (int i = 0; i < portableCacheKeys.length; i++) {
            portableCacheKeys[i] = wrap(keys.get(i));
        }
        return portableCacheKeys;
    }

    /**
     * Unwraps the serializable key from the supplied portable cache key.
     *
     * @param cacheKey The portable cache key
     * @return The extracted serializable key
     */
    public static Serializable unwrap(PortableCacheKey cacheKey) {
        return cacheKey.key;
    }

    /**
     * Unwraps the serializable keys from the supplied portable cache key array.
     *
     * @param cacheKey The portable cache key array
     * @return The extracted serializable keys
     */
    public static List<Serializable> unwrap(PortableCacheKey[] cacheKeys) {
        if (null == cacheKeys) {
            return null;
        }
        List<Serializable> keys = new ArrayList<Serializable>(cacheKeys.length);
        for (PortableCacheKey portableCacheKey : cacheKeys) {
            keys.add(portableCacheKey.key);
        }
        return keys;
    }


    private Serializable key;

    /**
     * Initializes a new {@link PortableCacheKey}.
     */
    public PortableCacheKey() {
        super();
    }

    @Override
    public int getClassId() {
        return CLASS_ID;
    }

    @Override
    public void writePortable(PortableWriter writer) throws IOException {
        if (null == key) {
            writer.writeInt("k", 0);
            writer.writeInt("c", -1);
            writer.writeInt("s", -1);
        } else if (CacheKey.class.isInstance(key)) {
            writer.writeInt("k", 1);
            CacheKey cacheKey = (CacheKey)key;
            writer.writeInt("c", cacheKey.getContextId());
            String[] keys = cacheKey.getKeys();
            if (null == keys) {
                writer.writeInt("s", -1);
            } else {
                writer.writeInt("s", keys.length);
                ObjectDataOutput out = writer.getRawDataOutput();
                for (int i = 0; i < keys.length; i++) {
                    out.writeUTF(keys[i]);
                }
            }
        } else {
            writer.writeInt("k", 2);
            writer.writeInt("c", -1);
            writer.writeInt("s", -1);
            writer.getRawDataOutput().writeObject(key);
        }
    }

    @Override
    public void readPortable(PortableReader reader) throws IOException {
        int keyType = reader.readInt("k");
        if (1 == keyType) {
            int contextID = reader.readInt("c");
            String[] keys;
            int keySize = reader.readInt("s");
            if (0 <= keySize) {
                keys = new String[keySize];
                ObjectDataInput in = reader.getRawDataInput();
                for (int i = 0; i < keySize; i++) {
                    keys[i] = in.readUTF();
                }
            } else {
                keys = null;
            }
            key = CKS_REFERENCE.get().newCacheKey(contextID, keys);
        } else if (2 == keyType) {
            key = reader.getRawDataInput().readObject();
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((key == null) ? 0 : key.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PortableCacheKey other = (PortableCacheKey) obj;
        if (key == null) {
            if (other.key != null)
                return false;
        } else if (!key.equals(other.key))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return String.valueOf(key);
    }

}
