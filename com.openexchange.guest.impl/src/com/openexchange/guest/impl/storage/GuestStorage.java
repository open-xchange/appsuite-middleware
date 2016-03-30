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

package com.openexchange.guest.impl.storage;

import java.sql.Connection;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.guest.GuestAssignment;
import com.openexchange.guest.impl.internal.RdbGuestStorage;

/**
 * This class defines the methods for accessing the storage of guests.
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
    public static final long NOT_FOUND = -1;

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
                        tmp = new RdbGuestStorage();
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
     * @param mailAddress - mail address of the guest to add
     * @param groupId - the group the context is assigned to
     * @param Connection - existing connection that should be used to add the guest.
     * @return the internal id of the guest
     * @throws OXException
     */
    public abstract long addGuest(String mailAddress, String groupId, Connection connection) throws OXException;

    /**
     * Adds a new assignment for an already existing guest.
     *
     * @param assignment - the assignment to add
     * @param Connection - existing connection that should be used to create the guest assignment.
     * @throws OXException
     */
    public abstract void addGuestAssignment(GuestAssignment assignment, Connection connection) throws OXException;

    /**
     * Removes the guest with the given internal id
     *
     * @param guestId - the internal id of the guest (e. g. retrieved via com.openexchange.guest.impl.impl.storage.GuestStorage.getGuestId(String))
     * @param Connection - existing connection that should be used to remove the guest.
     * @throws OXException
     */
    public abstract void removeGuest(long guestId, Connection connection) throws OXException;

    /**
     * Removes the guests with the given groupId
     *
     * @param groupId - the group the guests should be removed for
     * @param Connection - existing connection that should be used to remove the guest.
     * @throws OXException
     */
    public abstract void removeGuests(String groupId, Connection connection) throws OXException;

    /**
     * Removes the guests assignments for the given context
     *
     * @param contextId - id of the context to get the guest ids for.
     * @param Connection - existing connection that should be used to remove the guests.
     * @return long - number of assignments that have been removed
     * @throws OXException
     */
    public abstract long removeGuestAssignments(int contextId, Connection connection) throws OXException;

    /**
     * Returns the internal guest ids that currently have assignments for the given context.
     *
     * @param contextId - id of the context to get the guest ids for.
     * @param Connection - existing connection that should be used to remove the guests.
     * @return List with Long containing internal guest ids to be able to check if there still are assignments existing or if the guest should be removed.
     * @throws OXException
     */
    public abstract List<Long> resolveGuestAssignments(int contextId, Connection connection) throws OXException;

    /**
     * Returns the internal guest ids that currently have assignments for the given groupId.
     *
     * @param guestIds - List with the ids of the guest to remove assignments for
     * @param Connection - existing connection that should be used to remove the guests.
     * @return List with Long containing internal guest ids to be able to check if there still are assignments existing or if the guest should be removed.
     * @throws OXException
     *
     * TODO remove this and all caller methods (as just designed to delete based on group) if the CLT removes all data
     */
    public abstract long removeGuestAssignments(List<Long> guestIds, Connection connection) throws OXException;

    /**
     * Removes the assignment of the guest based on the given internal guestId, context and user id
     *
     * @param guestId - the internal id of the guest to remove an assignment for (e. g. retrieved via com.openexchange.guest.impl.impl.storage.GuestStorage.getGuestId(String))
     * @param contextId - the context the guest is assigned to
     * @param userId - the id of the guest within the provided context
     * @param Connection - existing connection that should be used to remove the guest assignment.
     * @throws OXException
     */
    public abstract void removeGuestAssignment(long guestId, int contextId, int userId, Connection connection) throws OXException;

    /**
     * Returns the {@link GuestAssignment}s the guest (with the given mail address) is currently registered for.
     *
     * @param guestId - internal guest id of the user
     * @param Connection - existing connection that should be used to get the guest assignments.
     * @return List with {@link GuestAssignment}s
     * @throws OXException
     */
    public abstract List<GuestAssignment> getGuestAssignments(final long guestId, Connection connection) throws OXException;

    /**
     * Returns the assignment of the guest based on the given internal guestId, context and user id
     *
     * @param guestId - the internal id of the guest to remove an assignment for (e. g. retrieved via com.openexchange.guest.impl.impl.storage.GuestStorage.getGuestId(String))
     * @param contextId - the context the guest is assigned to
     * @param userId - the id of the guest within the provided context
     * @param Connection - existing connection that should be used to remove the guest assignment.
     * @throws OXException
     */
    public abstract GuestAssignment getGuestAssignment(long guestId, int contextId, int userId, Connection connection) throws OXException;

    /**
     * Checks if exactly this mapping (user with mail address to context and user) is already existing
     *
     * @param guestId - internal guest id of the user
     * @param contextId - the context to check for
     * @param userId - the id of the guest user to check for
     * @param Connection - existing connection that should be used to evaluate if assignment is existing.
     * @return <code>true</code> if existing, otherwise <code>false</code>
     * @throws OXException
     */
    public abstract boolean isAssignmentExisting(long guestId, int contextId, int userId, Connection connection) throws OXException;

    /**
     * Returns the number of currently available context/user assignments to the given internal guest id.
     *
     * @param guestId - internal guest id of the user
     * @param Connection - existing connection that should be used to number of guest assignments.
     * @return int with the number of assignments
     * @throws OXException
     */
    public abstract long getNumberOfAssignments(long guestId, Connection connection) throws OXException;

    /**
     * Returns the internally used guest id associated to the given context id/user id tuple or -1 if the guest does not exist.
     *
     * @param mailAddress - mail address of the guest to get its internal id for
     * @param groupId - the group the context is assigned to
     * @param Connection - existing connection that should be used to get the guest id.
     * @return int with the internal guest id or -1 if the guest does currently not exist
     * @throws OXException
     */
    public abstract long getGuestId(String mailAddress, String groupId, Connection connection) throws OXException;

    /**
     * Returns the internal guest ids that are assigned to the given group id
     *
     * @param groupId - id of the context group to return guest ids for
     * @param connection - the {@link Connection} to get ids with
     * @return {@link List} of {@link Long} with the ids that are assigned to the given group
     * @throws OXException
     */
    public abstract List<Long> getGuestIds(String groupId, Connection connection) throws OXException;

    /**
     * Updates password and password mechanism for the given assignment
     *
     * @param assignment - {@link GuestAssignment} with the user to update and the new values for password/passwordMech
     * @param connection - the {@link Connection} to update with
     * @throws OXException
     */
    public abstract void updateGuestAssignment(GuestAssignment assignment, Connection connection) throws OXException;

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
