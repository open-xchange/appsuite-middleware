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
import java.util.List;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.guest.GuestAssignment;
import com.openexchange.guest.internal.GuestStorageServiceLookup;

/**
 * This class defines the methods for accessing the storage of guests. {@link GuestStorage}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.8.0
 */
public abstract class GuestStorage {

    /**
     * Logger.
     */
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(GuestStorage.class);

    /**
     * Singleton implementation.
     */
    private static volatile GuestStorage impl;

    /**
     * Will be returned if a guest cannot be found through its mail address.
     */
    public static final int NOT_FOUND = -1;

    /**
     * Creates an instance implementing the context storage.
     *
     * @return an instance implementing the context storage.
     */
    public static GuestStorage getInstance() {
        GuestStorage tmp = impl;
        if (null == tmp) {
            synchronized (GuestStorage.class) {
                tmp = impl;
                if (null == tmp) {
                    try {
                        tmp = CachingGuestStorage.parent = new CachingGuestStorage(new RdbGuestStorage(GuestStorageServiceLookup.getService(DatabaseService.class)));
                        tmp.startUp();
                        impl = tmp;
                    } catch (final OXException e) {
                        // Cannot occur
                        LOG.warn("", e);
                    }
                }
            }
        }
        return tmp;
    }

    /**
     * Tries to create a completely new guest entry. Please make sure to create related assignment for the returned guest id.
     *
     * @see com.openexchange.guest.storage.GuestStorage.addAssignment(int, int, int)
     * @param mailAddress
     * @return the id of the guest
     * @throws OXException
     */
    public abstract int addGuest(String mailAddress) throws OXException;

    public abstract int addGuest(String mailAddress, Connection connection) throws OXException;

    /**
     * Adds a new assignment for an already existing guest.
     *
     * @param guestId
     * @param contextId
     * @param userId
     * @throws OXException
     */
    public abstract void addGuestAssignment(int guestId, int contextId, int userId) throws OXException;

    public abstract void addGuestAssignment(int guestId, int contextId, int userId, Connection connection) throws OXException;

    public abstract void removeGuest(int contextId, int userId) throws OXException;

    public abstract void removeGuestAssignment(int contextId, int userId) throws OXException;

    /**
     * Returns the {@link GuestAssignment}s the user with the given mail address is currently registered in different contexts
     *
     * @param mailAddress
     * @return
     * @throws OXException
     */
    public abstract List<Serializable> getGuestAssignments(final String mailAddress) throws OXException;

    public abstract boolean isGuestExisting(String mailAddress) throws OXException;

    /**
     *
     * @param contextId
     * @param userid
     * @return
     * @throws OXException
     */
    public abstract boolean isAssignmentExisting(String mailAddress, int contextId, int userId) throws OXException;

    public abstract int getNumberOfAssignments(String mailAddress) throws OXException;

    public abstract int getNumberOfAssignments(int contextId, int userId) throws OXException;

    public abstract int getGuestId(String mailAddress) throws OXException;

    public abstract int getGuestId(int contextId, int userId) throws OXException;

    /**
     * Internal start-up routine invoked in {@link #start()}
     *
     * @throws OXException If an error occurs
     */
    protected abstract void startUp() throws OXException;

    /**
     * Internal shut-down routine invoked in {@link #stop()}
     *
     * @throws OXException If an error occurs
     */
    protected abstract void shutDown() throws OXException;
}
