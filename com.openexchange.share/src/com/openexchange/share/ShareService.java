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
     * @param token The token to resolve
     * @return The guest share, containing all shares the user has access to, or <code>null</code> if no valid share could be looked up
     */
    GuestShare resolveToken(String token) throws OXException;

    /**
     * Resolves the supplied base token to a list of shares, holding all accessible share targets in their original from (i.e. not
     * personalized for the guest user).
     *
     * @param token The token to resolve
     * @return A list of shares the guest user behind the token has access to, or <code>null</code> if no valid share could be looked up
     */
    List<ShareInfo> getShares(Session session, String token) throws OXException;

    /**
     * Resolves the supplied token and path to a single share in its original from (i.e. not personalized for the guest user).
     *
     * @param token The token to resolve
     * @param path The path to the share target
     * @return The share info, or <code>null</code> if no valid share could be looked up
     */
    ShareInfo getShare(Session session, String token, String path) throws OXException;

    /**
     * Adds multiple targets to the shares of guest users. Guest users for each individual recipient are created implicitly as needed.
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
     * @param targets The share targets to add
     * @param recipients The recipients for the shares
     * @return The created shares for each recipient, where each share corresponds to a target, in the same order as the supplied target list
     */
    Map<ShareRecipient, List<ShareInfo>> addTargets(Session session, List<ShareTarget> targets, List<ShareRecipient> recipients) throws OXException;

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
    List<ShareInfo> addTarget(Session session, ShareTarget target, List<ShareRecipient> recipients) throws OXException;

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
     * Updates multiple targets shared to a specific guest user. This currently includes updating the "meta" information or adjusting the
     * expiry date.
     *
     * @param session The session
     * @param targets The share targets to update
     * @param guestID The identifier of the guest user to update the share targets for
     * @param clientTimestamp The time the associated shares were last read from the client to catch concurrent modifications
     * @return A guest share reflecting the updated shares for the guest user
     */
    GuestShare updateTargets(Session session, List<ShareTarget> targets, int guestID, Date clientTimestamp) throws OXException;

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
     * Gets all shares that were created by the supplied session's user.
     *
     * @param session The session
     * @return The shares, or an empty list if there are none.
     */
    List<ShareInfo> getAllShares(Session session) throws OXException;

    /**
     * Gets all users that shared something to specified guest.
     *
     * @param contextId The context identifier
     * @param guestId The guest identifier
     * @return The identifiers from sharing users or an empty set
     */
    Set<Integer> getSharingUsersFor(int contextId, int guestId) throws OXException;

    /**
     * Resolves the guest associated to the given token.
     *
     * @param token - the token the GuestInfo should be resolved for
     * @return GuestInfo with information about the guest associated to the token
     * @throws OXException
     */
    GuestInfo resolveGuest(String token) throws OXException;

}
