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

package com.openexchange.tokenlogin;

import com.openexchange.exception.OXException;
import com.openexchange.session.Session;

/**
 * {@link TokenLoginService} - The token-login service.
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public interface TokenLoginService {

    /**
     * Acquires a unique token for specified session.
     * @param session The associated session
     *
     * @return The token as a string
     * @throws OXException If token cannot be generated for any reason
     */
    String acquireToken(Session session) throws OXException;

    /**
     * Redeems given token and generates an appropriate session.
     *
     * @param token The token previously generated
     * @param appSecret The secret identifier associated with requesting Web service/application
     * @param optClientIdentifier The optional client identifier
     * @param optAuthId The optional authentication identifier
     * @param optHash The optional hash value that applies to newly generated session
     * @param optClientIp The optional client IP address that applies to newly generated session
     * @return The generated session
     * @throws OXException If token cannot be turned into a valid session
     */
    Session redeemToken(String token, String appSecret, String optClientIdentifier, String optAuthId, String optHash, String optClientIp) throws OXException;

    /**
     * Gets the token-login secret (and its parameters) for specified secret identifier.
     *
     * @param secret The secret identifier
     * @return The associated token-login secret or <code>null</code> if there is none associated with given secret identifier
     */
    TokenLoginSecret getTokenLoginSecret(String secret);

}
