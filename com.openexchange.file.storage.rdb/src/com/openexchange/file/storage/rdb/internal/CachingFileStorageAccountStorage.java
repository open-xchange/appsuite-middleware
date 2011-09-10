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

package com.openexchange.file.storage.rdb.internal;

import gnu.trove.TIntArrayList;
import gnu.trove.TIntProcedure;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheKey;
import com.openexchange.caching.CacheService;
import com.openexchange.caching.dynamic.OXObjectFactory;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageService;
import com.openexchange.file.storage.rdb.services.FileStorageRdbServiceRegistry;
import com.openexchange.server.osgiservice.ServiceRegistry;
import com.openexchange.session.Session;

/**
 * {@link CachingFileStorageAccountStorage} - The messaging account manager backed by {@link CacheService}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.18.2
 */
public final class CachingFileStorageAccountStorage implements FileStorageAccountStorage {

    private static final CachingFileStorageAccountStorage INSTANCE = new CachingFileStorageAccountStorage();

    private static final String REGION_NAME = "FileStorageAccount";

    /**
     * Gets the cache region name.
     *
     * @return The cache region name
     */
    public static String getRegionName() {
        return REGION_NAME;
    }

    /**
     * Gets the cache-backed instance.
     *
     * @return The cache-backed instance
     */
    public static CachingFileStorageAccountStorage getInstance() {
        return INSTANCE;
    }

    /**
     * Generates a new cache key.
     *
     * @return The new cache key
     */
    private static CacheKey newCacheKey(final CacheService cacheService, final String serviceId, final int id, final int user, final int cid) {
        return cacheService.newCacheKey(cid, serviceId, Integer.valueOf(id), Integer.valueOf(user));
    }

    /*-
     * ------------------------------ Member section ------------------------------
     */

    /**
     * The database-backed delegatee.
     */
    private final RdbFileStorageAccountStorage delegatee;

    /**
     * Lock for the cache.
     */
    private final Lock cacheLock;

    /**
     * The service registry.
     */
    private final ServiceRegistry serviceRegistry;

    /**
     * Initializes a new {@link CachingFileStorageAccountStorage}.
     */
    private CachingFileStorageAccountStorage() {
        super();
        delegatee = RdbFileStorageAccountStorage.getInstance();
        cacheLock = new ReentrantLock(true);
        serviceRegistry = FileStorageRdbServiceRegistry.getServiceRegistry();
    }

    private void invalidateFileStorageAccount(final String serviceId, final int id, final int user, final int cid) throws OXException {
        final CacheService cacheService = serviceRegistry.getService(CacheService.class);
        if (null != cacheService) {
            final Cache cache = cacheService.getCache(REGION_NAME);
            cache.remove(newCacheKey(cacheService, serviceId, id, user, cid));
        }
    }

    @Override
    public int addAccount(final String serviceId, final FileStorageAccount account, final Session session) throws OXException {
        return delegatee.addAccount(serviceId, account, session);
    }

    @Override
    public void deleteAccount(final String serviceId, final FileStorageAccount account, final Session session) throws OXException {
        delegatee.deleteAccount(serviceId, account, session);
        invalidateFileStorageAccount(serviceId, Integer.parseInt(account.getId()), session.getUserId(), session.getContextId());
    }

    @Override
    public FileStorageAccount getAccount(final String serviceId, final int id, final Session session) throws OXException {
        final CacheService cacheService = serviceRegistry.getService(CacheService.class);
        if (cacheService == null) {
            return delegatee.getAccount(serviceId, id, session);
        }
        try {
            final RdbFileStorageAccountStorage accountStorage = delegatee;
            final Lock lock = cacheLock;
            final OXObjectFactory<FileStorageAccount> factory = new OXObjectFactory<FileStorageAccount>() {

                @Override
                public Serializable getKey() {
                    return newCacheKey(cacheService,
                        serviceId,
                        id,
                        session.getUserId(),
                        session.getContextId());
                }

                @Override
                public FileStorageAccount load() throws OXException {
                    return accountStorage.getAccount(serviceId, id, session);
                }

                @Override
                public Lock getCacheLock() {
                    return lock;
                }
                
            };
            return new FileStorageAccountReloader(factory, REGION_NAME);
        } catch (final OXException e) {
            throw e;
        }
    }

    @Override
    public List<FileStorageAccount> getAccounts(final String serviceId, final Session session) throws OXException {
        final TIntArrayList ids = delegatee.getAccountIDs(serviceId, session);
        if (ids.isEmpty()) {
            return Collections.emptyList();
        }
        final List<FileStorageAccount> accounts = new ArrayList<FileStorageAccount>(ids.size());
        class AdderProcedure implements TIntProcedure {

            OXException fsException;

            @Override
            public boolean execute(final int id) {
                try {
                    accounts.add(getAccount(serviceId, id, session));
                    return true;
                } catch (final OXException e) {
                    fsException = e;
                    return false;
                }
            }

        }
        final AdderProcedure ap = new AdderProcedure();
        if (!ids.forEach(ap) && null != ap.fsException) {
            throw ap.fsException;
        }
        return accounts;
    }

    @Override
    public void updateAccount(final String serviceId, final FileStorageAccount account, final Session session) throws OXException {
        delegatee.updateAccount(serviceId, account, session);
        invalidateFileStorageAccount(serviceId, Integer.parseInt(account.getId()), session.getUserId(), session.getContextId());
    }

    public boolean checkSecretCanDecryptStrings(final FileStorageService parentService, final Session session, final String secret) throws OXException {
        return delegatee.checkSecretCanDecryptStrings(parentService, session, secret);
    }

    public void migrateToNewSecret(final FileStorageService parentService, final String oldSecret, final String newSecret, final Session session) throws OXException {
        delegatee.migrateToNewSecret(parentService, oldSecret, newSecret, session);
    }

}
