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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.oauth.provider.OAuthInvalidTokenException.Reason;


/**
 * {@link SimOAuthProvider}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class SimOAuthProvider implements OAuthProviderService {

    private final Map<String, OAuthToken> tokens = new HashMap<>();

    @Override
    public OAuthToken validate(String accessToken) throws OXException {
        OAuthToken token = tokens.get(accessToken);
        if (token == null) {
            throw new OAuthInvalidTokenException(Reason.TOKEN_UNKNOWN);
        }

        if (new Date().after(token.getExpirationDate())) {
            throw new OAuthInvalidTokenException(Reason.TOKEN_EXPIRED);
        }

        return token;
    }

    public void addToken(OAuthToken token) {
        tokens.put(token.getAccessToken(), token);
    }

    public void removeToken(String accessToken) {
        tokens.remove(accessToken);
    }

    @Override
    public String generateToken(int contextId, int userId, Scope scope) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String generateAuthToken(int contextId, int userId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Client getClient(OAuthToken token) throws OXException {
        return new Client() {

            @Override
            public String getName() {
                return "Example App";
            }

            @Override
            public String getId() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public String getDescription() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public String getSecret() {
                // TODO Auto-generated method stub
                return null;
            }

        };
    }

    // -----------------------------------------------------------------------------------------------------

    @Override
    public boolean validateClientId(String clientId) throws OXException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean validateRedirectUri(String clientId, String redirectUri) throws OXException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Scope validateScope(String scope) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Client getClientByID(String clientID) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

}
