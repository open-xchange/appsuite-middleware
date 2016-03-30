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

package com.openexchange.share.impl.mbean;

import com.openexchange.exception.OXException;


/**
 * {@link ShareMBean}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.8.0
 */
public interface ShareMBean {

    public final static String DOMAIN = "com.openexchange.share";

    /**
     * Lists all shares in supplied context.
     *
     * @param contextId The contextId
     * @return The shares
     * @throws OXException On error
     */
    String listShares(int contextId) throws OXException;

    /**
     * Lists all shares in supplied context for the supplied guest user id.
     *
     * @param contextId The contextId
     * @param guestId The guest user id
     * @return The shares
     * @throws OXException On error
     */
    String listShares(int contextId, int guestId) throws OXException;

    /**
     * List share identified by supplied token
     *
     * @param token The token
     * @return The share
     * @throws OXException On error
     */
    String listShares(String token) throws OXException;

    /**
     * Removes all targets identified by supplied token.
     * @param token The token
     * @param path The share path
     * @throws OXException
     */
    int removeShare(String token, String path) throws OXException;

    /**
     * Removes all targets in supplied context identified by supplied token.
     * @param shareToken The token
     * @param targetPath The share path
     * @param contextId The contextId
     * @throws OXException
     */
    int removeShare(String shareToken, String targetPath, int contextId) throws OXException;

    /**
     * Remove all shares from supplied context.
     *
     * @param contextId The contextId
     * @throws OXException On error
     */
    int removeShares(int contextId) throws OXException;

    /**
     * Removes all shares in supplied context for the supplied guest user.
     *
     * @param contextId The contextId
     * @param guestId The guest user id
     * @throws OXException On error
     */
    int removeShares(int contextId, int guestId) throws OXException;

}
