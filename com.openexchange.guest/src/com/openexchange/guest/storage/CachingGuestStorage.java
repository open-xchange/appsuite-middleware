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

package com.openexchange.guest.storage;

import java.io.Serializable;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheService;
import com.openexchange.exception.OXException;
import com.openexchange.guest.internal.GuestStorageServiceLookup;

/**
 *
 * This class implements a caching for the guest storage.
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.8.0
 */
public class CachingGuestStorage extends GuestStorage {

    static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CachingGuestStorage.class);

    private static final String REGION_NAME = "Guest";

    public static volatile CachingGuestStorage parent;

    private final GuestStorage persistantImpl;

    private boolean started;

    /**
     * Initializes a new {@link CachingGuestStorage}.
     *
     * @param persistantImpl
     */
    public CachingGuestStorage(final GuestStorage persistantImpl) {
        super();
        this.persistantImpl = persistantImpl;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Serializable> getGuestAssignments(String mailAddress) throws OXException {
        final CacheService cacheService = GuestStorageServiceLookup.getService(CacheService.class);
        if (cacheService == null) {
            return persistantImpl.getGuestAssignments(mailAddress);
        }

        final Cache cache = cacheService.getCache(REGION_NAME);
        Object cachedObj = cache.get(mailAddress);
        List<Serializable> guestAssignments = new ArrayList<Serializable>();
        if (null == cachedObj) {
            LOG.trace("Cache MISS. Mail address: {}", mailAddress);
            guestAssignments = persistantImpl.getGuestAssignments(mailAddress);
            if (guestAssignments != null) {
                try {
                    cache.put(mailAddress, (ArrayList<Serializable>) guestAssignments, false);
                } catch (final OXException e) {
                    LOG.error("", e);
                }
            }
        } else {
            LOG.trace("Cache HIT for mail address: {}", mailAddress);
        }
        return guestAssignments;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void startUp() throws OXException {
        if (started) {
            LOG.error("Duplicate initialization of CachingGuestStorage.");
            return;
        }
        persistantImpl.startUp();
        started = true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void shutDown() throws OXException {
        if (!started) {
            LOG.error("Duplicate shutdown of CachingGuestStorage.");
            return;
        }
        final CacheService cacheService = GuestStorageServiceLookup.getService(CacheService.class);
        if (cacheService != null) {
            try {
                cacheService.freeCache(REGION_NAME);
            } catch (final OXException e) {
                LOG.error("", e);
            }
        }
        persistantImpl.shutDown();
        started = false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAssignmentExisting(String mailAddress, int contextId, int userId) throws OXException {
        final CacheService cacheService = GuestStorageServiceLookup.getService(CacheService.class);
        if (cacheService == null) {
            return persistantImpl.isAssignmentExisting(mailAddress, contextId, userId);
        }
        //TODO -handle appropriate if service is available
        return persistantImpl.isAssignmentExisting(mailAddress, contextId, userId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getGuestId(String mailAddress) throws OXException {
        final CacheService cacheService = GuestStorageServiceLookup.getService(CacheService.class);
        if (cacheService == null) {
            return persistantImpl.getGuestId(mailAddress);
        }
        //TODO -handle appropriate if service is available
        return persistantImpl.getGuestId(mailAddress);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int addGuest(String mailAddress) throws OXException {
        final CacheService cacheService = GuestStorageServiceLookup.getService(CacheService.class);
        if (cacheService == null) {
            return persistantImpl.addGuest(mailAddress);
        }
        //TODO -handle appropriate if service is available
        return persistantImpl.addGuest(mailAddress);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addGuestAssignment(int guestId, int contextId, int userId) throws OXException {
        final CacheService cacheService = GuestStorageServiceLookup.getService(CacheService.class);
        if (cacheService == null) {
            persistantImpl.addGuestAssignment(guestId, contextId, userId);
            return;
        }
        //TODO -handle appropriate if service is available
        persistantImpl.addGuestAssignment(guestId, contextId, userId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeGuestAssignment(int guestId, int contextId, int userId) throws OXException {
        final CacheService cacheService = GuestStorageServiceLookup.getService(CacheService.class);
        if (cacheService == null) {
            persistantImpl.removeGuestAssignment(guestId, contextId, userId);
            return;
        }
        //TODO -handle appropriate if service is available
        persistantImpl.removeGuestAssignment(guestId, contextId, userId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumberOfAssignments(int guestId) throws OXException {
        final CacheService cacheService = GuestStorageServiceLookup.getService(CacheService.class);
        if (cacheService == null) {
            return persistantImpl.getNumberOfAssignments(guestId);
        }
        //TODO -handle appropriate if service is available
        return persistantImpl.getNumberOfAssignments(guestId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getGuestId(int contextId, int userId) throws OXException {
        final CacheService cacheService = GuestStorageServiceLookup.getService(CacheService.class);
        if (cacheService == null) {
            return persistantImpl.getGuestId(contextId, userId);
        }
        //TODO -handle appropriate if service is available
        return persistantImpl.getGuestId(contextId, userId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int addGuest(String mailAddress, Connection connection) throws OXException {
        final CacheService cacheService = GuestStorageServiceLookup.getService(CacheService.class);
        if (cacheService == null) {
            return persistantImpl.addGuest(mailAddress, connection);
        }
        //TODO -handle appropriate if service is available
        return persistantImpl.addGuest(mailAddress, connection);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addGuestAssignment(int guestId, int contextId, int userId, Connection connection) throws OXException {
        final CacheService cacheService = GuestStorageServiceLookup.getService(CacheService.class);
        if (cacheService == null) {
            persistantImpl.addGuestAssignment(guestId, contextId, userId, connection);
            return;
        }
        //TODO -handle appropriate if service is available
        persistantImpl.addGuestAssignment(guestId, contextId, userId, connection);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeGuest(int guestId) throws OXException {
        final CacheService cacheService = GuestStorageServiceLookup.getService(CacheService.class);
        if (cacheService == null) {
            persistantImpl.removeGuest(guestId);
            return;
        }
        //TODO -handle appropriate if service is available
        persistantImpl.removeGuest(guestId);
    }
}
