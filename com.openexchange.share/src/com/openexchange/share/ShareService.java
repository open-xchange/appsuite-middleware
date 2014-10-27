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
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;
import com.openexchange.share.recipient.AnonymousRecipient;
import com.openexchange.share.recipient.ShareRecipient;

/**
 * {@link ShareService}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public interface ShareService {

    /**
     * Resolves the supplied guest token a guest share, holding all accessible share targets from the guest user's point of view.
     *
     * @param token The token to resolve
     * @return The guest share, containing all shares the user has access to, or <code>null</code> if no valid share could be looked up
     * @throws OXException
     */
    GuestShare resolveToken(String token) throws OXException;

    /**
     * Adds multiple targets to the shares of guest users. Initial shares for each individual recipient are created implicitly as needed.
     *
     * @param session The session
     * @param targets The share targets to add
     * @param recipients The recipients for the shares
     * @return The new or updated shares, where each share corresponds to a recipient, in the same order as the supplied recipient list
     * @throws OXException
     */
    List<GuestShare> addTargets(Session session, List<ShareTarget> targets, List<ShareRecipient> recipients) throws OXException;

    /**
     * Deletes a list of share targets for all shares that belong to a certain list of guests.
     *
     * @param session The session
     * @param targets The share targets to delete
     * @param guestIDs The guest IDs to consider, or <code>null</code> to delete all shares of all guests referencing the targets
     * @throws OXException
     */
    void deleteTargets(Session session, List<ShareTarget> targets, List<Integer> guestIDs) throws OXException;

    /**
     * Updates multiple targets shared to a specific guest user. This currently includes updating the "meta" information or adjusting the
     * expiry date.
     *
     * @param session The session
     * @param targets The share targets to update
     * @param guestID The identifier of the guest user to update the share targets for
     * @param clientLastModified The time the associated shares were last read from the client to catch concurrent modifications
     * @return A guest share reflecting the updated shares for the guest user
     */
    GuestShare updateTargets(Session session, List<ShareTarget> targets, int guestID, Date clientLastModified) throws OXException;

    /**
     * Updates an anonymous share recipient. This currently only includes updating/setting the password for the guest user.
     *
     * @param session The session
     * @param guestID The identifier of the guest user to update the share target for
     * @param recipient The recipient definition to use for the update
     */
    void updateRecipient(Session session, int guestID, AnonymousRecipient recipient) throws OXException;

    /**
     * Gets all shares created by the supplied session's user.
     *
     * @param session The session
     * @return The shares
     * @throws OXException
     */
    List<Share> getAllShares(Session session) throws OXException;


    void deleteShares(Session session, List<Share> shares) throws OXException;


    /**
     * Updates an existing share.
     *
     * @param session The session
     * @param share The share to update
     * @param clientTimestamp The time the tokens were fetched to catch concurrent modifications
     * @return The update share
     * @throws OXException
     */
    void updateShare(Session session, Share share, Date clientTimestamp) throws OXException;

    /**
     * Generates a URL for every share that is passed.
     *
     * @param contextId The context ID
     * @param shares A list of shares
     * @param protocol The protocol to use (e.g. <code>http://</code>). If <code>null</code> <code>https://</code> is used.
     *                 You probably want to pass com.openexchange.tools.servlet.http.Tools.getProtocol() here.
     * @param fallbackHostname The hostname to use if no HostnameService is available.
     *                 You probably want to pass HttpServletRequest.getServerName() here.
     * @return A list of URLs, one for every share. The URLs are guaranteed to be in the same order as their according shares.
     */
    List<String> generateShareURLs(int contextId, List<Share> shares, String protocol, String fallbackHostname) throws OXException;

    String generateShareURL(int contextId, int guestId, int shareCreator, ShareTarget target, String protocol, String fallbackHostname) throws OXException;

    AuthenticationMode getAuthenticationMode(int contextId, int guestID) throws OXException;

}
