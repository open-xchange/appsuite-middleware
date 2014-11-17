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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.filestore.impl;

import java.net.URI;
import java.net.URISyntaxException;
import org.slf4j.Logger;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheService;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.FileStorageService;
import com.openexchange.filestore.QuotaFileStorage;
import com.openexchange.filestore.QuotaFileStorageExceptionCodes;
import com.openexchange.filestore.QuotaFileStorageService;
import com.openexchange.filestore.impl.osgi.Services;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.FileStorageInfo;
import com.openexchange.groupware.filestore.FilestoreExceptionCodes;
import com.openexchange.groupware.filestore.FilestoreStorage;
import com.openexchange.groupware.ldap.User;
import com.openexchange.java.IntReference;
import com.openexchange.user.UserService;

/**
 * {@link DBQuotaFileStorageService} - The database-backed {@link QuotaFileStorageService}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class DBQuotaFileStorageService implements QuotaFileStorageService {

    private final FileStorageService fileStorageService;
    private final String regionName = "QuotaFileStorages";

    /**
     * Initializes a new {@link DBQuotaFileStorageService}.
     *
     * @param fileStorageService The file storage service
     */
    public DBQuotaFileStorageService(FileStorageService fileStorageService) {
        super();
        this.fileStorageService = fileStorageService;
    }

    private Cache optCache() {
        try {
            CacheService optService = Services.optService(CacheService.class);
            return null == optService ? null : optService.getCache(regionName);
        } catch (Exception e) {
            Logger logger = org.slf4j.LoggerFactory.getLogger(DBQuotaFileStorageService.class);
            logger.warn("Could not return cache instance", e);
            return null;
        }
    }

    private DBQuotaFileStorage getCachedFileStorage(int userId, int contextId) {
        Cache cache = optCache();
        if (null == cache) {
            return null;
        }

        Object object = cache.getFromGroup(Integer.valueOf(userId), Integer.toString(contextId));
        return object instanceof DBQuotaFileStorage ? (DBQuotaFileStorage) object : null;
    }

    private void putCachedFileStorage(int userId, int contextId, DBQuotaFileStorage fileStorage) {
        Cache cache = optCache();
        if (null != cache) {
            try {
                cache.putInGroup(Integer.valueOf(userId), Integer.toString(contextId), fileStorage, false);
            } catch (Exception e) {
                Logger logger = org.slf4j.LoggerFactory.getLogger(DBQuotaFileStorageService.class);
                logger.warn("Could not put into cache", e);
            }
        }
    }

    @Override
    public void invalidateCacheFor(int contextId) {
        Cache cache = optCache();
        if (null != cache) {
            cache.invalidateGroup(Integer.toString(contextId));
        }
    }

    @Override
    public void invalidateCacheFor(int userId, int contextId) {
        Cache cache = optCache();
        if (null != cache) {
            cache.removeFromGroup(Integer.valueOf(userId), Integer.toString(contextId));
        }
    }

    @Override
    public QuotaFileStorage getQuotaFileStorage(int contextId) throws OXException {
        return getQuotaFileStorage(-1, contextId);
    }

    @Override
    public QuotaFileStorage getQuotaFileStorage(int userId, int contextId) throws OXException {
        FileStorageService fileStorageService = this.fileStorageService;
        if (fileStorageService == null) {
            throw QuotaFileStorageExceptionCodes.INSTANTIATIONERROR.create();
        }

        DBQuotaFileStorage storage = getCachedFileStorage(userId, contextId);
        if (null == storage) {
            // Get the file storage info
            IntReference fsOwner = new IntReference(0);
            FileStorageInfo info = getFileStorageInfoFor(userId, contextId, fsOwner);

            // Determine file storage's base URI
            URI baseUri = FilestoreStorage.getInstance().getFilestore(info.getFilestoreId()).getUri();
            try {
                // Generate full URI
                URI uri = new URI(baseUri.getScheme(), baseUri.getAuthority(), baseUri.getPath() + '/' + info.getFilestoreName(), baseUri.getQuery(), baseUri.getFragment());

                // Create appropriate file storage instance
                storage = new DBQuotaFileStorage(contextId, fsOwner.getValue(), info.getFileStorageQuota(), fileStorageService.getFileStorage(uri));

                // Put it into cache
                putCachedFileStorage(userId, contextId, storage);
            } catch (final URISyntaxException e) {
                throw FilestoreExceptionCodes.URI_CREATION_FAILED.create(e, baseUri.toString() + '/' + info.getFilestoreName());
            }
        }

        return storage;
    }

    private FileStorageInfo getFileStorageInfoFor(int userId, int contextId, IntReference fsOwner) throws OXException {
        ContextService contextService = Services.requireService(ContextService.class);
        if (userId <= 0) {
            return contextService.getContext(contextId);
        }

        UserService userService = Services.requireService(UserService.class);
        Context context = contextService.getContext(contextId);
        User user = userService.getUser(userId, context);
        if (user.getFilestoreId() <= 0) {
            // No user-specific file storage
            return context;
        }

        // A user-specific file storage; determine its owner
        int owner = user.getFileStorageOwner();
        fsOwner.setValue(owner > 0 ? owner : userId);
        return user;
    }

}
