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
 *     Copyright (C) 2004-2015 Open-Xchange, Inc.
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

package com.openexchange.oauth.provider.internal.grant;

import com.openexchange.exception.OXException;
import com.openexchange.oauth.provider.tools.UserizedToken;


/**
 * {@link OAuthGrantStorage}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public interface OAuthGrantStorage {
    
    /**
     * The max. number of grants a client may occupy for one user
     */
    public static final int MAX_GRANTS_PER_CLIENT = 10;

    /**
     * Persists the given grant. If the number of existing grants for the according
     * client-user-combination is equals {@link #MAX_GRANTS_PER_CLIENT}, the oldest
     * grant is removed to enforce this limit.
     *
     * @param grant The grant to persist
     * @throws OXException When saving fails
     */
    public void persistGrant(StoredGrant grant) throws OXException;

    public void deleteGrantsForClient(String clientId) throws OXException;

    public StoredGrant getGrantByAccessToken(UserizedToken accessToken) throws OXException;

    public StoredGrant getGrantByRefreshToken(UserizedToken refreshToken) throws OXException;

//    public int countAllGrants(int contextId, int userId) throws OXException;
//
    public int countGrantsForClient(String clientId, int contextId, int userId) throws OXException;

    /**
     * Counts all grants for distinct clients of a given user.
     *
     * @param contextId The context ID
     * @param userId The user ID
     * @return The number of grants (>= 0)
     */
    public int countDistinctGrants(int contextId, int userId);

}
