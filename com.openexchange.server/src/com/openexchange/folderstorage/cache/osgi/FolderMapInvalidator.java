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

package com.openexchange.folderstorage.cache.osgi;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import com.openexchange.caching.CacheKey;
import com.openexchange.caching.events.CacheEvent;
import com.openexchange.caching.events.CacheEventService;
import com.openexchange.caching.events.CacheListener;
import com.openexchange.folderstorage.cache.memory.FolderMapManagement;
import com.openexchange.folderstorage.internal.Tools;

/**
 * {@link FolderMapInvalidator} - Invalidates folder map.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class FolderMapInvalidator implements CacheListener, ServiceTrackerCustomizer<CacheEventService, CacheEventService> {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(FolderMapInvalidator.class);

    private static final String REGION = FolderMapManagement.REGION;

    private final BundleContext context;

    /**
     * Initializes a new {@link FolderMapInvalidator}.
     *
     * @param context The bundle context
     */
    public FolderMapInvalidator(final BundleContext context) {
        super();
        this.context = context;
    }

    @Override
    public void onEvent(Object sender, CacheEvent cacheEvent, boolean fromRemote) {
        if (fromRemote) {
            // Remotely received
            LOGGER.debug("Handling incoming remote cache event: {}", cacheEvent);

            if (REGION.equals(cacheEvent.getRegion())) {
                int contextId = Tools.getUnsignedInteger(cacheEvent.getGroupName());
                if (contextId > 0) {
                    for (Serializable cacheKey : cacheEvent.getKeys()) {
                        handleCacheKey(cacheKey, contextId);
                    }
                }
            }
        }
    }

    /**
     * Handles specified cache key.
     *
     * @param cacheKey The cache key to handle
     * @param contextId The context identifier
     */
    public static void handleCacheKey(Serializable cacheKey, int contextId) {
        if (cacheKey instanceof CacheKey) {
            CacheKey key = (CacheKey) cacheKey;
            int optUser = key.getContextId();
            Serializable[] keys = key.getKeys();
            if (null == keys || 0 == keys.length) {
                /*
                 * context-/user-wide invalidation
                 */
                if (0 < optUser) {
                    FolderMapManagement.getInstance().dropFor(optUser, contextId, false);
                } else {
                    FolderMapManagement.getInstance().dropFor(contextId, false);
                }
            } else {
                /*
                 * explicit invalidation of one or more folders
                 */
                String treeId = String.valueOf(keys[0]);
                List<String> folderIds = new ArrayList<String>(keys.length - 1);
                for (int i = 1; i < keys.length; i++) {
                    folderIds.add(String.valueOf(keys[i]));
                }
                FolderMapManagement.getInstance().dropFor(folderIds, treeId, optUser, contextId, null, false);
            }
        }
    }

    @Override
    public CacheEventService addingService(ServiceReference<CacheEventService> reference) {
        CacheEventService service = context.getService(reference);
        service.addListener(REGION, this);
        return service;
    }

    @Override
    public void modifiedService(ServiceReference<CacheEventService> reference, CacheEventService service) {
        // Nothing to do
    }

    @Override
    public void removedService(ServiceReference<CacheEventService> reference, CacheEventService service) {
        service.removeListener(REGION, this);
        context.ungetService(reference);
    }

}
