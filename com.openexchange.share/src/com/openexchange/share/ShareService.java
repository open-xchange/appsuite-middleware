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

import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;

/**
 * {@link ShareService}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public interface ShareService {

    /**
     * Resolves the supplied token to a share.
     *
     * @param token The token to resolve
     * @return The share, or <code>null</code> if no valid share could be looked up
     * @throws OXException
     */
    Share resolveToken(String token) throws OXException;

    /**
     * Gets all shares.
     *
     * @return The shares
     * @throws OXException
     */
    List<Share> getAllShares() throws OXException;
    
    /**
     * Gets all shares created in the supplied context.
     *
     * @param contextId The contextId
     * @return The shares
     * @throws OXException
     */
    List<Share> getAllShares(int contextId) throws OXException;
    
    /**
     * Gets all shares created in the supplied context by supplied user.
     *
     * @param contextId The contextId
     * @param userId The userId
     * @return The shares
     * @throws OXException
     */
    List<Share> getAllShares(int contextId, int userId) throws OXException;
    
    /**
     * Gets all shares created by the supplied session's user.
     *
     * @param session The session
     * @return The shares
     * @throws OXException
     */
    List<Share> getAllShares(Session session) throws OXException;

    /**
     * Deletes shares for a specific folder that were bound to one of the supplied guest user IDs.
     *
     * @param session The session
     * @param folder The identifier of the folder to remove the shares for
     * @param module The module of the folder
     * @param guests The user identifiers of the guests
     * @return The identifiers of the guest users that have been removed through the removal of the share
     * @throws OXException
     */
    int[] deleteSharesForFolder(Session session, String folder, int module, int[] guests) throws OXException;

    /**
     * Creates shares for a specific folder for the supplied guest users.
     *
     * @param session The session
     * @param folder The identifier of the folder to remove the shares for
     * @param module The module of the folder
     * @param guests The guest users for the shares
     * @return The created shares, where each share corresponds to a guest user that has been added through the creation of the shares,
     *         in the same order as the supplied guests list
     */
    List<Share> addSharesToFolder(Session session, String folder, int module, List<Guest> guests) throws OXException;
    
    /**
     * Remove all shares identified by supplied tokens.
     * 
     * @param tokens The tokens
     * @throws OXException If removal fails
     */
    void removeShares(String[] tokens) throws OXException;
    
    /**
     * Remove all shares in supplied context.
     * 
     * @param contextId The contextId
     * @throws OXException If removal fails.
     */
    void removeShares(int contextId) throws OXException;
    
    /**
     * Remove all shares created by supplied user in supplied context.
     * 
     * @param contextId The contextId
     * @param userId The userId
     * @throws OXException If removal fails
     */
    void removeShares(int contextId, int userId) throws OXException;

}
