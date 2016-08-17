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

package com.openexchange.share;

import java.util.Date;
import java.util.List;
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
     * Resolves the guest associated to the given token.
     *
     * @param token - the token the GuestInfo should be resolved for
     * @return GuestInfo with information about the guest associated to the token or <code>null</code> if no guest user exists for the token
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
     * @param target The share target to add from the sharing users point of view
     * @param recipients The recipients for the shares
     * @return The created shares for each recipient, in the same order as the supplied recipient list
     */
    CreatedShares addTarget(Session session, ShareTarget target, List<ShareRecipient> recipients) throws OXException;

    /**
     * Gets an existing "anonymous" share link with read-only permissions for a specific target if one exists.
     *
     * @param session The session
     * @param target The share target from the session users point of view
     * @return The share link, or <code>null</code> if there is none
     */
    ShareLink optLink(Session session, ShareTarget target) throws OXException;

    /**
     * Gets or creates an "anonymous" share link with read-only permissions for a specific target.
     * <p/>
     * <b>Remarks:</b>
     * <ul>
     * <li>Permissions are checked based on the the session's user being able to update the referenced share target or not, throwing an
     * appropriate exception if the permissions are not sufficient</li>
     * </ul>
     *
     * @param session The session
     * @param target The share target from the session users point of view
     * @return The share link
     */
    ShareLink getLink(Session session, ShareTarget target) throws OXException;

    /**
     * Updates the certain properties of a specific "anonymous" share link.
     * <p/>
     * <b>Remarks:</b>
     * <ul>
     * <li>Permissions are checked based on the the session's user being able to update the referenced share target or not, throwing an
     * appropriate exception if the permissions are not sufficient</li>
     * </ul>
     *
     * @param session The session
     * @param target The share target from the session users point of view
     * @param linkUpdate The link update holding the updated properties
     * @param clientTimestamp The client timestamp to catch concurrent modifications
     * @return The share link
     */
    ShareLink updateLink(Session session, ShareTarget target, LinkUpdate linkUpdate, Date clientTimestamp) throws OXException;

    /**
     * Deletes an existing "anonymous" share link.
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
     * @param target The share to delete from the session users point of view
     * @param clientTimestamp The time the associated shares were last read from the client to catch concurrent modifications
     */
    void deleteLink(Session session, ShareTarget target, Date clientTimestamp) throws OXException;

    /**
     * Gets all users that shared something to specified guest.
     * <p/>
     * More concrete, gets the identifiers of all other user entities present in the permissions of all shared folders and items the
     * guest user has access to, i.e. the IDs of those users the guest user is allowed to to "see".
     *
     * @param contextID The context identifier
     * @param guestID The guest identifier
     * @return The identifiers from sharing users, or an empty set if there are none
     */
    Set<Integer> getSharingUsersFor(int contextID, int guestID) throws OXException;

    /**
     * Schedules guest cleanup tasks in a context.
     *
     * @param contextID The context ID
     * @param guestIDs The guest IDs to consider, or <code>null</code> to cleanup all guest users in the context
     */
    void scheduleGuestCleanup(int contextID, int...guestIDs) throws OXException;

}
