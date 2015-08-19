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

package com.openexchange.share;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;
import com.openexchange.share.recipient.ShareRecipient;

/**
 * {@link ShareService}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public interface ShareService {

    /**
     * Resolves the supplied guest token to a guest share, holding all accessible share targets from the guest user's point of view.
     *
     * @param token The guest users base token
     * @return The guest share, containing all shares the user has access to, or <code>null</code> if no valid share could be looked up
     * @throws OXException If the passed token is invalid (i.e. malformed or does not match the encoded guest user) {@link ShareExceptionCodes#INVALID_TOKEN}
     * is thrown.
     */
    GuestShare resolveToken(String token) throws OXException;

    /**
     * Resolves the guest associated to the given token.
     *
     * @param token - the token the GuestInfo should be resolved for
     * @return GuestInfo with information about the guest associated to the token
     * @throws OXException If the passed token is invalid (i.e. malformed or does not match the encoded guest user) {@link ShareExceptionCodes#INVALID_TOKEN}
     * is thrown.
     */
    GuestInfo resolveGuest(String token) throws OXException;

    /**
     * Gets the guest info for the given user identifier. If no guest user is found, <code>null</code> is returned.
     *
     * @param session The session
     * @param guestID The user identifier of the guest
     * @return The guest info, or <code>null</code> if no guest user with this identifier was found
     */
    GuestInfo getGuestInfo(Session session, int guestID) throws OXException;

    /**
     * Resolves the supplied token and path to a single share. If the session's user is the guest user behind the base token himself, the
     * share target is adjusted implicitly for the guest user's point of view. Otherwise, the share target is kept in its original form
     * (i.e. not personalized for the guest user).
     *
     * @param session The session
     * @param token The token to resolve
     * @param path The path to the share target
     * @return The share info, or <code>null</code> if no valid share could be looked up
     * @throws OXException If the passed token is invalid (i.e. malformed or does not match the encoded guest user) {@link ShareExceptionCodes#INVALID_TOKEN}
     * is thrown.
     */
    ShareInfo getShare(Session session, String token, String path) throws OXException;

    /**
     * Resolves the supplied token and path to a single share.
     *
     * @param token The token to resolve
     * @param path The path to the share target
     * @return The share
     * @throws OXException On error
     */
    List<ShareInfo> getShare(String token, String path) throws OXException;

    /**
     * Adds a single target to the shares of guest users. Guest users for each individual recipient are created implicitly as needed.
     * <p/>
     * <b>Remarks:</b>
     * <ul>
     * <li>Associated permissions of the guest users on the share target are not updated implicitly, so that it's up to the
     * caller to take care of the referenced share target on his own</li>
     * <li>No permissions checks are performed, especially regarding the session's user being able to update the referenced share target
     * or not, so again it's up to the caller to perform the necessary checks</li>
     * </ul>
     *
     * @param session The session
     * @param target The share target to add
     * @param recipients The recipients for the shares
     * @return The created shares for each recipient, in the same order as the supplied recipient list
     */
    CreatedShares addTarget(Session session, ShareTarget target, List<ShareRecipient> recipients) throws OXException;

    /**
     * Adds a single target to the shares of guest users. Guest users for each individual recipient are created implicitly as needed.
     * <p/>
     * <b>Remarks:</b>
     * <ul>
     * <li>Associated permissions of the guest users on the share target are not updated implicitly, so that it's up to the
     * caller to take care of the referenced share target on his own</li>
     * <li>No permissions checks are performed, especially regarding the session's user being able to update the referenced share target
     * or not, so again it's up to the caller to perform the necessary checks</li>
     * <li>If specified, the metadata is is stored for each created share (i.e. for each recipient). If different metadata is required
     * for different recipients, this method should be invoked multiple times.</li>
     * </ul>
     *
     * @param session The session
     * @param target The share target to add
     * @param recipients The recipients for the shares
     * @param meta Additional metadata to store along with the created share(s), or <code>null</code> if not needed
     * @return The created shares for each recipient, in the same order as the supplied recipient list
     */
    CreatedShares addTarget(Session session, ShareTarget target, List<ShareRecipient> recipients, Map<String, Object> meta) throws OXException;

    /**
     * Adds a share to a single target for a specific recipient. An appropriate guest user is created implicitly as needed.
     * <p/>
     * <b>Remarks:</b>
     * <ul>
     * <li>Associated permissions of the guest user on the share target are updated implicitly via corresponding target proxies
     * automatically</li>
     * <li>Permissions checks are performed implicitly during the update of the referenced target</li>
     * </ul>
     *
     * @param session The session
     * @param target The share target to add
     * @param recipient The recipient for the share
     * @return The created share
     */
    CreatedShare addShare(Session session, ShareTarget target, ShareRecipient recipient, Map<String, Object> meta) throws OXException;

    /**
     * Deletes a list of share targets for all shares that belong to a certain list of guests.
     * <p/>
     * <b>Remarks:</b>
     * <ul>
     * <li>Associated permissions of the guest users on the share targets are not updated implicitly, so that it's up to the
     * caller to take care of the referenced share targets on his own</li>
     * <li>No permissions checks are performed, especially regarding the session's user being able to update the referenced share targets
     * or not, so again it's up to the caller to perform the necessary checks</li>
     * <li>Shares targeting items located in a folder whose target is deleted are not removed implicitly; this can be achieved via
     * {@link #deleteTargets(Session, List, List, boolean)}</li>
     * </ul>
     *
     * @param session The session
     * @param targets The share targets to delete
     * @param guestIDs The guest IDs to consider, or <code>null</code> to delete all shares of all guests referencing the targets
     */
    void deleteTargets(Session session, List<ShareTarget> targets, List<Integer> guestIDs) throws OXException;

    /**
     * Deletes a list of share targets for all shares of to any guest user, optionally including item targets in case the parent folder
     * target is removed.
     * <p/>
     * <b>Remarks:</b>
     * <ul>
     * <li>Associated permissions of the guest users on the share targets are not updated implicitly, so that it's up to the
     * caller to take care of the referenced share targets on his own</li>
     * <li>No permissions checks are performed, especially regarding the session's user being able to update the referenced share targets
     * or not, so again it's up to the caller to perform the necessary checks</li>
     * </ul>
     *
     * @param session The session
     * @param targets The share targets to delete
     * @param includeItems <code>true</code> to remove item targets upon parent folder target removal, <code>false</code>, otherwise
     */
    void deleteTargets(Session session, List<ShareTarget> targets, boolean includeItems) throws OXException;

    /**
     * Removes all shares identified by the supplied tokens. The tokens might either be in their absolute format (i.e. base token plus
     * path), as well as in their base format only, which in turn leads to all share targets associated with the base token being
     * removed.
     * <p/>
     * <b>Remarks:</b>
     * <ul>
     * <li>Associated guest permission entities from the referenced share targets are removed implicitly, so there's no need to take care
     * of those for the caller</li>
     * <li>Since the referenced share targets are are updated accordingly, depending permissions checks are performed, especially
     * regarding the session's user being able to update the referenced share targets or not, throwing an appropriate exception if the
     * permissions are not sufficient</li>
     * </ul>
     *
     * @param session The session
     * @param tokens The tokens to delete the shares for
     */
    void deleteShares(Session session, List<String> tokens) throws OXException;

    /**
     * Deletes a share.
     * <p/>
     * <b>Remarks:</b>
     * <ul>
     * <li>Associated guest permission entities from the referenced share targets are removed implicitly, so there's no need to take care
     * of those for the caller</li>
     * <li>Since the referenced share targets are updated accordingly, depending permissions checks are performed, especially
     * regarding the session's user being able to update the referenced share targets or not, throwing an appropriate exception if the
     * permissions are not sufficient</li>
     * </ul>
     *
     * @param session The session
     * @param share The share to delete
     * @param clientTimestamp The time the associated shares were last read from the client to catch concurrent modifications
     */
    void deleteShare(Session session, Share share, Date clientTimestamp) throws OXException;

    /**
     * Updates certain properties of a specific share. This currently includes the expiry date and the arbitrary meta-field.
     * <p/>
     * <b>Remarks:</b>
     * <ul>
     * <li>Permissions are checked based on the the session's user being able to update the referenced share target or not, throwing an
     * appropriate exception if the permissions are not sufficient</li>
     * <li>The supplied share must contain the target, as well as the referenced guest identifier</li>
     * <li>Only modified properties are updated, i.e. those properties where the {@link Share#containsXXX}</li>-methods return
     * <code>true</code>
     * </ul>
     *
     * @param session The session
     * @param share The share to update, with only modified fields being set
     * @param clientTimestamp The time the associated shares were last read from the client to catch concurrent modifications
     * @return A share info representing the updated share
     */
    ShareInfo updateShare(Session session, Share share, Date clientTimestamp) throws OXException;

    /**
     * Updates certain properties of a specific "anonymous" share. This currently includes the expiry date and the arbitrary meta-field,
     * and/or the password of the share's guest user.
     * <p/>
     * <b>Remarks:</b>
     * <ul>
     * <li>Permissions are checked based on the the session's user being able to update the referenced share target or not, throwing an
     * appropriate exception if the permissions are not sufficient</li>
     * <li>The supplied share must contain the target, as well as the referenced guest identifier</li>
     * <li>Only modified properties are updated, i.e. those properties where the {@link Share#containsXXX}</li>-methods return
     * <code>true</code></li>
     * <li>This method should only be called when updating the password of the anonymous guest behind the share - passing
     * <code>null</code> as password will remove it!</li>
     * </ul>
     *
     * @param session The session
     * @param share The share to update, with only modified fields being set
     * @param password The password to set for the anonymous guest user, or <code>null</code> to remove the password protection
     * @param clientTimestamp The time the associated shares were last read from the client to catch concurrent modifications
     * @return A share info representing the updated share
     */
    ShareInfo updateShare(Session session, Share share, String password, Date clientTimestamp) throws OXException;

    /**
     * Gets all shares that were created by the supplied session's user.
     *
     * @param session The session
     * @return The shares, or an empty list if there are none.
     */
    List<ShareInfo> getAllShares(Session session) throws OXException;

    /**
     * Gets all shares that were created for the specified module by the supplied session's user.
     *
     * @param session The session
     * @param module The module
     * @return The shares, or an empty list if there are none.
     */
    List<ShareInfo> getAllShares(Session session, String module) throws OXException;

    /**
     * Gets all shares for a specific target.
     *
     * @param session The session
     * @param module The module
     * @param folder The folder
     * @param item The item, or <code>null</code> if not applicable
     * @return The shares, or an empty list if there are none
     */
    List<ShareInfo> getShares(Session session, String module, String folder, String item) throws OXException;

    /**
     * Gets all users that shared something to specified guest.
     *
     * @param contextId The context identifier
     * @param guestId The guest identifier
     * @return The identifiers from sharing users or an empty set
     */
    Set<Integer> getSharingUsersFor(int contextId, int guestId) throws OXException;

}
