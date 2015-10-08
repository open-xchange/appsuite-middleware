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

package com.openexchange.oauth.provider;

import com.openexchange.exception.OXException;
import com.openexchange.oauth.provider.client.Client;
import com.openexchange.oauth.provider.exceptions.OAuthInvalidTokenException;
import com.openexchange.oauth.provider.grant.OAuthGrant;
import com.openexchange.osgi.annotation.SingletonService;


/**
 * The service to be used by OAuth resources (i.e. HTTP API actions) to interact with the OAuth
 * provider component.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 * @see OAuthProviderService
 */
@SingletonService
public interface OAuthResourceService {

    /**
     * Validates the given access token and returns an according {@link OAuthGrant} instance.
     *
     * @param accessToken The access token
     * @return The grant
     * @throws OXException If the token is invalid {@link OAuthInvalidTokenException} is thrown
     */
    OAuthGrant validate(String accessToken) throws OXException;

    /**
     * Gets the client for a given grant.
     *
     * @param grant The grant
     * @return The client
     * @throws OXException If no client could be found (e.g. it has been deleted in the meantime) {@link OAuthInvalidTokenException} is thrown
     */
    Client getClient(OAuthGrant grant) throws OXException;

}
