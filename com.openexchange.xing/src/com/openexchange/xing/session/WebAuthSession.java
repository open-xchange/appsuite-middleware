/*-
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

package com.openexchange.xing.session;

import com.openexchange.xing.XingAPI;

/**
 * Keeps track of a logged in user contains configuration options for the {@link XingAPI}. This type of {@link Session} uses the web OAuth
 * flow to authenticate users:
 * <ol>
 * <li>A request token + secret and redirect URL are retrieved using {@link WebAuthSession#getAuthInfo()} or
 * {@link WebAuthSession#getAuthInfo(String)}.</li>
 * <li>You store the request token + secret, and redirect the user to the redirect URL where they will authenticate with XING and grant your
 * app permission to access their account.</li>
 * <li>XING will redirect back to your site if it was provided a URL to do so (otherwise, you have to ask the user when he/she is done).</li>
 * <li>The user's access token + secret are set on this session when you call
 * {@link WebAuthSession#retrieveWebAccessToken(RequestTokenPair)} with the previously-saved request token + secret. You have a limited
 * amount of time to make this call or the request token will expire.</li>
 * </ol>
 */
public class WebAuthSession extends AbstractSession {

    /**
     * Creates a new web auth session with the given app key pair and access type. The session will not be linked because it has no access
     * token or secret.
     */
    public WebAuthSession(AppKeyPair appKeyPair) {
        super(appKeyPair);
    }

    /**
     * Creates a new web auth session with the given app key pair and access type. The session will be linked to the account corresponding
     * to the given access token pair.
     */
    public WebAuthSession(AppKeyPair appKeyPair, AccessTokenPair accessTokenPair) {
        super(appKeyPair, accessTokenPair);
    }
    
    /**
     * Initializes a new {@link WebAuthSession} with the specified {@link ConsumerPair}. The session will be used to create a Xing
     * profile, based on the OX account (upsell).
     * 
     * @param appKeyPair the appKeyPair
     * @param consumerPair the ConsumerPair
     */
    public WebAuthSession(AppKeyPair appKeyPair, ConsumerPair consumerPair) {
        super(appKeyPair, consumerPair);
    }
    

}
