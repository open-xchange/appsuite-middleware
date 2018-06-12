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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.ms.internal.portable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheService;
import com.openexchange.context.ContextService;
import com.openexchange.context.PoolAndSchema;
import com.openexchange.hazelcast.serialization.AbstractCustomPortable;
import com.openexchange.java.Strings;
import com.openexchange.ms.internal.Services;

/**
 * {@link PortableContextInvalidationCallable}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class PortableContextInvalidationCallable extends AbstractCustomPortable implements Callable<Boolean> {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(PortableContextInvalidationCallable.class);

    public static final int CLASS_ID = 26;

    public static final String PARAMETER_CONTEXT_IDS = "contextIds";

    private static final String CACHE_REGION_SCHEMA_STORE = "OXDBPoolCache";

    private int[] contextIds;

    /**
     * Initializes a new {@link PortableContextInvalidationCallable}.
     */
    public PortableContextInvalidationCallable() {
        super();
    }

    /**
     * Initializes a new {@link PortableContextInvalidationCallable}.
     *
     * @param contextIds The identifiers of the contexts, which shall be invalidated
     */
    public PortableContextInvalidationCallable(int[] contextIds) {
        super();
        this.contextIds = contextIds;
    }

    @Override
    public Boolean call() throws Exception {
        ContextService contextService = Services.optService(ContextService.class);
        if (null == contextService) {
            LOGGER.warn("Failed invalidating of the following contexts due to absence of context service:{}{}", Strings.getLineSeparator(), Arrays.toString(contextIds));
            return Boolean.FALSE;
        }

        CacheService cacheService = Services.optService(CacheService.class);
        if (null != cacheService) {
            try {
                Cache cache = cacheService.getCache(CACHE_REGION_SCHEMA_STORE);
                Map<PoolAndSchema, List<Integer>> schemaAssociations = contextService.getSchemaAssociationsFor(asList(contextIds));
                for (PoolAndSchema pas : schemaAssociations.keySet()) {
                    cache.remove(cache.newCacheKey(pas.getPoolId(), pas.getSchema()));
                    LOGGER.info("Successfully invalidated schema {} from pool {}", pas.getSchema(), Integer.valueOf(pas.getPoolId()));
                }
            } catch (Exception e) {
                LOGGER.error("Failed to invalidate schema store cache", e);
            }
        }

        contextService.invalidateContexts(contextIds);
        LOGGER.info("Successfully invalidated contexts:{}{}", Strings.getLineSeparator(), Arrays.toString(contextIds));
        return Boolean.TRUE;
    }

    private static List<Integer> asList(int[] contextIds) {
        List<Integer> l = new ArrayList<>(contextIds.length);
        for (int contextId : contextIds) {
            l.add(Integer.valueOf(contextId));
        }
        return l;
    }

    @Override
    public int getClassId() {
        return CLASS_ID;
    }

    @Override
    public void writePortable(PortableWriter writer) throws IOException {
        writer.writeIntArray(PARAMETER_CONTEXT_IDS, contextIds);
    }

    @Override
    public void readPortable(PortableReader reader) throws IOException {
        contextIds = reader.readIntArray(PARAMETER_CONTEXT_IDS);
    }

}
