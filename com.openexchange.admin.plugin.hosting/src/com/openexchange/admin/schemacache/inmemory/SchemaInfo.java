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

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * {@link SchemaInfo} - Provides the cached information for a certain database/pool.
 * <p>
 * This implementation is <b>not</b> thread-safe.<br>
 * Accessing methods needs to be performed by acquiring the lock:
 * <pre>
 * SchemaInfo schemaInfo = ...;
 * synchronized (schemaInfo) {
 *     ...
 * }
 * </pre>
 * The only exception is the {@link #isDeprecated()} method, which is allowed to be called w/o holding instance lock.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class SchemaInfo {

    private final PriorityQueue<SchemaCount> queue;
    private final Map<String, SchemaCount> inUse;
    private final int poolId;
    private long stamp;
    private long modCount;
    private volatile boolean deprecated; // Declare as "volatile" for non-synchronized access

    /**
     * Initializes a new {@link SchemaInfo}.
     */
    public SchemaInfo(int poolId) {
        super();
        this.poolId = poolId;
        queue = new PriorityQueue<SchemaCount>(32);
        inUse = new HashMap<String, SchemaCount>(32, 0.9F);
        deprecated = true; // Deprecated by default
        modCount = 0L;
    }

    /**
     * Gets the current modification Count
     *
     * @return The current modification Count
     */
    public long getModCount() {
        return modCount;
    }

    /**
     * Gets the stamp
     *
     * @return The stamp
     */
    public long getStamp() {
        return stamp;
    }

    /**
     * Checks if this schema info is deprecated
     * <p>
     * Except all other methods this method is allowed to be called w/o holding the lock on this {@code SchemaInfo} instance.
     *
     * @return <code>true</code> if deprecated; otherwise <code>false</code>
     */
    public boolean isDeprecated() {
        return deprecated;
    }

    /**
     * Clears this schema info to force re-initialization.
     */
    public void clear() {
        deprecated = true;
        stamp = 0;
        queue.clear();
        inUse.clear();
    }

    /**
     * Applies given context count mapping.
     * <p>
     * Old state gets cleared.
     *
     * @param contextCountPerSchema The mapping for context count per schema
     * @param modCount The modification count
     */
    public void initializeWith(Map<String, Integer> contextCountPerSchema) {
        // Clear, ...
        queue.clear();
        inUse.clear();

        // Increase modification count and ...
        modCount++;

        // ... refill queue
        for (Map.Entry<String, Integer> entry : contextCountPerSchema.entrySet()) {
            queue.offer(new SchemaCount(entry.getKey(), entry.getValue().intValue(), modCount));
        }
        stamp = System.currentTimeMillis();
        deprecated = false;

        // Notify possibly waiting threads
        this.notifyAll();
    }

    /**
     * Gets (and increments used count) for next available schema
     *
     * @param maxContexts The configured max. number of contexts allowed per schema
     * @param modCount The modification count at the time when the schema should be obtained
     * @return The next schema or <code>null</code>
     * @throws InterruptedException If threads gets interrupted
     */
    public SchemaCount getAndIncrementNextSchema(int maxContexts, long modCount) throws InterruptedException {
        if (deprecated) {
            return null;
        }

        while (true) {
            if (this.modCount != modCount) {
                // Reinitialized in the meantime
                return null;
            }

            for (SchemaCount nextSchema; (nextSchema = queue.poll()) != null;) {
                if (nextSchema.count < maxContexts) {
                    // May be used for at least one more context
                    nextSchema.incrementCount();

                    // Put into in-use collection
                    inUse.put(nextSchema.name, nextSchema);

                    return nextSchema;
                }
            }

            // Found no available schema. Are there schemas currently in use?
            if (inUse.isEmpty()) {
                return null;
            }

            // Await until an in-use one becomes available
            this.wait();
        }
    }

    /**
     * Releases the used schema count
     *
     * @param schemaName The schema name
     * @param decrement <code>true</code> to decrement counter; otherwise <code>false</code> to leave as-is (incremented before)
     * @param modCount The modification count at the time when the schema count was obtained
     */
    public void releaseSchema(String schemaName, boolean decrement, long modCount) {
        try {
            if (this.modCount != modCount) {
                // Reinitialized in the meantime
                return;
            }

            SchemaCount usedSchemaCount = inUse.remove(schemaName);
            if (null != usedSchemaCount) {
                // Decrement counter and make it re-available
                if (decrement) {
                    usedSchemaCount.decrementCount();
                }

                // Re-offer
                queue.offer(usedSchemaCount);
            }
        } finally {
            // Notify possibly waiting threads
            this.notifyAll();
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(128).append("SchemaInfo [");
        builder.append("queue=").append(queue).append(", poolId=").append(poolId).append(", stamp=").append(stamp);
        builder.append(", deprecated=").append(deprecated).append(", modCount=").append(modCount).append("]");
        return builder.toString();
    }
}
