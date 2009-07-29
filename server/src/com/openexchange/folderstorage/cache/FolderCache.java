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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.folderstorage.cache;

import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheException;
import com.openexchange.caching.CacheKey;
import com.openexchange.caching.CacheService;
import com.openexchange.folderstorage.FolderException;
import com.openexchange.server.ServiceException;

/**
 * {@link FolderCache} - TODO Short description of this class' purpose.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class FolderCache {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(FolderCache.class);

    private static final String FOLDER_CACHE_REGION_NAME = "FolderCache";

    private static final FolderCache instance = new FolderCache();

    /**
     * Gets the {@link FolderCache} instance.
     * 
     * @return The {@link FolderCache} instance.
     */
    public static FolderCache getInstance() {
        return instance;
    }

    private static CacheKey newCacheKey(final CacheService cacheService, final String folderId, final String treeId, final int cid) {
        return cacheService.newCacheKey(cid, treeId, folderId);
    }

    /*-
     * Member section
     */

    private Cache cache;

    /**
     * Initializes a new {@link FolderCache}.
     */
    private FolderCache() {
        super();
    }

    /**
     * Initializes this folder cache on available cache service.
     * 
     * @throws FolderException If initialization of this folder cache fails
     */
    void onCacheAvailable() throws FolderException {
        try {
            final CacheService cacheService = CacheServiceRegistry.getServiceRegistry().getService(CacheService.class, true);
            // TODO: cacheService.loadConfiguration();
            cache = cacheService.getCache(FOLDER_CACHE_REGION_NAME);
        } catch (final ServiceException e) {
            throw new FolderException(e);
        } catch (final CacheException e) {
            throw new FolderException(e);
        }
    }

    /**
     * Disposes this folder cache on absent cache service.
     * 
     * @throws FolderException If disposal of this folder cache fails
     */
    void onCacheAbsent() throws FolderException {
        if (cache == null) {
            return;
        }
        try {
            cache.clear();
            final CacheService cacheService = CacheServiceRegistry.getServiceRegistry().getService(CacheService.class);
            if (null != cacheService) {
                cacheService.freeCache(FOLDER_CACHE_REGION_NAME);
            }
        } catch (final CacheException e) {
            throw new FolderException(e);
        } finally {
            cache = null;
        }
    }

    
}
