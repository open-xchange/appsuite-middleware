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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.caching.events.ms.internal;

import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicReference;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.openexchange.caching.CacheKey;
import com.openexchange.caching.CacheKeyService;
import com.openexchange.caching.events.CacheEvent;
import com.openexchange.caching.events.CacheOperation;
import com.openexchange.hazelcast.serialization.AbstractCustomPortable;

/**
 * {@link PortableCacheEvent}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class PortableCacheEvent extends AbstractCustomPortable {

    private static final AtomicReference<CacheKeyService> CKS_REFERENCE = new AtomicReference<CacheKeyService>();

    /**
     * Sets the specified {@link CacheKeyService}.
     *
     * @param service The {@link CacheKeyService}
     */
    public static void setCacheKeyService(CacheKeyService service) {
        CKS_REFERENCE.set(service);
    }

    /** The unique portable class ID of the {@link PortableCacheEvent} */
    public static final int CLASS_ID = 4;

    private Serializable key;
    private CacheOperation operation;
    private String groupName;
    private String region;

    /**
     * Initializes a new {@link PortableCacheEvent}.
     */
    public PortableCacheEvent() {
        super();
    }

    @Override
    public int getClassId() {
        return CLASS_ID;
    }

    @Override
    public void writePortable(PortableWriter writer) throws IOException {
        writer.writeUTF("r", region);
        writer.writeUTF("g", groupName);
        writer.writeUTF("o", operation.getId());
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
        region = reader.readUTF("r");
        groupName = reader.readUTF("g");
        String operationID = reader.readUTF("o");
        if (null != operationID) {
            operation = CacheOperation.cacheOperationFor(operationID);
        }
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

    public static PortableCacheEvent wrap(CacheEvent cacheEvent) {
        if (null == cacheEvent) {
            return null;
        }
        PortableCacheEvent portableEvent = new PortableCacheEvent();
        portableEvent.region = cacheEvent.getRegion();
        portableEvent.groupName = cacheEvent.getGroupName();
        portableEvent.operation = cacheEvent.getOperation();
        portableEvent.key = cacheEvent.getKey();
        return portableEvent;
    }

    public static CacheEvent unwrap(PortableCacheEvent portableEvent) {
        return new CacheEvent(portableEvent.operation, portableEvent.region, portableEvent.key, portableEvent.groupName);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((groupName == null) ? 0 : groupName.hashCode());
        result = prime * result + ((key == null) ? 0 : key.hashCode());
        result = prime * result + ((operation == null) ? 0 : operation.hashCode());
        result = prime * result + ((region == null) ? 0 : region.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof PortableCacheEvent)) {
            return false;
        }
        PortableCacheEvent other = (PortableCacheEvent) obj;
        if (groupName == null) {
            if (other.groupName != null) {
                return false;
            }
        } else if (!groupName.equals(other.groupName)) {
            return false;
        }
        if (key == null) {
            if (other.key != null) {
                return false;
            }
        } else if (!key.equals(other.key)) {
            return false;
        }
        if (operation != other.operation) {
            return false;
        }
        if (region == null) {
            if (other.region != null) {
                return false;
            }
        } else if (!region.equals(other.region)) {
            return false;
        }
        return true;
    }

}
