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

package com.openexchange.rest.client.session;

import com.openexchange.rest.client.session.pair.AccessTokenPair;
import com.openexchange.rest.client.session.pair.AppKeyPair;
import com.openexchange.rest.client.session.pair.ConsumerPair;

/**
 * {@link WebAuthSession}
 * 
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
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
     * Creates a new web auth session with the given app key pair and access token pair. The session will be linked to the account
     * corresponding to the given access token pair.
     */
    public WebAuthSession(AppKeyPair appKeyPair, AccessTokenPair accessTokenPair) {
        super(appKeyPair, accessTokenPair);
    }

    /**
     * Initializes a new {@link WebAuthSession} with the specified {@link ConsumerPair}. The session will be used to create an account to
     * the remote system, based on the OX account (upsell).
     * 
     * @param appKeyPair the appKeyPair
     * @param consumerPair the ConsumerPair
     */
    public WebAuthSession(AppKeyPair appKeyPair, ConsumerPair consumerPair) {
        super(appKeyPair, consumerPair);
    }

}
