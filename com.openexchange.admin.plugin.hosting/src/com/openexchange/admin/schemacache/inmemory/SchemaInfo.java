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

package com.openexchange.admin.schemacache.inmemory;

import java.util.Map;
import java.util.PriorityQueue;

/**
 * {@link SchemaInfo}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class SchemaInfo {

    private static final class SchemaCount implements Comparable<SchemaCount> {

        final String name;
        int count;

        SchemaCount(String name, int count) {
            super();
            this.name = name;
            this.count = count;
        }

        @Override
        public int compareTo(SchemaCount o) {
            int thisCount = this.count;
            int otherCount = o.count;
            return thisCount < otherCount ? -1 : (thisCount == otherCount ? 0 : 1);
        }

        void incrementCount() {
            count++;
        }
    }

    // -----------------------------------------------------------------------------------------------------------------------------

    private final PriorityQueue<SchemaCount> queue;
    private final int poolId;
    private long stamp;
    private boolean deprecated;

    /**
     * Initializes a new {@link SchemaInfo}.
     */
    public SchemaInfo(int poolId) {
        super();
        this.poolId = poolId;
        queue = new PriorityQueue<SchemaCount>(32);
        deprecated = true; // Deprecated by default
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
    }

    /**
     * Applies given context count mapping.
     * <p>
     * Old state gets cleared.
     *
     * @param contextCountPerSchema The mapping for context count per schema
     */
    public void initializeWith(Map<String, Integer> contextCountPerSchema) {
        queue.clear();
        for (Map.Entry<String, Integer> entry : contextCountPerSchema.entrySet()) {
            queue.offer(new SchemaCount(entry.getKey(), entry.getValue().intValue()));
        }
        stamp = System.currentTimeMillis();
        deprecated = false;
    }

    /**
     * Gets (and increments used count) for next available schema
     *
     * @return The next schema or <code>null</code>
     */
    public String getAndIncrementNextSchema() {
        if (deprecated) {
            return null;
        }
        SchemaCount leastSchemaCount = queue.poll();
        if (null == leastSchemaCount) {
            return null;
        }

        leastSchemaCount.incrementCount();
        queue.offer(leastSchemaCount);
        return leastSchemaCount.name;
    }

}
