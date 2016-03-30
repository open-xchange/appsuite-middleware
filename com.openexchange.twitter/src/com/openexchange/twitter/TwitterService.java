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

package com.openexchange.twitter;

import com.openexchange.exception.OXException;

/**
 * {@link TwitterService} - The <a href="http://twitter.com/">twitter</a> service.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface TwitterService {

    /**
     * Gets the access token for specified user credentials.
     *
     * @param twitterId The twitter id
     * @param password The twitter password
     * @return The access token for specified user credentials
     * @throws OXException If OAuth twitter access token cannot be returned
     */
    public TwitterAccessToken getTwitterAccessToken(String twitterId, String password) throws OXException;

    /**
     * Gets the OAuth twitter access instance for the authenticating user.
     *
     * @param token The twitter token
     * @param tokenSecret The twitter token secret
     * @return The authenticated twitter access
     * @throws OXException If OAuth twitter access cannot be returned
     * @see #getTwitterAccessToken(String, String)
     */
    public TwitterAccess getOAuthTwitterAccess(String token, String tokenSecret) throws OXException;

    /**
     * Gets the twitter access instance for the authenticating user.
     *
     * @param twitterId The twitter id
     * @param password The twitter password
     * @return The authenticated twitter access
     * @deprecated Use {@link #getOAuthTwitterAccess(String, String)} instead
     */
    @Deprecated
    public TwitterAccess getTwitterAccess(String twitterId, String password);

    /**
     * Gets an unauthenticated twitter access instance.
     *
     * @return An unauthenticated twitter access
     */
    public TwitterAccess getUnauthenticatedTwitterAccess();

    /**
     * Creates a new instance of {@link Paging}.
     *
     * @return A new instance of {@link Paging}
     */
    public Paging newPaging();

}
