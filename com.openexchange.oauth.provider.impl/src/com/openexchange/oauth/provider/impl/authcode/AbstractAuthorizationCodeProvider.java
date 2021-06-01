/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 * 
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.oauth.provider.impl.authcode;

import org.apache.commons.lang.RandomStringUtils;
import com.openexchange.exception.OXException;
import com.openexchange.oauth.provider.resourceserver.scope.Scope;


/**
 * {@link AbstractAuthorizationCodeProvider}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public abstract class AbstractAuthorizationCodeProvider {

    /**
     * Initializes a new {@link AbstractAuthorizationCodeProvider}.
     */
    protected AbstractAuthorizationCodeProvider() {
        super();
    }

    public String generateAuthorizationCodeFor(String clientId, String redirectURI, Scope scope, int userId, int contextId) throws OXException {
        String authCode = generateAuthCode(userId, contextId);
        put(new AuthCodeInfo(authCode, clientId, redirectURI, scope, userId, contextId, System.currentTimeMillis()));
        return authCode;
    }

    public abstract AuthCodeInfo remove(String authCode) throws OXException;

    protected abstract void put(AuthCodeInfo authCodeInfo) throws OXException;

    /**
     * Generates the authorization code
     * 
     * @param userId The user identifier
     * @param contextId The context identifier
     */
    protected String generateAuthCode(int userId, int contextId) {
        return RandomStringUtils.randomAlphabetic(64);
    }


}
