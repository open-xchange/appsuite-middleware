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

package com.openexchange.oauth.provider.impl.grant;

import java.util.Date;
import com.openexchange.oauth.provider.authorizationserver.grant.Grant;
import com.openexchange.oauth.provider.impl.authcode.AuthCodeInfo;
import com.openexchange.oauth.provider.resourceserver.scope.Scope;


/**
 * {@link OAuthGrantImpl}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class OAuthGrantImpl implements Grant {

    private final int contextId;
    private final int userId;
    private String accessToken;
    private String refreshToken;
    private Date expirationDate;
    private Scope scope;
    private String clientId;

    public OAuthGrantImpl(AuthCodeInfo authCodeInfo, String accessToken, String refreshToken, Date expirationDate, String clientId) {
        super();
        contextId = authCodeInfo.getContextId();
        userId = authCodeInfo.getUserId();
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expirationDate = expirationDate;
        scope = authCodeInfo.getScope();
        this.clientId = clientId;
    }

    public OAuthGrantImpl(StoredGrant storedGrant) {
        super();
        contextId = storedGrant.getContextId();
        userId = storedGrant.getUserId();
        accessToken = storedGrant.getAccessToken().getToken();
        refreshToken = storedGrant.getRefreshToken().getToken();
        expirationDate = storedGrant.getExpirationDate();
        scope = storedGrant.getScope();
        clientId = storedGrant.getClientId();
    }

    /**
     * Copy constructor
     */
    public OAuthGrantImpl(Grant grant) {
        super();
        contextId = grant.getContextId();
        userId = grant.getUserId();
        accessToken = grant.getAccessToken();
        refreshToken = grant.getRefreshToken();
        expirationDate = grant.getExpirationDate();
        scope = grant.getScope();
        clientId = grant.getClientId();
    }

    @Override
    public int getContextId() {
        return contextId;
    }

    @Override
    public int getUserId() {
        return userId;
    }

    @Override
    public String getAccessToken() {
        return accessToken;
    }

    @Override
    public String getRefreshToken() {
        return refreshToken;
    }

    @Override
    public Date getExpirationDate() {
        return expirationDate;
    }

    @Override
    public Scope getScope() {
        return scope;
    }

    @Override
    public String getClientId() {
        return clientId;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    public void setScope(Scope scope) {
        this.scope = scope;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

}
