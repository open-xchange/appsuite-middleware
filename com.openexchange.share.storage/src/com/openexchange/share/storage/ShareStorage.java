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
import com.openexchange.exception.OXException;
import com.openexchange.share.Share;


/**
 * {@link ShareStorage}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.6.1
 */
public interface ShareStorage {

    /**
     * Loads a share identified by it's unique token.
     *
     * @param contextID The context ID
     * @param token The token
     * @param parameters The storage parameters
     * @return The share, or <code>null</code> if not found
     * @throws OXException
     */
    Share loadShare(int contextID, String token, StorageParameters parameters) throws OXException;

    /**
     * Saves a new share in the storage.
     *
     * @param share The share to store
     * @param parameters The storage parameters
     */
    void storeShare(Share share, StorageParameters parameters) throws OXException;

    /**
     * Updates an already existing share in the storage.
     *
     * @param share The share to update
     * @param parameters The storage parameters
     */
    void updateShare(Share share, StorageParameters parameters) throws OXException;

    /**
     * Deletes a share identified by its unique token.
     *
     * @param contextID The context ID
     * @param token The token
     * @param parameters The storage parameters
     * @throws OXException
     */
    void deleteShare(int contextID, String token, StorageParameters parameters) throws OXException;

    /**
     * Deletes multiple shares identified by their unique token.
     *
     * @param contextID The context ID
     * @param tokens The tokens
     * @param parameters The storage parameters
     * @throws OXException
     */
    void deleteShares(int contextID, List<String> tokens, StorageParameters parameters) throws OXException;

    /**
     * Loads all shares that were created by a specific user ID.
     *
     * @param contextID The context ID
     * @param createdBy The ID of the user to load the shares from
     * @param parameters The storage parameters
     * @return The shares
     */
    List<Share> loadSharesCreatedBy(int contextID, int createdBy, StorageParameters parameters) throws OXException;

    /**
     * Loads all shares identified by their tokens.
     *
     * @param contextID The context ID
     * @param tokens The tokens of the shares to load
     * @param parameters The storage parameters
     * @return The shares
     */
    List<Share> loadShares(int contextID, List<String> tokens, StorageParameters parameters) throws OXException;

    /**
     * Loads all shares that were created for a specific folder.
     *
     * @param contextID The context ID
     * @param folder The ID of the folder to load the shares for
     * @param parameters The storage parameters
     * @return The shares
     */
    List<Share> loadSharesForFolder(int contextID, String folder, StorageParameters parameters) throws OXException;

    /**
     * Loads all shares that were created for a specific item.
     *
     * @param contextID The context ID
     * @param folder The ID of the folder where the item is located in
     * @param item The ID of the item to load the shares for
     * @param parameters The storage parameters
     * @return The shares
     */
    List<Share> loadSharesForItem(int contextID, String folder, String item, StorageParameters parameters) throws OXException;

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
     * Loads all shares that were created for a specific guest user ID.
     *
     * @param contextID The context ID
     * @param guestID The ID of the guest user to whom the share is for
     * @param parameters The storage parameters
     * @return The shares
     */
    List<Share> loadSharesForGuest(int contextID, int guestID, StorageParameters parameters) throws OXException;

    /**
     * Loads all shares that were created in supplied context.
     *
     * @param contextID The contextId
     * @param parameters The storage parameters
     * @return The shares
     * @throws OXException On error
     */
    List<Share> loadSharesForContext(int contextID, StorageParameters parameters) throws OXException;

}
