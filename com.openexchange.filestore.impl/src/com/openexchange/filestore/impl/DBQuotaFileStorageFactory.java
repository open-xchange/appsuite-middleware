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
import java.util.concurrent.TimeUnit;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
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
import com.openexchange.java.Key;
import com.openexchange.user.UserService;

/**
 * {@link DBQuotaFileStorageFactory} - The database-backed {@link QuotaFileStorageService}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class DBQuotaFileStorageFactory implements QuotaFileStorageService {

    private static final Cache<Key, DBQuotaFileStorage> CACHE_STORAGES = CacheBuilder.newBuilder().maximumSize(1500).expireAfterWrite(30, TimeUnit.MINUTES).build();

    private final FileStorageService fileStorageService;

    /**
     * Initializes a new {@link DBQuotaFileStorageFactory}.
     *
     * @param fileStorageService The file storage service
     */
    public DBQuotaFileStorageFactory(FileStorageService fileStorageService) {
        super();
        this.fileStorageService = fileStorageService;
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

        Key key = new Key(userId, contextId);
        DBQuotaFileStorage storage = CACHE_STORAGES.getIfPresent(key);
        if (null == storage) {
            // Get the file storage info
            FileStorageInfo info = getFileStorageInfoFor(userId, contextId);

            // Determine file storage's base URI
            URI fileStorageUri = FilestoreStorage.getInstance().getFilestore(info.getFilestoreId()).getUri();
            try {
                // Generate full URI
                URI uri = new URI(fileStorageUri.getScheme(), fileStorageUri.getAuthority(), fileStorageUri.getPath() + '/' + info.getFilestoreName(), fileStorageUri.getQuery(), fileStorageUri.getFragment());

                // Create appropriate file storage instance
                storage = new DBQuotaFileStorage(contextId, info.getFileStorageQuota(), fileStorageService.getFileStorage(uri));

                // Put it into cache
                CACHE_STORAGES.put(key, storage);
            } catch (final URISyntaxException e) {
                throw FilestoreExceptionCodes.URI_CREATION_FAILED.create(e, fileStorageUri.toString() + '/' + info.getFilestoreName());
            }
        }

        return storage;
    }

    private FileStorageInfo getFileStorageInfoFor(int userId, int contextId) throws OXException {
        ContextService contextService = Services.requireService(ContextService.class);
        if (userId <= 0) {
            return contextService.getContext(contextId);
        }

        UserService userService = Services.requireService(UserService.class);
        Context context = contextService.getContext(contextId);
        User user = userService.getUser(userId, context);
        return user.getFilestoreId() > 0 ? user : context;
    }

}
