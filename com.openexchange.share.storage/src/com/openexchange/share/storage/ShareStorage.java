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

package com.openexchange.share.storage;

import java.util.Date;
import java.util.List;
import java.util.Set;
import com.openexchange.exception.OXException;
import com.openexchange.share.Share;
import com.openexchange.share.ShareTarget;


/**
 * {@link ShareStorage}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.0
 */
public interface ShareStorage {

    /**
     * Loads all shares stored for a specific guest user.
     *
     * @param contextID The context ID
     * @param guest The identifier of the guest user to load the shares for
     * @param parameters The storage parameters
     * @return The shares, or an empty list if none were found
     */
    List<Share> loadSharesForGuest(int contextID, int guest, StorageParameters parameters) throws OXException;

    /**
     * Loads all shares that were created in a specific context.
     *
     * @param contextID The context ID
     * @param parameters The storage parameters
     * @return The shares
     */
    List<Share> loadSharesForContext(int contextID, StorageParameters parameters) throws OXException;

    /**
     * Loads all shares that are expired after the given date.
     *
     * @param contextID The context ID
     * @param expires The date to compare the share's expires value with
     * @param parameters The storage parameters
     * @return The shares
     */
    List<Share> loadSharesExpiredAfter(int contextID, Date expires, StorageParameters parameters) throws OXException;

    /**
     * Deletes all shares that are expired after the given date.
     *
     * @param contextID The context ID
     * @param expires The date to compare the share's expires value with
     * @param parameters The storage parameters
     * @return The number of affected entries
     */
    int deleteSharesExpiredAfter(int contextID, Date expires, StorageParameters parameters) throws OXException;

    /**
     * Gets a value indicating whether there are any stored shares for a specific guest user or not.
     *
     * @param contextID The context ID
     * @param guest The identifier of the guest user to check the shares for
     * @param parameters The storage parameters
     * @return <code>true</code> if there is at least one share stored, <code>false</code>, otherwise
     */
    boolean hasShares(int contextID, int guest, StorageParameters parameters) throws OXException;

    /**
     * Gets a set of all module identifiers a specific guest user has share targets for.
     *
     * @param contextID The context ID
     * @param guest The identifier of the guest user to get the shared modules for
     * @param parameters The storage parameters
     * @return The shared modules, or an empty set if there are none
     * @throws OXException
     */
    Set<Integer> getSharedModules(int contextID, int guest, StorageParameters parameters) throws OXException;

    /**
     * Gets the identifiers of all users that have at least created one share for a specific guest.
     *
     * @param contextID The context ID
     * @param guest The identifier of the guest user to get the sharing users for
     * @param parameters The storage parameters
     * @return The identifiers of the sharing users, or an empty set if there are none
     * @throws OXException
     */
    Set<Integer> getSharingUsers(int contextID, int guest, StorageParameters parameters) throws OXException;

    /**
     * Saves multiple shares in the storage. Existing shares for a guest pointing to the same target are updated implicitly.
     *
     * @param contextID The context ID
     * @param shares The shares to insert or update
     * @param parameters The storage parameters
     * @throws OXException
     */
    void storeShares(int contextID, List<Share> shares, StorageParameters parameters) throws OXException;

    /**
     * Updates multiple shares in the storage.
     *
     * @param contextID The context ID
     * @param shares The shares to update
     * @param clientLastModified The time the shares were last read from the client to catch concurrent modifications
     * @param parameters The storage parameters
     * @throws OXException
     */
    void updateShares(int contextID, List<Share> shares, Date clientLastModified, StorageParameters parameters) throws OXException;

    /**
     * Deletes multiple shares from the storage, based on guest- and target-information present in the supplied shares.
     *
     * @param contextID The context ID
     * @param shares The shares to delete
     * @param parameters The storage parameters
     * @return The number of affected entries
     * @throws OXException
     */
    int deleteShares(int contextID, List<Share> shares, StorageParameters parameters) throws OXException;

    /**
     * Deletes multiple shares targets associated to any guest user from the storage.
     *
     * @param contextID The context ID
     * @param shares The share targets to delete
     * @param parameters The storage parameters
     * @return The identifiers of the affected guest users, or an empty array if no shares were deleted
     * @throws OXException
     */
    int[] deleteTargets(int contextID, List<ShareTarget> targets, StorageParameters parameters) throws OXException;

    /**
     * Deletes multiple shares targets associated to any guest user from the storage.
     *
     * @param contextID The context ID
     * @param shares The share targets to delete
     * @param includeItems <code>true</code> to remove item targets upon parent folder target removal, <code>false</code>, otherwise
     * @param parameters The storage parameters
     * @return The identifiers of the affected guest users, or an empty array if no shares were deleted
     */
    int[] deleteTargets(int contextID, List<ShareTarget> targets, boolean includeItems, StorageParameters parameters) throws OXException;

    /**
     * Counts the number of currently created shares for the given user id.
     *
     * @param contextID The context ID
     * @param userId The user Id to count for
     * @param parameters The storage parameters
     * @return The number of created shares for the user within the context
     * @throws OXException
     */
    int countShares(int contextID, int userId, StorageParameters parameters) throws OXException;

    //TODO: ownedBy and/or createdBy?
    List<Share> loadSharesCreatedBy(int contextID, int createdBy, StorageParameters parameters) throws OXException;

    //    int deleteShares(int contextID, List<ShareTarget> targets, int[] guests, StorageParameters parameters) throws OXException;
}
