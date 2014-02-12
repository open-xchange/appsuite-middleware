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

package com.openexchange.lock.impl;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheService;
import com.openexchange.exception.OXException;
import com.openexchange.lock.LockService;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.services.ServerServiceRegistry;


/**
 * {@link LockServiceImpl} - The default implementation based on {@link CacheService}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class LockServiceImpl implements LockService {

    private final String region;

    /**
     * Initializes a new {@link LockServiceImpl}.
     */
    public LockServiceImpl() {
        super();
        region = "GenLocks";
    }

    private Cache getCache() throws OXException {
        final CacheService cacheService = ServerServiceRegistry.getInstance().getService(CacheService.class);
        if (null == cacheService) {
            throw ServiceExceptionCode.absentService(CacheService.class);
        }
        return cacheService.getCache(region);
    }

    @Override
    public Lock getLockFor(final String identifier) throws OXException {
        final Cache cache = getCache();

        Object object = cache.get(identifier);
        if (object instanceof Lock) {
            return (Lock) object;
        }

        try {
            final ReentrantLock newLock = new ReentrantLock();
            cache.putSafe(identifier, newLock);
            return newLock;
        } catch (final OXException e) {
            // Already present
            object = cache.get(identifier);
            if (object instanceof Lock) {
                return (Lock) object;
            }
        }

        // Failed...
        throw OXException.general("Could not create lock for identifier: " + identifier);
    }

    @Override
    public void removeLockFor(final String identifier) {
        final CacheService cacheService = ServerServiceRegistry.getInstance().getService(CacheService.class);
        if (null != cacheService) {
            try {
                cacheService.getCache(region).remove(identifier);
            } catch (final Exception e) {
                // Ignore
            }
        }
    }

}
