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

package com.openexchange.admin.schemacache.inmemory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.schemacache.ContextCountPerSchemaClosure;
import com.openexchange.admin.schemacache.SchemaCache;
import com.openexchange.admin.schemacache.SchemaCacheFinalize;
import com.openexchange.admin.schemacache.SchemaCacheResult;

/**
 * {@link InMemorySchemaCache} - The in-memory schema cache implementation.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class InMemorySchemaCache implements SchemaCache {

    private static final class InMemoryFinalize implements SchemaCacheFinalize {

        private final int poolId;
        private final ConcurrentMap<Integer, SchemaInfo> cache;
        private final long modCount;
        private final String schemaName;

        InMemoryFinalize(String schemaName, long modCount, int poolId, ConcurrentMap<Integer, SchemaInfo> cache) {
            super();
            this.poolId = poolId;
            this.cache = cache;
            this.schemaName = schemaName;
            this.modCount = modCount;
        }

        @Override
        public void finalize(boolean contextCreated) {
            SchemaInfo schemaInfo = cache.get(Integer.valueOf(poolId));
            if (null != schemaInfo) {
                synchronized (schemaInfo) {
                    schemaInfo.releaseSchema(schemaName, false == contextCreated, modCount);
                }
            }
        }
    }

    // -------------------------------------------------------------------------------------------------------------------

    private final ConcurrentMap<Integer, SchemaInfo> cache;
    private final long timeout;

    /**
     * Initializes a new {@link InMemorySchemaCache}.
     */
    public InMemorySchemaCache(long timeout) {
        super();
        cache = new ConcurrentHashMap<Integer, SchemaInfo>(16, 0.9F, 1);
        this.timeout = timeout;
    }

    private SchemaInfo getSchemaInfo(int writePoolId) {
        Integer key = Integer.valueOf(writePoolId);
        SchemaInfo schemaInfo = cache.get(key);
        if (null == schemaInfo) {
            SchemaInfo newSchemaInfo = new SchemaInfo(writePoolId);
            schemaInfo = cache.putIfAbsent(key, newSchemaInfo);
            if (null == schemaInfo) {
                schemaInfo = newSchemaInfo;
            }
        }
        return schemaInfo;
    }

    @Override
    public SchemaCacheResult getNextSchemaFor(int poolId, int maxContexts, ContextCountPerSchemaClosure closure) throws StorageException {
        SchemaInfo schemaInfo = getSchemaInfo(poolId);
        synchronized (schemaInfo) {
            if (false == isAccessible(schemaInfo)) {
                schemaInfo.initializeWith(closure.getContextCountPerSchema(poolId, maxContexts));
            }

            try {
                long currentModCount = schemaInfo.getModCount();
                SchemaCount schemaCount = schemaInfo.getAndIncrementNextSchema(maxContexts, currentModCount);
                if (null == schemaCount) {
                    // No further schema available. Force re-initialization on next access attempt and return null.
                    schemaInfo.clear();
                    return null;
                }
                String schemaName = schemaCount.name;
                return new SchemaCacheResult(schemaName, new InMemoryFinalize(schemaName, schemaCount.modCount, poolId, cache));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new StorageException("Thread interrupted while getting next schema from cache", e);
            }
        }
    }

    private boolean isAccessible(SchemaInfo schemaInfo) {
        if (schemaInfo.isDeprecated()) {
            return false;
        }
        long timeout = this.timeout;
        return timeout <= 0 || ((System.currentTimeMillis() - schemaInfo.getStamp()) <= timeout);
    }

    @Override
    public void clearFor(int poolId) throws StorageException {
        SchemaInfo schemaInfo = cache.get(Integer.valueOf(poolId));
        if (null != schemaInfo) {
            if (schemaInfo.isDeprecated()) {
                // Already deprecated
                return;
            }
            synchronized (schemaInfo) {
                schemaInfo.clear();
            }
        }
    }

    @Override
    public String toString() {
        return cache.toString();
    }

}
