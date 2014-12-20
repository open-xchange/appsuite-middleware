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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import com.openexchange.caching.events.CacheEvent;
import com.openexchange.caching.events.CacheEventService;
import com.openexchange.caching.events.CacheListener;
import com.openexchange.folderstorage.cache.memory.FolderMapManagement;


/**
 * {@link FolderMapInvalidator} - Invalidates folder map.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class FolderMapInvalidator implements CacheListener, ServiceTrackerCustomizer<CacheEventService, CacheEventService> {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(FolderMapInvalidator.class);

    private static final String REGION = FolderMapManagement.REGION;

    private static final String KEYP_FOLDER_ID = "folderId";
    private static final String KEYP_TREE_ID = "treeId";
    private static final String KEYP_USER_ID = "userId";
    private static final String KEYP_CONTEXT_ID = "contextId";

    /**
     * Gets the invalidation key for given arguments.
     *
     * @param folderId The folder identifier
     * @param treeId The tree identifier
     * @param optUser The optional user identifier
     * @param contextId The context identifier
     * @return The appropriate invalidation key
     */
    public static String keyFor(String folderId, String treeId, int optUser, int contextId) {
        JSONObject jKey = new JSONObject(6);
        if (null != folderId) {
            try { jKey.put(KEYP_FOLDER_ID, folderId); } catch (JSONException e) { /* Cannot occur */ }
        }
        if (null != treeId) {
            try { jKey.put(KEYP_TREE_ID, treeId); } catch (JSONException e) { /* Cannot occur */ }
        }
        try { jKey.put(KEYP_USER_ID, optUser); } catch (JSONException e) { /* Cannot occur */ }
        try { jKey.put(KEYP_CONTEXT_ID, contextId); } catch (JSONException e) { /* Cannot occur */ }
        return jKey.toString();
    }

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

            String region = cacheEvent.getRegion();
            if (REGION.equals(region)) {
                for (Serializable cacheKey : cacheEvent.getKeys()) {
                    handleCacheKey(cacheKey);
                }
            }
        }
    }

    /**
     * Handles specified cache key.
     *
     * @param cacheKey The cache key to handle
     */
    public static void handleCacheKey(Serializable cacheKey) {
        JSONObject jKey = optJsonObject(String.valueOf(cacheKey));
        if (null != jKey) {
            String folderId = jKey.optString(KEYP_FOLDER_ID);
            String treeId = jKey.optString(KEYP_TREE_ID);
            int optUser = jKey.optInt(KEYP_USER_ID, -1);
            int contextId = jKey.optInt(KEYP_CONTEXT_ID, -1);
            if (null == folderId && null == treeId && optUser <= 0) {
                FolderMapManagement.getInstance().dropFor(contextId, false);
            } else if (null == folderId && null == treeId && optUser > 0) {
                FolderMapManagement.getInstance().dropFor(optUser, contextId, false);
            } else {
                FolderMapManagement.getInstance().dropFor(folderId, treeId, optUser, contextId, null, false);
            }
        }
    }

    private static JSONObject optJsonObject(String key)  {
        try {
            return new JSONObject(key);
        } catch (JSONException e) {
            return null;
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
